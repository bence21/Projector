package projector.network;

import com.bence.projector.common.dto.ProjectionDTO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.ProjectionScreenController;
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
    private static String openIp;

    public synchronized static void connectToShared(ProjectionScreenController projectionScreenController) {
        if (thread != null) {
            close();
        }
        thread = new Thread(() -> {
            try {
                Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
                List<String> ips = new ArrayList<>();
                while (enumeration.hasMoreElements()) {
                    NetworkInterface n = enumeration.nextElement();
                    Enumeration<InetAddress> ee = n.getInetAddresses();
                    while (ee.hasMoreElements()) {
                        InetAddress i = ee.nextElement();
                        String hostAddress = i.getHostAddress();
                        if (hostAddress.matches("192.168.[12]?[0-9]{1,2}.[12]?[0-9]{1,2}")) {
                            ips.add(hostAddress);
                            System.out.println(hostAddress);
                        }
                    }
                }
                openIp = null;
                List<Thread> threads = new ArrayList<>(ips.size() * 255);
                for (String ip : ips) {
                    String[] split = ip.split("\\.");
                    String firstThree = split[0] + "." + split[1] + "." + split[2] + ".";
                    for (int i = 1; i <= 255; ++i) {
                        String ip1 = firstThree + i;
                        Thread thread = new Thread(() -> {
                            if (isOpenAddress(ip1)) {
                                System.out.println("ip = " + ip1);
                                openIp = ip1;
                            }
                        });
                        thread.start();
                        threads.add(thread);
                    }
                }
                for (Thread thread : threads) {
                    thread.join(5000);
                }
//                openIp = "192.168.43.175";
                System.out.println("openIp = " + openIp);
                if (openIp != null) {
                    TCPImageClient.connectToShared(projectionScreenController, openIp);
                    clientSocket = new Socket(openIp, PORT);
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());

                    Settings settings = Settings.getInstance();
                    settings.setConnectedToShared(true);
                    reader = new Thread(() -> {
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
                                            projectionScreenController.setText(text, ProjectionType.valueOf(projectionTypeName), projectionDTO);
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
            text.append(getBibleVerseWithReferenceText(verseIndices, preferred,
                    projectionDTO.getSelectedBook(), projectionDTO.getSelectedPart(), projectionDTO.getVerseIndicesByPart()));
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
        thread.interrupt();
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
        close(outToServer, inFromServer, clientSocket, LOG, thread, null);
        TCPImageClient.closeInstance();
    }
}
