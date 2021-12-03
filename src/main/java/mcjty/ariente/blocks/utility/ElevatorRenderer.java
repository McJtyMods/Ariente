package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.Random;

public class ElevatorRenderer extends TileEntityRenderer<ElevatorTile> {

    public static final ResourceLocation ELEVATOR_BEAM = new ResourceLocation(Ariente.MODID, "block/machines/elevator_beam");
    private Random random = new Random();

    public ElevatorRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    private static float randomX[] = new float[]{.2f, .3f, .2f, .7f, .8f, .5f, .2f, .8f, .4f, .6f};
    private static float randomZ[] = new float[]{.3f, .2f, .8f, .3f, .7f, .6f, .4f, .5f, .2f, .3f};
    private static int randomY[] = new int[]{0, 3, 2, 1, 6, 5, 6, 8, 2, 3};

    @Override
    public void render(ElevatorTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(ELEVATOR_BEAM);

        int tex = te.getBlockPos().getX();
        int tey = te.getBlockPos().getY();
        int tez = te.getBlockPos().getZ();
        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().add(-tex, -tey, -tez);
        Vector3f player = new Vector3f((float)projectedView.x, (float)projectedView.y, (float)projectedView.z);

        long tt = System.currentTimeMillis() / 100;

        IVertexBuilder builder = buffer.getBuffer(CustomRenderTypes.TRANSLUCENT_ADD);

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
            RenderHelper.drawBeam(matrixStack.last().pose(), builder, sprite, new Vector3f(xx, yy, zz), new Vector3f(xx, yy + 4, zz), player, 0.2f);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ElevatorTile te) {
        return true;
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.ELEVATOR_TILE.get(), ElevatorRenderer::new);
    }
}
