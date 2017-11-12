package ar.higesoft;

import ar.fi.uba.celdas.Perception;
import core.game.StateObservation;
import tools.Vector2d;
import tools.pathfinder.Node;
import tools.pathfinder.PathFinder;

import java.util.ArrayList;

import static ar.higesoft.Planner.*;

/**
 * Copyright 2017
 * Gaston Martinez gaston.martinez.90@gmail.com
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses
 */
public class WorldStatus {

    final public static int STATUS_SIZE = 16;


    private int player_row;
    private int player_column;
    private int key_row;
    private int key_column;
    private int door_row;
    private int door_column;
    private boolean has_key;
    private int direction;
    private int points;

    private Perception perception;

    private char facing_a = '0';
    private char facing_b = '0';

    private char goalCol = '0';
    private char goalRow = '0';

    private int action = 0;

    private boolean playerAlive = true;

    private PathFinder paths;
    private StateObservation state;

    public WorldStatus(StateObservation stateObs) {

        has_key = false;
        Perception world = new Perception(stateObs);
        ArrayList<Integer> obstacles = new ArrayList<>();
        obstacles.add(0);
        paths = new PathFinder(obstacles);

        for (int i = 0; i < world.getLevelHeight(); i++) {
            for (int j = 0; j < world.getLevelWidth(); j++) {

                char item = world.getAt(i, j);

                switch (item) {
                    case 'A':
                        player_column = j;
                        player_row = i;
                        break;
                    case '+':
                        key_column = j;
                        key_row = i;
                        break;
                    case 'g':
                        door_column = j;
                        door_row = i;
                        break;
                }
            }
        }
    }

    public boolean isPlayerAlive() {
        return playerAlive;
    }

    private char getAt(int row, int column) {
        if ((column >= 0 && column < perception.getLevelWidth()) && (row >= 0 && row < perception.getLevelHeight())) {
            return perception.getAt(row, column);
        }

        return 'w';

    }

    public int getDirection() {
        return direction;
    }

    public int getPlayer_column() {
        return player_column;
    }

    public int getDoor_column() {
        return door_column;

    }

    public boolean getHasKey() {
        return has_key;
    }

    public int getKey_row() {

        return key_row;
    }

    public int getDoor_row() {
        return door_row;
    }

    public int getKey_column() {
        return key_column;
    }

    public int getPlayer_row() {
        return player_row;
    }

    public String getWorldStatus() {
        char status[] = new char[STATUS_SIZE];

        int row = getPlayer_row();
        int col = getPlayer_column();

        status[0] = getAt(row - 2, col);

        status[1] = getAt(row - 1, col - 1);
        status[2] = getAt(row - 1, col);
        status[3] = getAt(row - 1, col + 1);

        status[4] = getAt(row, col - 2);
        status[5] = getAt(row, col - 1);

        status[6] = getAt(row, col + 1);
        status[7] = getAt(row, col + 2);

        status[8] = getAt(row + 1, col - 1);
        status[9] = getAt(row + 1, col);
        status[10] = getAt(row + 1, col + 1);

        status[11] = getAt(row + 2, col);

        status[12] = facing_a;
        status[13] = facing_b;

        status[14] = goalCol;
        status[15] = goalRow;

        if (has_key) {
            return new String(status).replace('g', '+');
        } else {
            return new String(status).replace('g', 'w');
        }
    }

    public void updateWorld(StateObservation stateObs) {
        paths.run(stateObs);
        this.state = stateObs;

        Perception world = new Perception(stateObs);
        perception = world;

        Vector2d orientation = stateObs.getAvatarOrientation();
        double x = orientation.x;
        double y = orientation.y;

        playerAlive = !(x == -1 && y == -1);

        if (!playerAlive) {
            System.out.println("I'm Dead");
        }

        facing_a = '0';
        if (x != 0) {
            facing_a = (x > 0) ? 'f' : 'b';
        }

        facing_b = '0';
        if (y != 0) {
            facing_b = (y > 0) ? 'f' : 'b';
        }

        updatePlayerKey(world);

        goalCol = '0';
        goalRow = '0';

        if (has_key) {
            if (player_column != door_column) {
                goalCol = (player_column < door_column) ? 'f' : 'b';
            }
            if (player_row != door_row) {
                goalRow = (player_row < door_row) ? 'f' : 'b';
            }
        } else {
            if (player_column != key_column) {
                goalCol = (player_column < key_column) ? 'f' : 'b';
            }
            if (player_row != key_row) {
                goalRow = (player_row < key_row) ? 'f' : 'b';
            }
        }
    }

    private void updatePlayerKey(Perception world) {
        for (int i = 0; i < world.getLevelHeight(); i++) {
            for (int j = 0; j < world.getLevelWidth(); j++) {

                char item = world.getAt(i, j);

                switch (item) {
                    case '?':
                    case 'A':
                        player_column = j;
                        player_row = i;

                        if (player_column == key_column && player_row == key_row && !has_key) {
                            has_key = true;
                        }
                        return;
                }

            }
        }
    }

    public int getDistanceToGoal() {
        ArrayList<Node> list;
        if (has_key) {
            list = paths.getPath(new Vector2d(player_column, player_row), new Vector2d(door_column, door_row));
        } else {
            list = paths.getPath(new Vector2d(player_column, player_row), new Vector2d(key_column, key_row));
        }

        if (list != null) {
            return list.size();
        }

        return 100;
    }

    public int getDistanceToGoalFrom(int direction) {
        int playcol = player_column;
        int playrow = player_row;

        switch (direction) {
            case UP:
                playrow -= 1;
                break;
            case DOWN:
                playrow += 1;
                break;
            case LEFT:
                playcol -= 1;
                break;
            case RIGHT:
                playcol += 1;
                break;
        }


        int thisGoalRow;
        int thisGoalCol;
        if (has_key) {
            thisGoalCol = door_column;
            thisGoalRow = door_row;
        } else {
            thisGoalCol = key_column;
            thisGoalRow = key_row;
        }

        ArrayList<Node> list = paths.getPath(new Vector2d(playcol, playrow), new Vector2d(thisGoalCol, thisGoalRow));

        if (list != null) {
            return list.size();
        }

        if (thisGoalCol == playcol && thisGoalRow == playrow) {
            return 0;
        }

        return 100;
    }

}
