package projector.http;

import java.util.List;

/**
 * Spring Security form login with {@code followRedirects(false)}: success is often a redirect (e.g. 302), not 2xx.
 */
public final class LoginHttpResponseUtil {

    private LoginHttpResponseUtil() {
    }

    /**
     * @param responseCode           HTTP status from the login request
     * @param locationHeaderValues   {@code Location} header values (e.g. OkHttp {@code Headers.values("Location")})
     */
    public static boolean isLoginHttpSuccess(int responseCode, List<String> locationHeaderValues) {
        if (responseCode >= 200 && responseCode < 300) {
            return true;
        }
        if (responseCode == 401 || responseCode == 403) {
            return false;
        }
        if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307) {
            if (locationHeaderValues == null || locationHeaderValues.isEmpty()) {
                return false;
            }
            for (String loc : locationHeaderValues) {
                if (loc == null) {
                    continue;
                }
                String lower = loc.toLowerCase();
                if (lower.contains("error=") || lower.contains("?error") || lower.contains("&error")) {
                    return false;
                }
                if (lower.endsWith("/#/login") || lower.contains("/#/login?")) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
