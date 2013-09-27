package de.hapm.chunkvis;

import java.awt.geom.Point2D;
import java.util.Set;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.Query;

import de.hapm.swu.data.ChunkInfo;

public class EbeanChunkInfoProvider implements ChunkInfoProvider {
	private EbeanServer server;
	
	public EbeanChunkInfoProvider(EbeanServer server) {
		this.server = server;
	}
	
	public Set<ChunkInfo> getChunks(Point2D minXY, Point2D maxXY) {
		Query<ChunkInfo> chunkQuery = server.find(ChunkInfo.class);
		final ExpressionFactory exp = server.getExpressionFactory();
		chunkQuery.where(exp.and(exp.between("x", minXY.getX(), maxXY.getX()), exp.between("z", minXY.getY(), maxXY.getY())));
		return chunkQuery.findSet();
	}

}
