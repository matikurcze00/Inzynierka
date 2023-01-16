package com.example.inzynierka.controller;

import com.example.inzynierka.EA.AlgorytmEwolucyjny;
import com.example.inzynierka.obiekty.SISO;
import com.example.inzynierka.modele.Odpowiedz;
import com.example.inzynierka.modele.ParObiekt;
import com.example.inzynierka.modele.ParRegulator;
import com.example.inzynierka.modele.ParStrojenie;
import com.example.inzynierka.regulatory.DMC;
import com.example.inzynierka.regulatory.PID;
import com.example.inzynierka.regulatory.Regulator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {

//    @CrossOrigin(origins = "http://localhost:8080")
    @RequestMapping(value="/")
    public static String Welcome() {
        return "Welcome to Spring Boot \n" +
                "Remember to subscribe and leave a comment";
    }

    @RequestMapping(value="/strojenie", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public static ResponseEntity<Odpowiedz> strojenie(@RequestBody ParStrojenie parStrojenie)
    {
        System.out.println("strojenie::start ");
        try {
            ParObiekt parObiekt = parStrojenie.getParObiekt();
            ParRegulator parRegulator = parStrojenie.getParRegulator();
            parRegulator.setTyp("pid");
            parRegulator.setUMax(100.0);
            parRegulator.setDuMax(3.0);
            Double[] z;
            if (parObiekt.getZ2() != null)
                z = new Double[]{parObiekt.getZ1(), parObiekt.getZ2()};
            else if (parObiekt.getZ1() != null)
                z = new Double[]{parObiekt.getZ1()};
            else
                z = new Double[]{};

            Double[] b;
            if (parObiekt.getB3() != null)
                b = new Double[]{parObiekt.getB1(), parObiekt.getB2(), parObiekt.getB3()};
            else if (parObiekt.getB2() != null)
                b = new Double[]{parObiekt.getZ1(), parObiekt.getB2()};
            else
                b = new Double[]{parObiekt.getZ1()};
            b[2]=b[2]+9.0;
            SISO SISO = new SISO(z, b, parObiekt.getK(), parRegulator.getUMax(), parObiekt.getTs(), parObiekt.getOpoznienie(), parObiekt.getSzum());
            Regulator regulator;
            if (parRegulator.getTyp().equals("pid"))
                regulator = new PID(0.0, 0.0, 0.0, parObiekt.getTs(), SISO.getYMax() / 2, parRegulator.getDuMax(), parRegulator.getUMax());
            else if (parRegulator.getTyp().equals("dmc"))
                regulator = new DMC(5, 0.1, SISO, SISO.getYMax() / 2, parRegulator.getDuMax(), 11);
            else
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(100, 50, 10, 0.3, 0.2);
            Odpowiedz odpowiedz = new Odpowiedz();
            odpowiedz.setWspolczynniki(GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, SISO));
            regulator.zmienWartosci(odpowiedz.getWspolczynniki());
            regulator.setCel(SISO.getYMax() / 3);
            odpowiedz.setCel(regulator.getCel());
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
                blad+=Math.pow(Y[i]-regulator.getCel(),2);
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
}
