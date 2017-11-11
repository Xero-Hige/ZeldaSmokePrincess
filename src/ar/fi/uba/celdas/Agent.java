package ar.fi.uba.celdas;

import ar.higesoft.Planner;
import ar.higesoft.WorldStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import org.apache.commons.io.FileUtils;
import tools.ElapsedCpuTimer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

        world = new WorldStatus(stateObs);
        load_planner();

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
        world.updateWorld(stateObs);
        String status = world.getWorldStatus();
        planner.updateTheories(status, stateObs);

        ArrayList<ACTIONS> actions = stateObs.getAvailableActions();
        return actions.get(planner.getNextAction(status));
    }

    @Override
    public void result(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {
        world.updateWorld(stateObs);
        String status = world.getWorldStatus();
        planner.updateTheories(status, stateObs);

        persistPlanner();
    }

    private void persistPlanner() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (PrintWriter out = new PrintWriter("planner.json")) {
            mapper.writeValue(out, planner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void load_planner() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File("planner.json");
            String everything = FileUtils.readFileToString(file);
            planner = mapper.readValue(everything, Planner.class);
            planner.cleanPlanner();
        } catch (IOException e) {
            planner = new Planner();
        }
    }
}
