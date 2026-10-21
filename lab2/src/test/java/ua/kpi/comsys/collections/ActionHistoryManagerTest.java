package ua.kpi.comsys.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ActionHistoryManagerTest {

    @Test
    void constructorRejectsNonPositiveCapacity() {
        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class, () -> new ActionHistoryManager(0));
        assertEquals("maxCapacity must be greater than 0", zero.getMessage());
        assertThrows(IllegalArgumentException.class, () -> new ActionHistoryManager(-1));
    }

    @Test
    void performActionRejectsNullAndBlank() {
        ActionHistoryManager manager = new ActionHistoryManager(3);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.performAction(null));
        assertEquals("Action cannot be null or blank", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> manager.performAction(""));
        assertThrows(IllegalArgumentException.class, () -> manager.performAction("   "));
        assertEquals(0, manager.getHistorySize());
    }

    @Test
    void historyIsOrderedFromNewestToOldest() {
        ActionHistoryManager manager = new ActionHistoryManager(5);
        manager.performAction("a");
        manager.performAction("b");
        manager.performAction("c");
        assertEquals(List.of("c", "b", "a"), manager.getHistory());
        assertEquals(3, manager.getHistorySize());
    }

    @Test
    void oldestActionIsEvictedWhenCapacityExceeded() {
        ActionHistoryManager manager = new ActionHistoryManager(2);
        manager.performAction("a");
        manager.performAction("b");
        manager.performAction("c");
        assertEquals(List.of("c", "b"), manager.getHistory());
        assertEquals(2, manager.getHistorySize());
    }

    @Test
    void undoRemovesAndReturnsNewestAction() {
        ActionHistoryManager manager = new ActionHistoryManager(3);
        manager.performAction("a");
        manager.performAction("b");
        assertEquals("b", manager.undo());
        assertEquals("a", manager.undo());
        assertEquals(0, manager.getHistorySize());
    }

    @Test
    void undoOnEmptyHistoryReturnsNull() {
        assertNull(new ActionHistoryManager(1).undo());
    }

    @Test
    void peekReturnsNewestWithoutRemoving() {
        ActionHistoryManager manager = new ActionHistoryManager(3);
        manager.performAction("a");
        manager.performAction("b");
        assertEquals("b", manager.peekRecentAction());
        assertEquals(2, manager.getHistorySize());
    }

    @Test
    void peekOnEmptyHistoryReturnsNull() {
        assertNull(new ActionHistoryManager(1).peekRecentAction());
    }

    @Test
    void emptyHistoryIsEmptyList() {
        ActionHistoryManager manager = new ActionHistoryManager(1);
        assertTrue(manager.getHistory().isEmpty());
        assertEquals(0, manager.getHistorySize());
    }

    @Test
    void modifyingReturnedListDoesNotAffectHistory() {
        ActionHistoryManager manager = new ActionHistoryManager(3);
        manager.performAction("a");
        List<String> copy = manager.getHistory();
        copy.clear();
        copy.add("x");
        assertEquals(List.of("a"), manager.getHistory());
        assertEquals(1, manager.getHistorySize());
    }

    @Test
    void clearRemovesAllActions() {
        ActionHistoryManager manager = new ActionHistoryManager(3);
        manager.performAction("a");
        manager.performAction("b");
        manager.clear();
        assertEquals(0, manager.getHistorySize());
        assertNull(manager.peekRecentAction());
    }

    @Test
    void capacityOfOneKeepsOnlyLatestAction() {
        ActionHistoryManager manager = new ActionHistoryManager(1);
        manager.performAction("a");
        manager.performAction("b");
        assertEquals(List.of("b"), manager.getHistory());
    }
}
