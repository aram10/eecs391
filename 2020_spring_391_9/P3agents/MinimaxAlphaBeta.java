package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MinimaxAlphaBeta extends Agent {

    /*
     * Comparator for orderChildrenWithHeuristic()
     */
    private static final Comparator<GameStateChild> comp = (obj1, obj2) -> {
        if (obj2.state.getUtility() < obj1.state.getUtility()) {
            return -1;
        } else if (obj2.state.getUtility() > obj1.state.getUtility()) {
            return 1;
        } else return 0;
    };
    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args) {
        super(playernum);

        if (args.length < 1) {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate), numPlys, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     * <p>
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     * <p>
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node   The action and state to search from
     * @param depth  The remaining number of plys under this node
     * @param alpha  The current best value for the maximizing node from this node to the root
     * @param beta   The current best value for the minimizing node from this node to the root
     * @param player 0 if maximizer is playing, 1 if minimizer is playing
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta) {
        double bestVal = maxValue(node, depth, alpha, beta);
        GameStateChild bestChild = null;
        bestChild = findChildWithValue(node, bestVal);
        return bestChild;
    }

    /*
     * This method is initially called by alphaBetaSearch(), to get around the fact that
     * a GameStateChild must be returned. This method returns the best value for the given
     * node, and then we retrieve the child with this cost.
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @param player 0 if maximizer is playing, 1 if minimizer is playing
     * @return best value
     */


    private double maxValue(GameStateChild node, int depth, double alpha, double beta) {
        if (cutOffTest(node, depth)) {
            return node.state.getUtility();
        }
        double bestVal = Double.NEGATIVE_INFINITY;
        for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren())) {
            bestVal = Math.max(bestVal, minValue(child, depth - 1, alpha, beta));
            if (bestVal >= beta) {
                return bestVal;
            }
            alpha = Math.max(alpha, bestVal);
        }
        return bestVal;
    }

    private double minValue(GameStateChild node, int depth, double alpha, double beta) {
        if (cutOffTest(node, depth)) {
            return node.state.getUtility();
        }
        double bestVal = Double.POSITIVE_INFINITY;
        for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren())) {
            bestVal = Math.min(bestVal, maxValue(child, depth - 1, alpha, beta));
            if (bestVal <= alpha) {
                return bestVal;
            }
            beta = Math.min(beta, bestVal);
        }
        return bestVal;
    }

    /**
     * You will implement this.
     * <p>
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     * <p>
     * Use this function inside of your alphaBetaSearch method.
     * <p>
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    /*
     * The heuristic that is used to determine expansion order is whether or not one of both of
     * the footmen can attack. If they are able to attack, they should invariably do so, as killing archers
     * is the goal of the game and any other action in such close proximity to the archer will be perilous.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children) {
        List<GameStateChild> sortedList = new ArrayList<GameStateChild>();
        //if neither unit can attack, it is put here for low-priority sorting
        List<GameStateChild> leftover = new ArrayList<GameStateChild>();
        for (GameStateChild child : children) {
            if (child.state.numberOfUnitsThatCanAttack(child.state.playerTurn) == 2) {
                sortedList.add(0, child);
                continue;
            }
            //states in which only one of the units can attack
            if (child.state.numberOfUnitsThatCanAttack(child.state.playerTurn) == 1) {
                sortedList.add(child);
                continue;
            }
            leftover.add(child);
        }
        leftover.sort(comp);
        sortedList.addAll(children);
        sortedList.sort(comp);
        return sortedList;
    }

    /*
     * Once alphaBetaSearch() has determined the best value at this state, this method
     * looks through the children and returns the node with that value.
     * @param node The action and state to search from
     * @param value of best child
     * @return child with that value
     */
    public GameStateChild findChildWithValue(GameStateChild node, double value) {
        List<GameStateChild> children = node.state.getChildren();
        for (GameStateChild child : children) {
            if (child.state.getUtility() == value) {
                return child;
            }
        }
        /*
         * if we get here because we haven't found the corresponding child, then we've clearly screwed
         * the pooch somewhere along the line
         */
        return children.get(0);
    }

    private boolean cutOffTest(GameStateChild node, int depth) {
        return depth == 0;
    }

}
