package com.example.inzynierka.obiekty;


import lombok.Getter;

import java.util.List;

@Getter
public class TransmitancjaCiagla {
    private double gain;
    private double R1;
    private double R2;
    private int Q1;
    private int Q2;
    private double T1;
    private double T2;
    private double T3;
    private int delay;
    private double Tp;
    private double Upp = 0;
    private double Ypp = 0;
    private double const1; //(-R1/T1+Q1)
    private double const2; //(-R2/T2+Q2)
    private Lag lag1;
    private Lag lag2;
    private Lag lag3;

    public TransmitancjaCiagla(double gain, double R1, int Q1, double R2,
                               int Q2, double T1,
                               double T2, double T3, int delay, double Tp) {
        this.gain = gain;
        this.R1 = R1;
        this.R2 = R2;
        this.Q1 = Q1;
        this.Q2 = Q2;
        this.T1 = T1;
        this.T2 = T2;
        this.T3 = T3;
        this.delay = delay;
        this.Tp = Tp;

        checkArgs(gain, R1, Q1, R2, Q2, T1, T2, T3, delay, Tp);
        this.const1 = -this.R1 / this.T1 + this.Q1;
        this.const2 = -this.R2 / this.T2 + this.Q2;
        lag1 = new Lag();
        lag2 = new Lag();
        lag3 = new Lag();
    }

    public double obliczKrok(List<Double> u) {


        double in = u.get(delay);
        double leadLag1;
        double leadLag2;
        double out;
        if (T1 > 0)
            leadLag1 = R1 / T1 * in + lag1.calc(T1, in) * const1;
        else if (Q1 != 0)
            leadLag1 = Q1 * in;
        else
            leadLag1 = in;

        if (T2 > 0 && (R2 > 0 || Q2 > 0))
            leadLag2 = R2 / T2 * leadLag1 + lag2.calc(T2, leadLag1) * const2;
        else if (T2 > 0)
            leadLag2 = leadLag1;
        else if (Q2 != 0)
            leadLag2 = Q2 * leadLag1;
        else
            leadLag2 = leadLag1;

        if (T3 > 0)
            out = gain * lag3.calc(T3, leadLag2);
        else
            out = gain * leadLag2;

        return out;
    }


    public void reset() {
        lag1.reset();
        lag2.reset();
        lag3.reset();
    }


    private class Lag {
        private double prev_x;
        private double prev_lag;

        Lag() {
            prev_x = 0;
            prev_lag = 0;
        }

        double calc(double T, double X) {
            if (T <= 0)
                return X;
            else if (T < Tp)
                T = Tp;

            double result = Tp * (X + prev_x) / (2 * T + Tp) + (2 * T - Tp) * prev_lag / (2 * T + Tp);
            prev_x = X;
            prev_lag = result;
            return result;
        }

        void reset() {
            prev_x = 0;
            prev_lag = 0;
        }
    }

    private void checkArgs(double gain, double R1, double Q1, double R2, double Q2, double T1,
                           double T2, double T3, double delay, double Tp)
            throws IllegalArgumentException {

        if (Tp <= 0)
            throw new IllegalArgumentException("Tp must be greater than 0");
        //denominator degree must be greater or equal to nominator
        int denomDeg = 0;
        int nomDeg = 0;
        if (T1 > 0) denomDeg++;
        if (T2 > 0) denomDeg++;
        if (T3 > 0) denomDeg++;
        if (R1 > 0) nomDeg++;
        if (R2 > 0) nomDeg++;
        if (denomDeg < nomDeg)
            throw new IllegalArgumentException("Denominator degree must be greater or equal to nominator");

        if (Q1 != -1 && Q1 != 0 && Q1 != 1)
            throw new IllegalArgumentException("Q1 have to be equal to -1 or 0 or 1");

        if (Q2 != -1 && Q2 != 0 && Q2 != 1)
            throw new IllegalArgumentException("Q2 have to be equal to -1 or 0 or 1");

        if (T1 <= 0 && R1 > 0) {
            System.out.println("T1 less or equal to 0. Force R1 to 0");
            this.R1 = 0;
        }

        if (T2 <= 0 && R2 > 0) {
            System.out.println("T2 less or equal to 0. Force R2 to 0");
            this.R2 = 0;
        }
    }
}
