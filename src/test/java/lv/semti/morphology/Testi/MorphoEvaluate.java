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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.corpus.Statistics;

public class MorphoEvaluate {
	private static Analyzer locītājs;
	private static Statistics statistics;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			locītājs = new Analyzer("dist/Lexicon.xml");
			statistics = new Statistics("dist/Statistics.xml");
		} catch(Exception e) {
			e.printStackTrace();
		} 
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
				new InputStreamReader(new FileInputStream("dist/test.txt"), "UTF-8"));
		
		LinkedList<Etalons> etaloni = new LinkedList<Etalons>();
		
		while ((rinda = ieeja.readLine()) != null) {
			if (rinda.contains("<s>") || rinda.contains("</s>")) continue;
			etaloni.add(new Etalons(rinda));
		}
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		
		long sākums = System.currentTimeMillis();
		
		locītājs.enableVocative = true;
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = true;
		locītājs.enableGuessing = true; // jāčeko
		locītājs.enableAllGuesses = false;
		locītājs.meklētsalikteņus = false; 
		
		int perfect = 0;
		int first_match = 0;
		int one_of_options = 0;
		int match = 0;
		int not_recognized = 0;
		int wrong = 0;
		int pos_correct=0;
		int oov=0; //out of vocabulary
		int unambiguous=0;
		int ambiguous=0;
		int wfcount=0;
		int ambig_wfcount=0;
		
		for (Etalons e : etaloni) {
			Word w = locītājs.analyze(e.wordform);
			AttributeValues etalonaAV = MarkupConverter.fromKamolsMarkup(e.tag);
			if (!e.tag.equalsIgnoreCase(MarkupConverter.toKamolsMarkupNoDefaults(etalonaAV))) {
				System.out.printf("Slikts tags vārdam %s : '%s' -> '%s' \n", e.wordform, e.tag, MarkupConverter.toKamolsMarkupNoDefaults(etalonaAV));
			}
			etalonaAV.removeNonlexicalAttributes();
			e.tag = MarkupConverter.toKamolsMarkup(etalonaAV);
			
			boolean in_voc=false;
			for (Wordform wf : w.wordforms) {
				if (wf.getValue(AttributeNames.i_Guess)==null || wf.getValue(AttributeNames.i_Guess).equalsIgnoreCase("Nav")) in_voc=true;
			}
			//System.out.printf("%s in vocabulary:%s\n",e.wordform,Boolean.toString(in_voc));
			if (!in_voc) oov++;
			
			wfcount += w.wordformsCount();
			if (w.wordformsCount() > 1) {
				ambiguous++;
				ambig_wfcount += w.wordformsCount();
			} else unambiguous++;
			
			String output = "Neatpazīts";
			if (w.isRecognized()) {
				Wordform mainwf = w.wordforms.get(0);
				double maxticamība = -1;
				for (Wordform wf : w.wordforms) {  // Paskatamies visus atrastos variantus un ņemam statistiski ticamāko
					//tag += String.format("%s\t%d\n", wf.getDescription(), MorphoServer.statistics.getTicamība(wf));
					wf.removeNonlexicalAttributes();
					if (statistics.getEstimate(wf) > maxticamība) {
						maxticamība = statistics.getEstimate(wf);
						mainwf = wf;
					}
				}
				
				if (mainwf.getValue(AttributeNames.i_PartOfSpeech).equalsIgnoreCase(etalonaAV.getValue(AttributeNames.i_PartOfSpeech)))
						pos_correct++;
				
				output = "\t" + mainwf.getValue(AttributeNames.i_Lemma) + "\t" + mainwf.getTag() + "\n";
				if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && mainwf.getTag().equalsIgnoreCase(e.tag))
					perfect++;  
				//else if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && mainwf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag)))
				else if (mainwf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag)))
					first_match++;
				else {
					boolean found = false;
					boolean found_match = false;
					output = "";
					for (Wordform wf : w.wordforms) {
						if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && wf.getTag().equalsIgnoreCase(e.tag)) found = true;
//						if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && wf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag))) found_match=true;
						if (wf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag))) found_match=true;
						output += "\t" + wf.getValue(AttributeNames.i_Lemma) + "\t" + wf.getTag() + "\n";
					}
					if (found) one_of_options++;
					else if (found_match) match++; 
					else {
						wrong++;
						izeja.print(e.wordform+"\nDer:\t"+e.lemma+"\t"+e.tag+"\n"+output);
					}
				}	
			} else {
				not_recognized++;
				//izeja.print(e.wordform+"\t"+e.lemma+"\t"+e.tag+"\n");
			}						
		}
		
		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		izeja.flush();
		
		System.out.printf("Etalona pārbaude: pagāja %d ms\n%d pieprasījumi sekundē\n", starpība, etaloni.size()*1000/starpība);
		System.out.printf("\nAnalīzes rezultāti:\n");
		System.out.printf("\tPareizi:\t%4.1f%%\t%d\n", perfect*100.0/etaloni.size(), perfect);
		System.out.printf("\tDer:    \t%4.1f%%\t%d\n", (first_match+perfect)*100.0/etaloni.size(), first_match);
		System.out.printf("\tNav pirmais:\t%4.1f%%\t%d\n", one_of_options*100.0/etaloni.size(), one_of_options);
		System.out.printf("\tDer ne pirmais:\t%4.1f%%\t%d\n", (match+one_of_options)*100.0/etaloni.size(), match);
		System.out.printf("\tNekas neder:\t%4.1f%%\t%d\n", wrong*100.0/etaloni.size(), wrong);
		System.out.printf("\tNeatpazīti:\t%4.1f%%\t%d\n", not_recognized*100.0/etaloni.size(), not_recognized);
		System.out.printf("\tPareizs POS:\t%4.1f%%\t%d\n", pos_correct*100.0/etaloni.size(), pos_correct);		
		System.out.printf("\nEtalons: Pareizi 85.9%%, Der 87.6%%, Nav vārdnīcā 5.6%%, Neatpazīti zem 2%%\n");
		
		System.out.printf("\nStatistika:\n");
		System.out.printf("\tNav vārdnīcā:\t\t%4.1f%%\t%d\n", oov*100.0/etaloni.size(), oov);
		System.out.printf("\tViennozīmīgi:\t\t%4.1f%%\t%d\n", unambiguous*100.0/(unambiguous+ambiguous), unambiguous);
		System.out.printf("\tDaudznozīmīgi:\t\t%4.1f%%\t%d\n", ambiguous*100.0/(unambiguous+ambiguous), ambiguous);
		System.out.printf("\tVariantu skaits:\t%4.2f\n", wfcount*1.0/(unambiguous+ambiguous));
		System.out.printf("\tVariantu skaits tiem, kas daudznozīmīgi:\t%4.2f\n", ambig_wfcount*1.0/ambiguous);
		
	}
	
	class Etalons {
		String wordform;
		String lemma;
		String tag;
		
		Etalons(String rinda) {
			String[] parse = rinda.split("\t");
			wordform = parse[0];
			lemma = parse[2];
			tag = parse[1];
		}
	}
	
}
