package dbtool.domain;

import dbtool.algorithm.MapElement;
import dbtool.algorithm.MapPair;
import dbtool.algorithm.Mapping;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Erik
 */
public class View implements ITable {

    private final String name;
    private final Query query;

    private final ViewType viewType;

    public View(String name, Query query, ViewType viewType) {
        this.name = name;
        this.query = query;
        this.viewType = viewType;
        mapAll();
    }

    private Set<MapPair> allMapPairs;

    /**
     * Maps all of the columns for this view to their corresponding tables.
     */
    private void mapAll() {
        this.allMapPairs = new HashSet<>();
        for (Column c : getColumns()) {
            for (ITable t : getQuery().getFrom()) {
                for (Column c2 : t.getColumns()) {
                    if (c.equals(c2)) {
                        MapElement<View> me1 = new MapElement<>(this, c);
                        MapElement<Table> me2;
                        if (t.getClass().equals(TableRef.class)) {
                            me2 = new MapElement<>((Table) ((TableRef) t).getRef(), c2);
                        } else {
                            me2 = new MapElement<>((Table) t, c2);
                        }
                        MapPair mp = new MapPair(me1, me2);
                        this.allMapPairs.add(mp);
                        Mapping.put(mp);
                    }
                }
            }
        }
    }

    public Set<MapPair> getAllMapPairs() {
        return allMapPairs;
    }

    @Override
    public List<Column> getColumns() {
        return query.getSelect();
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public String toSQLCreate() {
        switch (viewType) {
            case Default:
            default:
                return "CREATE VIEW " + name + " AS \n" + query.toSQL();
            case Materialized:
                return "CREATE MATERIALIZED VIEW " + name + " AS \n" + query.toSQL();
            case AsTable:
                return "CREATE TABLE " + name + " AS \n" + query.toSQL();
        }
    }

    @Override
    public String toSQLDrop() {
        switch (viewType) {
            case Default:
            default:
                return "DROP VIEW IF EXISTS " + name + ";";
            case Materialized:
                return "DROP MATERIALIZED VIEW IF EXISTS " + name + ";";
            case AsTable:
                return "DROP TABLE IF EXISTS " + name + " CASCADE ;";
        }
    }

    @Override
    public Column getPK() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getEntryCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks whether the given table is contained by this View.
     *
     * @param otherFrom
     * @return
     */
    public boolean isContained(Table otherFrom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.query);
        hash = 29 * hash + Objects.hashCode(this.viewType);
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
        final View other = (View) obj;
        return (Objects.equals(this.name, other.name)
                && Objects.equals(this.query, other.query)
                && this.viewType == other.viewType);
    }
}
