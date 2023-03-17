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
import java.util.*;

import com.google.common.collect.*;
import com.google.common.primitives.Ints;
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
//			locītājs = new Analyzer("Lexicon.xml", false);
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

    /**
     * Code from https://stackoverflow.com/questions/7881629/sort-guava-multimap-by-number-of-values answer
     * @return a {@link Multimap} whose entries are sorted by descending frequency
     */
    public Multimap<String, String> sortedByDescendingFrequency(Multimap<String, String> multimap) {
        return ImmutableMultimap.<String, String>builder()
                .orderKeysBy(descendingCountOrdering(multimap.keys()))
                .putAll(multimap)
                .build();
    }

    private static Ordering<String> descendingCountOrdering(final Multiset<String> multiset) {
        return new Ordering<String>() {
            @Override
            public int compare(String left, String right) {
                return Ints.compare(multiset.count(right), multiset.count(left));
            }
        };
    }
	
	public void evaluate(LinkedList<Etalons> etaloni) throws IOException{				
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		
		long sākums = System.currentTimeMillis();
		
		locītājs.defaultSettings();
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;

        int first_pos_correct=0;
        int any_pos_correct=0;
        int first_lemma_correct=0;
        int any_lemma_correct=0;
		int first_all_correct = 0;
        int any_all_correct = 0;
		int first_compatible = 0;
		int any_compatible = 0;
		int not_recognized = 0;
		int wrong = 0;
		int oov=0; //out of vocabulary
		int unambiguous=0;
		int unambiguous_lemma=0;
		int ambiguous=0;
		int wfcount=0;
		int ambig_wfcount=0;

		TagSet tags = TagSet.getTagSet();

        Multimap<String, String> mistakes_by_lemma = ArrayListMultimap.create();
        Map<String, Integer> oov_frequency = new HashMap();
		List<String> mistakes = new LinkedList<String>();
		List<String> capitalization_mistakes = new LinkedList<String>();
		
		for (Etalons e : etaloni) {
		    if (e.tag.startsWith("np") && !e.lemma.matches("(?U)^\\p{Lu}[\\p{Alnum}-.`]*$")) {
		    	if (!e.lemma.startsWith("airBaltic"))
					capitalization_mistakes.add(String.format("Īpašvārda lemma nav ar lielo burtu: %s\t%s", e.lemma, e.id));
			}

            if (e.tag.startsWith("n") && !e.tag.startsWith("np") && !e.lemma.matches("(?U)^[\\p{Ll}-.]+$"))
                capitalization_mistakes.add(String.format("Sugasvārda lemma nav ar mazajiem burtiem: %s\t%s", e.lemma, e.id));

			Word w = locītājs.analyze(e.wordform);
			AttributeValues etalonaAV = tags.fromTag(e.tag);
			if (!e.tag.equalsIgnoreCase(tags.toTag(etalonaAV))) {
				System.out.printf("Nesavietojams tags vārdam %s : failā '%s', morfostruktūrās '%s' \t\t%s\n", e.wordform, e.tag, tags.toTag(etalonaAV), e.id);
			}
			etalonaAV.removeAttributesForCorpusTest();
			e.tag = tags.toTag(etalonaAV);


			boolean in_voc=false;
			for (Wordform wf : w.wordforms) {
				if (wf.getValue(AttributeNames.i_Guess)==null
						|| wf.isMatchingStrong(AttributeNames.i_Guess, "Nav")
						|| (wf.getValue(AttributeNames.i_Guess).equalsIgnoreCase(AttributeNames.v_Prefix) &&
							wf.isMatchingStrong(AttributeNames.i_Prefix, "ne"))
				) in_voc=true;
			}
			//System.out.printf("%s in vocabulary:%s\n",e.wordform,Boolean.toString(in_voc));
			if (!in_voc) {
				oov++;
				oov_frequency.put(e.wordform, oov_frequency.getOrDefault(e.wordform, 0)+1);
			}
			
			wfcount += w.wordformsCount();
			if (w.wordformsCount() > 1) {
				ambiguous++;
				ambig_wfcount += w.wordformsCount();
			} else unambiguous++;
			
			if (w.isRecognized()) {
				Wordform mainwf = w.wordforms.get(0);
				double maxticamība = -1;
				boolean visas_lemmas_vienādas = true;
				for (Wordform wf : w.wordforms) {  // Paskatamies visus atrastos variantus un ņemam statistiski ticamāko
					//tag += String.format("%s\t%d\n", wf.getDescription(), MorphoServer.statistics.getTicamība(wf));
					wf.removeAttributesForCorpusTest();
					double estimate = Statistics.getStatistics().getEstimate(wf);
					if (estimate > maxticamība) {
						maxticamība = estimate;
						mainwf = wf;
					}
					if (! wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(mainwf.getValue(AttributeNames.i_Lemma)))
						visas_lemmas_vienādas = false;
				}
				if (visas_lemmas_vienādas) unambiguous_lemma++;
				
				if (mainwf.getValue(AttributeNames.i_PartOfSpeech).equalsIgnoreCase(etalonaAV.getValue(AttributeNames.i_PartOfSpeech)))
					first_pos_correct++;
				else {
                    boolean found_pos = false;
                    for (Wordform wf : w.wordforms) {
                        if (wf.getValue(AttributeNames.i_PartOfSpeech).equalsIgnoreCase(etalonaAV.getValue(AttributeNames.i_PartOfSpeech)))
                            found_pos = true;
                    }
                    if (found_pos) any_pos_correct++;
                }
                if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma))
                    first_lemma_correct++;
                else {
                    boolean found_lemma = false;
                    for (Wordform wf : w.wordforms) {
                        if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma)) found_lemma = true;
                    }
                    if (found_lemma) {
                    	any_lemma_correct++;
					} else {
						String mistake_description = String.format("Lemma korpusā %s, analizatoram %s (%s - %s)", e.lemma, mainwf.getValue(AttributeNames.i_Lemma), mainwf.getToken(), e.id);
						mistakes.add(mistake_description);
						mistakes_by_lemma.put(e.lemma, mistake_description);
					}
                }
				if (mainwf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && mainwf.getTag().equalsIgnoreCase(e.tag))
					first_all_correct++;
				if (mainwf.isMatchingWeak(tags.fromTag(e.tag)))
					first_compatible++;
				else {
					boolean found_all_correct = false;
					boolean found_compatible = false;
                    boolean first_output = true;
					String output = "";
					for (Wordform wf : w.wordforms) {
						if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(e.lemma) && wf.getTag().equalsIgnoreCase(e.tag)) found_all_correct = true;
                        if (wf.isMatchingWeak(tags.fromTag(e.tag))) found_compatible = true;

                        if (first_output)
                            output += "Varianti:\t";
                        else
                            output += "\t\t\t";
						output += wf.getValue(AttributeNames.i_Lemma) + "\t" + wf.getTag() + "\n";
						first_output = false;
					}
					if (found_all_correct) any_all_correct++;
					if (found_compatible) any_compatible++;
					else {
						wrong++;
						String mistake_description = e.tag+"\t"+e.wordform+"\nKorpusā:\t"+e.lemma+"\t"+e.tag+"\t\t(" + e.id + ")\n"+output;
						mistakes.add(mistake_description);
						mistakes_by_lemma.put(e.lemma, mistake_description);
					}
				}	
			} else {
				not_recognized++;
				mistakes.add("Nav variantu :( \t"+e.wordform+"\t"+e.lemma+"\t"+e.tag+"\t\t"+e.id+"\n");
			}						
		}

		// No AttributeValues.removeAttributesForCorpusTest()
		izeja.println("Tagus salīdzinot neņem vērā transitivitāti, apstākļa vārda tipu, īpašības vārda tipu, prievārdu novietojumu");

        Collections.sort(capitalization_mistakes);
        for (String mistake:capitalization_mistakes){
            izeja.println(mistake);
        }
