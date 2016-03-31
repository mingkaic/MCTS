package Synapse.MonteCarlo;

import Synapse.Synapse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cmk on 2016-03-19.
 */
public class MCSynapse <Policy extends MCState> implements Synapse<Policy, MCMove> {

    private MCNode<Policy> root;
    public int simLimit = 1000; // pretty damn big

    public static class MCCollection {
        public Map<MCMove, MCNode> collection = new HashMap<>();

        public void collect(MCSynapse syn) {
            ((HashMap) collection).putAll(syn.root.children);
        }
    }

    public MCSynapse(Policy rule) {
        root = new MCNode(rule);
    }

    // evaluate the effectiveness of training regiments
    public double convergence() {
        if (root.hasUntriedMoves()) {
            return 0;
        }
        double averageWR = 0;
        List<Double> WRSet = new ArrayList<>();
        for (MCNode n : root.children.values()) {
            double winrate = n.totValue / n.nVisits;
            averageWR += winrate;
            WRSet.add(winrate);
        }
        int N = root.arity();
        averageWR /= N;
        if (N == 0) {
            return Double.MAX_VALUE;
        }
        final double finalAverageWR = averageWR;
        double variance = WRSet.stream().mapToDouble(d-> {
            double i = d-finalAverageWR;
            return i*i;
        }).sum()/N;
        double stdev = Math.sqrt(variance);
        return stdev;
    }

    public double rootVisits() {
        return root.nVisits;
    }

    @Override
    public void train(Policy currentState, MCMove o) {
        Policy stateCpy = null;
        synchronized (currentState) {
            stateCpy = (Policy) currentState.copy();
        }
        if (null == stateCpy) return;
        MCNode node = root;
        MCMove move = node.getUntriedMoves();

        // selection
        while (null == move && 0 != node.arity()) {
            move = node.selectMove();
            stateCpy.doMove(move);
            node = node.getChild(move);

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
        // back-propagation
        while (node != root) {
            node.updateStats(stateCpy.getResult(node.playerId));
            node = node.parent;
        }
    }

    @Override
    public MCMove output(Policy currentState) {
        return root.bestMove();
    }

    public void moveDown(final MCMove moveTaken, Policy rule) {
        MCNode foundRoot = root.getChild(moveTaken);

        if (null == foundRoot) {
            // unable to recycle
            root = new MCNode<>(rule);
        } else {
            root = foundRoot; // TODO: sometimes size of root.children and root.move are both 0 RESOLVE (solution 1: take policy moveset)
            root.parent = null; // garabage collect parent and sibling branches?
        }
    }
}
