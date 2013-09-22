package de.hapm.swu;

import java.io.Serializable;

import org.bukkit.Chunk;

public class ChunkInfoId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3487813011891898938L;
	
	public String world;
	public long key;
	
	public ChunkInfoId() {
		
	}
	
	public ChunkInfoId(final String world, final long key) {
		this.key = key;
		this.world = world;
	}
	
	public ChunkInfoId(final String world, final int x, final int z) {
		this(world, ChunkInfo.getKey(x, z));
	}

	public ChunkInfoId(final Chunk chunk) {
		this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ChunkInfoId))
			return false;
		
		ChunkInfoId id = (ChunkInfoId)obj;
		
		return id.world == null || (id.world.equals(world) && id.key == key);
	}
	
	@Override
	public int hashCode() {
		return ((world == null ? "" : world) + "|" + key).hashCode();
	}
}
