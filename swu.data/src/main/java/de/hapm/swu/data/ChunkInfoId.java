package de.hapm.swu.data;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class ChunkInfoId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3487813011891898938L;
	
	public String world;
	public int x;
	public int z;
	
	public ChunkInfoId() {
		
	}
	
	public ChunkInfoId(ChunkInfo info) {
		this.world = info.getWorld();
		this.x = info.getX();
		this.z = info.getZ();
	}
	
	public ChunkInfoId(final String world, final int x, final int z) {
		this.world = world;
		this.x = x;
		this.z = z;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ChunkInfoId))
			return false;
		
		ChunkInfoId id = (ChunkInfoId)obj;
		
		return ((id.world == null && world == null) || id.world.equals(world)) && id.x == x && id.z == z;
	}
	
	@Override
	public int hashCode() {
		return ((world == null ? "" : world) + "|" + getXZ()).hashCode();
	}

	private long getXZ() {
		return getXZ(x, z);
	}

	public static long getXZ(int x, int z) {
		long key = x;
		key = key << 32;
		key = key | (long)z;
		return key;
	}
}
