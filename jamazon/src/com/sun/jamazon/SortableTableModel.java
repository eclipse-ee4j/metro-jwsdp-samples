/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.jamazon;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TableModelEvent;

import javax.swing.table.AbstractTableModel;

/**
 * A table model which support sorting the rows by its columns. 
 * The <code>SortableTableHeader</code> should be
 * used in order to realize the usablity of column sorting in the table, .
 * <p>
 * The data contained in this table has two constraints:
 * <ol>
 *   <li>The collection of rows must be represented as a <code>java.util.List</code>.
 *   <li>Each cell value must be an instance of <code>java.util.Comparable</code>.
 * </ol>
 * The default implementation can handle rows which are instances of Lists and Arrays
 * as long as the column index can retrieve the correct cell value in the row element. 
 * the <code>getValueForColumn<code> method should be implemented for rows which are
 * represented as a class or use non-sequential indexes.
 * <p>
 * See the description for the superclass for guidance on which methods should be
 * overridden to create a functioning table model.
 *
 * @see SortableTableHeader
 * @see java.util.List
 * @see java.lang.Comparable
 */
public abstract class SortableTableModel extends AbstractTableModel {
    
    private TableModelComparator comparator = new TableModelComparator();

    /**
     * All the rows are stored as a List. This should be set in the constructor.
     */
    private List elements;
    
    /**
     * Used by the SortableTableHeader.SortHeaderMouseAdapter
     */
    void sortByColumn(int column, boolean ascending) {
	comparator.addColumn(column);
	comparator.setAscending(ascending);

	if (elements != null) {
	    Collections.sort(elements, comparator);
	    fireTableChanged(new TableModelEvent(this));
	}
    }

    /**
     * Set the row elements of the model. This method must be called before
     * sorting can occur.
     * 
     * @param elements the collection of rows represented as List elements
     */
    protected void setRowElements(List elements) {
	this.elements = elements;
    }

    boolean isAscending() {
	return comparator.isAscending();
    }

    /**
     * Returns the last column that was sorted.
     * Required for the SortHeaderCellRenderer
     */
    int getColumn() {
	return comparator.getColumn();
    }

    /**
     * Returns the cell value for a row indexed by the column. The default
     * implementation returns the column values for the row if it's a List or
     * an Array. 
     * <p>
     * This method should be overridden if there is any specific 
     * data conversion or cast that should occur. For example, if each row
     * element is represented as a class then the proper getter method should
     * be invoked to return the object that corresponds to a column index.
     *
     * @param obj an object which represents a row.
     * @param column the column to retrieve
     */
    public Object getValueForColumn(Object obj, int column) {
	if (obj instanceof List) {
	    return ((List)obj).get(column);
	} else if (obj.getClass().isArray()) {
	    return ((Object[])obj)[column];
	}
	return null;
    }

    /**
     * A comparator which compares rows in a table model
     */
    private class TableModelComparator implements Comparator {
	
	private boolean ascending;
	private int[] columns;

	/**
	 * Add the column to the sort criteria
	 */
	public void addColumn(int column) {
	    if (columns == null) {
		// XXX - Should actually listen for column changes and resize
		columns = new int[getColumnCount()];
		Arrays.fill(columns, -1);
	    }

	    // Shift columns in the array
	    int[] tempArray = new int[getColumnCount()];
	    System.arraycopy(columns, 0, tempArray, 1, columns.length - 1);
	    tempArray[0] = column;

	    columns = tempArray;
	}
    
	/**
	 * Get the last column that was sorted
	 * @return index of the last sorted column or -1 if it has never been sorted
	 */
	public int getColumn() {
	    if (columns == null) {
		return -1;
	    }
	    return columns[0];
	}
	
	public void setAscending(boolean ascending) {
	    this.ascending = ascending;
	}
	
	public boolean isAscending() {
	    return ascending;
	}
	
	/**
	 * Implementation of the comparator method. A comparison is
	 * made for rows.
	 */
	public int compare(Object row1, Object row2) {
	    if (columns == null) {
		return 0;
	    }
	    
	    for (int i = 0; i < columns.length && columns[i] != -1; i++) {
		
		Object o1 = getValueForColumn(row1, columns[i]);
		Object o2 = getValueForColumn(row2, columns[i]); 

		// If both values are null, return 0.
		if (o1 == null && o2 == null) {
		    return 0; 
		} else if (o1 == null) { // Define null less than everything. 
		    return -1; 
		} else if (o2 == null) { 
		    return 1; 
		}

		int result = 0;

		if (o1 instanceof Comparable) {
		    Comparable c1 = (Comparable)o1;
		    Comparable c2 = (Comparable)o2;
		    
		    result = c1.compareTo(c2);
		}

		if (result != 0) {
		    return ascending ? result : -result;
		}
	    }
	    return 0;
	}
    } // end TableModelComparator
}

    
