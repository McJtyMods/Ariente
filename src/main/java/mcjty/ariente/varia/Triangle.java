package mcjty.ariente.varia;

import net.minecraft.util.math.vector.Vector3d;

public class Triangle {
    public final Vector3d a;
    public final Vector3d b;
    public final Vector3d c;

    private final Vector3d mid;

    public Triangle(Vector3d a, Vector3d b, Vector3d c, Vector3d mid) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.mid = mid;
    }

    public Triangle(Vector3d a, Vector3d b, Vector3d c) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.mid = null;
    }

    public Vector3d getA() {
        return a;
    }

    public Vector3d getB() {
        return b;
    }

    public Vector3d getC() {
        return c;
    }

    public Vector3d getMid() {
        return mid;
    }
}
