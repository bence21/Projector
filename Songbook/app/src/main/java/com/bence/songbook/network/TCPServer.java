package com.bence.songbook.network;

import static com.bence.songbook.network.TCPClient.PORT;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {

    private static final String TAG = TCPServer.class.getName();
    private static Thread thread;
    private static boolean closed = false;
    private static final List<Sender> senders = new ArrayList<>();
    private static ServerSocket welcomeSocket;
    private static String lastText;

    public synchronized static void startShareNetwork(final List<ProjectionTextChangeListener> projectionTextChangeListeners) {
        closed = false;
        projectionTextChangeListeners.add(text -> lastText = text);
        thread = new Thread(() -> {
            try {
                welcomeSocket = new ServerSocket(PORT);
                while (!closed) {
                    Socket connectionSocket = welcomeSocket.accept();
                    if (!connectionSocket.isClosed()) {
                        Sender sender = new Sender(connectionSocket, projectionTextChangeListeners, lastText);
                        addSocket(sender);
                    }
                }
            } catch (SocketException e) {
                try {
                    String message = e.getMessage();
                    if (message != null && message.equalsIgnoreCase("socket closed")) {
                        return;
                    }
                } catch (Exception e1) {
                    Log.e(TAG, e1.getMessage(), e1);
                }
                Log.e(TAG, e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
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
            } catch (IOException ignored) {
            }

        }
    }
}
