import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BHMain {
    public static final boolean DRAW_QUADS = true;
    public static final int DT = 10;
    public static final int WIDTH = 800;
    public static final int HEIGHT = WIDTH;
    private ArrayList<Body> bodies = new ArrayList<>();
    private Quad root;
    private MyPanel panel;
    private JFrame frame;
    private long time;
    private ExecutorService executorService;

    public static void main(String[] args) {
        BHMain sim = new BHMain();
    }

    public BHMain() {
        panel = new MyPanel();
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame = new JFrame();
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        time = System.currentTimeMillis();

        addCircleBodies(1000, 2);
        buildBarnesHutTree();

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Timer myTimer = new Timer(DT, new TimerListener());
        myTimer.start();
    }

    private void addRandomBodies(int n, float m) {
        double margin = 0.2;
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            double x = HEIGHT * (margin + (1 - 2 * margin) * rand.nextFloat());
            double y = WIDTH * (margin + (1 - 2 * margin) * rand.nextFloat());
            double vx = 1 * (y - HEIGHT / 2);
            double vy = -1 * (x - WIDTH / 2);

            Body p = new Body((int) x, (int) y, vx, vy, m);
            bodies.add(p);
        }
    }

    private void addCircleBodies(int n, float m) {
        double r = 0.4 * WIDTH;
        for (int i = 0; i < n; i++) {
            double x = WIDTH / 2 + r * Math.cos(2 * Math.PI * i / n);
            double y = HEIGHT / 2 + r * Math.sin(2 * Math.PI * i / n);
            double vx = 1 * (y - HEIGHT / 2) * 0.5;
            double vy = -1 * (x - WIDTH / 2) * 0.5;
            vx = 0;
            vy = 0;

            Body p = new Body((int) x, (int) y, vx, vy, m);
            bodies.add(p);
        }
    }

    private void addSolarSystem(int n, float m) {
        double r = 0.4 * WIDTH;
        Body star = new Body(WIDTH / 2, HEIGHT / 2, 0, 0, 10);
        bodies.add(star);
        for (int i = 0; i < n; i++) {
            double x = WIDTH / 2 + r * i / n;
            double y = HEIGHT / 2;
            double vx = 2 * (y - HEIGHT / 2);
            double vy = -2 * (x - WIDTH / 2);

            Body p = new Body((int) x, (int) y, vx, vy, m);
            bodies.add(p);
        }
    }

    private void buildBarnesHutTree() {
        root = new Quad(WIDTH / 2, HEIGHT / 2, WIDTH, 0);
        for (Body b : bodies) {
            root.insert(b);
        }
    }

    private void update() {
        for (Body b : bodies) {
            b.updatePosition();
            root.updateVelocityOf(b);
        }
        buildBarnesHutTree();
    }

    private int calcFrameRate() {
        long newTime = System.currentTimeMillis();
        long fps = 1000 / (newTime - time);
        time = newTime;
        return Math.round(fps);
    }

    private class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            update();
            frame.repaint();
            int fps = calcFrameRate();
            frame.setTitle("Body Simulation | " + fps + " fps | " + bodies.size() + " bodies");
        }
    }

    private class MyPanel extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setBackground(Color.BLACK);
            g2d.clearRect(0, 0, getWidth(), getHeight());

            g2d.scale(1, -1);
            g2d.translate(0, -getHeight());

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);

            // Draw all Bodies in Body array using parallel streams
            try {
                List<Callable<Void>> drawTasks = bodies.stream().map(body -> (Callable<Void>) () -> {
                    body.drawBody(g2d);
                    return null;
                }).collect(Collectors.toList());
                executorService.invokeAll(drawTasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (DRAW_QUADS) {
                g2d.setColor(Color.GRAY);
                root.drawAll(g2d);
            }
        }
    }
}
