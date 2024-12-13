package projector.utils.monitors;

import java.util.List;

public class MonitorInfo {
    private String manufacturer;
    private String model;
    private String serialNumber;
    private List<Integer> position;
    private List<Integer> resolution;
    private Integer x;
    private Integer y;
    public static final String SEPARATOR = "-";
    private boolean used;

    // Constructor
    public MonitorInfo(String manufacturer, String model, String serialNumber, List<Integer> position, List<Integer> resolution) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.position = position;
        this.resolution = resolution;
    }

    // Getters and setters
    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getX() {
        if (this.x == null) {
            this.x = getIntFromPosition(0);
        }
        return this.x;
    }

    private int getIntFromPosition(int index) {
        if (position == null || position.size() <= index) {
            return -1;
        }
        return position.get(index);
    }

    public int getY() {
        if (this.y == null) {
            this.y = getIntFromPosition(1);
        }
        return this.y;
    }

    public void setPosition(List<Integer> position) {
        this.position = position;
    }

    public List<Integer> getResolution() {
        return resolution;
    }

    public void setResolution(List<Integer> resolution) {
        this.resolution = resolution;
    }

    public String getMonitorIdentifier() {
        return manufacturer + SEPARATOR + model + SEPARATOR + serialNumber;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }
}