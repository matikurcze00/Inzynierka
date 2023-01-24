package com.example.inzynierka.controller;

import com.example.inzynierka.EA.AlgorytmEwolucyjny;
import com.example.inzynierka.modele.*;
import com.example.inzynierka.obiekty.MIMO;
import com.example.inzynierka.obiekty.SISO;
import com.example.inzynierka.regulatory.DMC;
import com.example.inzynierka.regulatory.PID;
import com.example.inzynierka.regulatory.Regulator;
import com.example.inzynierka.regulatory.ZbiorPID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class Controller {

//    @CrossOrigin(origins = "http://localhost:8080")
    @RequestMapping(value="/")
    public static String Welcome() {
        return "Welcome to Spring Boot \n" +
                "Remember to subscribe and leave a comment";
    }

    @RequestMapping(value="/strojenie/SISO",  method = RequestMethod.POST)
    @ResponseBody
    public static ResponseEntity<Odpowiedz> strojenieSISO(@RequestBody ParStrojenie parStrojenie)
    {
        System.out.println("strojenieSISO::start ");
        try {
            ParObiekt parObiekt = parStrojenie.getParObiekt();
            ParRegulator parRegulator = parStrojenie.getParRegulator();
//            parRegulator.setTyp("pid");
//            parRegulator.setUMax(100.0);
//            parRegulator.setDuMax(3.0);

//            b[2]=b[2]+9.0;
            SISO SISO = new SISO(parObiekt, parRegulator.getUMax());
            Regulator regulator;

            if (parRegulator.getTyp().equals("pid"))
                regulator = new PID(0.0, 0.0, 0.0, parObiekt.getTp(),new double[]{SISO.getYMax()} , parRegulator.getDuMax(), parRegulator.getUMax());
            else if (parRegulator.getTyp().equals("dmc"))
                regulator = new DMC(5, 0.1, SISO, SISO.getYMax() / 2, parRegulator.getDuMax(), 11);
            else
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(300, 40, 6, 0.3, 0.2);
            Odpowiedz odpowiedz = new Odpowiedz();
            odpowiedz.setWspolczynniki(GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, SISO));
            regulator.zmienWartosci(odpowiedz.getWspolczynniki());
            regulator.setCel(new double[]{SISO.getYMax() / 3});
            odpowiedz.setCel(regulator.getCel()[0]);
            double[] Y = new double[50];

            SISO.resetObiektu();
            Y[0]=SISO.getYpp();
            for (int i = 1; i < 50; i++) {
                Y[i] = SISO.obliczKrok(regulator.policzOutput(SISO.getAktualna()));
            }
            System.out.println("strojenie::OK");
            odpowiedz.setWykres(Y);
            double blad = 0.0;
            for(int i = 0; i<50; i++)
            {
                blad+=Math.pow(Y[i]-regulator.getCel()[0],2);
            }
            blad=blad/Y.length;
            System.out.println("BLAD:" + blad);
            return ResponseEntity.ok(odpowiedz);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            System.out.println(ex.getCause());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value="/strojenie/MIMO", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public static ResponseEntity<OdpowiedzMIMO> strojenieMIMO(@RequestPart("file") MultipartFile file, @ModelAttribute ParRegulator parRegulator) {
        System.out.println("strojenieMIMO::start ");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(file.getInputStream());
            ParObiektMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
            MIMO obiekt  = new MIMO(obiekty);
            Regulator regulator;
            if(parRegulator.getTyp().equals("pid"))
            {
                Integer[] PV = objectMapper.treeToValue(root.path("PV"), Integer[].class);
                regulator = new ZbiorPID(obiekt,PV, parRegulator.getDuMax());
            }else if (parRegulator.getTyp().equals("dmc"))
            {
                double[] tempLambda = {0.5,0.5};
                regulator = new DMC(5, tempLambda, obiekt, obiekt.getYMax(), parRegulator.getDuMax(), 11);
            }
            else
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(400, 100, 20, 0.3, 0.4);
            OdpowiedzMIMO odpowiedz = new OdpowiedzMIMO();
            odpowiedz.setWspolczynniki(GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt));
            regulator.zmienWartosci(odpowiedz.getWspolczynniki());
            int dlugoscSymulacji = 50;
            double[][] Y = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji*obiekt.getLiczbaOUT()+1];
            double[] celTemp = new double[obiekt.getLiczbaOUT()];
            for(int i = 0; i <obiekt.getLiczbaOUT(); i++)
                celTemp[i] = 0;

            obiekt.resetObiektu();
            for(int i = 0; i<obiekt.getLiczbaOUT(); i++)
                Y[i][0]=obiekt.getYpp(i);
            double[] tempY = new double[obiekt.getLiczbaOUT()];
            for(int k = 0; k<obiekt.getLiczbaOUT(); k++)
            {
                for(int i = 0; i<=k; i++)
                {
                    celTemp[i]=obiekt.getYMax()[i]/3;
                    regulator.setCel(celTemp);
                }
                for (int i = 0; i < dlugoscSymulacji; i++) {
                    tempY = obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne()));
                    for(int j = 0; j < obiekt.getLiczbaOUT(); j++)
                        Y[j][k*dlugoscSymulacji+i+1]=tempY[j];
                }
            }

            System.out.println("strojenie::OK");
            odpowiedz.setCel(celTemp);
            odpowiedz.setWykres(Y);
            double blad = 0.0;
            for(int i = 0; i<50*obiekt.getLiczbaOUT(); i++)
            {
                blad+=Math.pow(Y[0][i]-regulator.getCel()[0],2);
            }
            blad=blad/Y[0].length;
            System.out.println(blad);
            return ResponseEntity.ok(odpowiedz);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<OdpowiedzMIMO>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
