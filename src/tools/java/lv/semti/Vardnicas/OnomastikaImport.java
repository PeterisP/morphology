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
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.Lexeme;

public class OnomastikaImport {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF-8"));
		
		Analyzer analizators = new Analyzer("dist/Lexicon.xml");
		analizators.guessNouns = true;
		analizators.guessParticibles = false;
		analizators.guessVerbs = false;
		analizators.guessAdjectives = false;
		analizators.enableDiminutive = false;
		analizators.enablePrefixes = false;
		analizators.enableGuessing = false;
		analizators.meklētsalikteņus = false;
		analizators.guessInflexibleNouns = true;
		analizators.setCacheSize(0);

		String source = "Onomastica - nepārbaudīts visu vārdu imports";
		
		BufferedReader ieeja;
		String vārds;
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("/Users/pet/Dropbox/Resursi/Onomastika jaunā/bezatstarpju visi.txt"), "UTF-8"));
		
		int i = 0;
		while ((vārds = ieeja.readLine()) != null) {
			i++;
			//if (i>10000) break;
			
			vārds = vārds.trim().toLowerCase();
			String Vārds = vārds.substring(0, 1).toUpperCase() + vārds.substring(1,vārds.length());
			Word w = analizators.analyzeLemma(vārds);
			
			if (irLeksikonā(w)) {
				//izeja.println("Vārds '" + w.getToken() + "' jau ir leksikonā!");
			} else {
				
				w = analizators.guessByEnding(vārds);

				izmestNepareizāsParadigmas(w);
				
				AttributeValues filtrs_vsk = new AttributeValues();
				filtrs_vsk.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);

				AttributeValues filtrs_3 = new AttributeValues();
				filtrs_3.addAttribute(AttributeNames.i_ParadigmID, "3");
				
				AttributeValues filtrs_1 = new AttributeValues();
				filtrs_1.addAttribute(AttributeNames.i_ParadigmID, "1");
				
				boolean irVienskaitlis = false;
				for (Wordform wf : w.wordforms) {
					if (wf.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Singular)) irVienskaitlis = true;
				}
				if (irVienskaitlis)	w.filterByAttributes(filtrs_vsk);
				else {
					if (vārds.endsWith("ši") || vārds.endsWith("ži") || vārds.endsWith("či") || vārds.endsWith("šļi") || vārds.endsWith("žļi") || vārds.endsWith("ži") ||
							vārds.endsWith("ņi") || vārds.endsWith("pji") || vārds.endsWith("pji") || vārds.endsWith("bji") || vārds.endsWith("mji") || vārds.endsWith("vji"))
						w.filterByAttributes(filtrs_3);
					else if (vārds.endsWith("i"))
						w.filterByAttributes(filtrs_1);
				}
				
				if (w.wordforms.size() == 0) {					
					if (vārds.endsWith("o") || vārds.endsWith("ē")) {
						Lexeme jaunais = analizators.createLexeme(vārds, 111, source); // Nelokāmie lietvārdi
						jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
						jaunais.addAttribute(AttributeNames.i_Lemma, Vārds);
						//izeja.println("Pielikām leksikonam vārdu '" + w.getVārds() +"'");
						//jaunais.describe(izeja);
						//analizators.analyze(vārds).Aprakstīt(izeja);						
					} else {
						izeja.println("Neuzminējās varianti '" + w.getToken() +"'!");					
					}
				} else if (w.wordforms.size() == 1) {					
					Lexeme jaunais = analizators.createLexeme(vārds, w.wordforms.get(0).getEnding().getID(),source);
					jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
					jaunais.addAttribute(AttributeNames.i_Lemma, Vārds);
					if (w.wordforms.get(0).isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Plural)) {
						jaunais.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
					}
					//izeja.println("Pielikām leksikonam vārdu '" + w.getToken() +"'");
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
					
		izeja.flush();
		analizators.toXML_sub("Lexicon_onomastica.xml", source);
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
				!wf.isMatchingWeak(AttributeNames.i_Case, AttributeNames.v_Nominative)
				) {
					izmetamie.add(wf);
			}
			
		}
		for (Wordform izmetamais : izmetamie)
			w.wordforms.remove(izmetamais);
	}
	
	private static void izmestDaudzskaitļus(Word w) {
		LinkedList<Wordform> izmetamie = new LinkedList<Wordform>();
		for (Wordform wf : w.wordforms) {
			if ( !wf.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Singular)	) {
					izmetamie.add(wf);
			}
			
		}
		for (Wordform izmetamais : izmetamie)
			w.wordforms.remove(izmetamais);
	}

	private static boolean irLeksikonā(Word w) {
		for (Wordform wf : w.wordforms) {
			if (wf.isMatchingWeak(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun) || wf.isMatchingWeak(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective))
				return true;
		}
		return false;
	}

}
