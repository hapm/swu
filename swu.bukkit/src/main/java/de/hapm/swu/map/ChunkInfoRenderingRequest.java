package de.hapm.swu.map;

/**
 * Data class holding all information needed to render a map from a set of
 * ChunkInfos.
 * 
 * @author Markus Andree
 */
public class ChunkInfoRenderingRequest {
    /**
     * The center world x coord used to render the map.
     */
    private int centerX;

    /**
     * The center world y coord used to render the map.
     */
    private int centerZ;

    /**
     * The name of the world, to render the map for.
     */
    private String worldName;

    /**
     * The id of the map, this ChunkInfoRenderingRequest was created for.
     */
    private short mapId;

    /**
     * The rendered data, ready for publishing on the map.
     */
    private byte[][] data;

    /**
     * Saves the state of the rendering process.
     */
    private boolean done;

    /**
     * Saves the last line that was rendered.
     */
    private int lastLine;

    /**
     * Initializes a new instance of the {@link ChunkInfoRenderingRequest}
     * class.
     * 
     * @param id
     *            The id of the map, the rendering should be done for.
     * @param centerX
     *            The current world x coordinate in the center of the map.
     * @param centerZ
     *            The current world y coordinate in the center of the map.
     * @param world
     *            The name of the world, to render the map for.
     */
    public ChunkInfoRenderingRequest(short id, int centerX, int centerZ,
	    String world) {
	this.mapId = id;
	this.centerX = centerX;
	this.centerZ = centerZ;
	this.worldName = world;
	data = new byte[128][128];
    }

    /**
     * Gets the x world coordinates for for the center of the map, that should
     * be rendered.
     * 
     * @return The x world coordinate.
     */
    public int getCenterX() {
	return centerX;
    }

    /**
     * Gets the z world coordinates for the center of the map, that should be
     * rendered.
     * 
     * @return The z world coordinate.
     */
    public int getCenterZ() {
	return centerZ;
    }

    /**
     * Gets the name of the world, this request is for.
     * 
     * @return The world name.
     */
    public String getWorldName() {
	return worldName;
    }

    /**
     * Gets the id of the map, this request is for.
     * 
     * @return
     */
    public short getMapId() {
	return mapId;
    }

    /**
     * Gets the number of the next line to render.
     * 
     * @return A number between 0 and 127, indicating a line that wasn't
     *         rendered fo rthe longest time.
     */
    public int nextLine() {
	lastLine += 4;
	if (lastLine > 127) {
	    lastLine = lastLine % 4 + 1;
	}

	return lastLine;
    }

    /**
     * Gets a reference to the internal image data buffer.
     * 
     * It can be used to directly store MapPallete values, that will be rendered
     * next time the map is rendered.
     * 
     * @return The reference to the internal image data buffer.
     */
    public byte[][] getData() {
	return data;
    }

    /**
     * Checks if the background rendering task has at least rendered this image
     * once.
     * 
     * @return Returns true, if there was at least one complete rendering cycle
     *         for this request, false otherwise.
     */
    public boolean isDone() {
	return done;
    }

    /**
     * Sets this request to be done or not.
     * 
     * @param Set
     *            to true to mark this request as done, to false otherwise.
     */
    public void setDone(boolean done) {
	this.done = done;
    }
}
