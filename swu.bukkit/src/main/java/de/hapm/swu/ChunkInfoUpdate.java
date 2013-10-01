package de.hapm.swu;

import de.hapm.swu.data.ChunkInfo;
import de.hapm.swu.data.ChunkInfoId;

/**
 * A ChunkInfoUpdate is used to save the informations needed for a ChunkInfo update.
 * 
 * The ChunkInfoUpdate.update method will be run in a background thread, where
 * you can access the database without any hassel about lag.
 * 
 * @author Markus Andree
 */
public abstract class ChunkInfoUpdate {
	/**
	 * The id of the ChunkInfo that should be updated.
	 */
	private ChunkInfoId id;
	
	/**
	 * A flag indicating whether the Chunk for the ChunkInfo was loaded the first time.
	 */
	private boolean firstLoad;
	
	/**
	 * Possible results of the update.
	 * 
	 * @author Markus Andree
	 */
	public static enum UpdateResult {
		/**
		 * Will save the ChunkInfo and all its relations.
		 */
		SaveAll,
		
		/**
		 * Will only save the relations.
		 */
		RelationsOnly, 
		
		/**
		 * Will only save the ChunkInfo itself.
		 */
		EntityOnly,
		
		/**
		 * Will save nothing automatically.
		 */
		None
	}

	/**
	 * Intializing a new instance of the ChunkInfoUpdate for the given ChunkInfoId.
	 * 
	 * @param chunkId The id to create the ChunkInfoupdate for.
	 */
	public ChunkInfoUpdate(ChunkInfoId chunkId) {
		this.id = chunkId;
	}

	/**
	 * Intializing a new instance of the ChunkInfoUpdate for the given ChunkInfoId.
	 * 
	 * @param chunkId The id to create the ChunkInfoupdate for.
	 * @param firstLoad Should be set to true , if the Chunk identified by the given id
	 *                  was loaded the first time.
	 */
	public ChunkInfoUpdate(ChunkInfoId chunkId, boolean firstLoad) {
		this.id = chunkId;
		this.firstLoad = firstLoad;
	}
	
	/**
	 * Gets the id of the ChunkInfo, that should be updated.
	 * 
	 * @return The id to be updated.
	 */
	public ChunkInfoId getId() {
		return id;
	}
	
	/**
	 * Gets a value indicating whether the Chunk identified by the ChunkInfoId of the update, 
	 * was loaded the first time.
	 * 
	 * @return Returns true if the Chunk is new, false otherwise.
	 */
	public boolean isFirstLoad() {
		return firstLoad;
	}
	
	/**
	 * Describes the update steps, that should happen on the ChunkInfo object.
	 * 
	 * @param chunk The ChunkInfo for the ChunkInfoId of this update.
	 * @return The action that should happen after the update.
	 */
	public abstract UpdateResult update(ChunkInfo chunk);
}
