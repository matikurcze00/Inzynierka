package com.example.inzynierka.services;

import com.example.inzynierka.models.*;
import com.example.inzynierka.objects.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StepResponseService {

    public OdpowiedzSkokowa SISOOdpowiedz(ParStrojenie parStrojenie) {
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        if(parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {

            return getStepResponseSISODPA(parStrojenie, parRegulator, parWizualizacja);
        } else {
            return getStepResponseSISODiscrete(parStrojenie, parRegulator, parWizualizacja);
        }
    }

    private OdpowiedzSkokowa getStepResponseSISODiscrete(ParStrojenie parStrojenie, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ParObiektRownania parObiektRownania = parStrojenie.getParObiektRownania();
        SISODiscrete obiekt;
        try {
            obiekt = new SISODiscrete(parObiektRownania, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        double[][] stepResponse = new double[1][parWizualizacja.getDlugosc()];
        simulateSISO(parWizualizacja, obiekt, stepResponse);
        return new OdpowiedzSkokowa(stepResponse);
    }

    private OdpowiedzSkokowa getStepResponseSISODPA(ParStrojenie parStrojenie, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ParObiektDPA parObiektDPA = parStrojenie.getParObiektDPA();
        SISODPA object;
        try {
            object = new SISODPA(parObiektDPA, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        double[][] stepResponse = new double[1][parWizualizacja.getDlugosc()];
        simulateSISO(parWizualizacja, object, stepResponse);
        return new OdpowiedzSkokowa(stepResponse);
    }

    private void simulateSISO(ParWizualizacja parWizualizacja, SISO object, double[][] stepResponse) {
        double UStep = 1;
        double UZero = 0;
        stepResponse[0][0] = object.simulateStep(UZero);
        stepResponse[0][1] = object.simulateStep(UStep);
        for (int i = 2; i < parWizualizacja.getDlugosc(); i++) {
            stepResponse[0][i] = object.simulateStep(UZero);
        }
    }

    public OdpowiedzSkokowa MIMOStepResponse(MultipartFile file, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ObjectMapper objectMapper = new ObjectMapper();
        if(parRegulator.getTyp().equals("pid") || parRegulator.getTyp().equals("dmc")) {
            return getStepResponseMIMODPA(file, parWizualizacja, objectMapper);
        } else {
            return getStepResponseMIMODiscrete(file, parWizualizacja, objectMapper);
        }
    }

    private OdpowiedzSkokowa getStepResponseMIMODPA(MultipartFile file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        JsonNode root;
        ParObiektDPAMIMO[] transmittances;
        MIMODPA object;
        try {
            root = objectMapper.readTree(file.getInputStream());
            transmittances = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
            object = new MIMODPA(transmittances, parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        double U = 1;
        double Utemp = 0;
        double[][] stepResponse = new double[object.getEntriesNumber() * object.getOutputNumber()][parWizualizacja.getDlugosc()];
        getStepResponseMIMO(parWizualizacja, object, U, Utemp, stepResponse);
        return new OdpowiedzSkokowa(stepResponse);
    }
    private OdpowiedzSkokowa getStepResponseMIMODiscrete(MultipartFile file, ParWizualizacja parWizualizacja, ObjectMapper objectMapper) {
        JsonNode root;
        ParObiektRownaniaMIMO[] transmittances;
        MIMODiscrete object;
        try {
            root = objectMapper.readTree(file.getInputStream());
            transmittances = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
            object = new MIMODiscrete(transmittances, parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        double U = 1;
        double Utemp = 0;
        double[][] stepResponse = new double[object.getEntriesNumber() * object.getOutputNumber()][parWizualizacja.getDlugosc()];
        getStepResponseMIMO(parWizualizacja, object, U, Utemp, stepResponse);
        return new OdpowiedzSkokowa(stepResponse);
    }

    private void getStepResponseMIMO(ParWizualizacja parWizualizacja, MIMO Object, double U, double Utemp, double[][] stepResponse) {
        for (int k = 0; k < Object.getEntriesNumber(); k++) {
            for (int j = 0; j < Object.getOutputNumber(); j++) {
                Object.resetObject();
                stepResponse[k * Object.getEntriesNumber() + j][0] = Object.simulateStep(Utemp, k, j);
                stepResponse[k * Object.getEntriesNumber() + j][1] = Object.simulateStep(U, k, j);
                for (int i = 2; i < parWizualizacja.getDlugosc(); i++) {
                    stepResponse[k * Object.getEntriesNumber() + j][i] = Object.simulateStep(Utemp, k, j);
                }
            }
        }
    }

}
