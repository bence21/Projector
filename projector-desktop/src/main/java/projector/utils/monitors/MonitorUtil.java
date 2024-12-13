package projector.utils.monitors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.utils.AppProperties;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static projector.application.ProjectionScreenSettings.calculateDistance;
import static projector.application.ProjectionScreenSettings.calculateOverlap;

public class MonitorUtil {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorUtil.class);

    private Monitor primaryMonitor;
    private List<MonitorInfo> monitorInfos;
    private List<Monitor> extendedMonitors;
    private static List<Monitor> monitors = new ArrayList<>();

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
            System.out.println(monitor.getMonitorIdentifier());
        }
    }

    public List<Monitor> getExtendedMonitors() {
        if (extendedMonitors == null) {
            calculateExtendedMonitors();
        }
        return extendedMonitors;
    }

    private void calculateExtendedMonitors() {
        List<Monitor> extendedMonitors = new ArrayList<>();
        try {
            User32 user32 = User32.INSTANCE;
            MonitorEnumProc monitorEnumProc = MonitorUtil::monitorEnumProc;
            monitors = new ArrayList<>();
            user32.EnumDisplayMonitors(null, null, monitorEnumProc, null);
            applyMonitorInfos(monitors);
            calculateDpiPositions(monitors);
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
        this.extendedMonitors = extendedMonitors;
    }

    private void calculateDpiPositions(List<Monitor> monitors) {
        clearMonitors(monitors);
        Monitor primaryMonitor = filterPrimaryMonitor(monitors);
        if (primaryMonitor == null) {
            return;
        }
        List<Monitor> placedMonitors = new ArrayList<>();
        placePrimaryMonitor(primaryMonitor, placedMonitors);
        Rectangle2D primaryMonitorArea = getRectangle2D(primaryMonitor.getMonitorArea());
        monitors.sort((monitor1, monitor2) -> {
            Rectangle2D area1 = getRectangle2D(monitor1.getMonitorArea());
            Rectangle2D area2 = getRectangle2D(monitor2.getMonitorArea());
            double distance1 = calculateDistance(primaryMonitorArea, area1);
            double distance2 = calculateDistance(primaryMonitorArea, area2);
            return Double.compare(distance1, distance2);
        });
        for (Monitor monitor : monitors) {
            if (monitor.isPlaced()) {
                continue;
            }
            MagnetDirection magnetDirection = getMagnetDirection(primaryMonitor, monitor);
            applyDpiForSize(monitor);
            switch (magnetDirection) {
                case NORTH -> {
                    placeMonitorNorth(placedMonitors, monitor);
                    secondaryPlaceWestEast(monitor, primaryMonitor, placedMonitors);
                }
                case WEST -> {
                    placeMonitorWest(placedMonitors, monitor);
                    secondaryPlaceNorthSouth(monitor, primaryMonitor, placedMonitors);
                }
                case SOUTH -> {
                    placeMonitorSouth(placedMonitors, monitor);
                    secondaryPlaceWestEast(monitor, primaryMonitor, placedMonitors);
                }
                case EAST -> {
                    placeMonitorEast(placedMonitors, monitor);
                    secondaryPlaceNorthSouth(monitor, primaryMonitor, placedMonitors);
                }
            }
            addAsPlaced(monitor, placedMonitors);
        }
    }

    private static void secondaryPlaceWestEast(Monitor monitor, Monitor primaryMonitor, List<Monitor> placedMonitors) {
        if (monitor.getRight() <= primaryMonitor.getLeft()) {
            placeMonitorEast(placedMonitors, monitor);
        }
        if (monitor.getLeft() >= primaryMonitor.getRight()) {
            placeMonitorWest(placedMonitors, monitor);
        }
    }

    private static void secondaryPlaceNorthSouth(Monitor monitor, Monitor primaryMonitor, List<Monitor> placedMonitors) {
        if (monitor.getBottom() <= primaryMonitor.getTop()) {
            placeMonitorSouth(placedMonitors, monitor);
        }
        if (monitor.getTop() >= primaryMonitor.getBottom()) {
            placeMonitorNorth(placedMonitors, monitor);
        }
    }

    public static Rectangle2D getRectangle2D(Rectangle rectangle) {
        return new Rectangle2D(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    private static void addAsPlaced(Monitor monitor, List<Monitor> placedMonitors) {
        monitor.setPlaced(true);
        placedMonitors.add(monitor);
    }

    private static void applyDpiForSize(Monitor monitor) {
        double dpiFactor = monitor.getDpiScale();
        monitor.setDpiWidth(monitor.getWidth() / dpiFactor);
        monitor.setDpiHeight(monitor.getHeight() / dpiFactor);
    }

    private static void clearMonitors(List<Monitor> monitors) {
        for (Monitor monitor : monitors) {
            monitor.clear();
        }
    }

    public static double getAngleBetweenRectangles(Rectangle rect1, Rectangle rect2) {
        // Find the middle points of both rectangles
        double x1 = (rect1.getMinX() + rect1.getMinX()) / 2;
        double y1 = (rect1.getMinY() + rect1.getMinY()) / 2;
        double x2 = (rect2.getMinX() + rect2.getMinX()) / 2;
        double y2 = (rect2.getMinY() + rect2.getMinY()) / 2;

        // Calculate the angle between the two middle points using atan2
        double angleRad = Math.atan2(y2 - y1, x2 - x1);

        // Convert the angle from radians to degrees
        double angleDeg = Math.toDegrees(angleRad);

        // Return the angle, ensuring it's between 0 and 360 degrees
        if (angleDeg < 0) {
            angleDeg += 360;
        }

        return angleDeg;
    }

    private static MagnetDirection getMagnetDirection(Monitor primaryMonitor, Monitor monitor) {
        double angle = getAngleBetweenRectangles(primaryMonitor.getMonitorArea(), monitor.getMonitorArea());
        // Based on the angle, determine the direction
        if (angle > 135 && angle <= 225) {
            return MagnetDirection.EAST;  // Right side
        } else if (angle > 45 && angle <= 135) {
            return MagnetDirection.SOUTH; // Bottom side
        } else if (angle > 315 || angle <= 45) {
            return MagnetDirection.WEST;  // Left side
        } else {
            return MagnetDirection.NORTH; // Top side
        }
    }

    private static void placeMonitorNorth(List<Monitor> placedMonitors, Monitor monitor) {
        placeMonitorNorthOrSouth(placedMonitors, monitor, -1, getMinY(placedMonitors));
    }

    private static void placeMonitorWest(List<Monitor> placedMonitors, Monitor monitor) {
        placeMonitorWestOrEast(placedMonitors, monitor, -1, getMinX(placedMonitors));
    }

    private static void placeMonitorSouth(List<Monitor> placedMonitors, Monitor monitor) {
        placeMonitorNorthOrSouth(placedMonitors, monitor, 1, getMaxY(placedMonitors));
    }

    private static void placeMonitorEast(List<Monitor> placedMonitors, Monitor monitor) {
        placeMonitorWestOrEast(placedMonitors, monitor, 1, getMaxX(placedMonitors));
    }

    private static void placeMonitorWestOrEast(List<Monitor> placedMonitors, Monitor monitor, int i, double edge) {
        double newX = monitor.getX();
        int d = getD(i);
        while (d * (newX - edge) < 0) {
            newX = newX + i;
            monitor.setDpiX(newX);
            if (isOverlappingOrTouching(placedMonitors, monitor)) {
                if (isOverlapping(placedMonitors, monitor)) {
                    newX = newX - i;
                }
                break;
            }
        }
        monitor.setDpiX(newX);
    }

    private static int getD(int i) {
        int d = 1;
        if (i < 0) {
            d = -1;
        }
        return d;
    }

    private static void placeMonitorNorthOrSouth(List<Monitor> placedMonitors, Monitor monitor, int i, double edge) {
        double newY = monitor.getY();
        int d = getD(i);
        while (d * (newY - edge) < 0) {
            newY = newY + i;
            monitor.setDpiY(newY);
            if (isOverlappingOrTouching(placedMonitors, monitor)) {
                if (isOverlapping(placedMonitors, monitor)) {
                    newY = newY - i;
                }
                break;
            }
        }
        monitor.setDpiY(newY);
    }

    private static double getMinX(List<Monitor> placedMonitors) {
        if (placedMonitors.size() == 0) {
            return 0.0;
        }
        double minX = placedMonitors.get(0).getDpiX();
        for (Monitor monitor : placedMonitors) {
            minX = Math.min(minX, monitor.getDpiX());
        }
        return minX;
    }

    private static double getMinY(List<Monitor> placedMonitors) {
        if (placedMonitors.size() == 0) {
            return 0.0;
        }
        double minY = placedMonitors.get(0).getDpiY();
        for (Monitor monitor : placedMonitors) {
            minY = Math.min(minY, monitor.getDpiY());
        }
        return minY;
    }

    private static double getMaxX(List<Monitor> placedMonitors) {
        if (placedMonitors.size() == 0) {
            return 0.0;
        }
        double maxX = placedMonitors.get(0).getDpiX();
        for (Monitor monitor : placedMonitors) {
            maxX = Math.max(maxX, monitor.getDpiX());
        }
        return maxX;
    }

    private static double getMaxY(List<Monitor> placedMonitors) {
        if (placedMonitors.size() == 0) {
            return 0.0;
        }
        double maxY = placedMonitors.get(0).getDpiY();
        for (Monitor monitor : placedMonitors) {
            maxY = Math.max(maxY, monitor.getDpiY());
        }
        return maxY;
    }

    private static boolean isOverlapping(List<Monitor> placedMonitors, Monitor monitor) {
        for (Monitor placedMonitor : placedMonitors) {
            if (calculateOverlap(placedMonitor.getRDpiMonitorArea(), monitor.getRDpiMonitorArea()) > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEdgeTouching(Rectangle2D rect1, Rectangle2D rect2) {
        // Check if edges are aligned and touching horizontally
        boolean isHorizontallyAlignedAndAdjacent =
                rect1.getMaxX() == rect2.getMinX() && // rect1's right edge touches rect2's left edge
                        rect1.getMaxY() > rect2.getMinY() && // Ensure some vertical alignment
                        rect1.getMinY() < rect2.getMaxY();   // Ensure some vertical alignment
        boolean isVerticallyAlignedAndAdjacent =
                rect1.getMaxY() == rect2.getMinY() && // rect1's bottom edge touches rect2's top edge
                        rect1.getMaxX() > rect2.getMinX() && // Ensure some horizontal alignment
                        rect1.getMinX() < rect2.getMaxX();   // Ensure some horizontal alignment

        // Check corner touching cases
        boolean isCornerTouching =
                (rect1.getMaxX() == rect2.getMinX() && // Right edge of rect1 touches left edge of rect2
                        (rect1.getMaxY() == rect2.getMinY() || rect1.getMinY() == rect2.getMaxY())) || // Top-right or bottom-right
                        (rect1.getMinX() == rect2.getMaxX() && // Left edge of rect1 touches right edge of rect2
                                (rect1.getMaxY() == rect2.getMinY() || rect1.getMinY() == rect2.getMaxY()));  // Top-left or bottom-left

        // Edge touching includes proper alignment and no overlap
        return isHorizontallyAlignedAndAdjacent || isVerticallyAlignedAndAdjacent || isCornerTouching;
    }

    private static boolean isOverlappingOrTouching(List<Monitor> placedMonitors, Monitor monitor) {
        for (Monitor placedMonitor : placedMonitors) {
            Rectangle2D rDpiMonitorArea = placedMonitor.getRDpiMonitorArea();
            Rectangle2D rDpiMonitorArea2 = monitor.getRDpiMonitorArea();
            if (calculateOverlap(rDpiMonitorArea, rDpiMonitorArea2) > 0) {
                return true;
            }
            if (isEdgeTouching(rDpiMonitorArea, rDpiMonitorArea2)) {
                return true;
            }
        }
        return false;
    }

    private static void placePrimaryMonitor(Monitor primaryMonitor, List<Monitor> placedMonitors) {
        addAsPlaced(primaryMonitor, placedMonitors);
        double dpiFactor = primaryMonitor.getDpiScale();
        primaryMonitor.setDpiX(primaryMonitor.getX() / dpiFactor);
        primaryMonitor.setDpiY(primaryMonitor.getY() / dpiFactor);
        primaryMonitor.setDpiWidth(primaryMonitor.getWidth() / dpiFactor);
        primaryMonitor.setDpiHeight(primaryMonitor.getHeight() / dpiFactor);
    }

    private static Monitor filterPrimaryMonitor(List<Monitor> monitors) {
        for (Monitor monitor : monitors) {
            if (monitor.isPrimaryMonitor()) {
                return monitor;
            }
        }
        return null;
    }

    private void applyMonitorInfos(List<Monitor> monitors) {
        List<MonitorInfo> monitorInfos = getMonitorInfos();
        clearMonitorInfosUsed(monitorInfos);
        for (Monitor monitor : monitors) {
            linkMonitorInfoToMonitor(monitor, monitorInfos);
        }
    }

    private static void linkMonitorInfoToMonitor(Monitor monitor, List<MonitorInfo> monitorInfos) {
        for (MonitorInfo monitorInfo : monitorInfos) {
            if (!monitorInfo.isUsed() && monitor.samePosition(monitorInfo.getX(), monitorInfo.getY())) {
                monitor.setIdentifier(monitorInfo.getMonitorIdentifier());
                monitorInfo.setUsed(true);
                return;
            }
        }
    }

    private void clearMonitorInfosUsed(List<MonitorInfo> monitorInfos) {
        for (MonitorInfo monitorInfo : monitorInfos) {
            monitorInfo.setUsed(false);
        }
    }

    private List<MonitorInfo> getMonitorInfos() {
        try {
            if (monitorInfos != null) {
                return monitorInfos;
            }
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();
            Type listType = new TypeToken<ArrayList<MonitorInfo>>() {
            }.getType();

            String json = getMonitorsJson();
            List<MonitorInfo> monitorInfos = gson.fromJson(json, listType);
            this.monitorInfos = monitorInfos;
            return monitorInfos;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void clearMonitorInfos() {
        this.monitorInfos = null;
        this.extendedMonitors = null;
    }

    public static String getMonitorsJson() {
        Date startDate = new Date();
        try {
            try {
                String pythonExePath = AppProperties.getInstance().getWorkDirectory() + "app/get_monitors.exe";
                return executeExe(pythonExePath);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } finally {
            System.out.println(new Date().getTime() - startDate.getTime());
        }
        return "[]";
    }

    private static String executeExe(String pythonExePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(pythonExePath);
        processBuilder.redirectErrorStream(true);

        // Start the process
        Process process = processBuilder.start();

        // Capture and print the output of the Python script
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder s = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            s.append(line);
        }
        return s.toString();
    }
}
