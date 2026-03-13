package com.github.outlashed.granitetracker.overlays;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import com.github.outlashed.granitetracker.services.GraniteTrackingService;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class GraniteOverlay extends OverlayPanel
{
    private final GraniteTrackingService graniteTrackingService;

    @Inject
    public GraniteOverlay(GraniteTrackingService graniteTrackingService)
    {
        this.graniteTrackingService = graniteTrackingService;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Granite")
                .build());

        int total = graniteTrackingService.getTotalGraniteOres();

        panelComponent.getChildren().add(LineComponent.builder()
                .left("500g")
                .right(formatWithPercent(graniteTrackingService.getCount500g(), total))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("2kg")
                .right(formatWithPercent(graniteTrackingService.getCount2kg(), total))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("5kg")
                .right(formatWithPercent(graniteTrackingService.getCount5kg(), total))
                .build());

        return super.render(graphics);
    }

    private String formatWithPercent(int value, int total)
    {
        if (total == 0)
        {
            return String.valueOf(value);
        }

        double pct = (double) value / total * 100.0;
        return value + " (" + String.format("%.1f", pct) + "%)";
    }
}
