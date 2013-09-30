package de.hapm.swu.map;

import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.MemoryImageSource;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import de.hapm.swu.SmoothWorldUpdaterPlugin;

public class ChunkInfoRenderer extends MapRenderer {
	private ConcurrentHashMap<Short, ChunkInfoRenderingRequest> renderedRequests;
	private ChunkInfoRenderingTask renderTask;
	private SmoothWorldUpdaterPlugin plugin;
	private final ColorModel colorModel;

	public ChunkInfoRenderer(SmoothWorldUpdaterPlugin plugin) {
		this.plugin = plugin;
		this.renderedRequests = new ConcurrentHashMap<Short, ChunkInfoRenderingRequest>();
		colorModel = new ComponentColorModel(
				   ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { 16 }, 
				   false, true, Transparency.BITMASK, DataBuffer.TYPE_BYTE);
	}

	@Override
	public void render(MapView view, MapCanvas canvas,
			Player player) {
		if (renderTask == null)
			return;
		
		ChunkInfoRenderingRequest request = renderedRequests.get(view.getId());
		if (request == null) {
			request = new ChunkInfoRenderingRequest(view.getId(), view.getCenterX(), view.getCenterZ(), view.getWorld().getName());
			renderedRequests.put(view.getId(), request);
			renderTask.add(request);
		}
		
		if (!request.isDone())
			return;

		if (request.getLatestImage() == null) {
			MemoryImageSource source = new MemoryImageSource(128, 128, colorModel, request.getData(), 0, 128);
			request.setLatestImage(Toolkit.getDefaultToolkit().createImage(source));
		}
		
		canvas.drawImage(0, 0, request.getLatestImage());
		
		request.setDone(false);
	}

	public void start() {
		stop();
		
		renderTask = new ChunkInfoRenderingTask(plugin);
		renderTask.runTaskTimerAsynchronously(plugin, 0, 1200);
	}
	
	public void stop() {
		if (renderTask == null)
			return;
		
		renderTask.cancel();
		renderTask = null;
	}
}
