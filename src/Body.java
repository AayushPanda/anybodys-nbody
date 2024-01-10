import java.awt.*;

public class Body {
    private final double G = 1E5;     // Gravity strength
    private final double DAMP = 100; // Damping strength used in updatePosition()
    final double DT = BHMain.DT / 1000.0;      // Time step in seconds
    private double xPos, yPos;
    private double xVel, yVel;
    private double radius;
    private double mass;


    public Body(double xPos, double yPos, double xVel, double yVel, double radius) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xVel = xVel;
        this.yVel = yVel;
        this.radius = radius;
        this.mass = Math.PI * radius * radius; // Mass proportional to area
    }

    /*
     * Combines this body with another body and returns the aggregate body.
     *
     * Velocity is not calculated because position will be updated as the
     * contained bodies are updated individually.
     */
    public Body combine(Body b) {
        double m = mass + b.mass;
        double r = Math.sqrt(m / (Math.PI));
        double x = (xPos * mass + b.xPos * b.mass) / m;
        double y = (yPos * mass + b.yPos * b.mass) / m;
        return new Body(x, y, 0, 0, r);
    }

    /*
     * Draw this body as a circle centered on xPos and yPos
     */
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

    /*
     * Updates the position change of this body due to its velocity and
     * velocity change of this body due to force from Body b
     */
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


    double getX() {
        return xPos;
    }

    //setters
    void setX(double x) {
        xPos = x;
    }

    double getY() {
        return yPos;
    }

    void setY(double y) {
        yPos = y;
    }

    double getRadius() {
        return radius;
    }

    void setRadius(double r) {
        radius = r;
        mass = Math.PI * radius * radius;
    }

    void setVelocityX(double vx) {
        xVel = vx;
    }

    void setVelocityY(double vy) {
        yVel = vy;
    }
}
