package Synapse.MonteCarlo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by cmk on 2016-03-13.
 */
public class MCNode <Rules extends MCState> {

    private static Random r = new Random(LocalTime.now().hashCode());
    private static double epsilon = 1e-6;

    protected MCMove move = MCMove.NO_MOVE;
    public MCNode parent = null;
    protected List<MCNode> children;
    protected List<MCMove> moves; // rules
    protected double nVisits = 0, totValue = 0;

    public int playerId;

    public static Random getRandom() {return r;}

    public MCNode(Rules rule) {
        this.moves = rule.getMoves();
        this.playerId = rule.getPlayerId();
        children = new ArrayList<>();
    }

    private MCNode(Rules rule, MCMove move, MCNode<Rules> node) {
        this.moves = rule.getMoves();
        this.playerId = rule.getPlayerId();
        children = new ArrayList<>();
        this.move = move;
        parent = node;
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

    public MCNode selectChild() {
        MCNode selected = null;
        double bestValue = Double.MIN_VALUE;

        for (MCNode c : children) {
            double uctValue = c.totValue / (c.nVisits + epsilon) +
                    Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                    r.nextDouble() * epsilon; // epsilon is tie breaker

            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }

        return selected;
    }

    public MCNode addChild(MCMove move, Rules rule) {
        MCNode node = new MCNode(rule, move, this);
        children.add(node);
        this.moves.remove(move);
        return node;
    }

    public void updateStats(double value) {
        nVisits++;
        totValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.size();
    }

    public List<MCNode> getChildren() {
        return children;
    }

    public MCMove getMove() {
        return move;
    }

    public double getVisits() {
        return nVisits;
    }

    public double getTots() {
        return totValue;
    }

    public List<MCMove> getMoves() { // TODO : remove
        return moves;
    }
}
