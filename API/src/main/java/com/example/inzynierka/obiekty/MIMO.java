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

    private List<List<Transmitancja>> transmitancje; //LIST<LIST-IN<OUT>>
    @ Getter(AccessLevel.NONE)
    private List<List<Double>> U;
    private List<List<Double>> Y;
    private double[] YMax;
    private String blad;
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
                for(int k = 0; k< transmitancje.get(i).get(j).getZ().length; k++)
                    YaktIN+= U.get(i).get(k+ transmitancje.get(i).get(j).getOpoznienie())* transmitancje.get(i).get(j).getZ()[k];
                for(int k = 0; k< transmitancje.get(i).get(j).getB().length; k++)
                    YaktIN+= Y.get(i).get(k)* transmitancje.get(i).get(j).getB()[k];
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

        for(int k = 0; k< transmitancje.get(IN).get(OUT).getZ().length; k++)
            YaktIN+= U.get(IN).get(k+ transmitancje.get(IN).get(OUT).getOpoznienie())* transmitancje.get(IN).get(OUT).getZ()[k];
        for(int k = 0; k< transmitancje.get(IN).get(OUT).getB().length; k++)
            YaktIN+= Y.get(OUT).get(k)* transmitancje.get(IN).get(OUT).getB()[k];

        List<Double> Ytemp = Y.get(OUT);
        for (int j = Y.get(OUT).size() - 1; j > 0; j--)
            Ytemp.set(j, Ytemp.get(j - 1));
        Ytemp.set(0, YaktIN);
        Y.set(OUT, Ytemp);

        return YaktIN;
    }

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
    public void obliczU(double du, int IN)
    {

        double Uakt = U.get(IN).get(0) + du;
        if(Uakt>transmitancje.get(IN).get(0).getUMax()) {
            Uakt=transmitancje.get(IN).get(0).getUMax();
        }
        else if (Uakt<transmitancje.get(IN).get(0).getUMin()) {
            Uakt=transmitancje.get(IN).get(0).getUMin();
        }
        for(int i = U.get(IN).size()-1; i>0 ;i--)
            U.get(IN).set(i,U.get(IN).get(i-1));
        U.get(IN).set(0,Uakt);

    }

    public double getTs(int IN)
    {
        return transmitancje.get(IN).get(0).getTs();
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
        return transmitancje.get(IN).get(0).getUMax();
    }
    public double getYpp(int IN)
    {
        return transmitancje.get(IN).get(0).getYpp();
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
            uMax[i] = transmitancje.get(i).get(0).getUMax();
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
            Y.set(i, new ArrayList(Collections.nCopies(3, transmitancje.get(i).get(0).getYpp())));
        }
        for (int i = 0; i < this.liczbaIN; i++)
        {
            U.set(i, new ArrayList(Collections.nCopies(3+transmitancje.get(i).get(0).getOpoznienie(), transmitancje.get(i).get(0).getUpp())));
        }
    }

    @Data
    class Transmitancja {

        private double[] z;
        private double[] b;
        private double K;
        private double uMax;
        private double uMin = 0;

        private double Ypp;
        private double Upp;

        private double Ts;
        private int opoznienie;
        private double szum;


        public Transmitancja(Double[] z, Double[] b, double K, double uMax, double Ts, int opoznienie, double szum)
        {
            this.Ts = Ts;
            this.setK(K);
            this.setUMax(uMax);
            this.Ypp = 0;
            this.Upp = 0;
            this.opoznienie = opoznienie;
            if(z.length==2&&b.length==3)
                obiekt3b2z(z,b);
            else if(z.length==1&&b.length==3)
                obiekt3b1z(z,b);
            else
                obiektProsty();

        }



        private void obiekt3b2z(Double[] z, Double[] b)
        {
            double z1 = z[0];
            double z2 = z[1];
            double b1 = b[0];
            double b2 = b[1];
            double b3 = b[2];
            this.z = new double[3];
            this.b = new double[3];

            double k = (b1*b1 + (-(z1 + z2)*b1)+z1*z2)/((b2 - b1)*b3 - b1*b2 + b1*b1)/b1;
            double l = -(b2*b2+(-(z1 + z2)*b2 + z1*z2))/((b2 - b1)*b3 - b2*b2 + b1*b2)/b2;
            double m = (b3*b3+(-(z1 + z2)*b3 + z1*z2))/(b3*b3 - (b1 + b2)*b3 + b1*b2)/b3;
            double ap = ePotega(b1);
            double bp = ePotega(b2);
            double cp = ePotega(b3);
            this.z[0] = this.K*(-k*ap + k - l*bp + l - m*cp + m);
            this.z[1] = this.K*(k*ap*cp + k*ap*bp - k*cp - k*bp + l*bp*cp + l*bp*ap -l*cp - l*ap + m*bp*cp + m*ap*cp - m*bp - m*ap);
            this.z[2] = this.K*(-k*ap*bp*cp + k*bp*cp - l*ap*bp*cp + l*ap*cp - m*ap*bp*cp +m*ap*bp);

            this.b[0] = ap+bp+cp;
            this.b[1] = ap*bp + ap*cp + bp*cp;
            this.b[2] = ap*bp*cp;
        }
        private void obiekt3b1z(Double[] z, Double[] b)
        {
            double z1 = z[0];
            double b1 = b[0];
            double b2 = b[1];
            double b3 = b[2];
            this.z = new double[3];
            this.b = new double[3];
            double k = (z1 - b1)/(b1*b1 - b1*b2 + b3*(b2-b1))/b1;
            double l = (z1 - b2)/(b2*b2 - b1*b2 + b3*(b2-b1))/b2;
            double m = (b3 - z1)/(b3*b3 + b1*b2 - b3*(b1+b2))/b3;
            double ap = ePotega(b1);
            double bp = ePotega(b2);
            double cp = ePotega(b3);
            this.z[0] = this.K*(-k*ap + k - l*bp + l - m*cp + m);
            this.z[1] = this.K*(k*ap*cp + k*ap*bp - k*cp - k*bp + l*bp*cp + l*bp*ap -l*cp - l*ap + m*bp*cp + m*ap*cp - m*bp - m*ap);
            this.z[2] = this.K*(-k*ap*bp*cp + k*bp*cp - l*ap*bp*cp + l*ap*cp - m*ap*bp*cp +m*ap*bp);

            this.b[0] = ap+bp+cp;
            this.b[1] = ap*bp + ap*cp + bp*cp;
            this.b[2] = ap*bp*cp;

        }
        private void obiektProsty()
        {
            this.z = new double[1];
            this.b = new double[0];
            this.z[0]=this.K;
        }

        private double ePotega(double x)
        {
            return Math.exp(-x*this.getTs());
        }

    }

}
