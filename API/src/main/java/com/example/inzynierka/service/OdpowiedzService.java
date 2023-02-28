package com.example.inzynierka.service;

import com.example.inzynierka.modele.*;
import com.example.inzynierka.obiekty.MIMO;
import com.example.inzynierka.obiekty.SISO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OdpowiedzService {

    public OdpowiedzSkokowa SISOOdpowiedz(ParStrojenie parStrojenie) {
        ParObiekt parObiekt = parStrojenie.getParObiekt();
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        SISO obiekt;
        try {
            obiekt = new SISO(parObiekt, parRegulator.getUMax(), parRegulator.getUMin(), parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        double U = 1;
        double[][] odpSkokowa = new double[1][parWizualizacja.getDlugosc()];
        symulacjaOdpowiedziSISO(parWizualizacja, obiekt, U, odpSkokowa);
        return new OdpowiedzSkokowa(odpSkokowa);
    }

    private void symulacjaOdpowiedziSISO(ParWizualizacja parWizualizacja, SISO obiekt, double U, double[][] odpSkokowa) {
        double Utemp = 0;
        odpSkokowa[0][0] = obiekt.obliczKrok(Utemp);
        odpSkokowa[0][1] = obiekt.obliczKrok(U);
        for (int i = 2; i < parWizualizacja.getDlugosc(); i++)
            odpSkokowa[0][i] = obiekt.obliczKrok(Utemp);
    }

    public OdpowiedzSkokowa MIMOOdpowiedz(MultipartFile file, ParRegulator parRegulator, ParWizualizacja parWizualizacja) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektMIMO[] obiekty;
        MIMO obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
            obiekt = new MIMO(obiekty, parWizualizacja.getBlad());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        double U = 1;
        double Utemp = 0;
        double[][] odpSkokowa = new double[obiekt.getLiczbaIN() * obiekt.getLiczbaOUT()][parWizualizacja.getDlugosc()];
        symulacjaOdpowiedziMIMO(parWizualizacja, obiekt, U, Utemp, odpSkokowa);
        return new OdpowiedzSkokowa(odpSkokowa);
    }

    private void symulacjaOdpowiedziMIMO(ParWizualizacja parWizualizacja, MIMO obiekt, double U, double Utemp, double[][] odpSkokowa) {
        for (int k = 0; k < obiekt.getLiczbaIN(); k++) {
            for (int j = 0; j < obiekt.getLiczbaOUT(); j++) {
                obiekt.resetObiektu();
                odpSkokowa[k * obiekt.getLiczbaIN() + j][0] = obiekt.obliczKrok(Utemp, k, j);
                odpSkokowa[k * obiekt.getLiczbaIN() + j][1] = obiekt.obliczKrok(U, k, j);
                for (int i = 2; i < parWizualizacja.getDlugosc(); i++)
                    odpSkokowa[k * obiekt.getLiczbaIN() + j][i] = obiekt.obliczKrok(Utemp, k, j);
            }
        }
    }
}
