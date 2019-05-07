package mcjty.ariente.oregen;

import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.config.WorldgenConfiguration;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Random;

public class ArienteOreGen implements IWorldGenerator {

    public static final String RETRO_NAME = "ArienteOWGen";
    public static ArienteOreGen instance = new ArienteOreGen();

    private static boolean retrogen = true;    // @todo config

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        generateWorld(random, chunkX, chunkZ, world, true);
    }

    public void generateWorld(Random random, int chunkX, int chunkZ, World world, boolean newGen) {
        if (!WorldgenConfiguration.doWorldGen()) {
            return;
        }

        if (!newGen && !retrogen) {
            return;
        }

        if (world.provider.getDimension() == 0) {
            addOreSpawn(ModBlocks.marble, (byte) MarbleColor.WHITE.ordinal(), Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    10, 20, 2, 16, 70);
            addOreSpawn(ModBlocks.lithiumore, (byte) 0, Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    2, 3, 2, 8, 20);        // RARE
            addOreSpawn(ModBlocks.manganeseore, (byte) 0, Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    3, 6, 3, 20, 50);       // MEDIUM
            addOreSpawn(ModBlocks.platinumore, (byte) 0, Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    3, 6, 4, 30, 80);
            addOreSpawn(ModBlocks.siliconore, (byte) 0, Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    3, 7, 4, 30, 60);
            addOreSpawn(ModBlocks.silverore, (byte) 0, Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    4, 8, 4, 10, 20);
            addOreSpawn(ModBlocks.negarite, (byte) 0, Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    4, 8, 5, 6, 30);
            addOreSpawn(ModBlocks.posirite, (byte) 0, Blocks.STONE, world, random, chunkX * 16, chunkZ * 16,
                    4, 8, 5, 6, 30);
        }

        if (!newGen) {
            world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
        }
    }


    public void addOreSpawn(Block block, byte blockMeta, Block targetBlock, World world, Random random, int blockXPos, int blockZPos, int minVeinSize, int maxVeinSize, int chancesToSpawn, int minY, int maxY) {
        WorldGenMinable minable = new WorldGenMinable(block.getStateFromMeta(blockMeta), (minVeinSize + random.nextInt(maxVeinSize - minVeinSize + 1)),
                input -> input.getBlock() == targetBlock && input.getBlock().getMetaFromState(input) == 0);
//                BlockMatcher.forBlock(targetBlock));
        for (int i = 0 ; i < chancesToSpawn ; i++) {
            int posX = blockXPos + random.nextInt(16);
            int posY = minY + random.nextInt(maxY - minY);
            int posZ = blockZPos + random.nextInt(16);
            minable.generate(world, random, new BlockPos(posX, posY, posZ));
        }
    }

    @SubscribeEvent
    public void handleChunkSaveEvent(ChunkDataEvent.Save event) {
        NBTTagCompound genTag = event.getData().getCompoundTag(RETRO_NAME);
        if (!genTag.hasKey("generated")) {
            // If we did not have this key then this is a new chunk and we will have proper ores generated.
            // Otherwise we are saving a chunk for which ores are not yet generated.
            genTag.setBoolean("generated", true);
        }
        event.getData().setTag(RETRO_NAME, genTag);
    }

    @SubscribeEvent
    public void handleChunkLoadEvent(ChunkDataEvent.Load event) {
        int dim = event.getWorld().provider.getDimension();

        boolean regen = false;
        NBTTagCompound tag = (NBTTagCompound) event.getData().getTag(RETRO_NAME);
        NBTTagList list = null;
        Pair<Integer,Integer> cCoord = Pair.of(event.getChunk().x, event.getChunk().z);

        if (tag != null) {
            boolean generated = retrogen && !tag.hasKey("generated");
            if (generated) {
                regen = true;
            }
        } else {
            regen = retrogen;
        }

        if (regen) {
            ArrayDeque<WorldTickHandler.RetroChunkCoord> chunks = WorldTickHandler.chunksToGen.get(dim);

            if (chunks == null) {
                WorldTickHandler.chunksToGen.put(dim, new ArrayDeque<>(128));
                chunks = WorldTickHandler.chunksToGen.get(dim);
            }
            if (chunks != null) {
                chunks.addLast(new WorldTickHandler.RetroChunkCoord(cCoord, list));
                WorldTickHandler.chunksToGen.put(dim, chunks);
            }
        }
    }

}
