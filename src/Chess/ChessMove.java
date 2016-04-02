/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chess;

import Synapse.MonteCarlo.MCMove;
import com.mgs.chess.core.Location;
import com.mgs.chess.core.Piece;
import com.mgs.chess.core.PieceOnLocation;
import com.mgs.chess.core.movement.Movement;

/**
 *
 * @author mchen
 */
public class ChessMove extends Movement implements MCMove<ChessMove> {

    public ChessMove(Piece piece, Location from, Location to) {
        super(piece, from, to);
    }
    public ChessMove(PieceOnLocation piece, Location to) {
        super(piece, to);
    }

    @Override
    public int compare(ChessMove o1, ChessMove o2) {
        return 0;
    }

    @Override
    public void setWeight(double w) {

    }

    @Override
    public double getWeight() {
        return 0;
    }
}