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
package lv.semti.Vardnicas;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Skirklis {
	public String vf; //vārdforma
	public String gram; //gramatika
	public ArrayList<String> d = new ArrayList<String>();
	
	public void Aprakstīt(PrintWriter truba){		
		truba.format("%s : %s\n", vf, gram);
		int i = 0;
		for (String def : d) {
			i++;
			truba.format("Nozīme %s: %s\n", i, def);
		}
		truba.print("\n");
		truba.flush();
	}
}
