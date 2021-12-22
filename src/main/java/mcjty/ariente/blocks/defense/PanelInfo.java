package mcjty.ariente.blocks.defense;

import mcjty.ariente.varia.Intersections;
import mcjty.ariente.varia.Triangle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import static mcjty.ariente.config.Config.SHIELD_PANEL_LIFE;

public class PanelInfo {
    private final int index;
    private final double x;
    private final double y;
    private final double z;
    private int life;       // Negative means the panel is still building up

    public PanelInfo(int index, double x, double y, double z) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.z = z;
        this.life = -100;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public float getLifePercentage() {
        return life / (float) SHIELD_PANEL_LIFE;
    }

    public int getIndex() {
        return index;
    }

    public boolean testCollisionEntity(Entity entity, double scale) {
        AABB box = entity.getBoundingBox().inflate(0, -.4, 0);
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        // @todo not very efficient
        Vec3 offs = triangle.getMid().scale(scale);
        Vec3 entityPos = new Vec3(x, y, z);
        Vec3 a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3 b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3 c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
        return Intersections.boxTriangleTest(box, new Triangle(a, b, c));
    }

    @Nullable
    public Vec3 testCollisionSegment(Vec3 p1, Vec3 p2, double scale) {
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        // @todo not very efficient, cache
        Vec3 offs = triangle.getMid().scale(scale);
        Vec3 entityPos = new Vec3(x, y, z);
        Vec3 a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3 b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3 c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
        return Intersections.segmentTriangleTest(p1, p2, new Triangle(a, b, c));
    }

    public double getSquaredDistance(Vec3 p, double scale) {
        // @todo optimize/cache?
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        Vec3 offs = triangle.getMid().scale(scale);
        Vec3 entityPos = new Vec3(x, y, z);
        Vec3 a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3 b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3 c = triangle.getC().scale(scale).subtract(offs).add(entityPos);

        // Calculate triangle normal
        Vec3 e0 = b.subtract(a);
        Vec3 n = e0.cross(c.subtract(a)).normalize();

        Vec3 v = p.subtract(a);
        double dot = v.dot(n);
        Vec3 s = v.subtract(n.scale(dot));
        return s.x*s.x + s.y*s.y + s.z*s.z;
    }

    public Vec3 getClosestPoint(Vec3 p, double scale) {
        // @todo optimize/cache?
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        Vec3 offs = triangle.getMid().scale(scale);
        Vec3 entityPos = new Vec3(x, y, z);
        Vec3 a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3 b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3 c = triangle.getC().scale(scale).subtract(offs).add(entityPos);

        // Calculate triangle normal
        Vec3 e0 = b.subtract(a);
        Vec3 n = e0.cross(c.subtract(a)).normalize();

        Vec3 v = p.subtract(a);
        double dot = v.dot(n);
        Vec3 s = v.subtract(n.scale(dot));
        return a.add(s);
    }
}
