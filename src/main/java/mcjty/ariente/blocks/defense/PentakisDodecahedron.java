package mcjty.ariente.blocks.defense;

import mcjty.ariente.varia.Intersections;
import mcjty.ariente.varia.Triangle;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.sqrt;

public class PentakisDodecahedron {

    public static class TriangleIdx {
        private final int a;
        private final int b;
        private final int c;

        public TriangleIdx(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public int getC() {
            return c;
        }
    }

    public static final int MAX_VERTICES = 32;
    public static final int MAX_TRIANGLES = 60;

    public static final Vec3d[] VERTICES = new Vec3d[MAX_VERTICES];
    public static final TriangleIdx[] TRIANGLES = new TriangleIdx[MAX_TRIANGLES];

    static {
        double c0 = 3 * (sqrt(5) - 1) / 4;
        double c1 = 9 * (9 + sqrt(5)) / 76;
        double c2 = 9 * (7 + 5 * sqrt(5)) / 76;
        double c3 = 3 * (1 + sqrt(5)) / 4;

        VERTICES[0] = new Vec3d(0.0, c0, c3);
        VERTICES[1] = new Vec3d(0.0, c0, -c3);
        VERTICES[2] = new Vec3d(0.0, -c0, c3);
        VERTICES[3] = new Vec3d(0.0, -c0, -c3);
        VERTICES[4] = new Vec3d(c3, 0.0, c0);
        VERTICES[5] = new Vec3d(c3, 0.0, -c0);
        VERTICES[6] = new Vec3d(-c3, 0.0, c0);
        VERTICES[7] = new Vec3d(-c3, 0.0, -c0);
        VERTICES[8] = new Vec3d(c0, c3, 0.0);
        VERTICES[9] = new Vec3d(c0, -c3, 0.0);
        VERTICES[10] = new Vec3d(-c0, c3, 0.0);
        VERTICES[11] = new Vec3d(-c0, -c3, 0.0);
        VERTICES[12] = new Vec3d(c1, 0.0, c2);
        VERTICES[13] = new Vec3d(c1, 0.0, -c2);
        VERTICES[14] = new Vec3d(-c1, 0.0, c2);
        VERTICES[15] = new Vec3d(-c1, 0.0, -c2);
        VERTICES[16] = new Vec3d(c2, c1, 0.0);
        VERTICES[17] = new Vec3d(c2, -c1, 0.0);
        VERTICES[18] = new Vec3d(-c2, c1, 0.0);
        VERTICES[19] = new Vec3d(-c2, -c1, 0.0);
        VERTICES[20] = new Vec3d(0.0, c2, c1);
        VERTICES[21] = new Vec3d(0.0, c2, -c1);
        VERTICES[22] = new Vec3d(0.0, -c2, c1);
        VERTICES[23] = new Vec3d(0.0, -c2, -c1);
        VERTICES[24] = new Vec3d(1.5, 1.5, 1.5);
        VERTICES[25] = new Vec3d(1.5, 1.5, -1.5);
        VERTICES[26] = new Vec3d(1.5, -1.5, 1.5);
        VERTICES[27] = new Vec3d(1.5, -1.5, -1.5);
        VERTICES[28] = new Vec3d(-1.5, 1.5, 1.5);
        VERTICES[29] = new Vec3d(-1.5, 1.5, -1.5);
        VERTICES[30] = new Vec3d(-1.5, -1.5, 1.5);
        VERTICES[31] = new Vec3d(-1.5, -1.5, -1.5);

        int i = 0;
        TRIANGLES[i++] = new TriangleIdx(12, 0, 2);
        TRIANGLES[i++] = new TriangleIdx(12, 2, 26);
        TRIANGLES[i++] = new TriangleIdx(12, 26, 4);
        TRIANGLES[i++] = new TriangleIdx(12, 4, 24);
        TRIANGLES[i++] = new TriangleIdx(12, 24, 0);
        TRIANGLES[i++] = new TriangleIdx(13, 3, 1);
        TRIANGLES[i++] = new TriangleIdx(13, 1, 25);
        TRIANGLES[i++] = new TriangleIdx(13, 25, 5);
        TRIANGLES[i++] = new TriangleIdx(13, 5, 27);
        TRIANGLES[i++] = new TriangleIdx(13, 27, 3);
        TRIANGLES[i++] = new TriangleIdx(14, 2, 0);
        TRIANGLES[i++] = new TriangleIdx(14, 0, 28);
        TRIANGLES[i++] = new TriangleIdx(14, 28, 6);
        TRIANGLES[i++] = new TriangleIdx(14, 6, 30);
        TRIANGLES[i++] = new TriangleIdx(14, 30, 2);
        TRIANGLES[i++] = new TriangleIdx(15, 1, 3);
        TRIANGLES[i++] = new TriangleIdx(15, 3, 31);
        TRIANGLES[i++] = new TriangleIdx(15, 31, 7);
        TRIANGLES[i++] = new TriangleIdx(15, 7, 29);
        TRIANGLES[i++] = new TriangleIdx(15, 29, 1);
        TRIANGLES[i++] = new TriangleIdx(16, 4, 5);
        TRIANGLES[i++] = new TriangleIdx(16, 5, 25);
        TRIANGLES[i++] = new TriangleIdx(16, 25, 8);
        TRIANGLES[i++] = new TriangleIdx(16, 8, 24);
        TRIANGLES[i++] = new TriangleIdx(16, 24, 4);
        TRIANGLES[i++] = new TriangleIdx(17, 5, 4);
        TRIANGLES[i++] = new TriangleIdx(17, 4, 26);
        TRIANGLES[i++] = new TriangleIdx(17, 26, 9);
        TRIANGLES[i++] = new TriangleIdx(17, 9, 27);
        TRIANGLES[i++] = new TriangleIdx(17, 27, 5);
        TRIANGLES[i++] = new TriangleIdx(18, 7, 6);
        TRIANGLES[i++] = new TriangleIdx(18, 6, 28);
        TRIANGLES[i++] = new TriangleIdx(18, 28, 10);
        TRIANGLES[i++] = new TriangleIdx(18, 10, 29);
        TRIANGLES[i++] = new TriangleIdx(18, 29, 7);
        TRIANGLES[i++] = new TriangleIdx(19, 6, 7);
        TRIANGLES[i++] = new TriangleIdx(19, 7, 31);
        TRIANGLES[i++] = new TriangleIdx(19, 31, 11);
        TRIANGLES[i++] = new TriangleIdx(19, 11, 30);
        TRIANGLES[i++] = new TriangleIdx(19, 30, 6);
        TRIANGLES[i++] = new TriangleIdx(20, 8, 10);
        TRIANGLES[i++] = new TriangleIdx(20, 10, 28);
        TRIANGLES[i++] = new TriangleIdx(20, 28, 0);
        TRIANGLES[i++] = new TriangleIdx(20, 0, 24);
        TRIANGLES[i++] = new TriangleIdx(20, 24, 8);
        TRIANGLES[i++] = new TriangleIdx(21, 10, 8);
        TRIANGLES[i++] = new TriangleIdx(21, 8, 25);
        TRIANGLES[i++] = new TriangleIdx(21, 25, 1);
        TRIANGLES[i++] = new TriangleIdx(21, 1, 29);
        TRIANGLES[i++] = new TriangleIdx(21, 29, 10);
        TRIANGLES[i++] = new TriangleIdx(22, 11, 9);
        TRIANGLES[i++] = new TriangleIdx(22, 9, 26);
        TRIANGLES[i++] = new TriangleIdx(22, 26, 2);
        TRIANGLES[i++] = new TriangleIdx(22, 2, 30);
        TRIANGLES[i++] = new TriangleIdx(22, 30, 11);
        TRIANGLES[i++] = new TriangleIdx(23, 9, 11);
        TRIANGLES[i++] = new TriangleIdx(23, 11, 31);
        TRIANGLES[i++] = new TriangleIdx(23, 31, 3);
        TRIANGLES[i++] = new TriangleIdx(23, 3, 27);
        TRIANGLES[i++] = new TriangleIdx(23, 27, 9);
    }

    public static Triangle getTriangle(int index) {
        TriangleIdx tri = TRIANGLES[index];
        Vec3d a = VERTICES[tri.getA()];
        Vec3d b = VERTICES[tri.getB()];
        Vec3d c = VERTICES[tri.getC()];
        // The mid point is horizontally the mid point but vertically the bottom point
        Vec3d mid = a.add(b).add(c).scale(1.0/3.0);
        double miny = Intersections.min3(a.y, b.y, c.y);
        return new Triangle(a, b, c, new Vec3d(mid.x, miny, mid.z));
    }
}
