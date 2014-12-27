package com.jforex.dzplugin;

/*
 * #%L
 * dzplugin
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 juxeii
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


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
