package it.klynet.crystals;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigProvider {

    private int nonOwnedCrystalPickupDelay = 100;

    private final Crystals plugin;
    private final FileConfiguration config;

    public ConfigProvider(Crystals plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void loadFromConfig() {
        nonOwnedCrystalPickupDelay = config.getInt("non_owned_crystal_pickup_delay", nonOwnedCrystalPickupDelay);
    }

    public int getNonOwnedCrystalPickupDelay() {
        return nonOwnedCrystalPickupDelay;
    }

}
