package com.cylonid.nativealpha.util;

import com.cylonid.nativealpha.model.AdblockConfig;

import java.util.ArrayList;

public class Const {
    public static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0";

    public static final String INTENT_WEBAPPID = "webappID";
    public static final String INTENT_URL = "url";
    public static final String INTENT_BACKUP_RESTORED = "backup_restored";
    public static final String INTENT_WEBAPP_CHANGED = "webapp_changed";
    public static final String INTENT_REFRESH_NEW_THEME = "theme_changed";

    public static final int NO_CONTAINER = -1;

    public static final int PERMISSION_RC_LOCATION = 123;
    public static final int PERMISSION_RC_STORAGE = 132;
    public static final int PERMISSION_CAMERA = 100;
    public static final int PERMISSION_AUDIO = 101;

    public static final int CODE_OPEN_FILE = 512;
    public static final int CODE_WRITE_FILE = 4096;

    public static final int FAVICON_MIN_WIDTH = 96;

    public static final String AUTOMATION_ACTION_COMMAND = "com.cylonid.nativealpha.action.AUTOMATION_COMMAND";

    public static final String AUTOMATION_EXTRA_COMMAND = "automation_command";
    public static final String AUTOMATION_EXTRA_PASSCODE = "automation_passcode";
    public static final String AUTOMATION_EXTRA_REQUEST_ID = "request_id";
    public static final String AUTOMATION_EXTRA_RESULT_ACTION = "result_broadcast_action";
    public static final String AUTOMATION_EXTRA_LAUNCH_IF_INACTIVE = "launch_if_inactive";

    public static final String AUTOMATION_EXTRA_DX = "dx";
    public static final String AUTOMATION_EXTRA_DY = "dy";
    public static final String AUTOMATION_EXTRA_X = "x";
    public static final String AUTOMATION_EXTRA_Y = "y";
    public static final String AUTOMATION_EXTRA_SCROLL_UNIT = "scroll_unit";
    public static final String AUTOMATION_EXTRA_ZOOM_FACTOR = "zoom_factor";
    public static final String AUTOMATION_EXTRA_JS = "js";
    public static final String AUTOMATION_EXTRA_TEXT = "text";
    public static final String AUTOMATION_EXTRA_SCROLL_INTO_VIEW = "scroll_into_view";

    public static final String AUTOMATION_RESULT_OK = "ok";
    public static final String AUTOMATION_RESULT_STATUS = "status";
    public static final String AUTOMATION_RESULT_ERROR_CODE = "error_code";
    public static final String AUTOMATION_RESULT_ERROR_MESSAGE = "error_message";
    public static final String AUTOMATION_RESULT_JSON = "json_result";

    public static final String AUTOMATION_COMMAND_ZOOM_IN = "zoom_in";
    public static final String AUTOMATION_COMMAND_ZOOM_OUT = "zoom_out";
    public static final String AUTOMATION_COMMAND_ZOOM_BY = "zoom_by";
    public static final String AUTOMATION_COMMAND_SCROLL_BY = "scroll_by";
    public static final String AUTOMATION_COMMAND_SCROLL_TO = "scroll_to";
    public static final String AUTOMATION_COMMAND_RUN_JS = "run_js";
    public static final String AUTOMATION_COMMAND_FIND_TEXT = "find_text";
    public static final String AUTOMATION_COMMAND_CLICK_TEXT = "click_text";

    public static final int AUTOMATION_MAX_QUEUE_SIZE_PER_WEBAPP = 10;
    public static final int AUTOMATION_MAX_JS_LENGTH_BYTES = 64 * 1024;

    public static final long AUTOMATION_NATIVE_TIMEOUT_MS = 10_000L;
    public static final long AUTOMATION_DOM_TIMEOUT_MS = 30_000L;

    public static ArrayList<AdblockConfig> getDefaultAdBlockConfig() {
        ArrayList<AdblockConfig> list = new ArrayList<>();
        list.add(new AdblockConfig("Fanboy Ultimate List", "https://fanboy.co.nz/r/fanboy-ultimate.txt"));
        return list;
    }
}
