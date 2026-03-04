package projector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

public class ProcessUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    public static boolean killOtherProcesses(boolean confirmBeforeKill) {
        try {
            // Determine the executable name based on OS
            String os = System.getProperty("os.name").toLowerCase();
            String processName = os.contains("win") ? "Projector.exe" : "Projector";
            
            String targetFolder = System.getProperty("user.dir");
            long currentPid = ProcessHandle.current().pid();
            
            // Define the full path we are looking for
            // Using File to handle cross-platform separators (/ vs \)
            String targetPath = new File(targetFolder, processName).getAbsolutePath();

            boolean yesPressed = false;

            // Step 1: Iterate through all processes natively
            return ProcessHandle.allProcesses()
                    .filter(ph -> ph.pid() != currentPid) // Don't kill ourselves
                    .filter(ph -> ph.info().command().isPresent()) // Ensure we can see the path
                    .filter(ph -> {
                        String cmd = ph.info().command().get();
                        // Check if the process path matches our target folder path
                        return cmd.equalsIgnoreCase(targetPath);
                    })
                    .map(ph -> {
                        long pid = ph.pid();
                        String executablePath = ph.info().command().orElse("Unknown");

                        // Step 2: Confirmation UI
                        if (confirmBeforeKill && !yesPressed) {
                            int response = JOptionPane.showConfirmDialog(
                                    null,
                                    "<html><body style='font-family: Arial; font-size: 12px;'>" +
                                            "The application <b>" + processName + "</b> is already running in the current folder.<br>" +
                                            "<b>Details:</b><br>" +
                                            "Path: <i>" + executablePath + "</i><br>" +
                                            "Process ID (PID): <i>" + pid + "</i><br><br>" +
                                            "Would you like to terminate this instance?</body></html>",
                                    "Confirm Termination",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE
                            );

                            if (response != JOptionPane.YES_OPTION) {
                                return false; // User cancelled
                            }
                        }

                        // Step 3: Terminate the process
                        boolean destroyed = ph.destroyForcibly();
                        if (destroyed) {
                            System.out.println("Terminated process: " + processName + " (PID: " + pid + ")");
                        }
                        return true;
                    })
                    .reduce(true, (acc, result) -> acc && result);

        } catch (Exception e) {
            LOG.error("Failed to check or kill other processes: " + e.getMessage(), e);
        }
        return true;
    }
}