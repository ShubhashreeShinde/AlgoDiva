public class Edge {
    private Device deviceA;
    private Device deviceB;
    private int weight;
    private boolean isBroken;

    // Constructor
    public Edge(Device deviceA, Device deviceB, int weight) {
        this.deviceA = deviceA;
        this.deviceB = deviceB;
        this.weight = weight;
        this.isBroken = false;
    }

    // Getters
    public Device getDeviceA() {
        return this.deviceA;
    }

    public Device getDeviceB() {
        return this.deviceB;
    }

    public int getWeight() {
        return this.weight;
    }

    public boolean isBroken() {
        return this.isBroken;
    }

    // Setter
    public void setBroken(boolean broken) {
        this.isBroken = broken;
    }

    // Utility Methods
    public boolean connects(Device a, Device b) {
        return (deviceA.equals(a) && deviceB.equals(b)) ||
               (deviceA.equals(b) && deviceB.equals(a));
    }

    public Device getOtherEnd(Device d) {
        if (deviceA.equals(d)) return deviceB;
        if (deviceB.equals(d)) return deviceA;
        return null;
    }

    // toString
    @Override
    public String toString() {
        String link = isBroken ? " --X-- " : " --" + weight + "-- ";
        return deviceA + link + deviceB;
    }
}