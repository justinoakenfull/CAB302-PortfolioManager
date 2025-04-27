package com.javarepowizards.portfoliomanager.services;
import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.services.StockDataFilter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StockDataFilterTest {

    @Test
    void getDataFromLastYear_filtersCorrectly() {
        LocalDate ref = LocalDate.of(2023, 12, 29);
        StockDataFilter filter = new StockDataFilter();

        // Build test data:
        //  - two years ago   -> should EXCLUDE
        //  - six months ago  -> should INCLUDE
        //  - exactly 1 year ago -> should INCLUDE
        //  - one year + 1 day ago -> should EXCLUDE
        StockData twoYearsAgo     = makeData(ref.minusYears(2),  10);
        StockData sixMonthsAgo    = makeData(ref.minusMonths(6), 20);
        StockData exactlyOneYear  = makeData(ref.minusYears(1),  30);
        StockData oneYearPlusDay  = makeData(ref.minusYears(1).minusDays(1), 40);

        List<StockData> all = List.of(
                twoYearsAgo,
                sixMonthsAgo,
                exactlyOneYear,
                oneYearPlusDay
        );

        List<StockData> lastYear = filter.getDataFromLastYear(all, ref);

        // Assert contains only the two we expect
        assertTrue( lastYear.contains(sixMonthsAgo),    "6-months-ago should be included" );
        assertTrue( lastYear.contains(exactlyOneYear),  "exactly 1 year ago should be included" );
        assertFalse(lastYear.contains(twoYearsAgo),     "2-years-ago should be excluded" );
        assertFalse(lastYear.contains(oneYearPlusDay),  "1-year-plus-1day should be excluded" );
    }

    /** Helper to make a StockData with a date and a dummy close value. */
    private StockData makeData(LocalDate date, double close) {
        StockData sd = new StockData(date);
        sd.setClose(close);
        return sd;
    }

}
