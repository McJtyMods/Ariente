package mcjty.ariente.varia;

import net.minecraft.util.math.Vec3d;

public class Triangle {
    public final Vec3d a;
    public final Vec3d b;
    public final Vec3d c;

    private final Vec3d mid;

    public Triangle(Vec3d a, Vec3d b, Vec3d c, Vec3d mid) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.mid = mid;
    }

    public Triangle(Vec3d a, Vec3d b, Vec3d c) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.mid = null;
    }

    public Vec3d getA() {
        return a;
    }

    public Vec3d getB() {
        return b;
    }

    public Vec3d getC() {
        return c;
    }

    public Vec3d getMid() {
        return mid;
    }
}
