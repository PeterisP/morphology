/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia;
 * Author: Pēteris Paikens, Imants Borodkins
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

/**
 * Tools for detecting chunk and token bounds.
 * When you edit this, please, update tokenize.pl in Chunker, too!
 */
public class Splitting {
	// Vārdā, atdalītājā, atstarpē
	private enum Status {IN_WORD, IN_DELIMITER, IN_SPACE, IN_EXCEPTION};
	public static Trie automats=new Trie("dist/Exceptions.txt");
	
	
	/**
	 * Determine, if given string is a chunk delimiter.
	 */
	public static boolean isChunkSeperator(String s)
	{
		String seperatorSymbols = ".!?;:<>(){}[]/\n"; //komata te nav
		if (seperatorSymbols.contains(s)) {
			return true;
		}
		return false;
	}

	/**
	 * Determine, if given char is a chunk delimiter.
	 */
	public static boolean isChunkSeperator(char c)
	{
		return isChunkSeperator(String.valueOf(c));
	}
	
	public static boolean isChunkOpener(String s)
	{
		String seperatorSymbols = "<({['\"";
		if (seperatorSymbols.contains(s)) {
			return true;
		}
		return false;
	}

	public static boolean isChunkCloser(String s)
	{
		String seperatorSymbols = ">)}]'\"";
		if (seperatorSymbols.contains(s)) {
			return true;
		}
		return false;
	}
	
	public static boolean isSeperator(char c)
	{
		String seperators=" .?:/!,;\"'`´(){}<>«»-[]—‐‑‒–―‘’‚‛“”„‟′″‴‵‶‷‹›‼‽⁈⁉․‥…";
		return seperators.contains(String.valueOf(c));
	}
	
