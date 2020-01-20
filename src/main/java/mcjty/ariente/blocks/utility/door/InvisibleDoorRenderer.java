package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;



public class InvisibleDoorRenderer extends TileEntityRenderer<InvisibleDoorTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public InvisibleDoorRenderer() {
    }

    @Override
    public void render(InvisibleDoorTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        BlockState state = getWorld().getBlockState(te.getPos());
        if (state.getBlock() != ModBlocks.invisibleDoorBlock.get()) {
            return;
        }

        DoorMarkerTile doorMarkerTile = te.findDoorMarker();
        if (doorMarkerTile == null) {
            return;
        }

        int openphase = DoorMarkerRenderer.getOpenphase(doorMarkerTile);
        int iconIndex = doorMarkerTile.getIconIndex();

        GlStateManager.pushMatrix();

        Direction frontDirection = ModBlocks.invisibleDoorBlock.get().getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            GlStateManager.translated(x, y, z+.5);
            GlStateManager.rotatef(90, 0, 1, 0);
        } else {
            GlStateManager.translated(x + .5, y, z);
        }

        bindTexture(halo);
        DoorMarkerRenderer.renderDoorSegment(openphase, iconIndex);

        GlStateManager.popMatrix();
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(InvisibleDoorTile.class, new InvisibleDoorRenderer());
    }
}
