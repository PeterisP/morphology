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
package lv.semti.morphology.corpus;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class handles frequency statistics that assess the more probable
 * analysis variant.
 */
public class Statistics {
	
	/**
	 * Default filename for statistics file.
	 */
	public final static String DEFAULT_STATISTICS_FILE = "Statistics.xml";
	public double lexemeWeight = 100; //How much lexeme count should be weighed as a multiple of ending count 
	
	/**
	 * Lexeme frequencies, indexed by lexeme IDs.
	 */
	public HashMap<Integer, Integer> lexemeFrequency = new HashMap<Integer, Integer>();
	/**
	 * Ending frequencies, indexed by ending IDs.
	 */
	public HashMap<Integer, Integer> endingFrequency = new HashMap<Integer, Integer>();
	
	/**
	 * Add one occurrence of one lexeme.
	 * @param lexemeId	lexeme identifier.
	 */
	public void addLexeme(int lexemeId) {
		int count = 1;
		if (lexemeFrequency.get(lexemeId) != null)
			count = lexemeFrequency.get(lexemeId) + 1;
		lexemeFrequency.put(lexemeId, count);
	}
	/**
	 * Add one occurrence of one ending.
	 * @param endingId	ending identifier.
	 */
	public void addEnding(int endingId) {
		int count = 1;
		if (endingFrequency.get(endingId) != null)
			count = endingFrequency.get(endingId) + 1;
		endingFrequency.put(endingId, count);
	}

	/**
	 * Convert frequency data in XML format.
	 * @param stream	output stream.
	 */
	public void toXML (Writer stream)
	throws IOException {
		stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		stream.write("<Statistika>\n");
		stream.write("<Galotņu_biežums\n");
		for (Entry<Integer,Integer> tuple : endingFrequency.entrySet()) {
			stream.write(" Galotne_"+tuple.getKey().toString()+"=\""+tuple.getValue().toString()+"\"");
		}
		stream.write("/>\n");

		stream.write("<Leksēmu_biežums\n");
		for (Entry<Integer,Integer> tuple : lexemeFrequency.entrySet()) {
			stream.write(" Leksēma_"+tuple.getKey().toString()+"=\""+tuple.getValue().toString()+"\"");
		}
		stream.write("/>\n");
		stream.write("</Statistika>\n");
		stream.flush();
	}

	/**
	 * Create empty statistics object.
	 */
	public Statistics() {
		// var arī bez nekā
	}

	/**
	 * Create statistics object from XML file.
	 * @param fileName	input file.
	 */
	public Statistics(String fileName)
	throws SAXException, IOException, ParserConfigurationException {

		Document doc = null;

		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = docBuilder.parse(new File(fileName));

		Node node = doc.getDocumentElement();
		if (!node.getNodeName().equalsIgnoreCase("Statistika"))
			throw new Error("Node " + node.getNodeName() + " nav Statistika!");

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Galotņu_biežums"))
				for (int j = 0; j < nodes.item(i).getAttributes().getLength(); j++) {
					Node n = nodes.item(i).getAttributes().item(j);
					int endingId = Integer.parseInt(n.getNodeName().substring(n.getNodeName().indexOf('_')+1));
					int count = Integer.parseInt(n.getTextContent());
					endingFrequency.put(endingId, count);
				}
			if (nodes.item(i).getNodeName().equals("Leksēmu_biežums"))
				for (int j = 0; j < nodes.item(i).getAttributes().getLength(); j++) {
					Node n = nodes.item(i).getAttributes().item(j);
					int lexemeId = Integer.parseInt(n.getNodeName().substring(n.getNodeName().indexOf('_')+1));
					int count = Integer.parseInt(n.getTextContent());
					lexemeFrequency.put(lexemeId, count);
				}
		}
	}

	/**
	 * Cumulative frequency estimate for given wordform.
	 * @param wordform	wordform to describe.
	 * @return			lexeme frequency + ending frequency.
	 */
	public double getEstimate(AttributeValues wordform) {
		int estimate = 1;
		String endingIdStr = wordform.getValue(AttributeNames.i_EndingID);
		int endingId = (endingIdStr == null) ? -1 : Integer.parseInt(endingIdStr);
		if (endingFrequency.get(endingId) != null) estimate += endingFrequency.get(endingId);

		String lexemeIdStr = wordform.getValue(AttributeNames.i_LexemeID);
		int lexemeId = (lexemeIdStr == null) ? -1 : Integer.parseInt(lexemeIdStr);
		if (lexemeFrequency.get(lexemeId) != null) estimate += lexemeFrequency.get(lexemeId) * lexemeWeight;

		return estimate;
	}


}
