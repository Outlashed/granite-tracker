package com.github.outlashed.granitetracker.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;
import com.github.outlashed.granitetracker.model.ProfileData;

@Singleton
public class ProfileStorageService
{
    private static final String CONFIG_GROUP = "granitetracker";
    private static final String PROFILE_NAMES_KEY = "savedProfileNames";
    private static final String PROFILE_PREFIX = "profile.";
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    @Inject
    private Gson gson;

    @Inject
    private ConfigManager configManager;

    public void saveProfile(String name, GraniteTrackingService service)
    {
        ProfileData data = new ProfileData();
        data.name = name;
        data.attempts = service.getAttempts();
        data.successes = service.getSuccesses();
        data.varrockArmorCount = service.getVarrockArmorCount();
        data.celestialRingCount = service.getCelestialRingCount();
        data.miningCapeCount = service.getMiningCapeCount();
        data.count500g = service.getCount500g();
        data.count2kg = service.getCount2kg();
        data.count5kg = service.getCount5kg();

        configManager.setConfiguration(CONFIG_GROUP, PROFILE_PREFIX + name, gson.toJson(data));

        List<String> names = getProfileNames();
        if (!names.contains(name))
        {
            names.add(name);
            configManager.setConfiguration(CONFIG_GROUP, PROFILE_NAMES_KEY, gson.toJson(names));
        }
    }

    public ProfileData loadProfile(String name)
    {
        String json = configManager.getConfiguration(CONFIG_GROUP, PROFILE_PREFIX + name);
        if (json == null || json.isEmpty())
        {
            return null;
        }
        return gson.fromJson(json, ProfileData.class);
    }

    public void deleteProfile(String name)
    {
        configManager.unsetConfiguration(CONFIG_GROUP, PROFILE_PREFIX + name);
        List<String> names = getProfileNames();
        names.remove(name);
        configManager.setConfiguration(CONFIG_GROUP, PROFILE_NAMES_KEY, gson.toJson(names));
    }

    public List<String> getProfileNames()
    {
        String json = configManager.getConfiguration(CONFIG_GROUP, PROFILE_NAMES_KEY);
        if (json == null || json.isEmpty())
        {
            return new ArrayList<>();
        }
        List<String> names = gson.fromJson(json, STRING_LIST_TYPE);
        return names != null ? names : new ArrayList<>();
    }
}
