package it.crescentsun.mobmadness.data.entity;

import it.crescentsun.mobmadness.data.entity.aura.Aura;
import it.crescentsun.mobmadness.data.entity.aura.AuraType;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/**
 * Represents an entity that can hold an aura.
 * Entities implementing this interface can have various auras applied to them.
 * Auras are buffs and debuffs, in other words.
 */
public interface AuraHolder {

    /**
     * Gets a map containing the current aura types and their respective Aura object.
     * @return a map containing the current auras.
     */
    Map<AuraType, Aura> getCurrentAuras();

    /**
     * Method used to return an instance of the entity which the aura is attached to.
     * @return the entity which the aura is applied to
     */
    LivingEntity getLivingEntity();

}
