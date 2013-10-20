package de.hapm.swu.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.Transient;

/**
 * Represents a height map for a chunk.
 * 
 * @author hapm
 */
@Embeddable
public class HeightMap {
    /**
     * Size of a height map in bytes.
     */
    public static final int SIZE = Integer.SIZE/8*256;
    
    /**
     * Saves the byte array for the height map.
     */
    @Lob
    private byte[] data;

    /**
     * Temporary IntBuffer used to extract the integer values from the data byte array.
     */
    @Transient
    private IntBuffer buffer;
    
    /**
     * Sets the complete height map to a new value.
     * 
     * @param map The new height map data to use.
     */
    public void setData(byte[] map) {
        if (map.length != SIZE)
            throw new IllegalArgumentException("A height map always need to have 256 values");
        
        this.data = Arrays.copyOf(map, SIZE);
        this.buffer = null;
    }
    
    /**
     * Gets a copy of the currently used height map.
     * 
     * @return The height map as an array, or null if the height map isn't serialized yet.
     */
    public byte[] getData() {
        if (!isInitialized())
            return null;
        
        return Arrays.copyOf(data, SIZE);
    }
    
    /**
     * Gets the height of the height map at the given local coordinates.
     * 
     * @param x The local x coordinate.
     * @param z The local z coordinate.
     * @return The height as a byte, or Integer.MIN_VALUE, if the height map isn't serialized yet.
     */
    public int getHeightAt(int x, int z) {
        if (x > 15 || z > 15 || x < 0 || z < 0)
            throw new IllegalArgumentException("local x and y coordinates need to be between 0 and 15");
        
        if (!isInitialized())
            return Integer.MIN_VALUE;
        
        x <<= 4;
        x += z;
        return getInternalIntBuffer().get(x);
    }
    
    /**
     * Sets the height of the height map at the given local coordinates.
     * 
     * @param x The local x coordinate.
     * @param z The local z coordinate.
     * @param height The height as a byte.
     */
    public void setHeightAt(int x, int z, int height) {
        if (x > 15 || z > 15 || x < 0 || z < 0)
            throw new IllegalArgumentException("local x and y coordinates need to be between 0 and 15");

        if (!isInitialized())
           data = new byte[SIZE];
        
        x <<= 4;
        x += z;
        getInternalIntBuffer().put(x, height);
    }
    
    /**
     * Returns a value indicating whether the map was already initialized or not.
     * 
     * @return Returns true if the map is initialized, false otherwise.
     */
    public boolean isInitialized() {
        return data != null && data.length == SIZE;
    }

    /**
     * Gets an IntBuffer that is backed by the data byte array.
     * 
     * @return The IntBuffer instance.
     */
    private IntBuffer getInternalIntBuffer() {
        if (buffer == null)
            buffer = ByteBuffer.wrap(data).asIntBuffer();
        
        return buffer;
    }
}
