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
import java.io.*;
import java.util.*;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Vardnica {
	ArrayList<Skirklis> šķirkļi;
	
	public enum KurEsam {
		NEKAS, ŠĶIRKLIS, NOZĪME, PIEMĒRS, FRĀZE//, VĀRDS, DEFINĪCIJA
	}
	
	class LLVVLasītājs extends DefaultHandler {
//		Ielasa Latviešu Literārās Valodas Vārdnīcas XML formātu
				
		Skirklis lasāmaisŠķirklis; // pagaidu mainīgais, kurā tiek savākti dati 
		KurEsam kurEsam; // vai šobrīd tiek lasīti šķirkļa dati, vai arī esam kur citur
		
		String lauks="";  // XML elementa nosaukums, kuru šobrīd lasa, lai ieliktu laukā
		String vērtība=""; // pagaidām nolasītā vērtība - XML'a teksta daļa.
		String teksts=""; //'t' lauks XML'ā
		
		@Override
		public void startDocument() {
			kurEsam = KurEsam.NEKAS;
			šķirkļi = new ArrayList<Skirklis>();			
	    }

	    @Override
		public void startElement (String uri, String name,
				      String qName, Attributes atts) {
	    	
	    	switch (kurEsam) {
	    	case ŠĶIRKLIS:
	    		if (name.equals("n")) { // sākas nozīme  
	    			kurEsam = KurEsam.NOZĪME;	    			  
	    		} else if (name.equals("fraz")) { // sākas frāze  
	    			kurEsam = KurEsam.FRĀZE;	    			  
	    		} else vērtība = "";
	    	  //else System.out.println("Sākas nesaprotams elements "+name);
		    	break;
	    	case NEKAS:
	    		if (name.equals("s")) {
	    			lasāmaisŠķirklis = new Skirklis();
	    			kurEsam = KurEsam.ŠĶIRKLIS;
	    			lauks = "";
	    		}
	    		break;
	    	case NOZĪME:
	    	    if (name.equals("d")) { // sākas definīcija  
	    			teksts = "";	    			  
	    		} else if (name.equals("piem")) { // sākas piemērs  
	    			kurEsam = KurEsam.PIEMĒRS;	    			  
	    		} else vērtība = "";	    		
	    		break;
	    	case PIEMĒRS:
	    		break;
	    	case FRĀZE:
	    		break;
	    	default: System.out.println("Sākas nesaprotams elements "+name); 
	    	}
	    }

	    @Override
		public void endElement (String uri, String name, String qName) {
	      try {	
		    switch (kurEsam) {
		    case ŠĶIRKLIS:
	    		if (name.equals("s")) {
	    			šķirkļi.add(lasāmaisŠķirklis);
	    			lasāmaisŠķirklis = null;
	    			kurEsam = KurEsam.NEKAS;	    			
	    		} else if (name.equals("vf")) {
	    			lasāmaisŠķirklis.vf = vērtība;
	    			vērtība = "";
	    		} else if (name.equals("gram")) {
	    			lasāmaisŠķirklis.gram = vērtība;
	    			vērtība = "";
	    		} //else System.out.println("Beidzas nesaprotams elements "+name);
	    		break;
		    case NOZĪME:
	    		if (name.equals("n")) {
	    			kurEsam = KurEsam.ŠĶIRKLIS;
	    			vērtība = "";
	    		} else if (name.equals("d")) {
	    			lasāmaisŠķirklis.d.add(teksts);
	    			teksts = "";	    			
	    		} else if (name.equals("t")) {
	    			teksts = vērtība;
	    			vērtība = "";
	    		}//else System.out.println("Beidzas nesaprotams elements "+name); 
	    		break;
		    case PIEMĒRS:
		    	if (name.equals("piem")) {	    			
	    			kurEsam = KurEsam.NOZĪME;	    					    		
		    	}//else System.out.println("Beidzas nesaprotams elements "+name);
		    	break;
		    case FRĀZE:
		    	if (name.equals("fraz")) {	    			
	    			kurEsam = KurEsam.ŠĶIRKLIS;	    					    		
		    	}//else System.out.println("Beidzas nesaprotams elements "+name);
		    	break;
	    	default: 
	    		System.out.println("Neesam šķirklī, beidzas elements "+name); 
		    }  
	      }
	      catch (NumberFormatException e) {
			System.out.printf("Laukā %s bija vērtība %s\n",lauks,vērtība);}
	    }

	    @Override
	    public void characters (char ch[], int start, int length) {
	    	for (int i = start; i < start + length; i++) {
				vērtība += ch[i];
			}
	    }		
	}
	
	public Vardnica(String filename) throws Exception	{		
		XMLReader xr = XMLReaderFactory.createXMLReader();
		LLVVLasītājs lasītājs = new LLVVLasītājs();
		xr.setContentHandler(lasītājs);
				
		BufferedReader straume = 
			new BufferedReader(
					new InputStreamReader(new FileInputStream(filename), "UTF8"));		
		xr.parse(new InputSource(straume)); 
				
	}	
}
