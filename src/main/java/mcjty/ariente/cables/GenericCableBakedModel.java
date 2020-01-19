package mcjty.ariente.cables;

import com.google.common.base.Function;
import mcjty.ariente.Ariente;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static mcjty.ariente.cables.CablePatterns.SpriteIdx.*;
import static mcjty.ariente.cables.ConnectorType.BLOCK;
import static mcjty.ariente.cables.ConnectorType.CABLE;

public class GenericCableBakedModel implements IDynamicBakedModel {

    public static final ModelResourceLocation modelConnector = new ModelResourceLocation(Ariente.MODID + ":" + ConnectorBlock.CONNECTOR);
    public static final ModelResourceLocation modelCable = new ModelResourceLocation(Ariente.MODID + ":" + NetCableBlock.NETCABLE);

    private TextureAtlasSprite spriteCable;
    private TextureAtlasSprite spriteConnector;

    public static class CableTextures {
        TextureAtlasSprite spriteConnector;

        TextureAtlasSprite spriteNoneCable;
        TextureAtlasSprite spriteNormalCable;
        TextureAtlasSprite spriteEndCable;
        TextureAtlasSprite spriteCornerCable;
        TextureAtlasSprite spriteThreeCable;
        TextureAtlasSprite spriteCrossCable;
    }

    private static CableTextures[] cableTextures = null;
    private static TextureAtlasSprite spriteSide;

    private VertexFormat format;

