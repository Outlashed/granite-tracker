package com.github.outlashed.granitetracker.services;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import com.github.outlashed.granitetracker.model.ProfileData;

@Slf4j
@Singleton
public class GraniteTrackingService
{
    // VarPlayer 4526 = TRACKING_ORE_MINED
    // Increments once per ore mined, including bonus ores.
    // Multiple VarbitChanged events fire per tick when bonus ores occur.
    // Important, since inventory-tracking is useless if the player is using Infernal Pickaxe.
    // Since Infernal Pickaxe consumes the granite before it even enters inventory.
    private static final int TRACKING_ORE_MINED_VARP = 4526;

    // XP ranges per granite type (display values, not internal ×10).
    // OSRS tracks XP with one decimal internally. If an ore's true XP is fractional
    // (e.g. 75.5), StatChanged delivers either floor or ceil depending on accumulated
    // remainder — so two ores of the same type mined in the same tick can give
    // different display XP (e.g. 75+76=151 for 2x5kg).
    //
    // The +10 / +25 differences between ore types are constant across all modifiers.
    // Resolution uses a range check: distribution (a,b,c) is valid if
    //   MIN*a + (MIN+10)*b + (MIN+25)*c  <=  xp  <=  MAX*a + (MAX+10)*b + (MAX+25)*c
    //
    // Known ambiguities for ores=3 (returned as null / unresolved):
    //   xp in [175,176]: overlap between {2,0,1} and {1,2,0}
    //   xp in [185,186]: overlap between {1,1,1} and {0,3,0}
    // These require 2 bonus ores in one tick AND a specific XP value — negligibly rare.
    // As I'm not able to really figure out how to properly solve this, I'm leaving this bug as is.
    private static final int MIN_XP_500G = 50;
    private static final int MAX_XP_500G = 52;
    private static final int XP_DIFF_2KG = 10;
    private static final int XP_DIFF_5KG = 25;

    private static final String SWING_MESSAGE = "You swing your pick at the rock.";
    private static final String SUCCESS_PREFIX = "You manage to quarry some";

    private static final String VARROCK_ARMOR_MESSAGE = "The Varrock platebody enabled you to mine an additional ore.";
    private static final String CELESTIAL_RING_MESSAGE = "Your celestial ring glows and you manage to mine an extra ore.";
    private static final String MINING_CAPE_MESSAGE = "Your cape allows you to mine an additional ore.";

    private final Client client;

    // General statistics
    private int attempts = 0;
    private int successes = 0;

    // Bonus ore counts
    private int varrockArmorCount = 0;
    private int celestialRingCount = 0;
    private int miningCapeCount = 0;

    // Granite distribution
    private int count500g = 0;
    private int count2kg = 0;
    private int count5kg = 0;

    // Per-tick accumulation state
    private int lastMiningXp = -1;
    private int lastOreVarp = -1;
    private boolean oreBaselineInitialized = false;

    private int tickXpDelta = 0;
    private int tickOreDelta = 0;
    private boolean tickSuccess = false;

    @Inject
    public GraniteTrackingService(Client client)
    {
        this.client = client;
    }

    public void startUp()
    {
        reset();
    }

    public void shutDown()
    {
        attempts = 0;
        successes = 0;

        varrockArmorCount = 0;
        celestialRingCount = 0;
        miningCapeCount = 0;

        count500g = 0;
        count2kg = 0;
        count5kg = 0;

        lastMiningXp = -1;
        lastOreVarp = -1;
        oreBaselineInitialized = false;

        tickXpDelta = 0;
        tickOreDelta = 0;
        tickSuccess = false;
    }

    public void reset()
    {
        attempts = 0;
        successes = 0;

        varrockArmorCount = 0;
        celestialRingCount = 0;
        miningCapeCount = 0;

        count500g = 0;
        count2kg = 0;
        count5kg = 0;

        lastMiningXp = client.getSkillExperience(Skill.MINING);
        lastOreVarp = -1;
        oreBaselineInitialized = false;

        tickXpDelta = 0;
        tickOreDelta = 0;
        tickSuccess = false;
    }

    public void onChatMessage(ChatMessage event)
    {
        String message = event.getMessage();

        if (SWING_MESSAGE.equals(message))
        {
            attempts++;
            return;
        }

        if (message.startsWith(SUCCESS_PREFIX))
        {
            successes++;
            tickSuccess = true;
            return;
        }

        if (VARROCK_ARMOR_MESSAGE.equals(message))
        {
            varrockArmorCount++;
            return;
        }

        if (CELESTIAL_RING_MESSAGE.equals(message))
        {
            celestialRingCount++;
            return;
        }

        if (MINING_CAPE_MESSAGE.equals(message))
        {
            miningCapeCount++;
        }
    }

    public void onStatChanged(StatChanged event)
    {
        if (event.getSkill() != Skill.MINING)
        {
            return;
        }

        int currentXp = event.getXp();

        if (lastMiningXp != -1)
        {
            int delta = currentXp - lastMiningXp;

            if (delta > 0)
            {
                tickXpDelta += delta;
            }
        }

        lastMiningXp = currentXp;
    }

