package dbtool.domain;

import dbtool.exceptions.DatabaseBoxException;
import dbtool.exceptions.SQLFormatException;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Erik
 */
public class Query extends PartialQuery {

    public Query(List<Column> select, List<ITable> from, List<Where> where) {
        super(select, from, where);
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().getName().equals(Query.class.getName())) {
            return false;
        }

        Query p = (Query) obj;
        if (p.select.size() == this.select.size()) {
            if (!equalsList(select, p.select)) {
                return false;
            }
        }
        if (p.from.size() == this.from.size()) {
            if (!equalsList(this.from, p.from)) {
                return false;
            }
        }
        if (p.where.size() == this.where.size()) {
            if (!equalsList(this.where, p.where)) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsList(List l1, List l2) {
        for (int i = 0; i < l1.size(); i++) {
            if (!l2.contains(l1.get(i))) {
                return false;
            }
        }
        return true;
    }

    private String fullColumnName(Column column, ITable table) {
        if (table != null) {
            return table.getName() + "." + column.getName();
        } else {
            for (ITable f : this.from) {
                for (Column c : f.getColumns()) {
                    if (column.equals(c)) {
                        return f.getName() + "." + column.getName();
                    }
                }
            }
        }

        //A method to get view name if it is not in from (should be in from though)
        //MapPair mp = Mapping.allMappingsByQueryColumn.get(column);
        //String name =  mp.getViewElemental().getParent().getName() + "."+mp.getViewElemental().getColumn().getName();
        return "ERRORS_HERE";
    }

    //check if the select statement covers all the columns of all the tables
    private boolean checkSelectAll() {
        int size = 0;
        for (ITable from1 : from) {
            size += from1.getColumns().size();
        }
        return size == select.size();
    }

    public String toSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (checkSelectAll()) {
            sb.append("*");
        } else {
            for (int i = 0; i < select.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(fullColumnName(select.get(i), null));
                //sb.append(" AS C").append(Integer.toString(i));
            }
        }
        sb.append("\n");
        sb.append("FROM ");
        for (int i = 0; i < from.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(from.get(i).getFullName());
        }
        sb.append("\n");
        for (int i = 0; i < where.size(); i++) {
            if (i == 0) {
                sb.append("WHERE ");
            } else {
                sb.append(" AND ");
            }

            String val;
            if (where.get(i).getColumn2() != null) {
                val = fullColumnName(where.get(i).getColumn2(), where.get(i).Table2);
            } else {
                val = where.get(i).getValue() + "";
            }

            sb.append(fullColumnName(where.get(i).getColumn1(), where.get(i).Table1))
                    .append(" ")
                    .append(where.get(i).getCondition().sql)
                    .append(" ")
                    .append(val);
        }

        return sb.toString();
    }

