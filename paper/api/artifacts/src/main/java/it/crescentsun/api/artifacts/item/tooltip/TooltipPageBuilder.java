package it.crescentsun.api.artifacts.item.tooltip;

import java.util.ArrayList;
import java.util.List;

public class TooltipPageBuilder {
    private final TooltipBuilder parentBuilder;
    private final List<TooltipSection> sections = new ArrayList<>();
    private String paginationHintText;

    public TooltipPageBuilder(TooltipBuilder parentBuilder) {
        this.parentBuilder = parentBuilder;
    }

    public TooltipPageBuilder addSection(TooltipSection section) {
        sections.add(section);
        return this;
    }

    public TooltipSectionBuilder section(String headerText) {
        return new TooltipSectionBuilder(this, headerText);
    }

    public TooltipBuilder endPage() {
        TooltipPage page = new TooltipPage();
        for (TooltipSection section : sections) {
            page.addSection(section);
        }
        parentBuilder.addPage(page);
        return parentBuilder;
    }
}
