package dbtool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dbtool.algorithm.QueryRewriter;
import dbtool.connection.ConnectionType;
import dbtool.connection.DBConnection;
import dbtool.domain.Query;
import dbtool.domain.QueryResult;
import dbtool.domain.Table;
import dbtool.exceptions.NoDbConnectionException;
import dbtool.generator.Generator;

public class Tests {

    private List<Query> toTest;
    private List<Result> results;
    private final List<ConnectionType> connectionTypes;

    /**
     * How many times every query should be repeated to get average time.
     */
    private final int repeatNumber = Configuration.performanceTestsAvgRepeat;

    /**
     * Generates random queries, based on given tables.
     *
     * @param tables
     * @param connectionTypes
     * @param sampleSize How many samples shoudl be generated.
     * @param emptyQueries true if we want to test queries that returns zero
     * results
     */
    public Tests(List<Table> tables, List<ConnectionType> connectionTypes, int sampleSize, boolean emptyQueries) {
        this.connectionTypes = connectionTypes;
        toTest = new ArrayList<>();

        for (int i = 0; i < sampleSize; ++i) {
            Query query = Generator.generateQuery(tables, 3, Generator.Flag.ALL);
            if (emptyQueries) {
                toTest.add(query);
            } else {
                // test only on first DBMS
                try {
                    QueryResult result = DBConnection.getInstance(connectionTypes.get(0)).executeQuery(query);
                    if (!result.result.isEmpty()) {
                        toTest.add(query);
                    }
                } catch (SQLException | NoDbConnectionException e) {
                    throw new Error(e.getMessage());
                }
            }
        }
    }

    public void run() throws NoDbConnectionException {
        results = new ArrayList<>();
        int done = 0;
        int fails = 0;
        for (ConnectionType connectionType : connectionTypes) {
            for (Query tested : toTest) {

                try {
                    String original = tested.toSQL();
                    System.out.println("QUERY: " + original.replace("\n", " "));

                    // Deepcopy does not belong in the running time
                    Query rewritten = tested.deepCopy();
                    long timeStart = System.nanoTime();
                    rewritten = QueryRewriter.rewrite(rewritten);
                    double rewritingTime = (System.nanoTime() - timeStart) / 1000000000.0;

                    double originalTime = -1.0;
                    double rewrittenTime = -1.0;
                    System.out.println("REWRITING: " + rewritten.toSQL().replaceAll("\n", " "));

                    Tuple<Double, QueryResult> originalTimeAndResult = multipleExecutesAvgTime(tested, connectionType, repeatNumber);
                    originalTime = originalTimeAndResult.object1;

                    Tuple<Double, QueryResult> rewrittenTimeAndResult = multipleExecutesAvgTime(rewritten, connectionType, repeatNumber);
                    rewrittenTime = rewrittenTimeAndResult.object1;

                    boolean equalResult = originalTimeAndResult.object2.equals(rewrittenTimeAndResult.object2);
                    if (!equalResult) {

                        System.out.println("\u001B[31mRESULT: FAAAAAAAAAAAAAAAAAAIL:");
                        fails++;
                    } else {
                        System.out.println("\u001B[32mRESULT: SUCCESS!! :D:D:D:D:D:D:D:D:D:DD:D:D:D:D:D:D:D:D");

                        // TODO write to results with different conditions
                        // do we want more information about queries?
                        results.add(new Result(rewritingTime, originalTime, rewrittenTime, connectionType, original, rewritten));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                done++;
                System.out.println("[" + done + "/" + toTest.size() + "]");
            }
            System.out.println("Fails [" + fails + "/" + toTest.size() + "]");
        }
    }

    class Tuple<U, V> {

        public U object1;
        public V object2;

        public Tuple(U object1, V object2) {
            this.object1 = object1;
            this.object2 = object2;
        }
    }

    private Tuple<Double, QueryResult> multipleExecutesAvgTime(Query tested, ConnectionType connectionType, int amount) throws SQLException, NoDbConnectionException {
        double avg = 0.0;
        QueryResult result = null;
        for (int i = 0; i < amount; ++i) {
            result = DBConnection.getInstance(connectionType).executeQuery(tested);
            avg += result.executionTime;
        }
        return new Tuple<>((Double) (avg / ((double) amount)), result);
    }

    /**
     * Saves results to CSV file.
     *
     * @return
     */
    public String resultsToFile() {
        char delim = ',';
        File outputDirecotry = new File("results");
        outputDirecotry.mkdir();

        DateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
        Date date = new Date();
        String resultsFileName = dateFormat.format(date) + ".csv";

        try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            File f = new File(System.getProperty("user.dir") + File.separator + outputDirecotry.getName() + File.separator + resultsFileName);
            if (!f.exists()) {
                f.createNewFile();
            }
            try (FileWriter writer = new FileWriter(f)) {
                writer.append("used database" + delim
                        + "original time" + delim
                        + "rewritten time" + delim
                        + "rewriting time" + delim
                        + "rewritten + rewriting time" + delim
                        + "selects" + delim
                        + "froms" + delim
                        + "wheres" + delim
                        + "original" + delim
                        + "rewritten" + delim + " \n");
                for (Result rslt : results) {
                    // TODO we want to save more info about query?
                    writer.append(rslt.toCSVString(delim));
                    writer.append("\n");
                }

                writer.flush();
            }
        } catch (IOException e) {
            throw new Error(e.getMessage());
        }

        return outputDirecotry.getName() + File.separator + resultsFileName;
    }

    public class Result {

        public String originalQuery;
        public Query rewrittenQuery;
        public double rewritingTime;
        public double originalQueryTime;
        public double rewrittenQueryTime;
        public ConnectionType usedDatabase;

        public Result(double rewritingTime, double originalQueryTime, double rewrittenQueryTime, ConnectionType usedDatabase, String originalQuery, Query rewrittenQuery) {
            super();
            this.originalQuery = originalQuery.replace(',', ';').replace('\n', ' ');
            this.rewrittenQuery = rewrittenQuery;
            this.rewritingTime = rewritingTime;
            this.originalQueryTime = originalQueryTime;
            this.rewrittenQueryTime = rewrittenQueryTime;
            this.usedDatabase = usedDatabase;
        }

        public String toCSVString(char delim) {
            return usedDatabase.toString() + delim
                    + originalQueryTime + delim
                    + rewrittenQueryTime + delim
                    + rewritingTime + delim
                    + String.valueOf(rewrittenQueryTime + rewritingTime) + delim
                    + rewrittenQuery.getSelect().size() + delim
                    + rewrittenQuery.getFrom().size() + delim
                    + rewrittenQuery.getWhere().size() + delim
                    + originalQuery + delim
                    + rewrittenQuery.toSQL().replace(',', ';').replace('\n', ' ');
        }
    }

}
