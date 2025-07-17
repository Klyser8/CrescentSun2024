package it.crescentsun.api.artifacts.item.tooltip;

import java.util.ArrayList;
import java.util.List;

public class TooltipBuilder {
    private final List<TooltipPage> pages = new ArrayList<>();

    public static TooltipBuilder builder() {
        return new TooltipBuilder();
    }

    //Also a builder in case one wants to add a page or more to an existing tooltip
    public static TooltipBuilder builder(Tooltip tooltip) {
        TooltipBuilder builder = new TooltipBuilder();
        builder.pages.addAll(tooltip.getPages());
        return builder;
    }

    public TooltipBuilder addPage(TooltipPage page) {
        pages.add(page);
        return this;
    }

    public TooltipPageBuilder page() {
        return new TooltipPageBuilder(this);
    }

    public Tooltip build() {
        Tooltip tooltip = new Tooltip();
        for (TooltipPage page : pages) {
            tooltip.addPage(page);
        }
        return tooltip;
    }
}
