package com.example.inzynierka.regulatory;

import Jama.Matrix;
import com.example.inzynierka.obiekty.SISORownianiaRoznicowe;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GPC extends Regulator{
    private Matrix K;
    protected Matrix M;
    protected List<List<Double>> S;
    protected List<List<Double>> A;
    protected List<List<Double>> B; //IN->OUT
    protected List<List<Double>> U;
    protected List<List<Double>> Y;
    protected List<Double> Lambda;
    private double[] uMin;
    private double[] uMax;
    private double[] duMax;
    private Integer N;
    private Integer Nu;
    private Integer IN;
    private Integer OUT;
    public GPC() {
    }
    public GPC(SISORownianiaRoznicowe sisoRownianiaRoznicowe, double lambda, double cel, double duMax) {

    }
    @Override
    public double policzOutput(double aktualna){
        obliczU(aktualna);
        Matrix yZad = ustawMatrixYZad();
        Matrix yAktualne = ustawMatrixY(aktualna);
        Matrix Utemp = K.times(yZad.minus(yAktualne));
        double du = poprawaUTemp(Utemp.get(0,0),0);
        zapiszU(du);
        return du;
    }

    @Override
    public double policzOutput(double aktualna, double[] sterowanieZaklocenia){
        return policzOutput(aktualna);
    }

    @Override
    public double[] policzOutput(double[] aktualna)
    {
        obliczU(aktualna);
        Matrix yZad = ustawMatrixYZad();
        Matrix yAktualne = ustawMatrixY(aktualna);
        Matrix Utemp = K.times(yZad.minus(yAktualne));
        double[] output = new double[IN];
        for(int i = 0; i < IN; i++)
            output[i] = poprawaUTemp(Utemp.get(i,0), i);
        zapiszU(output);
        return output;
    }

    @Override
    public double[] policzOutput(double[] aktualna, double[] sterowanieZaklocenia) {
        return policzOutput(aktualna);
    }

    @Override
    public void zmienWartosci(double[] wartosci) {

    }

    @Override
    public void resetujRegulator(){

    }

    @Override
    public int liczbaZmiennych(){
        return 0;
    }

    private void policzS() {
        S = new ArrayList<>();
        for(int i = 0; i < OUT ; i++) {
            for(int j = 0; j < IN; j++) {
                List<Double> Stemp = new ArrayList<>();
                for(int k = 0; k < N; k ++) {
                    Double Sk = 0.0;
                    for(int m = 0; m < Math.min(N, B.get(j).size()); m++)
                        Sk += B.get(j).get(m);
                    for(int m = 0; m < Math.min(Stemp.size(), A.get(j).size()); m++)
                        Sk -= A.get(j).get(m) - Stemp.get(Stemp.size() - m);
                    Stemp.add(Sk);
                }
                S.add(Stemp);
            }
        }
    }

    private void policzM() {
        M = new Matrix(N * OUT, Nu * IN);
        for (int i = 0; i < Nu; i++) {
            for (int j = 0; j < N; j++) {
                if (j >= i)
                    for (int k = 0; k < OUT; k++)
                        for (int m = 0; m < IN; m++)
                            M.set(j * OUT + k, i * IN + m, S.get(k * IN + m).get(j - i));
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
                    } else
                        I.set(i * IN + m, j * IN + m, 0);
                }
            }
        }
        this.K = ((M.transpose().times(M).plus(I)).inverse()).times(M.transpose());
    }
    public void obliczU(double du) {
        double Uakt = U.get(0).get(0) + du;
        if (Uakt > uMax[0])
            Uakt = uMax[0];
        else if (Uakt < uMin[0])
            Uakt = uMin[0];
        for (int i = U.size() - 1; i > 0; i--)
            U.get(0).set(i, U.get(0).get(i - 1));
        U.get(0).set(0, Uakt);
    }
    public void zapiszU(double du) {
        for(int j = U.get(0).size() -1; j > 0; j--)
            U.get(0).set(j,U.get(0).get(0-1));
        U.get(0).set(0,du);
    }
    public void zapiszU(double[] du) {
        for(int i = 0; i < U.size(); i ++) {
            for(int j = U.get(i).size() -1; j > 0; j--)
                U.get(i).set(j,U.get(i).get(j-1));
            U.get(i).set(0,du[i]);
        }
    }
    public void obliczU(double[] du) {
        for (int j = 0; j < du.length; j++) {
            double Uakt = U.get(j).get(0) + du[j];
            if (Uakt > uMax[j])
                Uakt = uMax[j];
            else if (Uakt < uMin[j])
                Uakt = uMin[j];

            for (int i = U.get(j).size() - 1; i > 0; i--)
                U.get(j).set(i, U.get(j).get(i - 1));
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
        for (int i = U.get(IN).size() - 1; i > 0; i--)
            U.get(IN).set(i, U.get(IN).get(i - 1));
        U.get(IN).set(0, Uakt);
    }
    protected Matrix ustawMatrixYZad() {
        double[] celTemp = new double[N * OUT];
        for (int i = 0; i < N * OUT; i++) {
            celTemp[i] = cel[i % OUT];
        }
        return new Matrix(celTemp, 1);
    }
    private Matrix ustawMatrixY(double aktualna) {
        Matrix YMatrix = new Matrix(1, N);
        for(int i = 0; i < N-1; i ++) {
            double yTemp = 0.0;
            yTemp += ustawBUSISO(i);
            yTemp += ustawAYSISO(i);
            YMatrix.set(0,i,yTemp);
        }
        return YMatrix;
    }

    private Matrix ustawMatrixY(double[] aktualna) {
        Matrix YMatrix = new Matrix(1, N * OUT);
        for(int i = 0; i < N-1; i ++) {
            double[] yTemp = ustawBUMIMO(i);
            yTemp = ustawAYMIMO(i, yTemp);
            for(int j = 0; j < OUT; j ++)
                YMatrix.set(1, i * OUT + j, yTemp[j]);
        }
        return YMatrix;
    }

    private double[] ustawAYMIMO(int i, double[] yTemp) {
        for(int k = 0; k < OUT; k++) {
            for(int l = 0; l < OUT; l++) {
                if(A.get(l*OUT + k).size() > i) {
                    for(int j = 0; j < A.get(l*OUT + k).size() - i; j++) {
                        yTemp[k] += Y.get(l).get(A.get(l*OUT + k).size() - 1 - i - j) * A.get(l*OUT + k).get(A.get(l*OUT + k).size() -1 - j);
                    }
                    for(int j = A.get(l*OUT + k).size() - i; j< A.get(l*OUT + k).size(); j++) {
                        yTemp[k] += Y.get(l).get(l*OUT + k) * A.get(l*OUT + k).get(A.get(l*OUT + k).size() - 1 - j);
                    }
                } else {
                    for(int j = 0; j< A.get(l*OUT + k).size(); j++) {
                        yTemp[k] += Y.get(l).get(0) * A.get(l*OUT + k).get(j);
                    }
                }
            }
        }
        return yTemp;
    }
    private double[] ustawBUMIMO(int i) {
        double[] yTemp = new double[OUT];
        for(int k = 0; k < OUT; k++) {
            for(int l = 0; l < IN; l++) {
                if(B.get(l*OUT + k).size()> i) {
                    for(int j = 0; j < B.get(l*OUT + k).size() - i; j++) {
                        yTemp[k] += U.get(l).get(B.get(l*OUT + k).size() - 1 - i - j) * B.get(l*OUT + k).get(B.get(l*OUT + k).size() -1 - j);
                    }
                    for(int j = B.get(l*OUT + k).size() - i; j< B.get(l*OUT + k).size(); j++) {
                        yTemp[k] += U.get(l).get(l*OUT + k) * B.get(l*OUT + k).get(B.get(l*OUT + k).size() - 1 - j);
                    }
                } else {
                    for(int j = 0; j< B.get(l*OUT + k).size(); j++) {
                        yTemp[k] += U.get(l).get(0) * B.get(l*OUT + k).get(j);
                    }
                }
            }
        }
        return yTemp;
    }
    private double ustawAYSISO(int i) {
        double yTemp = 0.0;
        if(A.get(0).size() > i) {
            for(int j = 0; j < A.get(0).size() - i; j++) {
                yTemp += Y.get(0).get(A.get(0).size() - 1 - i - j) * A.get(0).get(A.get(0).size() -1 - j);
            }
            for(int j = A.get(0).size() - i; j< A.get(0).size(); j++) {
                yTemp += Y.get(0).get(0) * A.get(0).get(A.get(0).size() - 1 - j);
            }
        } else {
            for(int j = 0; j< A.get(0).size(); j++) {
                yTemp += Y.get(0).get(0) * A.get(0).get(j);
            }
        }
        return yTemp;
    }
    private double ustawBUSISO(int i) {
        double yTemp = 0.0;
        if(B.get(0).size()> i) {
            for(int j = 0; j < B.get(0).size() - i; j++) {
                yTemp += U.get(0).get(B.get(0).size() - 1 - i - j) * B.get(0).get(B.get(0).size() -1 - j);
            }
            for(int j = B.get(0).size() - i; j< B.get(0).size(); j++) {
                yTemp += U.get(0).get(0) * B.get(0).get(B.get(0).size() - 1 - j);
            }
        } else {
            for(int j = 0; j< B.get(0).size(); j++) {
                yTemp += U.get(0).get(0) * B.get(0).get(j);
            }
        }
        return yTemp;
    }

    protected double poprawaUTemp(double Utemp, int i) {
        if(Utemp > duMax[i]) {
            return duMax[i];
        } else if(Utemp < -duMax[i]) {
            return -duMax[i];
        } else {
            return Utemp;
        }
    }
}
