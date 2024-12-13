package projector.utils.monitors;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.awt.*;

import static projector.utils.monitors.MonitorInfo.SEPARATOR;

public class Monitor {

    private String monitorDeviceId;
    private Rectangle monitorArea;
    private boolean primaryMonitor;
    private double dpiScale = 1.0;
    private String identifier;
    private double dpiX;
    private double dpiY;
    private double dpiWidth;
    private double dpiHeight;
    private boolean placed = false;
    private Screen screen;

    public void setMonitorDeviceId(String monitorDeviceId) {
        this.monitorDeviceId = monitorDeviceId;
    }

    private String getMonitorDeviceId() {
        return monitorDeviceId;
    }

    public void setMonitorArea(Rectangle monitorArea) {
        this.monitorArea = monitorArea;
    }

    public Rectangle getMonitorArea() {
        return monitorArea;
    }

    public Rectangle2D getRDpiMonitorArea() {
        return new Rectangle2D(dpiX, dpiY, dpiWidth, dpiHeight);
    }

    public void setPrimaryMonitor(boolean primaryMonitor) {
        this.primaryMonitor = primaryMonitor;
    }

    public boolean isPrimaryMonitor() {
        return primaryMonitor;
    }

    public void setDpiScale(double dpiScale) {
        this.dpiScale = dpiScale;
    }

    public double getDpiScale() {
        return dpiScale;
    }

    public double getWidth() {
        return monitorArea.getWidth();
    }

    public double getHeight() {
        return monitorArea.getHeight();
    }

    public boolean samePosition(int x, int y) {
        return monitorArea.x == x && monitorArea.y == y;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public static String makeFilenameSafe(String filename) {
        // Keep a-z, A-Z, 0-9, -, _, space, period, comma (and add any other good characters)
        return filename.replaceAll("[^a-zA-Z0-9-_ .,]", "");
    }

    public String getMonitorIdentifier() {
        return makeFilenameSafe(identifier + SEPARATOR + monitorDeviceId);
    }

    public double getX() {
        return monitorArea.getX();
    }

    public double getY() {
        return monitorArea.getY();
    }

    public void setDpiX(double dpiX) {
        this.dpiX = dpiX;
    }

    public double getDpiX() {
        return dpiX;
    }

    public void setDpiY(double dpiY) {
        this.dpiY = dpiY;
    }

    public double getDpiY() {
        return dpiY;
    }

    public void setDpiWidth(double dpiWidth) {
        this.dpiWidth = dpiWidth;
    }

    public double getDpiWidth() {
        return dpiWidth;
    }

    public void setDpiHeight(double dpiHeight) {
        this.dpiHeight = dpiHeight;
    }

    public double getDpiHeight() {
        return dpiHeight;
    }

    public void clear() {
        placed = false;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public double getBottom() {
        return monitorArea.getMaxY();
    }

    public double getTop() {
        return getY();
    }

    public double getLeft() {
        return getX();
    }

    public double getRight() {
        return monitorArea.getMaxX();
    }
}
