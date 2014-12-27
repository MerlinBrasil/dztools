package com.jforex.dzplugin.provider;

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

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IContext;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.OfferSide;
import com.jforex.dzplugin.ZorroLogger;
import com.jforex.dzplugin.config.DukascopyParams;
import com.jforex.dzplugin.utils.InstrumentUtils;

public class AccountInfo {

    private final IAccount account;
    private final JFUtils utils;
    private final IPriceEngine priceEngine;
    private final ICurrency accountCurrency;
    private final String accountID;
    private final double leverage;
    private final double accountLOTMargin;

    private final static Logger logger = LogManager.getLogger(AccountInfo.class);

    public AccountInfo(IContext context,
                       IPriceEngine priceEngine) {
        this.account = context.getAccount();
        this.utils = context.getUtils();
        this.priceEngine = priceEngine;

        accountCurrency = account.getAccountCurrency();
        accountID = account.getAccountId();
        leverage = account.getLeverage();
        accountLOTMargin = DukascopyParams.LOT_SIZE / leverage;
    }

    public double getBalance() {
        return account.getBalance();
    }

    public double getEquity() {
        return account.getEquity();
    }

    public String getID() {
        return accountID;
    }

    public ICurrency getCurrency() {
        return accountCurrency;
    }

    public double getTradeValue() {
        return account.getEquity() - account.getBalance();
    }

    public double getFreeMargin() {
        return account.getCreditLine() / leverage;
    }

    public double getUsedMargin() {
        return account.getEquity() - getFreeMargin();
    }

    public double getLeverage() {
        return leverage;
    }

    public double getAccountLotMargin() {
        return accountLOTMargin;
    }

    public double getMarginForLot(Instrument instrument) {
        if (accountCurrency == instrument.getPrimaryJFCurrency())
            return accountLOTMargin;

        Instrument conversionInstrument = InstrumentUtils.getfromCurrencies(accountCurrency, instrument.getPrimaryJFCurrency());
        double conversionPrice = priceEngine.getAsk(conversionInstrument);
        if (accountCurrency == conversionInstrument.getSecondaryJFCurrency())
            conversionPrice = 1f / conversionPrice;

        return accountLOTMargin / conversionPrice;
    }

    public double getPipCost(Instrument instrument,
                             OfferSide offerSide) {
        double pipCost = 0f;
        try {
            pipCost = utils.convertPipToCurrency(instrument, accountCurrency, offerSide) * DukascopyParams.LOT_SIZE;
        } catch (JFException e) {
            logger.error("Pipcost calculation exc: " + e.getMessage());
            ZorroLogger.inicateError();
        }
        return pipCost;
    }

    public boolean isConnected() {
        return account.isConnected();
    }

    public boolean isTradingPossible() {
        return account.isConnected() && account.getAccountState() == IAccount.AccountState.OK;
    }
}
