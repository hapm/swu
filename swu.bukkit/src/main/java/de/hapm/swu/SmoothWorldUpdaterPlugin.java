package de.hapm.swu;

import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.PersistenceException;

import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.avaje.ebean.Query;

import de.hapm.swu.commands.MapCommands;
import de.hapm.swu.data.BlockTypeInfo;
import de.hapm.swu.data.ChunkInfo;
import de.hapm.swu.data.ChunkInfoId;
import de.hapm.swu.filter.TypeFilter;
import de.hapm.swu.map.ChunkInfoRenderer;

/**
 * This little plugin tracks when chunks where generated, and with what version
 * of
 * 
 * @author Markus Andree
 */
public class SmoothWorldUpdaterPlugin extends JavaPlugin implements Listener {
    /**
     * A DatabaseUpdateTask can be used to schedule database updates for later
     * asynchronous execution.
     * 
     * @author hapm
     */
    private final class DatabaseUpdateTask extends BukkitRunnable {
	private Hashtable<ChunkInfoId, ChunkInfo> chunkInfoCache;
	private ConcurrentLinkedQueue<ChunkInfoUpdate> databaseUpdates;
	private BukkitTask meRunning;
	private int updateTime;

	/**
	 * Sets the delay between each run of the DatabaseUpdateTask.
	 * 
	 * @param updateTime
	 *            The delay in game ticks.
	 */
	public void setUpdateTime(int updateTime) {
	    this.updateTime = updateTime;
	    if (meRunning != null) {
		stop();
		start();
	    }
	}

	/**
	 * Initializes a new DatabaseUpdateTask.
	 */
	public DatabaseUpdateTask() {
	    chunkInfoCache = new Hashtable<ChunkInfoId, ChunkInfo>();
	    databaseUpdates = new ConcurrentLinkedQueue<ChunkInfoUpdate>();
	    updateTime = 2400;
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
		    ChunkInfo info = getChunkInfo(update.getId(),
			    update.isFirstLoad());
		    try {
			switch (update.update(info)) {
			case EntityOnly:
			    if (!getDatabase().getBeanState(info)
				    .isNewOrDirty())
				break;

			case RelationsOnly:
			case SaveAll:
			    cacheForWrite(info);
			    break;
			case None:
			    break;
			}
		    } catch (Exception ex) {
			exceptions++;
			getLogger().severe(ex.toString());
		    }
		}

