package com.javarepowizards.portfoliomanager.infrastructure;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Measures average execution time of a representative query
 * against the SQLite database, with nanosecond precision.
 * Fails if the average in milliseconds exceeds the threshold.
 * Steps:
 * 1. Open one shared connection.
 * 2. Disable auto-commit to avoid extra I/O.
 * 3. Run the prepared statement ITERATIONS times.
 * 4. Record each elapsed time in nanoseconds.
 * 5. Compute mean nanoseconds, convert to milliseconds (double),
 *    and assert it stays below MAX_AVG_MS.
 */
class DatabasePerformanceTest {

    /** Path to your SQLite DB file. */
    private static final Path DB_PATH = Path.of("database.db");

    /** JDBC URL for SQLite. */
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    /**
     * A representative join query using a username parameter.
     */
    private static final String QUERY =
            "SELECT ua.username, ub.balance " +
                    "FROM user_auth ua " +
                    "JOIN user_balances ub ON ua.user_id = ub.user_id " +
                    "WHERE ua.username = ?;";

    /** How many times to execute the query. */
    private static final int ITERATIONS = 500;

    /**
     * Fail if the mean execution time (in ms) is above this.
     */
    private static final double MAX_AVG_MS = 20.0;

    private static Connection connection;

    @BeforeAll
    static void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_URL);
        connection.setAutoCommit(true);
        // Warm up SQLite internals
        connection.createStatement().executeQuery("SELECT 1;").close();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Average user lookup latency under threshold")
    void averageUserLookupLatency() throws SQLException {
        List<Long> latenciesNanos = new ArrayList<>(ITERATIONS);

        try (PreparedStatement ps = connection.prepareStatement(QUERY)) {
            for (int i = 0; i < ITERATIONS; i++) {
                ps.setString(1, "test-user");

                long start = System.nanoTime();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                    }
                }
                latenciesNanos.add(System.nanoTime() - start);
            }
        }

        double avgNanos = latenciesNanos.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(Double.NaN);
        double avgMs = avgNanos / 1_000_000.0;

        System.out.printf("Average latency over %d runs: %.3f ms%n", ITERATIONS, avgMs);
        assertTrue(avgMs < MAX_AVG_MS,
                "Expected average < " + MAX_AVG_MS + " ms but was " +
                        String.format("%.3f", avgMs) + " ms");
    }
}
