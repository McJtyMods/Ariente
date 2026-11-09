package mcjty.ariente.blocks.utility;

import mcjty.lib.client.AbstractDynamicBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.BeamRenderTools;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import mcjty.ariente.client.ExtractedBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ElevatorRenderer extends ExtractedBlockRenderer<ElevatorTile> {

    protected BlockEntityRendererProvider.Context context;
    public static final Identifier ELEVATOR_BEAM = Identifier.fromNamespaceAndPath(Ariente.MODID, "block/machines/elevator_beam");

    public ElevatorRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    private static float randomX[] = new float[]{.2f, .3f, .2f, .7f, .8f, .5f, .2f, .8f, .4f, .6f};
    private static float randomZ[] = new float[]{.3f, .2f, .8f, .3f, .7f, .6f, .4f, .5f, .2f, .3f};
    private static int randomY[] = new int[]{0, 3, 2, 1, 6, 5, 6, 8, 2, 3};

    public AABB getRenderBoundingBox(ElevatorTile tile) {
        return tile.getRenderBoundingBox();
    }

    @Override
    public void render(ElevatorTile te, float partialTicks, PoseStack matrixStack, RenderBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        TextureAtlasSprite sprite = AbstractDynamicBakedModel.getTexture(ELEVATOR_BEAM);

        int tex = te.getBlockPos().getX();
        int tey = te.getBlockPos().getY();
        int tez = te.getBlockPos().getZ();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.mainCamera().position().add(-tex, -tey, -tez);
        Vec3 player = new Vec3((float)projectedView.x, (float)projectedView.y, (float)projectedView.z);

        long tt = System.currentTimeMillis() / 100;

        VertexConsumer builder = buffer.getBuffer(ArienteRenderType.BEAM);

        float height = te.getHeight();

        for (int i = 0; i < 5 + te.getHeight() / 2; i++) {
            int ii = i % 10;
            long ticks = (tt + randomY[ii]) % 80;
            if (ticks > 40) {
                ticks = 80 - ticks;
            }
            float i1 = ticks / 40.0f;
            float xx = randomX[ii];
            float zz = randomZ[ii];
            float yy = - 1.0f + i1 + (randomY[ii] * height) / 8.0f;
            BeamRenderTools.drawBeam(matrixStack, builder, sprite, new Vec3(xx, yy, zz), new Vec3(xx, yy + 4, zz), player, 0.2f);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.ELEVATOR_TILE.get(), ElevatorRenderer::new);
    }
}
