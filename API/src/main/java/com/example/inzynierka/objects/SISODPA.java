package com.example.inzynierka.objects;

import com.example.inzynierka.controllers.AbstractController;
import com.example.inzynierka.models.DisturbanceDPA;
import com.example.inzynierka.models.ParObiektDPA;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class SISODPA extends SISO {

    private DPA transmittance;
    private List<List<Double>> Uz;

    private List<DPA> disturbance;

    public SISODPA() {
    }

    public SISODPA(ParObiektDPA parObiektDPA, double uMax, double uMin, String typeOfError, DisturbanceDPA disturbanceDPAMierzalne) {
        this(parObiektDPA.getGain(), parObiektDPA.getR1(), parObiektDPA.getQ1(), parObiektDPA.getR2(),
            parObiektDPA.getQ2(), parObiektDPA.getT1(), parObiektDPA.getT2(), parObiektDPA.getT3()
            , parObiektDPA.getDelay(), parObiektDPA.getTp(), uMax, uMin, typeOfError);
        this.disturbance = new ArrayList<>();
        if (disturbanceDPAMierzalne.getGain() != null) {
            for (int i = 0; i < disturbanceDPAMierzalne.getGain().length; i++) {
                this.disturbance.add(new DPA(disturbanceDPAMierzalne.getGain()[i], disturbanceDPAMierzalne.getR1()[i], disturbanceDPAMierzalne.getQ1()[i],
                    disturbanceDPAMierzalne.getR2()[i], disturbanceDPAMierzalne.getQ2()[i], disturbanceDPAMierzalne.getT1()[i],
                    disturbanceDPAMierzalne.getT2()[i],
                    disturbanceDPAMierzalne.getT3()[i], disturbanceDPAMierzalne.getDelay()[i], disturbanceDPAMierzalne.getTp()[i]));
            }
        }
        resetObject();
    }

    public SISODPA(ParObiektDPA parObiektDPA, double uMax, double uMin, String typeOfError) {
        this(parObiektDPA.getGain(), parObiektDPA.getR1(), parObiektDPA.getQ1(), parObiektDPA.getR2(),
            parObiektDPA.getQ2(), parObiektDPA.getT1(), parObiektDPA.getT2(), parObiektDPA.getT3()
            , parObiektDPA.getDelay(), parObiektDPA.getTp(), uMax, uMin, typeOfError);
    }

    public SISODPA(double gain, double R1, int Q1, double R2, int Q2, double T1,
                   double T2, double T3, int delay, double Tp, double uMax, double uMin, String typeOfError) {
        this.transmittance = new DPA(gain, R1, Q1, R2, Q2, T1, T2, T3, delay, Tp);
        U = new ArrayList(Collections.nCopies(3 + delay, transmittance.getUpp()));
        Y = new ArrayList(Collections.nCopies(3, transmittance.getYpp()));
        this.uMax = uMax;
        this.uMin = uMin;
        this.typeOfError = typeOfError;
        calculateLength();
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
        setU(new ArrayList(Collections.nCopies(U.size(), transmittance.getUpp())));
        setY(new ArrayList(Collections.nCopies(Y.size(), transmittance.getYpp())));
        transmittance.reset();
        if (disturbance != null && !disturbance.isEmpty()) {
            this.Uz = new ArrayList<>();
            for (DPA zaklocenie : disturbance) {
                Uz.add(new ArrayList(Collections.nCopies(3 + zaklocenie.getDelay(), 0.0)));
                zaklocenie.reset();
            }
        }
    }

    public double simulateObjectRegulation(AbstractController abstractController, double[] setpoint) {
        resetObject();
        abstractController.setSetpoint(setpoint);
        double[] YSymulacji;
        if (disturbance != null && !disturbance.isEmpty()) {
            YSymulacji = simulateObjectWithDisturbance(abstractController);
        } else {
            YSymulacji = simulateObjectWithoutDisturbance(abstractController);
        }
        resetObject();
        return calculateError(YSymulacji, setpoint[0]);
    }

    private double[] simulateObjectWithoutDisturbance(AbstractController abstractController) {
        double[] YSimulated = new double[length];
        for (int i = 0; i < this.length; i++) {
            YSimulated[i] = simulateStep(abstractController.countControls(getOutput()));
        }
        return YSimulated;
    }

    public double[] simulateObjectWithDisturbance(AbstractController abstractController) {
        double[] YSimulated = new double[length];
        for (int i = 0; i < Math.floorDiv(this.length, 2); i++) {
            YSimulated[i] = simulateStep(abstractController.countControls(getOutput()));
        }
        double[] UDisturbance = new double[disturbance.size()];
        for (int i = 0; i < UDisturbance.length; i++) {
            UDisturbance[i] = 3 * transmittance.getGain() / disturbance.get(i).getGain();
        }
        for (int i = Math.floorDiv(this.length, 2); i < Math.floorDiv(this.length * 3, 4); i++) {
            YSimulated[i] = simulateStep(abstractController.countControls(getOutput(), UDisturbance), UDisturbance);
        }
        for (int i = 0; i < UDisturbance.length; i++) {
            UDisturbance[i] = 0.0;
        }
        for (int i = Math.floorDiv(this.length * 3, 4); i < length; i++) {
            YSimulated[i] = simulateStep(abstractController.countControls(getOutput(), UDisturbance), UDisturbance);
        }

        return YSimulated;
    }

    public double simulateStep(double du) {
        calculateU(du);
        double Yakt;
        Yakt = transmittance.simulateStep(U);

        saveY(Yakt);
        return Yakt;
    }

    private void saveY(double Yakt) {
        for (int i = Y.size() - 1; i > 0; i--) {
            Y.set(i, Y.get(i - 1));
        }
        Y.set(0, Yakt);
    }

    public double simulateStep(double du, double[] duZ) {
        calculateU(du);
        double Yakt;
        Yakt = transmittance.simulateStep(U);
        for (int i = 0; i < duZ.length; i++) {
            Yakt += simulateDisruptionStep(duZ[i], i);
        }
        saveY(Yakt);
        return Yakt;
    }

    public double simulateDisruptionStep(double du, int disturbanceNO) {
        double Yakt;
        calculateUZ(du, disturbanceNO);
        Yakt = this.disturbance.get(disturbanceNO).simulateStep(Uz.get(disturbanceNO));

        saveY(Yakt);
        return Yakt;
    }

    private void calculateUZ(double du, int disturbanceNO) {
        double Uakt = Uz.get(disturbanceNO).get(0) + du;
        if (Uakt > uMax) {
            Uakt = uMax;
        } else if (Uakt < uMin) {
            Uakt = uMin;
        }
        for (int i = Uz.get(disturbanceNO).size() - 1; i > 0; i--) {
            Uz.get(disturbanceNO).set(i, Uz.get(disturbanceNO).get(i - 1));
        }
        Uz.get(disturbanceNO).set(0, Uakt);
    }

    private void calculateU(double du) {
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

    @Override
    public double getYpp() {
        return transmittance.getYpp();
    }


    private void calculateYMax() {
        double Ytemp;
        for (int i = 0; i < this.length * 2; i++) {
            simulateStep(getUMax());
        }
        Ytemp = getOutput();
        resetObject();
        this.YMax = Ytemp;
    }

    private void calculateLength() {
        resetObject();
        double USkok = getUMax() / 2;
        int i = 2;
        List<Double> Stemp = new ArrayList<>();
        double Utemp = 0;
        Stemp.add((simulateStep(USkok) - getYpp()) / USkok);
        Stemp.add((simulateStep(Utemp) - getYpp()) / USkok);
        while ((Math.abs(Stemp.get(i - 1) - Stemp.get(i - 2)) >= 0.001) || Stemp.get(i - 2) == 0.0) {
            Stemp.add((simulateStep(Utemp) - getYpp()) / USkok);
            i++;
        }
        this.length = Stemp.size();
        if (this.length < 40) {
            this.length = 40;
        }
    }

    private double calculateError(double[] Y, double yZad) {
        double errorTemp = 0.0;
        if (this.typeOfError.equals("srednio")) {
            for (int i = 0; i < this.length; i++) {
                errorTemp += Math.pow(Y[i] - yZad, 2);
            }
        } else if (this.typeOfError.equals("absolutny")) {
            for (int i = 0; i < this.length; i++) {
                errorTemp += Math.abs(Y[i] - yZad);
            }
        }
        return errorTemp / this.length;
    }
}
