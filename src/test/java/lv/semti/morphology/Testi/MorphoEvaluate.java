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
package lv.semti.morphology.Testi;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.corpus.Statistics;
import lv.semti.morphology.lexicon.Ending;

public class MorphoEvaluate {
	private static Analyzer locītājs;
	private static Statistics statistics;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		locītājs = new Analyzer("dist/Lexicon.xml");
		statistics = new Statistics("dist/Statistics.xml");
	}
	
	@Before
	public void defaultsettings() { 
		locītājs.enableVocative = false;
		locītājs.enableDiminutive = false;
		locītājs.enablePrefixes = false;
		locītājs.enableGuessing = false;
		locītājs.enableAllGuesses = false;
		locītājs.meklētsalikteņus = false;
    }	
	
	@Test
	public void evaluate() throws IOException {
		BufferedReader ieeja;
		String rinda;
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("dist/morfoetalons.txt"), "UTF-8"));
		
		LinkedList<Etalons> etaloni = new LinkedList<Etalons>();
		
		while ((rinda = ieeja.readLine()) != null) {
			etaloni.add(new Etalons(rinda));
		}
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		
		long sākums = System.currentTimeMillis();
		
		locītājs.enableVocative = true;
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = true;
		locītājs.enableGuessing = false;
		locītājs.enableAllGuesses = false;
		locītājs.meklētsalikteņus = false; 
		
		int perfect = 0;
		int first_match = 0;
		int one_of_options = 0;
		int match = 0;
		int not_recognized = 0;
		int wrong = 0;
		
		for (Etalons e : etaloni) {
			Word w = locītājs.analyze(e.wordform);
			String output = "Neatpazīts";
			if (w.isRecognized()) {
				Wordform mainwf = w.wordforms.get(0);
				int maxticamība = -1;
				for (Wordform wf : w.wordforms) {  // Paskatamies visus atrastos variantus un ņemam statistiski ticamāko
					//tag += String.format("%s\t%d\n", wf.getDescription(), MorphoServer.statistics.getTicamība(wf));
					if (statistics.getEstimate(wf) > maxticamība) {
						maxticamība = statistics.getEstimate(wf);
						mainwf = wf;
					}
				}
				
				output = mainwf.getValue(AttributeNames.i_Lemma) + "\t" + mainwf.getTag();
				if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && mainwf.getTag().equalsIgnoreCase(e.tag))
					perfect++;  
				else if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && mainwf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag)))
					first_match++;
				else {
					boolean found = false;
					boolean found_match = false;
					for (Wordform wf : w.wordforms) {
						if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && wf.getTag().equalsIgnoreCase(e.tag)) found = true;
						if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && wf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag))) found_match=true;
					}
					if (found) one_of_options++;
					else if (found_match) match++; 
					else {
						wrong++;
						izeja.println(e.wordform+"\n\t"+e.lemma+"\t"+e.tag+"\n\t"+output);
					}
				}
			} else {
				not_recognized++;				
			}						
		}
		
		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		izeja.flush();
		
		System.out.printf("Etalona pārbaude: pagāja %d ms\n%d pieprasījumi sekundē\n", starpība, etaloni.size()*1000/starpība);
		System.out.printf("\nAnalīzes rezultāti:\n");
		System.out.printf("\tPareizi:\t%4.1f%%\t%d\n", perfect*100.0/etaloni.size(), perfect);
		System.out.printf("\tDer:\t%4.1f%%\t%d\n", first_match*100.0/etaloni.size(), first_match);
		System.out.printf("\tNav pirmais:\t%4.1f%%\t%d\n", one_of_options*100.0/etaloni.size(), one_of_options);
		System.out.printf("\tDer ne pirmais:\t%4.1f%%\t%d\n", match*100.0/etaloni.size(), match);
		System.out.printf("\tNav nekā pareiza:\t%4.1f%%\t%d\n", wrong*100.0/etaloni.size(), wrong);
		System.out.printf("\tNeatpazīti:\t%4.1f%%\t%d\n", not_recognized*100.0/etaloni.size(), not_recognized);
		System.out.printf("\nEtalons: Pareizi 65%%+,  Neatpazīti zem 4%%\n");
	}
	
	class Etalons {
		String wordform;
		String lemma;
		String tag;
		
		Etalons(String rinda) {
			String[] parse = rinda.split("\t");
			wordform = parse[0];
			lemma = parse[1];
			tag = parse[2];
		}
	}
}
