package projector.utils;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ApplicationUtil;
import projector.application.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class HandleUnexpectedError {
    private static final Logger LOG = LoggerFactory.getLogger(HandleUnexpectedError.class);
    private static JFrame previousFrame = null;
    private static int exceptionCount = 0;

    private static synchronized boolean incExceptionCount() {
        ++exceptionCount;
        return exceptionCount != 1;
    }

    private static synchronized void decExceptionCount() {
        --exceptionCount;
    }

    public static void setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                boolean anExceptionStarted = incExceptionCount();
                try {
                    String s = "Thread: " + t.getName() + " " + e.getMessage();
                    System.out.println(s);
                    e.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    logError(e);
                    if (!anExceptionStarted) {
                        showReLunchApp(s + "\n\n" + sw);
                    }
                } finally {
                    try {
                        decExceptionCount();
                    } catch (Exception e3) {
                        logError(e3);
                    }
                }
            } catch (Exception e2) {
                logError(e2);
            }
        });
    }

    private static void logError(Throwable e) {
        try {
            LOG.error(e.getMessage(), e);
        } catch (Exception e2) {
            try {
                System.out.println("Couldn't log");
            } catch (Exception ignored) {
            }
        }
    }

    private static void showReLunchApp(String errorMessage) {
        if (HandleUnexpectedError.previousFrame != null && HandleUnexpectedError.previousFrame.isVisible()) {
            return;
        }
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        JFrame frame = new JFrame(resourceBundle.getString("Unexpected error!"));
        frame.setSize(500, 200);
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        JTextArea textArea = new JTextArea(1, 30);
        textArea.setText(errorMessage);
        textArea.setMargin(new java.awt.Insets(0, 12, 0, 0)); // Set left margin of 10 pixels

        JLabel questionLabel = new JLabel(resourceBundle.getString("Relunch the Projector?"));
        JButton yesButton = new JButton(resourceBundle.getString("Yes"));
        JButton noButton = new JButton(resourceBundle.getString("No"));

        JPanel panel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setScrollPaneSize(frame, scrollPane);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setScrollPaneSize(frame, scrollPane);
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                frame.dispose();
            }
        });

        panel.add(scrollPane);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(questionLabel, BorderLayout.NORTH);
        contentPanel.add(yesButton, BorderLayout.CENTER);
        contentPanel.add(noButton, BorderLayout.EAST);
        panel.add(contentPanel, BorderLayout.CENTER);
        yesButton.addActionListener(e -> {
            reLunch();
            frame.dispose();
        });
        noButton.addActionListener(e -> frame.dispose());

        frame.getContentPane().add(panel);
        frame.setVisible(true);
        HandleUnexpectedError.previousFrame = frame;
    }

    private static void setScrollPaneSize(JFrame frame, JScrollPane scrollPane) {
        scrollPane.setPreferredSize(new Dimension(frame.getWidth(), 100)); // Set preferred size to match frame
    }

    private static void reLunch() {
        Platform.runLater(() -> {
            try {
                ApplicationUtil applicationUtil = ApplicationUtil.getInstance();
                applicationUtil.saveProjectorState();
                applicationUtil.closeApplication();
                new Thread(() -> {
                    try {
                        sleep(1000);
                        String command = "cmd /c Projector.exe";
                        Runtime.getRuntime().exec(command);
                    } catch (Exception e) {
                        logError(e);
                    }
                }).start();
            } catch (Exception e2) {
                logError(e2);
            }
        });
    }

}