//		Collections.sort(mistakes);
//		for (String mistake:mistakes){
//			izeja.println(mistake);
//		}
		int limit_differences = 1;
        int singletons = 0;
		for (String key : sortedByDescendingFrequency(mistakes_by_lemma).keySet()) {
            Collection<String> list = mistakes_by_lemma.get(key);
            if (list.size()<limit_differences) {
                singletons++;
                continue;
            }
            izeja.printf("%s : %d atšķirības\n", key, list.size());
            Set<String> sortedlist = new TreeSet<String>();
            sortedlist.addAll(list);
            for (String mistake : sortedlist)
                izeja.println(mistake);
        }
		if (singletons > 0) izeja.printf(".... un %d izolētas atšķirības\n", singletons);

		LinkedHashMap<String, Integer> most_common_oov = new LinkedHashMap<>();
		oov_frequency.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(x -> most_common_oov.put(x.getKey(), x.getValue()));

		int top_oov_words = 100;
		izeja.printf("%d biežākie vārdi, kas nav leksikonā:", top_oov_words);
		most_common_oov.entrySet()
				.stream()
				.limit(top_oov_words)
				.forEachOrdered(e -> izeja.printf("\t%s : %d\n", e.getKey(), e.getValue()));
		
		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		izeja.flush();
		
		System.out.printf("Etalona pārbaude: pagāja %d ms\n%d pieprasījumi sekundē\n", starpība, etaloni.size()*1000/starpība);
		System.out.printf("\nAnalīzes rezultāti: (pirmais/kandidāti)\n");
		System.out.printf("\tViss pareizi:\t%4.1f%% / %4.1f%%\t%6d\t%6d\tpaliek %5d\n", first_all_correct*100.0/etaloni.size(), (first_all_correct+any_all_correct)*100.0/etaloni.size(), first_all_correct, any_all_correct, etaloni.size()-first_all_correct-any_all_correct);
        System.out.printf("\tLemma pareiza:\t%4.1f%% / %4.1f%%\t%6d\t%6d\tpaliek %5d\n", first_lemma_correct*100.0/etaloni.size(), (first_lemma_correct+any_lemma_correct)*100.0/etaloni.size(), first_lemma_correct, any_lemma_correct, etaloni.size()-first_lemma_correct-any_lemma_correct);
		System.out.printf("\tTags der:    \t%4.1f%% / %4.1f%%\t%6d\t%6d\tpaliek %5d\n", first_compatible*100.0/etaloni.size(), (first_compatible+any_compatible)*100.0/etaloni.size(), first_compatible, any_compatible, etaloni.size()-first_compatible-any_compatible);
		System.out.printf("\tVarianti neder:\t%4.1f%%\t%6d\n", wrong*100.0/etaloni.size(), wrong);
		System.out.printf("\tNeatpazīti:    \t%4.1f%%\t%6d\n", not_recognized*100.0/etaloni.size(), not_recognized);
        System.out.printf("\tPareizs POS:\t%4.1f%% / %4.1f%%\t%6d\t%6d\tpaliek %5d\n", first_pos_correct*100.0/etaloni.size(), (any_pos_correct+first_pos_correct)*100.0/etaloni.size(), first_pos_correct, any_pos_correct, etaloni.size()-first_pos_correct-any_pos_correct);
