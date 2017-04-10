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
package lv.semti.morphology.Testi;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.attributes.TagSet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.corpus.Statistics;

public class MorphoEvaluate {
	private static Analyzer locītājs;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			locītājs = new Analyzer(false);
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
	public void testFile2017() throws IOException{
		LinkedList<Etalons> etaloni = readVertEtalons("all.txt");
		evaluate(etaloni);
	}
	
//	@Test
	public void testFile2013May() throws IOException{
		LinkedList<Etalons> etaloni = readCONLLEtalons("morfoetalons.conll");
		evaluate(etaloni);
	}
	
	public void evaluate(LinkedList<Etalons> etaloni) throws IOException{				
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
		int lemma_correct=0;

		TagSet tags = TagSet.getTagSet();
		
		List<String> mistakes = new LinkedList<String>();
		
		for (Etalons e : etaloni) {
			Word w = locītājs.analyze(e.wordform);
			AttributeValues etalonaAV = tags.fromTag(e.tag);
			if (!e.tag.equalsIgnoreCase(tags.toTag(etalonaAV))) {
				System.out.printf("Slikts tags vārdam %s : '%s' -> '%s' \t\t%s\n", e.wordform, e.tag, tags.toTag(etalonaAV), e.id);
			}
			etalonaAV.removeNonlexicalAttributes();
			e.tag = tags.toTag(etalonaAV);
			
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
					double estimate = Statistics.getStatistics().getEstimate(wf);
					if (estimate > maxticamība) {
						maxticamība = estimate;
						mainwf = wf;
					}
				}
				
				if (mainwf.getValue(AttributeNames.i_PartOfSpeech).equalsIgnoreCase(etalonaAV.getValue(AttributeNames.i_PartOfSpeech)))
					pos_correct++;
                if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma))
                    lemma_correct++;
				output = "\t" + mainwf.getValue(AttributeNames.i_Lemma) + "\t" + mainwf.getTag() + "\n";
				if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && mainwf.getTag().equalsIgnoreCase(e.tag))
					perfect++;  
				//else if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && mainwf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag)))
				else if (mainwf.isMatchingWeak(tags.fromTag(e.tag)))
					first_match++;
				else {
					boolean found = false;
					boolean found_match = false;
					output = "";
					for (Wordform wf : w.wordforms) {
						if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && wf.getTag().equalsIgnoreCase(e.tag)) found = true;
						if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && wf.isMatchingWeak(tags.fromTag(e.tag))) found_match=true;
						//if (wf.isMatchingWeak(MarkupConverter.fromKamolsMarkup(e.tag))) found_match=true;
						output += "\t\t" + wf.getValue(AttributeNames.i_Lemma) + "\t" + wf.getTag() + "\n";
					}
					if (found) one_of_options++;
					else if (found_match) match++; 
					else {
						wrong++;
						mistakes.add(e.wordform+"\nKorpusā:\t"+e.lemma+"\t"+e.tag+"\t\t(" + e.id + ")\n"+output);
					}
				}	
			} else {
				not_recognized++;
				mistakes.add("Nav variantu :( \t"+e.wordform+"\t"+e.lemma+"\t"+e.tag+"\t\t"+e.id+"\n");
			}						
		}
		
		Collections.sort(mistakes);
		for (String mistake:mistakes){
			izeja.println(mistake);
		}
		
		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		izeja.flush();
		
		System.out.printf("Etalona pārbaude: pagāja %d ms\n%d pieprasījumi sekundē\n", starpība, etaloni.size()*1000/starpība);
		System.out.printf("\nAnalīzes rezultāti:\n");
		System.out.printf("\tPareizi:\t%4.1f%%\t%d\n", perfect*100.0/etaloni.size(), perfect);
        System.out.printf("\tLemma ok:\t%4.1f%%\t%d\n", lemma_correct*100.0/etaloni.size(), perfect);
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

	private LinkedList<Etalons> readVertEtalons(String filename)
			throws IOException {
		BufferedReader ieeja;
		String rinda;
		ieeja = new BufferedReader(
				new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8"));
		
		LinkedList<Etalons> etaloni = new LinkedList<Etalons>();
		
		while ((rinda = ieeja.readLine()) != null) {
			if (rinda.contains("<s>") || rinda.contains("</s>") || rinda.equalsIgnoreCase("<g />") || rinda.isEmpty()) continue;
			etaloni.add(Etalons.loadVert(rinda));
		}		
		ieeja.close();
		
		return etaloni;
	}

	private LinkedList<Etalons> readCONLLEtalons(String filename) throws IOException {
		BufferedReader ieeja;
		String rinda;
		ieeja = new BufferedReader(
				new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8"));
		
		LinkedList<Etalons> etaloni = new LinkedList<Etalons>();
		
		while ((rinda = ieeja.readLine()) != null) {
			if (rinda.contains("<s>") || rinda.contains("</s>") || rinda.isEmpty()) continue;
			etaloni.add(Etalons.loadCONLL(rinda));
		}		
		ieeja.close();
		
		return etaloni;	}


	static class Etalons {
		String wordform;
		String lemma;
		String tag;
		String id;
		
		static Etalons loadVert(String rinda) {
			Etalons etalons = new Etalons();
			String[] parse = rinda.split("\t");
			etalons.wordform = parse[0];			
			etalons.tag = parse[1];
			etalons.lemma = parse[2];
			if (parse.length > 3) etalons.id = parse[3]; //some but not all vert files will have a 4th row containing the ID of the word in corpus
			return etalons;
		}

		static Etalons loadCONLL(String rinda) {
			Etalons etalons = new Etalons();
			String[] parse = rinda.split("\t");
			etalons.wordform = parse[1].replace('_', ' ');
			etalons.lemma = parse[2].replace('_', ' ');
			etalons.tag = parse[4];
			return etalons;
		}
	}
	
}
