package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.Collection;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.util.Direction;

public class Deposit_kAction implements StripsAction
{
	
	private ArrayList<Peasant> peasants = new ArrayList<Peasant>();
	private Position townHallPos = GameState.townhallPos;
	
	public Deposit_kAction(Collection<Peasant> peasants)
	{
		this.peasants.addAll(peasants);
	}

	@Override
	public boolean preconditionsMet(GameState state) 
	{
		for(Peasant peasant : this.peasants)
		{
			if((!peasant.hasGold() && !peasant.hasWood()) || !peasant.getPosition().equals(townHallPos))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void applyAction(GameState state) 
	{
		state.applyCompoundDepositAction(this, this.getUnitIds());
	}

	@Override
	public Action[] createSepiaActions(Direction[] directions) 
	{
		if(directions.length != this.peasants.size())
		{
			throw new IllegalArgumentException("Number of directions for primitive deposits must equal number of peasants.");
		}
		Action[] actions = new Action[this.peasants.size()];
		for(int i = 0; i < actions.length; i++)
		{
			actions[i] = Action.createPrimitiveDeposit(this.peasants.get(i).getId(), directions[i]);
		}
		return actions;
	}

	@Override
	public int[] getUnitIds() 
	{
		ArrayList<Integer> ids = new ArrayList<Integer>(this.peasants.size());
		this.peasants.stream().forEach((p) -> ids.add(p.getId()));
		return ids.stream().mapToInt(Integer::intValue).toArray();
	}
	
	@Override
	public int numPeasantsNeeded()
	{
		return this.peasants.size();
	}
	
	@Override
	public boolean isParallelAction()
	{
		return true;
	}
	
	@Override
	public boolean isDirectedAction()
	{
		return true;
	}
	
	@Override
	public Position getPositionForDirection() {
		return townHallPos;
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
		
		return "Peasants with ids " + concatIds.toString() + " depositing at the town hall.";
	}

}
