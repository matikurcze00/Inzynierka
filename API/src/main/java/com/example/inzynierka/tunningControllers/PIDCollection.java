package com.example.inzynierka.tunningControllers;

import com.example.inzynierka.objects.MIMODPA;

import java.util.ArrayList;
import java.util.List;

public class PIDCollection extends AbstractController {

    private final List<PIDController> PIDy;
    private final Integer[] PV;
    private final double[] uMax;

    public PIDCollection(MIMODPA object, Integer[] PV, double duMax, Double[] presetControls) {
        PIDy = new ArrayList<>(PV.length);
        uMax = object.getUMax();
        for (int i = 0; i < PV.length; i++) {
            double[] yMaxTemp = new double[] {object.getYMax()[i] / 2};
            PIDy.add(new PIDController((presetControls[i * 3] == null) ? 1.0 : presetControls[i * 3],
                (presetControls[i * 3 + 1] == null) ? 1.0 : presetControls[i * 3 + 1],
                (presetControls[i * 3 + 2] == null) ? 1.0 : presetControls[i * 3 + 2],
                object.getTp(PV[i]), yMaxTemp, duMax, uMax[PV[i]]));
        }
        this.PV = PV;
        int tempNumber = 0;
        this.presetControls = presetControls;
        for (Double wartosc : presetControls) {
            if (wartosc != null) {
                tempNumber += 1;
            }
        }
        this.presetControlsNumbers = tempNumber;
    }

    @Override
    public double countControls(double previousOutput) {
        return 0;
    }

    @Override
    public double countControls(double previousOutput, double[] disturbanceTuning) {
        return 0;
    }

    @Override
    public double[] countControls(double[] previousOutput) {
        double[] output = new double[PV.length];
        for (int i = 0; i < PV.length; i++) {
            output[PV[i]] = PIDy.get(i).countControls(previousOutput[i]);
        }
        return output;
    }

    @Override
    public double[] countControls(double[] previousOutput, double[] disturbanceTuning) {
        return countControls(previousOutput);
    }

    @Override
    public void setSetpoint(double[] setpoint) {
        this.setpoint = setpoint;
        for (int i = 0; i < PIDy.size(); i++) {
            PIDy.get(i).setSetpoint(new double[] {setpoint[i]});
        }
    }

    @Override
    public void resetController() {
        for (PIDController pidController : PIDy) {
            pidController.resetController();
        }
    }

    @Override
    public void changeTuning(double[] parameters) {
        int iTemp = 0;
        for (int i = 0; i < PV.length; i++) {
            double[] parametersTemp = new double[3];
            if (presetControlsNumbers == 0) {
                parametersTemp = new double[] {parameters[i * 3], parameters[i * 3 + 1], parameters[i * 3 + 2]};
            } else {
                for (int j = 0; j < 3; j++) {
                    if (presetControls[i * 3 + j] == null) {
                        parametersTemp[j] = parameters[iTemp];
                        iTemp += 1;
                    } else {
                        parametersTemp[j] = presetControls[i * 3 + j];
                    }
                }

            }
            PIDy.get(i).changeTuning(parametersTemp);
        }
    }

    @Override
    public int getNumberOfTuningParameters() {
        return 3 * PV.length - presetControlsNumbers;
    }
}
