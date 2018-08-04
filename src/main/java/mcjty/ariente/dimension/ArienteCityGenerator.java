package mcjty.ariente.dimension;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.decorative.MarbleBlock;
import mcjty.ariente.blocks.decorative.MarbleColor;
import mcjty.ariente.cities.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArienteCityGenerator {

    private Set<Character> rotatableChars = null;
    private boolean initialized = false;
    private ArienteChunkGenerator generator;

    private char airChar;
    private char hardAirChar;
    private char glowstoneChar;
    private char baseChar;
    private char gravelChar;
    private char glassChar;
    private char liquidChar;
    private char ironbarsChar;
    private char grassChar;
    private char bedrockChar;
    private char fillerChar;

    public void initialize(ArienteChunkGenerator generator) {
        this.generator = generator;
        if (!initialized) {
            airChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());
            hardAirChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.COMMAND_BLOCK.getDefaultState());
            glowstoneChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GLOWSTONE.getDefaultState());
            baseChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.STONE.getDefaultState());
            gravelChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GRAVEL.getDefaultState());
            liquidChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.WATER.getDefaultState());

            // @todo
            glassChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GLASS.getDefaultState());

            ironbarsChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.IRON_BARS.getDefaultState());
            grassChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GRASS.getDefaultState());
            bedrockChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.BEDROCK.getDefaultState());
            fillerChar = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.marble_bricks.getDefaultState().withProperty(MarbleBlock.COLOR, MarbleColor.BLACK));

            initialized = true;
        }
    }


    public Set<Character> getRotatableChars() {
        if (rotatableChars == null) {
            rotatableChars = new HashSet<>();
            addStates(Blocks.ACACIA_STAIRS, rotatableChars);
            addStates(Blocks.BIRCH_STAIRS, rotatableChars);
            addStates(Blocks.BRICK_STAIRS, rotatableChars);
            addStates(Blocks.QUARTZ_STAIRS, rotatableChars);
            addStates(Blocks.STONE_BRICK_STAIRS, rotatableChars);
            addStates(Blocks.DARK_OAK_STAIRS, rotatableChars);
            addStates(Blocks.JUNGLE_STAIRS, rotatableChars);
            addStates(Blocks.NETHER_BRICK_STAIRS, rotatableChars);
            addStates(Blocks.OAK_STAIRS, rotatableChars);
            addStates(Blocks.PURPUR_STAIRS, rotatableChars);
            addStates(Blocks.RED_SANDSTONE_STAIRS, rotatableChars);
            addStates(Blocks.SANDSTONE_STAIRS, rotatableChars);
            addStates(Blocks.SPRUCE_STAIRS, rotatableChars);
            addStates(Blocks.STONE_STAIRS, rotatableChars);
            addStates(Blocks.LADDER, rotatableChars);
        }
        return rotatableChars;
    }

    private static void addStates(Block block, Set<Character> set) {
        for (int m = 0; m < 16; m++) {
            try {
                IBlockState state = block.getStateFromMeta(m);
                set.add((char) Block.BLOCK_STATE_IDS.get(state));
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void generate(World worldIn, int x, int z, ChunkPrimer primer) {
        City city = CityTools.getNearestCity(generator, x, z);
        if (city != null) {
            List<BuildingPart> parts = CityTools.getBuildingParts(city, x, z);
            if (!parts.isEmpty()) {
                int lowestY = CityTools.getLowestHeight(city, generator, x, z);
                int y = lowestY;
                CityPlan plan = city.getPlan();
                for (BuildingPart part : parts) {
                    generatePart(primer, plan, part, Transform.ROTATE_NONE, 0, y, 0);
                    y += part.getSliceCount();
                }

                // Make pilars down if needed
                CityIndex cityIndex = CityTools.getCityIndex(x, z);
                assert cityIndex != null;
                if (cityIndex.isTopLeft()) {
                    fillDown(primer, lowestY, 2, 2);
                }
                if (cityIndex.isTopRight()) {
                    fillDown(primer, lowestY, 13, 2);
                }
                if (cityIndex.isBottomLeft()) {
                    fillDown(primer, lowestY, 2, 13);
                }
                if (cityIndex.isBottomRight()) {
                    fillDown(primer, lowestY, 13, 13);
                }
            }
        }
    }

    private void fillDown(ChunkPrimer primer, int lowestY, int dx, int dz) {
        int y = lowestY-1;
        int index = (dx << 12) | (dz << 8);
        while (y > 1) {
            if (primer.data[index+y] == airChar) {
                primer.data[index+y] = fillerChar;
            } else {
                break;
            }
            y--;
        }
    }

    private int generatePart(ChunkPrimer primer, CityPlan cityPlan,
                                    BuildingPart part,
                                    Transform transform,
                                    int ox, int oy, int oz) {
        CompiledPalette compiledPalette = CompiledPalette.getCompiledPalette(cityPlan.getPalette());

        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                char[] vs = part.getVSlice(x, z);
                if (vs != null) {
                    int rx = ox + transform.rotateX(x, z);
                    int rz = oz + transform.rotateZ(x, z);
                    int index = (rx << 12) | (rz << 8) + oy;
                    int len = vs.length;
                    for (int y = 0; y < len; y++) {
                        char c = vs[y];
                        Character b = compiledPalette.get(c);
                        if (b == null) {
                            throw new RuntimeException("Could not find entry '" + c + "' in the palette for part '" + part.getName() + "'!");
                        }

                        if (transform != Transform.ROTATE_NONE) {
                            if (getRotatableChars().contains(b)) {
                                IBlockState bs = Block.BLOCK_STATE_IDS.getByValue(b);
                                bs = bs.withRotation(transform.getMcRotation());
                                b = (char) Block.BLOCK_STATE_IDS.get(bs);
                            }
                        }
                        // We don't replace the world where the part is empty (air)
//                        if (b != airChar) {
//                            if (b == liquidChar) {
//                            } else if (b == hardAirChar) {
//                                b = airChar;
//                            }
//                        }
                        primer.data[index] = b;
                        index++;
                    }
                }
            }
        }
        return oy + part.getSliceCount();
    }


}
