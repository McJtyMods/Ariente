package mcjty.ariente.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BlueprintRenderer extends BlockEntityWithoutLevelRenderer {

    public static final ResourceLocation BASE_MODEL = ResourceLocation.fromNamespaceAndPath("ariente", "item/blueprint_base");

    public BlueprintRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext display, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();
        pose.pushPose();
        pose.translate(.5, .5, .5);
        renderer.render(stack, ItemDisplayContext.GUI, false, pose, buffers, light, overlay, mc.getModelManager().getModel(BASE_MODEL));
        ItemStack destination = BlueprintItem.getDestination(stack);
        if (!destination.isEmpty() && !destination.is(Registration.BLUEPRINT.get())) {
            pose.translate(0, 0, .15);
            pose.scale(.5F, .5F, .5F);
            pose.mulPose(Axis.XP.rotationDegrees(30));
            pose.mulPose(Axis.YP.rotationDegrees((System.currentTimeMillis() / 50) % 360));
            renderer.renderStatic(destination, ItemDisplayContext.GUI, light, overlay, pose, buffers, mc.level, 0);
        }
        pose.popPose();
    }
}
