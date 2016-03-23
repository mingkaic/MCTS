package Chess;


import Synapse.MonteCarlo.MCMove;
import Synapse.MonteCarlo.MCState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mchen
 */
public class ChessState implements MCState {
    
    private char playerMarker[] = {'*', 'W', 'B'};
    private char chessMarker[] = {'P', 'R', 'N', 'B', 'Q', 'K'};
    private int pieceScore[] = {1, 5, 3, 3, 9, 0};
    private double maxScore = 8*pieceScore[0]+2*pieceScore[1]+2*pieceScore[2]+2*pieceScore[3]+pieceScore[4];
    private PlayerPiece[][] board = new PlayerPiece[8][8];
    private int player = 1;
    private int turn = 0;
    private ChessMove pawnPromote = null;

    private enum Piece {
        PAWN(0), ROOK(1), KNIGHT(2), BISHOP(3), QUEEN(4), KING(5);
        private final int value;
        Piece(final int value) {this.value = value;}
    }
    
    private class PlayerPiece {
        public final Piece p;
        public final int player;
        public int lastMoved = -1;
        
        PlayerPiece(Piece p, int player) { this.p = p; this.player = player; }
        
        PlayerPiece(PlayerPiece p) {
            this.p = p.p; 
            player = p.player; 
            lastMoved = p.lastMoved; 
        }
    }
    
    public static int coordHash(int x, int y) {
        return x*8+y;
    }

    public void printBoard() {
        System.out.print("\n  ");
        for (int col = 0; col < 8; ++col) {
            System.out.print(col+"  ");
        }
        System.out.print("\n");
        for (int row = 0; row < 8; ++row) {
            System.out.print((char)('A'+row)+" ");
            for (PlayerPiece p : board[row]) {
                if (null == p) {
                    System.out.print(playerMarker[0]+"  ");
                } else {
                    System.out.print(playerMarker[p.player]+""+chessMarker[p.p.value]+" ");
                }
            }
            System.out.println("|");
        }
        System.out.print("+-");
        for (int col = 0; col < 8 - 1; ++col) {
            System.out.print("---");
        }
        System.out.println("--+");
        System.out.println(playerMarker[player]+" to move");
    }
    
    public class ChessMove extends MCMove {
        private Piece p;
        private int x = 0, y = 0, prevX = 0, prevY = 0;
        
        public ChessMove(int value) { super(value); }
        
        public ChessMove(Piece p, int move_x, int move_y, int x, int y) {
            super((p.value+1) * coordHash(x, y) * coordHash(move_x, move_y));
            this.x = move_x;
            this.y = move_y;
            this.prevX = x;
            this.prevY = y;
            this.p = p;
        }
        
        public int hashPrev() {
            return coordHash(prevX, prevY);
        }
        
        public int hashNext() {
            return coordHash(x, y);
        }
    }
    
