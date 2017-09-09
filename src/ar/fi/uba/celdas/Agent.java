package ar.fi.uba.celdas;

import ar.higesoft.WorldParser;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

public class Agent extends AbstractPlayer {

    private WorldParser world;

    /**
     * initialize all variables for the agent
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        world = new WorldParser(new Perception(stateObs));
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

        if (world.getPlayer_column() > world.getKey_column()) {

            if (perception.getAt(world.getPlayer_column() - 1, world.getDoor_row()) != 'w') {
                world.move(WorldParser.LEFT);
                return stateObs.getAvailableActions().get(1);
            }
        }

        if (world.getPlayer_column() < world.getKey_column()) {
            if (perception.getAt(world.getPlayer_column() + 1, world.getDoor_row()) != 'w') {
                {
                    world.move(WorldParser.RIGHT);
                    return stateObs.getAvailableActions().get(2);
                }
            }
        }

        if (world.getPlayer_row() > world.getKey_row()) {

            if (perception.getAt(world.getPlayer_column(), world.getDoor_row() + 1) != 'w') {
                {
                    world.move(WorldParser.UP);
                    return stateObs.getAvailableActions().get(4);
                }
            }
        }

        if (world.getPlayer_row() < world.getKey_row()) {

            if (perception.getAt(world.getPlayer_column(), world.getDoor_row() - 1) != 'w') {
                {
                    world.move(WorldParser.DOWN);
                    return stateObs.getAvailableActions().get(3);
                }
            }
        }

        System.out.println("Looking for the answers");
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        int index = (int) (Math.random() * actions.size());
        return actions.get(0);
    }
}
