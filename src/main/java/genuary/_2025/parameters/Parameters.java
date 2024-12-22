package genuary._2025.parameters;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Parameters {
    public static final long SEED = 20250102;
    public static final int WIDTH = 2025;
    public static final int HEIGHT = 2025;
    public static final float MARGIN = 200;
    public static final int NUMBER_OF_LAYERS = 10;
    public static final int NUMBER_OF_CUTS = 10;
    public static final int CHAIKIN_DEPTH = 10;
    public static final float CHAIKIN_PROPORTION = .2f;
    public static final float CHAIKIN_THRESHOLD = 3;
    public static final float CONTRACTION = 5f;
    public static final Color BACKGROUND_COLOR = new Color(0);
    public static final Color LAYER_COLOR = new Color(235);
    public static final Color DEPTH_SHADOW_COLOR = new Color(0, 50);
    public static final int PROJECTED_SHADOW_LAYERS = 10;
    public static final float PROJECTED_SHADOW_STROKE_WEIGHT = 30f;
    public static final float PROJECTED_SHADOW_STROKE_WEIGHT_FACTOR = 3f;
    public static final Color PROJECTED_SHADOW_COLOR = new Color(0, 10);

    /**
     * Helper method to extract the constants in order to save them to a json file
     *
     * @return a Map of the constants (name -> value)
     */
    public static Map<String, Object> toJsonMap() throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();

        Field[] declaredFields = Parameters.class.getDeclaredFields();
        for (Field field : declaredFields) {
            map.put(field.getName(), field.get(Parameters.class));
        }

        return Collections.singletonMap(Parameters.class.getSimpleName(), map);
    }

    public record Color(float red, float green, float blue, float alpha) {
        public Color(float red, float green, float blue) {
            this(red, green, blue, 255);
        }

        public Color(float grayscale, float alpha) {
            this(grayscale, grayscale, grayscale, alpha);
        }

        public Color(float grayscale) {
            this(grayscale, 255);
        }

        public Color(String hexCode) {
            this(decode(hexCode));
        }

        public Color(Color color) {
            this(color.red, color.green, color.blue, color.alpha);
        }

        public static Color decode(String hexCode) {
            return switch (hexCode.length()) {
                case 2 -> new Color(Integer.valueOf(hexCode, 16));
                case 4 -> new Color(Integer.valueOf(hexCode.substring(0, 2), 16),
                        Integer.valueOf(hexCode.substring(2, 4), 16));
                case 6 -> new Color(Integer.valueOf(hexCode.substring(0, 2), 16),
                        Integer.valueOf(hexCode.substring(2, 4), 16),
                        Integer.valueOf(hexCode.substring(4, 6), 16));
                case 8 -> new Color(Integer.valueOf(hexCode.substring(0, 2), 16),
                        Integer.valueOf(hexCode.substring(2, 4), 16),
                        Integer.valueOf(hexCode.substring(4, 6), 16),
                        Integer.valueOf(hexCode.substring(6, 8), 16));
                default -> throw new IllegalArgumentException();
            };
        }
    }
}
