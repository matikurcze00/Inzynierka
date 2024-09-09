package com.example.inzynierka.controllers;

import Jama.Matrix;
import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractMPCController extends AbstractController {
    protected Integer D;
    protected Integer N;
    protected Integer Nu;
    protected Matrix M;
    protected List<Double> Lambda;
    protected Matrix K;
    List<List<Double>> S;

}
