package com.example.inzynierka.service;

import com.example.inzynierka.modele.OdpowiedzInfoMIMO;
import com.example.inzynierka.modele.ParObiektMIMO;
import com.example.inzynierka.obiekty.MIMO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InfoService {
    public OdpowiedzInfoMIMO InfoWejsciaWyjscia(MultipartFile file)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        ParObiektMIMO[] obiekty;
        MIMO obiekt;
        try {
            root = objectMapper.readTree(file.getInputStream());
            obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
            obiekt = new MIMO(obiekty);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        OdpowiedzInfoMIMO odpowiedz = new OdpowiedzInfoMIMO();
        odpowiedz.setWejscia(obiekt.getLiczbaIN());
        odpowiedz.setWyjscia(obiekt.getLiczbaOUT());
        return odpowiedz;
    }
}
