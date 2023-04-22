package com.example.inzynierka.obiekty;

import com.example.inzynierka.modele.ParObiektMIMO;
import com.example.inzynierka.regulatory.Regulator;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class MIMOTransmitancjaCiagla {

    private List<List<TransmitancjaCiagla>> transmitancja; //LIST<LIST-IN<OUT>>
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
    private MIMOTransmitancjaCiagla zakloceniaMierzalne;

    public MIMOTransmitancjaCiagla() {
    }

    public MIMOTransmitancjaCiagla(ParObiektMIMO[] parObiektMIMOS)
    {
        stworzTransmitancje(parObiektMIMOS);
        liczbaOUT = transmitancja.get(0).size();
        liczbaIN = transmitancja.size();
        obliczDelayMax();
        this.blad = "srednio";
        obliczUMax(parObiektMIMOS);
        this.Y = new ArrayList();
        for (int i = 0; i < this.liczbaOUT; i++) {
            Y.add(new ArrayList(Collections.nCopies(3, transmitancja.get(0).get(i).getYpp())));
        }
        this.U = new ArrayList();
        for (int i = 0; i < this.liczbaIN; i++) {
            U.add(new ArrayList(Collections.nCopies(3 + delayMax, transmitancja.get(i).get(0).getUpp())));
        }
        obliczDlugosc();
        obliczYMax();
    }

    public MIMOTransmitancjaCiagla(ParObiektMIMO[] parObiektMIMOS, String blad) {
        stworzTransmitancje(parObiektMIMOS);
        liczbaOUT = transmitancja.get(0).size();
        liczbaIN = transmitancja.size();
        obliczDelayMax();
        this.blad = blad;
        obliczUMax(parObiektMIMOS);

        this.Y = new ArrayList();
        for (int i = 0; i < this.liczbaOUT; i++) {
            Y.add(new ArrayList(Collections.nCopies(3, transmitancja.get(0).get(i).getYpp())));
        }
        this.U = new ArrayList();
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

    public void stworzTransmitancje(ParObiektMIMO[] parObiektMIMOS) {
        this.transmitancja = new ArrayList();
        for (ParObiektMIMO parObiekt : parObiektMIMOS) {
            List<TransmitancjaCiagla> transmitancjaTemp = new ArrayList();
            for (int i = 0; i < parObiekt.getGain().length; i++) {

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

    private void obliczUMax(ParObiektMIMO[] obiektyMIMO) {
        this.uMax = new double[obiektyMIMO.length];
        this.uMin = new double[obiektyMIMO.length];
        for (int i = 0; i < obiektyMIMO.length; i++) {
            this.uMax[i] = obiektyMIMO[i].getUMax();
            this.uMin[i] = obiektyMIMO[i].getUMin();
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
        double blad = 0.0;
        if(zakloceniaMierzalne !=null) {
            blad = obliczPraceZZakloceniem(regulator, cel, blad);
        } else {
            blad = obliczPraceBezZaklocen(regulator, cel, blad);
        }
        blad = blad / this.dlugosc * liczbaOUT * liczbaOUT;
        resetObiektu();
        return blad;
    }

    private double obliczPraceBezZaklocen(Regulator regulator, double[] cel, double blad) {
        double[] tempCel = new double[liczbaOUT];
        for (int k = 0; k < liczbaOUT; k++) {
            for (int i = 0; i < liczbaOUT; i++)
                tempCel[i] = 0;
            tempCel[k] = cel[k];
            resetObiektu();
            regulator.resetujRegulator();
            regulator.setCel(tempCel);
            for (int i = 0; i < this.dlugosc; i++) {
                double[] Ytepm = obliczKrok(regulator.policzOutput(getAktualne()));
                for (int j = 0; j < liczbaOUT; j++) {
                    if (this.blad.equals("srednio"))
                        blad += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        blad += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
        }
        return blad;
    }

    private double obliczPraceZZakloceniem(Regulator regulator, double[] cel, double blad) {
        double[] tempCel = new double[liczbaOUT];
        for (int k = 0; k < liczbaOUT; k++) {
            for (int i = 0; i < liczbaOUT; i++)
                tempCel[i] = 0;
            tempCel[k] = cel[k];
            regulator.setCel(tempCel);
            resetObiektu();
            regulator.resetujRegulator();
            for (int i = 0; i < Math.floorDiv(this.dlugosc,2); i++) {
                double[] Ytepm = obliczKrok(regulator.policzOutput(getAktualne()));
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio"))
                        blad += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        blad += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
            double[] zakloceniaU = new double[zakloceniaMierzalne.getTransmitancja().size()];
            for(int i = 0; i < zakloceniaMierzalne.getTransmitancja().size(); i++)
                zakloceniaU[i] = zakloceniaMierzalne.getUMax(i)/this.dlugosc;

            for (int i = 0; i < Math.floorDiv(this.dlugosc,8); i++) {
                double[] Ytepm = obliczKrok(regulator.policzOutput(getAktualne(), zakloceniaU),zakloceniaU);
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio"))
                        blad += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        blad += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
            for(int i = 0; i < zakloceniaMierzalne.getTransmitancja().size(); i++)
                zakloceniaU[i] = 0.0;

            for (int i = 0; i < Math.floorDiv(this.dlugosc*3,8); i++) {
                double[] Ytepm = obliczKrok(regulator.policzOutput(getAktualne(), zakloceniaU),zakloceniaU);
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.blad.equals("srednio"))
                        blad += Math.pow(Ytepm[j] - tempCel[j], 2);
                    else if (this.blad.equals("absolutny"))
                        blad += Math.abs(Ytepm[j] - tempCel[j]);
                }
            }
        }
        return blad;
    }

    public void SetU(List<List<Double>> noweU) {
        this.U = noweU;
    }

    public void SetY(List<List<Double>> noweY) {
        this.Y = noweY;
    }

    public double[] getYMax() {
        return YMax;
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
        double[] uMax = new double[liczbaIN];
        for (int i = 0; i < liczbaIN; i++) {
            uMax[i] = this.uMax[i];
        }
        for (int i = 0; i < dlugosc * 2; i++) {
            obliczKrok(uMax);
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
        for (List<TransmitancjaCiagla> ListaTransmitancji : transmitancja)
            for (TransmitancjaCiagla tran : ListaTransmitancji)
                tran.reset();
        if(zakloceniaMierzalne !=null) {
            zakloceniaMierzalne.resetObiektu();
        }
    }

    public void obliczDelayMax() {
        for (List<TransmitancjaCiagla> listaTransmitancji : transmitancja) {
            for (TransmitancjaCiagla tran : listaTransmitancji) {
                if (tran.getDelay() > this.delayMax) {
                    this.delayMax = tran.getDelay();
                }
            }
        }
    }

    private void obliczDlugosc() {
        List<List<Double>> dlugosc = new ArrayList();
        for (int i = 0; i < this.getLiczbaOUT(); i++) {
            for (int j = 0; j < this.getLiczbaIN(); j++) {
                this.resetObiektu();
                double U = this.getUMax(j) / 2;
                double Utemp = 0;

                int k = 2;
                List<Double> dlugoscTemp = new ArrayList<Double>();
                dlugoscTemp.add((this.obliczKrok(U, j, i) - this.getYpp(i)) / U);
                dlugoscTemp.add((this.obliczKrok(Utemp, j, i) - this.getYpp(i)) / U);
                while ((!(Math.abs(dlugoscTemp.get(k - 1) - dlugoscTemp.get(k - 2)) < 0.005) || dlugoscTemp.get(k - 2) == 0.0)
                    &&((k<=10) || (k>10 && dlugoscTemp.get(k - 2) != 0.0))) {
                    dlugoscTemp.add((this.obliczKrok(Utemp, j, i) - this.getYpp(i)) / U);
                    k++;
                }
                dlugosc.add(dlugoscTemp);
            }
        }
        int dlugoscInt = dlugosc.get(0).size();
        for (int i = 0; i < dlugosc.size(); i++) {
            if (dlugoscInt < dlugosc.get(i).size()) {
                dlugoscInt = dlugosc.get(i).size();
            }
        }
        this.dlugosc = dlugoscInt;
        if (this.dlugosc < 40)
            this.dlugosc = 40;
    }

    public void setZakloceniaMierzalne(MIMOTransmitancjaCiagla zaklocenieMierzalne) {
        this.zakloceniaMierzalne = zaklocenieMierzalne;
        zaklocenieMierzalne.resetObiektu();
    }
}
