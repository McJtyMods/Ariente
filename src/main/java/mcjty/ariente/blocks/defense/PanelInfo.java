package mcjty.ariente.blocks.defense;

import mcjty.ariente.varia.Intersections;
import mcjty.ariente.varia.Triangle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

import static mcjty.ariente.config.ArienteConfiguration.SHIELD_PANEL_LIFE;

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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public boolean testCollisionEntity(Entity entity, double scale) {
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

    @Nullable
    public Vec3d testCollisionSegment(Vec3d p1, Vec3d p2, double scale) {
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
