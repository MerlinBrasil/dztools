package com.jforex.dzplugin.handler;

/*
 * #%L
 * dzplugin
 * %%
 * Copyright (C) 2014 - 2015 juxeii
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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.utils.DateTimeUtils;

public class BarFileWriter {
    private final List<IBar> bars;
    private ByteBuffer byteBuffer;
    private FileOutputStream outStream;
    private final String fileName;

    private final static Logger logger = LogManager.getLogger(BarFileWriter.class);

    public BarFileWriter(String fileName,
                         List<IBar> bars) {
        this.fileName = fileName;
        this.bars = bars;
    }

    public boolean isWriteBarsToTICKsFileOK() {
        initByteBuffer();
        writeBarsToBuffer();
        return isWriteBufferToFileOK();
    }

    private void initByteBuffer() {
        byteBuffer = ByteBuffer.allocate(24 * bars.size());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    private void writeBarsToBuffer() {
        for (IBar bar : bars) {
            byteBuffer.putFloat((float) bar.getOpen());
            byteBuffer.putFloat((float) bar.getClose());
            byteBuffer.putFloat((float) bar.getHigh());
            byteBuffer.putFloat((float) bar.getLow());
            byteBuffer.putDouble(DateTimeUtils.getUTCTimeFromBar(bar));
        }
    }

    private boolean isWriteBufferToFileOK() {
        try {
            outStream = new FileOutputStream(fileName);
            outStream.write(byteBuffer.array(), 0, byteBuffer.limit());
            outStream.close();
            return true;
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException: " + e.getMessage());
            ZorroLogger.indicateError();

        } catch (IOException e) {
            logger.error("IOException while writing TICKs file! " + e.getMessage());
            ZorroLogger.indicateError();
        }
        return false;
    }
}
