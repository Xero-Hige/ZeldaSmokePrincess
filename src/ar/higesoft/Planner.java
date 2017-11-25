package ar.higesoft;

import core.game.StateObservation;
import tools.Vector2d;

import java.util.HashMap;
import java.util.LinkedList;

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
    private double previousScore;
    private int previousDistance;
    private int executed = 0;
    private Vector2d previousOrientation = new Vector2d(0, 0);

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

    public void removeDuplicated() {
        theories.sort((a1, a2) -> {
            if (!a1.causes.equals(a2.causes)) {
                return a1.causes.compareTo(a2.causes);
            }

            if (!a1.consequences.equals(a2.consequences)) {
                return a1.consequences.compareTo(a2.consequences);
            }

            if (a1.delta != a2.delta) {
                return Integer.compare(a1.delta, a2.delta);
            }

            return Integer.compare(a1.action, a2.action);
        });

        LinkedList<Theory> new_theories = new LinkedList<>();

        Theory a = theories.removeFirst();
        while (theories.size() > 0) {
            Theory b = theories.removeFirst();

            if (a.areEquals(b)) {
                a.setApplied_times(a.getApplied_times() + b.getApplied_times());
                a.setSuccess_times(a.getSuccess_times() + b.getSuccess_times());
            } else {
                new_theories.addLast(a);
                a = b;
            }
        }

        if (a != new_theories.getLast()) {
            new_theories.addLast(a);
        }

        theories = new_theories;
    }

    public int getNextAction(String status, WorldStatus world, StateObservation stateObservation) {

        executed += 1;
        if (executed % 16 == 15) {
            removeUnsuccess();
            //removeDuplicated();
        }

        LinkedList<Theory> relevant_theories = new LinkedList<>();

        for (Theory t : theories) {
            if (t.apply_to(status)) {
                relevant_theories.push(t);
            }
        }

        if (relevant_theories.size() == 0) {

            int actions[] = {UP, DOWN, LEFT, RIGHT, A};

            for (int i : actions) {
                Theory new_theory = new Theory(status, i, status, 0);
                new_theory.applied_times = 1;
                new_theory.success_times = 1;
                relevant_theories.push(new_theory);
            }

            for (Theory t : relevant_theories) {
                theories.push(t);
            }
        }

        HashMap<Integer, Theory> options = new HashMap<>();

        options.put(UP, new Theory(status, UP, status, -2000));
        options.put(DOWN, new Theory(status, DOWN, status, -2000));
        options.put(LEFT, new Theory(status, LEFT, status, -2000));
        options.put(RIGHT, new Theory(status, RIGHT, status, -2000));
        options.put(A, new Theory(status, A, status, -2000));

        for (Theory t : relevant_theories) {
            if (t.delta > options.get(t.action).delta) {
                options.put(t.action, t);
            }
            //if (t.succesRateGet() > options.get(t.action).succesRateGet()) {
            //    options.put(t.action, t);
            //}
        }

        Theory best_theory = getBestTheoryFromOptions(options);

        double max_delta = best_theory.delta;
        if (max_delta < 0) {

            int actions[] = {UP, DOWN, LEFT, RIGHT, A};

            for (int i : actions) {
                if (i == best_theory.action) {
                    continue;
                }
                Theory new_theory = new Theory(status, i, status, 0);
                new_theory.applied_times = 1;
                new_theory.success_times = 1;
                theories.addLast(new_theory);
            }
        }

        applied_theory = best_theory;
        previous_status = status;
        predicted_status = best_theory.consequences;
        best_theory.applied_times += 1;
        previousDistance = world.getDistanceToGoal();
        previousScore = (int) stateObservation.getGameScore();
        return best_theory.action;
    }

    private Theory getBestTheoryFromOptions(HashMap<Integer, Theory> options) {
        Theory best_theory = new Theory();
        double max_delta = -2000;

        for (Theory t : options.values()) {

            if (t.delta < max_delta) {
                continue;
            }

            if (t.delta > max_delta) {
                best_theory = t;
                max_delta = t.delta;
            }
        }
        return best_theory;
    }

    public void updateTheories(String status, WorldStatus world, StateObservation stateObs) {
        //System.out.println(String.format("Status: %s Action: %d", status, applied_theory.action));
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


        //int new_gain = calcGain(world, stateObs);
        int delta = computeDelta(world, stateObs);

        String s = previous_status;
        String s_p = status;
        Theory t = applied_theory;
        String c = applied_theory.causes;
        String e = applied_theory.consequences;

        if (wrong) {
            retract(t, delta, s, s_p, e);
        }

        if (!wrong && delta != applied_theory.delta) {
            Theory new_theory = new Theory(applied_theory.causes, applied_theory.action, applied_theory.consequences, delta);
            new_theory.setSuccess_times(1);
            new_theory.setApplied_times(1);

            theories.addLast(new_theory);
        }

        if (!world.isPlayerAlive()) {
            applied_theory.delta = delta;
        }

        if (!wrong && delta == applied_theory.delta) {
            applied_theory.success_times += 1;
        }
    }

    private void retract(Theory retracted, int delta, String s, String s_p, String e) {
        char e_t2[] = e.toCharArray();

        for (int i = 0; i < predicted_status.length(); i++) {
            if (e.charAt(i) == '-') {
                e_t2[i] = '-';
                continue;
            }

            if (e.charAt(i) != s_p.charAt(i)) {
                e_t2[i] = '-';
            }
        }

        String e_t2_string = new String(e_t2);

        retracted.setConsequences(e_t2_string);
        retracted.setDelta(delta);
        //retracted.setApplied_times(1);
        //retracted.setSuccess_times(1);

        //theories.addLast(retracted);
    }

    private int computeDelta(WorldStatus world, StateObservation stateObservation) {
        if (!world.isPlayerAlive()) { //Dead
            return -1000;
        }

        System.out.println(String.format("Ticks %d", stateObservation.getGameTick()));
        if (stateObservation.getGameTick() > 1999) { //Dead
            return -1000;
        }

        int actual_distance = world.getDistanceToGoal();

        if (previousDistance == actual_distance) {
            if (stateObservation.getGameScore() == previousScore)
                return stateObservation.getAvatarOrientation() != previousOrientation ? -1 : -3;
            return stateObservation.getGameScore() > previousScore ? 10 : -10;
        }
        return previousDistance < actual_distance ? -2 : 3;
    }

    public void removeUnsuccess() {
        //theories.removeIf(t -> (t.succesRateGet() == 1 && t.consequences.equals(t.causes)));
        theories.removeIf(t -> t.succesRateGet() <= 0.1);
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
            this.action = -5;
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
            if (applied_times == 0) {
                return 0;
            }

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

        boolean areEquals(Theory other) {
            if (this.action != other.action) {
                return false;
            }

            if (this.delta != other.delta) {
                return false;
            }

            if (!this.consequences.equals(other.consequences)) {
                return false;
            }

            return this.causes.equals(other.causes);
        }
    }
}
