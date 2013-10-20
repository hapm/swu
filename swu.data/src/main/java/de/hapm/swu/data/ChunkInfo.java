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

import com.avaje.ebean.annotation.EmbeddedColumns;

/**
 * Represents all information saved for a given Chunk.
 * 
 * @author Markus Andree
 */
@Entity
@UniqueConstraint(columnNames = { "world", "x", "z" })
public class ChunkInfo {
    /**
     * Saves the id of the ChunkInfo.
     */
    @EmbeddedId
    private ChunkInfoId id;

    /**
     * The generator version used to create the chunk.
     */
    private int generatorVersion;

    /**
     * The time in ms, when this chunk was seen the first time.
     */
    private long firstSeen;
    
    /**
     * Saves the height map of the chunk as seen the first time.
     */
    @EmbeddedColumns(columns="data=height_map")
    private HeightMap heightMap;

    /**
     * Saves if this Chunk was manually fixed by the user.
     */
    private boolean isFixed;

    /**
     * Saves a list of all block types, that where already broken in the
     * represented Chunk.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "chunk_info_breaked_blocks", joinColumns = {
            @JoinColumn(name = "chunk_info_world", referencedColumnName = "world"),
            @JoinColumn(name = "chunk_info_x", referencedColumnName = "x"),
            @JoinColumn(name = "chunk_info_z", referencedColumnName = "z") })
    Set<BlockTypeInfo> breakedBlocks;

    /**
     * Saves a list of all block types, that where already placed in the
     * represented Chunk.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "chunk_info_placed_blocks", joinColumns = {
            @JoinColumn(name = "chunk_info_world", referencedColumnName = "world"),
            @JoinColumn(name = "chunk_info_x", referencedColumnName = "x"),
            @JoinColumn(name = "chunk_info_z", referencedColumnName = "z") })
    Set<BlockTypeInfo> placedBlocks;

    /**
     * Used as the generator version for Chunk, where swu couldn't get the
     * generator version for.
     */
    public static final int UNKOWN_GENERATOR_VERSION = -1;

    /**
     * Initializes a new instance of the ChunkInfo class.
     */
    public ChunkInfo() {
        this.breakedBlocks = new HashSet<BlockTypeInfo>();
        this.placedBlocks = new HashSet<BlockTypeInfo>();
    }

    /**
     * Initializes a new instance of the ChunkInfo class.
     * 
     * @param id
     *            The id of the ChunkInfo to create.
     * @param generatorVersion
     *            The generator version used to create the Chunk represented by
     *            the ChunkInfo to create.
     * @param firstSeen
     *            The time in ms, when the chunk was seen the first time.
     */
    public ChunkInfo(ChunkInfoId id, int generatorVersion, long firstSeen) {
        this.breakedBlocks = new HashSet<BlockTypeInfo>();
        this.placedBlocks = new HashSet<BlockTypeInfo>();
        this.generatorVersion = generatorVersion;
        this.id = id;
        this.firstSeen = firstSeen;
    }

    /**
     * Sets the generator version to the given number.
     * 
     * @param version
     *            The version of the generator.
     */
    public void setGeneratorVersion(int version) {
        generatorVersion = version;
    }

    /**
     * Gets the version of the generator used to generate the Chunk represented
     * by this ChunkInfo.
     * 
     * @return The generator version.
     */
    public int getGeneratorVersion() {
        return generatorVersion;
    }

    /**
     * Gets the ChunkInfoId for this ChunkInfo.
     * 
     * @return The id.
     */
    public ChunkInfoId getId() {
        return id;
    }

    /**
     * Gets the chunk x coord for this ChunkInfo.
     * 
     * @return The chunk x coordinate.
     */
    public int getX() {
        return id.x;
    }

    /**
     * Gets the chunk y coord for this ChunkInfo.
     * 
     * @return The chunk y coordinate.
     */
    public int getZ() {
        return id.z;
    }

    /**
     * Gets the time when the Chunk represented by this ChunkInfo was first
     * seen.
     * 
     * @return The time in ms.
     */
    public long getFirstSeen() {
        return firstSeen;
    }

    /**
     * Gets the name of the World, this ChunkInfo is for.
     * 
     * @return The worlds name.
     */
    public String getWorld() {
        return id.world;
    }

    /**
     * Gets a list of all BlockTypeInfos placed in the Chunk represented by this
     * ChunkInfo.
     * 
     * @return The list of BlockTypeInfos.
     */
    public Set<BlockTypeInfo> getPlacedBlocks() {
        return placedBlocks;
    }

    /**
     * Gets a list of all BlockTypeInfos broken in the Chunk represented by this
     * ChunkInfo.
     * 
     * @return The list of BlockTypeInfos.
     */
    public Set<BlockTypeInfo> getBreakedBlocks() {
        return breakedBlocks;
    }
    
    /**
     * Gets the earliest known height map for the chunk.
     * 
     * @return The height map.
     */
    public HeightMap getHeightMap() {
        if (heightMap == null)
            heightMap = new HeightMap();
        
        return heightMap;
    }

    /**
     * Gets a value indicating whether the related chunk was set to be fixed
     * manually by the user.
     * 
     * If this is false, the correction of the Chunk will be decided on the
     * dynamic rules defined in the configuration of the plugin.
     * 
     * @return Returns true, if the user forced this Chunk to be not
     *         regenerated, false otherwise.
     */
    public boolean isManuallyFixed() {
        return isFixed;
    }

    /**
     * Sets a value indicating whether the related Chunk should never be
     * corrected or not.
     * 
     * @param fixed
     *            If true, the Chunk will never be corrected on map updates,
     *            else the rules defined in the configuration will aply to check
     *            if the chunk is correctable.
     */
    public void setManuallyFixed(boolean fixed) {
        isFixed = fixed;
    }

    @Override
    public String toString() {
        return String.format("ChunkInfo (%s %d,%d v:%d)", id.world, id.x, id.z,
                generatorVersion);
    }
}
