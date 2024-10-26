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
import projector.controller.util.AutomaticAction;
import projector.controller.util.ProjectionData;
import projector.controller.util.ProjectionScreensUtil;

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
import java.util.Date;

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
                public void onSetText(String text, ProjectionType projectionType, ProjectionData projectionData) {
                    if (senderType != SenderType.TEXT) {
                        return;
                    }
                    sendTextInThread(text, projectionType, projectionData, this);
                }

                @Override
                public void onImageChanged(Image image, ProjectionType projectionType, ProjectionData projectionData) {
                    if (senderType != SenderType.IMAGE) {
                        return;
                    }
                    sendImageInThread(image, this);
                }

                @Override
                public void onSetCountDownTimer(Date finishDate, AutomaticAction selectedAction, boolean showFinishTime) {
                    if (senderType != SenderType.TEXT) {
                        return;
                    }
                    ProjectionData projectionData = new ProjectionData();
                    ProjectionDTO projectionDTO = new ProjectionDTO();
                    projectionDTO.setFinishDate(finishDate);
                    projectionDTO.setSelectedAction(selectedAction.ordinal());
                    projectionDTO.setShowFinishTime(showFinishTime);
                    projectionData.setProjectionDTO(projectionDTO);
                    sendTextInThread("", ProjectionType.COUNTDOWN_TIMER, projectionData, this);
                }
            };
            ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
            projectionScreensUtil.addProjectionTextChangeListener(projectionTextChangeListener);
            if (senderType == SenderType.IMAGE) {
                projectionScreensUtil.addImageChangeListenerToProjectionScreenController(projectionTextChangeListener, projectionScreenController);
            }
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

    private void sendTextInThread(String text, ProjectionType projectionType, ProjectionData projectionData,
                                  ProjectionTextChangeListener projectionTextChangeListener) {
        if (text == null || projectionType == null) {
            return;
        }
        waitPreviousThread();
        thread = new Thread(() -> {
            try {
                String s = "start 'text'\n"
                        + text + "\n"
                        + "end 'text'\n"
                        + START_PROJECTION_DTO + "\n"
                        + getProjectionJson(projectionData) + "\n"
                        + END_PROJECTION_DTO + "\n"
                        + "start 'projectionType'\n"
                        + projectionType.name() + "\n"
                        + "end 'projectionType'\n";
                outToClient.write(s.getBytes(StandardCharsets.UTF_8));
            } catch (SocketException e) {
                onSocketException(e, projectionTextChangeListener);
            } catch (Exception e) {
                onListenerException(e, projectionTextChangeListener);
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

    private void onSocketException(SocketException e, ProjectionTextChangeListener projectionTextChangeListener) {
        ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
        String message = e.getMessage();
        if (message.equals("Socket closed")) {
            projectionScreensUtil.removeProjectionTextChangeListener(projectionTextChangeListener);
            close();
            return;
        } else if (!message.equals("Connection reset by peer: socket write error") &&
                !message.equals("Software caused connection abort: socket write error") &&
                !message.equals("Connection reset by peer")
        ) {
            LOG.error(message, e);
        }
        projectionScreensUtil.removeProjectionTextChangeListener(projectionTextChangeListener);
    }

    private void onListenerException(Exception e, ProjectionTextChangeListener projectionTextChangeListener) {
        LOG.error(e.getMessage(), e);
        ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
        projectionScreensUtil.removeProjectionTextChangeListener(projectionTextChangeListener);
        close();
    }

    private void sendImageInThread(Image image, ProjectionTextChangeListener projectionTextChangeListener) {
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
                onSocketException(e, projectionTextChangeListener);
            } catch (Exception e) {
                onListenerException(e, projectionTextChangeListener);
            }
        });
        thread.start();
    }

    private final BufferedReader inFromClient;

    private ProjectionDTO getProjectionDTO(ProjectionData projectionData) {
        if (projectionData == null) {
            return null;
        } else {
            return projectionData.getProjectionDTO();
        }
    }

    private String getProjectionJson(ProjectionData projectionData) {
        Gson gson = getGson();
        return gson.toJson(getProjectionDTO(projectionData));
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
