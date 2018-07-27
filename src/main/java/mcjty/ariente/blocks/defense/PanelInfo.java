package mcjty.ariente.blocks.defense;

import mcjty.ariente.varia.Intersections;
import mcjty.ariente.varia.Triangle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class PanelInfo {
    private final int index;
    private final double x;
    private final double y;
    private final double z;
    private final double scale;

    public PanelInfo(int index, double x, double y, double z, double scale) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = scale;
    }

    public int getIndex() {
        return index;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getScale() {
        return scale;
    }

    public boolean testCollisionEntity(Entity entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        // @todo not very efficient
        Vec3d offs = triangle.getMid().scale(scale);
        Vec3d entityPos = new Vec3d(x, y, z);
        Vec3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
        return Intersections.boxTriangleTest(box, new Triangle(a, b, c));
    }

    public boolean testCollisionSegment(Vec3d p1, Vec3d p2) {
        Triangle triangle = PentakisDodecahedron.getTriangle(getIndex());
        // @todo not very efficient
        Vec3d offs = triangle.getMid().scale(scale);
        Vec3d entityPos = new Vec3d(x, y, z);
        Vec3d a = triangle.getA().scale(scale).subtract(offs).add(entityPos);
        Vec3d b = triangle.getB().scale(scale).subtract(offs).add(entityPos);
        Vec3d c = triangle.getC().scale(scale).subtract(offs).add(entityPos);
        return Intersections.segmentTriangleTest(p1, p2, new Triangle(a, b, c));
    }
}
