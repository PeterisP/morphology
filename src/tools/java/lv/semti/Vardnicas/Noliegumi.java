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
import java.util.ArrayList;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;
import lv.semti.morphology.lexicon.Paradigm;

public class Noliegumi {

	public static void main(String[] args) throws Exception {
				
		Analyzer analizators = new Analyzer("dist\\Lexicon.xml");
		analizators.guessNouns = false;
		analizators.guessParticibles = false;
		analizators.guessVerbs = false;
		analizators.guessAdjectives = true;
		analizators.enableDiminutive = false;
		analizators.enablePrefixes = false;
		analizators.enableGuessing = false;
		analizators.meklētsalikteņus = false;
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "windows-1257"));
		BufferedReader ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("dist\\noliegtie.txt"), "windows-1257"));

		String rinda;
		ArrayList<String> noliegtie = new ArrayList<String>();
		try {
			while ((rinda = ieeja.readLine()) != null) {
				noliegtie.add(rinda);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Paradigm vārdgrupa : analizators.paradigms) {
			for (Lexeme leksēma : vārdgrupa.lexemes) {
				if (leksēma.getStem(0).startsWith("ne") && !vārdgrupa.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective) &&
						!vārdgrupa.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun)) {
					for (String elements : noliegtie) {
						if (leksēma.getStem(0).equalsIgnoreCase(elements)) {
							izeja.printf("Haa, noliedzam! ");		
							leksēma.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
						}
					}
					izeja.printf("%s\n"//"Leksēmai '%s' noliegums :%s\n"
							,leksēma.getStem(0), leksēma.getValue(AttributeNames.i_Noliegums));					
				}
			}
		}
		
	
//		izeja.printf("No %d vārdiem %d sakrīt; %d neatpazīti (un %d no tiem pielikti leksikonam); bet %d nevar saprast.\n",
//					Valērijvārdi_kopā,Valērijvārdi_sakrīt_labi,Valērijvārdi_neatpazīti,Valērijvārdi_neatpazīti_uzminēti,Valērijvārdi_hvz);
		izeja.flush();
		analizators.toXML("dist\\Leksikons_updated.xml");
	}

}
