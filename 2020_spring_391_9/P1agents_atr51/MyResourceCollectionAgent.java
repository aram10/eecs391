package edu.cwru.sepia.agent;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class MyResourceCollectionAgent extends Agent {

    private static final long serialVersionUID = -7481143097108592969L;

    public MyResourceCollectionAgent(int playernum) {
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

        // These will store the Unit IDs that are peasants and townhalls respectively
        List<Integer> peasantIds = new ArrayList<Integer>();
        List<Integer> townhallIds = new ArrayList<Integer>();

        List<Integer> farmIds = new ArrayList<Integer>();
        List<Integer> barracksIds = new ArrayList<Integer>();
        List<Integer> footmenIds = new ArrayList<Integer>();

        //For picking random spots for building
        Random random = new Random();

        // This loop will examine each of our unit IDs and classify them as either
        // a Townhall or a Peasant
        for (Integer unitID : myUnitIds) {
            // UnitViews extract information about a specified unit id
            // from the current state. Using a unit view you can determine
            // the type of the unit with the given ID as well as other information
            // such as health and resources carried.
            UnitView unit = newstate.getUnit(unitID);

            // To find properties that all units of a given type share
            // access the UnitTemplateView using the `getTemplateView()`
            // method of a UnitView instance. In this case we are getting
            // the type name so that we can classify our units as Peasants and Townhalls
            String unitTypeName = unit.getTemplateView().getName();

            if (unitTypeName.equals("TownHall")) townhallIds.add(unitID);
            else if (unitTypeName.equals("Peasant")) peasantIds.add(unitID);
                //Now also classifying units as Farms, Barracks, and Footmen
            else if (unitTypeName.equals("Farm")) farmIds.add(unitID);
            else if (unitTypeName.equals("Barracks")) barracksIds.add(unitID);
            else if (unitTypeName.equals("Footman")) footmenIds.add(unitID);
            else System.err.println("Unexpected Unit type: " + unitTypeName);
        }

        // get the amount of wood and gold you have in your Town Hall
        int currentGold = newstate.getResourceAmount(playernum, ResourceType.GOLD);
        int currentWood = newstate.getResourceAmount(playernum, ResourceType.WOOD);

        List<Integer> goldMines = newstate.getResourceNodeIds(Type.GOLD_MINE);
        List<Integer> trees = newstate.getResourceNodeIds(Type.TREE);

        //These booleans make sure that two peasants are not trying to build a building at the same time,
        //which may result in one of the peasants being sorely disappointed that the town hall lacks 
        //sufficient resources.

        //templates for buildings
        TemplateView farmTemplate = newstate.getTemplate(playernum, "Farm");
        TemplateView barracksTemplate = newstate.getTemplate(playernum, "Barracks");

        //Fetching unique building IDs
        int barracksTemplateID = barracksTemplate.getID();
        int farmTemplateID = farmTemplate.getID();

        boolean peasantBuildingFarm = false;
        boolean peasantBuildingBarracks = false;

        // Now that we know the unit types we can assign our peasants to collect resources
        for (Integer peasantID : peasantIds) {
            //Now that we are building farms, we need to update our gold and wood
            currentGold = newstate.getResourceAmount(playernum, ResourceType.GOLD);
            currentWood = newstate.getResourceAmount(playernum, ResourceType.WOOD);
            Action action = null;
            if (newstate.getUnit(peasantID).getCargoAmount() > 0) {
                // If the agent is carrying cargo then command it to deposit what its carrying at the townhall.
                // Here we are constructing a new TargetedAction. The first parameter is the unit being commanded.
                // The second parameter is the action type, in this case a COMPOUNDDEPOSIT. The actions starting
                // with COMPOUND are convenience actions made up of multiple move actions and another final action
                // in this case DEPOSIT. The moves are determined using A* planning to the location of the unit
                // specified by the 3rd argument of the constructor.
                action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIds.get(0));
            } else if (barracksIds.size() <= 5 && currentGold >= 700 && currentWood >= 400 && !peasantBuildingBarracks) {
                peasantBuildingBarracks = true;
                //Pick some available spot closest to a random tile, build a farm there
                action = new ProductionAction(peasantID, ActionType.PRIMITIVEBUILD, barracksTemplateID);
            } else if (farmIds.size() <= 5 && barracksIds.size() >= 1 && currentGold >= 500 && currentWood >= 250 && !peasantBuildingFarm) {
                peasantBuildingFarm = true;
                // This compound build action will have a peasant construct a farm at some random tile
                action = new ProductionAction(peasantID, ActionType.PRIMITIVEBUILD, farmTemplateID);
            } else {
                // If the agent isn't carrying anything instruct it to go collect either gold or wood
                // whichever you have less of
                if (currentGold < currentWood) {
                    action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, goldMines.get(0));
                } else {
                    action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, trees.get(0));
                }
            }

            // Put the actions in the action map.
            // Without this step your agent will do nothing.
            actions.put(peasantID, action);
        }
        if (peasantIds.size() < 5) {
            // only try to build a new peasant if
            // the agent possess's the required resources
            // For a peasant that is 400 Gold
            if (currentGold >= 400) {
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
        if (footmenIds.size() < 3 && barracksIds.size() >= 1) {
            if (currentGold >= 600) {
                //get the template for the footman
                TemplateView footmanTemplate = newstate.getTemplate(playernum, "Footman");
                int footmanTemplateID = footmanTemplate.getID();
                //get the ID of the town hall
                int barracksId = barracksIds.get(0);
                //creating a footman
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