/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chess;

import static java.lang.Math.abs;
import java.util.Comparator;
import static java.lang.Math.abs;

/**
 *
 * @author mchen
 */
public class coord implements Comparator<coord> {
    
    public static coord roundNormal(coord origPos, coord movePos) {
        int x = origPos.x-movePos.x;
        int y = origPos.y-movePos.y;
        if (x != 0) x /= abs(x);
        if (y != 0) y /= abs(y);
        return new coord(x, y);
    }
    
    public final int x, y;
    public coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return x*8+y;
    }

    @Override
    public int compare(coord o1, coord o2) {
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof coord) {
            coord c = (coord) o;
            return c.x == x && c.y == y;
        }
        return false;
    }
}
