package mcjty.ariente.blocks.storagecube;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.blocks.storagecube.client.StorageCubeClient;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.components.AbstractHoloComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

/** The server component holds no mutable player state. All transfers use validated atomic actions. */
public class StorageCubePanel extends AbstractHoloComponent<StorageCubePanel> {

    private final StorageCubeTile tile;

    public StorageCubePanel(StorageCubeTile tile) {
        // Include the header controls, scroll bars and lowered hotbar in hover/tooltip targeting.
        super(-.2, -.5, 8.4, 9.2);
        this.tile = tile;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double x, double y) {
        StorageCubeClient.render(tile, poses, buffer, holo, x, y);
    }

    @Override
    public void renderTooltip(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double x, double y) {
        StorageCubeClient.tooltip(tile, holo, poses, buffer, x, y);
    }
}
