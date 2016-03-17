package MCTS;

import java.util.Comparator;

/**
 * Created by cmk on 2016-03-15.
 */
public class MCMove implements Comparator<MCMove> {
    public static final MCMove NO_MOVE = new MCMove(-1);

    private final int value;

    public MCMove(final int value)  {this.value = value;}

    public int getValue() {return value;}

    @Override
    public int compare(MCMove o1, MCMove o2) {
        return 0;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        MCMove m = (MCMove) o;
        return m.getValue() == value;
    }
}
