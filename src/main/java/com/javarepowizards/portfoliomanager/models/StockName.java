package com.javarepowizards.portfoliomanager.models;

public enum StockName {
    WES_AX("WES.AX", "Wesfarmers Ltd"),
    TLS_AX("TLS.AX", "Telstra Corp Ltd"),
    AMC_AX("AMC.AX", "Amcor PLC"),
    XRO_AX("XRO.AX", "Xero Ltd"),
    SHL_AX("SHL.AX", "Sonic Healthcare Ltd"),
    DMP_AX("DMP.AX", "Domino’s Pizza Ent."),
    FMG_AX("FMG.AX", "Fortescue Metals Grp"),
    CSL_AX("CSL.AX", "CSL Ltd"),
    WBC_AX("WBC.AX", "Westpac Banking Corp"),
    BXB_AX("BXB.AX", "Brambles Ltd"),
    BHP_AX("BHP.AX", "BHP Group Ltd"),
    RIO_AX("RIO.AX", "Rio Tinto Ltd"),
    NXT_AX("NXT.AX", "NextDC Ltd"),
    MQG_AX("MQG.AX", "Macquarie Group Ltd"),
    AXJO_AX("^AXJO.AX", "S&P/ASX 200 Index"),
    REA_AX("REA.AX", "REA Group Ltd"),
    COH_AX("COH.AX", "Cochlear Ltd"),
    GMG_AX("GMG.AX", "Goodman Group"),
    JBH_AX("JBH.AX", "JB Hi-Fi Ltd"),
    ALL_AX("ALL.AX", "Aristocrat Leisure Ltd"),
    WOW_AX("WOW.AX", "Woolworths Group Ltd");

    private final String symbol;
    private final String displayName;

    StockName(String symbol, String displayName) {
        this.symbol      = symbol;
        this.displayName = displayName;
    }

    /** The exact string used in your CSV header, e.g. "WES.AX". */
    public String getSymbol() {
        return symbol;
    }

    /** Human‐friendly company name, e.g. "Wesfarmers Ltd". */
    public String getDisplayName() {
        return displayName;
    }

    public String getCompanyName(){
        return displayName;
    }

    @Override
    public String toString() {
        return symbol;
    }

    public static StockName fromString(String symbol) {
        for (StockName sn : values()) {
            if (sn.symbol.equals(symbol.trim())) {
                return sn;
            }
        }
        throw new IllegalArgumentException("Unknown stock symbol: " + symbol);
    }
}
