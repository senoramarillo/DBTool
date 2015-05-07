package dbtool.domain;

import java.util.Objects;
import java.util.Random;

/**
 * 
 * @author Erik
 */
public class Column {
    private final String name;
    private final int length;
    private SqlType type;
    private int fkTableId;

    public Column(String name, int length, SqlType type) {
        this.name = name;
        this.length = length;
        this.type=type;
    }

    public void makeFK(int tableID) {
        type = SqlType.FK;
        fkTableId = tableID;
    }
    
    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public SqlType getType() {
        return this.type;
    }
    
    
    public String getWhereValue(ITable table, Random random){
        if (type == SqlType.PK){
            return Integer.toString(random.nextInt(table.getEntryCount()) + 1);
        }
        return getInsertValue(table, random);
    }

    public String getInsertValue(ITable table, Random random) {
        if (type == SqlType.FK){
            ITable otherTable = getFKTable();
            return otherTable.getPK().getWhereValue(otherTable, random);
        }
        
        return type.getInsertValue(random);
    }

    String getTypeName() {
        if (type == SqlType.FK){
            ITable otherTable = getFKTable();
            return type.getName() + " REFERENCES " + otherTable.getFullName() + "(" + otherTable.getPK().getName() + ")";
        }
        return type.getName();   
    }

    public ITable getFKTable() {
        if (type == SqlType.FK){
            return DatabaseBox.tables.get(fkTableId);
        }
        throw new UnsupportedOperationException("getFKTable may only be called on FK columns");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + this.length;
        hash = 41 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Column other = (Column) obj;
        
        return
            (Objects.equals(this.name, other.name) &&
            (this.length == other.length) &&
            (this.type == other.type));
    }
}
