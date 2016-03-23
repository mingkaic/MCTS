/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Go;

import Synapse.MonteCarlo.MCMove;
import Synapse.MonteCarlo.AI;
import java.util.Scanner;

/**
 *
 * @author mchen
 */
public class Go {
    private static void SeeUsGO() {
        boolean player1Human = true;
        boolean player2Human = true;
        Scanner reader = new Scanner(System.in);

        // AI settings
        int maxIt = 10000;
        int nTrees = 10;

        int turn, i, j;

	    GoState rules = new GoState();
        AI bot1 = null;
        AI bot2 = null;
        if (false == player1Human) {
            bot1 = new AI(rules, maxIt);
        }
        if (false == player2Human) {
            bot2 = new AI(rules, maxIt);
        }
        MCMove move = MCMove.NO_MOVE;
        while (rules.hasMoves()) {
            System.out.println("State: " );
            System.out.println(rules.printBoard());

            turn = rules.getPlayerId();

            if (turn == 1 && false == player1Human) {
                move = bot1.computeMove(move, rules);
            } else if (turn == 2 && false == player2Human) {
                move = bot2.computeMove(move, rules);
            } else {
                boolean stalling = true;
                while (stalling) {
                    move = MCMove.NO_MOVE;
                    System.out.print("Input Row: ");
                    i = reader.nextInt();
                    System.out.print("Input Col: ");
                    j = reader.nextInt();
                    move = new MCMove(rules.getIndex(i, j));

                    if (rules.isPossible(i, j)) {
                        stalling = false;
                    } else {
                        System.out.println("invalid move");
                    }
                }
            }
            rules.doMove(move);
        }

        System.out.println("\nFinal state: ");
        System.out.println(rules.printBoard());

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
            SeeUsGO();
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }
}
