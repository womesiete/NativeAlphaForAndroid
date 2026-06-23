package com.cylonid.nativealpha.automation;

import com.cylonid.nativealpha.util.Const;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AutomationCommand {
    private final String commandName;
    private final String requestId;
    private final String resultBroadcastAction;
    private final boolean explicitWebappId;
    private final boolean launchIfInactive;
    private final Map<String, String> extras;
    private final long createdAtMillis;
    private final long timeoutMs;
    private int webappId;

    public AutomationCommand(
            String commandName,
            String requestId,
            String resultBroadcastAction,
            int webappId,
            boolean explicitWebappId,
            boolean launchIfInactive,
            Map<String, String> extras
    ) {
        this.commandName = commandName;
        this.requestId = requestId;
        this.resultBroadcastAction = resultBroadcastAction;
        this.webappId = webappId;
        this.explicitWebappId = explicitWebappId;
        this.launchIfInactive = launchIfInactive;
        this.extras = Collections.unmodifiableMap(new HashMap<>(extras));
        this.createdAtMillis = System.currentTimeMillis();
        this.timeoutMs = requiresDom(commandName)
                ? Const.AUTOMATION_DOM_TIMEOUT_MS
                : Const.AUTOMATION_NATIVE_TIMEOUT_MS;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getResultBroadcastAction() {
        return resultBroadcastAction;
    }

    public int getWebappId() {
        return webappId;
    }

    public void setWebappId(int webappId) {
        this.webappId = webappId;
    }

    public boolean hasExplicitWebappId() {
        return explicitWebappId;
    }

    public boolean shouldLaunchIfInactive() {
        return launchIfInactive;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public String getExtra(String key) {
        return extras.get(key);
    }

    public String getExtra(String key, String fallback) {
        String value = extras.get(key);
        return value == null ? fallback : value;
    }

    public boolean getBooleanExtra(String key, boolean fallback) {
        String value = extras.get(key);
        if (value == null) {
            return fallback;
        }
        return "true".equalsIgnoreCase(value)
                || "1".equals(value)
                || "yes".equalsIgnoreCase(value);
    }

    public double getDoubleExtra(String key, double fallback) {
        String value = extras.get(key);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAtMillis > timeoutMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public boolean requiresDom() {
        return requiresDom(commandName);
    }

    public boolean requiresJavaScript() {
        return requiresDom();
    }

    public static boolean requiresDom(String commandName) {
        return Const.AUTOMATION_COMMAND_RUN_JS.equals(commandName)
                || Const.AUTOMATION_COMMAND_FIND_TEXT.equals(commandName)
                || Const.AUTOMATION_COMMAND_CLICK_TEXT.equals(commandName);
    }

    public static boolean isSupportedCommand(String commandName) {
        return Const.AUTOMATION_COMMAND_ZOOM_IN.equals(commandName)
                || Const.AUTOMATION_COMMAND_ZOOM_OUT.equals(commandName)
                || Const.AUTOMATION_COMMAND_ZOOM_BY.equals(commandName)
                || Const.AUTOMATION_COMMAND_SCROLL_BY.equals(commandName)
                || Const.AUTOMATION_COMMAND_SCROLL_TO.equals(commandName)
                || Const.AUTOMATION_COMMAND_RUN_JS.equals(commandName)
                || Const.AUTOMATION_COMMAND_FIND_TEXT.equals(commandName)
                || Const.AUTOMATION_COMMAND_CLICK_TEXT.equals(commandName);
    }
}