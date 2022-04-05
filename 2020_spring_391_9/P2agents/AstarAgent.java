package edu.cwru.sepia.agent;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class AstarAgent extends Agent {

    Stack<MapLocation> path = new Stack<MapLocation>();
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;
    //The starting position of our peasant
    MapLocation initialLoc;
    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs

    public AstarAgent(int playernum) {
        super(playernum);

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);

        if (unitIDs.size() == 0) {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);

        // double check that this is a footman
        if (!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman")) {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
        int enemyPlayerNum = -1;
        for (Integer playerNum : playerNums) {
            if (playerNum != playernum) {
                enemyPlayerNum = playerNum;
                break;
            }
        }

        if (enemyPlayerNum == -1) {
            System.err.println("Failed to get enemy playernumber");
            return null;
        }

        // find the townhall ID
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if (enemyUnitIDs.size() == 0) {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for (Integer unitID : enemyUnitIDs) {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if (unitType.equals("townhall")) {
                townhallID = unitID;
            } else if (unitType.equals("footman")) {
                enemyFootmanID = unitID;
            } else {
                System.err.println("Unknown unit type");
            }
        }

        if (townhallID == -1) {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        long startTime = System.nanoTime();
        long planTime = 0;

        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if (shouldReplanPath(newstate, statehistory, path)) {
            System.out.println("Replanning");
            long planStartTime = System.nanoTime();
            path = findPath(newstate);
            planTime = System.nanoTime() - planStartTime;
            totalPlanTime += planTime;
        }

        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if (!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

            // stat moving to the next step in the path
            nextLoc = path.pop();

            System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if (nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y)) {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if (townhallUnit == null) {
                terminalStep(newstate, statehistory);
                return actions;
            }

            if (Math.abs(footmanX - townhallUnit.getXPosition()) > 1 || Math.abs(footmanY - townhallUnit.getYPosition()) > 1) {
                System.err.println("Invalid plan. Cannot attack townhall");
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            } else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime / 1e9);
        System.out.println("Total execution time: " + totalExecutionTime / 1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime) / 1e9);
    }

    public void savePlayerData(OutputStream os) {

    }

    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this method.
     * <p>
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     * <p>
     * You can check the position of the enemy footman with the following code:
     * state.getUnit(enemyFootmanID).getXPosition() or .getYPosition().
     * <p>
     * There are more examples of getting the positions of objects in SEPIA in the findPath method.
     *
     * @param state
     * @param history
     * @param currentPath
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath) {
        int xpos = state.getUnit(enemyFootmanID).getXPosition();
        int ypos = state.getUnit(enemyFootmanID).getYPosition();

        int xpos2 = state.getUnit(footmanID).getXPosition();
        int ypos2 = state.getUnit(footmanID).getYPosition();

        Iterator<MapLocation> itr = currentPath.iterator();

        while (itr.hasNext()) {
            MapLocation temp = itr.next();
            if (temp.x == xpos && temp.y == ypos) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state) {
        //clear current path
        this.path = new Stack<MapLocation>();
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

        MapLocation footmanLoc = null;
        if (enemyFootmanID != -1) {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for (Integer resourceID : resourceIDs) {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);
            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }

    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     * <p>
     * Therefore your you need to find some possible adjacent steps which are in range
     * and are not trees or the enemy footman.
     * Hint: Set<MapLocation> resourceLocations contains the locations of trees
     * <p>
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     * <p>
     * As an example consider the following simple map
     * <p>
     * F - - - -
     * x x x - x
     * H - - - -
     * <p>
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     * <p>
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     * <p>
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     * <p>
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     * <p>
     * The path would be
     * <p>
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     * <p>
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start             Starting position of the footman
     * @param goal              MapLocation of the townhall
     * @param xExtent           Width of the map
     * @param yExtent           Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations) {
        if (enemyFootmanLoc.x == 9 && enemyFootmanLoc.y == 12) {
            System.out.println("Here");
        }
        //create open and closed lists, and begin by adding start location to open list
        ArrayList<MapLocation> closedList = new ArrayList<MapLocation>();
        ArrayList<MapLocation> openList = new ArrayList<MapLocation>();

        openList.add(start);
        initialLoc = start;

        outerloop:
        while (!openList.isEmpty()) {
            //find the lowest cost node in the openlist
            MapLocation current = lowestCost(openList, goal);

            //add all of current's neighbors to the open list
            List<MapLocation> neighbors = getValidNeighbors(current, xExtent, yExtent, enemyFootmanLoc, resourceLocations);
            openList.remove(current);
            innerloop:
            for (MapLocation neighbor : neighbors) {
                neighbor.cameFrom = current;
                //check to see if we have made it to the goal
                if (neighbor.x == goal.x && neighbor.y == goal.y) {
                    this.reconstructPath(current);
                    break outerloop;
                }
                //if it's already in the open list and the cost is lower, ignore it
                for (MapLocation mloc : openList) {
                    if (mloc.x == neighbor.x && mloc.y == neighbor.y) {
                        if (mloc.cost + this.calculateHCost(mloc, goal) < neighbor.cost + this.calculateHCost(neighbor, goal)) {
                            continue innerloop;
                        }
                    }
                }
                //if it's already in the closed list and the cost is lower, ignore it
                for (MapLocation mloc : closedList) {
                    if (mloc.x == neighbor.x && mloc.y == neighbor.y) {
                        if (mloc.cost + this.calculateHCost(mloc, goal) < neighbor.cost + this.calculateHCost(neighbor, goal)) {
                            continue innerloop;
                        }
                    }
                }
                openList.add(neighbor);
            }
            closedList.add(current);
        }
        for (MapLocation mloc : path) {
            System.out.println("(" + mloc.x + ", " + mloc.y + ")");
        }

        return path;
    }

    /**
     * Primitive actions take a direction (e.g. Direction.NORTH, Direction.NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if (xDiff == 1 && yDiff == 1) {
            return Direction.SOUTHEAST;
        } else if (xDiff == 1 && yDiff == 0) {
            return Direction.EAST;
        } else if (xDiff == 1 && yDiff == -1) {
            return Direction.NORTHEAST;
        } else if (xDiff == 0 && yDiff == 1) {
            return Direction.SOUTH;
        } else if (xDiff == 0 && yDiff == -1) {
            return Direction.NORTH;
        } else if (xDiff == -1 && yDiff == 1) {
            return Direction.SOUTHWEST;
        } else if (xDiff == -1 && yDiff == 0) {
            return Direction.WEST;
        } else if (xDiff == -1 && yDiff == -1) {
            return Direction.NORTHWEST;
        }
        return null;
    }

    /*
     * Calculates the cost of traversing to goal using the Chebyshev distance
     */
    private float calculateHCost(MapLocation from, MapLocation to) {
        return Math.max(Math.abs(to.x - from.x), Math.abs(to.y - from.y));
    }

    /*
     * Looks at all tiles adjacent to the given tile, and checks if they are capable of being occupied before adding them
     * to the list.
     */
    private List<MapLocation> getValidNeighbors(MapLocation current, int xExtent, int yExtent, MapLocation footmanPosition, Set<MapLocation> resourcePositions) {
        //Where we're considering
        int xPos = current.x;
        int yPos = current.y;

        //All nodes adjacent to the node we're considering
        ArrayList<MapLocation> tempList = new ArrayList<MapLocation>();
        tempList.add(new MapLocation(xPos - 1, yPos - 1, current, current.cost + 1));
        tempList.add(new MapLocation(xPos, yPos - 1, current, current.cost + 1));
        tempList.add(new MapLocation(xPos + 1, yPos - 1, current, current.cost + 1));
        tempList.add(new MapLocation(xPos - 1, yPos, current, current.cost + 1));
        tempList.add(new MapLocation(xPos + 1, yPos, current, current.cost + 1));
        tempList.add(new MapLocation(xPos - 1, yPos + 1, current, current.cost + 1));
        tempList.add(new MapLocation(xPos, yPos + 1, current, current.cost + 1));
        tempList.add(new MapLocation(xPos + 1, yPos + 1, current, current.cost + 1));

        //prune the list of possible locations to move to
        for (int i = 0; i < tempList.size(); i++) {
            MapLocation mloc = tempList.get(i);
            /*
             * We remove the MapLocation from our list if any of the coordinates exceed the bounds of the map, or the tile lies on
             * top of the footman or any resources
             */
            innerloop:
            for (MapLocation rloc : resourcePositions) {
                boolean invalid;
                //We need to check whether or not an enemy footman hat been spawned, to determine whether or not to look out for it
                if (footmanPosition == null) {
                    invalid = (mloc.x < 0 || mloc.y < 0 || mloc.x >= xExtent || mloc.y >= yExtent || (rloc.x == mloc.x && rloc.y == mloc.y));
                } else {
                    invalid = (mloc.x < 0 || mloc.y < 0 || mloc.x >= xExtent || mloc.y >= yExtent || (mloc.x == footmanPosition.x && mloc.y == footmanPosition.y) || (rloc.x == mloc.x && rloc.y == mloc.y));
                }

                if (invalid) {
                    //record that the MapLocation at this index is invalid
                    mloc.isInvalid();
                    break innerloop;
                }
            }
        }
        //only contains valid nodes from tempList
        ArrayList<MapLocation> finalList = new ArrayList<MapLocation>();
        for (MapLocation mloc : tempList) {
            if (!mloc.toBeRemoved) {
                finalList.add(mloc);
            }
        }
        return finalList;

    }

    /*
     * Returns the MapLocation with the lowest Chebyshev distance and the cost to the map location
     */
    private MapLocation lowestCost(ArrayList<MapLocation> openList, MapLocation goal) {
        if (openList.isEmpty()) return null;
        MapLocation minCostLocation = openList.get(0);
        float minCost = minCostLocation.cost + calculateHCost(minCostLocation, goal);
        for (MapLocation mloc : openList) {
            float temp = mloc.cost + calculateHCost(mloc, goal);
            if (temp <= minCost) {
                minCostLocation = mloc;
                minCost = temp;
            }
        }
        return minCostLocation;
    }

    /*
     * Once we have found a MapLocation that is adjacent to the goal, we must reconstruct the path
     * that got us there.
     */
    private void reconstructPath(MapLocation adjToGoal) {
        MapLocation tempLocation = adjToGoal;
        System.out.println(path);
        while (true) {
            //a location with no parent should be where we started
            if (tempLocation.cameFrom == null) {
                //if it isn't where we started, then something went wrong
                if (tempLocation.x != initialLoc.x || tempLocation.y != initialLoc.y) {
                    System.out.println("Path reconstruction hasn't returned to the initial state");
                    System.exit(1);
                } else return;
            }
            path.push(tempLocation);
            tempLocation = tempLocation.cameFrom;
        }
    }

    private boolean areAdjacent(int x1, int y1, int x2, int y2) {
        if (Math.abs(x2 - x1) <= 1 && Math.abs(y2 - y1) <= 1) {
            return true;
        } else return false;
    }

    class MapLocation {
        public int x, y;
        public float cost;
        public MapLocation cameFrom;
        //variables that becomes true if this node is invalid
        public boolean toBeRemoved = false;

        public MapLocation(int x, int y, MapLocation cameFrom, float cost) {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.cameFrom = cameFrom;
        }

        public void isInvalid() {
            this.toBeRemoved = true;
        }
    }

}