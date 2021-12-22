package mcjty.ariente.varia;

import net.minecraft.world.phys.Vec3;

public class Triangle {
    public final Vec3 a;
    public final Vec3 b;
    public final Vec3 c;

    private final Vec3 mid;

    public Triangle(Vec3 a, Vec3 b, Vec3 c, Vec3 mid) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.mid = mid;
    }

    public Triangle(Vec3 a, Vec3 b, Vec3 c) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.mid = null;
    }

    public Vec3 getA() {
        return a;
    }

    public Vec3 getB() {
        return b;
    }

    public Vec3 getC() {
        return c;
    }

    public Vec3 getMid() {
        return mid;
    }
}
