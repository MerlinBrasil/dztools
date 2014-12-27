package com.jforex.dzplugin.config;

public class DukascopyParams {

    public static double LOT_SIZE = 1000;
    public static double LOT_SCALE = 1000000;
    public static double LOT_SIZE_SCALED = LOT_SIZE / LOT_SCALE;
    public static final double DEFAULT_SLIPPAGE = 3f;
    public static final String ORDER_PREFIX_LABEL = "Zorro";
}
