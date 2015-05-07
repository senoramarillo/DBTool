/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool.domain;

import java.util.Random;
import java.lang.UnsupportedOperationException ;


/**
 *
 * @author Koen
 */
public enum ComparisonCondition {
    Equal("="),
    Lesser("<"),
    LesserOrEqual("<="),
    Greater(">"),
    GreaterOrEqual(">="),
    Unequal("!=");
    //Like

    public static ComparisonCondition any(Random r) {
        return values()[r.nextInt(values().length)];
    }
    
    private ComparisonCondition(String sql){
        this.sql = sql;
    }
    
    String sql;
    
    public static String[] all(){
        String[] all = new String[ComparisonCondition.values().length];
        for(int i = 0; i < ComparisonCondition.values().length; i++){
            all[i]=ComparisonCondition.values()[i].sql;
        }
        return all;
    }
    
    /**
     * Returns the inverted (flipped) comparison condition.
     * @return 
     */
    public ComparisonCondition getInverted(){
        switch(this){
            case Equal: return Unequal;
            case LesserOrEqual: return Greater;
            case Lesser: return GreaterOrEqual;
            case GreaterOrEqual: return Lesser;
            case Greater: return LesserOrEqual;
            case Unequal: return Equal;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
