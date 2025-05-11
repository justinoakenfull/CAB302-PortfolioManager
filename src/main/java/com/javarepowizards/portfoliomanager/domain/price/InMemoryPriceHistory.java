package com.javarepowizards.portfoliomanager.domain.price;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryPriceHistory implements PriceHistory {
    private final List<PriceRecord> records;   // sorted by date

    /**
     * Expects callers to provide a pre-sorted, unmodifiable List.
     */
    public InMemoryPriceHistory(List<PriceRecord> records) {
        if (!records.stream()
                .sorted(Comparator.comparing(PriceRecord::getDate))
                .toList()
                .equals(records)) {
            throw new IllegalArgumentException("Records must be sorted by date");
        }
        this.records = List.copyOf(records);
    }

    @Override
    public Optional<PriceRecord> getRecord(LocalDate date) {
        return records.stream()
                .filter(r -> r.getDate().equals(date))
                .findFirst();
    }

    @Override
    public List<PriceRecord> getRecords(LocalDate start, LocalDate end) {
        return records.stream()
                .filter(r -> !r.getDate().isBefore(start) &&
                        !r.getDate().isAfter(end))
                .toList();
    }
}
