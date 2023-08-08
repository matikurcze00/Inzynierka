package com.example.inzynierka.tunningControllers;

import Jama.Matrix;
import lombok.Data;

import java.util.List;

@Data
public abstract class ControllerTunningMPC extends ControllerTunning {
    protected Integer D;
    protected Integer N;
    protected Integer Nu;
    protected Matrix M;
    List<List<Double>> S;
    protected List<Double> Lambda;
    protected Matrix K;

}
