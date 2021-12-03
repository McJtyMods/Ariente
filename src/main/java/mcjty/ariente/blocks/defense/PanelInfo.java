package mcjty.ariente.blocks.defense;

import mcjty.ariente.varia.Intersections;
import mcjty.ariente.varia.Triangle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

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
        AxisAlignedBB box = entity.getBoundingBox().grow(0, -.4, 0);
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        // @todo not very efficient
        Vector3d offs = triangle.getMid().scale(scale);
        Vector3d entityPos = new Vector3d(x, y, z);
        Vector3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vector3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vector3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
        return Intersections.boxTriangleTest(box, new Triangle(a, b, c));
    }

    @Nullable
    public Vector3d testCollisionSegment(Vector3d p1, Vector3d p2, double scale) {
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        // @todo not very efficient, cache
        Vector3d offs = triangle.getMid().scale(scale);
        Vector3d entityPos = new Vector3d(x, y, z);
        Vector3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vector3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vector3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
        return Intersections.segmentTriangleTest(p1, p2, new Triangle(a, b, c));
    }

    public double getSquaredDistance(Vector3d p, double scale) {
        // @todo optimize/cache?
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        Vector3d offs = triangle.getMid().scale(scale);
        Vector3d entityPos = new Vector3d(x, y, z);
        Vector3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vector3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vector3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);

        // Calculate triangle normal
        Vector3d e0 = b.subtract(a);
        Vector3d n = e0.crossProduct(c.subtract(a)).normalize();

        Vector3d v = p.subtract(a);
        double dot = v.dotProduct(n);
        Vector3d s = v.subtract(n.scale(dot));
        return s.x*s.x + s.y*s.y + s.z*s.z;
    }

    public Vector3d getClosestPoint(Vector3d p, double scale) {
        // @todo optimize/cache?
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        Vector3d offs = triangle.getMid().scale(scale);
        Vector3d entityPos = new Vector3d(x, y, z);
        Vector3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vector3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vector3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);

        // Calculate triangle normal
        Vector3d e0 = b.subtract(a);
        Vector3d n = e0.crossProduct(c.subtract(a)).normalize();

        Vector3d v = p.subtract(a);
        double dot = v.dotProduct(n);
        Vector3d s = v.subtract(n.scale(dot));
        return a.add(s);
    }
}