    public void onVarbitChanged(VarbitChanged event)
    {
        if (event.getVarpId() != TRACKING_ORE_MINED_VARP)
        {
            return;
        }

        int current = client.getVarpValue(TRACKING_ORE_MINED_VARP);

        if (!oreBaselineInitialized)
        {
            lastOreVarp = current;
            oreBaselineInitialized = true;
            return;
        }

        int delta = current - lastOreVarp;

        if (delta > 0)
        {
            tickOreDelta += delta;
        }

        lastOreVarp = current;
    }

    public void onGameTick(GameTick event)
    {
        // Only process if a success chat message was received this tick and at least
        // one ore was registered via the varp — both conditions must hold to avoid
        // counting spurious XP (e.g. from other mining activities).
        if (tickSuccess && tickOreDelta > 0)
        {
            processRoll(tickXpDelta, tickOreDelta);
        }

        // Always clear per-tick state so stale values never bleed into the next tick.
        tickXpDelta = 0;
        tickOreDelta = 0;
        tickSuccess = false;
    }

    private void processRoll(int xp, int ores)
    {
        int[] result = resolveGranite(xp, ores);

        if (result != null)
        {
            count500g += result[0];
            count2kg += result[1];
            count5kg += result[2];
        }

        if (result != null)
        {
            log.debug("Granite -> 500g: {} | 2kg: {} | 5kg: {}", result[0], result[1], result[2]);
        }
        else
        {
            log.debug("Granite resolution failed -> XP: {} | Ores: {}", xp, ores);
        }
    }

    /**
     * Resolves granite ore distribution from XP delta and ore count.
     *
     * Each ore type has an XP range due to fractional XP rounding in OSRS:
     *   500g: [MIN_XP_500G,          MAX_XP_500G         ]
     *   2kg:  [MIN_XP_500G+10,       MAX_XP_500G+10      ]
     *   5kg:  [MIN_XP_500G+25,       MAX_XP_500G+25      ]
     *
     * Distribution (a, b, c) is valid when the observed xp falls within the
     * achievable range for that distribution:
     *   minXp = MIN*a + (MIN+10)*b + (MIN+25)*c
     *   maxXp = MAX*a + (MAX+10)*b + (MAX+25)*c
     *
     * Returns null if zero or more than one distribution matches (ambiguous).
     *
     * @return int[3] = {count500g, count2kg, count5kg} or null if unresolvable
     */
    private int[] resolveGranite(int xp, int ores)
    {
        int[] solution = null;
        int solutionCount = 0;

        for (int c = ores; c >= 0; c--)
        {
            for (int b = ores - c; b >= 0; b--)
            {
                int a = ores - b - c;
                int minXp = MIN_XP_500G * a + (MIN_XP_500G + XP_DIFF_2KG) * b + (MIN_XP_500G + XP_DIFF_5KG) * c;
                int maxXp = MAX_XP_500G * a + (MAX_XP_500G + XP_DIFF_2KG) * b + (MAX_XP_500G + XP_DIFF_5KG) * c;

                if (xp >= minXp && xp <= maxXp)
                {
                    solution = new int[]{a, b, c};
                    solutionCount++;
                }
            }
        }

        return solutionCount == 1 ? solution : null;
    }

    /**
     * Loads saved statistics from a profile snapshot.
     * The XP and varp baselines are intentionally preserved so tracking continues
     * seamlessly from the current game state after the load.
     */
    public void loadFromProfile(ProfileData data)
    {
        attempts = data.attempts;
        successes = data.successes;
        varrockArmorCount = data.varrockArmorCount;
        celestialRingCount = data.celestialRingCount;
        miningCapeCount = data.miningCapeCount;
        count500g = data.count500g;
        count2kg = data.count2kg;
        count5kg = data.count5kg;

        // Reset tick accumulation — don't touch XP/varp baseline (keep tracking from now)
        tickXpDelta = 0;
        tickOreDelta = 0;
        tickSuccess = false;
    }

    // --- Getters: General statistics ---

    public int getAttempts()
    {
        return attempts;
    }

    public int getSuccesses()
    {
        return successes;
    }

    public int getFailures()
    {
        return Math.max(0, attempts - successes);
    }

    // --- Getters: Bonus ore counts ---

    public int getVarrockArmorCount()
    {
        return varrockArmorCount;
    }

    public int getCelestialRingCount()
    {
        return celestialRingCount;
    }

    public int getMiningCapeCount()
    {
        return miningCapeCount;
    }

    // --- Getters: Granite distribution ---

    public int getCount500g()
    {
        return count500g;
    }

    public int getCount2kg()
    {
        return count2kg;
    }

    public int getCount5kg()
    {
        return count5kg;
    }

    public int getTotalGraniteOres()
    {
        return count500g + count2kg + count5kg;
    }
}
