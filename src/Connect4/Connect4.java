package Connect4;

import Synapse.MonteCarlo.AI;
import Synapse.MonteCarlo.MCMove;
import Synapse.MonteCarlo.MCMoveImpl;

import java.util.Scanner;

/**
 * Created by cmk on 2016-03-15.
 */
public class Connect4 {

    private static void connect() {
        while (true) {
            boolean human_player = false;
            Scanner reader = new Scanner(System.in);

            int maxIt = 80000;

            C4State rules = new C4State();
            AI bot1 = new AI(rules, maxIt);
            AI bot2 = null;
            if (false == human_player) {
                bot2 = new AI(rules, maxIt);
            }
            MCMove move = MCMoveImpl.NO_MOVE;
            while (rules.hasMoves()) {
                System.out.print("State: ");
                rules.printBoard();

                if (rules.getPlayerId() == 2) {
                    move = bot1.doMove(move);
                } else {
                    if (human_player) {
                        while (true) {
                            System.out.print("Input your move: ");
                            move = new MCMoveImpl(reader.nextInt());
                            try {
                                rules.doMove(move);
                                break;
                            } catch (Exception e) {
                                System.out.println("invalid move");
                            }
                        }
                    } else {
                        move = bot2.doMove(move);
                    }
                }
            }

            System.out.print("\nFinal state: ");
            rules.printBoard();

            String winning;
            if (rules.getResult(2) == 1.0) {
                winning = "Player 1 wins!";
            } else if (rules.getResult(1) == 1.0) {
                winning = "Player 2 wins!";
            } else {
                winning = "Both of you sucks!";
            }
            System.out.println(winning);
        }
    }

    public static void main(String[] args) {
        try {
            connect();
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }

}
