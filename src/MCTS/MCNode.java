package MCTS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by cmk on 2016-03-13.
 */
public class MCNode <Rules extends MCState> {

    private static Random r = new Random(LocalTime.now().hashCode());
    private static double epsilon = 1e-6;

    protected MCMove move = MCMove.NO_MOVE;
    protected MCNode parent = null;
    protected List<MCNode> children;
    protected List<MCMove> moves; // rules
    protected double nVisits = 0, totValue = 0;

    public static Random getRandom() {return r;}

    public MCNode(Rules rule) {
        moves = rule.getMoves();
        children = new ArrayList<>();
    }

    public MCNode(Rules rule, MCMove move, MCNode<Rules> node) {
        moves = rule.getMoves();
        children = new ArrayList<>();
        this.move = move;
        parent = node;
    }

    public MCMove getUntriedMoves() {
        return 0 == moves.size() ? null : moves.get(r.nextInt(moves.size()-1));
    }

    public MCNode getBestChild() {
        return Collections.max(children, (MCNode n1, MCNode n2)->{
            int compValue = 0;
            if (n1.nVisits < n2.nVisits) {
                compValue = -1;
            } else if (n1.nVisits > n2.nVisits) {
                compValue = -1;
            }
            return compValue;
        });
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

    public List<MCNode> getChildren() {
        return children;
    }

    public void output(Predicate<MCNode<Rules>> out) {
        for (MCNode<Rules> child : children) {
            out.test(child);
        }
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

    public MCNode getParent() {
        return parent;
    }
}
