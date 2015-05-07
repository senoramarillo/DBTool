package dbtool.domain;

import java.util.Objects;

/**
 *
 * @author Erik
 */
public class Where {

    private final Column column;
    private final Object value;

    private final ComparisonCondition condition;

    public Where(Column column1, String value, ComparisonCondition condition) {
        this.column = column1;
        this.value = value;
        this.condition = condition;
        
        if (this.column == null || this.value == null || this.condition == null)
            throw new NullPointerException();
    }

    public Where(Column column1, Column column2, ComparisonCondition condition) {
        this.column = column1;
        this.value = column2;
        this.condition = condition;
        
        if (this.column == null || this.value == null || this.condition == null || this.column.getName()==null)
            throw new NullPointerException();
    }
    
    public ITable Table1;
    public ITable Table2;
    /**
     * This constructor should only be used for natural join of views with the same columns
     * @param table1
     * @param table2 
     * @param column 
     */
    public Where(ITable table1, ITable table2, Column column){
        this.Table1 = table1;
        this.Table2 = table2;
        this.column = column;
        this.value = column;
        this.condition = ComparisonCondition.Equal;
        
        if (this.column == null || this.value == null || this.condition == null || this.column.getName()==null){
            throw new NullPointerException();
        }
        if(Table1 == Table2){
            throw new IllegalArgumentException("Table1 and Table2 can't be the same");
        }
    }

    public Column getColumn1() {
        return column;
    }

    public Column getColumn2() {
        if (value.getClass().getName().equals(Column.class.getName())) {
            return (Column) value;
        }
        return null;
    }

    public Object getValue() {
        return value;
    }

    public ComparisonCondition getCondition() {
        return condition;
    }

    /**
     * Checks whether the where condition is "contained" by the other.
     * Containment includes equivalence of both values, and the comparator
     * condition. Note: This function is not complete. It is sound.
     *
     * @param condition2
     * @return
     */
    public boolean isContained(Where condition2) {
        // First check if they have the same comparator.
        boolean sameComparator = isWhereComparatorEquivalent(condition2);

        //If so check both columns:
        if (sameComparator) {
            ///First check the if the columns are the same
            if (isWherePrimaryColumnEquivalent(condition2)) {
                //Either check on secundary column or value based on wether second column is zero
                if (this.getColumn2() != null) {
                    return isWhereSecondColumnEquivalent(condition2);
                } else {
                    ///if the second value is not a column
                    return isWhereValueEquivalent(condition2);
                }
            }
        } else {
            //If not, check the inverse comparator: 
            if (isWhereComparatorInverseEquivalent(condition2)) {
                //and the values are both columns and both equal?
                return (this.getColumn1() != null && this.getColumn2() != null
                        && isWherePrimaryColumnEquivalent(condition2)
                        && isWhereSecondColumnEquivalent(condition2));
            }
        }
        return false;
    }

    private boolean isWherePrimaryColumnEquivalent(Where condition2) {
        return this.getColumn1().equals(condition2.getColumn1());
    }

    /**
     * Checks whether given Where object has equal secondary column.
     */
    private boolean isWhereSecondColumnEquivalent(Where condition2) {
        Column column2 = getColumn2();
        ///if the second value is column
        Column c2column2 = condition2.getColumn2();
        if (condition2.getColumn2() != null) {
            ///if the other condition second value also is a column
            return column2.equals(c2column2);
        }
        return false;
    }

    /**
     * Checks whether given Where object has equal comparator condition.
     */
    private boolean isWhereComparatorEquivalent(Where condition2) {
        return this.condition.equals(condition2.condition);
    }

    /**
     * Checks whether the given Where object's comparator condition is
     * equivalent to the inverse of this one.
     */
    private boolean isWhereComparatorInverseEquivalent(Where condition2) {
        return this.condition.equals(condition2.getCondition().getInverted());
    }

    /**
     * Checks whether Where object has equal value.
     */
    private boolean isWhereValueEquivalent(Where condition2) {
        return this.value.equals(condition2.value); // this should cover both integer and string values right?
    }
    
        /**
     * Checks whether a collection of Where objects from the query are contained
     * by the view.
     *
     * @param view
     * @param query
     * @return
     */
    public static boolean isWhereContained(View view, Query query) {
        for (Where viewWheres : view.getQuery().getWhere()) {
            boolean containedPart = false;
            for (Where queryWheres : query.getWhere()) {
                if (viewWheres.isContained(queryWheres)) {
                    containedPart = true;
                    break;
                }
            }
            if (!containedPart) {
                return containedPart;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.column);
        hash = 61 * hash + Objects.hashCode(this.value);
        hash = 61 * hash + Objects.hashCode(this.condition);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Where other = (Where) obj;
        if (!Objects.equals(this.column, other.column)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!this.condition.equals(other.condition)) {
            return false;
        }
        return true;
    }

        
    
    
}
