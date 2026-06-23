package com.cylonid.nativealpha.automation;

import java.util.HashMap;
import java.util.Map;

public class AutomationResult {
    private final boolean ok;
    private final String status;
    private final String errorCode;
    private final String errorMessage;
    private final Map<String, Object> data = new HashMap<>();

    private AutomationResult(boolean ok, String status, String errorCode, String errorMessage) {
        this.ok = ok;
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static AutomationResult success(String status) {
        return new AutomationResult(true, status, "", "");
    }

    public static AutomationResult failure(String status, String errorCode, String errorMessage) {
        return new AutomationResult(false, status, errorCode, errorMessage);
    }

    public boolean isOk() {
        return ok;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public AutomationResult put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public AutomationResult putAll(Map<String, Object> values) {
        data.putAll(values);
        return this;
    }
}