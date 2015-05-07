package dbtool.domain;

import java.util.List;

/**
 * 
 * @author Erik
 */
public interface ITable {

    String getName();
    
    String getFullName();

    List<Column> getColumns();
    
    //Created general SQLCreate String method
    String toSQLCreate();
    
    /**
     * Creates an SQL DROP statement based on this ITable's definition.
     * @return Raw SQLQuery representing the drop statement.
     */
    String toSQLDrop();

    Column getPK();
    int getEntryCount();
    
    @Override
    boolean equals(Object obj);
}
