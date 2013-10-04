package de.hapm.swu.filter;

import java.util.Set;

import de.hapm.swu.data.BlockTypeInfo;

public abstract class TypeFilter {
    public abstract boolean matches(Set<BlockTypeInfo> breakedBlocks,
	    Set<BlockTypeInfo> placedBlocks);
    
    public TypeFilter or(TypeFilter filter) {
	return new OrTypeFilter(this, filter);
    }
    
    public TypeFilter and(TypeFilter filter) {
	return new OrTypeFilter(this, filter);
    }
    
    public TypeFilter not() {
	return new NotTypeFilter(this);
    }
    
    public static TypeFilter includes(BlockTypeInfo type, boolean breaked) {
	return new IncludesTypeFilter(type, breaked);
    }

}