package projector.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.ProjectionScreenController;
import projector.controller.song.SongController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {

    private static final Logger LOG = LoggerFactory.getLogger(TCPServer.class);
    private static Thread thread;
    private static boolean closed = false;
    private static final List<Sender> senders = new ArrayList<>();
    private static ServerSocket welcomeSocket;

    public synchronized static void startShareNetwork(ProjectionScreenController projectionScreenController, SongController songController) {
        Settings.getInstance().setShareOnNetwork(true);
        if (thread == null) {
            thread = new Thread(() -> {
                try {
                    welcomeSocket = new ServerSocket(TCPClient.PORT);
                    while (!closed) {
                        Socket connectionSocket = welcomeSocket.accept();
                        Sender sender = new Sender(connectionSocket, projectionScreenController, songController);
                        addSocket(sender);
                    }
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
        } else {
            close();
        }
        thread.start();
    }

    private synchronized static void addSocket(Sender connectionSocket) {
        senders.add(connectionSocket);
    }

    public synchronized static void close() {
        closed = true;
        for (Sender sender : senders) {
            sender.stop();
        }
        if (thread != null) {
            thread.interrupt();
            try {
                welcomeSocket.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }

        }
    }
}
