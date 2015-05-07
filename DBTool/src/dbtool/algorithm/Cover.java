/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.algorithm;

import dbtool.domain.Column;
import dbtool.domain.ITable;
import dbtool.domain.PartialQuery;
import dbtool.domain.Table;
import dbtool.domain.TableRef;
import dbtool.domain.Where;

/**
 * Represents a partial query that covers part of another query.
 *
 * @author Koen
 */
public class Cover extends PartialQuery {

    private final Mapping mapping;
    private boolean inverted;

    /**
     * Creates cover based on the elements in a View-Query mapping.
     *
     * @param mapping
     * @param invertCover Whether it needs to be inverted or not. See also
     * buildBody.
     */
    public Cover(Mapping mapping, boolean invertCover) {
        super();
        this.mapping = mapping;
        buildBody(invertCover);
    }

    /**
     * Creates the body of this cover based on given Mapping.
     *
     * @param inverted Whether it needs to be inverted or not.
     */
    private void buildBody(boolean inverted) {
        this.inverted = inverted;
        this.from.clear();
        this.select.clear();
        this.where.clear();

        //Add the Selects for this cover
        for (Column c : mapping.mappedQuery.getSelect()) {
            //Check whether column from query is contained by this mapping
            MapPair mp = mapping.getMapPair(c);

            //If so, use mappair determine to be selected column.
            if (mp != null) {
                Column addedSelect = inverted
                        ? mp.getViewElemental().getColumn()
                        : mp.getTableElemental().getColumn();

                select.add(addedSelect);
            }
        }

        //Add the Froms for this cover.
        for (ITable itable : mapping.mappedQuery.getFrom()) {
            Table t = (Table) ((TableRef) itable).getRef(); //magic cast 

            //Check whether from clause is mapped, if so add it to this cover.
            if (mapping.isTableMapped(t)) {
                ITable addedFrom = inverted
                        ? mapping.mappedView
                        : itable;

                from.add(addedFrom);
            }
        }

//        //Add Wheres for this cover TODO: Check if nessassery
//        for (Where where : mapping.mappedQuery.getWhere()) {
//            MapPair mp1 = Mapping.allMappingsByQueryColumn.get(where.getColumn1()).get(0);
//
//            //First column should always be mapped.
//            if (mp1 == null) {
//                throw new RuntimeException("Encountered a column that is not mapped through any view.");
//                //continue;
//            }
//
//            //Select column either from table or view.
//            Column c1 = inverted ? mp1.getViewElemental().getColumn() : mp1.getTableElemental().getColumn();
//
//            //If second value is a column, get this one from map, otherwise, use value.
//            if (where.getColumn2() != null) {
//                MapPair mp2 = Mapping.allMappingsByQueryColumn.get(where.getColumn2()).get(0);
//
//                if (mp2 == null) {
//                    //System.out.println("# Where has second column that is not in view ");
//                    continue;
//                }
//
//                Column c2 = inverted ? mp2.getViewElemental().getColumn() : mp2.getTableElemental().getColumn();
//                this.where.add(new Where(c1, c2, where.getCondition()));
//            } else {
//                this.where.add(new Where(c1, (String) where.getValue(), where.getCondition()));
//            }
//        }
    }

    /**
     * Inverts the cover.
     *
     */
    public void invert() {
        buildBody(!this.inverted);
    }

    public Mapping getMapping() {
        return mapping;
    }
}
