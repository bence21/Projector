package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.ui.utils.Preferences;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.bence.songbook.network.TCPClient.PORT;

public class ConnectToSharedActivity extends AppCompatActivity {

    private static int timeout = 3400;
    private Button tryAgainButton;

    private static boolean isOpenAddress(String ip) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, PORT), timeout);
            socket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_shared);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        tryAgainButton = findViewById(R.id.tryAgainButton);
        searchAndLoadIps();
    }

    private void searchAndLoadIps() {
        final List<String> openIps = new ArrayList<>();
        findShared(openIps);

        ListView listView = findViewById(R.id.listView);
        MyCustomAdapter dataAdapter = new MyCustomAdapter(this,
                R.layout.content_connect_to_shared, openIps);
        listView.setAdapter(dataAdapter);
        if (openIps.size() == 1) {
            connect(openIps.get(0));
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                connect(openIps.get(position));
            }

        });
        TextView textView = findViewById(R.id.textView);
        if (openIps.size() == 0) {
            textView.setText(R.string.no_connections);
        } else {
            textView.setText(R.string.select_one_below);
        }
    }

    private void connect(String ip) {
        final Intent fullScreenIntent = new Intent(this, ConnectToSharedFullscreenActivity.class);
        fullScreenIntent.putExtra("connectToShared", ip);
        startActivityForResult(fullScreenIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Memory memory = Memory.getInstance();
        memory.setSharedTexts(null);
        finish();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTryAgainClick(View view) {
        tryAgainButton.setEnabled(false);
        timeout += 1000;
        searchAndLoadIps();
        tryAgainButton.setEnabled(true);
    }

    @SuppressWarnings("ConstantConditions")
    private class MyCustomAdapter extends ArrayAdapter<String> {

        private List<String> numbers;

        MyCustomAdapter(Context context, int textViewResourceId,
                        List<String> numbers) {
            super(context, textViewResourceId, numbers);
            this.numbers = new ArrayList<>();
            this.numbers.addAll(numbers);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            MyCustomAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.content_connect_to_shared_list_row, null);

                holder = new MyCustomAdapter.ViewHolder();
                holder.textView = convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
            } else {
                holder = (MyCustomAdapter.ViewHolder) convertView.getTag();
            }

            String integer = numbers.get(position);
            holder.textView.setText(integer);
            return convertView;
        }

        private class ViewHolder {
            TextView textView;
        }

    }
}
