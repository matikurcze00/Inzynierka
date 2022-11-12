import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;

public class WlasnaFunkcja {
    public static void main(String[] args) {
        // write your code here
        System.out.println("Program to: 'WlasnaFunckja.java'\n");
        System.out.print("Dzielniki liczby to 42 - to:\n");

        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            double a = 42;
            double[] roots = eng.feval("liczenie", a);
            for (double e:  roots) {
                System.out.println(e);
            }
            eng.close();
        } catch (EngineException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
}
