package com.myq.interview.cleaner;

import com.myq.interview.cleaner.data.Input;
import com.myq.interview.cleaner.data.Position;

import java.util.Set;
import java.util.TreeSet;

public class Room {

    private final Character[][] map;
    private final Set<Position> visited;
    private final Set<Position> cleaned;

    public Room(Input input) {
        map = input.getMap();
        visited = new TreeSet<>();  // if no need to be sorted, use HashSet
        cleaned = new TreeSet<>();
    }

    public static final Character NO_ROOM_NULL_CHAR = null;
    public static final Character COLUMN_CHAR = 'C';
    public static final Character SPACE_CHAR = 'S';

    /**
     * Returns true if the given position is out of map (wall) or there is a column on the position.
     *
     * @return false if robot can move to the given position
     */
    public boolean isObstacle(Position position) {
        // getting out of the map
        if (position.getX() < 0) return true;
        if (position.getY() < 0) return true;
        if (position.getY() >= map.length) return true;
        if (position.getX() >= map[position.getY()].length) return true;
        // is it out of room or is there a column?
        Character c = map[position.getY()][position.getX()];
        return c != SPACE_CHAR;
    }

    public void visited(Position position) {
        visited.add(position);
    }

    public void cleaned(Position position) {
        cleaned.add(position);
    }

    public Set<Position> getVisited() {
        return visited;
    }

    public Set<Position> getCleaned() {
        return cleaned;
    }

}
