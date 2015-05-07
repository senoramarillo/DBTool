/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool.connection;


import dbtool.exceptions.NoDbConnectionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

    public static String getPostgresVersion() throws NoDbConnectionException{
        Connection con = null; 
        Statement st = null;//TODO: move all connection stuff to generatic structure
        ResultSet rs = null;
        
        ConnectionSettings conf = new ConnectionSettings(ConnectionType.Postgres);
        
        String version = "";
            
        try {
            System.out.println("Testing database connection...");

            con = DriverManager.getConnection(conf.getUri(), conf.getUserName(), conf.getPassword());
            st = con.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            
            
            if (rs.next()) {
                version = rs.getString(1);
                System.out.println(version);
            }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Util.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

            throw new NoDbConnectionException();
            
        } finally {
            try {//TODO: move all connection stuff to generatic structure
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(Util.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        
        return version;
    }
}

