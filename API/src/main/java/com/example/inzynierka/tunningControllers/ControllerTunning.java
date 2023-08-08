package com.example.inzynierka.tunningControllers;

import lombok.Data;

@Data
abstract public class ControllerTunning {
    double[] setpoint;
    protected double duMax;
    protected Double[] presetControls;
    protected int presetControlsNumbers = 0;

    public abstract double countControls(double aktualna);

    public abstract double countControls(double aktualna, double[] disturbanceTuning);

    public abstract double[] countControls(double[] aktualna);

    public abstract double[] countControls(double[] aktualna, double[] disturbanceTuning);

    public abstract void changeTuning(double[] wartosci);

    public abstract void resetController();

    public abstract int getNumberOfTuningParameters();
}
