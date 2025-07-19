package it.crescentsun.mobmadness.data.entity;

import it.crescentsun.mobmadness.game.Tier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.EntityType;

public enum BeastType {

    SKELETON(EntityType.SKELETON, Component.text("Skeleton"), Tier.I.getTierColor(), 7.0, 2, 0.22, 1),
    CREEPER(EntityType.CREEPER, Component.text("Creeper"), Tier.I.getTierColor(), 9.0, 7, 0.21, 2),
    SPIDER(EntityType.SPIDER, Component.text("Spider"), Tier.I.getTierColor(), 12.0, 2, 0.23, 1),
    ZOMBIE(EntityType.ZOMBIE, Component.text("Zombie"), Tier.I.getTierColor(), 15.0, 3, 0.2, 1);


    final EntityType entityType;
    final TextComponent beastName;
    final TextColor nameColor;
    final double hp;
    final double dmg;
    final double speed;
    final int weight;

    BeastType(EntityType entityType, TextComponent beastName, TextColor nameColor, double hp, double dmg, double speed, int weight) {
        this.entityType = entityType;
        this.beastName = beastName;
        this.nameColor = nameColor;
        this.hp = hp;
        this.dmg = dmg;
        this.speed = speed;
        this.weight = weight;
    }
}
