// src/main/java/com/kbf/employee/exception/InvalidDataException.java
package com.kbf.employee.exception;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }

}