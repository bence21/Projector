package com.bence.songbook.ui.utils;

import static com.bence.songbook.utils.BaseURL.BASE_URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

public class YouTubeIFrame {
    public static void setYouTubeIFrameToWebView(WebView webView, String youTubeVideoId, Activity activity) {
        setupYouTubeWebViewErrorHandling(webView, activity);
        webView.post(() -> {
            int widthPixels = webView.getWidth();
            int heightPixels = widthPixels * 9 / 16;
            webView.setLayoutParams(new LinearLayout.LayoutParams(widthPixels, heightPixels));
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if required

        String iframeHtml =
                "<iframe class=\"embeddedVideo\" src=\"" + BASE_URL + "youtube-embed.html?v=" + youTubeVideoId + "\"" +
                        "  frameborder=\"0\"\n" +
                        "  allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\"" +
                        "  ></iframe>";

        String finalHtml = "<!DOCTYPE html><html><head>" +
                "<style>\n" +
                "  body, html {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "  }\n" +
                "  .embeddedVideo {\n" +
                "    position: absolute;\n" +
                "    top: 0;\n" +
                "    left: 0;\n" +
                "    width: 100%;\n" +
                "    height: 100%;\n" +
                "  }\n" +
                "</style>\n" +
                "</head>" +
                "<body style=\"margin:0; padding:0;\">" + iframeHtml + "</body></html>";
        // Use BASE_URL as base URL so CSP frame-ancestors * works correctly
        // Using null causes the WebView to use file:// or data:// scheme which doesn't match frame-ancestors *
        webView.loadDataWithBaseURL(BASE_URL, finalHtml, "text/html", "UTF-8", null);
    }

    public static void setupYouTubeWebViewErrorHandling(WebView webView, Activity activity) {
        // Set up WebChromeClient to catch console messages (including ERR_BLOCKED_BY_RESPONSE)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = consoleMessage.message();
                String sourceId = consoleMessage.sourceId();
                int lineNumber = consoleMessage.lineNumber();

                // Log all console messages
                Log.d(YouTubeIFrame.class.getSimpleName(),
                        "Console [" + consoleMessage.messageLevel() + "]: " + message +
                                " at " + sourceId + ":" + lineNumber);

                // Check for ERR_BLOCKED_BY_RESPONSE or other blocking errors
                if (message != null && (message.contains("ERR_BLOCKED_BY_RESPONSE") ||
                        message.contains("blocked") ||
                        message.contains("Refused to") ||
                        message.contains("frame-ancestors"))) {
                    String errorMessage = "Console error detected: " + message;
                    Log.e(YouTubeIFrame.class.getSimpleName(), errorMessage);

                    // Show toast to user
                    activity.runOnUiThread(() -> Toast.makeText(activity,
                            "Failed to load YouTube video: " + message,
                            Toast.LENGTH_LONG).show());
                }

                return true; // Message handled
            }
        });

        // Set up WebViewClient to catch errors
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(YouTubeIFrame.class.getSimpleName(), "Page started loading: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(YouTubeIFrame.class.getSimpleName(), "Page finished loading: " + url);
            }

            @Override
            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                super.onUnhandledKeyEvent(view, event);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                String errorDescription;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    errorDescription = error.getDescription().toString();
                } else {
                    errorDescription = null;
                }
                String errorCode = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    errorCode = String.valueOf(error.getErrorCode());
                }

                String requestUrl;
                boolean isMainFrame;
                requestUrl = request.getUrl().toString();
                isMainFrame = request.isForMainFrame();

                String errorMessage = "WebView error: " + errorDescription + " (Code: " + errorCode +
                        ") URL: " + requestUrl + " MainFrame: " + isMainFrame;

                // Log the error
                Log.e(YouTubeIFrame.class.getSimpleName(), errorMessage);

                // Show toast to user
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        "Failed to load YouTube video: " + errorDescription,
                        Toast.LENGTH_LONG).show());
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            android.webkit.WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);

                String requestUrl;
                boolean isMainFrame;
                requestUrl = request.getUrl().toString();
                isMainFrame = request.isForMainFrame();

                String errorMessage = "HTTP error: " + errorResponse.getStatusCode() +
                        " - " + errorResponse.getReasonPhrase() +
                        " URL: " + requestUrl + " MainFrame: " + isMainFrame;

                // Log the error
                Log.e(YouTubeIFrame.class.getSimpleName(), errorMessage);

                // Show toast to user
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        "HTTP error loading YouTube video: " + errorResponse.getStatusCode(),
                        Toast.LENGTH_LONG).show());
            }
        });
    }
}
