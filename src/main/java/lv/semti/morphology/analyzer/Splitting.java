/*******************************************************************************
 * Copyright 2008, 2009, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens, Imants Borodkins
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

import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.attributes.AttributeNames;

/**
 * Tools for detecting chunk and token bounds.
 * When you edit this, please, update tokenize.pl in Chunker, too!
 */
public class Splitting {
	// Vārdā, atdalītājā, atstarpē
	private enum Status {IN_WORD, IN_SPACE}; // , IN_DELIMITER, IN_EXCEPTION - tagad šo funkcionalitāti dara Trie morphoAnalyzer.automats
	
	public static int DEFAULT_SENTENCE_LENGTH_CAP = 50;
	
	/**
	 * Determine, if given word should split a chunk (ends a sentence, like a period or exclamation mark)
	 */
	public static boolean isChunkCloser(Word word) {
		return word.hasAttribute(AttributeNames.i_PieturziimesTips, AttributeNames.v_Punkts); // pieņemam, ka tikai 'zs' tags ir teikuma beigas - tur ir punkts, jautājumzīme, izsaukumzīme, daudzpunkte un to kombinācijas/variācijas.
	}
		
	public static boolean isSeparator(char c)
	{
		String separators=" .?:/!,;\"'`´(){}<>«»-[]—‐‑‒–―‘’‚‛“”„‟′″‴‵‶‷‹›‼‽⁈⁉․‥…&•*";
		return separators.contains(String.valueOf(c));
	}

	/**
	 * Determine if given char is a whitespace char (space, tab, newline).
	 */
	public static boolean isSpace(char c)
	{
	    return (" \t\n\r\u00A0".indexOf(c) != -1);
	}

	/*
	 * Tokenizes the string (sentence?) and runs morphoanalysis on each word.
	 */
	public static LinkedList<Word> tokenize(Analyzer morphoAnalyzer, String chunk) {		
		LinkedList<Word> tokens = new LinkedList<Word>();
		if (chunk == null) return tokens;
		
		Trie automats = morphoAnalyzer.automats;

		// te tiek ciklā doti visi tekstā esošie vārdi uz morfoanalīzi.    
	    int progress = 0;
	    //bug fix - pievienota beigās whitespace
		String str = chunk+" ";
		str = str.replace('\n', ' ');
		str = str.replace('\r', ' ');
		str = str.replace('\t', ' ');
		str = str.replace('\u00A0', ' ');
		boolean inApostrophes=false;
		Status statuss = Status.IN_SPACE;
		
		int lastGoodEnd=0;
		boolean canEndInNextStep=false;
		
		for (int i = 0; i < str.length(); i++) {
			switch (statuss) {
			case IN_SPACE:
				if (!Splitting.isSpace(str.charAt(i))) {
					if (str.charAt(i)=='\'') inApostrophes=true;

					automats.reset(); //atjauno automāta stāvokli
					automats.findNextBranch(str.charAt(i)); //atrod pirmo derīgo zaru
					
					if(automats.status()>0) { //pārbauda vai atrada meklēto simbolu
						//ja atrada
						statuss=Status.IN_WORD;
						progress=i;
						//pārbauda vai ar to var arī virkne beigties
						canEndInNextStep = (automats.status()==2);
					} else {
						//ja neatrada, pievieno simbolu rezultātam
						tokens.add( (morphoAnalyzer == null) ? 
								new Word(str.substring(i,i+1)) :
								morphoAnalyzer.analyze(str.substring(i,i+1)) );
					}
				}
				break;
			case IN_WORD:
				//pārbauda vai ir atrastas potenciālās beigas
		        if(canEndInNextStep==true && 
			            (
			              ( Splitting.isSeparator(str.charAt(i)) && Character.isLetter((i>0 ? str.charAt(i-1) : 0))  ) 
			              || !Character.isLetter((i>0 ? str.charAt(i-1) : 0) )     
			            ) )
				{
					lastGoodEnd=i;
					if(str.charAt(i)=='\'' && inApostrophes) {
						tokens.add( (morphoAnalyzer == null) ? 
								new Word(str.substring(progress,i)) :
								morphoAnalyzer.analyze(str.substring(progress,i)) );
						tokens.add( (morphoAnalyzer == null) ? 
								new Word(str.substring(i,i+1)) :   
								morphoAnalyzer.analyze(str.substring(i,i+1)) );
						inApostrophes=false;
						statuss=Status.IN_SPACE;
						break;
					}
				}
				canEndInNextStep=false;
				
				//mēģina atrast nākamo simbolu automātā
				if (automats.findNext(str.charAt(i))>0) { //ja atrada 
					//pārbauda vai ar to var arī virkne beigties
					if (automats.status()==2)
						canEndInNextStep=true;
				} else {
					//ja neatrada, pārbauda vai darbības laikā tika atrasta potenciālā beigu pozīcija
					if (lastGoodEnd>progress) {
						tokens.add( (morphoAnalyzer == null) ? 
								new Word(str.substring(progress,lastGoodEnd)) :
								morphoAnalyzer.analyze(str.substring(progress,lastGoodEnd)) );
						i=lastGoodEnd-1;
						statuss = Status.IN_SPACE;
					} else {
						i=progress;
						//mēgina atrast nākamo derīgo zaru
						automats.nextBranch();
						automats.findNextBranch(str.charAt(i));
						if(automats.status()>0) { //pārbauda vai atrada meklēto simbolu
							//pārbauda vai ar to var arī virkne beigties
							if(automats.status()==2)
								canEndInNextStep=true;
						} else {
							//ja neatrada, pievieno simbolu rezultātam un pēc tam dosies meklēt jauno sākumu
							tokens.add( (morphoAnalyzer == null) ? 
									new Word(str.substring(i,i+1)) :
									morphoAnalyzer.analyze(str.substring(i,i+1)) );
							statuss = Status.IN_SPACE;
						}
					}
				}				
				break;
			}
		} // for i..
		
		
		if (statuss == Status.IN_WORD) { 
			tokens.add( (morphoAnalyzer == null) ? 
					new Word(str.substring(progress,str.length())) :
					morphoAnalyzer.analyze(str.substring(progress,str.length())) );
		}
		
		return tokens;
	}
	
	
	/***
	 * Tokenizes some text (usually a sentence)
	 * @param morphoAnalyzer
	 * @param chunk
	 * @param bruteSplit
	 * @return
	 */
	public static LinkedList<Word> tokenize(Analyzer morphoAnalyzer, String chunk, boolean bruteSplit) {
		if(bruteSplit)
		{
			LinkedList<Word> tokens = new LinkedList<Word>();
			if (chunk == null) return tokens;
			String[] parts_of_string = chunk.trim().split(" ");
			for(String part : parts_of_string) 
			{
				if (part.length()>0)
					tokens.add( (morphoAnalyzer == null) ? 
						new Word(part) :
						morphoAnalyzer.analyze(part));
			}
			return tokens;
		}
		else
		{
			return tokenize(morphoAnalyzer, chunk);
		}
	}

