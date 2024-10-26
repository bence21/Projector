package projector.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.song.SongController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class RemoteServer {

    private static final int PORT = 21042;
    private static final Logger LOG = LoggerFactory.getLogger(RemoteServer.class);
    private static Thread thread;
    private static boolean closed = false;
    private static final List<Sender> senders = new ArrayList<>();
    private static ServerSocket welcomeSocket;

    public synchronized static void startRemoteServer(SongController songController) {
        if (thread == null) {
            thread = new Thread(() -> {
                try {
                    welcomeSocket = new ServerSocket(PORT);
                    while (!closed) {
                        Socket connectionSocket = welcomeSocket.accept();
                        Sender sender = new Sender(connectionSocket, songController);
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
