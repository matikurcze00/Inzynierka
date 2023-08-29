package com.example.inzynierka.services;

import com.example.inzynierka.EA.EvolutionaryAlgorithm;
import com.example.inzynierka.models.*;
import com.example.inzynierka.objects.*;
import com.example.inzynierka.tunningControllers.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Service
public class TuningService {

    private static void setDisturbanceTuningValueEntry(WizualizacjaZaklocen wizualizacjaZaklocen, double[][] dUZTemp, double[] uEntry, int i, int j) {
        if (j > 0) {
            if (uEntry[i] != 0.0) {
                if (uEntry[i] - wizualizacjaZaklocen.getDeltaU()[i] < 0) {
                    dUZTemp[j][i] = uEntry[i];
                    uEntry[i] = 0.0;
                } else {
                    dUZTemp[j][i] = wizualizacjaZaklocen.getDeltaU()[i];
                    uEntry[i] -= wizualizacjaZaklocen.getDeltaU()[i];
                }
            } else {
                dUZTemp[j][i] = 0.0;
            }
        } else {
            dUZTemp[j][i] = Math.min(wizualizacjaZaklocen.getUSkok()[i], wizualizacjaZaklocen.getDeltaU()[i]);
            uEntry[i] -= Math.min(wizualizacjaZaklocen.getUSkok()[i], wizualizacjaZaklocen.getDeltaU()[i]);
        }
    }

    private static void setDisturbanceTuningValueExit(WizualizacjaZaklocen wizualizacjaZaklocen, double[][] dUZTemp, double[] uExit, int i, int j) {
        if (j > 0) {
            if (uExit[i] != 0.0) {
                if (uExit[i] - wizualizacjaZaklocen.getDeltaU()[i] < 0) {
                    dUZTemp[j][i] = -uExit[i];
                    uExit[i] = 0.0;
                } else {
                    dUZTemp[j][i] = -wizualizacjaZaklocen.getDeltaU()[i];
                    uExit[i] -= wizualizacjaZaklocen.getDeltaU()[i];
                }
            } else {
                dUZTemp[j][i] = 0.0;
            }
        } else {
            dUZTemp[j][i] = wizualizacjaZaklocen.getDeltaU()[i];
        }
    }

