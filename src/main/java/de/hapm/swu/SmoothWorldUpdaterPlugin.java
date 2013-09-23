package de.hapm.swu;

import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.PersistenceException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * This little plugin tracks when chunks where generated, and with what version of 
 * 
 * @author Markus Andree
 */
public class SmoothWorldUpdaterPlugin extends JavaPlugin implements Listener {
	private final class DatabaseUpdateTask extends BukkitRunnable {
		private Hashtable<ChunkInfoId, ChunkInfo> chunkInfoCache;
		private ConcurrentLinkedQueue<ChunkInfoUpdate> databaseUpdates;
		private BukkitTask meRunning;
		
		public DatabaseUpdateTask() {
			chunkInfoCache = new Hashtable<ChunkInfoId, ChunkInfo>();
			databaseUpdates = new ConcurrentLinkedQueue<ChunkInfoUpdate>();
		}
		
		public void run() {
			synchronized (chunkInfoCache) {
				if (databaseUpdates.size() == 0)
					return;
				
				ChunkInfoUpdate update;
				while ((update = databaseUpdates.poll()) != null) {
					ChunkInfo info = getChunkInfo(update.getId(), update.isFirstLoad());
					try {
						if (update.update(info))
							cacheForWrite(info);
					}
					catch (Exception ex) {
						getLogger().severe(ex.toString());
					}
				}
				
				if (chunkInfoCache.size() > 0) {
					try {
						getDatabase().save(Collections.list(chunkInfoCache.elements()));
						getLogger().info(String.format("Saved %d chunks", chunkInfoCache.size()));
					}
					catch (Exception ex) {
						getLogger().severe(ex.toString());
					}
					finally {
						chunkInfoCache.clear();
					}
				}
			}
		}
		
		public void start() {
			if (meRunning != null)
				return;
			
			meRunning = runTaskTimerAsynchronously(SmoothWorldUpdaterPlugin.this, 0, 200);
		}
		
		public void stop() {
			if (meRunning == null)
				return;
			
			meRunning.cancel();
			// Do a last run to be sure anything was written
			run();
			meRunning = null;
		}
		
		public void add(ChunkInfoUpdate update) {
			databaseUpdates.add(update);
		}
		
		private void cacheForWrite(ChunkInfo info) {
			final ChunkInfoId key = new ChunkInfoId(info);
			if (!chunkInfoCache.contains(key)) {
				chunkInfoCache.put(key, info);
			}
		}

		private ChunkInfo getChunkInfo(ChunkInfoId id, final boolean isNew) {
			ChunkInfo info = lookupInCache(id);
			if (info == null) {
				info = lookupInDatabase(id);
			}
			
			if (info == null) {
				info = new ChunkInfo(id.world, id.x, id.z, isNew ? getActiveVersion() : ChunkInfo.UNKOWN_GENERATOR_VERSION, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
			}
			
			return info;
		}

		private ChunkInfo lookupInCache(ChunkInfoId key) {
			Hashtable<ChunkInfoId, ChunkInfo> chunkInfoCache = new Hashtable<ChunkInfoId, ChunkInfo>();;
			if (chunkInfoCache.containsKey(key))
				return chunkInfoCache.get(key);
			
			return null;
		}

		private ChunkInfo lookupInDatabase(ChunkInfoId key) {
			Hashtable<String, Object> id = new Hashtable<String, Object>();
			id.put("world", key.world);
			id.put("x", key.x);
			id.put("z", key.z);
			ChunkInfo info = getDatabase().find(ChunkInfo.class, id);
			return info;
		}
	}

	private DatabaseUpdateTask updateTask;
	
	@Override
	public void onEnable() {
		super.onEnable();
		setupDatabase();
		getServer().getPluginManager().registerEvents(this, this);
		updateTask = new DatabaseUpdateTask();
		updateTask.start();
	}

	public void setupDatabase() {
		try {
			getDatabase().find(ChunkInfo.class).findRowCount();
		}
		catch (PersistenceException ex) {
			getLogger().info("Installing database for " + getDescription().getName() + " due to first time usage");
			installDDL();
		}
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		updateTask.stop();
	}
	
	@EventHandler
	public void chunkLoad(final ChunkLoadEvent args) {
		updateTask.add(new ChunkInfoUpdate(new ChunkInfoId(args.getChunk()),args.isNewChunk()) {
			@Override
			public boolean update(ChunkInfo chunk) {
				return true;
			}
		});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void blockPlaced(final BlockPlaceEvent args) {
		final int typeId = args.getBlockPlaced().getTypeId();
		updateTask.add(new ChunkInfoUpdate(new ChunkInfoId(args.getBlock().getChunk())) {
			@Override
			public boolean update(ChunkInfo chunk) {
				chunk.setPlaced(typeId);
				return true;
			}
		});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void blockBreak(final BlockBreakEvent args) {
		final int typeId = args.getBlock().getTypeId();
		
		updateTask.add(new ChunkInfoUpdate(new ChunkInfoId(args.getBlock().getChunk())) {
			@Override
			public boolean update(ChunkInfo chunk) {
				chunk.setBreaked(typeId);
				return true;
			}
		});
	}
	
	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> classes = super.getDatabaseClasses();
		classes.add(ChunkInfo.class);
		classes.add(BlockTypeId.class);
		return classes;
	}

	private int getActiveVersion() {
		return 1;
	}
}
