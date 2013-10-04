package de.hapm.chunkvis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import de.hapm.swu.data.ChunkInfo;

public class ChunkInfoVisualizer {
    private ChunkInfoProvider provider;
    private double scale;
    private AffineTransform paintTransform;
    private Paint backgroundPaint;
    private Paint createdChunkPaint;

    public ChunkInfoVisualizer() {
        backgroundPaint = Color.white;
        createdChunkPaint = Color.lightGray;
        scale = 1;
    }

    public void draw(Graphics2D g) {
        Set<ChunkInfo> chunks;
        g.setPaint(backgroundPaint);
        g.fill(g.getClip());
        if (provider == null)
            return;

        AffineTransform originalTransform = g.getTransform();
        Point2D minXY = new Point2D.Double();
        Point2D maxXY = new Point2D.Double();
        paintTransform = new AffineTransform();
        paintTransform.scale(scale, scale);
        paintTransform.translate(-200, -50);
        paintTransform.transform(new Point2D.Double(
                g.getClipBounds().getMinX(), g.getClipBounds().getMinY()),
                minXY);
        paintTransform.transform(new Point2D.Double(
                g.getClipBounds().getMaxX(), g.getClipBounds().getMaxY()),
                maxXY);
        chunks = provider.getChunks(minXY, maxXY);
        if (chunks == null || chunks.size() == 0)
            return;

        try {
            paintTransform.invert();
        } catch (NoninvertibleTransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        g.transform(paintTransform);
        Rectangle2D chunkRectangle = new Rectangle2D.Double();
        chunkRectangle.setRect(0, 0, 1, 1);
        for (ChunkInfo info : chunks) {
            g.setPaint(createdChunkPaint);
            chunkRectangle.setRect(info.getX(), info.getZ(), 1, 1);
            g.fill(chunkRectangle);
        }

        g.setTransform(originalTransform);
    }

    public void setProvider(ChunkInfoProvider provider) {
        this.provider = provider;
    }
}
