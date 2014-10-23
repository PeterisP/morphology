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

public class VardgrupuModelis extends AbstractTableModel {
	/**
	 * Eclipsei par prieku.
	 */
	private static final long serialVersionUID = 1L;
	
	Lexicon lexicon = null;
	String[] columnNames = {"Nr","Nosaukums","Saknes","Galotnes","Leksēmas","Pamatforma"};

	public VardgrupuModelis(Lexicon kotadeditējam) {
		lexicon = kotadeditējam;
		fireTableStructureChanged();
	}

    @Override
	public String getColumnName(int col) {
        return columnNames[col].toString();
    }
    public int getRowCount() { return lexicon.paradigms.size(); }
    public int getColumnCount() { return columnNames.length; }

    public Object getValueAt(int row, int col) {
    	if (row < 0 || row >= lexicon.paradigms.size()) return null;
    	Paradigm vārdgrupa = lexicon.paradigms.get(row);
    	switch (col) {
    	case 0 : return vārdgrupa.getID();
    	case 1 : return vārdgrupa.getName();
    	case 2 : return vārdgrupa.getStems();
    	case 3 : return vārdgrupa.numberOfEndings();
    	case 4 : return vārdgrupa.numberOfLexemes();
    	case 5 : return (vārdgrupa.getLemmaEnding() == null) ? 0 : vārdgrupa.getLemmaEnding().getID();
    	default: return null;
    	}
    }
    @Override
	public boolean isCellEditable(int row, int col)
        { return ((col < 3 || col == 5) && row >= 0); }

    @Override
	public void setValueAt(Object value, int row, int col) {
    	Paradigm vārdgrupa = lexicon.paradigms.get(row);

    	try {
	    	switch (col) {
	    		case 0 : vārdgrupa.setID(Integer.parseInt(value.toString())); break;
	    		case 1 : vārdgrupa.setDescription( value.toString() ); break;
	    		case 2 : vārdgrupa.setStems ( Integer.parseInt(value.toString())); break;
	    		case 5 : vārdgrupa.setLemmaEnding( Integer.parseInt(value.toString())); break;
	    	}
    	} catch (NumberFormatException E) {
    		JOptionPane.showMessageDialog(null, String.format("Kolonnā %d ielikts '%s' - neder!",col,value));
    	}
        fireTableCellUpdated(row, col);
    }

    public void removeRow(int row) {
    	lexicon.removeParadigm(lexicon.paradigms.get(row));
    	fireTableRowsDeleted(row,row);
    	//FIXME - hvz vai un kā pēc šitā noapdeitojas vārdgrupīpašību tabula.
    }

    public void addRow() {
    	lexicon.addParadigm(new Paradigm(lexicon));
    	fireTableDataChanged();
    	//FIXME - hvz vai un kā pēc šitā noapdeitojas vārdgrupīpašību tabula.
    }
}