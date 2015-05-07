/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.algorithm;

import dbtool.domain.Column;
import dbtool.domain.ComparisonCondition;
import dbtool.domain.ITable;
import dbtool.domain.Query;
import dbtool.domain.Table;
import dbtool.domain.TableRef;
import dbtool.domain.View;
import dbtool.domain.Where;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Erik
 */
public class PartitionCover {

    private final List<Cover> covers;
    private final Query query;

    public PartitionCover(Query query, List<Cover> covers) {
        this.query = query;
        this.covers = covers;
    }

    /**
     * Checks whether the the given coversToCheck provide a complete cover of
     * the query or not.
     *
     * @param coversToCheck
     * @return
     */
    private boolean isFullCover(Set<Cover> coversToCheck) {
        List<Column> querySelection = query.deepCopy().getSelect();
        List<ITable> queryFrom = query.deepCopy().getFrom();
        for (Cover cover : coversToCheck) {
            for (Column column : cover.getSelect()) {
                querySelection.remove(column);
            }
            for (ITable tab : cover.getMapping().mappedTables) {
                queryFrom.remove(new TableRef(tab));
            }
        }
        //System.out.println(queryFrom.size());

        return querySelection.isEmpty() && queryFrom.isEmpty();
    }

    /**
     * Builds query based on this partitions covers.
     *
     * @return
     */
    public Query constructQuery() {
        List<Column> coverSelect = new ArrayList<>();
        HashSet<ITable> coverFrom = new HashSet<>();
        HashSet<Where> coverWhere = new HashSet<>();
        List<Cover> usedCovers = findCovers();

        for (Cover c : usedCovers) {
            //coverSelect.addAll(c.getSelect());
            coverFrom.addAll(c.getFrom());
            //coverWhere.addAll(c.getWhere());
        }
        for (Column col : query.getSelect()) {
            coverSelect.add(getBest(Mapping.allMappingsByQueryColumn.get(col), coverSelect, coverFrom).getViewElemental().getColumn());
        }
        List<Where> queryWheres = new ArrayList<>(query.deepCopy().getWhere());
        //queryWheres.addAll(query.getWhere());

        for (Where queryWhere : queryWheres) {

            boolean isJoin = false;
            for (ITable f : coverFrom) {
                if (((View) f).getQuery().getWhere().contains(queryWhere)) {
                    System.out.println("IS JOIN");
                    isJoin = true;
                    break;
                }
            }
            if (isJoin) {
                continue;
            }

            Object c1 = null, c2 = null;

            List<MapPair> mappedToC1 = Mapping.allMappingsByQueryColumn.get(queryWhere.getColumn1());

            MapPair mp1 = getBest(mappedToC1, coverSelect, coverFrom);

            c1 = mp1.getViewElemental().getColumn();
            MapPair mp2 = null;
            if (queryWhere.getColumn2() != null) {
                List<MapPair> mappedToC2 = Mapping.allMappingsByQueryColumn.get(queryWhere.getColumn2());

                mp2 = getBest(mappedToC2, coverSelect, coverFrom);

                c2 = mp2.getViewElemental().getColumn();
            } else {
                c2 = queryWhere.getValue();
            }
            coverWhere.add(queryWhere);
////            if (c1 != null && c2 != null) {
////                if (!coverFrom.contains(mp1.getViewElemental().getParent())) {
////                    coverFrom.add(mp1.getViewElemental().getParent());
////                }
////                if (mp2 != null && !coverFrom.contains(mp2.getViewElemental().getParent())) {
////                    coverFrom.add(mp2.getViewElemental().getParent());
////
////                }
////
////                if (c2.getClass().equals(Column.class)) {
////                    Where toAdd = new Where((Column) c1, (Column) c2, queryWhere.getCondition());
////
////                    coverWhere.add(toAdd);
////
////                } else {
////                    coverWhere.add(new Where((Column) c1, (String) c2, queryWhere.getCondition()));
////                }
////            } else {
////                //Query where is not covered by the view so leave it.
////                //System.out.println("Oh noes, the column of this query's where does not appear all the mappings cover :(");
////            }
        }
        coverWhere.addAll(getNaturalJoins(coverFrom));
        return new Query(coverSelect, new ArrayList<>(coverFrom), new ArrayList<>(coverWhere));
    }

