package de.hapm.swu;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.UniqueConstraint;

@Entity @IdClass(ChunkInfoId.class) @UniqueConstraint(columnNames={"world","x","z"})
public class ChunkInfo {
	@Id 
	@Column(name="world")
	private String world;
	
	@Id
	@Column(name="z")
	private int z;

	@Id
	@Column(name="x")
	private int x;
		
	private int generatorVersion;
	private long firstSeen;

	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="chunk_info_breaked_blocks")
	Set<BlockTypeId> breakedBlocks;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="chunk_info_placed_blocks")
	Set<BlockTypeId> placedBlocks;
	
	public static final int UNKOWN_GENERATOR_VERSION = -1;
	
	public ChunkInfo(String world, int x, int z, int generatorVersion, long firstSeen) {
		this.breakedBlocks = new HashSet<BlockTypeId>();
		this.placedBlocks = new HashSet<BlockTypeId>();
		this.generatorVersion = generatorVersion;
		this.x = x;
		this.z = z;
		this.firstSeen = firstSeen;
		this.world = world;
	}
	
	public void setGeneratorVersion(int version) {
		generatorVersion = version;
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

	public String getWorld() {
		return world;
	}
	
	public boolean setPlaced(BlockTypeId typeId) {
		return placedBlocks.add(typeId);
	}

	public boolean setBreaked(BlockTypeId typeId) {
		return breakedBlocks.add(typeId);	
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChunkInfo))
			return false;
		ChunkInfo chunkObj = (ChunkInfo)obj;
		return chunkObj.x == x && chunkObj.z == z && chunkObj.world.equals(world);
	}
	
	@Override
	public int hashCode() {
		return (world + "|" + ChunkInfoId.getXZ(x, z)).hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("ChunkInfo (%s %d,%d v:%d)", world, x, z, generatorVersion);
	}
}
