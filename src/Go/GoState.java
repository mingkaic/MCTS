/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Go;

import Synapse.MonteCarlo.MCMove;
import Synapse.MonteCarlo.MCState;

import java.util.*;

/**
 *
 * @author mchen
 */
public class GoState implements MCState {

    private static char[] playerMarker = {'*', 'X', 'O'};
    private static MCMove PASS = new MCMove(-2);
    private piece board[][];

    private int lastHash;
    private Set<Integer> allHash;
    private int depth, playerToMove, nRow, nCol;

    private class piece {
        private final boolean isBlack; // once you go black, you can never go back
        private int liberty = 4;
        private Set<MCMove> positions;
        private Set<MCMove> eyes;

        public piece(final boolean black, MCMove move) {
            isBlack = black;
            positions = new HashSet<>();
            eyes = new HashSet<>();
            if (null != move) {
                positions.add(move);
            }
        }

        public piece(final boolean black, MCMove move, int lib) {
            this(black, move);
            liberty = lib;
        }

        public piece join(piece hood) {
            if (hood != null) {
                positions.addAll(hood.positions);
                eyes.addAll(hood.eyes);
                liberty += hood.liberty;
            }
            return this;
        }

        public piece clone() {
            piece c = new piece(isBlack, null);
            c.positions.addAll(positions);
            c.liberty = liberty;
            return c;
        }
    }

    private void setPiece(MCMove move, boolean black) {
        int row = move.getValue() / nCol;
        int col = move.getValue() % nCol;

        List<piece> hoodz = new ArrayList<>();
        List<Integer> moves = new ArrayList<>();
        int myLiberty = 0;

        if (row > 0) {
            hoodz.add(board[row-1][col]);
            moves.add(getIndex(row-1, col));
            myLiberty++;
        }
        if (row < nRow-1) {
            hoodz.add(board[row+1][col]);
            moves.add(getIndex(row+1, col));
            myLiberty++;
        }
        if (col > 0) {
            hoodz.add(board[row][col-1]);
            moves.add(getIndex(row, col-1));
            myLiberty++;
        }
        if (col < nCol-1) {
            hoodz.add(board[row][col+1]);
            moves.add(getIndex(row, col+1));
            myLiberty++;
        }

        piece myHood = null;

        Set<piece> foes = new HashSet<>(); // could be surrounded by foes of the same piece
        Set<piece> friends = new HashSet<>();

        for (int i = 0; i < hoodz.size(); i++) {
            piece neighbour = hoodz.get(i);
            if (null != neighbour) {
                myLiberty--;
                neighbour.liberty--;
                if (neighbour.isBlack == black) {
                    friends.add(neighbour);
                } else {
                    foes.add(neighbour);
                }
            } else {
                //isEye(moves.get(i));
            }
        }
        if (null != myHood) {
            myHood.positions.add(move);
            myHood.liberty += myLiberty;
        } else {
            board[row][col] = new piece(black, move, myLiberty);
        }
        // my foe's demise
        for (piece foe : foes) {
            if (0 == foe.liberty) {
                for (MCMove m : foe.positions) {
                    int i = m.getValue() / nCol;
                    int j = m.getValue() % nCol;
                    board[i][j] = null;
                }
            }
        }
    }

    public int getIndex(int i, int j) {
        return nCol*i + j;
    }
        
    public GoState() {
        this(5, 5);
    }

    public GoState(int nRow, int nCol) {
        playerToMove = 1;
        lastHash = 0;
        depth = 0;
        this.nRow = nRow;
        this.nCol = nCol;

        board = new piece[nRow][nCol];
        for (int i = 0; i < nRow; i++) {
            Arrays.fill(board[i], null); // init
        }
        allHash = new HashSet<>();
        allHash.add(computeHash());
    }
    
