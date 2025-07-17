package it.crescentsun.api.artifacts.item.tooltip;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the style configuration for a tooltip, including various color codes for different parts of the tooltip.
 */
public class TooltipStyle {

    public static final TooltipStyle DEFAULT = new TooltipStyle(
            TextColor.color(0xe0d68a).asHexString(),
            TextColor.color(0xcb9173).asHexString(),
            TextColor.color(0x8e443d).asHexString(),

            TextColor.color(0xedd5bf).asHexString(),
            TextColor.color(0x8d86c9).asHexString(),
            TextColor.color(0xcac4ce).asHexString(),

            TextColor.color(0xadeee3).asHexString(),
            TextColor.color(0x86deb7).asHexString(),
            TextColor.color(0x56b38c).asHexString(),

            TextColor.color(0x460b99).asHexString()
    );

    private final String headerHex1;
    private final String headerHex2;
    private final String headerHex3;
    private final String primaryHex1;
    private final String primaryHex2;
    private final String primaryHex3;
    private final String secondaryHex1;
    private final String secondaryHex2;
    private final String secondaryHex3;
    private final String tertiaryHex;

    /**
     * Constructs a new TooltipStyle with the specified color codes.
     *
     * @param headerHex1 the first color code for the header
     * @param headerHex2 the second color code for the header
     * @param attributeHex the color code for attributes
     * @param flavorTextHex the color code for flavor text
     * @param actionsHex the color code for actions
     * @param actionDescriptionsHex the color code for action descriptions
     * @param paginationHex the color code for pagination
     */
    public TooltipStyle(String headerHex1, String primaryHex1, String secondaryHex1, String headerHex2, String primaryHex2,
                        String secondaryHex2, String headerHex3, String primaryHex3, String secondaryHex3, String tertiaryHex) {
        this.headerHex1 = headerHex1;
        this.primaryHex1 = primaryHex1;
        this.secondaryHex1 = secondaryHex1;

        this.headerHex2 = headerHex2;
        this.primaryHex2 = primaryHex2;
        this.secondaryHex2 = secondaryHex2;

        this.headerHex3 = headerHex3;
        this.primaryHex3 = primaryHex3;
        this.secondaryHex3 = secondaryHex3;

        this.tertiaryHex = tertiaryHex;
    }

/**
 * Returns the first color code for the header.
 *
 * @return the first color code for the header
 */
public String getHeaderHex1() {
    return "<" + headerHex1 + ">";
}

/**
 * Returns the second color code for the header.
 *
 * @return the second color code for the header
 */
public String getHeaderHex2() {
    return "<" + headerHex2 + ">";
}

/**
 * Returns the third color code for the header.
 *
 * @return the third color code for the header
 */
public String getHeaderHex3() {
    return "<" + headerHex3 + ">";
}

/**
 * Returns the first primary color code.
 *
 * @return the first primary color code
 */
public String getPrimaryHex1() {
    return "<" + primaryHex1 + ">";
}

/**
 * Returns the second primary color code.
 *
 * @return the second primary color code
 */
public String getPrimaryHex2() {
    return "<" + primaryHex2 + ">";
}

/**
 * Returns the third primary color code.
 *
 * @return the third primary color code
 */
public String getPrimaryHex3() {
    return "<" + primaryHex3 + ">";
}

/**
 * Returns the first secondary color code.
 *
 * @return the first secondary color code
 */
public String getSecondaryHex1() {
    return "<" + secondaryHex1 + ">";
}

/**
 * Returns the second secondary color code.
 *
 * @return the second secondary color code
 */
public String getSecondaryHex2() {
    return "<" + secondaryHex2 + ">";
}

/**
 * Returns the third secondary color code.
 *
 * @return the third secondary color code
 */
public String getSecondaryHex3() {
    return "<" + secondaryHex3 + ">";
}

/**
 * Returns the tertiary color code.
 *
 * @return the tertiary color code
 */
public String getTertiaryHex() {
    return "<" + tertiaryHex + ">";
}

    /**
     * Disables italic text decoration for the given component.
     *
     * @param component the component to process
     * @return the processed component with italic decoration disabled
     */
    public static Component disableItalic(Component component) {
        // Check if the italic decoration is not set
        if (!component.style().hasDecoration(TextDecoration.ITALIC)) {
            // Set italic to false
            component = component.decoration(TextDecoration.ITALIC, false);
        }

        // Recursively process child components
        List<Component> children = component.children().stream()
                .map(TooltipStyle::disableItalic)
                .collect(Collectors.toList());

        return component.children(children);
    }

}