package edu.cwru.sepia.agent.planner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.Deposit_kAction;
import edu.cwru.sepia.agent.planner.actions.Harvest_kAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class PEAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private static long totalTime = 0;
    private Stack<StripsAction> plan = null;
    private boolean printed = false;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        this.plan = plan;
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        return middleStep(stateView, historyView);
    }

    /**
     * @return a map from unitId to Action containing all actions that should be applied at the next turn
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {

        Map<Integer, Action> actionMap = new HashMap<Integer, Action>();

        if (plan.isEmpty()) {
            return actionMap;
        }

        /*
         * I've realized I can't do this when the plan is empty because otherwise the scenario
         * will end first.
         */
        if (plan.size() <= 2 && !printed) {
            printed = true;
            double seconds = (double) totalTime / Math.pow(10.0, 9.0);
            System.out.println("Total execution time: " + seconds + " seconds.");
        }

        int previousTurnNumber = stateView.getTurnNumber() - 1;

        if (previousTurnNumber < 0) {
            addNextAction(actionMap, stateView);
            return actionMap;
        }

        long startTime = System.nanoTime();

        Map<Integer, ActionResult> previousActions = historyView.getCommandFeedback(playernum, previousTurnNumber);
        boolean done = false;
        while (!done) {
            if (plan.empty()) {
                done = true;
            } else {
                StripsAction next = plan.peek();
                int[] ids = next.getUnitIds();
                if (!next.isParallelAction()) {
                    ActionResult previous = previousActions.get(ids[0]);
                    if (lastActionFailed(previous)) {
                        actionMap.put(previous.getAction().getUnitId(), previous.getAction());
                    }
                    if (!peasantAvailable(actionMap, next, previous)) {
                        done = true;
                    } else {
                        if (waitOnBuild(actionMap, next)) {
                            done = true;
                        } else {
                            addNextAction(actionMap, stateView);
                        }
                    }
                } else {
                    for (ActionResult result : previousActions.values()) {
                        if (lastActionFailed(result)) {
                            actionMap.put(result.getAction().getUnitId(), result.getAction());
                        }
                        if (!peasantAvailable(actionMap, next, result)) {
                            return actionMap;
                        }
                    }
                    if (waitOnBuild(actionMap, next)) {
                        done = true;
                    } else {
                        this.addNextParallelAction(actionMap, stateView);
                    }
                }

            }
        }

        long endTime = System.nanoTime();

        totalTime += (endTime - startTime);

        return actionMap;
    }

    private boolean lastActionFailed(ActionResult previous) {
        return previous != null && previous.getFeedback() == ActionFeedback.FAILED;
    }

    /*
     * See addNextAction(). This method is analogous, but deals with the parallel actions.
     */
    private void addNextParallelAction(Map<Integer, Action> actionMap, State.StateView state) {
        StripsAction action = plan.pop();
        Action[] sepiaActions = new Action[action.numPeasantsNeeded()];
        if (!action.isDirectedAction()) {
            sepiaActions = action.createSepiaActions(null);
        } else {
            int numPeasants = action.numPeasantsNeeded();
            Position destinationPos = action.getPositionForDirection();
            ArrayList<UnitView> peasants = new ArrayList<UnitView>();
            Direction[] directions = new Direction[numPeasants];
            boolean failed = false;
            for (int i = 0; i < numPeasants; i++) {
                UnitView temp = state.getUnit(action.getUnitIds()[i]);
                if (temp == null) {
                    plan.push(action);
                    return;
                }
                peasants.add(temp);
                Position pos = new Position(temp.getXPosition(), temp.getYPosition());
                directions[i] = pos.getDirection(destinationPos);
                if (directions[i] == null) {
                    if (!failed) {
                        failed = true;
                        plan.push(action);
                    }
                    Peasant peasant = new Peasant(temp.getID(), pos);
                    MoveAction move = new MoveAction(peasant, destinationPos);
                    plan.push(move);
                }
            }
            if (failed) {
                return;
            }
            sepiaActions = action.createSepiaActions(directions);
        }
        for (int i = 0; i < sepiaActions.length; i++) {
            actionMap.put(sepiaActions[i].getUnitId(), sepiaActions[i]);
        }
    }

    /*
     * The methods below are only for non-parallel actions.
     */

    private boolean peasantAvailable(Map<Integer, Action> actionMap, StripsAction next, ActionResult previous) {
        return !actionMap.containsKey(next.getUnitIds()[0]) && !(previous != null && previous.getFeedback().ordinal() == ActionFeedback.INCOMPLETE.ordinal());
    }


    /*
     * To avoid assigning actions to a peasant that hasn't been "built" yet, we temporarily
     * halt queuing new actions if a BuildAction has yet to be completed.
     */
    private boolean waitOnBuild(Map<Integer, Action> actionMap, StripsAction next) {
        return next.getUnitIds()[0] == GameState.townhallId && !actionMap.isEmpty();
    }

    /*
     * This method is called by middleStep when it is determined that we're ready to queue
     * the next singleton action for this turn. Pops the StripsAction off the stack, and
     * puts the appropriate sepia action into the action map.
     */
    private void addNextAction(Map<Integer, Action> actionMap, State.StateView state) {
        StripsAction action = plan.pop();
        Action sepiaAction = null;
        if (!action.isDirectedAction()) {
            sepiaAction = action.createSepiaActions(null)[0];
        } else {
            UnitView peasant = state.getUnit(action.getUnitIds()[0]);
            if (peasant == null) {
                plan.push(action);
                return;
            }
            Position peasantPos = new Position(peasant.getXPosition(), peasant.getYPosition());
            Position destinationPos = action.getPositionForDirection();
            Direction[] directions = {peasantPos.getDirection(destinationPos)};
            sepiaAction = action.createSepiaActions(directions)[0];
        }
        actionMap.put(sepiaAction.getUnitId(), sepiaAction);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}