import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.LocatedProductionAction;
import edu.cwru.sepia.action.ProductionAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class jsp130ResourceAgent extends Agent {

	private static final long serialVersionUID = -7481143097108592969L;

	public jsp130ResourceAgent(int playernum) {
		super(playernum);
				
		System.out.println("Constructed My First Agent");
	}

	public Map initialStep(StateView newstate, HistoryView statehistory) {
		return middleStep(newstate, statehistory);
	}

	public Map middleStep(StateView newstate, HistoryView statehistory) {
		// This stores the action that each unit will perform
		// if there are no changes to the current actions then this
		// map will be empty.
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		
		// this will return a list of all of your units
		// You will need to check each unit ID to determine the unit's type
		List<Integer> myUnitIds = newstate.getUnitIds(playernum);
		
		// These will store the Unit IDs that are peasants, townhalls, footmen, farms, and barracks respectively
		List<Integer> peasantIds = new ArrayList<Integer>();
		List<Integer> townhallIds = new ArrayList<Integer>();
		List<Integer> footmanIds = new ArrayList<Integer>();
		List<Integer> farmIds = new ArrayList<Integer>();
		List<Integer> barracksIds = new ArrayList<Integer>();
		
		// This loop will examine each of our unit IDs and classify them
		for(Integer unitID : myUnitIds)
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
			
			if(unitTypeName.equals("TownHall"))
				townhallIds.add(unitID);
			else if(unitTypeName.equals("Peasant"))
				peasantIds.add(unitID);
			else if(unitTypeName.equals("Footman"))
				footmanIds.add(unitID);
			else if(unitTypeName.equals("Farm"))
				farmIds.add(unitID);
			else if(unitTypeName.equals("Barracks"))
				barracksIds.add(unitID);
			else
				System.err.println("Unexpected Unit type: " + unitTypeName);
		}
		
		// get the amount of wood and gold you have in your Town Hall
		int currentGold = newstate.getResourceAmount(playernum, ResourceType.GOLD);
		int currentWood = newstate.getResourceAmount(playernum, ResourceType.WOOD);
		
		List<Integer> goldMines = newstate.getResourceNodeIds(Type.GOLD_MINE);
		List<Integer> trees = newstate.getResourceNodeIds(Type.TREE);
		
		//IDs for buildings for action parameters
		TemplateView farmTemplate = newstate.getTemplate(playernum, "Farm");
		int farmTemplateID = farmTemplate.getID();
		
		TemplateView barracksTemplate = newstate.getTemplate(playernum, "Barracks");
		int barracksTemplateID = barracksTemplate.getID();
		
		TemplateView footmanTemplate = newstate.getTemplate(playernum, "Footman");
		int footmanTemplateID = footmanTemplate.getID();
		
		//knowing whether another peasant is building a farm/barracks, so that only one may be built at a time
		boolean peasantBuildingFarm = false;
		boolean peasantBuildingBarracks = false;
		
		//determine where to build a farm/barracks, constantly moving to the right after building so the peasant doesn't get boxed in
		//keep track of how many rows are filled
		int fullRowsFarms = 0;
		int fullRowsBarracks = 0;
		int xCoordFarm = 1 + farmIds.size() - (fullRowsFarms * 25);
		int yCoordFarm = 0;
		
		int xCoordBarracks = 1 + barracksIds.size() - (fullRowsBarracks * 25);
		int yCoordBarracks = 15;
		
		//if we have reached the edge of the map, move the y coordinate one down and reset the xCoord
		if (xCoordFarm >= 25)
		{
			xCoordFarm = 1;
			yCoordFarm += 1;
		}
		if (xCoordBarracks >= 25)
		{
			xCoordBarracks = 1;
			yCoordBarracks += 1;
		}
		
		// Now that we know the unit types we can assign our peasants to collect resources
		for(Integer peasantID : peasantIds)
		{
			Action action = null;
			if(newstate.getUnit(peasantID).getCargoAmount() > 0)
			{
				// If the agent is carrying cargo then command it to deposit what its carrying at the townhall.
				// Here we are constructing a new TargetedAction. The first parameter is the unit being commanded.
				// The second parameter is the action type, in this case a COMPOUNDDEPOSIT. The actions starting
				// with COMPOUND are convenience actions made up of multiple move actions and another final action
				// in this case DEPOSIT. The moves are determined using A* planning to the location of the unit
				// specified by the 3rd argument of the constructor.
				action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIds.get(0));
			}
			//if the number of farms is less than or equal to the number of barracks, and the peasant has enough resources accounting
			//for the other peasants currently building, then build a farm
			else if(farmIds.size() <= barracksIds.size() && !peasantBuildingFarm && currentGold >= 500 && currentWood >= 250)
			{
				peasantBuildingFarm = true;
				action = new LocatedProductionAction(peasantID, ActionType.COMPOUNDBUILD, farmTemplateID, xCoordFarm, yCoordFarm);
			}
			//if the number of farms is less than or equal to the number of barracks, and the peasant has enough resources accounting
			//for the other peasants currently building, then build a farm
			else if(barracksIds.size() < farmIds.size() && !peasantBuildingBarracks && currentGold >= 700 && currentWood >= 400)
			{
				peasantBuildingBarracks = true;
				action = new LocatedProductionAction(peasantID, ActionType.COMPOUNDBUILD, barracksTemplateID, xCoordBarracks, yCoordBarracks);
			}
			else
			{
				// If the agent isn't carrying anything instruct it to go collect either gold or wood
				// whichever you have less of
				if(currentGold < currentWood)
				{
					action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, goldMines.get(0));
				}
				else
				{
					action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, trees.get(0));
				}
			}
			
			
			// Put the actions in the action map.
			// Without this step your agent will do nothing.
			actions.put(peasantID, action);
		}
		// Build 2 more peasants
		if(peasantIds.size() < 3)
		{
		        // only try to build a new peasant if
		        // the agent possess's the required resources
		        // For a peasant that is 400 Gold
		        if(currentGold >= 400)
		        {
		                // Get the peasant template's unique ID
		                // this is how SEPIA identifies what type of unit to build
		                TemplateView peasantTemplate = newstate.getTemplate(playernum, "Peasant");
		                int peasantTemplateID = peasantTemplate.getID();

		                // Grab the first townhall
		                // this assumes there is at least one townhall in the map
		                int townhallID = townhallIds.get(0);

		                // create a new CompoundProduction action at the townhall. This instructs the specified townhall
		                // to build a unit with the peasant template ID.
		                actions.put(townhallID, Action.createCompoundProduction(townhallID, peasantTemplateID));
		        }
		}
		
		//build 1 footman once you have one barracks
		if(footmanIds.size() < 1 && barracksIds.size() >= 1)
		{
			if(currentGold >= 600)
			{
				//get the ID of the barracks and build a footman
				int barracksId = barracksIds.get(0);
				actions.put(barracksId, Action.createCompoundProduction(barracksId, footmanTemplateID));
			}
		}
		
		
		
		return actions;
	}

	public void terminalStep(StateView newstate, HistoryView statehistory) {
		System.out.println("Finsihed the episode");
	}

	public void savePlayerData(OutputStream os) {
		// this agent doesn't learn so nothing needs to be saved
	}

	public void loadPlayerData(InputStream is) {
		// this agent doesn't learn so nothing is loaded
	}

}