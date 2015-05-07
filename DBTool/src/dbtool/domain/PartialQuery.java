/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class PartialQuery implements IPartialQuery {

    protected final List<Where> where;
    protected final List<Column> select;
    protected final List<ITable> from;

    public PartialQuery(List<Column> select, List<ITable> from, List<Where> where) {
        this.select = select;
        this.from = from;
        this.where = where;
    }

    public PartialQuery() {
        this.select = new ArrayList<>();
        this.where = new ArrayList<>();
        this.from = new ArrayList<>();
    }

    @Override
    public List<Where> getWhere() {
        return where;
    }

    @Override
    public List<Column> getSelect() {
        return select;
    }

    @Override
    public List<ITable> getFrom() {
        return from;
    }

}
