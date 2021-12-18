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
    public void colorHandlerEventBlock(ColorHandlerEvent.Block event) {
        // @todo 1.14
//        ModBlocks.initColorHandlers(event.getBlockColors());
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        // @todo 1.14
//        event.getModelRegistry().putObject(EMPTY_BLUEPRINT_MODEL, new BuiltInModel(ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE));
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        // @todo 1.14
//        event.getMap().registerSprite(FluxShipRender.TEXTURE);
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawSelectionEvent event) {
        if (event.getTarget().getType() == HitResult.Type.BLOCK && event.getTarget() instanceof BlockHitResult) {
            BlockPos pos = ((BlockHitResult) event.getTarget()).getBlockPos();
            Player player = Minecraft.getInstance().player;
            BlockState state = player.getCommandSenderWorld().getBlockState(pos);
            if (state.getBlock() == Registration.RAMP.get()) {
                drawSelectionBox(state, player, pos, event.getPartialTicks());
                event.setCanceled(true);
            }
        }
    }

    private static void drawSelectionBox(BlockState state, Player player, BlockPos pos, float partialTicks) {
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value, GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ZERO.value);
        // @todo 1.18 GlStateManager._lineWidth(2.0F);
        GlStateManager._disableTexture();
        GlStateManager._depthMask(false);

        double d3 = player.xOld + (player.getX() - player.xOld) * partialTicks;
        double d4 = player.yOld + (player.getY() - player.yOld) * partialTicks;
        double d5 = player.zOld + (player.getZ() - player.zOld) * partialTicks;
        // @todo 1.14
//        ModBlocks.rampBlock.handleAABB(state, aabb -> RenderGlobal.drawSelectionBoundingBox(aabb.offset(pos).grow(0.002D).offset(-d3, -d4, -d5), 0.0F, 0.0F, 0.0F, 0.4F));

        GlStateManager._depthMask(true);
        GlStateManager._enableTexture();
        GlStateManager._disableBlend();
    }

    @SubscribeEvent
    public void onRenderWorldEvent(RenderLevelLastEvent event) {
        ForceFieldRenderer.renderForceFields(event.getPartialTick());
    }

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (event.isMounting() && event.getWorldObj().isClientSide && event.getEntityBeingMounted() instanceof FluxLevitatorEntity) {
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
        double max = (player.isOnGround() || !hasFlight) ?
                powersuitMaxForwardGroundSpeed :
                powersuitMaxForwardFlySpeed;

        if (player.isOnGround() && !player.isSprinting()) {
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
