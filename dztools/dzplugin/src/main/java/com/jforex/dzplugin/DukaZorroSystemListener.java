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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ISystemListener;

public class DukaZorroSystemListener implements ISystemListener {

    private final static Logger logger = LogManager.getLogger(DukaZorroSystemListener.class);

    @Override
    public void onConnect() {
        logger.debug("connected...");
    }

    @Override
    public void onDisconnect() {
        logger.debug("disconnected...");
    }

    @Override
    public void onStart(long arg0) {
        logger.debug("strategy started...");
    }

    @Override
    public void onStop(long arg0) {
        logger.debug("strategy stopped...");
    }
}
