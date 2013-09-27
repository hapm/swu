package de.hapm.swu;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import de.hapm.swu.data.BlockTypeInfo;
import de.hapm.swu.data.ChunkInfo;
import de.hapm.swu.data.ChunkInfoId;

/**
 * This little plugin tracks when chunks where generated, and with what version of 
 * 
 * @author Markus Andree
 */
public class SmoothWorldUpdaterPlugin extends JavaPlugin implements Listener {
	/**
	 * A DatabaseUpdateTask can be used to schedule database updates for later asynchronous execution.
	 * 
	 * @author hapm
	 */
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
				long start = System.currentTimeMillis();
				long requests = 0;
				long exceptions = 0;
				ChunkInfoUpdate update;
				while ((update = databaseUpdates.poll()) != null) {
					requests++;
					ChunkInfo info = getChunkInfo(update.getId(), update.isFirstLoad());
					try {
						switch (update.update(info)) {
						case EntityOnly:
							if (!getDatabase().getBeanState(info).isNewOrDirty())
								break;
							
						case RelationsOnly:
						case SaveAll:
							cacheForWrite(info);
							break;
						case None:
							break;
						}
					}
					catch (Exception ex) {
						exceptions++;
						getLogger().severe(ex.toString());
					}
				}
				
				if (chunkInfoCache.size() > 0) {
					try {
						getDatabase().save(Collections.list(chunkInfoCache.elements()));
						long needed = System.currentTimeMillis() - start;
						getLogger().info(String.format("Saved %d chunks in %dms from %d update requests with %d exceptions", chunkInfoCache.size(), needed, requests, exceptions));
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
			
			meRunning = runTaskTimerAsynchronously(SmoothWorldUpdaterPlugin.this, 0, 2400);
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
				info = new ChunkInfo(id, isNew ? getActiveVersion() : ChunkInfo.UNKOWN_GENERATOR_VERSION, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
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
			//Hashtable<String, Object> id = new Hashtable<String, Object>();
			//id.put("world", key.world);
			//id.put("x", key.x);
			//id.put("z", key.z);
			ChunkInfo info = getDatabase().find(ChunkInfo.class, key /* id */);
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
		updateTask.add(new ChunkInfoUpdate(getIdForChunk(args.getChunk()),args.isNewChunk()) {
			@Override
			public UpdateResult update(ChunkInfo chunk) {
				return UpdateResult.EntityOnly;
			}
		});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void blockPlaced(final BlockPlaceEvent args) {
		final int typeId = args.getBlockPlaced().getTypeId();
		updateTask.add(new ChunkInfoUpdate(getIdForChunk(args.getBlock().getChunk())) {
			@Override
			public UpdateResult update(ChunkInfo chunk) {
				return chunk.getPlacedBlocks().add(lookupType(typeId)) ? UpdateResult.RelationsOnly : UpdateResult.None;
			}
		});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void blockBreak(final BlockBreakEvent args) {
		final int typeId = args.getBlock().getTypeId();
		
		updateTask.add(new ChunkInfoUpdate(getIdForChunk(args.getBlock().getChunk())) {
			@Override
			public UpdateResult update(ChunkInfo chunk) {
				return chunk.getBreakedBlocks().add(lookupType(typeId)) ? UpdateResult.RelationsOnly : UpdateResult.None;
			}
		});
	}
	
	public ChunkInfoId getIdForChunk(Chunk chunk) {
		return new ChunkInfoId(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	private BlockTypeInfo lookupType(int typeId) {
		BlockTypeInfo id = getDatabase().find(BlockTypeInfo.class, typeId);
		if (id == null) {
			id = new BlockTypeInfo(typeId);
			getDatabase().save(id);
		}
		
		return id;
	}
	
	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> classes = super.getDatabaseClasses();
		classes.add(ChunkInfoId.class);
		classes.add(ChunkInfo.class);
		classes.add(BlockTypeInfo.class);
		return classes;
	}

	private int getActiveVersion() {
		return 1;
	}
	
	private void loadAllChunks(World world) {
        final Pattern regionPattern = Pattern.compile("r\\.([0-9-]+)\\.([0-9-]+)\\.mca");
 
        File worldDir = new File(Bukkit.getWorldContainer(), world.getName());
        File regionDir = new File(worldDir, "region");
 
        File[] regionFiles = regionDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return regionPattern.matcher(name).matches();
            }
        });
 
        getLogger().info("Found " + (regionFiles.length * 1024) + " chunk candidates in " + regionFiles.length + " files to check for loading ...");
 
        for (File f : regionFiles) {
            // extract coordinates from filename
            Matcher matcher = regionPattern.matcher(f.getName());
            if (!matcher.matches()) {
                getLogger().warning("FilenameFilter accepted unmatched filename: " + f.getName());
                continue;
            }
 
            int mcaX = Integer.parseInt(matcher.group(1));
            int mcaZ = Integer.parseInt(matcher.group(2));
 
            int loadedCount = 0;
 
            for (int cx = 0; cx < 32; cx++) {
                for (int cz = 0; cz < 32; cz++) {
                    // local chunk coordinates need to be transformed into global ones
                    boolean didLoad = world.loadChunk((mcaX << 5) + cx, (mcaZ << 5) + cz, false);
                    if(didLoad)
                        loadedCount++;
                }
            }
 
            getLogger().info("Actually loaded " + loadedCount + " chunks from " + f.getName() + ".");
        }
    }
}
