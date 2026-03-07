package projector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public class ProcessUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtil.class);

    public static void killOtherProcesses() {
        try {
            // In Linux, the executable usually doesn't have an extension
            String processName = "Projector"; 
            String targetFolder = System.getProperty("user.dir");
            
            // Construct the absolute path to the executable we want to target
            String targetPath = new File(targetFolder, processName).getAbsolutePath();
            
            // Get our own PID so we don't kill ourselves
            long currentPid = ProcessHandle.current().pid();

            System.out.println("Scanning for instances of: " + targetPath);

            // Iterate through all processes visible to the current user
            ProcessHandle.allProcesses().forEach(process -> {
                long pid = process.pid();
                
                // Skip the current process
                if (pid == currentPid) return;

                Optional<String> command = process.info().command();

                if (command.isPresent() && command.get().equals(targetPath)) {
                    System.out.println("Found process in target folder: " + command.get() + " (PID: " + pid + ")");
                    
                    // Terminate the process (destroyForcibly is equivalent to kill -9)
                    if (process.destroyForcibly()) {
                        System.out.println("Terminated process: " + processName + " (PID: " + pid + ")");
                    } else {
                        LOG.warn("Failed to terminate process PID: " + pid);
                    }
                }
            });

        } catch (Exception e) {
            LOG.error("Error while trying to kill other processes: " + e.getMessage(), e);
        }
    }
}