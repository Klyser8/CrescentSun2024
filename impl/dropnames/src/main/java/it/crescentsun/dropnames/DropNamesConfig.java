package it.crescentsun.dropnames;

import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.SingletonPluginData;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;

import java.util.UUID;

@DatabaseTable(tableName="config", plugin=DropNames.class)
public class DropNamesConfig extends SingletonPluginData {

    @DatabaseColumn(columnName="uuid", dataType= DataType.VARCHAR_36, isPrimaryKey=true, order=1)
    public final UUID uuid = DropNames.CONFIG_UUID;

    @DatabaseColumn(columnName="drop_format", dataType=DataType.VARCHAR_255, order=2)
    private String dropFormat = CrescentHexCodes.YELLOW + "[%amount%]" + CrescentHexCodes.WHITE + " - %name%";

    @DatabaseColumn(columnName="max_render_distance", dataType=DataType.FLOAT, order=3)
    private float maxRenderDistance = 10.0f;

    @DatabaseColumn(columnName="min_render_distance", dataType=DataType.FLOAT, order=4)
    private float minRenderDistance = 0.75f;

    @DatabaseColumn(columnName="overwrite_display_name", dataType=DataType.BOOLEAN, order=5)
    private boolean overwriteDisplayName = false;

    @DatabaseColumn(columnName="force_line_of_sight", dataType=DataType.BOOLEAN, order=6)
    private boolean mustBeVisible = true;

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public String getDropFormat() {
        return dropFormat;
    }

    public void setDropFormat(String dropFormat) {
        this.dropFormat = dropFormat;
    }

    public float getMaxRenderDistance() {
        return maxRenderDistance;
    }

    public void setMaxRenderDistance(float maxRenderDistance) {
        this.maxRenderDistance = maxRenderDistance;
    }

    public float getMinRenderDistance() {
        return minRenderDistance;
    }

    public void setMinRenderDistance(float minRenderDistance) {
        this.minRenderDistance = minRenderDistance;
    }

    public boolean isOverwriteDisplayName() {
        return overwriteDisplayName;
    }

    public void setOverwriteDisplayName(boolean overwriteDisplayName) {
        this.overwriteDisplayName = overwriteDisplayName;
    }

    public boolean isMustBeVisible() {
        return mustBeVisible;
    }

    public void setMustBeVisible(boolean mustBeVisible) {
        this.mustBeVisible = mustBeVisible;
    }

    @Override
    public String toString() {
        return "DropNamesConfig{" +
                "dropFormat='" + dropFormat + '\'' +
                ", maxRenderDistance=" + maxRenderDistance +
                ", minRenderDistance=" + minRenderDistance +
                ", overwriteDisplayName=" + overwriteDisplayName +
                ", mustBeVisible=" + mustBeVisible +
                '}';
    }

    @Override
    public boolean isProxyDependent() {
        return false;
    }
}
