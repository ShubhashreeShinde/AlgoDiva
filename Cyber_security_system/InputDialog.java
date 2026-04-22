import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InputDialog extends JDialog {

    private JTextField sizeField;
    private JTextField infectedField;
    private DefaultTableModel edgeTableModel;

    private int networkSize = -1;
    private int infectedId  = -1;
    private List<int[]> edgeList = new ArrayList<>();
    private boolean confirmed = false;

    private static final Color BG       = new Color(14, 16, 36);
    private static final Color PANEL_BG = new Color(20, 24, 50);
    private static final Color ACCENT   = new Color(70, 130, 240);
    private static final Color FG       = new Color(210, 220, 255);
    private static final Color FG_DIM   = new Color(120, 130, 170);

    public InputDialog(JFrame parent) {
        super(parent, "Network Setup", true);
        setUndecorated(false);
        buildUI();
        setSize(520, 540);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(BG);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(10, 12, 28));
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Network Configuration");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(FG);

        JLabel sub = new JLabel("Define devices, edges, and initial infection");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(FG_DIM);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 3));
        text.setBackground(new Color(10, 12, 28));
        text.add(title);
        text.add(sub);
        p.add(text, BorderLayout.WEST);
        return p;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));

        // Size + infected row
        JPanel topRow = new JPanel(new GridLayout(1, 2, 14, 0));
        topRow.setBackground(BG);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        sizeField     = styledField("e.g. 6");
        infectedField = styledField("e.g. 1");

        topRow.add(labeledField("Number of Devices (1–15)", sizeField));
        topRow.add(labeledField("Initial Infected Device ID", infectedField));
        center.add(topRow);
        center.add(Box.createVerticalStrut(14));

        // Edge table
        JLabel edgeLbl = new JLabel("Edges  —  Device A, Device B, Weight");
        edgeLbl.setForeground(FG_DIM);
        edgeLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        edgeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(edgeLbl);
        center.add(Box.createVerticalStrut(6));

        String[] cols = {"Device A", "Device B", "Weight"};
        edgeTableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(edgeTableModel);
        table.setBackground(PANEL_BG);
        table.setForeground(FG);
        table.setGridColor(new Color(35, 40, 75));
        table.setSelectionBackground(new Color(50, 80, 160));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(26);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(25, 30, 65));
        header.setForeground(FG_DIM);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(PANEL_BG);
        scroll.getViewport().setBackground(PANEL_BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 50, 100)));
        scroll.setPreferredSize(new Dimension(460, 180));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(scroll);
        center.add(Box.createVerticalStrut(8));

        // Add / Remove buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addBtn = smallBtn("+ Add Row", new Color(40, 90, 160));
        JButton remBtn = smallBtn("− Remove Last", new Color(100, 35, 35));
        addBtn.addActionListener(e -> edgeTableModel.addRow(new Object[]{"", "", ""}));
        remBtn.addActionListener(e -> {
            int r = edgeTableModel.getRowCount();
            if (r > 0) edgeTableModel.removeRow(r - 1);
        });

        btnRow.add(addBtn);
        btnRow.add(remBtn);
        center.add(btnRow);

        return center;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        p.setBackground(new Color(10, 12, 28));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 40, 80)));

        JButton cancel = smallBtn("Cancel", new Color(50, 55, 80));
        JButton ok     = smallBtn("▶  Start Simulation", new Color(40, 120, 60));
        ok.setPreferredSize(new Dimension(160, 34));

        cancel.addActionListener(e -> { confirmed = false; dispose(); });
        ok.addActionListener(e -> handleOk());

        p.add(cancel);
        p.add(ok);
        return p;
    }

    private void handleOk() {
        try {
            networkSize = Integer.parseInt(sizeField.getText().trim());
            if (networkSize < 1 || networkSize > 15) { err("Devices must be between 1 and 15."); return; }
        } catch (NumberFormatException e) { err("Enter a valid number for devices."); return; }

        try {
            infectedId = Integer.parseInt(infectedField.getText().trim());
            if (infectedId < 1 || infectedId > networkSize) { err("Infected ID must be 1–" + networkSize + "."); return; }
        } catch (NumberFormatException e) { err("Enter a valid infected device ID."); return; }

        edgeList.clear();
        for (int i = 0; i < edgeTableModel.getRowCount(); i++) {
            try {
                int a = Integer.parseInt(edgeTableModel.getValueAt(i, 0).toString().trim());
                int b = Integer.parseInt(edgeTableModel.getValueAt(i, 1).toString().trim());
                int w = Integer.parseInt(edgeTableModel.getValueAt(i, 2).toString().trim());
                if (a < 1 || a > networkSize || b < 1 || b > networkSize) { err("Row " + (i+1) + ": IDs must be 1–" + networkSize); return; }
                if (a == b) { err("Row " + (i+1) + ": Self-loop not allowed."); return; }
                if (w <= 0) { err("Row " + (i+1) + ": Weight must be positive."); return; }
                edgeList.add(new int[]{a, b, w});
            } catch (NumberFormatException | NullPointerException ex) {
                err("Row " + (i+1) + ": Fill all three values."); return;
            }
        }

        if (edgeList.isEmpty()) { err("Add at least one edge."); return; }

        confirmed = true;
        dispose();
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- Helpers ----

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(PANEL_BG);
        f.setForeground(FG);
        f.setCaretColor(FG);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 60, 110)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        f.setToolTipText(placeholder);
        return f;
    }

    private JPanel labeledField(String labelText, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(BG);
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(FG_DIM);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JButton smallBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Getters
    public boolean isConfirmed()      { return confirmed; }
    public int getNetworkSize()       { return networkSize; }
    public int getInfectedId()        { return infectedId; }
    public List<int[]> getEdgeList()  { return edgeList; }
}
