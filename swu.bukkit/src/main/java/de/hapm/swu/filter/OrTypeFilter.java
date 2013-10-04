package de.hapm.swu.filter;

import java.util.Set;

import de.hapm.swu.data.BlockTypeInfo;

public class OrTypeFilter extends TypeFilter {
    private TypeFilter filter1;
    private TypeFilter filter2;

    public OrTypeFilter(TypeFilter filter1, TypeFilter filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    public boolean matches(Set<BlockTypeInfo> breakedBlocks,
            Set<BlockTypeInfo> placedBlocks) {
        return filter1.matches(breakedBlocks, placedBlocks)
                || filter2.matches(breakedBlocks, placedBlocks);
    }

}
