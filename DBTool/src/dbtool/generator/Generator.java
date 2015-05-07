/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.generator;

import dbtool.Configuration;
import dbtool.algorithm.Tuple;
import dbtool.connection.ConnectionType;
import dbtool.connection.DBConnection;
import dbtool.domain.Column;
import dbtool.domain.ComparisonCondition;
import dbtool.domain.DatabaseBox;
import dbtool.domain.ITable;
import dbtool.domain.Query;
import dbtool.domain.SqlType;
import dbtool.domain.Table;
import dbtool.domain.TableRef;
import dbtool.domain.View;
import dbtool.domain.ViewType;
import dbtool.domain.Where;
import dbtool.exceptions.NoDbConnectionException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides static methods to generate database objects and populate it.
 *
 * @author Koen
 */
public class Generator {

    /**
     * The random object used for table and data generation throughout the
     * Generator.
     */
    private static Random random = new Random();

    /**
     * Sets a seed to use for generation.
     *
     * @param seed a long representing the seed
     */
    public static void setSeed(long seed) {
        random = new Random(seed);
    }

    /**
     * Generates randomized tables.
     *
     * @param noOfTables the number of tables to be generated
     * @param columnRange range in which the number of columns per table will be
     * based upon
     * @throws dbtool.exceptions.NoDbConnectionException
     * @throws java.sql.SQLException
     * @return returns the created tables.
     */
    public static List<Table> generateTables(int noOfTables, Range columnRange) throws NoDbConnectionException, SQLException {

        List<Table> tables = new ArrayList<>();

        for (int i = 0; i < noOfTables; i++) {
            //create table
            String tableName = "T" + i;
            Table A = new Table(tableName);
            DatabaseBox.tables.add(A);

            //get a random number of columns for this table that lays within the bounds
            int numberOfColumns = columnRange.getIntegerInRange(random);

            //create columns
            for (int j = 0; j < numberOfColumns; j++) {
                SqlType type = random.nextDouble() < 0.5 ? SqlType.INT : SqlType.VARCHAR;
                if (j == 0) {
                    type = SqlType.PK;
                }
                Column columnA1 = new Column("T" + i + "C" + j, 2, type);
                DatabaseBox.columns.add(columnA1);

                if (i > 0 && j > 0 && random.nextDouble() < 0.3) {
                    // Change column in a foregn key column
                    columnA1.makeFK(random.nextInt(i));
                }

                A.addColumns(columnA1);
            }

            //Drop if it exists already
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(A.toSQLDrop());

            //create query for table
            String rawCreateTableQuery = A.toSQLCreate();
            System.out.println(rawCreateTableQuery);
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(rawCreateTableQuery);

            tables.add(A);
        }
        return tables;
    }

    /**
     * Populates a given number of tables with random data.
     *
     * @param tables the table definitions used for population
     * @param rangeOfEntries the range in which the number of data entries per
     * table will be
     * @throws dbtool.exceptions.NoDbConnectionException
     * @throws java.sql.SQLException
     */
    public static void populateTables(List<Table> tables, Range rangeOfEntries) throws NoDbConnectionException, SQLException {
        final int BATCH_SIZE = Configuration.generatorBatchSize;
        for (Table t : tables) {
            //determine the random number of entries to be generated for the table
            int numberOfEntries = rangeOfEntries.getIntegerInRange(random);

            //calc number of batches
            int batches = (int) Math.ceil((double) numberOfEntries / (double) BATCH_SIZE);

            for (int i = 0; i < batches; i++) {
                //determine batch size
                int todo = numberOfEntries - (BATCH_SIZE * i);
                int nextBatchSize = Math.min(todo, BATCH_SIZE);

                //create and execute query
                String rawInsertQuery = toMultiInsertQuery(t, nextBatchSize);
                System.out.println(rawInsertQuery);
                DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(rawInsertQuery);
                t.addEntries(nextBatchSize);
            }
        }
    }

    /**
     * Creates insert query based on given table definition.
     *
     * @param t table definition
     * @return Raw SQL query
     */
    private static String toMultiInsertQuery(Table t, int numberOfEntries) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(t.getName()).append(" VALUES ");

        for (int i = 0; i < numberOfEntries; i++) {//todo randomize data size
            query.append("(");

            boolean firstColumn = true;
            for (Column c : t.getColumns()) {
                if (!firstColumn) {
                    query.append(", ");
                }
                query.append(c.getInsertValue(t, random));
                firstColumn = false;
            }
            query.append("),");
        }
        //delete last comma
        query.deleteCharAt(query.length() - 1);

