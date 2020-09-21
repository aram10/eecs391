package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Collection;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.Resource;
import edu.cwru.sepia.util.Direction;

public class Harvest_kAction implements StripsAction
{
	private ArrayList<Peasant> peasants = new ArrayList<Peasant>();
	
	private Position resourcePosition;
	
	private int resourceId;
	
	private boolean isEmpty;

	public Harvest_kAction(ArrayList<Peasant> peasants, Resource resource)
	{
		this.peasants = peasants;
		this.resourcePosition = resource.getPosition();
		this.resourceId = resource.getId();
		this.isEmpty = !resource.hasRemaining();
	}

	@Override
	public boolean preconditionsMet(GameState state) 
	{
		if(isEmpty)
		{
			return false;
		}
		for(Peasant peasant : this.peasants)
		{
			if(!peasant.getPosition().equals(resourcePosition))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void applyAction(GameState state) 
	{
		state.applyCompoundHarvestAction(this, this.getUnitIds(), resourceId);
	}

	@Override
	public Action[] createSepiaActions(Direction[] directions) 
	{
		if(directions.length != this.peasants.size())
		{
			throw new IllegalArgumentException("Number of directions for primitive gathers must equal number of peasants.");
		}
		Action[] actions = new Action[this.peasants.size()];
		for(int i = 0; i < actions.length; i++)
		{
			actions[i] = Action.createPrimitiveGather(this.peasants.get(i).getId(), directions[i]);
		}
		return actions;
	}

	@Override
	public int[] getUnitIds() 
	{
		int[] ids = new int[this.peasants.size()];
		for(int i = 0; i < ids.length; i++)
		{
			ids[i] = this.peasants.get(i).getId();
		}
		return ids;
	}
	
	@Override
	public int numPeasantsNeeded()
	{
		return this.peasants.size();
	}
	
	@Override
	public boolean isDirectedAction()
	{
		return true;
	}
	
	@Override
	public Position getPositionForDirection() 
	{
		return resourcePosition;
	}
	
	@Override
	public boolean isParallelAction()
	{
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder concatIds = new StringBuilder();
		concatIds.append("{");
		for(Peasant peasant : this.peasants)
		{
			concatIds.append(peasant.getId());
			concatIds.append(", ");
		}
		concatIds.deleteCharAt(concatIds.length() - 1);
		concatIds.deleteCharAt(concatIds.length() - 1);
		concatIds.append("}");
		
		return "Peasants with ids " + concatIds.toString() + " harvest from resource with position (" + this.resourcePosition.x + ", " + this.resourcePosition.y + ")";
	}
	
	public Position getResourcePosition()
	{
		return this.resourcePosition;
	}
	

}
