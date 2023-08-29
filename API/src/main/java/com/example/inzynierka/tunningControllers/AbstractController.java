package com.example.inzynierka.tunningControllers;

import lombok.Data;

@Data
abstract public class AbstractController {
    double[] setpoint;
    protected double duMax;
    protected Double[] presetControls;
    protected int presetControlsNumbers = 0;

    public abstract double countControls(double previousOutput);

    public abstract double countControls(double previousOutput, double[] disturbanceTuning);

    public abstract double[] countControls(double[] previousOutput);

    public abstract double[] countControls(double[] previousOutput, double[] disturbanceTuning);

    public abstract void changeTuning(double[] parameters);

    public abstract void resetController();

    public abstract int getNumberOfTuningParameters();
}
