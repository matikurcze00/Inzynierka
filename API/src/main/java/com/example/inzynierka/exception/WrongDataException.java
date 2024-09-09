package com.example.inzynierka.exception;

public class WrongDataException extends RuntimeException {

    public WrongDataException(String message) {
        super(message);
    }

    public static WrongDataException wrongObjectException(String name) {
        return new WrongDataException(String.format("There was problem for creating object for controller: %s", name));
    }

    public static WrongDataException wrongControllerException(String name) {
        return new WrongDataException(String.format("There is no controller named: %s", name));
    }
}
