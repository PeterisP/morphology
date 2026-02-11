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

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import lv.semti.morphology.lexicon.*;

public class LexemeModel extends AbstractTableModel {
	/**
	 * Eclipsei par prieku.
	 */
	private static final long serialVersionUID = 1L;
	
	Paradigm vārdgrupa = null;

	public void setVārdgrupa(Paradigm vārdgrupa) {
		this.vārdgrupa = vārdgrupa;
		fireTableStructureChanged();
	}

    @Override
	public String getColumnName(int col) {
    	String[] columnNames1 = {"Nr","Sakne"};
    	String[] columnNames3 = {"Nr","Sakne1","Sakne2","Sakne3"};
    	if (vārdgrupa != null) {
    		if (vārdgrupa.getStems().size() >= 3) return columnNames3[col];
    	}
        return columnNames1[col];
    }

    public int getRowCount() {
    	if (vārdgrupa == null) return 0;

    	return vārdgrupa.numberOfLexemes();
    }
    public int getColumnCount() {
    	if (vārdgrupa != null) {
    		if (vārdgrupa.getStems().size() >= 3) return 4;
    	}

    	return 2;
    }

    public Object getValueAt(int row, int col) {
    	if (vārdgrupa == null) return null;
    	if (row < 0 || row >= vārdgrupa.lexemes.size()) return null;
    	if (col > vārdgrupa.getStems().size()) return null;

    	switch (col) {
    	case 0 : return vārdgrupa.lexemes.get(row).getID();
    	case 1 : return vārdgrupa.lexemes.get(row).getStem(StemType.STEM1);
    	case 2 : return vārdgrupa.lexemes.get(row).getStem(StemType.STEM2);
    	case 3 : return vārdgrupa.lexemes.get(row).getStem(StemType.STEM3);
    	default: return null;
    	}
    }
    @Override
	public boolean isCellEditable(int row, int col)
        { return (col < 4 & row >= 0); }

    @Override
	public void setValueAt(Object value, int row, int col) {
    	if (vārdgrupa == null) return;
    	if (row >= vārdgrupa.lexemes.size()) return;
    	if (col > vārdgrupa.getStems().size()) return;
    	try {
			switch (col) {
			case 0 : vārdgrupa.lexemes.get(row).setID(Integer.parseInt(value.toString())); break;
			case 1 : vārdgrupa.lexemes.get(row).setStem(StemType.STEM1, value.toString()); break;
			case 2 : vārdgrupa.lexemes.get(row).setStem(StemType.STEM2, value.toString()); break;
			case 3 : vārdgrupa.lexemes.get(row).setStem(StemType.STEM3, value.toString()); break;
			}
    	} catch (NumberFormatException E) {
    		JOptionPane.showMessageDialog(null, String.format("Kolonnā %d ielikts '%s' - neder!",col,value));
    	}
    	if (col == 0) fireTableCellUpdated(row, col);
    	else fireTableDataChanged();
    }

    public void removeRow(int row) {
    	if (vārdgrupa == null) return;
    	vārdgrupa.removeLexeme(vārdgrupa.lexemes.get(row));
    	fireTableRowsDeleted(row,row);
    	//FIXME - hvz vai un kā pēc šitā noapdeitojas leksēmīpašību tabula.
    }

    public void addRow() {
    	if (vārdgrupa == null) return;
    	vārdgrupa.addLexeme(new Lexeme());
    	fireTableDataChanged();
    	//FIXME - hvz vai un kā pēc šitā noapdeitojas leksēmīpašību tabula.
    }
}