package com.javarepowizards.portfoliomanager.domain.price;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * In-memory implementation of the PriceHistory interface.
 * Stores a sorted list of PriceRecord objects and provides
 * methods to retrieve records by date or by date range.
 */
public class InMemoryPriceHistory implements PriceHistory {
    private final List<PriceRecord> records;   // sorted by date

    /**
     * Constructs an in-memory price history using the provided list of records.
     * Records must be sorted by date in ascending order and unmodifiable.
     *
     * @param records a pre-sorted, unmodifiable list of PriceRecord objects
     * @throws IllegalArgumentException if the provided list is not sorted by date
     */
    public InMemoryPriceHistory(List<PriceRecord> records) {
        if (!records.stream()
                .sorted(Comparator.comparing(PriceRecord::date))
                .toList()
                .equals(records)) {
            throw new IllegalArgumentException("Records must be sorted by date");
        }
        this.records = List.copyOf(records);
    }

    /**
     * Retrieves the price record for the specified date.
     *
     * @param date the date for which to retrieve the price record
     * @return an Optional containing the matching PriceRecord if found,
     *         or an empty Optional if no record exists for that date
     */
    @Override
    public Optional<PriceRecord> getRecord(LocalDate date) {
        return records.stream()
                .filter(r -> r.date().equals(date))
                .findFirst();
    }

    /**
     * Retrieves all price records within the given date range, inclusive.
     *
     * @param start the start date of the range
     * @param end   the end date of the range
     * @return a list of PriceRecord objects whose dates fall between
     *         start and end, inclusive; the list retains the original sort order
     */
    @Override
    public List<PriceRecord> getRecords(LocalDate start, LocalDate end) {
        return records.stream()
                .filter(r -> !r.date().isBefore(start) &&
                             !r.date().isAfter(end))
                .toList();
    }
}
