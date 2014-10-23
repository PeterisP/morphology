/*******************************************************************************
 * Copyright 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
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

import java.util.ArrayList;
import java.util.Random;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.lexicon.Lexeme;
import lv.semti.morphology.lexicon.Lexicon;
import lv.semti.morphology.lexicon.Paradigm;

public class LeksikonFiltrs {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Analyzer analizators = new Analyzer("dist/Lexicon.xml",false);
		analizators.setCacheSize(0);
		
		int [] limits = {35000, 25000, 15000, 5000};
		
		for (int limit:limits) {
			filterLexicon(analizators, limit);					
			analizators.toXML(String.format("Lexicon_valerijs_%d.xml", limit));
		}
	}
	
	private static void filterLexicon(Lexicon lexicon, int limit) {
		int count = 0;
		ArrayList<Lexeme> allLexemes = new ArrayList<Lexeme>();
		for (Paradigm paradigm : lexicon.paradigms) {
			count += paradigm.numberOfLexemes();
			allLexemes.addAll(paradigm.lexemes);
		}
		Random rand = new Random();
		
		System.out.println(count);
		for (;count > limit;count--) {
			int randomNr = rand.nextInt(allLexemes.size());
			Lexeme randomLexeme = allLexemes.get( randomNr );
			randomLexeme.getParadigm().removeLexeme(randomLexeme);
			allLexemes.remove(randomNr);
		}
		
	}

}
