package it.crescentsun.api.artifacts.item.tooltip;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a page in a tooltip, which can contain multiple sections and an optional pagination hint.
 */
public class TooltipPage {

    private final List<TooltipSection> sections = new ArrayList<>();
    private String paginationHintText;

    protected TooltipPage() {
    }

    /**
     * Adds a section to the tooltip page.
     *
     * @param section the section to add
     */
    public void addSection(TooltipSection section) {
        sections.add(section);
    }

    public List<TooltipSection> getSections() {
        return sections;
    }

    /**
     * Assembles the lore components for the tooltip page, including sections and the pagination hint.
     *
     * @param miniMessage the MiniMessage instance used to deserialize text
     * @return a list of components representing the lore
     */
    public List<Component> assembleLore(MiniMessage miniMessage) {
        List<Component> lore = new ArrayList<>();
        for (TooltipSection section : sections) {
            lore.add(section.getHeader(miniMessage));
            lore.addAll(section.getContent(miniMessage));
        }
        if (paginationHintText != null) {
            lore.add(Component.empty()); // Empty line before hint
            lore.add(miniMessage.deserialize(paginationHintText));
        }
        // Count lines, if over limit then throw warning
        if (lore.size() > Tooltip.MAX_PAGE_LINES) {
            CrescentPlugin.logger().warning("Created a tooltip page with more than  " + Tooltip.MAX_PAGE_LINES + " lines.");
        }
        return lore;
    }
}