    private static MIMO configureObject(MultipartFile[] file, ParRegulator parRegulator, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        if (parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {
            try {
                if (file.length == 3) {
                    JsonNode root = objectMapper.readTree(file[2].getInputStream());
                    ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                    MIMODPA zaklocenie = new MIMODPA(obiekty, parWizualizacja.getBlad());
                    root = objectMapper.readTree(file[0].getInputStream());
                    obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                    return new MIMODPA(obiekty, parWizualizacja.getBlad(), zaklocenie);
                } else {
                    JsonNode root = objectMapper.readTree(file[0].getInputStream());
                    ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                    return new MIMODPA(obiekty, parWizualizacja.getBlad());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            try {
                if (file.length == 3) {
                    JsonNode root = objectMapper.readTree(file[2].getInputStream());
                    DisturbanceDiscrete[] disturbanceDiscrete = objectMapper.treeToValue(root.path("ZakloceniaRownania"), DisturbanceDiscrete[].class);
                    root = objectMapper.readTree(file[0].getInputStream());
                    ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                    return new MIMODiscrete(obiekty, parWizualizacja.getBlad(), disturbanceDiscrete);
                } else {
                    JsonNode root = objectMapper.readTree(file[0].getInputStream());
                    ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                    return new MIMODiscrete(obiekty, parWizualizacja.getBlad());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    private static MIMO configureSimulatedObject(MultipartFile[] file, ParRegulator parRegulator, ParWizualizacja parWizualizacja, ObjectMapper objectMapper,
                                                 MIMO object) {
        if (file.length > 1) {
            if (parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {
                object = configureObjectDPA(file, parWizualizacja, objectMapper);
            } else {
                object = configureObjectDiscrete(file, parWizualizacja, objectMapper);
            }
        }
        return object;
    }

    private static MIMO configureObjectDPA(MultipartFile[] file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        MIMO object;
        JsonNode root;
        try {

            if (file.length == 3) {
                root = objectMapper.readTree(file[2].getInputStream());
                ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                MIMODPA zaklocenie = new MIMODPA(obiekty, parWizualizacja.getBlad());
                root = objectMapper.readTree(file[1].getInputStream());
                obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                object = new MIMODPA(obiekty, parWizualizacja.getBlad(), zaklocenie);
            } else {
                root = objectMapper.readTree(file[1].getInputStream());
                ParObiektDPAMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
                object = new MIMODPA(obiekty, parWizualizacja.getBlad());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return object;
    }

    private static MIMO configureObjectDiscrete(MultipartFile[] file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        JsonNode root;
        MIMO object;
        try {
            if (file.length == 3) {
                root = objectMapper.readTree(file[2].getInputStream());
                DisturbanceDiscrete[] disturbanceDiscrete = objectMapper.treeToValue(root.path("ZakloceniaRownania"), DisturbanceDiscrete[].class);
                root = objectMapper.readTree(file[1].getInputStream());
                ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                object = new MIMODiscrete(obiekty, parWizualizacja.getBlad(), disturbanceDiscrete);
            } else {
                root = objectMapper.readTree(file[1].getInputStream());
                ParObiektRownaniaMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
                object = new MIMODiscrete(obiekty, parWizualizacja.getBlad());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return object;
    }

    public OdpowiedzStrojenie SISOTuning(ParStrojenie parStrojenie) {
        ParObiektDPA parObiektDPA = parStrojenie.getParObiektDPA();
        ParObiektRownania parObiektRownania = parStrojenie.getParObiektRownania();
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        ZakloceniaDPA zakloceniaDPA;
        DisturbanceDiscrete disturbanceDiscrete;
        if (parStrojenie.getZakloceniaDPA().getGain() != null && parStrojenie.getZakloceniaDPA().getGain().length != 0) {
            zakloceniaDPA = parStrojenie.getZakloceniaDPA();
        } else {
            zakloceniaDPA = null;
        }
        if (parStrojenie.getZakloceniaRownania().getB1() != null && parStrojenie.getZakloceniaRownania().getB1().length != 0) {
            disturbanceDiscrete = parStrojenie.getZakloceniaRownania();
        } else {
            disturbanceDiscrete = null;
        }

        Integer[] EAParameters = new Integer[3];
        OdpowiedzStrojenie odpowiedzStrojenie = new OdpowiedzStrojenie();

        SISO object = getSISOObject(parObiektDPA, parObiektRownania, parRegulator, parWizualizacja, zakloceniaDPA, disturbanceDiscrete);
        if (object == null) {
            return null;
        }
        AbstractController abstractController;
        abstractController = getSISOController(parObiektDPA, parRegulator, parWizualizacja, object, EAParameters);
        if (abstractController == null) {
            return null;
        }
        if (abstractController.getNumberOfTuningParameters() != 0) {
            EvolutionaryAlgorithm GA = new EvolutionaryAlgorithm(EAParameters[0], EAParameters[1], EAParameters[2], 0.3, 0.2);
            getSISOTuning(parWizualizacja, object, abstractController, GA, odpowiedzStrojenie);
        } else {
            double[] primitiveArray = new double[parWizualizacja.getStrojenie().length];
            for (int i = 0; i < parWizualizacja.getStrojenie().length; i++) {
                primitiveArray[i] = parWizualizacja.getStrojenie()[i];
            }
            odpowiedzStrojenie.setWspolczynniki(primitiveArray);
        }
        if (parStrojenie.getParObiektSymulacjiDPA() != null) {
            if (zakloceniaDPA != null) {
                object = new SISODPA(parStrojenie.getParObiektSymulacjiDPA(), parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(),
                    zakloceniaDPA);
            } else {
                object = new SISODPA(parStrojenie.getParObiektSymulacjiDPA(), parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
            }
        }
        double[] Y = simulateSISO(parWizualizacja, object, abstractController, odpowiedzStrojenie, parStrojenie.getWizualizacjaZaklocen());
        System.out.println("strojenie::OK");
        getSISOError(parWizualizacja, abstractController, Y);
        return odpowiedzStrojenie;
    }

    private void getSISOError(ParWizualizacja parWizualizacja, AbstractController abstractController, double[] Y) {
        double error = 0.0;
        for (int i = 0; i < parWizualizacja.getSkok()[0]; i++) {
            error += Math.pow(Y[i] - parWizualizacja.getYPP()[0], 2);
        }
        for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
            error += Math.pow(Y[i] - abstractController.getSetpoint()[0], 2);
        }
        error = error / parWizualizacja.getDlugosc();
        System.out.println("BLAD:" + error);
    }

    private double[] simulateSISO(ParWizualizacja parWizualizacja, SISO object, AbstractController abstractController,
                                  OdpowiedzStrojenie odpowiedzStrojenie, WizualizacjaZaklocen wizualizacjaZaklocen) {
        setSetpoint(parWizualizacja, abstractController, odpowiedzStrojenie);
        if (wizualizacjaZaklocen.getUSkok() != null && wizualizacjaZaklocen.getUSkok().length != 0) {
            return simulationSISO(parWizualizacja, object, abstractController, odpowiedzStrojenie,
                setDisturbanceTuning(parWizualizacja, wizualizacjaZaklocen));
        } else {
            return simulationSISO(parWizualizacja, object, abstractController, odpowiedzStrojenie);
        }

    }

    private void setSetpoint(ParWizualizacja parWizualizacja, AbstractController abstractController, OdpowiedzStrojenie odpowiedzStrojenie) {
        abstractController.setSetpoint(new double[] {parWizualizacja.getYZad()[0]});
        double[] setpointTemp = new double[parWizualizacja.getDlugosc()];
        for (int i = 0; i < parWizualizacja.getDlugosc(); i++) {
            if (i < parWizualizacja.getSkok()[0]) {
                setpointTemp[i] = parWizualizacja.getYPP()[0];
            } else {
                setpointTemp[i] = parWizualizacja.getYZad()[0];
            }

        }
        odpowiedzStrojenie.setCel(setpointTemp);
    }

    private double[][] setDisturbanceTuning(ParWizualizacja parWizualizacja, WizualizacjaZaklocen wizualizacjaZaklocen) {
        double[][] dUZTemp = new double[parWizualizacja.getDlugosc()][wizualizacjaZaklocen.getUSkok().length];
        double[] uEntry = Arrays.stream(wizualizacjaZaklocen.getUSkok().clone()).mapToDouble(Double::doubleValue).toArray();
        double[] uExit = Arrays.stream(wizualizacjaZaklocen.getUSkok().clone()).mapToDouble(Double::doubleValue).toArray();
        for (int i = 0; i < wizualizacjaZaklocen.getUSkok().length; i++) {
            for (int j = 0; j < parWizualizacja.getDlugosc(); j++) {
                if (j < wizualizacjaZaklocen.getSkokZaklocenia()[i]) {
                    dUZTemp[j][i] = 0.0;
                } else {
                    if (j < wizualizacjaZaklocen.getSkokPowrotnyZaklocenia()[i]) {
                        setDisturbanceTuningValueEntry(wizualizacjaZaklocen, dUZTemp, uEntry, i, j);
                    } else {
                        setDisturbanceTuningValueExit(wizualizacjaZaklocen, dUZTemp, uExit, i, j);
                    }
                }
            }
        }
        return dUZTemp;
    }

    private void getSISOTuning(ParWizualizacja parWizualizacja, SISO object, AbstractController abstractController, EvolutionaryAlgorithm GA,
                               OdpowiedzStrojenie odpowiedzStrojenie) {
        double[] tempGAParameters = GA.getTuningParameters(abstractController.getNumberOfTuningParameters(), abstractController, object);
        double[] tempTuning = setTuning(parWizualizacja, tempGAParameters);
        odpowiedzStrojenie.setWspolczynniki(tempTuning);
        abstractController.changeTuning(tempTuning);
    }

    private SISO getSISOObject(ParObiektDPA parObiektDPA, ParObiektRownania parObiektRownania, ParRegulator parRegulator,
                               ParWizualizacja parWizualizacja, ZakloceniaDPA zakloceniaDPA, DisturbanceDiscrete disturbanceDiscrete) {
        try {
            if (!parRegulator.getTyp().equals("gpc")) {

                if (zakloceniaDPA != null) {
                    return new SISODPA(parObiektDPA, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(), zakloceniaDPA);
                } else {
                    return new SISODPA(parObiektDPA, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
                }
            } else {
                if (disturbanceDiscrete != null) {
                    return new SISODiscrete(parObiektRownania, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad(),
                        disturbanceDiscrete);
                } else {
                    return new SISODiscrete(parObiektRownania, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private AbstractController getSISOController(ParObiektDPA parObiektDPA, ParRegulator parRegulator, ParWizualizacja parWizualizacja, SISO object,
                                                 Integer[] EAParameters) {
        AbstractController abstractController;
        if (parRegulator.getTyp().equals("pid")) {
            abstractController = new PIDController(0.0, 0.0, 0.0, parObiektDPA.getTp(), new double[] {object.getYMax()}, parRegulator.getDuMax(), parRegulator.getUMax(),
                parWizualizacja.getStrojenie());
            EAParameters[0] = 150;
            EAParameters[1] = 50;
            EAParameters[2] = 600;
        } else if (parRegulator.getTyp().equals("dmc")) {
            abstractController = new DMCController(4, 0.1, (SISODPA) object, object.getYMax() / 2, parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            EAParameters[0] = 50;
            EAParameters[1] = 25;
            EAParameters[2] = 200;
        } else if (parRegulator.getTyp().equals("gpc")) {
            abstractController = new GPCController((SISODiscrete) object, 0.1, object.getYMax() / 2, parRegulator.getDuMax(), parWizualizacja.getStrojenie());
            EAParameters[0] = 50;
            EAParameters[1] = 25;
            EAParameters[2] = 200;
        } else {
            return null;
        }
        return abstractController;
    }

    private double[] simulationSISO(ParWizualizacja parWizualizacja, SISO object, AbstractController abstractController,
                                    OdpowiedzStrojenie odpowiedzStrojenie) {
        double[] Y = new double[parWizualizacja.getDlugosc()];
        double[] U = new double[parWizualizacja.getDlugosc()];
        object.resetObject();
        abstractController.resetController();
        Y[0] = object.getYpp();
        U[0] = parWizualizacja.getUPP()[0];
        for (int i = 1; i < parWizualizacja.getSkok()[0]; i++) {
            U[i] = parWizualizacja.getUPP()[0];
            Y[i] = object.simulateStep(parWizualizacja.getUPP()[0]);
        }
        for (int i = Math.max(1, parWizualizacja.getSkok()[0]); i < parWizualizacja.getDlugosc(); i++) {
            {
                Y[i] = object.simulateStep(abstractController.countControls(object.getOutput()));
                U[i] = object.getU().get(0);
            }
        }
        odpowiedzStrojenie.setWykres(Y);
        odpowiedzStrojenie.setSterowanie(U);
        return Y;
    }

    private double[] simulationSISO(ParWizualizacja parWizualizacja, SISO object, AbstractController abstractController,
                                    OdpowiedzStrojenie odpowiedzStrojenie, double[][] dUZ) {
        double[] Y = new double[parWizualizacja.getDlugosc()];
        double[] U = new double[parWizualizacja.getDlugosc()];
        double[][] Uz = new double[dUZ[0].length][parWizualizacja.getDlugosc()];
        object.resetObject();
        abstractController.resetController();
        Y[0] = object.getYpp();
        U[0] = parWizualizacja.getUPP()[0];

        abstractController.setSetpoint(parWizualizacja.getYPP());
        for (int i = 0; i < parWizualizacja.getSkok()[0]; i++) {
            Y[i] = object.simulateStep(abstractController.countControls(object.getOutput(), dUZ[i]), dUZ[i]);
            U[i] = object.getU().get(0);
        }
        abstractController.setSetpoint(new double[] {parWizualizacja.getYZad()[0]});
        for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
            {
                Y[i] = object.simulateStep(abstractController.countControls(object.getOutput(), dUZ[i]), dUZ[i]);
                U[i] = object.getU().get(0);
            }
        }
        for (int j = 0; j < dUZ[0].length; j++) {
            Uz[j][0] = dUZ[0][j];
        }

        for (int i = 1; i < parWizualizacja.getDlugosc(); i++) {
            for (int j = 0; j < dUZ[0].length; j++) {
                Uz[j][i] = Uz[j][i - 1] + dUZ[i][j];
            }
        }
        odpowiedzStrojenie.setWykres(Y);
        odpowiedzStrojenie.setSterowanie(U);
        odpowiedzStrojenie.setSterowanieZaklocenia(Uz);
        return Y;
    }

    private double[] setTuning(ParWizualizacja parWizualizacja, double[] tempWartosciGA) {
        int iTemp = 0;
        double[] tempStrojenie = new double[parWizualizacja.getStrojenie().length];
        for (int i = 0; i < parWizualizacja.getStrojenie().length; i++) {
            if (parWizualizacja.getStrojenie()[i] == null) {
                tempStrojenie[i] = tempWartosciGA[iTemp];
                iTemp += 1;
            } else {
                tempStrojenie[i] = parWizualizacja.getStrojenie()[i];
            }
        }
        return tempStrojenie;
    }

    public OdpowiedzStrojenieMIMO MIMOTuning(MultipartFile[] file, ParRegulator parRegulator, ParWizualizacja parWizualizacja,
                                             WizualizacjaZaklocen wizualizacjaZaklocen) {
        ObjectMapper objectMapper = new ObjectMapper();
        MIMO object;

        Integer[] EAParameters = new Integer[3];
        OdpowiedzStrojenieMIMO odpowiedz = new OdpowiedzStrojenieMIMO();
        object = configureObject(file, parRegulator, parWizualizacja, objectMapper);
        if (object == null) {
            return null;
        }
        AbstractController abstractController = getMIMOController(parRegulator, parWizualizacja, objectMapper, object, EAParameters, file);
        if (abstractController == null) {
            return null;
        }
        if (abstractController.getNumberOfTuningParameters() != 0) {
            EvolutionaryAlgorithm GA = new EvolutionaryAlgorithm(EAParameters[0], EAParameters[1], EAParameters[2], 0.5, 0.5);
            getMIMOTuning(parWizualizacja, object, abstractController, GA, odpowiedz);
        } else {
            double[] tuningTemp = new double[parWizualizacja.getStrojenie().length];
            for (int i = 0; i < parWizualizacja.getStrojenie().length; i++) {
                tuningTemp[i] = parWizualizacja.getStrojenie()[i];
            }
            odpowiedz.setWspolczynniki(tuningTemp);
        }
        object = configureSimulatedObject(file, parRegulator, parWizualizacja, objectMapper, object);
        if (object == null) {
            return null;
        }
        double[][] Y = simulateMIMO(parWizualizacja, object, abstractController, odpowiedz, wizualizacjaZaklocen);
        System.out.println("strojenie::OK");
        getMIMOError(abstractController, parWizualizacja.getDlugosc(), Y);
        return odpowiedz;
    }

    private double[][] simulateMIMO(ParWizualizacja parWizualizacja, MIMO object,
                                    AbstractController abstractController, OdpowiedzStrojenieMIMO odpowiedz, WizualizacjaZaklocen wizualizacjaZaklocen) {
        double[][] setpointTemp = getSetpointMIMO(parWizualizacja, object, parWizualizacja.getDlugosc());
        odpowiedz.setCel(setpointTemp);
        if (wizualizacjaZaklocen.getUSkok() != null && wizualizacjaZaklocen.getUSkok().length != 0) {
            return simulationMIMO(object, abstractController, odpowiedz, parWizualizacja.getDlugosc(), setpointTemp,
                setDisturbanceTuning(parWizualizacja, wizualizacjaZaklocen));
        } else {
            return simulationMIMO(object, abstractController, odpowiedz, parWizualizacja.getDlugosc(), setpointTemp);
        }
    }

    private void getMIMOTuning(ParWizualizacja parWizualizacja, MIMO object, AbstractController abstractController, EvolutionaryAlgorithm GA,
                               OdpowiedzStrojenieMIMO odpowiedz) {
        double[] tempWartosciGA = GA.getTuningParameters(abstractController.getNumberOfTuningParameters(), abstractController, object);
        abstractController.changeTuning(tempWartosciGA);
        double[] tempStrojenie = setTuning(parWizualizacja, tempWartosciGA);
        odpowiedz.setWspolczynniki(tempStrojenie);
    }

    private void getMIMOError(AbstractController abstractController, int simulationLength, double[][] Y) {
        double error = 0.0;
        for (int i = 0; i < simulationLength; i++) {
            error += Math.pow(Y[0][i] - abstractController.getSetpoint()[0], 2);
        }
        error = error / Y[0].length;
        System.out.println(error);
    }

    private AbstractController getMIMOController(ParRegulator parRegulator, ParWizualizacja parWizualizacja,
                                                 ObjectMapper objectMapper, MIMO object, Integer[] EAParameters, MultipartFile[] file) {
        AbstractController abstractController;
        if (parRegulator.getTyp().equals("pid")) {
            Integer[] PV;
            try {
                JsonNode root = objectMapper.readTree(file[0].getInputStream());
                PV = objectMapper.treeToValue(root.path("PV"), Integer[].class);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            abstractController = new PIDCollection((MIMODPA) object, PV, parRegulator.getDuMax(), parWizualizacja.getStrojenie());
            EAParameters[0] = 50;
            EAParameters[1] = 25;
            EAParameters[2] = 200;
        } else if (parRegulator.getTyp().equals("dmc")) {
            double[] tempLambda = new double[object.getEntriesNumber()];
            for (int i = 0; i < object.getEntriesNumber(); i++) {
                tempLambda[i] = 0.5;
            }
            abstractController = new DMCController(4, tempLambda, (MIMODPA) object, object.getYMax(), parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            EAParameters[0] = 25;
            EAParameters[1] = 25;
            EAParameters[2] = 100;
        } else if (parRegulator.getTyp().equals("gpc")) {
            double[] tempLambda = new double[object.getEntriesNumber()];
            for (int i = 0; i < object.getEntriesNumber(); i++) {
                tempLambda[i] = 0.5;
            }
            abstractController = new GPCController((MIMODiscrete) object, 5, object.getYMax(), parRegulator.getDuMax(), parWizualizacja.getStrojenie(), tempLambda);
            EAParameters[0] = 25;
            EAParameters[1] = 25;
            EAParameters[2] = 100;
        } else {
            throw new RuntimeException();
        }
        return abstractController;
    }

    private double[][] simulationMIMO(MIMO object, AbstractController abstractController, OdpowiedzStrojenieMIMO odpowiedz, int simulationLength, double[][] setpointTemp) {
        object.resetObject();
        abstractController.resetController();
        double[][] Y = new double[object.getOutputNumber()][simulationLength];
        double[][] U = new double[object.getEntriesNumber()][simulationLength];
        for (int i = 0; i < simulationLength; i++) {
            double[] temp = new double[object.getOutputNumber()];
            //przy każdej iteracji jest pobierane yzad
            for (int m = 0; m < object.getOutputNumber(); m++) {
                temp[m] = setpointTemp[m][i];
            }
            //ustawiane
            abstractController.setSetpoint(temp);
            double[] tempY = object.simulateStep(abstractController.countControls(object.getOutput()));
            for (int j = 0; j < object.getOutputNumber(); j++) {
                Y[j][i] = tempY[j];
            }
            for (int j = 0; j < object.getEntriesNumber(); j++) {
                U[j][i] = object.getU().get(j).get(0);
            }
        }
        odpowiedz.setSterowanie(U);
        odpowiedz.setWykres(Y);
        return Y;
    }

    private double[][] simulationMIMO(MIMO object, AbstractController abstractController,
                                      OdpowiedzStrojenieMIMO odpowiedz, int simulationLength,
                                      double[][] setpointTemp, double[][] dUZ) {
        object.resetObject();
        abstractController.resetController();
        double[][] Y = new double[object.getOutputNumber()][simulationLength];
        double[][] U = new double[object.getEntriesNumber()][simulationLength];
        double[][] Uz = new double[dUZ[0].length][simulationLength];

        for (int i = 0; i < simulationLength; i++) {
            double[] temp = new double[object.getOutputNumber()];
            //przy każdej iteracji jest pobierane yzad
            for (int m = 0; m < object.getOutputNumber(); m++) {
                temp[m] = setpointTemp[m][i];
            }
            //ustawiane
            abstractController.setSetpoint(temp);
            double[] tempY = object.simulateStep(abstractController.countControls(object.getOutput(), dUZ[i]), dUZ[i]);
            for (int j = 0; j < object.getOutputNumber(); j++) {
                Y[j][i] = tempY[j];
            }
            for (int j = 0; j < object.getEntriesNumber(); j++) {
                U[j][i] = object.getU().get(j).get(0);
            }
        }
        for (int j = 0; j < dUZ[0].length; j++) {
            Uz[j][0] = dUZ[0][j];
        }

        for (int i = 1; i < simulationLength; i++) {
            for (int j = 0; j < dUZ[0].length; j++) {
                Uz[j][i] = Uz[j][i - 1] + dUZ[i][j];
            }
        }
        odpowiedz.setSterowanie(U);
        odpowiedz.setWykres(Y);
        odpowiedz.setSterowanieZaklocenia(Uz);
        return Y;
    }

    private double[][] getSetpointMIMO(ParWizualizacja parWizualizacja, MIMO object, int simulationLength) {
        double[][] setpointTemp = new double[object.getOutputNumber()][simulationLength];
        for (int i = 0; i < object.getOutputNumber(); i++) {
            for (int j = 0; j < simulationLength; j++) {
                if (j < parWizualizacja.getSkok()[i]) {
                    setpointTemp[i][j] = parWizualizacja.getYPP()[i];
                } else {
                    setpointTemp[i][j] = parWizualizacja.getYZad()[i];
                }
            }
        }
        return setpointTemp;
    }
}
