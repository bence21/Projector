package com.bence.psbremote.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.bence.psbremote.ProjectionTextChangeListener;
import com.bence.psbremote.R;
import com.bence.psbremote.SongRemoteListener;
import com.bence.psbremote.TCPClient;
import com.bence.psbremote.model.Song;
import com.bence.psbremote.util.Memory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.bence.psbremote.TCPClient.PORT;

public class MainActivity extends AppCompatActivity {

    private Memory memory = Memory.getInstance();
    private TextView infoText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoText = findViewById(R.id.infoText);
        connectClick(null);
    }

    private void findShared(final List<String> openIps) {
        try {
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            List<String> ips = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) enumeration.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String hostAddress = i.getHostAddress();
                    if (hostAddress.matches("192.168.[12]?[0-9]{1,2}.[12]?[0-9]{1,2}")) {
                        ips.add(hostAddress);
                    }
                }
            }
            List<Thread> threads = new ArrayList<>(ips.size() * 255);
            for (String ip : ips) {
                String[] split = ip.split("\\.");
                String firstThree = split[0] + "." + split[1] + "." + split[2] + ".";
                for (int i = 1; i <= 255; ++i) {
                    final String ip1 = firstThree + i;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isOpenAddress(ip1)) {
                                    openIps.add(ip1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                    threads.add(thread);
                }
            }
            for (Thread thread : threads) {
                thread.join(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void songButtonClick(View view) {
        Intent intent = new Intent(this, SongsActivity.class);
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    public void connectClick(View view) {
        final List<String> openIps = new ArrayList<>();
        findShared(openIps);
        if (openIps.size() > 0) {
            String connectToShared = openIps.get(0);
            infoText.setText("Connected to: " + connectToShared);
            TCPClient.connectToShared(this, connectToShared, new ProjectionTextChangeListener() {
                @Override
                public void onSetText(final String text) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                memory.setProjectionText(text);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new SongRemoteListener() {
                @Override
                public void onSongVerseListViewChanged(final List<String> list) {
                    memory.setSongVerses(list);
                }

                @Override
                public void onSongListViewChanged(List<Song> list) {
                    memory.setSongs(list);
                }
            });
        }
    }
}
