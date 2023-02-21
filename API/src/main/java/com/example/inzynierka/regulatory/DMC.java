package com.example.inzynierka.regulatory;

import Jama.Matrix;
import com.example.inzynierka.obiekty.MIMO;
import com.example.inzynierka.obiekty.SISO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class DMC extends Regulator{
    private Integer D;
    private Integer N;
    private Integer Nu;
    private List<Double> Lambda;
    private Matrix Mp;
    private Matrix K;
    private List<List<Double>> S;
    private Matrix dU;
    private Matrix M;
    private double duMax;
    private double[] cel;
    private Double[] strojenieZadane;
    private int liczbaStrojeniaZadanego;

    public DMC(int Nu, double lambda, SISO SISO, double cel, double duMax, int N, Double[] strojenieZadane)
    {
        this(Nu, lambda, SISO, cel, duMax, N);
        if(strojenieZadane[0]!=null)
        {
            liczbaStrojeniaZadanego=1;
            this.strojenieZadane = strojenieZadane;
            this.getLambda().set(0,strojenieZadane[0]);
            this.policzWartosci(SISO);
        }
    }
    public DMC(int Nu, double lambda, SISO siso, double cel, double duMax, int N)
    {
        this.Lambda = Arrays.asList(lambda);
        this.Nu = Nu;
        this.N = N;
        this.cel = new double[]{cel};
        this.duMax = duMax;
        policzWartosci(siso);

    }
    public DMC(int Nu, double[] lambda, MIMO obiekt, double[] cel, double duMax, int N, Double[] strojenieZadane)
    {
        this(Nu, lambda, obiekt, cel, duMax, N);
        this.liczbaStrojeniaZadanego=0;
        this.strojenieZadane = strojenieZadane;
        for(int i = 0; i <strojenieZadane.length; i++)
        {
            if(strojenieZadane[i]!=null)
            {
                liczbaStrojeniaZadanego+=1;
                this.getLambda().set(i,strojenieZadane[i]);
            }
        }
        this.policzWartosci(obiekt);
    }
    public DMC(int Nu, double[] lambda, MIMO obiekt, double[] cel, double duMax, int N)
    {
        List<Double> tempLambda = new ArrayList();
        for(double wartosc : lambda)
        {
            tempLambda.add(wartosc);
        }
        this.Lambda = new ArrayList<>(tempLambda);
        this.Nu = Nu;
        this.N = N;
        this.cel = cel;
        this.duMax = duMax;
        policzWartosci(obiekt);
    }

    public double policzOutput(double aktualna)
    {

        List<double[]> yZadTemp = new ArrayList<>(cel.length);
        for(int i = 0; i<cel.length; i++)
        {
            double[] tempCel = new double[N];
            Arrays.fill(tempCel,cel[i]);
            yZadTemp.add(tempCel);
        }
        Matrix yZad = new Matrix(cel.length, N);
        for(int i = 0; i<cel.length; i++)
            for(int j = 0; j<N; j++)
                yZad.set(i,j,cel[i]);
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

    public double[] policzOutput(double[] aktualna)
    {
        int OUT = cel.length; //TODO zaimplementowa

        double[] celTemp = new double[N * OUT];
        for(int i = 0; i<N * OUT; i++)
        {
            celTemp[i] = cel[i%OUT];
        }
        Matrix yZad = new Matrix(celTemp,1);


        double[] yTemp = new double[N * OUT];
        for(int i = 0; i<N * OUT; i++)
        {
            yTemp[i] = aktualna[i%OUT];
        }
        Matrix y = new Matrix(yTemp,1);
        //UTEMP wychodzi odwrócone
        Matrix Utemp = K.times(yZad.transpose().minus(y.transpose()).minus(Mp.times(dU.transpose())));
        double[] tempdU = new double[getLambda().size()];
        for(int i = 0; i<getLambda().size(); i++) {
            //TODO DO POPRAWY
            if (Utemp.get(i, 0) > duMax) {
                Utemp.set(i, 0, duMax);
            } else if (Utemp.get(i, 0) < -duMax) {
                Utemp.set(i, 0, -duMax);
            }
            tempdU[i] = Utemp.get(i,0);
        }
        dodajdU(tempdU);
        return tempdU;
    }
    private void policzWartosci(SISO siso)
    {
        this.S = new ArrayList();
        policzS(siso);
        policzMp();
        policzM();
        policzK();
        resetujRegulator(1);
        siso.resetObiektu();
    }
    private void policzWartosci(MIMO mimo)
    {
        this.S = new ArrayList();
        policzS(mimo);
        policzMp(mimo.getLiczbaIN(),mimo.getLiczbaOUT());
        policzM(mimo.getLiczbaIN(),mimo.getLiczbaOUT());
        policzK(mimo.getLiczbaIN());
        resetujRegulator(mimo.getLiczbaIN());
        mimo.resetObiektu();
    }

    public void zmienWartosci(double[] wartosci){
        List<Double> tempLambda = new ArrayList();
        if(this.liczbaStrojeniaZadanego==0)
        {
            for(double wartosc : wartosci)
            {
                tempLambda.add(wartosc);
            }
        }
        else
        {
            int iTemp = 0;
            for(int i = 0; i<getLambda().size(); i++)
            {
                if(strojenieZadane[i]!=null)
                {
                    tempLambda.add(strojenieZadane[i]);
                }
                else
                {
                    tempLambda.add(wartosci[iTemp]);
                    iTemp+=1;
                }
            }
        }
        setLambda(tempLambda);
        policzK(tempLambda.size());
        resetujRegulator(tempLambda.size());
    }
    public void resetujRegulator()
    {
        for(int i = 0; i < dU.getColumnDimension(); i++)
            for(int j = 0; j < dU.getRowDimension(); j++)
                dU.set(j,i,0.0);
    }

    public void resetujRegulator(int IN)
    {
        dU = new Matrix(1, (D-1)*IN, 0.0);
    }

    private void policzS(SISO SISO)
    {
        double U = SISO.getUMax()/2;
        int i = 2;
        List<Double> Stemp = new ArrayList<Double>();
        double Utemp = 0;
        SISO.resetObiektu();
        Stemp.add((SISO.obliczKrok(U)- SISO.getYpp())/U);
        Stemp.add((SISO.obliczKrok(Utemp)- SISO.getYpp())/U);
        while(!(Math.abs(Stemp.get(i-1)-Stemp.get(i-2))<0.005) || Stemp.get(i-2)==0.0)
        {
            Stemp.add((SISO.obliczKrok(Utemp)- SISO.getYpp())/U);
            i++;
        }
        this.S.add(Stemp);
        this.D = S.get(0).size();
        this.N = D;

    }
    private void policzS(MIMO obiekt)
    {
        for(int i = 0; i < obiekt.getLiczbaOUT(); i ++)
        {
           for (int j = 0; j < obiekt.getLiczbaIN(); j++)
           {
            obiekt.resetObiektu();
            double U = obiekt.getUMax(j)/2;
            double Utemp = 0;
            int k = 2;
            List<Double> Stemp = new ArrayList();
            Stemp.add((obiekt.obliczKrok(U, j, i)- obiekt.getYpp(i))/U);
            Stemp.add((obiekt.obliczKrok(Utemp, j, i)- obiekt.getYpp(i))/U);
           while(!(Math.abs(Stemp.get(k-1)-Stemp.get(k-2))<0.005) || Stemp.get(k-2)==0.0)
            {
                Stemp.add((obiekt.obliczKrok(Utemp,j, i )- obiekt.getYpp(i))/U);
                k++;
            }
            this.S.add(Stemp);
            }
        }
        this.D = S.get(0).size();
        for (int i = 0; i < S.size(); i++)
        {
            if(D<S.get(i).size())
            {
                D=S.get(i).size();
            }
        }
        for (int i = 0; i < S.size(); i++)
        {
            while(D!=S.get(i).size())
            {
                S.get(i).add(S.get(i).get(S.size()));
            }
        }
        this.N=D;
    }
    private void policzM()
    {
        Matrix M = new Matrix(N,Nu);
        for (int i = 0; i<Nu; i++)
        {
            for (int j = 0; j<N; j++)
            {
                if(j>=i)
                    M.set(j,i,S.get(0).get(j-i));
                else
                    M.set(j,i,0);
            }
        }
        this.M = M;
    }
    private void policzM(int IN, int OUT)
    {
        Matrix M = new Matrix(N*OUT,Nu*IN);
        for (int i = 0; i<Nu; i++)
        {
            for (int j = 0; j<N; j++)
            {
                if(j>=i)
                    for(int k = 0; k<OUT; k++)
                        for(int m = 0; m<IN; m++)
                            M.set(j*OUT+k,i*IN+m,S.get(k*IN+m).get(j-i));
            }
        }
        this.M = M;
    }
    private void policzMp()
    {
        Mp = new Matrix(N,D-1);
        for (int i = 0; i<D-1; i++)
        {
            for (int j = 0; j<N; j++)
            {
                if((j+i+1)<D)
                    Mp.set(j,i,S.get(0).get(j+i+1)-S.get(0).get(i));
                else
                    Mp.set(j,i,S.get(0).get(D-1)-S.get(0).get(i));
            }
        }
    }
    private void policzMp(int IN, int OUT)
    {
        Mp = new Matrix(N*OUT,(D-1)*IN);
        for (int i = 0; i<D-1; i++)
        {
            for (int j = 0; j<N; j++)
            {
                for(int k = 0; k<OUT; k++) {
                    for (int m = 0; m < IN; m++) {
                        if ((j + i + 1) < D) {
                            Mp.set(j*OUT+k, i*IN+m, S.get(k*IN+m).get(j + i + 1) - S.get(k*IN+m).get(i));
                        } else
                            Mp.set(j*OUT+k, i*IN+m, S.get(k*IN+m).get(D - 1) - S.get(k*IN+m).get(i));
                    }
                }
            }
        }
    }
    private void policzK()
    {
        Matrix I = new Matrix(Nu,Nu);
        for (int i = 0; i<Nu; i++)
        {
            for (int j = 0; j<Nu; j++)
            {
                if (i==j) {
                    I.set(i,j, getLambda().get(0));
                }
                else
                    I.set(i,j,0);
            }
        }
        this.K = ((M.transpose().times(M).plus(I)).inverse()).times(M.transpose());
    }
    private void policzK(int IN)
    {
        Matrix I = new Matrix(Nu*IN,Nu*IN);
        for (int i = 0; i<Nu; i++)
        {
            for (int j = 0; j<Nu; j++)
            {
                for (int m = 0; m < IN; m++) {
                    if (i == j) {
                        I.set(i*IN+m, j*IN+m, getLambda().get(m));
                    } else
                        I.set(i*IN+m, j*IN+m, 0);
                }
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
    private void dodajdU(double[] dUAktualne)
    {

        for(int i = D-1; i>1; i--)
        {
            for(int j = dUAktualne.length-1; j>=0; j--)
                dU.set(0, dUAktualne.length*i-1-j,dU.get(0,dUAktualne.length*i-dUAktualne.length-j));
        }

        for(int j = dUAktualne.length-1; j>=0; j--)
            dU.set(0,j,dUAktualne[j]);

    }
    public int liczbaZmiennych()
    {
        return getLambda().size() - liczbaStrojeniaZadanego;
    }
}
