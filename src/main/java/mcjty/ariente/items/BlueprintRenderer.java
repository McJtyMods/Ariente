package mcjty.ariente.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class BlueprintRenderer implements SpecialModelRenderer<BlueprintRenderer.State> {

    public static final Identifier BASE_MODEL = Identifier.fromNamespaceAndPath("ariente", "item/blueprint_base");

    public record State(ItemStackRenderState item, float rotation) {
    }

    @Override
    public State extractArgument(ItemStack stack) {
        ItemStack destination = BlueprintItem.getDestination(stack);
        ItemStackRenderState item = new ItemStackRenderState();
        if (!destination.isEmpty() && !destination.is(Registration.BLUEPRINT.get())) {
            Minecraft mc = Minecraft.getInstance();
            mc.getItemModelResolver().updateForTopItem(item, destination, ItemDisplayContext.GUI, mc.level, mc.player, 0);
        }
        return new State(item, (System.currentTimeMillis() / 50) % 360);
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
        poses.pushPose();
        poses.translate(.5, .5, .65);
        poses.scale(.5f, .5f, .5f);
        poses.rotate(Axis.XP.rotationDegrees(30));
        poses.rotate(Axis.YP.rotationDegrees(state.rotation()));
        state.item().submit(poses, collector, light, overlay, outline);
        poses.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<BlueprintRenderer.State> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<State> bake(SpecialModelRenderer.BakingContext context) {
            return new BlueprintRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
