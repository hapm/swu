package de.hapm.swu.map;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.map.MapPalette;
import org.bukkit.scheduler.BukkitRunnable;

import de.hapm.swu.SmoothWorldUpdaterPlugin;
import de.hapm.swu.data.ChunkInfo;

public class ChunkInfoRenderingTask extends BukkitRunnable {
	private ConcurrentLinkedQueue<ChunkInfoRenderingRequest> openRequest;
	private SmoothWorldUpdaterPlugin plugin;
	private boolean running;
	
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

	private void render(ChunkInfoRenderingRequest request) {
		byte[][] destImage = request.getData();
		int minX = (request.getCenterX()>>4) - 64;
		int minZ = (request.getCenterZ()>>4) - 64;
		int maxX = minX + 128;
		int maxZ = minZ + 128;

		long started = System.currentTimeMillis();
		ChunkInfo[] chunkInfos = plugin.getChunkInfosInRange(request.getWorldName(), minX, minZ, maxX, maxZ);
		for (ChunkInfo info : chunkInfos) {
			final int localZ = info.getZ()-minZ;
			final int localX = info.getX()-minX;

			destImage[localZ][localX] = MapPalette.LIGHT_GRAY;
			if (info.getBreakedBlocks().size() > 0)
				destImage[localZ][localX] = MapPalette.DARK_GRAY;
			if (info.getPlacedBlocks().size() > 0)
				destImage[localZ][localX] = MapPalette.RED;
		}
		
		plugin.getLogger().info(String.format("Rendered %d chunks in %dms", chunkInfos.length, System.currentTimeMillis()-started));
		request.setDone(true);
	}

	public void add(ChunkInfoRenderingRequest request) {
		openRequest.add(request);
	}
}
