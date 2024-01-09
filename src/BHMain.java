import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * This project simulates n bodies using the Barnes-Hut algorithm.
 * 
 * The Barnes Hut structure is defined as a quad-tree with each node's four
 * children denoting the NW, NE, SW and SE sections of the square it represents.
 * 
 * Bodies are initialized and stored in an ArrayList.
 * Each frame, particle positions are updated using their velocity from last frame, and
 * a Barnes Hut quad-tree is created from the ArrayList of bodies and used calculate and
 * update the velocity of each particle.
 */

public class BHMain{
    public static final boolean DRAW_QUADS = true; // Render Barnes Hut quads or just particles
    public static final int DT = 10;        // Time-step in milliseconds
    public static final int WIDTH = 800;    // Dimensions of JPanel
    public static final int HEIGHT = WIDTH; // Algorithm assumes a square universe
    private ArrayList<Body> bodies = new ArrayList<>();    // Stores all bodies
    private Quad root;        // Root node of the Barnes Hut quad-tree
    private MyPanel panel;    // Bodies are drawn to this overridden JPanel
    private JFrame frame;     // Contains the JPanel
    private long time;        // Used to calculate the fps in calcFrameRate()
    
    public static void main(String[] args) {
        BHMain sim = new BHMain();
    }
    
    public BHMain(){
        // Create and initialize GUI
        panel = new MyPanel();
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame = new JFrame();
        frame.add(panel);
        //set background to black so that the JPanel is transparent
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        time = System.currentTimeMillis();
        
        // Create starting bodies and build the initial Barnes Hut tree
        addSolarSystem(2, 2);
        buildBarnesHutTree();

        // Start the simulation
        Timer myTimer = new Timer(DT, new TimerListener());
        myTimer.start();
    }
    
    /*
     * Creates n randomly distributed particles in a square area with a margin
     * of empty space around the edge of the JPanel
     * 
     */
    private void addRandomBodies(int n, float m){
        double margin = 0.2;
        Random rand = new Random();
        for(int i=0; i<n; i++){
            double x = HEIGHT*(margin + (1-2*margin)*rand.nextFloat());
            double y = WIDTH*(margin + (1-2*margin)*rand.nextFloat());
            //Give the particles a circular motion around the center of the frame
            double vx = 1*(y-HEIGHT/2);
            double vy = -1*(x-WIDTH/2);
            
            Body p = new Body((int)x,(int)y,vx,vy,m);
            bodies.add(p);
        }
    }

    /*
     * Creates n points in a circle with radius r centered at the center of the JPanel,
     * with all points evenly distributed around the circle
     */
    /*
    */
    private void addCircleBodies(int n, float m){
        double r = 0.4*WIDTH;
        for(int i=0; i<n; i++){
            double x = WIDTH/2 + r*Math.cos(2*Math.PI*i/n);
            double y = HEIGHT/2 + r*Math.sin(2*Math.PI*i/n);
            //Give the particles a circular motion around the center of the frame
            double vx = 1*(y-HEIGHT/2)*0.5;
            double vy = -1*(x-WIDTH/2)*0.5;
            vx=0;
            vy=0;
            
            Body p = new Body((int)x,(int)y,vx,vy,m);
            bodies.add(p);
        }
    }

    /*
     * Creates a solar system with a star at the center and n planets orbiting it
     * Planets spawn in a line, with subsequent planets spawning further away from the star
     */
    private void addSolarSystem(int n, float m){
        double r = 0.4*WIDTH;
        Body star = new Body(WIDTH/2, HEIGHT/2, 0, 0, 10);
        bodies.add(star);
        for(int i=0; i<n; i++){
            
            double x = WIDTH/2 + r*i/n;
            double y = HEIGHT/2;

            double vx = 2*(y-HEIGHT/2);
            double vy = -2*(x-WIDTH/2);
            
            Body p = new Body((int)x,(int)y,vx,vy,m);
            bodies.add(p);
        }
    }

    /*
     * Build a Barnes Hut quad tree containing all the bodies in the bodies
     * array with the root node as its head
     */
    private void buildBarnesHutTree(){
        //The root will be a square quad with its length equal to the JPanel
        root = new Quad(WIDTH/2, HEIGHT/2, WIDTH, 0);
        for(Body b : bodies){
            root.insert(b);
        }
    }
    
    /*
     * Update the position and velocity of all bodies for the current frame
     * then build a new Barnes Hut tree with the updated particles
     */
    private void update(){
        for(Body b : bodies){
            b.updatePosition();
            root.updateVelocityOf(b);
        }
        buildBarnesHutTree();
    }
    
    /*
     * Returns the frame rate of the simulation by dividing by the 
     * time between the current and previous frame
     */
    private int calcFrameRate(){
        long newTime = System.currentTimeMillis();
        long fps = 1000/(newTime - time);
        time = newTime;
        return Math.round(fps);
    }
    
    /*
     * Updates and renders bodies every frame
     * 
     * The time interval between frames is defined by the constant 
     * DT used to create myTimer in BHMain()
     * 
     */
    private class TimerListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            update();
            frame.repaint();
            int fps = calcFrameRate();
            frame.setTitle("Body Simulation | " + fps + " fps | " + bodies.size() + " bodies");
        }
    }
    
    /*
     * Renders the JPanel when repaint is called every frame
     */
    private class MyPanel extends JPanel{
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
    
            // Set background color to black
            g2d.setBackground(Color.BLACK);  // Import java.awt.Color if not already imported
            g2d.clearRect(0, 0, getWidth(), getHeight());
    
            // Redefines coordinates so that 0,0 is at the bottom left
            g2d.scale(1, -1);
            g2d.translate(0, -getHeight());
    
            // Turn on anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
            // Set body color to white
            g2d.setColor(Color.WHITE);
    
            // Draw all Bodies in Body array
            for (Body p : bodies) {
                p.drawBody(g2d);
            }
    
            // Draw Barnes Hut quads
            if (DRAW_QUADS) {
                g2d.setColor(Color.GRAY); // Set quad color (you can choose a different color)
                root.drawAll(g2d);
            }
        }
    }
}
