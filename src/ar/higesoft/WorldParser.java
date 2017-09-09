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

    public static int UP = 0;
    public static int DOWN = 0;
    public static int LEFT = 0;
    public static int RIGHT = 0;
    private int player_row;
    private int player_column;
    private int key_row;
    private int key_column;
    private int door_row;
    private int door_column;
    private boolean has_key;
    private ArrayList<ArrayList<Integer>> steps;

    public WorldParser(Perception world) {

        steps = new ArrayList<>(world.getLevelHeight());

        has_key = false;

        for (int i = 0; i < world.getLevelHeight(); i++) {
            steps.add(new ArrayList<>(world.getLevelWidth()));
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

    public int getPlayer_column() {
        return player_column;
    }

    public int getDoor_column() {
        return door_column;

    }

    public boolean isHas_key() {
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
        for (int i = 0; i < world.getLevelHeight(); i++) {
            for (int j = 0; j < world.getLevelWidth(); j++) {

                char item = world.getAt(i, j);

                switch (item) {
                    case 'A':
                        player_column = j;
                        player_row = i;
                        return;
                }

            }
        }
    }

    public void move(int direction) {
        if (direction == UP)
            player_row -= 1;

        if (direction == DOWN)
            player_row += 1;

        if (direction == LEFT)
            player_column -= 1;

        if (direction == RIGHT)
            player_column += 1;

        steps.get(player_column).set(player_row, steps.get(player_column).get(player_row) + 1);

        if (player_column == key_column && player_row == key_row) {
            has_key = true;
        }


        System.out.printf("[%d,%d]", player_row, player_column);
    }
}
