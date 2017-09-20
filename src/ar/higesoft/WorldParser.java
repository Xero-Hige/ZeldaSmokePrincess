package ar.higesoft;

import ar.fi.uba.celdas.Perception;

import java.util.ArrayList;

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
public class WorldParser {

    public static final int UP = 4;
    public static final int DOWN = 3;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private int player_row;
    private int player_column;
    private int key_row;
    private int key_column;
    private int door_row;
    private int door_column;
    private boolean has_key;
    private ArrayList<ArrayList<Integer>> steps;
    private int direction;

    private Perception perception = null;

    private int action = 0;

    public WorldParser(Perception world) {

        steps = new ArrayList<>(0);
        direction = LEFT;

        has_key = false;

        for (int i = 0; i < world.getLevelHeight(); i++) {
            steps.add(new ArrayList<>(0));
        }

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

                switch (item) {
                    case 'A':
                        steps.get(i).add(1);
                        break;
                    case 'g':
                    case 'w':
                        steps.get(i).add(Integer.MAX_VALUE);
                        break;
                    default:
                        steps.get(i).add(0);
                        break;
                }
            }
        }
    }

    public char getFacingElement() {
        switch (direction) {
            case UP:
                return getAtPlayerUp();
            case DOWN:
                return getAtPlayerDown();
            case LEFT:
                return getAtPlayerLeft();
            case RIGHT:
                return getAtPlayerRight();
            default:
                return '-';
        }
    }

    public int getAction() {
        System.out.println("Facing:");
        System.out.println(getFacingElement());
        System.out.println(perception.getAt(key_row, key_column));
        System.out.println(perception);

        return action;
    }

    public void setAction(int action) {
        this.action = action;
        switch (action) {
            case 1:
                direction = LEFT;
                break;
            case 2:
                direction = RIGHT;
                break;
            case 3:
                direction = DOWN;
                break;
            case 4:
                direction = UP;
        }
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


    public char getAtPlayerLeft() {
        if (perception == null)
            return '.';
        return perception.getAt(player_row, player_column - 1);
    }

    public char getAtPlayerRight() {
        if (perception == null)
            return '.';
        return perception.getAt(player_row, player_column + 1);
    }

    public char getAtPlayerUp() {
        if (perception == null)
            return '.';
        return perception.getAt(player_row - 1, player_column);
    }

    public char getAtPlayerDown() {
        if (perception == null)
            return '.';
        return perception.getAt(player_row + 1, player_column);
    }


    public boolean getEnemyUp() {
        return getAtPlayerUp() == '2';
    }

    public boolean getCanGoUp() {
        return (!getEnemyUp()) && (getAtPlayerUp() != 'w');
    }


    public boolean getEnemyDown() {
        return getAtPlayerDown() == '2';
    }

    public boolean getCanGoDown() {
        return (!getEnemyDown()) && (getAtPlayerDown() != 'w');
    }


    public boolean getEnemyLeft() {
        return getAtPlayerLeft() == '2';
    }

    public boolean getCanGoLeft() {
        return (!getEnemyLeft()) && (getAtPlayerLeft() != 'w');
    }


    public boolean getEnemyRight() {
        return getAtPlayerRight() == '2';
    }

    public boolean getCanGoRight() {
        return (!getEnemyRight()) && (getAtPlayerRight() != 'w');
    }

    public boolean getShouldGoUp() {
        return getTimesVisitedUp() <= getTimesVisitedLeft() &&
                getTimesVisitedUp() <= getTimesVisitedRight() &&
                getTimesVisitedUp() <= getTimesVisitedDown();
    }

    public boolean getShouldGoDown() {
        return getTimesVisitedDown() <= getTimesVisitedLeft() &&
                getTimesVisitedDown() <= getTimesVisitedRight() &&
                getTimesVisitedDown() <= getTimesVisitedUp();
    }

    public boolean getShouldGoRight() {
        return getTimesVisitedRight() <= getTimesVisitedLeft() &&
                getTimesVisitedRight() <= getTimesVisitedUp() &&
                getTimesVisitedRight() <= getTimesVisitedDown();
    }

    public boolean getShouldGoLeft() {
        return getTimesVisitedLeft() <= getTimesVisitedUp() &&
                getTimesVisitedLeft() <= getTimesVisitedRight() &&
                getTimesVisitedLeft() <= getTimesVisitedDown();
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

    public void updateWorld(Perception world) {

        perception = world;

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
                            steps.get(door_row).set(door_column, 0);

                            for (int x = 0; x < world.getLevelHeight(); x++) {
                                for (int y = 0; y < world.getLevelWidth(); y++) {
                                    if (steps.get(x).get(y) < Integer.MAX_VALUE) {
                                        steps.get(x).set(y, 0);
                                    }
                                }
                            }
                        }

                        steps.get(i).set(j, steps.get(i).get(j) + 1);
                        return;
                }

            }
        }
    }

    private int getTimesVisitedUp() {
        return steps.get(player_row - 1).get(player_column);
    }

    private int getTimesVisitedDown() {
        return steps.get(player_row + 1).get(player_column);
    }

    private int getTimesVisitedLeft() {
        return steps.get(player_row).get(player_column - 1);
    }

    private int getTimesVisitedRight() {
        return steps.get(player_row).get(player_column + 1);
    }

}
