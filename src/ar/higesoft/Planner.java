package ar.higesoft;

import java.util.HashMap;

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
public class Planner {

    public static final int UP = 4;
    public static final int DOWN = 3;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private HashMap<String, Integer> theories;
    public Planner() {
        theories = new HashMap<>();
    }

    public int getNextAction(char[] status) {

        String s_status = new String(status);

        if (!theories.containsKey(s_status)) {
            theories.put(s_status, UP);
        } else {
            theories.put(s_status, DOWN);
        }

        System.out.println(s_status);

        return theories.get(s_status);
    }
}
