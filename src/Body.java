import java.awt.Color;
import java.awt.Graphics2D;

public class Body {
    private final double G = 1E5;     // Gravity strength
    private final double DAMP = 90; // Damping strength
    final double DT = BHMain.DT / 1000.0;      // Time step in seconds
    double xPos;
    double yPos;
    double xVel;
    double yVel;
    double radius;
    private double mass;


    public Body(double xPos, double yPos, double xVel, double yVel, double radius) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xVel = xVel;
        this.yVel = yVel;
        this.radius = radius;
        this.mass = Math.PI * radius * radius; // Mass proportional to area
    }

    // Combine two bodies into one
    public Body combine(Body b) {
        double m = mass + b.mass;
        double r = Math.sqrt(m / (Math.PI));
        double x = (xPos * mass + b.xPos * b.mass) / m;
        double y = (yPos * mass + b.yPos * b.mass) / m;
        return new Body(x, y, 0, 0, r);
    }


    void drawBody(Graphics2D g2d) {
        int diameter = (int) Math.abs(2 * radius);
        int x = (int) (xPos - radius);
        int y = (int) (yPos - radius);
        if (radius < 0) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.fillOval(x, y, diameter, diameter);
    }

    // Rectangle integral approximation
    void updatePosition() {
        xPos += DT * xVel;
        yPos += DT * yVel;
    }

    void updateVelocity(Body b) {
        double EPS = DAMP * radius;

        // Force on this body
        double dx = xPos - b.xPos;
        double dy = yPos - b.yPos;
        double r_sq = (dx * dx + dy * dy);

        // Softened Newtonian force to eliminate erratic behavior on collisions
        double F = -G * mass * b.mass / (r_sq + EPS * EPS);
        double angle = Math.atan2(dy, dx);
        double Fx = F * Math.cos(angle);
        double Fy = F * Math.sin(angle);

        // Update velocity. a = F / m.  v = v0 + a*t
        xVel += DT * Fx / mass;
        yVel += DT * Fy / mass;
    }

}
