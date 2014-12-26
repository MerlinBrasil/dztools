package com.jforex.dukazorrobridge.config;

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
