package com.ov.tracker.utils;

import android.util.Log;

public class LogUtil {

    private static final String BLE_SCALE = "【==BLE-SCANNER==】";

    private static final boolean DEBUG = true;

    public static void info(String msg) {
        String name = Thread.currentThread().getName();
        msg = name + "：：" + msg;
        if (DEBUG)
            Log.i(BLE_SCALE, msg);
    }

    public static void error(String msg) {
        String name = Thread.currentThread().getName();
        msg = name + "：：" + msg;
        if (DEBUG)
            Log.e(BLE_SCALE, msg);
    }

    public static void debug(String msg) {
        String name = Thread.currentThread().getName();
        msg = name + "：：" + msg;
        if (DEBUG)
            Log.d(BLE_SCALE, msg);
    }

}
