package ar.higesoft;

import java.util.Collections;
import java.util.Comparator;
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

    public static final int A = 0;

    private String previous_status;
    private String predicted_status;
    private Theory applied_theory;
    private LinkedList<Theory> theories;

    public Planner() {
        theories = new LinkedList<>();
        predicted_status = "-";
        previous_status = "-";
        applied_theory = null;
    }

    public void cleanPlanner() {
        predicted_status = "-";
        previous_status = "-";
        applied_theory = null;
    }

    public String getPrevious_status() {
        return previous_status;
    }

    public void setPrevious_status(String previous_status) {
        this.previous_status = previous_status;
    }

    public String getPredicted_status() {
        return predicted_status;
    }

    public void setPredicted_status(String predicted_status) {
        this.predicted_status = predicted_status;
    }

    public Theory getApplied_theory() {
        return applied_theory;
    }

    public void setApplied_theory(Theory applied_theory) {
        this.applied_theory = applied_theory;
    }

    public LinkedList<Theory> getTheories() {
        return theories;
    }

    public void setTheories(LinkedList<Theory> theories) {
        this.theories = theories;
    }

    public void generalize() {
        Collections.sort(theories, Comparator.comparing(t -> t.causes));

        for (int i = 0; i < theories.size() - 1; i++) {
            if (theories.get(i).causes.equals(theories.get(i + 1).causes)) {
            }
        }
    }

    public int getNextAction(String status) {
        LinkedList<Theory> relevant_theories = new LinkedList<>();

        for (Theory t : theories) {
            if (t.apply_to(status)) {
                relevant_theories.push(t);
            }
        }

        if (relevant_theories.size() == 0) {
            theories.push(new Theory(status, UP, status, -1));
            theories.push(new Theory(status, DOWN, status, -1));
            theories.push(new Theory(status, LEFT, status, -1));
            theories.push(new Theory(status, RIGHT, status, -1));

            theories.push(new Theory(status, A, status, -1));


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

        applied_theory = best_theory;
        previous_status = status;
        predicted_status = best_theory.consequences;
        best_theory.applied_times += 1;
        return best_theory.action;
    }

    public void updateTheories(String status, WorldStatus world) {
        if (applied_theory == null) {
            return;
        }

        boolean wrong = false;

        for (int i = 0; i < predicted_status.length(); i++) {
            if (predicted_status.charAt(i) == '-') {
                continue;
            }

            if (predicted_status.charAt(i) != status.charAt(i)) {
                wrong = true;
                break;
            }
        }

        if (wrong) {
            Theory new_t = new Theory(applied_theory.causes, applied_theory.action, status, 1);
            theories.push(new_t);

            if (!world.isPlayerAlive()) {
                new_t.delta = -1000;
            }
        } else {
            applied_theory.success_times += 1;

            if (applied_theory.causes.equals(applied_theory.getConsequences())) {
                applied_theory.delta = -100; //TODO: FIXME
            }

            applied_theory.delta += 1; //TODO: FIXME

            if (!world.isPlayerAlive()) {
                applied_theory.delta = -1000;
            }
        }
    }

    private static class Theory {
        String causes;
        int action;
        String consequences;
        int delta;

        int applied_times = 0;
        int success_times = 0;

        Theory(String causes, int action, String consequences, int delta) {
            this.causes = causes;
            this.action = action;
            this.consequences = consequences;
            this.delta = delta;
        }

        public Theory() {
            this.causes = "";
            this.action = -20;
            this.consequences = "";
            this.delta = 0;
        }

        public String getCauses() {
            return causes;
        }

        public void setCauses(String causes) {
            this.causes = causes;
        }

        public String getConsequences() {
            return consequences;
        }

        public void setConsequences(String consequences) {
            this.consequences = consequences;
        }

        public int getDelta() {
            return delta;
        }

        public void setDelta(int delta) {
            this.delta = delta;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public int getApplied_times() {
            return applied_times;
        }

        public void setApplied_times(int applied_times) {
            this.applied_times = applied_times;
        }

        public int getSuccess_times() {
            return success_times;
        }

        public void setSuccess_times(int success_times) {
            this.success_times = success_times;
        }

        public double succesRateGet() {
            return (double) success_times / applied_times;
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

        Theory generalize(Theory other) {
            if (this.action != other.action) {
                return null;
            }

            if (!this.consequences.equals(other.consequences)) {
                return null;
            }

            char[] new_c = new char[12];

            for (int i = 0; i < causes.length(); i++) {
                new_c[i] = (causes.charAt(i) == other.causes.charAt(i)) ? causes.charAt(i) : '-';
            }

            String new_causes = new String(new_c);
            return new Theory(new_causes, action, consequences, delta + other.delta);
        }
    }
}
