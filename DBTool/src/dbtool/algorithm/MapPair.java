/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.algorithm;

import dbtool.domain.Table;
import dbtool.domain.View;
import java.util.Objects;

/**
 * Maps an element from a view to its corresponding table element.
 *
 * @author Koen
 */
public class MapPair {

    private final MapElement<View> viewElemental;

    private final MapElement<Table> tableElemental;

    public MapPair(MapElement<View> viewElemental, MapElement<Table> tableElemental) {
        this.viewElemental = viewElemental;
        this.tableElemental = tableElemental;
    }

    public MapElement<View> getViewElemental() {
        return viewElemental;
    }

    public MapElement<Table> getTableElemental() {
        return tableElemental;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.viewElemental);
        hash = 67 * hash + Objects.hashCode(this.tableElemental);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapPair other = (MapPair) obj;
        if (!Objects.equals(this.viewElemental, other.viewElemental)) {
            return false;
        }
        if (!Objects.equals(this.tableElemental, other.tableElemental)) {
            return false;
        }
        return true;
    }
    
    
}
