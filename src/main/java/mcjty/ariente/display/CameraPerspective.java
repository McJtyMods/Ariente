package mcjty.ariente.display;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** A stable orbit around the captured scene; zoom changes the lens rather than the capture radius. */
public record CameraPerspective(Matrix4f view, Matrix4f projection, Vec3 eye) {

    private static final double HALF_FIELD_OF_VIEW = Math.toRadians(30);

    public static CameraPerspective of(Vec3 center, float radius, CameraView basis, int width, int height, int zoom) {
        float aspect = (float) width / height;
        double fitAngle = Math.min(HALF_FIELD_OF_VIEW, Math.atan(Math.tan(HALF_FIELD_OF_VIEW) * aspect));
        double distance = Math.max(4, Math.max(.5, radius) / Math.sin(fitAngle) * 1.05);
        Vec3 eye = center.add(basis.direction().scale(distance));
        Matrix4f view = new Matrix4f().lookAt((float) eye.x, (float) eye.y, (float) eye.z,
                (float) center.x, (float) center.y, (float) center.z,
                (float) basis.up().x, (float) basis.up().y, (float) basis.up().z);
        float fieldOfView = (float) (2 * Math.atan(Math.tan(HALF_FIELD_OF_VIEW) * 100 / Math.max(1, zoom)));
        // The eye stays outside the bounding sphere. A nearby clip plane wastes depth precision on empty space.
        float near = (float) Math.max(.05, (distance - radius) * .5);
        Matrix4f projection = new Matrix4f().perspective(fieldOfView, aspect, near, (float) (distance + radius * 2 + 16));
        return new CameraPerspective(view, projection, eye);
    }
}
