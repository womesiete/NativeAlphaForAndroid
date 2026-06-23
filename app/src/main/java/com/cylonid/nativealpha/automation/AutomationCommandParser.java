package com.cylonid.nativealpha.automation;

import android.content.Intent;
import android.os.Bundle;

import com.cylonid.nativealpha.util.Const;

import java.util.HashMap;
import java.util.Map;

public class AutomationCommandParser {
    private AutomationCommandParser() {}

    public static AutomationCommand parse(Intent intent) {
        Map<String, String> extras = extractExtras(intent);
        String commandName = extras.get(Const.AUTOMATION_EXTRA_COMMAND);
        String requestId = extras.get(Const.AUTOMATION_EXTRA_REQUEST_ID);
        String resultAction = extras.get(Const.AUTOMATION_EXTRA_RESULT_ACTION);

        boolean explicitWebappId = extras.containsKey(Const.INTENT_WEBAPPID);
        int webappId = parseInt(extras.get(Const.INTENT_WEBAPPID), -1);
        boolean launchIfInactive = parseBoolean(
                extras.get(Const.AUTOMATION_EXTRA_LAUNCH_IF_INACTIVE),
                explicitWebappId
        );

        return new AutomationCommand(
                commandName,
                requestId,
                resultAction,
                webappId,
                explicitWebappId,
                launchIfInactive,
                extras
        );
    }

    public static String getStringExtra(Intent intent, String key) {
        Bundle bundle = intent.getExtras();
        if (bundle == null || !bundle.containsKey(key)) {
            return null;
        }
        Object value = bundle.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Map<String, String> extractExtras(Intent intent) {
        Map<String, String> result = new HashMap<>();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return result;
        }

        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value != null) {
                result.put(key, String.valueOf(value));
            }
        }
        return result;
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        return "true".equalsIgnoreCase(value)
                || "1".equals(value)
                || "yes".equalsIgnoreCase(value);
    }
}