package com.example.inzynierka.exception;

import lombok.Data;


@Data
public class ErrorInfo {

    private String url;
    private String method;
    private String message;
    private String exception;
}
