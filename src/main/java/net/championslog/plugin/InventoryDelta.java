package net.championslog.plugin;

import net.runelite.api.Item;

import java.util.ArrayList;
import java.util.List;

public class InventoryDelta {

    private final List<Item> addedItems;
    private final List<Item> removedItems;

    private InventoryDelta(List<Item> addedItems, List<Item> removedItems) {
        this.addedItems = addedItems;
        this.removedItems = removedItems;
    }

    public List<Item> added() {
        return addedItems;
    }

    public List<Item> removed() {
        return removedItems;
    }

    public static InventoryDelta compute(Item[] previousState, Item[] currentState) {
        var removedItems = new ArrayList<Item>();
        var addedItems = new ArrayList<Item>();

        for (var previous : previousState) {
            boolean found = false;
            for (var current : currentState) {

                if (current.getId() == -1 || previous.getId() != current.getId()) {
                    continue;
                }
                var previousQtd = previous.getQuantity();
                var currentQtd = current.getQuantity();

                if (previousQtd < currentQtd) { // partial add
                    addedItems.add(new Item(previous.getId(), currentQtd - previousQtd));
                } else if (previousQtd > currentQtd) { // partial removal
                    removedItems.add(new Item(previous.getId(), previousQtd - currentQtd));
                }
                found = true;
            }

            if (!found) {
                removedItems.add(previous);
            }
        }

        for (Item current : currentState) {
            boolean found = false;

            if (current.getId() == -1) {
                continue;
            }
            for (var previous : previousState) {
                if (previous.getId() == current.getId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                addedItems.add(current);
            }
        }
        return new InventoryDelta(addedItems, removedItems);
    }

    @Override
    public String toString() {
        return "InventoryDelta{" +
                "addedItems=" + addedItems +
                ", removedItems=" + removedItems +
                '}';
    }
}
