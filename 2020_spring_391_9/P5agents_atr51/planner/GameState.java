package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.BuildAction;
import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.Deposit_kAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.Harvest_kAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.Move_kAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class GameState implements Comparable<GameState> {

	//player number
	private static int playerNum;

	//the goal amount of gold and wood to be successful
	private static int requiredGold;
	private static int requiredWood;

	public static int peasantTemplateId;

	//the amount of gold and wood we have now
	private int totalGold;
	private int totalWood;

	//map dimensions
	private int xExtent;
	private int yExtent;
	
	//this value can be changed - intended to stop wanton creation of peasants which would likely prevent victory
	private static final int MAX_NUM_PEASANTS = 3;

	//for creating new peasants
	private int nextId = 0;

	//peasants and resources
	private Map<Integer, Peasant> peasants = new HashMap<Integer, Peasant>(3);
	private Map<Integer, Resource> resources = new HashMap<Integer, Resource>(7);

	private List<Integer> unitIds = new ArrayList<Integer>();

	//townhall ID
	public static int townhallId;

	//location of townhall
	public static Position townhallPos;

	//whether or not we can build peasants
	private static boolean buildPeasants;

	//the set of actions that got us to this GameState
	private ArrayList<StripsAction> plan = new ArrayList<StripsAction>();

	protected double cost;
	protected double heuristic;
	



	/**
	 * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
	 * nodes should be constructed from the another constructor you create or by factory functions that you create.
	 *
	 * @param state The current stateview at the time the plan is being created
	 * @param playernum The player number of agent that is planning
	 * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
	 * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
	 * @param buildPeasants True if the BuildPeasant action should be considered
	 */
	public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {

		GameState.playerNum = playernum;
		GameState.requiredGold = requiredGold;
		GameState.requiredWood = requiredWood;
		GameState.buildPeasants = buildPeasants;
		this.xExtent = state.getXExtent();
		this.yExtent = state.getYExtent();
		for(UnitView unit : state.getAllUnits())
		{
			if(unit.getTemplateView().getName().toLowerCase().equals("townhall"))
			{
				GameState.townhallId = unit.getID();
				GameState.townhallPos = new Position(unit.getXPosition(), unit.getYPosition());
				unitIds.add(townhallId);
			}
			if(unit.getTemplateView().getName().toLowerCase().equals("peasant"))
			{
				GameState.peasantTemplateId = unit.getTemplateView().getID();
				Peasant temp = new Peasant(unit.getID(), new Position(unit.getXPosition(), unit.getYPosition()));
				peasants.put(temp.getId(), temp);
				unitIds.add(unit.getID());
			}
		}
		for(Integer id : state.getAllResourceIds())
		{
			ResourceView resource = state.getResourceNode(id);
			if(resource.getType() == Type.TREE)
			{
				Wood wood = new Wood(resource.getID(), resource.getAmountRemaining(), new Position(resource.getXPosition(), resource.getYPosition()));
				resources.put(wood.getId(), wood);
			}
			if(resource.getType() == Type.GOLD_MINE)
			{
				Gold gold = new Gold(resource.getID(), resource.getAmountRemaining(), new Position(resource.getXPosition(), resource.getYPosition()));
				resources.put(gold.getId(), gold);
			}
		}
		this.cost = 0.0;
		this.heuristic = 0.0;
		this.nextId = 1 + this.peasants.size() + this.resources.size();
	}

	/*
	 * This constructor generates a GameState given another GameState. After the initial GameState is created, this constructor
	 * is called to create all new GameStates
	 */
	public GameState(GameState state)
	{
		this.nextId = state.getNextId();
		this.cost = state.cost;
		this.totalGold = state.totalGold;
		this.totalWood = state.totalWood;
		state.peasants.values().stream().forEach(e -> this.peasants.put(e.getId(), new Peasant(e)));
		state.resources.values().stream().forEach(e -> {
			if(e.isGold()) 
			{
				this.resources.put(e.getId(), new Gold(e));
			} 
			else 
			{
				this.resources.put(e.getId(), new Wood(e));
			}
		});
		state.plan.stream().forEach(e -> plan.add(e));
	}

	/**
	 * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
	 * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
	 * this function to check if the goal conditions are met and return true if they are.
	 *
	 * @return true if the goal conditions are met in this instance of game state.
	 */
	public boolean isGoal() {

		if(this.totalGold >= GameState.requiredGold && this.totalWood >= GameState.requiredWood + 200)
		{
			return true;
		}
		return false;
	}

	/**
	 * The branching factor of this search graph are much higher than the planning. Generate all of the possible
	 * successor states and their associated actions in this method.
	 *
	 * @return A list of the possible successor states and their associated actions
	 */
	/**
	 * The branching factor of this search graph are much higher than the planning. Generate all of the possible
	 * successor states and their associated actions in this method.
	 *
	 * @return A list of the possible successor states and their associated actions
	 */
	public List<GameState> generateChildren() {
		List<GameState> children = new ArrayList<GameState>();
		//if we can build another peasant, this ought take priority
		if(this.canBuild() && GameState.buildPeasants)
		{
			GameState child = new GameState(this);
			BuildAction action = new BuildAction(GameState.townhallId, GameState.peasantTemplateId);
			if(action.preconditionsMet(child))
			{
				child = action.apply(child);
				children.add(child);
			}
			return children;
		}
		//map from resource nodes to the peasants that are located there and can harvest
		HashMap<Peasant, Resource> peasantsAtResource = new HashMap<Peasant, Resource>();
		//peasants at the townhall, carrying resources
		ArrayList<Peasant> peasantsAtTownhall = new ArrayList<Peasant>();
		GameState harvestChild = new GameState(this);
		//sort peasants by whether they can deposit or harvest
		for(Peasant peasant : this.peasants.values())
		{
			if(this.peasantAtTownhall(peasant) && peasant.hasResource())
			{
				peasantsAtTownhall.add(peasant);
			}
			else
			{
				for(Resource resource : this.resources.values())
				{
					if(peasant.getPosition().equals(resource.getPosition()))
					{
						peasantsAtResource.put(peasant, resource);
					}
				}
			}
		}
		//consider all peasants at a given resource to queue a harvest_k
		for(Resource resource : this.resources.values())
		{
			ArrayList<Peasant> pList = this.getAllPeasantsAtResource(peasantsAtResource, resource);
			if(pList.size() > 1)
			{
				Harvest_kAction harvestk = new Harvest_kAction(pList, resource);
				if(harvestk.preconditionsMet(harvestChild))
				{
					harvestChild = harvestk.apply(harvestChild);
					children.add(harvestChild);
					
				}
			}
		}
		//consider all peasants ready to deposit to queue a deposit_k
		if(peasantsAtTownhall.size() > 1)
		{
			GameState depositChild = new GameState(this);
			Deposit_kAction depositk = new Deposit_kAction(peasantsAtTownhall);
			if(depositk.preconditionsMet(depositChild))
			{
				depositChild = depositk.apply(depositChild);
				children.add(depositChild);
			}
		}
		//anyone who cannot harvest or deposit needs to move; consier first move_k actions
		ArrayList<Peasant> everyoneElse = new ArrayList<Peasant>();
		for(Peasant peasant : this.peasants.values())
		{
			if((!peasantsAtResource.containsKey(peasant) || peasant.hasResource()) && (!peasantsAtTownhall.contains(peasant) || !peasant.hasResource()))
			{
				everyoneElse.add(peasant);
			}
		}
		if(everyoneElse.size() > 1)
		{
			GameState moveChild = new GameState(this);
			Resource closest = this.getAverageClosestResourceNode(everyoneElse);
			Move_kAction movek = new Move_kAction(everyoneElse, closest.getPosition());
			if(movek.preconditionsMet(moveChild))
			{
				moveChild = movek.apply(moveChild);
				children.add(moveChild);
			}
		}
		/*
		 * Everything below concerns singleton actions.
		 */
		GameState child = new GameState(this);
		for(Peasant peasant : this.peasants.values())
		{
			if(peasant.hasGold() || peasant.hasWood())
			{
				if(peasant.getPosition().equals(GameState.townhallPos))
				{
					DepositAction action = new DepositAction(peasant);
					if(action.preconditionsMet(child))
					{
						child = action.apply(child);
					}
				}
				else 
				{
					MoveAction action = new MoveAction(peasant, GameState.townhallPos);
					if(action.preconditionsMet(child))
					{
						child = action.apply(child);
					}
				}
			}
			else if(peasantCanHarvest(peasant))
			{
				for(Resource resource : this.resources.values())
				{
					HarvestAction action = new HarvestAction(peasant, resource);
					if(action.preconditionsMet(child))
					{
						child = action.apply(child);
					}
				}
			}
			else
			{
				for(Resource resource : this.resources.values())
				{
					GameState innerChild = new GameState(child);
					MoveAction action = new MoveAction(peasant, resource.getPosition());
					if(action.preconditionsMet(innerChild))
					{
						innerChild = action.apply(innerChild);
					}
					for(Peasant other : this.peasants.values())
					{
						if(!other.equals(peasant) && !other.hasResource() && !peasantCanHarvest(peasant))
						{
							if(resource.getAmountLeft() >= 200)
							{
								MoveAction otherAction = new MoveAction(other, resource.getPosition());
								if(otherAction.preconditionsMet(innerChild))
								{
									innerChild = otherAction.apply(innerChild);
								}
							}
						}
					}
					children.add(innerChild);
				}
			}
		}
		children.add(child);
		for(Peasant peasant : this.peasants.values())
		{
			GameState innerChild = new GameState(this);
			DepositAction depositAction = new DepositAction(peasant);
			if(depositAction.preconditionsMet(innerChild))
			{
				innerChild = depositAction.apply(innerChild);
			}
			
			for(Resource resource : this.resources.values())
			{
				GameState innerInnerChild = new GameState(innerChild);
				StripsAction action = null;
				if(peasant.getPosition().equals(resource.getPosition()))
				{
					action = new HarvestAction(peasant, resource);
				}
				else
				{
					action = new MoveAction(peasant, resource.getPosition());
				}
				if(action.preconditionsMet(innerInnerChild))
				{
					innerInnerChild = action.apply(innerInnerChild);
				}
				children.add(innerInnerChild);
			}
			
			MoveAction moveAction = new MoveAction(peasant, GameState.townhallPos);
			if(moveAction.preconditionsMet(innerChild))
			{
				innerChild = moveAction.apply(innerChild);
			}
			children.add(innerChild);
		}
		
		return children;
	}
	
	/*
	 * Given a HarvestAction, a peasant id, and a resource id, modifies the current state
	 * to reflect a harvest of the peasant with id peasantid harvesting from resource with
	 * id resourceid.
	 */
	public void applyHarvestAction(StripsAction action, int peasantId, int resourceId) 
	{
		Peasant peasant = getPeasantWithId(peasantId);
		Resource resource = getResourceWithId(resourceId);
		if(resource.isWood()) 
		{
			peasant.setNumWood(Math.min(100, resource.getAmountLeft()));
			resource.setAmountLeft(Math.max(0, resource.getAmountLeft() - 100));
			return;
		} 
		peasant.setNumGold(Math.min(100, resource.getAmountLeft()));
		resource.setAmountLeft(Math.max(0, resource.getAmountLeft() - 100));
	}
	
	/*
	 * Given a Harvest_k StripsAction, peasant ids, and a resource ids, updates the current state
	 * to reflect a compound harvest.
	 */
	public void applyCompoundHarvestAction(StripsAction action, int[] peasantIds, int resourceId)
	{
		Resource resource = getResourceWithId(resourceId);
		ArrayList<Peasant> peasants = new ArrayList<Peasant>(peasantIds.length);
		for(int i = 0; i < peasantIds.length; i++)
		{
			peasants.add(getPeasantWithId(peasantIds[i]));
		}
		int amountLeft = resource.getAmountLeft();
		if(resource.isWood())
		{
			for(Peasant peasant : peasants)
			{
				if(amountLeft > 0)
				{
					int amountToTake = Math.min(100, amountLeft);
					peasant.setNumWood(amountToTake);
					amountLeft -= amountToTake;
					resource.setAmountLeft(Math.max(0, amountLeft));
				}
			}
			return;
		}
		for(Peasant peasant : peasants)
		{
			if(amountLeft > 0)
			{
				int amountToTake = Math.min(100, amountLeft);
				peasant.setNumGold(Math.min(100, amountToTake));
				amountLeft -= amountToTake;
				resource.setAmountLeft(Math.max(0, amountLeft));
			}
			
		}
	}
	
	/*
	 * Changes the current position of the peasant in question.
	 */
	public void applyMoveAction(StripsAction action, int peasantId, Position destination) 
	{
		Peasant peasant = this.getPeasantWithId(peasantId);
		peasant.setPosition(destination);
	}
	
	/*
	 * Moves the list of peasants to the given position.
	 */
	public void applyCompoundMoveAction(StripsAction action, int[] peasantIds, Position destination)
	{
		for(int i = 0; i < peasantIds.length; i++)
		{
			Peasant temp = this.getPeasantWithId(peasantIds[i]);
			temp.setPosition(destination);
		}
	}

	/*
	 * Adds to the total gold/wood based on what the peasant is carrying.
	 */
	public void applyDepositAction(StripsAction action, int peasantId) 
	{
		Peasant peasant = getPeasantWithId(peasantId);
		if(peasant.hasGold()) 
		{
			this.totalGold += peasant.getNumGold();
			peasant.setNumGold(0);
		} 
		if(peasant.hasWood()) 
		{
			this.totalWood += peasant.getNumWood();
			peasant.setNumWood(0);
		}
	}
	
	/*
	 * Adds to total wood and gold the sum of the inventories of the peasants.
	 */
	public void applyCompoundDepositAction(StripsAction action, int[] peasantIds)
	{
		for(int i = 0; i < peasantIds.length; i++)
		{
			Peasant temp = getPeasantWithId(peasantIds[i]);
			if(temp.hasGold()) 
			{
				this.totalGold += temp.getNumGold();
				temp.setNumGold(0);
			} 
			if(temp.hasWood()) 
			{
				this.totalWood += temp.getNumWood();
				temp.setNumWood(0);
			}
		}
	}
	
	/*
	 * Spawns a new peasant, decrements gold accordingly.
	 */
	public void applyBuildAction(StripsAction action) 
	{
		this.totalGold = this.totalGold - 400;
		Peasant peasant = new Peasant(nextId, new Position(GameState.townhallPos));
		nextId++;
		this.peasants.put(peasant.getId(), peasant);
	}
	

	/*
	 * Adds the StripsAction provided to the current plan, and updates
	 * the cost accordingly
	 */
	public void updatePlanAndCost(StripsAction action)
	{
		this.plan.add(action);
		if(action instanceof MoveAction)
		{
			MoveAction moveAction = (MoveAction) action;
			this.cost += moveAction.getCost();
		}
		else if(action instanceof Move_kAction)
		{
			Move_kAction movekAction = (Move_kAction) action;
			this.cost += movekAction.getCost();
		}
		else
		{
			this.cost += 1;
		}
	}
	
	private Peasant getPeasantWithId(int peasantId) {
		return this.peasants.get(peasantId);
	}
	
	private Resource getResourceWithId(int resourceId) {
		return this.resources.get(resourceId);
	}


	/**
	 * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
	 * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
	 *
	 * Add a description here in your submission explaining your heuristic.
	 *
	 * @return The value estimated remaining cost to reach a goal state from this state.
	 */
	/*
	 * This heuristic takes into account the current wood, current gold, the dispositions of the
	 * peasants (can they harvest, are they next to a resource/townhall, do they have full inventories),
	 * and whether or not more peasants are allowed to be built. The more wood and gold a state has,
	 * the more peasants poised to harvest with empty inventories or deposit full inventories at the 
	 * townhall, and the more peasants in existence, the closer we are to victory.
	 */
	public double heuristic() {
		if(this.isGoal())
		{
			this.heuristic = Double.NEGATIVE_INFINITY;
			return heuristic;
		}
		
		if(this.heuristic != 0) 
		{
			return heuristic;
		}
		
		double sum = 0;
		
		if(totalWood <= requiredWood) 
		{
			sum += (requiredWood - totalWood);
		} 
		else 
		{
			sum += totalWood - requiredWood;
		}
		
		if(totalGold <= requiredGold) 
		{
			sum += (requiredGold - totalGold);
		} 
		else 
		{
			sum += (totalGold - requiredGold);
		}
		
		if(totalWood > totalGold) 
		{
			sum += 100;
		}
		
		for(Peasant peasant : this.peasants.values()) 
		{
			if(peasant.hasResource()) 
			{
				sum -= peasant.getNumGold() + peasant.getNumWood();
			} 
			else 
			{
				if(peasantCanHarvest(peasant)) 
				{
					sum -= 50;
				} 
				else if(!isResourceLocation(peasant.getPosition())) 
				{
					sum += 100;
				}
			}
		}
		
		if(buildPeasants) 
		{
			sum += (MAX_NUM_PEASANTS - this.peasants.size()) * 1000;
			if(canBuild())
			{
				sum -= 1000;
			}
		}
		
		this.heuristic = sum;
		return this.heuristic;
	}
	
	/*
	 * Returns true if the peasant is standing at a resource node, false otherwise.
	 */
	public boolean peasantAtResourceNode(Peasant peasant)
	{
		for(Resource resource : this.resources.values())
		{
			if(resource.getPosition().equals(peasant.getPosition()) || resource.getPosition().isAdjacent(peasant.getPosition()))
			{
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Returns true if the peasant is at the townhall, false otherwise.
	 */
	public boolean peasantAtTownhall(Peasant peasant)
	{
		if(peasant.getPosition().equals(GameState.townhallPos) || peasant.getPosition().isAdjacent(GameState.townhallPos))
		{
			return true;
		}
		return false;
	}
	
	/*
	 * Used in calculation of heuristic.
	 */
	public double furthestResourceDistanceFromTownhall()
	{
		double maxDistance = Double.NEGATIVE_INFINITY;
		for(Resource resource : this.resources.values())
		{
			double temp = resource.getPosition().chebyshevDistance(GameState.townhallPos);
			if(temp > maxDistance)
			{
				maxDistance = temp;
			}
		}
		return maxDistance;
	}

	/**
	 *
	 * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
	 * determine which actions/states are better to explore.
	 *
	 * @return The current cost to reach this goal
	 */
	public double getCost() {
		return this.cost;
	}

	/**
	 * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
	 * interface documentation to learn how this function should work.
	 *
	 * @param o The other game state to compare
	 * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
	 */
	@Override
	public int compareTo(GameState o) {
		if(this.heuristic() > o.heuristic())
		{
			return 1;
		}
		else if(this.heuristic() < o.heuristic())
		{
			return -1;
		}
		else return 0;
	}

	/**
	 * This will be necessary to use the GameState as a key in a Set or Map.
	 *
	 * @param o The game state to compare
	 * @return True if this state equals the other state, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		GameState gs;
		try
		{
			gs = (GameState) o;
		}
		catch(ClassCastException e) { return false; }
		
		if(gs.cost != gs.getCost())
		{
			return false;
		}
		if(this.getCurrentGold() != gs.getCurrentGold())
		{
			return false;
		}
		if(this.getCurrentWood() != gs.getCurrentWood())
		{
			return false;
		}
		if(this.getPeasants().size() != gs.getPeasants().size())
		{
			return false;
		}
		if(this.heuristic() != gs.heuristic())
		{
			return false;
		}
		if(!peasants.equals(gs.peasants))
		{
			return false;
		}
		
		return true;
	}

	/**
	 * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
	 * equal they should hash to the same value.
	 *
	 * @return An integer hashcode that is equal for equal states.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getCurrentGold();
		result = prime * result + this.getCurrentWood();
		result = prime * result + ((peasants == null) ? 0 : peasants.hashCode());
		return result;
	}

	public int getXExtent()
	{
		return this.xExtent;
	}

	public int getYExtent()
	{
		return this.yExtent;
	}

	public Map<Integer, Resource> getResources()
	{
		return this.resources;
	}

	public Map<Integer, Peasant> getPeasants()
	{
		return this.peasants;
	}

	public static int getPlayerNum()
	{
		return GameState.playerNum;
	}

	public static int getRequiredGold()
	{
		return GameState.requiredGold;
	}

	public static int getRequiredWood()
	{
		return GameState.requiredWood;
	}

	public boolean canBuild()
	{
		return totalGold >= 400 && this.peasants.size() < MAX_NUM_PEASANTS;
	}

	public static int getTownhallId()
	{
		return GameState.townhallId;
	}

	public static Position getTownhallPos()
	{
		return GameState.townhallPos;
	}
	
	public Stack<StripsAction> getPlan()
	{
		Stack<StripsAction> plan = new Stack<StripsAction>();
		for(int i = this.plan.size() - 1; i > -1; i--) {
			plan.push(this.plan.get(i));
		}
		return plan;
	}

	public int getCurrentGold()
	{
		return this.totalGold;
	}

	public int getCurrentWood()
	{
		return this.totalWood;
	}

	public int getNextId()
	{
		return nextId;
	}
	
	private boolean peasantCanHarvest(Peasant peasant) 
	{
		return isResourceLocation(peasant.getPosition()) && getResourceForPosition(peasant.getPosition()).getAmountLeft() > 0;
	}
	
	/*
	 * Checks if the given position is a resource.
	 */
	private boolean isResourceLocation(Position destination) 
	{ 
		for(Resource resource : this.resources.values())
		{
			if(resource.getPosition().equals(destination))
			{
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Finds the resource at the given position. If no such
	 * resource exists, returns null.
	 */
	private Resource getResourceForPosition(Position position) 
	{
		for(Resource resource : this.resources.values())
		{
			if(resource.getPosition().equals(position))
			{
				return resource;
			}
		}
		return null;
	}

	/*
	 * Creates a new peasant with a unique ID and adds it to the list.
	 */
	public void generatePeasant()
	{
		Peasant p = new Peasant(nextId, GameState.townhallPos);
		nextId++;
		this.peasants.put(p.getId(), p);
	}
	
	/*
	 * Helper method for getting all peasants at a particular resource.
	 */
	private ArrayList<Peasant> getAllPeasantsAtResource(Map<Peasant, Resource> map, Resource resource) 
	{
		ArrayList<Peasant> listOfKeys = new ArrayList<Peasant>();
		if(map.containsValue(resource))
		{
			for (Map.Entry<Peasant, Resource> entry : map.entrySet()) 
			{
				if (entry.getValue().equals(resource))
				{
					listOfKeys.add(entry.getKey());
				}
			}
		}
		return listOfKeys;	
	}
	
	/*
	 * The Chebyshev distances between all considered peasants and every resource node are compiled and averaged;
	 * the resource node with the smallest average distance is returned.
	 */
	private Resource getAverageClosestResourceNode(ArrayList<Peasant> peasants)
	{
		HashMap<Resource, Integer> distances = new HashMap<Resource, Integer>();
		for(Resource resource : this.resources.values())
		{
			distances.put(resource, 0);
		}
		for(Resource resource : distances.keySet())
		{
			double sum = 0;
			for(Peasant peasant : peasants)
			{
				sum += resource.getPosition().chebyshevDistance(peasant.getPosition());
			}
			distances.replace(resource, (int) sum / peasants.size());
		}
		Integer smallest =  Collections.min(distances.values());
		return distances.entrySet().stream().filter(entry -> smallest.equals(entry.getValue())).map(Map.Entry::getKey).findFirst().get();
	}


}
