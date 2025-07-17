package it.crescentsun.api.artifacts.item.tooltip;

import java.util.ArrayList;
import java.util.List;

public class TooltipSectionBuilder {
    private final TooltipPageBuilder parentBuilder;
    private final String headerText;
    private final List<String> contentTexts = new ArrayList<>();

    public TooltipSectionBuilder(TooltipPageBuilder parentBuilder, String headerText) {
        this.parentBuilder = parentBuilder;
        this.headerText = headerText;
    }

    public TooltipSectionBuilder addLine(String line) {
        contentTexts.add(line);
        return this;
    }

    public TooltipSectionBuilder addLines(List<String> lines) {
        contentTexts.addAll(lines);
        return this;
    }

    public TooltipPageBuilder endSection() {
        TooltipSection section = new TooltipSection(headerText);
        for (String line : contentTexts) {
            section.addContentLine(line);
        }
        parentBuilder.addSection(section);
        return parentBuilder;
    }
}
