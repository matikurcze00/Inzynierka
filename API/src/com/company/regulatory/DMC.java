package com.company.regulatory;

import com.company.Obiekt;
import lombok.Data;
import Jama.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class DMC extends Regulator{
    private int D;
    private int N;
    private int Nu;
    private double Lambda;
    private Matrix Mp;
    private Matrix K;
    private List<Double> S;
    private Matrix dU;

    @Override
    public double policzOutput(double aktualna)
    {
        double[] yZadTemp = new double[N];
        Arrays.fill(yZadTemp,cel);
        Matrix yZad = new Matrix(yZadTemp,1);
        double[] yTemp = new double[N];
        Arrays.fill(yTemp,aktualna);
        Matrix y = new Matrix(yTemp,1);
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose())));
        if(Utemp.get(0,0)>duMax)
        {
            Utemp.set(0,0,duMax);
        }
        else if(Utemp.get(0,0)<-duMax)
        {
            Utemp.set(0,0,-duMax);
        }
        dodajdU(Utemp.get(0,0));
        return Utemp.get(0,0);
    }

    public DMC(int Nu, double lambda, Obiekt obiekt, double cel, double duMax)
    {
        this.Lambda = lambda;
        this.Nu = Nu;
        this.cel = cel;
        this.duMax = duMax;
        policzWartosci(obiekt);
    }
    private void policzWartosci(Obiekt obiekt)
    {
        policzS(obiekt);
        policzMp();
        policzK();
        resetujRegulator();
    }
    @Override
    public void zmienWartosci(double[] wartosci){
        setLambda((int)wartosci[0]);
        resetujRegulator();
    }

    @Override
    public void resetujRegulator()
    {
       double[] uTemp = new double[D-1];
        Arrays.fill(uTemp,0);
        dU = new Matrix(uTemp,1);
    }
    private void policzS(Obiekt obiekt)
    {
        double U = obiekt.getUMax()/2;
        int i = 2;
        S = new ArrayList<Double>();
        S.add((obiekt.obliczKrok(U)- obiekt.getYpp())/U);
        S.add((obiekt.obliczKrok(U)- obiekt.getYpp())/U);
        while(Math.abs(S.get(i-1)-S.get(i-2))>0.05)
        {
            S.add((obiekt.obliczKrok(U)- obiekt.getYpp())/U);
            i++;
        }

        this.D = S.size();
        this.N = S.size();
    }
    private void policzMp()
    {
        Mp = new Matrix(N,D-1);
        for (int i = 0; i<D-1; i++)
        {
            for (int j = 0; j<N; j++)
            {
                if((j+i+1)<D)
                    Mp.set(j,i,S.get(j+i+1)-S.get(i));
                else
                    Mp.set(j,i,S.get(D-1)-S.get(i));
            }
        }
    }
    private void policzK()
    {
        Matrix M = new Matrix(N,Nu);
        for (int i = 0; i<Nu; i++)
        {
            for (int j = 0; j<N; j++)
            {
                if(j>=i)
                    M.set(j,i,S.get(j-i));
                else
                    M.set(j,i,0);
            }
        }

        Matrix I = new Matrix(Nu,Nu);
        for (int i = 0; i<Nu; i++)
        {
            for (int j = 0; j<Nu; j++)
            {
                if (i==j)
                    I.set(i,j, getLambda());
                else
                    I.set(i,j,0);
            }
        }

        K = M.transpose().times(M).plus(I).inverse().times(M.transpose());
    }
    private void dodajdU(double dUAktualne)
    {
        for(int i = D-1; i>1; i--)
        {
            dU.set(0,i-1,dU.get(0,i-2));
        }
        dU.set(0,0,dUAktualne);
    }
}
