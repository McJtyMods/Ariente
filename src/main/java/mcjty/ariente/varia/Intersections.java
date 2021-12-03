package mcjty.ariente.varia;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

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

    public static Vector3d segmentTriangleTest(Vector3d p1, Vector3d p2, Triangle tri) {
        // Get triangle edge vectors and plane normal
        Vector3d u = tri.b.subtract(tri.a);
        Vector3d v = tri.c.subtract(tri.a);
        Vector3d n = u.cross(v);

        Vector3d dir = p2.subtract(p1);              // ray direction vector
        Vector3d w0 = p1.subtract(tri.a);
        double a = -n.dot(w0);
        double b = n.dot(dir);

        if (Math.abs(b) < 0.00000001) {     // ray is  parallel to triangle plane
            return null;
        }

        // get intersect point of ray with triangle plane
        double r = a / b;
        if (r < 0.0) {                    // ray goes away from triangle
            return null;                   // => no intersect
        }
        // for a segment, also test if (r > 1.0) => no intersect

        Vector3d intersection = p1.add(dir.scale(r));// intersect point of ray and plane

        // is I inside T?
        double uu = u.dot(u);;
        double uv = u.dot(v);;
        double vv = v.dot(v);
        Vector3d w = intersection.subtract(tri.a);
        double wu = w.dot(u);
        double wv = w.dot(v);
        double D = uv * uv - uu * vv;

        // get and test parametric coords
        double s = (uv * wv - vv * wu) / D;
        if (s < 0.0 || s > 1.0) {         // I is outside T
            return null;
        }
        double t = (uv * wu - uu * wv) / D;
        if (t < 0.0 || (s + t) > 1.0) {  // I is outside T
            return null;
        }

        return intersection;                       // I is in T
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
        Vector3d normal;

        // Move to center of box
        Vector3d center = new Vector3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
        Vector3d v0 = tri.a.subtract(center);
        Vector3d v1 = tri.b.subtract(center);
        Vector3d v2 = tri.c.subtract(center);

        // Triangle edges
        Vector3d e0 = v1.subtract(v0);
        Vector3d e1 = v2.subtract(v1);
        Vector3d e2 = v0.subtract(v2);

        Vector3d extent = new Vector3d(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);

        // test the 9 tests first (this was faster)
        Vector3d f = new Vector3d(Math.abs(e0.x), Math.abs(e0.y), Math.abs(e0.z));
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

        f = new Vector3d(Math.abs(e1.x), Math.abs(e1.y), Math.abs(e1.z));
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

        f = new Vector3d(Math.abs(e2.x), Math.abs(e2.y), Math.abs(e2.z));
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
        normal = e0.cross(e1);
        double d = -normal.dot(v0);
        if (!planeBoxOverlap(normal, d, extent)) {
            return false;
        }
        return true;
    }

    private static boolean planeBoxOverlap(Vector3d normal, double d, Vector3d maxbox) {
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

        Vector3d vmin = new Vector3d(vminx, vminy, vminz);
        if (normal.dot(vmin) + d > 0.0) {
            return false;
        }
        Vector3d vmax = new Vector3d(vmaxx, vmaxy, vmaxz);
        if (normal.dot(vmax) + d >= 0.0) {
            return true;
        }
        return false;
    }

}
