package com.cylonid.nativealpha.automation;

import android.content.Intent;
import android.os.Bundle;

import com.cylonid.nativealpha.util.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
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
        Map<String, String> extras = extractExtras(intent);
        return extras.get(key);
    }

    public static String getRawStringExtra(Intent intent, String key) {
        if (intent == null || intent.getExtras() == null) {
            return null;
        }

        Bundle bundle = intent.getExtras();
        if (!bundle.containsKey(key)) {
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

        String payload = getBundleString(bundle, Const.AUTOMATION_EXTRA_PAYLOAD);
        mergeJsonPayload(result, payload);

        for (String key : bundle.keySet()) {
            if (Const.AUTOMATION_EXTRA_PAYLOAD.equals(key)) {
                continue;
            }

            Object value = bundle.get(key);
            if (value != null) {
                result.put(key, String.valueOf(value));
            }
        }

        return result;
    }

    private static void mergeJsonPayload(Map<String, String> result, String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return;
        }


        String normalizedPayload = normalizePayload(payload);

        try {
            JSONObject json = new JSONObject(normalizedPayload);
            Iterator<String> keys = json.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                Object value = json.get(key);

                if (value != null && !JSONObject.NULL.equals(value)) {
                    result.put(key, String.valueOf(value));
                }
            }
        } catch (JSONException firstError) {
            String secondAttempt = normalizedPayload.replace("\\\"", "\"");

            try {
                JSONObject json = new JSONObject(secondAttempt);
                Iterator<String> keys = json.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = json.get(key);

                    if (value != null && !JSONObject.NULL.equals(value)) {
                        result.put(key, String.valueOf(value));
                    }
                }
            } catch (JSONException secondError) {
                result.put(Const.AUTOMATION_EXTRA_PAYLOAD_PARSE_ERROR, secondError.getMessage());
            }
        }
    }

    private static String normalizePayload(String payload) {
        String trimmed = payload.trim();

        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            try {
                return new JSONObject("{\"payload\":" + trimmed + "}").getString("payload");
            } catch (JSONException ignored) {
                return trimmed;
            }
        }

        return trimmed;
    }

    private static String getBundleString(Bundle bundle, String key) {
        if (!bundle.containsKey(key)) {
            return null;
        }

        Object value = bundle.get(key);
        return value == null ? null : String.valueOf(value);
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