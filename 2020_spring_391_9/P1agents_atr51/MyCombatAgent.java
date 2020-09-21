package edu.cwru.sepia.agent;

import java.io.InputStream;
import java.util.Map;
import java.util.Random;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.DeathLog;
import edu.cwru.sepia.environment.model.history.EventLogger;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class MyCombatAgent extends Agent {
	
	private int enemyPlayerNum = 1;
	
	public MyCombatAgent(int playernum, String[] otherargs) {
		super(playernum);
		
		if(otherargs.length > 0)
		{
			enemyPlayerNum = new Integer(otherargs[0]);
		}
		
		System.out.println("Constructed MyCombatAgent");
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate,
			HistoryView statehistory) {
		// This stores the action that each unit will perform
		// if there are no changes to the current actions then this
		// map will be empty
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		// This is a list of all of your units
		// Refer to the resource agent example for ways of
		// differentiating between different unit types based on
		// the list of IDs
		List<Integer> myUnitIDs = newstate.getUnitIds(playernum);
		
		// This is a list of enemy units
		List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);
		
		//Enemy units that can be seen
		List<UnitView> enemyUnits = newstate.getUnits(enemyPlayerNum);
		
		//Will compare all enemies to this one to see if they are further left
		int closestEnemy = enemyUnitIDs.get(0);
		
		//We loop through all enemy units and determine which unit is closest,
		//so we can attack it
		for(UnitView view : enemyUnits)
		{
			if(view.getXPosition() < enemyUnits.get(closestEnemy).getXPosition())
			{
				closestEnemy = view.getID();
			}
		}
		
		
		if(enemyUnitIDs.size() == 0)
		{
			// Nothing to do because there is no one left to attack
			return actions;
		}
		for(Integer myUnitID : myUnitIDs)
        {
			//All units will be focused on the enemy closest to our army, to minimize exposure to turret and rest of enemy army.
			//There's no point in seeking out the most damaged enemy yet because this is only the start of the battle.
			actions.put(myUnitID, Action.createCompoundAttack(myUnitID, closestEnemy));
        }
		
		return actions;
	}

	@Override
	public Map<Integer, Action> middleStep(StateView newstate,
			HistoryView statehistory) {
		// This stores the action that each unit will perform
		// if there are no changes to the current actions then this
		// map will be empty
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		// This is a list of enemy units
		List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);
		
		//Enemy units that can be seen
		List<UnitView> enemyUnits = newstate.getUnits(enemyPlayerNum);
		
		//Enemy closest to the left hand side of the screen.
		//We will use this information to determine which enemy to attack.
		int closestEnemy = enemyUnitIDs.get(0);
		
		//Enemy who has taken the most damage
		int mostDamagedEnemy = enemyUnitIDs.get(0);
		
		//Determine both closest enemy, and most damaged enemy
		for(UnitView view : enemyUnits)
		{
			if(view.getXPosition() < enemyUnits.get(closestEnemy).getXPosition())
			{
				closestEnemy = view.getID();
			}
			if(view.getHP() <= enemyUnits.get(mostDamagedEnemy).getHP())
			{
				mostDamagedEnemy = view.getID();
			}
		}
		
		if(enemyUnitIDs.size() == 0)
		{
			// Nothing to do because there is no one left to attack
			return actions;
		}
		
		int currentStep = newstate.getTurnNumber();
		
		
		// go through the action history
		for(ActionResult feedback : statehistory.getCommandFeedback(playernum, currentStep-1).values())
		{
			// if the previous action is no longer in progress (either due to failure or completion)
			// then add a new action for this unit
			if(feedback.getFeedback() != ActionFeedback.INCOMPLETE)
			{
				//Evenly split up units into attacking closest enemy and attacking most damaged enemy
				int unitID = feedback.getAction().getUnitId();
				if(unitID % 2 == 0)
				{
					actions.put(unitID, Action.createCompoundAttack(unitID, closestEnemy));
				}
				else
				{
					actions.put(unitID, Action.createCompoundAttack(unitID, mostDamagedEnemy));
				}
				
			}
		}

		return actions;
	}

	@Override
	public void terminalStep(StateView newstate, HistoryView statehistory) {
		System.out.println("Finished the episode");
	}

	@Override
	public void savePlayerData(OutputStream os) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPlayerData(InputStream is) {
		// TODO Auto-generated method stub

	}

}