	public static LinkedList<LinkedList<Word>> tokenizeSentences(
			Analyzer morphoAnalyzer, String paragraph) {
		return tokenizeSentences(morphoAnalyzer, paragraph, DEFAULT_SENTENCE_LENGTH_CAP);
	}
	/***
	 * Tokenizes a paragraph, and splits it into sentences.
	 * @param morphoAnalyzer
	 * @param paragraph
	 * @return
	 */
	public static LinkedList<LinkedList<Word>> tokenizeSentences(
		 	Analyzer morphoAnalyzer, String paragraph, int lengthCap) {
		LinkedList<LinkedList<Word>> result = new LinkedList<LinkedList<Word>>();
		
		List<Word> tokens = Splitting.tokenize(morphoAnalyzer, paragraph);
		LinkedList<Word> sentence = new LinkedList<Word>();
		for (Word word : tokens) {
		    // Teikumu beigas iekšā tiešajā runā - pievelkam pēdiņu klāt
            if (sentence.size() == 0 && word.getToken().equals("\"")) {
                // Pārbaudam vai iepriekšējā teikuma beigās ir punkts
                if (!result.isEmpty() && !result.getLast().isEmpty() && result.getLast().getLast().getToken().equals(".")) {
                    result.getLast().add(word);
                    continue;
                }
            }

			sentence.add(word);
			if ( Splitting.isChunkCloser(word) || // does this token look like end of sentence
				(sentence.size() >= lengthCap-5 && (word.hasAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Punctuation) || word.getToken().startsWith("<")) )
				|| sentence.size() > lengthCap) { 		// hard limit		
				result.add(sentence);
				sentence = new LinkedList<Word>();
			}
		}
		
		if (!sentence.isEmpty()) 
			result.add(sentence);
		return result;
	}


}