//		System.out.printf("\nEtalons uz 2.2.1 relīzi: Viss pareizi 78.5%%/90.2%%, Lemma pareiza 94.6%%/99.1%%, Tags der 83.0%%/98.9%%, Nav vārdnīcā 3.4%%\n");
//		System.out.printf("\nEtalons uz 2.2.7 relīzi: Viss pareizi 81.0%%/96.0%%, Lemma pareiza 95.4%%/99.6%%, Tags der 83.4%%/99.2%%, Nav vārdnīcā 2.2%%\n");
		System.out.printf("\nEtalons uz 2.2.8 relīzi: Viss pareizi 81.2%%/96.1%%, Lemma pareiza 95.4%%/99.6%%, Tags der 83.5%%/99.2%%, Nav vārdnīcā 2.1%%\n");
		
		System.out.printf("\nStatistika:\n");
		System.out.printf("\tKopā vārdlietojumi:\t\t\t%6d\n", etaloni.size());
		System.out.printf("\tNav vārdnīcā:\t\t%4.1f%%\t%6d\n", oov*100.0/etaloni.size(), oov);
		System.out.printf("\tDaudznozīmīgi:\t\t%4.1f%%\t%6d\n", ambiguous*100.0/(unambiguous+ambiguous), ambiguous);
		System.out.printf("\tViennozīmīgi:\t\t%4.1f%%\t%6d\n", unambiguous*100.0/(unambiguous+ambiguous), unambiguous);
		System.out.printf("\tViennozīmīga pamatforma:\t\t%4.1f%%\t%6d\n", unambiguous_lemma*100.0/(unambiguous+ambiguous), unambiguous_lemma);
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
