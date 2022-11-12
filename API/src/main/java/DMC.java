import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.concurrent.ExecutionException;

import static javafx.application.Application.launch;

public class DMC extends Application {
    @Override
    public void start(Stage stage) {
        // write your code here
        System.out.println("Program to: 'DMC.java'\n");


        try {
            MatlabEngine eng = MatlabEngine.startMatlab();
            double a = 42;
            eng.evalAsync("[Y_zad, Y_wyjsc, E] = Model_DMC(3.15,[2.36*4.21 2.36+4.21 1])");
            double[] Y_zad = eng.getVariable("Y_zad");
            double[] Y_wyjsc = eng.getVariable("Y_wyjsc");
            double E = eng.getVariable("E");

            System.out.println("Błąd kwadratowy funkcji to: "+E);

            eng.close();

            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("X");
            yAxis.setLabel("Y");

            final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
            XYChart.Series series1 = new XYChart.Series();
            series1.setName("Y Zadane");
            for (int i = 0; i < Y_zad.length; i++) {
                series1.getData().add(new XYChart.Data(i, Y_zad[i]));
            }
            lineChart.getData().add(series1);

            XYChart.Series series2 = new XYChart.Series();
            series2.setName("Y wyjsciowe");
            for (int i = 0; i < Y_wyjsc.length; i++) {
                series2.getData().add(new XYChart.Data(i, Y_wyjsc[i]));
            }
            lineChart.getData().add(series2);

            Scene scene = new Scene(lineChart, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (EngineException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        // write your code here

        launch(args);


    }
}
