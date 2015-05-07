/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.domain;

import dbtool.exceptions.DatabaseBoxException;
import dbtool.exceptions.SQLFormatException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Erik
 */
public class DatabaseBox {

    public static List<Query> queries;
    public static List<View> views;
    public static List<ITable> tables;
    public static List<Column> columns;

    public static void init() {
        queries = new ArrayList<>();
        views = new ArrayList<>();
        tables = new ArrayList<>();
        columns = new ArrayList<>();
    }

    public static <T> T getObjectFromString(Class<T> type, String value) throws DatabaseBoxException {
        if (queries == null || views == null || tables == null) {
            throw new DatabaseBoxException("Database not initialized!");
        }
        value = value.toUpperCase();
        String className = type.getName();
        if (className.equals(Column.class.getName())) {
            int point = value.indexOf(".");
            String[] values = new String[]{value.substring(0, point), value.substring(point + 1)};
            for (ITable table : tables) {
                if (table.getName().toUpperCase().equals(values[0])) {
                    for (Column c : table.getColumns()) {
                        if (c.getName().toUpperCase().equals(values[1])) {
                            return (T) c;
                        }
                    }
                }
            }
        } else if (className.equals(Table.class.getName())) {
            for (ITable table : tables) {
                if (table.getName().toUpperCase().equals(value)) {
                    return (T) table;
                }
            }
        } else if (className.equals(View.class.getName())) {
            for (ITable view : views) {
                if (view.getName().toUpperCase().equals(value)) {
                    return (T) view;
                }
            }
        } else if (className.equals(ITable.class.getName())) {
            for (ITable table : tables) {
                if (table.getName().toUpperCase().equals(value)) {
                    return (T) table;
                }
            }
            for (ITable view : views) {
                if (view.getName().toUpperCase().equals(value)) {
                    return (T) view;
                }
            }
        }
        return null;
    }

    public static Column getColumnFromTables(List<ITable> custTables, String value) throws SQLFormatException {
        Column returnValue = null;

        String tableName, columnName;

        int point = value.indexOf(".");
        if (point < 0) {
            // There is no '.'
            tableName = "";
            columnName = value;
        } else {
            // There is a dot
            tableName = value.substring(0, point).toUpperCase();
            columnName = value.substring(point + 1).toUpperCase();
        }

        for (ITable table : custTables) {
            if (table.getName().toUpperCase().equals(tableName) || "".equals(tableName)) {
                for (Column c : table.getColumns()) {
                    if (c.getName().toUpperCase().equals(columnName)) {
                        if (returnValue != null) {
                            throw new SQLFormatException("Ambiguity in column name " + value);
                        }

                        returnValue = c;
                    }
                }
            }
        }
        return returnValue;
    }
}
