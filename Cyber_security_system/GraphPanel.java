import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {

    private NetworkGraph network;
    private Map<Device, Point> positions = new LinkedHashMap<>();

    private Device draggedDevice = null;
    private int dragOffsetX, dragOffsetY;

    private static final int R = 28;
    private static final Color BG_TOP       = new Color(10, 12, 28);
    private static final Color BG_BOT       = new Color(18, 22, 48);
    private static final Color COL_SAFE     = new Color(56, 193, 114);
    private static final Color COL_INFECTED = new Color(229, 57, 53);
    private static final Color COL_BOUNDARY = new Color(255, 179, 0);
    private static final Color COL_EDGE     = new Color(100, 120, 160);
    private static final Color COL_BROKEN   = new Color(60, 65, 80);
    private static final Color COL_LABEL    = new Color(220, 230, 255);

    private boolean positionsInitialized = false;

    public GraphPanel(NetworkGraph network) {
        this.network = network;
        setOpaque(true);
        setPreferredSize(new Dimension(720, 520));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!positionsInitialized && getWidth() > 10 && getHeight() > 10) {
                    initPositions();
                    positionsInitialized = true;
                }
            }
        });

        setupMouse();
    }

    public void initPositions() {
        positions.clear();
        List<Device> devices = getDevices();
        int n = devices.size();
        if (n == 0) return;

        int w = Math.max(getWidth(), 400);
        int h = Math.max(getHeight(), 300);
        int cx = w / 2;
        int cy = h / 2;
        int radius = Math.min(w, h) / 2 - 80;
        if (radius < 60) radius = 60;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            int x = (int) (cx + radius * Math.cos(angle));
            int y = (int) (cy + radius * Math.sin(angle));
            positions.put(devices.get(i), new Point(x, y));
        }
        repaint();
    }

    private List<Device> getDevices() {
        List<Device> list = new ArrayList<>();
        for (int i = 1; i <= network.getSize(); i++) {
            Device d = network.getDeviceById(i);
            if (d != null) list.add(d);
        }
        return list;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background gradient
        g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Dot grid
        g2.setColor(new Color(255, 255, 255, 10));
        for (int x = 0; x < getWidth(); x += 40)
            for (int y = 0; y < getHeight(); y += 40)
                g2.fillOval(x - 1, y - 1, 2, 2);

        if (positions.isEmpty()) {
            g2.setColor(new Color(100, 110, 140));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String msg = "Loading...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            return;
        }

        drawEdges(g2);
        drawDevices(g2);
    }

    private void drawEdges(Graphics2D g2) {
        List<Edge> boundary = network.getBoundaryEdges();

        for (Edge e : network.getEdges()) {
            Point pA = positions.get(e.getDeviceA());
            Point pB = positions.get(e.getDeviceB());
            if (pA == null || pB == null) continue;

            if (e.isBroken()) {
                g2.setColor(COL_BROKEN);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, new float[]{5f, 5f}, 0f));
                g2.drawLine(pA.x, pA.y, pB.x, pB.y);

            } else if (boundary.contains(e)) {
                // Glow
                g2.setColor(new Color(255, 179, 0, 35));
                g2.setStroke(new BasicStroke(9f));
                g2.drawLine(pA.x, pA.y, pB.x, pB.y);
                g2.setColor(COL_BOUNDARY);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(pA.x, pA.y, pB.x, pB.y);

            } else {
                g2.setColor(new Color(100, 120, 160, 50));
                g2.setStroke(new BasicStroke(5f));
                g2.drawLine(pA.x, pA.y, pB.x, pB.y);
                g2.setColor(COL_EDGE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(pA.x, pA.y, pB.x, pB.y);
            }

            drawWeightBadge(g2, e, pA, pB, boundary.contains(e));
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawWeightBadge(Graphics2D g2, Edge e, Point pA, Point pB, boolean isBoundary) {
        int mx = (pA.x + pB.x) / 2;
        int my = (pA.y + pB.y) / 2;
        String label = e.isBroken() ? "X" : String.valueOf(e.getWeight());
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(label);
        int lh = fm.getAscent();
        int pad = 4;

        Color bg = e.isBroken() ? new Color(35, 38, 52)
                 : isBoundary   ? new Color(70, 50, 0)
                                : new Color(22, 26, 50);

        g2.setColor(bg);
        g2.fillRoundRect(mx - lw / 2 - pad, my - lh - pad / 2, lw + pad * 2, lh + pad, 6, 6);
        g2.setColor(e.isBroken() ? COL_BROKEN : isBoundary ? COL_BOUNDARY : COL_EDGE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(mx - lw / 2 - pad, my - lh - pad / 2, lw + pad * 2, lh + pad, 6, 6);
        g2.setColor(e.isBroken() ? COL_BROKEN : COL_LABEL);
        g2.drawString(label, mx - lw / 2, my - 1);
    }

    private void drawDevices(Graphics2D g2) {
        for (Map.Entry<Device, Point> entry : positions.entrySet()) {
            Device d = entry.getKey();
            Point p = entry.getValue();
            Color base = d.isInfected() ? COL_INFECTED : COL_SAFE;

            // Outer glow
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 35));
            g2.fillOval(p.x - R - 9, p.y - R - 9, (R + 9) * 2, (R + 9) * 2);

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(p.x - R + 3, p.y - R + 4, R * 2, R * 2);

            // Radial gradient fill
            RadialGradientPaint grad = new RadialGradientPaint(
                    new Point(p.x - R / 3, p.y - R / 3), R * 1.4f,
                    new float[]{0f, 1f},
                    new Color[]{base.brighter(), base.darker().darker()}
            );
            g2.setPaint(grad);
            g2.fillOval(p.x - R, p.y - R, R * 2, R * 2);

            // Border
            g2.setPaint(null);
            g2.setColor(base.brighter());
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(p.x - R, p.y - R, R * 2, R * 2);

            // Label
            String label = "D" + d.getDeviceId();
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int lw = fm.stringWidth(label);
            int lh = fm.getAscent();
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawString(label, p.x - lw / 2 + 1, p.y + lh / 2 - 3);
            g2.setColor(Color.WHITE);
            g2.drawString(label, p.x - lw / 2, p.y + lh / 2 - 4);

            // Status dot top-right
            int dx = p.x + R - 8, dy = p.y - R + 1;
            g2.setColor(d.isInfected() ? new Color(255, 120, 120) : new Color(120, 255, 170));
            g2.fillOval(dx, dy, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(dx, dy, 10, 10);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void setupMouse() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (Map.Entry<Device, Point> entry : positions.entrySet()) {
                    if (entry.getValue().distance(e.getX(), e.getY()) <= R + 8) {
                        draggedDevice = entry.getKey();
                        dragOffsetX = e.getX() - entry.getValue().x;
                        dragOffsetY = e.getY() - entry.getValue().y;
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        break;
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedDevice != null) {
                    int nx = Math.max(R + 5, Math.min(getWidth() - R - 5, e.getX() - dragOffsetX));
                    int ny = Math.max(R + 5, Math.min(getHeight() - R - 5, e.getY() - dragOffsetY));
                    positions.put(draggedDevice, new Point(nx, ny));
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedDevice = null;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hover = positions.values().stream()
                        .anyMatch(p -> p.distance(e.getX(), e.getY()) <= R + 8);
                setCursor(hover
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void refresh() { repaint(); }
}
