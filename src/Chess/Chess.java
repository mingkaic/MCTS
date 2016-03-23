package Chess;

import Synapse.MonteCarlo.AI;
import Synapse.MonteCarlo.MCMove;

import java.util.List;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mchen
 */
public class Chess {
    private static void ChessInit() {
        boolean player1Human = true;
        boolean player2Human = true;
        Scanner reader = new Scanner(System.in);
        
        // AI settings
        int maxIt = 10000;
        int moveDepth = 100;

        int player, a, b, c, d;
	    ChessState rules = new ChessState();
        AI bot1 = null;
        AI bot2 = null;
        if (false == player1Human) {
            bot1 = new AI(rules, maxIt, moveDepth);
        }
        if (false == player2Human) {
            bot2 = new AI(rules, maxIt, moveDepth);
        }
        ChessState.ChessMove move = (ChessState.ChessMove) MCMove.NO_MOVE;
        while (rules.hasMoves()) {
            System.out.println("State: " );
            rules.printBoard();
            player = rules.getPlayerId();
            
            if (player == 1 && false == player1Human) {
                move = (ChessState.ChessMove) bot1.computeMove(move, rules);
            } else if (player == 2 && false == player2Human){
                move = (ChessState.ChessMove) bot2.computeMove(move, rules);
            } else {
                List<MCMove> moves = rules.getMoves();
                ChessState.ChessMove validMove = null;
                while (null == validMove) {
                    System.out.print("Select Letter: ");
                    String A = reader.next().toLowerCase();
                    if (false == moves.isEmpty() && moves.get(0).getValue() < 0) {
                        int moveId = 0;
                        switch(A.charAt(0)) {
                            case 'r':
                                moveId = -2;
                                break;
                            case 'k':
                                moveId = -3;
                                break;
                            case 'b':
                                moveId = -4;
                                break;
                            case 'q':
                                moveId = -5;
                                break;
                        }
                        for (MCMove m : moves) {
                            if (move.getValue() == moveId) {
                                validMove = (ChessState.ChessMove) m;
                            }
                        }
                    } else {
                        a = A.charAt(0)-'a';
                        System.out.print("Select Number: ");
                        b = reader.nextInt();
                        int prev = ChessState.coordHash(a, b);
                        System.out.print("Move to Letter: ");
                        String C = reader.next().toLowerCase();
                        c = C.charAt(0)-'a';
                        System.out.print("Move to Number: ");
                        d = reader.nextInt();
                        int next = ChessState.coordHash(c, d);
                        for (MCMove m : moves) {
                            ChessState.ChessMove cm = (ChessState.ChessMove) m;
                            if (prev == cm.hashPrev() && next == cm.hashNext()) {
                                validMove = cm;
                            }
                        }
                    }
                    if (validMove == null) {
                        System.out.println("invalid move");
                    }
                }
                move = validMove;
            }
            rules.doMove(move);
        }

        System.out.println("\nFinal state: ");
        rules.printBoard();

        String winning;
        if (rules.getResult(2) == 1.0) {
            winning = "Player 1 wins!";
        }
        else if (rules.getResult(1) == 1.0) {
            winning = "Player 2 wins!";
        }
        else {
            winning = "Both of you sucks!";
        }
        System.out.println(winning);
    }

    public static void main(String[] args) {
        try {
            ChessInit();
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }
}
