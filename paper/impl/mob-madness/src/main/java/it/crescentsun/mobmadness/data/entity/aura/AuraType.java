package it.crescentsun.mobmadness.data.entity.aura;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public enum AuraType {

    VEILED(Component.text("Veiled"), true, true, true), // Gold heaerts
    GHOSTED(Component.text("Ghosted"), true, true, false), // Goes through entities, can't take damage from entities
    SLOWED(Component.text("Slowed"), false, true, true), // Slowed movement speed
    MARKED(Component.text("Marked"), false, true, true), // Takes extra damage from arrows
    GROUNDED(Component.text("Grounded"), false, true, true), // Can't jump or move
    FROZEN(Component.text("Frozen"), false, true, true); // Entity is stuck in ice

    final TextComponent auraName;
    final boolean buff;
    final boolean hasEffectOnPlayers;
    final boolean hasEffectOnMobs;

    AuraType(TextComponent auraName, boolean buff, boolean hasEffectOnPlayers, boolean hasEffectOnMobs) {
        this.auraName = auraName;
        this.buff = buff;
        this.hasEffectOnPlayers = hasEffectOnPlayers;
        this.hasEffectOnMobs = hasEffectOnMobs;
    }

    public TextComponent getAuraName() {
        return auraName;
    }

    public boolean isBuff() {
        return buff;
    }

    public boolean isHasEffectOnPlayers() {
        return hasEffectOnPlayers;
    }

    public boolean isHasEffectOnMobs() {
        return hasEffectOnMobs;
    }
}
