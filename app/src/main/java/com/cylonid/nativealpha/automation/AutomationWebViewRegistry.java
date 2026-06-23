package com.cylonid.nativealpha.automation;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class AutomationWebViewRegistry {
    private static final Map<Integer, Entry> entries = new HashMap<>();
    private static int lastResumedWebappId = -1;

    private AutomationWebViewRegistry() {}

    public static synchronized void register(int webappId, AutomationWebViewHost host) {
        Entry existing = entries.get(webappId);
        boolean resumed = existing != null && existing.resumed;
        entries.put(webappId, new Entry(host, resumed));
    }

    public static synchronized void markResumed(int webappId, AutomationWebViewHost host) {
        entries.put(webappId, new Entry(host, true));
        lastResumedWebappId = webappId;
    }

    public static synchronized void markPaused(int webappId, AutomationWebViewHost host) {
        Entry existing = entries.get(webappId);
        if (existing != null && existing.getHost() == host) {
            existing.resumed = false;
        }
        if (lastResumedWebappId == webappId) {
            lastResumedWebappId = -1;
        }
    }

    public static synchronized void unregister(int webappId, AutomationWebViewHost host) {
        Entry existing = entries.get(webappId);
        if (existing != null && existing.getHost() == host) {
            entries.remove(webappId);
        }
        if (lastResumedWebappId == webappId) {
            lastResumedWebappId = -1;
        }
    }

    public static synchronized AutomationWebViewHost getResumedHost(int webappId) {
        Entry entry = entries.get(webappId);
        if (entry == null || !entry.resumed) {
            return null;
        }

        AutomationWebViewHost host = entry.getHost();
        if (host == null) {
            entries.remove(webappId);
        }
        return host;
    }

    public static synchronized AutomationWebViewHost getActiveHost() {
        if (lastResumedWebappId == -1) {
            return null;
        }
        return getResumedHost(lastResumedWebappId);
    }

    private static class Entry {
        private final WeakReference<AutomationWebViewHost> hostRef;
        private boolean resumed;

        private Entry(AutomationWebViewHost host, boolean resumed) {
            this.hostRef = new WeakReference<>(host);
            this.resumed = resumed;
        }

        private AutomationWebViewHost getHost() {
            return hostRef.get();
        }
    }
}