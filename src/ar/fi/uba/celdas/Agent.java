package ar.fi.uba.celdas;

import ar.higesoft.Planner;
import ar.higesoft.WorldStatus;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

public class Agent extends AbstractPlayer {

    private WorldStatus world;

    private int action;
    private Planner planner;

    /**
     * initialize all variables for the agent
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        world = new WorldStatus(new Perception(stateObs));
        planner = new Planner();
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
        String status = world.getWorldStatus();
        planner.updateTheories(status);

        ArrayList<ACTIONS> actions = stateObs.getAvailableActions();
        return actions.get(planner.getNextAction(status));
    }

    @Override
    public void result(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {
        Perception perception = new Perception(stateObs);
        world.updateWorld(perception);
        String status = world.getWorldStatus();
        planner.updateTheories(status);
    }
}
