/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.connection;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Koen
 */
public class ConnectionSettings {
    
    private String userName = "";
    private String password = "";
    private String uri = "";

    public ConnectionSettings(ConnectionType type) {

        Properties prop = new Properties();
        try {
            //load a properties file from class path, inside static method
            prop.load(ConnectionSettings.class.getClassLoader().getResourceAsStream(type.name() + ".properties"));

            //get the property value and print it out
            this.userName = prop.getProperty("username");
            this.password = prop.getProperty("password");
            this.uri = prop.getProperty("uri");

        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

    public String getUserName(){
        return userName;
    }
    
    public String getPassword(){
        return password;
    }
    
    public String getUri() {
        return uri;
    }
}
