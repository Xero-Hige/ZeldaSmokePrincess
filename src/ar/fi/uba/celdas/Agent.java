package ar.fi.uba.celdas;

import ar.higesoft.Runner;
import ar.higesoft.WorldParser;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsParserException;
import tools.ElapsedCpuTimer;

import java.io.IOException;
import java.util.ArrayList;

public class Agent extends AbstractPlayer {

    private WorldParser world;

    private static Runner r = new Runner();

    private int action;
    private RuleBase ruleBase;
    private WorkingMemory workingMemory;

    /**
     * initialize all variables for the agent
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        world = new WorldParser(new Perception(stateObs));
        try {
            ruleBase = r.initialiseDrools();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DroolsParserException e) {
            e.printStackTrace();
        }
        r.setWorld(world);
        workingMemory = r.initializeMessageObjects(ruleBase);
    }

    /**
     * return ACTION_NIL on every call to simulate doNothing player
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return ACTION_NIL all the time
     */
    @Override
    public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        Perception perception = new Perception(stateObs);
        world.updateWorld(perception);

        workingMemory = r.initializeMessageObjects(ruleBase);

        int actualNumberOfRulesFired = workingMemory.fireAllRules();

        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();

        if (actualNumberOfRulesFired < 0) {
            return actions.get(0);
        }

        return actions.get(world.getAction());
    }
}
