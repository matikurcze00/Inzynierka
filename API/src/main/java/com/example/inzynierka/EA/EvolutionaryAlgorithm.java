package com.example.inzynierka.EA;

import com.example.inzynierka.objects.MIMO;
import com.example.inzynierka.objects.SISO;
import com.example.inzynierka.tunningControllers.AbstractController;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class EvolutionaryAlgorithm {
    private static int sigma = 15;
    private int populationNumber;
    private int iterationNumber;
    private int mu;
    private int recombinationNumber;
    private int mutationNumber;
    private double mutationProbability;
    private List<Individual> population;
    private int changeFactor = 0;

    public EvolutionaryAlgorithm(int populationNumber, int iterationNumber, int mu, double mutationProbability, double recombinationFrequency) {
        this.populationNumber = populationNumber;
        this.iterationNumber = iterationNumber;
        this.mu = mu;
        this.mutationProbability = mutationProbability;
        this.recombinationNumber = (int) Math.floor(mu * recombinationFrequency);
        this.mutationNumber = mu - recombinationNumber;
    }

    public double[] getTuningParameters(int argumentsNumber, AbstractController abstractController, SISO object) {
        population = new ArrayList<>();
        Random r = new Random();
        double[] setpoint = new double[] {object.getYMax() / 5};
        abstractController.setSetpoint(setpoint);
        Initialization(argumentsNumber, abstractController, object, r, setpoint);
        Collections.sort(population);
        for (int k = 0; k < iterationNumber; k++) {
            evolution(argumentsNumber, abstractController, object, setpoint, k);
        }
        return population.get(0).getParameters();
    }

    private void Initialization(int argumentsNumber, AbstractController abstractController, SISO object, Random r, double[] setpoint) {
        for (int i = 0; i < populationNumber; i++) {
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.setParameter(j, r.nextDouble(5.0));
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            abstractController.resetController();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            population.add(individualTemp);
        }
    }

    private void evolution(int argumentsNumber, AbstractController abstractController, SISO object, double[] setpoint, int iteration) {
        Random r = new Random();
        List<Individual> reproduction = new ArrayList<>(population);
        recombination(argumentsNumber, abstractController, object, setpoint, r, reproduction);
        mutations(argumentsNumber, abstractController, object, setpoint, r, reproduction);
        Collections.sort(reproduction);
        if (reproduction.get(0).getValue() < population.get(0).getValue()) {
            changeFactor += 1;
        }

        if ((iteration + 1) % sigma == 0) {
            setChangeFactor();
        }
        population = reproduction.stream().limit(populationNumber).collect(Collectors.toList());
    }

    private void mutations(int argumentsNumber, AbstractController abstractController, SISO object, double[] setpoint, Random r, List<Individual> reproduction) {
        for (int i = 0; i < mutationNumber; i++) {
            int parent = r.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] =
                    (r.nextDouble() < getMutationProbability()) ? Math.abs(r.nextGaussian(population.get(parent).getParameters()[j], 0.4)) :
                        population.get(parent).getParameters()[j];
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            abstractController.resetController();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            reproduction.add(individualTemp);
        }
    }

    private void recombination(int argumentsNumber, AbstractController abstractController, SISO object, double[] setpoint, Random r, List<Individual> reproduction) {
        for (int i = 0; i < recombinationNumber; i++) {
            int individual1 = r.nextInt(populationNumber);
            int individual2 = r.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] = (r.nextBoolean()) ? population.get(individual1).getParameters()[j] : population.get(individual2).getParameters()[j];
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            abstractController.resetController();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            reproduction.add(individualTemp);
        }
    }

    public double[] getTuningParameters(int argumentsNumber, AbstractController abstractController, MIMO object) {
        population = new ArrayList<>();
        Random r = new Random();
        double[] setpoint = Arrays.copyOf(object.getYMax(), object.getYMax().length);
        for (int i = 0; i < setpoint.length; i++) {
            setpoint[i] = setpoint[i] / 5;
        }

        abstractController.setSetpoint(setpoint);
        for (int i = 0; i < populationNumber; i++) {
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.setParameter(j, r.nextDouble(3.0));
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            population.add(individualTemp);
        }
        Collections.sort(population);
        for (int k = 0; k < iterationNumber; k++) {
            evolution(argumentsNumber, abstractController, object, setpoint, k);
        }
        Collections.sort(population);
        return population.get(0).getParameters();
    }

    private void evolution(int argumentsNumber, AbstractController abstractController, MIMO object, double[] setpoint, int iteration) {
        Random r = new Random();
        List<Individual> reproduction = new ArrayList<>(population);
        recombination(argumentsNumber, abstractController, object, setpoint, r, reproduction);
        mutations(argumentsNumber, abstractController, object, setpoint, r, reproduction);
        Collections.sort(reproduction);
        if (reproduction.get(0).getValue() < population.get(0).getValue()) {
            changeFactor += 1;
        }

        if ((iteration + 1) % sigma == 0) {
            setChangeFactor();
        }
        population = reproduction.stream().limit(populationNumber).collect(Collectors.toList());
    }

    private void mutations(int argumentsNumber, AbstractController abstractController, MIMO object, double[] setpoint, Random r, List<Individual> reproduction) {
        for (int i = 0; i < mutationNumber; i++) {
            int parent = r.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] =
                    (r.nextDouble() < getMutationProbability()) ? Math.abs(r.nextGaussian(population.get(parent).getParameters()[j], 0.6)) :
                        population.get(parent).getParameters()[j];
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            reproduction.add(individualTemp);
        }
    }

    private void recombination(int argumentsNumber, AbstractController abstractController, MIMO obiekt, double[] setpoint, Random r, List<Individual> reproduction) {
        for (int i = 0; i < recombinationNumber; i++) {
            int individual1 = r.nextInt(populationNumber);
            int individual2 = r.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] = (r.nextBoolean()) ? population.get(individual1).getParameters()[j] : population.get(individual2).getParameters()[j];
            }
            abstractController.changeTuning(individualTemp.getParameters());
            obiekt.resetObject();
            individualTemp.setValue(obiekt.simulateObjectRegulation(abstractController, setpoint));
            reproduction.add(individualTemp);
        }
    }

    private void setChangeFactor() {
        if (changeFactor > sigma / 5) {
            mutationProbability = mutationProbability * 0.9;
        } else if (changeFactor < sigma / 5) {
            mutationProbability = mutationProbability / 0.9;
        }
        changeFactor = 0;
    }
}
