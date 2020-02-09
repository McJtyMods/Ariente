package mcjty.ariente;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.network.PacketHitForcefield;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
    public void onDrawBlockHighlight(DrawHighlightEvent event) {
        if (event.getTarget().getType() == RayTraceResult.Type.BLOCK && event.getTarget() instanceof BlockRayTraceResult) {
            BlockPos pos = ((BlockRayTraceResult) event.getTarget()).getPos();
            PlayerEntity player = Minecraft.getInstance().player;
            BlockState state = player.getEntityWorld().getBlockState(pos);
            if (state.getBlock() == ModBlocks.RAMP_BLOCK) {
                drawSelectionBox(state, player, pos, event.getPartialTicks());
                event.setCanceled(true);
            }
        }
    }

    private static void drawSelectionBox(BlockState state, PlayerEntity player, BlockPos pos, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
        GlStateManager.lineWidth(2.0F);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);

        double d3 = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * partialTicks;
        double d4 = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * partialTicks;
        double d5 = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * partialTicks;
        // @todo 1.14
//        ModBlocks.rampBlock.handleAABB(state, aabb -> RenderGlobal.drawSelectionBoundingBox(aabb.offset(pos).grow(0.002D).offset(-d3, -d4, -d5), 0.0F, 0.0F, 0.0F, 0.4F));

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    @SubscribeEvent
    public void onRenderWorldEvent(RenderWorldLastEvent event) {
        ForceFieldRenderer.renderForceFields(event.getPartialTicks());
    }

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (event.isMounting() && event.getWorldObj().isRemote && event.getEntityBeingMounted() instanceof FluxLevitatorEntity) {
            FluxLevitatorEntity levitator = (FluxLevitatorEntity) event.getEntityBeingMounted();
            if (event.getEntityMounting() instanceof PlayerEntity) {
                FluxLevitatorSounds.playMovingSoundClientInside((PlayerEntity) event.getEntityMounting(), levitator);
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

    private void handleSpeed(PlayerEntity player, double v2, double powersuitMaxForwardGroundSpeed, double powersuitMaxForwardFlySpeed, boolean hasFlight) {
        Vec3d vec3d = player.getLookVec().normalize().scale(v2);
        double motionX = player.getMotion().x;
        double motionY = player.getMotion().y;
        double motionZ = player.getMotion().z;
        motionX += vec3d.x;
        motionZ += vec3d.z;
        Vec3d v = new Vec3d(motionX, motionY, motionZ);
        double max = (player.onGround || !hasFlight) ?
                powersuitMaxForwardGroundSpeed :
                powersuitMaxForwardFlySpeed;

        if (player.onGround && !player.isSprinting()) {
            max /= 2;
        }

        if (v.lengthSquared() > max * max) {
            v = v.normalize().scale(max);
            motionX = v.x;
//            player.motionY = v.y;
            motionZ = v.z;
        }
        player.setMotion(motionX, motionY, motionZ);
    }

}
