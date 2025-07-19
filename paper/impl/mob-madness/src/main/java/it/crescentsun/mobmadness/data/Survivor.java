package it.crescentsun.mobmadness.data;

import it.crescentsun.mobmadness.data.entity.AuraHolder;
import it.crescentsun.mobmadness.data.entity.aura.Aura;
import it.crescentsun.mobmadness.data.entity.aura.AuraType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Survivor implements AuraHolder {

    private final Player player;
    private int score;
    private final Map<AuraType, Aura> currentAuras;

    public Survivor(Player player) {
        this.player = player;
        currentAuras = new HashMap<>();
    }

    public Player getPlayer() {
        return player;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    @Override
    public Map<AuraType, Aura> getCurrentAuras() {
        return null;
    }

    @Override
    public LivingEntity getLivingEntity() {
        return player;
    }
}