    // if king, doesn't check for checks
    private List<ChessMove> getMoves(PlayerPiece p, int x, int y) {
        List<ChessMove> moves = new ArrayList<>();
        int side = p.player;
        int offsetGen[][] = null;
        int offsetRook[][] = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        int offsetBishop[][] = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        int offsetQueen[][] = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}, 
                                {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        int[][] knightOffset = {{1, 2}, {2, 1}, 
                                {-1, 2}, {-2, 1}, 
                                {1, -2}, {2, -1}, 
                                {-1, -2}, {-2, -1}};
        switch(p.p) {
            case PAWN:
                int direction = 1;
                if (side == 2) direction = -1;
                if (null == board[x+direction][y]) {
                    moves.add(new ChessMove(p.p, direction+x, y, x, y));
                }
                
                int[] y_offsets = {-1, 1};
                for (int horz :  y_offsets )
                if (y+horz > -1 && y+horz < 8) {
                    // en passe
                    if (null != board[x][y+horz] && 
                        board[x][y+horz].lastMoved == turn-1) {
                        moves.add(new ChessMove(p.p, x+direction, y+horz, x, y));
                    } else if (null != board[x+direction][y+horz] && 
                        board[x+direction][y+horz].player != side) {
                        moves.add(new ChessMove(p.p, x+direction, y+horz, x, y));
                    }
                }
                
                if (0 > p.lastMoved) {
                    moves.add(new ChessMove(p.p, 2*direction+x, y, x, y));
                }
                break;
            case ROOK:
                offsetGen = offsetRook;
            case BISHOP:
                if (null == offsetGen) {
                    offsetGen = offsetBishop;
                }
            case QUEEN:
                if (null == offsetGen) {
                    offsetGen = offsetQueen;
                }
                try {
                for (int[] offset : offsetGen) {
                    int i = 1;
                    boolean flag = x+offset[0]*i > -1 && x+offset[0]*i < 8 && 
                            y+offset[1]*i > -1 && y+offset[1]*i < 8;
                    while (flag && null == board[x+offset[0]*i][y+offset[1]*i]) {
                        moves.add(new ChessMove(p.p, x+offset[0]*i, y+offset[1]*i, x, y));
                        i++;
                        flag = x+offset[0]*i > -1 && x+offset[0]*i < 8 && 
                            y+offset[1]*i > -1 && y+offset[1]*i < 8;
                    }
                    if (flag && board[x+offset[0]*i][y+offset[1]*i].player != side) { // enemy encountered
                        moves.add(new ChessMove(p.p, x+offset[0]*i, y+offset[1]*i, x, y));
                    }
                }
                } catch (Exception e) {
                    System.out.println("hoot");
                }
                break;
            case KNIGHT:
                offsetGen = knightOffset;
            case KING:
                if (null == offsetGen) {
                    offsetGen = offsetQueen;
                }
                try {
                for (int[] offset : offsetGen) {
                    int i = 1;
                    boolean flag = x+offset[0]*i > -1 && x+offset[0]*i < 8 && 
                            y+offset[1]*i > -1 && y+offset[1]*i < 8;
                    if (flag && 
                        (null == board[x+offset[0]][y+offset[1]] || 
                         side != board[x+offset[0]][y+offset[1]].player)) {
                        moves.add(new ChessMove(p.p, x+offset[0], y+offset[1], x, y));
                    }
                }
                } catch (Exception e) {
                    System.out.println("hoot2");
                }
                break;
        }
        
        return moves;
    }
    
    private boolean endangerKing(int x, int y, int kingX, int kingY, List<ChessMove> enemyMoves) {
        
        return true;
    }
    
    public ChessState() {
        board[0][0] = new PlayerPiece(Piece.ROOK, 1);
        board[0][1] = new PlayerPiece(Piece.KNIGHT, 1);
        board[0][2] = new PlayerPiece(Piece.BISHOP, 1);
        board[0][3] = new PlayerPiece(Piece.QUEEN, 1);
        board[0][4] = new PlayerPiece(Piece.KING, 1);
        board[0][5] = new PlayerPiece(Piece.BISHOP, 1);
        board[0][6] = new PlayerPiece(Piece.KNIGHT, 1);
        board[0][7] = new PlayerPiece(Piece.ROOK, 1);
        for (int i = 0; i < 8; i++) {
            board[1][i] = new PlayerPiece(Piece.PAWN, 1);
        }
        for (int i = 2; i < 6; i++) {
            Arrays.fill(board[i], null);
        }
        for (int i = 0; i < 8; i++) {
            board[6][i] = new PlayerPiece(Piece.PAWN, 2);
        }
        board[7][0] = new PlayerPiece(Piece.ROOK, 2);
        board[7][1] = new PlayerPiece(Piece.KNIGHT, 2);
        board[7][2] = new PlayerPiece(Piece.BISHOP, 2);
        board[7][3] = new PlayerPiece(Piece.QUEEN, 2);
        board[7][4] = new PlayerPiece(Piece.KING, 2);
        board[7][5] = new PlayerPiece(Piece.BISHOP, 2);
        board[7][6] = new PlayerPiece(Piece.KNIGHT, 2);
        board[7][7] = new PlayerPiece(Piece.ROOK, 2);
    }
    
