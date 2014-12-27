package com.jforex.dzplugin.config;

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


public class ReturnCodes {

    public static final int LOGIN_OK = 1;
    public static final int LOGIN_FAIL = 0;
    public static final int LOGOUT_OK = 1;

    public static final int CONNECTION_OK = 2;
    public static final int CONNECTION_OK_BUT_MARKET_CLOSED = 1;
    public static final int CONNECTION_FAIL = 0;

    public static final int ASSET_AVAILABLE = 1;
    public static final int ASSET_UNAVAILABLE = 0;

    public static final int ACCOUNT_AVAILABLE = 1;
    public static final int ACCOUNT_UNAVAILABLE = 0;

    public static final int ORDER_SUBMIT_OK = 0;
    public static final int ORDER_SUBMIT_FAIL = 0;

    public static final int ORDER_CLOSE_FAIL = 0;
    public static final int INVALID_ORDER_ID = 0;
    public static final int ORDER_IS_CLOSED = -1;

    public static final int ADJUST_SL_OK = 1;
    public static final int ADJUST_SL_FAIL = 0;

    public static final int HISTORY_FAIL = 0;

}
