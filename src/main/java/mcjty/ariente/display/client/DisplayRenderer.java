package mcjty.ariente.display.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import mcjty.ariente.client.WorldHolograms;
import mcjty.ariente.display.DisplayAppearance;
import mcjty.ariente.display.DisplayBackground;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.DisplayModuleType;
import mcjty.ariente.display.InstalledModule;
import net.minecraft.client.renderer.MultiBufferSource;
import mcjty.ariente.client.ExtractedBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DisplayRenderer extends ExtractedBlockRenderer<DisplayBaseTile> {

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void transform(PoseStack poses, DisplayBaseTile tile) {
        Direction normal = DisplayGeometry.facing(tile);
        Direction right = normal.getCounterClockWise();
        poses.translate(.5 - right.getStepX() * .5, tile.getHeight(), .5 - right.getStepZ() * .5);
        poses.mulPose(Axis.YP.rotationDegrees(-normal.toYRot()));
        poses.scale(1 / 64f, -1 / 64f, 1 / 64f);
    }

    public AABB getRenderBoundingBox(DisplayBaseTile tile) {
        return tile.getRenderBoundingBox();
    }

    @Override
    public void render(DisplayBaseTile tile, float partialTick, PoseStack poses, MultiBufferSource buffer, int light, int overlay) {
        if (LiveCameraRenderer.isRendering()) {
            return;
        }
        List<DisplayBaseTile> row = DisplayGeometry.row(tile);
        DisplayBaseTile first = row.get(0);
        if (!WorldHolograms.begin(first)) {
            return;
        }
        PoseStack snapshot = WorldHolograms.copy(poses);
        snapshot.translate(first.getBlockPos().getX() - tile.getBlockPos().getX(), 0,
                first.getBlockPos().getZ() - tile.getBlockPos().getZ());
        Vec3 center = Vec3.atCenterOf(first.getBlockPos()).add(Vec3.atLowerCornerOf(DisplayGeometry.facing(first)
                .getCounterClockWise().getUnitVec3i()).scale((row.size() - 1) / 2.0)).add(0, (first.getHeight() - 1) / 2.0, 0);
        WorldHolograms.add(center, buffers -> renderRow(first, row, snapshot, buffers));
    }

    private static void renderRow(DisplayBaseTile tile, List<DisplayBaseTile> row, PoseStack poses, MultiBufferSource buffer) {
        poses.pushPose();
        transform(poses, tile);
        DisplayCanvas c = new DisplayCanvas(poses, buffer);
        int height = tile.getHeight() * 64 - 4;
        int width = row.size() * 64;
        DisplayAppearance appearance = tile.appearance();
        // One joined surface, flushed before every module, including modules spanning bases.
        c.fill(0, 0, width, height, appearance.surfaceColor());
        poses.translate(0, 0, .02);
        // A continuous surface with restrained scan lines and optional illuminated perimeter rails.
        if (appearance.type() == DisplayBackground.TRANSPARENT && appearance.alpha() > 0) {
            for (int y = 4; y < height; y += 4) {
                c.fill(0, y, width, .25f, appearance.scanLineColor());
            }
        }
        if (appearance.border()) {
            c.border(0, 0, width, height, 0xb077edff);
            c.fill(0, 0, 6, 1.5f, 0xffc0fbff);
            c.fill(0, height - 1.5f, 6, 1.5f, 0xffc0fbff);
            c.fill(width - 6, 0, 6, 1.5f, 0xffc0fbff);
            c.fill(width - 6, height - 1.5f, 6, 1.5f, 0xffc0fbff);
        }
        poses.translate(0, 0, .1);
        for (int column = 0; column < row.size(); column++) {
            DisplayBaseTile owner = row.get(column);
            for (InstalledModule module : owner.modules()) {
                if (!DisplayModuleDrag.hides(module.id()) && DisplayGeometry.fits(owner, module.x(), module.y(), module.size())) {
                    poses.pushPose();
                    poses.translate(column * 64 + module.x(), module.y(), 0);
                    DisplayModuleRenderers.render(c, module);
                    poses.popPose();
                }
            }
        }
        poses.popPose();
    }

    public static void preview(DisplayGeometry.Hit hit, ItemStack held, PoseStack poses, MultiBufferSource buffer) {
        if (!(held.getItem() instanceof DisplayModuleItem item)) {
            return;
        }
        DisplayModuleType.Size size = item.type().size(held);
        boolean valid = DisplayGeometry.canPlace(hit.tile(), size, hit.snapX(), hit.snapY());
        poses.pushPose();
        transform(poses, hit.tile());
        poses.translate(0, 0, .5);
        DisplayCanvas c = new DisplayCanvas(poses, buffer);
        int rgb = valid ? 0x54ffb1 : 0xff596c;
        c.fill(hit.snapX(), hit.snapY(), size.width(), size.height(), 0x28000000 | rgb);
        c.border(hit.snapX(), hit.snapY(), size.width(), size.height(), 0xff000000 | rgb);
        c.text(valid ? "INSTALL" : "NO ROOM", hit.snapX() + 3, hit.snapY() + 3, rgb, .65f, size.width() - 6);
        poses.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
