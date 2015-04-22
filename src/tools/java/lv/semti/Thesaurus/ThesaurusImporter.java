/*******************************************************************************
 * Copyright 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Lauma Pretkalniņa, Pēteris Paikens
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

package lv.semti.Thesaurus;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;


public class ThesaurusImporter {
	
	protected Analyzer analyzer;
	
	public String newLexiconFile = "Lexicon_sv.xml";
	
	public String importSource = "Imports no Tezaura SV " + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	
	/**
	 * 
	 * @param args File name expected as first argument.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String thesaurusFile = args[0];
		ThesaurusImporter importer = new ThesaurusImporter();
		importer.addThesaurusJsonToLexicon(thesaurusFile);
		
	}
	
	/**
	 * Initialize analyzer.
	 * @throws Exception
	 */
	public ThesaurusImporter()
	throws Exception
	{
		analyzer = new Analyzer("dist/Lexicon.xml",
				new ArrayList<String>(Arrays.asList(newLexiconFile)));
		analyzer.guessNouns = true;
		analyzer.guessParticiples = false;
		analyzer.guessVerbs = false;
		analyzer.guessAdjectives = false;
		analyzer.enableDiminutive = false;
		analyzer.enablePrefixes = false;
		analyzer.enableGuessing = false;
		analyzer.meklētsalikteņus = false;
		analyzer.guessInflexibleNouns = true;
		analyzer.setCacheSize(0);
	}
	
	/**
	 * Read JSON file and add all entries where exactly one paradigm is given
	 * to sublexicon.
	 * @param path JSON file to read
	 * @throws IOException file reading error
	 * @throws ParseException JSON parsing error
	 */
	protected void addThesaurusJsonToLexicon(String path)
	throws IOException, ParseException
	{
		// Read JSON
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(path), "UTF-8"));
		//StringBuilder jsontxt = new StringBuilder();
		//while (in.ready())
		//	jsontxt.append(in.readLine());
		JSONParser parser = new JSONParser();
		JSONArray thesaurus = (JSONArray) parser.parse(in);
		
		// Process each entry
		for (Object entry : thesaurus)
			addEntryToLexicon((JSONObject) entry);
		
		analyzer.toXML_sub(newLexiconFile, importSource);
		in.close();
	}
	
	/**
	 * Analyze single thesaurus entry (one element in the JSON array).
	 * @param entry 
	 */
	protected void addEntryToLexicon(JSONObject entry)
	{
		addHeaderToLexicon((JSONObject)entry.get("Header"));
		JSONArray derivs = (JSONArray) entry.get("Derivatives");
		if (derivs != null)
			for (Object header : derivs)
				addHeaderToLexicon((JSONObject)((JSONObject)header).get("Header"));
	}
	
	/**
	 * Analyze one "Header" element of an entry.
	 * @param header
	 */
	protected void addHeaderToLexicon(JSONObject header)
	{
		if (header == null) return;
		if (header.get("Lemma") == null)
		{
			System.out.println("Header bez Lemmas:"  + header.toJSONString());
			return;
		}
		try
		{
			String lemma = (String)header.get("Lemma");
			Word w = analyzer.analyzeLemma(lemma);
			if (w.isRecognized()) 
				return; //throw new Exception(String.format("Vārds %s jau ir leksikonā", lemma));
			
			if (header.get("Gram") == null) throw new Exception(String.format("Vārdam %s nav gramatikas", lemma));
			if (((JSONObject)header.get("Gram")).get("Paradigm") == null) throw new Exception(String.format("Vārdam %s nav atrastas paradigmas", lemma));
			JSONArray paradigms = (JSONArray) ((JSONObject)header.get("Gram")).get("Paradigm");
			if (paradigms.size() != 1) throw new Exception(String.format("Vārdam %s ir %d paradigmas", lemma, paradigms.size()));
			int paradigmID = Integer.parseInt((String)paradigms.get(0));
			if (paradigmID == 0) throw new Exception(String.format("Vārdam %s paradigma ir 0", lemma));
			
			Lexeme l = analyzer.createLexemeFromParadigm(lemma, paradigmID, importSource);
			if (l == null) throw new Exception(String.format("createLexemeFromParadigm nofailoja uz %s / %d", lemma, paradigmID));
			if (l.getParadigmID() == 29)
			{ // Hardcoded unflexible words
				l.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				if (((JSONObject)header.get("Gram")).get("Flags") != null)
				{
					JSONArray flags = (JSONArray) ((JSONObject)header.get("Gram")).get("Flags");
					if (flags.contains("Saīsinājums"))
						l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Abbreviation);
					else if (flags.contains("Vārds svešvalodā"))
						l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Foreign); 
					else if (flags.contains("Izsauksmes vārds"))
						l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Interjection); 
				}
				
			}
			//System.out.printf("Jess %s\n", lemma);
		} catch (Exception e) {
			System.err.printf("Nesanāca ielikt leksēmu :(%s\n",e.getMessage());
			if (e.getMessage() == null) e.printStackTrace();
		}
	}
	
/*	public void addToLexicon(Analyzer analizators, String importSource) {
		try {
			String lemma = this.lemma.text;
			Word w = analizators.analyzeLemma(lemma);
			if (w.isRecognized()) 
				return; //throw new Exception(String.format("Vārds %s jau ir leksikonā", lemma));
			
			if (this.gram == null) throw new Exception(String.format("Vārdam %s nav gramatikas", lemma));
			if (this.gram.paradigm == null) throw new Exception(String.format("Vārdam %s nav atrastas paradigmas", lemma));
			HashSet<Integer> paradigms = this.gram.paradigm;
			if (paradigms.size() != 1) throw new Exception(String.format("Vārdam %s ir %d paradigmas", lemma, paradigms.size()));
			int paradigmID = paradigms.iterator().next();
						
			Lexeme l = analizators.createLexemeFromParadigm(lemma, paradigmID, importSource);
			if (l == null) throw new Exception(String.format("createLexemeFromParadigm nofailoja uz %s / %d", lemma, paradigmID));
			if (l.getParadigmID() == 29) { // Hardcoded unflexible words
				l.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				if (this.gram.flags.contains("Saīsinājums"))
					l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Abbreviation);
				else if (this.gram.flags.contains("Vārds svešvalodā"))
					l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Foreign); 
				else if (this.gram.flags.contains("Izsauksmes vārds"))
					l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Interjection); 
				
			}
			//System.out.printf("Jess %s\n", lemma);
		} catch (Exception e) {
			System.err.printf("Nesanāca ielikt leksēmu :(%s\n",e.getMessage());
			if (e.getMessage() == null) e.printStackTrace();
		}
	}*/
	
/*	public void addToLexicon(Analyzer analizators, String importSource) {
		this.head.addToLexicon(analizators, importSource);
		if (this.derivs != null)
			for (Header h : this.derivs)
				h.addToLexicon(analizators, importSource);		
	}*/

}
