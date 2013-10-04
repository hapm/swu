package de.hapm.swu.filter;

import java.util.Set;

import de.hapm.swu.data.BlockTypeInfo;

public class NotTypeFilter extends TypeFilter {

    private TypeFilter filter;

    public NotTypeFilter(TypeFilter filter) {
        this.filter = filter;
    }

    public boolean matches(Set<BlockTypeInfo> breakedBlocks,
            Set<BlockTypeInfo> placedBlocks) {
        return !filter.matches(breakedBlocks, placedBlocks);
    }

}
