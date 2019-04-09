package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.client.RenderHelper;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.spline.CatmullRomSpline;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

// A single item transfer render in progress
public class TransferRender {
    private final long startTime;
    private final long endTime;
    private final ItemStack stack;
    private final CatmullRomSpline<Vec3dAlpha> spline;

    private final Random random = new Random();

    private static class Vec3dAlpha {
        private final double x;
        private final double y;
        private final double z;
        private final double alpha;

        public Vec3dAlpha() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.alpha = 0;
        }

        public Vec3dAlpha(double x, double y, double z, double alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.alpha = alpha;
        }

        public Vec3dAlpha subtract(Vec3dAlpha other) {
            return new Vec3dAlpha(x - other.x, y - other.y, z - other.z, alpha - other.alpha);
        }

        public Vec3dAlpha add(Vec3dAlpha other) {
            return new Vec3dAlpha(x + other.x, y + other.y, z + other.z, alpha + other.alpha);
        }

        public Vec3dAlpha scale(double scale) {
            return new Vec3dAlpha(x * scale, y * scale, z * scale, alpha * scale);
        }
    }

    public TransferRender(AutoFieldRenderInfo.Transfer transfer, BlockPos relative) {
        startTime = System.currentTimeMillis();
        endTime = startTime + 2000;
        stack = new ItemStack(transfer.getItem(), 1, transfer.getMeta());

        Vec3d start = new Vec3d(transfer.getSourcePos().getPos().subtract(relative)).add(getPos(transfer.getSourcePos().getSlot()));
        Vec3d end = new Vec3d(transfer.getDestPos().getPos().subtract(relative)).add(getPos(transfer.getDestPos().getSlot()));
        Vec3d mid = start.add(end).scale(0.5).addVector(random.nextFloat() * .6 - .3, random.nextFloat() * .6 - .3, random.nextFloat() * .6 - .3);

        spline = new CatmullRomSpline<>(
                Vec3dAlpha::new,
                Vec3dAlpha::subtract,
                Vec3dAlpha::add,
                Vec3dAlpha::scale);
        spline.addPoint(new Vec3dAlpha(start.x, start.y, start.z, 0.0), 0.0f);
        spline.addPoint(new Vec3dAlpha(mid.x, mid.y, mid.z, 0.5), 0.5f);
        spline.addPoint(new Vec3dAlpha(end.x, end.y, end.z, 0.0), 1.0f);
    }

    public boolean render() {
        long time = System.currentTimeMillis();
        if (time > endTime) {
            return false;
        }
        double factor = (time - startTime) / 2000.0;
        spline.calculate((float) factor);
        Vec3dAlpha pos = spline.getInterpolated();
        renderStack(stack, new Vec3d(pos.x, pos.y, pos.z), pos.alpha);

        return true;
    }

    private void renderStack(ItemStack stack, Vec3d pos, double alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.x, pos.y, pos.z);
        GlStateManager.scale(.4f, .4f, .4f);
        GlStateManager.rotate((float) alpha * 300, 0, 1, 0);
        RenderHelper.renderStackOnGround(stack, alpha);

//        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }


    private static Vec3d getPos(PartSlot slot) {
        double d = .3;
        double e = .7;
        switch (slot) {
            case NONE:
                return Vec3d.ZERO;
            case DOWN:
                return new Vec3d(.5, d, .5);
            case UP:
                return new Vec3d(.5, e, .5);
            case NORTH:
                return new Vec3d(.5, .5, d);
            case SOUTH:
                return new Vec3d(.5, .5, e);
            case WEST:
                return new Vec3d(d, .5, .5);
            case EAST:
                return new Vec3d(e, .5, .5);
            case DOWN_NW:
                return new Vec3d(.25, d, .25);
            case DOWN_NE:
                return new Vec3d(.75, d, .25);
            case DOWN_SW:
                return new Vec3d(.25, d, .75);
            case DOWN_SE:
                return new Vec3d(.75, d, .75);
            case UP_NW:
                return new Vec3d(.25, e, .25);
            case UP_NE:
                return new Vec3d(.75, e, .25);
            case UP_SW:
                return new Vec3d(.25, e, .75);
            case UP_SE:
                return new Vec3d(.75, e, .75);
            case NORTH_UW:
                return new Vec3d(.25, .75, d);
            case NORTH_UE:
                return new Vec3d(.75, .75, d);
            case NORTH_DW:
                return new Vec3d(.25, .25, d);
            case NORTH_DE:
                return new Vec3d(.75, .25, d);
            case SOUTH_UW:
                return new Vec3d(.25, .75, e);
            case SOUTH_UE:
                return new Vec3d(.75, .75, e);
            case SOUTH_DW:
                return new Vec3d(.25, .25, e);
            case SOUTH_DE:
                return new Vec3d(.75, .25, e);
            case WEST_US:
                return new Vec3d(d, .75, .75);
            case WEST_UN:
                return new Vec3d(d, .75, .25);
            case WEST_DS:
                return new Vec3d(d, .25, .75);
            case WEST_DN:
                return new Vec3d(d, .25, .25);
            case EAST_US:
                return new Vec3d(e, .75, .75);
            case EAST_UN:
                return new Vec3d(e, .75, .25);
            case EAST_DS:
                return new Vec3d(e, .25, .75);
            case EAST_DN:
                return new Vec3d(e, .25, .25);
        }
        return Vec3d.ZERO;
    }


}
