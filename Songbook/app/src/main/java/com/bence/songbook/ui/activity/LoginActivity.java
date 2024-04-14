package com.bence.songbook.ui.activity;

import static com.bence.songbook.utils.BaseURL.BASE_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bence.projector.common.dto.LoginDTO;
import com.bence.projector.common.dto.UserDTO;
import com.bence.songbook.R;
import com.bence.songbook.api.LoginApiBean;
import com.bence.songbook.api.UserApiBean;
import com.bence.songbook.models.LoggedInUser;
import com.bence.songbook.repository.impl.ormLite.LoggedInUserRepositoryImpl;
import com.bence.songbook.ui.utils.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LoginActivity extends AppCompatActivity {

    public static final int RESULT_LOGGED_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.login);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
        FloatingActionButton fab = findViewById(R.id.fabSubmit);
        fab.setOnClickListener(view -> login());
    }

    private void login() {
        final LoginDTO loginDTO = getLoginDTO();
        Thread thread = new Thread(() -> {
            LoginApiBean loginApiBean = new LoginApiBean();
            if (!loginApiBean.login(loginDTO)) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, R.string.try_again_later, Toast.LENGTH_SHORT).show());
                return;
            }
            UserApiBean userApiBean = new UserApiBean();
            UserDTO userDTO = userApiBean.getLoggedInUser();
            if (userDTO != null) {
                LoggedInUserRepositoryImpl loggedInUserRepository = new LoggedInUserRepositoryImpl(LoginActivity.this);
                LoggedInUser loggedInUser = new LoggedInUser();
                loggedInUser.setEmail(userDTO.getEmail());
                loggedInUser.setPassword(loginDTO.getPassword());
                loggedInUserRepository.save(loggedInUser);
                setResult(RESULT_LOGGED_IN);
                finish();
            } else {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, R.string.password_or_email_incorrect, Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
    }

    private LoginDTO getLoginDTO() {
        LoginDTO loginDTO = new LoginDTO();
        EditText editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
        loginDTO.setUsername(getTrimText(editTextTextEmailAddress));
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        String password = getStringFromEditText(editTextPassword);
        loginDTO.setPassword(password);
        return loginDTO;
    }

    private String getTrimText(EditText editTextTextEmailAddress) {
        return getStringFromEditText(editTextTextEmailAddress).trim();
    }

    private String getStringFromEditText(EditText editTextTextEmailAddress) {
        return editTextTextEmailAddress.getText().toString();
    }

    public void onCreateAccountClick(View view) {
        Uri uri = Uri.parse(BASE_URL + "#/registration");
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
