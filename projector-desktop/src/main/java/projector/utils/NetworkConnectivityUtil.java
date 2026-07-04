package projector.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public final class NetworkConnectivityUtil {

    private static final String INTERNET_PROBE_URL = "https://www.google.com/generate_204";
    private static final int PROBE_TIMEOUT_MS = 3000;

    private NetworkConnectivityUtil() {
    }

    /**
     * Heuristic connectivity check (some networks block Google).
     * Call from a background thread only.
     */
    public static boolean isInternetReachable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(INTERNET_PROBE_URL).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(PROBE_TIMEOUT_MS);
            connection.setReadTimeout(PROBE_TIMEOUT_MS);
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT;
        } catch (Exception ignored) {
            return false;
        }
    }
}
