package projector.network;

import com.bence.projector.common.dto.ProjectionDTO;
import com.google.gson.Gson;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.util.AutomaticAction;
import projector.controller.util.ProjectionData;
import projector.controller.util.ProjectionScreensUtil;
import projector.model.Bible;
import projector.model.VerseIndex;
import projector.service.ServiceManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import static projector.controller.BibleController.getBibleVerseWithReferenceText;
import static projector.controller.util.FileUtil.getGson;
import static projector.network.Sender.END_PROJECTION_DTO;
import static projector.network.Sender.START_PROJECTION_DTO;

public class TCPClient {

    static final int PORT = 21041;
    private static final Logger LOG = LoggerFactory.getLogger(TCPClient.class);
    private static Thread thread;
    private static Thread reader;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;
    private static final List<String> openIps = Collections.synchronizedList(new ArrayList<>());
    private static Thread autoConnectThread;
    private static volatile boolean autoConnectEnabled = false;

    private static List<String> getIps() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // Skip loopback interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    // Skip IPv6 addresses
                    if (inetAddress.getAddress().length == 4) {
                        String hostAddress = inetAddress.getHostAddress();
                        ips.add(hostAddress);
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        return ips;
    }

    private static void sortOpenIps() {
        openIps.sort((ip1, ip2) -> {
            boolean preferredIp1 = preferredIp(ip1);
            boolean preferredIp2 = preferredIp(ip2);
            if (preferredIp1 && !preferredIp2) {
                return -1;
            } else if (preferredIp2 && !preferredIp1) {
                return 1;
            }
            return ip1.compareTo(ip2);
        });
    }

    private static void printOpenIps() {
        for (String ip : openIps) {
            System.out.println(ip);
        }
    }

    private static boolean preferredIp(String ip) {
        return ip.matches("192.168.[12]?[0-9]{1,2}.[12]?[0-9]{1,2}");
    }

