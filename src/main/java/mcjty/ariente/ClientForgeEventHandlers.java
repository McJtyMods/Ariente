package mcjty.ariente;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.network.PacketHitForcefield;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static mcjty.ariente.items.BlueprintItem.EMPTY_BLUEPRINT_MODEL;

public class ClientForgeEventHandlers {

    public static final float STEP_HEIGHT = 1.902f;

    @SubscribeEvent
    public void colorHandlerEventBlock(ColorHandlerEvent.Block event) {
        ModBlocks.initColorHandlers(event.getBlockColors());
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(EMPTY_BLUEPRINT_MODEL, new BuiltInModel(ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE));
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(FluxShipRender.TEXTURE);
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = event.getTarget().getBlockPos();
            EntityPlayer player = event.getPlayer();
            IBlockState state = player.getEntityWorld().getBlockState(pos);
            if (state.getBlock() == ModBlocks.rampBlock) {
                drawSelectionBox(state, player, pos, event.getPartialTicks());
                event.setCanceled(true);
            }
        }
    }

    private static void drawSelectionBox(IBlockState state, EntityPlayer player, BlockPos pos, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        ModBlocks.rampBlock.handleAABB(state, aabb -> RenderGlobal.drawSelectionBoundingBox(aabb.offset(pos).grow(0.002D).offset(-d3, -d4, -d5), 0.0F, 0.0F, 0.0F, 0.4F));

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
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
            if (event.getEntityMounting() instanceof EntityPlayer) {
                FluxLevitatorSounds.playMovingSoundClientInside((EntityPlayer) event.getEntityMounting(), levitator);
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
        EventPriority phase = event.getPhase();
        if (phase == EventPriority.NORMAL) {
            EntityPlayerSP player = (EntityPlayerSP) Ariente.proxy.getClientPlayer();
            if (player != null && Minecraft.getMinecraft().currentScreen == null) {
                ItemStack chestStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                boolean hovering = false;
                if (isValidArmorPiece(chestStack, ModItems.powerSuitChest)) {
                    boolean hasFlight = ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FLIGHT);
                    if (hasFlight) {
                        boolean hasHover = ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.HOVER);
                        if (hasHover) {
                            player.isAirBorne = true;
                            player.motionY = 0;
                            hovering = true;
                        }

                        if (Ariente.proxy.isJumpKeyDown()) {
                            player.isAirBorne = true;
                            player.motionY = UtilityConfiguration.POWERSUIT_FLYVERTICAL_FACTOR.get();
                        } else if (hovering && Ariente.proxy.isSneakKeyDown()) {
                            player.isAirBorne = true;
                            player.motionY = -UtilityConfiguration.POWERSUIT_FLYVERTICAL_FACTOR.get();
                        }
                    }
                }

                ItemStack feetStack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
                player.stepHeight = 0.6f;
                if (isValidArmorPiece(feetStack, ModItems.powerSuitBoots)) {
                    if (ModuleSupport.hasWorkingUpgrade(feetStack, ArmorUpgradeType.STEPASSIST)) {
                        player.stepHeight = STEP_HEIGHT;
                    }
                }

                ItemStack legsStack = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
                if (isValidArmorPiece(legsStack, ModItems.powerSuitLegs)) {
                    if (ModuleSupport.hasWorkingUpgrade(legsStack, ArmorUpgradeType.SPEED)) {
                        if (Ariente.proxy.isForwardKeyDown()) {
                            handleSpeed(player, 0.5, UtilityConfiguration.POWERSUIT_MAX_FORWARD_GROUND_SPEED.get(), UtilityConfiguration.POWERSUIT_MAX_FORWARD_FLY_SPEED.get());
                        } else if (Ariente.proxy.isBackKeyDown()) {
                            handleSpeed(player, -0.5, UtilityConfiguration.POWERSUIT_MAX_BACK_GROUND_SPEED.get(), UtilityConfiguration.POWERSUIT_MAX_BACK_FLY_SPEED.get());
                        }
                    }
                }
            }
        }
    }

    private boolean isValidArmorPiece(ItemStack chestStack, Item powerSuitChest) {
        return !chestStack.isEmpty() && chestStack.getItem() == powerSuitChest && chestStack.hasTagCompound();
    }

    private void handleSpeed(EntityPlayerSP player, double v2, double powersuitMaxForwardGroundSpeed, double powersuitMaxForwardFlySpeed) {
        Vec3d vec3d = player.getLookVec().normalize().scale(v2);
        player.motionX += vec3d.x;
        player.motionZ += vec3d.z;
        Vec3d v = new Vec3d(player.motionX, player.motionY, player.motionZ);
        double max = player.onGround ?
                powersuitMaxForwardGroundSpeed :
                powersuitMaxForwardFlySpeed;
        if (v.lengthSquared() > max * max) {
            v = v.normalize().scale(max);
            player.motionX = v.x;
            player.motionY = v.y;
            player.motionZ = v.z;
        }
    }

}
