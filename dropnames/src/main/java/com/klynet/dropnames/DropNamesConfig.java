package com.klynet.dropnames;

import java.util.Objects;

public final class DropNamesConfig {
    private DropNames plugin;
    private String dropFormat;
    private boolean shouldOverwriteDisplayName;
    private int maxDistance;
    private boolean forceLineOfSight;

    public DropNamesConfig(DropNames plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        dropFormat = plugin.getConfig().getString("drop_format");
        shouldOverwriteDisplayName = plugin.getConfig().getBoolean("should_overwrite_display_name");
        maxDistance = plugin.getConfig().getInt("max_distance");
        forceLineOfSight = plugin.getConfig().getBoolean("force_line_of_sight");
        if (dropFormat == null || dropFormat.isEmpty()) {
            throw new IllegalArgumentException("dropFormat cannot be null or empty.");
        }
        if (maxDistance < 0) {
            throw new IllegalArgumentException("maxDistance cannot be negative.");
        }
    }

    public String dropFormat() {
        return dropFormat;
    }

    public boolean shouldOverwriteDisplayName() {
        return shouldOverwriteDisplayName;
    }

    public int maxDistance() {
        return maxDistance;
    }

    public boolean forceLineOfSight() {
        return forceLineOfSight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DropNamesConfig) obj;
        return Objects.equals(this.dropFormat, that.dropFormat) &&
                this.shouldOverwriteDisplayName == that.shouldOverwriteDisplayName &&
                this.maxDistance == that.maxDistance &&
                this.forceLineOfSight == that.forceLineOfSight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dropFormat, shouldOverwriteDisplayName, maxDistance, forceLineOfSight);
    }

    @Override
    public String toString() {
        return "DropNamesConfig[" +
                "dropFormat=" + dropFormat + ", " +
                "shouldOverwriteDisplayName=" + shouldOverwriteDisplayName + ", " +
                "maxDistance=" + maxDistance + ", " +
                "forceLineOfSight=" + forceLineOfSight + ']';
    }

}
