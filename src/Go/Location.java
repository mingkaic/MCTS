/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Go;

import java.util.Comparator;

/**
 *
 * @author mchen
 */
public class Location implements Comparator<Location> {
    public final int x;
    public final int y;
    public final Location neighbours[];
    
    private Location(int x, int y, Location neighbours[]) {
        this.x = x;
        this.y = y;
        this.neighbours = neighbours;
    }
    
    public Location(int x, int y) {
        this(x, y,  new Location[]{
            new Location(x, y+1, null),
            new Location(x, y-1, null),
            new Location(x-1, y, null),
            new Location(x+1, y, null)
        });
    }

    @Override
    public int compare(Location o1, Location o2) {
        return o2.hashCode()-o1.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Location) {
            Location l = (Location) o;
            return x == l.x && y == l.y;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return x*1000+y;
    }
}
