package de.hapm.swu;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;

import com.avaje.ebean.EbeanServer;

import de.hapm.swu.data.ChunkInfo;
import de.hapm.swu.data.ChunkInfoId;

/**
 * Used to calculate all needed changes for a chunk to be fixed.
 * 
 * @author Markus Andree
 */
public class ChunkCorrectionJob implements Runnable {
    /**
     * Saves a snapshot of the chunk, used for the calculations.
     */
    private ChunkSnapshot chunk;

    /**
     * Saves the instance of the associated plugin.
     */
    private SmoothWorldUpdaterPlugin plugin;

    /**
     * Initializes a new instance of the ChunkCorrectionJob class.
     * 
     * @param chunk
     *            The Chunk, that should be corrected.
     * @param plugin
     *            The plugin to associate the new job to.
     */
    public ChunkCorrectionJob(Chunk chunk, SmoothWorldUpdaterPlugin plugin) {
        this.chunk = chunk.getChunkSnapshot();
        this.plugin = plugin;
    }

    public void run() {
        EbeanServer server = plugin.getDatabase();
        ChunkInfoId id = new ChunkInfoId(chunk.getWorldName(), chunk.getX(), chunk.getZ());
        ChunkInfo info = server.find(ChunkInfo.class, id);
        
    }

}
