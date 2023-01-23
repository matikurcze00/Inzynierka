package com.example.inzynierka;


import com.example.inzynierka.obiekty.TransmitancjaCiagla;
import lombok.val;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {

        ////TransferFunction
        val obj = new TransmitancjaCiagla(1, 5, 0, 0, 0, 1, 0, 0, 0, 0.5);
        val y = new double[1][100];
        val u = new double[1][100];
        Arrays.fill(u[0], 1.0);
        for (int i = 0; i < 100; i++) {
            val calcOut = obj.obliczKrok(i, u, y, false);
            y[0][i] = calcOut[0];
        }
        //Values from Matlab simulation
        val expected = new double[]{0, 5, 4.7561, 4.5242, 4.3035, 4.0937, 3.894, 3.7041, 3.5234, 3.3516, 3.1881, 3.0327, 2.8847, 2.7441, 2.6102, 2.4829, 2.3618, 2.2466, 2.1371, 2.0328, 1.9337, 1.8394, 1.7497, 1.6644, 1.5832, 1.506, 1.4325, 1.3627, 1.2962, 1.233, 1.1729, 1.1157, 1.0612, 1.0095, 0.96025, 0.91342, 0.86887, 0.82649, 0.78619, 0.74784, 0.71137, 0.67668, 0.64367, 0.61228, 0.58242, 0.55402, 0.527, 0.50129, 0.47685, 0.45359, 0.43147, 0.41042, 0.39041, 0.37137, 0.35326, 0.33603, 0.31964, 0.30405, 0.28922, 0.27512, 0.2617, 0.24894, 0.23679, 0.22525, 0.21426, 0.20381, 0.19387, 0.18442, 0.17542, 0.16687, 0.15873, 0.15099, 0.14362, 0.13662, 0.12996, 0.12362, 0.11759, 0.11185, 0.1064, 0.10121, 0.096274, 0.091578, 0.087112, 0.082863, 0.078822, 0.074978, 0.071321, 0.067843, 0.064534, 0.061387, 0.058393, 0.055545, 0.052836, 0.050259, 0.047808, 0.045476, 0.043258, 0.041149, 0.039142, 0.037233};
//

        ////MIMO

//        ObjectMapper objectMapper = new ObjectMapper();
//        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
//        ParObiektMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektMIMO"), ParObiektMIMO[].class);
//        System.out.println(Obiekty[1]);
//        MIMO obiekt  = new MIMO(Obiekty);
//        ZbiorPID zbior = new ZbiorPID(obiekt,PV, 3.0, 100.0);
//        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(1000, 400, 16, 0.5,0.8);
//        zbior.zmienWartosci(GA.dobierzWartosci(zbior.liczbaZmiennych(), zbior, obiekt));
//        double[] celTemp = {200.0,0.0};
//        zbior.setCel(celTemp);
//        List<double[]> Y = new ArrayList<>();
//        obiekt.resetObiektu();
//        for(int k = 0; k<60; k++)
//        {
//                Y.add(obiekt.obliczKrok(zbior.policzOutput(obiekt.getAktualne())));
//
//        }
//        System.out.println("ok");
//        //DMC - MIMO
//        double[] celTemp = {550.0,100.0};
//        List<double[]> Y = new ArrayList<>();
//        double[] tempLambda = {0.05,0.05};
//        DMC regulator = new DMC(3, tempLambda, obiekt, celTemp, 3, 10);
//        obiekt.resetObiektu();
//        for(int k = 0; k<40; k++)
//        {
//                Y.add(obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne())));
//
//        }
//        System.out.println(Y.get(35)[0]);
//        System.out.println(Y.get(35)[1]);
//
//        System.out.println(regulator.getCel()[0]);
//        System.out.println(regulator.getCel()[1]);


        ////SISO
//        Double[] b = {1.0, 4.0, 5.0};
//        Double[] z = {3.0, 3.5};
//        SISO siso = new SISO(z,b,5, 100, 1.0, 0, 0);
//        List<Double> Y = new ArrayList<>();
//        double[] resetD = {0};
//        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(100, 400, 10, 0.3,0.2);
//        DMC dmc = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 10);
//
//        Double srednia = 0.0;
//            dmc.zmienWartosci(resetD);
//            double[] tempD = GA.dobierzWartosci(dmc.liczbaZmiennych(), dmc, siso);
//            dmc.zmienWartosci(tempD);
//            Y.clear();
//            siso.resetObiektu();
//            for (int i = 0; i < 100; i++) {
//                Y.add(siso.obliczKrok(dmc.policzOutput(siso.getAktualna())));
//            }
//            double blad = 0.0;
//            for (int i = 0; i < 50; i++) {
//                blad += Math.pow(Y.get(i) - dmc.getCel()[0], 2);
//            }
//            blad = blad / Y.size();
//            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
//            System.out.println("DMC : " + dmc.getLambda());
//            System.out.println("Zadane : " + dmc.getCel()[0]);
//            srednia +=blad;
//        }
//        srednia=srednia/3;
//        System.out.println("Sredni blad dla Nu = 18: " + srednia );


    }

}