	/**
	 * Determine, if given string is a punctuation mark.
	 */
	public static boolean isTokenSeperator(String s)
	{
		String seperatorSymbols = ".,!?;:<>()#$%@^&*-~_+={}[]/\\'\"«»„”" +
			"\u0060\u00b4\u00ab\u00bb\u2018\u2019\u201a\u201b\u201c\u201d" +
			"\u201e\u201f\u2032\u2033\u2035\u2036\u2010\u2011\u2012\u2013" +
			"\u2014\u2015"; // Quotation marks and hyphens/horizontal bars.
		if (seperatorSymbols.contains(s)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Determine, if given char is a punctuation mark.
	 */
	public static boolean isTokenSeperator(char c)
	{
		return isTokenSeperator(String.valueOf(c));
	}
	
	/**
	 * Determine, if given string is a punctuation mark (each punctuation mark
	 * of this kind forms separate token).
	 */
	public static boolean isSingleTokenSeperator(String s)
	{
		String seperatorSymbols = "?!;:<>(){}[]/'\\\"~«»„”" +
			"\u0060\u00b4\u00ab\u00bb\u2018\u2019\u201a\u201b\u201c\u201d" +
			"\u201e\u201f\u2032\u2033\u2035\u2036"; // Quotation marks.
		if (seperatorSymbols.contains(s))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Determine, if given char is a punctuation mark (each punctuation
	 * mark of this kind forms separate token).
	 */
	public static boolean isSingleTokenSeperator(char c)
	{
		return isSingleTokenSeperator(String.valueOf(c));
	}	

	/**
	 * Determine, if given string is a whitespace char (space, tab, newline).
	 */
	public static boolean isSpace(String s)
	{
		String seperatorSymbols = " \t\n\r";
		if (seperatorSymbols.contains(s))
		{
			return true;
		}
		return false;
	}

	/**
	 * Determine, if given char is a whitespace char (space, tab, newline).
	 */
	public static boolean isSpace(char c)
	{
		return isSpace(String.valueOf(c));
	}

	public static LinkedList<Word> tokenize(Analyzer morphoAnalyzer, String chunk) {
		
		LinkedList<Word> tokens = new LinkedList<Word>();
		if (chunk == null) return tokens;

		// te tiek ciklā doti visi tekstā esošie vārdi uz morfoanalīzi.    
	    int progress = 0;
	    //bug fix - pievonata beigās whitespace
		String str = chunk+" ";
		boolean inApostrophes=false;
		Status statuss = Status.IN_SPACE;
		
		int lastGoodEnd=0;
		boolean canEndInNextStep=false;
		
		for (int i = 0; i < str.length(); i++) {
			switch (statuss) {
			case IN_SPACE:
				if (!Splitting.isSpace(str.charAt(i))) {
					if(str.charAt(i)=='\'')
					{
						tokens.add( (morphoAnalyzer == null) ? 
								new Word("'") :
								morphoAnalyzer.analyze("'") );
						inApostrophes=true;
					}
					else
					{
						//atjauno automāta stāvokli
						automats.reset();
						//atrod pirmo derīgo zaru
						automats.findNextBranch(str.charAt(i));
						
						if(automats.status()>0) //pārbauda vai atrada meklēto simbolu
						{
							//ja atrada
							statuss=Status.IN_WORD;
							progress=i;
							//pārbauda vai ar to var arī virkne beigties
							if(automats.status()==2)
							{
								canEndInNextStep=true;
							}
							else
							{
								canEndInNextStep=false;
							}
						}
						else
						{
							//ja neatrada, pievieno simbolu rezultātam
							tokens.add( (morphoAnalyzer == null) ? 
									new Word(str.substring(i,i+1)) :
									morphoAnalyzer.analyze(str.substring(i,i+1)) );
						}
					}
				}
				break;
			case IN_WORD:
				//pārbauda vai ir atrastas potenciālās beigas
				if(canEndInNextStep==true && Splitting.isSeperator(str.charAt(i)))
				{
					lastGoodEnd=i;
					if(str.charAt(i)=='\'' && inApostrophes)
					{
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
				if (automats.findNext(str.charAt(i))>0) //ja atrada 
				{
					//pārbauda vai ar to var arī virkne beigties
					if(automats.status()==2)
					{
						canEndInNextStep=true;
					}
				} 
				else  
				{
					//ja neatrada, pārbauda vai darbības laikā tika atrasta potenciālā beigu pozīcija
					if(lastGoodEnd>progress)
					{
						tokens.add( (morphoAnalyzer == null) ? 
								new Word(str.substring(progress,lastGoodEnd)) :
								morphoAnalyzer.analyze(str.substring(progress,lastGoodEnd)) );
						i=lastGoodEnd-1;
						statuss = Status.IN_SPACE;
					}
					else
					{
						i=progress;
						//mēgina atrast nākamo derīgo zaru
						automats.nextBranch();
						automats.findNextBranch(str.charAt(i));
						if(automats.status()>0) //pārbauda vai atrada meklēto simbolu
						{							
							//pārbauda vai ar to var arī virkne beigties
							if(automats.status()==2)
							{
								canEndInNextStep=true;
							}
						}
						else
						{
							//ja neatrada, pievieno simbolu rezultātam
							tokens.add( (morphoAnalyzer == null) ? 
									new Word(str.substring(i,i+1)) :
									morphoAnalyzer.analyze(str.substring(i,i+1)) );
							statuss = Status.IN_SPACE;
						}
					}
				}				
				break;

			}
		}
		if (statuss == Status.IN_WORD) { 
			tokens.add( (morphoAnalyzer == null) ? 
					new Word(str.substring(progress,str.length())) :
					morphoAnalyzer.analyze(str.substring(progress,str.length())) );
		}
		
		return tokens;
	}
	
	
	public static LinkedList<Word> tokenize(Analyzer morphoAnalyzer, String chunk, boolean bruteSplit) {
		if(bruteSplit)
		{
			LinkedList<Word> tokens = new LinkedList<Word>();
			if (chunk == null) return tokens;
			String[] parts_of_string = chunk.split(" ");
			for(String part : parts_of_string) 
			{
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
}
