package de.hapm.swu.data;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * Represents a unique id for a given Chunk object.
 * 
 * @author Markus Andree
 */
@Embeddable
public class ChunkInfoId implements Serializable {
    /**
     * The serialization id of the class.
     */
    private static final long serialVersionUID = -3487813011891898938L;

    /**
     * Saves the name of the world, the identified Chunk is in.
     */
    public String world;

    /**
     * Saves the x chunk coordinate of the identified Chunk.
     */
    public int x;
    /**
     * Saves the y chunk coordinate of the identified Chunk.
     */
    public int z;

    /**
     * Initializes a new ChunkInfoId.
     */
    public ChunkInfoId() {

    }

    /**
     * Initializes a new ChunkInfoId identifying the given Chunk.
     * @param world The name of the world, the chunk is in.
     * @param x The x chunk coordinate of the identified Chunk.
     * @param z The z chunk coordinate of the identified Chunk.
     */
    public ChunkInfoId(final String world, final int x, final int z) {
	this.world = world;
	this.x = x;
	this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null || !(obj instanceof ChunkInfoId))
	    return false;

	ChunkInfoId id = (ChunkInfoId) obj;

	return ((id.world == null && world == null) || id.world.equals(world))
		&& id.x == x && id.z == z;
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
	key = key | (long) z;
	return key;
    }
}
