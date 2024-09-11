package it.crescentsun.jumpwarps.warphandling;

import it.crescentsun.crescentcore.api.VectorUtils;
import it.crescentsun.crescentcore.api.data.plugin.AbstractPluginDataManager;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.jumpwarps.JumpWarpData;
import it.crescentsun.jumpwarps.JumpWarps;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages all jump warps.
 */
public class JumpWarpManager extends AbstractPluginDataManager<JumpWarps, JumpWarpData> {

    public JumpWarpManager(JumpWarps plugin) {
        super(plugin, JumpWarpData.class);
    }

    /**
     * Creates a new jumpwarp at the player's location.
     *
     * @param creator The player who created the jumpwarp.
     * @param warpName The name of the jumpwarp.
     * @param targetServerName The name of the target server.
     * @return True if the jumpwarp was created successfully, false otherwise.
     */
    public JumpWarpData createJumpWarp(Player creator, String warpName, String targetServerName) {
        if (creator == null
                || warpName == null || warpName.isEmpty()
                || targetServerName == null || targetServerName.isEmpty()) {
            return null;
        }
        JumpWarpData jumpWarp = new JumpWarpData(
                UUID.randomUUID(),
                warpName,
                plugin.getCrescentCore().getServerName(),
                creator.getLocation(),
                targetServerName
        );
        jumpWarp.init();
        jumpWarp.saveAndSync();
        return jumpWarp;
    }

    /**
     * Gets the JumpWarpBlock object found at the specified location, if any.
     *
     * @param location The location to check.
     * @return The JumpWarpBlock object that the player is standing on,
     * or null if no jumpwarp is found.
     */
    @Nullable public JumpWarpData getJumpWarpAtLocation(Location location) {
        // Loop through all JumpWarpData
        for (JumpWarpData jumpWarp : getAllData(true)) {
            if (VectorUtils.isInSameBlockLocation(location, jumpWarp.getLocation())) {
                return jumpWarp;
            }
        }
        return null;
    }

    /**
     * Checks if a jumpwarp with the specified name exists.
     * @param warpName The name of the jumpwarp.
     * @return True if the jumpwarp exists, false otherwise.
     */
    public boolean doesJumpWarpExist(String warpName) {
        return getJumpWarpByName(warpName) != null;
    }

    /**
     * Checks if a server destination exists.
     * @param serverDestination The name of the server destination.
     * @return True if the server destination exists, false otherwise.
     */
    public boolean doesServerDestinationExist(String serverDestination) {
        return plugin.getCrescentCore().getServerList().contains(serverDestination);
    }

    public JumpWarpData getJumpWarpByName(String warpName) {
        for (JumpWarpData jumpWarp : getAllData(false)) {
            if (jumpWarp.getWarpName().equalsIgnoreCase(warpName)) {
                return jumpWarp;
            }
        }
        return null;
    }

    public boolean deleteJumpWarp(String warpName) {
        JumpWarpData jumpWarp = getJumpWarpByName(warpName);
        if (jumpWarp != null) {
            jumpWarp.deleteAndSync();
            return true;
        }
        return false;
    }
}
