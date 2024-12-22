package voronoi;

import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class Delaunay {
    // starting edge for walk (see locate() method)
    private QuadEdge startingEdge;

    // list of quadEdge belonging to Delaunay triangulation
    private final List<QuadEdge> quadEdge = new ArrayList<>();

    // Bounding box of the triangulation
    static class BoundingBox {
        float minX;
        float minY;
        float maxX;
        float maxY;
        PVector a = new PVector(); // lower left
        PVector b = new PVector(); // lower right
        PVector c = new PVector(); // upper right
        PVector d = new PVector(); // upper left
    }

    private final BoundingBox boundingBox = new BoundingBox();

    /**
     * Constructor:
     */
    public Delaunay() {

        boundingBox.minX = Integer.MAX_VALUE;
        boundingBox.maxX = Integer.MIN_VALUE;
        boundingBox.minY = Integer.MAX_VALUE;
        boundingBox.maxY = Integer.MIN_VALUE;

        // create the QuadEdge graph of the bounding box
        QuadEdge ab = QuadEdge.makeEdge(boundingBox.a, boundingBox.b);
        QuadEdge bc = QuadEdge.makeEdge(boundingBox.b, boundingBox.c);
        QuadEdge cd = QuadEdge.makeEdge(boundingBox.c, boundingBox.d);
        QuadEdge da = QuadEdge.makeEdge(boundingBox.d, boundingBox.a);
        QuadEdge.splice(ab.symmetric(), bc);
        QuadEdge.splice(bc.symmetric(), cd);
        QuadEdge.splice(cd.symmetric(), da);
        QuadEdge.splice(da.symmetric(), ab);

        this.startingEdge = ab;
    }

    /**
     * update the dimension of the bounding box
     *
     * @param minX, minY, maxX, maxY summits of the rectangle
     */
    public void setBoundingBox(float minX, float minY, float maxX, float maxY) {
        // update saved values
        boundingBox.minX = minX;
        boundingBox.maxX = maxX;
        boundingBox.minY = minY;
        boundingBox.maxY = maxY;

        // extend the bounding-box to surround min/max
        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;
        float xMin = (minX - centerX - 1) * 10 + centerX;
        float xMax = (maxX - centerX + 1) * 10 + centerX;
        float yMin = (minY - centerY - 1) * 10 + centerY;
        float yMax = (maxY - centerY + 1) * 10 + centerY;

        // set new positions
        boundingBox.a.x = xMin;
        boundingBox.a.y = yMin;
        boundingBox.b.x = xMax;
        boundingBox.b.y = yMin;
        boundingBox.c.x = xMax;
        boundingBox.c.y = yMax;
        boundingBox.d.x = xMin;
        boundingBox.d.y = yMax;
    }

    // update the size of the bounding box (cf locate() method)
    private void updateBoundingBox(PVector p) {
        float minX = Math.min(boundingBox.minX, p.x);
        float maxX = Math.max(boundingBox.maxX, p.x);
        float minY = Math.min(boundingBox.minY, p.y);
        float maxY = Math.max(boundingBox.maxY, p.y);
        setBoundingBox(minX, minY, maxX, maxY);
    }

    /**
     * Returns an edge e of the triangle containing the point p
     * (Guibas and Stolfi)
     *
     * @param p the point to locate
     * @return the edge of the triangle
     */
    private QuadEdge locate(PVector p) {

        /* outside the bounding box ? */
        if (p.x < boundingBox.minX || p.x > boundingBox.maxX || p.y < boundingBox.minY || p.y > boundingBox.maxY) {
            updateBoundingBox(p);
        }

        QuadEdge e = startingEdge;
        while (true) {
            /* duplicate point ? */
            if (p.x == e.orig().x && p.y == e.orig().y) return e;
            if (p.x == e.destination().x && p.y == e.destination().y) return e;

            /* walk */
            if (QuadEdge.isAtRightOf(e, p))
                e = e.symmetric();
            else if (!QuadEdge.isAtRightOf(e.next(), p))
                e = e.next();
            else if (!QuadEdge.isAtRightOf(e.destinationPrevious(), p))
                e = e.destinationPrevious();
            else
                return e;
        }
    }

    /**
     * Inserts a new point into a Delaunay triangulation
     * (Guibas and Stolfi)
     *
     * @param p the point to insert
     */
    public void insertPoint(PVector p) {
        QuadEdge e = locate(p);

        // point is a duplicate -> nothing to do
        if (p.x == e.orig().x && p.y == e.orig().y) return;
        if (p.x == e.destination().x && p.y == e.destination().y) return;

        // point is on an existing edge -> remove the edge
        if (QuadEdge.isOnLine(e, p)) {
            e = e.previous();
            this.quadEdge.remove(e.next().symmetric());
            this.quadEdge.remove(e.next());
            QuadEdge.deleteEdge(e.next());
        }

        // Connect the new point to the vertices of the containing triangle
        // (or quadrilateral in case of the point is on an existing edge)
        QuadEdge base = QuadEdge.makeEdge(e.orig(), p);
        this.quadEdge.add(base);

        QuadEdge.splice(base, e);
        this.startingEdge = base;
        do {
            base = QuadEdge.connect(e, base.symmetric());
            this.quadEdge.add(base);
            e = base.previous();
        } while (e.leftNext() != startingEdge);

        // Examine suspect edges to ensure that the Delaunay condition is satisfied.
        do {
            QuadEdge t = e.previous();

            if (QuadEdge.isAtRightOf(e, t.destination()) &&
                    QuadEdge.inCircle(e.orig(), t.destination(), e.destination(), p)) {
                // flip triangles
                QuadEdge.swapEdge(e);
                e = e.previous();
            } else if (e.next() == startingEdge)
                return; // no more suspect edges
            else
                e = e.next().leftPrevious();  // next suspect edge
        } while (true);
    }

    /**
     * compute and return the list of edges
     */
    public List<PVector[]> computeEdges() {
        List<PVector[]> edges = new ArrayList<>();
        // do not return edges pointing to/from surrounding triangle
        for (QuadEdge q : this.quadEdge) {
            if ((q.orig() == boundingBox.a || q.orig() == boundingBox.b || q.orig() == boundingBox.c || q.orig() == boundingBox.d)
               || (q.destination() == boundingBox.a || q.destination() == boundingBox.b || q.destination() == boundingBox.c || q.destination() == boundingBox.d))
                continue;
            edges.add(new PVector[]{q.orig(), q.destination()});
        }
        return edges;
    }

    /**
     * compute and return the list of triangles
     */
    public List<PVector[]> computeTriangles() {
        List<PVector[]> triangles = new ArrayList<>();

        // do not process edges pointing to/from surrounding triangle
        // --> mark() them as already computed
        for (QuadEdge q : this.quadEdge) {
            q.setMark(false);
            q.symmetric().setMark(false);
            if (q.orig() == boundingBox.a || q.orig() == boundingBox.b || q.orig() == boundingBox.c || q.orig() == boundingBox.d) {
                q.setMark(true);
            }
            if (q.destination() == boundingBox.a || q.destination() == boundingBox.b || q.destination() == boundingBox.c || q.destination() == boundingBox.d) {
                q.symmetric().setMark(true);
            }
        }

        // compute the 2 triangles associated to each quadEdge
        for (QuadEdge q1 : quadEdge) {
            // first triangle
            QuadEdge q2 = q1.leftNext();
            QuadEdge q3 = q2.leftNext();
            if (!q1.mark() && !q2.mark() && !q3.mark()) {
                triangles.add(new PVector[]{q1.orig(), q2.orig(), q3.orig()});
            }

            // second triangle
            QuadEdge qSym1 = q1.symmetric();
            QuadEdge qSym2 = qSym1.leftNext();
            QuadEdge qSym3 = qSym2.leftNext();
            if (!qSym1.mark() && !qSym2.mark() && !qSym3.mark()) {
                triangles.add(new PVector[]{qSym1.orig(), qSym2.orig(), qSym3.orig()});
            }

            // mark() as used
            q1.setMark(true);
            q1.symmetric().setMark(true);
        }

        return triangles;
    }

    public List<List<PVector>> computeVoronoi() {
        List<List<PVector>> voronoi = new ArrayList<>();

        // do not process edges pointing to/from surrounding triangle
        // --> mark() them as already computed
        for (QuadEdge q : this.quadEdge) {
            q.setMark(false);
            q.symmetric().setMark(false);
            if (q.orig() == boundingBox.a || q.orig() == boundingBox.b || q.orig() == boundingBox.c || q.orig() == boundingBox.d) {
                q.setMark(true);
            }
            if (q.destination() == boundingBox.a || q.destination() == boundingBox.b || q.destination() == boundingBox.c || q.destination() == boundingBox.d) {
                q.symmetric().setMark(true);
            }
        }

        for (QuadEdge qe : quadEdge) {

            // walk through left and right region
            for (int b = 0; b <= 1; b++) {
                QuadEdge qStart = (b == 0) ? qe : qe.symmetric();
                if (qStart.mark()) {
                    continue;
                }

                // new region start
                List<PVector> poly = new ArrayList<>();

                // walk around region
                QuadEdge qRegion = qStart;
                do {
                    qRegion.setMark(true);

                    // compute CircumCenter if needed
                    if (qRegion.rot().orig() == null) {
                        PVector p = getCircumCenter(qRegion);
                        qRegion.rot().setOrigin(p);
                    }

                    poly.add(qRegion.rot().orig());

                    qRegion = qRegion.next();
                } while(!qRegion.equals(qStart));

                // add region to output list
                voronoi.add(poly);
            }
        }
        return voronoi;
    }

    private static PVector getCircumCenter(QuadEdge q1) {
        PVector p0 = q1.orig();
        QuadEdge q2 = q1.leftNext();
        PVector p1 = q2.orig();
        QuadEdge q3 = q2.leftNext();
        PVector p2 = q3.orig();

        float ex = p1.x - p0.x;
        float ey = p1.y - p0.y;
        float nx = p2.y - p1.y;
        float ny = p1.x - p2.x;
        float dx = (p0.x - p2.x) * 0.5f;
        float dy = (p0.y - p2.y) * 0.5f;
        float s = (ex * dx + ey * dy) / (ex * nx + ey * ny);
        float cx = (p1.x + p2.x) * 0.5f + s * nx;
        float cy = (p1.y + p2.y) * 0.5f + s * ny;

        return new PVector((int) cx, (int) cy);
    }
}
