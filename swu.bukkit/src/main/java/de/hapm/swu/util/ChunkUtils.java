package de.hapm.swu.util;

import org.bukkit.ChunkSnapshot;

import de.hapm.swu.data.HeightMap;

/**
 * Utils to mess around with chunks
 * 
 * @author hapm
 */
public final class ChunkUtils {
    private ChunkUtils() {
    }
    
    /**
     * Copies the height map of the given ChunkSnapshot to the given HeightMap instance.
     * 
     * @param chunk The ChunkInfo to get the heights from.
     * @param map The height map to copy the values to.
     */
    public static void copyHeightMap(ChunkSnapshot chunk, HeightMap map) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                map.setHeightAt(x, z, chunk.getHighestBlockYAt(x, z));
            }
        }
    }
}
