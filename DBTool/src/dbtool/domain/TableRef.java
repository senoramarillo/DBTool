/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.domain;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Erik
 */
public class TableRef implements ITable {

    private final String name;
    private final ITable ref;

    public TableRef(String as, ITable ref) {
        this.name = as;
        this.ref = ref;
    }

    public TableRef(ITable ref) {
        this.name = ref.getName();
        this.ref = ref;
    }

    /**
     * Create a table ref with suffix to be added to its name (to create alias in query).
     * @param ref
     * @param suffix 
     */
    public TableRef(ITable ref, String suffix) {
        this.name = ref.getName() + suffix;
        this.ref = ref;
    }

    public ITable getRef() {
        return ref;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        return ref.getName() + " " + name;
    }

    @Override
    public List<Column> getColumns() {
        return ref.getColumns();
    }

    @Override
    public String toSQLCreate() {
        return ref.toSQLCreate();
    }

    @Override
    public String toSQLDrop() {
        return ref.toSQLDrop();
    }

    @Override
    public Column getPK() {
        return ref.getPK();
    }

    @Override
    public int getEntryCount() {
        return ref.getEntryCount();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.ref);
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
        final TableRef other = (TableRef) obj;
        return (Objects.equals(this.name, other.name) && Objects.equals(this.ref, other.ref));
    }
}
