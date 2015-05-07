/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool;

import dbtool.algorithm.Mapping;
import dbtool.algorithm.QueryRewriter;
import dbtool.connection.ConnectionType;
import dbtool.connection.DBConnection;
import dbtool.connection.Util;
import dbtool.domain.Column;
import dbtool.domain.DatabaseBox;
import dbtool.domain.Query;
import dbtool.domain.QueryResult;
import dbtool.domain.SqlType;
import dbtool.domain.Table;
import dbtool.domain.View;
import dbtool.domain.ViewType;
import dbtool.exceptions.DatabaseBoxException;
import dbtool.exceptions.NoDbConnectionException;
import dbtool.exceptions.SQLFormatException;
import dbtool.generator.Generator;
import dbtool.generator.Range;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.exit;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wokste
 */
public class Menu {

    List<Table> tables = new ArrayList<>();
    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

    public void mainMenu() throws NoDbConnectionException {
        //Displays the main menu and handles passing off to next menu. 
        int selection = 0;
        Configuration.loadConfiguration("config.properties");

        fillDB(Configuration.seed);

        while (true) {
            System.out.println("Please Make a selection:");
            System.out.println("[1] Execute Random Queries");
            System.out.println("[2] Execute Custom Queries");
            System.out.println("[3] Execute Tests");
            System.out.println("[4] Algorithm Test");
            System.out.println("[5] Exit");
            System.out.println("[6] Paper test Example");
            System.out.println("Selection: ");

            try {
                String s = bufferRead.readLine();
                selection = Integer.parseInt(s);
            } catch (IOException | NumberFormatException ex) {
                Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            }

            switch (selection) {
                case 1:
                    execRandomQueries();
                    break;
                case 2:
                    try {
                        execCustomQueries();
                    } catch (NoDbConnectionException | DatabaseBoxException ex) {
                        Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case 3:
                    performTests();
                    break;
                case 4:
                    algorithmTest();
                    break;
                case 5:
                    return;
                case 6:
                    paperExample();
                    break;
                default:
                    System.out.println("Please enter a valid selection.");
            }
        }
    }

    private void fillDB(long seed) throws NoDbConnectionException {
        try {
            DatabaseBox.init();
            Generator.setSeed(seed);
            Range columnRange = new Range(Configuration.minColumnRange, Configuration.maxColumnRange); // maybe as (optional) parameter for menu
            tables = Generator.generateTables(Configuration.numberOfTablesToGenerate, columnRange);
            Range dataRange = new Range(Configuration.minDataRange, Configuration.maxDataRange); //maybe as (optional) parameter for menu
            Generator.populateTables(tables, dataRange);

            Generator.generateViews(30, ViewType.Materialized, Generator.Flag.ALL);
        } catch (SQLException ex) {
            throw new Error(ex.getMessage());
        }
    }

    private void execRandomQueries() throws NoDbConnectionException {
        for (int i = 0; i < Configuration.numberOfRandomQueriesToExecute; i++) {
            try {
                Query query = Generator.generateQuery(tables, 3, Generator.Flag.ALL);

                System.out.println(query.toSQL());
                QueryResult qResult = DBConnection.getInstance(ConnectionType.Postgres).executeQuery(query);
                System.out.println("No of matches to tested query: " + qResult.result.size());
            } catch (SQLException ex) {
                System.out.println("Failed to execute query");
                System.out.println(ex.getMessage());
            }
        }
    }

    private void execCustomQueries() throws NoDbConnectionException, DatabaseBoxException {
        System.out.println("This functionality compares two queries.");
        System.out.println("Please insert first query:");
        Query query1 = readQuery();
        System.out.println("Please insert second query:");
        Query query2 = readQuery();

        QueryComparer qc = new QueryComparer(query1, query2);
        qc.describeResults();
    }

    private Query readQuery() throws DatabaseBoxException {
        while (true) {
            try {
                String sql = bufferRead.readLine();
                sql = sql.toUpperCase();
                Query query = Query.fromSQL(sql);
                return query;
            } catch (IOException ex) {
                throw new Error(ex.getMessage());
            } catch (SQLFormatException ex) {
                System.out.println("Could not execute query.");
                System.out.println(ex.getMessage());
                System.out.println("Please type in query again.");
            }
        }
    }

    private void performTests() {
        System.out.println("Tests in progres...");

        // how many query samples should be generated
        int sampleSize = Configuration.performanceTestsSampleSize;

        // To use more DBMS, extend this list.
        List<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(ConnectionType.Postgres);

        Tests tests = new Tests(tables, connectionTypes, sampleSize, true);
        try {
            tests.run();
        } catch (NoDbConnectionException e) {
            throw new Error(e.getMessage());
        }
        System.out.println("Tests done. Results saved in file: " + tests.resultsToFile());
    }

    private void algorithmTest() {
        try {
            Query q1 = Generator.generateQuery(tables, 3, Generator.Flag.ALL);
            Query q2 = Generator.generateQuery(tables, 3, Generator.Flag.ALL);
            View view1 = Generator.generateViewForQuery(q1, ViewType.Materialized, "testDefault");
            View view2 = Generator.generateViewForQuery(q2, ViewType.Materialized, "testMaterialized");

            DatabaseBox.views.add(view1);
            DatabaseBox.views.add(view2);

            Query p1 = QueryRewriter.rewrite(q1);
        } catch (NoDbConnectionException | SQLException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void paperExample() {
        List<View> oldViews = DatabaseBox.views;
        DatabaseBox.views = new ArrayList<>();
        Mapping.allMappings.clear();
        Mapping.allMappingsByQueryColumn.clear();
        Mapping.allQueryColumnsByViewColumns.clear();
        try {
            DatabaseBox.views.clear();
            //        "v1(A;C) :- a(A; B); b(B;C)"
//       "v2(B; D) :- b(B;C); c(C; D)"
//        "v3(B;C; D) :- b(B;C); c(C; D)"

            Table A = new Table("A");
            A.addColumns(new Column("AC0", 10, SqlType.PK));
            A.addColumns(new Column("AC1", 10, SqlType.INT));
            DatabaseBox.tables.add(A);

            Table B = new Table("B");
            B.addColumns(new Column("BC0", 10, SqlType.PK));
            B.addColumns(new Column("BC1", 10, SqlType.INT));
            DatabaseBox.tables.add(B);

            Table C = new Table("C");
            C.addColumns(new Column("CC0", 10, SqlType.PK));
            C.addColumns(new Column("CC1", 10, SqlType.INT));
            DatabaseBox.tables.add(C);

            //Drop if it exists already
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(A.toSQLDrop());
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(B.toSQLDrop());
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(C.toSQLDrop());

            //create query for table
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(A.toSQLCreate());
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(B.toSQLCreate());
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(C.toSQLCreate());

            View V1 = new View("v1", Query.fromSQL("SELECT A.AC0 AS AC0, B.BC1 AS BC1 FROM A, B WHERE A.AC1 = B.BC0"), ViewType.Materialized);
            DatabaseBox.views.add(V1);
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(V1.toSQLDrop());
            System.out.println(V1.toSQLCreate());
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(V1.toSQLCreate());

            View V2 = new View("v2", Query.fromSQL("SELECT B.BC0 AS BC0, C.CC1 AS CC1 FROM B, C WHERE B.BC1 = C.CC0"), ViewType.Materialized);
            DatabaseBox.views.add(V2);
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(V2.toSQLDrop());
            System.out.println(V2.toSQLCreate());
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(V2.toSQLCreate());
            
            View V3 = new View("v3", Query.fromSQL("SELECT B.BC0 AS BC0, B.BC1 AS BC1, C.CC1 AS CC1 FROM B, C WHERE B.BC1 = C.CC0"), ViewType.Materialized);
            DatabaseBox.views.add(V3);
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(V3.toSQLDrop());
            System.out.println(V3.toSQLCreate());
            DBConnection.getInstance(ConnectionType.Postgres).executeRawQuery(V3.toSQLCreate());

            //"q(X;W) :- a(X; Y); b(Y; Z); c(Z;W):"
            Query q = Query.fromSQL("SELECT A.AC0, C.CC1 FROM A, B, C WHERE A.AC1 = B.BC0 AND B.BC1 = C.CC0");
            Query q1 = QueryRewriter.rewrite(q);

            System.out.println(q1.toSQL());
            String sql = q1.toSQL();
            for (int i = 0; i < 10; i++) {
                if (!QueryRewriter.rewrite(q).toSQL().equals(sql)) {
                    System.out.println("oops, NOT REALLY");
                }
            }

            //q0(X;W) :- v1(X; Z); v3(Y; Z;W)
            QueryResult qResult1 = DBConnection.getInstance(ConnectionType.Postgres).executeQuery(q);
            QueryResult qResult2 = DBConnection.getInstance(ConnectionType.Postgres).executeQuery(q1);
            if (qResult1.equals(qResult2)) {
                System.out.println("YES WE DID IT!");
            }
//            String expectedResult = "SELECT v1.v1C0 AS C0, v3.v3C1 AS C1 FROM v1, v3 WHERE v1.v1C1 = v3.v3C1";
//            String q1SQL = q1.toSQL();
//            if(q1SQL.equals(expectedResult)){
//                
//            }
        } catch (SQLFormatException | DatabaseBoxException | NoDbConnectionException | SQLException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
        DatabaseBox.views = oldViews;
    }
}