    public ChessState(ChessState other) {
        for (int i = 0; i < other.board.length; i++) {
            for (int j = 0; j < other.board[0].length; j++) {
                if (null != other.board[i][j]) {
                    board[i][j] = new PlayerPiece(other.board[i][j]);
                }
            }
        }
        player = other.player;
        turn = other.turn;
    }
    
    @Override
    public void doMove(MCMove move) {
        ChessMove cMove = (ChessMove)move;
        if (null == pawnPromote) {
            int posX = cMove.prevX;
            int posY = cMove.prevY;
            int endLine = player == 1 ? 7 : 0;
            PlayerPiece piece = board[posX][posY];

            if (null == piece) {
                // error handle
            } else {
                if (piece.p == Piece.PAWN) {
                    if (cMove.y != cMove.prevY && null == board[cMove.x][cMove.y]) { // en passee
                        board[cMove.x-1][cMove.y] = null; 
                    }
                    if (cMove.x == endLine) {
                        pawnPromote = cMove;
                    }
                }
                board[cMove.x][cMove.y] = piece;
                board[posX][posY] = null;
                piece.lastMoved = turn;
            }
        } else {
            int posX = pawnPromote.x;
            int posY = pawnPromote.y;
            Piece promote = Piece.values()[-cMove.getValue()];
            board[posX][posY] = new PlayerPiece(promote, player);
            pawnPromote = null;
        }

        if (null == pawnPromote) {
            player = 3 - player;
            turn++;
        }
    }

    @Override
    public void doRandomMoves(Random r) {
        List<MCMove> moves = getMoves();
        if (false == moves.isEmpty()) {
            int index = r.nextInt(moves.size());
            doMove(moves.get(index));
        }
    }

    @Override
    public boolean hasMoves() {
        return false == getMoves().isEmpty();
    }

    @Override
    public List<MCMove> getMoves() { // TODO: implement castling and promotion
        List<MCMove> moves = new ArrayList<>();
        if (null == pawnPromote) {
            List<ChessMove> enemyMoves = new ArrayList<>();
            List<ChessMove> kingMove = new ArrayList<>();

            int kingX = -1, kingY = -1;
            
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (null != board[x][y]) {
                        if (board[x][y].player == player) {
                            if (board[x][y].p == Piece.KING) {
                                kingX = x;
                                kingY = y;
                            }
                        } else {
                            enemyMoves.addAll(getMoves(board[x][y], x, y));
                        }
                    }
                }
            }

            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (null != board[x][y]) {
                        if (board[x][y].player == player && 
                            false == endangerKing(x, y, kingX, kingY, enemyMoves)) {
                            if (board[x][y].p != Piece.KING) {
                                kingMove.addAll(getMoves(board[x][y], x, y));
                            } else {
                                moves.addAll(getMoves(board[x][y], x, y));
                            }
                        }
                    }
                }
            }

            boolean checked = false;
            List<MCMove> actualKingMoves = new ArrayList<>();
            for (ChessMove attempt : enemyMoves) {
                if (attempt.x == kingX && attempt.y == kingY) {
                    checked = true;
                }
                for (MCMove km : kingMove) {
                    ChessMove ck = (ChessMove) km;
                    if (ck.x == attempt.x && ck.y == attempt.y) {
                    } else {
                        actualKingMoves.add(km);
                    }
                }
            }
            if (checked) {
                moves = actualKingMoves;
            } else {
                moves.addAll(kingMove);
            }   
        } else {
            moves.add(new ChessMove(-2)); // promote to rook
            moves.add(new ChessMove(-3)); // promote to knight
            moves.add(new ChessMove(-4)); // promote to bishop
            moves.add(new ChessMove(-5)); // promote to queen
        }
        
        return moves;
    }

    @Override
    public double getResult(int playerId) {
        if (false == hasMoves()) {
            if (player == playerId) {
                return 0;
            }
            return 1;
        } 
        double score = 0;
        for (PlayerPiece row[] : board) {
            for (PlayerPiece p : row) {
                if (p == null) {
                    
                } else if (p.player == playerId) {
                    score+=pieceScore[p.p.value];
                } else {
                    score-=pieceScore[p.p.value]/2;
                }
            }
        }
        return (score-0.5)/maxScore; // not quite as good as winning
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
