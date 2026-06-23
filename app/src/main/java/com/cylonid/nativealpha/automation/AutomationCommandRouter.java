package com.cylonid.nativealpha.automation;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.cylonid.nativealpha.model.WebApp;
import com.cylonid.nativealpha.util.App;
import com.cylonid.nativealpha.util.WebViewLauncher;

import java.util.HashSet;
import java.util.Set;

public class AutomationCommandRouter {
    private static final Set<Integer> runningWebappIds = new HashSet<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private AutomationCommandRouter() {}

    public static void route(Context context, AutomationCommand command, WebApp webApp) {
        AutomationWebViewHost host = AutomationWebViewRegistry.getResumedHost(command.getWebappId());

        if (host == null && !command.hasExplicitWebappId()) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "activity_not_active",
                            "No active Web App is available."
                    )
            );
            return;
        }

        if (!AutomationCommandQueue.enqueue(command)) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "queue_full",
                            "Automation queue is full for this Web App."
                    )
            );
            return;
        }

        scheduleTimeoutCheck(command);

        if (host == null && command.hasExplicitWebappId() && command.shouldLaunchIfInactive()) {
            launchTarget(context, command, webApp);
        }

        drain(command.getWebappId());
    }

    public static void onHostReady(int webappId) {
        drain(webappId);
    }

    public static void onDomReady(int webappId) {
        drain(webappId);
    }

    public static void onCommandFinished(int webappId) {
        synchronized (runningWebappIds) {
            runningWebappIds.remove(webappId);
        }
        drain(webappId);
    }

    private static void launchTarget(Context context, AutomationCommand command, WebApp webApp) {
        Intent launchIntent = WebViewLauncher.createWebViewIntent(webApp, context, null);
        if (launchIntent == null) {
            AutomationCommandQueue.remove(command);
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "activity_launch_failed",
                            "Could not create WebViewActivity launch intent."
                    )
            );
            return;
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }

    private static void drain(int webappId) {
        clearExpiredCommands(webappId);

        synchronized (runningWebappIds) {
            if (runningWebappIds.contains(webappId)) {
                return;
            }
        }

        AutomationWebViewHost host = AutomationWebViewRegistry.getResumedHost(webappId);
        if (host == null || !host.isAutomationWebViewReady()) {
            return;
        }

        AutomationCommand command = AutomationCommandQueue.peek(webappId);
        if (command == null) {
            return;
        }

        if (command.requiresDom() && !host.isAutomationDomReady()) {
            return;
        }

        AutomationCommandQueue.poll(webappId);

        synchronized (runningWebappIds) {
            runningWebappIds.add(webappId);
        }

        try {
            host.executeAutomationCommand(command);
        } catch (Exception e) {
            AutomationResultBroadcaster.broadcast(
                    App.getAppContext(),
                    command,
                    AutomationResult.failure(
                            "failed",
                            "execution_exception",
                            e.getMessage()
                    )
            );
            onCommandFinished(webappId);
        }
    }

    private static void clearExpiredCommands(int webappId) {
        while (true) {
            AutomationCommand command = AutomationCommandQueue.peek(webappId);
            if (command == null || !command.isExpired()) {
                return;
            }

            AutomationCommandQueue.poll(webappId);
            AutomationResultBroadcaster.broadcast(
                    App.getAppContext(),
                    command,
                    AutomationResult.failure(
                            "expired",
                            "command_timeout",
                            "Automation command expired before it could run."
                    )
            );
        }
    }

    private static void scheduleTimeoutCheck(AutomationCommand command) {
        mainHandler.postDelayed(
                () -> drain(command.getWebappId()),
                command.getTimeoutMs() + 250L
        );
    }
}