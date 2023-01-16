package com.example.inzynierka.regulatory;

import Jama.Matrix;
import com.example.inzynierka.obiekty.SISO;
import lombok.Data;

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
    private Matrix M;

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

    public DMC(int Nu, double lambda, SISO SISO, double cel, double duMax, int N)
    {
        this.Lambda = lambda;
        this.Nu = Nu;
        this.N = N;
        this.cel = cel;
        this.duMax = duMax;
        policzWartosci(SISO);

    }
    private void policzWartosci(SISO SISO)
    {
        policzS(SISO);
        policzMp();
        policzM();
        policzK();
        resetujRegulator();
    }
    @Override
    public void zmienWartosci(double[] wartosci){
        setLambda(wartosci[0]);
        policzK();
        resetujRegulator();
    }

    @Override
    public void resetujRegulator()
    {
       double[] uTemp = new double[D-1];
        Arrays.fill(uTemp,0);
        dU = new Matrix(uTemp,1);
    }
    private void policzS(SISO SISO)
    {
        double U = SISO.getUMax()/2;
        int i = 2;
        S = new ArrayList<Double>();
        S.add((SISO.obliczKrok(U)- SISO.getYpp())/U);
        S.add((SISO.obliczKrok(U)- SISO.getYpp())/U);
        while((!(S.get(i-1)==S.get(i-2))||S.get(i-2)==0.0)&&i<11)
        {
            S.add((SISO.obliczKrok(U)- SISO.getYpp())/U);
            i++;
        }

        this.D = S.size();

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
    private void policzM()
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
        this.M = M;
    }
    private void policzK()
    {
        Matrix I = new Matrix(Nu,Nu);
        for (int i = 0; i<Nu; i++)
        {
            for (int j = 0; j<Nu; j++)
            {
                if (i==j) {
                    I.set(i,j, getLambda());
                }
                else
                    I.set(i,j,0);
            }
        }
        this.K = ((M.transpose().times(M).plus(I)).inverse()).times(M.transpose());

    }
    private void dodajdU(double dUAktualne)
    {
        for(int i = D-1; i>1; i--)
        {
            dU.set(0,i-1,dU.get(0,i-2));
        }
        dU.set(0,0,dUAktualne);
    }
    public int liczbaZmiennych()
    {
        return 1;
    }
}
