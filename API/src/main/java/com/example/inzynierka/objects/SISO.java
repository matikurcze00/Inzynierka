package com.example.inzynierka.objects;

import com.example.inzynierka.tunningControllers.AbstractController;
import lombok.Data;

import java.util.List;

@Data
public abstract class SISO {
    List<Double> Y;
    List<Double> U;
    double uMin;
    double uMax;
    double YMax;
    int length;
    String typeOfError;
    double Ypp;

    public abstract Double getOutput();

    public abstract void resetObject();

    public abstract double simulateObjectRegulation(AbstractController abstractController, double[] cel);

    public abstract double simulateStep(double du);

    public abstract double simulateStep(double du, double[] duZ);

    public abstract double simulateDisruptionStep(double du, int zaklocenie);
}
