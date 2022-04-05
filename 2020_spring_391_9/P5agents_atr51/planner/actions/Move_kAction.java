package edu.cwru.sepia.agent.planner.actions;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class Move_kAction implements StripsAction {

    private ArrayList<Peasant> peasants;

    private Position destination;

    public Move_kAction(ArrayList<Peasant> peasants, Position destination) {
        this.peasants = peasants;
        this.destination = destination;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        for (Peasant peasant : this.peasants) {
            if (peasant.getPosition().equals(this.destination)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void applyAction(GameState state) {
        int[] unitIds = this.getUnitIds();
        state.applyCompoundMoveAction(this, unitIds, destination);
    }

    @Override
    public Action[] createSepiaActions(Direction[] directions) {
        Action[] actions = new Action[this.peasants.size()];
        for (int i = 0; i < peasants.size(); i++) {
            actions[i] = Action.createCompoundMove(this.peasants.get(i).getId(), destination.x, destination.y);
        }
        return actions;
    }

    @Override
    public int[] getUnitIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>(this.peasants.size());
        this.peasants.stream().forEach((e) -> ids.add(e.getId()));
        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public boolean isParallelAction() {
        return true;
    }

    @Override
    public double getCost() {
        double sum = 0;
        for (Peasant peasant : this.peasants) {
            sum += peasant.getPosition().chebyshevDistance(destination) - 1;
        }
        return sum / this.peasants.size();
    }

    @Override
    public int numPeasantsNeeded() {
        return this.peasants.size();
    }

    @Override
    public String toString() {
        StringBuilder concatIds = new StringBuilder();
        concatIds.append("{");
        for (Peasant peasant : this.peasants) {
            concatIds.append(peasant.getId());
            concatIds.append(", ");
        }
        concatIds.deleteCharAt(concatIds.length() - 1);
        concatIds.deleteCharAt(concatIds.length() - 1);
        concatIds.append("}");
        return "Peasants with ids " + concatIds.toString() + " move to coordinates (" + this.destination.x + ", " + this.destination.y + ").";
    }

}
