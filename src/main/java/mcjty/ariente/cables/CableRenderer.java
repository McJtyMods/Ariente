package mcjty.ariente.cables;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class CableRenderer implements BlockEntityRenderer<GenericCableTileEntity> {

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
        public final Vector3f player;
        public final Matrix4f matrix;
        public final VertexConsumer builder;
        public final TextureAtlasSprite sprite;

        public RenderInfo(Vector3f player, Matrix4f matrix, VertexConsumer builder, TextureAtlasSprite sprite) {
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
                new Vector3f(sx, sy, sz),
                new Vector3f(dx, dy, dz),
                info.player, .1f);
    }

    protected BlockEntityRendererProvider.Context context;

    public CableRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(GenericCableTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (true) { // @todo only when it has power
            BlockState state = te.getLevel().getBlockState(te.getBlockPos());
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

                int tex = te.getBlockPos().getX();
                int tey = te.getBlockPos().getY();
                int tez = te.getBlockPos().getZ();
                Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().add(-tex, -tey, -tez);
                Vector3f player = new Vector3f((float)projectedView.x, (float)projectedView.y, (float)projectedView.z);

                Matrix4f matrix = matrixStack.last().pose();
                TextureAtlasSprite sprite = ArienteSpriteUploader.INSTANCE.getSprite(txt);
                VertexConsumer builder = buffer.getBuffer(ArienteRenderType.ARIENTE_TRANSLUCENT);

                int mask_ud = ((GenericCableBlock) block).getUpDownMask(state, te.getLevel(), te.getBlockPos());
                int mask_ew = ((GenericCableBlock) block).getEastWestMask(state, te.getLevel(), te.getBlockPos());
                int mask_ns = ((GenericCableBlock) block).getNorthSouthMask(state, te.getLevel(), te.getBlockPos());

                RenderInfo info = new RenderInfo(player, matrix, builder, sprite);
                BEAM_RENDERERS_UP_DOWN.get(mask_ud).accept(info);
                BEAM_RENDERERS_NORTH_SOUTH.get(mask_ns).accept(info);
                BEAM_RENDERERS_EAST_WEST.get(mask_ew).accept(info);
            }
        }
    }

    public static void register(BlockEntityType<? extends GenericCableTileEntity> type) {
        BlockEntityRenderers.register(type, CableRenderer::new);
    }

}
