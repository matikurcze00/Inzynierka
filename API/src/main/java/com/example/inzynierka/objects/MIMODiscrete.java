package com.example.inzynierka.objects;

import com.example.inzynierka.models.ParObiektRownaniaMIMO;
import com.example.inzynierka.models.DisturbanceDiscrete;
import com.example.inzynierka.tunningControllers.AbstractController;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class MIMODiscrete extends MIMO {
    List<List<Double[]>> A;
    List<List<Double[]>> B;
    List<List<Double[]>> Bz;
    List<List<Double>> Uz;

    private double[] Ypp;
    private double[] Upp;
    private int numberOfDisturbance;
    private int[] delay;

    public MIMODiscrete(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS, String typeOfError, DisturbanceDiscrete[] disturbanceDiscrete) {
        this(parObiektRownaniaMIMOS);
        this.typeOfError = typeOfError;
        this.Bz = new ArrayList<>();
        for (DisturbanceDiscrete disturbance : disturbanceDiscrete) {
            List<Double[]> BzTemp = new ArrayList<>();
            BzTemp.add(disturbance.getB1());
            BzTemp.add(disturbance.getB2());
            BzTemp.add(disturbance.getB3());
            BzTemp.add(disturbance.getB4());
            BzTemp.add(disturbance.getB5());
            Bz.add(BzTemp);
        }
        this.numberOfDisturbance = Bz.get(0).get(0).length;
        this.Uz = new ArrayList<>();
        for (int i = 0; i < numberOfDisturbance; i++) {
            Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public MIMODiscrete(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS, String typeOfError) {
        this(parObiektRownaniaMIMOS);
        this.typeOfError = typeOfError;
    }

    public MIMODiscrete(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS) {
        this.A = new ArrayList<>();
        this.B = new ArrayList<>();
        this.U = new ArrayList<>();
        this.Y = new ArrayList<>();
        this.uMin = new double[parObiektRownaniaMIMOS.length];
        this.uMax = new double[parObiektRownaniaMIMOS.length];
        List<Double> uMinTemp = new ArrayList<>();
        List<Double> uMaxTemp = new ArrayList<>();
        this.length = 50;
        setABMIMO(parObiektRownaniaMIMOS, uMinTemp, uMaxTemp);
        setLimitations(uMinTemp, uMaxTemp);
        outputNumber = parObiektRownaniaMIMOS.length;
        entriesNumber = parObiektRownaniaMIMOS[0].getB1().length;
        initializationUYMIMO();
        calculateYMax();
    }

    private void initializationUYMIMO() {
        this.Y = new ArrayList();
        for (int i = 0; i < this.outputNumber; i++) {
            Y.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
        this.U = new ArrayList();
        for (int i = 0; i < this.entriesNumber; i++) {
            U.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    private void setLimitations(List uMinTemp, List uMaxTemp) {
        for (int i = 0; i < uMinTemp.size(); i++) {
            uMin[i] = (double) uMinTemp.get(i);
            uMax[i] = (double) uMaxTemp.get(i);
        }
    }

    private void setABMIMO(ParObiektRownaniaMIMO[] parObiektRownaniaMIMOS, List uMinTemp, List uMaxTemp) {
        for (ParObiektRownaniaMIMO parObiekt : parObiektRownaniaMIMOS) {
            List Atemp = new ArrayList<>();
            Atemp.add(parObiekt.getA1());
            Atemp.add(parObiekt.getA2());
            Atemp.add(parObiekt.getA3());
            Atemp.add(parObiekt.getA4());
            Atemp.add(parObiekt.getA5());
            this.A.add(Atemp);
            List Btemp = new ArrayList<>();
            Btemp.add(parObiekt.getB1());
            Btemp.add(parObiekt.getB2());
            Btemp.add(parObiekt.getB3());
            Btemp.add(parObiekt.getB4());
            Btemp.add(parObiekt.getB5());
            this.B.add(Btemp);
            uMinTemp.add(parObiekt.getUMin());
            uMaxTemp.add(parObiekt.getUMax());
        }
    }

    public double simulateObjectRegulation(AbstractController abstractController, double[] cel) {

        resetObject();
        double wartoscBlad = 0.0;

        wartoscBlad = simulateObjectWithoutDisturbance(abstractController, cel, wartoscBlad);

        wartoscBlad = wartoscBlad / this.length * outputNumber * outputNumber;
        resetObject();
        return wartoscBlad;
    }

    private double simulateObjectWithoutDisturbance(AbstractController abstractController, double[] cel, double blad) {
        double[] tempCel = new double[outputNumber];
        double[] Ymed = new double[outputNumber];
        for (int k = 0; k < outputNumber; k++) {
            for (int i = 0; i < outputNumber; i++) {
                tempCel[i] = 0;
            }
            tempCel[k] = cel[k];
            abstractController.setSetpoint(tempCel);
            resetObject();
            abstractController.resetController();
            for (int i = 0; i < this.length; i++) {
                double[] Ytepm = simulateStep(abstractController.countControls(getOutput()));
                for (int j = 0; j < Ytepm.length; j++) {
                    if (this.typeOfError.equals("srednio")) {
                        blad += Math.pow(Ytepm[j] - tempCel[j], 2);
                    } else if (this.typeOfError.equals("absolutny")) {
                        blad += Math.abs(Ytepm[j] - tempCel[j]);
                    } else if (this.typeOfError.equals("mediana")) {
                        if (i == Math.floorDiv(this.length, 16)) {
                            Ymed = Ytepm;
                        }
                    }

                }
            }
            if(this.typeOfError.equals("mediana")) {
                for (int j = 0; j < outputNumber; j++) {
                    blad += Math.abs(Ymed[j] - tempCel[j]);
                }
            }
        }
        return blad;
    }

    public double[] simulateStep(double[] du) {
        calculateU(du);
        double[] Yakt = new double[outputNumber];

        for (int i = 0; i < outputNumber; i++) {
            Yakt[i] = calculateOutput(i);
        }
        saveY(Yakt);
        return Yakt;
    }

    public double[] simulateStep(double[] du, double[] duZ) {
        calculateUz(duZ);
        return simulateStep(du);
    }

    public double simulateDisruptionStep(double du, int IN, int OUT) {
        return 0.0;
    }

    public double simulateStep(double du, int IN, int OUT) {
        calculateU(du, IN);
        double YaktIN = calculateOutput(OUT);
        saveY(OUT, YaktIN);
        return YaktIN;
    }

    public double[] getOutput() {
        double[] YAkt = new double[outputNumber];
        for (int i = 0; i < outputNumber; i++) {
            YAkt[i] = Y.get(i).get(0);
        }
        return YAkt;
    }

    private double calculateOutput(int out) {
        Double Yakt = 0.0;
        for (int j = 0; j < B.get(out).size(); j++) {
            for (int k = 0; k < entriesNumber; k++) {
                Yakt += B.get(out).get(j)[k] * U.get(k).get(j);
            }
        }

        for (int i = 0; i < numberOfDisturbance; i++) {
            for (int j = 0; j < Bz.size(); j++) {
                Yakt += Bz.get(out).get(j)[i] * Uz.get(i).get(j);
            }
        }

        for (int j = 0; j < A.get(out).size(); j++) {
            for (int k = 0; k < outputNumber; k++) {
                Yakt -= A.get(out).get(j)[k] * Y.get(k).get(j);
            }
        }
        return Yakt;
    }

    public void resetObject() {
        for (int i = 0; i < this.outputNumber; i++) {
            Y.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.entriesNumber; i++) {
            U.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
        for (int i = 0; i < this.numberOfDisturbance; i++) {
            Uz.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public void calculateU(double[] du) {
        for (int j = 0; j < entriesNumber; j++) {
            double Uakt = U.get(j).get(0) + du[j];
            if (Uakt > uMax[j]) {
                Uakt = uMax[j];
            } else if (Uakt < uMin[j]) {
                Uakt = uMin[j];
            }

            for (int i = U.get(j).size() - 1; i > 0; i--) {
                U.get(j).set(i, U.get(j).get(i - 1));
            }
            U.get(j).set(0, Uakt);
        }
    }

    public void calculateU(double du, int IN) {
        double Uakt = U.get(IN).get(0) + du;
        if (Uakt > uMax[IN]) {
            Uakt = uMax[IN];
        } else if (Uakt < uMin[IN]) {
            Uakt = uMin[IN];
        }
        for (int i = U.get(IN).size() - 1; i > 0; i--) {
            U.get(IN).set(i, U.get(IN).get(i - 1));
        }
        U.get(IN).set(0, Uakt);
    }

    public void calculateUz(double[] du) {
        for (int j = 0; j < numberOfDisturbance; j++) {
            double Uakt = Uz.get(j).get(0) + du[j];
            for (int i = Uz.get(j).size() - 1; i > 0; i--) {
                Uz.get(j).set(i, Uz.get(j).get(i - 1));
            }
            Uz.get(j).set(0, Uakt);
        }
    }

    private void saveY(double[] Yakt) {
        for (int i = 0; i < outputNumber; i++) {
            List<Double> Ytemp = Y.get(i);
            for (int j = Y.get(i).size() - 1; j > 0; j--) {
                Ytemp.set(j, Ytemp.get(j - 1));
            }
            Ytemp.set(0, Yakt[i]);
            Y.set(i, Ytemp);
        }
    }

    private void saveY(int OUT, double YaktIN) {
        List<Double> Ytemp = Y.get(OUT);
        for (int j = Y.get(OUT).size() - 1; j > 0; j--) {
            Ytemp.set(j, Ytemp.get(j - 1));
        }
        Ytemp.set(0, YaktIN);
        Y.set(OUT, Ytemp);
    }

    private void calculateYMax() {
        double[] Ytemp;

        this.YMax = new double[outputNumber];
        for (int i = 0; i < outputNumber; i++) {
            this.YMax[i] = 0.0;
        }
        double[] uMaxTemp = new double[entriesNumber];
        System.arraycopy(this.uMax, 0, uMaxTemp, 0, entriesNumber);
        for (int i = 0; i < length * 2; i++) {
            simulateStep(uMaxTemp);
        }
        Ytemp = getOutput();
        resetObject();
        for (int i = 0; i < this.YMax.length; i++) {
            if (this.YMax[i] < Ytemp[i]) {
                this.YMax[i] = Ytemp[i];
            }
        }
    }
}
