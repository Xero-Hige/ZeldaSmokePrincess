package ar.higesoft;

import java.util.LinkedList;
import java.util.Random;

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

    private String previous_status;
    private String predicted_status;
    private Theory applied_theory;
    private LinkedList<Theory> theories;

    public Planner() {
        theories = new LinkedList<>();
        predicted_status = "------------";
        previous_status = "------------";
        applied_theory = null;
    }

    public int getNextAction(char[] status) {

        String s_status = new String(status);

        if (applied_theory != null) {

            boolean wrong = false;

            for (int i = 0; i < predicted_status.length(); i++) {
                if (predicted_status.charAt(i) == '-') {
                    continue;
                }

                if (predicted_status.charAt(i) != s_status.charAt(i)) {
                    wrong = true;
                    break;
                }
            }

            if (wrong) {
                Theory new_t = new Theory(applied_theory.causes, applied_theory.action, s_status, 1);
                theories.push(new_t);
            } else {
                if (applied_theory.causes.equals(applied_theory.consecuence)) {
                    applied_theory.delta = -10; //TODO: FIXME
                }
                applied_theory.delta += 1; //TODO: FIXME
            }
        }


        LinkedList<Theory> relevant_theories = new LinkedList<>();

        for (Theory t : theories) {
            if (t.apply_to(s_status)) {
                relevant_theories.push(t);
            }
        }

        if (relevant_theories.size() == 0) {
            theories.push(new Theory(s_status, UP, s_status, -1));
            theories.push(new Theory(s_status, DOWN, s_status, -1));
            theories.push(new Theory(s_status, LEFT, s_status, -1));
            theories.push(new Theory(s_status, RIGHT, s_status, -1));

            relevant_theories = theories;
        }

        int max_delta = relevant_theories.get(0).delta;
        Theory best_theory = relevant_theories.get(0);

        for (Theory t : relevant_theories) {
            if (t.delta > max_delta) {
                best_theory = t;
                max_delta = t.delta;
            }
            if (t.delta == max_delta && new Random().nextBoolean()) {
                best_theory = t;
                max_delta = t.delta;
            }
        }

        System.out.println(s_status);

        return best_theory.action;
    }

    private class Theory {
        public String causes;
        public int action;
        public String consecuence;
        public int delta;

        Theory(String causes, int action, String consecuence, int delta) {
            this.causes = causes;
            this.action = action;
            this.consecuence = consecuence;
            this.delta = delta;
        }

        boolean apply_to(String status) {
            for (int i = 0; i < status.length(); i++) {
                if (causes.charAt(i) == '€') {
                    continue;
                }

                if (causes.charAt(i) != status.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
    }
}