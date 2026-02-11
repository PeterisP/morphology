/*******************************************************************************
 * Copyright 2012, 2014 Institute of Mathematics and Computer Science, University of Latvia
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
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;
import lv.semti.morphology.lexicon.Paradigm;

public class VardadienuImport {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF-8"));
		
		Analyzer analizators = new Analyzer("dist/Lexicon.xml", false);
		analizators.guessNouns = true;
		analizators.guessParticiples = false;
		analizators.guessVerbs = false;
		analizators.guessAdjectives = false;
		analizators.enableDiminutive = false;
		analizators.enablePrefixes = false;
		analizators.enableGuessing = false;
		analizators.searchCompoundWords = false;
		analizators.guessInflexibleNouns = true;
		analizators.setCacheSize(0);

		String source = "VVC paplašinātais vārdadienu saraksts 2014-10-31";
		
		String deletable_source = "Onomastica - cilvēkvārdi";
		Set<Lexeme> deletables = new HashSet<Lexeme>();
		
		BufferedReader ieeja;
		String vārds;
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("dist/vardadienas.txt"), "UTF-8"));
		
		int i = 0;
		while ((vārds = ieeja.readLine()) != null) {
			i++;
			//if (i>10) break;
			
			Word w = analizators.analyzeLemma(vārds);
			
			if (irLeksikonā(w)) {
				for (Wordform wf : w.wordforms)
					if (deletable_source.equalsIgnoreCase(wf.getValue(AttributeNames.i_Source))) 
						deletables.add(wf.lexeme);
					//izeja.printf("Vārds '%s' jau ir leksikonā - %s\n", w.getToken(), w.getBestWordform().getValue(AttributeNames.i_Source));
					//w.describe(System.out);
			} 
			
			if (true) { // šeit pofig par leksikonu, taisīsim dublējošas leksēmas - ja vizbulīte ir jau leksikonā, tad personvādu liksim anyway				
				w = analizators.guessByEnding(vārds, vārds);
				izmestNepareizāsParadigmas(w);
								
				if (w.wordforms.size() == 0) {					
					if (vārds.endsWith("o") || vārds.endsWith("ē") || vārds.endsWith("ū") || vārds.endsWith("ī") || vārds.endsWith("i") || vārds.endsWith("u")) {
						Lexeme jaunais = analizators.createLexeme(vārds, analizators.endingByID( 111), source); // Nelokāmie lietvārdi
						jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
						jaunais.addAttribute("Īpašvārda veids", "Priekšvārds");
						jaunais.addAttribute(AttributeNames.i_Lemma, vārds);
						//izeja.printf("Pielikām leksikonam vārdu '%s' - nelokāms\n", w.getToken());
						//jaunais.describe(izeja);
						//analizators.analyze(vārds).Aprakstīt(izeja);						
					} else {
						izeja.println("Neuzminējās varianti '" + w.getToken() +"'!");					
					}
				} else if (w.wordforms.size() == 1) {					
					Lexeme jaunais = analizators.createLexeme(vārds, w.wordforms.get(0).getEnding(),source);
					jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
					jaunais.addAttribute("Īpašvārda veids", "Priekšvārds");
					jaunais.addAttribute(AttributeNames.i_Lemma, vārds);
					if (w.wordforms.get(0).isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Plural)) {
						jaunais.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
					}
					//izeja.printf("Pielikām leksikonam vārdu '%s' - %s\n", w.getToken(), w.wordforms.get(0).getValue(AttributeNames.i_Gender));
					//w.print(izeja);
					//jaunais.describe(izeja);
					//analizators.analyze(vārds).Aprakstīt(izeja);
				} else {
					izeja.println("tipa dereetu pielikt leksikonam vārdu '" + w.getToken() +"' bet ir vairāki varianti");
					w.print(izeja);
				}
			}
			izeja.flush();
		}
					
		for (Paradigm p : analizators.paradigms)
			for (Lexeme l : p.lexemes)
				if (deletable_source.equalsIgnoreCase(l.getValue(AttributeNames.i_Source)))
					if (deletables.contains(l)) {
						// System.out.println("Var droši dzēst "+l.getValue(AttributeNames.i_Lemma));
					} else 
						System.out.println("A kas ar "+l.getValue(AttributeNames.i_Lemma) + "?");
		
		izeja.flush();
		izeja.close();
		ieeja.close();
		analizators.toXML_sub("Lexicon_firstnames.xml", source);
	}

	private static void izmestNepareizāsParadigmas(Word w) {
		LinkedList<Wordform> izmetamie = new LinkedList<Wordform>();
		for (Wordform wf : w.wordforms) {
			if (wf.getValue(AttributeNames.i_ParadigmID).equals("4") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("5") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("8") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("10") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("11") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("12") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("31") ||
				!wf.isMatchingWeak(AttributeNames.i_Case, AttributeNames.v_Nominative) ||
				!wf.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Singular)
				) {
					izmetamie.add(wf);
			}
			
		}
		for (Wordform izmetamais : izmetamie)
			w.wordforms.remove(izmetamais);
	}
	
	private static boolean irLeksikonā(Word w) {
		for (Wordform wf : w.wordforms) {
			if (wf.isMatchingWeak(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun) &&
				!"Imports no Tezaura SV 2014-08-02".equalsIgnoreCase(wf.getValue(AttributeNames.i_Source)) &&
				!"Valērija leksikons".equalsIgnoreCase(wf.getValue(AttributeNames.i_Source)) &&
				!"LĢIS".equalsIgnoreCase(wf.getValue(AttributeNames.i_Source)))
				return true;
		}
		return false;
	}

}
