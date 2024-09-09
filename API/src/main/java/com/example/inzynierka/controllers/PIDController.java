package com.example.inzynierka.controllers;


import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class PIDController extends AbstractController {
    private double K;
    private double Ti;
    private double Td;
    private double Ts;
    private List<Double> E = Arrays.asList(0.0, 0.0, 0.0);
    private double r0;
    private double r1;
    private double r2;
    private double uMax;

    public PIDController(double P, double I, double D, double Ts, double[] goal, double duMax,
                         double uMax, Double[] presetControls) {

        this(P, I, D, Ts, goal, duMax, uMax);
        if (presetControls[0] != null) {
            this.K = presetControls[0];
        }
        if (presetControls[1] != null) {
            this.Ti = presetControls[1];
        }
        if (presetControls[2] != null) {
            this.Td = presetControls[2];
        }
        calculateParameters();
        resetController();
        this.presetControls = presetControls;
        int tempNumber = 0;
        for (Double wartosc : presetControls) {
            if (wartosc != null) {
                tempNumber += 1;
            }
        }
        this.presetControlsNumbers = tempNumber;

    }

    public PIDController(double P, double I, double D, double Ts, double[] goal, double duMax, double uMax) {
        this.K = P;
        this.Ti = I;
        this.Td = D;
        this.Ts = Ts;
        this.setpoint = goal;
        this.duMax = duMax;
        this.uMax = uMax;
        calculateParameters();
        resetController();
    }

    @Override
    public double countControls(double previousOutput) {
        //Wyliczanie błędu
        E.set(2, E.get(1));
        E.set(1, E.get(0));
        E.set(0, setpoint[0] - previousOutput);

        //
        double du = r0 * E.get(0) + r1 * E.get(1) + r2 * E.get(2);
        if (du > duMax) {
            du = duMax;
        } else if (du < -duMax) {
            du = -duMax;
        }
        return du;
    }

    @Override
    public double countControls(double previousOutput, double[] disturbanceTuning) {
        return countControls(previousOutput);
    }

    @Override
    public double[] countControls(double[] previousOutput) {
        return new double[0];
    }

    @Override
    public double[] countControls(double[] previousOutput, double[] disturbanceTuning) {
        return new double[0];
    }

    @Override
    public void changeTuning(double[] parameters) {
        if (this.presetControlsNumbers == 0) {
            this.K = parameters[0];
            this.Ti = parameters[1];
            this.Td = parameters[2];
        } else {
            int iTemp = 0;
            if (presetControls[0] == null) {
                this.K = parameters[iTemp];
                iTemp += 1;
            }
            if (presetControls[1] == null) {
                this.Ti = parameters[iTemp];
                iTemp += 1;
            }
            if (presetControls[2] == null) {
                this.Td = parameters[iTemp];
            }
        }
        calculateParameters();
        resetController();
    }

    @Override
    public void resetController() {
        E = Arrays.asList(0.0, 0.0, 0.0);

    }

    private void calculateParameters() {
        //ISA
        r2 = K * Td / Ts;
        r1 = -K * (1 + 2 * Td / Ts - Ts / (2 * Ti));
        r0 = K * (1 + Ts / (2 * Ti) + Td / Ts);
    }

    public int getNumberOfTuningParameters() {
        return 3 - this.presetControlsNumbers;
    }
}
