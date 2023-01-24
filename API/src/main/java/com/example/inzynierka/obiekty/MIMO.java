package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiektMIMO;
import com.example.inzynierka.regulatory.Regulator;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class MIMO {

    private List<List<TransmitancjaCiagla>> transmitancja; //LIST<LIST-IN<OUT>>
    @ Getter(AccessLevel.NONE)
    private List<List<Double>> U;
    private List<List<Double>> Y;
    private double[] YMax;
    private double[] uMax;
    private String blad;
    private int liczbaOUT;
    private int liczbaIN;
    private int delayMax = 0;
    public MIMO() {}
    public MIMO(ParObiektMIMO[] parObiektMIMOS)
    {
        stworzTransmitancje(parObiektMIMOS);
        liczbaOUT = transmitancja.get(0).size();
        liczbaIN = transmitancja.size();
        obliczDelayMax();
        obliczUMax(parObiektMIMOS);

        this.Y = new ArrayList();
        for (int i = 0; i < this.liczbaOUT; i++)
        {
            Y.add(new ArrayList(Collections.nCopies(3, transmitancja.get(i).get(0).getYpp())));
        }
        this.U = new ArrayList();
        for (int i = 0; i < this.liczbaIN; i++)
        {
            U.add(new ArrayList(Collections.nCopies(3+delayMax, transmitancja.get(i).get(0).getUpp())));
        }
        obliczYMax();
    }
    public double[] obliczKrok(double[] du)
    {
        obliczU(du);
        double[] Yakt = new double[liczbaIN];
        for (int i = 0; i<liczbaOUT; i++)
        {
            double YaktIN = 0.0;
            for (int j = 0; j<liczbaIN; j++)
            {
                    YaktIN+= transmitancja.get(j).get(i).obliczKrok(U.get(j));
            }
            Yakt[i] = YaktIN;
        }
        for(int i = 0; i<liczbaOUT; i++) {
            List<Double> Ytemp = Y.get(i);
            for (int j = Y.get(i).size() - 1; j > 0; j--)
                Ytemp.set(j, Ytemp.get(j-1));
            Ytemp.set(0, Yakt[i]);
            Y.set(i,Ytemp);
        }
        return Yakt;
    }

    public double obliczKrok(double du, int IN, int OUT)
    {
        obliczU(du, IN);
        double YaktIN = 0.0;
        YaktIN+= transmitancja.get(IN).get(OUT).obliczKrok(U.get(IN));
        List<Double> Ytemp = Y.get(OUT);
        for (int j = Y.get(OUT).size() - 1; j > 0; j--)
            Ytemp.set(j, Ytemp.get(j - 1));
        Ytemp.set(0, YaktIN);
        Y.set(OUT, Ytemp);

        return YaktIN;
    }

    public void stworzTransmitancje(ParObiektMIMO[] parObiektMIMOS)
    {
        this.transmitancja = new ArrayList();
        for(ParObiektMIMO parObiekt: parObiektMIMOS)
        {
            List<TransmitancjaCiagla> transmitancjaTemp = new ArrayList();
            for(int i = 0; i<parObiekt.getGain().length; i++)
            {

            transmitancjaTemp.add(new TransmitancjaCiagla(
                    parObiekt.getGain()[i],
                    parObiekt.getR1()[i],
                    parObiekt.getQ1()[i],
                    parObiekt.getR2()[i],
                    parObiekt.getQ2()[i],
                    parObiekt.getT1()[i],
                    parObiekt.getT2()[i],
                    parObiekt.getT3()[i],
                    parObiekt.getDelay()[i],
                    parObiekt.getTp()));
            }
        this.transmitancja.add(transmitancjaTemp);
        }
    }
    private void obliczUMax(ParObiektMIMO[] obiektyMIMO)
    {
        this.uMax= new double[obiektyMIMO.length];
        for(int i = 0; i<obiektyMIMO.length; i++)
            this.uMax[i]=obiektyMIMO[i].getUMax();

    }
    public void obliczU(double[] du)
    {
        for(int j = 0; j<du.length; j++)
        {
            double Uakt = U.get(j).get(0) + du[j];
            if(uMax[j]>0.0)
            {
                if(Uakt>uMax[j])
                    Uakt=uMax[j];
                else if (Uakt<0.0)
                    Uakt=0.0;
            }
            else
            {
                if(Uakt<uMax[j])
                    Uakt=uMax[j];

                else if (Uakt>0.0)
                    Uakt=0.0;

            }

            for(int i = U.get(j).size()-1; i>0 ;i--)
                U.get(j).set(i,U.get(j).get(i-1));
            U.get(j).set(0,Uakt);
        }
    }
    public void obliczU(double du, int IN)
    {
        double Uakt = U.get(IN).get(0) + du;
        if(uMax[IN]>0)
        {
            if(Uakt>uMax[IN]) {
                Uakt=uMax[IN];
            }
            else if (Uakt<0.0) {
                Uakt=0.0;
            }
        }
        else
        {
            if(Uakt<uMax[IN]) {
                Uakt=uMax[IN];
            }
            else if (Uakt>0.0) {
                Uakt=0.0;
            }
        }
        for(int i = U.get(IN).size()-1; i>0 ;i--)
            U.get(IN).set(i,U.get(IN).get(i-1));
        U.get(IN).set(0,Uakt);
    }

    public double getTp(int IN)
    {
        return transmitancja.get(IN).get(0).getTp();
    }

    public double[] getAktualne(){
        double[] YAkt = new double[liczbaIN];
        for (int i = 0; i<liczbaIN; i++)
            YAkt[i] = Y.get(i).get(0);
        return YAkt;
    }
    public double obliczPraceObiektu(Regulator regulator, double[] cel)
    {
        int dlugoscBadania = 50;
        resetObiektu();
        double blad = 0.0;
        double[] tempCel = new double[liczbaOUT];


        for(int k = 0; k < liczbaOUT; k++)
        {
            for(int i = 0; i < liczbaOUT; i++)
                tempCel[i] = 0;
            tempCel[k] = cel[k];
            regulator.setCel(tempCel);
            resetObiektu();
            regulator.resetujRegulator();
            for (int i = 0; i<dlugoscBadania; i++)
            {
                double[] Ytepm = obliczKrok(regulator.policzOutput(getAktualne()));
                for(int j = 0; j < Ytepm.length; j++)
                {
                    blad+=Math.pow(Ytepm[j]-tempCel[j],2);
                }
            }
        }
        blad=blad/dlugoscBadania*liczbaOUT*liczbaOUT;
        resetObiektu();
        return blad;
    }

    public void SetU (List<List<Double>> noweU)
    {
        this.U = noweU;
    }

    public void SetY (List<List<Double>> noweY)
    {
        this.Y = noweY;
    }

    public double[] getYMax()
    {
        return YMax;
    }
    public double getUMax(int IN)
    {
        return getUMax()[IN];
    }
    public double getYpp(int IN)
    {
        return 0.0;
    }
    public double obliczBlad(int dlugosc, List<double[]> wyjscie, double[] cel) {
        double blad = 0.0;
        switch (this.blad) {
            case "mediana":
                for (int i = 0; i < cel.length; i++)
                    blad += wyjscie.get(0)[(int) Math.floor(dlugosc / cel.length)] - cel[i];
                break;
            case "srednio":
                for(int i = 0; i < dlugosc; i++)
                    for(int j = 0; j < cel.length; j++)
                        blad+=Math.pow(wyjscie.get(j)[i]-cel[j],2);
                break;
            case "absolutny":
                for(int i = 0; i < dlugosc; i++)
                    for(int j = 0; j < cel.length; j++)
                        blad+=Math.abs(wyjscie.get(j)[i]-cel[j]);
                break;
            default:
                break;
            }
        return blad;
    }

    private void obliczYMax()
    {
        double[] Ytemp;

        this.YMax = new double[liczbaOUT];
        for(int i = 0; i<liczbaOUT; i++)
            this.YMax[i] = 0.0;
        double[] uMax = new double[liczbaIN];
        for(int i = 0; i < liczbaIN; i ++)
        {
            uMax[i] = this.uMax[i];
        }
        for (int i = 0; i<100; i++)
        {
            obliczKrok(uMax);
        }
        Ytemp = getAktualne();
        resetObiektu();
        for(int i = 0; i<this.YMax.length; i++)
            if(this.YMax[i]<Ytemp[i])
                this.YMax[i] = Ytemp[i];
    }

    public void resetObiektu()
    {
        for (int i = 0; i < this.liczbaOUT; i++)
        {
            Y.set(i, new ArrayList(Collections.nCopies(3, transmitancja.get(i).get(0).getYpp())));
        }
        for (int i = 0; i < this.liczbaIN; i++)
        {
            U.set(i, new ArrayList(Collections.nCopies(3+delayMax, transmitancja.get(i).get(0).getUpp())));
        }
        for(List<TransmitancjaCiagla> ListaTransmitancji: transmitancja)
            for(TransmitancjaCiagla tran: ListaTransmitancji)
                tran.reset();
    }
    public void obliczDelayMax()
    {
        for(List<TransmitancjaCiagla> listaTransmitancji: transmitancja)
        {
            for(TransmitancjaCiagla tran: listaTransmitancji)
            {
                if(tran.getDelay()>this.delayMax) {
                    this.delayMax=tran.getDelay();
                }
            }
        }
    }
}
