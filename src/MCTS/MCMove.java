package MCTS;

/**
 * Created by cmk on 2016-03-15.
 */
public class MCMove {
    public static final MCMove NO_MOVE = new MCMove(-1);

    private final int value;

    public MCMove(final int value)  {this.value = value;}

    public int getValue() {return value;}
}
