/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool.algorithm;

/**
 *
 * @author Erik
 * A Tuple contains 2 values of the same type
 * @param <U> The type of the elements in the tuple
 */
public class Tuple<U> {
    
    public U object1;
    public U object2;
    
    /**
     * Construct a new Tuple of type U with two objects
     * @param obj1 the first object
     * @param obj2 the second object
     */
    public Tuple(U obj1, U obj2){
        object1 = obj1;
        object2 = obj2;
    }
}
