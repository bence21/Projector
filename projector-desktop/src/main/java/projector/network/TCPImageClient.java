package projector.network;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.ProjectionScreenController;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import static projector.network.TCPClient.close;
import static projector.network.TCPClient.isUnknownException;

public class TCPImageClient {

    static final int PORT = 21042;
    private static final Logger LOG = LoggerFactory.getLogger(TCPImageClient.class);
    private static Thread thread;
    private static Thread reader;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static DataInputStream inFromServer;

    public synchronized static void connectToShared(ProjectionScreenController projectionScreenController, String openIp) {
        if (thread != null) {
            closeInstance();
        }
        thread = new Thread(() -> {
            try {
                if (openIp != null) {
                    clientSocket = new Socket(openIp, PORT);
                    InputStream inputStream = clientSocket.getInputStream();
                    inFromServer = new DataInputStream(inputStream);
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());

                    Settings settings = Settings.getInstance();
                    settings.setConnectedToShared(true);
                    reader = new Thread(() -> {
                        while (settings.isConnectedToShared()) {
                            try {
                                readImage(inFromServer, projectionScreenController);
                            } catch (SocketException e) {
                                if (isUnknownException(e)) {
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
            } catch (ConnectException e) {
                if (!e.getMessage().contains("refused")) {
                    LOG.error(e.getMessage(), e);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        thread.start();
    }

    private static void readImage(DataInputStream dataInputStream, ProjectionScreenController projectionScreenController) throws IOException {
        int imageLength = dataInputStream.readInt();
        // Read the byte array itself
        byte[] imageBytes = new byte[imageLength];
        dataInputStream.readFully(imageBytes, 0, imageLength);
        ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes);
        Image image = new Image(imageStream);
        projectionScreenController.drawImage(image);
    }

    static void closeInstance() {
        close(outToServer, null, clientSocket, LOG, thread, inFromServer);
    }
}
