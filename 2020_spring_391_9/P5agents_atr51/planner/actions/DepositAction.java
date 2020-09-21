package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class DepositAction implements StripsAction {
	
	private int peasantId;
	private Peasant peasant;
	private Position peasantPos;
	private Position townHallPos = GameState.townhallPos;
	private boolean hasResource;
	
	public DepositAction(Peasant peasant) {
		this.peasant = peasant;
		this.peasantId = peasant.getId();
		this.peasantPos = peasant.getPosition();
		this.hasResource = peasant.hasResource();
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		this.hasResource = peasant.hasResource();
		return hasResource && peasantPos.equals(townHallPos);
	}

	@Override
	public void applyAction(GameState state) {
		state.applyDepositAction(this, peasantId);
	}

	@Override
	public boolean isDirectedAction() {
		return true;
	}
	
	@Override
	public Position getPositionForDirection() {
		return townHallPos;
	}
	
	@Override
	public Action[] createSepiaActions(Direction[] directions) {
		Action[] actions = {Action.createPrimitiveDeposit(peasantId, directions[0])};
		return actions;
	}
	
	@Override
	public int[] getUnitIds() {
		int[] ids = {peasantId};
		return ids;	
	}
	
	@Override
	public String toString()
	{
		String carry = (peasant.hasGold()) ? "gold" : "wood";
		return "Peasant with ID " + peasantId + " depositing " + carry + " at town hall.";
	}

}