package com.company.regulatory;

import com.company.Obiekt;
import lombok.Data;
import Jama.Matrix;
import java.util.List;

@Data
public class DMC extends Regulator{
    private int D;
    private int N;
    private int Nu;
    private int Lambda;
    private Matrix M;
    private Matrix Mp;
    private double[] K;
    private double[] S;
    @Override
    public double policzOutput(double aktualna) {
        return 0;
    }
    private void policzWartosci(Obiekt obiekt)
    {
        policzS(obiekt);
        policzM();
    }
    public void zmienWartosci(double[] wartosci){

    }
    private void policzS(Obiekt obiekt)
    {
        S = new double[D];
        double U1 = (2*obiekt.getUMin() + 1*obiekt.getUMax())/3;
        double U2 = (1*obiekt.getUMin()+ 2*obiekt.getUMax())/3;
        double du = U2-U1;
        double Ypp;
        for(int i = 0; i<D; i++)
        {
            obiekt.obliczKrok(U1);
        }
        Ypp = obiekt.getY().get(0);
        for (int i = 0; i<D; i++)
        {
        S[i] = ((obiekt.obliczKrok(U2)-Ypp)/du);
        }
    }
    private void policzM()
    {
        M = new Matrix(N,D);
        for (int i = 0; i<D-1; i++)
        {
            for (int j = 0; j<N; j++)
            {
                if((j+i)<D)
                    M.set(j,i,S[j+i]-S[i]);
                else
                    M.set(j,i,S[D]-S[i]);
            }
        }
        Matrix I = new Matrix(N,N);
        for (int i = 0; i<N; i++)
        {
            for (int j = 0; j<N; j++)
            {
                if (i==j)
                    I.set(i,j, getLambda());
                else
                    I.set(i,j,0);
            }
        }
        Matrix Ktemp = M.transpose().times(M).plus(I).inverse().times(M.transpose());
        K = Ktemp.getColumnPackedCopy();
    }
}
