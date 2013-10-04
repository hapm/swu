package de.hapm.swu.filter;

import java.util.Set;

import de.hapm.swu.data.BlockTypeInfo;

public class IncludesTypeFilter extends TypeFilter {
    private BlockTypeInfo type;
    private boolean breaked;

    public IncludesTypeFilter(BlockTypeInfo type, boolean breaked) {
	this.type = type;
	this.breaked = breaked;
    }

    /* (non-Javadoc)
     * @see de.hapm.swu.TypeFilter#matches(java.util.Set, java.util.Set)
     */
    public boolean matches(Set<BlockTypeInfo> breakedBlocks,
	    Set<BlockTypeInfo> placedBlocks) {
	if (breaked)
	    return breakedBlocks.contains(type);
	else 
	    return placedBlocks.contains(type);
    }

}
