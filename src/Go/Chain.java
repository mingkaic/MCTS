/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Go;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author mchen
 */
public class Chain implements Comparator<Chain> {
    public Set<Movement> stones = new HashSet<>();
    public Stone[][] board;

    public Chain(Stone[][] board, Movement pieceNLoc) {
        this.board = board;
        if (pieceNLoc != null) {
            stones.add(pieceNLoc);
        }
    }
    
    public Set<Location> getLiberties() {
        Set<Location> surroundings = new HashSet<>();
        for (Movement stone : stones) {
            for (Location flanders : stone.placement.neighbours) {
                if (flanders.x > -1 && flanders.x < board.length &&
                    flanders.y > -1 && flanders.y < board[0].length) {
                    surroundings.add(flanders);
                }
            }
        }
        return surroundings.stream()
                .filter((l) -> (board[l.x][l.y] == Stone.EMPTY))
                .collect(Collectors.toSet());
    }

    public int getNLiberties() {
        return getLiberties().size();
    }

    public void addStone(Movement piece) {
        if (piece != null) {
            stones.add(piece);
        }
    }
    
    public void join(Chain chain) {
        if (null == chain) return;
        this.stones.addAll(chain.stones);
    }

    public Set<Location> getEyes() {
        Map<Location, Integer> surroundings = new HashMap<>();
        for (Movement stone : stones) {
            for (Location flanders : stone.placement.neighbours) {
                if (flanders.x > -1 && flanders.x < board.length &&
                    flanders.y > -1 && flanders.y < board[0].length) {
                    Integer i = surroundings.get(flanders);
                    if ( i == null ) {
                        surroundings.put(flanders, 0);
                    } else {
                        surroundings.put(flanders, i+1);
                    }
                }
            }
        }
        Set<Location> eyes = new HashSet<>();
        for (Location place : surroundings.keySet()) {
            if (surroundings.get(place) == 3) {
                eyes.add(place);
            }
        }
        return eyes;
    }
    
    public int getNEyes() {
        return getEyes().size();
    }

    @Override
    public int compare(Chain o1, Chain o2) {
        if (o2.stones.contains(o1.stones.toArray()[0]))
            return 0;
        return 1;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Chain) {
            Chain c = (Chain) o;
            return stones.contains(c.stones.toArray()[0]);
        }
        return false;
    }
    
}
