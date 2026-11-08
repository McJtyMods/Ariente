package mcjty.ariente.blocks.defense;

import javax.annotation.Nullable;
import mcjty.ariente.varia.Intersections;
import mcjty.ariente.varia.Triangle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
        return p.distanceToSqr(getClosestPoint(p, scale));
    }

    public Vec3 getClosestPoint(Vec3 p, double scale) {
        // @todo optimize/cache?
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        Vec3 offs = triangle.getMid().scale(scale);
        Vec3 entityPos = new Vec3(x, y, z);
        Vec3 a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3 b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3 c = triangle.getC().scale(scale).subtract(offs).add(entityPos);

        Vec3 normal = b.subtract(a).cross(c.subtract(a));
        double lengthSquared = normal.lengthSqr();
        if (lengthSquared > 0) {
            Vec3 projected = p.subtract(normal.scale(p.subtract(a).dot(normal) / lengthSquared));
            if (b.subtract(a).cross(projected.subtract(a)).dot(normal) >= 0
                    && c.subtract(b).cross(projected.subtract(b)).dot(normal) >= 0
                    && a.subtract(c).cross(projected.subtract(c)).dot(normal) >= 0) {
                return projected;
            }
        }
        // Outside the face, the nearest point lies on one of its finite edges.
        Vec3 closest = closestOnSegment(p, a, b);
        Vec3 candidate = closestOnSegment(p, b, c);
        if (p.distanceToSqr(candidate) < p.distanceToSqr(closest)) {
            closest = candidate;
        }
        candidate = closestOnSegment(p, c, a);
        return p.distanceToSqr(candidate) < p.distanceToSqr(closest) ? candidate : closest;
    }

    private static Vec3 closestOnSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 edge = end.subtract(start);
        double lengthSquared = edge.lengthSqr();
        if (lengthSquared == 0) {
            return start;
        }
        double fraction = Mth.clamp(point.subtract(start).dot(edge) / lengthSquared, 0, 1);
        return start.add(edge.scale(fraction));
    }
}
