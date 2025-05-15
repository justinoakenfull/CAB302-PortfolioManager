package com.javarepowizards.portfoliomanager.domain;

import com.javarepowizards.portfoliomanager.domain.stock.StockDescription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockDescriptionTest {

    @Test
    void gettersReturnConstructorValues() {
        String shortDesc = "Short summary";
        String longDesc  = "A much longer, detailed description of the stock’s business and outlook.";

        StockDescription desc = new StockDescription(shortDesc, longDesc);

        assertEquals(shortDesc, desc.getShortDescription(),
                "getShortDescription() should return the value passed to the constructor");
        assertEquals(longDesc, desc.getLongDescription(),
                "getLongDescription() should return the value passed to the constructor");
    }

    @Test
    void nullValuesAreAllowed() {
        StockDescription desc = new StockDescription(null, null);

        assertNull(desc.getShortDescription(),
                "If null was passed as shortDescription, getter should return null");
        assertNull(desc.getLongDescription(),
                "If null was passed as longDescription, getter should return null");
    }
}
