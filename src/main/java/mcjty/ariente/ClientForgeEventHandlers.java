package mcjty.ariente;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.network.PacketHitForcefield;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientForgeEventHandlers {

    public static final float STEP_HEIGHT = 1.902f;

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

    // @todo 1.14
//    @SubscribeEvent
//    public void onClientTick(TickEvent.ClientTickEvent event) {
//        EventPriority phase = event.getPhase();
//        if (phase == EventPriority.NORMAL) {
//            EntityPlayerSP player = (EntityPlayerSP) Ariente.proxy.getClientPlayer();
//            if (player != null && Minecraft.getInstance().currentScreen == null) {
//                ItemStack chestStack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
//                boolean hovering = false;
//                boolean hasFlight = false;
//                if (isValidArmorPiece(chestStack, ModItems.powerSuitChest)) {
//                    hasFlight = ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FLIGHT);
//                    if (hasFlight) {
//                        boolean hasHover = ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.HOVER);
//                        if (hasHover) {
//                            player.isAirBorne = true;
//                            player.motionY = 0;
//                            hovering = true;
//                        }
//
//                        if (Ariente.proxy.isJumpKeyDown()) {
//                            player.isAirBorne = true;
//                            player.motionY = UtilityConfiguration.POWERSUIT_FLYVERTICAL_FACTOR.get();
//                        } else if (hovering && Ariente.proxy.isSneakKeyDown()) {
//                            player.isAirBorne = true;
//                            player.motionY = -UtilityConfiguration.POWERSUIT_FLYVERTICAL_FACTOR.get();
//                        }
//                    }
//                }
//
//                ItemStack feetStack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
//                player.stepHeight = 0.6f;
//                if (isValidArmorPiece(feetStack, ModItems.powerSuitBoots)) {
//                    if (ModuleSupport.hasWorkingUpgrade(feetStack, ArmorUpgradeType.STEPASSIST)) {
//                        player.stepHeight = STEP_HEIGHT;
//                    }
//                }
//
//                ItemStack legsStack = player.getItemStackFromSlot(EquipmentSlotType.LEGS);
//                if (isValidArmorPiece(legsStack, ModItems.powerSuitLegs)) {
//                    if (ModuleSupport.hasWorkingUpgrade(legsStack, ArmorUpgradeType.SPEED)) {
//                        if (Ariente.proxy.isForwardKeyDown()) {
//                            handleSpeed(player, 0.5, UtilityConfiguration.POWERSUIT_MAX_FORWARD_GROUND_SPEED.get(), UtilityConfiguration.POWERSUIT_MAX_FORWARD_FLY_SPEED.get(), hasFlight);
//                        } else if (Ariente.proxy.isBackKeyDown()) {
//                            handleSpeed(player, -0.5, UtilityConfiguration.POWERSUIT_MAX_BACK_GROUND_SPEED.get(), UtilityConfiguration.POWERSUIT_MAX_BACK_FLY_SPEED.get(), hasFlight);
//                        }
//                    }
//                }
//            }
//        }
//    }

    private boolean isValidArmorPiece(ItemStack chestStack, Item powerSuitChest) {
        return !chestStack.isEmpty() && chestStack.getItem() == powerSuitChest && chestStack.hasTag();
    }

    private void handleSpeed(Player player, double v2, double powersuitMaxForwardGroundSpeed, double powersuitMaxForwardFlySpeed, boolean hasFlight) {
        Vec3 vec3d = player.getLookAngle().normalize().scale(v2);
        double motionX = player.getDeltaMovement().x;
        double motionY = player.getDeltaMovement().y;
        double motionZ = player.getDeltaMovement().z;
        motionX += vec3d.x;
        motionZ += vec3d.z;
        Vec3 v = new Vec3(motionX, motionY, motionZ);
        double max = (player.onGround() || !hasFlight) ?
                powersuitMaxForwardGroundSpeed :
                powersuitMaxForwardFlySpeed;

        if (player.onGround() && !player.isSprinting()) {
            max /= 2;
        }

        if (v.lengthSqr() > max * max) {
            v = v.normalize().scale(max);
            motionX = v.x;
//            player.motionY = v.y;
            motionZ = v.z;
        }
        player.setDeltaMovement(motionX, motionY, motionZ);
    }

}
