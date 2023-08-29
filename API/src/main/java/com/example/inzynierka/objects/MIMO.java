package com.example.inzynierka.objects;

import com.example.inzynierka.tunningControllers.AbstractController;
import lombok.Data;

import java.util.List;

@Data
public abstract class MIMO {
    int outputNumber;
    int entriesNumber;
    int delayMax;
    String typeOfError;
    int length;
    List<List<Double>> U;
    List<List<Double>> Y;
    double[] YMax;
    double[] uMin;
    double[] uMax;

    public abstract double[] simulateStep(double[] du);

    public abstract double[] simulateStep(double[] du, double[] dUz);

    public abstract double simulateStep(double du, int IN, int OUT);

    public abstract double simulateDisruptionStep(double du, int IN, int OUT);

    public abstract double[] getOutput();

    public abstract double simulateObjectRegulation(AbstractController abstractController, double[] cel);

    public abstract void resetObject();

}
