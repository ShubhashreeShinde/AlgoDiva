import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ControlPanel extends JPanel {

    private NetworkGraph network;
    private GraphPanel graphPanel;
    private Runnable onReset;

    private JButton nextStepBtn;
    private JButton autoRunBtn;
    private JButton resetBtn;
    private JSlider speedSlider;
    private JLabel statusLabel;
    private JLabel stepLabel;
    private JLabel safeLabel;
    private JLabel infectedLabel;

    private Timer autoTimer;
    private boolean autoRunning = false;
    private int stepCount = 0;

    public ControlPanel(NetworkGraph network, GraphPanel graphPanel, Runnable onReset) {
        this.network = network;
        this.graphPanel = graphPanel;
        this.onReset = onReset;

        setBackground(new Color(14, 16, 36));
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 90)));

        buildUI();
        updateStatus();
    }

    private void buildUI() {
        // ---- STATS ROW (top of control panel) ----
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 6));
        statsPanel.setBackground(new Color(10, 12, 28));
        statsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 40, 80)));

        stepLabel     = makeStatLabel("Step: 0", new Color(160, 170, 210));
        safeLabel     = makeStatLabel("Safe: –", new Color(56, 193, 114));
        infectedLabel = makeStatLabel("Infected: –", new Color(229, 57, 53));

        statsPanel.add(makeStatChip("STEP",     stepLabel,     new Color(50, 60, 100)));
        statsPanel.add(makeStatChip("SAFE",     safeLabel,     new Color(30, 80, 50)));
        statsPanel.add(makeStatChip("INFECTED", infectedLabel, new Color(80, 30, 30)));

        add(statsPanel, BorderLayout.NORTH);

        // ---- BUTTONS ROW ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        btnPanel.setBackground(new Color(14, 16, 36));

        nextStepBtn = makeBtn("▶  Next Step", new Color(50, 100, 200), new Color(70, 130, 240));
        autoRunBtn  = makeBtn("⏩  Auto Run",  new Color(30, 120, 70),  new Color(40, 160, 90));
        resetBtn    = makeBtn("↺  Reset",      new Color(140, 40, 40),  new Color(190, 60, 60));

        nextStepBtn.addActionListener(e -> doNextStep());
        autoRunBtn.addActionListener(e -> toggleAutoRun());
        resetBtn.addActionListener(e -> doReset());

        // Speed label + slider
        JLabel slowLbl = tinyLabel("Slow");
        JLabel fastLbl = tinyLabel("Fast");

        speedSlider = new JSlider(200, 2000, 900);
        speedSlider.setInverted(true);
        speedSlider.setPreferredSize(new Dimension(110, 28));
        speedSlider.setBackground(new Color(14, 16, 36));
        speedSlider.addChangeListener(e -> { if (autoTimer != null) autoTimer.setDelay(speedSlider.getValue()); });

        JPanel sliderWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        sliderWrap.setBackground(new Color(14, 16, 36));
        sliderWrap.add(fastLbl);
        sliderWrap.add(speedSlider);
        sliderWrap.add(slowLbl);

        btnPanel.add(nextStepBtn);
        btnPanel.add(autoRunBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(Box.createHorizontalStrut(16));
        btnPanel.add(sliderWrap);

        add(btnPanel, BorderLayout.CENTER);

        // ---- LEGEND ROW ----
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 5));
        legendPanel.setBackground(new Color(10, 12, 28));
        legendPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 40, 80)));

        legendPanel.add(legendItem(new Color(56, 193, 114),  "Safe Device"));
        legendPanel.add(legendItem(new Color(229, 57, 53),   "Infected Device"));
        legendPanel.add(legendItem(new Color(255, 179, 0),   "Boundary Edge"));
        legendPanel.add(legendItem(new Color(60, 65, 80),    "Broken Link"));

        add(legendPanel, BorderLayout.SOUTH);
    }

    // ---- SIMULATION ----

    private void doNextStep() {
        if (!network.canSpread()) { showFinished(); return; }
        network.spreadNextStep();
        network.containOneStep();
        stepCount++;
        graphPanel.refresh();
        updateStatus();
        if (!network.canSpread()) showFinished();
    }

    private void toggleAutoRun() {
        if (autoRunning) {
            autoTimer.stop();
            autoRunning = false;
            autoRunBtn.setText("⏩  Auto Run");
            recolorBtn(autoRunBtn, new Color(30, 120, 70), new Color(40, 160, 90));
            nextStepBtn.setEnabled(true);
        } else {
            if (!network.canSpread()) { showFinished(); return; }
            autoRunning = true;
            autoRunBtn.setText("⏸  Pause");
            recolorBtn(autoRunBtn, new Color(160, 110, 0), new Color(210, 150, 0));
            nextStepBtn.setEnabled(false);

            autoTimer = new Timer(speedSlider.getValue(), e -> {
                if (network.canSpread()) {
                    network.spreadNextStep();
                    network.containOneStep();
                    stepCount++;
                    graphPanel.refresh();
                    updateStatus();
                } else {
                    autoTimer.stop();
                    autoRunning = false;
                    autoRunBtn.setText("⏩  Auto Run");
                    recolorBtn(autoRunBtn, new Color(30, 120, 70), new Color(40, 160, 90));
                    nextStepBtn.setEnabled(true);
                    showFinished();
                }
            });
            autoTimer.start();
        }
    }

    private void doReset() {
        if (autoTimer != null && autoRunning) { autoTimer.stop(); autoRunning = false; }
        stepCount = 0;
        onReset.run();
    }

    private void showFinished() {
        nextStepBtn.setEnabled(false);
        autoRunBtn.setEnabled(false);
        stepLabel.setText("Done!");
        stepLabel.setForeground(new Color(255, 210, 60));
    }

    public void updateStatus() {
        stepLabel.setText(String.valueOf(stepCount));
        safeLabel.setText(String.valueOf(network.countSafeDevices()));
        infectedLabel.setText(String.valueOf(network.countInfectedDevices()));
    }

    public void reset() {
        stepCount = 0;
        nextStepBtn.setEnabled(true);
        autoRunBtn.setEnabled(true);
        autoRunBtn.setText("⏩  Auto Run");
        recolorBtn(autoRunBtn, new Color(30, 120, 70), new Color(40, 160, 90));
        updateStatus();
    }

    // ---- UI HELPERS ----

    private JButton makeBtn(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? hover : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(c.brighter());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(140, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("bg", bg);
        btn.putClientProperty("hover", hover);
        return btn;
    }

    private void recolorBtn(JButton btn, Color bg, Color hover) {
        btn.putClientProperty("bg", bg);
        btn.putClientProperty("hover", hover);
        btn.repaint();
    }

    private JLabel makeStatLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        return lbl;
    }

    private JPanel makeStatChip(String title, JLabel valueLabel, Color chipBg) {
        JPanel chip = new JPanel(new BorderLayout(4, 0));
        chip.setBackground(chipBg);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(chipBg.brighter(), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(new Color(150, 160, 200));
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 9));

        chip.add(titleLbl, BorderLayout.NORTH);
        chip.add(valueLabel, BorderLayout.CENTER);
        return chip;
    }

    private JLabel tinyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(100, 110, 150));
        l.setFont(new Font("SansSerif", Font.PLAIN, 10));
        return l;
    }

    private JPanel legendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setBackground(new Color(10, 12, 28));

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, 12, 12);
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(140, 150, 185));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));

        item.add(dot);
        item.add(lbl);
        return item;
    }
}
