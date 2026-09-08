package com.bence.songbook.ui.activity;

import static com.bence.songbook.network.TCPClient.PORT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.network.LocalNetwork;
import com.bence.songbook.ui.utils.Preferences;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectToSharedActivity extends BaseActivity {

    private static final String[] FALLBACK_GATEWAYS = {
            "192.168.43.1",
            "192.168.137.1",
            "192.168.42.1",
            "172.20.10.1",
            "192.168.1.1",
            "10.0.0.1"
    };
    private static final int SCAN_POOL_SIZE = 32;
    private static int timeout = 800;
    private Button tryAgainButton;
    private Button connectByIpButton;
    private EditText ipAddressEditText;
    private ProgressBar searchProgressBar;
    private TextView textView;
    private ListView listView;
    private volatile boolean scanning;
    private volatile boolean destroyed;
    private ExecutorService scanExecutor;

    private boolean isOpenAddress(String ip) {
        Socket socket = null;
        try {
            socket = new Socket();
            LocalNetwork.bindSocket(this, socket);
            socket.connect(new InetSocketAddress(ip, PORT), timeout);
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
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
        connectByIpButton = findViewById(R.id.connectByIpButton);
        ipAddressEditText = findViewById(R.id.ipAddressEditText);
        searchProgressBar = findViewById(R.id.searchProgressBar);
        textView = findViewById(R.id.textView);
        listView = findViewById(R.id.listView);
        connectByIpButton.setOnClickListener(v -> connectFromManualIp());
        ipAddressEditText.setOnEditorActionListener((v, actionId, event) -> {
            connectFromManualIp();
            return true;
        });
        searchAndLoadIps();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        shutdownScanExecutor();
        super.onDestroy();
    }

    private void searchAndLoadIps() {
        if (scanning) {
            return;
        }
        scanning = true;
        tryAgainButton.setEnabled(false);
        searchProgressBar.setVisibility(View.VISIBLE);
        textView.setText(R.string.searching_shared);
        new Thread(() -> {
            List<String> openIps = Collections.synchronizedList(new ArrayList<>());
            findShared(openIps);
            List<String> result = new ArrayList<>(openIps);
            runOnUiThread(() -> {
                scanning = false;
                if (destroyed || isFinishing()) {
                    return;
                }
                tryAgainButton.setEnabled(true);
                searchProgressBar.setVisibility(View.GONE);
                showResults(result);
            });
        }).start();
    }

    private void showResults(List<String> openIps) {
        MyCustomAdapter dataAdapter = new MyCustomAdapter(this, R.layout.content_connect_to_shared, openIps);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> connect(openIps.get(position)));
        if (openIps.isEmpty()) {
            textView.setText(R.string.no_connections);
        } else {
            textView.setText(R.string.select_one_below);
        }
        if (openIps.size() == 1) {
            connect(openIps.get(0));
        }
    }

    private void connectFromManualIp() {
        String ip = ipAddressEditText.getText().toString().trim();
        if (!LocalNetwork.isValidIpv4(ip)) {
            Toast.makeText(this, R.string.invalid_ip_address, Toast.LENGTH_SHORT).show();
            return;
        }
        connect(ip);
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
        ExecutorService executor = Executors.newFixedThreadPool(SCAN_POOL_SIZE);
        scanExecutor = executor;
        try {
            List<String> hostsToProbe = buildHostsToProbe();
            List<Callable<Void>> tasks = new ArrayList<>(hostsToProbe.size());
            for (final String ip : hostsToProbe) {
                tasks.add(() -> {
                    try {
                        if (!destroyed && isOpenAddress(ip)) {
                            openIps.add(ip);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            }
            long waitMs = Math.max(15000L, ((hostsToProbe.size() + SCAN_POOL_SIZE - 1L) / SCAN_POOL_SIZE) * (timeout + 200L) + 2000L);
            executor.invokeAll(tasks, waitMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
            if (scanExecutor == executor) {
                scanExecutor = null;
            }
        }
    }

    private List<String> buildHostsToProbe() {
        Set<String> ownIps = new LinkedHashSet<>(LocalNetwork.getRfc1918Addresses());
        Set<String> prefixes = new LinkedHashSet<>();
        for (String ip : ownIps) {
            String prefix = LocalNetwork.subnetPrefix(ip);
            if (prefix != null) {
                prefixes.add(prefix);
            }
        }
        String gateway = LocalNetwork.getDhcpGateway(this);
        if (gateway != null) {
            String prefix = LocalNetwork.subnetPrefix(gateway);
            if (prefix != null) {
                prefixes.add(prefix);
            }
        }
        Set<String> added = new LinkedHashSet<>();
        List<String> hosts = new ArrayList<>();
        addHost(hosts, added, ownIps, gateway);
        if (prefixes.isEmpty()) {
            for (String fallback : FALLBACK_GATEWAYS) {
                addHost(hosts, added, ownIps, fallback);
            }
            return hosts;
        }
        for (String prefix : prefixes) {
            addHost(hosts, added, ownIps, prefix + "1");
        }
        for (String prefix : prefixes) {
            for (int i = 1; i <= 255; ++i) {
                addHost(hosts, added, ownIps, prefix + i);
            }
        }
        return hosts;
    }

    private void addHost(List<String> hosts, Set<String> added, Set<String> ownIps, String ip) {
        if (ip == null || added.contains(ip) || ownIps.contains(ip)) {
            return;
        }
        added.add(ip);
        hosts.add(ip);
    }

    private void shutdownScanExecutor() {
        ExecutorService executor = scanExecutor;
        if (executor != null) {
            executor.shutdownNow();
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
        timeout += 1000;
        searchAndLoadIps();
    }

    private class MyCustomAdapter extends ArrayAdapter<String> {

        private final List<String> numbers;

        MyCustomAdapter(Context context, int textViewResourceId, List<String> numbers) {
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
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
