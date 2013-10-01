package de.hapm.chunkvis;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class ChunkInfoVisualizerPanel extends JPanel implements ActionListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = -1135055416153286125L;
    private ChunkInfoVisualizer visualizer;
    private Timer repaintTimer;

    public ChunkInfoVisualizerPanel() {
	repaintTimer = new Timer(500, this);
	repaintTimer.setInitialDelay(1000);
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	if (visualizer != null)
	    visualizer.draw((Graphics2D) g);
    }

    public void setVisualizer(ChunkInfoVisualizer visualizer) {
	this.visualizer = visualizer;
    }

    public void actionPerformed(ActionEvent e) {
	this.repaint();
    }

    public void start() {
	repaintTimer.start();
    }
}
