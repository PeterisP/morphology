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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lv.semti.Thesaurus.struct.ThesaurusEntry;
import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

public class ThesaurusImport {

	public static boolean addToLexicon = false;
	/**
	 * 
	 * @param args File name expected as first argument.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String thesaurusFile = args[0];
		String goodOutputFile = "tezaurs-good.json";
		String noParadigm = "tezaurs-noParadigm.json";
		String badOutputFile = "tezaurs-bad.json";
		String newLexiconFile = "Lexicon_sv.xml";
		String importSource = "Imports no Tezaura SV " + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		Analyzer analizators = null;
		if (addToLexicon)
		{
			analizators = new Analyzer("dist/Lexicon.xml",
					new ArrayList<String>(Arrays.asList(newLexiconFile)));
			analizators.guessNouns = true;
			analizators.guessParticiples = false;
			analizators.guessVerbs = false;
			analizators.guessAdjectives = false;
			analizators.enableDiminutive = false;
			analizators.enablePrefixes = false;
			analizators.enableGuessing = false;
			analizators.meklētsalikteņus = false;
			analizators.guessInflexibleNouns = true;
			analizators.setCacheSize(0);
		}
		
		// Load Thesaurus file.
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = docBuilder.parse(new File(thesaurusFile));
		Node node = doc.getDocumentElement();
		if (!node.getNodeName().equalsIgnoreCase("tezaurs"))
			throw new Error("Node '" + node.getNodeName() + "' but tezaurs expected!");
		
		//List<ThesaurusEntry> entries = new LinkedList<ThesaurusEntry>();
		
		// Output.
		BufferedWriter goodOut = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(goodOutputFile), "UTF-8"));
		BufferedWriter noParadigmOut = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(noParadigm), "UTF-8"));
		BufferedWriter badOut = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(badOutputFile), "UTF-8"));
		
		// Process each node.
		NodeList thesaurusEntries = node.getChildNodes(); // Thesaurus entries
		int badCount = 0;
		for (int i = 0; i < thesaurusEntries.getLength(); i++) {
			Node sNode = thesaurusEntries.item(i);
			if (sNode.getNodeName().equals("s")) {
				ThesaurusEntry entry = new ThesaurusEntry(sNode);
				if (!entry.inBlacklist()) { // Blacklisted entries are not included in output logs.			
					//entries.add(entry);
					if (entry.hasParadigm() && !entry.hasUnparsedGram()) {
						// Looks good, let's write it to all the proper output
						goodOut.write(entry.toJSON() + "\n");
						if (addToLexicon)
							entry.addToLexicon(analizators, importSource);
					} else if (!entry.hasParadigm() && !entry.hasUnparsedGram())
						noParadigmOut.write(entry.toJSON() + "\n");
					else {
						badOut.write(entry.toJSON() + "\n");
						badCount++;
					}
				}
			}
			else if (!sNode.getNodeName().equals("#text")) { // Text nodes here are ignored.
				goodOut.close();
				noParadigmOut.close();
				badOut.close();				
				throw new Error("Node '" + sNode.getNodeName() + "' but s (šķirklis) expected!");
			}
			//if (badCount >= 40) break;	//Temporary.
		}
		
		goodOut.close();
		noParadigmOut.close();
		badOut.close();
		
		if (addToLexicon) analizators.toXML_sub(newLexiconFile, importSource);
	}

}
