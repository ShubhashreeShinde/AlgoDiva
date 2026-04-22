import java.util.Objects;

public class Device {
    private int deviceId;
    private boolean isInfected;

    // Constructor
    public Device(int deviceId) {
        this.deviceId = deviceId;
        this.isInfected = false;
    }

    // Getters
    public int getDeviceId() {
        return this.deviceId;
    }

    public boolean isInfected() {
        return this.isInfected;
    }

    // Setter
    public void setInfected(boolean infected) {
        this.isInfected = infected;
    }

    // toString
    @Override
    public String toString() {
        return "D" + deviceId + (isInfected ? "[INFECTED]" : "[SAFE]");
    }

    // equals and hashCode based on deviceId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device)) return false;
        Device d = (Device) o;
        return this.deviceId == d.deviceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }
}