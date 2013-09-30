package de.hapm.swu.map;

import java.awt.Image;

public class ChunkInfoRenderingRequest {
	private int centerX;
	private int centerZ;
	private String worldName;
	private short mapId;
	private byte[] data;
	private boolean done;
	private Image latestImage;
	
	public ChunkInfoRenderingRequest(short id, int centerX, int centerZ, String world) {
		this.mapId = id;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.worldName = world;
		data = new byte[128*128];
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

	public byte[] getData() {
		return data;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public Image getLatestImage() {
		return latestImage;
	}

	public void setLatestImage(Image latestImage) {
		this.latestImage = latestImage;
	}
}
