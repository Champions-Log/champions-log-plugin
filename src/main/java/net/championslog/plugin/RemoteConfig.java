package net.championslog.plugin;

import net.championslog.plugin.triggers.InventoryTrigger;

import java.util.List;

public class RemoteConfig {

    private final int version;
    private final List<InventoryTrigger> inventoryTriggers;

    public RemoteConfig(int version, List<InventoryTrigger> inventoryTriggers) {
        this.version = version;
        this.inventoryTriggers = inventoryTriggers;
    }

    public int version() {
        return version;
    }

    public List<InventoryTrigger> inventoryTriggers() {
        return inventoryTriggers;
    }
}
