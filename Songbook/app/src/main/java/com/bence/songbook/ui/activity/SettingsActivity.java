package com.bence.songbook.ui.activity;

import static com.bence.songbook.utils.BaseURL.BASE_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.network.LocalNetwork;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.network.TCPClient;
import com.bence.songbook.network.TCPServer;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseActivity {

    private TextView shareOnNetworkAddressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean light_theme_switch = sharedPreferences.getBoolean("light_theme_switch", false);
        final Switch lightThemeSwitch = findViewById(R.id.lightThemeSwitch);
        lightThemeSwitch.setChecked(light_theme_switch);
        lightThemeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("light_theme_switch", lightThemeSwitch.isChecked()).apply();
                recreate();
            }
        });
        SeekBar seekBar = findViewById(R.id.seekBar);
        int max_text_size = sharedPreferences.getInt("max_text_size", 129);
        seekBar.setProgress(max_text_size);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress + 10;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sharedPreferences.edit().putInt("max_text_size", progress).apply();
                Toast.makeText(getApplicationContext(), "" + progress, Toast.LENGTH_SHORT).show();
            }
        });
        boolean show_title_switch = sharedPreferences.getBoolean("show_title_switch", false);
        final Switch showTitleSwitch = findViewById(R.id.showTitleSwitch);
        showTitleSwitch.setChecked(show_title_switch);
        showTitleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("show_title_switch", showTitleSwitch.isChecked()).apply();
            }
        });
        boolean blank_switch = sharedPreferences.getBoolean("blank_switch", false);
        final Switch blankSwitch = findViewById(R.id.blankSwitch);
        blankSwitch.setChecked(blank_switch);
        blankSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("blank_switch", blankSwitch.isChecked()).apply();
            }
        });
        boolean shortCollection_switch = sharedPreferences.getBoolean("shortCollectionName", false);
        final Switch shortCollectionSwitch = findViewById(R.id.shortCollectionSwitch);
        shortCollectionSwitch.setChecked(shortCollection_switch);
        shortCollectionSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = shortCollectionSwitch.isChecked();
                sharedPreferences.edit().putBoolean("shortCollectionName", checked).apply();
                Memory.getInstance().getMainActivity().setShortCollectionName(checked);
            }
        });
        boolean rotateInFullscreen = sharedPreferences.getBoolean("rotateInFullscreen", true);
        final Switch rotateInFullscreenSwitch = findViewById(R.id.rotateInFullscreenSwitch);
        rotateInFullscreenSwitch.setChecked(rotateInFullscreen);
        rotateInFullscreenSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = rotateInFullscreenSwitch.isChecked();
                sharedPreferences.edit().putBoolean("rotateInFullscreen", checked).apply();
            }
        });
        final Switch shareOnNetworkSwitch = findViewById(R.id.shareOnNetworkSwitch);
        shareOnNetworkAddressText = findViewById(R.id.shareOnNetworkAddressText);
        final Memory memory = Memory.getInstance();
        shareOnNetworkSwitch.setChecked(memory.isShareOnNetwork());
        updateShareAddressVisibility(memory.isShareOnNetwork());
        shareOnNetworkSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean checked = shareOnNetworkSwitch.isChecked();
                    if (checked) {
                        List<ProjectionTextChangeListener> projectionTextChangeListeners = new ArrayList<>();
                        TCPServer.startShareNetwork(projectionTextChangeListeners);
                        memory.setProjectionTextChangeListeners(projectionTextChangeListeners);
                    } else {
                        TCPServer.close();
                    }
                    memory.setShareOnNetwork(checked);
                    updateShareAddressVisibility(checked);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button connectToSharedButton = findViewById(R.id.connectToSharedButton);
        connectToSharedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ConnectToSharedActivity.class);
                startActivity(intent);
            }
        });

        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        if (UserService.getInstance().isLoggedIn(this)) {
            deleteAccountButton.setVisibility(View.VISIBLE);
            deleteAccountButton.setOnClickListener(v -> {
                Uri uri = Uri.parse(BASE_URL + "#/delete-account");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            });
        } else {
            deleteAccountButton.setVisibility(View.GONE);
        }
    }

    private void updateShareAddressVisibility(boolean sharing) {
        if (shareOnNetworkAddressText == null) {
            return;
        }
        if (!sharing) {
            shareOnNetworkAddressText.setVisibility(View.GONE);
            return;
        }
        shareOnNetworkAddressText.setVisibility(View.VISIBLE);
        List<String> ips = LocalNetwork.getRfc1918Addresses();
        if (ips.isEmpty()) {
            shareOnNetworkAddressText.setText(getString(R.string.share_on_network_no_lan_ip, TCPClient.PORT));
            return;
        }
        StringBuilder addresses = new StringBuilder();
        for (String ip : ips) {
            if (addresses.length() > 0) {
                addresses.append('\n');
            }
            addresses.append(ip).append(':').append(TCPClient.PORT);
        }
        shareOnNetworkAddressText.setText(getString(R.string.share_on_network_addresses, addresses.toString()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shareOnNetworkAddressText != null) {
            updateShareAddressVisibility(Memory.getInstance().isShareOnNetwork());
        }
    }

    @Override
    public void onBackPressed() {
        setResult(0);
        finish();
    }
}
