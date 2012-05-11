/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia; Author: Pēteris Paikens
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

public class EndingModel extends AbstractTableModel {
	/**
	 * Eclipsei par prieku.
	 */
	private static final long serialVersionUID = 1L;
	
	Paradigm vārdgrupa = null;
	String[] columnNames = {"Nr","Galotne","Mija","SaknesNr","Pamatforma"};

	public void setVārdgrupa(Paradigm vārdgrupa) {
		this.vārdgrupa = vārdgrupa;
		fireTableDataChanged();
	}

    @Override
	public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() {
    	if (vārdgrupa == null) return 0;
    	return vārdgrupa.numberOfEndings();
    }
    public int getColumnCount() { return columnNames.length; }

    public Object getValueAt(int row, int col) {
    	if (vārdgrupa == null) return null;
    	if (row < 0 || row >= vārdgrupa.endings.size()) return null;

    	switch (col) {
    	case 0 : return vārdgrupa.endings.get(row).getID();
    	case 1 : return vārdgrupa.endings.get(row).getEnding();
    	case 2 : return vārdgrupa.endings.get(row).getMija();
    	case 3 : return vārdgrupa.endings.get(row).stemID;
    	case 4 : return (vārdgrupa.endings.get(row).getLemmaEnding() == null) ? 0 : vārdgrupa.endings.get(row).getLemmaEnding().getID();
    	}
		return null;
    }

    @Override
	public boolean isCellEditable(int row, int col)
        { return (col < 5 & row >= 0); }

    @Override
	public void setValueAt(Object value, int row, int col) {
    	if (vārdgrupa == null) return;

    	try {
			switch (col) {
//			case 0 : vārdgrupa.galotnes.get(row).setNr(Integer.parseInt(value.toString())); break;
			case 1 : vārdgrupa.endings.get(row).setEnding(value.toString()); break;
			case 2 : vārdgrupa.endings.get(row).setMija(Integer.parseInt(value.toString())); break;
			case 3 : vārdgrupa.endings.get(row).stemID = Integer.parseInt(value.toString()); break;
			case 4 : vārdgrupa.endings.get(row).setLemmaEnding(Integer.parseInt(value.toString())); break;
			}
    	} catch (NumberFormatException E) {
    		JOptionPane.showMessageDialog(null, String.format("Kolonnā %d ielikts '%s' - neder!",col,value));
    	} catch (Exception E) {
    		JOptionPane.showMessageDialog(null, "Nekorekts pamatformas numurs!");
    	}

    	fireTableCellUpdated(row, col);
    }

    public void removeRow(int row) {
    	if (vārdgrupa == null) return;
    	vārdgrupa.removeEnding(vārdgrupa.endings.get(row));
    	fireTableRowsDeleted(row,row);
    	//FIXME - hvz vai un kā pēc šitā noapdeitojas galotņuīpašību tabula.
    }

    public void addRow() {
    	if (vārdgrupa == null) return;

    	vārdgrupa.addEnding(new Ending());
    	fireTableDataChanged();
    	//FIXME - hvz vai un kā pēc šitā noapdeitojas galotņuīpašību tabula.
    }
}