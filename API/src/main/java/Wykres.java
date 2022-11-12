import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import java.util.concurrent.ExecutionException;

public class Wykres extends Application{
    @Override public void start(Stage stage){

        System.out.println("Program to: 'Wykres.java'\n");
        //System.out.print("Pierwiastki liczb: 2, 4, 9 - to:\n");


        try {
            MatlabEngine eng = MatlabEngine.startMatlab();

            eng.evalAsync("X = linspace(10,100,10);");
            eng.evalAsync("Y = X*1.5;");
            double[] X  = eng.getVariable("X");
            double[] Y = eng.getVariable("Y");
            eng.evalAsync("Y2 = X*4;");
            double[] Y2 = eng.getVariable("Y2");
            for (double e:  Y) {
                System.out.println(e);
            }
            eng.close();


            stage.setTitle("Wykres na podstawie danych z Matlab");
            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("X");
            final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
            lineChart.setTitle("Wykres na podstawie adnych z Matlab");

            XYChart.Series series = new XYChart.Series();
            series.setName("1.5*x");

            for(int i=0; i<10; i++){
                series.getData().add(new XYChart.Data(X[i], Y[i]));
            }
            XYChart.Series series2 = new XYChart.Series();
            series2.setName("4*x");
            for(int i=0; i<10; i++){
                series2.getData().add(new XYChart.Data(X[i], Y2[i]));
            }
            Scene scene  = new Scene(lineChart,800,600);
            lineChart.getData().add(series);
            lineChart.getData().add(series2);
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
