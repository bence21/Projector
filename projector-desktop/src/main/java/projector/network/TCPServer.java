package projector.network;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.ProjectionScreenController;
import projector.controller.song.SongController;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {

    private static final Logger LOG = LoggerFactory.getLogger(TCPServer.class);
    private static final List<Sender> senders = new ArrayList<>();
    private static Thread thread;
    private static Thread imageThread;
    private static boolean closed = false;
    private static final List<ServerSocket> welcomeSockets = new ArrayList<>();

    public synchronized static void startShareNetwork(ProjectionScreenController projectionScreenController, SongController songController) {
        Settings.getInstance().setShareOnNetwork(true);
        // Clear any previous error message
        Settings.getInstance().setNetworkSharingError(null);
        if (thread == null) {
            createThreads(projectionScreenController, songController);
        } else {
            close();
            createThreads(projectionScreenController, songController);
        }
        thread.start();
        imageThread.start();
    }

    private static void createThreads(ProjectionScreenController projectionScreenController, SongController songController) {
        thread = getSenderThread(projectionScreenController, songController, TCPClient.PORT, SenderType.TEXT);
        imageThread = getSenderThread(projectionScreenController, songController, TCPImageClient.PORT, SenderType.IMAGE);
    }

    private static Thread getSenderThread(ProjectionScreenController projectionScreenController, SongController songController, int port, SenderType senderType) {
        return new Thread(() -> {
            try {
                ServerSocket welcomeSocket = new ServerSocket(port);
                welcomeSockets.add(welcomeSocket);
                while (!closed) {
                    Socket connectionSocket = welcomeSocket.accept();
                    Sender sender = new Sender(connectionSocket, projectionScreenController, songController, senderType);
                    addSocket(sender);
                }
            } catch (BindException e) {
                handleBindException(e, senderType);
            } catch (SocketException e) {
                try {
                    if (e.getMessage().equalsIgnoreCase("socket closed")) {
                        return;
                    }
                } catch (Exception e1) {
                    LOG.error(e1.getMessage(), e1);
                }
                LOG.error(e.getMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private synchronized static void addSocket(Sender connectionSocket) {
        senders.add(connectionSocket);
    }

    public synchronized static void close() {
        closed = true;
        for (Sender sender : senders) {
            sender.stop();
        }
        interruptTread(thread);
        interruptTread(imageThread);
        for (ServerSocket welcomeSocket : welcomeSockets) {
            try {
                welcomeSocket.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static void interruptTread(Thread thread) {
        try {
            if (thread != null) {
                thread.interrupt();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void handleBindException(BindException e, SenderType senderType) {
        LOG.error("Address already in use: bind", e);
        Platform.runLater(() -> {
            Settings settings = Settings.getInstance();
            String errorMessage;
            if (senderType == SenderType.IMAGE) {
                // Image port failure is non-critical - text sharing can still work
                errorMessage = settings.getResourceBundle().getString("Image sharing port is unavailable, but text sharing is working.");
                // Don't stop network sharing for image port issues
            } else {
                // Text port failure is critical - stop network sharing
                settings.setShareOnNetwork(false);
                errorMessage = settings.getResourceBundle().getString("Port already in use. Another application may be using the required port. Please close it and try again.");
            }
            settings.setNetworkSharingError(errorMessage);
        });
    }
}
