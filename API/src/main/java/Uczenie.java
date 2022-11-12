import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.ExecutionException;

public class Uczenie extends Application {
    @Override
    public void start(Stage stage) {

        System.out.println("Program to: 'Uczenie.java'\n");
        //System.out.print("Pierwiastki liczb: 2, 4, 9 - to:\n");


        try {
            MatlabEngine eng = MatlabEngine.startMatlab();

            eng.evalAsync("X = linspace(10,100,10);");
            eng.evalAsync("Y = X*1.5;");
            double[] X = eng.getVariable("X");
            double[] Y = eng.getVariable("Y");
            eng.evalAsync("Y2 = X*4;");
            double[] Y2 = eng.getVariable("Y2");

            eng.evalAsync("load \'C:\\Users\\HP\\Desktop\\Inżynierka\\danestat41.txt\';");
            eng.evalAsync("u = danestat41(:,1);");
            eng.evalAsync("y = danestat41(:,2);");
            eng.evalAsync("u_wer = u(1:2:end);");
            eng.evalAsync("u_ucz = u(2:2:end);");
            eng.evalAsync("y_wer = y(1:2:end);");
            eng.evalAsync("y_ucz = y(2:2:end);");

            double[] u_wer = eng.getVariable("u_wer");
            double[] u_ucz = eng.getVariable("u_ucz");
            double[] y_wer = eng.getVariable("y_wer");
            double[] y_ucz = eng.getVariable("y_ucz");

            eng.evalAsync("[Model_sorted1, Model_sorted2, blad_ucz, blad_wer] = Model_nieliniowy(u_wer,u_ucz,y_wer,y_ucz);");
            double[] Model_x = eng.getVariable("Model_sorted1");
            double[] Model_y = eng.getVariable("Model_sorted2");
            double blad_ucz = eng.getVariable("blad_ucz");
            double blad_wer = eng.getVariable("blad_wer");

            eng.close();

            System.out.print("Blad kwadratowy na zbiorze uczacym to: " + blad_ucz + "\n");
            System.out.print("Blad kwadratowy na zbiorze weryfikujacym to: " + blad_wer);

            stage.setTitle("Wykres na podstawie danych z Matlab");
            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("X");
            yAxis.setLabel("Y");

            //LineChart
            final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
            // lineChart.setLegendVisible(false);
            lineChart.setAnimated(false);


            XYChart.Series series = new XYChart.Series();
            series.setName("Wykres nieliniowy");
            for (int i = 0; i < Model_x.length; i++) {
                series.getData().add(new XYChart.Data(Model_x[i], Model_y[i]));
            }
            lineChart.getData().add(series);


            //xy chart
            final ScatterChart<Number, Number> sc = new ScatterChart<Number, Number>(xAxis, yAxis);
            //sc.setLegendVisible(false);
            sc.setAnimated(false);


            XYChart.Series series2 = new XYChart.Series();
            series2.setName("Zestaw uczacy");
            for (int i = 0; i < u_ucz.length; i++) {
                series2.getData().add(new XYChart.Data(u_ucz[i], y_ucz[i]));
            }

            XYChart.Series series3 = new XYChart.Series();
            series3.setName("Zestaw weryfikujacy");
            for (int i = 0; i < u_wer.length; i++) {
                series3.getData().add(new XYChart.Data(u_wer[i], y_wer[i]));
            }
            sc.getData().addAll(series2, series3);
            sc.setOpacity(0.5);


            StackPane root = new StackPane();

            root.getChildren().add(lineChart);
            root.getChildren().add(sc);
            Scene scene = new Scene(root, 800, 600);
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
