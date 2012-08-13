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
package lv.semti.morphology.analyzer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.*;

public class Analyzer extends Lexicon {

	public boolean enablePrefixes = true;
	public boolean meklētsalikteņus = false;
	public boolean enableGuessing = false;
	public boolean enableDiminutive = true;
	public boolean enableVocative = false;
	public boolean guessNouns = true;
    public boolean guessVerbs = true;
    public boolean guessParticibles = true;
    public boolean guessAdjectives = true;
    public boolean enableAllGuesses = false;
	public boolean guessInflexibleNouns = false;
	
	private Pattern p_number = Pattern.compile("(\\d+[\\., ])*\\d+([\\.,][-‐‑‒–—―])?");
	private Pattern p_ordinal = Pattern.compile("\\d+\\.");
	private Pattern p_fractional = Pattern.compile("\\d+[\\\\/]\\d+");
	private Pattern p_abbrev = Pattern.compile("\\w+\\.");
	
	private Cache<String, Word> wordCache = new Cache<String, Word>();

	public Analyzer () throws Exception {
		super();
	}
    
	public Analyzer (String lexiconFileName) throws Exception {
		super(lexiconFileName);
	}
	
	public Analyzer (InputStream lexiconStream) throws Exception {
		super(lexiconStream);
	}
    
	/* TODO - salikteņu minēšana jāuzaisa 
	private boolean DerSalikteņaSākumam(Ending ending) {
		if (ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Noun))
			return ending.isMatchingStrong(AttributeNames.i_Case,AttributeNames.v_Genitive);

		return false;
	} */
	
	/**
	 * @param String lexiconFileName - main lexicon file name 
	 * @param boolean useAuxiliaryLexicons
	 */
	public Analyzer(String lexiconFileName, boolean useAuxiliaryLexicons) throws Exception{
		super(lexiconFileName, useAuxiliaryLexicons);
	}

	public void defaultSettings(){
		enablePrefixes = true;
		meklētsalikteņus = false;
		enableGuessing = false;
		enableDiminutive = true;
		enableVocative = false;
		guessNouns = true;
	    guessVerbs = true;
	    guessParticibles = true;
	    guessAdjectives = true;
	    enableAllGuesses = false;
		guessInflexibleNouns = false;
	}

	/**
	 * Veic morfoloģisko analīzi
	 *
	 */
	public Word analyze(String word) {
		word = word.trim();
		if (!word.equals(word.toLowerCase().trim())) {
			Word rezults = new Word(word);
			Word lowercase = analyzeLowercase(word.toLowerCase().trim(), word.matches("\\p{Lu}.*"));
			for (Wordform vārdforma : lowercase.wordforms) {
				vārdforma.setToken(word.trim());
				rezults.addWordform(vārdforma);
			}
			return rezults;
		} else return analyzeLowercase(word, false);
	}
	
	private Word analyzeLowercase(String word, boolean properName) {
		Word cacheWord = wordCache.get(word);
		if (cacheWord != null) return (Word) cacheWord.clone();		
		
		Word rezultāts = new Word(word);
		
		for (Ending ending : getAllEndings().matchedEndings(word)) {
			ArrayList<Variants> celmi = Mijas.mijuVarianti(ending.stem(word), ending.getMija(), properName);

			for (Variants celms : celmi) {
				ArrayList<Lexeme> leksēmas = ending.getEndingLexemes(celms.celms);
				if (leksēmas != null)
					for (Lexeme leksēma : leksēmas) {
						Wordform variants = new Wordform(word, leksēma, ending);
						variants.addAttributes(celms);
						variants.addAttribute(AttributeNames.i_Guess, AttributeNames.v_NoGuess);
						rezultāts.addWordform(variants);
					}

				if (leksēmas == null && enableDiminutive) 
					guessDeminutive(word, rezultāts, ending, celms);
			}
		}

		filterUnacceptable(rezultāts); // izmetam tos variantus, kas nav īsti pieļaujami - vienskaitliniekus daudzskaitlī, vokatīvus ja tos negrib

		if (!rezultāts.isRecognized()) {
			if (p_number.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				wf.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Number);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				rezultāts.addWordform(wf);
				return rezultāts;
			}
			if (p_fractional.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				wf.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Number);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				rezultāts.addWordform(wf);
				return rezultāts;
			}
			if (p_ordinal.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				wf.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Ordinal);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				rezultāts.addWordform(wf);
				return rezultāts;
			}
			if (p_abbrev.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Abbreviation);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				rezultāts.addWordform(wf);
				return rezultāts;
			}
		}
		
		if (!rezultāts.isRecognized() && enablePrefixes )
			rezultāts = guessByPrefix(word);
