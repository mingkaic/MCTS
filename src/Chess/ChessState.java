package Chess;


import Synapse.MonteCarlo.MCMove;
import Synapse.MonteCarlo.MCState;
import com.mgs.chess.core.*;
import com.mgs.chess.core.movement.Movement;

import java.util.*;

public class ChessState implements MCState {
    private ChessAnaliserImpl analiser = new ChessAnaliserImpl(new ChessReaderImpl());
    private ChessBoard board;
    private HashMap<PieceOnLocation, Movement> prevMove = new HashMap<>();
    private int player = 1;
    private MCMove lastMove = null;

    public ChessState() {
        board = new ChessBoard();
    }

    public ChessState(ChessState source) {
        for (PieceOnLocation p : source.prevMove.keySet()) {
            Movement oldMove = source.prevMove.get(p);
            PieceOnLocation duplicateKey = new PieceOnLocation(p.getPiece(), p.getLocation());
            Movement duplicateValue = null;
            if (null != oldMove) {
                duplicateValue = new Movement(oldMove.getMovingPiece(), oldMove.getFrom(), oldMove.getTo());
            }
            prevMove.put(duplicateKey, duplicateValue);
        }
        board = source.board;
        player = source.player;
    }

    public void setPiece(Piece p, Location l) {
        board = board.addPiece(p, l);
        prevMove.put(new PieceOnLocation(p, l), null); // make piece unique id
    }

    @Override
    public void doMove(MCMove move) {
        if (move instanceof ChessMove) {
            ChessMove cm = (ChessMove) move;
            prevMove.put(new PieceOnLocation(cm.getMovingPiece(), cm.getFrom()), cm);
            board = board.performMovement(cm);

            if (cm.getMovingPiece().getType() == PieceType.PAWN) {
                Location arrival = cm.getTo();
                if (arrival.getCoordinateY() == 1 || arrival.getCoordinateY() == 8) {
                    // promotion
                    Color me = cm.getMovingPiece().getColor();
                    board = board.addPiece(Piece.with(PieceType.QUEEN, me), cm.getTo());
                }
            }

            lastMove = move;
            player = 3-player;
        }
    }

    @Override
    public void doRandomMoves(Random r) {
        List<MCMove> moveset = getMoves();
        doMove(moveset.get(r.nextInt(moveset.size())));
    }

    @Override
    public boolean hasMoves() {
        return false == getMoves().isEmpty();
    }

    @Override
    public ArrayList<MCMove> getMoves() {
        ArrayList<MCMove> moveset = new ArrayList<>();

        if (false == analiser.isCheckMate(board)) {
            Color side = player == 1 ? Color.WHITE : Color.BLACK;
            List<PieceOnLocation> myPieces = board.getPieces(side);
            for (PieceOnLocation p : myPieces) {
                List<Location> nextPos = analiser.findReachableLocations(p, board, prevMove.get(p));
                for (Location l : nextPos) {
                    moveset.add(new ChessMove(p, l));
                }
            }
        }

        return moveset;
    }

    @Override
    public double getResult(int playerId) {
        if (analiser.isCheckMate(board) && player == playerId) {
            return 1;
        }
        Color side = player == 1 ? Color.WHITE : Color.BLACK;
        List<PieceOnLocation> myPieces = board.getPieces(side);
        double score = 0;
        double totalScore = 39;
        for (PieceOnLocation p : myPieces) {
            switch(p.getType()) {
                case PAWN:
                    score++;
                    break;
                case ROOK:
                    score+=5;
                    break;
                case KNIGHT:
                case BISHOP:
                    score+=3;
                    break;
                case QUEEN:
                    score+=9;
                    break;
            }
        }
        return score/totalScore;
    }

    public MCMove getLastMove() {
        return lastMove;
    }

    @Override
    public MCState copy() {
        return new ChessState(this);
    }

    @Override
    public int getPlayerId() {
        return player;
    }
}
