package com.bence.songbook.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

class Sender {

    public static final String START_PROJECTION_DTO = "start 'projectionDTO'";
    public static final String END_PROJECTION_DTO = "end 'projectionDTO'";
    private final String TAG = Sender.class.getName();
    private final Thread writer;
    private final DataOutputStream outToClient;
    private final Socket connectionSocket;
    private final Thread reader;
    private final BufferedReader inFromClient;

    Sender(Socket connectionSocket, final List<ProjectionTextChangeListener> projectionTextChangeListeners, String lastText) throws IOException {
        this.connectionSocket = connectionSocket;
        outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        writer = new Thread(new Runnable() {
            @Override
            public void run() {

                ProjectionTextChangeListener projectionTextChangeListener = new ProjectionTextChangeListener() {
                    @Override
                    public void onSetText(String text) {
                        sendTextOverNetwork(text, projectionTextChangeListeners, this);
                    }
                };
                projectionTextChangeListeners.add(projectionTextChangeListener);
                if (lastText != null) {
                    projectionTextChangeListener.onSetText(lastText);
                }
            }
        });
        writer.start();
        reader = initializeReader();
    }

    private void sendTextOverNetwork(String text, List<ProjectionTextChangeListener> projectionTextChangeListeners, ProjectionTextChangeListener projectionTextChangeListener) {
        new Thread(() -> {
            try {
                String s = "start 'text'\n"
                        + text + "\n"
                        + "end 'text'\n"
                        + "start 'projectionType'\n"
                        + "SONG" + "\n"
                        + "end 'projectionType'\n";
                //noinspection CharsetObjectCanBeUsed
                byte[] bytes = s.getBytes("UTF-8");
                outToClient.write(bytes);
            } catch (SocketException e) {
                String message = e.getMessage();
                if (message != null) {
                    if (message.equals("Socket closed")) {
                        projectionTextChangeListeners.remove(projectionTextChangeListener);
                        close();
                        return;
                    } else if (!message.equals("Connection reset by peer: socket write error") && !message.equals("Software caused connection abort: socket write error")) {
                        Log.e(TAG, message, e);
                    }
                }
                projectionTextChangeListeners.remove(projectionTextChangeListener);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                projectionTextChangeListeners.remove(projectionTextChangeListener);
                close();
            }
        }).start();
    }

    @NonNull
    private Thread initializeReader() {
        final Thread reader;
        reader = new Thread(() -> {
            try {
                inFromClient.readLine();
                close();
            } catch (SocketException ignored) {
            } catch (Exception e) {
                close();
            }
        });
        reader.start();
        return reader;
    }

    private void close() {
        closeConnections();
        writer.interrupt();
        reader.interrupt();
    }

    private void closeConnections() {
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
        } catch (IOException ignored) {
        }
    }

    void stop() {
        reader.interrupt();
        try {
            if (outToClient != null && connectionSocket != null) {
                outToClient.writeBytes("Finished\n");
            }
        } catch (SocketException e) {
            if ("Socket closed".equals(e.getMessage())) {
                return;
            }
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        closeConnections();
        writer.interrupt();
        reader.interrupt();
        Thread.currentThread().interrupt();
    }
}
