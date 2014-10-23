/*******************************************************************************
 * Copyright 2008, 2009, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.semti.morphology.lexicon.TableModels;

import javax.swing.table.AbstractTableModel;

import lv.semti.morphology.attributes.*;

public class AttributeModel extends AbstractTableModel {
	/**
	 * Eclipsei par prieku.
	 */
	private static final long serialVersionUID = 1L;
	
//FIXME - būtu jauki, ka tad, ja ir tukša tabula, tad rādītu ""/"" pāri editēšanai.
	private  AttributeValues attributeValues = null;
	private String[] columnNames = {"Īpašība","Vērtība"};

	public AttributeModel(AttributeValues kotadeditējam) {
		attributeValues = kotadeditējam;
	}

    @Override
	public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getColumnCount() { return columnNames.length; }
    public int getRowCount() {
    	if (attributeValues == null) return 0;

    	return attributeValues.size();
    }

    public Object getValueAt(int row, int col) {
    	if (attributeValues == null) return null;
    	if (row < 0 || row >= attributeValues.size()) return null;

    	switch (col) {
    	case 0 : return attributeValues.get(row).getKey();
    	case 1 : return attributeValues.get(row).getValue();
    	default: return null;
    	}
    }
    @Override
	public boolean isCellEditable(int row, int col)
	{
    	return (row >= 0);
	}

    @Override
	public void setValueAt(Object value, int row, int col) {
    	if (attributeValues == null) return;
    	String newValue = "";
    	if (value != null) newValue = value.toString();
    	
    	switch (col) {
    	case 0 :
    		String vērtība = attributeValues.get(row).getValue();
    		attributeValues.removeAttribute(attributeValues.get(row).getKey());
    		attributeValues.addAttribute(newValue, vērtība);
    		fireTableDataChanged();
    		break;
    	case 1 :
    		attributeValues.get(row).setValue(newValue);
    		fireTableCellUpdated(row, col);
    		break;
    	}
    }

    public void removeRow(int row) {
    	if (attributeValues == null) return;
    	attributeValues.removeAttribute(attributeValues.get(row).getKey());
    	fireTableRowsDeleted(row,row);
    }

    public void addRow() {
    	if (attributeValues == null) return;
    	attributeValues.addAttribute("", "");
    	fireTableDataChanged();
    }

	public void setAttributes(AttributeValues attributes) {
		this.attributeValues = attributes;
		fireTableDataChanged();
	}
	
	public String getAttribute(int row) {
	   	if (attributeValues == null) return null;
    	if (row < 0 || row >= attributeValues.size()) return null;

    	return attributeValues.get(row).getKey();		
	}
}