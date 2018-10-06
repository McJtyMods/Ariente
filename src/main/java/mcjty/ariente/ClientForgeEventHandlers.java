package mcjty.ariente;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.dimension.EditMode;
import mcjty.ariente.dimension.EditModeClient;
import mcjty.ariente.entities.FluxLevitatorEntity;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.modules.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.network.PacketHitForcefield;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static mcjty.ariente.items.BlueprintItem.EMPTY_BLUEPRINT_MODEL;

public class ClientForgeEventHandlers {

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(EMPTY_BLUEPRINT_MODEL, new BuiltInModel(ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE));

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
        if (EditMode.editMode) {
            EditModeClient.renderPart(event.getPartialTicks());
        }
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
            if (player != null) {
                ItemStack chestStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (!chestStack.isEmpty() && chestStack.getItem() == ModItems.powerSuitChest && chestStack.hasTagCompound()) {
                    if (ModuleSupport.hasWorkingUpgrade(chestStack, ArmorUpgradeType.FLIGHT)) {
                        if (Ariente.proxy.isJumpKeyDown()) {
                            player.isAirBorne = true;
                            player.motionY = 0.4;
                        }
                    }
                }

                ItemStack feetStack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
                player.stepHeight = 0.6f;
                if (!feetStack.isEmpty() && feetStack.getItem() == ModItems.powerSuitBoots && feetStack.hasTagCompound()) {
                    if (ModuleSupport.hasWorkingUpgrade(feetStack, ArmorUpgradeType.STEPASSIST)) {
                        player.stepHeight = 1.9f;
                    }
                }
            }
        }
    }

}
