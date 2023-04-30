package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiektDPAMIMO;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class MIMODPA extends MIMO{

    private List<List<DPA>> transmitancja; //LIST<LIST-IN<OUT>>
    private List<List<Double>> U;
    private List<List<Double>> Y;
    private double[] YMax;
    private double[] uMin;
    private double[] uMax;
    private String blad;
    private int liczbaOUT;
    private int liczbaIN;
    private int delayMax = 0;
    private int dlugosc;
    private MIMODPA zakloceniaMierzalne;

    public MIMODPA() {
    }

    public MIMODPA(ParObiektDPAMIMO[] parObiektDPAMIMOS)
    {
        stworzTransmitancje(parObiektDPAMIMOS);
        liczbaOUT = transmitancja.get(0).size();
        liczbaIN = transmitancja.size();
        obliczDelayMax();
        this.blad = "srednio";
        obliczUMax(parObiektDPAMIMOS);
        this.Y = new ArrayList<>();
        for (int i = 0; i < this.liczbaOUT; i++) {
            Y.add(new ArrayList(Collections.nCopies(3, transmitancja.get(0).get(i).getYpp())));
        }
        this.U = new ArrayList<>();
        for (int i = 0; i < this.liczbaIN; i++) {
            U.add(new ArrayList(Collections.nCopies(3 + delayMax, transmitancja.get(i).get(0).getUpp())));
        }
        obliczDlugosc();
        obliczYMax();
    }
    public MIMODPA(ParObiektDPAMIMO[] parObiektDPAMIMOS, String blad, MIMODPA zakloceniaMierzalne) {
        this(parObiektDPAMIMOS, blad);
        setZakloceniaMierzalne(zakloceniaMierzalne);
    }
    public MIMODPA(ParObiektDPAMIMO[] parObiektDPAMIMOS, String blad) {
        stworzTransmitancje(parObiektDPAMIMOS);
        liczbaOUT = transmitancja.get(0).size();
        liczbaIN = transmitancja.size();
        obliczDelayMax();
        this.blad = blad;
        obliczUMax(parObiektDPAMIMOS);

        this.Y = new ArrayList<>();
        for (int i = 0; i < this.liczbaOUT; i++) {
            Y.add(new ArrayList(Collections.nCopies(3, transmitancja.get(0).get(i).getYpp())));
        }
        this.U = new ArrayList<>();
        for (int i = 0; i < this.liczbaIN; i++) {
            U.add(new ArrayList(Collections.nCopies(3 + delayMax, transmitancja.get(i).get(0).getUpp())));
        }
        obliczDlugosc();
        obliczYMax();
    }

    public double[] obliczKrok(double[] du) {
        obliczU(du);
        double[] Yakt = new double[liczbaOUT];
        for (int i = 0; i < liczbaOUT; i++) {
            double YaktIN = 0.0;
            for (int j = 0; j < liczbaIN; j++) {
                YaktIN += transmitancja.get(j).get(i).obliczKrok(U.get(j));
            }
            Yakt[i] = YaktIN;
        }
        dodajY(Yakt);
        return Yakt;
    }

    private void dodajY(double[] Yakt) {
        for (int i = 0; i < liczbaOUT; i++) {
            dodajY(i, Yakt[i]);
        }
    }

    public double[] obliczKrok(double[] du, double[] dUz) {
        obliczU(du);
        double[] Yakt = zakloceniaMierzalne.obliczKrok(dUz);
        for (int i = 0; i < liczbaOUT; i++) {
            double YaktIN = 0.0;
            for (int j = 0; j < liczbaIN; j++) {
                YaktIN += transmitancja.get(j).get(i).obliczKrok(U.get(j));
            }
            Yakt[i] += YaktIN;
        }
        dodajY(Yakt);
        return Yakt;
    }
    public double obliczKrok(double du, int IN, int OUT) {
        obliczU(du, IN);
        double YaktIN = transmitancja.get(IN).get(OUT).obliczKrok(U.get(IN));
        dodajY(OUT, YaktIN);

        return YaktIN;
    }

    private void dodajY(int OUT, double YaktIN) {
        List<Double> Ytemp = Y.get(OUT);
        for (int j = Y.get(OUT).size() - 1; j > 0; j--)
            Ytemp.set(j, Ytemp.get(j - 1));
        Ytemp.set(0, YaktIN);
        Y.set(OUT, Ytemp);
    }

    public double obliczKrokZaklocenia(double du, int IN, int OUT) {

        return zakloceniaMierzalne.obliczKrok(du, IN, OUT);
    }

    public void stworzTransmitancje(ParObiektDPAMIMO[] parObiektDPAMIMOS) {
        this.transmitancja = new ArrayList<>();
        for (ParObiektDPAMIMO parObiekt : parObiektDPAMIMOS) {
            List<DPA> transmitancjaTemp = new ArrayList<>();
            for (int i = 0; i < parObiekt.getGain().length; i++) {

                transmitancjaTemp.add(new DPA(
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

    private void obliczUMax(ParObiektDPAMIMO[] obiektyMIMO) {
        this.uMax = new double[obiektyMIMO.length];
        this.uMin = new double[obiektyMIMO.length];
        for (int i = 0; i < obiektyMIMO.length; i++) {
            this.uMax[i] = obiektyMIMO[i].getUMax();
            this.uMin[i] = obiektyMIMO[i].getUMin();
        }
    }

    private void obliczU(double[] du) {
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

    private void obliczU(double du, int IN) {
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

    public double getTp(int IN) {
        return transmitancja.get(IN).get(0).getTp();
    }

    public double[] getAktualne() {
        double[] YAkt = new double[liczbaOUT];
        for (int i = 0; i < liczbaOUT; i++)
            YAkt[i] = Y.get(i).get(0);
        return YAkt;
    }

    public double obliczPraceObiektu(Regulator regulator, double[] cel) {

        resetObiektu();
        double bladSymulacji = 0.0;
        if(zakloceniaMierzalne !=null) {
            bladSymulacji = obliczPraceZZakloceniem(regulator, cel, bladSymulacji);
        } else {
            bladSymulacji = obliczPraceBezZaklocen(regulator, cel, bladSymulacji);
        }
        bladSymulacji = bladSymulacji / this.dlugosc * liczbaOUT * liczbaOUT;
        resetObiektu();
        return bladSymulacji;
    }

    private double obliczPraceBezZaklocen(Regulator regulator, double[] cel, double bladSymulacji) {
        double[] tempCel = new double[liczbaOUT];
        for (int k = 0; k < liczbaOUT; k++) {
            for (int i = 0; i < liczbaOUT; i++)
                tempCel[i] = 0;
            tempCel[k] = cel[k];
            resetObiektu();
            regulator.resetujRegulator();
            regulator.setCel(tempCel);
            for (int i = 0; i < this.dlugosc; i++) {
                double[] Ytepm = obliczKrok(regulator.policzSterowanie(getAktualne()));
                for (int j = 0; j < liczbaOUT; j++) {
                    if (this.blad.equals("srednio"))
                        bladSymulacji += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        bladSymulacji += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
        }
        return bladSymulacji;
    }

    private double obliczPraceZZakloceniem(Regulator regulator, double[] cel, double bladSymulacji) {
        double[] tempCel = new double[liczbaOUT];
        for (int k = 0; k < liczbaOUT; k++) {
            for (int i = 0; i < liczbaOUT; i++)
                tempCel[i] = 0;
            tempCel[k] = cel[k];
            regulator.setCel(tempCel);
            resetObiektu();
            regulator.resetujRegulator();
            for (int i = 0; i < Math.floorDiv(this.dlugosc,2); i++) {
                double[] Ytepm = obliczKrok(regulator.policzSterowanie(getAktualne()));
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio"))
                        bladSymulacji += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        bladSymulacji += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
            double[] zakloceniaU = new double[zakloceniaMierzalne.getTransmitancja().size()];
            for(int i = 0; i < zakloceniaMierzalne.getTransmitancja().size(); i++)
                zakloceniaU[i] = zakloceniaMierzalne.getUMax(i)/this.dlugosc;

            for (int i = 0; i < Math.floorDiv(this.dlugosc,8); i++) {
                double[] Ytepm = obliczKrok(regulator.policzSterowanie(getAktualne(), zakloceniaU),zakloceniaU);
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio"))
                        bladSymulacji += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        bladSymulacji += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
            for(int i = 0; i < zakloceniaMierzalne.getTransmitancja().size(); i++)
                zakloceniaU[i] = 0.0;

            for (int i = 0; i < Math.floorDiv(this.dlugosc*3,8); i++) {
                double[] Ytepm = obliczKrok(regulator.policzSterowanie(getAktualne(), zakloceniaU),zakloceniaU);
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio"))
                        bladSymulacji += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        bladSymulacji += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
        }
        return bladSymulacji;
    }


    public double getUMax(int IN) {
        return getUMax()[IN];
    }

    public double getYpp(int IN) {
        return 0.0;
    }

    public double obliczBlad(int dlugosc, List<double[]> wyjscie, double[] cel) {
        double blad = 0.0;
        switch (this.blad) {
            case "mediana":
                for (int i = 0; i < liczbaIN; i++)
                    blad += wyjscie.get(0)[Math.floorDiv(dlugosc, 2)] - cel[i];
                break;
            case "srednio":
                for (int i = 0; i < dlugosc; i++)
                    for (int j = 0; j < cel.length; j++)
                        blad += Math.pow(wyjscie.get(j)[i] - cel[j], 2);
                break;
            case "absolutny":
                for (int i = 0; i < dlugosc; i++)
                    for (int j = 0; j < cel.length; j++)
                        blad += Math.abs(wyjscie.get(j)[i] - cel[j]);
                break;
            default:
                break;
        }
        return blad;
    }

    private void obliczYMax() {
        double[] Ytemp;

        this.YMax = new double[liczbaOUT];
        for (int i = 0; i < liczbaOUT; i++)
            this.YMax[i] = 0.0;
        double[] uMaxTemp = new double[liczbaIN];
        for (int i = 0; i < liczbaIN; i++) {
            uMaxTemp[i] = this.uMax[i];
        }
        for (int i = 0; i < dlugosc * 2; i++) {
            obliczKrok(uMaxTemp);
        }
        Ytemp = getAktualne();
        resetObiektu();
        for (int i = 0; i < this.YMax.length; i++)
            if (this.YMax[i] < Ytemp[i])
                this.YMax[i] = Ytemp[i];
    }

    public void resetObiektu() {
        for (int i = 0; i < this.liczbaOUT; i++) {
            Y.set(i, new ArrayList(Collections.nCopies(3, transmitancja.get(0).get(i).getYpp())));
        }
        for (int i = 0; i < this.liczbaIN; i++) {
            U.set(i, new ArrayList(Collections.nCopies(3 + delayMax, transmitancja.get(i).get(0).getUpp())));
        }
        for (List<DPA> ListaTransmitancji : transmitancja)
            for (DPA tran : ListaTransmitancji)
                tran.reset();
        if(zakloceniaMierzalne !=null) {
            zakloceniaMierzalne.resetObiektu();
        }
    }

    public void obliczDelayMax() {
        for (List<DPA> listaTransmitancji : transmitancja) {
            for (DPA tran : listaTransmitancji) {
                if (tran.getDelay() > this.delayMax) {
                    this.delayMax = tran.getDelay();
                }
            }
        }
    }

    private void obliczDlugosc() {
        int dlugoscTemp = 40;
        for (int i = 0; i < this.getLiczbaOUT(); i++) {
            for (int j = 0; j < this.getLiczbaIN(); j++) {
                this.resetObiektu();
                double Uskok = this.getUMax(j) / 2;
                double Utemp = 0;

                int k = 2;
                List<Double> symulacja = new ArrayList<Double>();
                symulacja.add((this.obliczKrok(Uskok, j, i) - this.getYpp(i)) / Uskok);
                symulacja.add((this.obliczKrok(Utemp, j, i) - this.getYpp(i)) / Uskok);
                while ((!(Math.abs(symulacja.get(k - 1) - symulacja.get(k - 2)) < 0.005) || symulacja.get(k - 2) == 0.0)
                    &&((k<=10) || (k>10 && symulacja.get(k - 2) != 0.0))) {
                    symulacja.add((this.obliczKrok(Utemp, j, i) - this.getYpp(i)) / Uskok);
                    k++;
                }
                if(dlugoscTemp<symulacja.size())
                    dlugoscTemp = symulacja.size();
            }
        }
        this.dlugosc = dlugoscTemp;

    }

    public void setZakloceniaMierzalne(MIMODPA zaklocenieMierzalne) {
        this.zakloceniaMierzalne = zaklocenieMierzalne;
        zaklocenieMierzalne.resetObiektu();
    }
}
