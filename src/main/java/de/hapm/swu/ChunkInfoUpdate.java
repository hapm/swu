package de.hapm.swu;

public abstract class ChunkInfoUpdate {
	private ChunkInfoId id;
	private boolean firstLoad;

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
	
	public abstract boolean update(ChunkInfo chunk);
}
