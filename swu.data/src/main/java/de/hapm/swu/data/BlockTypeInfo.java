package de.hapm.swu.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BlockTypeInfo {
	@Id private int id;
	
	protected BlockTypeInfo() {
	}
	
	public BlockTypeInfo(final int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
