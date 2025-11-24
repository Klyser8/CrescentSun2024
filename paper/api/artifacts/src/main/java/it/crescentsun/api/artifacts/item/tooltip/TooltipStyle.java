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
            TextColor.color(0xE0D68A).asHexString(),
            TextColor.color(0xCB9173).asHexString(),
            TextColor.color(0x8E443D).asHexString(),
            TextColor.color(0xD4AFB5).asHexString(),

            TextColor.color(0xEDD1B1).asHexString(),
            TextColor.color(0x8D86C9).asHexString(),
            TextColor.color(0xB99DCE).asHexString(),
            TextColor.color(0xD4D1CD).asHexString(),

            TextColor.color(0xADEEE3).asHexString(),
            TextColor.color(0x86DEB7).asHexString(),
            TextColor.color(0x56B38C).asHexString(),
            TextColor.color(0xA5CBAA).asHexString(),

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
    private final String highlightHex1;
    private final String highlightHex2;
    private final String highlightHex3;
    private final String tertiaryHex;

    /**
     * Constructs a new TooltipStyle with the specified color codes.
     *
     * @param headerHex1 the first color code for the header
     * @param headerHex2 the second color code for the header
    * @param headerHex3 the third color code for the header
    * @param primaryHex1 the first primary color code
    * @param primaryHex2 the second primary color code
    * @param primaryHex3 the third primary color code
    * @param secondaryHex1 the first secondary color code
    * @param secondaryHex2 the second secondary color code
    * @param secondaryHex3 the third secondary color code
     * @param highlightHex1 the first highlight color code - applied to whatever text is between curly braces {}
     * @param highlightHex2 the second highlight color code - applied to whatever text is between curly braces {}
     * @param highlightHex3 the third highlight color code - applied to whatever text is between curly braces {}
    * @param tertiaryHex the tertiary color code
    */
    public TooltipStyle(String headerHex1, String primaryHex1, String secondaryHex1, String highlightHex1,
                        String headerHex2, String primaryHex2, String secondaryHex2, String highlightHex2,
                        String headerHex3, String primaryHex3, String secondaryHex3, String highlightHex3,
                        String tertiaryHex) {
        this.headerHex1 = headerHex1;
        this.primaryHex1 = primaryHex1;
        this.secondaryHex1 = secondaryHex1;

        this.headerHex2 = headerHex2;
        this.primaryHex2 = primaryHex2;
        this.secondaryHex2 = secondaryHex2;

        this.headerHex3 = headerHex3;
        this.primaryHex3 = primaryHex3;
        this.secondaryHex3 = secondaryHex3;

        this.highlightHex1 = highlightHex1;
        this.highlightHex2 = highlightHex2;
        this.highlightHex3 = highlightHex3;


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
     * Returns the first highlight color code.
     *
     * @return the first highlight color code
     */
    public String getHighlightHex1() {
        return "<" + highlightHex1 + ">";
    }

    /**
     * Returns the second highlight color code.
     *
     * @return the second highlight color code
     */
    public String getHighlightHex2() {
        return "<" + highlightHex2 + ">";
    }

    /**
     * Returns the third highlight color code.
     *
     * @return the third highlight color code
     */
    public String getHighlightHex3() {
        return "<" + highlightHex3 + ">";
    }

    /**
     * Applies highlight formatting to any text wrapped in curly braces. The highlight color used will
     * be inferred from the primary/secondary/header color present in the line, falling back to the
     * first highlight color if no base color can be detected.
     *
     * @param text the text to process for highlights
     * @return the processed text with highlight colors applied
     */
    public String applyHighlighting(String text) {
        if (!text.contains("{") || !text.contains("}")) {
            return text;
        }

        String baseColor = findBaseColor(text);
        String highlightColor = findHighlightColor(baseColor);

        if (highlightColor == null) {
            return text;
        }

        String highlightClosingTag = getClosingTag(highlightColor);
        String resetColor = baseColor != null ? baseColor : "";

        return text.replaceAll("\\{([^{}]+)}", highlightColor + "$1" + highlightClosingTag + resetColor);
    }

    private String findBaseColor(String text) {
        String[] orderedColors = new String[]{
                getPrimaryHex1(), getSecondaryHex1(), getHeaderHex1(),
                getPrimaryHex2(), getSecondaryHex2(), getHeaderHex2(),
                getPrimaryHex3(), getSecondaryHex3(), getHeaderHex3()
        };

        for (String color : orderedColors) {
            if (text.contains(color)) {
                return color;
            }
        }

        return null;
    }

    private String findHighlightColor(String baseColor) {
        if (baseColor == null) {
            return getHighlightHex1();
        }

        if (baseColor.equals(getPrimaryHex1()) || baseColor.equals(getSecondaryHex1()) || baseColor.equals(getHeaderHex1())) {
            return getHighlightHex1();
        }
        if (baseColor.equals(getPrimaryHex2()) || baseColor.equals(getSecondaryHex2()) || baseColor.equals(getHeaderHex2())) {
            return getHighlightHex2();
        }
        if (baseColor.equals(getPrimaryHex3()) || baseColor.equals(getSecondaryHex3()) || baseColor.equals(getHeaderHex3())) {
            return getHighlightHex3();
        }

        return null;
    }

    private String getClosingTag(String openingTag) {
        int tagStart = openingTag.indexOf('<');
        int tagEnd = openingTag.indexOf('>');
        if (tagStart != -1 && tagEnd != -1) {
            String tagContent = openingTag.substring(tagStart + 1, tagEnd);
            int colonIndex = tagContent.indexOf(':');
            String tagName = (colonIndex != -1) ? tagContent.substring(0, colonIndex) : tagContent;
            return "</" + tagName + ">";
        }
        return "";
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