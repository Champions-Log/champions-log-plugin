package net.championslog.plugin.events;

import net.runelite.api.Item;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.http.api.config.Profile;

import java.util.Map;

public class LogEvent {

    private final long characterId;
    private final RuneScapeProfileType profileType;
    private final String activity;
    private final Item[] inventory;
    private final Map<String, Object> metadata;

    public LogEvent(long characterId, RuneScapeProfileType profileType, String activity, Item[] inventory, Map<String, Object> metadata) {
        this.characterId = characterId;
        this.profileType = profileType;
        this.activity = activity;
        this.inventory = inventory;
        this.metadata = metadata;
    }
}
