package it.crescentsun.jumpwarps.warphandling;

import it.crescentsun.crescentcore.api.VectorUtils;
import it.crescentsun.jumpwarps.JumpWarps;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all jump warps.
 */
public class JumpWarpManager {
    private final JumpWarps plugin;
    private final Map<String, JumpWarpBlock> jumpWarps;

    public JumpWarpManager(JumpWarps plugin) {
        this.plugin = plugin;
        jumpWarps = new HashMap<>();
    }

    /**
     * Creates a new jumpwarp at the player's location.
     *
     * @param creator The player who created the jumpwarp.
     * @param warpName The name of the jumpwarp.
     * @param targetServerName The name of the target server.
     * @return True if the jumpwarp was created successfully, false otherwise.
     */
    public boolean createJumpWarp(Player creator, String warpName, String targetServerName) {
        if (creator == null
                || warpName == null || warpName.isEmpty()
                || targetServerName == null || targetServerName.isEmpty()) {
            return false;
        }
        JumpWarpBlock jumpWarpBlock = new JumpWarpBlock(plugin, warpName, creator.getLocation().toBlockLocation(), targetServerName);
        jumpWarps.put(warpName, jumpWarpBlock);
        plugin.getConfig().set(warpName + ".World", creator.getWorld().getName());
        plugin.getConfig().set(warpName + ".X", jumpWarpBlock.getX());
        plugin.getConfig().set(warpName + ".Y", jumpWarpBlock.getY());
        plugin.getConfig().set(warpName + ".Z", jumpWarpBlock.getZ());
        plugin.getConfig().set(warpName + ".TargetServer", jumpWarpBlock.getTargetServerName());
        plugin.saveConfig();
        return true;
    }

    /**
     * Removes the specified jumpwarp from the JumpWarps HashMap and the config file.
     * @param warpName The name of the jumpwarp to remove.
     */
    public void deleteJumpWarp(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return;
        }
        jumpWarps.remove(warpName);
        plugin.getConfig().set(warpName, null);
        plugin.saveConfig();
    }

    /**
     * Loads all jump warps from the config file.
     */
    public void loadJumpWarps() {
        jumpWarps.clear();
        for (String warpName : plugin.getConfig().getKeys(false)) {
            String worldName = plugin.getConfig().getString(warpName + ".World");
            int x = plugin.getConfig().getInt(warpName + ".X");
            int y = plugin.getConfig().getInt(warpName + ".Y");
            int z = plugin.getConfig().getInt(warpName + ".Z");
            String targetServerName = plugin.getConfig().getString(warpName + ".TargetServer");
            JumpWarpBlock jumpWarpBlock = null;
            if (worldName != null) {
                jumpWarpBlock = new JumpWarpBlock(plugin, warpName,
                         new Location(plugin.getServer().getWorld(worldName), x, y, z).toBlockLocation(),
                        targetServerName);
            }
            jumpWarps.put(warpName, jumpWarpBlock);
        }
    }

    /**
     * Gets the JumpWarpBlock object with the specified name.
     *
     * @param warpName The name of the jumpwarp to get.
     * @return The JumpWarpBlock object with the specified name.
     */
    public JumpWarpBlock getJumpWarp(String warpName) {
        return jumpWarps.get(warpName);
    }

    /**
     * Gets the JumpWarps HashMap.
     *
     * @return The JumpWarps HashMap.
     */
    public Map<String, JumpWarpBlock> getJumpWarps() {
        return jumpWarps;
    }

    /**
     * Gets the JumpWarpBlock object found at the specified location, if any.
     *
     * @param location The location to check.
     * @return The JumpWarpBlock object that the player is standing on,
     * or null if no jumpwarp is found.
     */
    public JumpWarpBlock getJumpWarpAtLocation(Location location) {
        for (JumpWarpBlock jumpWarpBlock : getJumpWarps().values()) {
            if (VectorUtils.isInSameBlockLocation(location, jumpWarpBlock.getLocation())) {
                return jumpWarpBlock;
            }
        }
        return null;
    }

}
