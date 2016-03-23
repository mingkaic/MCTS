package Synapse;

/**
 *
 * @author mchen
 */
public interface Synapse <Input, Output> {
    abstract public void train(Input i, Output o);
    abstract public Output output(Input i);
}