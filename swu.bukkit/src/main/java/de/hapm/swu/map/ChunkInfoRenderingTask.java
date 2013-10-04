package de.hapm.swu.map;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.map.MapPalette;
import org.bukkit.scheduler.BukkitRunnable;

import de.hapm.swu.SmoothWorldUpdaterPlugin;
import de.hapm.swu.data.ChunkInfo;

/**
 * The ChunkInfoRenderingTask is used to draw the ChunkInfo from the database
 * using the MapPalette.
 * 
 * @author Markus Andree
 */
public class ChunkInfoRenderingTask extends BukkitRunnable {
    /**
     * Saves the queue of open request, that need to be processed by the
     * background rendering task.
     */
    private ConcurrentLinkedQueue<ChunkInfoRenderingRequest> openRequest;

    /**
     * Saves the plugin instance, this rendereing task is associated to.
     */
    private SmoothWorldUpdaterPlugin plugin;

    /**
     * Saves a flag indicating whether the task is already running.
     */
    private boolean running;

    /**
     * Initializes a new instance of the ChunkInfoRenderingTask.
     * 
     * @param plugin
     *            The plugin instance ot associate this task to.
     */
    public ChunkInfoRenderingTask(SmoothWorldUpdaterPlugin plugin) {
        this.plugin = plugin;
        this.openRequest = new ConcurrentLinkedQueue<ChunkInfoRenderingRequest>();
    }

    public void run() {
        if (running) {
            return;
        }

        running = true;
        ChunkInfoRenderingRequest request;
        while ((request = openRequest.poll()) != null) {
            render(request);
        }

        running = false;
    }

    /**
     * Renders the given ChunkInfoRenderingRequest.
     * 
     * @param request
     *            The request to render.
     */
    private void render(ChunkInfoRenderingRequest request) {
        byte[][] destImage = request.getData();
        int minX = (request.getCenterX() >> 4) - 64;
        int minZ = (request.getCenterZ() >> 4) - 64;
        int maxX = minX + 128;
        int maxZ = minZ + 128;

        long started = System.currentTimeMillis();
        ChunkInfo[] chunkInfos = plugin.getChunkInfosInRange(
                request.getWorldName(), minX, minZ, maxX, maxZ);
        for (ChunkInfo info : chunkInfos) {
            final int localZ = info.getZ() - minZ;
            final int localX = info.getX() - minX;

            destImage[localZ][localX] = MapPalette.LIGHT_GRAY;
            if (info.getBreakedBlocks().size() > 0)
                destImage[localZ][localX] = MapPalette.DARK_GRAY;
            if (info.getPlacedBlocks().size() > 0)
                destImage[localZ][localX] = MapPalette.RED;
        }

        plugin.getLogger().info(
                String.format("Rendered %d chunks in %dms", chunkInfos.length,
                        System.currentTimeMillis() - started));
        request.setDone(true);
    }

    /**
     * Adds the given request to the rendering queue for background processing.
     * 
     * @param request
     *            The request to add.
     */
    public void add(ChunkInfoRenderingRequest request) {
        openRequest.add(request);
    }
}
