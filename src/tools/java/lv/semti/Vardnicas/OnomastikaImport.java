package lv.semti.Vardnicas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;

public class OnomastikaImport {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "windows-1257"));
		
		Analyzer analizators = new Analyzer("D:\\Lingvistika\\java\\Morphology\\src\\main\\resources\\Lexicon.xml");
		analizators.guessNouns = true;
		analizators.guessParticibles = false;
		analizators.guessVerbs = false;
		analizators.guessAdjectives = false;
		analizators.enableDiminutive = false;
		analizators.enablePrefixes = false;
		analizators.enableGuessing = false;
		analizators.meklētsalikteņus = false;
		analizators.guessInflexibeNouns = true;

		BufferedReader ieeja;
		String vārds;
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("D:\\Lingvistika\\Onomastika\\uzvardi (A-Z).txt"), "UTF-8"));
		
		while ((vārds = ieeja.readLine()) != null) {
			vārds = vārds.trim();
			Word w = analizators.analyzeLemma(vārds);
			
			if (irLeksikonā(w)) {
				//izeja.println("Vārds '" + w.getVārds() + "' jau ir leksikonā!");
			} else {
				
				w = analizators.guessByEnding(vārds);

				izmestNepareizāsDzimtes(w);
				
				if (w.wordforms.size() == 0) {					
					if (vārds.endsWith("as") || vārds.endsWith("i") || vārds.endsWith("ī") || vārds.endsWith("u")) {
						Lexeme jaunais = analizators.createLexeme(vārds, 111, "Onomastica - uzvārdi");
						jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
						jaunais.addAttribute(AttributeNames.i_Lemma, vārds);
						//izeja.println("Pielikām leksikonam vārdu '" + w.getVārds() +"'");
						//jaunais.describe(izeja);
						//analizators.analyze(vārds).Aprakstīt(izeja);						
					} else {
						izeja.println("Neuzminējās varianti '" + w.getToken() +"'!");					
					}
				} else if (w.wordforms.size() == 1) {					
					Lexeme jaunais = analizators.createLexeme(vārds, Integer.parseInt(w.wordforms.get(0).getValue(AttributeNames.i_EndingID)),"Onomastica - uzvārdi");
					jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
					jaunais.addAttribute(AttributeNames.i_Lemma, vārds);
					izeja.println("Pielikām leksikonam vārdu '" + w.getToken() +"'");
					//jaunais.describe(izeja);
					//analizators.analyze(vārds).Aprakstīt(izeja);
				} else {
					izeja.println("tipa dereetu pielikt leksikonam vārdu '" + w.getToken() +"' bet ir vairāki varianti");
					w.print(izeja);
				}
			}
		}
					
		izeja.flush();
		analizators.toXML("D:\\Lingvistika\\java\\Morphology\\src\\main\\resources\\Lexicon2.xml");
	}

	private static void izmestNepareizāsDzimtes(Word w) {
		LinkedList<Wordform> izmetamie = new LinkedList<Wordform>();
		for (Wordform wf : w.wordforms) {
			if (wf.getValue(AttributeNames.i_ParadigmID).equals("4") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("5") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("7") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("9") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("11") ||				
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
			if (wf.isMatchingWeak(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun) && wf.isMatchingWeak(AttributeNames.i_NounType, AttributeNames.v_ProperNoun))
				return true;			
		}
		return false;
	}

}
