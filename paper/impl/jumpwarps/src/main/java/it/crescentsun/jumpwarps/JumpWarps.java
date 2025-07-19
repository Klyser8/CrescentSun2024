package it.crescentsun.jumpwarps;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.crescentsun.api.crescentcore.CrescentCoreAPI;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.data.player.PlayerDataService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataService;
import it.crescentsun.jumpwarps.lang.JumpWarpLocalization;
import it.crescentsun.jumpwarps.warphandling.JumpListener;
import it.crescentsun.jumpwarps.warphandling.JumpWarpData;
import it.crescentsun.jumpwarps.warphandling.JumpWarpManager;
import it.crescentsun.triumphcmd.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import javax.management.ServiceNotFoundException;

public final class JumpWarps extends CrescentPlugin {

    private JumpWarpManager jumpWarpManager;
    private static JumpWarps instance;
    // Additional data keys

    @Override
    public void onEnable() {
        instance = this;
        initServices();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        BukkitCommandManager<CommandSender> manager = BukkitCommandManager.create(this);
        manager.registerCommand(new JumpWarpCommands(this));
        Bukkit.getPluginManager().registerEvents(new JumpListener(this), this);
        jumpWarpManager = new JumpWarpManager(this, pluginDataService);

        JumpWarpLocalization jumpWarpLocalization = new JumpWarpLocalization();
        jumpWarpLocalization.registerEnglishTranslations();
        jumpWarpLocalization.registerItalianTranslations();
        getLogger().info("[JumpWarps] JumpWarps has been enabled!");
    }

    @Override
    public void onPluginDataRegister(PluginDataRegistryService service) {
        service.registerDataClass(this, JumpWarpData.class);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    protected void initServices() {
        try {
            playerDataService = getServiceProvider(PlayerDataService.class);
            pluginDataService = getServiceProvider(PluginDataService.class);
            crescentCoreAPI = getServiceProvider(CrescentCoreAPI.class);
        } catch (ServiceNotFoundException e) {
            getLogger().severe("Service initialization failed: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public JumpWarpManager getJumpWarpManager() {
        return jumpWarpManager;
    }
    public static JumpWarps getInstance() {
        return instance;
    }

    // Rewrite above, with new Location() instead. These will be used as offsets,
    private static Vector3i[] goldBlockOffsets = new Vector3i[] {
            new Vector3i(-2, -1, -2),
            new Vector3i(-2, -1, 2),
            new Vector3i(2, -1, -2),
            new Vector3i(2, -1, 2),
            new Vector3i(-1, -1, -1),
            new Vector3i(-1, -1, 1),
            new Vector3i(1, -1, -1),
            new Vector3i(1, -1, 1)
    };

    /**
     * Check if the structure of a jumpwarp is valid.
     * The structure, from the top-down view, should look like this: <br>
     * - G = Gold block <br>
     * - A = Anything <br>
     * - P = Gold pressure plate - one block higher than the rest <br>
     * G A A A G <br>
     * A G A G A <br>
     * A A P A A <br>
     * A G A G A <br>
     * G A A A G <br>
     *
     * @param location The location of the jumpwarp AKA the gold pressure plate
     * @return Whether the structure is valid or not.
     */
    public static boolean isJumpWarpStructureValid(@NotNull  Location location) {
        //Check whether the block at the current location is of type gold pressure plate
        if (!location.getBlock().getType().equals(Material.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
            return false;
        }
        // Check that there are gold blocks at offsets matching the grid above
        for (Vector3i offset : goldBlockOffsets) {
            Location loc = location.clone().offset(offset.x, offset.y, offset.z).toLocation(location.getWorld());
            if (!loc.getBlock().getType().equals(Material.GOLD_BLOCK)) {
                return false;
            }
        }
        return true;
    }
}
