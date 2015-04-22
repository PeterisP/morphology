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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lv.semti.Thesaurus.struct.Entry;

public class DictionaryXmlToJsonUI {

	/**
	 * Create file with all pronunciation.
	 */
	public static boolean makePronunceList = true;
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
		String pronunciationOutputFile = "tezaurs-pronunce.txt";
		
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
		goodOut.write("[\n");
		BufferedWriter noParadigmOut = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(noParadigm), "UTF-8"));
		noParadigmOut.write("[\n");
		BufferedWriter badOut = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(badOutputFile), "UTF-8"));
		badOut.write("[\n");
		BufferedWriter pronunceOut = null;
		if (makePronunceList)
			pronunceOut = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(pronunciationOutputFile), "UTF-8"));
		
		// Process each node.
		NodeList thesaurusEntries = node.getChildNodes(); // Thesaurus entries
		int badCount = 0;
		for (int i = 0; i < thesaurusEntries.getLength(); i++) {
			Node sNode = thesaurusEntries.item(i);
			if (sNode.getNodeName().equals("s")) {
				Entry entry = new Entry(sNode);
				// Print out all pronunciations.
				if (makePronunceList)
				{
					for (String p : entry.collectPronunciations())
						pronunceOut.write(p + "\t" + entry.head.lemma.text + "\t" + entry.homId + "\n");
				}
				if (!entry.inBlacklist()) { // Blacklisted entries are not included in output logs.			
					//entries.add(entry);
					if (entry.hasParadigm() && !entry.hasUnparsedGram()) {
						// Looks good, let's write it to all the proper output
						goodOut.write(entry.toJSON() + ",\n");
					} else if (!entry.hasParadigm() && !entry.hasUnparsedGram())
						noParadigmOut.write(entry.toJSON() + ",\n");
					else {
						badOut.write(entry.toJSON() + ",\n");
						badCount++;
					}
				}
			}
			else if (!sNode.getNodeName().equals("#text")) { // Text nodes here are ignored.
				goodOut.close();
				noParadigmOut.close();
				badOut.close();
				if (makePronunceList) pronunceOut.close();
				throw new Error("Node '" + sNode.getNodeName() + "' but s (šķirklis) expected!");
			}
			//if (badCount >= 40) break;	//Temporary.
		}
		
		goodOut.write("]");
		goodOut.close();
		noParadigmOut.write("]");
		noParadigmOut.close();
		badOut.write("]");
		badOut.close();
		if (makePronunceList) pronunceOut.close();
		
	}
	


}
