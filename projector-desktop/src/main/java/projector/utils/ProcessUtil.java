package projector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    public static boolean killOtherProcesses(boolean confirmBeforeKill) {
        try {
            String processName = "Projector.exe";
            String targetFolder = System.getProperty("user.dir");

            // Get the current process ID
            String currentPid = String.valueOf(ProcessHandle.current().pid());

            // Step 1: Run the WMIC query
            String query = "wmic process where \"name='" + processName + "'\" get ProcessId,ExecutablePath";
            Process process = Runtime.getRuntime().exec(query);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            boolean yesPressed = false;
            // Step 2: Parse the output
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("ExecutablePath") || line.startsWith("No Instance(s)")) {
                    continue; // Skip headers and empty lines
                }

                // Split the line into ExecutablePath and ProcessId
                String[] details = line.split("\\s{2,}"); // WMIC separates columns by multiple spaces
                if (details.length == 2) {
                    String executablePath = details[0];
                    String pid = details[1];

                    // Step 3: Check if the process is from the target folder and not the current process
                    if (executablePath.equalsIgnoreCase(targetFolder + "\\Projector.exe") && !pid.equals(currentPid)) {
                        System.out.println("Found process in target folder: " + executablePath + " (PID: " + pid + ")");

                        // Ask for confirmation if the flag is true
                        if (confirmBeforeKill && !yesPressed) {
                            int response = JOptionPane.showConfirmDialog(
                                    null,
                                    "<html><body style='font-family: Arial; font-size: 12px;'>" +
                                            "The application <b>Projector.exe</b> is already running in the current folder.<br>" +
                                            "<b>Details:</b><br>" +
                                            "Path: <i>" + executablePath + "</i><br>" +
                                            "Process ID (PID): <i>" + pid + "</i><br><br>" +
                                            "Would you like to terminate this instance?</body></html>",
                                    "Confirm Termination",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE
                            );

                            if (response != JOptionPane.YES_OPTION) {
                                return false;
                            } else {
                                yesPressed = true;
                            }
                        }

                        // Step 4: Terminate the process
                        String killCommand = "taskkill /PID " + pid + " /F";
                        Runtime.getRuntime().exec(killCommand);
                        System.out.println("Terminated process: " + processName + " (PID: " + pid + ")");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return true;
    }

}
