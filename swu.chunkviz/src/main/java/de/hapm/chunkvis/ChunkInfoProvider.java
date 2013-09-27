package de.hapm.chunkvis;

import java.awt.geom.Point2D;
import java.util.Set;

import de.hapm.swu.data.ChunkInfo;

public interface ChunkInfoProvider {

	Set<ChunkInfo> getChunks(Point2D minXY, Point2D maxXY);

}
