package it.crescentsun.crystals;

import it.crescentsun.api.artifacts.ArtifactProvider;
import it.crescentsun.api.artifacts.ArtifactRegistryService;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.artifacts.item.tooltip.TooltipStyle;
import it.crescentsun.api.common.ArtifactNamespacedKeys;
import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentCoreAPI;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.PrematureAccessException;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.player.PlayerDataRegistryService;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataRegistryService;
import it.crescentsun.api.crystals.CrystalSpawnAnimation;
import it.crescentsun.api.crystals.CrystalsAPI;
import it.crescentsun.api.artifacts.item.ArtifactFlag;
import it.crescentsun.api.crystals.CrystalsService;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.artifact.CrystalArtifact;
import it.crescentsun.crystals.data.CrystalsSettings;
import it.crescentsun.crystals.data.CrystalsStatistics;
import it.crescentsun.crystals.sound.CrystalsSFX;
import it.crescentsun.crystals.vault.VaultData;
import it.crescentsun.crystals.vault.VaultListener;
import it.crescentsun.crystals.vault.VaultManager;
import it.crescentsun.triumphcmd.bukkit.BukkitCommandManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;

import javax.management.ServiceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class Crystals extends CrescentPlugin implements CrystalsAPI, ArtifactProvider {

    public static final UUID SETTINGS_UUID = UUID.fromString("051ed6ab-ab7a-40e8-ad5c-8d6e7ab8b175");
    public static final UUID STATISTICS_UUID = UUID.fromString("d6d3909a-0fae-4d1f-a124-f564783256ee");

    private CrystalsStatistics statistics;
    private CrystalsSettings settings;
    private CrystalsService crystalsService;
    private CrystalsAPI crystalsAPI;
    private CrystalsSFX crystalsSFX;

    private VaultManager vaultManager;

    private ArtifactRegistryService artifactRegistryService;

    @Override
    public void onEnable() {
        initServices();
        crystalsSFX = new CrystalsSFX(this);
        vaultManager = new VaultManager(this, pluginDataService);
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new CrystalsCommands(this));

        commandManager.registerArgument(CrystalSpawnAnimation.class, (commandSender, argument) -> {
            try {
                return CrystalSpawnAnimation.valueOf(argument.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
        // Register argument
        commandManager.registerSuggestion(CrystalSpawnAnimation.class, (suggestionContext) -> {
            List<String> suggestions = new ArrayList<>();
            for (CrystalSpawnAnimation animation :CrystalSpawnAnimation.values()) {
                suggestions.add(animation.toString());
            }
            return suggestions;
        });
    }

    @Override
    public void onPlayerDataRegister(PlayerDataRegistryService service) {
        // How many crystals the player has in their vault
        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_CRYSTALS_SPAWNED, DataType.INT,true, 0);
        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT, DataType.INT,true, 0);
        // Whether the player has claimed their crystals from previous advancements. Shall be removed eventually.
        service.registerDataDefinition(DatabaseNamespacedKeys.PLAYER_CRYSTALS_CLAIMED, DataType.BOOLEAN, true, false);
    }

    @Override
    public void onPluginDataRegister(PluginDataRegistryService service) {
        service.registerDataClass(this, CrystalsStatistics.class);
        service.registerDataClass(this, CrystalsSettings.class);
        service.registerDataClass(this, VaultData.class);
    }

    @Override
    protected void initServices() {
        super.initServices();
        crystalsService = new CrystalManager(this);
        crystalsAPI = this;
        Bukkit.getPluginManager().registerEvents(new CrystalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VaultListener(this), this);
        serviceManager.register(CrystalsService.class, crystalsService, this, ServicePriority.Normal);
        serviceManager.register(CrystalsAPI.class, crystalsAPI, this, ServicePriority.Normal);
        try {
            artifactRegistryService = getServiceProvider(ArtifactRegistryService.class);
            crescentCoreAPI = getServiceProvider(CrescentCoreAPI.class);
        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onArtifactRegister(ArtifactRegistryService registryService) {
        registryService.registerArtifact(new CrystalArtifact(
                this,
                ArtifactNamespacedKeys.CRYSTAL,
                new ItemStack(Material.PRISMARINE_CRYSTALS),
                "<gradient:" + CrescentHexCodes.ICE_CITADEL.replace(">", "").replace("<", "")
                        + ":" + CrescentHexCodes.DROPLET.replace(">", "").replace("<", "") + ">Crystal",
                TooltipStyle.DEFAULT,
                ArtifactFlag.WITH_GLINT));
    }

    @Override
    public void onDataLoad() {
        setStatistics(getPluginDataService().getData(CrystalsStatistics.class, Crystals.STATISTICS_UUID));
        setSettings(getPluginDataService().getData(CrystalsSettings.class, Crystals.SETTINGS_UUID));
    }

    public CrystalsStatistics getStatistics() {
        if (statistics == null) {
            throw new PrematureAccessException("CrystalsStatistics object is not initialized yet.");
        }
        return statistics;
    }

    public void setStatistics(CrystalsStatistics statistics) {
        if (this.statistics == null) {
            this.statistics = statistics;
        } else {
            getLogger().warning("Attempted overwriting of CrystalsData object. Ignoring.");
        }
    }

    public CrystalsSettings getSettings() {
        return settings;
    }

    public void setSettings(CrystalsSettings settings) {
        if (this.settings == null) {
            this.settings = settings;
        } else {
            getLogger().warning("Attempted overwriting of CrystalsSettings object. Ignoring.");
        }
    }

    public ArtifactRegistryService getArtifactRegistryService() {
        return artifactRegistryService;
    }

    public CrystalsService getCrystalsService() {
        return crystalsService;
    }

    public CrystalsAPI getCrystalsAPI() {
        return crystalsAPI;
    }

    public CrystalsSFX getCrystalsSFX() {
        return crystalsSFX;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }
}
