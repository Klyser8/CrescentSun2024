package it.crescentsun.artifacts;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import it.crescentsun.api.artifacts.ArtifactRegistryService;
import it.crescentsun.api.artifacts.ArtifactsAPI;
import it.crescentsun.api.artifacts.item.Artifact;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.artifacts.command.ArtifactCommands;
import it.crescentsun.artifacts.event.ArtifactRegistrationEvent;
import it.crescentsun.artifacts.listener.ArtifactListener;
import it.crescentsun.artifacts.registry.ArtifactRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;

import javax.management.ServiceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Artifacts extends CrescentPlugin implements ArtifactsAPI {

    private ArtifactRegistryService artifactRegistryService;
    private ArtifactsAPI artifactsAPI;

    @Override
    public void onEnable() {
        initServices();
        Bukkit.getPluginManager().registerEvents(new ArtifactListener(this), this);
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerArgument(Artifact.class, (commandSender, argument) -> {
            NamespacedKey key = NamespacedKey.fromString(argument);
            if (key == null) {
                return null;
            }
            return artifactRegistryService.getArtifact(key);
        });
        // Register argument
        commandManager.registerSuggestion(Artifact.class, (commandSender, suggestionContext) -> {
            List<String> suggestions = new ArrayList<>();
            for (Artifact artifact : artifactRegistryService.getRegisteredArtifacts()) {
                suggestions.add(artifact.namespacedKey().toString());
            }
            return suggestions;
        });
        commandManager.registerCommand(new ArtifactCommands(this));
    }

    @Override
    public void onDataLoad() {
        ArtifactRegistrationEvent event = new ArtifactRegistrationEvent(getArtifactRegistryService());
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public ItemStack createArtifactStack(NamespacedKey namespacedKey, int amount) {
        Artifact artifact = artifactRegistryService.getArtifact(namespacedKey);
        return artifact.createStack(amount);
    }

    @Override
    protected void initServices() {
        super.initServices();
        artifactRegistryService = new ArtifactRegistry();
        artifactsAPI = this;
        serviceManager.register(ArtifactRegistryService.class, artifactRegistryService, this, ServicePriority.Normal);
        serviceManager.register(ArtifactsAPI.class, artifactsAPI, this, ServicePriority.Normal);
    }

    public ArtifactRegistryService getArtifactRegistryService() {
        return artifactRegistryService;
    }
}
