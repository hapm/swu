package de.hapm.swu;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity @IdClass(ChunkInfoId.class)
public class ChunkInfo {
	@Id private String world;
	@Id private int key;
	private int generatorVersion;
	private int z;
	private int x;
	private long firstSeen;

	@ManyToMany
	@JoinTable(name="chunk_info_breaked_blocks")
	Set<BlockTypeId> breakedBlocks;
	@ManyToMany
	@JoinTable(name="chunk_info_placed_blocks")
	Set<BlockTypeId> placedBlocks;
	
	public static final int UNKOWN_GENERATOR_VERSION = -1;
	
	public ChunkInfo(int x, int z, int generatorVersion, long firstSeen) {
		this.breakedBlocks = new HashSet<BlockTypeId>();
		this.placedBlocks = new HashSet<BlockTypeId>();
		this.generatorVersion = generatorVersion;
		this.x = x;
		this.z = z;
		this.firstSeen = firstSeen;
	}
	
	public void setGeneratorVersion(int version) {
		generatorVersion = version;
	}
	
	public long getKey() {
		return key;
	}

	public int getGeneratorVersion() {
		return generatorVersion;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}

	public long getFirstSeen() {
		return firstSeen;
	}
	
	public void setPlaced(int typeId) {
		placedBlocks.add(new BlockTypeId(typeId));
	}

	public void setBreaked(int typeId) {
		breakedBlocks.add(new BlockTypeId(typeId));	
	}
	
	public static long getKey(int x, int z) {
		long key = x;
		key = key << 32;
		key = key | z;
		return key;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChunkInfo))
			return false;
		ChunkInfo chunkObj = (ChunkInfo)obj;
		return chunkObj.key == key && chunkObj.world.equals(world);
	}
	
	@Override
	public int hashCode() {
		return (world + "|" + key).hashCode();
	}
	
	@Override
	public String toString() {
		String.format("ChunkInfo (%d) world:%s, x:%d, z:%d, v:%d", key, world, x, z, generatorVersion);
		return super.toString();
	}
}
