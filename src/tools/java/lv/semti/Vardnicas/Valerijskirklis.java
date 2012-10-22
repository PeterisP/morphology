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

import java.util.Scanner;

import lv.semti.morphology.attributes.*;

public class Valerijskirklis {
	public String rinda;
	public String celms;
	public String paradigma;
	public String saknesnr;
	public String vārds;
	public String īpašībasVārdaTips;
	
	public Valerijskirklis (String _rinda) {
		rinda = _rinda;
		Scanner s = new Scanner(rinda);
		celms = s.next();
		paradigma = s.next();
		saknesnr = s.next();
	    vārds = s.next().replace("|", "");
	    s.next();
	    s.next();
	    īpašībasVārdaTips = s.next();
        s.close(); 
	}
	
	public AttributeValues VajadzīgāsĪpašības() {
		AttributeValues rezultāts = new AttributeValues();
		switch (paradigma.charAt(0)){
		case '0':
			rezultāts.addAttribute("Vārdšķira", "Lietvārds");
			//rezultāts.PieliktĪpašību("Skaitlis", "Vienskaitlis"); ir arī daudzskaitlinieki
			rezultāts.addAttribute("Locījums", "Nominatīvs");
			break;
		case '2':
			rezultāts.addAttribute("Vārdšķira", "Īpašības vārds");
			// rezultāts.PieliktĪpašību("Skaitlis", "Vienskaitlis"); ir arī daudzskaitlinieki
			rezultāts.addAttribute("Locījums", "Nominatīvs"); // FIXME - ir arī gjenitīveņi
			break;
		}
		
		if (paradigma.equals("00")) {
			rezultāts.addAttribute("Vārdgrupas nr", "11");
		}		
		if (paradigma.equals("01")) {
			rezultāts.addAttribute("Deklinācija", "1");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
			rezultāts.addAttribute("Vārdgrupas nr", "1");
		}
		if (paradigma.equals("02")) {
			rezultāts.addAttribute("Deklinācija", "2");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
		}
		if (paradigma.equals("03")) {
			rezultāts.addAttribute("Deklinācija", "3");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
		}
		if (paradigma.equals("04")) {
			rezultāts.addAttribute("Deklinācija", "4");
			rezultāts.addAttribute("Dzimte", "Sieviešu");
		}
		if (paradigma.equals("05")) {
			rezultāts.addAttribute("Deklinācija", "5");
			rezultāts.addAttribute("Dzimte", "Sieviešu");
		}
		if (paradigma.equals("06")) {
			rezultāts.addAttribute("Deklinācija", "6");
			rezultāts.addAttribute("Dzimte", "Sieviešu");
		}
//		if (paradigma.equals("07")) ???
		if (paradigma.equals("08")) {
			rezultāts.addAttribute("Deklinācija", "1");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
			rezultāts.addAttribute("Vārdgrupas nr", "2");
		}
		if (paradigma.equals("09")) {
			rezultāts.addAttribute("Deklinācija", "2");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
		}
		if (paradigma.equals("0a")) {
			rezultāts.addAttribute("Deklinācija", "2");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
		}
		if (paradigma.equals("0b")) {
			rezultāts.addAttribute("Deklinācija", "2");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
		}
		if (paradigma.equals("0c")) {
			rezultāts.addAttribute("Deklinācija", "4");
			rezultāts.addAttribute("Dzimte", "Vīriešu");
		}
		if (paradigma.equals("0d")) rezultāts.addAttribute("Deklinācija", "4");
		if (paradigma.equals("0e")) {
			rezultāts.addAttribute("Deklinācija", "5");
			rezultāts.addAttribute("Dzimte", "Sieviešu");
		}
		if (paradigma.equals("0f")) {
			rezultāts.addAttribute("Deklinācija", "6");
			rezultāts.addAttribute("Dzimte", "Sieviešu");
		}
		if (paradigma.equals("0f")) {
			rezultāts.addAttribute("Deklinācija", "6");
			rezultāts.addAttribute("Dzimte", "Sieviešu");
		}
		
		return rezultāts;
	}
}
