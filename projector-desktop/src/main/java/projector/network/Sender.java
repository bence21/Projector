package projector.network;

import com.bence.projector.common.dto.ProjectionDTO;
import com.google.gson.Gson;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.controller.ProjectionScreenController;
import projector.controller.ProjectionTextChangeListener;
import projector.controller.song.SongController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import static projector.controller.util.FileUtil.getGson;

public class Sender {

    public static final String START_PROJECTION_DTO = "start 'projectionDTO'";
    public static final String END_PROJECTION_DTO = "end 'projectionDTO'";
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
    private final Thread writer;
    private final DataOutputStream outToClient;
    private final Socket connectionSocket;
    private final Thread reader;
    private Thread thread;

    Sender(Socket connectionSocket, ProjectionScreenController projectionScreenController, SongController songController, SenderType senderType) throws IOException {
        this.connectionSocket = connectionSocket;
        outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        writer = new Thread(() -> {
            ProjectionTextChangeListener projectionTextChangeListener = new ProjectionTextChangeListener() {
                @Override
                public void onSetText(String text, ProjectionType projectionType, ProjectionDTO projectionDTO) {
                    if (senderType != SenderType.TEXT) {
                        return;
                    }
                    sendTextInThread(text, projectionType, projectionDTO, this, projectionScreenController);
                }

                @Override
                public void onImageChanged(Image image, ProjectionType projectionType, ProjectionDTO projectionDTO) {
                    if (senderType != SenderType.IMAGE) {
                        return;
                    }
                    sendImageInThread(image, this, projectionScreenController);
                }
            };
            projectionScreenController.addProjectionTextChangeListener(projectionTextChangeListener);
            songController.addProjectionTextChangeListener(projectionTextChangeListener);
        });
        writer.start();
        reader = new Thread(() -> {
            try {
                inFromClient.readLine();
                //                while (!s.equals("Finished")) {
                //                    s = inFromClient.readLine();
                //                }
                close();
            } catch (SocketException e) {
                if (e.getMessage().equals("Socket closed")) {
                    return;
                }
                LOG.error(e.getMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                close();
            }
        });
        reader.start();
    }

    private void sendTextInThread(String text, ProjectionType projectionType, ProjectionDTO projectionDTO,
                                  ProjectionTextChangeListener projectionTextChangeListener, ProjectionScreenController projectionScreenController) {
        waitPreviousThread();
        thread = new Thread(() -> {
            try {
                String s = "start 'text'\n"
                        + text + "\n"
                        + "end 'text'\n"
                        + START_PROJECTION_DTO + "\n"
                        + getProjectionJson(projectionDTO) + "\n"
                        + END_PROJECTION_DTO + "\n"
                        + "start 'projectionType'\n"
                        + projectionType.name() + "\n"
                        + "end 'projectionType'\n";
                outToClient.write(s.getBytes(StandardCharsets.UTF_8));
            } catch (SocketException e) {
                onSocketException(e, projectionScreenController, projectionTextChangeListener);
            } catch (Exception e) {
                onListenerException(e, projectionScreenController, projectionTextChangeListener);
            }
        });
        thread.start();
    }

    private void waitPreviousThread() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                try {
                    thread.interrupt();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void onSocketException(SocketException e, ProjectionScreenController projectionScreenController, ProjectionTextChangeListener projectionTextChangeListener) {
        String message = e.getMessage();
        if (message.equals("Socket closed")) {
            projectionScreenController.removeProjectionTextChangeListener(projectionTextChangeListener);
            close();
            return;
        } else if (!message.equals("Connection reset by peer: socket write error") &&
                !message.equals("Software caused connection abort: socket write error") &&
                !message.equals("Connection reset by peer")
        ) {
            LOG.error(message, e);
        }
        projectionScreenController.removeProjectionTextChangeListener(projectionTextChangeListener);
    }

    private void onListenerException(Exception e, ProjectionScreenController projectionScreenController, ProjectionTextChangeListener projectionTextChangeListener) {
        LOG.error(e.getMessage(), e);
        projectionScreenController.removeProjectionTextChangeListener(projectionTextChangeListener);
        close();
    }

    private void sendImageInThread(Image image, ProjectionTextChangeListener projectionTextChangeListener, ProjectionScreenController projectionScreenController) {
        waitPreviousThread();
        thread = new Thread(() -> {
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                outToClient.writeInt(imageBytes.length);
                // Send the image byte array
                outToClient.write(imageBytes, 0, imageBytes.length);
            } catch (SocketException e) {
                onSocketException(e, projectionScreenController, projectionTextChangeListener);
            } catch (Exception e) {
                onListenerException(e, projectionScreenController, projectionTextChangeListener);
            }
        });
        thread.start();
    }

    private final BufferedReader inFromClient;

    private String getProjectionJson(ProjectionDTO projectionDTO) {
        Gson gson = getGson();
        return gson.toJson(projectionDTO);
    }

    public void close() {
        closeConnections();
        writer.interrupt();
        reader.interrupt();
    }

    private void closeConnections() {
        closeConnectionsMethod(connectionSocket, outToClient, inFromClient, LOG);
    }

    public static void closeConnectionsMethod(Socket connectionSocket, DataOutputStream outToClient, BufferedReader inFromClient, Logger log) {
        try {
            if (connectionSocket != null) {
                connectionSocket.close();
            }
            if (outToClient != null) {
                outToClient.close();
            }
            if (inFromClient != null) {
                inFromClient.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        reader.interrupt();
        try {
            if (outToClient != null && connectionSocket != null) {
                outToClient.writeBytes("Finished\n");
            }
        } catch (SocketException e) {
            if (e.getMessage().equals("Socket closed")) {
                return;
            }
            LOG.error(e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        closeConnections();
        writer.stop();
        reader.stop();
        Thread.currentThread().interrupt();
    }
}
