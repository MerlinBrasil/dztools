package com.jforex.dukazorrobridge;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ZorroLogger {

    public static void log(String errorMsg) {
        jcallback_BrokerError(errorMsg);
    }

    public static void logProgress(int progress) {
        jcallback_BrokerProgress(progress);
    }

    public static void logDiagnose(String errorMsg) {
        log("#" + errorMsg);
    }

    public static void logPopUp(String errorMsg) {
        log("!" + errorMsg);
    }

    public static void logSystem(String errorMsg) {
        System.out.println(errorMsg);
    }

    public static void logDateTime(String prefix,
                                   long dateTime) {
        log(prefix + " " + formatDateTime(dateTime));
    }

    public static String formatDateTime(long dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date(dateTime));
    }

    private static native void jcallback_BrokerError(String errorMsg);

    private static native void jcallback_BrokerProgress(int progress);
}
