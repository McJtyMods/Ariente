package mcjty.ariente.cables;

import mcjty.ariente.client.SnapshotModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.List;
import mcjty.ariente.Ariente;
import mcjty.lib.client.AbstractDynamicBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelProperty;

import static mcjty.ariente.cables.CablePatterns.SpriteIdx.*;
import static mcjty.ariente.cables.ConnectorType.BLOCK;
import static mcjty.ariente.cables.ConnectorType.CABLE;

public class GenericCableBakedModel extends AbstractDynamicBakedModel {

    private static final ModelProperty<BlockState> CONNECTIONS = new ModelProperty<>();

    public static class CableTextures {
        private TextureAtlasSprite spriteConnector;
        private TextureAtlasSprite spriteNoneCable;
        private TextureAtlasSprite spriteNormalCable;
        private TextureAtlasSprite spriteEndCable;
        private TextureAtlasSprite spriteCornerCable;
        private TextureAtlasSprite spriteThreeCable;
        private TextureAtlasSprite spriteCrossCable;
    }

    private CableTextures[] cableTextures = null;
    private TextureAtlasSprite spriteSide;

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

    private void initTextures() {
        if (cableTextures == null) {
            CableTextures[] tt = new CableTextures[CableColor.VALUES.length];
            for (CableColor color : CableColor.VALUES) {
                int i = color.ordinal();
                String typeName = color.getSerializedName();
                tt[i] = new CableTextures();
                tt[i].spriteConnector = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/" + typeName + "/connector"));
                tt[i].spriteNormalCable = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/" + typeName + "/normal_netcable"));
                tt[i].spriteNoneCable = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/" + typeName + "/normal_none_netcable"));
                tt[i].spriteEndCable = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/" + typeName + "/normal_end_netcable"));
                tt[i].spriteCornerCable = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/" + typeName + "/normal_corner_netcable"));
                tt[i].spriteThreeCable = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/" + typeName + "/normal_three_netcable"));
                tt[i].spriteCrossCable = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/" + typeName + "/normal_cross_netcable"));
            }

            spriteSide = getTexture(Identifier.fromNamespaceAndPath(Ariente.MODID, "block/cables/connector_side"));
            cableTextures = tt;
        }
    }

