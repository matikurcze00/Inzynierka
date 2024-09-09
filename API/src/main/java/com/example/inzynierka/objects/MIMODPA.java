package com.example.inzynierka.objects;

import com.example.inzynierka.controllers.AbstractController;
import com.example.inzynierka.models.ParObiektDPAMIMO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class MIMODPA extends MIMO {

    private List<List<DPA>> transmittance; //LIST<LIST-IN<OUT>>
    private MIMODPA disturbance;

    public MIMODPA() {
    }

    public MIMODPA(ParObiektDPAMIMO[] parObiektDPAMIMOS) {
        createTransmittance(parObiektDPAMIMOS);
        outputNumber = transmittance.get(0).size();
        entriesNumber = transmittance.size();
        countDelayMax();
        this.typeOfError = "srednio";
        setUMax(parObiektDPAMIMOS);
        this.Y = new ArrayList<>();
        for (int i = 0; i < this.outputNumber; i++) {
            Y.add(new ArrayList(Collections.nCopies(3, transmittance.get(0).get(i).getYpp())));
        }
        this.U = new ArrayList<>();
        for (int i = 0; i < this.entriesNumber; i++) {
            U.add(new ArrayList(Collections.nCopies(3 + delayMax, transmittance.get(i).get(0).getUpp())));
        }
        countLength();
        calculateYMax();
    }

    public MIMODPA(ParObiektDPAMIMO[] parObiektDPAMIMOS, String typeOfError, MIMODPA disturbance) {
        this(parObiektDPAMIMOS, typeOfError);
        setDisturbance(disturbance);
    }

    public MIMODPA(ParObiektDPAMIMO[] parObiektDPAMIMOS, String typeOfError) {
        createTransmittance(parObiektDPAMIMOS);
        outputNumber = transmittance.get(0).size();
        entriesNumber = transmittance.size();
        countDelayMax();
        this.typeOfError = typeOfError;
        setUMax(parObiektDPAMIMOS);

        this.Y = new ArrayList<>();
        for (int i = 0; i < this.outputNumber; i++) {
            Y.add(new ArrayList(Collections.nCopies(3, transmittance.get(0).get(i).getYpp())));
        }
        this.U = new ArrayList<>();
        for (int i = 0; i < this.entriesNumber; i++) {
            U.add(new ArrayList(Collections.nCopies(3 + delayMax, transmittance.get(i).get(0).getUpp())));
        }
        countLength();
        calculateYMax();
    }

    public double[] simulateStep(double[] du) {
        countU(du);
        double[] Yakt = new double[outputNumber];
        for (int i = 0; i < outputNumber; i++) {
            double YaktIN = 0.0;
            for (int j = 0; j < entriesNumber; j++) {
                YaktIN += transmittance.get(j).get(i).simulateStep(U.get(j));
            }
            Yakt[i] = YaktIN;
        }
        saveY(Yakt);
        return Yakt;
    }

    private void saveY(double[] Yakt) {
        for (int i = 0; i < outputNumber; i++) {
            saveY(i, Yakt[i]);
        }
    }

    public double[] simulateStep(double[] du, double[] dUz) {
        countU(du);
        double[] Yakt = disturbance.simulateStep(dUz);
        for (int i = 0; i < outputNumber; i++) {
            double YaktIN = 0.0;
            for (int j = 0; j < entriesNumber; j++) {
                YaktIN += transmittance.get(j).get(i).simulateStep(U.get(j));
            }
            Yakt[i] += YaktIN;
        }
        saveY(Yakt);
        return Yakt;
    }

    public double simulateStep(double du, int IN, int OUT) {
        countU(du, IN);
        double YaktIN = transmittance.get(IN).get(OUT).simulateStep(U.get(IN));
        saveY(OUT, YaktIN);

        return YaktIN;
    }

    private void saveY(int OUT, double YaktIN) {
        List<Double> Ytemp = Y.get(OUT);
        for (int j = Y.get(OUT).size() - 1; j > 0; j--) {
            Ytemp.set(j, Ytemp.get(j - 1));
        }
        Ytemp.set(0, YaktIN);
        Y.set(OUT, Ytemp);
    }

    public double simulateDisruptionStep(double du, int IN, int OUT) {

        return disturbance.simulateStep(du, IN, OUT);
    }

    public void createTransmittance(ParObiektDPAMIMO[] parObiektDPAMIMOS) {
        this.transmittance = new ArrayList<>();
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
            this.transmittance.add(transmitancjaTemp);
        }
    }

    private void setUMax(ParObiektDPAMIMO[] obiektyMIMO) {
        this.uMax = new double[obiektyMIMO.length];
        this.uMin = new double[obiektyMIMO.length];
        for (int i = 0; i < obiektyMIMO.length; i++) {
            this.uMax[i] = obiektyMIMO[i].getUMax();
            this.uMin[i] = obiektyMIMO[i].getUMin();
        }
    }

    private void countU(double[] du) {
        for (int j = 0; j < du.length; j++) {
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

    private void countU(double du, int IN) {
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

    public double getTp(int IN) {
        return transmittance.get(IN).get(0).getTp();
    }

    public double[] getOutput() {
        double[] YAkt = new double[outputNumber];
        for (int i = 0; i < outputNumber; i++) {
            YAkt[i] = Y.get(i).get(0);
        }
        return YAkt;
    }

    public double simulateObjectRegulation(AbstractController abstractController, double[] goal) {

        resetObject();
        double simulationError;
        if (disturbance != null) {
            simulationError = simulateObjectWithDisturbance(abstractController, goal);
        } else {
            simulationError = simulateObjectWithoutDisturbance(abstractController, goal);
        }
        simulationError = simulationError / this.simulationLength * outputNumber * outputNumber;
        resetObject();
        return simulationError;
    }

    private double simulateObjectWithoutDisturbance(AbstractController abstractController, double[] goal) {
        double[] tempGoal = new double[outputNumber];
        double simulationError = 0.0;

        for (int k = 0; k < outputNumber; k++) {
            for (int i = 0; i < outputNumber; i++) {
                tempGoal[i] = 0;
            }
            tempGoal[k] = goal[k];
            resetObject();
            abstractController.resetController();
            abstractController.setSetpoint(tempGoal);
            for (int i = 0; i < this.simulationLength; i++) {
                double[] Ytepm = simulateStep(abstractController.countControls(getOutput()));
                simulationError = calculateError(Ytepm, simulationError, tempGoal, i, 4);
            }
        }
        return simulationError;
    }

    private double simulateObjectWithDisturbance(AbstractController abstractController, double[] goal) {
        double[] tempGoal = new double[outputNumber];
        double simulationError = 0.0;
        for (int k = 0; k < outputNumber; k++) {
            for (int i = 0; i < outputNumber; i++) {
                tempGoal[i] = 0;
            }
            tempGoal[k] = goal[k];
            abstractController.setSetpoint(tempGoal);
            resetObject();
            abstractController.resetController();
            simulationError += simulateFirstHalf(abstractController, tempGoal);
            double[] UDisturbance = calculateDisturbanceVector();
            simulationError += simulateDisturbance(abstractController, tempGoal, UDisturbance);
            simulationError += simulatePartAfterDisturbance(abstractController, tempGoal, UDisturbance);
        }
        return simulationError;
    }

    private double[] calculateDisturbanceVector() {
        double[] UDisturbance = new double[disturbance.getTransmittance().size()];
        for (int i = 0; i < disturbance.getTransmittance().size(); i++) {
            UDisturbance[i] = disturbance.getUMax(i) / this.simulationLength;
        }
        return UDisturbance;
    }


    /*
    Simulating part without disturbance with goal
     */
    private double simulateFirstHalf(AbstractController abstractController, double[] tempGoal) {
        double simulationError = 0.0;
        double[] Ytepm = simulateStep(abstractController.countControls(getOutput()));
        int half = 4;

        for (int i = 0; i < Math.floorDiv(this.simulationLength, 2); i++) {
            simulationError = calculateError(Ytepm, simulationError, tempGoal, i, 4);
        }

        return simulationError;
    }

    private double calculateError(double[] Ytepm, double simulationError, double[] tempGoal, int i, int y) {
        for (int j = 0; j < Ytepm.length; j++) {
            if (this.typeOfError.equals("srednio")) {
                simulationError += Math.pow(Ytepm[j] - tempGoal[j], 2);
            } else if (this.typeOfError.equals("absolutny")) {
                simulationError += Math.abs(Ytepm[j] - tempGoal[j]);
            } else if (this.typeOfError.equals("mediana") && i == Math.floorDiv(this.simulationLength, y)) {
                for (int out = 0; out < outputNumber; out++) {
                    simulationError += Math.abs(Ytepm[j] - tempGoal[out]);
                }
            }
        }
        return simulationError;
    }

    /*
    Adding disturbance to the simulation
     */
    private double simulateDisturbance(AbstractController abstractController, double[] tempGoal, double[] UDisturbance) {
        double simulationError = 0.0;
        double[] Ytepm;
        int half = 16;

        for (int i = 0; i < Math.floorDiv(this.simulationLength, 8); i++) {
            Ytepm = simulateStep(abstractController.countControls(getOutput(), UDisturbance), UDisturbance);
            simulationError = calculateError(Ytepm, simulationError, tempGoal, i, half);
        }

        return simulationError;
    }

    /*
    Simulating part after disturbance to check if controller can stand it
     */
    private double simulatePartAfterDisturbance(AbstractController abstractController, double[] tempGoal, double[] UDisturbance) {
        double simulationError = 0.0;
        double[] Ytepm;
        int half = 48;
        for (int i = 0; i < disturbance.getTransmittance().size(); i++) {
            UDisturbance[i] = 0.0;
        }

        for (int i = 0; i < Math.floorDiv(this.simulationLength * 3, 8); i++) {
            Ytepm = simulateStep(abstractController.countControls(getOutput(), UDisturbance), UDisturbance);
            simulationError = calculateError(Ytepm, simulationError, tempGoal, i, half);
        }
        return simulationError;
    }

    public double getUMax(int IN) {
        return getUMax()[IN];
    }

    public double getYpp(int IN) {
        return 0.0;
    }

    public double countError(int dlugosc, List<double[]> wyjscie, double[] goal) {
        double error = 0.0;
        switch (this.typeOfError) {
            case "mediana":
                for (int i = 0; i < entriesNumber; i++) {
                    error += wyjscie.get(0)[Math.floorDiv(dlugosc, 2)] - goal[i];
                }
                break;
            case "srednio":
                for (int i = 0; i < dlugosc; i++) {
                    for (int j = 0; j < goal.length; j++) {
                        error += Math.pow(wyjscie.get(j)[i] - goal[j], 2);
                    }
                }
                break;
            case "absolutny":
                for (int i = 0; i < dlugosc; i++) {
                    for (int j = 0; j < goal.length; j++) {
                        error += Math.abs(wyjscie.get(j)[i] - goal[j]);
                    }
                }
                break;
            default:
                break;
        }
        return error;
    }

    private void calculateYMax() {
        double[] Ytemp;

        this.YMax = new double[outputNumber];
        for (int i = 0; i < outputNumber; i++) {
            this.YMax[i] = 0.0;
        }
        double[] uMaxTemp = new double[entriesNumber];
        System.arraycopy(this.uMax, 0, uMaxTemp, 0, entriesNumber);
        for (int i = 0; i < simulationLength * 2; i++) {
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

    public void resetObject() {
        for (int i = 0; i < this.outputNumber; i++) {
            Y.set(i, new ArrayList(Collections.nCopies(3, transmittance.get(0).get(i).getYpp())));
        }
        for (int i = 0; i < this.entriesNumber; i++) {
            U.set(i, new ArrayList(Collections.nCopies(3 + delayMax, transmittance.get(i).get(0).getUpp())));
        }
        for (List<DPA> transmittanceList : transmittance) {
            for (DPA transmittance : transmittanceList) {
                transmittance.reset();
            }
        }
        if (disturbance != null) {
            disturbance.resetObject();
        }
    }

    public void countDelayMax() {
        for (List<DPA> listaTransmitancji : transmittance) {
            for (DPA tran : listaTransmitancji) {
                if (tran.getDelay() > this.delayMax) {
                    this.delayMax = tran.getDelay();
                }
            }
        }
    }

    private void countLength() {
        int lengthTemp = 40;
        for (int i = 0; i < this.getOutputNumber(); i++) {
            for (int j = 0; j < this.getEntriesNumber(); j++) {
                this.resetObject();
                double Uskok = this.getUMax(j) / 2;
                double Utemp = 0;

                int k = 2;
                List<Double> simulation = new ArrayList<>();
                simulation.add((this.simulateStep(Uskok, j, i) - this.getYpp(i)) / Uskok);
                simulation.add((this.simulateStep(Utemp, j, i) - this.getYpp(i)) / Uskok);
                while (((Math.abs(simulation.get(k - 1) - simulation.get(k - 2)) >= 0.005) || simulation.get(k - 2) == 0.0)
                    && ((k <= 10) || (k > 10 && simulation.get(k - 2) != 0.0))) {
                    simulation.add((this.simulateStep(Utemp, j, i) - this.getYpp(i)) / Uskok);
                    k++;
                }
                if (lengthTemp < simulation.size()) {
                    lengthTemp = simulation.size();
                }
            }
        }
        this.simulationLength = lengthTemp;

    }

    public void setDisturbance(MIMODPA disturbance) {
        this.disturbance = disturbance;
        disturbance.resetObject();
    }
}
