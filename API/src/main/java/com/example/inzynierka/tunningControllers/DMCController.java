package com.example.inzynierka.tunningControllers;

import Jama.Matrix;
import com.example.inzynierka.objects.MIMODPA;
import com.example.inzynierka.objects.SISODPA;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class DMCController extends AbstractMPCController {

    protected Matrix Mpz;
    protected Matrix Mz;
    protected List<List<Double>> Sz;
    protected Matrix dU;
    protected Matrix dUz;
    protected Matrix Mp;

    public DMCController() {
    }

    public DMCController(int Nu, double lambda, SISODPA object, double cel, double duMax, int N, Double[] presetTuning) {
        this(Nu, lambda, object, cel, duMax, N);
        if (presetTuning[0] != null) {
            presetControlsNumbers = 1;
            this.presetControls = presetTuning;
            List temp = new ArrayList();
            temp.add(presetTuning[0]);
            this.setLambda(temp);
            this.calculateParameters(object);
        }
    }

    public DMCController(int Nu, double lambda, SISODPA object, double cel, double duMax, int N) {
        this.Lambda = List.of(lambda);
        this.Nu = Nu;
        this.N = N;
        this.setpoint = new double[] {cel};
        this.duMax = duMax;
        calculateParameters(object);

    }

    public DMCController(int Nu, double[] lambda, MIMODPA object, double[] cel, double duMax, int N, Double[] presetTuning) {
        this(Nu, lambda, object, cel, duMax, N);
        this.presetControlsNumbers = 0;
        this.presetControls = presetTuning;
        for (int i = 0; i < presetTuning.length; i++) {
            if (presetTuning[i] != null) {
                presetControlsNumbers += 1;
                this.getLambda().set(i, presetTuning[i]);
            }
        }
        this.calculateParameters(object);
    }

    public DMCController(int Nu, double[] lambda, MIMODPA object, double[] setpoint, double duMax, int N) {
        List<Double> tempLambda = new ArrayList<>();
        for (double wartosc : lambda) {
            tempLambda.add(wartosc);
        }
        this.Lambda = new ArrayList<>(tempLambda);
        this.Nu = Nu;
        this.N = N;
        this.setpoint = setpoint;
        this.duMax = duMax;
        calculateParameters(object);
    }

    public double countControls(double previousOutput) {
        Matrix yZad = ustawMatrixYZad();
        double[] yTemp = new double[N];
        Arrays.fill(yTemp, previousOutput);
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose())));
        verifyUTemp(Utemp, 0);
        savedU(Utemp.get(0, 0));
        return Utemp.get(0, 0);
    }

    public double countControls(double previousOutput, double[] disturbanceTuning) {
        Matrix yZad = ustawMatrixYZad();
        savedUz(disturbanceTuning);
        double[] yTemp = new double[N];
        Arrays.fill(yTemp, previousOutput);
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose()))
            .minus(Mpz.times(dUz.transpose())));
        verifyUTemp(Utemp, 0);
        savedU(Utemp.get(0, 0));
        return Utemp.get(0, 0);
    }

    public double[] countControls(double[] previousOutput) {
        int OUT = setpoint.length;
        double[] celTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            celTemp[i] = setpoint[i % OUT];
        }
        Matrix yZad = new Matrix(celTemp, 1);
        double[] yTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            yTemp[i] = previousOutput[i % OUT];
        }
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose())));
        double[] tempdU = new double[getLambda().size()];
        for (int i = 0; i < getLambda().size(); i++) {
            verifyUTemp(Utemp, i);
            tempdU[i] = Utemp.get(i, 0);
        }
        savedU(tempdU);
        return tempdU;
    }

    public double[] countControls(double[] aktualna, double[] disturbanceTuning) {
        int OUT = setpoint.length;
        savedUz(disturbanceTuning);
        double[] celTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            celTemp[i] = setpoint[i % OUT];
        }
        Matrix yZad = new Matrix(celTemp, 1);
        double[] yTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            yTemp[i] = aktualna[i % OUT];
        }
        Matrix y = new Matrix(yTemp, 1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose()))
            .minus(Mpz.times(dUz.transpose())));
        double[] tempdU = new double[getLambda().size()];
        for (int i = 0; i < getLambda().size(); i++) {
            verifyUTemp(Utemp, i);
            tempdU[i] = Utemp.get(i, 0);
        }
        savedU(tempdU);
        return tempdU;
    }

    protected void verifyUTemp(Matrix Utemp, int i) {
        if (Utemp.get(i, 0) > duMax) {
            Utemp.set(i, 0, duMax);
        } else if (Utemp.get(i, 0) < -duMax) {
            Utemp.set(i, 0, -duMax);
        }
    }

    protected Matrix ustawMatrixYZad() {
        Matrix ySet = new Matrix(setpoint.length, N);
        for (int i = 0; i < setpoint.length; i++) {
            for (int j = 0; j < N; j++) {
                ySet.set(i, j, setpoint[i]);
            }
        }
        return ySet;
    }


    protected void calculateParameters(SISODPA object) {
        this.S = new ArrayList<>();
        calculateS(object);
        calculateMp();
        calculateM();
        calculateK();
        if (object.getDisturbance() != null && !object.getDisturbance().isEmpty()) {
            resetDisturbance(object.getDisturbance().size());
            calculateSz(object);
            calculateMz(object.getDisturbance().size(), 1);
            calculateMpz(object.getDisturbance().size(), 1);
        }
        resetController(1);
        object.resetObject();
    }

    protected void calculateParameters(MIMODPA MIMODPA) {
        this.S = new ArrayList<>();
        calculateS(MIMODPA);
        calculateMp(MIMODPA.getEntriesNumber(), MIMODPA.getOutputNumber());
        calculateM(MIMODPA.getEntriesNumber(), MIMODPA.getOutputNumber());
        calculateK(MIMODPA.getEntriesNumber());
        if (MIMODPA.getDisturbance() != null) {
            resetDisturbance(MIMODPA.getDisturbance().getEntriesNumber());
            calculateSz(MIMODPA);
            calculateMz(MIMODPA.getDisturbance().getEntriesNumber(), MIMODPA.getDisturbance().getOutputNumber());
            calculateMpz(MIMODPA.getDisturbance().getEntriesNumber(), MIMODPA.getDisturbance().getOutputNumber());
        }
        resetController(MIMODPA.getEntriesNumber());
        MIMODPA.resetObject();
    }

    public void changeTuning(double[] parameters) {
        List<Double> tempLambda = new ArrayList<>();
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
        calculateK(tempLambda.size());
        resetController(tempLambda.size());
    }

    public void resetController() {
        for (int i = 0; i < dU.getColumnDimension(); i++) {
            for (int j = 0; j < dU.getRowDimension(); j++) {
                dU.set(j, i, 0.0);
            }
        }
        if (dUz != null) {
            for (int i = 0; i < dUz.getColumnDimension(); i++) {
                for (int j = 0; j < dUz.getRowDimension(); j++) {
                    dUz.set(j, i, 0.0);
                }
            }
        }
    }

    public void resetController(int IN) {
        dU = new Matrix(1, (D - 1) * IN, 0.0);
    }

    public void resetDisturbance(int IN) {
        dUz = new Matrix(1, (D) * IN, 0.0);
    }

    protected void calculateS(SISODPA SISODPA) {
        double U = 1;
        int i = 2;
        List<Double> Stemp = new ArrayList<>();
        double Utemp = 0;
        SISODPA.resetObject();
        Stemp.add((SISODPA.simulateStep(U) - SISODPA.getYpp()) / U);
        Stemp.add((SISODPA.simulateStep(Utemp) - SISODPA.getYpp()) / U);
        while ((Math.abs(Stemp.get(i - 1) - Stemp.get(i - 2)) >= 0.002) || Stemp.get(i - 2) == 0.0) {
            Stemp.add((SISODPA.simulateStep(Utemp) - SISODPA.getYpp()) / U);
            i++;
        }
        this.S.add(Stemp);
        this.D = S.get(0).size();
        this.N = this.D;
    }

    protected void calculateSz(SISODPA object) {
        this.Sz = new ArrayList<>();
        for (int i = 0; i < object.getDisturbance().size(); i++) {
            List<Double> Stemp = new ArrayList<>();
            double Utemp = 0;
            object.resetObject();
            Stemp.add(object.simulateDisruptionStep(1, i));
            for (int j = 1; j < D + 1; j++) {
                Stemp.add(object.simulateDisruptionStep(Utemp, i));
            }
            this.Sz.add(Stemp);
        }
    }

    protected void calculateSz(MIMODPA object) {
        this.Sz = new ArrayList<>();
        for (int i = 0; i < object.getDisturbance().getOutputNumber(); i++) {
            for (int j = 0; j < object.getDisturbance().getEntriesNumber(); j++) {
                object.resetObject();
                double U = 1;
                double Utemp = 0;
                List<Double> Stemp = new ArrayList<>();
                Stemp.add(object.simulateDisruptionStep(U, j, i) / U);
                for (int k = 1; k < D + 1; k++) {
                    Stemp.add(object.simulateDisruptionStep(Utemp, j, i) / U);
                }
                this.Sz.add(Stemp);
            }
        }
    }

    protected void calculateS(MIMODPA object) {
        for (int i = 0; i < object.getOutputNumber(); i++) {
            for (int j = 0; j < object.getEntriesNumber(); j++) {
                object.resetObject();
                double U = 1;
                double Utemp = 0;
                int k = 2;
                List<Double> Stemp = new ArrayList<>();
                Stemp.add((object.simulateStep(U, j, i) - object.getYpp(i)) / U);
                Stemp.add((object.simulateStep(Utemp, j, i) - object.getYpp(i)) / U);
                while ((Math.abs(Stemp.get(k - 1) - Stemp.get(k - 2)) >= 0.005) || Stemp.get(k - 2) == 0.0) {
                    Stemp.add((object.simulateStep(Utemp, j, i) - object.getYpp(i)) / U);
                    k++;
                }
                this.S.add(Stemp);
            }
        }
        this.D = S.get(0).size();
        for (int i = 0; i < S.size(); i++) {
            if (D < S.get(i).size()) {
                D = S.get(i).size();
            }
        }
        for (int i = 0; i < S.size(); i++) {
            while (D != S.get(i).size()) {
                S.get(i).add(S.get(i).get(S.get(i).size() - 1));
            }
        }
        this.N = this.D;
    }

    private void calculateM() {
        Matrix MTemp = new Matrix(N, Nu);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < N; j++) {
                if (j >= i) {
                    MTemp.set(j, i, S.get(0).get(j - i));
                }
            }
        }
        this.M = MTemp;
    }

    protected void calculateM(int IN, int OUT) {
        Matrix MTemp = new Matrix(N * OUT, Nu * IN);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < N; j++) {
                if (j >= i) {
                    for (int k = 0; k < OUT; k++) {
                        for (int m = 0; m < IN; m++) {
                            MTemp.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(j - i));
                        }
                    }
                }
            }
        }
        this.M = MTemp;
    }

    protected void calculateMp() {
        Mp = new Matrix(N, D - 1);
        for (int i = 0; i < D - 1; i++) {
            for (int j = 0; j < N; j++) {
                if ((j + i + 1) < D) {
                    Mp.set(j, i, S.get(0).get(j + i + 1) - S.get(0).get(i));
                } else {
                    Mp.set(j, i, S.get(0).get(D - 1) - S.get(0).get(i));
                }
            }
        }
    }

    protected void calculateMp(int IN, int OUT) {
        Mp = new Matrix(N * OUT, (D - 1) * IN);
        for (int i = 0; i < D - 1; i++) { //bok ,wszerz
            for (int j = 0; j < N; j++) { //dół
                for (int k = 0; k < OUT; k++) {
                    for (int m = 0; m < IN; m++) {
                        if ((j + i + 1) < D) {
                            Mp.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(j + i + 1) - S.get(k * IN + m).get(i));
                        } else {
                            Mp.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(D - 1) - S.get(k * IN + m).get(i));
                        }
                    }
                }
            }
        }
    }

    protected void calculateMz(int IN, int OUT) {
        Matrix MzTemp = new Matrix(N * OUT, (D) * IN);
        for (int i = 0; i < D; i++) { //wszerz
            for (int j = -1; j < N - 1; j++) { //wzdłuż
                if (i > 1 + Nu) {
                    for (int k = 0; k < OUT; k++) {
                        for (int m = 0; m < IN; m++) {
                            if (j < Nu) {
                                MzTemp.set((j + 1) * OUT + k, i * IN + m, 0.0);
                            } else {
                                MzTemp.set((j + 1) * OUT + k, i * IN + m, MzTemp.get((j + 1) * OUT + k, (i - 1) * IN + m) * 0.6);
                            }
                        }
                    }
                } else if (j >= i) {
                    for (int k = 0; k < OUT; k++) {
                        for (int m = 0; m < IN; m++) {
                            if (j - i >= D) {
                                MzTemp.set((j + 1) * OUT + k, i * IN + m, Sz.get(k * IN + m).get(D));
                            } else {
                                MzTemp.set((j + 1) * OUT + k, i * IN + m, Sz.get(k * IN + m).get(j - i + 1));
                            }
                        }
                    }
                }
            }
        }
        this.Mz = MzTemp;
    }

    protected void calculateMpz(int IN, int OUT) {
        Mpz = new Matrix(N * OUT, (D) * IN);
        //pierwsza kolumna
        for (int j = 0; j < N; j++) {
            for (int k = 0; k < OUT; k++) {
                for (int m = 0; m < IN; m++) {
                    if (j <= D) {
                        Mpz.set(j * OUT + k, m, Sz.get(k * IN + m).get(j));
                    } else {
                        Mpz.set(j * OUT + k, m, Sz.get(k * IN + m).get(D));
                    }
                }
            }
        }
        //kolejne
        for (int i = 0; i < D - 1; i++) {
            for (int j = 0; j < N; j++) {
                for (int k = 0; k < OUT; k++) {
                    for (int m = 0; m < IN; m++) {
                        if ((j + i + 1) < D + 1) {
                            Mpz.set(j * OUT + k, (i + 1) * IN + m, (Sz.get(k * IN + m).get(j + i + 1) - Sz.get(k * IN + m).get(i)));
                        } else {
                            Mpz.set(j * OUT + k, (i + 1) * IN + m, (Sz.get(k * IN + m).get(D) - Sz.get(k * IN + m).get(i)));
                        }
                    }
                }
            }
        }
    }


    private void calculateK() {
        Matrix I = new Matrix(Nu, Nu);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < Nu; j++) {
                if (i == j) {
                    I.set(i, j, getLambda().get(0));
                } else {
                    I.set(i, j, 0);
                }
            }
        }
        this.K = ((M.transpose().times(M).plus(I)).inverse()).times(M.transpose());
    }

    private void calculateK(int IN) {
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

    protected void savedU(double dUNewest) {
        int horyzont = D>12? 11: 1;
        for (int i = D - 1; i > D - horyzont; i--) {
            dU.set(0, i - 1, (dU.get(0, i - 1) + 3 * dU.get(0, i - 2)) / 4);
        }
        for (int i = D - horyzont; i > 1; i--) {
            dU.set(0, i - 1, dU.get(0, i - 2));
        }
        dU.set(0, 0, dUNewest);
    }

    protected void savedUz(double[] disturbanceTuning) {
        if (disturbanceTuning.length > 1) {
            for (int i = D - 1; i > 1; i--) {
                for (int j = disturbanceTuning.length - 1; j >= 0; j--) {
                    dUz.set(0, disturbanceTuning.length * i - 1 - j, dUz.get(0, disturbanceTuning.length * i - disturbanceTuning.length - j - 1));
                }
            }

            for (int j = disturbanceTuning.length - 1; j >= 0; j--) {
                dUz.set(0, j, disturbanceTuning[disturbanceTuning.length - j - 1]);
            }

        } else {
            for (int i = D; i > 1; i--) {
                dUz.set(0, i - 1, dUz.get(0, i - 2));
            }
            dUz.set(0, 0, disturbanceTuning[0]);
        }
    }

    protected void savedU(double[] NewestdU) {

        for (int i = D - 1; i > 1; i--) {
            for (int j = NewestdU.length - 1; j >= 0; j--) {
                dU.set(0, NewestdU.length * i - j - 1, dU.get(0, NewestdU.length * i - NewestdU.length - j - 1));
            }
        }

        for (int j = 0; j < NewestdU.length; j++) {
            dU.set(0, j, NewestdU[j]);
        }

    }

    public int getNumberOfTuningParameters() {

        return getLambda().size() - presetControlsNumbers;
    }
}
