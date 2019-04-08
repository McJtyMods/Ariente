package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

// A single item transfer render in progress
public class TransferRender {
    private final long startTime;
    private final long endTime;
    private final Vec3d startPos;
    private final Vec3d endPos;
    private final Vec3d d;
    private final ItemStack stack;

    public TransferRender(AutoFieldRenderInfo.Transfer transfer, BlockPos relative) {
        startTime = System.currentTimeMillis();
        endTime = startTime + 2000;
        startPos = new Vec3d(transfer.getSourcePos().getPos().subtract(relative)).add(getPos(transfer.getSourcePos().getSlot()));
        endPos = new Vec3d(transfer.getDestPos().getPos().subtract(relative)).add(getPos(transfer.getDestPos().getSlot()));
        d = endPos.subtract(startPos);
        stack = new ItemStack(transfer.getItem(), 1, transfer.getMeta());
    }

    public boolean render() {
        long time = System.currentTimeMillis();
        if (time > endTime) {
            return false;
        }
        double factor = (time - startTime) / 2000.0;
        Vec3d pos = startPos.add(d.scale(factor));
        renderStack(stack, pos);

        return true;
    }

    private void renderStack(ItemStack stack, Vec3d pos) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.x, pos.y, pos.z);
        GlStateManager.scale(.4f, .4f, .4f);

        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }


    private static Vec3d getPos(PartSlot slot) {
        switch (slot) {
            case NONE:
                return Vec3d.ZERO;
            case DOWN:
                return new Vec3d(.5, .2, .5);
            case UP:
                return new Vec3d(.5, .8, .5);
            case NORTH:
                return new Vec3d(.5, .5, .2);
            case SOUTH:
                return new Vec3d(.5, .5, .8);
            case WEST:
                return new Vec3d(.2, .5, .5);
            case EAST:
                return new Vec3d(.8, .5, .5);
            case DOWN_NW:
                return new Vec3d(.25, .2, .25);
            case DOWN_NE:
                return new Vec3d(.75, .2, .25);
            case DOWN_SW:
                return new Vec3d(.25, .2, .75);
            case DOWN_SE:
                return new Vec3d(.75, .2, .75);
            case UP_NW:
                return new Vec3d(.25, .8, .25);
            case UP_NE:
                return new Vec3d(.75, .8, .25);
            case UP_SW:
                return new Vec3d(.25, .8, .75);
            case UP_SE:
                return new Vec3d(.75, .8, .75);
            case NORTH_UW:
                return new Vec3d(.25, .75, .2);
            case NORTH_UE:
                return new Vec3d(.75, .75, .2);
            case NORTH_DW:
                return new Vec3d(.25, .25, .2);
            case NORTH_DE:
                return new Vec3d(.75, .25, .2);
            case SOUTH_UW:
                return new Vec3d(.25, .75, .8);
            case SOUTH_UE:
                return new Vec3d(.75, .75, .8);
            case SOUTH_DW:
                return new Vec3d(.25, .25, .8);
            case SOUTH_DE:
                return new Vec3d(.75, .25, .8);
            case WEST_US:
                return new Vec3d(.2, .75, .75);
            case WEST_UN:
                return new Vec3d(.2, .75, .25);
            case WEST_DS:
                return new Vec3d(.2, .25, .75);
            case WEST_DN:
                return new Vec3d(.2, .25, .25);
            case EAST_US:
                return new Vec3d(.8, .75, .75);
            case EAST_UN:
                return new Vec3d(.8, .75, .25);
            case EAST_DS:
                return new Vec3d(.8, .25, .75);
            case EAST_DN:
                return new Vec3d(.8, .25, .25);
        }
        return Vec3d.ZERO;
    }

}
