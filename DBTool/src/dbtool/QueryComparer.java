/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool;

import static java.lang.System.exit;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import dbtool.algorithm.QueryRewriter;
import dbtool.connection.ConnectionType;
import dbtool.connection.DBConnection;
import dbtool.domain.Query;
import dbtool.domain.QueryResult;
import dbtool.exceptions.NoDbConnectionException;

/**
 *
 * @author wokste
 */
public class QueryComparer {
    public double time1;
    public double time2;

    public boolean bEqualHeuristic;
    public boolean bEqualRewriting;
    public int numSQLResults;
    
    public QueryComparer(Query query1, Query query2) throws NoDbConnectionException{
        heuristicEquivilanceTest(query1, query2);
        rewritingEquivilanceTest(query1, query2);
    }
    
    public boolean statisticallySignificant(){
        return numSQLResults > Configuration.numSQLResults;
    }
    
    private void heuristicEquivilanceTest(Query query1, Query query2) throws NoDbConnectionException{
        try {
            QueryResult result1 = DBConnection.getInstance(ConnectionType.Postgres).executeQuery(query1);
            QueryResult result2 = DBConnection.getInstance(ConnectionType.Postgres).executeQuery(query2);
            if (result1.equals(result2))
                bEqualHeuristic = true;
            numSQLResults = result1.result.size();
            time1 = result1.executionTime;
            time2 = result2.executionTime;
        } catch (SQLException ex) {
            throw new Error(ex.getMessage());
        }
    }
    
    private void rewritingEquivilanceTest(Query query1, Query query2){
        Query query1rewritten = QueryRewriter.rewrite(query1);
        Query query2rewritten = QueryRewriter.rewrite(query2);
        
        query1rewritten.sortFields();
        query2rewritten.sortFields();
        
        if (query1rewritten.toSQL().equals(query2rewritten.toSQL()))
            bEqualRewriting = true;
        
        bEqualRewriting = false;
    }

    public void describeResults() {
        if (bEqualHeuristic){
            if (!statisticallySignificant())
                System.out.println("Heuristic Test: Equal but not enough rows ( " + Integer.toString(numSQLResults) + " rows )");
            else
                System.out.println("Heuristic Test: Equal ( " + Integer.toString(numSQLResults) + " rows )");
        } else {
            System.out.println("Heuristic Test: Different");        
        }
        System.out.println("                query 1 took " + Double.toString(time1) + " s"); 
        System.out.println("                query 2 took " + Double.toString(time2) + " s"); 
        
        if (bEqualRewriting){
            System.out.println("Rewriting Test: Equal");
        } else {
            System.out.println("Rewriting Test: Different");
            System.out.println("WARNING: Equaltiy rewriting isn't completely implemented.");
            System.out.println("         It is sound but not complete.");
        }
        
        if (!bEqualHeuristic && bEqualRewriting){
            System.out.println("ERROR: Heuristic method fails and rewriting method succeeds");
            System.out.println("       Either there is a bug in the code or the rewriting isn't sound.");
            throw new Error("");
        }
        
        if (bEqualHeuristic && !bEqualRewriting && statisticallySignificant()){
            System.out.println("ERROR: Heuristic method succeeds and rewriting method fails");
            System.out.println("       Likely the heiristic method is too optimistic.");
            exit(0);
        }
    }
}
