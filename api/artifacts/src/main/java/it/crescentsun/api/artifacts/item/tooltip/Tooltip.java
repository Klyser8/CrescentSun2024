package it.crescentsun.api.artifacts.item.tooltip;

import it.crescentsun.api.crescentcore.CrescentPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tooltip that can contain multiple pages.
 */
public class Tooltip {

    public static final int MAX_LINE_LENGTH = 30;
    public static final int MAX_PAGE_LINES = 16;
    public static final int MAX_PAGES = 3;

    private final List<TooltipPage> pages = new ArrayList<>();

    protected Tooltip() {
    }

    /**
     * Adds a page to the tooltip.
     *
     * @param page the page to add
     */
    public void addPage(TooltipPage page) {
        if (pages.size() >= MAX_PAGES) {
            CrescentPlugin.logger().warning("Attempted to add more than " + MAX_PAGES + " pages to a tooltip.");
            return;
        }
        pages.add(page);
    }

    /**
     * Returns the list of pages in the tooltip.
     *
     * @return the list of pages
     */
    public List<TooltipPage> getPages() {
        return pages;
    }

    /**
     * Creates a formatted header string for the tooltip. This will then be parsed by the MiniMessage library.
     *
     * @param headerText the text of the header
     * @param totalLength the total length of the header line
     * @param leftDashFormat the format for the left dashes
     * @param headerFormat the format for the header text
     * @param rightDashFormat the format for the right dashes
     * @return the formatted header string
     */
    public static String createHeader(String headerText, String leftDashFormat, String headerFormat, String rightDashFormat) {
        String header = headerText.toUpperCase();
        int headerLength = header.length();

        int leftDashLength = (MAX_LINE_LENGTH - headerLength - 2) / 2;
        int rightDashLength = MAX_LINE_LENGTH - leftDashLength - headerLength - 2; // Adjust for odd lengths

        String leftDashes = "-".repeat(Math.max(0, leftDashLength));
        String rightDashes = "-".repeat(Math.max(0, rightDashLength));

        // Apply formatting to each part
        String formattedLeftDashes = leftDashFormat + leftDashes + getClosingTag(leftDashFormat);
        String formattedHeader = headerFormat + header + getClosingTag(headerFormat);
        String formattedRightDashes = rightDashFormat + rightDashes + getClosingTag(rightDashFormat);

        // Construct the line with spaces
        String finalHeader = formattedLeftDashes + " " + formattedHeader + " " + formattedRightDashes;
        if (finalHeader.length() < MAX_LINE_LENGTH) {
            CrescentPlugin.logger().warning("Created a tooltip header with length more than " + MAX_LINE_LENGTH + " characters: " + finalHeader);
        }
        return finalHeader;
    }

    public static String createHeader(String headerText, String format) {
        return createHeader(headerText, format, format, format);
    }

    /**
     * Returns the closing tag for a given opening tag.
     *
     * @param openingTag the opening tag
     * @return the closing tag
     */
    private static String getClosingTag(String openingTag) {
        int tagStart = openingTag.indexOf('<');
        int tagEnd = openingTag.indexOf('>');
        if (tagStart != -1 && tagEnd != -1) {
            String tagContent = openingTag.substring(tagStart + 1, tagEnd);
            // Handle cases like <gradient:color1:color2>
            int colonIndex = tagContent.indexOf(':');
            String tagName = (colonIndex != -1) ? tagContent.substring(0, colonIndex) : tagContent;
            return "</" + tagName + ">";
        }
        return "";
    }
}