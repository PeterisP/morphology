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
package lv.semti.morphology.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    public boolean guessParticiples = false;
    public boolean guessAdjectives = true;
    public boolean enableAllGuesses = false;
	public boolean guessInflexibleNouns = false;
	public boolean removeRareWords = true;
	
	private Pattern p_number = Pattern.compile("[\\d\\., ]*\\d+([\\.,][-‐‑‒–—―])?");
	private Pattern p_ordinal = Pattern.compile("\\d+\\.");
	private Pattern p_fractional = Pattern.compile("\\d+[\\\\/]\\d+");
	private Pattern p_abbrev = Pattern.compile("\\w+\\.");
	private Pattern p_acronym = Pattern.compile("(\\p{Lu}){2,5}"); // all caps, repeated 2-5 times
	private Pattern p_url = Pattern.compile("[\\.\\w]+\\.(lv|com|org)");
		
	private Cache<String, Word> wordCache = new Cache<String, Word>();

	public Trie automats;
		
	/**
	 * Construct the morphological analyzer object by loading the lexicon from either the default location, a specified file name or an inputstream.
	 * @throws Exception
	 */
	public Analyzer () throws Exception {
		super();
		loadExceptionFile();
	}
	public Analyzer (boolean useAuxiliaryLexicons) throws Exception {
		super(useAuxiliaryLexicons);
		loadExceptionFile();
	}
	public Analyzer (String lexiconFileName) throws Exception {
		super(lexiconFileName);
		loadExceptionFile();
	}	
	public Analyzer (InputStream lexiconStream) throws Exception {
		super(lexiconStream);
		loadExceptionFile();
	}

	private void loadExceptionFile() throws IOException {
		String exceptionName = "Exceptions.txt";
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(exceptionName);
			if (stream != null) {
				automats = new Trie( stream );
				return;
			}

			String path = new File(this.filename).getParent();
			if (path != null) exceptionName = path + java.io.File.separatorChar + exceptionName;
			automats=new Trie(exceptionName);
		} catch (Exception e) { 
			System.err.append(String.format("A1\nLeksikona ceļš:%s\nException ceļš:%s\n",this.filename,exceptionName));
			e.printStackTrace();
			automats = new Trie("");
			System.err.println("Nesanāca ielādēt exceptionus");
		}
	}
	
	public Analyzer (InputStream lexiconStream, InputStream[] auxiliaryLexiconStreams, InputStream exceptionStream) throws Exception {
		super(lexiconStream, auxiliaryLexiconStreams);

		try {
			automats=new Trie(exceptionStream);
		} catch (Exception e) { 
			e.printStackTrace();
			System.err.println("Nesanāca ielādēt exceptionus");
			automats = new Trie("");
		}
	}
    
	/* TODO - salikteņu minēšana jāuzaisa 
	private boolean DerSalikteņaSākumam(Ending ending) {
		if (ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Noun))
			return ending.isMatchingStrong(AttributeNames.i_Case,AttributeNames.v_Genitive);

		return false;
	} */
	
	/**
	 * Loads the analyzer lexicon from the specified file
	 * @param lexiconFileName - main lexicon file name
	 * @param useAuxiliaryLexicons should secondary lexicon files be included in addition to the core lexicons
	 */
	public Analyzer(String lexiconFileName, boolean useAuxiliaryLexicons) throws Exception{
		super(lexiconFileName, useAuxiliaryLexicons);
		loadExceptionFile();
	}

	/**
	 * Loads the analyzer lexicon from the specified file, but excludes a blacklist of sub-lexicons when doing so
	 * @param lexiconFileName filename of the core lexicon
	 * @param blacklist  list of sub-lexicon file names to skip from loading
	 */
	public Analyzer(String lexiconFileName, ArrayList<String> blacklist) throws Exception{
		super(lexiconFileName, blacklist);
		loadExceptionFile();
	}

	public void defaultSettings(){
		enablePrefixes = true;
		meklētsalikteņus = false;
		enableGuessing = false;
		enableDiminutive = true;
		enableVocative = false;
		guessNouns = true;
	    guessVerbs = true;
	    guessParticiples = true;
	    guessAdjectives = true;
	    enableAllGuesses = false;
		guessInflexibleNouns = false;
	}
	
	public void describe(PrintWriter pipe) {
		pipe.format("enableGuessing:\t%b\n", enableGuessing);
		pipe.format("enablePrefixes:\t%b\n", enablePrefixes);
		pipe.format("enableDiminutive:\t%b\n", enableDiminutive);
		pipe.format("enableVocative:\t%b\n", enableVocative);
		pipe.format("enableAllGuesses:\t%b\n", enableAllGuesses);
		pipe.format("meklētsalikteņus:\t%b\n", meklētsalikteņus);
		pipe.format("guessNouns:\t\t%b\n", guessNouns);
		pipe.format("guessVerbs:\t\t%b\n", guessVerbs);
		pipe.format("guessParticiples:\t%b\n", guessParticiples);
		pipe.format("guessAdjectives:\t%b\n", guessAdjectives);
		pipe.format("guessInflexibleNouns:\t%b\n", guessInflexibleNouns);
	
		pipe.flush();
	}

	/**
	 * Veic morfoloģisko analīzi
	 *
	 */
	public Word analyze(String word) {
		word = word.trim();
		
		Word cacheWord = wordCache.get(word);
		if (cacheWord != null) return (Word) cacheWord.clone();		
				
		Word rezults = new Word(word);
		if (!word.equals(word.toLowerCase().trim())) {
			String lettercase = AttributeNames.v_Lowercase;
			if (p_firstcap.matcher(word).matches()) lettercase = AttributeNames.v_FirstUpper;
			if (p_allcaps.matcher(word).matches()) lettercase = AttributeNames.v_AllUpper;
			Word lowercase = analyzeLowercase(word.toLowerCase().trim(), word);			
			for (Wordform vārdforma : lowercase.wordforms) {
				vārdforma.setToken(word.trim());
				vārdforma.addAttribute(AttributeNames.i_CapitalLetters, lettercase);
				rezults.addWordform(vārdforma);
			}
		} else { 
			rezults = analyzeLowercase(word, word);
		}
		
		wordCache.put(word, (Word) rezults.clone());
		return rezults;
	}
	
	private Word analyzeLowercase(String word, String originalWord) {
		Word result = new Word(word);
		
		for (Ending ending : getAllEndings().matchedEndings(word)) {
			String stemBezMijas;
			try {
				stemBezMijas = ending.stem(word);
			} catch (Ending.WrongEndingException e) {
				throw new Error(e); // Shouldn't ever happen - matchedEndings should ensure that word contains that ending.
			}
			int stemChange = ending.getMija();
			boolean properName = p_firstcap.matcher(originalWord).matches();
			ArrayList<Variants> celmi = Mijas.mijuVarianti(stemBezMijas, stemChange, properName);

			for (Variants celms : celmi) {
				ArrayList<Lexeme> lexemes = ending.getEndingLexemes(celms.celms);
				boolean foundSomethingHere = false;
				if (lexemes != null) 					
					for (Lexeme lexeme : lexemes) {
						String trešāSakne = stemBezMijas;
						if (lexeme.getParadigm().getStems() == 3) {
							trešāSakne = lexeme.getStem(2);
						}
						if (!Mijas.atpakaļlocīšanasVerifikācija(celms, stemBezMijas, stemChange, trešāSakne, properName))
							continue;
						Wordform variants = new Wordform(word, lexeme, ending);
						variants.addAttributes(celms);
						variants.addAttribute(AttributeNames.i_Guess, AttributeNames.v_NoGuess);
						if (this.isAcceptable(variants)) { // izmetam tos variantus, kas nav īsti pieļaujami - vienskaitliniekus daudzskaitlī, vokatīvus ja tos negrib
							result.addWordform(variants);
							foundSomethingHere = true;
						}
					}				

				if (!foundSomethingHere && enableDiminutive) 
					guessDeminutive(word, result, ending, celms, originalWord);
			}
		}
		
		if (result.isRecognized() && removeRareWords) {
			boolean hasNonrareOption = false;
			for (Wordform wf : result.wordforms)
				if (!wf.isMatchingStrong(AttributeNames.i_Frequency, AttributeNames.v_Rare))
					hasNonrareOption = true;
			
			
			if (hasNonrareOption) {
				List<Wordform> to_remove = new LinkedList<Wordform>();
				for (Wordform wf : result.wordforms)
					if (wf.isMatchingStrong(AttributeNames.i_Frequency, AttributeNames.v_Rare))
						to_remove.add(wf);
				result.wordforms.removeAll(to_remove);
			}			
		}

		if (!result.isRecognized()) {  //Hardcoded izņēmumi (ar regex) kas atpazīst ciparus, kārtas skaitļus utml
			if (p_number.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.setEnding(this.endingByID(1158)); // FIXME - hardkodēts numurs hardcoded vārdu galotnei
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				wf.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Number);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				result.addWordform(wf);
				return result;
			}
			if (p_fractional.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.setEnding(this.endingByID(1158)); // FIXME - hardkodēts numurs hardcoded vārdu galotnei
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				wf.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Number);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				result.addWordform(wf);
				return result;
			}
			if (p_ordinal.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.setEnding(this.endingByID(1158)); // FIXME - hardkodēts numurs hardcoded vārdu galotnei
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				wf.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Ordinal);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				result.addWordform(wf);
				return result;
			}
			if (p_abbrev.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.setEnding(this.endingByID(1158)); // FIXME - hardkodēts numurs hardcoded vārdu galotnei
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Abbreviation);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				result.addWordform(wf);
				return result;
			}
			if (enableGuessing && p_acronym.matcher(originalWord).matches()) { // Treating all short allcaps words as acronyms is a form of guessing, since it's not safe and makes a brave assumption about such outofvocabulary words
				Wordform wf = new Wordform(originalWord);
				Paradigm p = this.paradigmByID(12); // FIXME - hardkodēts numurs nelokāmo lietvārdu paradigmai
				Ending e = p.getLemmaEnding();
				wf.setEnding(e);
				wf.addAttribute(AttributeNames.i_Word, word);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttributes(p);
				wf.addAttributes(e);
				wf.addAttribute(AttributeNames.i_EndingID, Integer.toString(e.getID()));
				wf.addAttribute(AttributeNames.i_ParadigmID, Integer.toString(p.getID()));
				result.addWordform(wf);
				return result;
			}
			if (p_url.matcher(word).matches()) {
				Wordform wf = new Wordform(word);
				wf.setEnding(this.endingByID(1158)); // FIXME - hardkodēts numurs hardcoded vārdu galotnei
				wf.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				wf.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_URI);
				wf.addAttribute(AttributeNames.i_Lemma, word);
				wf.addAttribute(AttributeNames.i_Word, word);
				result.addWordform(wf);
				return result;
			}
		}
		
		if (!result.isRecognized() && enablePrefixes )
			result = guessByPrefix(word);