    /**
     * Creates a list of joins on PK's between a set of views
     */
    HashSet<Where> getNaturalJoins(HashSet<ITable> views) {
        HashSet<Where> joinConditions = new HashSet<>();
        for (ITable table1 : views) {
            View v1 = (View) table1;
            List<ITable> v1Tables = v1.getQuery().getFrom();

            for (ITable table2 : views) {
                if (table1 != table2) {
                    View v2 = (View) table2;
                    List<ITable> v2Tables = v2.getQuery().getFrom();
                    Map<Tuple<View>, ITable> duplicates = findDuplicateTables(v1Tables, v1, v2Tables, v2);
                    for (Tuple<View> dup : duplicates.keySet()) {
                        List<Column> posJoinCol1 = dup.object1.getQuery().getColumnsForJoinOfITable(duplicates.get(dup));
                        List<Column> posJoinCol2 = dup.object2.getQuery().getColumnsForJoinOfITable(duplicates.get(dup));
                        List<Column> joinCols = findDuplicates(posJoinCol1, posJoinCol2);
                        Column joinCol = joinCols.get(0);
                        joinConditions.add(new Where(v1, v2, joinCol));
                    }
                    //duplicateTables.addAll();
                }
            }
        }
//////
//////        for (ITable t1 : views) {
//////            View v1 = (View) t1;
//////            for (ITable t2 : views) {
//////                View v2 = (View) t2;
//////                if (v1 == v2) {
//////                    continue;
//////                }
//////
//////                for (ITable v1t : v1.getQuery().getFrom()) {
//////                    for (ITable v2t : v2.getQuery().getFrom()) {
//////                        if (v1t.getName().equals(v2t.getName())) {
//////                            // Add join condition.
//////                            joinConditions.add(new Where(v1t.getPK(), v2t.getPK(), ComparisonCondition.Equal));
//////                            // BUG: v1t.getPK() and v2t.getPK() have no references to v1 and v2
//////                            // Desired: Something like VPK1.T1C0 = VPK2.T1C0
//////                            // Outcome: Something like VPK1.T1C0 = VPK1.T1C0
//////                            // Therefore the condition is useless
//////                        }
//////                    }
//////                }
//////            }
//////        }
        return joinConditions;
    }

    private static Map<Tuple<View>, ITable> findDuplicateTables(List<ITable> l1, View v1, List<ITable> l2, View v2) {
        Map<Tuple<View>, ITable> returnList = new HashMap<>();
        for (ITable tab1 : l1) {
            if (l2.contains(tab1)) {
                returnList.put(new Tuple<View>(v1, v2), tab1);
            }
        }
        return returnList;
    }

    private static List<Column> findDuplicates(List<Column> l1, List<Column> l2) {
        List<Column> returnList = new ArrayList<>();
        for (Column col1 : l1) {
            if (l2.contains(col1)) {
                returnList.add(col1);
            }
        }
        return returnList;
    }

    /**
     * find the covers that must be used for the rewritten query this should be
     * done by Knuths dancing links but this is my own implementation
     *
     * @return The covers that must be used
     */
    private List<Cover> findCovers() {
        Set<Set<Cover>> possibleCovers = new HashSet<>();
        for (int i = 1; i <= Math.min(covers.size(), 4); i++) {
            possibleCovers.addAll(getCoverCombinations(covers, i));
        }

        List<Set<Cover>> fullCovers = new ArrayList<>();

        //Determine full covers
        for (Set<Cover> candidate : possibleCovers) {
            if (isFullCover(candidate)) {
                fullCovers.add(candidate);
            }
        }
        return optimalFullCover(fullCovers);
    }

