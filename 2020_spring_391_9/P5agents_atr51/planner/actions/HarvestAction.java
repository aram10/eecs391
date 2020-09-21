package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.Resource;
import edu.cwru.sepia.util.Direction;

public class HarvestAction implements StripsAction {
	Peasant peasant;
	int resourceId;
	Position peasantPos;
	Position resourcePos;
	boolean hasResource;

	public HarvestAction(Peasant peasant, Resource resource) {
		this.peasant = peasant;
		this.resourceId = resource.getId();
		this.peasantPos = peasant.getPosition();
		this.resourcePos = resource.getPosition();
		this.hasResource = resource.hasRemaining();
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return hasResource && !peasant.hasResource() && peasantPos.equals(resourcePos);	
	}

	@Override
	public void applyAction(GameState state) {
		state.applyHarvestAction(this, peasant.getId(), resourceId);
	}

	@Override
	public boolean isDirectedAction() {
		return true;
	}
	
	@Override
	public Position getPositionForDirection() {
		return resourcePos;
	}
	
	@Override
	public Action[] createSepiaActions(Direction[] directions) {
		Action[] actions = {Action.createPrimitiveGather(peasant.getId(), directions[0])};
		return actions;
	}
	
	@Override
	public int[] getUnitIds() {
		int[] ids = {peasant.getId()};
		return ids;	
	}
	
	@Override
	public String toString()
	{
		return "Peasant with ID " + peasant.getId() + " harvests from resource with position (" + resourcePos.x + ", " + resourcePos.y + ")";
	}
}