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
public class WorldStatus {


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

    public WorldStatus(Perception world) {

        steps = new ArrayList<>(0);

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

    public char[] getWorldStatus() {
        char status[] = new char[12];

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

        return status;
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

}
