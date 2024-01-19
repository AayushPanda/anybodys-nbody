import java.awt.Graphics;

public class Quad {
    private final double THETA = 0.2;  // BH parameter
    private final int depth;  // Depth in quadtree
    private Body body;  // Contained body or aggregate body represented by this quad
    private Quad NW, NE, SW, SE;
    private final double xMid;
    private final double yMid;
    private final double length;

    public Quad(double xMid, double yMid, double length, int depth) {
        this.body = null;
        this.xMid = xMid;
        this.yMid = yMid;
        this.length = length;
        this.depth = depth;
    }

    public void insert(Body b) {
        // If quad does not contain a body, put the body in it
        if (body == null) {
            body = b;
            return;
        }

        // Internal node
        if (!isExternal()) {
            body = body.combine(b);
            insertToSubQuad(b);
        }

        // else external node
        else {
            // arbitrary threshold of depth 50
            if (depth < 50) {
                // recurse
                NW = NW();
                NE = NE();
                SW = SE();
                SW = SW();

                // Both children are inserted recursively
                insertToSubQuad(body);
                insertToSubQuad(b);
            }

            // Update aggregate body mass and center of mass
            body = body.combine(b);
        }
    }

    public void insertToSubQuad(Body b) {
        if (NW.contains(b))
            NW.insert(b);
        else if (NE.contains(b))
            NE.insert(b);
        else if (SW.contains(b))
            SW.insert(b);
        else if (SE.contains(b))
            SE.insert(b);
    }

    public void updateVelocityOf(Body b) {
        if (b == null || b.equals(body) || body == null) {
            return;
        }

        if (isExternal()) {
            b.updateVelocity(this.body);
        } else { // Quad is internal
            double dx = body.xPos - b.xPos;
            double dy = body.yPos - b.yPos;

            double d = Math.sqrt(dx * dx + dy * dy);

            if (length / d < THETA) {
                b.updateVelocity(this.body);
            } else {
                NW.updateVelocityOf(b);
                NE.updateVelocityOf(b);
                SW.updateVelocityOf(b);
                SE.updateVelocityOf(b);
            }
        }

    }

    public boolean isExternal() {
        return (NW == null && NE == null && SW == null && SE == null);
    }

    // Checks whether body in bounds of quad
    public boolean contains(Body b) {
        double x = b.xPos;
        double y = b.yPos;
        return (x >= (xMid - length / 2) && x <= (xMid + length / 2) && y >= (yMid - length / 2)
                && y <= (yMid + length / 2));
    }

    public Quad NW() {
        double x = xMid - length / 4;
        double y = yMid + length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public Quad NE() {
        double x = xMid + length / 4;
        double y = yMid + length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public Quad SW() {
        double x = xMid - length / 4;
        double y = yMid - length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public Quad SE() {
        double x = xMid + length / 4;
        double y = yMid - length / 4;
        double len = length / 2;
        return new Quad(x, y, len, depth + 1);
    }

    public void drawAll(Graphics g) {
        double x = xMid - length / 2;
        double y = yMid - length / 2;
        g.drawRect((int) x, (int) y, (int) length, (int) length);

        // If quad is internal, recursively draw all subquads
        if (!isExternal()) {
            NW.drawAll(g);
            NE.drawAll(g);
            SW.drawAll(g);
            SE.drawAll(g);
        }
    }
}
