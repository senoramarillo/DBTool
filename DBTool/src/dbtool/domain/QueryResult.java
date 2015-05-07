/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Erik
 * @param <T> Usually string
 */
public class QueryResult<T extends Comparable<T>> {

    class RowComparator<T extends Comparable> implements Comparator<List<T>> {

        @Override
        public int compare(List<T> list1, List<T> list2) {
            for (int i = 0; i < list1.size(); i++) {
                int compare = (list1.get(i)).compareTo(list2.get(i));
                if (compare != 0) {
                    return compare;
                }
            }
            return 0;
        }
    }

    public List<List<T>> result;
    public double executionTime;

    public QueryResult() {
        result = new ArrayList<>();
    }

    public void sort() {
        Collections.sort(result, new RowComparator<T>());
    }

    public void addRow(List<T> row) {
        result.add(row);
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass()))
            return false;
        
        QueryResult queryResult2 = (QueryResult) obj;
        List<List<T>> result2 = queryResult2.result;
        return resultEquality(result, result2) && setEquality(queryResult2);
        
    }

    private boolean resultEquality(List<List<T>> result1, List<List<T>> result2) {
        if (result1.size() != result2.size()) {
            return false;
        }
        for (int i = 0; i < result1.size(); ++i) {
            if (result1.get(i).size() != result2.get(i).size()) {
                return false;
            }
            for (int x = 0; x < result1.get(i).size(); ++x) {
                if (!result1.get(i).get(x).equals(result2.get(i).get(x))) {//wrong
                    return false;
                }
            }
        }
        return true;
    }

    public boolean setEquality(QueryResult queryResult2) {
        //List<List<T>> result2 = queryResult2.resultWithoutDuplicates();
       // List<List<T>> result1 = this.resultWithoutDuplicates();
        
        Set<Set<T>> r1 = new HashSet<>();
        for(List<T> r : this.result){
            r1.add(new HashSet<>(r));
        }
        Set<Set<T>> r2 = new HashSet<>();
        List<List<T>> queryResult2AsQueryResult = ((QueryResult)queryResult2).result;
        for(List<T> r : queryResult2AsQueryResult){
            r2.add(new HashSet<> (r));
        }
        
        return r1.equals(r2);
        
        
        //return resultEquality(result1, result2);
    }

    public List<List<T>> resultWithoutDuplicates() {
        List<List<T>> output = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            List<T> part = new ArrayList<>();
            if (result.get(i).size() > 0) {
                part.add(result.get(i).get(0));
                for (int x = 1; x < result.get(i).size(); x++) {
                    if (result.get(i).get(x - 1).compareTo(result.get(i).get(x)) != 0) {
                        part.add(result.get(i).get(x));
                    }
                }
                output.add(part);
            }
        }
        return output;
    }
}
