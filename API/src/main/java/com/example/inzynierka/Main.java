package com.example.inzynierka;


import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        ////TransferFunction
//        val obj = new TransmitancjaCiagla(1, 5, 0, 0, 0, 1, 0, 0, 0, 0.5);
//        val y = new double [100];
//        List<Double> u = new ArrayList();
//        Collections.fill(u,0.0);
//        for (int i = 0; i < 100; i++) {
//            val calcOut = obj.obliczKrok(u);
//            y[i] = calcOut;
//        }


        ////MIMO

//        ObjectMapper objectMapper = new ObjectMapper();
//        Integer[] PV = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("PV"), Integer[].class);
//        ParObiektMIMO[] Obiekty = objectMapper.treeToValue(objectMapper.readTree(new FileInputStream("src/main/java/com/example/inzynierka/ObiektMIMO.json")).path("ParObiektMIMO"), ParObiektMIMO[].class);
//        System.out.println(Obiekty[0]);
//        System.out.println(Obiekty[1]);
//        MIMO obiekt  = new MIMO(Obiekty);
//        ZbiorPID regulator = new ZbiorPID(obiekt,PV, 3.0, par);
//        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(1000, 400, 20, 0.5,0.8);
//        regulator.zmienWartosci(GA.dobierzWartosci(regulator.liczbaZmiennych(), regulator, obiekt));
//        double[] celTemp = {200.0,0.0};
//        zbior.setCel(celTemp);
//        List<double[]> Y = new ArrayList<>();
//        obiekt.resetObiektu();
//        for(int k = 0; k<60; k++)
//        {
//                Y.add(obiekt.obliczKrok(zbior.policzOutput(obiekt.getAktualne())));
//        }
//        System.out.println("ok");
//        //DMC - MIMO
//        double[] celTemp = {550.0,0.0};
//        List<double[]> Y = new ArrayList<>();
//        double[] tempLambda = {0.05,0.05};
////        DMC regulator = new DMC(3, tempLambda, obiekt, celTemp, 3, 10);
//        regulator.setCel(celTemp);
//        obiekt.resetObiektu();
//        for(int k = 0; k<40; k++)
//        {
//                Y.add(obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne())));
//
//        }
//        celTemp = new double[]{550.0,100.0};
//        regulator.setCel(celTemp);
//        for(int k = 0; k<40; k++)
//        {
//            Y.add(obiekt.obliczKrok(regulator.policzOutput(obiekt.getAktualne())));
//
//        }
//        System.out.println(Y.get(35)[0]);
//        System.out.println(Y.get(35)[1]);
//
//        System.out.println(regulator.getCel()[0]);
//        System.out.println(regulator.getCel()[1]);


        ////SISO

//        SISO siso = new SISO(10, 5, 1, 2, 1, 10, 5, 0, 0, 0.5, 100.0);
//        List<Double> Y = new ArrayList<>();
//        double[] resetD = {0};
//        AlgorytmEwolucyjny GA = new AlgorytmEwolucyjny(100, 400, 10, 0.3,0.2);
////        DMC dmc = new DMC(4,0.1, siso, siso.getYMax()/2, 3, 10);
//
////        dmc.setCel(new double[]{siso.getYMax()/2});
//        PID pid = new PID(0,0,0,1,new double[]{siso.getYMax()/2},3,100);
////        dmc.zmienWartosci(resetD);
//            double[] tempD = GA.dobierzWartosci(pid.liczbaZmiennych(), pid, siso);
//            pid.zmienWartosci(tempD);
//            Y.clear();
//            siso.resetObiektu();
//            for (int i = 0; i < 100; i++) {
//                Y.add(siso.obliczKrok(pid.policzOutput(siso.getAktualna())));
//            }
//            double blad = 0.0;
//            for (int i = 0; i < 50; i++) {
//                blad += Math.pow(Y.get(i) - pid.getCel()[0], 2);
//            }
//            blad = blad / Y.size();
//            System.out.println("BLAD:" + blad);
//            System.out.println("Y : " + Y);
////            System.out.println("DMC : " + dmc.getLambda());
//            System.out.println("Zadane : " + pid.getCel()[0]);


    }

}
