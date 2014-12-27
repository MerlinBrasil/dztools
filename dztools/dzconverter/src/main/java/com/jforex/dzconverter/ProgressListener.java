package com.jforex.dzconverter;

/*
 * #%L
 * dzconverter
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


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.LoadingProgressListener;

public class ProgressListener implements LoadingProgressListener {

    private final DukaZorroConverter dukaZorroConverter;
    private final Instrument instrument;
    private final List<ZorroBar> bars;
    private FileOutputStream outStream;
    private ByteBuffer bbf;

    private final static Logger logger = LogManager.getLogger(ProgressListener.class);

    public ProgressListener(DukaZorroConverter dukaZorroConverter,
                            List<ZorroBar> bars) {
        this.dukaZorroConverter = dukaZorroConverter;
        this.bars = bars;
        this.instrument = dukaZorroConverter.getInstrument();
    }

    @Override
    public void loadingFinished(boolean allDataLoaded, long start, long end, long currentPosition) {
        if (!allDataLoaded)
            logErrorAndExit("Data loading failed!");
        else {
            logger.info("Data loading finished");
            Collections.reverse(bars);
            try {
                writeBarsToTICKsFile();
            } catch (IOException e) {
                logErrorAndExit("IOException while wrting file: " + e.getMessage());
            }
            dukaZorroConverter.disconnect();
            System.exit(0);
        }
    }

    private String getFileName() {
        return "bars\\" + instrument.getPrimaryJFCurrency().getCurrencyCode() +
                instrument.getSecondaryJFCurrency().getCurrencyCode() +
                "_" + dukaZorroConverter.getYear() + ".bar";
    }

    private void writeBarsToTICKsFile() throws IOException {
        logger.info("Writing TICKs to file...");

        initByteBuffer(bars.size());
        writeBarsToBuffer(bbf);
        writeBufferToFile(bbf);

        logger.info("Writing TICKs file finished.");
    }

    private void writeBarsToBuffer(ByteBuffer bbf) {
        for (IBar bar : bars) {
            bbf.putFloat((float) bar.getOpen());
            bbf.putFloat((float) bar.getClose());
            bbf.putFloat((float) bar.getHigh());
            bbf.putFloat((float) bar.getLow());
            bbf.putDouble(getOLEDateFromDateMilllis(bar.getTime()));
        }
    }

    private void initByteBuffer(int barsCount) {
        bbf = ByteBuffer.allocate(24 * barsCount);
        bbf.order(ByteOrder.LITTLE_ENDIAN);
    }

    private void writeBufferToFile(ByteBuffer bbf) {
        try {
            outStream = new FileOutputStream(getFileName());
            outStream.write(bbf.array(), 0, bbf.limit());
            outStream.close();
        } catch (FileNotFoundException e) {
            logErrorAndExit("FileNotFoundException while writing TICKs file!");
        } catch (IOException e) {
            logErrorAndExit("IOException while writing TICKs file! " + e.getMessage());
        }
    }

    private double getOLEDateFromDateMilllis(long millis) {
        Date date = new Date(millis);
        double oleDate = 25569;
        oleDate += (double) date.getTime() / (1000f * 3600f * 24f) + 1e-8;
        return oleDate;
    }

    private void logErrorAndExit(String errorMsg) {
        logger.error(errorMsg);
        dukaZorroConverter.disconnect();
        System.exit(0);
    }

    @Override
    public boolean stopJob() {
        return false;
    }

    @Override
    public void dataLoaded(long start, long end, long currentPosition, String information) {

    }
}
