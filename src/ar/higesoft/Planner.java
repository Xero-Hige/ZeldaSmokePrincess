package ar.higesoft;

import core.game.StateObservation;
import tools.Vector2d;

import java.util.*;

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

    public static final int A = 0;
    static final int UP = 4;
    static final int DOWN = 3;
    static final int LEFT = 1;
    static final int RIGHT = 2;
    static final char WILDCARD_CAUSES = '€';

    private String previousStatus;
    private String predictedStatus;
    private Theory appliedTheory;
    private LinkedList<Theory> theories;
    private double previousScore;
    private int previousDistance;
    private int executed = 0;
    private Vector2d previousOrientation = new Vector2d(0, 0);


    public Planner() {
        theories = new LinkedList<>();
        predictedStatus = "-";
        previousStatus = "-";
        appliedTheory = null;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getPredictedStatus() {
        return predictedStatus;
    }

    public void setPredictedStatus(String predictedStatus) {
        this.predictedStatus = predictedStatus;
    }

    public Theory getAppliedTheory() {
        return appliedTheory;
    }

    public void setAppliedTheory(Theory appliedTheory) {
        this.appliedTheory = appliedTheory;
    }

    public LinkedList<Theory> getTheories() {
        return theories;
    }

    public void setTheories(LinkedList<Theory> theories) {
        this.theories = theories;
    }

    public void cleanPlanner() {
        predictedStatus = "-";
        previousStatus = "-";
        appliedTheory = null;
    }

    public void removeDummys() {
        theories.removeIf(t ->
                t.delta == 0
        );
    }

    public void generalizeTheories() {
        //FIXME: Name

        theories.sort(getTheoryComparator());

        LinkedList<Theory> newTheories = new LinkedList<>();

        for (int i = 0; i + 1 < theories.size(); i += 2) {
            Theory a = theories.get(i);
            Theory b = theories.get(i + 1);

            if (a.isEquivalentTo(b) && !a.causes.equals(b.causes)) {

                char causes[] = a.causes.toCharArray();
                char anotherCauses[] = b.causes.toCharArray();

                for (int j = 0; j < causes.length; j++) {
                    if (causes[j] != anotherCauses[j]) {
                        causes[j] = WILDCARD_CAUSES;
                    }
                }

                String causesString = new String(causes);
                int count = causesString.length() - causesString.replace("€", "").length();

                if (count == causesString.length()) {
                    continue;
                }

                Theory newTheory = new Theory(causesString, a.action, a.consequences, a.delta);
                newTheory.appliedTimes = a.appliedTimes + b.appliedTimes;
                newTheory.successTimes = a.successTimes + b.successTimes;

                newTheories.addLast(newTheory);
            }
        }


        for (Theory t : newTheories) {
            theories.addLast(t);
        }
    }

    public void removeDuplicated() {
        theories.sort(getTheoryComparator());

        LinkedList<Theory> newTheories = new LinkedList<>();

        Theory a = theories.removeFirst();
        while (theories.size() > 0) {
            Theory b = theories.removeFirst();

            if (a.areEquals(b)) {
                a.setAppliedTimes(a.getAppliedTimes() + b.getAppliedTimes());
                a.setSuccessTimes(a.getSuccessTimes() + b.getSuccessTimes());
            } else {
                newTheories.addLast(a);
                a = b;
            }
        }

        if (a != newTheories.getLast()) {
            newTheories.addLast(a);
        }

        theories = newTheories;
    }

    private Comparator<Theory> getTheoryComparator() {
        return (a1, a2) -> {
            if (!a1.consequences.equals(a2.consequences)) {
                return a1.consequences.compareTo(a2.consequences);
            }

            if (a1.delta != a2.delta) {
                return Integer.compare(a1.delta, a2.delta);
            }

            if (a1.action != a2.action) {
                return Integer.compare(a1.action, a2.action);
            }

            if (!a1.causes.equals(a2.causes)) {
                return a1.causes.compareTo(a2.causes);
            }

            return Double.compare(a2.successRateGet(), a1.successRateGet());
        };
    }

    public int getNextAction(String status, WorldStatus world, StateObservation stateObservation) {

        executed += 1;
        if (executed % 16 == 0) {
            removeDummys();
            removeUnsuccess();
            removeDuplicated();
            generalizeTheories();
        }

        HashMap<Integer, Theory> options = getAllOptionsAtState(status);


        Theory bestTheory = new Theory();
        int bestDelta = -9999;

        ArrayList<Theory> optionValues = new ArrayList<>();
        optionValues.addAll(options.values());
        Collections.shuffle(optionValues);

        for (Theory t : optionValues) {

            char predictedStatus[] = status.toCharArray();
            char consequences[] = t.getConsequences().toCharArray();

            predictNextState(predictedStatus, consequences);

            HashMap<Integer, Theory> predicted_options = getAllOptionsAtState(new String(predictedStatus));
            Theory nextTheory = getBestTheoryFromOptions(predicted_options);

            int newDelta = t.delta + nextTheory.delta;

            if (t.delta != 0 && newDelta == 0) {
                newDelta = -1;
            }

            if (newDelta > bestDelta) {
                bestTheory = t;
                bestDelta = newDelta;
            }
        }

        if (bestDelta <= 0 || options.size() < 5) {

            int actions[] = {UP, DOWN, LEFT, RIGHT, A};

            for (int i : actions) {
                if (i == bestTheory.action) {
                    continue;
                }
                Theory new_theory = new Theory(status, i, status, 0);
                new_theory.appliedTimes = 1;
                new_theory.successTimes = 1;
                theories.addLast(new_theory);
            }
        }

        appliedTheory = bestTheory;
        previousStatus = status;

        char predicted[] = status.toCharArray();
        char consequences[] = bestTheory.getConsequences().toCharArray();
        predictNextState(predicted, consequences);
        predictedStatus = new String(predicted);

        bestTheory.appliedTimes += 1;
        previousDistance = world.getDistanceToGoal();
        previousScore = (int) stateObservation.getGameScore();
        previousOrientation = stateObservation.getAvatarOrientation();
        return bestTheory.action;
    }

    private void predictNextState(char[] predictedStatus, char[] consequences) {
        for (int i = 0; i < consequences.length; i++) {
            if (consequences[i] == WILDCARD_CAUSES) {
                predictedStatus[i] = WILDCARD_CAUSES;
                continue;
            }

            predictedStatus[i] = consequences[i];
        }
    }

    private HashMap<Integer, Theory> getAllOptionsAtState(String status) {
        LinkedList<Theory> relevantTheories = new LinkedList<>();

        for (Theory t : theories) {
            if (t.doesApplyTo(status)) {
                relevantTheories.push(t);
            }
        }

        if (relevantTheories.size() == 0) {

            int actions[] = {UP, DOWN, LEFT, RIGHT, A};
            //TODO: Check
            for (int i : actions) {
                Theory new_theory = new Theory(status, i, status, 0);
                new_theory.appliedTimes = 1;
                new_theory.successTimes = 1;
                relevantTheories.push(new_theory);
            }

            for (Theory t : relevantTheories) {
                theories.push(t);
            }
        }

        HashMap<Integer, Theory> options = new HashMap<>();

        for (Theory t : relevantTheories) {
            if (!options.containsKey(t.action)) {
                options.put(t.action, t);
            }

            Theory actionTheory = options.get(t.action);
            if (t.delta > actionTheory.delta) {
                options.put(t.action, t);
            }
            //if (t.successRateGet() > options.get(t.action).successRateGet()) {
            //    options.put(t.action, t);
            //}
        }
        return options;
    }

    private Theory getBestTheoryFromOptions(HashMap<Integer, Theory> options) {
        Theory best_theory = new Theory();
        int max_delta = -10000;

        ArrayList<Theory> optionValues = new ArrayList<>();
        optionValues.addAll(options.values());
        Collections.shuffle(optionValues);

        for (Theory t : optionValues) {

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

        if (appliedTheory == null) {
            return;
        }

        //System.out.println(String.format("Status: %s Action: %d", status, appliedTheory.action));
        //System.out.println(String.format("Predicted: %s Delta: %d", predictedStatus, appliedTheory.delta));


        int delta = computeDelta(world, stateObs);

        if (appliedTheory.delta == 0) {
            appliedTheory.consequences = status;
            appliedTheory.appliedTimes = 3;
            appliedTheory.successTimes = 3;
            appliedTheory.delta = delta;

            theories.push(appliedTheory);
            return;
        }

        boolean wrong = false;

        for (int i = 0; i < predictedStatus.length(); i++) {
            if (predictedStatus.charAt(i) == WILDCARD_CAUSES) {
                continue;
            }

            if (predictedStatus.charAt(i) != status.charAt(i)) {
                wrong = true;
                break;
            }
        }

        String s = previousStatus;
        String s_p = status;
        Theory t = appliedTheory;
        String c = appliedTheory.causes;
        String e = appliedTheory.consequences;

        if (wrong) {
            retract(t, delta, s, s_p, e);
        }

        if (!wrong && delta != appliedTheory.delta) {
            Theory newTheory = new Theory(appliedTheory.causes, appliedTheory.action, appliedTheory.consequences, delta);
            newTheory.setSuccessTimes(1);
            newTheory.setAppliedTimes(1);

            theories.addLast(newTheory);
        }

        if (!world.isPlayerAlive()) {
            appliedTheory.delta = delta;
        }

        if (!wrong && delta == appliedTheory.delta) {
            appliedTheory.successTimes += 1;
        }
    }

    private void retract(Theory retracted, int delta, String s, String s_p, String e) {
        char e_t2[] = e.toCharArray();

        for (int i = 0; i < predictedStatus.length(); i++) {
            if (e.charAt(i) == WILDCARD_CAUSES) {
                continue;
            }

            if (e.charAt(i) != s_p.charAt(i)) {
                e_t2[i] = WILDCARD_CAUSES;
            }
        }


        String e_t2_string = new String(e_t2);
        int count = e_t2_string.length() - e_t2_string.replace("€", "").length();

        if (count == e_t2.length) {
            return;
        }

        Theory retractedTheory = new Theory();
        retractedTheory.setConsequences(e_t2_string);
        retractedTheory.setDelta(delta);

        retractedTheory.setAppliedTimes(1);
        retractedTheory.setSuccessTimes(1);

        theories.addLast(retractedTheory);
    }

    private int computeDelta(WorldStatus world, StateObservation stateObservation) {
        if (!world.isPlayerAlive()) { //Dead
            return -1000;
        }

        //System.out.println(String.format("Ticks %d", stateObservation.getGameTick()));
        if (stateObservation.getGameTick() > 1999) { //Dead
            return -1000;
        }

        int actualDistance = world.getDistanceToGoal();

        if (previousDistance == actualDistance) {
            if (stateObservation.getGameScore() == previousScore)
                return stateObservation.getAvatarOrientation() != previousOrientation ? -1 : -3;
            return stateObservation.getGameScore() > previousScore ? 10 : -10;
        }
        return previousDistance < actualDistance ? -2 : 3;
    }

    public void removeUnsuccess() {
        //theories.removeIf(t -> (t.successRateGet() == 1 && t.consequences.equals(t.causes)));
        theories.removeIf(t -> t.successRateGet() <= 0.1);
    }

    private static class Theory {
        String causes;
        int action;
        String consequences;
        int delta;

        long appliedTimes = 0;
        long successTimes = 0;

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

        public long getAppliedTimes() {
            if (appliedTimes > 10000) {
                appliedTimes /= 2;
                successTimes /= 2;
            }

            return appliedTimes;
        }

        public void setAppliedTimes(long appliedTimes) {
            this.appliedTimes = appliedTimes;
        }

        public long getSuccessTimes() {
            return successTimes;
        }

        public void setSuccessTimes(long successTimes) {
            this.successTimes = successTimes;
        }

        double successRateGet() {
            if (appliedTimes == 0)
                return 0;

            return (double) successTimes / appliedTimes;
        }

        boolean doesApplyTo(String status) {
            for (int i = 0; i < status.length(); i++) {
                if (causes.charAt(i) == WILDCARD_CAUSES)
                    continue;

                if (causes.charAt(i) != status.charAt(i))
                    return false;
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

        boolean isEquivalentTo(Theory other) {
            if (this.action != other.action) {
                return false;
            }

            if (this.delta != other.delta) {
                return false;
            }

            return this.consequences.equals(other.consequences);
        }
    }
}
