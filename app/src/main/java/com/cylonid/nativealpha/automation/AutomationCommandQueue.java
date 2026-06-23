package com.cylonid.nativealpha.automation;

import com.cylonid.nativealpha.util.Const;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class AutomationCommandQueue {
    private static final Map<Integer, ArrayDeque<AutomationCommand>> queues = new HashMap<>();

    private AutomationCommandQueue() {}

    public static synchronized boolean enqueue(AutomationCommand command) {
        ArrayDeque<AutomationCommand> queue = queues.get(command.getWebappId());
        if (queue == null) {
            queue = new ArrayDeque<>();
            queues.put(command.getWebappId(), queue);
        }

        if (queue.size() >= Const.AUTOMATION_MAX_QUEUE_SIZE_PER_WEBAPP) {
            return false;
        }

        queue.offer(command);
        return true;
    }

    public static synchronized AutomationCommand peek(int webappId) {
        ArrayDeque<AutomationCommand> queue = queues.get(webappId);
        return queue == null ? null : queue.peek();
    }

    public static synchronized AutomationCommand poll(int webappId) {
        ArrayDeque<AutomationCommand> queue = queues.get(webappId);
        if (queue == null) {
            return null;
        }

        AutomationCommand command = queue.poll();
        if (queue.isEmpty()) {
            queues.remove(webappId);
        }
        return command;
    }

    public static synchronized void remove(AutomationCommand command) {
        ArrayDeque<AutomationCommand> queue = queues.get(command.getWebappId());
        if (queue == null) {
            return;
        }

        queue.remove(command);
        if (queue.isEmpty()) {
            queues.remove(command.getWebappId());
        }
    }
}