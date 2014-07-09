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

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * 
 * @author Pēteris
 * Atribūti, kam atļauts tikai fiksēts saraksts ar vērtībām
 *
 */
class FixedAttribute extends Attribute {
	LinkedList<AttributeValue> allowedValues = new LinkedList<AttributeValue>();
	String partOfSpeech;
	int markupPos = -1;
	
	FixedAttribute (Node node) {
		super(node);
		
		Node n = node.getAttributes().getNamedItem("PartOfSpeech");
		if (n != null)
			this.partOfSpeech = n.getTextContent();
		n = node.getAttributes().getNamedItem("MarkupPos");
		if (n != null)
			this.markupPos = Integer.parseInt(n.getTextContent());
		
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node value = nodes.item(i);
			if (value.getNodeName().equals("Value")) {
				AttributeValue v = new AttributeValue(value);
				allowedValues.add(v);
			}
		}
	}
	
	protected void toXMLData(Writer straume) throws IOException {
		for (AttributeValue vērtība : allowedValues) {
			straume.write(vērtība.getXml()); 
		}
	}	
	
	public boolean isAllowed(String value) {
		for (AttributeValue vērtība : allowedValues)
			if (vērtība.accept(value)) return true;
		return false;
	}

	@Override
	public List<String> getAllowedValues(String language) {
		LinkedList<String> result = new LinkedList<String>();
		for (AttributeValue av: allowedValues) {
			if (language.equalsIgnoreCase("LV"))
				result.add(av.valueLV);
			if (language.equalsIgnoreCase("EN"))
				result.add(av.valueEN);
		}
		return result;
	}

	@Override
	protected String xmlTagName() {
		return "Attribute";
	}

	public AttributeValue getTagValue(char tag) {
		for (AttributeValue av : allowedValues)
			if (av.markupTag == tag) return av;
		
		return null;
	}

	public void addValue(AttributeValues values, char tag, String language) {
		AttributeValue tagValue = getTagValue(tag);
		if (tagValue != null) {
			if (language.equalsIgnoreCase("LV"))
				values.addAttribute(this.attributeLV, tagValue.valueLV);
			if (language.equalsIgnoreCase("EN"))
				values.addAttribute(this.attributeEN, tagValue.valueEN);
		}
	}

	/**
	 * Checks if this attribute is valid for the part of speech
	 * @param value
	 * @return
	 */
	public boolean matchPos(String value) {
		if (partOfSpeech == null) return (markupPos >= 0);
		return partOfSpeech.equalsIgnoreCase(value);
	}

	public String markValue(AttributeValues values, String tag) {
		if (markupPos<0) return tag;
		String value = values.getValue(this.attributeLV); //FIXME - EN atribūti?
		AttributeValue the_av = null;
		for (AttributeValue av : allowedValues) 
			if (av.valueLV.equalsIgnoreCase(value))
				the_av = av;
		if (the_av == null) return tag;
		
		StringBuilder result;
		if (tag.equalsIgnoreCase("") && the_av.defaultTags != null) 
			result = new StringBuilder(the_av.defaultTags);
		else
			result = new StringBuilder(String.format("%-"+(markupPos+1)+"s", tag));		
		result.setCharAt(markupPos, the_av.markupTag);
		
		return result.toString();
	}

	/**
	 * Finds the appropriate english translation of a LV value for this attribute
	 * @param valueLV
	 * @return The translation, or null if it's not found
	 */
	public String toEnglish(String valueLV) {
		for (AttributeValue option : allowedValues) 
			if (option.valueLV.equalsIgnoreCase(valueLV)) 
				return option.valueEN;
		return null;
	}
}

class AttributeValue {
	String valueLV="_missing_";
	String valueEN="_missing_";
	char markupTag=' ';
	String defaultTags = null;
	
	protected AttributeValue(Node value) {
		Node lv = value.getAttributes().getNamedItem("LV");
		if (lv != null) 
			this.valueLV = lv.getTextContent();
			
		Node en = value.getAttributes().getNamedItem("EN");
		if (en != null) 
			this.valueEN = en.getTextContent();

		Node tag = value.getAttributes().getNamedItem("Tag");
		if (tag != null)
			this.markupTag = tag.getTextContent().charAt(0);
			
		Node defaults = value.getAttributes().getNamedItem("DefaultTags");
		if (defaults != null)
			this.defaultTags = defaults.getTextContent();
	}
	
	protected String getXml() {
		StringBuilder sb = new StringBuilder("<Value LV=\"" + valueLV + "\"");
		if (!valueEN.equalsIgnoreCase("_missing"))
			sb.append(" EN=\"" + valueEN + "\"");
		if (markupTag != ' ')
			sb.append(" Tag=\"" + markupTag + "\"");
		if (defaultTags != null)
			sb.append(" DefaultTags=\"" + defaultTags + "\"");
		sb.append("/>\n");
		return sb.toString();
	}
	
	protected boolean accept(String compareTo) {
		return valueLV.equalsIgnoreCase(compareTo); //FIXME - EN valoda ?
	}
}