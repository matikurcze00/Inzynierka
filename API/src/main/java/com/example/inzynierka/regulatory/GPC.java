package com.example.inzynierka.regulatory;

import Jama.Matrix;
import com.example.inzynierka.obiekty.SISORownianiaRoznicowe;

import java.util.List;

public class GPC extends Regulator{
    private Matrix K;
    protected Matrix M;
    protected List<List<Double>> S;
    protected List<List<Double>> A;
    protected List<List<Double>> B;
    protected List<List<Double>> U;
    protected List<List<Double>> Y;
    private double[] uMin;
    private double[] uMax;
    private double[] duMax;
    private Integer N;

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
        return poprawaUTemp(Utemp.get(0,0),0);
    }



    @Override
    public double policzOutput(double aktualna, double[] sterowanieZaklocenia){
        return policzOutput(aktualna);
    }

    @Override
    public double[] policzOutput(double[] aktualna)
    {
        return new double[]{0.0};
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
        Matrix yZad = new Matrix(cel.length, N);
        for (int i = 0; i < cel.length; i++)
            for (int j = 0; j < N; j++)
                yZad.set(i, j, cel[i]);
        return yZad;
    }
    private Matrix ustawMatrixY(double aktualna) {
        Matrix YMatrix = new Matrix(1, N);
        for(int i = 0; i < N-1; i ++) {
            double yTemp = 0.0;
            //i = 1
            //B.size() = 3
            //b0, b1, b2
            //u0, u1, u2
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
            YMatrix.set(0,i,yTemp);
        }
        return YMatrix;
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
