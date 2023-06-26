package com.example.inzynierka.regulatory;

import Jama.Matrix;
import com.example.inzynierka.obiekty.MIMORownianiaRoznicowe;
import com.example.inzynierka.obiekty.SISORownianiaRoznicowe;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class GPC extends RegulatorMPC {
    protected List<List<Double>> S;
    protected List<List<Double[]>> A;
    protected List<List<Double[]>> B; //IN->OUT
    protected List<List<Double>> U;
    protected List<List<Double[]>> Bz;
    protected List<List<Double>> Uz;
    protected List<List<Double>> Y;
    protected Double[] strojenieZadane;
    protected int liczbaStrojeniaZadanego = 0;
    protected int liczbaZaklocen = 0;
    private Matrix K;
    private double[] uMin;
    private double[] uMax;
    private Integer IN;
    private Integer OUT;

    public GPC(SISORownianiaRoznicowe sisoRownianiaRoznicowe, double lambda, double cel,
               double duMax, Double[] strojenieZadane) {
        this(sisoRownianiaRoznicowe, lambda, cel, duMax);
        if (strojenieZadane[0] != null) {
            liczbaStrojeniaZadanego = 1;
            this.strojenieZadane = strojenieZadane;
            this.getLambda().set(0, strojenieZadane[0]);
        }
        policzK();
    }

    public GPC(SISORownianiaRoznicowe sisoRownianiaRoznicowe, double lambda, double cel, double duMax) {
        this.Lambda = List.of(lambda);
        this.cel = new double[] {cel};
        this.duMax = duMax;
        policzWartosci(sisoRownianiaRoznicowe);
    }

    public GPC(MIMORownianiaRoznicowe mimoRownianiaRoznicowe, int Nu, double[] cel, double duMax, Double[] strojenieZadane, double[] lambda) {
        this.strojenieZadane = strojenieZadane;
        this.cel = cel;
        this.duMax = duMax;
        this.Nu = Nu;
        this.IN = mimoRownianiaRoznicowe.getLiczbaIN();
        this.OUT = mimoRownianiaRoznicowe.getLiczbaOUT();
        List<Double> tempLambda = new ArrayList<>();
        for (double wartosc : lambda) {
            tempLambda.add(wartosc);
        }
        this.Lambda = new ArrayList<>(tempLambda);
        for (int i = 0; i < strojenieZadane.length; i++) {
            if (strojenieZadane[i] != null) {
                liczbaStrojeniaZadanego += 1;
                this.getLambda().set(i, strojenieZadane[i]);
            }
        }
        this.policzWartosci(mimoRownianiaRoznicowe);
    }

    @Override
    public double policzSterowanie(double aktualna) {
        zapiszY(aktualna);
        Matrix yZad = ustawMatrixYZad();
        Matrix yAktualne = ustawMatrixYSISO();
        Matrix Utemp = K.times(yZad.transpose().minus(yAktualne.transpose()));
        double du = poprawaUTemp(Utemp.get(0, 0));
        zapiszU(du);
        return du;
    }

    @Override
    public double policzSterowanie(double aktualna, double[] sterowanieZaklocenia) {
        zapiszUz(sterowanieZaklocenia);
        return policzSterowanie(aktualna);
    }

    @Override
    public double[] policzSterowanie(double[] aktualna) {
        zapiszY(aktualna);
        Matrix yZad = ustawMatrixYZad();
        Matrix yAktualne = ustawMatrixYMIMO();
        Matrix Utemp = K.times(yZad.transpose().minus(yAktualne.transpose()));
        double[] output = new double[IN];
        for (int i = 0; i < IN; i++) {
            output[i] = poprawaUTemp(Utemp.get(i, 0));
        }
        zapiszU(output);
        return output;
    }

    @Override
    public double[] policzSterowanie(double[] aktualna, double[] sterowanieZaklocenia) {
        zapiszUz(sterowanieZaklocenia);
        return policzSterowanie(aktualna);
    }

    @Override
    public void zmienNastawy(double[] wartosci) {
        List<Double> tempLambda = new ArrayList();
        if (this.liczbaStrojeniaZadanego == 0) {
            for (double wartosc : wartosci) {
                tempLambda.add(wartosc);
            }
        } else {
            int iTemp = 0;
            for (int i = 0; i < getLambda().size(); i++) {
                if (strojenieZadane[i] != null) {
                    tempLambda.add(strojenieZadane[i]);
                } else {
                    tempLambda.add(wartosci[iTemp]);
                    iTemp += 1;
                }
            }
        }
        setLambda(tempLambda);
        policzK();
        resetujRegulator();
    }

    @Override
    public void resetujRegulator() {
        for (int i = 0; i < this.OUT; i++) {
            Y.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.IN; i++) {
            U.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.liczbaZaklocen; i++) {
            Uz.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public void policzWartosci(SISORownianiaRoznicowe sisoRownianiaRoznicowe) {
        this.S = new ArrayList();

        ustawABSISO(sisoRownianiaRoznicowe);
        this.IN = 1;
        this.OUT = 1;
        this.Nu = 4;
        ustawUYSISO();
        this.uMax = new double[] {sisoRownianiaRoznicowe.getUMax()};
        this.uMin = new double[] {sisoRownianiaRoznicowe.getUMin()};
        policzS();
        policzM();
        policzK();
        resetujRegulator();
        sisoRownianiaRoznicowe.resetObiektu();
    }

    private void ustawUYSISO() {
        this.Y = new ArrayList<>();
        this.Y.add(new ArrayList(Collections.nCopies(5, 0.0)));
        this.U = new ArrayList<>();
        this.U.add(new ArrayList(Collections.nCopies(5, 0.0)));
        if (this.liczbaZaklocen > 0) {
            this.Uz = new ArrayList<>();
            for (int i = 0; i < liczbaZaklocen; i++) {
                this.Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
            }
        }
    }

    private void ustawABSISO(SISORownianiaRoznicowe sisoRownianiaRoznicowe) {
        this.A = new ArrayList<>();
        List<Double[]> tempA = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tempA.add(new Double[] {sisoRownianiaRoznicowe.getA().get(i)});
        }
        A.add(tempA);
        this.B = new ArrayList<>();
        List<Double[]> tempB = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tempB.add(new Double[] {sisoRownianiaRoznicowe.getB().get(i)});
        }
        B.add(tempB);
        if (sisoRownianiaRoznicowe.getLiczbaZaklocen() > 0) {
            this.liczbaZaklocen = sisoRownianiaRoznicowe.getLiczbaZaklocen();
            this.Bz = new ArrayList<>();
            List<Double[]> tempBz = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                tempBz.add(sisoRownianiaRoznicowe.getBz().get(i));
            }
            Bz.add(tempBz);
        }
    }

    public void policzWartosci(MIMORownianiaRoznicowe mimoRownianiaRoznicowe) {
        this.A = mimoRownianiaRoznicowe.getA();
        this.B = mimoRownianiaRoznicowe.getB();
        this.Bz = mimoRownianiaRoznicowe.getBz();
        this.liczbaZaklocen = mimoRownianiaRoznicowe.getLiczbaZaklocen();
        ustawUYMIMO();
        this.uMax = mimoRownianiaRoznicowe.getUMax();
        this.uMin = mimoRownianiaRoznicowe.getUMin();
        policzS();
        policzM();
        policzK();
        resetujRegulator();
        mimoRownianiaRoznicowe.resetObiektu();
    }

    private void ustawUYMIMO() {
        this.Y = new ArrayList<>();
        for (int i = 0; i < IN; i++) {
            this.Y.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
        this.U = new ArrayList<>();
        for (int i = 0; i < OUT; i++) {
            this.U.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
        if (this.liczbaZaklocen > 0) {
            this.Uz = new ArrayList<>();
            for (int i = 0; i < liczbaZaklocen; i++) {
                this.Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
            }
        }
    }

    @Override
    public int liczbaZmiennych() {

        return getLambda().size() - liczbaStrojeniaZadanego;
    }

    private void policzS() {
        S = new ArrayList<>();
        for (int i = 0; i < OUT; i++) {
            for (int j = 0; j < IN; j++) {
                List<Double> Stemp = new ArrayList<>();
                int k = 2;
                obliczSk(i, j, Stemp, 0);
                obliczSk(i, j, Stemp, 1);
                while (!(Math.abs(Stemp.get(k - 1) - Stemp.get(k - 2)) >= 0.00005) || Stemp.get(k - 2) == 0.0) {
                    obliczSk(i, j, Stemp, k);
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

    private void obliczSk(int out, int in, List<Double> Stemp, int k) {
        Double Sk = 0.0;
        for (int m = 0; m < Math.min(k + 1, B.get(out).size()); m++) {
            Sk += B.get(out).get(m)[in];
        }
        for (int m = 0; m < Math.min(k, A.get(out).size()); m++) {
            Sk -= A.get(out).get(m)[out] * Stemp.get(k - 1 - m);
        }
        Stemp.add(Sk);
    }

    private void policzM() {
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


    private void policzK() {
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

    public void obliczU(double du) {
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

    public void zapiszY(double aktualna) {
        for (int j = Y.get(0).size() - 1; j > 0; j--) {
            Y.get(0).set(j, Y.get(0).get(j - 1));
        }
        Y.get(0).set(0, aktualna);
    }

    public void zapiszU(double du) {
        for (int j = U.get(0).size() - 1; j > 0; j--) {
            U.get(0).set(j, U.get(0).get(j - 1));
        }
        if(U.get(0).get(0) + du > uMax[0]) {
            U.get(0).set(0, uMax[0]);
        } else if(U.get(0).get(0) + du < uMin[0]) {
            U.get(0).set(0, uMin[0]);
        } else {
            U.get(0).set(0, U.get(0).get(0) + du);
        }
        U.get(0).set(0, U.get(0).get(0) + du);
    }

    public void zapiszY(double[] aktualna) {
        for (int i = 0; i < Y.size(); i++) {
            for (int j = Y.get(i).size() - 1; j > 0; j--) {
                Y.get(i).set(j, Y.get(i).get(j - 1));
            }
            Y.get(i).set(0, aktualna[i]);
        }
    }

    public void zapiszUz(double[] duz) {
        for (int i = 0; i < liczbaZaklocen; i++) {
            for (int j = Uz.get(i).size() - 1; j > 0; j--) {
                Uz.get(i).set(j, Uz.get(i).get(j - 1));
            }
            Uz.get(i).set(0, Uz.get(i).get(0) + duz[i]);
        }
    }

    public void zapiszU(double[] du) {
        for (int i = 0; i < IN; i++) {
            for (int j = U.get(i).size() - 1; j > 0; j--) {
                U.get(i).set(j, U.get(i).get(j - 1));
            }
            if(U.get(i).get(0) + du[i] > uMax[i]) {
                U.get(i).set(0, uMax[i]);
            } else if(U.get(i).get(0) + du[i] < uMin[i]) {
                U.get(i).set(0, uMin[i]);
            } else {
                U.get(i).set(0, U.get(i).get(0) + du[i]);
            }
        }
    }

    public void obliczU(double[] du) {
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

    public void obliczU(double du, int IN) {
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

    protected Matrix ustawMatrixYZad() {
        double[] celTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            celTemp[i] = cel[i % OUT];
        }
        return new Matrix(celTemp, 1);
    }

    private Matrix ustawMatrixYSISO() {
        Matrix YMatrix = new Matrix(1, N);
        for (int i = 0; i < N; i++) {
            double yTemp = 0.0;
            yTemp += ustawBUSISO(i);
            yTemp += ustawZakloceniaSISO(i);
            yTemp += ustawYSISO(i);
            YMatrix.set(0, i, yTemp);
        }
        return YMatrix;
    }

    private Matrix ustawMatrixYMIMO() {
        Matrix YMatrix = new Matrix(1, N * OUT);
        for (int i = 0; i < N - 1; i++) {
            double[] yTemp = ustawBUMIMO(i);
            yTemp = ustawZakloczeniaMIMO(i, yTemp);
            yTemp = ustawYMIMO(i, yTemp);
            for (int j = 0; j < OUT; j++) {
                YMatrix.set(0, i * OUT + j, yTemp[j]);
            }
        }
        return YMatrix;
    }

    private double[] ustawZakloczeniaMIMO(int i, double[] yTemp) {
        for (int k = 0; k < OUT; k++) {
            for (int l = 0; l < liczbaZaklocen; l++) {
                if (Bz.get(k).size() > i) {
                    for (int j = 0; j < Bz.get(k).size() - i; j++) {
                        yTemp[k] += Uz.get(l).get(Bz.get(k).size() - 1 - i - j) * Bz.get(k).get(Bz.get(k).size() - 1 - j)[l];
                    }
                    for (int j = Bz.get(k).size() - i; j < Bz.get(k).size(); j++) {
                        yTemp[k] += Uz.get(l).get(0) * Bz.get(k).get(Bz.get(k).size() - 1 - j)[l];
                    }
                } else {
                    for (int j = 0; j < Bz.get(k).size(); j++) {
                        yTemp[k] += Uz.get(l).get(0) * Bz.get(k).get(j)[l];
                    }
                }
            }
        }
        return yTemp;
    }

    private double[] ustawYMIMO(int i, double[] yTemp) {
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

    private double[] ustawBUMIMO(int i) {
        double[] yTemp = new double[OUT];
        for (int k = 0; k < OUT; k++) {
            for (int l = 0; l < IN; l++) {
                if (B.get(k).size() > i) {
                    for (int j = 0; j < B.get(k).size() - i; j++) {
                        yTemp[k] += U.get(l).get(B.get(k).size() - 1 - i - j) * B.get(k).get(B.get(k).size() - 1 - j)[l];
                    }
                    for (int j = B.get(k).size() - i; j < B.get(k).size(); j++) {
                        yTemp[k] += U.get(l).get(0) * B.get(k).get(B.get(k).size() - 1 - j)[l];
                    }
                } else {
                    for (int j = 0; j < B.get(k).size(); j++) {
                        yTemp[k] += U.get(l).get(0) * B.get(k).get(j)[l];
                    }
                }
            }
        }
        return yTemp;
    }

    private double ustawYSISO(int i) {
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

    private double ustawZakloceniaSISO(int i) {
        double yTemp = 0.0;
        for (int l = 0; l < liczbaZaklocen; l++) {
            if (Bz.get(0).size() > i) {
                for (int j = 0; j < Bz.get(0).size() - i; j++) {
                    yTemp += Uz.get(l).get(Bz.get(0).size() - 1 - i - j) * Bz.get(0).get(Bz.get(0).size() - 1 - j)[l];
                }
                for (int j = Bz.get(0).size() - i; j < Bz.get(0).size(); j++) {
                    yTemp += Uz.get(l).get(0) * Bz.get(0).get(Bz.get(0).size() - 1 - j)[l];
                }
            } else {
                for (int j = 0; j < Bz.get(0).size(); j++) {
                    yTemp += Uz.get(l).get(0) * Bz.get(0).get(j)[l];
                }
            }
        }
        return yTemp;
    }

    private double ustawBUSISO(int i) {
        double yTemp = 0.0;
        if (B.get(0).size() > i) {
            for (int j = 0; j < B.get(0).size() - i; j++) {
                yTemp += U.get(0).get(B.get(0).size() - 1 - i - j) * B.get(0).get(B.get(0).size() - 1 - j)[0];
            }
            for (int j = B.get(0).size() - i; j < B.get(0).size(); j++) {
                yTemp += U.get(0).get(0) * B.get(0).get(B.get(0).size() - 1 - j)[0];
            }
        } else {
            for (int j = 0; j < B.get(0).size(); j++) {
                yTemp += U.get(0).get(0) * B.get(0).get(j)[0];
            }
        }
        return yTemp;
    }

    protected double poprawaUTemp(double Utemp) {
        if (Utemp > duMax) {
            return duMax;
        } else if (Utemp < -duMax) {
            return -duMax;
        } else {
            return Utemp;
        }
    }
}
