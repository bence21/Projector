package com.bence.songbook.rules;

import com.bence.songbook.BuildConfig;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.HttpURLConnection;
import java.net.URL;

public class RequiresNetworkRule implements TestRule {

    private static final int CONNECT_TIMEOUT_MS = 3_000;
    private static final int READ_TIMEOUT_MS = 3_000;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue(
                        "API server unreachable at " + BuildConfig.API_BASE_URL,
                        isApiServerReachable());
                base.evaluate();
            }
        };
    }

    private static boolean isApiServerReachable() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BuildConfig.API_BASE_URL + "/api/languages");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 500;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