    private GoState(GoState source) {
        playerToMove = 1;
        lastHash = 0;
        depth = 0;
        this.nRow = source.nRow;
        this.nCol = source.nCol;
        
        board = new piece[nRow][nCol];
        Set<piece> uniqueSets = new HashSet<>();
        for(int i = 0; i < nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                if (null != source.board[i][j]) {
                    piece elem = source.board[i][j].clone();
                    if (uniqueSets.add(elem)) {
                        board[i][j] = elem;
                    }
                }
            }
        }
    }

    public int computeHash() {
        return Arrays.deepHashCode(board);
    }

    public boolean isPossible(int i, int j) {
        return isPossible(i, j, playerToMove);
    }

    public boolean isPossible (int i, int j, int player) {
        int opponent = 3 - player;

        if (i < 0 || j < 0 || i >= nRow || j >= nCol || board[i][j] != null) {
            return false;
        }

        boolean playerIsBlack = player == 1;

        boolean possible = false;

        Set<Integer> pieces = new HashSet<>();
        /*board[i][j] = playerMarker[player];
        if (isAlive(i, j, pieces)) {
            // This stone is immediately alive.
            possible = true;
        } else {
            // Warning! killer stone!
            possible = (i > 0 && board[i - 1][j] == opponent && false == isAlive(i - 1, j, pieces)) ||
                (i < nRow - 1 && board[i + 1][j] == opponent && false == isAlive(i + 1, j, pieces)) ||
                (j > 0 && board[i][j - 1] == opponent && false == isAlive(i, j - 1, pieces)) ||
                (j < nCol - 1 && board[i][j + 1] == opponent && false == isAlive(i, j + 1, pieces));
        }*/

        int hash = computeHash();

        if (possible && (hash == lastHash || allHash.contains(hash) || isEye(i, j, player))) {
            possible = false;
        }

        // killed
        board[i][j] = null;
        return possible;
    }

    public boolean isEye(int i, int j, int player) {
        boolean eye = true;
        /*if ((i > 0 && board[i - 1][j] != player) ||
                (i < nRow - 1 && board[i + 1][j] != player) ||
                (j > 0 && board[i][j - 1] != player) ||
                (j < nCol - 1 && board[i][j + 1] != player)) {
            eye = false;
        }*/
        return eye;
    }

    public boolean isAlive(int i_start, int j_start, Set<Integer> pieces) {
        piece player = board[i_start][j_start];
        if (player == null) {
            return true;
        }
        pieces.clear();
        Stack<Integer> sI = new Stack<>();
        sI.push(i_start);
        Stack<Integer> sJ = new Stack<>();
        sJ.push(j_start);

        while (false == sI.empty()) {
            int i = sI.pop();
            int j = sJ.pop();

            if (board[i][j] == player && pieces.add(getIndex(i, j))) {
                if (i > 0) {
                    sI.push(i-1);
                    sJ.push(j);
                }
                if (i < nRow - 1) {
                    sI.push(i+1);
                    sJ.push(j);
                }
                if (j > 0) {
                    sI.push(i);
                    sJ.push(j-1);
                }
                if (j < nCol - 1) {
                    sI.push(i);
                    sJ.push(j+1);
                }
            } else if (board[i][j] == null) {
                return true;
            }
        }
        return false;
    }

    public int getScore(int player) {
        int score = 0;
        for (int i = 0; i < nRow; ++i) {
            for (int j = 0; j < nCol; ++j) {
                piece p = board[i][j];
                boolean currentBlack = player == 1;
                if (board[i][j] == null) {
                    if (isEye(i, j, player)) {
                        score++;
                    }
                } else if (p.isBlack == currentBlack) {
                    score++;
                }
            }
        }
        return score;
    }

    public String printBoard() {
        String sBoard;
        sBoard = "Row = "+nRow+"\nCol = "+nCol+"\n";
        int id = 0;
        for (piece[] row : board) {
            sBoard += ' ';
            for (piece c : row) {
                if (null == c) {
                    id = 0;
                } else {
                    id = c.isBlack ? 1 : 2;
                }
                sBoard += " " + playerMarker[id];
            }
            sBoard += '\n';
        }
        return sBoard;
    }

    @Override
    public void doMove(MCMove move) {

        depth++;

        int opponent = 3 - playerToMove;

        if (move == PASS) {
            playerToMove = opponent;
            return;
        }

        setPiece(move, playerToMove == 1);

        // We save the hash values before all captures as this is way easier
        // to check.
        lastHash = computeHash();
        allHash.add(lastHash);

        // Next player
        playerToMove = opponent;
    }

    @Override
    public void doRandomMoves(Random r) {
        List<MCMove> moves = getMoves();
        if (false == moves.isEmpty()) {
            int index = r.nextInt(moves.size());
            MCMove move = moves.get(index);
            doMove(move);
        }
    }

    @Override
    public boolean hasMoves() {
        return false == getMoves().isEmpty();
    }

    @Override
    public List<MCMove> getMoves() {
        List<MCMove> moves = new ArrayList<>();
        if (depth > 1000) {
            return moves;
        }

        boolean opponentMove = false;
        for (int i = 0; i < nRow; ++i) {
            for (int j = 0; j < nCol; ++j) {
                if (isPossible(i, j)) {
                    moves.add(new MCMove(getIndex(i, j)));
                }

                if (false == opponentMove && isPossible(i, j, 3 - playerToMove)) {
                    opponentMove = true;
                }
            }
        }

        if (moves.isEmpty() && true == opponentMove) {
            moves.add(PASS);
        }

        return moves;
    }

    @Override
    public double getResult(int playerId) {
        int score1 = getScore(1);
        int score2 = getScore(2);

        if (score1 == score2) {
                return 0.5;
        }
        int winner = 0;
        if (score1 > score2) {
                winner = 1;
        } else {
                winner = 2;
        }

        if (playerId == winner) {
                return 0.0;
        } else {
                return 1.0;
        }
    }

    @Override
    public MCState copy() {
        return new GoState(this);
    }

    @Override
    public int getPlayerId() {
        return playerToMove;
    }
    
}
