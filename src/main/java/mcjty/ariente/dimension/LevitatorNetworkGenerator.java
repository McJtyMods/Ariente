package mcjty.ariente.dimension;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.decorative.*;
import mcjty.ariente.cities.*;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class LevitatorNetworkGenerator {

    private static boolean initialized = false;
    private static char airChar;
    private static char blueMarble;
    private static char blackMarble;
    private static char horizontalBeam;
    private static char verticalBeam;
    private static char slopeNorth;
    private static char slopeSouth;
    private static char slopeEast;
    private static char slopeWest;
    private static char slopeNorthUp;
    private static char slopeSouthUp;
    private static char slopeEastUp;
    private static char slopeWestUp;
    private static char glowLines;
    private static char lampTop;

    private static void initialize() {
        if (!initialized) {
            airChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());
            blueMarble = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.marble_smooth.getDefaultState().withProperty(MarbleBlock.COLOR, MarbleColor.BLUE));
            blackMarble = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.marble_smooth.getDefaultState().withProperty(MarbleBlock.COLOR, MarbleColor.BLACK));
            horizontalBeam = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.fluxBeamBlock.getDefaultState().withProperty(BaseBlock.FACING_HORIZ, EnumFacing.WEST));
            verticalBeam = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.fluxBeamBlock.getDefaultState().withProperty(BaseBlock.FACING_HORIZ, EnumFacing.NORTH));
            slopeNorth = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.NORTH));
            slopeSouth = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.SOUTH));
            slopeWest = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.WEST));
            slopeEast = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.EAST));
            slopeNorthUp = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.NORTH_UP));
            slopeSouthUp = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.SOUTH_UP));
            slopeWestUp = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.WEST_UP));
            slopeEastUp = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.slopeBlock.getDefaultState().withProperty(SlopeBlock.FACING, EnumFacingUpDown.EAST_UP));
            glowLines = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.blackmarble_techpat.getDefaultState().withProperty(BlackTechBlock.TYPE, TechType.LINES_GLOW));
            lampTop = (char) Block.BLOCK_STATE_IDS.get(ModBlocks.flatLightBlock.getDefaultState().withProperty(BaseBlock.FACING, EnumFacing.DOWN));
            initialized = true;
        }
    }


    public static void generate(World world, int chunkX, int chunkZ, ChunkPrimer primer, ArienteChunkGenerator generator) {
        if (CityTools.isCityChunk(chunkX, chunkZ)) {
            City city = CityTools.getNearestCity(generator, chunkX, chunkZ);
            if (city != null && city.getPlan().isUnderground()) {
                return;
            }
        }

        initialize();

        int cx = chunkX & 0xf;
        int cz = chunkZ & 0xf;

        boolean candidateX = cx == 8;
        boolean candidateZ = cz == 8;

        boolean inStation = cx >= 7 && cx <= 9 && cz >= 7 && cz <= 9;

        if (inStation) {
            CityPlan station = AssetRegistries.CITYPLANS.get("station");
            CityIndex index = CityTools.getCityIndex(chunkX, chunkZ, CityTools.getNearestStationCenter(chunkX, chunkZ), station);
            BuildingPart part = CityTools.getPart(chunkX, chunkZ, index, station, station.getPlan(), 3939321);
            if (part != null) {
                ArienteCityGenerator.generatePart(primer, station.getPalette(), part, Transform.ROTATE_NONE, 0, 30, 0);
            }
//
//            // Crossing
//            for (int dx = 0 ; dx <= 15 ; dx++) {
//                fillSide(primer, dx, 4);
//                fillSide(primer, dx, 12);
//            }
//            for (int dz = 0 ; dz <= 15 ; dz++) {
//                fillSide(primer, 4, dz);
//                fillSide(primer, 12, dz);
//            }
//            for (int dx = 0 ; dx <= 15 ; dx++) {
//                for (int dz = 0 ; dz <= 15 ; dz++) {
//                    if ((dx >= 5 && dx <= 11) || (dz >= 5 && dz <= 11)) {
//                        fillInner(primer, dx, dz);
//                    }
//                }
//            }
        } else if (candidateX) {
            for (int dx = 5 ; dx <= 11 ; dx++) {
                for (int dz = 0 ; dz <= 15 ; dz++) {
                    if (dx == 8) {
                        fillHorizontalBeam(primer, dx, dz);
                    } else if (dx == 5) {
                        fillInnerRamp(primer, dx, dz, slopeEast, slopeEastUp);
                    } else if (dx == 11) {
                        fillInnerRamp(primer, dx, dz, slopeWest, slopeWestUp);
                    } else {
                        fillInner(primer, dx, dz);
                    }
                }
            }
            for (int dz = 0 ; dz <= 15 ; dz++) {
                if (dz == 4 || dz == 11) {
                    fillGlowingSide(primer, 4, dz);
                    fillGlowingSide(primer, 12, dz);
                } else {
                    fillSide(primer, 4, dz);
                    fillSide(primer, 12, dz);
                }
            }
            primer.data[(8 << 12) | (8 << 8) + 36] = lampTop;
        } else if (candidateZ) {
            for (int dx = 0 ; dx <= 15 ; dx++) {
                for (int dz = 5 ; dz <= 11 ; dz++) {
                    if (dz == 8) {
                        fillVerticalBeam(primer, dx, dz);
                    } else if (dz == 5) {
                        fillInnerRamp(primer, dx, dz, slopeSouth, slopeSouthUp);
                    } else if (dz == 11) {
                        fillInnerRamp(primer, dx, dz, slopeNorth, slopeNorthUp);
                    } else {
                        fillInner(primer, dx, dz);
                    }
                }
            }
            for (int dx = 0 ; dx <= 15 ; dx++) {
                if (dx == 4 || dx == 11) {
                    fillGlowingSide(primer, dx, 4);
                    fillGlowingSide(primer, dx, 12);
                } else {
                    fillSide(primer, dx, 4);
                    fillSide(primer, dx, 12);
                }
            }
            primer.data[(8 << 12) | (8 << 8) + 36] = lampTop;
        }
    }

    private static void fillInner(ChunkPrimer primer, int dx, int dz) {
        int index = (dx << 12) | (dz << 8) + 30;
        primer.data[index++] = blueMarble;
        for (int y = 0 ; y < 5 ; y++) {
            primer.data[index++] = airChar;
        }
        primer.data[index++] = blueMarble;
    }

    private static void fillInnerRamp(ChunkPrimer primer, int dx, int dz, char rampBlock, char rampBlockUp) {
        int index = (dx << 12) | (dz << 8) + 30;
        primer.data[index++] = blueMarble;
        primer.data[index++] = rampBlock;
        for (int y = 0 ; y < 3 ; y++) {
            primer.data[index++] = airChar;
        }
        primer.data[index++] = rampBlockUp;
        primer.data[index++] = blueMarble;
    }

    private static void fillSide(ChunkPrimer primer, int dx, int dz) {
        int index = (dx << 12) | (dz << 8) + 30;
        primer.data[index++] = blueMarble;
        for (int y = 0 ; y < 5 ; y++) {
            primer.data[index++] = blackMarble;
        }
        primer.data[index++] = blueMarble;
    }

    private static void fillGlowingSide(ChunkPrimer primer, int dx, int dz) {
        int index = (dx << 12) | (dz << 8) + 30;
        primer.data[index++] = blueMarble;
        for (int y = 0 ; y < 5 ; y++) {
            primer.data[index++] = glowLines;
        }
        primer.data[index++] = blueMarble;
    }

    private static void fillHorizontalBeam(ChunkPrimer primer, int dx, int dz) {
        int index = (dx << 12) | (dz << 8) + 30;
        primer.data[index++] = blueMarble;
        primer.data[index++] = airChar;
        primer.data[index++] = horizontalBeam;
        for (int y = 0 ; y < 3 ; y++) {
            primer.data[index++] = airChar;
        }
        primer.data[index++] = blueMarble;
    }

    private static void fillVerticalBeam(ChunkPrimer primer, int dx, int dz) {
        int index = (dx << 12) | (dz << 8) + 30;
        primer.data[index++] = blueMarble;
        primer.data[index++] = airChar;
        primer.data[index++] = verticalBeam;
        for (int y = 0 ; y < 3 ; y++) {
            primer.data[index++] = airChar;
        }
        primer.data[index++] = blueMarble;
    }
}