    static {
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, false, false, false), new CablePatterns.QuadSetting(SPRITE_NONE, 0));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, false, false, false), new CablePatterns.QuadSetting(SPRITE_END, 3));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, true, false, false), new CablePatterns.QuadSetting(SPRITE_END, 0));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, false, true, false), new CablePatterns.QuadSetting(SPRITE_END, 1));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, false, false, true), new CablePatterns.QuadSetting(SPRITE_END, 2));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, true, false, false), new CablePatterns.QuadSetting(SPRITE_CORNER, 0));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, true, true, false), new CablePatterns.QuadSetting(SPRITE_CORNER, 1));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, false, true, true), new CablePatterns.QuadSetting(SPRITE_CORNER, 2));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, false, false, true), new CablePatterns.QuadSetting(SPRITE_CORNER, 3));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, true, false, true), new CablePatterns.QuadSetting(SPRITE_STRAIGHT, 0));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, false, true, false), new CablePatterns.QuadSetting(SPRITE_STRAIGHT, 1));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, true, true, false), new CablePatterns.QuadSetting(SPRITE_THREE, 0));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(false, true, true, true), new CablePatterns.QuadSetting(SPRITE_THREE, 1));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, false, true, true), new CablePatterns.QuadSetting(SPRITE_THREE, 2));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, true, false, true), new CablePatterns.QuadSetting(SPRITE_THREE, 3));
        CablePatterns.PATTERNS.put(new CablePatterns.Pattern(true, true, true, true), new CablePatterns.QuadSetting(SPRITE_CROSS, 0));
    }

    private static void initTextures() {
        if (cableTextures == null) {
            CableTextures[] tt = new CableTextures[CableColor.VALUES.length];
            for (CableColor color : CableColor.VALUES) {
                int i = color.ordinal();
                String typeName = color.getName();
                tt[i] = new CableTextures();
                tt[i].spriteConnector = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/" + typeName + "/connector");
                tt[i].spriteNormalCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/" + typeName + "/normal_netcable");
                tt[i].spriteNoneCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/" + typeName + "/normal_none_netcable");
                tt[i].spriteEndCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/" + typeName + "/normal_end_netcable");
                tt[i].spriteCornerCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/" + typeName + "/normal_corner_netcable");
                tt[i].spriteThreeCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/" + typeName + "/normal_three_netcable");
                tt[i].spriteCrossCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/" + typeName + "/normal_cross_netcable");
            }

            spriteSide = Minecraft.getInstance().getTextureMap().getAtlasSprite(Ariente.MODID + ":blocks/cables/connector_side");
            cableTextures = tt;
        }
    }

    private static TextureAtlasSprite getSpriteNormal(CablePatterns.SpriteIdx idx, int index) {
        initTextures();
        CableTextures cableTexture = cableTextures[index];
        switch (idx) {
            case SPRITE_NONE:
                return cableTexture.spriteNoneCable;
            case SPRITE_END:
                return cableTexture.spriteEndCable;
            case SPRITE_STRAIGHT:
                return cableTexture.spriteNormalCable;
            case SPRITE_CORNER:
                return cableTexture.spriteCornerCable;
            case SPRITE_THREE:
                return cableTexture.spriteThreeCable;
            case SPRITE_CROSS:
                return cableTexture.spriteCrossCable;
        }
        return cableTexture.spriteNoneCable;
    }

    public GenericCableBakedModel(VertexFormat format) {
        this.format = format;
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float color) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, color, color, color, 1.0f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        u = sprite.getInterpolatedU(u);
                        v = sprite.getInterpolatedV(v);
                        builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                case NORMAL:
                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z, 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private void createQuad(List<BakedQuad> quads, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite, int rotation, float hilight) {
        switch (rotation) {
            case 0:
                createQuad(quads, v1, v2, v3, v4, sprite, hilight);
                return;
            case 1:
                createQuad(quads, v2, v3, v4, v1, sprite, hilight);
                return;
            case 2:
                createQuad(quads, v3, v4, v1, v2, sprite, hilight);
                return;
            case 3:
                createQuad(quads, v4, v1, v2, v3, sprite, hilight);
                return;
        }
        createQuad(quads, v1, v2, v3, v4, sprite, hilight);
    }

    private void createQuad(List<BakedQuad> quads, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite, float hilight) {
        createQuadI(quads, v1, v2, v3, v4, sprite, hilight);
        createQuadI(quads, v4, v3, v2, v1, sprite, hilight);
    }

    private void createQuadI(List<BakedQuad> quads, Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite, float hilight) {
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, hilight);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, hilight);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, hilight);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, hilight);
        quads.add(builder.build());
    }

    private static Vec3d v(double x, double y, double z) {
        return new Vec3d(x, y, z);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        if (state == null) {
            return Collections.emptyList();
        }

        BlockState facadeId = extraData.getData(GenericCableBlock.FACADEID);
        if (facadeId != null) {
            BlockState facadeState = facadeId.getBlockState();
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
                return Collections.emptyList();
            }
            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(facadeState);
            try {
                return model.getQuads(state, side, rand);
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }

        if (side != null || (MinecraftForgeClient.getRenderLayer() != BlockRenderLayer.CUTOUT_MIPPED)) {
            return Collections.emptyList();
        }

        // Called with the blockstate from our block. Here we get the values of the six properties and pass that to
        // our baked model implementation.
        ConnectorType north = state.get(GenericCableBlock.NORTH);
        ConnectorType south = state.get(GenericCableBlock.SOUTH);
        ConnectorType west = state.get(GenericCableBlock.WEST);
        ConnectorType east = state.get(GenericCableBlock.EAST);
        ConnectorType up = state.get(GenericCableBlock.UP);
        ConnectorType down = state.get(GenericCableBlock.DOWN);
        CableColor cableColor = state.get(GenericCableBlock.COLOR);
        int index = cableColor.ordinal();

        initTextures();
        CableTextures ct = cableTextures[index];
        spriteCable = ct.spriteNormalCable;
        GenericCableBlock block = (GenericCableBlock) state.getBlock();
        spriteConnector = ct.spriteConnector;
        Function<CablePatterns.SpriteIdx, TextureAtlasSprite> getSprite = idx -> getSpriteNormal(idx, index);
        float hilight = 1.0f;
        if (block instanceof ConnectorBlock) {
            if (north != BLOCK && south != BLOCK && west != BLOCK && east != BLOCK && up != BLOCK && down != BLOCK) {
                hilight = 0.5f; // To make connectors with no actual connections visible
            }
        }

        List<BakedQuad> quads = new ArrayList<>();

        double o = .4;      // Thickness of the cable. .0 would be full block, .5 is infinitely thin.
        double p = .1;      // Thickness of the connector as it is put on the connecting block
        double q = .2;      // The wideness of the connector

        // For each side we either cap it off if there is no similar block adjacent on that side
        // or else we extend so that we touch the adjacent block:

        if (up == CABLE) {
            createQuad(quads, v(1 - o, 1,     o),     v(1 - o, 1,     1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o),     spriteCable, hilight);
            createQuad(quads, v(o,     1,     1 - o), v(o,     1,     o),     v(o,     1 - o, o),     v(o,     1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o,     1,     o),     v(1 - o, 1,     o),     v(1 - o, 1 - o, o),     v(o,     1 - o, o), spriteCable, hilight);
            createQuad(quads, v(o,     1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1,     1 - o), v(o,     1,     1 - o), spriteCable, hilight);
        } else if (up == BLOCK) {
            createQuad(quads, v(1 - o, 1 - p,     o),     v(1 - o, 1 - p,     1 - o), v(1 - o, 1 - o, 1 - o),     v(1 - o, 1 - o, o),     spriteCable, hilight);
            createQuad(quads, v(o,     1 - p,     1 - o), v(o,     1 - p,     o),     v(o,     1 - o, o),         v(o,     1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o,     1 - p,     o),     v(1 - o, 1 - p,     o),     v(1 - o, 1 - o, o),         v(o,     1 - o, o), spriteCable, hilight);
            createQuad(quads, v(o,     1 - o, 1 - o),     v(1 - o, 1 - o, 1 - o),     v(1 - o, 1 - p,     1 - o), v(o,     1 - p,     1 - o), spriteCable, hilight);

            createQuad(quads, v(1 - q, 1 - p, q),     v(1 - q, 1,     q),     v(1 - q, 1,     1 - q), v(1 - q, 1 - p, 1 - q), spriteSide, hilight);
            createQuad(quads, v(q,     1 - p, 1 - q), v(q,     1,     1 - q), v(q,     1,     q),     v(q,     1 - p, q), spriteSide, hilight);
            createQuad(quads, v(q,     1,     q),     v(1 - q, 1,     q),     v(1 - q, 1 - p, q),     v(q,     1 - p, q), spriteSide, hilight);
            createQuad(quads, v(q,     1 - p, 1 - q), v(1 - q, 1 - p, 1 - q), v(1 - q, 1,     1 - q), v(q,     1,     1 - q), spriteSide, hilight);

            createQuad(quads, v(q,     1 - p, q),     v(1 - q, 1 - p, q),     v(1 - q, 1 - p, 1 - q), v(q,     1 - p, 1 - q), spriteConnector, hilight);
            createQuad(quads, v(q,     1, q),         v(q,     1, 1 - q),     v(1 - q, 1, 1 - q),     v(1 - q, 1, q), spriteSide, hilight);
        } else {
            CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, south, east, north);
            createQuad(quads, v(o,     1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o),     v(o,     1 - o, o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
        }

        if (down == CABLE) {
            createQuad(quads, v(1 - o, o, o),     v(1 - o, o, 1 - o), v(1 - o, 0, 1 - o), v(1 - o, 0, o),     spriteCable, hilight);
            createQuad(quads, v(o,     o, 1 - o), v(o,     o, o),     v(o,     0, o),     v(o,     0, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o,     o, o),     v(1 - o, o, o),     v(1 - o, 0, o),     v(o,     0, o), spriteCable, hilight);
            createQuad(quads, v(o,     0, 1 - o), v(1 - o, 0, 1 - o), v(1 - o, o, 1 - o), v(o,     o, 1 - o), spriteCable, hilight);
        } else if (down == BLOCK) {
            createQuad(quads, v(1 - o, o, o),     v(1 - o, o, 1 - o), v(1 - o, p, 1 - o), v(1 - o, p, o),     spriteCable, hilight);
            createQuad(quads, v(o,     o, 1 - o), v(o,     o, o),     v(o,     p, o),     v(o,     p, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o,     o, o),     v(1 - o, o, o),     v(1 - o, p, o),     v(o,     p, o), spriteCable, hilight);
            createQuad(quads, v(o,     p, 1 - o), v(1 - o, p, 1 - o), v(1 - o, o, 1 - o), v(o,     o, 1 - o), spriteCable, hilight);

            createQuad(quads, v(1 - q, 0, q),     v(1 - q, p, q),     v(1 - q, p, 1 - q), v(1 - q, 0, 1 - q), spriteSide, hilight);
            createQuad(quads, v(q,     0, 1 - q), v(q,     p, 1 - q), v(q,     p, q),     v(q,     0, q), spriteSide, hilight);
            createQuad(quads, v(q,     p, q),     v(1 - q, p, q),     v(1 - q, 0, q),     v(q,     0, q), spriteSide, hilight);
            createQuad(quads, v(q,     0, 1 - q), v(1 - q, 0, 1 - q), v(1 - q, p, 1 - q), v(q,     p, 1 - q), spriteSide, hilight);

            createQuad(quads, v(q,     p, 1 - q), v(1 - q, p, 1 - q), v(1 - q, p, q),     v(q,     p, q), spriteConnector, hilight);
            createQuad(quads, v(q,     0, 1 - q), v(q,     0, q),     v(1 - q, 0, q),     v(1 - q, 0, 1 - q), spriteSide, hilight);
        } else {
            CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, north, east, south);
            createQuad(quads, v(o, o, o), v(1 - o, o, o), v(1 - o, o, 1 - o), v(o, o, 1 - o), getSprite.apply(pattern.getSprite()),pattern.getRotation(), hilight);
        }

        if (east == CABLE) {
            createQuad(quads, v(1, 1 - o, 1 - o), v(1, 1 - o, o),     v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(1, o,     o),     v(1, o,     1 - o), v(1 - o, o,     1 - o), v(1 - o, o,     o),     spriteCable, hilight);
            createQuad(quads, v(1, 1 - o, o),     v(1, o,     o),     v(1 - o, o,     o), v(1 - o, 1 - o, o),     spriteCable, hilight);
            createQuad(quads, v(1, o,     1 - o), v(1, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, o,     1 - o), spriteCable, hilight);
        } else if (east == BLOCK) {
            createQuad(quads, v(1 - p, 1 - o, 1 - o), v(1 - p, 1 - o, o),     v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(1 - p, o,     o),     v(1 - p, o,     1 - o), v(1 - o, o,     1 - o), v(1 - o, o,     o),     spriteCable, hilight);
            createQuad(quads, v(1 - p, 1 - o, o),     v(1 - p, o,     o),     v(1 - o, o,     o), v(1 - o, 1 - o, o),     spriteCable, hilight);
            createQuad(quads, v(1 - p, o,     1 - o), v(1 - p, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, o,     1 - o), spriteCable, hilight);

            createQuad(quads, v(1 - p, 1 - q, 1 - q), v(1, 1 - q, 1 - q), v(1, 1 - q, q),     v(1 - p, 1 - q, q), spriteSide, hilight);
            createQuad(quads, v(1 - p, q,     q),     v(1, q,     q),     v(1, q,     1 - q), v(1 - p, q,     1 - q), spriteSide, hilight);
            createQuad(quads, v(1 - p, 1 - q, q),     v(1, 1 - q, q),     v(1, q,     q),     v(1 - p, q,     q), spriteSide, hilight);
            createQuad(quads, v(1 - p, q,     1 - q), v(1, q,     1 - q), v(1, 1 - q, 1 - q), v(1 - p, 1 - q, 1 - q), spriteSide, hilight);

            createQuad(quads, v(1 - p, q, 1 - q), v(1 - p, 1 - q, 1 - q), v(1 - p, 1 - q, q), v(1 - p, q, q), spriteConnector, hilight);
            createQuad(quads, v(1, q, 1 - q),     v(1, q, q),             v(1, 1 - q, q),     v(1, 1 - q, 1 - q), spriteSide, hilight);
        } else {
            CablePatterns.QuadSetting pattern = CablePatterns.findPattern(down, north, up, south);
            createQuad(quads, v(1 - o, o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o), v(1 - o, o, 1 - o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
        }

        if (west == CABLE) {
            createQuad(quads, v(o, 1 - o, 1 - o), v(o, 1 - o, o),     v(0, 1 - o, o), v(0, 1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o, o,     o),     v(o, o,     1 - o), v(0, o,     1 - o), v(0, o,     o),     spriteCable, hilight);
            createQuad(quads, v(o, 1 - o, o),     v(o, o,     o),     v(0, o,     o), v(0, 1 - o, o),     spriteCable, hilight);
            createQuad(quads, v(o, o,     1 - o), v(o, 1 - o, 1 - o), v(0, 1 - o, 1 - o), v(0, o,     1 - o), spriteCable, hilight);
        } else if (west == BLOCK) {
            createQuad(quads, v(o, 1 - o, 1 - o), v(o, 1 - o, o),     v(p, 1 - o, o), v(p, 1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o, o,     o),     v(o, o,     1 - o), v(p, o,     1 - o), v(p, o,     o),     spriteCable, hilight);
            createQuad(quads, v(o, 1 - o, o),     v(o, o,     o),     v(p, o,     o), v(p, 1 - o, o),     spriteCable, hilight);
            createQuad(quads, v(o, o,     1 - o), v(o, 1 - o, 1 - o), v(p, 1 - o, 1 - o), v(p, o,     1 - o), spriteCable, hilight);

            createQuad(quads, v(0, 1 - q, 1 - q), v(p, 1 - q, 1 - q), v(p, 1 - q, q),     v(0, 1 - q, q), spriteSide, hilight);
            createQuad(quads, v(0, q,     q),     v(p, q,     q),     v(p, q,     1 - q), v(0, q,     1 - q), spriteSide, hilight);
            createQuad(quads, v(0, 1 - q, q),     v(p, 1 - q, q),     v(p, q,     q),     v(0, q,     q), spriteSide, hilight);
            createQuad(quads, v(0, q,     1 - q), v(p, q,     1 - q), v(p, 1 - q, 1 - q), v(0, 1 - q, 1 - q), spriteSide, hilight);

            createQuad(quads, v(p, q, q), v(p, 1 - q, q), v(p, 1 - q, 1 - q), v(p, q, 1 - q), spriteConnector, hilight);
            createQuad(quads, v(0, q, q), v(0, q, 1 - q), v(0, 1 - q, 1 - q), v(0, 1 - q, q), spriteSide, hilight);
        } else {
            CablePatterns.QuadSetting pattern = CablePatterns.findPattern(down, south, up, north);
            createQuad(quads, v(o, o, 1 - o), v(o, 1 - o, 1 - o), v(o, 1 - o, o), v(o, o, o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
        }

        if (north == CABLE) {
            createQuad(quads, v(o,     1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 0), v(o,     1 - o, 0), spriteCable, hilight);
            createQuad(quads, v(o,     o,     0), v(1 - o, o,     0), v(1 - o, o,     o), v(o,     o,     o), spriteCable, hilight);
            createQuad(quads, v(1 - o, o,     0), v(1 - o, 1 - o, 0), v(1 - o, 1 - o, o), v(1 - o, o,     o), spriteCable, hilight);
            createQuad(quads, v(o,     o,     o), v(o,     1 - o, o), v(o,     1 - o, 0), v(o,     o,     0), spriteCable, hilight);
        } else if (north == BLOCK) {
            createQuad(quads, v(o,     1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, p), v(o,     1 - o, p), spriteCable, hilight);
            createQuad(quads, v(o,     o,     p), v(1 - o, o,     p), v(1 - o, o,     o), v(o,     o,     o), spriteCable, hilight);
            createQuad(quads, v(1 - o, o,     p), v(1 - o, 1 - o, p), v(1 - o, 1 - o, o), v(1 - o, o,     o), spriteCable, hilight);
            createQuad(quads, v(o,     o,     o), v(o,     1 - o, o), v(o,     1 - o, p), v(o,     o,     p), spriteCable, hilight);

            createQuad(quads, v(q,     1 - q, p), v(1 - q, 1 - q, p), v(1 - q, 1 - q, 0), v(q,     1 - q, 0), spriteSide, hilight);
            createQuad(quads, v(q,     q,     0), v(1 - q, q,     0), v(1 - q, q,     p), v(q,     q,     p), spriteSide, hilight);
            createQuad(quads, v(1 - q, q,     0), v(1 - q, 1 - q, 0), v(1 - q, 1 - q, p), v(1 - q, q,     p), spriteSide, hilight);
            createQuad(quads, v(q,     q,     p), v(q,     1 - q, p), v(q,     1 - q, 0), v(q,     q,     0), spriteSide, hilight);

            createQuad(quads, v(q, q, p), v(1 - q, q, p), v(1 - q, 1 - q, p), v(q, 1 - q, p), spriteConnector, hilight);
            createQuad(quads, v(q, q, 0), v(q, 1 - q, 0), v(1 - q, 1 - q, 0), v(1 - q, q, 0), spriteSide, hilight);
        } else {
            CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, up, east, down);
            createQuad(quads, v(o, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, o, o), v(o, o, o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
        }

        if (south == CABLE) {
            createQuad(quads, v(o,     1 - o, 1),     v(1 - o, 1 - o, 1),     v(1 - o, 1 - o, 1 - o), v(o,     1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o,     o,     1 - o), v(1 - o, o,     1 - o), v(1 - o, o,     1),     v(o,     o,     1), spriteCable, hilight);
            createQuad(quads, v(1 - o, o,     1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, 1),     v(1 - o, o,     1), spriteCable, hilight);
            createQuad(quads, v(o,     o,     1),     v(o,     1 - o, 1),     v(o,     1 - o, 1 - o), v(o,     o,     1 - o), spriteCable, hilight);
        } else if (south == BLOCK) {
            createQuad(quads, v(o,     1 - o, 1 - p), v(1 - o, 1 - o, 1 - p), v(1 - o, 1 - o, 1 - o), v(o,     1 - o, 1 - o), spriteCable, hilight);
            createQuad(quads, v(o,     o,     1 - o), v(1 - o, o,     1 - o), v(1 - o, o,     1 - p), v(o,     o,     1 - p), spriteCable, hilight);
            createQuad(quads, v(1 - o, o,     1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - p), v(1 - o, o,     1 - p), spriteCable, hilight);
            createQuad(quads, v(o,     o,     1 - p), v(o,     1 - o, 1 - p), v(o,     1 - o, 1 - o), v(o,     o,     1 - o), spriteCable, hilight);

            createQuad(quads, v(q,     1 - q, 1),     v(1 - q, 1 - q, 1),     v(1 - q, 1 - q, 1 - p), v(q,     1 - q, 1 - p), spriteSide, hilight);
            createQuad(quads, v(q,     q,     1 - p), v(1 - q, q,     1 - p), v(1 - q, q,     1),     v(q,     q,     1), spriteSide, hilight);
            createQuad(quads, v(1 - q, q,     1 - p), v(1 - q, 1 - q, 1 - p), v(1 - q, 1 - q, 1),     v(1 - q, q,     1), spriteSide, hilight);
            createQuad(quads, v(q,     q,     1),     v(q,     1 - q, 1),     v(q,     1 - q, 1 - p), v(q,     q,     1 - p), spriteSide, hilight);

            createQuad(quads, v(q, 1 - q, 1 - p), v(1 - q, 1 - q, 1 - p), v(1 - q, q, 1 - p), v(q, q, 1 - p), spriteConnector, hilight);
            createQuad(quads, v(q, 1 - q, 1),     v(q, q, 1),             v(1 - q, q, 1),     v(1 - q, 1 - q, 1), spriteSide, hilight);
        } else {
            CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, down, east, up);
            createQuad(quads, v(o, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, 1 - o, 1 - o), v(o, 1 - o, 1 - o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
        }



        return quads;
    }


    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return spriteCable == null ? Minecraft.getInstance().getTextureMap().getSprite(new ResourceLocation("minecraft", "missingno")) : spriteCable;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

}
