package com.cylonid.nativealpha.automation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cylonid.nativealpha.model.DataManager;
import com.cylonid.nativealpha.model.GlobalSettings;
import com.cylonid.nativealpha.model.WebApp;
import com.cylonid.nativealpha.util.Const;

import java.nio.charset.StandardCharsets;

public class AutomationCommandReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Const.AUTOMATION_ACTION_COMMAND.equals(intent.getAction())) {
            return;
        }

        DataManager.getInstance().loadAppData();

        AutomationCommand command = AutomationCommandParser.parse(intent);

        if (isBlank(command.getCommandName())) {
            AutomationResultBroadcaster.broadcastFailureFromIntent(
                    context,
                    intent,
                    "failed",
                    "missing_command",
                    "No automation command was supplied."
            );
            return;
        }

        if (!AutomationCommand.isSupportedCommand(command.getCommandName())) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "unknown_command",
                            "Unsupported automation command: " + command.getCommandName()
                    )
            );
            return;
        }

        if (!validatePasscode(context, intent, command)) {
            return;
        }

        WebApp targetWebApp = resolveTargetWebApp(context, command);
        if (targetWebApp == null) {
            return;
        }

        if (targetWebApp.isUseContainer() || targetWebApp.getContainerId() != Const.NO_CONTAINER) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "unsupported_in_sandbox_process",
                            "Automation routing for sandbox/container Web Apps is not implemented."
                    )
            );
            return;
        }

        if (!targetWebApp.isAllowAutomationIntents()) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "automation_disabled_for_webapp",
                            "Automation intents are disabled for this Web App."
                    )
            );
            return;
        }

        if (command.requiresJavaScript() && !targetWebApp.isAllowJs()) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "javascript_disabled_for_webapp",
                            "JavaScript is disabled for this Web App."
                    )
            );
            return;
        }

        if (!validateCommandArguments(context, command)) {
            return;
        }

        AutomationCommandRouter.route(context.getApplicationContext(), command, targetWebApp);
    }

    private boolean validatePasscode(Context context, Intent intent, AutomationCommand command) {
        GlobalSettings settings = DataManager.getInstance().getSettings();
        String configuredPasscode = settings.getAutomationPasscode();
        String suppliedPasscode = AutomationCommandParser.getStringExtra(
                intent,
                Const.AUTOMATION_EXTRA_PASSCODE
        );

        if (isBlank(configuredPasscode)) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "automation_passcode_not_configured",
                            "No global automation passcode is configured."
                    )
            );
            return false;
        }

        if (isBlank(suppliedPasscode)) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "missing_passcode",
                            "No automation passcode was supplied."
                    )
            );
            return false;
        }

        if (!configuredPasscode.equals(suppliedPasscode)) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "invalid_passcode",
                            "Invalid automation passcode."
                    )
            );
            return false;
        }

        return true;
    }

    private WebApp resolveTargetWebApp(Context context, AutomationCommand command) {
        if (!command.hasExplicitWebappId()) {
            AutomationWebViewHost activeHost = AutomationWebViewRegistry.getActiveHost();
            if (activeHost == null) {
                AutomationResultBroadcaster.broadcast(
                        context,
                        command,
                        AutomationResult.failure(
                                "failed",
                                "activity_not_active",
                                "No active Web App is available."
                        )
                );
                return null;
            }

            command.setWebappId(activeHost.getAutomationWebAppId());
        }

        WebApp targetWebApp = DataManager.getInstance()
                .getWebAppIgnoringGlobalOverride(command.getWebappId(), true);

        if (targetWebApp == null) {
            AutomationResultBroadcaster.broadcast(
                    context,
                    command,
                    AutomationResult.failure(
                            "failed",
                            "webapp_not_found",
                            "Target Web App was not found."
                    )
            );
        }

        return targetWebApp;
    }

    private boolean validateCommandArguments(Context context, AutomationCommand command) {
        String commandName = command.getCommandName();

        if (Const.AUTOMATION_COMMAND_ZOOM_BY.equals(commandName)) {
            String factor = command.getExtra(Const.AUTOMATION_EXTRA_ZOOM_FACTOR);
            if (!isValidDouble(factor)) {
                AutomationResultBroadcaster.broadcast(
                        context,
                        command,
                        AutomationResult.failure(
                                "failed",
                                "invalid_argument",
                                "zoom_factor must be numeric."
                        )
                );
                return false;
            }
        }

        if (Const.AUTOMATION_COMMAND_RUN_JS.equals(commandName)) {
            String js = command.getExtra(Const.AUTOMATION_EXTRA_JS);
            if (isBlank(js)) {
                AutomationResultBroadcaster.broadcast(
                        context,
                        command,
                        AutomationResult.failure(
                                "failed",
                                "invalid_argument",
                                "js is required."
                        )
                );
                return false;
            }

            int byteLength = js.getBytes(StandardCharsets.UTF_8).length;
            if (byteLength > Const.AUTOMATION_MAX_JS_LENGTH_BYTES) {
                AutomationResultBroadcaster.broadcast(
                        context,
                        command,
                        AutomationResult.failure(
                                "failed",
                                "invalid_argument",
                                "js exceeds the 64 KB limit."
                        )
                );
                return false;
            }
        }

        if (Const.AUTOMATION_COMMAND_FIND_TEXT.equals(commandName)
                || Const.AUTOMATION_COMMAND_CLICK_TEXT.equals(commandName)) {
            String text = command.getExtra(Const.AUTOMATION_EXTRA_TEXT);
            if (isBlank(text)) {
                AutomationResultBroadcaster.broadcast(
                        context,
                        command,
                        AutomationResult.failure(
                                "failed",
                                "invalid_argument",
                                "text is required."
                        )
                );
                return false;
            }
        }

        return true;
    }

    private boolean isValidDouble(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}