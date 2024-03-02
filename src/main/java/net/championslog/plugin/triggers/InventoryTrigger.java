package net.championslog.plugin.triggers;

public class InventoryTrigger {

    private final int inventoryId;
    private final String activity;

    public InventoryTrigger(int inventoryId, String activity) {
        this.inventoryId = inventoryId;
        this.activity = activity;
    }

    public int inventoryId() {
        return inventoryId;
    }

    public String activity() {
        return activity;
    }
}
