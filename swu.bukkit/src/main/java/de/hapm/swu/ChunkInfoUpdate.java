package de.hapm.swu;

import de.hapm.swu.data.ChunkInfo;
import de.hapm.swu.data.ChunkInfoId;

public abstract class ChunkInfoUpdate {
	private ChunkInfoId id;
	private boolean firstLoad;
	
	public static enum UpdateResult {
		SaveAll,
		RelationsOnly, 
		EntityOnly,
		None
	}

	public ChunkInfoUpdate(ChunkInfoId chunkId) {
		this.id = chunkId;
	}

	public ChunkInfoUpdate(ChunkInfoId chunkId, boolean firstLoad) {
		this.id = chunkId;
		this.firstLoad = firstLoad;
	}
	
	public ChunkInfoId getId() {
		return id;
	}
	
	public boolean isFirstLoad() {
		return firstLoad;
	}
	
	public abstract UpdateResult update(ChunkInfo chunk);
}
