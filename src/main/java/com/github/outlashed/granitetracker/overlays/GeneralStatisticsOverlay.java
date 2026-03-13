package com.github.outlashed.granitetracker.overlays;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import com.github.outlashed.granitetracker.services.GraniteTrackingService;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class GeneralStatisticsOverlay extends OverlayPanel
{
    private final GraniteTrackingService graniteTrackingService;

    @Inject
    public GeneralStatisticsOverlay(GraniteTrackingService graniteTrackingService)
    {
        this.graniteTrackingService = graniteTrackingService;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("General Statistics")
                .build());

        int attempts = graniteTrackingService.getAttempts();
        int successes = graniteTrackingService.getSuccesses();
        int failures = graniteTrackingService.getFailures();

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Attempts")
                .right(String.valueOf(attempts))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Successes")
                .right(formatWithPercent(successes, attempts))
                .rightColor(Color.GREEN)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Failed")
                .right(formatWithPercent(failures, attempts))
                .rightColor(Color.RED)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Varrock Armor")
                .right(formatWithPercent(graniteTrackingService.getVarrockArmorCount(), successes))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Celestial Ring")
                .right(formatWithPercent(graniteTrackingService.getCelestialRingCount(), successes))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Mining Cape")
                .right(formatWithPercent(graniteTrackingService.getMiningCapeCount(), successes))
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
