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


public class DukascopyParams {

    public static double LOT_SIZE = 1000;
    public static double LOT_SCALE = 1000000;
    public static double LOT_SIZE_SCALED = LOT_SIZE / LOT_SCALE;
    public static final double DEFAULT_SLIPPAGE = 3f;
    public static final String ORDER_PREFIX_LABEL = "Zorro";
}
