/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dbtool.domain;

import java.util.List;

/**
 *
 * @author Koen
 */
public interface IPartialQuery {

    List<ITable> getFrom();

    List<Column> getSelect();

    List<Where> getWhere();
    
}
