package com.bence.songbook.ui.utils;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class YouTubeIFrame {
    public static void setYouTubeIFrameToWebView(WebView webView, String youTubeVideoId) {
        webView.post(() -> {
            int widthPixels = webView.getWidth();
            int heightPixels = widthPixels * 9 / 16;
            webView.setLayoutParams(new LinearLayout.LayoutParams(widthPixels, heightPixels));
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if required

        String iframeHtml =
                "<iframe class=\"embeddedVideo\" src=\"https://www.youtube-nocookie.com/embed/" + youTubeVideoId + "?fs=0\"" +
                        "  frameborder=\"0\"\n" +
                        "  allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\"" +
                        "  ></iframe>" +
                        "";

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
        webView.loadDataWithBaseURL(null, finalHtml, "text/html", "UTF-8", null);
    }
}