        query.append(";");

        return query.toString();
    }

    public enum Flag {

        FILTER_COLUMN,
        WHERE_JOIN_LOOP,
        WHERE_VALUE_CHECK,
        WHERE_NONEQUAL;

        public static final EnumSet<Flag> ALL = EnumSet.allOf(Flag.class);
    }

    /**
     * Generates random query based on a given set of tables.
     *
     * @param tables the tables for which queries need to be generated.
     * @param numTables
     * @param flags
     *
     * @return
     */
    public static Query generateQuery(List<Table> tables, int numTables, EnumSet<Flag> flags) {
        List<Column> select = new ArrayList<>();
        List<Column> fks = new ArrayList<>();
        List<ITable> from = new ArrayList<>();
        List<Where> where = new ArrayList<>();

        HashMap<String, Integer> referenced = new HashMap<>();

        for (int i = 0; i < numTables; i++) {
            Column fk = null;
            if (fks.size() > 0 && random.nextDouble() < 0.9) {
                fk = fks.get(random.nextInt(fks.size()));
                fks.remove(fk);
            }

            ITable newTable = (fk != null) ? fk.getFKTable() : tables.get(random.nextInt(tables.size()));

            //Check if this table is already referenced in this query, if so, it will be given a suffix.
            if (referenced.containsKey(newTable.getName())) {
                continue;
                //newTable = (fk != null) ? fk.getFKTable() : tables.get(random.nextInt(tables.size()));
//                referenced.put(newTable.getName(), referenced.get(newTable.getName()) + 1);
//
//                String suffix = "";
//                for (int suffixCnt = 0; suffixCnt < referenced.get(newTable.getName()); suffixCnt++) {
//                    suffix += "_";
//                }
//                from.add(new TableRef(newTable, suffix));

            } else {
                referenced.put(newTable.getName(), 0);
                from.add(new TableRef(newTable));
            }

            // Create a where clause for a join
            if (from.size() >= 2) {
                for (int attempt = 0; attempt < 10; attempt++) {
                    Column c1, c2;
                    if (fk == null) {
                        ITable oldTable = from.get(random.nextInt(from.size() - 1));
                        c1 = newTable.getColumns().get(random.nextInt(newTable.getColumns().size()));

                        c2 = oldTable.getColumns().get(random.nextInt(oldTable.getColumns().size()));

                        if (c1.getType().sameType(c2.getType())) {
                            //Add a where condition with any random operator.
                            where.add(new Where(c1, c2, ComparisonCondition.Equal));//generateComparison(flags)));
                            break;
                        }
                    } else {
                        c1 = fk;
                        c2 = newTable.getPK();

                        where.add(new Where(c1, c2, ComparisonCondition.Equal));
                        break;
                    }
                }

            }

            for (Column c : newTable.getColumns()) {
                // Adding stuff in select
                if (random.nextDouble() < 0.4 || !flags.contains(Flag.FILTER_COLUMN)) {
                    select.add(c);
                }

                // FKs to be added
                if (c.getType() == SqlType.FK) {
                    fks.add(c);
                }
            }
        }

        // Fix for query in line 45
        if (select.isEmpty()){
            select.add(from.get(0).getPK());
        }
        
        if (flags.contains(Flag.WHERE_JOIN_LOOP)) {
            while (random.nextDouble() < 0.3) {
                for (int attempt = 0; attempt < 10; attempt++) {
                    ITable t1 = from.get(random.nextInt(from.size()));
                    ITable t2 = from.get(random.nextInt(from.size()));
                    Column c1 = t1.getColumns().get(random.nextInt(t1.getColumns().size()));
                    Column c2 = t2.getColumns().get(random.nextInt(t2.getColumns().size()));
                    if (c1.getType().sameType(c2.getType()) && !c1.equals(c2)) {
                        where.add(new Where(c1, c2, generateComparison(flags)));
                        break;
                    }
                }
            }
        }

        if (flags.contains(Flag.WHERE_VALUE_CHECK)) {
            while (random.nextDouble() < 0.5) {
                ITable t1 = from.get(random.nextInt(from.size()));
                Column c1 = t1.getColumns().get(random.nextInt(t1.getColumns().size()));
                //TODO: Generate random values better
                where.add(new Where(c1, c1.getWhereValue(t1, random), generateComparison(flags)));
            }
        }
        Query query = new Query(select, from, where);
        DatabaseBox.queries.add(query);
        return query;
    }

    static ComparisonCondition generateComparison(EnumSet<Flag> flags) {
        if (flags.contains(Flag.WHERE_NONEQUAL)) {
            return ComparisonCondition.any(random);
        } else {
            return ComparisonCondition.Equal;
        }
    }

    /**
     * Generates a collection of views for the given queries.
     *
     * @param count
     * @param viewType The type of views to be generated
     * @param flags
     * @throws NoDbConnectionException
     * @throws SQLException
     */
    public static void generateViews(int count, ViewType viewType, EnumSet<Flag> flags) throws NoDbConnectionException, SQLException {
        DatabaseBox.views.clear();

        List<Table> tableList = new ArrayList<>();
        for (ITable iTable : DatabaseBox.tables) {
            if (iTable.getClass().equals(Table.class)) {
                Table table = (Table) iTable;
                tableList.add(table);
                View vt = table.makeView();
                createInDatabase(vt);
            }
        }
        generateExtraViews(Configuration.extraViewsToGenerate);
//        // Generate views from queries
//        for (int i = 0; i < count; i++) {
//            Query q = generateQuery(tableList, 2, flags);
//            View v = generateViewForQuery(q, viewType, "VQ" + i);
//            DatabaseBox.views.add(v);
//        }
    }

    private static void generateExtraViews(int amount) {
        int counter = 0;
        for (int i = 0; i < amount; i++) {
            try {
                List<ITable> tables = getRandomTables(2);
                List<Column> select = new ArrayList<>();
                
//                Map<Column, ITable> pk = new HashMap<>();
//                Map<Column, ITable> fk = new HashMap<>();
                List<Tuple<Column>> columns = new ArrayList<>();
                for (ITable table : tables) {
                    select.addAll(table.getColumns());
                }
                for (int y = 1; y < tables.size(); y++) {
                    ITable tab1 = tables.get(y - 1);
                    ITable tab2 = tables.get(y);
                    for (Column col1 : tab1.getColumns()) {
                        for (Column col2 : tab2.getColumns()) {
                            if (col1.getType().sameType(col2.getType())) {
                                columns.add(new Tuple(col1, col2));
                            }
                        }
                    }
                }

                for (Tuple<Column> colTup : columns) {
                    List<Where> conditions = new ArrayList<>();
                    conditions.add(new Where(colTup.object1, colTup.object2, ComparisonCondition.Equal));
                    Query q = new Query(select, tables, conditions);
                    View v = new View("PKV" + counter, q, ViewType.Materialized);
                    createInDatabase(v);
                    //View v = generateViewForQuery(q, ViewType.Materialized, "PKV" + i);
                    DatabaseBox.views.add(v);
                    counter++;
                }
//                Query q = new Query(select, tables, conditions);
//                View v = new View("PKV" + i, q, ViewType.Materialized);
//                createInDatabase(v);
//                //View v = generateViewForQuery(q, ViewType.Materialized, "PKV" + i);
//                DatabaseBox.views.add(v);
            } catch (NoDbConnectionException | SQLException ex) {
                Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static List<ITable> getRandomTables(int amount) {
        List<ITable> tables = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            TableRef tab = new TableRef(DatabaseBox.tables.get(random.nextInt(DatabaseBox.tables.size())));
            while (tables.contains(tab)) {
                tab = new TableRef(DatabaseBox.tables.get(random.nextInt(DatabaseBox.tables.size())));
            }
            tables.add(tab);
        }
        return tables;
    }

    /**
     * Generates a view for a given query.
     *
     * @param query The query used for view generation
     * @param viewType The type of view used
     * @param viewName The unique name for the view (may override other view if
     * not unique).
     * @return The generated view.
     * @throws dbtool.exceptions.NoDbConnectionException
     * @throws java.sql.SQLException
     */
    public static View generateViewForQuery(Query query, ViewType viewType, String viewName) throws NoDbConnectionException, SQLException {
        View v = new View(viewName, query, viewType);
        v = deleteQuerySelect(v);
        createInDatabase(v);
        return v;
    }

    private static void createInDatabase(ITable t) throws NoDbConnectionException, SQLException {
        //Drop if it exists already
        DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(t.toSQLDrop());

        //create query for view
        String rawCreateViewQuery = t.toSQLCreate();
        System.out.println(rawCreateViewQuery);
        DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(rawCreateViewQuery);
    }

    private static View deleteQuerySelect(View v) {
        int count = (v.getColumns().size() - 1) / 2;
        while (count > 0) {
            int index = random.nextInt(v.getColumns().size() - 1);
            v.getColumns().remove(index);
            count--;
        }
        return v;
    }
}
