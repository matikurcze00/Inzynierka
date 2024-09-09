package com.example.inzynierka.objects;


import com.example.inzynierka.controllers.AbstractController;
import com.example.inzynierka.models.DisturbanceDiscrete;
import com.example.inzynierka.models.ParObiektRownania;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISODiscrete extends SISO {
    private List<Double> A;
    private List<Double> B;
    private List<Double[]> Bz;
    private List<List<Double>> Uz;
    private int numberOfDisturbance;
    private double Upp;


    public SISODiscrete(ParObiektRownania parObiektRownania, double uMax, double uMin, String typeOfError, DisturbanceDiscrete disturbanceDiscrete) {
        this(parObiektRownania, uMax, uMin, typeOfError);
        List<Double[]> BzTemp = new ArrayList<>();
        BzTemp.add(disturbanceDiscrete.getB1());
        BzTemp.add(disturbanceDiscrete.getB2());
        BzTemp.add(disturbanceDiscrete.getB3());
        BzTemp.add(disturbanceDiscrete.getB4());
        BzTemp.add(disturbanceDiscrete.getB5());
        this.Bz = BzTemp;
        numberOfDisturbance = disturbanceDiscrete.getB1().length;
        this.Uz = new ArrayList<>();
        for (int i = 0; i < numberOfDisturbance; i++) {
            Uz.add(new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public SISODiscrete(ParObiektRownania parObiektRownania, double uMax, double uMin, String typeOfError) {
        this(parObiektRownania.getA1(), parObiektRownania.getA2(), parObiektRownania.getA3(), parObiektRownania.getA4(), parObiektRownania.getA5(),
            parObiektRownania.getB1(), parObiektRownania.getB2(), parObiektRownania.getB3(), parObiektRownania.getB4(), parObiektRownania.getB5(),
            uMax, uMin, typeOfError);
    }

    public SISODiscrete(double A1, double A2, double A3, double A4, double A5,
                        double B1, double B2, double B3, double B4, double B5,
                        double uMax, double uMin, String typeOfError) {
        this.A = new ArrayList<>();
        this.A.add(A1);
        this.A.add(A2);
        this.A.add(A3);
        this.A.add(A4);
        this.A.add(A5);
        this.B = new ArrayList<>();
        this.B.add(B1);
        this.B.add(B2);
        this.B.add(B3);
        this.B.add(B4);
        this.B.add(B5);
        this.uMax = uMax;
        this.uMin = uMin;
        this.typeOfError = typeOfError;
        this.Y = new ArrayList<>(Collections.nCopies(5, 0.0));
        this.U = new ArrayList<>(Collections.nCopies(5, 0.0));
        this.length = 50;
        calculateYMax();

    }

    public Double getOutput() {
        try {
            return Y.get(0);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public void resetObject() {
        setU(new ArrayList<>(Collections.nCopies(U.size(), Upp)));
        setY(new ArrayList<>(Collections.nCopies(Y.size(), Ypp)));
        for (int i = 0; i < this.numberOfDisturbance; i++) {
            Uz.set(i, new ArrayList(Collections.nCopies(5, 0.0)));
        }
    }

    public double simulateObjectRegulation(AbstractController abstractController, double setpoint) {
        resetObject();
        abstractController.setSetpoint(new double[] {setpoint});

        double[] YSimulated = simulateObjectWithoutDisturbance(abstractController);

        resetObject();
        return calculateError(YSimulated, setpoint);
    }

    public double simulateObjectRegulation(AbstractController abstractController, double[] setpoint) {
        resetObject();
        abstractController.setSetpoint(setpoint);

        double[] YSimulated = simulateObjectWithoutDisturbance(abstractController);

        resetObject();
        return calculateError(YSimulated, setpoint[0]);
    }

    private double[] simulateObjectWithoutDisturbance(AbstractController abstractController) {
        double[] YSimulated = new double[length];
        for (int i = 0; i < this.length; i++) {
            YSimulated[i] = simulateStep(abstractController.countControls(getOutput()));
        }
        return YSimulated;
    }

    public double simulateStep(double du) {
        calculateU(du);
        double Yakt = countOutput();

        saveY(Yakt);
        return Yakt;
    }

    public double simulateStep(double du, double[] duZ) {
        countUz(duZ);
        return simulateStep(du);
    }

    public double simulateDisruptionStep(double du, int disturbanceNo) {
        countUz(du, disturbanceNo);
        double Yakt = countDisturbanceOutput(disturbanceNo);
        saveY(Yakt);
        return Yakt;
    }

    public double countDisturbanceOutput(int disturbanceNo) {
        double Yakt = 0.0;
        for (int j = 0; j < Bz.size(); j++) {
            Yakt += Bz.get(j)[disturbanceNo] * Uz.get(disturbanceNo).get(j);
        }
        for (int i = 0; i < A.size(); i++) {
            Yakt -= A.get(i) * Y.get(i);
        }
        return Yakt;
    }

    public void countUz(double[] du) {
        for (int j = 0; j < numberOfDisturbance; j++) {
            double Uakt = Uz.get(j).get(0) + du[j];
            for (int i = Uz.get(j).size() - 1; i > 0; i--) {
                Uz.get(j).set(i, Uz.get(j).get(i - 1));
            }
            Uz.get(j).set(0, Uakt);
        }
    }

    public void countUz(double du, int disturbanceNo) {
        double Uakt = Uz.get(disturbanceNo).get(0) + du;
        if (Uakt > uMax) {
            Uakt = uMax;
        } else if (Uakt < uMin) {
            Uakt = uMin;
        }

        for (int i = Uz.get(disturbanceNo).size() - 1; i > 0; i--) {
            Uz.get(disturbanceNo).set(i, Uz.get(disturbanceNo).get(i - 1));
        }
        Uz.get(disturbanceNo).set(0, Uakt);
    }

    private double countOutput() {
        double Yakt = 0.0;
        for (int i = 0; i < B.size(); i++) {
            Yakt += B.get(i) * U.get(i);
        }
        for (int i = 0; i < numberOfDisturbance; i++) {
            for (int j = 0; j < Bz.size(); j++) {
                Yakt += Bz.get(j)[i] * Uz.get(i).get(j);
            }
        }
        for (int i = 0; i < A.size(); i++) {
            Yakt -= A.get(i) * Y.get(i);
        }
        return Yakt;
    }

    public void calculateU(double du) {
        double Uakt = U.get(0) + du;
        if (Uakt > uMax) {
            Uakt = uMax;
        } else if (Uakt < uMin) {
            Uakt = uMin;
        }
        for (int i = U.size() - 1; i > 0; i--) {
            U.set(i, U.get(i - 1));
        }
        U.set(0, Uakt);
    }

    private void saveY(double Yakt) {
        for (int i = Y.size() - 1; i > 0; i--) {
            Y.set(i, Y.get(i - 1));
        }
        Y.set(0, Yakt);
    }

    public double calculateError(double[] YSymulacji, double yZad) {
        double errorTemp = 0.0;
        if (this.typeOfError.equals("srednio")) {
            for (int i = 0; i < this.length; i++) {
                errorTemp += Math.pow(YSymulacji[i] - yZad, 2);
            }
        } else if (this.typeOfError.equals("absolutny")) {
            for (int i = 0; i < this.length; i++) {
                errorTemp += Math.abs(YSymulacji[i] - yZad);
            }
        }
        return errorTemp / this.length;
    }

    private void calculateYMax() {
        double yMax = 0;
        double yTemp = 0;
        for (int i = 0; i < B.size(); i++) {
            yTemp += B.get(i) * this.uMax;
        }
        yMax += yTemp;
        for (int i = 0; i < A.size(); i++) {
            yMax -= A.get(i) * yTemp;
        }
        this.YMax = yMax;
    }
}
