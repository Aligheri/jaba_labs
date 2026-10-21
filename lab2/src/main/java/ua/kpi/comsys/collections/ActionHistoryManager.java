package ua.kpi.comsys.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ActionHistoryManager {

    private final int maxCapacity;
    private final Deque<String> history = new ArrayDeque<>();

    public ActionHistoryManager(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be greater than 0");
        }
        this.maxCapacity = maxCapacity;
    }

    public void performAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or blank");
        }
        history.addFirst(action);
        if (history.size() > maxCapacity) {
            history.removeLast();
        }
    }

    public String undo() {
        return history.pollFirst();
    }

    public String peekRecentAction() {
        return history.peekFirst();
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public int getHistorySize() {
        return history.size();
    }

    public void clear() {
        history.clear();
    }
}
