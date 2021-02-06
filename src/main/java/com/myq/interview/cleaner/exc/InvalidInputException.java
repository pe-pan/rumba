package com.myq.interview.cleaner.exc;

import java.io.IOException;

/**
 * Thrown when a not valid input is provided.
 */
public class InvalidInputException extends IOException {

    public InvalidInputException() {
    }

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputException(Throwable cause) {
        super(cause);
    }
}
