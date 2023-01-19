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
public class MIMO extends SISO {

    private List<List<Transmitancja>> transmitancje;
    @ Getter(AccessLevel.NONE)
    private List<List<Double>> U;
    private List<List<Double>> Y;
    private double[] YMax;

    private int liczbaOUT;
    private int liczbaIN;
    public MIMO() {}
    public MIMO(ParObiektMIMO[] parObiektMIMOS)
    {
        stworzTransmitancje(parObiektMIMOS);
        liczbaOUT = transmitancje.get(0).size();
        liczbaIN = transmitancje.size();

        this.Y = new ArrayList();
        for (int i = 0; i < this.liczbaOUT; i++)
        {
            Y.add(new ArrayList(Collections.nCopies(3, transmitancje.get(i).get(0).getYpp())));
        }
        this.U = new ArrayList();
        for (int i = 0; i < this.liczbaIN; i++)
        {
            U.add(new ArrayList(Collections.nCopies(3+transmitancje.get(i).get(0).getOpoznienie(), transmitancje.get(i).get(0).getUpp())));
        }
        obliczYMax();
    }
    public double[] obliczKrok(double[] du)
    {
        obliczU(du);
        double[] Yakt = new double[liczbaIN];
        for (int i = 0; i<liczbaIN; i++)
        {
            double YaktIN = 0.0;
            for (int j = 0; j<liczbaOUT; j++)
            {
                for(int k = 0; k< transmitancje.get(j).get(i).getZ().length; k++)
                    YaktIN+= U.get(i).get(k+ transmitancje.get(j).get(i).getOpoznienie())* transmitancje.get(j).get(i).getZ()[k];
                for(int k = 0; k< transmitancje.get(j).get(i).getB().length; k++)
                    YaktIN+= Y.get(i).get(k)* transmitancje.get(j).get(i).getB()[k];
            }

            Yakt[i] = YaktIN;
        }
        return Yakt;
    }
//    public double[] obliczPraceObiektu(Regulator[] regulator, double[] cel)
//    {
//        int dlugoscBadania = 50;
//        resetObiektu();
//        double blad = 0.0;
//        regulator.setCel(cel);
//        for (int i = 0; i<dlugoscBadania; i++)
//        {
//            if(i==25)
//                blad = Math.abs(obliczKrok(regulator.policzOutput(getAktualna()))-cel);
////            blad+=Math.pow(obliczKrok(regulator.policzOutput(getAktualna()))-cel,2);
//        }
////        blad=blad/dlugoscBadania;
//        resetObiektu();
//        return blad;
//    }
    public void stworzTransmitancje(ParObiektMIMO[] parObiektMIMOS)
    {
        this.transmitancje = new ArrayList();
        for(ParObiektMIMO parObiekt: parObiektMIMOS)
        {
            List<Transmitancja> transmitancjaTemp = new ArrayList();
            for(int i = 0; i<parObiekt.getB1().length; i++)
            {
                Double[] z;
                if (parObiekt.getZ2()[i] != null)
                    z = new Double[]{parObiekt.getZ1()[i], parObiekt.getZ2()[i]};
                else if (parObiekt.getZ1()[i] != null)
                    z = new Double[]{parObiekt.getZ1()[i]};
                else
                    z = new Double[]{};

                Double[] b;
                if (parObiekt.getB3()[i] != null)
                    b = new Double[]{parObiekt.getB1()[i], parObiekt.getB2()[i], parObiekt.getB3()[i]};
                else if (parObiekt.getB2()[i] != null)
                    b = new Double[]{parObiekt.getB1()[i], parObiekt.getB2()[i]};
                else if (parObiekt.getB1()[i] != null)
                    b = new Double[]{parObiekt.getB1()[i]};
                else
                    b = new Double[]{};
            transmitancjaTemp.add(new Transmitancja(z, b, parObiekt.getK()[i], parObiekt.getUMax(), parObiekt.getTs(), parObiekt.getOpoznienie(), parObiekt.getSzum()));
            }
        this.transmitancje.add(transmitancjaTemp);
        }
    }

    public void obliczU(double[] du)
    {
        for(int j = 0; j<du.length; j++)
        {
            double Uakt = U.get(j).get(0) + du[j];
            if(Uakt>transmitancje.get(j).get(0).getUMax()) {
                Uakt=transmitancje.get(j).get(0).getUMax();
            }
            else if (Uakt<transmitancje.get(j).get(0).getUMin()) {
                Uakt=transmitancje.get(j).get(0).getUMin();
            }
            for(int i = U.get(j).size()-1; i>0 ;i--)
                U.get(j).set(i,U.get(j).get(i-1));
            U.get(j).set(0,Uakt);
        }
    }



    public double[] getAktualne(){
        double[] YAkt = new double[liczbaIN];
        for (int i = 0; i<liczbaIN; i++)
            YAkt[i] = Y.get(i).get(0);

        return YAkt;
    }
    public void SetU (List<List<Double>> noweU)
    {
        this.U = noweU;
    }

    public void SetY (List<List<Double>> noweY)
    {
        this.Y = noweY;
    }
    @Override
    public double[] getYMax()
    {
        return YMax;
    }

    private void obliczYMax()
    {
        double[] Ytemp;
        double[] uMax = new double[liczbaIN];
        for(int i = 0; i < liczbaIN; i ++)
        {
            uMax[i] = transmitancje.get(i).get(0).getUMax();
        }
        for (int i = 0; i<100; i++)
        {
            obliczKrok(uMax);
        }
        Ytemp = getAktualne();
        resetObiektu();
        this.YMax = Ytemp;
    }

}
