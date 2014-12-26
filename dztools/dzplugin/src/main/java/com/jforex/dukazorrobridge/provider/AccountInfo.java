package com.jforex.dukazorrobridge.provider;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IContext;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.JFUtils;
import com.dukascopy.api.OfferSide;
import com.jforex.dukazorrobridge.ZorroLogger;
import com.jforex.dukazorrobridge.config.DukascopyParams;
import com.jforex.dukazorrobridge.utils.InstrumentUtils;

public class AccountInfo {

    private final IAccount account;
    private final JFUtils utils;
    private final IPriceEngine priceEngine;
    private final ICurrency accountCurrency;
    private final String accountID;
    private final double leverage;
    private final double accountLOTMargin;

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
            ZorroLogger.log("Pipcost calculation exc: " + e.getMessage());
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
