/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool.domain;

import java.util.Random;

/**
 *
 * @author wokste
 */
public enum SqlType {
    INT("INT"),
    VARCHAR("VARCHAR"),
    
    PK("SERIAL PRIMARY KEY"),
    FK("INT");
    
    private SqlType(String name){
        this.name = name;
    }
    
    private final String name;
    
    public String getInsertValue(Random r){
        switch(this){
            case PK:
                return "DEFAULT";
            case INT:
                return Integer.toString((int)Math.ceil(r.nextDouble() * 16));
            case VARCHAR:
                return "'"+getRandomString(r)+"'";
            default:
                throw new RuntimeException("Failed to add random value for type " + name);
        }
    }

    public String getName() {
        return name;
    }
    
    /**
     * Get random string 
     * @param r the random object
     * @return a random string
     */
    private static String getRandomString(Random r) {
        // Based on the 100 most common words in english
        // Source: http://www.oxforddictionaries.com/words/the-oec-facts-about-the-language
        String[] mostCommonNouns = {"time","person","year","way","day","thing","man","world","life","hand","part","child","eye","woman","place","work","week","case","point","government","company","number","group","problem","fact"};
        return mostCommonNouns[r.nextInt(mostCommonNouns.length)];
    }

    public boolean sameType(SqlType that) {
        return this.equals(that) 
                || (this == INT && that == FK)
                || (this == FK && that == INT);
    }
}
