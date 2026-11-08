package mcjty.ariente.display;

/** Clips magnified camera faces to the module, interpolating texture coordinates and vertex colors. */
public class CameraViewClipper {

    public static final int STRIDE = 7;
    private float[] vertices = new float[12 * STRIDE];
    private float[] scratch = new float[12 * STRIDE];
    private int count;

    public static int outcode(float x, float y, float left, float top, float right, float bottom) {
        return (x < left ? 1 : x > right ? 2 : 0) | (y < top ? 4 : y > bottom ? 8 : 0);
    }

    public void begin() {
        count = 0;
    }

    public void vertex(float x, float y, float u, float v, int color) {
        int offset = count++ * STRIDE;
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = u;
        vertices[offset + 3] = v;
        vertices[offset + 4] = color >> 16 & 255;
        vertices[offset + 5] = color >> 8 & 255;
        vertices[offset + 6] = color & 255;
    }

    public int clip(float left, float top, float right, float bottom) {
        edge(0, left, 1);
        edge(0, right, -1);
        edge(1, top, 1);
        edge(1, bottom, -1);
        return count;
    }

    public float[] vertices() {
        return vertices;
    }

    private void edge(int axis, float boundary, int sign) {
        if (count == 0) {
            return;
        }
        int output = 0;
        int previous = (count - 1) * STRIDE;
        float previousDistance = (vertices[previous + axis] - boundary) * sign;
        for (int index = 0; index < count; index++) {
            int current = index * STRIDE;
            float distance = (vertices[current + axis] - boundary) * sign;
            if ((distance >= 0) != (previousDistance >= 0)) {
                float fraction = previousDistance / (previousDistance - distance);
                for (int field = 0; field < STRIDE; field++) {
                    scratch[output * STRIDE + field] = vertices[previous + field]
                            + (vertices[current + field] - vertices[previous + field]) * fraction;
                }
                scratch[output * STRIDE + axis] = boundary;
                output++;
            }
            if (distance >= 0) {
                System.arraycopy(vertices, current, scratch, output++ * STRIDE, STRIDE);
            }
            previous = current;
            previousDistance = distance;
        }
        float[] swap = vertices;
        vertices = scratch;
        scratch = swap;
        count = output;
    }
}
