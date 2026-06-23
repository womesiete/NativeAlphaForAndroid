package com.cylonid.nativealpha.automation;

import android.content.Context;
import android.content.Intent;

import com.cylonid.nativealpha.util.Const;
import com.google.gson.Gson;

public class AutomationResultBroadcaster {
    private static final Gson gson = new Gson();

    private AutomationResultBroadcaster() {}

    public static void broadcast(Context context, AutomationCommand command, AutomationResult result) {
        if (command == null || isBlank(command.getResultBroadcastAction())) {
            return;
        }

        Intent intent = new Intent(command.getResultBroadcastAction());
        intent.putExtra(Const.AUTOMATION_EXTRA_REQUEST_ID, command.getRequestId());
        intent.putExtra(Const.AUTOMATION_EXTRA_COMMAND, command.getCommandName());
        intent.putExtra(Const.INTENT_WEBAPPID, command.getWebappId());
        intent.putExtra(Const.AUTOMATION_RESULT_OK, result.isOk());
        intent.putExtra(Const.AUTOMATION_RESULT_STATUS, result.getStatus());
        intent.putExtra(Const.AUTOMATION_RESULT_ERROR_CODE, result.getErrorCode());
        intent.putExtra(Const.AUTOMATION_RESULT_ERROR_MESSAGE, result.getErrorMessage());
        intent.putExtra(Const.AUTOMATION_RESULT_JSON, gson.toJson(result.getData()));

        context.sendBroadcast(intent);
    }

    public static void broadcastFailureFromIntent(
            Context context,
            Intent requestIntent,
            String status,
            String errorCode,
            String errorMessage
    ) {
        String resultAction = AutomationCommandParser.getStringExtra(
                requestIntent,
                Const.AUTOMATION_EXTRA_RESULT_ACTION
        );
        if (isBlank(resultAction)) {
            return;
        }

        Intent intent = new Intent(resultAction);
        intent.putExtra(
                Const.AUTOMATION_EXTRA_REQUEST_ID,
                AutomationCommandParser.getStringExtra(requestIntent, Const.AUTOMATION_EXTRA_REQUEST_ID)
        );
        intent.putExtra(
                Const.AUTOMATION_EXTRA_COMMAND,
                AutomationCommandParser.getStringExtra(requestIntent, Const.AUTOMATION_EXTRA_COMMAND)
        );
        intent.putExtra(Const.AUTOMATION_RESULT_OK, false);
        intent.putExtra(Const.AUTOMATION_RESULT_STATUS, status);
        intent.putExtra(Const.AUTOMATION_RESULT_ERROR_CODE, errorCode);
        intent.putExtra(Const.AUTOMATION_RESULT_ERROR_MESSAGE, errorMessage);
        intent.putExtra(Const.AUTOMATION_RESULT_JSON, "{}");

        context.sendBroadcast(intent);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}