package com.myq.interview.cleaner;

import com.myq.interview.cleaner.data.Input;
import com.myq.interview.cleaner.data.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Main {
    public static final String APP_NAME = "cleaning_robot";
    private static final Logger log = LogManager.getLogger(Main.class);
    public static final int ERR_INVALID_PARAMETERS = 1;
    public static final int ERR_INVALID_INPUT = 2;
    public static final int ERR_INVALID_OUTPUT = 3;

    public static void main(String[] args) {
        InputOutputParser parser = InputOutputParser.parseArguments(args);
        if (parser == null) {
            System.exit(ERR_INVALID_PARAMETERS);
        }
        Input input = null;
        try {
            input = parser.getInput();
        } catch (IOException e) {
            log.error(String.format("Can't parse the input JSON from %s.", args[0]), e);
            System.exit(ERR_INVALID_INPUT);
        }

        Robot robot = new Robot(input);
        Output output = robot.work();

        try {
            parser.writeOutput(output);
        } catch (IOException e) {
            log.error(String.format("Can't write the output JSON to %s", args[1]), e);
            System.exit(ERR_INVALID_OUTPUT);
        }
    }
}
