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
package lv.semti.morphology.attributes;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.AbstractCollection;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Peteris Paikens
 * Kontrolēšana, vai sarakstītie īpašību-vērtību pāri atbilst pieļautajiem zināmajiem variantiem
 */
public class TagSet {

	private final static String DEFAULT_TAGSET_FILE = "dist/TagSet.xml";
	
	private static TagSet ref; //Reference uz singletonu
	
	private LinkedList<Attribute> attributes = new LinkedList<Attribute> ();

	private TagSet() throws SAXException, IOException, ParserConfigurationException {
		this(DEFAULT_TAGSET_FILE);
	}
	
	private TagSet(String fileName) throws SAXException, IOException, ParserConfigurationException{
		Document doc = null;
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = docBuilder.parse(new File(fileName));

		Node node = doc.getDocumentElement();
		if (!node.getNodeName().equalsIgnoreCase("TagSet")) throw new Error("Node " + node.getNodeName() + ", but TagSet was expected");

		NodeList nodes = node.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Attribute"))
				attributes.add(new FixedAttribute(nodes.item(i)));
			else if (nodes.item(i).getNodeName().equals("FreeAttribute"))
				attributes.add(new FreeAttribute(nodes.item(i)));
		}
	}
	
	public static synchronized TagSet getTagSet(){
		if (ref == null) { // Ja nav vēl ielasīts, ielasam no faila
			try {
				ref = new TagSet();
			} catch (Exception e) {
				try {
					ref = new TagSet("TagSet.xml");
				} catch (Exception e2) {
					e.printStackTrace();
				}
			} 
		}
		return ref;
	}

	private LinkedList<Attribute> getAttribute(String attributeName, String language){
		LinkedList<Attribute> result = new LinkedList<Attribute>();
		for (Attribute attribute : attributes) {
			String attributeName2 = null;
			if (language.equals("LV")) attributeName2 = attribute.attributeLV;
			if (language.equals("EN")) attributeName2 = attribute.attributeEN;
			
			if (attributeName.equals(attributeName2)) result.add(attribute);
		}
		return result;
	}

	public String validate(AttributeValues attributesToBeChecked, String language) {
		for (Entry<String,String> pāris : attributesToBeChecked.entrySet()) {
			LinkedList<Attribute> attrs = getAttribute(pāris.getKey(), language);
			if (attributes.size() < 1)
				return String.format("Nesaprasta īpašība %s !!", pāris.getKey());
			boolean allowed = false;
			for (Attribute a : attrs)
				if (a.isAllowed(pāris.getValue())) allowed = true;
			
			if (!allowed)
				return String.format("Īpašība %s : %s neder!", pāris.getKey(), pāris.getValue());
		}
		return null;
	}

	public void toXML (Writer pipe) throws IOException {
		pipe.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		pipe.write("<TagSet>\n");
		for (Attribute attribute : attributes) {
			attribute.toXML(pipe);
		}
		pipe.write("</TagSet>\n");
		pipe.flush();
	}

	public Object[] getAllowedValues(String attributeName, String language) {
		LinkedList<String> result = new LinkedList<String>();
		LinkedList<Attribute> attrs = getAttribute(attributeName, language); 
		for (Attribute a : attrs) {
			for (AttributeValue av : a.getAllowedValues(language)) {
				if (!result.contains(av.value)) result.add(av.value);
			}
		}
		return result.toArray();
	}
	
	public AbstractCollection<String> allowedAttributes(String language) {
		LinkedList<String> result = new LinkedList<String>();
		for (Attribute attribute : attributes) {
			if (language.equals("LV")) result.add(attribute.attributeLV);
			if (language.equals("EN")) result.add(attribute.attributeEN);			
		}
		
		return result;
	}
	
	/**
	 * Convert SemTi-Kamols markup tag to internal attribute-value structure.
	 */
	public AttributeValues fromKamolsMarkup(String tag) {
		AttributeValues values = new AttributeValues();
		if (tag.equals("")) return values;
		
		FixedAttribute postag = null;
		for (Attribute attribute : attributes) 
			if (attribute instanceof FixedAttribute) {
				FixedAttribute fattribute = (FixedAttribute) attribute;
				if (fattribute.markupPos == 0) postag = fattribute;
			}
		if (postag == null) return values;
		AttributeValue av_pos = postag.getTagValue(tag.charAt(0));
		if (av_pos==null) {
			System.out.println("nesapratu pos tagam '"+tag+"'");
			return values;
		}
		String pos = av_pos.value;
		if (pos.equalsIgnoreCase(AttributeNames.v_Verb) && tag.length() > 3 && tag.charAt(3) == 'p') {
			pos = AttributeNames.v_Participle;
		}
		postag.addValue(values, tag.charAt(0));
		
		for (Attribute attribute : attributes) 
			if (attribute instanceof FixedAttribute) {
				FixedAttribute fattribute = (FixedAttribute) attribute;
				if (fattribute.matchPos(pos) && fattribute.markupPos < tag.length()) 
					fattribute.addValue(values, tag.charAt(fattribute.markupPos));
			}
		
		return values;
	}
	
	public String toKamolsMarkup(AttributeValues values) {
		LinkedList<Attribute> posoptions = getAttribute(AttributeNames.i_PartOfSpeech, "LV");
		assert (posoptions.size()==1) : "Tagset.xml jābūt tieši 1 elementam 'Vārdšķira'";
		FixedAttribute postag = (FixedAttribute) posoptions.get(0);
		
		if (postag == null) return "-"; //FIXME - neesmu droshs ka "-" ir pareizaakais variants
		String result = postag.markValue(values, "");
		String pos = values.getValue(AttributeNames.i_PartOfSpeech);
		if (pos == null) return "-"; //FIXME - neesmu droshs ka "-" ir pareizaakais variants
		if (pos.equalsIgnoreCase(AttributeNames.v_Verb) && values.isMatchingStrong(AttributeNames.i_Izteiksme, AttributeNames.v_Participle)) {
			pos = AttributeNames.v_Participle;
		}
		
		for (Attribute attribute : attributes) 
			if (attribute instanceof FixedAttribute) {
				FixedAttribute fattribute = (FixedAttribute) attribute;
				if (fattribute.matchPos(pos))
					result = fattribute.markValue(values, result);
			}
		
		return result;
	}
	
}
