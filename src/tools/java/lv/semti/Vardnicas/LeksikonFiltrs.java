package lv.semti.Vardnicas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
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
