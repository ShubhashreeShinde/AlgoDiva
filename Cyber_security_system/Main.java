import java.util.Scanner;

public class Main {

    private static final int MAX_DEVICES = 15;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=======================================");
        System.out.println("       NETWORK SIMULATION SYSTEM       ");
        System.out.println("=======================================");

        // Input network size
        System.out.print("Enter number of devices (1 - 15): ");
        int size = sc.nextInt();

        if (!validateSize(size)) {
            System.out.println("Invalid size! Must be between 1 and 15.");
            sc.close();
            return;
        }

        // Create graph
        NetworkGraph network = new NetworkGraph(size);

        // Input edges
        System.out.println("\nEnter edges (format: deviceA deviceB weight)");
        System.out.println("Type -1 to stop:");

        while (true) {
            int a = sc.nextInt();
            if (a == -1) break;
            int b = sc.nextInt();
            int w = sc.nextInt();
            network.addEdge(a, b, w);
        }

        // Input infected device
        System.out.print("\nEnter initial infected device ID (1 - " + size + "): ");
        int infectedId = sc.nextInt();

        if (!validateId(infectedId, size)) {
            System.out.println("Invalid device ID!");
            sc.close();
            return;
        }

        // Run simulation
        System.out.println("\n=======================================");
        System.out.println("         Starting Simulation           ");
        System.out.println("=======================================\n");

        network.displayNetwork();
        System.out.println();

        network.infectInitialDevice(infectedId);
        System.out.println();

        network.displayNetwork();
        System.out.println();

        network.simulate();

        System.out.println("\n=======================================");
        System.out.println("           Simulation Ended            ");
        System.out.println("=======================================");

        sc.close();
    }

    private static boolean validateSize(int size) {
        return size >= 1 && size <= MAX_DEVICES;
    }

    private static boolean validateId(int id, int size) {
        return id >= 1 && id <= size;
    }
}