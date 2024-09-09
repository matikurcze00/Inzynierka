package com.example.inzynierka.controllers;

import lombok.Data;

@Data
public abstract class AbstractController {
    protected double duMax;
    protected Double[] presetControls;
    protected int presetControlsNumbers = 0;
    double[] setpoint;

    public abstract double countControls(double previousOutput);

    public abstract double countControls(double previousOutput, double[] disturbanceTuning);

    public abstract double[] countControls(double[] previousOutput);

    public abstract double[] countControls(double[] previousOutput, double[] disturbanceTuning);

    public abstract void changeTuning(double[] parameters);

    public abstract void resetController();

    public abstract int getNumberOfTuningParameters();
}