    public static Query fromSQL(String sql) throws SQLFormatException, DatabaseBoxException {
        //The select part
//        sql = sql.toUpperCase();
        if (!sql.startsWith("SELECT") || !sql.contains("FROM")) {
            throw new SQLFormatException("Query needs to start with SELECT and have a FROM clause");
        }

        //The from part needs to be evaluated first
        String fromPart;
        if (sql.contains("WHERE")) {
            fromPart = sql.substring(sql.indexOf("FROM") + 4, sql.indexOf("WHERE"));
        } else if (sql.contains("WHERE")) {
            fromPart = sql.substring(sql.indexOf("FROM") + 4, sql.indexOf("ORDER"));
        } else {
            fromPart = sql.substring(sql.indexOf("FROM") + 4, sql.length());
        }
        String[] fromArray = fromPart.split(",");
        List<ITable> from = new ArrayList<>();
        for (String fromString : fromArray) {
            fromString = fromString.trim();
            String name = null;
            if (fromString.contains(" ")) {
                String temp = fromString.substring(0, fromString.indexOf(" "));
                name = fromString.substring(fromString.indexOf(" ") + 1);
                fromString = temp;
            }
            ITable table = DatabaseBox.getObjectFromString(ITable.class, fromString.trim());
            if (table == null) {
                throw new SQLFormatException("no table named " + fromString.trim() + "");
            }
            //If the table is renamed
            TableRef ref = (name == null) ? new TableRef(table) : new TableRef(name, table);

            from.add(ref);
            if (name != null) {
                // TODO: Can anyone explain me why this happens in this way?
                DatabaseBox.tables.add(ref);
            }
        }

        // The select part
        String selectPart = sql.substring(sql.indexOf("SELECT") + 6, sql.indexOf("FROM"));
        String[] selectArray = selectPart.split(",");
        List<Column> select = new ArrayList<>();
        if (selectArray.length == 1 && selectArray[0].contains("*")) {
            // Select everything
            for (int i = 0; i < from.size(); i++) {
                select.addAll(from.get(i).getColumns());
            }
        } else {
            for (String selectString : selectArray) {
                String columnName = selectString.trim();
                if (columnName.contains(" AS ")) {
                    columnName = columnName.substring(0, columnName.indexOf(" AS "));
                }

                Column column = DatabaseBox.getColumnFromTables(from, columnName);
                if (column == null) {
                    throw new SQLFormatException("no column named " + columnName + "");
                }
                select.add(column);
            }
        }

        //The where part
        List<Where> where = new ArrayList<>();
        if (sql.contains("WHERE")) {
            String wherePart;
            if (sql.contains("ORDER")) {
                wherePart = sql.substring(sql.indexOf("WHERE") + 5, sql.indexOf("ORDER"));
            } else {
                wherePart = sql.substring(sql.indexOf("WHERE") + 5);
            }
            String[] whereArray = wherePart.split(" AND ");
            for (String whereString : whereArray) {
                Where whereCondition = parseWhere(from, whereString);
                where.add(whereCondition);
            }
        }

        Query query = new Query(select, from, where);
        DatabaseBox.queries.add(query);
        return query;
    }

    /**
     * Parses the where condition from a given where string. It uses the
     * different comparison conditions to determine the right condition.
     */
    private static Where parseWhere(List<ITable> from, String whereString) throws SQLFormatException {
        ComparisonCondition condition = null;
        for (ComparisonCondition checkedCondition : ComparisonCondition.values()) {
            if (whereString.contains(checkedCondition.sql)) { // we first check by the longer operators (first >= instead of >) to make sure we get the right match.
                condition = checkedCondition;
            }
        }

        if (condition == null) {
            throw new SQLFormatException("No comparison condition found in where clause '" + whereString + "'");
        }

        String[] whereSplit = whereString.split(condition.sql);
        if (whereSplit.length != 2) {
            throw new SQLFormatException("Where condition " + whereString.trim() + " needs to contain exactly one =");
        }
        Column column1 = DatabaseBox.getColumnFromTables(from, whereSplit[0].trim());
        Column column2 = DatabaseBox.getColumnFromTables(from, whereSplit[1].trim());
        Where whereCondition;
        if (column2 != null) {
            if (whereSplit.length != 2) {
                throw new SQLFormatException("Where condition " + whereString.trim() + " needs to be of the same type");
            }
            whereCondition = new Where(column1, column2, condition);
        } else {
            whereCondition = new Where(column1, whereSplit[1].trim(), condition);
        }
        return whereCondition;
    }

    public Query deepCopy() {
        try {
            return fromSQL(this.toSQL());
        } catch (SQLFormatException | DatabaseBoxException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void sortFields() {
        java.util.Comparator comparator = new java.util.Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                String s_1 = o1.toString();
                String s_2 = o2.toString();

                return s_1.compareTo(s_2);
            }
        };

        java.util.Collections.sort(from, comparator);
        java.util.Collections.sort(where, comparator);
    }

    public ITable determineITableOfColumn(Column column) {
        for (ITable table : from) {
            if (table.getColumns().contains(column)) {
                return table;
            }
        }
        return null;
    }

    public List<Column> getWhereColumns() {
        List<Column> cols = new ArrayList<>();
        for (Where w : where) {
            cols.add(w.getColumn1());
            if (w.getColumn2() != null) {
                cols.add(w.getColumn2());
            }
        }
        return cols;
    }

    public List<Column> getColumnsForJoinOfITable(ITable table) {
        if (from.contains(table)) {
            List<Column> allSelects = this.deepCopy().getSelect();
            for (Column c : table.getColumns()) {
                if (!allSelects.contains(c)) {
                    allSelects.remove(c);
                }
            }
            if (!allSelects.isEmpty()) {
                return allSelects;
            }
        }
        throw new NullPointerException("From clause of query does not contain: " + table.getFullName());
    }
}
