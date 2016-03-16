package MCTest;

import MCTS.MCMove;
import MCTS.MCTree;

import java.util.Scanner;

/**
 * Created by cmk on 2016-03-15.
 */
public class Connect4 {

    private static void connect() {
        boolean human_player = true;
        Scanner reader = new Scanner(System.in);

        int maxIt = 100000;
        int nTrees = 8;

        C4State rules = new C4State();
        do {
            System.out.print("State: " );
            rules.printBoard();

            MCMove move = MCMove.NO_MOVE;
            if (rules.getPlayerId() == 1) {
                rules.doMove(MCTree.computeMove(rules, maxIt, nTrees));
            } else {
                if (human_player) {
                    while (true) {
                        move = MCMove.NO_MOVE;
                        System.out.print("Input your move: ");
                        move = new MCMove(reader.nextInt());
                        try {
                            rules.doMove(move);
                            break;
                        } catch (Exception e) {
                            System.out.println("invalid move");
                        }
                    }
                } else {
                    rules.doMove(MCTree.computeMove(rules, maxIt, nTrees));
                }
            }
        } while (rules.hasMoves());

        System.out.print("\nFinal state: ");
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
            connect();
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }

}
