package ar.higesoft;

import core.game.StateObservation;

import java.util.LinkedList;
import java.util.Random;

import static ar.higesoft.WorldStatus.STATUS_SIZE;
import static java.lang.Math.abs;

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
    private int previousGain;

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
        theories.sort((a1, a2) -> {
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
        while (theories.size() > 2) {
            Theory b = theories.removeFirst();

            if (b.succesRateGet() <= 0.2) {
                new_theories.addLast(b);
                continue;
            }

            Theory general = b.generalize(a);

            if (general != null) {
                new_theories.addLast(general);
                a = theories.removeFirst();
            } else {
                new_theories.addLast(a);
                a = b;
            }
        }

        while (theories.size() > 0) {
            new_theories.addLast(theories.removeFirst());
        }

        theories = new_theories;
    }

    public int getNextAction(String status) {
        LinkedList<Theory> relevant_theories = new LinkedList<>();

        for (Theory t : theories) {
            if (t.apply_to(status)) {
                relevant_theories.push(t);
            }
        }

        if (relevant_theories.size() == 0) {
            theories.push(new Theory(status, UP, status, 10));
            theories.push(new Theory(status, DOWN, status, 10));
            theories.push(new Theory(status, LEFT, status, 10));
            theories.push(new Theory(status, RIGHT, status, 10));

            theories.push(new Theory(status, A, status, 10));

            relevant_theories = theories;
        }

        double max_delta = relevant_theories.get(0).delta;
        Theory best_theory = relevant_theories.get(0);

        for (Theory t : relevant_theories) {
            if (t.delta > max_delta) {
                best_theory = t;
                max_delta = t.delta;
            }

            if (t.delta < max_delta) {
                continue;
            }

            if (t.causes.lastIndexOf("€") != -1 && best_theory.causes.lastIndexOf("€") == -1) {
                best_theory = t;
            }
        }

        if (max_delta < -10) {
            int array[] = {UP, DOWN, LEFT, RIGHT, A, A, A, A};
            int rnd = new Random().nextInt(array.length);
            best_theory = new Theory(status, array[rnd], status, 10);
        }

        applied_theory = best_theory;
        previous_status = status;
        predicted_status = best_theory.consequences;
        best_theory.applied_times += 1;
        return best_theory.action;
    }

    private int calcGain(WorldStatus world, StateObservation stateObs) {
        int a;
        int b;
        if (world.getHasKey()) {
            a = world.getDoor_column();
            b = world.getDoor_row();
        } else {
            a = world.getKey_column();
            b = world.getKey_row();
        }

        int col_diff = abs(a - world.getPlayer_column());
        int row_diff = abs(b - world.getPlayer_row());

        return (col_diff + row_diff) * -1 + 15 * ((world.getHasKey()) ? 1 : 0) + (int) (7 * stateObs.getGameScore());
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


        int new_gain = calcGain(world, stateObs);
        int delta = computeDelta(status, world, new_gain);

        String s = previous_status;
        String s_p = status;
        //Theory t = applied_theory;
        //String c = applied_theory.causes;
        String e = applied_theory.consequences;

        if (wrong) {
            retract(delta, s, s_p, e);
        }

        /*
        if (wrong || delta != applied_theory.delta) {
            applied_theory.applied_times += 1;

            for (Theory t : theories) {
                if (!t.causes.equals(applied_theory.causes)) {
                    continue;
                }

                if (!t.consequences.equals(applied_theory.consequences)) {
                    continue;
                }

                if (t.action != applied_theory.action) {
                    continue;
                }

                if (t.delta != delta) {
                    continue;
                }

                t.applied_times += 1;
                t.success_times += 1;
                previousGain = new_gain;
                return;
            }

            Theory new_t = new Theory(applied_theory.causes, applied_theory.action, status, delta);
            theories.push(new_t);

            new_t.setApplied_times(2);
            new_t.setSuccess_times(2);
            previousGain = new_gain;
            return;
        }

        applied_theory.success_times += 1;
        previousGain = new_gain;
        */
    }

    private void retract(int delta, String s, String s_p, String e) {
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
        Theory retracted = new Theory(s, applied_theory.action, e_t2_string, delta);
        retracted.setApplied_times(1);
        retracted.setSuccess_times(1);

        theories.addLast(retracted);
    }

    private int computeDelta(String status, WorldStatus world, int new_gain) {
        int delta = previousGain - new_gain;

        if (new_gain == previousGain) { //Nothing best

            if (previous_status.equals(status)) { //Not moved
                delta = -300;
                applied_theory.delta = -300;
            }
        }

        if (!world.isPlayerAlive()) { //Dead
            delta = -1000;
        }
        return delta;
    }

    public void removeUnsuccess() {
        theories.removeIf(t -> t.applied_times == 0);
        theories.removeIf(t -> t.delta < -1000);
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

            if (this.delta != other.delta) {
                return null;
            }

            if (!this.consequences.equals(other.consequences)) {
                return null;
            }

            if (this.succesRateGet() < 0.20 || other.succesRateGet() < 0.20) {
                return null;
            }

            char[] new_c = new char[STATUS_SIZE];

            for (int i = 0; i < causes.length(); i++) {
                new_c[i] = (causes.charAt(i) == other.causes.charAt(i)) ? causes.charAt(i) : '€';
            }

            String new_causes = new String(new_c);
            Theory new_theory = new Theory(new_causes, action, consequences, (delta + other.delta) / 2);
            new_theory.setSuccess_times(10);
            new_theory.setApplied_times(10);

            return new_theory;
        }
    }
}
