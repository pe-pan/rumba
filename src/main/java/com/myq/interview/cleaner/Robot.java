package com.myq.interview.cleaner;

import com.myq.interview.cleaner.data.Input;
import com.myq.interview.cleaner.data.Output;
import com.myq.interview.cleaner.data.Position;
import com.myq.interview.cleaner.data.RobotPosition;
import com.myq.interview.cleaner.exc.LowBatteryException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Robot {
    private static final Logger log = LogManager.getLogger(Main.class);

    public static final int TURN_CONSUMPTION = 1;
    public static final int GO_ADVANCE_CONSUMPTION = 2;
    public static final int GO_BACK_CONSUMPTION = 3;
    public static final int CLEAN_CONSUMPTION = 5;

    private static final String[][] BACKOFF_STRATEGIES = {
            {"TR", "A", "TL"},
            {"TR", "A", "TR"},
            {"TR", "A", "TR"},
            {"TR", "B", "TR", "A"},
            {"TL", "TL", "A"},
    };

    private final RobotPosition position;       //current position
    private int battery;                        //current battery level
    private final String[] commands;
    private final Room room;

    public Robot(Input input) {
        this.position = input.getStart();
        this.battery = input.getBattery();
        this.commands = input.getCommands();
        this.room = new Room(input);
    }

    public Output work() {
        boolean all_commands = true;
        room.visited(new Position(position));
        try {
            for (String command : commands) {
                log.info(String.format("Command: %s; Robot at %d, %d; facing %s; battery: %d", command, position.getX(), position.getY(), position.getFacing(), battery));
                if (!run_command(command)) {
                    if (!triggerBackOffStrategy()) {
                        log.error(String.format("Robot stuck at %d, %d; facing %s; battery: %d", position.getX(), position.getY(), position.getFacing(), battery));
                        all_commands = false;
                        break;
                    }
                }
            }
        } catch (LowBatteryException e) {
            log.error(e);
        }
        if (all_commands) {
            log.info(String.format("Done; Robot at %d, %d; facing %s; battery: %d", position.getX(), position.getY(), position.getFacing(), battery));
        }
        return new Output(room.getVisited(), room.getCleaned(), position, battery); // is sorted already
    }

    public static final String COMMAND_CLEAN = "C";
    public static final String COMMAND_TURN_R = "TR";
    public static final String COMMAND_TURN_L = "TL";
    public static final String COMMAND_ADVANCE = "A";
    public static final String COMMAND_BACK = "B";
    public static final String[] COMMAND_ARR = {COMMAND_CLEAN, COMMAND_TURN_R, COMMAND_TURN_L, COMMAND_ADVANCE, COMMAND_BACK};
    public static final Set<String> COMMAND_SET = new HashSet<>(Arrays.asList(COMMAND_ARR));

    private boolean run_command(String command) throws LowBatteryException {
        switch (command) {
            case COMMAND_CLEAN:
                clean();
                return true;
            case COMMAND_TURN_R:
                turn(TURN_RIGHT);
                return true;
            case COMMAND_TURN_L:
                turn(TURN_LEFT);
                return true;
            case COMMAND_ADVANCE:
                return advance();
            case COMMAND_BACK:
                return backward();
        }
        throw new RuntimeException(String.format("Unknown command: %s", command));
    }

    private void clean() throws LowBatteryException {
        consume(CLEAN_CONSUMPTION);
        room.cleaned(new Position(position));   // set new position
        log.debug(String.format("Cleaning; battery level at %d", battery));
    }

    /**
     * Consumes given amount of energy.
     *
     * @param c how much to consume.
     */
    private void consume(int c) throws LowBatteryException {
        if (battery < c) {
            String message = String.format("Not enough energy; should be consumed %d; remaining %d", c, battery);
            log.debug(message);
            throw new LowBatteryException(message);
        }
        battery -= c;
        log.debug(String.format("Consumed %d; remaining %d", c, battery));
    }

    /**
     * Returns true if one of the strategies worked.
     *
     * @return false if none of the strategies worked.
     * @throws LowBatteryException when having not enough energy to finish
     */
    private boolean triggerBackOffStrategy() throws LowBatteryException {
        log.info("Triggering back off strategy");

        boolean worked = false;
        for (String[] strategy : BACKOFF_STRATEGIES) {
            log.info("Trying strategy: " + Arrays.toString(strategy));
            worked = true;
            for (String command : strategy) {
                if (!run_command(command)) {
                    worked = false;
                    break;
                }
            }
            if (worked) {
                log.debug("Backoff strategy worked!!");
                break;
            }
        }
        return worked;
    }

    /**
     * Returns true when robot moved.
     *
     * @param consumption how much energy will be consumed upon the move
     * @param front       1 for going forward; -1 for going backwards
     * @return false when obstacle is preventing the robot to move
     * @throws LowBatteryException when not enough energy to finish the move.
     */
    private boolean move(int consumption, int front) throws LowBatteryException {
        consume(consumption);
        Position nextPosition = getNextPosition(front);    // returns a new instance
        if (!room.isObstacle(nextPosition)) {
            room.visited(nextPosition);
            position.setPosition(nextPosition);
            log.debug(String.format("Moving to new position %d, %d", position.getX(), position.getY()));
            return true;
        } else {
            log.debug(String.format("Can't move to %d, %d", nextPosition.getX(), nextPosition.getY()));
            return false;
        }
    }

    public static final int GO_AHEAD = 1;
    public static final int GO_BACK = -1;

    /**
     * Returns true if the move is possible (no obstacle).
     */
    private boolean advance() throws LowBatteryException {
        return move(GO_ADVANCE_CONSUMPTION, GO_AHEAD);
    }

    /**
     * Returns true if the move is possible (no obstacle).
     */
    private boolean backward() throws LowBatteryException {
        return move(GO_BACK_CONSUMPTION, GO_BACK);
    }

    public static final String DIRECTION_N = "N";
    public static final String DIRECTION_E = "E";
    public static final String DIRECTION_S = "S";
    public static final String DIRECION_W = "W";
    public static final String[] DIRECTION = {DIRECTION_N, DIRECTION_E, DIRECTION_S, DIRECION_W};
    public static final Map<String, Integer> DIRECTION_MAP = new HashMap<>(DIRECTION.length);

    static {
        for (int i = 0; i < DIRECTION.length; i++) {
            DIRECTION_MAP.put(DIRECTION[i], i);
        }
    }

    private void turn(int turn) throws LowBatteryException {
        log.debug(String.format("Turning %d", turn));
        consume(TURN_CONSUMPTION);

        position.setFacing(DIRECTION[(DIRECTION_MAP.get(position.getFacing()) + DIRECTION_MAP.size() + turn) % DIRECTION_MAP.size()]);
        log.debug(String.format("Turned to: %s", position.getFacing()));
    }

    private static final int TURN_RIGHT = 1;
    private static final int TURN_LEFT = -1;

    /**
     * Returns the front or back position.
     * front = 1 is for FRONT; -1 is for BACK
     *
     * @return next position
     */
    private Position getNextPosition(int front) {
        switch (position.getFacing()) {
            case DIRECTION_N:
                return new Position(position.getX(), position.getY() - front);
            case DIRECTION_E:
                return new Position(position.getX() + front, position.getY());
            case DIRECTION_S:
                return new Position(position.getX(), position.getY() + front);
            case DIRECION_W:
                return new Position(position.getX() - front, position.getY());
        }
        throw new RuntimeException("Unknown position: " + position.getFacing());  // never happens
    }
}
