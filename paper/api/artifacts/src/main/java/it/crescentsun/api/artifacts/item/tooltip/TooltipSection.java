package it.crescentsun.api.artifacts.item.tooltip;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a section of a tooltip, including a header and multiple content lines.
 */
public class TooltipSection {
    private final String headerText;
    private final List<String> contentTexts = new ArrayList<>();

    /**
     * Constructs a new TooltipSection with the specified header text.
     *
     * @param headerText the text for the header of this section
     */
    protected TooltipSection(String headerText) {
        this.headerText = headerText;
    }

    /**
     * Adds a line of content to this section.
     *
     * @param text the content line to add
     */
    public void addContentLine(String text) {
        contentTexts.add(text);
    }

    /**
     * Returns the header component of this section.
     *
     * @param miniMessage the MiniMessage instance used to deserialize the header text
     * @return the header component
     */
    public Component getHeader(MiniMessage miniMessage) {
        return miniMessage.deserialize(headerText);
    }

    /**
     * Returns the content components of this section.
     *
     * @param miniMessage the MiniMessage instance used to deserialize the content texts
     * @return a list of content components
     */
    public List<Component> getContent(MiniMessage miniMessage) {
        List<Component> components = new ArrayList<>();
        for (String text : contentTexts) {
            components.add(miniMessage.deserialize(text));
        }
        return components;
    }
}
