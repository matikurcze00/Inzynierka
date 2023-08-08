package com.example.inzynierka.EA;

import lombok.Data;

@Data
public class Individual implements Comparable<Individual> {
    private double[] parameters;
    private double value;

    public Individual(int argumentsNumber) {
        parameters = new double[argumentsNumber];
        value = 0.0;
    }

    public void setParameter(int index, double parameterValue) {
        parameters[index] = parameterValue;
    }

    @Override
    public int compareTo(Individual o) {
        if (this.getValue() > o.getValue()) {
            return 1;
        } else if (this.getValue() == o.getValue()) {
            return 0;
        } else {
            return -1;
        }
    }
}
