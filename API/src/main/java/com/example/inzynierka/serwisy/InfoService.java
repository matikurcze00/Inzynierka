package com.example.inzynierka.serwisy;

import com.example.inzynierka.modele.OdpowiedzInfoMIMO;
import com.example.inzynierka.modele.ParObiektDPAMIMO;
import com.example.inzynierka.modele.ParObiektRownaniaMIMO;
import com.example.inzynierka.modele.ZakloceniaRownania;
import com.example.inzynierka.obiekty.MIMODPA;
import com.example.inzynierka.obiekty.MIMORownianiaRoznicowe;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InfoService {
    public OdpowiedzInfoMIMO InfoWejsciaWyjsciaDPA(MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektDPAMIMO[] obiekty;
        MIMODPA obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektDPAMIMO"), ParObiektDPAMIMO[].class);
            obiekt = new MIMODPA(obiekty);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(obiekt.getLiczbaIN());
        odpowiedz.setWyjscia(obiekt.getLiczbaOUT());
        return odpowiedz;
    }

    public OdpowiedzInfoMIMO InfoWejsciaWyjsciaRownania(MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektRownaniaMIMO[] obiekty;
        MIMORownianiaRoznicowe obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektRownaniaMIMO"), ParObiektRownaniaMIMO[].class);
            obiekt = new MIMORownianiaRoznicowe(obiekty);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(obiekt.getLiczbaIN());
        odpowiedz.setWyjscia(obiekt.getLiczbaOUT());
        return odpowiedz;
    }

    public OdpowiedzInfoMIMO InfoWejsciaWyjsciaZakloceniaRownania(MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ZakloceniaRownania[] obiekty;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ZakloceniaRownania"), ZakloceniaRownania[].class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(obiekty.length);
        odpowiedz.setWyjscia(obiekty[0].getB1().length);
        return odpowiedz;
    }
}
