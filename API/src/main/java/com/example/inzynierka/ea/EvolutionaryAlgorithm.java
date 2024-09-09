package com.example.inzynierka.ea;

import com.example.inzynierka.controllers.AbstractController;
import com.example.inzynierka.exception.SecureRandomAlgorithmException;
import com.example.inzynierka.objects.MIMO;
import com.example.inzynierka.objects.SISO;
import lombok.Data;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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

    private Random rand;

    public EvolutionaryAlgorithm(int populationNumber, int iterationNumber, int mu, double mutationProbability, double recombinationFrequency) {
        try {
            rand = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw SecureRandomAlgorithmException.assigningSecureRandomException(e.getMessage());
        }
        this.populationNumber = populationNumber;
        this.iterationNumber = iterationNumber;
        this.mu = mu;
        this.mutationProbability = mutationProbability;
        this.recombinationNumber = (int) Math.floor(mu * recombinationFrequency);
        this.mutationNumber = mu - recombinationNumber;
    }

    public double[] getTuningParameters(int argumentsNumber, AbstractController abstractController, SISO object) {
        population = new ArrayList<>();
        double[] setpoint = new double[] {object.getYMax() / 5};
        abstractController.setSetpoint(setpoint);
        Initialization(argumentsNumber, abstractController, object, setpoint);
        Collections.sort(population);
        for (int k = 0; k < iterationNumber; k++) {
            evolution(argumentsNumber, abstractController, object, setpoint, k);
        }
        return population.get(0).getParameters();
    }

    private void Initialization(int argumentsNumber, AbstractController abstractController, SISO object, double[] setpoint) {
        for (int i = 0; i < populationNumber; i++) {
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.setParameter(j, rand.nextDouble(5.0));
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            abstractController.resetController();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            population.add(individualTemp);
        }
    }

    private void evolution(int argumentsNumber, AbstractController abstractController, SISO object, double[] setpoint, int iteration) {
        List<Individual> reproduction = new ArrayList<>(population);
        recombination(argumentsNumber, abstractController, object, setpoint, reproduction);
        mutations(argumentsNumber, abstractController, object, setpoint, reproduction);
        Collections.sort(reproduction);
        if (reproduction.get(0).getValue() < population.get(0).getValue()) {
            changeFactor += 1;
        }

        if ((iteration + 1) % sigma == 0) {
            setChangeFactor();
        }
        population = reproduction.stream().limit(populationNumber).collect(Collectors.toList());
    }

    private void mutations(int argumentsNumber, AbstractController abstractController, SISO object, double[] setpoint, List<Individual> reproduction) {
        for (int i = 0; i < mutationNumber; i++) {
            int parent = rand.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] =
                    (rand.nextDouble() < getMutationProbability()) ? Math.abs(rand.nextGaussian(population.get(parent).getParameters()[j], 0.4)) :
                        population.get(parent).getParameters()[j];
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            abstractController.resetController();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            reproduction.add(individualTemp);
        }
    }

    private void recombination(int argumentsNumber, AbstractController abstractController, SISO object, double[] setpoint, List<Individual> reproduction) {
        for (int i = 0; i < recombinationNumber; i++) {
            int individual1 = rand.nextInt(populationNumber);
            int individual2 = rand.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] =
                    (rand.nextBoolean()) ? population.get(individual1).getParameters()[j] : population.get(individual2).getParameters()[j];
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
        double[] setpoint = Arrays.copyOf(object.getYMax(), object.getYMax().length);
        for (int i = 0; i < setpoint.length; i++) {
            setpoint[i] = setpoint[i] / 5;
        }

        abstractController.setSetpoint(setpoint);
        for (int i = 0; i < populationNumber; i++) {
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.setParameter(j, rand.nextDouble(3.0));
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
        List<Individual> reproduction = new ArrayList<>(population);
        recombination(argumentsNumber, abstractController, object, setpoint, reproduction);
        mutations(argumentsNumber, abstractController, object, setpoint, reproduction);
        Collections.sort(reproduction);
        if (reproduction.get(0).getValue() < population.get(0).getValue()) {
            changeFactor += 1;
        }

        if ((iteration + 1) % sigma == 0) {
            setChangeFactor();
        }
        population = reproduction.stream().limit(populationNumber).collect(Collectors.toList());
    }

    private void mutations(int argumentsNumber, AbstractController abstractController, MIMO object, double[] setpoint, List<Individual> reproduction) {
        for (int i = 0; i < mutationNumber; i++) {
            int parent = rand.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] =
                    (rand.nextDouble() < getMutationProbability()) ? Math.abs(rand.nextGaussian(population.get(parent).getParameters()[j], 0.6)) :
                        population.get(parent).getParameters()[j];
            }
            abstractController.changeTuning(individualTemp.getParameters());
            object.resetObject();
            individualTemp.setValue(object.simulateObjectRegulation(abstractController, setpoint));
            reproduction.add(individualTemp);
        }
    }

    private void recombination(int argumentsNumber, AbstractController abstractController, MIMO obiekt, double[] setpoint, List<Individual> reproduction) {
        for (int i = 0; i < recombinationNumber; i++) {
            int individual1 = rand.nextInt(populationNumber);
            int individual2 = rand.nextInt(populationNumber);
            Individual individualTemp = new Individual(argumentsNumber);
            for (int j = 0; j < argumentsNumber; j++) {
                individualTemp.getParameters()[j] =
                    (rand.nextBoolean()) ? population.get(individual1).getParameters()[j] : population.get(individual2).getParameters()[j];
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
