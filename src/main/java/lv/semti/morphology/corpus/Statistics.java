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
package lv.semti.morphology.corpus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	public double lexemeWeight = 1000; //How much lexeme count should be weighed as a multiple of ending count
	// Determined/verified empirically by testing on corpus; coefficient of 100 instead of 1000 gives 0.2% decrease
	
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

		stream.write("<Leksēmu_biežums>\n");
		for (Entry<Integer, Integer> tuple : lexemeFrequency.entrySet()) {
			if (tuple.getValue() > 1) {
				stream.write("  <Leksēma id=\"" + tuple.getKey() + "\" count=\"" + tuple.getValue() + "\"/>\n");
			}
		}
		stream.write("</Leksēmu_biežums>\n");
		stream.write("</Statistika>\n");
		stream.flush();
	}

	private static Statistics singleton;
	
	public static Statistics getStatistics() {
		if (singleton == null)
			try {
			    singleton = new Statistics(DEFAULT_STATISTICS_FILE);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                singleton = new Statistics();
            }
		return singleton;
	}
	
	public static Statistics getStatistics(String fileName) {
		if (singleton == null)
			try {
				singleton = new Statistics(fileName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
                singleton = new Statistics();
			}
		return singleton;
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
	private Statistics(String fileName)
	throws SAXException, IOException, ParserConfigurationException {

		Document doc = null;

		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileName);
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = docBuilder.parse(stream);

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
			if (nodes.item(i).getNodeName().equals("Leksēmu_biežums")) {
				NodeList children = nodes.item(i).getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("Leksēma")) {
						Element el = (Element) child;
						int lexemeId = Integer.parseInt(el.getAttribute("id"));
						int count = Integer.parseInt(el.getAttribute("count"));
						lexemeFrequency.put(lexemeId, count);
					}
				}
			}
		}
	}

	/**
	 * Cumulative frequency estimate for given wordform.
	 * @param wordform	wordform to describe.
	 * @return			lexeme frequency + ending frequency.
	 */
	public double getEstimate(AttributeValues wordform) {
		double estimate = 0.1;
		String endingIdStr = wordform.getValue(AttributeNames.i_EndingID);
		int endingId = (endingIdStr == null) ? -1 : Integer.parseInt(endingIdStr);
		if (endingFrequency.get(endingId) != null) estimate += endingFrequency.get(endingId);

		String lexemeIdStr = wordform.getValue(AttributeNames.i_LexemeID);
		int lexemeId = (lexemeIdStr == null) ? -1 : Integer.parseInt(lexemeIdStr);
		if (lexemeFrequency.get(lexemeId) != null) estimate += lexemeFrequency.get(lexemeId) * lexemeWeight;

		return estimate;
	}


}
