package mcjty.ariente.display.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.display.DisplayAppearance;
import mcjty.ariente.display.DisplayBackground;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.DisplayModuleType;
import mcjty.ariente.display.InstalledModule;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class DisplayRenderer implements BlockEntityRenderer<DisplayBaseTile> {

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void transform(PoseStack poses, DisplayBaseTile tile) {
        Direction normal = DisplayGeometry.facing(tile);
        Direction right = normal.getCounterClockWise();
        poses.translate(.5 - right.getStepX() * .5, tile.getHeight(), .5 - right.getStepZ() * .5);
        poses.mulPose(Axis.YP.rotationDegrees(-normal.toYRot()));
        poses.scale(1 / 64f, -1 / 64f, 1 / 64f);
    }

    @Override
    public void render(DisplayBaseTile tile, float partialTick, PoseStack poses, MultiBufferSource buffer, int light, int overlay) {
        poses.pushPose();
        transform(poses, tile);
        DisplayCanvas c = new DisplayCanvas(poses, buffer);
        int height = tile.getHeight() * 64 - 4;
        boolean left = DisplayGeometry.neighbor(tile, -1) == null;
        boolean right = DisplayGeometry.neighbor(tile, 1) == null;
        DisplayAppearance appearance = tile.appearance();
        c.fill(0, 0, 64, height, appearance.surfaceColor());
        poses.translate(0, 0, .02);
        // A continuous surface with restrained scan lines and optional illuminated perimeter rails.
        if (appearance.type() == DisplayBackground.TRANSPARENT && appearance.alpha() > 0) {
            for (int y = 4; y < height; y += 4) {
                c.fill(0, y, 64, .25f, appearance.scanLineColor());
            }
        }
        if (appearance.border()) {
            c.fill(0, 0, 64, .7f, 0xb077edff);
            c.fill(0, height - .7f, 64, .7f, 0xb077edff);
            if (left) {
                c.fill(0, 0, .7f, height, 0xb077edff);
                c.fill(0, 0, 6, 1.5f, 0xffc0fbff);
                c.fill(0, height - 1.5f, 6, 1.5f, 0xffc0fbff);
            }
            if (right) {
                c.fill(63.3f, 0, .7f, height, 0xb077edff);
                c.fill(58, 0, 6, 1.5f, 0xffc0fbff);
                c.fill(58, height - 1.5f, 6, 1.5f, 0xffc0fbff);
            }
        }
        poses.translate(0, 0, .1);
        for (InstalledModule module : tile.modules()) {
            if (!DisplayModuleDrag.hides(module.id()) && DisplayGeometry.fits(tile, module.x(), module.y(), module.size())) {
                poses.pushPose();
                poses.translate(module.x(), module.y(), 0);
                DisplayModuleRenderers.render(c, module);
                poses.popPose();
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
    public boolean shouldRenderOffScreen(DisplayBaseTile tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
