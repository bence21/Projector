package projector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    public static void killOtherProcesses() {
        try {
            String processName = "Projector.exe";
            String targetFolder = System.getProperty("user.dir");
            // Step 1: Run the WMIC query
            String query = "wmic process where \"name='" + processName + "'\" get ProcessId,ExecutablePath";
            Process process = Runtime.getRuntime().exec(query);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            boolean processFound = false;

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

                    // Step 3: Check if the process is from the target folder
                    if (executablePath.equalsIgnoreCase(targetFolder + "\\Projector.exe")) {
                        processFound = true;
                        System.out.println("Found process in target folder: " + executablePath + " (PID: " + pid + ")");

                        // Step 4: Terminate the process
                        String killCommand = "taskkill /PID " + pid + " /F";
                        Runtime.getRuntime().exec(killCommand);
                        System.out.println("Terminated process: " + processName + " (PID: " + pid + ")");
                    }
                }
            }
            if (!processFound) {
                System.out.println("No process found with name '" + processName + "' running in folder: " + targetFolder);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
