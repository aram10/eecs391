package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.util.Direction;

public class BuildAction implements StripsAction {
    int townhallId;
    int peasantTemplateId;

    public BuildAction(int townhallId, int peasantTemplateId) {
        this.townhallId = townhallId;
        this.peasantTemplateId = peasantTemplateId;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        return state.canBuild();
    }

    @Override
    public void applyAction(GameState state) {
        state.applyBuildAction(this);
    }

    @Override
    public Action[] createSepiaActions(Direction[] directions) {
        Action[] actions = {Action.createPrimitiveProduction(townhallId, peasantTemplateId)};
        return actions;
    }

    @Override
    public int[] getUnitIds() {
        int[] ids = {townhallId};
        return ids;
    }

    @Override
    public String toString() {
        return "A new peasant is built.";
    }

}