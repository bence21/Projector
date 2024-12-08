package projector.utils;

import javafx.geometry.Rectangle2D;

import java.awt.*;

public class Monitor {

    private String monitorDeviceId;
    private Rectangle monitorArea;
    private boolean primaryMonitor;
    private double dpiScale = 1.0;

    public void setMonitorDeviceId(String monitorDeviceId) {
        this.monitorDeviceId = monitorDeviceId;
    }

    public String getMonitorDeviceId() {
        return monitorDeviceId;
    }

    public void setMonitorArea(Rectangle monitorArea) {
        this.monitorArea = monitorArea;
    }

    public Rectangle getMonitorArea() {
        return monitorArea;
    }

    public Rectangle2D getRDpiMonitorArea() {
        double primaryMonitorDpiScale = MonitorUtil.getInstance().getPrimaryMonitor().getDpiScale();
        return new Rectangle2D(
                (monitorArea.getX() / primaryMonitorDpiScale),
                (monitorArea.getY()),
                (monitorArea.getWidth() / dpiScale),
                (monitorArea.getHeight() / dpiScale)
        );
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
}
