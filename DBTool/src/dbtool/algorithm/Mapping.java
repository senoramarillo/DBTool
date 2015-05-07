/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.algorithm;

import dbtool.domain.Column;
import dbtool.domain.Query;
import dbtool.domain.Table;
import dbtool.domain.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a View-Query mapping and provides methods to use this info.
 *
 * @author Koen
 */
public class Mapping {

    protected final Set<MapPair> mappedPairs;
    protected final Query mappedQuery;
    protected final View mappedView;

    public final HashSet<Table> mappedTables;
    private final HashMap<Column, MapPair> mappedQueryColumns;

    public static Set<MapPair> allMappings;
    public static Map<Column, List<MapPair>> allMappingsByQueryColumn;
    
    public static Map<Column, Column> allQueryColumnsByViewColumns;//key:view column, value:query oclumn
    public static Map<Column, MapPair> allMapPairsByViewColumns;//key:view column, value:query oclumn

    public static void put(MapPair mp) {
        if (allMappings == null) {
            allMappings = new HashSet<>();
        }

        if (allMappingsByQueryColumn == null) {
            allMappingsByQueryColumn = new HashMap<>();
        }
        if(allQueryColumnsByViewColumns==null){
            allQueryColumnsByViewColumns= new HashMap<>();
        }if(allMapPairsByViewColumns==null){
            allMapPairsByViewColumns = new HashMap<>();
        }

        allMappings.add(mp);

        if (allMappingsByQueryColumn.containsKey(mp.getTableElemental().getColumn())) {
            if (allMappingsByQueryColumn.get(mp.getTableElemental().getColumn()).contains(mp)) {
                System.out.println("DUPLICATE " + mp.getViewElemental().getParent().getName());
            } else {
                allMappingsByQueryColumn.get(mp.getTableElemental().getColumn()).add(mp);
            }

        } else {
            allMappingsByQueryColumn.put(mp.getTableElemental().getColumn(), new ArrayList<MapPair>());
            allMappingsByQueryColumn.get(mp.getTableElemental().getColumn()).add(mp);
        }
        
        allMapPairsByViewColumns.put(mp.getTableElemental().getColumn(),mp);
        allQueryColumnsByViewColumns.put(mp.getViewElemental().getColumn(), mp.getTableElemental().getColumn());
    }

    public Mapping(Query p, View v, Set<MapPair> mappings) {
        this.mappedPairs = mappings;
        this.mappedQuery = p;
        this.mappedView = v;

        //collect info about the mapped tables and mapped columns
        mappedTables = new HashSet<>();
        mappedQueryColumns = new HashMap<>();

        for (MapPair mp : mappedPairs) {
            if (!mappedTables.contains(mp.getTableElemental().getParent())) {
                mappedTables.add(mp.getTableElemental().getParent());
            }

            if (!mappedQueryColumns.containsKey(mp.getTableElemental().getColumn())) {
                mappedQueryColumns.put(mp.getTableElemental().getColumn(), mp);
            } else {
                throw new Error("mappedQueryColumns already contains column " + mp.getTableElemental().getColumn().getName());
            } //This event throws error, but why should it?
        }
    }

    /**
     * From a given mapping, this function returns a set of mappings. Each
     * mapping returned is safely invertable.
     */
    public List<Mapping> getInvertableMappings() {
        List<MapElement<Table>> duplicateColumns = getDuplicateTableColumns();
        if (duplicateColumns.isEmpty()) {
            List<Mapping> mappingList = new ArrayList<>();
            mappingList.add(this);
            return mappingList;
        }// It never gets past this if, condluded from tests

        // Copy mappedPairs
        Set<MapPair> safeToAdd = new HashSet<>(mappedPairs);
        List<Set<MapPair>> pairsForMappings = new ArrayList<>();

        for (MapElement<Table> duplicateColumn : duplicateColumns) {
            List<MapPair> AddOnlyOne = getPairsToTable(duplicateColumn);
            safeToAdd.removeAll(AddOnlyOne);
        }

        pairsForMappings.add(safeToAdd);

        for (MapElement<Table> duplicateColumn : duplicateColumns) {
            pairsForMappings = addAnyPairToAll(pairsForMappings, getPairsToTable(duplicateColumn));
        }

        //TODO: In this part of the code the mappings needs to be created.
        //Each mapping must have all mapPairs in safetoAdd
        return makeMappings(mappedQuery, mappedView, pairsForMappings);
    }

    private List<MapPair> getPairsToTable(MapElement<Table> to) {
        List<MapPair> list = new ArrayList<>();
        for (MapPair mp : mappedPairs) {
            MapElement<Table> col = mp.getTableElemental();

            if (col.equals(to)) {
                list.add(mp);
                //break; should not break since then it will never find all
            }
        }
        return list;
    }

    private List<Set<MapPair>> addAnyPairToAll(List<Set<MapPair>> pairsForMappings, List<MapPair> addOnlyOne) {
        List<Set<MapPair>> newPairs = new ArrayList();
        for (Set<MapPair> pairsForMapping : pairsForMappings) {
            for (MapPair chosenOne : addOnlyOne) {
                Set<MapPair> copy = new HashSet<>(pairsForMapping);
                copy.add(chosenOne);
                newPairs.add(copy);
            }
        }
        return newPairs;
    }

    private List<MapElement<Table>> getDuplicateTableColumns() {
        List<MapElement<Table>> list = new ArrayList<>();
        for (MapPair mp1 : mappedPairs) {
            MapElement<Table> col = mp1.getTableElemental();

            if (list.contains(col)) {
                continue;
            }

            if (getPairsToTable(col).size() > 1) {
                list.add(col);
            }
        }

        if (list.size() > 1) {
            throw new RuntimeException("IT DOESNT EVEN GET HERE, EVER");
        }

        return list;
    }

    public static List<Mapping> makeMappings(Query p, View v, List<Set<MapPair>> elements) {
        List<Mapping> mappings = new ArrayList<>();
        for (Set<MapPair> elem : elements) {
            mappings.add(new Mapping(p, v, elem));
        }
        return mappings;
    }

    public MapPair getMapPair(Column queryColumn) {
        return mappedQueryColumns.get(queryColumn);
    }

    boolean isTableMapped(Table t) {
        return mappedTables.contains(t);
    }

}
