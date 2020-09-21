import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.DamageLog;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class jsp130CombatAgent extends Agent {
	
	private int enemyPlayerNum = 1;

	public jsp130CombatAgent(int playernum, String[] otherargs) {
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
		
		// These will store the types of units of my army
		List<Integer> myArcherIds = new ArrayList<Integer>();
		List<Integer> myFootmanIds = new ArrayList<Integer>();
		List<Integer> myBallistaIds = new ArrayList<Integer>();

		// This loop will examine each of our unit IDs and classify them
		for(Integer unitID : myUnitIDs)
		{
			// UnitViews extract information about a specified unit id
			// from the current state. Using a unit view you can determine
			// the type of the unit with the given ID as well as other information
			// such as health and resources carried.
			UnitView unit = newstate.getUnit(unitID);
			
			// To find properties that all units of a given type share
			// access the UnitTemplateView using the `getTemplateView()`
			// method of a UnitView instance. In this case we are getting
			// the type name so that we can classify our units
			String unitTypeName = unit.getTemplateView().getName();
			
			if(unitTypeName.equals("Footman"))
				myFootmanIds.add(unitID);
			else if(unitTypeName.equals("Archer"))
				myArcherIds.add(unitID);
			else if(unitTypeName.equals("Ballista"))
				myBallistaIds.add(unitID);
			else
				System.err.println("Unexpected Unit type: " + unitTypeName);
		}
		// This is a list of enemy units
		List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);
		
		if(enemyUnitIDs.size() == 0)
		{
			// Nothing to do because there is no one left to attack
			return actions;
		}
		
		// start by commanding the footmen to move just outside the range of the tower, archers behind them, ballista behind archers
		for(Integer footman : myFootmanIds)
		{
			actions.put(footman, Action.createCompoundMove(footman, 14, 5));
		}
		for(Integer archer : myArcherIds)
		{
			actions.put(archer, Action.createCompoundMove(archer, 11, 8));
		}
		for(Integer ballista : myBallistaIds)
		{
			actions.put(ballista, Action.createCompoundMove(ballista, 9, 9));
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
		
		// This is a list of enemy units as a UnitView for easier access later
		List<UnitView> enemyUnits = newstate.getUnits(enemyPlayerNum);


		//Enemy closest will be set initially to the first enemy
		int closestEnemy = enemyUnitIDs.get(0);
		
		//most damaged enemy will be set to the first enemy initially
		int lowEnemy = enemyUnitIDs.get(0);
		
		//Determine the closest enemy based on distance and the most damaged enemy, ignore the tower
		for(UnitView enemyUnit : enemyUnits)
		{
			if (enemyUnit.getTemplateView().getName().equals("ScoutTower"))
			{
				continue;
			}
			//calculate distance of both new enemy and old closest enemy by x coordinate
			int newXCoord = enemyUnit.getXPosition();
			
			UnitView oldEnemy = enemyUnits.get(closestEnemy);
			
			int oldXCoord = oldEnemy.getXPosition();
			
			if(newXCoord < oldXCoord)
			{
				closestEnemy = enemyUnit.getID();
			}
			//make new enemy the "lowest unit" even if they are tied, so we can later compare to the first and see if they all have the same health
			if(enemyUnit.getHP() <= enemyUnits.get(lowEnemy).getHP())
			{
				lowEnemy = enemyUnit.getID();
			}
		}
		
		//make sure most damaged enemy is actually the most damaged. If all hp is equal, then set lowEnemy to -1 so we can ignore it
		if(enemyUnits.get(lowEnemy).getHP() == enemyUnits.get(0).getHP())
			lowEnemy = -1;
		
		if(enemyUnitIDs.size() == 0)
		{
			// Nothing to do because there is no one left to attack
			return actions;
		}
		
		int currentStep = newstate.getTurnNumber();
		
		// go through the action history
		for(ActionResult feedback : statehistory.getCommandFeedback(playernum, currentStep-1).values())
		{
			
			//get damage log
			List<DamageLog> damageLogs = statehistory.getDamageLogs(currentStep);
			//get units damaged
			List<Integer> damagedIds = new ArrayList<Integer>();
			for (DamageLog damageLog : damageLogs)
			{
				int damagedId = damageLog.getDefenderID();
				//ensure unit is still alive
				UnitView unit = newstate.getUnit(damagedId);
				if(unit.getHP()>0)
				damagedIds.add(damagedId);
			}
			//units damaged move back
			for (Integer damagedId : damagedIds)
			{
				UnitView unit = newstate.getUnit(damagedId);
				actions.put(damagedId, Action.createCompoundMove(damagedId, 0, 29));
			}
	
			
			// if the previous action is no longer in progress (either due to failure or completion)
			// then add a new action for this unit
			if(feedback.getFeedback() != ActionFeedback.INCOMPLETE)
			{
				
				// get unit
				int unitID = feedback.getAction().getUnitId();
				
				//attack lowest enemy if one exists
				if (lowEnemy != -1)
				{
					actions.put(unitID, Action.createCompoundAttack(unitID, lowEnemy));
				}
				//otherwise attack the closest enemy
				else
				{
					actions.put(unitID, Action.createCompoundAttack(unitID, closestEnemy));
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