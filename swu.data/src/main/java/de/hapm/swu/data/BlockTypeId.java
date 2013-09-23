package de.hapm.swu.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BlockTypeId {
	@Id private int id;
	
	public BlockTypeId(final int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
