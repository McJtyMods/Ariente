package mcjty.ariente.facade;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class FacadeRenderer implements SpecialModelRenderer<BlockModelRenderState> {

    @Override
    public BlockModelRenderState extractArgument(ItemStack stack) {
        BlockModelRenderState state = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(state, FacadeItemBlock.getMimicBlock(stack), BlockDisplayContext.create());
        return state;
    }

    @Override
    public void submit(BlockModelRenderState state, PoseStack poses, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
        state.submitMultiLayer(poses, collector, light, overlay, outline);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<BlockModelRenderState> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<BlockModelRenderState> bake(SpecialModelRenderer.BakingContext context) {
            return new FacadeRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
