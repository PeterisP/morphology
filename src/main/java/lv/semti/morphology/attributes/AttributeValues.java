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
package lv.semti.morphology.attributes;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import lv.semti.morphology.analyzer.MarkupConverter;

import org.json.simple.JSONValue;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//TODO - būtu vienkārši jāinherito HashMap<String, String>
public class AttributeValues implements FeatureStructure, Cloneable {
	protected HashMap<String, String> attributes = new HashMap<String, String>();
	
	public void describe() {
		PrintWriter izeja;
		try {
			izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
			this.describe(izeja);
			izeja.flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void describe(PrintStream pipe) {
		this.describe(new PrintWriter(pipe));
	}
	
	public void describe(PrintWriter pipe) {
		for (Entry<String,String> īpašība : attributes.entrySet()) {
			pipe.format("\t\t%s = %s%n", īpašība.getKey(),īpašība.getValue());
		}
		pipe.flush();
	}

	public void addAttribute(String attribute, String value) {
		//FIXME - vajag nodalīt īpašību pielikšanu no īpašību aizvietošanas
		attributes.put(attribute, value);
	}

	public void removeAttribute(String attribute) {
		attributes.remove(attribute);
	}

	public void addAttributes(HashMap<String,String> newAttributes) {
		this.attributes.putAll(newAttributes);
		//FIXME - a ko tad, ja kautkas konfliktē??
	}

	public void addAttributes(AttributeValues newAttributes) {
		this.attributes.putAll(newAttributes.attributes);
		//FIXME - a ko tad, ja kautkas konfliktē??
	}
	
	/**
	 * Remove all attributes except those listed.
	 */
	public void filterAttributes(Collection<String> leaveAttributes) {
		attributes.keySet().retainAll(leaveAttributes);
	}

	/***
	 *  Returns null if attribute does not exist
	 */
	public String getValue(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * Returns true either if the attribute exists and matches the provided
	 * value or if attribute doesn't exist and provided value is null.  
	 */
	public boolean isMatchingStrong (String attribute, String value) {
		String result = attributes.get(attribute);
		if (result == null && value == null) return true;
		return (result == null) ? false : result.equalsIgnoreCase(value);
	}

	/**
	 * Returns true either if all attributes in provided test set are strongly
	 * matching on this, and if all attributes in this are strongly matching on
	 * attributes provided in test set.
	 */
	public boolean isMatchingStrong(AttributeValues testSet) {
		boolean match = true;
		for (Entry<String,String> aVPair : testSet.entrySet()) {
			if (!this.isMatchingStrong(aVPair.getKey(), aVPair.getValue()))
				match = false;
		}
		for (Entry<String,String> aVPair : this.entrySet()) {
			if (!testSet.isMatchingStrong(aVPair.getKey(), aVPair.getValue()))
				match = false;
		}
		return match;
	}
	
	/**
	 * Returns true either if the attribute exists and matches the provided
	 * value or if attribute doesn't exist.
	 */
	public boolean isMatchingWeak (String attribute, String value) {
		String result = attributes.get(attribute);
		return (result == null) ? true : result.equalsIgnoreCase(value);
	}	// Atshkjiriiba no checkAttribute - ja atribuuta nav, bet padotaa veertiiba nav null.
		// Shii metode dod true, check attribute - false.

	/**
	 * Returns true if all attributes provided in test set weakly matches on
	 * this.
	 */
	public boolean isMatchingWeak(AttributeValues testSet) {
		boolean der = true;
		for (Entry<String,String> pāris : testSet.entrySet()) {
			if (!this.isMatchingWeak(pāris.getKey(), pāris.getValue()))
				der = false;
		}
		return der;
	}

	
	public void toXML (Writer straume) throws IOException {
		straume.write("<Attributes");
		for (Entry<String,String> pāris : attributes.entrySet()) {
			String īpašība = pāris.getKey().replace(" ", "_").replace("\"", "&quot;").replace("&", "&amp;");
			if (īpašība.equals("")) continue;
			String vērtība = pāris.getValue().replace("\"", "&quot;").replace("&", "&amp;");
			straume.write(" "+īpašība+"=\""+vērtība+"\"");
		}
		straume.write("/>");
	}
	
	public String toJSON() {
		return JSONValue.toJSONString(attributes);
	}

	public Entry<String,String> get(int nr) {
	//FIXME - atgriež rediģējamu pāri... netīri kautkā, tas ir kā getteris domāts, nevis rakstīšanai..
	//jāmaina pieeja tur kur to sauc.

		Entry<String,String> rezults = null;
		int i=0;
		for (Entry<String,String> īpašība : attributes.entrySet()) {
			if (i==nr) rezults = īpašība;
			i++;
		}
		return rezults;
	}

	public int size() {
		return attributes.entrySet().size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() throws CloneNotSupportedException {
		try {
			AttributeValues kopija = (AttributeValues)super.clone();
			kopija.attributes = (HashMap<String,String>)attributes.clone();
			return kopija;
        } catch (CloneNotSupportedException e) {
            throw new Error("Gļuks - nu vajag varēt klasi AttributeValues noklonēt.");
        }
	}

	public String getDescription() {
		String ret = "";
		for (Entry<String,String> Īpašība : attributes.entrySet()) {
			if (!Īpašība.getKey().startsWith("Nozīme")) {
			if (ret.length() < 1) {
				ret = Īpašība.getValue();
			} else {
				ret = ret + ", "/* + Īpašība.getKey() + " = "*/ + Īpašība.getValue();
			}
			}
		}
		return ret;
	}

	public Set<Entry<String,String>> entrySet() {
	//FIXME - jākopē, lai nav editējams - vai jāmaina pieeja tur kur šo sauc.
		return attributes.entrySet();
	}

	public AttributeValues(Node node) {
		NodeList nodes = node.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Attributes"))
				for (int j = 0; j < nodes.item(i).getAttributes().getLength(); j++) {
					Node n = nodes.item(i).getAttributes().item(j);
					addAttribute(n.getNodeName().replaceAll("_", " "), n.getTextContent());
				}
		}
	}

	public AttributeValues() {
		//irok
	}

	/**
	 * Creates a new set of AttributeValues, initializing the contents from a source AV object
	 * @param source
	 */
	public AttributeValues(AttributeValues source) {
		this.addAttributes(source);
	}

	public void clear() {
		attributes.clear();
	}
	
	/**
	 * Returns Semti-Kamols style positional morphosyntactic markup tag of this set of attributes
	 * @return
	 */
	public String getTag() {
		return MarkupConverter.toKamolsMarkup(this);
	}	

	/**
	 * Removes a set of attributes that are considered not target of POS/morphotagging; mainly lexical features. 
	 * TODO - confusing name of function?
	 */
	public void removeNonlexicalAttributes() {
		removeAttribute(AttributeNames.i_Transitivity);
		removeAttribute(AttributeNames.i_VerbType);
		removeAttribute(AttributeNames.i_NounType);
		removeAttribute(AttributeNames.i_Declension);
		removeAttribute(AttributeNames.i_Konjugaacija);
		
		removeAttribute(AttributeNames.i_ApstTips);
		removeAttribute(AttributeNames.i_SaikljaTips);
		removeAttribute(AttributeNames.i_SkaitljaTips);
		removeAttribute(AttributeNames.i_AdjectiveType);		
		removeAttribute(AttributeNames.i_Uzbuuve);
		removeAttribute(AttributeNames.i_Order);
		//removeAttribute(AttributeNames.i_VvTips);
		removeAttribute(AttributeNames.i_Noliegums);
		removeAttribute(AttributeNames.i_VietasApstNoziime);
		
		if (isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Preposition)) {			
			removeAttribute(AttributeNames.i_Novietojums);
			//removeAttribute(AttributeNames.i_Rekcija);  // FIXME - may be needed
			//removeAttribute(AttributeNames.i_Number);   // FIXME - may be needed
		}
		
		//par šiem jādomā
		removeAttribute(AttributeNames.i_Degree);
		removeAttribute(AttributeNames.i_Reflexive);
		removeAttribute(AttributeNames.i_Laiks);
		removeAttribute(AttributeNames.i_Voice);
	}
	
	public void removeTechnicalAttributes() {
		removeAttribute(AttributeNames.i_LexemeID);
		removeAttribute(AttributeNames.i_EndingID);
		removeAttribute(AttributeNames.i_ParadigmID);
		removeAttribute(AttributeNames.i_Source);
		removeAttribute(AttributeNames.i_Word);
		removeAttribute(AttributeNames.i_Mija);
		removeAttribute(AttributeNames.i_Guess);
		removeAttribute(AttributeNames.i_Generate);
		removeAttribute(AttributeNames.i_Konjugaacija);
		removeAttribute(AttributeNames.i_Declension);
	}

	public StringBuilder pipeDelimitedEntries() {
		StringBuilder s = new StringBuilder();
		for (Entry<String, String> entry : this.entrySet()) { // visi attributevalue paariishi
			 s.append(entry.getKey().replace(' ', '_'));
			 s.append('=');
			 s.append(entry.getValue().replace(' ', '_'));
			 s.append('|');
		}
		return s;
	}

}
