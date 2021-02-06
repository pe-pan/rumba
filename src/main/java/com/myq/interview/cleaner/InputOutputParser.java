package com.myq.interview.cleaner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myq.interview.cleaner.data.Input;
import com.myq.interview.cleaner.data.Output;
import com.myq.interview.cleaner.exc.InvalidInputException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InputOutputParser {
    private static final Logger log = LogManager.getLogger(InputOutputParser.class);

    private final File inputFile;
    private final File outputFile;

    InputOutputParser(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public static InputOutputParser parseArguments(String[] args) {
        if (args.length != 2) {
            log.error(String.format("Usage: %s <source.json> <result.json>\nProvide exactly 2 parameters!", Main.APP_NAME));
            return null;
        }

        File inputFile = new File(args[0]);

        if (!inputFile.exists()) {
            log.error(String.format("The input file (%s) does not exist; please, provide a valid file path.", inputFile.getAbsolutePath()));
            return null;
        }
        if (!inputFile.isFile()) {
            log.error(String.format("The input file (%s) is not a valid file; please, provide path to a valid file.", inputFile.getAbsolutePath()));
            return null;
        }

        File outputFile = new File(args[1]);
        if (outputFile.exists()) {
            if (!outputFile.isFile()) {
                log.error(String.format("The output file (%s) already exists and can't be overwritten.", outputFile.getAbsolutePath()));
                return null;
            }
            log.warn(String.format("The output file (%s) will be overwritten.", outputFile.getAbsolutePath()));
        }

        log.info(String.format("Input file: %s", inputFile.getAbsolutePath()));
        log.info(String.format("Output file: %s", outputFile.getAbsolutePath()));
        return new InputOutputParser(inputFile, outputFile);
    }

    /**
     * @return Object representation of the JSON file (input).
     * @throws IOException when the input cannot be deserialized from JSON or is missing mandatory data.
     */
    public Input getInput() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        Input input = mapper.readValue(inputFile, Input.class);
        verifyInputValidity(input);
        return input;
    }

    /**
     * Validates the input for validity.
     *
     * @throws InvalidInputException if the input is not valid.
     */
    private void verifyInputValidity(Input input) throws InvalidInputException {
        try {
            if (input.getBattery() == null) {
                throw new InvalidInputException("No battery given.");
            }
            if (input.getBattery() < 0) {
                throw new InvalidInputException(String.format("Robot's battery can't be negative. Now it is %d.", input.getBattery()));
            }
            if (input.getCommands() == null) {
                throw new InvalidInputException("No list of commands given.");
            }
            if (input.getMap() == null) {
                throw new InvalidInputException("No room map given.");
            }
            if (input.getStart() == null) {
                throw new InvalidInputException("No robot's starting position given.");
            }

            Integer x = input.getStart().getX();
            if (x == null) {
                throw new InvalidInputException("Robot's starting X position not given.");
            }
            Integer y = input.getStart().getY();
            if (y == null) {
                throw new InvalidInputException("Robot's starting Y position not given.");
            }
            if (y < 0 || y >= input.getMap().length || x < 0 || x >= input.getMap()[0].length) {
                throw new InvalidInputException(String.format("Robot can't stand out of the room; now it's on %d, %d.", x, y));
            }

            try {
                for (Character[] lines : input.getMap()) {
                    for (Character c : lines) {
                        if (c != Room.NO_ROOM_NULL_CHAR && c != Room.SPACE_CHAR && c != Room.COLUMN_CHAR) {
                            throw new InvalidInputException(String.format(" Invalid character in the map: '%s'.", c));
                        }
                    }
                }
            } catch (InvalidInputException e) {
                throw e;
            } catch (Exception e) {
                throw new InvalidInputException("Invalid room map.", e);
            }
            if (Robot.DIRECTION_MAP.get(input.getStart().getFacing()) == null) {
                throw new InvalidInputException(String.format("Unknown facing string: '%s'.", input.getStart().getFacing()));
            }
            try {
                for (String command : input.getCommands()) {
                    if (!Robot.COMMAND_SET.contains(command)) {
                        throw new InvalidInputException(String.format("Unknown command: '%s'.", command));
                    }
                }
            } catch (InvalidInputException e) {
                throw e;
            } catch (Exception e) {
                throw new InvalidInputException("Invalid list of commands.", e);
            }
            Character c = input.getMap()[y][x];
            if (c != Room.SPACE_CHAR) {
                throw new InvalidInputException(String.format("Starting robot's position should be on '%s' but it is standing on '%s' instead.", Room.SPACE_CHAR, c));
            }
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            // just in case
            throw new InvalidInputException("Invalid input data.", e);
        }
    }


    /**
     * @param output Object representation of the JSON file (output).
     * @throws IOException when the output cannot be serialized to JSON.
     */
    public void writeOutput(Output output) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new FileWriter(outputFile), output);
    }
}
