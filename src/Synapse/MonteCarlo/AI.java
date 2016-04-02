package Synapse.MonteCarlo;

public class AI <Policy extends MCState> {
    private static final int DEFAULT_NSYN = 8;
    private MCSynapse syns[];
    private int nTraining;
    private int sampleRate = 100;
    private Policy policy;

    private class TrainingSession implements Runnable {
        MCSynapse syn;
        int nTraining;
        TrainingSession(MCSynapse syn, int nTraining) { this.syn = syn; this.nTraining = nTraining; }

        @Override
        public void run() {
            double stdev = 1;
            Policy snapshot = null;
            synchronized (policy) {
                snapshot = (Policy) policy.copy();
            }
            if (snapshot != null) {
                for (int i = 0; i < nTraining && stdev > 0.1; i++) {
                    if (i % sampleRate == sampleRate - 1) {
                        stdev = syn.convergence();
                    }
                    syn.train(snapshot, null);
                }
            }
        }
    }

    public AI(Policy policy, int nTraining) {
        this(policy, nTraining, DEFAULT_NSYN);
    }

    public AI(Policy policy, int nTraining, int nSyns) {
        this.policy = policy;
        this.nTraining = nTraining;
        this.syns = new MCSynapse[nSyns];
        for (int i = 0; i < syns.length; i++) {
            syns[i] = new MCSynapse(policy);
        }
    }

    public AI(Policy policy, int nTraining, int nSyns, int simLimit) {
        this(policy, nTraining, nSyns);
        for (MCSynapse s : syns) {
            s.simLimit = simLimit; // sets a limit to the depth of the tree
        }
    }

    private void training(Policy state) {
        if (syns.length == 1) {
            new TrainingSession(syns[0], (int) (nTraining - syns[0].rootVisits())).run();
        } else {
            Thread sess[] = new Thread[syns.length];
            for (int i = 0; i < syns.length; i++) {
                MCSynapse syn = syns[i];
                sess[i] = new Thread(new TrainingSession(syn, (int) (nTraining - syn.rootVisits())));
                sess[i].start();
            }

            for (Thread t : sess) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateSynapse(MCMove lastMove, Policy policy) {
        for (MCSynapse syn : syns) {
            syn.moveDown(lastMove, policy);
        }
        training(policy);
    }

    public MCMove doMove(MCMove lastMove) {
        if (null != lastMove && false == lastMove.equals(MCMoveImpl.NO_MOVE)) {
            updateSynapse(lastMove, policy);
        } else {
            training(policy);
        }
        MCSynapse.MCCollection col = new MCSynapse.MCCollection();
        for (MCSynapse syn : syns) {
            col.collect(syn);
        }
        MCMove bestmove = MCNode.bestMove(col.collection);
        policy.doMove(bestmove);
        updateSynapse(bestmove, policy);
        return bestmove;
    }
}
