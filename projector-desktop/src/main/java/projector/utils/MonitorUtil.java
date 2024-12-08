package projector.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MonitorUtil {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorUtil.class);

    private Monitor primaryMonitor;

    private MonitorUtil() {
    }

    // Static inner class - inner classes are not loaded until they are referenced
    private static class Holder {
        private static final MonitorUtil INSTANCE = new MonitorUtil();
    }

    public static MonitorUtil getInstance() {
        return Holder.INSTANCE;
    }

    public void setPrimaryMonitor(Monitor primaryMonitor) {
        this.primaryMonitor = primaryMonitor;
    }

    public Monitor getPrimaryMonitor() {
        return primaryMonitor;
    }

    // Define the Shcore interface to access GetDpiForMonitor
    public interface Shcore extends Library {
        Shcore INSTANCE = Native.load("Shcore", Shcore.class);

        // Define GetDpiForMonitor (available in Windows 8.1 and later)
        int GetDpiForMonitor(WinUser.HMONITOR hMonitor, int dpiType, IntByReference dpiX, IntByReference dpiY);
    }

    // Define the User32 interface for EnumDisplayMonitors and GetMonitorInfoW
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        void EnumDisplayMonitors(WinDef.HDC hdc, WinDef.RECT lprcClip, MonitorEnumProc lpfnEnum, Pointer dwData);

        boolean GetMonitorInfoW(WinUser.HMONITOR hMonitor, MONITORINFOEX lpMonitorInfo); // GetMonitorInfoW for Unicode
    }

    // Define the MONITORINFOEX structure
    @SuppressWarnings("unused")
    public static class MONITORINFOEX extends Structure {
        public int cbSize; // Size of the structure
        public WinDef.RECT rcMonitor;  // Monitor display area
        public WinDef.RECT rcWork;     // Work area excluding taskbars, etc.
        public int dwFlags;
        public char[] szDevice = new char[32]; // Device name (monitor)

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("cbSize", "rcMonitor", "rcWork", "dwFlags", "szDevice");
        }
    }

    // Define the DPI types (only DPI_EFFECTIVE or DPI_RAW are commonly used)
    public static final int DPI_TYPE_EFFECTIVE = 0;

    // Define a callback function type for the monitor enumeration
    @SuppressWarnings("unused")
    public interface MonitorEnumProc extends StdCallLibrary.StdCallCallback {
        boolean callback(WinUser.HMONITOR hMonitor, WinDef.HDC hdc, WinDef.RECT lprcClip, Pointer dwData);
    }

    public static final int MONITORINFOF_PRIMARY = 0x00000001;

    private static List<Monitor> monitors = new ArrayList<>();

    // Callback function implementation for handling each monitor
    public static boolean monitorEnumProc(WinUser.HMONITOR hMonitor, WinDef.HDC hdc, WinDef.RECT lprcClip, Pointer dwData) {
        MONITORINFOEX monitorInfo = new MONITORINFOEX();
        monitorInfo.cbSize = monitorInfo.size(); // Initialize cbSize field

        if (User32.INSTANCE.GetMonitorInfoW(hMonitor, monitorInfo)) {
            // Check if this monitor is the primary monitor
            boolean isPrimary = (monitorInfo.dwFlags & MONITORINFOF_PRIMARY) != 0;

            // Print monitor details
            String monitorDeviceID = new String(monitorInfo.szDevice).trim();
            Monitor monitor = new Monitor();
            monitor.setMonitorDeviceId(monitorDeviceID);
            monitor.setMonitorArea(monitorInfo.rcMonitor.toRectangle());
            monitor.setPrimaryMonitor(isPrimary);

            // Get DPI scaling for the monitor
            IntByReference dpiX = new IntByReference();
            IntByReference dpiY = new IntByReference();
            int dpiResult = Shcore.INSTANCE.GetDpiForMonitor(hMonitor, DPI_TYPE_EFFECTIVE, dpiX, dpiY);
            if (dpiResult == 0) {
                double dpiXValue = dpiX.getValue();
                int DPI_WITHOUT_SCALE = 96;
                dpiXValue /= DPI_WITHOUT_SCALE;
                monitor.setDpiScale(dpiXValue);
            } else {
                System.out.println("Failed to retrieve DPI information.");
            }
            monitors.add(monitor); // Add the current monitor to the list
        }
        return true; // Return true to continue enumeration
    }

    public static void main(String[] args) {
        List<Monitor> monitors = MonitorUtil.getInstance().getExtendedMonitors();
        for (Monitor monitor : monitors) {
            System.out.println(monitor.getMonitorDeviceId());
        }
    }

    public List<Monitor> getExtendedMonitors() {
        List<Monitor> extendedMonitors = new ArrayList<>();
        try {
            User32 user32 = User32.INSTANCE;
            MonitorEnumProc monitorEnumProc = MonitorUtil::monitorEnumProc;
            monitors = new ArrayList<>();
            user32.EnumDisplayMonitors(null, null, monitorEnumProc, null);
            for (Monitor monitor : monitors) {
                if (!monitor.isPrimaryMonitor()) {
                    extendedMonitors.add(monitor);
                } else {
                    MonitorUtil.getInstance().setPrimaryMonitor(monitor);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return extendedMonitors;
    }
}
