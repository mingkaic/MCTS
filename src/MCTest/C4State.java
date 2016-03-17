package MCTest;

import MCTS.MCMove;
import MCTS.MCState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by cmk on 2016-03-15.
 */
public class C4State implements MCState {

    public static char[] playerMarker = {'*', 'X', 'O'};
    private int playerToMove, nRow, nCol, lastRow, lastCol;
    private char[][] board;

    private void setUp(int nRow, int nCol) {
        playerToMove = 1;
        this.nRow = nRow;
        this.nCol = nCol;
        this.lastRow = this.lastCol = -1;

        board = new char[nRow][nCol];
        for (int i = 0; i < nRow; i++) {
            Arrays.fill(board[i], playerMarker[0]); // init
        }
    }

    public C4State() {
        setUp(6, 7);
    }

    public C4State(int nRow, int nCol) {
        setUp(nRow, nCol);
    }
    
    private C4State(C4State source) {
        playerToMove = source.playerToMove;
        this.nRow = source.nRow;
        this.nCol = source.nCol;
        this.lastRow = source.lastRow;
        this.lastCol = source.lastCol;
        
        board = new char[nRow][];
        for(int i = 0; i < nRow; i++) {
            board[i] = source.board[i].clone();
        }
    }

    @Override
    public void doMove(MCMove move) {
        int row = nRow - 1;
        while (board[row][move.getValue()] != playerMarker[0]) {
            row--;
        }
        board[row][move.getValue()] = playerMarker[playerToMove];
        lastCol = move.getValue();
        lastRow = row;

        playerToMove = 3 - playerToMove;
    }

    @Override
    public void doRandomMoves(Random r) {
        int rVal;
        do {
            rVal = r.nextInt(nCol);
        } while (board[0][rVal] != playerMarker[0]);
        doMove(new MCMove(rVal));
    }

    @Override
    public boolean hasMoves() {
        char winner = getWinner();
        if (winner != playerMarker[0]) {
            return false;
        }

        for (int col = 0; col < nCol; ++col) {
            if (board[0][col] == playerMarker[0]) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<MCMove> getMoves() {
        List<MCMove> moves = new ArrayList<>();
        if (getWinner() != playerMarker[0]) {
            return moves;
        }

        for (int col = 0; col < nCol; ++col) {
            if (board[0][col] == playerMarker[0]) {
                moves.add(new MCMove(col));
            }
        }
        return moves;
    }

    @Override
    public double getResult(int playerId) {
        char winner = getWinner();
        if (winner == playerMarker[0]) {
            return 0.5;
        }

        if (winner == playerMarker[playerId]) {
            return 0.0;
        }
        else {
            return 1.0;
        }
    }
    
    @Override
    public MCState copy() {
        return new C4State(this);
    }

    @Override
    public int getPlayerId() {
        return playerToMove;
    }

    private char getWinner() {
        if (lastCol < 0) {
            return playerMarker[0];
        }

        // We only need to check around the last piece played.
        char piece = board[lastRow][lastCol];

        // X X X X
        int left = 0, right = 0;
        for (int col = lastCol - 1; col >= 0 && board[lastRow][col] == piece; --col) {
            left++;
        }
        for (int col = lastCol + 1; col < nCol && board[lastRow][col] == piece; ++col) {
            right++;
        }
        if (left + 1 + right >= 4) {
            return piece;
        }

        // X
        // X
        // X
        // X
        int up = 0, down = 0;
        for (int row = lastRow - 1; row >= 0 && board[row][lastCol] == piece; --row) {
            up++;
        }
        for (int row = lastRow + 1; row < nRow && board[row][lastCol] == piece; ++row) {
            down++;
        }
        if (up + 1 + down >= 4) {
            return piece;
        }

        // X
        //  X
        //   X
        //    X
        up = down = 0;
        for (int row = lastRow - 1, col = lastCol - 1; row >= 0 && col >= 0 && board[row][col] == piece; --row, --col) {
            up++;
        }
        for (int row = lastRow + 1, col = lastCol + 1; row < nRow && col < nCol && board[row][col] == piece; ++row, ++col) {
            down++;
        }
        if (up + 1 + down >= 4) {
            return piece;
        }

        //    X
        //   X
        //  X
        // X
        up = down = 0;
        for (int row = lastRow + 1, col = lastCol - 1; row < nRow && col >= 0 && board[row][col] == piece; ++row, --col) {
            up++;
        }
        for (int row = lastRow - 1, col = lastCol + 1; row >= 0 && col < nCol && board[row][col] == piece; --row, ++col) {
            down++;
        }
        if (up + 1 + down >= 4) {
            return piece;
        }

        return playerMarker[0];
    }

    public void printBoard() {
        System.out.print("\n  ");
        for (int col = 0; col < nCol; ++col) {
            System.out.print(col+" ");
        }
        System.out.print("\n");
        for (int row = 0; row < nRow; ++row) {
            System.out.print("|");
            for (char c : board[row]) {
                System.out.print(" "+c);
            }
            System.out.println(" |");
        }
        System.out.print("+-");
        for (int col = 0; col < nCol - 1; ++col) {
            System.out.print("--");
        }
        System.out.println("--+");
        System.out.println(playerMarker[playerToMove]+" to move");
    }
}
