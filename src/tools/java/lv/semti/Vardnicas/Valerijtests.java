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
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;

public class Valerijtests {

	public static void main(String[] args) throws Exception {
				
		Valerijformats lietvārdi = new Valerijformats("I:\\Gvarvins\\darbagalds\\Morphology\\src\\tools\\resources\\valerijs\\lietvardi.txt");
		Valerijformats īpašībasVārdi = new Valerijformats("I:\\Gvarvins\\darbagalds\\Morphology\\src\\tools\\resources\\valerijs\\adjectives.txt");
		Analyzer analizators = new Analyzer("I:\\Gvarvins\\darbagalds\\Morphology\\src\\main\\resources\\Leksikons_tukshs.xml");
		analizators.guessNouns = false;
		analizators.guessParticiples = false;
		analizators.guessVerbs = false;
		analizators.guessAdjectives = true;
		analizators.enableDiminutive = false;
		analizators.enablePrefixes = false;
		analizators.enableGuessing = false;
		analizators.meklētsalikteņus = false;
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "windows-1257"));
		
		//analizators.Analizēt("abonēšana").Aprakstīt(izeja);
		
		//int Valērijvārdi_kopā = 0;
		//int Valērijvārdi_neatpazīti = 0;
		//int Valērijvārdi_neatpazīti_uzminēti = 0;
		//int Valērijvārdi_sakrīt_labi = 0;
		//int Valērijvārdi_hvz = 0;
		
		for (Valerijskirklis šķirklis : lietvārdi.Šķirkļi) {	
			if (!šķirklis.saknesnr.equalsIgnoreCase("01")) continue; 
			    // skatamies tikai pirmo no visām saknēm, kas Valērijam ir vairākas pat miju dēļ
			
			if (šķirklis.paradigma.equals("00")) {
				//izeja.printf("%s %d \n",šķirklis.vārds, analizators.vārdgrupaPēcID(11).getPamatforma().getNr() );
				analizators.createLexeme(šķirklis.vārds, 111, "Imports no Valērija faila");
			}
						
			if (šķirklis.paradigma.equals("01")) 
				pieliktVārdu(analizators, izeja, šķirklis, 1, 8);
			
			if (šķirklis.paradigma.equals("02")) 
				pieliktVārdu(analizators, izeja, šķirklis, 27, 33);
			
			if (šķirklis.paradigma.equals("03")) 
				pieliktVārdu(analizators, izeja, šķirklis, 51, 57);

			if (šķirklis.paradigma.equals("04")) 
				pieliktVārdu(analizators, izeja, šķirklis, 75, 81);

			if (šķirklis.paradigma.equals("05"))				
				pieliktVārdu(analizators, izeja, šķirklis, 87, 93);

			if (šķirklis.paradigma.equals("06"))
				pieliktVārdu(analizators, izeja, šķirklis, 99, 105);

			if (šķirklis.paradigma.equals("07")) {
				pieliktVārdu(analizators, izeja, šķirklis, 1211, 1211);
			}
			
			if (šķirklis.paradigma.equals("08"))
				pieliktVārdu(analizators, izeja, šķirklis, 14, 21);

			if (šķirklis.paradigma.equals("09"))
				pieliktVārdu(analizators, izeja, šķirklis, 39, 45);

			if (šķirklis.paradigma.equals("0a"))
				pieliktVārdu(analizators, izeja, šķirklis, 27, 33);

			if (šķirklis.paradigma.equals("0b"))
				pieliktVārdu(analizators, izeja, šķirklis, 39, 45);

			if (šķirklis.paradigma.equals("0c"))
				pieliktVārdu(analizators, izeja, šķirklis, 63, 69);

			if (šķirklis.paradigma.equals("0d")) {
				pieliktVārdu(analizators, izeja, šķirklis, 63, 69);
				pieliktVārdu(analizators, izeja, šķirklis, 75, 81);
			}	

			if (šķirklis.paradigma.equals("0e"))
				pieliktVārdu(analizators, izeja, šķirklis, 87, 93);

			if (šķirklis.paradigma.equals("0f"))
				pieliktVārdu(analizators, izeja, šķirklis, 99, 105);

		}

		for (Valerijskirklis šķirklis : īpašībasVārdi.Šķirkļi) {	
			if (!šķirklis.saknesnr.equalsIgnoreCase("01")) {
				izeja.printf("Saknes nr nav 01: %s \n",šķirklis.vārds);
				continue; 
			}
			    // skatamies tikai pirmo no visām saknēm, kas Valērijam ir vairākas pat miju dēļ
			
			if (šķirklis.paradigma.equals("21")) {
//				pieliktVārdu(analizators, izeja, šķirklis, 135, 93);
				Lexeme jaunais = analizators.createLexeme(šķirklis.vārds, 135, "Imports no Valērija faila");
				if (šķirklis.īpašībasVārdaTips.equals("q"))
					jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_QualificativeAdjective);
				else jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_RelativeAdjective);
			}
			if (šķirklis.paradigma.equals("22")) {
//				pieliktVārdu(analizators, izeja, šķirklis, 180, 93);
				Lexeme jaunais = analizators.createLexeme(šķirklis.vārds, 180, "Imports no Valērija faila");
				if (šķirklis.īpašībasVārdaTips.equals("q"))
					jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_QualificativeAdjective);
				else jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_RelativeAdjective);
			}
			if (šķirklis.paradigma.equals("29")) {
				if (šķirklis.vārds.endsWith(analizators.endingByID(136).getEnding())) {
					Lexeme jaunais = analizators.createLexeme(šķirklis.vārds, 136, "Imports no Valērija faila");
					jaunais.addAttribute(AttributeNames.i_CaseSpecial, AttributeNames.v_InflexibleGenitive);
					jaunais.addAttribute(AttributeNames.i_Lemma, šķirklis.vārds);
					if (šķirklis.īpašībasVārdaTips.equals("q"))
						jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_QualificativeAdjective);
					else jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_RelativeAdjective);
				} else if (šķirklis.vārds.endsWith(analizators.endingByID(141).getEnding())) {
					Lexeme jaunais = analizators.createLexeme(šķirklis.vārds, 141, "Imports no Valērija faila"); // daudzskaitliieks
					jaunais.addAttribute(AttributeNames.i_CaseSpecial, AttributeNames.v_InflexibleGenitive);
					jaunais.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
					jaunais.addAttribute(AttributeNames.i_Lemma, šķirklis.vārds);
					if (šķirklis.īpašībasVārdaTips.equals("q"))
						jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_QualificativeAdjective);
					else jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_RelativeAdjective);
				} else if (šķirklis.vārds.endsWith(analizators.endingByID(76).getEnding())) {
					Lexeme jaunais = analizators.createLexeme(šķirklis.vārds, 76, "Imports no Valērija faila");
					jaunais.addAttribute(AttributeNames.i_CaseSpecial, AttributeNames.v_InflexibleGenitive);
					jaunais.addAttribute(AttributeNames.i_Lemma, šķirklis.vārds);
					if (šķirklis.īpašībasVārdaTips.equals("q"))
						jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_QualificativeAdjective);
					else jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_RelativeAdjective);
				} else if (šķirklis.vārds.endsWith(analizators.endingByID(88).getEnding())) {
					Lexeme jaunais = analizators.createLexeme(šķirklis.vārds, 88, "Imports no Valērija faila");
					jaunais.addAttribute(AttributeNames.i_CaseSpecial, AttributeNames.v_InflexibleGenitive);
					jaunais.addAttribute(AttributeNames.i_Lemma, šķirklis.vārds);
					if (šķirklis.īpašībasVārdaTips.equals("q"))
						jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_QualificativeAdjective);
					else jaunais.addAttribute(AttributeNames.i_AdjectiveType, AttributeNames.v_RelativeAdjective);
				} else izeja.printf("%s \n",šķirklis.vārds);
			}
		}
		
		BufferedReader ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("I:\\Gvarvins\\darbagalds\\Morphology\\src\\tools\\resources\\valerijs\\2_conj.txt"), "windows-1257"));

		String rinda;
		try {
			while ((rinda = ieeja.readLine()) != null) {
				rinda = rinda.trim();
				if (rinda.endsWith(analizators.endingByID(225).getEnding())) {
					analizators.createLexeme(rinda, 225, "Imports no Valērija faila");
				} else if (rinda.endsWith(analizators.endingByID(955).getEnding())) {
					analizators.createLexeme(rinda, 955, "Imports no Valērija faila");					
				} else izeja.printf("%s \n",rinda);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("I:\\Gvarvins\\darbagalds\\Morphology\\src\\tools\\resources\\valerijs\\3_conj.txt"), "windows-1257"));

		try {
			while ((rinda = ieeja.readLine()) != null) {
				rinda = rinda.trim();
				if (rinda.endsWith(analizators.endingByID(468).getEnding())) {
					analizators.createLexeme(rinda, 468, "Imports no Valērija faila");
				} else if (rinda.endsWith(analizators.endingByID(1008).getEnding())) {
					analizators.createLexeme(rinda, 1008, "Imports no Valērija faila");					
				} else izeja.printf("%s \n",rinda);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		izeja.printf("No %d vārdiem %d sakrīt; %d neatpazīti (un %d no tiem pielikti leksikonam); bet %d nevar saprast.\n",
//					Valērijvārdi_kopā,Valērijvārdi_sakrīt_labi,Valērijvārdi_neatpazīti,Valērijvārdi_neatpazīti_uzminēti,Valērijvārdi_hvz);
		izeja.flush();
		analizators.toXML("I:\\Gvarvins\\darbagalds\\Morphology\\src\\main\\resources\\Leksikons_Valeerija.xml");
	}

	private static void pieliktVārdu(Analyzer analizators, PrintWriter izeja,
			Valerijskirklis šķirklis, int vienskGalotne, int daudzskGalotne) {
		if (!šķirklis.vārds.endsWith(analizators.endingByID(vienskGalotne).getEnding())) {
			Lexeme jaunais = analizators.createLexeme(šķirklis.vārds, daudzskGalotne, "Imports no Valērija faila"); // daudzskaitliieks
			jaunais.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
			jaunais.addAttribute(AttributeNames.i_Lemma, šķirklis.vārds);
		} else analizators.createLexeme(šķirklis.vārds, vienskGalotne, "Imports no Valērija faila");
	}

}
