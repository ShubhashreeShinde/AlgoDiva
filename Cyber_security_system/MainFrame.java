import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private GraphPanel graphPanel;
    private ControlPanel controlPanel;

    public MainFrame() {
        setTitle("Network Infection Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 680);
        setMinimumSize(new Dimension(700, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(14, 16, 36));
        setLayout(new BorderLayout());

        launchSimulation();
    }

    private void launchSimulation() {
        InputDialog dialog = new InputDialog(this);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            System.exit(0);
        }

        // Build network
        NetworkGraph network = new NetworkGraph(dialog.getNetworkSize());
        for (int[] edge : dialog.getEdgeList()) {
            network.addEdge(edge[0], edge[1], edge[2]);
        }
        network.infectInitialDevice(dialog.getInfectedId());

        // Remove old panels
        if (graphPanel != null)   remove(graphPanel);
        if (controlPanel != null) remove(controlPanel);

        // Create new panels
        graphPanel   = new GraphPanel(network);
        controlPanel = new ControlPanel(network, graphPanel, this::launchSimulation);

        add(graphPanel,   BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();

        // Init positions after layout is done and panel has real size
        SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
            graphPanel.initPositions();
        }));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Dark title bar on supported systems
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
