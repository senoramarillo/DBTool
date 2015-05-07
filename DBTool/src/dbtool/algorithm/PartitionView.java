/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.algorithm;

import dbtool.domain.Column;
import dbtool.domain.ITable;
import dbtool.domain.Query;
import dbtool.domain.Table;
import dbtool.domain.TableRef;
import dbtool.domain.View;
import dbtool.domain.Where;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Definition 3.3: (view partition) A view v is partitioned by forming the
 * symmetric transitive closure of the relation that relates two existential
 * variables if they both occur in any atom, and then partitioning v according
 * to the resulting equivalence classes. Atoms with no existential variables are
 * singletons in the partition.
 *
 * @author Koen
 */
public class PartitionView {

    private final View view;

    public PartitionView(View v) {
        this.view = v;
    }

    /**
     * Gets mappings for the view based on the given query.
     *
     * @param p
     * @return
     */
    public Mapping getMapping(Query p) {

        Set<MapPair> mappings = determineViewContainment(view, p);

        if (mappings != null && mappings.size() > 0) {
            return new Mapping(p, view, mappings);
        } else {
            return null;
        }
    }

    /**
     * ViewContainment checks if the query is contained within the view. First
     * by checking if the where clauses are not opposites of each other. Second
     * by mapping all the tables of the view with a boolean value to which is
     * used in the query. And as last for all the true tables make a list of
     * columns that the query needs.
     *
     * @param view The query of the view
     * @param query The to be rewritten query
     * @return it returns a mapping from ITables that are usable for the query
     * mapped to usable columns. Null if not contained.
     */
    private static Set<MapPair> determineViewContainment(View view, Query query) {
        // Check whether where is contained, if not return
        //if (!isWhereContained(view, query)) { //where does not need to be contained (as discussed
        //    return null; 
        //}
        List<ITable> viewTables = view.getQuery().deepCopy().getFrom();
        for (ITable table : query.getFrom()) {
            viewTables.remove(table);
        }
        if (!viewTables.isEmpty()) {
            return null;
        }
        for (Where where : view.getQuery().getWhere()) {
            Boolean contained = false;
            for (Where where2 : query.getWhere()) {
                if (whereCheck(where, where2)) {
                    contained = true;
                }
            }
            if (!contained) {
                return null;
            }
        }
        Set<MapPair> mappings = new HashSet<>();

        //Put all the ITables and Columns of the where clause of the query in a map to check containment
        Map<ITable, List<Column>> whereColumns = new HashMap<>();
        //loop trough all the wheres
        for (Where where : query.getWhere()) {
            //get the ITable of the where column
            ITable queryTable = query.determineITableOfColumn(where.getColumn1());
            if (!whereColumns.containsKey(queryTable)) {
                whereColumns.put(queryTable, new ArrayList<Column>());
            }
            whereColumns.get(queryTable).add(where.getColumn1());
            if (where.getColumn2() != null) {
                ITable queryTable2 = query.determineITableOfColumn(where.getColumn2());
                if (!whereColumns.containsKey(queryTable2)) {
                    whereColumns.put(queryTable2, new ArrayList<Column>());
                }
                whereColumns.get(queryTable2).add(where.getColumn2());
            }
        }

        //If the Tables in the FROM clause of the view 
        for (ITable table : view.getQuery().getFrom()) {
            if (query.getFrom().contains(table)) {

                List<Column> queryWhereColumns = whereColumns.get(table);
                //Maps affected columns
                List<Column> affectedColumns = new ArrayList<>();
                for (Column c : view.getQuery().getSelect()) {
                    if (table.getColumns().contains(c)) {
                        affectedColumns.add(c);
                    }
                    if (queryWhereColumns.contains(c)) {
                        MapElement<View> viewElement = new MapElement<>(view, c);
                        Table actualTable = null;
                        if (table.getClass().equals(TableRef.class)) {
                            actualTable = (Table) ((TableRef) table).getRef();
                        } else {
                            System.out.println("############shouldnt get here");
                        }
                        MapElement<Table> queryElement = new MapElement<>(actualTable, c);
                        mappings.add(new MapPair(viewElement, queryElement));
                    }
                }

                for (Column c : view.getQuery().getWhereColumns()) {
                    if (queryWhereColumns.contains(c)) {
                        MapElement<View> viewElement = new MapElement<>(view, c);
                        Table actualTable = null;
                        if (table.getClass().equals(TableRef.class)) {
                            actualTable = (Table) ((TableRef) table).getRef();
                        } else {
                            System.out.println("############shouldnt get here");
                        }
                        MapElement<Table> queryElement = new MapElement<>(actualTable, c);
                        mappings.add(new MapPair(viewElement, queryElement));
                    }
                }
                
                
                //Maps column to something called 'yeah'.
                for (Column c : query.getSelect()) {
                    if (affectedColumns.contains(c)) {

                        MapElement<View> viewElement = new MapElement<>(view, c);
                        Table actualTable = null;
                        if (table.getClass().equals(TableRef.class)) {
                            actualTable = (Table) ((TableRef) table).getRef();
                        } else {
                            System.out.println("############shouldnt get here");
                        }
                        MapElement<Table> queryElement = new MapElement<>(actualTable, c);

                        mappings.add(new MapPair(viewElement, queryElement));
                    }
                }
            }
        }

        return mappings;
    }

    private static Boolean whereCheck(Where where1, Where where2) {
        if (where1.equals(where2)) {
            return true;
        } else {
            if (where1.getColumn1().equals(where2.getColumn1())) {
                if (where1.getColumn2().equals(where2.getColumn2())) {
                    if (where1.getCondition().equals(where2.getCondition())) {
                        return true;
                    }
                }
            }
            if (where1.getColumn1().equals(where2.getColumn2())) {
                if (where1.getColumn2().equals(where2.getColumn1())) {
                    if (where1.getCondition().equals(where2.getCondition())) {
                        return true;
                    }
                }
            }
            if (where1.getColumn1().equals(where2.getColumn1())) {
                if (where1.getColumn2() == null) {
                    if (where2.getColumn2() == null) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
