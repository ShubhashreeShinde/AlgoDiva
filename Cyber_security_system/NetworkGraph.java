import java.util.*;

public class NetworkGraph {
    private List<Device> devices;
    private List<Edge> edges;
    private int size;
    private Set<Device> infectedSet;

    // Constructor
    public NetworkGraph(int n) {
        this.size = n;
        this.devices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.infectedSet = new HashSet<>();

        for (int i = 1; i <= n; i++) {
            devices.add(new Device(i));
        }
    }

    // Getters needed by UI
    public int getSize() {
        return size;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    // Edge Building
    public void addEdge(int idA, int idB, int weight) {
        Device a = getDeviceById(idA);
        Device b = getDeviceById(idB);

        if (a == null || b == null) {
            System.out.println("Invalid device ID in edge: " + idA + " " + idB);
            return;
        }

        if (a.equals(b)) {
            System.out.println("Self loop not allowed: " + idA);
            return;
        }

        if (getEdgeBetween(a, b) != null) {
            System.out.println("Edge already exists between D" + idA + " and D" + idB);
            return;
        }

        if (weight <= 0) {
            System.out.println("Weight must be positive");
            return;
        }

        edges.add(new Edge(a, b, weight));
    }

    // Lookup Methods
    public Device getDeviceById(int id) {
        for (Device d : devices) {
            if (d.getDeviceId() == id) return d;
        }
        return null;
    }

    public Edge getEdgeBetween(Device a, Device b) {
        for (Edge e : edges) {
            if (e.connects(a, b)) return e;
        }
        return null;
    }

    public List<Device> getActiveNeighbours(Device d) {
        List<Device> neighbours = new ArrayList<>();
        for (Edge e : edges) {
            if (!e.isBroken()) {
                Device other = e.getOtherEnd(d);
                if (other != null) neighbours.add(other);
            }
        }
        return neighbours;
    }

    // Boundary Edge Methods
    public List<Edge> getBoundaryEdges() {
        List<Edge> boundary = new ArrayList<>();
        for (Edge e : edges) {
            if (e.isBroken()) continue;
            boolean aInfected = e.getDeviceA().isInfected();
            boolean bInfected = e.getDeviceB().isInfected();
            if (aInfected != bInfected) {
                boundary.add(e);
            }
        }
        return boundary;
    }

    public Edge getCheapestBoundaryEdge() {
        List<Edge> boundary = getBoundaryEdges();
        if (boundary.isEmpty()) return null;

        Edge cheapest = boundary.get(0);
        for (Edge e : boundary) {
            if (e.getWeight() < cheapest.getWeight()) cheapest = e;
        }
        return cheapest;
    }

    public Edge getMostExpensiveBoundaryEdge() {
        List<Edge> boundary = getBoundaryEdges();
        if (boundary.isEmpty()) return null;

        Edge expensive = boundary.get(0);
        for (Edge e : boundary) {
            if (e.getWeight() > expensive.getWeight()) expensive = e;
        }
        return expensive;
    }

    // Simulation Methods
    public void infectInitialDevice(int id) {
        Device d = getDeviceById(id);

        if (d == null) {
            System.out.println("Device not found: " + id);
            return;
        }

        d.setInfected(true);
        infectedSet.add(d);
        System.out.println("Initial infection at D" + id);
    }

    public boolean canSpread() {
        return !getBoundaryEdges().isEmpty();
    }

    public void spreadNextStep() {
        Edge cheapest = getCheapestBoundaryEdge();
        if (cheapest == null) return;

        Device toInfect = cheapest.getDeviceA().isInfected()
                ? cheapest.getDeviceB()
                : cheapest.getDeviceA();

        toInfect.setInfected(true);
        infectedSet.add(toInfect);
        System.out.println("Virus spread to " + toInfect + " via weight " + cheapest.getWeight());
    }

    public void containOneStep() {
        Edge expensive = getMostExpensiveBoundaryEdge();
        if (expensive == null) return;

        expensive.setBroken(true);
        System.out.println("Link broken: " + expensive);
    }

    public void simulate() {
        while (canSpread()) {
            spreadNextStep();
            containOneStep();
        }
        System.out.println("Simulation finished.");
        System.out.println("Safe: " + countSafeDevices() + " | Infected: " + countInfectedDevices());
    }

    // Display Methods
    public void displayNetwork() {
        for (Device d : devices) {
            System.out.print(d + " | Neighbours: ");
            List<String> links = new ArrayList<>();
            for (Edge e : edges) {
                Device other = e.getOtherEnd(d);
                if (other != null) {
                    links.add(e.isBroken()
                            ? other + "(X)"
                            : other + "(" + e.getWeight() + ")");
                }
            }
            System.out.println(String.join(", ", links));
        }
    }

    public int countSafeDevices() {
        int count = 0;
        for (Device d : devices) {
            if (!d.isInfected()) count++;
        }
        return count;
    }

    public int countInfectedDevices() {
        return size - countSafeDevices();
    }
}