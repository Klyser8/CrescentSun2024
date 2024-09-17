package it.crescentsun.artifacts;

import it.crescentsun.artifacts.command.ArtifactCommands;
import it.crescentsun.artifacts.item.Artifact;
import it.crescentsun.artifacts.item.AsyncArtifact;
import it.crescentsun.artifacts.listener.ArtifactListener;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.ArtifactsProvider;
import it.crescentsun.crescentcore.cmd.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static it.crescentsun.artifacts.registry.ArtifactRegistry.ARTIFACTS;

public class Artifacts extends JavaPlugin implements ArtifactsProvider {

    private static Artifacts instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().setLevel(Level.FINEST);
        Bukkit.getPluginManager().registerEvents(new ArtifactListener(this), this);
        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerArgument(Artifact.class, (commandSender, argument) -> {
            NamespacedKey key = NamespacedKey.fromString(argument);
            if (key == null) {
                return null;
            }
            return getArtifact(key);
        });
        // Register argument
        commandManager.registerSuggestion(Artifact.class, (commandSender, suggestionContext) -> {
            List<String> suggestions = new ArrayList<>();
            for (Artifact artifact : ARTIFACTS.values()) {
                suggestions.add(artifact.namespacedKey().toString());
            }
            return suggestions;
        });
        commandManager.registerCommand(new ArtifactCommands(this));
    }

    public NamespacedKey id(String id) {
        return new NamespacedKey(this, id);
    }

    public CrescentCore crescentCore() {
        return CrescentCore.getInstance();
    }

    public static Artifacts getInstance() {
        return instance;
    }

    public static Artifact getArtifact(NamespacedKey key) {
        return ARTIFACTS.get(key);
    }

    @Override
    public ItemStack getItem(NamespacedKey namespacedKey, int amount) {
        Artifact artifact = Artifacts.getArtifact(namespacedKey);
        return artifact.getItem(amount);
    }

    @Override
    public CompletableFuture<ItemStack> getItemAsync(NamespacedKey namespacedKey, int amount) {
        Artifact artifact = Artifacts.getArtifact(namespacedKey);
        if (artifact instanceof AsyncArtifact asyncArtifact) {
            return asyncArtifact.getItemAsync(amount);
        }
        return CompletableFuture.supplyAsync(() -> getItem(namespacedKey, amount));
    }

    @Override
    public ItemStack getItem(NamespacedKey namespacedKey) {
        return getItem(namespacedKey, 1);
    }
}