    private TextureAtlasSprite getSpriteNormal(CablePatterns.SpriteIdx idx, int index) {
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

    private void createQuad(List<BakedQuad> quads, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, TextureAtlasSprite sprite, int rotation, float hilight) {
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

    private void createQuad(List<BakedQuad> quads, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, TextureAtlasSprite sprite, float hilight) {
        quads.add(super.createQuad(v1, v2, v3, v4, sprite, hilight));
        quads.add(super.createQuad(v4, v3, v2, v1, sprite, hilight));
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        if (!(state.getBlock() instanceof GenericCableBlock cable)) return;
        BlockState facade = level.getModelData(pos).get(GenericCableBlock.FACADEID);
        if (facade != null) {
            Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(facade).collectParts(level, pos, facade, random, parts);
            return;
        }
        BlockState connections = cable.calculateState(level, pos, state);
        parts.add(SnapshotModelPart.unculled(geometry(connections), particleMaterial()));
    }

    @Override
    public Material.Baked particleMaterial() {
        initTextures();
        return new Material.Baked(cableTextures[CableColor.NEGARITE.ordinal()].spriteNormalCable, false);
    }

    private List<BakedQuad> geometry(BlockState state) {
        List<BakedQuad> quads = new ArrayList<>();

        {

            // Called with the blockstate from our block. Here we get the values of the six properties and pass that to
            // our baked model implementation.
            ConnectorType north = state.getValue(GenericCableBlock.NORTH);
            ConnectorType south = state.getValue(GenericCableBlock.SOUTH);
            ConnectorType west = state.getValue(GenericCableBlock.WEST);
            ConnectorType east = state.getValue(GenericCableBlock.EAST);
            ConnectorType up = state.getValue(GenericCableBlock.UP);
            ConnectorType down = state.getValue(GenericCableBlock.DOWN);
            CableColor cableColor = state.getValue(GenericCableBlock.COLOR);
            int index = cableColor.ordinal();

            initTextures();
            CableTextures ct = cableTextures[index];
            TextureAtlasSprite spriteCable = ct.spriteNormalCable;
            GenericCableBlock block = (GenericCableBlock) state.getBlock();
            TextureAtlasSprite spriteConnector = ct.spriteConnector;
            Function<CablePatterns.SpriteIdx, TextureAtlasSprite> getSprite = idx -> getSpriteNormal(idx, index);
            float hilight = 1.0f;
            if (block instanceof ConnectorBlock) {
                if (north != BLOCK && south != BLOCK && west != BLOCK && east != BLOCK && up != BLOCK && down != BLOCK) {
                    hilight = 0.5f; // To make connectors with no actual connections visible
                }
            }

            double o = .4;      // Thickness of the cable. .0 would be full block, .5 is infinitely thin.
            double p = .1;      // Thickness of the connector as it is put on the connecting block
            double q = .2;      // The wideness of the connector

            // For each side we either cap it off if there is no similar block adjacent on that side
            // or else we extend so that we touch the adjacent block:

            if (up == CABLE) {
                createQuad(quads, v(1 - o, 1, o), v(1 - o, 1, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(o, 1, 1 - o), v(o, 1, o), v(o, 1 - o, o), v(o, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, 1, o), v(1 - o, 1, o), v(1 - o, 1 - o, o), v(o, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1, 1 - o), v(o, 1, 1 - o), spriteCable, hilight);
            } else if (up == BLOCK) {
                createQuad(quads, v(1 - o, 1 - p, o), v(1 - o, 1 - p, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(o, 1 - p, 1 - o), v(o, 1 - p, o), v(o, 1 - o, o), v(o, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, 1 - p, o), v(1 - o, 1 - p, o), v(1 - o, 1 - o, o), v(o, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - p, 1 - o), v(o, 1 - p, 1 - o), spriteCable, hilight);

                createQuad(quads, v(1 - q, 1 - p, q), v(1 - q, 1, q), v(1 - q, 1, 1 - q), v(1 - q, 1 - p, 1 - q), spriteSide, hilight);
                createQuad(quads, v(q, 1 - p, 1 - q), v(q, 1, 1 - q), v(q, 1, q), v(q, 1 - p, q), spriteSide, hilight);
                createQuad(quads, v(q, 1, q), v(1 - q, 1, q), v(1 - q, 1 - p, q), v(q, 1 - p, q), spriteSide, hilight);
                createQuad(quads, v(q, 1 - p, 1 - q), v(1 - q, 1 - p, 1 - q), v(1 - q, 1, 1 - q), v(q, 1, 1 - q), spriteSide, hilight);

                createQuad(quads, v(q, 1 - p, q), v(1 - q, 1 - p, q), v(1 - q, 1 - p, 1 - q), v(q, 1 - p, 1 - q), spriteConnector, hilight);
                createQuad(quads, v(q, 1, q), v(q, 1, 1 - q), v(1 - q, 1, 1 - q), v(1 - q, 1, q), spriteSide, hilight);
            } else {
                CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, south, east, north);
                createQuad(quads, v(o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o), v(o, 1 - o, o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
            }

            if (down == CABLE) {
                createQuad(quads, v(1 - o, o, o), v(1 - o, o, 1 - o), v(1 - o, 0, 1 - o), v(1 - o, 0, o), spriteCable, hilight);
                createQuad(quads, v(o, o, 1 - o), v(o, o, o), v(o, 0, o), v(o, 0, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, o, o), v(1 - o, o, o), v(1 - o, 0, o), v(o, 0, o), spriteCable, hilight);
                createQuad(quads, v(o, 0, 1 - o), v(1 - o, 0, 1 - o), v(1 - o, o, 1 - o), v(o, o, 1 - o), spriteCable, hilight);
            } else if (down == BLOCK) {
                createQuad(quads, v(1 - o, o, o), v(1 - o, o, 1 - o), v(1 - o, p, 1 - o), v(1 - o, p, o), spriteCable, hilight);
                createQuad(quads, v(o, o, 1 - o), v(o, o, o), v(o, p, o), v(o, p, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, o, o), v(1 - o, o, o), v(1 - o, p, o), v(o, p, o), spriteCable, hilight);
                createQuad(quads, v(o, p, 1 - o), v(1 - o, p, 1 - o), v(1 - o, o, 1 - o), v(o, o, 1 - o), spriteCable, hilight);

                createQuad(quads, v(1 - q, 0, q), v(1 - q, p, q), v(1 - q, p, 1 - q), v(1 - q, 0, 1 - q), spriteSide, hilight);
                createQuad(quads, v(q, 0, 1 - q), v(q, p, 1 - q), v(q, p, q), v(q, 0, q), spriteSide, hilight);
                createQuad(quads, v(q, p, q), v(1 - q, p, q), v(1 - q, 0, q), v(q, 0, q), spriteSide, hilight);
                createQuad(quads, v(q, 0, 1 - q), v(1 - q, 0, 1 - q), v(1 - q, p, 1 - q), v(q, p, 1 - q), spriteSide, hilight);

                createQuad(quads, v(q, p, 1 - q), v(1 - q, p, 1 - q), v(1 - q, p, q), v(q, p, q), spriteConnector, hilight);
                createQuad(quads, v(q, 0, 1 - q), v(q, 0, q), v(1 - q, 0, q), v(1 - q, 0, 1 - q), spriteSide, hilight);
            } else {
                CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, north, east, south);
                createQuad(quads, v(o, o, o), v(1 - o, o, o), v(1 - o, o, 1 - o), v(o, o, 1 - o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
            }

            if (east == CABLE) {
                createQuad(quads, v(1, 1 - o, 1 - o), v(1, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(1, o, o), v(1, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, o), spriteCable, hilight);
                createQuad(quads, v(1, 1 - o, o), v(1, o, o), v(1 - o, o, o), v(1 - o, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(1, o, 1 - o), v(1, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, o, 1 - o), spriteCable, hilight);
            } else if (east == BLOCK) {
                createQuad(quads, v(1 - p, 1 - o, 1 - o), v(1 - p, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(1 - p, o, o), v(1 - p, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, o), spriteCable, hilight);
                createQuad(quads, v(1 - p, 1 - o, o), v(1 - p, o, o), v(1 - o, o, o), v(1 - o, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(1 - p, o, 1 - o), v(1 - p, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, o, 1 - o), spriteCable, hilight);

                createQuad(quads, v(1 - p, 1 - q, 1 - q), v(1, 1 - q, 1 - q), v(1, 1 - q, q), v(1 - p, 1 - q, q), spriteSide, hilight);
                createQuad(quads, v(1 - p, q, q), v(1, q, q), v(1, q, 1 - q), v(1 - p, q, 1 - q), spriteSide, hilight);
                createQuad(quads, v(1 - p, 1 - q, q), v(1, 1 - q, q), v(1, q, q), v(1 - p, q, q), spriteSide, hilight);
                createQuad(quads, v(1 - p, q, 1 - q), v(1, q, 1 - q), v(1, 1 - q, 1 - q), v(1 - p, 1 - q, 1 - q), spriteSide, hilight);

                createQuad(quads, v(1 - p, q, 1 - q), v(1 - p, 1 - q, 1 - q), v(1 - p, 1 - q, q), v(1 - p, q, q), spriteConnector, hilight);
                createQuad(quads, v(1, q, 1 - q), v(1, q, q), v(1, 1 - q, q), v(1, 1 - q, 1 - q), spriteSide, hilight);
            } else {
                CablePatterns.QuadSetting pattern = CablePatterns.findPattern(down, north, up, south);
                createQuad(quads, v(1 - o, o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o), v(1 - o, o, 1 - o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
            }

            if (west == CABLE) {
                createQuad(quads, v(o, 1 - o, 1 - o), v(o, 1 - o, o), v(0, 1 - o, o), v(0, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, o, o), v(o, o, 1 - o), v(0, o, 1 - o), v(0, o, o), spriteCable, hilight);
                createQuad(quads, v(o, 1 - o, o), v(o, o, o), v(0, o, o), v(0, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(o, o, 1 - o), v(o, 1 - o, 1 - o), v(0, 1 - o, 1 - o), v(0, o, 1 - o), spriteCable, hilight);
            } else if (west == BLOCK) {
                createQuad(quads, v(o, 1 - o, 1 - o), v(o, 1 - o, o), v(p, 1 - o, o), v(p, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, o, o), v(o, o, 1 - o), v(p, o, 1 - o), v(p, o, o), spriteCable, hilight);
                createQuad(quads, v(o, 1 - o, o), v(o, o, o), v(p, o, o), v(p, 1 - o, o), spriteCable, hilight);
                createQuad(quads, v(o, o, 1 - o), v(o, 1 - o, 1 - o), v(p, 1 - o, 1 - o), v(p, o, 1 - o), spriteCable, hilight);

                createQuad(quads, v(0, 1 - q, 1 - q), v(p, 1 - q, 1 - q), v(p, 1 - q, q), v(0, 1 - q, q), spriteSide, hilight);
                createQuad(quads, v(0, q, q), v(p, q, q), v(p, q, 1 - q), v(0, q, 1 - q), spriteSide, hilight);
                createQuad(quads, v(0, 1 - q, q), v(p, 1 - q, q), v(p, q, q), v(0, q, q), spriteSide, hilight);
                createQuad(quads, v(0, q, 1 - q), v(p, q, 1 - q), v(p, 1 - q, 1 - q), v(0, 1 - q, 1 - q), spriteSide, hilight);

                createQuad(quads, v(p, q, q), v(p, 1 - q, q), v(p, 1 - q, 1 - q), v(p, q, 1 - q), spriteConnector, hilight);
                createQuad(quads, v(0, q, q), v(0, q, 1 - q), v(0, 1 - q, 1 - q), v(0, 1 - q, q), spriteSide, hilight);
            } else {
                CablePatterns.QuadSetting pattern = CablePatterns.findPattern(down, south, up, north);
                createQuad(quads, v(o, o, 1 - o), v(o, 1 - o, 1 - o), v(o, 1 - o, o), v(o, o, o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
            }

            if (north == CABLE) {
                createQuad(quads, v(o, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 0), v(o, 1 - o, 0), spriteCable, hilight);
                createQuad(quads, v(o, o, 0), v(1 - o, o, 0), v(1 - o, o, o), v(o, o, o), spriteCable, hilight);
                createQuad(quads, v(1 - o, o, 0), v(1 - o, 1 - o, 0), v(1 - o, 1 - o, o), v(1 - o, o, o), spriteCable, hilight);
                createQuad(quads, v(o, o, o), v(o, 1 - o, o), v(o, 1 - o, 0), v(o, o, 0), spriteCable, hilight);
            } else if (north == BLOCK) {
                createQuad(quads, v(o, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, p), v(o, 1 - o, p), spriteCable, hilight);
                createQuad(quads, v(o, o, p), v(1 - o, o, p), v(1 - o, o, o), v(o, o, o), spriteCable, hilight);
                createQuad(quads, v(1 - o, o, p), v(1 - o, 1 - o, p), v(1 - o, 1 - o, o), v(1 - o, o, o), spriteCable, hilight);
                createQuad(quads, v(o, o, o), v(o, 1 - o, o), v(o, 1 - o, p), v(o, o, p), spriteCable, hilight);

                createQuad(quads, v(q, 1 - q, p), v(1 - q, 1 - q, p), v(1 - q, 1 - q, 0), v(q, 1 - q, 0), spriteSide, hilight);
                createQuad(quads, v(q, q, 0), v(1 - q, q, 0), v(1 - q, q, p), v(q, q, p), spriteSide, hilight);
                createQuad(quads, v(1 - q, q, 0), v(1 - q, 1 - q, 0), v(1 - q, 1 - q, p), v(1 - q, q, p), spriteSide, hilight);
                createQuad(quads, v(q, q, p), v(q, 1 - q, p), v(q, 1 - q, 0), v(q, q, 0), spriteSide, hilight);

                createQuad(quads, v(q, q, p), v(1 - q, q, p), v(1 - q, 1 - q, p), v(q, 1 - q, p), spriteConnector, hilight);
                createQuad(quads, v(q, q, 0), v(q, 1 - q, 0), v(1 - q, 1 - q, 0), v(1 - q, q, 0), spriteSide, hilight);
            } else {
                CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, up, east, down);
                createQuad(quads, v(o, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, o, o), v(o, o, o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
            }

            if (south == CABLE) {
                createQuad(quads, v(o, 1 - o, 1), v(1 - o, 1 - o, 1), v(1 - o, 1 - o, 1 - o), v(o, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, 1), v(o, o, 1), spriteCable, hilight);
                createQuad(quads, v(1 - o, o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, 1), v(1 - o, o, 1), spriteCable, hilight);
                createQuad(quads, v(o, o, 1), v(o, 1 - o, 1), v(o, 1 - o, 1 - o), v(o, o, 1 - o), spriteCable, hilight);
            } else if (south == BLOCK) {
                createQuad(quads, v(o, 1 - o, 1 - p), v(1 - o, 1 - o, 1 - p), v(1 - o, 1 - o, 1 - o), v(o, 1 - o, 1 - o), spriteCable, hilight);
                createQuad(quads, v(o, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, 1 - p), v(o, o, 1 - p), spriteCable, hilight);
                createQuad(quads, v(1 - o, o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - p), v(1 - o, o, 1 - p), spriteCable, hilight);
                createQuad(quads, v(o, o, 1 - p), v(o, 1 - o, 1 - p), v(o, 1 - o, 1 - o), v(o, o, 1 - o), spriteCable, hilight);

                createQuad(quads, v(q, 1 - q, 1), v(1 - q, 1 - q, 1), v(1 - q, 1 - q, 1 - p), v(q, 1 - q, 1 - p), spriteSide, hilight);
                createQuad(quads, v(q, q, 1 - p), v(1 - q, q, 1 - p), v(1 - q, q, 1), v(q, q, 1), spriteSide, hilight);
                createQuad(quads, v(1 - q, q, 1 - p), v(1 - q, 1 - q, 1 - p), v(1 - q, 1 - q, 1), v(1 - q, q, 1), spriteSide, hilight);
                createQuad(quads, v(q, q, 1), v(q, 1 - q, 1), v(q, 1 - q, 1 - p), v(q, q, 1 - p), spriteSide, hilight);

                createQuad(quads, v(q, 1 - q, 1 - p), v(1 - q, 1 - q, 1 - p), v(1 - q, q, 1 - p), v(q, q, 1 - p), spriteConnector, hilight);
                createQuad(quads, v(q, 1 - q, 1), v(q, q, 1), v(1 - q, q, 1), v(1 - q, 1 - q, 1), spriteSide, hilight);
            } else {
                CablePatterns.QuadSetting pattern = CablePatterns.findPattern(west, down, east, up);
                createQuad(quads, v(o, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, 1 - o, 1 - o), v(o, 1 - o, 1 - o), getSprite.apply(pattern.getSprite()), pattern.getRotation(), hilight);
            }

        }

        return quads;
    }

}
