package mcjty.ariente.varia;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class Intersections {

    public static double max3(double a, double b, double c) {
        if (a >= b && a >= c) {
            return a;
        }
        if (b >= c && b >= a) {
            return b;
        }
        return c;
    }

    public static double min3(double a, double b, double c) {
        if (a <= b && a <= c) {
            return a;
        }
        if (b <= c && b <= a) {
            return b;
        }
        return c;
    }


    private static boolean testAxis(double a, double b, double fa, double fb, double va,
                             double vb, double wa, double wb, double ea, double eb) {
        double p0 = a * va + b * vb;
        double p2 = a * wa + b * wb;
        double min;
        double max;
        if (p0 < p2) {
            min = p0;
            max = p2;
        } else {
            min = p2;
            max = p0;
        }
        double rad = fa * ea + fb * eb;
        return (min > rad || max < -rad);
    }

    public static boolean boxTriangleTest(AxisAlignedBB aabb, Triangle tri) {
        // use separating axis theorem to test overlap between triangle and box
        // need to test for overlap in these directions:
        //
        // 1) the {x,y,z}-directions (actually, since we use the AABB of the
        // triangle
        // we do not even need to test these)
        // 2) normal of the triangle
        // 3) crossproduct(edge from tri, {x,y,z}-directin)
        // this gives 3x3=9 more tests
        Vec3d normal;

        // Move to center of box
        Vec3d center = aabb.getCenter();
        Vec3d v0 = tri.a.subtract(center);
        Vec3d v1 = tri.b.subtract(center);
        Vec3d v2 = tri.c.subtract(center);

        // Triangle edges
        Vec3d e0 = v1.subtract(v0);
        Vec3d e1 = v2.subtract(v1);
        Vec3d e2 = v0.subtract(v2);

        Vec3d extent = new Vec3d(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);

        // test the 9 tests first (this was faster)
        Vec3d f = new Vec3d(Math.abs(e0.x), Math.abs(e0.y), Math.abs(e0.z));
        if (testAxis(e0.z, -e0.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, extent.y,
                extent.z)) {
            return false;
        }
        if (testAxis(-e0.z, e0.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, extent.x,
                extent.z)) {
            return false;
        }
        if (testAxis(e0.y, -e0.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, extent.x,
                extent.y)) {
            return false;
        }

        f = new Vec3d(Math.abs(e1.x), Math.abs(e1.y), Math.abs(e1.z));
        if (testAxis(e1.z, -e1.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, extent.y,
                extent.z)) {
            return false;
        }
        if (testAxis(-e1.z, e1.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, extent.x,
                extent.z)) {
            return false;
        }
        if (testAxis(e1.y, -e1.x, f.y, f.x, v0.x, v0.y, v1.x, v1.y, extent.x,
                extent.y)) {
            return false;
        }

        f = new Vec3d(Math.abs(e2.x), Math.abs(e2.y), Math.abs(e2.z));
        if (testAxis(e2.z, -e2.y, f.z, f.y, v0.y, v0.z, v1.y, v1.z, extent.y,
                extent.z)) {
            return false;
        }
        if (testAxis(-e2.z, e2.x, f.z, f.x, v0.x, v0.z, v1.x, v1.z, extent.x,
                extent.z)) {
            return false;
        }
        if (testAxis(e2.y, -e2.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, extent.x,
                extent.y)) {
            return false;
        }

        // first test overlap in the {x,y,z}-directions
        // find min, max of the triangle each direction, and test for overlap in
        // that direction -- this is equivalent to testing a minimal AABB around
        // the triangle against the AABB

        // test in X-direction
        if (min3(v0.x, v1.x, v2.x) > extent.x
                || max3(v0.x, v1.x, v2.x) < -extent.x) {
            return false;
        }

        // test in Y-direction
        if (min3(v0.y, v1.y, v2.y) > extent.y
                || max3(v0.y, v1.y, v2.y) < -extent.y) {
            return false;
        }

        // test in Z-direction
        if (min3(v0.z, v1.z, v2.z) > extent.z
                || max3(v0.z, v1.z, v2.z) < -extent.z) {
            return false;
        }

        // test if the box intersects the plane of the triangle
        // compute plane equation of triangle: normal*x+d=0
        normal = e0.crossProduct(e1);
        double d = -normal.dotProduct(v0);
        if (!planeBoxOverlap(normal, d, extent)) {
            return false;
        }
        return true;
    }

    private static boolean planeBoxOverlap(Vec3d normal, double d, Vec3d maxbox) {
        double vminx;
        double vmaxx;
        double vminy;
        double vmaxy;
        double vminz;
        double vmaxz;

        if (normal.x > 0.0) {
            vminx = -maxbox.x;
            vmaxx = maxbox.x;
        } else {
            vminx = maxbox.x;
            vmaxx = -maxbox.x;
        }

        if (normal.y > 0.0) {
            vminy = -maxbox.y;
            vmaxy = maxbox.y;
        } else {
            vminy = maxbox.y;
            vmaxy = -maxbox.y;
        }

        if (normal.z > 0.0) {
            vminz = -maxbox.z;
            vmaxz = maxbox.z;
        } else {
            vminz = maxbox.z;
            vmaxz = -maxbox.z;
        }

        Vec3d vmin = new Vec3d(vminx, vminy, vminz);
        if (normal.dotProduct(vmin) + d > 0.0) {
            return false;
        }
        Vec3d vmax = new Vec3d(vmaxx, vmaxy, vmaxz);
        if (normal.dotProduct(vmax) + d >= 0.0) {
            return true;
        }
        return false;
    }

}
