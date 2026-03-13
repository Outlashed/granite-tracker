package com.github.outlashed.granitetracker;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.github.outlashed.granitetracker.overlays.GeneralStatisticsOverlay;
import com.github.outlashed.granitetracker.overlays.GraniteOverlay;
import com.github.outlashed.granitetracker.services.GraniteTrackingService;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Granite Tracker",
        description = "Tracks granite ore distribution and mining statistics for 3T4G",
        tags = {"granite", "mining", "tracker", "3t4g"}
)
public class GraniteTrackerPlugin extends Plugin
{
    private static final BufferedImage PANEL_ICON = createPanelIcon();

    @Inject
    private GraniteTrackerConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private GeneralStatisticsOverlay generalStatisticsOverlay;

    @Inject
    private GraniteOverlay graniteOverlay;

    @Inject
    private GraniteTrackingService graniteTrackingService;

    @Inject
    private GraniteTrackerPanel panel;

    private NavigationButton navButton;

    @Provides
    GraniteTrackerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(GraniteTrackerConfig.class);
    }

    @Override
    protected void startUp()
    {
        graniteTrackingService.startUp();

        // Only register overlays that are enabled — avoids showing disabled overlays on login.
        if (config.showGeneralStats())
        {
            overlayManager.add(generalStatisticsOverlay);
        }
        if (config.showGraniteOverlay())
        {
            overlayManager.add(graniteOverlay);
        }

        navButton = NavigationButton.builder()
                .tooltip("Granite Tracker")
                .icon(PANEL_ICON)
                .priority(6)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown()
    {
        // Always remove both overlays regardless of toggle state to ensure clean shutdown.
        overlayManager.remove(generalStatisticsOverlay);
        overlayManager.remove(graniteOverlay);
        graniteTrackingService.shutDown();
        clientToolbar.removeNavigation(navButton);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        // Ignore config changes from unrelated plugins.
        if (!event.getGroup().equals("granitetracker"))
        {
            return;
        }

        // Sync overlay visibility whenever the toggle config items change.
        switch (event.getKey())
        {
            case "showGeneralStats":
                if (config.showGeneralStats())
                {
                    overlayManager.add(generalStatisticsOverlay);
                }
                else
                {
                    overlayManager.remove(generalStatisticsOverlay);
                }
                break;
            case "showGraniteOverlay":
                if (config.showGraniteOverlay())
                {
                    overlayManager.add(graniteOverlay);
                }
                else
                {
                    overlayManager.remove(graniteOverlay);
                }
                break;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        graniteTrackingService.onChatMessage(event);
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        graniteTrackingService.onStatChanged(event);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        graniteTrackingService.onVarbitChanged(event);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        graniteTrackingService.onGameTick(event);
    }

    /** Creates a simple granite-coloured oval as the sidebar navigation icon. */
    private static BufferedImage createPanelIcon()
    {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(162, 146, 116));
        g.fillOval(1, 1, 14, 14);
        g.setColor(new Color(100, 88, 68));
        g.drawOval(1, 1, 14, 14);
        g.dispose();
        return image;
    }
}
