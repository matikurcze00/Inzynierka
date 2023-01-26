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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public static ResponseEntity<OdpowiedzStrojenie> strojenieSISO(@RequestBody ParStrojenie parStrojenie)
    {
        System.out.println("strojenieSISO::start ");
        try {
            ParObiekt parObiekt = parStrojenie.getParObiekt();
            ParRegulator parRegulator = parStrojenie.getParRegulator();
            ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
            SISO SISO = new SISO(parObiekt, parRegulator.getUMax(), parRegulator.getUMin());
            Regulator regulator;

            if (parRegulator.getTyp().equals("pid"))
                regulator = new PID(0.0, 0.0, 0.0, parObiekt.getTp(),new double[]{SISO.getYMax()} , parRegulator.getDuMax(), parRegulator.getUMax(), parWizualizacja.getStrojenie());
            else if (parRegulator.getTyp().equals("dmc"))
                regulator = new DMC(4, 0.1, SISO, SISO.getYMax() / 2, parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            else
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(300, 40, 6, 0.3, 0.2);
            OdpowiedzStrojenie odpowiedzStrojenie = new OdpowiedzStrojenie();
            double[] tempWartosciGA = GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, SISO);
            int iTemp = 0;
            double[] tempStrojenie = new double[parWizualizacja.getStrojenie().length];
            for(int i =0; i<parWizualizacja.getStrojenie().length; i++)
            {
                if(parWizualizacja.getStrojenie()[i]==null)
                {
                    tempStrojenie[i]=tempWartosciGA[iTemp];
                    iTemp+=1;
                }
                else
                {
                    tempStrojenie[i]=parWizualizacja.getStrojenie()[i];
                }
            }
            odpowiedzStrojenie.setWspolczynniki(tempStrojenie);
            regulator.zmienWartosci(odpowiedzStrojenie.getWspolczynniki());
            regulator.setCel(new double[]{parWizualizacja.getYZad()[0]});
            double[] celTemp = new double[parWizualizacja.getDlugosc()];
            for(int i = 0; i< parWizualizacja.getDlugosc(); i++)
            {
                if(i<parWizualizacja.getSkok()[0])
                    celTemp[i] = parWizualizacja.getYPP()[0];
                else
                    celTemp[i] = parWizualizacja.getYZad()[0];

            }
            odpowiedzStrojenie.setCel(celTemp);

            double[] Y = new double[parWizualizacja.getDlugosc()];
            double[] U = new double[parWizualizacja.getDlugosc()];
            SISO.resetObiektu();
            regulator.resetujRegulator();
            Y[0]=SISO.getYpp();
            U[0]=parWizualizacja.getUPP()[0];
            for(int i = 0; i<parWizualizacja.getSkok()[0]; i++)
            {
                U[i] = parWizualizacja.getUPP()[0];
                Y[i] = SISO.obliczKrok(parWizualizacja.getUPP()[0]);
            }
            for (int i = parWizualizacja.getSkok()[0]; i < parWizualizacja.getDlugosc(); i++) {
                {
                    Y[i] = SISO.obliczKrok(regulator.policzOutput(SISO.getAktualna()));
                    U[i] = SISO.getU().get(0);
                }
            }
            System.out.println("strojenie::OK");

            odpowiedzStrojenie.setWykres(Y);
            odpowiedzStrojenie.setSterowanie(U);
            double blad = 0.0;
            for(int i = parWizualizacja.getSkok()[0]; i<parWizualizacja.getDlugosc(); i++)
            {
                blad+=Math.pow(Y[i]-regulator.getCel()[0],2);
            }
            blad=blad/(parWizualizacja.getDlugosc()-parWizualizacja.getSkok()[0]);
            System.out.println("BLAD:" + blad);
            return ResponseEntity.ok(odpowiedzStrojenie);
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
    public static ResponseEntity<OdpowiedzStrojenieMIMO> strojenieMIMO(@RequestPart("file") MultipartFile file, @ModelAttribute ParRegulator parRegulator, @ModelAttribute ParWizualizacja parWizualizacja) {
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
                regulator = new ZbiorPID(obiekt,PV, parRegulator.getDuMax(), parWizualizacja.getStrojenie());
            }else if (parRegulator.getTyp().equals("dmc"))
            {
                double[] tempLambda = {0.5,0.5};
                regulator = new DMC(5, tempLambda, obiekt, obiekt.getYMax(), parRegulator.getDuMax(), 11, parWizualizacja.getStrojenie());
            }
            else
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(40, 10, 2, 0.3, 0.4);
            OdpowiedzStrojenieMIMO odpowiedz = new OdpowiedzStrojenieMIMO();
            double[] tempWartosciGA =GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt);
            int iTemp = 0;
            double[] tempStrojenie = new double[parWizualizacja.getStrojenie().length];
            regulator.zmienWartosci(tempWartosciGA);
            for(int i =0; i<parWizualizacja.getStrojenie().length; i++)
            {
                if(parWizualizacja.getStrojenie()[i]==null)
                {
                    tempStrojenie[i]=tempWartosciGA[iTemp];
                    iTemp+=1;
                }
                else
                {
                    tempStrojenie[i]=parWizualizacja.getStrojenie()[i];
                }
            }
            odpowiedz.setWspolczynniki(tempStrojenie);
            int dlugoscSymulacji = parWizualizacja.getDlugosc();
            double[][] Y = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji];
            double[][] U = new double[obiekt.getLiczbaIN()][dlugoscSymulacji];

            //yZad
            double[][] celTemp = new double[obiekt.getLiczbaOUT()][dlugoscSymulacji];
            for(int i = 0; i < obiekt.getLiczbaOUT(); i++)
            {
                for(int j = 0; j < dlugoscSymulacji; j++)
                {
                    if(j<parWizualizacja.getSkok()[i])
                        celTemp[i][j] = parWizualizacja.getYPP()[i];
                    else
                        celTemp[i][j] = parWizualizacja.getYZad()[i];
                }
            }
            odpowiedz.setCel(celTemp);

            obiekt.resetObiektu();
            for (int i = 0; i < dlugoscSymulacji; i++) {
                double[] temp = new double[obiekt.getLiczbaOUT()];
                //przy każdej iteracji jest pobierane yzad
                for(int m = 0; m < obiekt.getLiczbaOUT(); m++)
                {
                    temp[m] = celTemp[m][i];
                }
                //ustawiane
                regulator.setCel(temp);
                double [] tempY = obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne()));
                for(int j = 0; j < obiekt.getLiczbaOUT(); j++)
                    Y[j][i]=tempY[j];
                for(int j = 0; j<obiekt.getLiczbaIN(); j++)
                    U[j][i]=obiekt.getU().get(j).get(0);
            }

            System.out.println("strojenie::OK");
            odpowiedz.setWykres(Y);
            odpowiedz.setSterowanie(U);
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
            return new ResponseEntity<OdpowiedzStrojenieMIMO>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value="/odpowiedz/SISO",  method = RequestMethod.POST)
    @ResponseBody
    public static ResponseEntity<OdpowiedzSkokowa> odpowiedzSISO(@RequestBody ParStrojenie parStrojenie)
    {
        ParObiekt parObiekt = parStrojenie.getParObiekt();
        ParRegulator parRegulator = parStrojenie.getParRegulator();
        ParWizualizacja parWizualizacja = parStrojenie.getParWizualizacja();
        SISO SISO = new SISO(parObiekt, parRegulator.getUMax(), parRegulator.getUMin());

        double U = 1;

        double[][] odpSkokowa = new double[1][parWizualizacja.getDlugosc()];
        double Utemp = 0;
        odpSkokowa[0][0]=SISO.obliczKrok(Utemp);
        odpSkokowa[0][1]=SISO.obliczKrok(U);
        for(int i = 2; i < parWizualizacja.getDlugosc(); i++)
            odpSkokowa[0][i] = SISO.obliczKrok(Utemp);
       return ResponseEntity.ok(new OdpowiedzSkokowa(odpSkokowa));
    }

    @RequestMapping(value="/odpowiedz/MIMO", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public static ResponseEntity<OdpowiedzSkokowa> odpowiedzMIMO(@RequestPart("file") MultipartFile file, @ModelAttribute ParRegulator parRegulator, @ModelAttribute ParWizualizacja parWizualizacja)
    {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(file.getInputStream());
            ParObiektMIMO[] obiekty = objectMapper.treeToValue(root.path("ParObiektMIMO"), ParObiektMIMO[].class);
            MIMO obiekt  = new MIMO(obiekty);

            double U = 1;
            double Utemp = 0;
            double[][] odpSkokowa = new double[obiekt.getLiczbaIN()*obiekt.getLiczbaOUT()][parWizualizacja.getDlugosc()];
            for(int k = 0; k<obiekt.getLiczbaIN(); k++)
            {
                for(int j = 0; j < obiekt.getLiczbaOUT(); j++)
                {
                    obiekt.resetObiektu();
                    odpSkokowa[k*obiekt.getLiczbaIN()+j][0]=obiekt.obliczKrok(Utemp,k,j);
                    odpSkokowa[k*obiekt.getLiczbaIN()+j][1]=obiekt.obliczKrok(U,k,j);
                    for(int i = 2; i < parWizualizacja.getDlugosc(); i++)
                        odpSkokowa[k*obiekt.getLiczbaIN()+j][i] = obiekt.obliczKrok(Utemp,k,j);
                }
            }
            return ResponseEntity.ok(new OdpowiedzSkokowa(odpSkokowa));
            }
            catch (Exception ex)
            {
                System.out.println(ex.getMessage());
                System.out.println(ex.getCause());
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
    }
}
