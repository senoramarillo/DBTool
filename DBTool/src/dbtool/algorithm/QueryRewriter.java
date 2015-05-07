/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.algorithm;

import dbtool.domain.DatabaseBox;
import dbtool.domain.Query;
import dbtool.domain.View;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides static method to rewrite queries.
 *
 * @author Koen
 */
public class QueryRewriter {

    /**
     * Rewrites a given query to one using views.
     *
     * @param query The to be rewritten query.
     * @return Equal query that uses views instead of tables.
     */
    public static Query rewrite(Query query) {

        List<Cover> covers = new ArrayList<>();

        // for each view v do:
        for (View v : DatabaseBox.views) {
            PartitionView viewPartition = new PartitionView(v);

            // for each theta s.t. v --> p map:
            Mapping mapping = viewPartition.getMapping(query);

            //Continue with next view if this view does not provide (partial) coverage.
            if (mapping == null) {
                continue;
            }
            
            for(Mapping invertableMapping : mapping.getInvertableMappings()){
                // Find inverses of theta for the v^i
                Cover invertedCover = new Cover(invertableMapping, true);

                // Record these covers for v and theta.
                covers.add(invertedCover);
            }
        }

        // Seach the covers for a partition of p.
        PartitionCover partition = new PartitionCover(query, covers);

        // Return rewritten query
        return partition.constructQuery();
    }
}
