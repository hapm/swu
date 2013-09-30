package de.hapm.swu.map;

public class ChunkInfoRenderingRequest {
	private int centerX;
	private int centerZ;
	private String worldName;
	private short mapId;
	private byte[][] data;
	private boolean done;
	private int lastLine;
	
	public ChunkInfoRenderingRequest(short id, int centerX, int centerZ, String world) {
		this.mapId = id;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.worldName = world;
		data = new byte[128][128];
	}

	public int getCenterX() {
		return centerX;
	}

	public int getCenterZ() {
		return centerZ;
	}
	
	public String getWorldName() {
		return worldName;
	}
	
	public short getMapId() {
		return mapId;
	}
	
	public int nextLine() {
		lastLine += 4;
		if (lastLine > 127) {
			lastLine = lastLine % 4 + 1;
		}
		
		return lastLine;
	}

	public byte[][] getData() {
		return data;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
