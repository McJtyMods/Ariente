package mcjty.ariente.cables;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.lib.client.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class CableRenderer extends TileEntityRenderer<GenericCableTileEntity> {

    private Random random = new Random();
    private static ResourceLocation negarite_laserbeams[] = new ResourceLocation[4];
    private static ResourceLocation posirite_laserbeams[] = new ResourceLocation[4];
    private static ResourceLocation data_laserbeams[] = new ResourceLocation[4];

    public static final ResourceLocation NEGARITE_LASERBEAM_1 = new ResourceLocation(Ariente.MODID, "negarite_laserbeam1");
    public static final ResourceLocation NEGARITE_LASERBEAM_2 = new ResourceLocation(Ariente.MODID, "negarite_laserbeam2");
    public static final ResourceLocation NEGARITE_LASERBEAM_3 = new ResourceLocation(Ariente.MODID, "negarite_laserbeam3");
    public static final ResourceLocation NEGARITE_LASERBEAM_4 = new ResourceLocation(Ariente.MODID, "negarite_laserbeam4");
    public static final ResourceLocation POSIRITE_LASERBEAM_1 = new ResourceLocation(Ariente.MODID, "posirite_laserbeam1");
    public static final ResourceLocation POSIRITE_LASERBEAM_2 = new ResourceLocation(Ariente.MODID, "posirite_laserbeam2");
    public static final ResourceLocation POSIRITE_LASERBEAM_3 = new ResourceLocation(Ariente.MODID, "posirite_laserbeam3");
    public static final ResourceLocation POSIRITE_LASERBEAM_4 = new ResourceLocation(Ariente.MODID, "posirite_laserbeam4");
    public static final ResourceLocation DATA_LASERBEAM_1 = new ResourceLocation(Ariente.MODID, "data_laserbeam1");
    public static final ResourceLocation DATA_LASERBEAM_2 = new ResourceLocation(Ariente.MODID, "data_laserbeam2");
    public static final ResourceLocation DATA_LASERBEAM_3 = new ResourceLocation(Ariente.MODID, "data_laserbeam3");
    public static final ResourceLocation DATA_LASERBEAM_4 = new ResourceLocation(Ariente.MODID, "data_laserbeam4");

    static {
        negarite_laserbeams[0] = NEGARITE_LASERBEAM_1;
        negarite_laserbeams[1] = NEGARITE_LASERBEAM_2;
        negarite_laserbeams[2] = NEGARITE_LASERBEAM_3;
        negarite_laserbeams[3] = NEGARITE_LASERBEAM_4;
        posirite_laserbeams[0] = POSIRITE_LASERBEAM_1;
        posirite_laserbeams[1] = POSIRITE_LASERBEAM_2;
        posirite_laserbeams[2] = POSIRITE_LASERBEAM_3;
        posirite_laserbeams[3] = POSIRITE_LASERBEAM_4;
        data_laserbeams[0] = DATA_LASERBEAM_1;
        data_laserbeams[1] = DATA_LASERBEAM_2;
        data_laserbeams[2] = DATA_LASERBEAM_3;
        data_laserbeams[3] = DATA_LASERBEAM_4;
    }


    private static class RenderInfo {
        public final RenderHelper.Vector player;
        public final Matrix4f matrix;
        public final IVertexBuilder builder;
        public final TextureAtlasSprite sprite;

        public RenderInfo(RenderHelper.Vector player, Matrix4f matrix, IVertexBuilder builder, TextureAtlasSprite sprite) {
            this.player = player;
            this.matrix = matrix;
            this.builder = builder;
            this.sprite = sprite;
        }
    }

    public static final List<Consumer<RenderInfo>> BEAM_RENDERERS_UP_DOWN = new ArrayList<>(4);
    public static final List<Consumer<RenderInfo>> BEAM_RENDERERS_EAST_WEST = new ArrayList<>(4);
    public static final List<Consumer<RenderInfo>> BEAM_RENDERERS_NORTH_SOUTH = new ArrayList<>(4);

    static {
        BEAM_RENDERERS_UP_DOWN.add(info -> {});
        BEAM_RENDERERS_UP_DOWN.add(info -> beam(info, .5f, .5f, .5f, .5f, 0f ,.5f));
        BEAM_RENDERERS_UP_DOWN.add(info -> beam(info, .5f, .5f, .5f, .5f, 1f ,.5f));
        BEAM_RENDERERS_UP_DOWN.add(info -> beam(info, .5f, 0f, .5f, .5f, 1f ,.5f));

        BEAM_RENDERERS_EAST_WEST.add(info -> {});
        BEAM_RENDERERS_EAST_WEST.add(info -> beam(info, .5f, .5f, .5f, 1f, .5f, .5f));
        BEAM_RENDERERS_EAST_WEST.add(info -> beam(info, .5f, .5f, .5f, 0f, .5f, .5f));
        BEAM_RENDERERS_EAST_WEST.add(info -> beam(info, 0f, .5f, .5f, 1f, .5f, .5f));

        BEAM_RENDERERS_NORTH_SOUTH.add(info -> {});
        BEAM_RENDERERS_NORTH_SOUTH.add(info -> beam(info, .5f, .5f, .5f, .5f, .5f, 1f));
        BEAM_RENDERERS_NORTH_SOUTH.add(info -> beam(info, .5f, .5f, .5f, .5f, .5f, 0f));
        BEAM_RENDERERS_NORTH_SOUTH.add(info -> beam(info, .5f, .5f, 0f, .5f, .5f, 1f));
    }

    private static void beam(RenderInfo info, float sx, float sy, float sz, float dx, float dy, float dz) {
        RenderHelper.drawBeam(info.matrix, info.builder, info.sprite,
                new RenderHelper.Vector(sx, sy, sz),
                new RenderHelper.Vector(dx, dy, dz),
                info.player, .1f);
    }

    public CableRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(GenericCableTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (true) { // @todo only when it has power
            BlockState state = te.getWorld().getBlockState(te.getPos());
            Block block = state.getBlock();
            if (block instanceof GenericCableBlock) {
                ResourceLocation txt;
                switch (te.getCableColor()) {
                    case NEGARITE:
                        txt = negarite_laserbeams[random.nextInt(4)];
                        break;
                    case POSIRITE:
                        txt = posirite_laserbeams[random.nextInt(4)];
                        break;
                    case COMBINED:
                        return;
                    case DATA:
                        txt = data_laserbeams[random.nextInt(4)];
                        break;
                    default:
                        return;
                }

                Minecraft mc = Minecraft.getInstance();
                PlayerEntity p = mc.player;
                double doubleX = p.lastTickPosX + (p.getPosX() - p.lastTickPosX) * partialTicks;
                double doubleY = p.lastTickPosY + (p.getPosY() - p.lastTickPosY) * partialTicks;
                double doubleZ = p.lastTickPosZ + (p.getPosZ() - p.lastTickPosZ) * partialTicks;

                RenderHelper.Vector player = new RenderHelper.Vector((float) doubleX - te.getPos().getX(), (float) doubleY + p.getEyeHeight() - te.getPos().getY(), (float) doubleZ - te.getPos().getZ());

                Matrix4f matrix = matrixStack.getLast().getMatrix();
                TextureAtlasSprite sprite = ArienteSpriteUploader.INSTANCE.getSprite(txt);
                IVertexBuilder builder = buffer.getBuffer(ArienteRenderType.ARIENTE_TRANSLUCENT);

                int mask_ud = ((GenericCableBlock) block).getUpDownMask(state, te.getWorld(), te.getPos());
                int mask_ew = ((GenericCableBlock) block).getEastWestMask(state, te.getWorld(), te.getPos());
                int mask_ns = ((GenericCableBlock) block).getNorthSouthMask(state, te.getWorld(), te.getPos());

                RenderInfo info = new RenderInfo(player, matrix, builder, sprite);
                BEAM_RENDERERS_UP_DOWN.get(mask_ud).accept(info);
                BEAM_RENDERERS_NORTH_SOUTH.get(mask_ns).accept(info);
                BEAM_RENDERERS_EAST_WEST.get(mask_ew).accept(info);
            }
        }
    }

    public static void register(TileEntityType<? extends GenericCableTileEntity> type) {
        ClientRegistry.bindTileEntityRenderer(type, CableRenderer::new);
    }

}
