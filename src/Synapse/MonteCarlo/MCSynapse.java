package Synapse.MonteCarlo;

import Synapse.Synapse;

import java.util.*;

/**
 * Created by cmk on 2016-03-19.
 */
public class MCSynapse <Policy extends MCState> implements Synapse<Policy, MCMove> {

    private MCNode<Policy> root;
    private Map<MCMove, Double> wins = new HashMap<>();
    private Map<MCMove, Double> visits = new HashMap<>();
    public int simLimit = 1000; // pretty damn big

    public MCSynapse(Policy rule) {
        root = new MCNode(rule);
        for (MCMove move : rule.getMoves()) {
            wins.put(move, 0.0);
            visits.put(move, 0.0);
        }
    }

    @Override
    public void train(Policy currentState, MCMove o) {
        Policy stateCpy = (Policy) currentState.copy();
        MCNode node = root;
        MCMove move = node.getUntriedMoves();

        // selection
        while (null == move && 0 != node.arity()) {
            node = node.selectChild();
            stateCpy.doMove(node.getMove());
            move = node.getUntriedMoves();
        }
        // expansion
        if (null != move) {
            stateCpy.doMove(move);
            node = node.addChild(move, stateCpy);
        }

        // simulation -- TODO: improve
        for (int i = 0; i < simLimit && stateCpy.hasMoves(); i++) {
            stateCpy.doRandomMoves(root.getRandom());
        }

        double latestResult = 0;
        MCMove latestMove = null;
        // back-propagation
        while (node != root) {
            latestResult = stateCpy.getResult(node.playerId);
            latestMove = node.getMove();
            node.updateStats(latestResult);
            node = node.parent;
        }
        if (null != latestMove) {
            wins.put(latestMove, wins.get(latestMove) + latestResult);
            visits.put(latestMove, visits.get(latestMove) + 1);
        }
    }

    @Override
    public MCMove output(Policy currentState) {
        double bestScore = Double.MIN_VALUE;
        MCMove bestMove = MCMove.NO_MOVE;
        Set<MCMove> moveSets = wins.keySet();
        for (MCMove move : moveSets) {
            double expectedSuccessRate = wins.get(move)/visits.get(move);
            if (expectedSuccessRate > bestScore) {
                bestMove = move;
                bestScore = expectedSuccessRate;
            }
        }
        return bestMove;
    }

    public void joinWeight(MCSynapse s) {
        wins.putAll(s.wins);
        visits.putAll(s.visits);
    }

    public void moveDown(MCMove moveTaken, Policy rule) {
        MCNode foundRoot = null;
        // TODO 2: optimize MCNode by mapping move to children nodes
        for (MCNode child : root.getChildren()) {
            if (moveTaken.equals(child.getMove())) {
                foundRoot = child;
            }
        }

        root = foundRoot; // TODO: sometimes size of root.children and root.move are both 0 RESOLVE (solution 1: take policy moveset)
        root.parent = null; // garabage collect parent and sibling branches?

        // refresh wins and visits
        wins.clear();
        visits.clear();
        for (MCMove move : rule.getMoves()) {
            wins.put(move, 0.0);
            visits.put(move, 0.0);
        }
        for (MCNode child : root.getChildren()) {
            MCMove move = child.getMove();
            double win = child.getTots();
            double visit = child.getVisits();
            if (wins.containsKey(move)) {
                wins.put(move, wins.get(move) + win);
                visits.put(move, visits.get(move) + visit);
            } else {
                wins.put(move, win);
                visits.put(move, visit);
            }
        }
    }
}
