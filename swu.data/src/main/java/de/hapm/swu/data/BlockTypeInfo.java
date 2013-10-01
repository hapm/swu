package de.hapm.swu.data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Describes a block type
 * 
 * @author Markus Andree
 */
@Entity
public class BlockTypeInfo {
    /**
     * The internal id identifying the type.
     */
    @Id
    private int id;

    protected BlockTypeInfo() {
    }

    /**
     * Initializes a new instance of the BlockTypeInfo class.
     * 
     * @param id
     *            The internal id to use for the type.
     */
    public BlockTypeInfo(final int id) {
	this.id = id;
    }

    /**
     * Gets the id of this BlockTypeInfo.
     * 
     * @return The internal id.
     */
    public int getId() {
	return id;
    }
}
