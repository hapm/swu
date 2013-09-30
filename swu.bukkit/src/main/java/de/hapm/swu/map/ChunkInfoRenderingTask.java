package de.hapm.swu.map;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.map.MapPalette;
import org.bukkit.scheduler.BukkitRunnable;

import de.hapm.swu.SmoothWorldUpdaterPlugin;
import de.hapm.swu.data.ChunkInfo;

public class ChunkInfoRenderingTask extends BukkitRunnable {
	private ConcurrentLinkedQueue<ChunkInfoRenderingRequest> openRequest;
	private SmoothWorldUpdaterPlugin plugin;
	
	public ChunkInfoRenderingTask(SmoothWorldUpdaterPlugin plugin) {
		this.plugin = plugin;
		this.openRequest = new ConcurrentLinkedQueue<ChunkInfoRenderingRequest>();
	}

	public void run() {
		ChunkInfoRenderingRequest request;
		while ((request = openRequest.poll()) != null) {
			render(request);
		}
	}

	private void render(ChunkInfoRenderingRequest request) {
		byte[] destImage = request.getData();
		int minX = (request.getCenterX()>>4) - 64;
		int minZ = (request.getCenterZ()>>4) - 64;
		int maxX = minX + 128;
		int maxZ = minZ + 128;

		long started = System.currentTimeMillis();
		ChunkInfo[] chunkInfos = plugin.getChunkInfosInRange(request.getWorldName(), minX, minZ, maxX, maxZ);
		for (ChunkInfo info : chunkInfos) {
			int index = info.getZ()-minZ<<8 + (info.getX()-minX);
			if (index < 0 || index >= destImage.length)
				continue;
			
			destImage[index] = MapPalette.RED;
		}
		
		plugin.getLogger().info(String.format("Rendered %d chunks in %dms", chunkInfos.length, System.currentTimeMillis()-started));
		
		request.setDone(true);
	}

	public void add(ChunkInfoRenderingRequest request) {
		openRequest.add(request);
	}
}
