package de.hapm.swu.data;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.UniqueConstraint;


@Entity @UniqueConstraint(columnNames={"world","x","z"})
public class ChunkInfo {
	@EmbeddedId
	private ChunkInfoId id;
		
	private int generatorVersion;
	private long firstSeen;

	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="chunk_info_breaked_blocks", joinColumns={@JoinColumn(name="chunk_info_world",referencedColumnName="world"),
			  @JoinColumn(name="chunk_info_x",referencedColumnName="x"),
			  @JoinColumn(name="chunk_info_z",referencedColumnName="z")})//,
			  //inverseJoinColumns=@JoinColumn(name="id",referencedColumnName="block_type_id"))
	Set<BlockTypeInfo> breakedBlocks;
	
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(name="chunk_info_placed_blocks", joinColumns={@JoinColumn(name="chunk_info_world",referencedColumnName="world"),
		  @JoinColumn(name="chunk_info_x",referencedColumnName="x"),
		  @JoinColumn(name="chunk_info_z",referencedColumnName="z")})//,
		  //inverseJoinColumns=@JoinColumn(name="block_type_id",referencedColumnName="id"))
	Set<BlockTypeInfo> placedBlocks;
	
	public static final int UNKOWN_GENERATOR_VERSION = -1;
	
	public ChunkInfo() {
		this.breakedBlocks = new HashSet<BlockTypeInfo>();
		this.placedBlocks = new HashSet<BlockTypeInfo>();
	}
	
	public ChunkInfo(ChunkInfoId id, int generatorVersion, long firstSeen) {
		this.breakedBlocks = new HashSet<BlockTypeInfo>();
		this.placedBlocks = new HashSet<BlockTypeInfo>();
		this.generatorVersion = generatorVersion;
		this.id = id;
		this.firstSeen = firstSeen;
	}
	
	public void setGeneratorVersion(int version) {
		generatorVersion = version;
	}

	public int getGeneratorVersion() {
		return generatorVersion;
	}
	
	public ChunkInfoId getId() {
		return id;
	}
	
	public int getX() {
		return id.x;
	}
	
	public int getZ() {
		return id.z;
	}

	public long getFirstSeen() {
		return firstSeen;
	}

	public String getWorld() {
		return id.world;
	}
	
	public Set<BlockTypeInfo> getPlacedBlocks() {
		return placedBlocks;
	}
	
	public Set<BlockTypeInfo> getBreakedBlocks() {
		return breakedBlocks;
	}
	
	@Override
	public String toString() {
		return String.format("ChunkInfo (%s %d,%d v:%d)", id.world, id.x, id.z, generatorVersion);
	}
}
