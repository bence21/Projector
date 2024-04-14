package com.bence.songbook.network;

import static com.bence.songbook.network.Sender.END_PROJECTION_DTO;
import static com.bence.songbook.network.Sender.START_PROJECTION_DTO;

import android.util.Log;

import com.bence.projector.common.dto.ProjectionDTO;
import com.bence.songbook.ui.activity.ConnectToSharedFullscreenActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPClient {

    public static final String TAG = TCPClient.class.getSimpleName();
    public static final int PORT = 21041;
    private static Thread thread;
    private static Thread reader;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;

    public synchronized static void connectToShared(final ConnectToSharedFullscreenActivity connectToSharedFullscreenActivity, final String openIp, final ProjectionTextChangeListener projectionTextChangeListener) {
        if (thread != null) {
            close();
        }
        thread = new Thread(() -> {
            try {
                if (openIp != null) {
                    clientSocket = new Socket(openIp, PORT);
                    //noinspection CharsetObjectCanBeUsed
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    reader = new Thread(() -> {
                        String fromServer;
                        while (true) {
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
                                    connectToSharedFullscreenActivity.finish();
                                    return;
                                }
                                if (fromServer.equals("start 'text'")) {
                                    String text = readTextToEndS("end 'text'");
                                    fromServer = inFromServer.readLine();
                                    ProjectionDTO projectionDTO;
                                    if (fromServer.equals(START_PROJECTION_DTO)) {
                                        projectionDTO = readProjectionDTO();
                                        text = getTextFromProjectionDTO(projectionDTO, text);
                                        fromServer = inFromServer.readLine();
                                    }
                                    if (fromServer.equals("start 'projectionType'")) {
                                        //noinspection unused
                                        String projectionTypeName = inFromServer.readLine();
                                        fromServer = inFromServer.readLine();
                                        if (fromServer.equals("end 'projectionType'")) {
                                            projectionTextChangeListener.onSetText(text);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage(), e);
                                break;
                            }
                        }
                    });
                    reader.start();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
        thread.start();
    }

    private static String getTextFromProjectionDTO(ProjectionDTO projectionDTO, String originalText) {
        try {
            if (projectionDTO == null) {
                return originalText;
            }
            return originalText;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return originalText;
        }
    }

    public synchronized static void close() {
        try {
            if (outToServer != null) {
                new Thread(() -> {
                    try {
                        outToServer.writeBytes("Finished\n");
                        outToServer.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }).start();
            }
            if (inFromServer != null) {
                inFromServer.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (reader != null) {
            reader.interrupt();
        }
        thread.interrupt();
    }

    private static ProjectionDTO readProjectionDTO() throws IOException {
        String text = readTextToEndS(END_PROJECTION_DTO);
        return getProjectionDTOFromJson(text);
    }

    private static String readTextToEndS(String endS) throws IOException {
        String fromServer;
        StringBuilder text = new StringBuilder(inFromServer.readLine());
        fromServer = inFromServer.readLine();
        while (!fromServer.equals(endS)) {
            text.append("\n").append(fromServer);
            fromServer = inFromServer.readLine();
        }
        return text.toString();
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    private static ProjectionDTO getProjectionDTOFromJson(String json) {
        Gson gson = getGson();
        return gson.fromJson(json, ProjectionDTO.class);
    }
}
