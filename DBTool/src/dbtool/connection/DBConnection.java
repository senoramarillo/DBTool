/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.connection;

import dbtool.domain.Query;
import dbtool.domain.QueryResult;
import dbtool.exceptions.NoDbConnectionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Erik
 */
public class DBConnection {

    private static Map<ConnectionType, DBConnection> instances;

    public static DBConnection getInstance(ConnectionType type) throws NoDbConnectionException {
        if (instances == null) {
            instances = new HashMap<>();
        }
        if (!instances.containsKey(type)) {
            instances.put(type, new DBConnection(type));
        }
        return instances.get(type);
    }

    private Connection dbConnection;
    
    private DBConnection(ConnectionType type) throws NoDbConnectionException {
        ConnectionSettings conf = new ConnectionSettings(type);
        try {
            System.out.println("Start database connection...");
            dbConnection = DriverManager.getConnection(conf.getUri(), conf.getUserName(), conf.getPassword());
        } catch (SQLException ex) {
            Logger lgr  = Logger.getLogger(Util.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new NoDbConnectionException();
        }
    }

    public QueryResult executeQuery(Query q) throws SQLException {
        Statement statement = dbConnection.createStatement();
        
        long startTime = System.nanoTime();
        ResultSet result = statement.executeQuery(q.toSQL());
        long endTime = System.nanoTime();
        
        int amountOfColumns = q.getSelect().size();
        QueryResult<String> qResult = new QueryResult();
        while (result.next()) {
            List<String> row = new ArrayList<>();
            for (int i = 1; i < amountOfColumns + 1; i++) {
                row.add(result.getString(i));
            }
            qResult.addRow(row);
        }
        qResult.sort();
        
        qResult.executionTime = (endTime - startTime) / 1000000000.0;
        return qResult;
    }

    /**
     * Executes a raw (string) SQL query without a result set.
     * @param query The to be executed query
     * @throws SQLException 
     */
    public void executeRawQuery(String query) throws SQLException {
        Statement statement = dbConnection.createStatement();
        statement.execute(query);
    }
}
