package com.jforex.dzconverter;

import com.dukascopy.api.IBar;

public class ZorroBar implements IBar {

    private long time;
    private double open;
    private double close;
    private double high;
    private double low;

    public ZorroBar(long time,
                    double open,
                    double close,
                    double high,
                    double low) {
        this.time = time;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }

    public long getTime() {
        return time;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }

    public double getVolume() {
        return 0;
    }
}
