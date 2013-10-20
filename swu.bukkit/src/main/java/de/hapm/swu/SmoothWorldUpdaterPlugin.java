package de.hapm.swu;

import java.util.List;

import javax.persistence.PersistenceException;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.Query;

import de.hapm.swu.commands.MapCommands;
import de.hapm.swu.data.BlockTypeInfo;
import de.hapm.swu.data.ChunkInfo;
import de.hapm.swu.data.ChunkInfoId;
import de.hapm.swu.filter.TypeFilter;
import de.hapm.swu.map.ChunkInfoRenderer;

/**
 * This plugin tracks when chunks where generated, and with what version
 * of bukkit it was created.
 * 
 * @author Markus Andree
 */
public class SmoothWorldUpdaterPlugin extends JavaPlugin implements Listener {
    /**
     * Saves the instance of the background database update task.
     */
    private DatabaseUpdateTask updateTask;
    
    private FutureTaskRunnable gameTask;

    /**
     * Saves the instance of the ChunkInfoRenderer used to render ChunkInfo
     * maps.
     */
    private ChunkInfoRenderer mapRenderer;

    /**
     * Saves the currently configured type filter.
     */
    private TypeFilter fixedFilter;

    @Override
    public void onEnable() {
        super.onEnable();
        setupDatabase();
        mapRenderer = new ChunkInfoRenderer(this);
        mapRenderer.start();
        gameTask = new FutureTaskRunnable(this);
        gameTask.start();
        updateTask = new DatabaseUpdateTask(this);
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
        gameTask.stop();
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
    
    public FutureTaskRunnable getGameTasker() {
        return gameTask;
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

    /**
     * Check if the given ChunkInfo should not be changed, because of the filter configured
     * in the config.
     * 
     * @param chunk The ChunkInfo to check.
     * @return Returns true if the ChunkInfo is fixed, because of filter rules from the config,
     *         false otherwise.
     */
    public boolean isFixedByConfig(ChunkInfo chunk) {
        if (fixedFilter.matches(chunk.getBreakedBlocks(),
                chunk.getPlacedBlocks()))
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
    int getActiveVersion() {
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
