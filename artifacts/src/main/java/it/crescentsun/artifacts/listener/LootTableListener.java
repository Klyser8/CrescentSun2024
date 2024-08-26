package it.crescentsun.artifacts.listener;

import it.crescentsun.artifacts.Artifacts;
import org.bukkit.event.Listener;

public class LootTableListener implements Listener {
    private final Artifacts plugin;
    public LootTableListener(Artifacts plugin) {
        this.plugin = plugin;
    }
}