    private static void getOpenIps() throws InterruptedException {
        List<String> ips = getIps();
        List<Thread> threads = new ArrayList<>(ips.size() * 255);
        openIps.clear();
        for (String ip : ips) {
            String[] split = ip.split("\\.");
            String firstThree = split[0] + "." + split[1] + "." + split[2] + ".";
            for (int i = 1; i <= 255; ++i) {
                String ip1 = firstThree + i;
                Thread thread = new Thread(() -> {
                    if (isOpenAddress(ip1)) {
                        openIps.add(ip1);
                    }
                });
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join(5000);
        }
    }

    public synchronized static void connectToShared() {
        if (thread != null) {
            close();
        }
        thread = new Thread(() -> {
            try {
                getOpenIps();
                sortOpenIps();
                printOpenIps();
                String openIp;
                if (openIps.size() > 0) {
                    openIp = openIps.get(0);
                } else {
                    openIp = null;
                }
                System.out.println("openIp = " + openIp);
                if (openIp != null) {
                    TCPImageClient.connectToShared(openIp);
                    clientSocket = new Socket(openIp, PORT);
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());

                    Settings settings = Settings.getInstance();
                    settings.setConnectedToShared(true);
                    reader = new Thread(() -> {
                        ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
                        String fromServer;
                        while (settings.isConnectedToShared()) {
                            try {
                                fromServer = inFromServer.readLine();
                                if (fromServer == null) {
                                    close();
                                    return;
                                }
                                if (fromServer.equals("Finished")) {
                                    outToServer.close();
                                    outToServer = null;
                                    close();
                                    return;
                                }
                                if (fromServer.equals("start 'text'")) {
                                    String text = readTextToEndS(settings, "end 'text'");
                                    fromServer = inFromServer.readLine();
                                    ProjectionDTO projectionDTO = null;
                                    if (fromServer.equals(START_PROJECTION_DTO)) {
                                        projectionDTO = readProjectionDTO(settings);
                                        text = getTextFromProjectionDTO(projectionDTO, text);
                                        fromServer = inFromServer.readLine();
                                    }
                                    if (fromServer.equals("start 'projectionType'")) {
                                        String projectionTypeName = inFromServer.readLine();
                                        fromServer = inFromServer.readLine();
                                        if (fromServer.equals("end 'projectionType'")) {
                                            ProjectionType projectionType = ProjectionType.valueOf(projectionTypeName);
                                            ProjectionData projectionData = getProjectionData(projectionDTO);
                                            if (projectionType == ProjectionType.COUNTDOWN_TIMER && projectionDTO != null) {
                                                Date finishDate = projectionDTO.getFinishDate();
                                                if (finishDate != null) {
                                                    projectionScreensUtil.setCountDownTimer(null, finishDate, getAutomaticAction(projectionDTO), projectionDTO.isShowFinishTime());
                                                }
                                            } else {
                                                projectionScreensUtil.setText(text, projectionType, projectionData);
                                            }
                                        }
                                    }
                                }
                            } catch (SocketException e) {
                                if (!e.getMessage().contains("Socket closed")) {
                                    LOG.error(e.getMessage(), e);
                                }
                                break;
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                                break;
                            }
                        }
                    });
                    reader.start();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        thread.start();
    }

    private static AutomaticAction getAutomaticAction(ProjectionDTO projectionDTO) {
        return AutomaticAction.getFromOrdinal(projectionDTO.getSelectedAction());
    }

    private static ProjectionData getProjectionData(ProjectionDTO projectionDTO) {
        ProjectionData projectionData = new ProjectionData();
        projectionData.setProjectionDTO(projectionDTO);
        return projectionData;
    }

    private static String getTextFromProjectionDTO(ProjectionDTO projectionDTO, String originalText) {
        try {
            if (projectionDTO == null) {
                return originalText;
            }
            if (!Settings.getInstance().isForIncomingDisplayOnlySelected()) {
                return originalText;
            }
            StringBuilder text = new StringBuilder();
            List<Long> verseIndexIntegers = projectionDTO.getVerseIndices();
            List<VerseIndex> verseIndices = getFromIntegers(verseIndexIntegers);
            List<Bible> bibles = ServiceManager.getBibleService().findAll();
            Bible preferred = getPreferredBible(projectionDTO, bibles);
            if (preferred == null) {
                return originalText;
            }
            text.append(getBibleVerseWithReferenceText(verseIndices, preferred, projectionDTO.getSelectedBook(), projectionDTO.getSelectedPart(), projectionDTO.getVerseIndicesByPart()));
            String s = text.toString().trim();
            if (s.isEmpty()) {
                return originalText;
            }
            return s;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return originalText;
        }
    }

    private static Bible getPreferredBible(ProjectionDTO projectionDTO, List<Bible> bibles) {
        String selectedBibleUuid = projectionDTO.getSelectedBibleUuid();
        String selectedBibleName = projectionDTO.getSelectedBibleName();
        Bible preferred = null;
        for (Bible bible : bibles) {
            if (!bible.isPreferredByRemote()) {
                continue;
            }
            if (selectedBibleUuid != null) {
                if (selectedBibleUuid.equals(bible.getUuid())) {
                    return bible;
                }
            } else {
                if (selectedBibleName != null && selectedBibleName.equals(bible.getName())) {
                    return bible;
                }
            }
            if (preferred == null) {
                preferred = bible;
            } else if (preferred.getParallelNumber() > bible.getParallelNumber() && bible.isParallelSelected()) {
                preferred = bible;
            }
        }
        return preferred;
    }

    private static List<VerseIndex> getFromIntegers(List<Long> verseIndexIntegers) {
        int size = 0;
        if (verseIndexIntegers != null) {
            size = verseIndexIntegers.size();
        }
        List<VerseIndex> indices = new ArrayList<>(size);
        if (verseIndexIntegers == null) {
            return indices;
        }
        for (Long aLong : verseIndexIntegers) {
            VerseIndex verseIndex = new VerseIndex();
            verseIndex.setIndexNumber(aLong);
            indices.add(verseIndex);
        }
        return indices;
    }

    private static ProjectionDTO readProjectionDTO(Settings settings) throws IOException {
        String text = readTextToEndS(settings, END_PROJECTION_DTO);
        return getProjectionDTOFromJson(text);
    }

    private static String readTextToEndS(Settings settings, String endS) throws IOException {
        String fromServer;
        StringBuilder text = new StringBuilder(inFromServer.readLine());
        fromServer = inFromServer.readLine();
        while (settings.isConnectedToShared() && !fromServer.equals(endS)) {
            text.append("\n").append(fromServer);
            fromServer = inFromServer.readLine();
        }
        return text.toString();
    }

    private static ProjectionDTO getProjectionDTOFromJson(String json) {
        Gson gson = getGson();
        return gson.fromJson(json, ProjectionDTO.class);
    }

    private static boolean isOpenAddress(String ip) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, PORT), 2000);
            socket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public synchronized static void close(DataOutputStream outToServer, BufferedReader inFromServer, Socket clientSocket, Logger log, Thread thread, DataInputStream inFromServer2) {
        try {
            if (outToServer != null) {
                outToServer.writeBytes("Finished\n");
                outToServer.close();
            }
            if (inFromServer != null) {
                inFromServer.close();
            }
            if (inFromServer2 != null) {
                inFromServer2.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (SocketException e) {
            if (isUnknownException(e)) {
                log.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    public static boolean isUnknownException(SocketException e) {
        String message = e.getMessage();
        List<String> knownExceptions = new ArrayList<>();
        knownExceptions.add("Socket closed");
        knownExceptions.add("An established connection was aborted by the software in your host machine");
        for (String knownException : knownExceptions) {
            if (knownException.contains(message)) {
                return false;
            }
        }
        return true;
    }

    public static void close() {
        Settings settings = Settings.getInstance();
        boolean wasConnected = settings.isConnectedToShared();
        close(outToServer, inFromServer, clientSocket, LOG, thread, null);
        TCPImageClient.closeInstance();
        if (wasConnected) {
            settings.setConnectedToShared(false);
        }
    }

    private static ChangeListener<Boolean> connectionLossListener;

    private static void setupConnectionLossListener(Settings settings) {
        // Add listener for connection loss
        if (connectionLossListener == null) {
            connectionLossListener = (observable, oldValue, newValue) -> {
                // If connection is lost and auto-connect is enabled, restart the loop
                if (!newValue && oldValue && autoConnectEnabled) {
                    // Restart in a separate thread to avoid deadlock
                    new Thread(() -> {
                        // Stop current loop if running
                        synchronized (TCPClient.class) {
                            if (autoConnectThread != null && autoConnectThread.isAlive()) {
                                autoConnectThread.interrupt();
                            }
                        }
                        // Wait a bit for the thread to stop
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        // Start new loop
                        startAutoConnectLoop();
                    }).start();
                }
            };
            settings.connectedToSharedProperty().addListener(connectionLossListener);
        }
    }

    @SuppressWarnings("BusyWait")
    public synchronized static void startAutoConnectLoop() {
        if (autoConnectThread != null && autoConnectThread.isAlive()) {
            return;
        }
        autoConnectEnabled = true;
        Settings settings = Settings.getInstance();
        
        setupConnectionLossListener(settings);
        
        autoConnectThread = new Thread(() -> {
            while (autoConnectEnabled) {
                try {
                    if (!settings.isConnectedToShared()) {
                        Thread connectionThread;
                        synchronized (TCPClient.class) {
                            connectToShared();
                            connectionThread = thread;
                        }
                        // Wait for the connection attempt to finish (with timeout)
                        if (connectionThread != null) {
                            try {
                                connectionThread.join(30000); // Wait up to 30 seconds for connection attempt
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        // Check if connection was established
                        if (settings.isConnectedToShared()) {
                            break;
                        }
                    } else {
                        break;
                    }
                    // Wait 5 seconds before next attempt
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    LOG.error("Error in auto-connect loop: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
            synchronized (TCPClient.class) {
                autoConnectThread = null;
            }
        });
        autoConnectThread.setDaemon(true);
        autoConnectThread.start();
    }

    public synchronized static void stopAutoConnectLoop() {
        autoConnectEnabled = false;
        if (autoConnectThread != null) {
            autoConnectThread.interrupt();
            try {
                autoConnectThread.join(1000);
            } catch (InterruptedException ignored) {
            }
            autoConnectThread = null;
        }
        // Remove connection loss listener
        if (connectionLossListener != null) {
            Settings settings = Settings.getInstance();
            settings.connectedToSharedProperty().removeListener(connectionLossListener);
            connectionLossListener = null;
        }
    }
}