/*
		if (!rezultāts.isRecognized() && meklētsalikteņus )
			for (Ending ending : allEndings())
				if (DerSalikteņaSākumam(ending)) {
					for (ArrayList<Leksēma> pirmiecelmi : galotne.getVārdgrupa().leksēmaspēcVārda.get(galotne.saknesNr-1).values()) {
						//FIXME - salikteņu meklēšana nav te ielikta
					}
				} */

		if (!result.isRecognized() && enableGuessing )
			result = guessByEnding(word, originalWord);

		/*for (Wordform variants : rezultāts.wordforms) {
			variants.addAttribute(AttributeNames.i_Tag, MarkupConverter.toKamolsMarkup(variants));
			if (variants.lexeme != null) {
				String locījumuDemo = "";
				for (Wordform locījums : generateInflectionsFromParadigm(variants.lexeme)) {
					locījumuDemo = locījumuDemo + locījums.getValue(AttributeNames.i_Word) + " " + locījums.getValue(AttributeNames.i_Case) + "\n";
				}
				variants.pieliktĪpašību("LocījumuDemo", locījumuDemo);
				//TODO - kautko jau ar to visu vajag; bet bez īpašas vajadzības tas ir performancehog
			}
		} */

		return result;
	}

	private void guessDeminutive(String word, Word rezultāts, Ending ending,
			Variants celms, String originalWord) {
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
						String lemma = leksēma.getStem(0) + "īt" + ending.getLemmaEnding().getEnding();
						lemma = recapitalize(lemma, originalWord);
						variants.addAttribute(AttributeNames.i_Lemma, lemma);
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
						String lemma = pamatforma + "iņ" + ending.getLemmaEnding().getEnding();
						lemma = recapitalize(lemma, originalWord);
						variants.addAttribute(AttributeNames.i_Lemma, lemma);
						
						rezultāts.addWordform(variants);										
					}
			}
		}
	}

	private boolean isAcceptable(Wordform variants) {
		if (!enableVocative && variants.isMatchingStrong(AttributeNames.i_Case,AttributeNames.v_Vocative))
			return false;

		if (variants.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum) &&
				!variants.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Plural))
			return false;

		if (variants.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum) &&
				!variants.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Singular))
			return false;

		if (variants.isMatchingStrong(AttributeNames.i_Declension, AttributeNames.v_InflexibleGenitive) &&
				!variants.isMatchingWeak(AttributeNames.i_Case, AttributeNames.v_Genitive))
			return false;
		
		return true;
	}

	private Word guessByPrefix(String word) {
		Word rezultāts = new Word(word);
		if (word.contains(" ")) return rezultāts;
		
		boolean vajadzība = false;
		if (word.startsWith("jā")) {
			vajadzība = true;
			word = word.substring(2);
		}
		
		for (String priedēklis : prefixes)
			if (word.startsWith(priedēklis)) {
				String cut_word = word.substring(priedēklis.length());
				if (vajadzība) cut_word = "jā" + cut_word;
				Word bezpriedēkļa = analyzeLowercase(cut_word, cut_word);
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
		vārds.notifyObservers();
	}

	public Word guessByEnding(String word, String originalWord) {
		Word rezultāts = new Word(word);

		for (int i=word.length()-2; i>=0; i--) { // TODO - duma heiristika, kas vērtē tīri pēc galotņu garuma; vajag pēc statistikas
			for (Ending ending : getAllEndings().matchedEndings(word))
				if (ending.getEnding().length()==i) {
					Paradigm p = ending.getParadigm();
					if (p.getName().equals("Hardcoded"))
						continue; // Hardcoded vārdgrupa minēšanai nav aktuāla

					String stem;
					try {
						stem = ending.stem(word);
					} catch (Ending.WrongEndingException e) {
						throw new Error(e); // Shouldn't ever happen - matchedEndings should ensure that word contains that ending.
					}

					ArrayList<Variants> celmi = Mijas.mijuVarianti(stem, ending.getMija(), false); //FIXME - te var būt arī propername... tikai kā tā info līdz šejienei nonāks?
					if (celmi.size() == 0) continue; // acīmredzot neder ar miju, ejam uz nākamo galotni.
					String celms = celmi.get(0).celms;
					
					if (!p.allowedGuess(celms))
						if (p_firstcap.matcher(originalWord).matches() && (p.getID() == 8 || p.getID() == 10 || p.getID() == 31 ))
						{} // Ja ir īpašvārds ar -a -e galotni, tad mēģina arī vīriešu dzimtes variantus uzvārdiem
						else continue; // citos gadījumos, ja beigu burti izskatās neadekvāti tam, kas leksikonā pie paradigmas norādīts - tad neminam.
					if (p.getID() == 5 && !celms.endsWith("sun"))
						continue; // der tikai -suns salikteņi
					//TODO - varbūt drīzāk whitelist datos - pie paradigmas karodziņu, ka tā ir atvērta un tajā drīkst minēt?
					//TODO te var vēl heiristikas salikt, lai uzlabotu minēšanu - ne katrs burts var būt darbībasvārdam beigās utml

					Wordform variants = new Wordform(word, null, ending);
					variants.addAttribute(AttributeNames.i_Source,"minējums pēc galotnes");
					variants.addAttribute(AttributeNames.i_Guess, AttributeNames.v_Ending);

					// FIXME ko ar pārējiem variantiem?? un ko ja nav variantu?
					Ending pamatforma = ending.getLemmaEnding();					
					if (pamatforma != null) {
						// Izdomājam korektu lemmu
						String lemma = celms + pamatforma.getEnding();
						lemma = recapitalize(lemma, originalWord);	
						
						variants.addAttribute(AttributeNames.i_Lemma, lemma);
					}

					if (  ((this.guessNouns && ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Noun) &&							
                            (enableVocative || !variants.isMatchingStrong(AttributeNames.i_Case,AttributeNames.v_Vocative)) &&
                            (guessInflexibleNouns || !variants.isMatchingStrong(AttributeNames.i_Declension,AttributeNames.v_NA))
                            ) ||
							(this.guessVerbs && ending.getParadigm().isMatchingWeak(AttributeNames.i_PartOfSpeech,AttributeNames.v_Verb)) ||
                            (this.guessAdjectives && ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Adjective)) ||
                            (this.guessParticiples && variants.isMatchingStrong(AttributeNames.i_Izteiksme,AttributeNames.v_Participle))) 
                      && (i>0 || variants.isMatchingStrong(AttributeNames.i_Declension,AttributeNames.v_NA)) ) // ja galotnes nav, tad vai nu nelokāms lietvārds vai neatpazīstam. Lai nav verbu bezgalotņu formas minējumos, kas parasti nav pareizās.
                            	{
									if (ending.getParadigm().isMatchingStrong(AttributeNames.i_PartOfSpeech,AttributeNames.v_Noun) && 
											variants.isMatchingStrong(AttributeNames.i_Declension,AttributeNames.v_NA)) {
										char last = celms.charAt(celms.length()-1);
 										if (!(last=='ā' || last == 'e' || last == 'ē' || last == 'i' || last == 'ī' || last == 'o' || last == 'ū' || celms.endsWith("as"))) {  // uzskatam, ka 'godīgi' nelokāmie lietvārdi beidzas tikai ar šiem - klasiski nelokāmie, un lietuviešu Arvydas
 											variants.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
 											if (!Character.isDigit(last)) {
 												variants.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Foreign);
												//Pieņemam, ka vārdi svešvalodā - 'crawling' 'Kirill' utml.
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

	/**
	 * Performs morphological analysis, assuming that we know explicitly that the form is a lemma
	 * @param word
	 * @return
	 */
	public Word analyzeLemma(String word) {
		Word result = new Word(word);
		Word varianti = analyze(word);

		for (Wordform vārdforma : varianti.wordforms) {			
			Ending ending = vārdforma.getEnding();
			
			AttributeValues filter = new AttributeValues();
			filter.addAttribute(AttributeNames.i_Lemma, word);
			filter.addAttribute(AttributeNames.i_Lemma, AttributeNames.v_Singular);
			
			if ( (ending != null && ending.getLemmaEnding() == ending) ||
				(vārdforma.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(word) && 
						(vārdforma.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum) || vārdforma.isMatchingStrong(AttributeNames.i_Declension, AttributeNames.v_InflexibleGenitive) )) )
				result.addWordform(vārdforma);
		}

		return result;
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
	
	public ArrayList<Wordform> generateInflections(String lemma, boolean nouns_only) {
		return generateInflections(lemma, nouns_only, new AttributeValues());
	}
	
	public ArrayList<Wordform> generateInflections(String lemma, boolean nouns_only, AttributeValues filter) {
		//Vispirms, pārbaudam specgadījumu - dubultuzvārdus
		if (p_doublesurname.matcher(lemma).matches()) {
			int hyphen = lemma.indexOf("-");
			AttributeValues part_filter = new AttributeValues(filter); // relax filter conditions for the first part, as it can have different endings than the whole compound surname
			part_filter.removeAttribute(AttributeNames.i_Lemma);
			ArrayList<Wordform> inflections2 = generateInflections(lemma.substring(hyphen+1, lemma.length()), nouns_only, part_filter);
			part_filter.removeAttribute(AttributeNames.i_Declension);
			part_filter.removeAttribute(AttributeNames.i_ParadigmID);
			if (part_filter.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual)) {
				part_filter.removeAttribute(AttributeNames.i_PartOfSpeech);
				part_filter.removeAttribute(AttributeNames.i_ResidualType);
			}
			ArrayList<Wordform> inflections1 = generateInflections(lemma.substring(0, hyphen), nouns_only, part_filter);
			
			if ( (inflections1.size()>1 && inflections2.size()>1) // Ja sanāk nelokāms kautkas, tad nemēģinam taisīt kā dubultuzvārdu - tie ir ļoti reti un tas salauztu vairāk nekā iegūtu
					|| lemma.substring(0, hyphen).equalsIgnoreCase("pavļuta")) 
				return mergeInflections(inflections1, inflections2, "-");
		}
		
		Word possibilities = this.analyze(lemma);
		
		filterInflectionPossibilities(nouns_only, filter, possibilities.wordforms);		
		
		ArrayList<Wordform> result = generateInflections_TryLemmas(lemma, possibilities);
		if (result != null) filterInflectionPossibilities(nouns_only, filter, result);
		
		// If result is null, it means that all the suggested lemma can be (and was) generated from another lemma - i.e. "Dīcis" from "dīkt"; but not from an existing lexicon lemma
		// We assume that a true lemma was passed by the caller, and we need to generate/guess the wordforms as if the lemma was correct.
		if ((result == null || result.size()==0) && this.enableGuessing) {
			possibilities = this.guessByEnding(lemma.toLowerCase(), lemma);
			filterInflectionPossibilities(nouns_only, filter, possibilities.wordforms);		
			
			result = generateInflections_TryLemmas(lemma, possibilities);			
		}			

		// If guessing didn't work, return an empty list
		if (result == null)
			result = new ArrayList<Wordform>();
		
		return result;
	}
	
	// Ņemam divas locījumu kopas un apvienojam vienā .... pašreiz pielietojums tikai dubultuzvārdiem, pēc tam varbūt vēl kaut kur (frāzes?)
	private ArrayList<Wordform> mergeInflections(
			ArrayList<Wordform> inflections1, ArrayList<Wordform> inflections2,
			String concatenator) {		
		ArrayList<Wordform> result = new ArrayList<Wordform>();
		

		if (inflections1.size() <= 1) {
			// Specgadījums - pirmais ir nelokāms
			String fixedtoken = "???";
			String fixedlemma = "???";
			if (inflections1.size() > 0) {
				fixedtoken = inflections1.get(0).getToken();
				fixedlemma = inflections1.get(0).getValue(AttributeNames.i_Lemma);
			}
			
			for (Wordform otrā : inflections2) {
				Wordform apvienojums = (Wordform) otrā.clone(); // Pamatinfo no otrās daļas, jo tā itkā ir gramatiski dominējoša
				apvienojums.setToken(fixedtoken + concatenator + apvienojums.getToken());
				apvienojums.addAttribute(AttributeNames.i_Lemma, fixedlemma + concatenator + apvienojums.getValue(AttributeNames.i_Lemma));
				// TODO - vēl kautkas?
				result.add(apvienojums);
			}
		} else if (inflections2.size() <= 1) {
			// Specgadījums - otrais ir nelokāms
			String fixedtoken = "???";
			String fixedlemma = "???";
			if (inflections2.size() > 0) {
				fixedtoken = inflections2.get(0).getToken();
				fixedlemma = inflections2.get(0).getValue(AttributeNames.i_Lemma);
			}
			
			for (Wordform pirmā : inflections1) {
				Wordform apvienojums = (Wordform) pirmā.clone(); // Pamatinfo no otrās daļas, jo tā itkā ir gramatiski dominējoša
				apvienojums.setToken(apvienojums.getToken() + concatenator + fixedtoken);
				apvienojums.addAttribute(AttributeNames.i_Lemma, apvienojums.getValue(AttributeNames.i_Lemma) + concatenator + fixedlemma);
				// TODO - vēl kautkas?
				result.add(apvienojums);
			}
		} else {
			// Normālais gadījums, kad vajag prātīgi apvienot
			
			for (Wordform pirmā : inflections1) {				
				AttributeValues filter = new AttributeValues();
				// Pieņemam, ka te tikai lietvārdi apgrozīsies
				filter.addAttribute(AttributeNames.i_Case, pirmā.getValue(AttributeNames.i_Case));
				filter.addAttribute(AttributeNames.i_Number, pirmā.getValue(AttributeNames.i_Number));
				ArrayList<Wordform> possibilities = (ArrayList<Wordform>) inflections2.clone(); 
				filterInflectionPossibilities(true, filter, possibilities);
				if (possibilities.size() == 0) {
					// Debuginfo
//					System.err.println("Problēma ar dubultuzvārdu locīšanu - nesanāca dabūt atbilstošu 'pārīti' šim te pirmās daļas locījumam");
//					pirmā.describe(new PrintWriter(System.err));
//					System.err.println(".. no šīs te kopas otrās daļas locījumu");
//					for (Wordform otrā : inflections2) {
//						otrā.describe(new PrintWriter(System.err));
//						System.err.println("  --");
//					}					
				} else {
					if ((!pirmā.isMatchingStrong(AttributeNames.i_Case, AttributeNames.v_Vocative) && possibilities.size() > 1) || possibilities.size() > 2) {
						// Debuginfo
//						System.err.println("Problēma ar dubultuzvārdu locīšanu - par daudz atbilstošu 'pārīšu' šim te pirmās daļas locījumam");
//						pirmā.describe(new PrintWriter(System.err));
//						System.err.println(".. no šīs te kopas otrās daļas locījumu");
//						for (Wordform otrā : inflections2) {
//							otrā.describe(new PrintWriter(System.err));
//							System.err.println("  --");
//						}					
					}	
					
					Wordform apvienojums = (Wordform) possibilities.get(0).clone(); // Pamatinfo no otrās daļas, jo tā itkā ir gramatiski dominējoša
					apvienojums.setToken(pirmā.getToken() + concatenator + apvienojums.getToken());
					apvienojums.addAttribute(AttributeNames.i_Lemma, pirmā.getValue(AttributeNames.i_Lemma) + concatenator + apvienojums.getValue(AttributeNames.i_Lemma));
					// TODO - vēl kautkas?
					result.add(apvienojums);
				}			
			}
		}
		
		return result;
	}

    // generate all forms if the paradigm # is known
    // TODO - needs more support for extra features (fixed-genitives, etc)
    public ArrayList<Wordform> generateInflectionsFromParadigm(String lemma, int paradigm, AttributeValues lemmaAttributes) {
        Paradigm p = this.paradigmByID(paradigm);

        if (p == null)
            return generateInflections(lemma); // If the supplied paradigm is invalid, we ignore it

        if (p.getStems() > 1)  // For 1st conjugation verbs, lemma is not enough info to inflect properly
            return generateInflections(lemma); // Assume that it will be in current lexicon..

        Ending ending = p.getLemmaEnding();
        if (lemmaAttributes.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum)
                && !ending.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Plural)) {
            // Assuming that there will be only one plural nominative entry in case of daudzskaitlinieki
            AttributeValues plural_nominative = new AttributeValues();
            plural_nominative.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
            plural_nominative.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
            for (Ending candidate_ending : ending.getParadigm().endings) {
                if (candidate_ending.isMatchingStrong(plural_nominative)) {
                    ending = candidate_ending;
                }
            }
        }

        if (!lemma.endsWith(ending.getEnding())) {
            System.err.printf("Attempted to generate inflections for lemma '%s' at paradigm '%d'; failed because of mismatched ending", lemma, paradigm);
        }

        Lexeme l = this.createLexeme(lemma, ending.getID(), "temp");
        if (l == null) { // Couldn't create the lexeme - the word wasn't compatible with the supplied paradigm
            return new ArrayList<Wordform>();
        }
        l.addAttributes(lemmaAttributes);
        ArrayList<Wordform> result = generateInflections(l, lemma);
        p.removeLexeme(l); // To not pollute the in-memory lexicon

        return result;
    }


    // generate all forms if the paradigm # is known
	public ArrayList<Wordform> generateInflectionsFromParadigm(String lemma, int paradigm) {
		return generateInflectionsFromParadigm(lemma, paradigm, new AttributeValues());
	}

	// generate all forms if the paradigm # and also the three lemmas (for 1st conjugation) are known
	public ArrayList<Wordform> generateInflectionsFromParadigm(String lemma, int paradigm, String stem1, String stem2, String stem3) {
		Paradigm p = this.paradigmByID(paradigm);

		if (p == null)
			return generateInflections(lemma); // If the supplied paradigm is invalid, we ignore it

		if (p.getStems() == 1)  // If it's not 1st conjugation verb, perform as if we didn't know the stems
			return generateInflectionsFromParadigm(lemma, paradigm);

		if (!lemma.endsWith(p.getLemmaEnding().getEnding())) {
			//FIXME - should check for plural nouns, etc
		}

		Lexeme l = this.createLexeme(lemma, p.getLemmaEnding().getID(), "temp");
		if (l == null) { // Couldn't create the lexeme - the word didn't wasn't compatible with the supplied paradigm
			return new ArrayList<Wordform>();
		}
        l.setStem(0, stem1);
        l.setStem(1, stem2);
        l.setStem(2, stem3);
		ArrayList<Wordform> result = generateInflections(l, lemma);
		p.removeLexeme(l); // To not pollute the in-memory lexicon

		return result;
	}
	
	// removes possibilities that aren't nouns/substantivised adjectives, and don't match the filter
	private void filterInflectionPossibilities(boolean nouns_only, AttributeValues filter, ArrayList<Wordform> possibilities) {
		ArrayList<Wordform> unsuitable = new ArrayList<Wordform>();
		for (Wordform wf : possibilities) {
			boolean suitable = ! nouns_only; // if nouns_only, then we want to test for partofspeech, if not, then okay by default
			if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun)) suitable = true;
			if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective) && wf.isMatchingStrong(AttributeNames.i_Definiteness, AttributeNames.v_Definite)) suitable = true;
			if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual) && wf.isMatchingStrong(AttributeNames.i_ResidualType, AttributeNames.v_Foreign)) suitable = true; // visādi Vadim, Kirill utml
			
			if (!wf.isMatchingWeak(filter) && !wf.isMatchingStrong(AttributeNames.i_ResidualType, AttributeNames.v_Foreign) && !wf.isMatchingStrong(AttributeNames.i_Declension, AttributeNames.v_NA)) suitable = false; //filter overrides everything except inflexible stuff
			
			if (!suitable) unsuitable.add(wf);
		}
		possibilities.removeAll(unsuitable);
	}

	private ArrayList<Wordform> generateInflections_TryLemmas(String lemma, Word w) {		
		for (Wordform wf : w.wordforms) {
			// Pamēģinam katru no analīzes variantiem, vai viņš ir pamatforma (atbilst vajadzīgajai lemmai)
			if (wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma) && !wf.isMatchingStrong(AttributeNames.i_Case, AttributeNames.v_Vocative)) {				
				Lexeme lex = wf.lexeme;
				if (lex == null || !lex.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma)) {
                    // Ja nav pareizā leksēma (atvasināšana vai minēšana) tad uztaisam leksēmu
					int endingID = wf.getEnding().getID();
					if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adverb))
						endingID = 954; // FIXME - hardcoded number of Adverb paradigm main ending, must match the ending number in Lexicon.xml
						
					lex = this.createLexeme(lemma, endingID, "generateInflectionsFromParadigm");
					if (lex.getValue(AttributeNames.i_PartOfSpeech) == null)
						lex.addAttribute(AttributeNames.i_PartOfSpeech, wf.getValue(AttributeNames.i_PartOfSpeech)); // Hardcoded vārdšķirai lai ir POS - saīsinājumi utml
					if (p_firstcap.matcher(lemma).matches())
						lex.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun); //FIXME - hack personvārdu 'Valdis' utml locīšanai
					if (wf.getEnding().getParadigm().getStems() > 1 && wf.lexeme != null && wf.getValue(AttributeNames.i_Prefix) != null) { // Priedēkļu atvasināšanai priedēklis jāpieliek arī pārējiem celmiem
						lex.setStem(1, wf.getValue(AttributeNames.i_Prefix) + wf.lexeme.getStem(1));
						lex.setStem(2, wf.getValue(AttributeNames.i_Prefix) + wf.lexeme.getStem(2));
					}
				}
				ArrayList<Wordform> result = generateInflections(lex, lemma);
				if (lex.isMatchingStrong(AttributeNames.i_Source, "generateInflectionsFromParadigm"))
					lex.getParadigm().removeLexeme(lex);
				return result;
			}
			if ( wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective) && (
				 (lemma.toLowerCase().endsWith("ais") && lemma.equalsIgnoreCase(wf.getValue(AttributeNames.i_Lemma).substring(0, wf.getValue(AttributeNames.i_Lemma).length()-1)+"ais")) ||
				 (lemma.toLowerCase().endsWith("ā") && wf.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma.substring(0, lemma.length()-1)+"a")) ) ) {
				// Exception for adjective-based surnames "Lielais", "Platais" etc
				Lexeme lex = wf.lexeme;
				if ((lex == null && lemma.toLowerCase().endsWith("ais")) || (lex != null && !lex.getValue(AttributeNames.i_Lemma).equalsIgnoreCase(lemma))) {
					lex = this.createLexeme(lemma, wf.getEnding().getID(), "generateInflectionsFromParadigm");
					if (p_firstcap.matcher(lemma).matches())
						lex.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun); //FIXME - hack personvārdu 'Valdis' utml locīšanai
				}
				if (lex == null) continue;
				ArrayList<Wordform> result = new ArrayList<Wordform>();
				for (Wordform wf2 : generateInflections(lex, lemma)) {
					if (wf2.isMatchingStrong(AttributeNames.i_Definiteness, AttributeNames.v_Definite) && wf2.isMatchingStrong(AttributeNames.i_Degree, AttributeNames.v_Positive) && wf2.isMatchingWeak(AttributeNames.i_Gender, wf.getValue(AttributeNames.i_Gender))) {
						result.add(wf2);
					}
				}
				if (lex.isMatchingStrong(AttributeNames.i_Source, "generateInflectionsFromParadigm"))
					lex.getParadigm().removeLexeme(lex);
				return result;
			}
		}
		return null;
	}
	
	public ArrayList<Wordform> generateInflections(Lexeme lexeme, String lemma)
	{
		String trešāSakne = null, vārds;
		//Vārds rezultāts = new Vārds(leksēma.īpašības.Īpašība(IpasibuNosaukumi.i_Pamatforma));
		ArrayList <Wordform> inflections =  new ArrayList<Wordform>(1);

		//priekš 1. konj nākotnes mijas nepieciešams zināt 3. sakni
		if (lexeme.getParadigm().getStems() == 3) {
			trešāSakne = lexeme.getStem(2);
		}

        if (lexeme.getParadigm().getID() == 29 || lexeme.getParadigm().getID() == 25 ) { // Hardcoded paradigma un vietniekvārdi
            // Ja vārds ir hardcoded, tad salasam visas hardcoded formas ar attiecīgo lemmu un tās arī atgriežam
            Ending ending = lexeme.getParadigm().getLemmaEnding();
            for (Lexeme formLexeme : this.hardcodedForms.get(lemma)) {
                inflections.add(new Wordform(formLexeme.getStem(0), formLexeme, ending));
            }
            return inflections;
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
		    		vārds = recapitalize(vārds, lemma);

		    		Wordform locījums = new Wordform(vārds, lexeme, ending);
					locījums.addAttributes(celms);
					boolean validOption = locījums.isMatchingWeak(AttributeNames.i_Generate, AttributeNames.v_Yes);
					if (locījums.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum) && locījums.isMatchingStrong(AttributeNames.i_Number, AttributeNames.v_Singular)) validOption = false;
					if (locījums.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum) && locījums.isMatchingStrong(AttributeNames.i_Number, AttributeNames.v_Plural)) validOption = false;
					if (locījums.isMatchingStrong(AttributeNames.i_Declension, AttributeNames.v_InflexibleGenitive) && !locījums.isMatchingStrong(AttributeNames.i_Case, AttributeNames.v_Genitive)) validOption = false;
					if (validOption) inflections.add(locījums);
		    	}
			}
		}

        // Pārbaudam, vai šai lemmai nav kāds hardcoded formas override (piemēram, kā formai viņš *ej -> viņš iet)
        for (Lexeme formLexeme : this.hardcodedForms.get(lemma)) {
            Ending ending = formLexeme.getParadigm().getLemmaEnding();
            Wordform hardcoded = new Wordform(formLexeme.getStem(0), formLexeme, ending);
            Wordform override = null;
            for (Wordform form : inflections) {
                if (form.isMatchingWeak(formLexeme)) {
                    override = form;
                }
            }
            if (override != null) {
                inflections.remove(override);
            }
            inflections.add(hardcoded);

        }

		return inflections;
	}

}
