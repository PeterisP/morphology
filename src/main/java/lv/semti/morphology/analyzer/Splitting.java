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
import java.util.stream.Collectors;

import lv.semti.morphology.attributes.AttributeNames;
import org.apache.commons.lang3.StringUtils;

/**
 * Tools for detecting chunk and token bounds.
 * When you edit this, please, update tokenize.pl in Chunker, too!
 */
public class Splitting {
	// Vārdā, atdalītājā, atstarpē
	private enum Status {IN_WORD, IN_SPACE}; // , IN_DELIMITER, IN_EXCEPTION - tagad šo funkcionalitāti dara Trie morphoAnalyzer.automats
	
	public static int DEFAULT_SENTENCE_LENGTH_CAP = 250;
	
	/**
	 * Determine, if given word should split a chunk (ends a sentence, like a period or exclamation mark)
	 */
	public static boolean isChunkCloser(Word word) {
		return word.hasAttribute(AttributeNames.i_PieturziimesTips, AttributeNames.v_Punkts); // pieņemam, ka tikai 'zs' tags ir teikuma beigas - tur ir punkts, jautājumzīme, izsaukumzīme, daudzpunkte un to kombinācijas/variācijas.
	}
		
	public static boolean isSeparator(char c)
	{
		String separators=" \t\n\r\u00A0\u2029\u200B.?:/!,;\"'`´(){}<>«»-+[]—‐‑‒–―‘’‚‛“”„‟′″‴‵‶‷‹›‼‽⁈⁉․‥…&•*";
		return separators.contains(String.valueOf(c));
	}

	/**
	 * Determine if given char is a whitespace char (space, tab, newline).
	 */
	public static boolean isSpace(char c)
	{
	    return Character.isWhitespace(c) || Character.isISOControl(c) || c == '\u00A0' || c == '\uFEFF' || c == '\u2029' || c == '\u200B';
	}

	private static Word formToken(Analyzer morphoAnalyzer, String str, int start, int end, StringBuilder accumulatedWhitespace) {
	    String word = str.substring(start, end);
	    word = word.replace("\u00AD", ""); // Soft hyphen gets removed from word before analysis
        Word token = (morphoAnalyzer == null) ? new Word(word) : morphoAnalyzer.analyze(word);

        String whitespace = accumulatedWhitespace.toString().replace("\u200B", ""); // zero-width spaces are used as temporary separators
        int offset = start - StringUtils.countMatches(str.substring(0,start), '\u200B');
        for (Wordform wf : token.wordforms) {
            wf.addAttribute(AttributeNames.i_WhitespaceBefore, whitespace);
            wf.addAttribute(AttributeNames.i_Offset, Integer.toString(offset));
        }
        return token;
    }

	/*
	 * Tokenizes the string (sentence?) and runs morphoanalysis on each word.
	 */
	public static LinkedList<Word> tokenize(Analyzer morphoAnalyzer, String chunk) {
		LinkedList<Word> tokens = new LinkedList<Word>();
		if (chunk == null) return tokens;
		
		Trie automats = new Trie(morphoAnalyzer.automats);

	    //bug fix - pievienota beigās whitespace
		String str = chunk+" ";
		//workaround dubultapostrofu izvirtībai
        str = str.replaceAll("''", "\u200B''"); // FIXME mēs te mazliet izčakarējam accumulatedWhitespace on Offsetus
        //workaround teikuma beigu saīsinājumiem utt
        str = str.replaceAll("([\\p{L}\\d])\\.(\\p{Z})*$", "$1\u200B.$2"); // FIXME mēs te mazliet izčakarējam accumulatedWhitespace un offsetus
//        str = str.replaceAll("([\\d])\\.(\\p{Z})*$", "$1\u200B.$2"); // FIXME mēs te mazliet izčakarējam accumulatedWhitespace un offsetus

        // te tiek ciklā doti visi tekstā esošie vārdi uz morfoanalīzi.
        int progress = 0;
		boolean inApostrophes=false;
		Status statuss = Status.IN_SPACE;
		StringBuilder accumulatedWhitespace = new StringBuilder();
		int lastGoodEnd=0;
		boolean canEndInNextStep=false;
		
		for (int i = 0; i < str.length(); i++) {
			switch (statuss) {
			case IN_SPACE:
				if (!Splitting.isSpace(str.charAt(i))) {
					if (str.charAt(i)=='\'') inApostrophes=true;

					automats.reset(); //atjauno automāta stāvokli
					automats.findNextBranch(str.charAt(i)); //atrod pirmo derīgo zaru
					
					if(automats.status()>0) { //pārbauda vai automātā atrada meklēto simbolu
						//ja atrada
						statuss=Status.IN_WORD;
						progress=i;
						//pārbauda vai ar to var arī virkne beigties
						canEndInNextStep = (automats.status()==2);
					} else {
						//ja neatrada, pievieno vienu simbolu un mēģina vēl
                        tokens.add( formToken(morphoAnalyzer, str, i, i+1, accumulatedWhitespace));
						accumulatedWhitespace = new StringBuilder();
					}
				} else {
				   accumulatedWhitespace.append(str.charAt(i));
                }
				break;
			case IN_WORD:
				//pārbauda vai ir atrastas potenciālās beigas
		        if (canEndInNextStep==true &&
			            (Splitting.isSeparator(str.charAt(i)) || !Character.isLetter((i>0 ? str.charAt(i-1) : 0) ) ) )
				{
					lastGoodEnd=i;
					if(str.charAt(i)=='\'' && inApostrophes) {
						tokens.add( formToken(morphoAnalyzer, str, progress, i, accumulatedWhitespace));
                        accumulatedWhitespace = new StringBuilder();
                        tokens.add( formToken(morphoAnalyzer, str, i, i+1, accumulatedWhitespace));
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
					//ja neatrada, pārbauda vai automāta darbības laikā tika atrasta potenciālā beigu pozīcija
					if (lastGoodEnd>progress) {
                        tokens.add( formToken(morphoAnalyzer, str, progress, lastGoodEnd, accumulatedWhitespace));
						i=lastGoodEnd-1;
						statuss = Status.IN_SPACE;
                        accumulatedWhitespace = new StringBuilder();
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
                            //vispār šis ir fishy. FIXME
                            tokens.add( formToken(morphoAnalyzer, str, i,i+1, accumulatedWhitespace));
							statuss = Status.IN_SPACE;
                            accumulatedWhitespace = new StringBuilder();
						}
					}
				}				
				break;
			}
		} // for i..
		
		
		if (statuss == Status.IN_WORD) {
			tokens.add( formToken(morphoAnalyzer, str, progress, str.length(), accumulatedWhitespace));
		}

		tokens = new LinkedList<>(tokens.stream().filter(s -> s.getToken().length() > 0).collect(Collectors.toList()));

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
            if (sentence.size() == 0) {
                if ((word.isRecognized() && word.getBestWordform().getTag().equalsIgnoreCase("zq")) || word.getToken().equals(")")) {
                    // Pārbaudam vai iepriekšējā teikuma beigās ir punkts
                    if (!result.isEmpty() && !result.getLast().isEmpty()) {
                        String prevtoken = result.getLast().getLast().getToken();
                        if (prevtoken.equals(".") || prevtoken.equals("!") || prevtoken.equals("?") || prevtoken.equals("\"")) {
                            result.getLast().add(word);
                            continue;
                        }
                    }
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
