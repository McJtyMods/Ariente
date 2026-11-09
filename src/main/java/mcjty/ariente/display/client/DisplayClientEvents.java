package mcjty.ariente.display.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import mcjty.ariente.client.InputGesture;
import mcjty.hologui.gui.HoloRenderBatch;
import mcjty.lib.network.ClientNetworking;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.client.WorldHolograms;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.PacketDisplayAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;

public class DisplayClientEvents {

    public static DisplayGeometry.Hit pick(Player player) {
        DisplayGeometry.Hit nearest = null;
        double reach = Math.min(8, player.blockInteractionRange());
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

    public void input(InputGesture event) {
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
                ClientNetworking.sendToServer(new PacketDisplayAction(hit.tile().getBlockPos(), selected, event.isAttack()));
            }
        }
    }

    public void tick() {
        if (true) {
            DisplayModuleDrag.tick();
            DisplayDataReceiver.tick();
            CameraPerspectiveRenderer.tick();
            LiveCameraRenderer.tick();
        }
    }

    public void extract(LevelExtractionContext event) {
        if (LiveCameraRenderer.isRendering()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null) {
            HoloRenderBatch batch = new HoloRenderBatch();
            PoseStack poses = new PoseStack();
            if (DisplayModuleDrag.active()) {
                DisplayModuleDrag.render(poses, batch, event.camera().position());
            } else {
                DisplayGeometry.Hit hit = pick(mc.player);
                if (hit != null) drawPreview(mc, hit, poses, event.camera().position(), batch);
            }
            WorldHolograms.addRelative(event.camera().position(), batch);
        }
        WorldHolograms.finishExtraction(event);
    }

    public void capture(LevelRenderContext event) {
        WorldHolograms.captureFrame(event);
    }

    public void render(LevelRenderContext event) {
        if (LiveCameraRenderer.isRendering()) return;
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.set(event.levelState().cameraRenderState.viewRotationMatrix);
        try {
            WorldHolograms.renderWorld();
        } finally {
            modelView.popMatrix();
        }
    }

    private static void drawPreview(Minecraft mc, DisplayGeometry.Hit hit, PoseStack poses, Vec3 camera, MultiBufferSource buffer) {
        InteractionHand hand = mc.player.getMainHandItem().getItem() instanceof DisplayModuleItem
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (mc.player.getItemInHand(hand).getItem() instanceof DisplayModuleItem && !mc.player.isShiftKeyDown()) {
            poses.pushPose();
            poses.translate(hit.tile().getBlockPos().getX() - camera.x, hit.tile().getBlockPos().getY() - camera.y,
                    hit.tile().getBlockPos().getZ() - camera.z);
            DisplayRenderer.preview(hit, mc.player.getItemInHand(hand), poses, buffer);
            poses.popPose();
        }
    }
}
