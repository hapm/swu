package de.hapm.chunkvis;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.RepaintManager;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChunkVisualizerMainForm {

    private JFrame frame;
    private ChunkInfoVisualizerPanel chunkInfoPanel;
    private EbeanServer server;
    private ChunkInfoVisualizer visualizer;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ChunkVisualizerMainForm window = new ChunkVisualizerMainForm();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ChunkVisualizerMainForm() {
        initialize();
        final RepaintManager currentManager = RepaintManager
                .currentManager(chunkInfoPanel);
        currentManager.setDoubleBufferingEnabled(true);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                ServerConfig config = new ServerConfig();
                config.setDefaultServer(true);
                final DataSourceConfig dataSourceConfig = new DataSourceConfig();
                dataSourceConfig
                        .setUrl("jdbc:sqlite:C:/Users/markus/Entwickeln/Java/CraftBukkitTestServer/plugins/SmoothWorldUpdater/SmoothWorldUpdater.db");
                dataSourceConfig.setDriver("org.sqlite.JDBC");
                dataSourceConfig.setMinConnections(1);
                dataSourceConfig.setMaxConnections(25);
                dataSourceConfig.setUsername("");
                dataSourceConfig.setPassword("");
                dataSourceConfig.setIsolationLevel(TransactionIsolation
                        .getLevel("READ_UNCOMMITTED"));
                config.setName("default");
                config.setDataSourceConfig(dataSourceConfig);
                config.addPackage("de.hapm.swu.data");
                server = EbeanServerFactory.create(config);
                visualizer = new ChunkInfoVisualizer();
                visualizer.setProvider(new EbeanChunkInfoProvider(server));
                chunkInfoPanel.setVisualizer(visualizer);
                chunkInfoPanel.start();
            }
        });
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chunkInfoPanel = new ChunkInfoVisualizerPanel();
        frame.getContentPane().add(chunkInfoPanel, BorderLayout.CENTER);
    }

}
