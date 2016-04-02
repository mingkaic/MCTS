/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Go;

import Synapse.MonteCarlo.MCMove;

/**
 *
 * @author mchen
 */
public class Movement implements MCMove {
    Stone piece;
    Location placement;
    public Movement(Stone piece, Location placement) {
        this.piece = piece;
        this.placement = placement;
    }

    @Override
    public void setWeight(double w) {

    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public int compare(Object o1, Object o2) {
        return 0;
    }
}
