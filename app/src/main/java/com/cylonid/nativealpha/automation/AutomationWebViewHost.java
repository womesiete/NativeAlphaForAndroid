package com.cylonid.nativealpha.automation;

public interface AutomationWebViewHost {
    int getAutomationWebAppId();

    boolean isAutomationWebViewReady();

    boolean isAutomationDomReady();

    void executeAutomationCommand(AutomationCommand command);
}
