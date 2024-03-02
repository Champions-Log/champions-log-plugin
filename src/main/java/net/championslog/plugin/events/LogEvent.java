package net.championslog.plugin.events;

import net.runelite.api.Item;

import java.util.Map;

public class LogEvent {

    private final long characterId;
    private final String activity;
    private final Item[] inventory;
    private final Map<String, Object> metadata;

    public LogEvent(long characterId, String activity, Item[] inventory, Map<String, Object> metadata) {
        this.characterId = characterId;
        this.activity = activity;
        this.inventory = inventory;
        this.metadata = metadata;
    }
}
