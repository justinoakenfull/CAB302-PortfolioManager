package com.javarepowizards.portfoliomanager.models;

/**
 * Defines the available difficulty levels for portfolio simulation.
 * Each level has an associated integer symbol used for lookup or storage.
 */
public enum SimulationDifficulty {
    /**
     * Easy difficulty level, symbol 1.
     */
    Easy(1),
    /**
     * Medium difficulty level, symbol 2.
     */
    Medium(2),
    /**
     * Hard difficulty level, symbol 3.
     */
    Hard(3);


    private final int difficulty;

    /**
     * Constructs a SimulationDifficulty with the given integer symbol.
     *
     * @param difficulty the integer symbol representing this level
     */
    SimulationDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Returns the integer symbol associated with this difficulty level.
     *
     * @return the difficulty symbol
     */
    public int getDifficulty() { return difficulty; }

    /**
     * Looks up a SimulationDifficulty based on its integer symbol.
     * Iterates through all defined levels and returns the one whose
     * symbol matches the provided value.
     *
     * @param symbol the integer symbol to match
     * @return the matching SimulationDifficulty
     * @throws IllegalArgumentException if no matching symbol is found
     */
    public static SimulationDifficulty fromString(int symbol) {
        // Loop through all the enum constants available in SimulationDifficulty.
        for (SimulationDifficulty sn : values()) {
            return sn;
        }
        // If no match is found, throw an exception indicating the symbol is unknown.
        throw new IllegalArgumentException();
    }
}


