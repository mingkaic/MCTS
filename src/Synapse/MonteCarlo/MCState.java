package Synapse.MonteCarlo;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by cmk on 2016-03-14.
 */
public interface MCState {

    public void doMove(MCMove move);

    public void doRandomMoves(Random r);

    public boolean hasMoves();

    public ArrayList<MCMove> getMoves();

    public double getResult(int playerId);
    
    public MCState copy();

    public int getPlayerId();

}
