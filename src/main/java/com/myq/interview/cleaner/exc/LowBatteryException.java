package com.myq.interview.cleaner.exc;

/**
 * Thrown when the robot does not have not enough energy to finish.
 */
public class LowBatteryException extends Exception {
    public LowBatteryException(String message) {
        super(message);
    }

    public LowBatteryException(String message, Throwable cause) {
        super(message, cause);
    }
}
