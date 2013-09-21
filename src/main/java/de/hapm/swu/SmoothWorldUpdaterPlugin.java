package de.hapm.swu;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.PersistenceException;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This little plugin tracks when chunks where generated, and with what version of 
 * 
 * @author Markus Andree
 */
public class SmoothWorldUpdaterPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		setupDatabase();
		super.onEnable();
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
	}
	
	@EventHandler
	public void chunkLoad(final ChunkLoadEvent args) {
		final Chunk chunk = args.getChunk();
		final boolean newChunk = args.isNewChunk();
		getDatabase().getBackgroundExecutor().execute(new Runnable() {
			public void run() {
				ChunkInfo info = getChunkInfo(chunk, newChunk);
				getDatabase().save(info);
				getLogger().info("New " + info.toString());
			}
		});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void blockPlaced(final BlockPlaceEvent args) {
		final Chunk chunk = args.getBlock().getChunk();
		final int typeId = args.getBlockPlaced().getTypeId();
		getDatabase().getBackgroundExecutor().execute(new Runnable() {
			public void run() {
				ChunkInfo info = getChunkInfo(chunk);
				info.setPlaced(typeId);
				getDatabase().save(info);
			}
		});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void blockBreak(final BlockBreakEvent args) {
		final Chunk chunk = args.getBlock().getChunk();
		final int typeId = args.getBlock().getTypeId();
		
		getDatabase().getBackgroundExecutor().execute(new Runnable() {
			public void run() {
				ChunkInfo info = getChunkInfo(chunk);
				info.setBreaked(typeId);
				getDatabase().save(info);
			}
		});
	}
	
	public ChunkInfo getChunkInfo(final Chunk chunk) {
		return getChunkInfo(chunk, false);
	}
	
	public ChunkInfo getChunkInfo(final Chunk chunk, final boolean isNew) {
		ChunkInfo info = getDatabase().find(ChunkInfo.class, new ChunkInfoId(chunk));
		if (info == null) {
			info = new ChunkInfo(chunk.getX(), chunk.getZ(), isNew ? getActiveVersion() : ChunkInfo.UNKOWN_GENERATOR_VERSION, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
		}
		
		return info;
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
