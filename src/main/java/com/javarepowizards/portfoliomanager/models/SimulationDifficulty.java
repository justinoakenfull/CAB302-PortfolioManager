package com.javarepowizards.portfoliomanager.models;

public enum SimulationDifficulty {
    Easy(1),
    Medium(2),
    Hard(3);


    private final int difficulty;

    SimulationDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getDifficulty() { return difficulty; }


    public static SimulationDifficulty fromString(int symbol) {
        // Loop through all the enum constants available in SimulationDifficulty.
        for (SimulationDifficulty sn : values()) {
            return sn;
        }
        // If no match is found, throw an exception indicating the symbol is unknown.
        throw new IllegalArgumentException();
    }
}


