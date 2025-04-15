// This package declaration indicates the enum belongs to the models package of the portfolio manager.
package com.javarepowizards.portfoliomanager.models;

/**
 * The StockName enum represents a fixed set of predefined stock symbols.
 * Each enum constant holds the string value of a stock's symbol (as used on the exchange).
 * Using an enum here provides type safety when handling stocks and eases the maintenance of
 * the list of valid symbols.
 */
public enum StockName {
    // Enum constants with their corresponding stock symbol strings.
    // For instance, WES_AX represents a stock with the symbol "WES.AX".
    WES_AX("WES.AX"),
    TLS_AX("TLS.AX"),
    AMC_AX("AMC.AX"),
    XRO_AX("XRO.AX"),
    SHL_AX("SHL.AX"),
    DMP_AX("DMP.AX"),
    FMG_AX("FMG.AX"),
    CSL_AX("CSL.AX"),
    WBC_AX("WBC.AX"),
    BXB_AX("BXB.AX"),
    BHP_AX("BHP.AX"),
    RIO_AX("RIO.AX"),
    NXT_AX("NXT.AX"),
    MQG_AX("MQG.AX"),
    AXJO_AX("^AXJO.AX"),
    REA_AX("REA.AX"),
    COH_AX("COH.AX"),
    GMG_AX("GMG.AX"),
    JBH_AX("JBH.AX"),
    ALL_AX("ALL.AX"),
    WOW_AX("WOW.AX");


    /**
     * The symbol field stores the string representation of the stock symbol for each enum constant.
     * It is private and final because each constant should have its unique, immutable symbol.
     */
    private final String symbol;

    /**
     * Constructor for the StockName enum.
     * This constructor is called implicitly for each enum constant, associating each constant with its symbol string.
     *
     * @param symbol the stock symbol as a String (e.g., "WES.AX")
     */
    StockName(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Retrieves the string representation (symbol) of the stock.
     *
     * @return the stock symbol, for example "WES.AX" for the WES_AX constant.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Converts a given string into the corresponding StockName enum constant.
     * This method iterates over all defined enum constants and compares their symbols with
     * the input string (after trimming any extraneous whitespace).
     *
     * @param symbol the string representation of a stock (e.g., "WES.AX")
     * @return the corresponding StockName constant if a match is found.
     * @throws IllegalArgumentException if the input does not match any known stock symbol.
     */
    public static StockName fromString(String symbol) {
        // Loop through all the enum constants available in StockName.
        for (StockName sn : values()) {
            // Compare the stored symbol of the enum constant with the provided string, using trim() to remove extra spaces.
            if (sn.symbol.equals(symbol.trim())) {
                // Return the matching enum constant if found.
                return sn;
            }
        }
        // If no match is found, throw an exception indicating the symbol is unknown.
        throw new IllegalArgumentException("Unknown stock symbol: " + symbol);
    }
}
