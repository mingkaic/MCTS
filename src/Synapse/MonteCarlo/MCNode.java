package Synapse.MonteCarlo;

import java.util.*;

/**
 * Created by cmk on 2016-03-13.
 */
public class MCNode <Rules extends MCState> {

    private static Random r = new Random();
    private static double epsilon = 1e-6;

    public MCNode parent = null;
    protected Map<MCMove, MCNode> children = new HashMap<>();
    protected ArrayList<MCMove> moves; // rules
    protected double nVisits = 0, totValue = 0;

    public int playerId;

    public static Random getRandom() {return r;}

    public MCNode(Rules rule) {
        moves = (ArrayList<MCMove>) rule.getMoves().clone();
        playerId = rule.getPlayerId();
    }

    private MCNode(Rules rule, MCNode<Rules> node) {
        this(rule);
        parent = node;
    }

    public boolean hasUntriedMoves() {
        return false == moves.isEmpty();
    }

    public MCMove getUntriedMoves() {
        MCMove m;
        if (0 == moves.size()) {
            m = null;
        } else {
            int uniform = r.nextInt(moves.size());
            m = moves.get(uniform);
        }
        return m;
    }

    public MCMove selectMove() {
        Set<MCMove> keys = children.keySet();
        double bestValue = Double.MIN_VALUE;
        MCMove bestMove = null;

        for (MCMove move : keys) {
            MCNode c = children.get(move);
            double uctValue = c.totValue / (c.nVisits + epsilon) +
                    Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                    r.nextDouble() * epsilon; // epsilon is tie breaker

            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestMove = move;
            }
        }

        return bestMove;
    }

    static MCMove bestMove(Map<MCMove, MCNode> children) {
        Set<MCMove> keys = children.keySet();
        MCMove best = null;
        double bestScore = -1;

        for (MCMove move : keys) {
            MCNode child = children.get(move);
            double expectedSuccessRate = child.totValue/child.nVisits;
            if (expectedSuccessRate > bestScore) {
                best = move;
                bestScore = expectedSuccessRate;
            }
        }
        return best;
    }

    public MCMove bestMove() {
        return bestMove(children);
    }

    public MCNode addChild(MCMove move, Rules rule) {
        MCNode node = new MCNode(rule, this);
        children.put(move, node);
        moves.remove(move);
        return node;
    }

    public void updateStats(double value) {
        nVisits++;
        totValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.size();
    }

    public MCNode getChild(MCMove move) {
        if (children.containsKey(move)) {
            return children.get(move);
        } else {
            return null;
        }
    }
}
