import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;

public class Dzielnik {
    public static void main(String[] args) {
        // write your code here
        System.out.println("Program to: 'Dzielnik.java'\n");
        System.out.print("Najwiekszy wspolny dzielnik liczb: 40, 60 - to:\n");

        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            double[] a = {40,60};
            Object[] dzielnik = eng.feval(3,"gcd", a[0],a[1]);

                System.out.println(dzielnik[0]);

            //}
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
