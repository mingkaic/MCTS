package MCTS;

import java.util.*;

/**
 * Created by cmk on 2016-03-14.
 */
public class MCTree <Rules extends MCState> {

    private MCNode<Rules> root;
    private int playerId;

    private MCTree(Rules state, int maxIt) {
        playerId = state.getPlayerId();
        root = new MCNode(state);

        for (int i = 1; i <= maxIt || maxIt < 0; i++) {
            MCNode node = root;

            // select path
            while (null == node.getUntriedMoves() && 0 != node.arity()) {
                node = node.selectChild();
                state.doMove(node.getMove());
            }

            // expand
            MCMove move = node.getUntriedMoves();
            if (null != move) {
                state.doMove(move);
                node = node.addChild(move, state);
            }

            // random :(
            while (state.hasMoves()) {
                state.doRandomMoves(root.getRandom());
            }

            // back-propagate
            while (node != null) {
                node.updateStats(state.getResult(playerId));
                node = node.getParent();
            }
        }
    }

    private long harvest(Map<MCMove, Double> wins, Map<MCMove, Double> visits) {
        long nVisits = 0;
        for (MCNode<Rules> child : root.getChildren()) {
            MCMove move = child.getMove();
            double visit = child.getVisits();
            nVisits += visit;
            if (wins.containsKey(move)) {
                wins.put (move, wins.get(move)+child.getTots());
                visits.put(move, visits.get(move)+visit);
            } else {
                wins.put(move, child.getTots());
                visits.put(move, visit);
            }
        }
        return nVisits;
    }

    public static MCMove computeMove(MCState rootState, int maxIt, int nTrees) {
        List<MCMove> moves = rootState.getMoves();

        if (moves.size() == 1) {
            return moves.get(0);
        }

        List<MCNode> nodeFarm = new ArrayList<>();

        long totalVisits = 0;
        Map<MCMove, Double> wins = new HashMap<>();
        Map<MCMove, Double> visits = new HashMap<>();

        for (int i = 0; i < nTrees; i++) {
            MCTree t = new MCTree(rootState, maxIt);
            totalVisits += t.harvest(wins, visits);
        }

        double bestScore = -1;
        MCMove bestMove = MCMove.NO_MOVE;
        Set<MCMove> visitSet = visits.keySet();
        for (MCMove move : visitSet) {
            double expectedSuccessRate = (wins.get(move) + 1) / (visits.get(move) + 2);
            if (expectedSuccessRate > bestScore) {
                bestMove = move;
                bestScore = expectedSuccessRate;
            }
        }
        return bestMove;
    }
}
