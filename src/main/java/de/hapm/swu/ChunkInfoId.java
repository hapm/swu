package de.hapm.swu;

import org.bukkit.Chunk;

public class ChunkInfoId {
	public long key;
	public String world;
	
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
}
