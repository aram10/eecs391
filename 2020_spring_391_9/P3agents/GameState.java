package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 * <p>
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {

    //best and worst possible utilities
    public static final double MAX_UTILITY = Double.POSITIVE_INFINITY;
    public static final double MIN_UTILITY = Double.NEGATIVE_INFINITY;

    //whether or not we've gotten the utility before
    boolean utilityCalculated = false;
    //the current state of the game
    State.StateView state;
    //dimensions of the map
    int xExtent;
    int yExtent;
    //list of resources
    List<ResourceView> resources = new ArrayList<ResourceView>();
    //whether or not it is the player's turn
    boolean playerTurn = true;
    List<Unit> goodGuys = new ArrayList<Unit>();
    List<Unit> badGuys = new ArrayList<Unit>();
    private double utility = 0.0;

    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     * <p>
     * You may find the following state methods useful:
     * <p>
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns the IDs of all of the obstacles in the map
     * state.getResourceNode(int resourceID): Return a ResourceView for the given ID
     * <p>
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     * <p>
     * You can get a list of all the units belonging to a player with the following command:
     * state.getUnitIds(int playerNum): gives a list of all unit IDs beloning to the player.
     * You control player 0, the enemy controls player 1.
     * <p>
     * In order to see information about a specific unit, you must first get the UnitView
     * corresponding to that unit.
     * state.getUnit(int id): gives the UnitView for a specific unit
     * <p>
     * With a UnitView you can find information about a given unit
     * unitView.getXPosition() and unitView.getYPosition(): get the current location of this unit
     * unitView.getHP(): get the current health of this unit
     * <p>
     * SEPIA stores information about unit types inside TemplateView objects.
     * For a given unit type you will need to find statistics from its Template View.
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit type deals
     * unitView.getTemplateView().getBaseHealth(): The initial amount of health of this unit type
     *
     * @param state Current state of the episode
     */

    /*
     * Things that are important in the representation of a state:
     * >Dimension of the map
     * >Locations of player's footmen
     * >Locations of enemy's archers
     * >Manhattan distance between each footman and each archer
     * 		-We can also use this information to determine whether or not
     * 		our footmen are in range to attack.
     * >Health of player's footmen
     * >Health of enemy's archers
     * >Actions our footmen can take at this position
     * >Actions enemy archers can take in this state
     *
     * This Constructor is called the first time a GameState is created
     */
    public GameState(State.StateView state) {

        this.state = state;

        //set map boundaries
        xExtent = state.getXExtent();
        yExtent = state.getYExtent();

        for (Integer i : state.getUnitIds(0)) {
            UnitView u = state.getUnit(i);
            int xPos = u.getXPosition();
            int yPos = u.getYPosition();
            int health = u.getHP();
            int range = u.getTemplateView().getRange();
            int maxHp = u.getTemplateView().getBaseHealth();
            int damage = u.getTemplateView().getBasicAttack();
            goodGuys.add(new Unit(0, i, xPos, yPos, health, maxHp, damage, range));
        }

        for (Integer i : state.getUnitIds(1)) {
            UnitView u = state.getUnit(i);
            int xPos = u.getXPosition();
            int yPos = u.getYPosition();
            int health = u.getHP();
            int range = u.getTemplateView().getRange();
            int maxHp = u.getTemplateView().getBaseHealth();
            int damage = u.getTemplateView().getBasicAttack();
            badGuys.add(new Unit(1, i, xPos, yPos, health, maxHp, damage, range));
        }

        this.resources = state.getAllResourceNodes();

    }


    /*
     * Constructor that is called for each subsequent GameState,
     * so that the current player can be set.
     */
    public GameState(GameState gameState) {
        this.state = gameState.state;

        //set map boundaries
        xExtent = state.getXExtent();
        yExtent = state.getYExtent();

        for (Integer i : state.getUnitIds(0)) {
            UnitView u = state.getUnit(i);
            int xPos = u.getXPosition();
            int yPos = u.getYPosition();
            int health = u.getHP();
            int range = u.getTemplateView().getRange();
            int maxHp = u.getTemplateView().getBaseHealth();
            int damage = u.getTemplateView().getBasicAttack();
            goodGuys.add(new Unit(0, i, xPos, yPos, health, maxHp, damage, range));
        }

        for (Integer i : state.getUnitIds(1)) {
            UnitView u = state.getUnit(i);
            int xPos = u.getXPosition();
            int yPos = u.getYPosition();
            int health = u.getHP();
            int range = u.getTemplateView().getRange();
            int maxHp = u.getTemplateView().getBaseHealth();
            int damage = u.getTemplateView().getBasicAttack();
            badGuys.add(new Unit(1, i, xPos, yPos, health, maxHp, damage, range));
        }

        this.resources = state.getAllResourceNodes();
        playerTurn = !gameState.playerTurn;
        this.utilityCalculated = gameState.utilityCalculated;
        this.utility = gameState.utility;
    }

    /**
     * You will implement this function.
     * <p>
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     * <p>
     * It may be useful to be able to create a SEPIA Action. In this assignment you will
     * deal with movement and attacking actions. There are static methods inside the Action
     * class that allow you to create basic actions:
     * Action.createPrimitiveAttack(int attackerID, int targetID): returns an Action where
     * the attacker unit attacks the target unit.
     * Action.createPrimitiveMove(int unitID, Direction dir): returns an Action where the unit
     * moves one space in the specified direction.
     * <p>
     * You may find it useful to iterate over all the different directions in SEPIA. This can
     * be done with the following loop:
     * for(Direction direction : Direction.values())
     * <p>
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     * <p>
     * If you wish to explicitly use a Direction you can use the Direction enum, for example
     * Direction.NORTH or Direction.NORTHEAST.
     * <p>
     * You can check many of the properties of an Action directly:
     * action.getType(): returns the ActionType of the action
     * action.getUnitID(): returns the ID of the unit performing the Action
     * <p>
     * ActionType is an enum containing different types of actions. The methods given above
     * create actions of type ActionType.PRIMITIVEATTACK and ActionType.PRIMITIVEMOVE.
     * <p>
     * For attack actions, you can check the unit that is being attacked. To do this, you
     * must cast the Action as a TargetedAction:
     * ((TargetedAction)action).getTargetID(): returns the ID of the unit being attacked
     *
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
        Collection<Unit> agentsActiveThisTurn;
        if (playerTurn) {
            agentsActiveThisTurn = goodGuys;
        } else {
            agentsActiveThisTurn = badGuys;
        }
        List<List<Action>> actionsForEachAgent = agentsActiveThisTurn.stream().map(e -> getAgentActions(e)).collect(Collectors.toList());
        List<Map<Integer, Action>> actionMaps = mapActions(actionsForEachAgent);
        return getAllStates(actionMaps);
    }

    //Given the list of footmen and archers, returns the max distance
    //between a footman and an archer
    public int calculateManhattan(List<Unit> footmen, List<Unit> archers) {
        int distanceSum = Integer.MAX_VALUE;
        for (Unit u1 : footmen) {
            for (Unit u2 : archers) {
                if (u1.isAlive() && u2.isAlive()) {
                    distanceSum = Math.min(distanceSum, (Math.abs(u2.getX() - u1.getX()) + Math.abs(u2.getY() - u1.getY())) - 1);
                }
            }
        }
        return distanceSum;
    }

    /*
     * Given a list of units, return a list containing the lists of valid
     * actions for each unit
     */
    public List<Action> getAgentActions(Unit unit) {
        //list of this unit's potential actions
        List<Action> unitActions = new ArrayList<Action>();
        for (Direction direction : Direction.values()) {
            switch (direction) {
                case NORTH:
                case SOUTH:
                case WEST:
                case EAST:
                    //if the move results in the unit being on a valid tile, add the move to the list
                    if (isEmpty(unit.getX() + direction.xComponent(), unit.getY() + direction.yComponent())) {
                        unitActions.add(Action.createPrimitiveMove(unit.getUnitId(), direction));
                    }
                    break;
                default:
                    break;
            }
        }
        List<Unit> tempList = new ArrayList<Unit>(); //list of units to iterate for

        if (playerTurn) //if the agent we're considering is friendly, we only care about which archers he can attack and vice versa
            tempList = badGuys;
        else tempList = goodGuys;

        for (Unit u : tempList) {
            //only add an attack action if the unit is in range to attack
            if (attackDistance(unit, u) <= unit.getRange()) {
                unitActions.add(Action.createPrimitiveAttack(unit.getUnitId(), u.getUnitId()));
            }
        }

        return unitActions;
    }

    /*
     * @param x and y coordinates of a position on the board
     * @return true if space is empty, false otherwise.
     */
    public boolean isEmpty(int x, int y) {
        //invalid state if outside bounds of map, or an agent is atop it
        if (x > this.xExtent || x < 0 || y > this.yExtent || y < 0) {
            return false;
        }
        for (Unit u : goodGuys) {
            if (x == u.getX() && y == u.getY()) {
                return false;
            }
        }
        for (Unit u : badGuys) {
            if (x == u.getX() && y == u.getY()) {
                return false;
            }
        }


        //make sure space not occupied by resource
        for (ResourceView resource : resources) {
            if (resource.getXPosition() == x && resource.getYPosition() == y) {
                return false;
            }
        }

        return true;
    }

    /*
     * Given the id of a unit, returns the corresponding Unit. Null if Unit is not found
     */
    public Unit getUnitWithId(int id) {
        for (Unit unit : goodGuys) {
            if (unit.getUnitId() == id) {
                return unit;
            }
        }
        for (Unit unit : badGuys) {
            if (unit.getUnitId() == id) {
                return unit;
            }
        }
        return null;
    }

    /**
     * You will implement this function.
     * <p>
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     * <p>
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     * <p>
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */

    /*
     * This utility is calculated based on stuff like the health of units,
     * how far apart the units are (footmen naturally want to be closer), how
     * many of each unit is left, and whether or not units can attack.
     */
    public double getUtility() {

        if (this.utilityCalculated) {
            return this.utility;
        }


        double damage = getDamageToEnemyUtility();
        double goodGuys = getHasGoodAgentsUtility();
        double badGuys = getHasBadAgentsUtility();
        double health = getHealthUtility();
        double location = getLocationUtility();
        double attack = getCanAttackUtility();

        this.utilityCalculated = true;
        this.utility += damage + goodGuys + badGuys + health + location + attack;
        return this.utility;

    }

    /*
     * Given an action, this method applies it to this GameState.
     */
    public void takeAction(Action action) {
        if (action.getType() == ActionType.PRIMITIVEMOVE) {
            DirectedAction dAct = (DirectedAction) action;
            Unit u = this.getUnitWithId(dAct.getUnitId());
            u.setX(u.getX() + dAct.getDirection().xComponent());
            u.setY(u.getY() + dAct.getDirection().yComponent());
        } else //otherwise it must be an attack
        {
            TargetedAction tAct = (TargetedAction) action;
            Unit attacker = this.getUnitWithId(tAct.getUnitId());
            Unit attacked = this.getUnitWithId(tAct.getTargetId());
            attacked.dealDamage(attacker.getDamage());
        }

    }

    /*
     * Given a list of maps from unitid to action, return
     * the possible resulting game states from all combinations of these
     * actions.
     */
    public List<GameStateChild> getAllStates(List<Map<Integer, Action>> mapList) {
        List<GameStateChild> children = new ArrayList<GameStateChild>();
        for (Map<Integer, Action> map : mapList) {
            GameState child = new GameState(this);
            Collection<Action> values = map.values();
            for (Action action : values) {
                child.takeAction(action);
            }
            children.add(new GameStateChild(map, child));
        }
        return children;
    }

    /*
     * Takes a list of lists of actions for each unit, and returns a list of maps
     * from a unit's id to the action.
     */
    public List<Map<Integer, Action>> mapActions(List<List<Action>> actions) {
        //if there are no actions
        if (actions.size() == 0) {
            //return an empty map
            return new ArrayList<Map<Integer, Action>>();
        }
        List<Map<Integer, Action>> actMap = new ArrayList<Map<Integer, Action>>();
        if (actions.size() == 1) //only one unit
        {
            List<Action> list1 = actions.get(0);
            for (Action action : list1) {
                Map<Integer, Action> tempMap;
                tempMap = new HashMap<Integer, Action>();
                int unitId = action.getUnitId();
                tempMap.put(unitId, action);
                actMap.add(tempMap);
            }
        } else //there's two
        {
            List<Action> list1 = actions.get(0);
            List<Action> list2 = actions.get(1);
            for (Action action1 : list1) {
                for (Action action2 : list2) {
                    Map<Integer, Action> tempMap = new HashMap<Integer, Action>();
                    //agent 1's action
                    tempMap.put(action1.getUnitId(), action1);
                    //agent 2's action
                    tempMap.put(action2.getUnitId(), action2);
                    actMap.add(tempMap);
                }
            }
        }
        return actMap;
    }

    //Given a unit, returns a list of units it can attack
    public List<Unit> attackableUnits(Unit unit) {
        List<Unit> temp = new ArrayList<Unit>();
        if (unit.isFriendly()) temp = badGuys;
        else temp = goodGuys;
        List<Unit> attackable = new ArrayList<Unit>();
        for (Unit u : temp) {
            if (!canAttack(unit, u)) {
                attackable.add(u);
            }
        }
        return attackable;
    }

    /*
     * Determines whether or not agent 1 can attack agent 2
     * @params footman in consideration, archer in consideration
     * @return if the archer can be attacked
     */
    public boolean canAttack(Unit agent1, Unit agent2) {
        if (attackDistance(agent1, agent2) <= agent1.getRange() && agent2.isAlive() && agent1.getPlayerId() != agent2.getPlayerId())
            return true;
        return false;
    }

    /*
     * Given 2 units, returns the distance that must be exceeded by a unit's range in order to successfully attack
     */
    public double attackDistance(Unit u1, Unit u2) {
        int deltaX = Math.abs(u1.getX() - u2.getX());
        int deltaY = Math.abs(u1.getY() - u2.getY());
        double hyp = Math.hypot(deltaX, deltaY);
        return Math.floor(hyp);
    }

    //returns true if all footmen (player = 0) or archers (player = 1) are dead, false otherwise
    public boolean unitsAllDead(int player) {
        if (numAlive(player) == 0) {
            return true;
        }
        return false;

    }

    //given whose turn it is, returns the number of that
    //player's units who can attack
    public int numberOfUnitsThatCanAttack(boolean playerTurn) {
        if (playerTurn) {
            int i = 0;
            for (Unit u : goodGuys) {
                if (attackableUnits(u).size() > 0) {
                    i++;
                }
            }
            return i;
        } else {
            int i = 0;
            for (Unit u : badGuys) {
                if (attackableUnits(u).size() > 0) {
                    i++;
                }
            }
            return i;
        }
    }

    //given the id of a player, return the number of that player's
    //units that are alive
    public int numAlive(int playernum) {
        if (playernum == 0) {
            int i = 0;
            for (Unit u : goodGuys) {
                if (u.isAlive()) {
                    i++;
                }
            }
            return i;
        } else {
            int i = 0;
            for (Unit u : badGuys) {
                if (u.isAlive()) {
                    i++;
                }
            }
            return i;
        }
    }

    public double distance(Unit agent1, Unit agent2) {
        int arg1 = Math.abs(agent1.getX() - agent2.getX());
        int arg2 = Math.abs(agent1.getY() - agent2.getY());
        return arg1 + arg2 - 1;
    }

    private double getHasGoodAgentsUtility() {
        return goodGuys.isEmpty() ? MIN_UTILITY : goodGuys.size();
    }

    private double getHasBadAgentsUtility() {
        return badGuys.isEmpty() ? MAX_UTILITY : badGuys.size();
    }

    private double getHealthUtility() {
        double utility = 0.0;
        for (Unit agent : goodGuys) {
            utility += agent.getCurrentHp() / agent.getMaxHp();
        }
        return utility;
    }

    private double getDamageToEnemyUtility() {
        double utility = 0.0;
        for (Unit unit : badGuys) {
            int maxHp = unit.getMaxHp();
            int currentHp = unit.getCurrentHp();
            utility += maxHp - currentHp;
        }
        return utility;
    }

    private double getCanAttackUtility() {
        double utility = 0.0;
        for (Unit agent : goodGuys) {
            utility += findAttackableAgents(agent).size();
        }
        return utility;
    }

    private double getLocationUtility() {
        return distanceFromEnemy() * -1;
    }

    private double distanceFromEnemy() {
        double utility = 0.0;
        for (Unit goodAgent : goodGuys) {
            double value = Double.POSITIVE_INFINITY;
            for (Unit badAgent : badGuys) {
                value = Math.min(value, distance(goodAgent, badAgent));
            }
            if (Double.POSITIVE_INFINITY != value) {
                utility += value;
            }
        }
        return utility;
    }

    private List<Integer> findAttackableAgents(Unit agent) {
        List<Integer> attackable = new ArrayList<Integer>();
        getAllAgents().forEach(otherAgent -> {
            if ((otherAgent.isFriendly() != agent.isFriendly()) && attackDistance(agent, otherAgent) <= agent.getRange() && otherAgent.getUnitId() != agent.getUnitId()) {
                attackable.add(otherAgent.getUnitId());
            }
        });
        return attackable;
    }

    public Collection<Unit> getAllAgents() {
        List<Unit> temp = new ArrayList<Unit>();
        temp.addAll(goodGuys);
        temp.addAll(badGuys);
        return temp;
    }

    /*
     * Class representing an agent on the field, either a footman or an archer.
     */
    public class Unit {
        private final int playerId;
        private final int unitId;
        private final int maxHp;
        private final int damage;
        private final int range;
        private int xPos;
        private int yPos;
        private int currentHp;

        public Unit(int playerId, int agentId, int xPos, int yPos, int currentHp, int maxHp, int damage, int range) {
            this.playerId = playerId;
            this.unitId = agentId;
            this.xPos = xPos;
            this.yPos = yPos;
            this.currentHp = currentHp;
            this.maxHp = maxHp;
            this.damage = damage;
            this.range = range;
        }

        public boolean isFriendly() {
            return this.playerId == 0;
        }

        public void dealDamage(int amount) {
            this.currentHp -= amount;
        }

        public int getUnitId() {
            return this.unitId;
        }

        public int getPlayerId() {
            return this.playerId;
        }

        public int getX() {
            return this.xPos;
        }

        public void setX(int xPos) {
            this.xPos = xPos;
        }

        public int getY() {
            return this.yPos;
        }

        public void setY(int yPos) {
            this.yPos = yPos;
        }

        public int getCurrentHp() {
            return this.currentHp;
        }

        public int getMaxHp() {
            return this.maxHp;
        }

        public int getDamage() {
            return this.damage;
        }

        public int getRange() {
            return this.range;
        }

        public boolean isAlive() {
            return this.currentHp > 0;
        }

        public boolean canUnitAttack() {
            if (isFriendly()) {
                for (Unit u : badGuys) {
                    if (canAttack(this, u)) {
                        return true;
                    }
                }
                return false;
            } else {
                for (Unit u : goodGuys) {
                    if (canAttack(this, u)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }


}
