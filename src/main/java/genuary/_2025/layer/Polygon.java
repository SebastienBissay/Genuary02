package genuary._2025.layer;

import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static genuary._2025.parameters.Parameters.*;
import static processing.core.PVector.dist;

public record Polygon(List<PVector> vertices) {

    public Polygon(List<PVector> vertices) {

        List<PVector> curve = new ArrayList<>(vertices);
        for (int k = 0; k < CHAIKIN_DEPTH; k++) {
            curve = chaikin(curve);
        }

        this.vertices = curve;

        contract();
    }

    private List<PVector> chaikin(List<PVector> curve) {
        List<PVector> newCurve = new ArrayList<>();
        for (int i = 0; i < curve.size(); i++) {
            PVector p = curve.get(i);
            PVector q = curve.get((i + 1) % curve.size());
            if (dist(p, q) < CHAIKIN_THRESHOLD) {
                continue;
            }
            newCurve.add(PVector.add(PVector.mult(p, 1 - CHAIKIN_PROPORTION), PVector.mult(q, CHAIKIN_PROPORTION)));
            newCurve.add(PVector.add(PVector.mult(p, CHAIKIN_PROPORTION), PVector.mult(q, 1 - CHAIKIN_PROPORTION)));
        }
        return newCurve;
    }

    private void contract() {
        PVector barycenter = vertices.stream()
                .reduce((p, q) -> PVector.add(p, q)).orElseThrow();
        barycenter.div(vertices.size());

        for (PVector vertex : vertices) {
            PVector direction = PVector.sub(vertex, barycenter);
            vertex.sub(direction.setMag(CONTRACTION));
        }
    }
}
