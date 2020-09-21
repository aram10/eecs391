package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

/**
 * A useful start of an interface representing strips actions. You may add new methods to this interface if needed, but
 * you should implement the ones provided. You may also find it useful to specify a method that returns the effects
 * of a StripsAction.
 */
public interface StripsAction {

    /**    
     * 
     * @param state GameState to check if action is applicable
     * @return true if apply can be called, false otherwise
     */
    public boolean preconditionsMet(GameState state);

    /**
     *
     * @param state State to apply action to
     * @return State resulting from successful action application.
     */
    public default GameState apply(GameState state) {
    	applyAction(state);
    	updateState(state);
    	return state;
    }
    
    public default void updateState(GameState state) {
    	state.updatePlanAndCost(this);
    }

	public void applyAction(GameState state);
    
    public default boolean isDirectedAction() {
		return false;
	}
    
    public default boolean isParallelAction() {
    	return false;
    }
    
    public default Position getPositionForDirection() {
    	return null;
    }
	
	/**
	 * 
	 * @param direction is ignored if not a directed action
	 * @return a SepiaAction of this action
	 */
	public Action[] createSepiaActions(Direction[] directions);

	/**
	 * 
	 * @return the id of the unit to perform the action
	 */
	public int[] getUnitIds();
	
	public default double getCost() {
		return 1;
	}
	
	public default int numPeasantsNeeded()
	{
		return 1;
	}
}