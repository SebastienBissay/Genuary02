package voronoi;

import processing.core.PVector;

public class QuadEdge {
    // pointer to the next (direct order) QuadEdge
    private QuadEdge next;

    // pointer to the dual QuadEdge (faces graph <-> edges graph)
    private QuadEdge rot;

    // origin point of the edge/face
    private PVector origin;

    // marker for triangle generation
    private boolean mark = false;

    /**
     * (private) constructor. Use makeEdge() to create a new QuadEdge
     *
     * @param next pointer to the next QuadEdge on the ring
     * @param rot  pointer to the next (direct order) crossing edge
     * @param orig Origin point
     */
    private QuadEdge(QuadEdge next, QuadEdge rot, PVector orig) {
        this.next = next;
        this.rot = rot;
        this.origin = orig;
    }

    // ----------------------------------------------------------------
    //                             Getter/Setter
    // ----------------------------------------------------------------

    public QuadEdge next() {
        return next;
    }

    public QuadEdge rot() {
        return rot;
    }

    public PVector orig() {
        return origin;
    }

    public boolean mark() {
        return mark;
    }

    public void setONext(QuadEdge next) {
        this.next = next;
    }

    public void setRot(QuadEdge rot) {
        this.rot = rot;
    }

    public void setOrigin(PVector p) {
        this.origin = p;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    // ----------------------------------------------------------------
    //                      QuadEdge Navigation
    // ----------------------------------------------------------------

    /**
     * @return the symmetric (reverse) QuadEdge
     */
    public QuadEdge symmetric() {
        return rot.rot();
    }

    /**
     * @return the other extremity point
     */
    public PVector destination() {
        return symmetric().orig();
    }

    /**
     * @return the symmetric dual QuadEdge
     */
    public QuadEdge symmetricDual() {
        return rot.symmetric();
    }

    /**
     * @return the previous QuadEdge (pointing to this.orig)
     */
    public QuadEdge previous() {
        return rot.next().rot();
    }

    /**
     * @return the previous QuadEdge starting from destination()
     */
    public QuadEdge destinationPrevious() {
        return symmetricDual().next().symmetricDual();
    }

    /**
     * @return the next QuadEdge on left Face
     */
    public QuadEdge leftNext() {
        return symmetricDual().next().rot();
    }

    /**
     * @return the previous QuadEdge on left Face
     */
    public QuadEdge leftPrevious() {
        return next().symmetric();
    }


    // ************************** STATIC ******************************


    /**
     * Create a new edge (i.e. a segment)
     *
     * @param origin      origin of the segment
     * @param destination end of the segment
     * @return the QuadEdge of the origin point
     */
    public static QuadEdge makeEdge(PVector origin, PVector destination) {
        QuadEdge q0 = new QuadEdge(null, null, origin);
        QuadEdge q1 = new QuadEdge(null, null, null);
        QuadEdge q2 = new QuadEdge(null, null, destination);
        QuadEdge q3 = new QuadEdge(null, null, null);

        // create the segment
        q0.next = q0;
        q2.next = q2; // lonely segment: no "next" quadEdge
        q1.next = q3;
        q3.next = q1; // in the dual: 2 communicating facets

        // dual switch
        q0.rot = q1;
        q1.rot = q2;
        q2.rot = q3;
        q3.rot = q0;

        return q0;
    }

    /**
     * attach/detach the two edges = combine/split the two rings in the dual space
     *
     * @param a, b the 2 QuadEdge to attach/detach
     */
    public static void splice(QuadEdge a, QuadEdge b) {
        QuadEdge alpha = a.next().rot();
        QuadEdge beta = b.next().rot();

        QuadEdge t1 = b.next();
        QuadEdge t2 = a.next();
        QuadEdge t3 = beta.next();
        QuadEdge t4 = alpha.next();

        a.setONext(t1);
        b.setONext(t2);
        alpha.setONext(t3);
        beta.setONext(t4);
    }

    /**
     * Create a new QuadEdge by connecting 2 QuadEdges
     *
     * @param e1,e2 the 2 QuadEdges to connect
     * @return the new QuadEdge
     */
    public static QuadEdge connect(QuadEdge e1, QuadEdge e2) {
        QuadEdge q = makeEdge(e1.destination(), e2.orig());
        splice(q, e1.leftNext());
        splice(q.symmetric(), e2);
        return q;
    }

    public static void swapEdge(QuadEdge e) {
        QuadEdge a = e.previous();
        QuadEdge b = e.symmetric().previous();
        splice(e, a);
        splice(e.symmetric(), b);
        splice(e, a.leftNext());
        splice(e.symmetric(), b.leftNext());
        e.origin = a.destination();
        e.symmetric().origin = b.destination();
    }

    /**
     * Delete a QuadEdge
     *
     * @param q the QuadEdge to delete
     */
    public static void deleteEdge(QuadEdge q) {
        splice(q, q.previous());
        splice(q.symmetric(), q.symmetric().previous());
    }

    // ----------------------------------------------------------------
    //                      Geometric computation
    // ----------------------------------------------------------------

    /**
     * Test if the PVector p is on the line porting the edge
     *
     * @param e QuadEdge
     * @param p PVector to test
     * @return true/false
     */
    public static boolean isOnLine(QuadEdge e, PVector p) {
        // test if the vector product is zero
        return (p.x - e.orig().x) * (p.y - e.destination().y) == (p.y - e.orig().y) * (p.x - e.destination().x);
    }

    /**
     * Test if the PVector p is at the right of the QuadEdge q.
     *
     * @param q reference
     * @param p PVector to test
     * @return true/false
     */
    public static boolean isAtRightOf(QuadEdge q, PVector p) {
        return isCounterClockwise(p, q.destination(), q.orig());
    }

    /**
     * return true if a, b and c turn in CounterClockwise direction
     *
     * @param a,b,c the 3 points to test
     * @return true if a, b and c turn in CounterClockwise direction
     */
    public static boolean isCounterClockwise(PVector a, PVector b, PVector c) {
        // test the sign of the determinant of ab x cb
        return (a.x - b.x) * (b.y - c.y) > (a.y - b.y) * (b.x - c.x);
    }

    /**
     * The Delaunay criteria:
     * <p>
     * test if the point d is inside the circumscribed circle of triangle a,b,c
     *
     * @param a,b,c triangle
     * @param d     point to test
     * @return true/false
     */
    public static boolean inCircle(PVector a, PVector b, PVector c, PVector d) {
		/*
		 if "d" is strictly INSIDE the circle, then

		     |d² dx dy 1|
             |a² ax ay 1|
		 det |b² bx by 1| < 0
		     |c² cx cy 1|

		*/
        float a2 = a.x * a.x + a.y * a.y;
        float b2 = b.x * b.x + b.y * b.y;
        float c2 = c.x * c.x + c.y * c.y;
        float d2 = d.x * d.x + d.y * d.y;

        float det44 = 0;
        det44 += d2 * det33(a.x, a.y, 1, b.x, b.y, 1, c.x, c.y, 1);
        det44 -= d.x * det33(a2, a.y, 1, b2, b.y, 1, c2, c.y, 1);
        det44 += d.y * det33(a2, a.x, 1, b2, b.x, 1, c2, c.x, 1);
        det44 -= 1 * det33(a2, a.x, a.y, b2, b.x, b.y, c2, c.x, c.y);

        return det44 < 0;
    }

    private static float det33(float... m) {
        float det33 = 0;
        det33 += m[0] * (m[4] * m[8] - m[5] * m[7]);
        det33 -= m[1] * (m[3] * m[8] - m[5] * m[6]);
        det33 += m[2] * (m[3] * m[7] - m[4] * m[6]);
        return det33;
    }
}
