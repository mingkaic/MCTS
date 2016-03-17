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
        root = new MCNode(state.getMoves());

        for (int i = 0; i < maxIt || maxIt < 0; i++) { // 1 it = 1 game
            Rules stateCpy = (Rules) state.copy();
            MCNode node = root;

            // selection
            while (null == node.getUntriedMoves() && 0 != node.arity()) {
                node = node.selectChild();
                stateCpy.doMove(node.getMove());
            }

            // expansion
            MCMove move = node.getUntriedMoves();
            if (null != move) {
                stateCpy.doMove(move);
                node = node.addChild(move, stateCpy.getMoves());
            }

            // simulation -- TODO: improve
            while (stateCpy.hasMoves()) {
                stateCpy.doRandomMoves(root.getRandom());
            }

            // back-propagation
            while (node != null) {
                node.updateStats(stateCpy.getResult(playerId));
                node = node.getParent();
            }
        }
    }

    private void harvest(Map<MCMove, Double> winrate) {
        List<MCNode> children = root.getChildren();
        for (MCNode<Rules> child : children) {
            MCMove move = child.getMove();
            double win = child.getTots();
            double visit = child.getVisits();
            if (winrate.containsKey(move)) {
                winrate.put(move, winrate.get(move) + win/visit);
            } else {
                winrate.put(move, win/visit);
            }
        }
    }

    public static MCMove computeMove(MCState rootState, int maxIt, int nTrees) {
        List<MCMove> moves = rootState.getMoves();

        if (moves.size() == 1) {
            return moves.get(0);
        }

        Map<MCMove, Double> winrates = new HashMap<>();

        for (int i = 0; i < nTrees; i++) {
            MCTree t = new MCTree(rootState, maxIt);
            t.harvest(winrates);
        }

        double bestScore = Double.MAX_VALUE;
        MCMove bestMove = MCMove.NO_MOVE;
        Set<MCMove> visitSet = winrates.keySet();
        for (MCMove move : visitSet) {
            double expectedSuccessRate = winrates.get(move);
            if (expectedSuccessRate < bestScore) {
                bestMove = move;
                bestScore = expectedSuccessRate;
            }
        }
        return bestMove;
    }
}
