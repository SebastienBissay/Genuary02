package genuary._2025.layer;

import processing.core.PApplet;
import processing.core.PVector;
import voronoi.Delaunay;

import java.util.ArrayList;
import java.util.List;

import static genuary._2025.parameters.Parameters.*;
import static processing.core.PConstants.CLOSE;

public class Layer {

    private static PApplet pApplet;
    private List<Polygon> polygons;

    public Layer() {
        polygons = new ArrayList<>();
        initializeLayer();
    }

    public static void setPApplet(PApplet pApplet) {
        Layer.pApplet = pApplet;
    }

    private void initializeLayer() {
        Delaunay delaunay = new Delaunay();

        for (int k = 0; k < NUMBER_OF_CUTS; k++) {
            delaunay.insertPoint(new PVector(pApplet.random(MARGIN, WIDTH - MARGIN),
                    pApplet.random(MARGIN, HEIGHT - MARGIN)));
        }

        polygons = delaunay.computeVoronoi().stream().map(Polygon::new).toList();
    }

    public void render(Color color) {
        // Shadow underneath layers
        pApplet.noStroke();
        pApplet.fill(DEPTH_SHADOW_COLOR.red(), DEPTH_SHADOW_COLOR.green(), DEPTH_SHADOW_COLOR.blue(),
                DEPTH_SHADOW_COLOR.alpha());
        pApplet.rect(0, 0, WIDTH, HEIGHT);

        // Drop shadow
        for (int i = 0; i < PROJECTED_SHADOW_LAYERS; i++) {
            pApplet.strokeWeight(PROJECTED_SHADOW_STROKE_WEIGHT - PROJECTED_SHADOW_STROKE_WEIGHT_FACTOR * i);
            pApplet.stroke(PROJECTED_SHADOW_COLOR.red(), PROJECTED_SHADOW_COLOR.green(),
                    PROJECTED_SHADOW_COLOR.blue(), PROJECTED_SHADOW_COLOR.alpha());
            pApplet.noFill();
            drawLayer();
        }

        // Draw layer
        pApplet.noStroke();
        pApplet.fill(color.red(), color.green(), color.blue(), color.alpha());
        drawLayer();
    }

    private void drawLayer() {
        pApplet.beginShape();
        pApplet.vertex(0, 0);
        pApplet.vertex(0, HEIGHT);
        pApplet.vertex(WIDTH, HEIGHT);
        pApplet.vertex(WIDTH, 0);
        for (Polygon polygon : polygons) {
            pApplet.beginContour();
            for (PVector vertex : polygon.vertices()) {
                pApplet.vertex(vertex.x, vertex.y);
            }
            pApplet.endContour();
        }
        pApplet.endShape(CLOSE);
    }
}
