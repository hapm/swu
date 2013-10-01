package de.hapm.swu.map;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import de.hapm.swu.SmoothWorldUpdaterPlugin;

/**
 * MapRenderer implementation to render ChunkInfos on a MapView.
 * 
 * @author Markus Andree
 */
public class ChunkInfoRenderer extends MapRenderer {
	/**
	 * Saves all active ChunkInfoRenderingRequests indexed by the map id using it.
	 */
	private ConcurrentHashMap<Short, ChunkInfoRenderingRequest> renderedRequests;
	
	/**
	 * The background task used to render the maps.
	 */
	private ChunkInfoRenderingTask renderTask;
	
	/**
	 * The plugin, this ChunkInfoRenderer ist assigned to.
	 */
	private SmoothWorldUpdaterPlugin plugin;

	/**
	 * Initializes a new ChunkInfoRenderer instance and associates it with the given plugin.
	 * 
	 * @param plugin The SmoothWorldUpdaterPlugin instance, this renderer belongs to.
	 */
	public ChunkInfoRenderer(SmoothWorldUpdaterPlugin plugin) {
		this.plugin = plugin;
		this.renderedRequests = new ConcurrentHashMap<Short, ChunkInfoRenderingRequest>();
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
		
		MapCursor cursor;
		
		final Location location = player.getLocation();
		final int playerX = ((location.getBlockX() - view.getCenterX()) >> 3) /*+ 128*/;
		final int playerZ = ((location.getBlockZ() - view.getCenterZ()) >> 3) /*+ 128*/;
		if (playerX < 128 && playerX >= -128 && playerZ < 128 || playerZ >= -128) {
			final byte direction = (byte)(((int)location.getYaw() + 360) % 360 * 16 / 360);
			if (canvas.getCursors().size() == 0) {
				canvas.getCursors().addCursor(playerX, playerZ, direction, MapCursor.Type.WHITE_POINTER.getValue());
			}
			else {
				cursor = canvas.getCursors().getCursor(0);
				cursor.setDirection(direction);
				cursor.setX((byte) (playerX));
				cursor.setY((byte) (playerZ));
			}
		}
		
		if (!request.isDone())
			return;

		final byte[][] data = request.getData();
		for (int ii = 0; ii < 2; ii++) {
			int currentLine = request.nextLine();
			for (int i = 0; i < 128; i++) {
				canvas.setPixel(i, currentLine, data[currentLine][i]);
			}
		}
	}

	/**
	 * Schedules the background rendering task for being executed.
	 */
	public void start() {
		stop();
		renderTask = new ChunkInfoRenderingTask(plugin);
		renderTask.runTaskTimerAsynchronously(plugin, 0, 20);
	}
	
	/**
	 * Removes the rendering background task from the scheduled task queue.
	 */
	public void stop() {
		if (renderTask == null)
			return;
		
		renderTask.cancel();
		renderTask = null;
	}
}
