package com.github.outlashed.granitetracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("granitetracker")
public interface GraniteTrackerConfig extends Config
{
    @ConfigItem(
            keyName = "showGeneralStats",
            name = "Show General Statistics",
            description = "Toggles the General Statistics overlay"
    )
    default boolean showGeneralStats()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showGraniteOverlay",
            name = "Show Granite Distribution",
            description = "Toggles the Granite Distribution overlay"
    )
    default boolean showGraniteOverlay()
    {
        return true;
    }
}
