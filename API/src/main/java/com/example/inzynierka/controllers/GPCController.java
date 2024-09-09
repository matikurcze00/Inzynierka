package com.example.inzynierka.controllers;

import Jama.Matrix;
import com.example.inzynierka.objects.MIMODiscrete;
import com.example.inzynierka.objects.SISODiscrete;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class GPCController extends AbstractMPCController {
    protected List<List<Double[]>> A;
    protected List<List<Double[]>> B; //IN->OUT
    protected List<List<Double>> U;
    protected List<List<Double[]>> Bz;
    protected List<List<Double>> Uz;
    protected List<List<Double>> Y;
    protected int disturbanceNumber = 0;
    private double[] uMin;
    private double[] uMax;
    private Integer IN;
    private Integer OUT;

    public GPCController(SISODiscrete sisoDiscrete, double lambda, double goal,
                         double duMax, Double[] presetTuning) {
        this(sisoDiscrete, lambda, goal, duMax);
        if (presetTuning[0] != null) {
            presetControlsNumbers = 1;
            this.presetControls = presetTuning;
            this.getLambda().set(0, presetTuning[0]);
        }
        calculateK();
    }

    public GPCController(SISODiscrete sisoDiscrete, double lambda, double goal, double duMax) {
        this.Lambda = new ArrayList<>(List.of(lambda));
        this.setpoint = new double[] {goal};
        this.duMax = duMax;
        calculateParameters(sisoDiscrete);
    }

    public GPCController(MIMODiscrete mimoDiscrete, int Nu, double[] goal, double duMax, Double[] presetTuning, double[] lambda) {
        this.presetControls = presetTuning;
        this.setpoint = goal;
        this.duMax = duMax;
        this.Nu = Nu;
        this.IN = mimoDiscrete.getEntriesNumber();
        this.OUT = mimoDiscrete.getOutputNumber();
        List<Double> tempLambda = new ArrayList<>();
        for (double wartosc : lambda) {
            tempLambda.add(wartosc);
        }
        this.Lambda = new ArrayList<>(tempLambda);
        for (int i = 0; i < presetTuning.length; i++) {
            if (presetTuning[i] != null) {
                presetControlsNumbers += 1;
                this.getLambda().set(i, presetTuning[i]);
            }
        }
        this.calculateParameters(mimoDiscrete);
    }

    @Override
    public double countControls(double previousOutput) {
        saveY(previousOutput);
        Matrix yZad = setMatrixYset();
        Matrix yNew = setMatrixYSISO();
        Matrix Utemp = this.K.times(yZad.transpose().minus(yNew.transpose()));
        double du = verifyUTemp(Utemp.get(0, 0));
        saveU(du);
        return du;
    }

    @Override
    public double countControls(double previousOutput, double[] disturbanceTuning) {
        saveUz(disturbanceTuning);
        return countControls(previousOutput);
    }

    @Override
    public double[] countControls(double[] previousOutput) {
        saveY(previousOutput);
        Matrix yZad = setMatrixYset();
        Matrix yNew = setMatrixYMIMO();
        Matrix Utemp = K.times(yZad.transpose().minus(yNew.transpose()));
        double[] output = new double[IN];
        for (int i = 0; i < IN; i++) {
            output[i] = verifyUTemp(Utemp.get(i, 0));
        }
        saveU(output);
        return output;
    }

    @Override
    public double[] countControls(double[] previousOutput, double[] disturbanceTuning) {
        saveUz(disturbanceTuning);
        return countControls(previousOutput);
    }

    @Override
    public void changeTuning(double[] parameters) {
        List<Double> tempLambda = new ArrayList();
        if (this.presetControlsNumbers == 0) {
            for (double parameter : parameters) {
                tempLambda.add(parameter);
            }
        } else {
            int iTemp = 0;
            for (int i = 0; i < getLambda().size(); i++) {
                if (presetControls[i] != null) {
                    tempLambda.add(presetControls[i]);
                } else {
                    tempLambda.add(parameters[iTemp]);
                    iTemp += 1;
                }
            }
        }
        setLambda(tempLambda);
        calculateK();
        resetController();
    }

    @Override
    public void resetController() {
        for (int i = 0; i < this.OUT; i++) {
            Y.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.IN; i++) {
            U.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.disturbanceNumber; i++) {
            Uz.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public void calculateParameters(SISODiscrete sisoDiscrete) {
        this.S = new ArrayList();

        setABSISO(sisoDiscrete);
        this.IN = 1;
        this.OUT = 1;
        this.Nu = 5;
        setUYSISO();
        this.uMax = new double[] {sisoDiscrete.getUMax()};
        this.uMin = new double[] {sisoDiscrete.getUMin()};
        calculateS();
        calculateM();
        calculateK();
        resetController();
        sisoDiscrete.resetObject();
    }

    private void setUYSISO() {
        this.Y = new ArrayList<>();
        this.Y.add(new ArrayList(Collections.nCopies(5, 0.0)));
        this.U = new ArrayList<>();
        this.U.add(new ArrayList(Collections.nCopies(5, 0.0)));
        if (this.disturbanceNumber > 0) {
            this.Uz = new ArrayList<>();
            for (int i = 0; i < disturbanceNumber; i++) {
                this.Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
            }
        }
    }

    private void setABSISO(SISODiscrete sisoDiscrete) {
        this.A = new ArrayList<>();
        List<Double[]> tempA = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tempA.add(new Double[] {sisoDiscrete.getA().get(i)});
        }
        A.add(tempA);
        this.B = new ArrayList<>();
        List<Double[]> tempB = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tempB.add(new Double[] {sisoDiscrete.getB().get(i)});
        }
        B.add(tempB);
        if (sisoDiscrete.getNumberOfDisturbance() > 0) {
            this.disturbanceNumber = sisoDiscrete.getNumberOfDisturbance();
            this.Bz = new ArrayList<>();
            List<Double[]> tempBz = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                tempBz.add(sisoDiscrete.getBz().get(i));
            }
            Bz.add(tempBz);
        }
    }

    public void calculateParameters(MIMODiscrete mimoDiscrete) {
        this.A = mimoDiscrete.getA();
        this.B = mimoDiscrete.getB();
        this.Bz = mimoDiscrete.getBz();
        this.disturbanceNumber = mimoDiscrete.getNumberOfDisturbance();
        setUYMIMO();
        this.uMax = mimoDiscrete.getUMax();
        this.uMin = mimoDiscrete.getUMin();
        calculateS();
        calculateM();
        calculateK();
        resetController();
        mimoDiscrete.resetObject();
    }

    private void setUYMIMO() {
        this.Y = new ArrayList<>();
        for (int i = 0; i < IN; i++) {
            this.Y.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
        this.U = new ArrayList<>();
        for (int i = 0; i < OUT; i++) {
            this.U.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
        if (this.disturbanceNumber > 0) {
            this.Uz = new ArrayList<>();
            for (int i = 0; i < disturbanceNumber; i++) {
                this.Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
            }
        }
    }

    @Override
    public int getNumberOfTuningParameters() {

        return getLambda().size() - presetControlsNumbers;
    }


    private void calculateS() {
        S = new ArrayList<>();
        for (int i = 0; i < OUT; i++) {
            for (int j = 0; j < IN; j++) {
                List<Double> Stemp = new ArrayList<>();
                int k = 2;
                calculateSk(i, j, Stemp, 0);
                calculateSk(i, j, Stemp, 1);
                while ((Math.abs(Stemp.get(k - 1) - Stemp.get(k - 2)) > 0.05) || Stemp.get(k - 2) == 0.0) {
                    calculateSk(i, j, Stemp, k);
                    k++;
                }
                S.add(Stemp);
            }
        }
        this.N = S.get(0).size();
        for (int i = 0; i < S.size(); i++) {
            if (N < S.get(i).size()) {
                N = S.get(i).size();
            }
        }
        for (int i = 0; i < S.size(); i++) {
            while (N != S.get(i).size()) {
                S.get(i).add(S.get(i).get(S.get(i).size() - 1));
            }
        }
    }

    private void calculateSk(int out, int in, List<Double> Stemp, int k) {
        Double Sk = 0.0;
        for (int m = 0; m < Math.min(k + 1, B.get(out).size()); m++) {
            Sk += B.get(out).get(m)[in];
        }
        for (int m = 0; m < Math.min(k, A.get(out).size()); m++) {
            Sk -= A.get(out).get(m)[out] * Stemp.get(k - 1 - m);
        }
        Stemp.add(Sk);
    }

    private void calculateM() {
        M = new Matrix(N * OUT, Nu * IN);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < N; j++) {
                if (j >= i) {
                    for (int k = 0; k < OUT; k++) {
                        for (int m = 0; m < IN; m++) {
                            M.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(j - i));
                        }
                    }
                }
            }
        }
    }


    private void calculateK() {
        Matrix I = new Matrix(Nu * IN, Nu * IN);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < Nu; j++) {
                for (int m = 0; m < IN; m++) {
                    if (i == j) {
                        I.set(i * IN + m, j * IN + m, getLambda().get(m));
                    } else {
                        I.set(i * IN + m, j * IN + m, 0);
                    }
                }
            }
        }
        this.K = ((M.transpose().times(M).plus(I)).inverse()).times(M.transpose());
    }

    public void countU(double du) {
        double Uakt = U.get(0).get(0) + du;
        if (Uakt > uMax[0]) {
            Uakt = uMax[0];
        } else if (Uakt < uMin[0]) {
            Uakt = uMin[0];
        }
        for (int i = U.size() - 1; i > 0; i--) {
            U.get(0).set(i, U.get(0).get(i - 1));
        }
        U.get(0).set(0, Uakt);
    }

    public void saveY(double output) {
        for (int j = Y.get(0).size() - 1; j > 0; j--) {
            Y.get(0).set(j, Y.get(0).get(j - 1));
        }
        Y.get(0).set(0, output);
    }

    public void saveU(double du) {
        for (int j = U.get(0).size() - 1; j > 0; j--) {
            U.get(0).set(j, U.get(0).get(j - 1));
        }
        if (U.get(0).get(0) + du > uMax[0]) {
            U.get(0).set(0, uMax[0]);
        } else if (U.get(0).get(0) + du < uMin[0]) {
            U.get(0).set(0, uMin[0]);
        } else {
            U.get(0).set(0, U.get(0).get(0) + du);
        }
    }

    public void saveY(double[] output) {
        for (int i = 0; i < Y.size(); i++) {
            for (int j = Y.get(i).size() - 1; j > 0; j--) {
                Y.get(i).set(j, Y.get(i).get(j - 1));
            }
            Y.get(i).set(0, output[i]);
        }
    }

    public void saveUz(double[] duz) {
        for (int i = 0; i < disturbanceNumber; i++) {
            for (int j = Uz.get(i).size() - 1; j > 0; j--) {
                Uz.get(i).set(j, Uz.get(i).get(j - 1));
            }
            Uz.get(i).set(0, Uz.get(i).get(0) + duz[i]);
        }
    }

    public void saveU(double[] du) {
        for (int i = 0; i < IN; i++) {
            for (int j = U.get(i).size() - 1; j > 0; j--) {
                U.get(i).set(j, U.get(i).get(j - 1));
            }
            if (U.get(i).get(0) + du[i] > uMax[i]) {
                U.get(i).set(0, uMax[i]);
            } else if (U.get(i).get(0) + du[i] < uMin[i]) {
                U.get(i).set(0, uMin[i]);
            } else {
                U.get(i).set(0, U.get(i).get(0) + du[i]);
            }
        }
    }

    public void countU(double[] du) {
        for (int j = 0; j < du.length; j++) {
            double Uakt = U.get(j).get(0) + du[j];
            if (Uakt > uMax[j]) {
                Uakt = uMax[j];
            } else if (Uakt < uMin[j]) {
                Uakt = uMin[j];
            }

            for (int i = U.get(j).size() - 1; i > 0; i--) {
                U.get(j).set(i, U.get(j).get(i - 1));
            }
            U.get(j).set(0, Uakt);
        }
    }

    public void countU(double du, int IN) {
        double Uakt = U.get(IN).get(0) + du;
        if (Uakt > uMax[IN]) {
            Uakt = uMax[IN];
        } else if (Uakt < uMin[IN]) {
            Uakt = uMin[IN];
        }
        for (int i = U.get(IN).size() - 1; i > 0; i--) {
            U.get(IN).set(i, U.get(IN).get(i - 1));
        }
        U.get(IN).set(0, Uakt);
    }

    protected Matrix setMatrixYset() {
        double[] goalTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            goalTemp[i] = setpoint[i % OUT];
        }
        return new Matrix(goalTemp, 1);
    }

    private Matrix setMatrixYSISO() {
        Matrix YMatrix = new Matrix(1, N);
        for (int i = 0; i < N; i++) {
            double yTemp = 0.0;
            yTemp += addBUSISO(i);
            yTemp += addDisturbanceSISO(i);
            yTemp += addYSISO(i);
            YMatrix.set(0, i, yTemp);
        }
        return YMatrix;
    }

    private Matrix setMatrixYMIMO() {
        Matrix YMatrix = new Matrix(1, N * OUT);
        for (int i = 0; i < N - 1; i++) {
            double[] yTemp = addBUMIMO(i);
            addDisturbanceMIMO(i, yTemp);
            addYMIMO(i, yTemp);
            for (int j = 0; j < OUT; j++) {
                YMatrix.set(0, i * OUT + j, yTemp[j]);
            }
        }
        return YMatrix;
    }

    private void addDisturbanceMIMO(int i, double[] yTemp) {
        for (int k = 0; k < OUT; k++) {
            for (int l = 0; l < disturbanceNumber; l++) {
                addDisturbanceRow(Bz, k, i, yTemp[k], Uz, l);
            }
        }
    }

    private void addDisturbanceRow(List<List<Double[]>> bz, int k, int i, double yTemp, List<List<Double>> uz, int l) {
        if (bz.get(k).size() > i) {
            for (int j = 0; j < bz.get(k).size() - i; j++) {
                yTemp += uz.get(l).get(bz.get(k).size() - 1 - i - j) * bz.get(k).get(bz.get(k).size() - 1 - j)[l];
            }
            for (int j = bz.get(k).size() - i; j < bz.get(k).size(); j++) {
                yTemp += uz.get(l).get(0) * bz.get(k).get(bz.get(k).size() - 1 - j)[l];
            }
        } else {
            for (int j = 0; j < bz.get(k).size(); j++) {
                yTemp += uz.get(l).get(0) * bz.get(k).get(j)[l];
            }
        }
    }

    private double[] addYMIMO(int i, double[] yTemp) {
        for (int k = 0; k < OUT; k++) {
            if (A.get(k).size() > i) {
                for (int j = 0; j < A.get(k).size() - i; j++) {
                    yTemp[k] -= Y.get(k).get(A.get(k).size() - 1 - i - j) * A.get(k).get(A.get(k).size() - 1 - j)[k];
                }
                for (int j = A.get(k).size() - i; j < A.get(k).size(); j++) {
                    yTemp[k] -= Y.get(k).get(0) * A.get(k).get(A.get(k).size() - 1 - j)[k];
                }
            } else {
                for (int j = 0; j < A.get(k).size(); j++) {
                    yTemp[k] -= Y.get(k).get(0) * A.get(k).get(j)[k];
                }
            }
        }
        return yTemp;
    }

    private double[] addBUMIMO(int i) {
        double[] yTemp = new double[OUT];
        for (int k = 0; k < OUT; k++) {
            for (int l = 0; l < IN; l++) {
                addDisturbanceRow(B, k, i, yTemp[k], U, l);
            }
        }
        return yTemp;
    }

    private double addYSISO(int i) {
        double yTemp = 0.0;
        if (A.get(0).size() > i) {
            for (int j = 0; j < A.get(0).size() - i; j++) {
                yTemp -= Y.get(0).get(A.get(0).size() - 1 - i - j) * A.get(0).get(A.get(0).size() - 1 - j)[0];
            }
            for (int j = A.get(0).size() - i; j < A.get(0).size(); j++) {
                yTemp -= Y.get(0).get(0) * A.get(0).get(A.get(0).size() - 1 - j)[0];
            }
        } else {
            for (int j = 0; j < A.get(0).size(); j++) {
                yTemp -= Y.get(0).get(0) * A.get(0).get(j)[0];
            }
        }
        return yTemp;
    }

    private double addDisturbanceSISO(int i) {
        double yTemp = 0.0;
        for (int l = 0; l < disturbanceNumber; l++) {
            addDisturbanceRow(Bz, 0, i, yTemp, Uz, l);
        }
        return yTemp;
    }

    private double addBUSISO(int i) {
        double yTemp = 0.0;
        addDisturbanceRow(B, 0, i, yTemp, U, 0);
        return yTemp;
    }

    protected double verifyUTemp(double Utemp) {
        if (Utemp > duMax) {
            return duMax;
        } else if (Utemp < -duMax) {
            return -duMax;
        } else {
            return Utemp;
        }
    }
}
