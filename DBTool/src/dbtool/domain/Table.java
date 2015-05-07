package dbtool.domain;

import dbtool.exceptions.DatabaseBoxException;
import dbtool.exceptions.SQLFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Erik
 */
public class Table implements ITable {
    private final String name;
    private final List<Column> columns;
    
    // Used for getting the PK and FK sizes.
    private int entries;
    
    public Table(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
    }

    @Override
    public List<Column> getColumns() {
        return columns;
    }

    public void addColumns(Column column) {
        this.columns.add(column);
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getFullName(){
        return name;
    }
    
    // I have a preference in changing this function that it also
    // executes the query.
    @Override
    public String toSQLCreate() {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE ").append(getName()).append(" (");
        
        boolean firstColumn = true;
        for(Column c : columns){
            if (!firstColumn)
                query.append(", ");
            query.append(c.getName()).append(" ").append(c.getTypeName());
            firstColumn = false;
        }
        query.append(");");
        return query.toString();
    }

    @Override
    public String toSQLDrop() {
        StringBuilder query = new StringBuilder();
        query.append("DROP TABLE IF EXISTS ").append(getName()).append(" CASCADE;");
        return query.toString();
    }

    @Override
    public Column getPK() {
        return getColumns().get(0);
    }

    public void addEntries(int addedEntries) {
        entries += addedEntries;
    }
    
    @Override
    public int getEntryCount() {
        return entries;
    }
    
    public View makeView(){
        try {
            Query q = Query.fromSQL("SELECT * FROM " + name);
            View v = new View("V"+name, q, ViewType.Default);
            DatabaseBox.views.add(v);
            return v;
            
        } catch (SQLFormatException | DatabaseBoxException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.columns);
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
        final Table other = (Table) obj;
        return (Objects.equals(this.name, other.name) && Objects.equals(this.columns, other.columns));
    }
}
