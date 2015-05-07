/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool.exceptions;

/**
 *
 * @author wokste
 */
public class SQLFormatException extends Exception {

    public SQLFormatException(String error) {
       super(error);
    }
    
}

