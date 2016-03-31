package Synapse.MonteCarlo;

/**
 * Created by cmk on 2016-03-15.
 */
public class MCMoveImpl implements MCMove<MCMoveImpl> {
    public static final MCMove NO_MOVE = new MCMoveImpl(-1);

    public final int value;

    public MCMoveImpl(final int value)  {this.value = value;}

    @Override
    public int compare(MCMoveImpl o1, MCMoveImpl o2) {
        return 0;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof  MCMoveImpl) {
            MCMoveImpl m = (MCMoveImpl) o;
            return m.value == value;
        }
        return false;
    }
}