		if (chunkInfoCache.size() > 0) {
		    try {
			getDatabase().save(
				Collections.list(chunkInfoCache.elements()));
			long needed = System.currentTimeMillis() - start;
			getLogger()
				.info(String
					.format("Saved %d chunks in %dms from %d update requests with %d exceptions",
						chunkInfoCache.size(), needed,
						requests, exceptions));
		    } catch (Exception ex) {
			getLogger().severe(ex.toString());
		    } finally {
			chunkInfoCache.clear();
		    }
		}
	    }
	}

	/**
	 * Starts the processing of {@link ChunkInfoUpdate} instances.
	 * 
	 * Schedules the {@link DatabaseUpdateTask} as an async background timer
	 * task to process {@link ChunkInfoUpdate}s.
	 */
	public void start() {
	    if (meRunning != null)
		return;

	    meRunning = runTaskTimerAsynchronously(
		    SmoothWorldUpdaterPlugin.this, 0, updateTime);
	}

	/**
	 * Stops the processing of {@link ChunkInfoUpdate} instances.
	 * 
	 * Removes the schedule of the {@link DatabaseUpdateTask} as an async
	 * background timer task.
	 */
	public void stop() {
	    if (meRunning == null)
		return;

	    meRunning.cancel();
	    // Do a last run to be sure anything was written
	    run();
	    meRunning = null;
	}

	/**
	 * Adds the given ChunkInfoUpdate for being processed.
	 * 
	 * @param update
	 *            The ChunkInfoUpdate to process.
	 */
	public void add(ChunkInfoUpdate update) {
	    databaseUpdates.add(update);
	}

	/**
	 * Caches the given ChunkInfo for being saved on the next save call.
	 * 
	 * @param info
	 *            The info to cache.
	 */
	private void cacheForWrite(ChunkInfo info) {
	    final ChunkInfoId key = info.getId();
	    if (!chunkInfoCache.contains(key)) {
		chunkInfoCache.put(key, info);
	    }
	}

	/**
	 * Gets the ChunkInfo with the given id.
	 * 
	 * This will first try to find the ChunkInfo in the cache for pending
	 * changes. Then it will try to find it in the database.
	 * 
	 * @param id
	 *            The id of the ChunkInfo to get.
	 * @param isNew
	 *            Indicating whether the ChunkInfo is for a Chunk, that was
	 *            newly created.
	 * 
	 * @return The ChunkInfo for the given ChunkInfoId.
	 */
	private ChunkInfo getChunkInfo(ChunkInfoId id, final boolean isNew) {
	    ChunkInfo info = lookupInCache(id);
	    if (info == null) {
		info = lookupInDatabase(id);
	    }

	    if (info == null) {
		info = new ChunkInfo(id, isNew ? getActiveVersion()
			: ChunkInfo.UNKOWN_GENERATOR_VERSION, Calendar
			.getInstance(TimeZone.getTimeZone("GMT"))
			.getTimeInMillis());
	    }

	    return info;
	}

	/**
	 * Searches the update pending cache for the given key.
	 * 
	 * @param key
	 *            The ChunkInfoId to search for.
	 * @return The ChunkInfo from the cache, or null if there is no pending
	 *         update for the ChunkInfo identified by the given key.
	 */
	private ChunkInfo lookupInCache(ChunkInfoId key) {
	    Hashtable<ChunkInfoId, ChunkInfo> chunkInfoCache = new Hashtable<ChunkInfoId, ChunkInfo>();
	    ;
	    if (chunkInfoCache.containsKey(key))
		return chunkInfoCache.get(key);

	    return null;
	}

	/**
	 * Searches the database for the given key.
	 * 
	 * @param key
	 *            The ChunkInfoId to search for.
	 * @return The ChunkInfo from the database, or null if there is
	 *         ChunkInfo identified by the given key in the database.
	 */
	private ChunkInfo lookupInDatabase(ChunkInfoId key) {
	    ChunkInfo info = getDatabase().find(ChunkInfo.class, key);
	    return info;
	}
    }

    /**
     * Saves the instance of the background database update task.
     */
    private DatabaseUpdateTask updateTask;

    /**
     * Saves the instance of the ChunkInfoRenderer used to render ChunkInfo
     * maps.
     */
    private ChunkInfoRenderer mapRenderer;

    private TypeFilter fixedFilter;

    @Override
    public void onEnable() {
	super.onEnable();
	setupDatabase();
	mapRenderer = new ChunkInfoRenderer(this);
	mapRenderer.start();
	updateTask = new DatabaseUpdateTask();
	updateTask.setUpdateTime((int) getConfig().getLong("updatetime") * 20);
	updateTask.start();
	getServer().getPluginManager().registerEvents(this, this);
	getCommand("swumap").setExecutor(new MapCommands(this));
    }

    /**
     * Sets up the database when enabling the plugin the first time.
     */
    public void setupDatabase() {
	try {
	    getDatabase().find(ChunkInfo.class).findRowCount();
	} catch (PersistenceException ex) {
	    getLogger().info(
		    "Installing database for " + getDescription().getName()
			    + " due to first time usage");
	    installDDL();
	}
    }

    @Override
    public void onDisable() {
	super.onDisable();
	mapRenderer.stop();
	mapRenderer = null;
	updateTask.stop();
    }

    @EventHandler
    public void chunkLoad(final ChunkLoadEvent args) {
	updateTask.add(new ChunkInfoUpdate(getIdForChunk(args.getChunk()), args
		.isNewChunk()) {
	    @Override
	    public UpdateResult update(ChunkInfo chunk) {
		return UpdateResult.EntityOnly;
	    }
	});
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockPlaced(final BlockPlaceEvent args) {
	final int typeId = args.getBlockPlaced().getTypeId();
	updateTask.add(new ChunkInfoUpdate(getIdForChunk(args.getBlock()
		.getChunk())) {
	    @Override
	    public UpdateResult update(ChunkInfo chunk) {
		return chunk.getPlacedBlocks().add(lookupType(typeId)) ? UpdateResult.RelationsOnly
			: UpdateResult.None;
	    }
	});
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockBreak(final BlockBreakEvent args) {
	final int typeId = args.getBlock().getTypeId();

	updateTask.add(new ChunkInfoUpdate(getIdForChunk(args.getBlock()
		.getChunk())) {
	    @Override
	    public UpdateResult update(ChunkInfo chunk) {
		return chunk.getBreakedBlocks().add(lookupType(typeId)) ? UpdateResult.RelationsOnly
			: UpdateResult.None;
	    }
	});
    }

    /**
     * Generates the ChunkInfoId for the given Chunk.
     * 
     * @param chunk
     *            The Chunk to get the id for.
     * @return The ChunkInfoId for the Chunk.
     */
    public ChunkInfoId getIdForChunk(Chunk chunk) {
	return new ChunkInfoId(chunk.getWorld().getName(), chunk.getX(),
		chunk.getZ());
    }

    /**
     * Gets all ChunkInfos from the database, that are in the given range.
     * 
     * @param world
     *            The name of the world to lookup the ChunkInfos for.
     * @param minX
     *            The minimum x coord to get the ChunkInfo for.
     * @param minZ
     *            The minimum z coord to get the ChunkInfo for.
     * @param maxX
     *            The maximum x coord to get the ChunkInfo for.
     * @param maxZ
     *            The maximum z coord to get the ChunkInfo for.
     * @return
     */
    public ChunkInfo[] getChunkInfosInRange(String world, int minX, int minZ,
	    int maxX, int maxZ) {
	Query<ChunkInfo> qry = getDatabase().find(ChunkInfo.class);
	qry.where().between("x", minX, maxX).conjunction()
		.between("z", minZ, maxZ).conjunction().eq("world", world);
	return qry.findList().toArray(new ChunkInfo[0]);
    }

    /**
     * Looks up the BlockTypeInfo for a given id.
     * 
     * @param typeId
     *            The id to look for.
     * @return The BlockType associated to the id.
     */
    private BlockTypeInfo lookupType(int typeId) {
	BlockTypeInfo id = getDatabase().find(BlockTypeInfo.class, typeId);
	if (id == null) {
	    id = new BlockTypeInfo(typeId);
	    getDatabase().save(id);
	}

	return id;
    }
    
    public boolean isFixedByConfig(ChunkInfo chunk) {
	if (fixedFilter.matches(chunk.getBreakedBlocks(), chunk.getPlacedBlocks()))
	    return true;
	
	return false;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
	List<Class<?>> classes = super.getDatabaseClasses();
	classes.add(ChunkInfoId.class);
	classes.add(ChunkInfo.class);
	classes.add(BlockTypeInfo.class);
	return classes;
    }

    /**
     * Gets the currently active swu generator version number.
     * 
     * @return An id that identifies the current generator.
     */
    private int getActiveVersion() {
	return 1;
    }

    /**
     * Changes the given MapView to render a ChunkInfo map.
     * 
     * Removes any other renderer from the MapView and adds the
     * ChunkInfoRenderer as the only new one.
     * 
     * @param map
     *            The MapView to change.
     */
    public void changeToSwuMap(MapView map) {
	if (map.isVirtual())
	    return;

	for (MapRenderer renderer : map.getRenderers()) {
	    map.removeRenderer(renderer);
	}

	map.addRenderer(mapRenderer);
    }
}
