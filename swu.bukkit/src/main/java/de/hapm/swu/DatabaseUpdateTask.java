package de.hapm.swu;

import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import de.hapm.swu.data.ChunkInfo;
import de.hapm.swu.data.ChunkInfoId;
import de.hapm.swu.data.HeightMap;
import de.hapm.swu.util.ChunkUtils;

/**
 * A DatabaseUpdateTask can be used to schedule database updates for later
 * asynchronous execution.
 * 
 * @author hapm
 */
final class DatabaseUpdateTask extends BukkitRunnable {
    /**
     * 
     */
    private final SmoothWorldUpdaterPlugin plugin;
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
     * 
     * @param plugin The {@link SmoothWorldUpdaterPlugin} instance to associate this DatabaseUpdateTask to.
     */
    public DatabaseUpdateTask(SmoothWorldUpdaterPlugin plugin) {
        this.plugin = plugin;
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
                        if (!plugin.getDatabase().getBeanState(info)
                                .isNewOrDirty())
                            break;

                    case RelationsOnly:
                    case SaveAll:
                        final HeightMap heightMap = info.getHeightMap();
                        if (heightMap == null || !heightMap.isInitialized()) {
                            updateHeightMap(info);
                        }
                        
                        cacheForWrite(info);
                        break;
                    case None:
                        break;
                    }
                } catch (Exception ex) {
                    exceptions++;
                    plugin.getLogger().log(Level.SEVERE, "Failed to execute a databse update", ex);
                }
            }

            if (chunkInfoCache.size() > 0) {
                try {
                    plugin.getDatabase().save(
                            Collections.list(chunkInfoCache.elements()));
                    long needed = System.currentTimeMillis() - start;
                    plugin.getLogger()
                            .info(String
                                    .format("Saved %d chunks in %dms from %d update requests with %d exceptions",
                                            chunkInfoCache.size(), needed,
                                            requests, exceptions));
                } catch (Exception ex) {
                    plugin.getLogger().severe(ex.toString());
                } finally {
                    chunkInfoCache.clear();
                }
            }
        }
    }

    /**
     * Updates the height map of the given ChunkInfo.
     * 
     * As this waits for the main thread, it can be time intensive.
     * 
     * @param info The ChunkInfo to update.
     */
    private void updateHeightMap(final ChunkInfo info) throws InterruptedException, ExecutionException {
        final FutureTask<ChunkSnapshot> task = new FutureTask<ChunkSnapshot>(new Callable<ChunkSnapshot>() {
            public ChunkSnapshot call() throws Exception {
                World w = plugin.getServer().getWorld(info.getWorld());
                if (w == null)
                    return null;
                
                return w.getChunkAt(info.getX(), info.getZ()).getChunkSnapshot();
            }
        });
        
        plugin.getGameTasker().add(task);
        ChunkSnapshot chunk = task.get();
        ChunkUtils.copyHeightMap(chunk, info.getHeightMap());
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
                plugin, 0, updateTime);
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
            info = new ChunkInfo(id, isNew ? plugin.getActiveVersion()
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
        ChunkInfo info = plugin.getDatabase().find(ChunkInfo.class, key);
        return info;
    }
}