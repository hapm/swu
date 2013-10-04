package de.hapm.swu.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.World;

public final class WorldUtils {
    private WorldUtils() {
    }

    public static void loadAllChunks(final World world) {
        final Pattern regionPattern = Pattern
                .compile("r\\.([0-9-]+)\\.([0-9-]+)\\.mca");

        File worldDir = new File(Bukkit.getWorldContainer(), world.getName());
        File regionDir = new File(worldDir, "region");

        File[] regionFiles = regionDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return regionPattern.matcher(name).matches();
            }
        });

        for (File f : regionFiles) {
            // extract coordinates from filename
            Matcher matcher = regionPattern.matcher(f.getName());
            if (!matcher.matches()) {
                continue;
            }

            int mcaX = Integer.parseInt(matcher.group(1));
            int mcaZ = Integer.parseInt(matcher.group(2));

            for (int cx = 0; cx < 32; cx++) {
                for (int cz = 0; cz < 32; cz++) {
                    // local chunk coordinates need to be transformed into
                    // global ones
                    world.loadChunk((mcaX << 5) + cx, (mcaZ << 5) + cz, false);
                }
            }
        }
    }
}
