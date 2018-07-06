package mcjty.ariente.dimension;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * A heightmap for a chunk
 */
public class ChunkHeightmap {
    private byte heightmap[] = new byte[16*16];

    public ChunkHeightmap(ChunkPrimer primer) {
        char air = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int index = (x << 12) | (z << 8);
                int y = 255;
                while (y > 0 && primer.data[index + y] == air) {
                    y--;
                }
                heightmap[z * 16 + x] = (byte) y;
            }
        }
    }

    public int getHeight(int x, int z) {
        return heightmap[z*16+x] & 0xff;
    }

    public int getAverageHeight() {
        int cnt = 0;
        int y = 0;
        int yy;
        yy = getHeight(2, 2);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(13, 2);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(2, 13);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(13, 13);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        yy = getHeight(8, 8);
        if (yy > 5) {
            y += yy;
            cnt++;
        }
        if (cnt > 0) {
            return y / cnt;
        } else {
            return 0;
        }
    }

    public int getMinimumHeight() {
        int y = 255;
        int yy;
        yy = getHeight(2, 2);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(13, 2);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(2, 13);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(13, 13);
        if (yy < y) {
            y = yy;
        }
        yy = getHeight(8, 8);
        if (yy < y) {
            y = yy;
        }
        return y;
    }

}
