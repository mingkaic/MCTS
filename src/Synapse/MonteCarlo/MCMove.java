package Synapse.MonteCarlo;

import java.util.Comparator;

public interface MCMove <T> extends Comparator<T> {
    public void setWeight(double w);
    public double getWeight();
}
