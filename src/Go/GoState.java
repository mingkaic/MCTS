/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Go;

import Synapse.MonteCarlo.MCMove;
import Synapse.MonteCarlo.MCState;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author mchen
 */

public class GoState implements MCState {
    private final int width;
    private final int height;
    private int playerToMove = 1;
    private Stone[][] board;
    private Set<Chain> white_chainz = new HashSet<>();
    private Set<Chain> black_chainz = new HashSet<>();
    
    private static final char markers[] = {'*', 'B', 'W'};
    
    public void printBoard() {
        System.out.print("\n    ");
        for (int col = 0; col < width; ++col) {
            System.out.print(""+(char)('A'+col)+" ");
        }
        System.out.print("\n  Y X");
        for (int col = 0; col < width - 1; ++col) {
            System.out.print("--");
        }
        System.out.println("-+");
        for (int y = 0; y < height; y++) {
            System.out.print(""+(char)('A'+y)+" |");
            for (int x = 0; x < width; x++) {
                char c;
                switch (board[x][y]) {
                    case BLACK :
                        c = markers[1];
                        break;
                    case WHITE :
                        c = markers[2];
                        break;
                    default :
                        c = markers[0];
                }
                System.out.print(" "+c);
            }
            System.out.println(" |");
        }
        System.out.print("  +-");
        for (int col = 0; col < width - 1; ++col) {
            System.out.print("--");
        }
        System.out.println("--+");
        System.out.println(markers[playerToMove]+" to move");
    }

    public GoState () {
        this(5, 5);
    }

    public GoState (int x, int y) {
        width = x;
        height = y;
        board = new  Stone[x][y];
        for (int i = 0; i < width; i++) {
            Arrays.fill(board[i], Stone.EMPTY);
        }
    }
    
    private GoState (GoState cpy) {
        width = cpy.width;
        height = cpy.height;
        board = new  Stone[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board[i][j] = cpy.board[i][j];
            }
        }
        playerToMove = cpy.playerToMove;
    }
    
    @Override
    public void doMove(MCMove move) {
        int opponent = 3 - playerToMove;
        if (move != null) {
            updateBoard((Movement)move);
        }

        playerToMove = opponent;
    }

    @Override
    public void doRandomMoves(Random r) {
        List<MCMove> moves = getMoves();
        if (false == moves.isEmpty()) {
            int index = r.nextInt(moves.size());
            Movement move = (Movement) moves.get(index);
            doMove(move);
        }
    }

    @Override
    public boolean hasMoves() {
        return false == getMoves().isEmpty();
    }

    @Override
    public ArrayList<MCMove> getMoves() {
        ArrayList<MCMove> moves = new ArrayList<>();

        Stone s = playerToMove == 1 ? Stone.BLACK : Stone.WHITE;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                if (board[i][j] == Stone.EMPTY) {
                    Movement candidate = new Movement(s, new Location(i, j));
                    if (isPossible(candidate)) {
                        moves.add(candidate);
                    }
                }
            }
        }

        return moves;
    }

    @Override
    public double getResult(int playerId) {
        int score1 = getScore(Stone.BLACK);
        int score2 = getScore(Stone.WHITE);

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

    private boolean isPossible(Movement move) {
        Location place = move.placement;
        if (canCapture(move)) return true;
        // prevent suicide
        return false == canSuicide(move);
    }
    
    private boolean canSuicide(Movement move) {
        Set<Chain> muhChainz;
        Stone enemy;
        if (move.piece == Stone.BLACK) {
            muhChainz = black_chainz;
            enemy = Stone.WHITE;
        } else {
            muhChainz = white_chainz;
            enemy = Stone.BLACK;
        }
        
        Location place = move.placement;
        
        int strikes = 0;
        for (Location flanders : place.neighbours) {
            if (flanders.x > -1 && flanders.x < board.length &&
                flanders.y > -1 && flanders.y < board[0].length && 
                enemy == board[flanders.x][flanders.y]) {
                strikes++;
            }
        }
        if (strikes == 4) return true; // totally in eye
        
        List<Chain> flanderChainz = muhChainz.stream()
            .filter((Chain chain) -> {
                for (Location flanders : place.neighbours) {
                    if (chain.stones.contains(flanders)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
        
        Chain simChain = new Chain(board, null);
        for (Chain flanders : flanderChainz) {
            simChain.join(flanders);
        }
        Set<Location> eyes = simChain.getEyes();
        int nEyes = eyes.size();
        if (nEyes > 1 && eyes.contains(place)) {
            nEyes--;
        }
        // we know simChain is a neighbour... so if there's only 1 liberty, the lone liberty must be place
        return nEyes > 1 || simChain.getNLiberties() > 1;
    }

    private void updateBoard(Movement move) {
        Set<Chain> chainz = white_chainz;
        Set<Chain> foeChainz = black_chainz;
        if (move.piece == Stone.BLACK) {
            chainz = black_chainz;
            foeChainz = white_chainz;
        }
        List<Chain> muhChainz = new ArrayList<>();
        
        for (Chain jz : chainz) {
            Set<Location> libs = jz.getLiberties();
            if (libs.contains(move.placement)) {
                muhChainz.add(jz);
            }
        }
        Chain finalChain = null;
        for (Chain c : muhChainz) {
            chainz.remove(c);
            c.join(finalChain);
            finalChain = c;
        }
        if (finalChain == null) {
            chainz.add(new Chain(board, move));
        } else {
            finalChain.addStone(move);
            chainz.add(finalChain);
        }

        board[move.placement.x][move.placement.y] = move.piece; // place on board
        
        // clearout dead pieces
        List<Chain> ripperino = foeChainz.stream()
                .filter((foe)->{
                    return 0 == foe.getNLiberties() && foe.getNEyes() < 2;
                }).collect(Collectors.toList());
        for (Chain rip : ripperino) {
            rip.stones.stream().forEach((m) -> {
                board[m.placement.x][m.placement.y] = Stone.EMPTY;
            });
            foeChainz.remove(rip);
        }
    }

    private int getScore(Stone color) {
        int score = 0;

        for (Stone[] row : board) {
            for (Stone s : row) {
                if (s == color) score++;
            }
        }

        return score;
    }

    private boolean canCapture(Movement move) {
        Location place = move.placement;
        Set<Chain> foez = move.piece == Stone.BLACK ? white_chainz : black_chainz;

        for (Chain f : foez) {
            Set<Location> libs = f.getLiberties();
            if (f.getNEyes() < 2 && libs.size() == 1 && libs.contains(place)) {
                return true;
            }
        }
        
        return false;
    }
    
}
