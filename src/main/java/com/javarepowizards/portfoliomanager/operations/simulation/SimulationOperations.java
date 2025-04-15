package com.javarepowizards.portfoliomanager.operations.simulation;

public class SimulationOperations {

    private final investmentTarget target;




    private enum investmentTarget{
        EASY,
        MEDIUM,
        HARD
    }

    
    public SimulationOperations(investmentTarget target) {
        this.target = target;
        
    }
    
    
    private double investmentTargetToDouble(investmentTarget target) {
        switch (target) {
            case EASY:
                return 9.35;
            case MEDIUM:
                return 15.43;
            case HARD:
                return 20.45;
            default:
                throw new IllegalArgumentException("Unknown investment target: " + target);
        }
    }

    private String investmentTargetToString(investmentTarget target) {
        switch (target) {
            case EASY:
                return "ASX All Ordinaries Index Fund";
            case MEDIUM:
                return "Smallco Broadcap Fund";
            case HARD:
                return "Selector High Conviction Fund";
            default:
                throw new IllegalArgumentException("Unknown investment target: " + target);
        }
    }

    //private double[] marketSimulator(double investment, int years, double target){
   // }
}
