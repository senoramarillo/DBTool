/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool.exceptions;

/**
 *
 * @author Koen
 */
public class NoDbConnectionException extends Exception {

    public NoDbConnectionException() {
       super("No database connection.");
    }
    
}
