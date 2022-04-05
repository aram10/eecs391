package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class MoveAction implements StripsAction {
    Peasant peasant;
    Position destination;

    public MoveAction(Peasant peasant, Position destination) {
        this.peasant = peasant;
        this.destination = destination;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        return !peasant.getPosition().equals(destination);
    }

    @Override
    public void applyAction(GameState state) {
        state.applyMoveAction(this, peasant.getId(), destination);
    }

    @Override
    public Action[] createSepiaActions(Direction[] directions) {
        Action[] actions = {Action.createCompoundMove(peasant.getId(), destination.x, destination.y)};
        return actions;
    }

    @Override
    public int[] getUnitIds() {
        int[] ids = {peasant.getId()};
        return ids;
    }

    @Override
    public double getCost() {
        return peasant.getPosition().chebyshevDistance(destination) - 1;
    }

    @Override
    public String toString() {
        return "Peasant with ID " + peasant.getId() + " moves to position with coordinates (" + destination.x + ", " + destination.y + ")";
    }

}