/*
		if (!rezultāts.isRecognized() && meklētsalikteņus )
			for (Ending ending : allEndings())
				if (DerSalikteņaSākumam(ending)) {
					for (ArrayList<Leksēma> pirmiecelmi : galotne.getVārdgrupa().leksēmaspēcVārda.get(galotne.saknesNr-1).values()) {
						//FIXME - salikteņu meklēšana nav te ielikta
					}
				} */

		if (!rezultāts.isRecognized() && enableGuessing )
			rezultāts = guessByEnding(word);

		/*for (Wordform variants : rezultāts.wordforms) {
			variants.addAttribute(AttributeNames.i_Tag, MarkupConverter.toKamolsMarkup(variants));
			if (variants.lexeme != null) {
				String locījumuDemo = "";
				for (Wordform locījums : generateInflections(variants.lexeme)) {
					locījumuDemo = locījumuDemo + locījums.getValue(AttributeNames.i_Word) + " " + locījums.getValue(AttributeNames.i_Case) + "\n";
				}
				variants.pieliktĪpašību("LocījumuDemo", locījumuDemo);
				//TODO - kautko jau ar to visu vajag; bet bez īpašas vajadzības tas ir performancehog
			}
		} */

		wordCache.put(word, (Word) rezultāts.clone());
		return rezultāts;
	}

	private void guessDeminutive(String word, Word rezultāts, Ending ending,
			Variants celms) {
		switch (ending.getParadigm().getID()) {
		// FIXME - neforšs hack, paļaujamies uz 'maģiskiem' vārdgrupu numuriem
		case 3: // 2. deklinācijas -is
		case 9:
		case 10: // 5. deklinācijas -e
			if (celms.celms.endsWith("īt")) {
				ArrayList<Lexeme> deminutīvleksēmas = ending.getEndingLexemes(celms.celms.substring(0,celms.celms.length()-2));
				if (deminutīvleksēmas != null)
					for (Lexeme leksēma : deminutīvleksēmas) {
						Wordform variants = new Wordform(word, leksēma, ending);
						variants.addAttributes(celms); // ?
						variants.addAttribute(AttributeNames.i_Deminutive, "-īt-");
						variants.addAttribute(AttributeNames.i_Source,"pamazināmo formu atvasināšana");
						variants.addAttribute(AttributeNames.i_SourceLemma, leksēma.getValue(AttributeNames.i_Lemma));
						variants.addAttribute(AttributeNames.i_Guess, AttributeNames.v_Deminutive);
						variants.addAttribute(AttributeNames.i_Lemma, leksēma.getStem(0) + "īt" + ending.getLemmaEnding().getEnding());
						rezultāts.addWordform(variants);										
					}
			}
			break;
		case 2: // 1. deklinācijas -š
		case 7: // 4. deklinācijas -a						
			if (celms.celms.endsWith("iņ")) {
				String pamatforma = celms.celms.substring(0,celms.celms.length()-2);
				String pamatforma2 = pamatforma;
				if (pamatforma.endsWith("dz")) pamatforma2 = pamatforma.substring(0,pamatforma.length()-2)+"g";
				if (pamatforma.endsWith("c")) pamatforma2 = pamatforma.substring(0,pamatforma.length()-1)+"k";

				ArrayList<Lexeme> deminutīvleksēmas = ending.getEndingLexemes(pamatforma2);

				if (ending.getParadigm().getID() == 2) {  // mainās deklinācija galds -> galdiņš, tāpēc īpaši
					deminutīvleksēmas = endingByID(1).getEndingLexemes(pamatforma2);
					//FIXME - nedroša atsauce uz galotni nr. 1

					if (pamatforma.endsWith("l")) pamatforma2 = pamatforma.substring(0,pamatforma.length()-1)+"ļ";
					ArrayList<Lexeme> deminutīvleksēmas2 = ending.getEndingLexemes(pamatforma2);
						// bet ir arī ceļš->celiņš, kur paliek 2. deklinācija
					if (deminutīvleksēmas == null) deminutīvleksēmas = deminutīvleksēmas2;
					else if (deminutīvleksēmas2 != null) deminutīvleksēmas.addAll(deminutīvleksēmas2);
				}
				if ((pamatforma.endsWith("ļ") && ending.getParadigm().getID() == 2) || pamatforma.endsWith("k") || pamatforma.endsWith("g"))
					deminutīvleksēmas = null; // nepieļaujam nepareizās mijas 'ceļiņš', 'pīrāgiņš', 'druskiņa'

				if (deminutīvleksēmas != null)
					for (Lexeme leksēma : deminutīvleksēmas) {
						Wordform variants = new Wordform(word, leksēma, ending);
						variants.addAttributes(celms); // ?
						variants.addAttribute(AttributeNames.i_Deminutive, "-iņ-");
						variants.addAttribute(AttributeNames.i_Source,"pamazināmo formu atvasināšana");
						variants.addAttribute(AttributeNames.i_SourceLemma, leksēma.getValue(AttributeNames.i_Lemma));
						variants.addAttribute(AttributeNames.i_Guess, AttributeNames.v_Deminutive);
						variants.addAttribute(AttributeNames.i_Lemma, pamatforma + "iņ" + ending.getLemmaEnding().getEnding());
						
						rezultāts.addWordform(variants);										
					}
			}
		}
	}

	private void filterUnacceptable(Word rezultāts) {
		LinkedList<Wordform> izmetamie = new LinkedList<Wordform>();
		for (Wordform variants : rezultāts.wordforms)
			if (!enableVocative && rezultāts.wordformsCount()>1 && variants.isMatchingStrong(AttributeNames.i_Case,AttributeNames.v_Vocative))
				izmetamie.add(variants); 			// ja negribam vokatīvus, un ir arī citi iespējami varianti, tad šādu variantu nepieliekam.

		for (Wordform variants : rezultāts.wordforms)
			if (variants.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum) &&
					!variants.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Plural))
				izmetamie.add(variants);

		for (Wordform variants : rezultāts.wordforms)
			if (variants.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum) &&
					!variants.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Singular))
				izmetamie.add(variants);

		for (Wordform variants : rezultāts.wordforms)
			if (variants.isMatchingStrong(AttributeNames.i_CaseSpecial, AttributeNames.v_InflexibleGenitive) &&
					!variants.isMatchingWeak(AttributeNames.i_Case, AttributeNames.v_Genitive))
				izmetamie.add(variants);

		for (Wordform izmetamais : izmetamie)
			rezultāts.wordforms.remove(izmetamais);
	}

	private Word guessByPrefix(String word) {
		Word rezultāts = new Word(word);
		if (word.contains(" ")) return rezultāts;
		
		for (String priedēklis : prefixes)
			if (word.startsWith(priedēklis)) {
				Word bezpriedēkļa = analyzeLowercase(word.substring(priedēklis.length()), false);
				for (Wordform variants : bezpriedēkļa.wordforms)
					if (variants.getEnding() != null && variants.getEnding().getParadigm() != null && variants.getEnding().getParadigm().getValue(AttributeNames.i_Konjugaacija) != null) { // Tikai no verbiem atvasinātās klases 
						variants.setToken(word);
						variants.addAttribute(AttributeNames.i_Source,"priedēkļu atvasināšana");
						variants.addAttribute(AttributeNames.i_Prefix, priedēklis);
						variants.addAttribute(AttributeNames.i_SourceLemma, variants.getValue(AttributeNames.i_Lemma));
						variants.addAttribute(AttributeNames.i_Lemma,priedēklis+variants.getValue(AttributeNames.i_Lemma));
						variants.addAttribute(AttributeNames.i_Guess, AttributeNames.v_Prefix);
						variants.addAttribute(AttributeNames.i_Noliegums,priedēklis.equals("ne") ? AttributeNames.v_Yes : AttributeNames.v_No);

						rezultāts.wordforms.add(variants);
					}
			}
		return rezultāts;
	}

	public void reanalyze(Word vārds) {
		Word jaunais = analyze(vārds.getToken());
		vārds.wordforms.clear();
		for (Wordform vārdforma : jaunais.wordforms)
			vārds.wordforms.add(vārdforma);
		vārds.dataHasChanged();
	}

	public Word guessByEnding(String word) {
		Word rezultāts = new Word(word);

		for (int i=word.length()-2; i>=0; i--) { // TODO - duma heiristika, kas vērtē tīri pēc galotņu garuma; vajag pēc statistikas
			for (Ending ending : getAllEndings().matchedEndings(word))
				if (ending.getEnding().length()==i) {
					if (ending.getParadigm().getName().equals("Hardcoded"))
						continue; // Hardcoded vārdgrupa minēšanai nav aktuāla

					ArrayList<Variants> celmi = Mijas.mijuVarianti(ending.stem(word), ending.getMija(), false);
					if (celmi.size() == 0) continue; // acīmredzot neder ar miju, ejam uz nākamo galotni.
					String celms = celmi.get(0).celms;

					Wordform variants = new Wordform(word, null, ending);
					variants.addAttribute(AttributeNames.i_Source,"minējums pēc galotnes");
					variants.addAttribute(AttributeNames.i_Guess, AttributeNames.v_Ending);

					Ending pamatforma = ending.getLemmaEnding();
					if (ending.getParadigm().getID() == 4 && !(celms.endsWith("n") || celms.endsWith("s")))
						continue; // der tikai -ss un -ns, kā 'mēness' un 'ūdens'
					if (ending.getParadigm().getID() == 5 && !celms.endsWith("sun"))
						continue; // der tikai -suns 
					if ((ending.getParadigm().isMatchingStrong(AttributeNames.i_Declension, "1") || ending.getParadigm().isMatchingStrong(AttributeNames.i_Declension, "6")) 
							&& celms.endsWith("a"))
						continue; // -as nav 1. dekl vārds
					//TODO te var vēl heiristikas salikt, lai uzlabotu minēšanu - ne katrs burts var būt darbībasvārdam beigās utml

					// FIXME ko ar pārējiem variantiem?? un ko ja nav variantu?
					if (pamatforma != null)
						variants.addAttribute(AttributeNames.i_Lemma,
							 celms + pamatforma.getEnding());

					if (  ((this.guessNouns && ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Noun) &&							
                            (enableVocative || !variants.isMatchingStrong(AttributeNames.i_Case,AttributeNames.v_Vocative)) &&
                            (guessInflexibleNouns || !variants.isMatchingStrong(AttributeNames.i_Declension,AttributeNames.v_NA))
                            ) ||
							(this.guessVerbs && ending.getParadigm().isMatchingWeak(AttributeNames.i_PartOfSpeech,AttributeNames.v_Verb)) ||
                            (this.guessAdjectives && ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Adjective)) ||
                            (this.guessParticibles && variants.isMatchingStrong(AttributeNames.i_Izteiksme,AttributeNames.v_Participle))) 
                      && (i>0 || variants.isMatchingStrong(AttributeNames.i_Declension,AttributeNames.v_NA)) ) // ja galotnes nav, tad vai nu nelokāms lietvārds vai neatpazīstam. Lai nav verbu bezgalotņu formas minējumos, kas parasti nav pareizās.
                            	{
									if (ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Noun) && 
											variants.isMatchingStrong(AttributeNames.i_Declension,AttributeNames.v_NA)) {
										char last = celms.charAt(celms.length()-1);
 										if (!(last=='ā' || last == 'e' || last == 'ē' || last == 'i' || last == 'ī' || last == 'o' || last == 'ū' || celms.endsWith("as"))) {  // uzskatam, ka 'godīgi' nelokāmie lietvārdi beidzas tikai ar šiem - klasiski nelokāmie, un lietuviešu Arvydas
 											variants.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
 											if (!Character.isDigit(last)) {
 												variants.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Foreign);
												//Pieņemam, ka vārdi svešvalodā - 'crawling' utml.
											} 
										}
									}
									rezultāts.wordforms.add(variants);
                            	}

				}
			if (rezultāts.isRecognized() && !enableAllGuesses) break;
			// FIXME - šo te vajag aizstāt ar kādu heiristiku, kas atrastu, piemēram, ticamākos lietvārdvariantus, ticamākos īpašībasvārdagadījumus utml.
		}
		return rezultāts;
	}

	public Word analyzeLemma(String word) {
		// Meklēt variantus, pieņemot, ka ir iedota tieši vārda pamatforma
		// FIXME - būtu jāapdomā, ko darīt, ja ir iedots substantivizēta darbības vārda vienskaitļa nominatīvs
		//  ^^ itkā tagad jāiet, bet jātestē
		// FIXME - daudzskaitlinieki?
		Word rezultāts = new Word(word);
		Word varianti = analyze(word);

		for (Wordform vārdforma : varianti.wordforms) {			
			Ending ending = vārdforma.getEnding();
			
			AttributeValues filter = new AttributeValues();
			filter.addAttribute(AttributeNames.i_Lemma, word);
			filter.addAttribute(AttributeNames.i_Lemma, AttributeNames.v_Singular);
			
			if ( (ending != null && ending.getLemmaEnding() == ending) ||
				(vārdforma.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(word) && 
						(vārdforma.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum) || vārdforma.isMatchingStrong(AttributeNames.i_CaseSpecial, AttributeNames.v_InflexibleGenitive) )) )
				rezultāts.addWordform(vārdforma);
		}

		return rezultāts;
	}

	public void setCacheSize (int maxSize) {
		wordCache.setSize(maxSize);
	}
	
	public void clearCache () {
		wordCache.clear();
	}

	public ArrayList<Wordform> generateInflections(String lemma) {
		return generateInflections(lemma, false);
	}
	
	public ArrayList<Wordform> generateInflections(String lemma, boolean filter) {
		Word possibilities = this.analyze(lemma);
		
		if (filter) {
			ArrayList<Wordform> unsuitable = new ArrayList<Wordform>();
			for (Wordform wf : possibilities.wordforms) {
				boolean suitable = false;
				if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun)) suitable = true;
				if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective) && wf.isMatchingStrong(AttributeNames.i_Definiteness, AttributeNames.v_Definite)) suitable = true;
				if (!suitable) unsuitable.add(wf);
			}
			possibilities.wordforms.removeAll(unsuitable);
		}
		
		ArrayList<Wordform> result = generateInflections_TryLemmas(lemma, possibilities);
		
		// If result is null, it means that all the suggested lemma can be (and was) generated from another lemma - i.e. "Dīcis" from "dīkt"; but not from an existing lexicon lemma
		// We assume that a true lemma was passed by the caller, and we need to generate/guess the wordforms as if the lemma was correct.
		if (result == null)
			result = generateInflections_TryLemmas(lemma, this.guessByEnding(lemma));

		// If guessing didn't work, return an empty list
		if (result == null)
			result = new ArrayList<Wordform>();
		
		return result;
	}

	private ArrayList<Wordform> generateInflections_TryLemmas(String lemma, Word w) {
		for (Wordform wf : w.wordforms) {
			if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma) && !wf.isMatchingStrong(AttributeNames.i_Case, AttributeNames.v_Vocative)) {				
				Lexeme lex = wf.lexeme;
				if (lex == null || !lex.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma)) {
					lex = this.createLexeme(lemma, wf.getEnding().getID(), "generateInflections");
					if (lemma.matches("\\p{Lu}.*"))
						lex.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun); //FIXME - hack personvārdu 'Valdis' utml locīšanai
				}
				return generateInflections(lex);
			}
			if ( (lemma.endsWith("ais") && lemma.equalsIgnoreCase(wf.getValue(AttributeNames.i_Lemma).substring(0, wf.getValue(AttributeNames.i_Lemma).length()-1)+"ais")) ||
				 (lemma.endsWith("ā") && wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma.substring(0, lemma.length()-1)+"a")) ) {
				// Exception for adjective-based surnames "Lielais", "Platais" etc
				Lexeme lex = wf.lexeme;
				if ((lex == null && lemma.endsWith("ais")) || (lex != null && !lex.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma))) {
					lex = this.createLexeme(lemma, wf.getEnding().getID(), "generateInflections");
					if (lemma.matches("\\p{Lu}.*"))
						lex.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun); //FIXME - hack personvārdu 'Valdis' utml locīšanai
				}
				if (lex == null) continue;
				ArrayList<Wordform> result = new ArrayList<Wordform>();
				for (Wordform wf2 : generateInflections(lex)) {
					if (wf2.isMatchingStrong(AttributeNames.i_Definiteness, AttributeNames.v_Definite) && wf2.isMatchingStrong(AttributeNames.i_Degree, AttributeNames.v_Positive) && wf2.isMatchingWeak(AttributeNames.i_Gender, wf.getValue(AttributeNames.i_Gender))) {
						result.add(wf2);
					}
				}
				return result;
			}
		}
		return null;
	}
	
	private ArrayList<Wordform> generateInflections(Lexeme lexeme)
	{
		String trešāSakne = null, vārds;
		//Vārds rezultāts = new Vārds(leksēma.īpašības.Īpašība(IpasibuNosaukumi.i_Pamatforma));
		ArrayList <Wordform> locījumi =  new ArrayList<Wordform>(1);

		//priekš 1. konj nākotnes mijas nepieciešams zināt 3. sakni
		if (lexeme.getParadigm().getStems() == 3) {
			trešāSakne = lexeme.getStem(2);
		}

		for (Ending ending : lexeme.getParadigm().endings){
			if ( ending.getValue(AttributeNames.i_PartOfSpeech)==null ||
					ending.getValue(AttributeNames.i_PartOfSpeech).equals(lexeme.getValue(AttributeNames.i_PartOfSpeech)) ||
					lexeme.getValue(AttributeNames.i_PartOfSpeech) == null) {
				
				boolean vispārākāPak = ending.isMatchingStrong(AttributeNames.i_Definiteness, AttributeNames.v_Definite);
				boolean properName = lexeme.isMatchingStrong(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
				
		    	ArrayList<Variants> celmi = Mijas.MijasLocīšanai(lexeme.getStem(ending.stemID-1), ending.getMija(), trešāSakne, vispārākāPak, properName);

		    	for (Variants celms : celmi){
		    		vārds = celms.celms + ending.getEnding();

		    		Wordform locījums = new Wordform(vārds, lexeme, ending);
					locījums.addAttributes(celms);
					if (locījums.isMatchingWeak(AttributeNames.i_Generate, AttributeNames.v_Yes))
						locījumi.add(locījums);
		    	}
			}
		}
		return locījumi;
	}

}
