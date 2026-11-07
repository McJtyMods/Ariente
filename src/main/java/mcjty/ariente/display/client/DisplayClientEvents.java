package mcjty.ariente.display.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.client.WorldHolograms;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.PacketDisplayAction;
import mcjty.ariente.network.ArienteMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

public class DisplayClientEvents {

    private PoseStack worldPoses;

    public static DisplayGeometry.Hit pick(Player player) {
        DisplayGeometry.Hit nearest = null;
        double reach = Math.min(8, player.getBlockReach());
        for (DisplayBaseTile base : DisplayBaseTile.LOADED) {
            if (base.isRemoved() || base.getLevel() != player.level()
                    || base.getBlockPos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 400) {
                continue;
            }
            DisplayGeometry.Hit hit = DisplayGeometry.intersect(base, player, reach);
            if (hit != null && (nearest == null || hit.distance() < nearest.distance())) {
                nearest = hit;
            }
        }
        Minecraft mc = Minecraft.getInstance();
        // Respect closer entities, particularly a configuration hologui in front of a display.
        if (nearest != null && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY
                && player.getEyePosition().distanceTo(mc.hitResult.getLocation()) < nearest.distance()) {
            return null;
        }
        return nearest;
    }

    @SubscribeEvent
    public void input(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !(event.isUseItem() || event.isAttack())) {
            return;
        }
        if (DisplayModuleDrag.active()) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        DisplayGeometry.Hit hit = pick(mc.player);
        if (hit == null) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        // Consume both hand events but submit only the requested hand, preventing double removal.
        InteractionHand selected = mc.player.getMainHandItem().getItem() instanceof DisplayModuleItem
                ? InteractionHand.MAIN_HAND : mc.player.getOffhandItem().getItem() instanceof DisplayModuleItem
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (event.isAttack() || event.getHand() == selected) {
            DisplayGeometry.ModuleHit module = DisplayGeometry.moduleAt(hit);
            if (event.isUseItem() && !mc.player.isShiftKeyDown() && module != null) {
                DisplayModuleDrag.begin(module);
            } else {
                ArienteMessages.INSTANCE.sendToServer(new PacketDisplayAction(hit.tile().getBlockPos(), selected, event.isAttack()));
            }
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DisplayModuleDrag.tick();
            DisplayDataReceiver.tick();
        }
    }

    @SubscribeEvent
    public void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            // AFTER_LEVEL supplies the projection pose in Forge 1.20.1; retain the world camera pose.
            worldPoses = WorldHolograms.copy(event.getPoseStack());
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        WorldHolograms.renderWorld();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (DisplayModuleDrag.active()) {
            DisplayModuleDrag.render(worldPoses, mc.renderBuffers().bufferSource(), event.getCamera().getPosition());
            return;
        }
        DisplayGeometry.Hit hit = pick(mc.player);
        if (hit == null) {
            return;
        }
        drawPreview(mc, hit, worldPoses, event.getCamera().getPosition());
    }

    private static void drawPreview(Minecraft mc, DisplayGeometry.Hit hit, PoseStack poses, Vec3 camera) {
        InteractionHand hand = mc.player.getMainHandItem().getItem() instanceof DisplayModuleItem
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (mc.player.getItemInHand(hand).getItem() instanceof DisplayModuleItem && !mc.player.isShiftKeyDown()) {
            MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
            poses.pushPose();
            poses.translate(hit.tile().getBlockPos().getX() - camera.x, hit.tile().getBlockPos().getY() - camera.y,
                    hit.tile().getBlockPos().getZ() - camera.z);
            DisplayRenderer.preview(hit, mc.player.getItemInHand(hand), poses, buffer);
            poses.popPose();
            buffer.endBatch();
        }
    }
}
