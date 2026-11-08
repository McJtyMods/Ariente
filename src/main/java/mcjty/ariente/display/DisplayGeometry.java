package mcjty.ariente.display;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Shared by preview, server placement validation and future screen interactions. */
public class DisplayGeometry {

    public static final int PIXELS = 64;
    public static final int SNAP = 4;

    public static Direction facing(DisplayBaseTile tile) {
        return tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public static DisplayBaseTile neighbor(DisplayBaseTile tile, int offset) {
        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos().relative(facing(tile).getCounterClockWise(), offset);
        if (level != null && level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof DisplayBaseTile other
                && facing(other) == facing(tile)) {
            return other;
        }
        return null;
    }

    public static List<DisplayBaseTile> row(DisplayBaseTile tile) {
        DisplayBaseTile left = tile;
        while (neighbor(left, -1) != null) {
            left = neighbor(left, -1);
        }
        List<DisplayBaseTile> row = new ArrayList<>();
        for (DisplayBaseTile next = left; next != null; next = neighbor(next, 1)) {
            row.add(next);
        }
        return row;
    }

    public static boolean overlaps(int x, int y, int w, int h, int ox, int oy, int ow, int oh) {
        return x < ox + ow && x + w > ox && y < oy + oh && y + h > oy;
    }

    public static boolean fits(DisplayBaseTile tile, int x, int y, DisplayModuleType.Size size) {
        if (x < 0 || x >= PIXELS || y < 0 || y + size.height() > tile.getHeight() * PIXELS - 4) {
            return false;
        }
        for (int offset = 1; offset <= (x + size.width() - 1) / PIXELS; offset++) {
            DisplayBaseTile other = neighbor(tile, offset);
            if (other == null || other.getHeight() != tile.getHeight()) {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlace(DisplayBaseTile tile, DisplayModuleType.Size size, int x, int y) {
        return canPlace(tile, size, x, y, null);
    }

    public static boolean canPlace(DisplayBaseTile tile, DisplayModuleType.Size size, int x, int y, InstalledModule exclude) {
        if (((exclude == null || !tile.modules().contains(exclude)) && tile.modules().size() >= DisplayBaseTile.MAX_MODULES) || !fits(tile, x, y, size)) {
            return false;
        }
        // A module can extend at most eight blocks beyond its owning base.
        for (int offset = -8; offset <= (x + size.width() - 1) / PIXELS; offset++) {
            DisplayBaseTile owner = neighbor(tile, offset);
            if (owner == null) {
                continue;
            }
            for (InstalledModule installed : owner.modules()) {
                if (installed != exclude && overlaps(x, y, size.width(), size.height(), installed.x() + offset * PIXELS,
                        installed.y(), installed.size().width(), installed.size().height())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ModuleHit moduleAt(Hit hit) {
        for (int offset = 0; offset >= -8; offset--) {
            DisplayBaseTile owner = neighbor(hit.tile(), offset);
            if (owner == null) {
                break;
            }
            int x = hit.x() - offset * PIXELS;
            for (InstalledModule module : owner.modules()) {
                if (module.contains(x, hit.y()) && fits(owner, module.x(), module.y(), module.size())) {
                    return new ModuleHit(owner, module, x - module.x(), hit.y() - module.y());
                }
            }
        }
        return null;
    }

    public static boolean sameScreen(DisplayBaseTile source, DisplayBaseTile target) {
        if (source.getLevel() != target.getLevel() || source.getBlockPos().getY() != target.getBlockPos().getY()
                || facing(source) != facing(target) || source.getHeight() != target.getHeight()) {
            return false;
        }
        Direction right = facing(source).getCounterClockWise();
        BlockPos delta = target.getBlockPos().subtract(source.getBlockPos());
        int offset = delta.getX() * right.getStepX() + delta.getZ() * right.getStepZ();
        if (!source.getBlockPos().relative(right, offset).equals(target.getBlockPos())) {
            return false;
        }
        int step = Integer.signum(offset);
        for (int i = step; step != 0 && Math.abs(i) <= Math.abs(offset); i += step) {
            if (neighbor(source, i) == null) {
                return false;
            }
        }
        return true;
    }

    public static DragPosition dragPosition(DisplayBaseTile source, InstalledModule module, Hit start, Hit current) {
        int x = Math.floorDiv(module.x() + current.x() - start.x(), SNAP) * SNAP;
        int y = Math.floorDiv(module.y() + current.y() - start.y(), SNAP) * SNAP;
        int offset = Math.floorDiv(x, PIXELS);
        DisplayBaseTile target = neighbor(source, offset);
        int localX = Math.floorMod(x, PIXELS);
        boolean valid = target != null && sameScreen(source, target)
                && canPlace(target, module.size(), localX, y, module);
        return new DragPosition(target, localX, y, x, valid);
    }

    public static Hit intersect(DisplayBaseTile tile, Player player, double reach) {
        Hit hit = project(tile, player, reach, player.getLookAngle());
        return hit != null && hit.x() >= 0 && hit.x() < PIXELS && hit.y() >= 0
                && hit.y() < tile.getHeight() * PIXELS - 4 ? hit : null;
    }

    /** Project onto the screen plane, including coordinates beyond its edges for invalid drag previews. */
    public static Hit project(DisplayBaseTile tile, Player player, double reach, Vec3 look) {
        Vec3 eye = player.getEyePosition();
        Direction facing = facing(tile);
        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        double denominator = look.dot(normal);
        // Only the front side is interactive; avoid parallel rays and hits behind the player.
        if (denominator >= -.0001) {
            return null;
        }
        Vec3 center = Vec3.atCenterOf(tile.getBlockPos());
        double distance = center.subtract(eye).dot(normal) / denominator;
        if (distance < 0 || distance > reach) {
            return null;
        }
        Vec3 point = eye.add(look.scale(distance));
        Vec3 right = Vec3.atLowerCornerOf(facing.getCounterClockWise().getNormal());
        int x = (int) Math.floor((point.subtract(center).dot(right) + .5) * PIXELS);
        int y = (int) Math.floor((tile.getBlockPos().getY() + tile.getHeight() - point.y) * PIXELS);
        BlockHitResult obstruction = tile.getLevel().clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (obstruction.getType() != HitResult.Type.MISS && eye.distanceToSqr(obstruction.getLocation()) + .0001 < distance * distance) {
            return null;
        }
        return new Hit(tile, x, y, distance);
    }

    public record Hit(DisplayBaseTile tile, int x, int y, double distance) {

        public int snapX() {
            return x / SNAP * SNAP;
        }

        public int snapY() {
            return y / SNAP * SNAP;
        }
    }

    public record DragPosition(DisplayBaseTile target, int x, int y, int sourceX, boolean valid) {
    }

    public record ModuleHit(DisplayBaseTile owner, InstalledModule module, int x, int y) {
    }
}
