import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;


public class Pierwiastek {

    public static void main(String[] args) {
        // write your code here
        System.out.println("Program to: 'Pierwiastek.java'\n");
        System.out.print("Pierwiastki liczb: 2, 4, 9 - to:\n");

        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            double[] a = {2.0 ,4.0, 9.0};
            double[] roots = eng.feval("sqrt", a);
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
