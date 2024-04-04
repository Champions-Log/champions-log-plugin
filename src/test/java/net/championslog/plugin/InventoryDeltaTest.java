package net.championslog.plugin;

import net.runelite.api.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(Lifecycle.PER_CLASS)
class InventoryDeltaTest {

    @Test
    public void testItemAdded() {
        Item[] previous = {new Item(565, 5)};
        Item[] current = {new Item(565, 5), new Item(560, 23)};

        var delta = InventoryDelta.compute(previous, current);

        var addedItems = delta.added();
        assertEquals(1, addedItems.size());

        var addedItem = delta.added().get(0);
        assertEquals(560, addedItem.getId());
        assertEquals(23, addedItem.getQuantity());
    }

    @Test
    public void testItemRemoved() {
        Item[] previous = {new Item(565, 5), new Item(560, 23)};
        Item[] current = {new Item(565, 5), new Item(-1, 0)};

        var delta = InventoryDelta.compute(previous, current);

        var removedItems = delta.removed();
        assertEquals(1, removedItems.size());

        var removedItem = delta.removed().get(0);
        assertEquals(560, removedItem.getId());
        assertEquals(23, removedItem.getQuantity());
    }

    @Test
    public void testItemStackIncremented() {
        Item[] previous = {new Item(565, 5), new Item(560, 23)};
        Item[] current = {new Item(565, 5), new Item(560, 28)};

        var delta = InventoryDelta.compute(previous, current);

        var addedItems = delta.added();
        assertEquals(1, addedItems.size());

        var addedItem = delta.added().get(0);
        assertEquals(560, addedItem.getId());
        assertEquals(5, addedItem.getQuantity());
    }

    @Test
    public void testItemStackDecremented() {
        Item[] previous = {new Item(565, 5), new Item(560, 23)};
        Item[] current = {new Item(565, 5), new Item(560, 13)};

        var delta = InventoryDelta.compute(previous, current);

        var removedItems = delta.removed();
        assertEquals(1, removedItems.size());

        var addedItem = delta.removed().get(0);
        assertEquals(560, addedItem.getId());
        assertEquals(10, addedItem.getQuantity());
    }
}