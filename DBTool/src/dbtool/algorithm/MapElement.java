/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.algorithm;

import dbtool.domain.Column;
import java.util.Objects;

/**
 * Object representing column and its parent (View/Table) for mapping. Generic
 * to have nice distinction between different types.
 *
 * @author Koen
 * @param <U> The type of the parent. Either a Veiw or a Table
 */
public class MapElement<U> {

    private final U parent;
    private final Column column;

    public MapElement(U target, Column column) {
        this.parent = target;
        this.column = column;
    }

    /**
     * Gets the object to which this column belongs (view or table).
     *
     * @return
     */
    public U getParent() {
        return parent;
    }

    public Column getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.parent);
        hash = 67 * hash + Objects.hashCode(this.column);
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
        final MapElement<?> other = (MapElement<?>) obj;
        if (!Objects.equals(this.parent, other.parent)) {
            return false;
        }
        if (!Objects.equals(this.column, other.column)) {
            return false;
        }
        return true;
    }
    
    

}
