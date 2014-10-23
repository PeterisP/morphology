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
package lv.semti.Vardnicas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Valerijformats {
	public ArrayList<Valerijskirklis> Šķirkļi = new ArrayList <Valerijskirklis>(); 
	
	public Valerijformats(String Filename) throws UnsupportedEncodingException, FileNotFoundException {
			BufferedReader ieeja = new BufferedReader(
									new InputStreamReader(new FileInputStream(Filename), "windows-1257"));
			
			String rinda;
		    try {
				while ((rinda = ieeja.readLine()) != null) {
					Valerijskirklis šķirklis = null;
					try {
						šķirklis = new Valerijskirklis(rinda);
					} catch (NoSuchElementException e) {
						//FIXME - ko ta nu darīt?}
					}
					if (šķirklis != null) Šķirkļi.add(šķirklis);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		
}
