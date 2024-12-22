package genuary._2025;

import genuary._2025.layer.Layer;
import processing.core.PApplet;

import static genuary._2025.parameters.Parameters.*;
import static genuary._2025.save.SaveUtil.saveSketch;

public class Genuary02 extends PApplet {
    public static void main(String[] args) {
        PApplet.main(Genuary02.class);
    }

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
        randomSeed(SEED);
    }

    @Override
    public void setup() {
        background(BACKGROUND_COLOR.red(), BACKGROUND_COLOR.green(), BACKGROUND_COLOR.green());
        noFill();
        noLoop();

        Layer.setPApplet(this);
    }

    @Override
    public void draw() {
        for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
            Layer layer = new Layer();
            layer.render(LAYER_COLOR);
        }

        // Borders
        fill(LAYER_COLOR.red(), LAYER_COLOR.green(), LAYER_COLOR.blue(), LAYER_COLOR.alpha());
        rect(0, 0, WIDTH, MARGIN);
        rect(0, 0, MARGIN, HEIGHT);
        rect(0, HEIGHT - MARGIN, WIDTH, MARGIN);
        rect(WIDTH - MARGIN, 0, MARGIN, HEIGHT);

        saveSketch(this);
    }
}