    /**
     * Determines all combinations of the cover. Warning: This function returns
     * an exponential amount of covers.
     *
     * @return
     */
    private static Set<Set<Cover>> getCoverCombinations(List<Cover> groupSize, int k) {
        Set<Set<Cover>> allCombos = new HashSet<>();
        // base cases for recursion
        if (k == 0) {
            // There is only one combination of size 0, the empty team.
            allCombos.add(new HashSet<Cover>());
            return allCombos;
        }
        if (k > groupSize.size()) {
            // There can be no teams with size larger than the group size,
            // so return allCombos without putting any teams in it.
            return allCombos;
        }

        // Create a copy of the group with one item removed.
        List<Cover> groupWithoutX = new ArrayList<>(groupSize);
        Cover x = groupWithoutX.remove(groupWithoutX.size() - 1);

        Set<Set<Cover>> combosWithoutX = getCoverCombinations(groupWithoutX, k);
        Set<Set<Cover>> combosWithX = getCoverCombinations(groupWithoutX, k - 1);
        for (Set<Cover> combo : combosWithX) {
            combo.add(x);
        }
        allCombos.addAll(combosWithoutX);
        allCombos.addAll(combosWithX);
        return allCombos;
    }

    private List<Cover> optimalFullCover(List<Set<Cover>> fullCovers) {
        //Select the best full cover
        if (fullCovers.size() > 0) {
            Map<Integer, ArrayList<Set<Cover>>> sizeMap = new HashMap<>();
            for (Set<Cover> fullCover : fullCovers) {
                if (sizeMap.containsKey(fullCover.size())) {
                    sizeMap.get(fullCover.size()).add(fullCover);
                } else {
                    sizeMap.put(fullCover.size(), new ArrayList<>(Arrays.asList(fullCover)));
                }
            }
            int smallest = Integer.MAX_VALUE;
            for (Integer x : sizeMap.keySet()) {
                if (x < smallest) {
                    smallest = x;
                }
            }
            List<Set<Cover>> remainers = sizeMap.get(smallest);
            int maxSelect = 0;
            Set<Cover> winner = null;
            for (Set<Cover> coverSet : remainers) {
                int selects = 0;
                for (Cover cover : coverSet) {
                    selects += cover.getMapping().mappedView.getQuery().getSelect().size();
                }
                if (selects > maxSelect) {
                    maxSelect = selects;
                    winner = coverSet;
                }
            }
            return new ArrayList<>(winner);
        } else {
            throw new NullPointerException("No possible covers found, check if there are views?");
        }
    }

    /**
     * Determines the best MapPair for this cover.
     *
     * @param mappedTo list of MapPairs that correspond to the to be mapped
     * column
     * @param coverSelect
     * @param coverFrom
     * @return
     */
    private MapPair getBest(List<MapPair> mappedTo, List<Column> coverSelect, Set<ITable> coverFrom) {

        //if only one in maplist, use that one
        if (mappedTo.size() == 1) {
            return mappedTo.get(0);
        } else if (mappedTo.size() < 1) {
            throw new RuntimeException("MapPair.getBest < 1");
        }

        for (MapPair mp : mappedTo) {
            //Check whether there is already a view in the from that contains this from's column
            for (ITable view : coverFrom) {
                if (mp.getViewElemental().getParent().equals(view)) {
                    return mp;
                }
            }
        }

        //Not already in this query, select the best view:
        {
            // VQ Views are probably more efficient, instead of randomly selecting one
            // it is better to delay this choise until we have found ALL the wheres
            // for which this choise has te be made, then selection based on this combination can be made.
            System.out.println("\u001B[33m>>> Should select best view, for now using a Table View instead (since VQ crashes anyway) >>>");
        }

        return mappedTo.get(0);

    }
}
