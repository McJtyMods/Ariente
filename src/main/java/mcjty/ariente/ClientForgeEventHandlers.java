package mcjty.ariente;

import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.network.PacketHitForcefield;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientForgeEventHandlers {

    @SubscribeEvent
    public void onRenderWorldEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            ForceFieldRenderer.renderForceFields(event.getPoseStack(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (event.isMounting() && event.getLevel().isClientSide && event.getEntityBeingMounted() instanceof FluxLevitatorEntity) {
            FluxLevitatorEntity levitator = (FluxLevitatorEntity) event.getEntityBeingMounted();
            if (event.getEntityMounting() instanceof Player) {
                FluxLevitatorSounds.playMovingSoundClientInside((Player) event.getEntityMounting(), levitator);
            }
        }
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty empty) {
        // This event is only called clientside. Tell the server
        ArienteMessages.INSTANCE.sendToServer(new PacketHitForcefield());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.START || mc.isPaused() || mc.player == null) {
            return;
        }
        Player player = mc.player;
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || player.getAbilities().flying
                || player.isFallFlying() || player.isInWater() || player.isInLava()) {
            return;
        }
        boolean flight = hasUpgrade(player, EquipmentSlot.CHEST, Registration.POWERSUIT_CHEST.get(), ArmorUpgradeType.FLIGHT);
        if (flight) {
            boolean hover = hasUpgrade(player, EquipmentSlot.CHEST, Registration.POWERSUIT_CHEST.get(), ArmorUpgradeType.HOVER);
            boolean jump = mc.screen == null && mc.options.keyJump.isDown();
            boolean sneak = mc.screen == null && mc.options.keyShift.isDown();
            if (jump || hover) {
                double vertical = jump ? UtilityConfiguration.POWERSUIT_FLYVERTICAL_FACTOR.get()
                        : sneak ? -UtilityConfiguration.POWERSUIT_FLYVERTICAL_FACTOR.get() : 0;
                player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1).add(0, vertical, 0));
                player.fallDistance = 0;
            }
        }
        if (mc.screen == null && hasUpgrade(player, EquipmentSlot.LEGS, Registration.POWERSUIT_LEGS.get(), ArmorUpgradeType.SPEED)) {
            if (mc.options.keyUp.isDown()) {
                handleSpeed(player, .5, UtilityConfiguration.POWERSUIT_MAX_FORWARD_GROUND_SPEED.get(),
                        UtilityConfiguration.POWERSUIT_MAX_FORWARD_FLY_SPEED.get(), flight);
            } else if (mc.options.keyDown.isDown()) {
                handleSpeed(player, -.5, UtilityConfiguration.POWERSUIT_MAX_BACK_GROUND_SPEED.get(),
                        UtilityConfiguration.POWERSUIT_MAX_BACK_FLY_SPEED.get(), flight);
            }
        }
    }

    private boolean hasUpgrade(Player player, EquipmentSlot slot, Item item, ArmorUpgradeType type) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.is(item) && ModuleSupport.hasWorkingUpgrade(stack, type);
    }

    private void handleSpeed(Player player, double acceleration, double groundCap, double airCap, boolean flight) {
        Vec3 motion = player.getDeltaMovement();
        Vec3 look = player.getLookAngle().scale(acceleration);
        Vec3 horizontal = new Vec3(motion.x + look.x, 0, motion.z + look.z);
        double max = player.onGround() || !flight ? groundCap : airCap;
        if (player.onGround() && !player.isSprinting()) {
            max /= 2;
        }
        if (horizontal.lengthSqr() > max * max) {
            horizontal = horizontal.normalize().scale(max);
        }
        player.setDeltaMovement(horizontal.x, motion.y, horizontal.z);
    }
}
