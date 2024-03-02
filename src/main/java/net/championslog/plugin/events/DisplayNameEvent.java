package net.championslog.plugin.events;

public class DisplayNameEvent {

    private final long characterId;
    private final String displayName;

    public DisplayNameEvent(long characterId, String displayName) {
        this.characterId = characterId;
        this.displayName = displayName;
    }
}
