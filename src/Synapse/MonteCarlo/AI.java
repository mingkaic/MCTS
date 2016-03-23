package Synapse.MonteCarlo;

/**
 * Created by cmk on 2016-03-14.
 */
public class AI <Rules extends MCState> {

    private MCSynapse syn; // single synapse TODO 3: spread out synapse, and make it thread-safe
    private int prevTraining;

    public AI(Rules policy, int nTraining) {
        prevTraining = nTraining;
        syn = new MCSynapse(policy);

        for (int i = 0; i < nTraining; i++) {
            syn.train(policy, null);
        }
    }

    public AI(Rules policy, int nTraining, int limitSim) {
        prevTraining = nTraining;
        syn = new MCSynapse(policy);

        syn.simLimit = limitSim; // prevents the simulation from looking too far/speeds up simulation

        for (int i = 0; i < nTraining; i++) {
            syn.train(policy, null);
        }
    }

    public void updateSynapse(MCMove lastMove, Rules policy) {
        syn.moveDown(lastMove, policy);
        for (int i = 0; i < prevTraining/2; i++) {
            syn.train(policy, null);
        }
    }

    public MCMove computeMove(MCMove lastMove, Rules policy) {
            if (false == lastMove.equals(MCMove.NO_MOVE)) {
                updateSynapse(lastMove, policy);
            }
            MCMove bestMove = syn.output(policy);
            updateSynapse(bestMove, policy);
            return bestMove;
    }
}
