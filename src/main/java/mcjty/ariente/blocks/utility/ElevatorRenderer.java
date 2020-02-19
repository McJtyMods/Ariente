package mcjty.ariente.blocks.utility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.Random;

public class ElevatorRenderer extends TileEntityRenderer<ElevatorTile> {

    public static final ResourceLocation ELEVATOR_BEAM = new ResourceLocation(Ariente.MODID, "blocks/machines/elevator_beam");
    private Random random = new Random();

    public ElevatorRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    private static float randomX[] = new float[]{.2f, .3f, .2f, .7f, .8f, .5f, .2f, .8f, .4f, .6f};
    private static float randomZ[] = new float[]{.3f, .2f, .8f, .3f, .7f, .6f, .4f, .5f, .2f, .3f};
    private static int randomY[] = new int[]{0, 3, 2, 1, 6, 5, 6, 8, 2, 3};

    @Override
    public void render(ElevatorTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.push();

        // @tod o1.15
//        GlStateManager.enableBlend();
//        GlStateManager.depthMask(false);
//        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//        GlStateManager.disableCull();
//        GlStateManager.enableDepthTest();

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(ELEVATOR_BEAM);


//
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        double doubleX = p.lastTickPosX + (p.getPosX() - p.lastTickPosX) * partialTicks;
        double doubleY = p.lastTickPosY + (p.getPosY() - p.lastTickPosY) * partialTicks;
        double doubleZ = p.lastTickPosZ + (p.getPosZ() - p.lastTickPosZ) * partialTicks;
//
//        GlStateManager.translated(-doubleX, -doubleY, -doubleZ);
//
        RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

        long tt = System.currentTimeMillis() / 100;

        IVertexBuilder builder = buffer.getBuffer(RenderType.translucent());

        float height = te.getHeight();

        for (int i = 0; i < 5 + te.getHeight() / 2; i++) {
            int ii = i % 10;
            long ticks = (tt + randomY[ii]) % 80;
            if (ticks > 40) {
                ticks = 80 - ticks;
            }
            float i1 = ticks / 40.0f;
            float xx = te.getPos().getX() + randomX[ii];
            float zz = te.getPos().getZ() + randomZ[ii];
            float yy = te.getPos().getY() - 1.0f + i1 + (randomY[ii] * height) / 8.0f;
            RenderHelper.drawBeam(matrixStack.getLast().getPositionMatrix(), builder, sprite, new RenderHelper.Vector(xx, yy, zz), new RenderHelper.Vector(xx, yy + 4, zz), player, 0.2f);
        }

//        net.minecraft.util.math.Vec3d cameraPos = net.minecraft.client.renderer.ActiveRenderInfo.getCameraPosition();
//        tessellator.getBuffer().sortVertexData((float) (player.x + doubleX), (float) (player.y + doubleY), (float) (player.z + doubleZ));
//        tessellator.getBuffer().sortVertexData((float)(cameraPos.x+doubleX), (float)(cameraPos.y+doubleY), (float)(cameraPos.z+doubleZ));

        // @todo 1.15
//        GlStateManager.depthMask(true);
//        GlStateManager.enableLighting();
//        GlStateManager.enableDepthTest();
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        matrixStack.pop();
    }

    @Override
    public boolean isGlobalRenderer(ElevatorTile te) {
        return true;
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.ELEVATOR_TILE.get(), ElevatorRenderer::new);
    }
}
