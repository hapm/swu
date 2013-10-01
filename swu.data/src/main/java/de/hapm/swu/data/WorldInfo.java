package de.hapm.swu.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class WorldInfo {
    @Id
    private String name;
    private boolean allChunksScanned;

    public WorldInfo(String name) {
	this.name = name;
	this.allChunksScanned = false;
    }

    public WorldInfo(String name, boolean allChunksScanned) {
	this.name = name;
	this.allChunksScanned = allChunksScanned;
    }

    public String getName() {
	return name;
    }

    public boolean areAllChunksScanned() {
	return allChunksScanned;
    }
}
