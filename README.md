# Network Infection Simulator

## Problem Statement

In a computer network, devices are connected to each other through communication links. When a virus infects one device, it can spread to neighbouring devices through these links. The goal of this simulation is to model how a virus spreads through a hybrid network topology and how a containment system attempts to stop it.

Given a network of devices with weighted edges (where the weight represents the strength or speed of the connection), the simulation works as follows:

- The virus always spreads through the **lowest weight (fastest) edge** at each step — modelling how a virus naturally exploits the strongest available connection.
- A containment system simultaneously tries to **break the highest weight (weakest) boundary edge** at each step — cutting off the least critical link to isolate the infection.
- The simulation continues until the virus can no longer reach any safe device.
- The final result shows how many devices were saved versus infected.

The network is a **hybrid topology** — the user defines exactly which devices are connected and at what weight, allowing any combination of bus, ring, mesh, star, or mixed structures.

---

## Data Structures Used

### 1. `Device` — Graph Node
Represents a single device in the network.

| Field | Type | Purpose |
|---|---|---|
| `deviceId` | `int` | Unique identifier for the device |
| `isInfected` | `boolean` | Tracks whether this device is infected |

### 2. `Edge` — Graph Edge
Represents a weighted, undirected link between two devices.

| Field | Type | Purpose |
|---|---|---|
| `deviceA` | `Device` | One endpoint of the connection |
| `deviceB` | `Device` | Other endpoint of the connection |
| `weight` | `int` | Cost/strength of the link — lower = faster/stronger |
| `isBroken` | `boolean` | Whether this link has been cut by containment |

### 3. `NetworkGraph` — Weighted Undirected Graph
The core data structure of the project. Stores the entire network and runs the simulation.

| Field | Type | Purpose |
|---|---|---|
| `devices` | `List<Device>` | All nodes in the graph |
| `edges` | `List<Edge>` | All edges in the graph |
| `infectedSet` | `Set<Device>` | Set of currently infected devices |

**Key operations:**
- `getBoundaryEdges()` — scans all edges to find those with exactly one infected endpoint (O(E))
- `getCheapestBoundaryEdge()` — linear scan of boundary edges for minimum weight
- `getMostExpensiveBoundaryEdge()` — linear scan of boundary edges for maximum weight
- `spreadNextStep()` — infects device at cheapest boundary edge
- `containOneStep()` — breaks most expensive boundary edge

### 4. Supporting UI Structures
- `Map<Device, Point>` — maps each device to its (x, y) screen coordinate for the drag-and-drop canvas
- `javax.swing.Timer` — drives the auto-run animation loop

### Why a Graph?

A **weighted undirected graph** was chosen over a linked list (used in the original Bus/Ring version) because:

- Devices can have **more than 2 neighbours** — supports mesh, star, and hybrid topologies
- **Link weight** adds realism — the virus uses the fastest path, containment cuts the weakest link
- **Any topology** can be represented by simply adding edges — no hardcoded structure needed
- The infection boundary is naturally represented as edges crossing between the infected set and the safe set

---

## How to Run

### Prerequisites
- Java 11 or above
- VS Code with the **Extension Pack for Java** (by Microsoft)

### Steps
1. Place all `.java` files in the same folder
2. Open `MainFrame.java` in VS Code
3. Click the **Run** button (top right) or right-click → **Run Java**
4. A setup dialog will appear — enter devices, edges, and the infected device ID
5. Drag devices to arrange the graph, then use **Next Step** or **Auto Run**

### File Structure
```
NetworkSimulation/
├── Device.java          — Graph node (device)
├── Edge.java            — Graph edge (weighted link)
├── NetworkGraph.java    — Core graph + simulation logic
├── GraphPanel.java      — Visual canvas with drag support
├── ControlPanel.java    — Simulation controls (next step, auto run, reset)
├── InputDialog.java     — Setup popup (devices, edges, infected ID)
└── MainFrame.java       — Main window and entry point
```

---

## Sample Test Cases

| Test | Devices | Infected | Edge Setup | What to Observe |
|---|---|---|---|---|
| Linear chain | 5 | 1 | 1-2-3-4-5 in line | One direction spread |
| Full ring | 5 | 3 | Circular connections | Bidirectional spread race |
| Star topology | 6 | 1 (hub) | Hub connected to all | All edges boundary at once |
| Two clusters + bridge | 6 | 1 | Two groups, one heavy bridge | Heavy bridge cut, second cluster saved |
| Disconnected device | 7 | 2 | D7 has no edges | Isolated device always safe |

---

## Demo Video

> https://drive.google.com/file/d/1C2jR7HCGB_JO214X6DwtT8f-hCYeaqPL/view?usp=sharing

---

## Authors

- **Shubhashree Shinde** — B.Tech Electronics and Telecommunication Engineering, Cummins College of Engineering for Women, Pune
