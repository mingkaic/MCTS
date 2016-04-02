/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Go;

import Synapse.MonteCarlo.AI;
import Synapse.MonteCarlo.MCMove;

import java.util.List;
import java.util.Scanner;

public class Go {
    Go() {
        GoState rule = new GoState();
        Scanner in = new Scanner(System.in);

        boolean human1 = false;
        boolean human2 = false;

        int nTraining = 10000;
        int nTree = 1;
        int depth = 100;

        AI bot1 = new AI(rule, nTraining, nTree, depth);
        AI bot2 = new AI(rule, nTraining, nTree, depth);
            
        List<MCMove> moves = rule.getMoves();
        MCMove validM = null;
        while (moves.size() > 0) {
            rule.printBoard();
            if (false == human1 && rule.getPlayerId() == 1) {
                validM = bot1.doMove(validM);
            } else if (false == human2 && rule.getPlayerId() == 2) {
                validM = bot2.doMove(validM);
            } else {
                validM = null;
                while (validM == null) {
                    String lstr = in.next().toLowerCase();
                    int x = lstr.charAt(0) - 'a';
                    int y = lstr.charAt(1) - 'a';
                    Location l = new Location(x, y);
                    for (MCMove m : moves) {
                        if (((Movement) m).placement.equals(l)) {
                            validM = m;
                        }
                    }
                }
                rule.doMove(validM);
            }
            moves = rule.getMoves();
        }
        System.out.println("game over! Blacks result: "+rule.getResult(2) + ", White result: "+rule.getResult(1));
    }
    
    public static void main(String[] args) {
        try {
            new Go();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}
