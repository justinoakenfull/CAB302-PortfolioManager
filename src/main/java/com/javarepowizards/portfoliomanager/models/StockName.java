package com.javarepowizards.portfoliomanager.models;

/**
 * Enumeration of supported ASX stock symbols and their corresponding display names.
 * Each constant holds the exact symbol used in CSV headers and a human-friendly name.
 */
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

    /**
     * Constructs a StockName constant with its CSV symbol and display name.
     *
     * @param symbol the exact string used in CSV headers, for example "WES.AX"
     * @param displayName the human-friendly name, for example "Wesfarmers Ltd"
     */
    StockName(String symbol, String displayName) {
        this.symbol      = symbol;
        this.displayName = displayName;
    }

    /**
     * Returns the symbol used in CSV headers.
     *
     * @return the CSV symbol string
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the human-friendly company name.
     *
     * @return the display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Alias for getDisplayName.
     *
     * @return the display name string
     */
    public String getCompanyName(){
        return displayName;
    }

    /**
     * Returns the symbol as the string representation.
     *
     * @return the CSV symbol
     */
    @Override
    public String toString() {
        return symbol;
    }

    /**
     * Parses the provided string to its corresponding StockName constant.
     *
     * @param symbol the CSV symbol or string to parse
     * @return the matching StockName constant
     * @throws IllegalArgumentException if the symbol does not match any constant
     */
    public static StockName fromString(String symbol) {
        for (StockName sn : values()) {
            if (sn.symbol.equals(symbol.trim())) {
                return sn;
            }
        }
        throw new IllegalArgumentException("Unknown stock symbol: " + symbol);
    }
}
