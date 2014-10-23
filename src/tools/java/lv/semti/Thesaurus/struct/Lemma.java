/*******************************************************************************
 * Copyright 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Lauma Pretkalniņa
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
package lv.semti.Thesaurus.struct;


import lv.semti.Thesaurus.utils.HasToJSON;

import org.json.simple.JSONObject;
import org.w3c.dom.Node;

/**
 * vf (vārdforma) field.
 */
public class Lemma implements HasToJSON
{
	public String text;
	/**
	 * ru (runa) field, optional here.
	 */
	public String pronunciation;
	
	public Lemma ()
	{
		text = null;
		pronunciation = null;
	}
	public Lemma (String lemma)
	{
		text = lemma;
		pronunciation = null;
	}		
	public Lemma (Node vfNode)
	{
		text = vfNode.getTextContent();
		pronunciation = ((org.w3c.dom.Element)vfNode).getAttribute("ru");
		if ("".equals(pronunciation)) pronunciation = null;
		if (pronunciation == null) return;
		if (pronunciation.startsWith("["))
			pronunciation = pronunciation.substring(1);
		if (pronunciation.endsWith("]"))
			pronunciation = pronunciation.substring(0, pronunciation.length() - 1);
	}
	
	/**
	 *  Set lemma and check if the information isn't already filled, to
	 *  detect possible overwritten data.
	 */
	public void set(String lemmaText) {
		if (text != null)
			System.err.printf(
				"Duplicate info for field 'lemma' : '%s' and '%s'", text,
				lemmaText);
		text = lemmaText;
	}
	
	// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
	@Override
	public boolean equals (Object o)
	{
		if (o == null) return false;
		if (this.getClass() != o.getClass()) return false;
		if ((text == null && ((Lemma)o).text == null || text != null && text.equals(((Lemma)o).text))
				&& (pronunciation == null && ((Lemma)o).pronunciation == null
				|| pronunciation != null && pronunciation.equals(((Lemma)o).pronunciation)))
			return true;
		else return false;
	}
	
	// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
	@Override
	public int hashCode()
	{
		return 1721 *(text == null ? 1 : text.hashCode())
				+ (pronunciation == null ? 1 : pronunciation.hashCode());
	}
	
	public String toJSON()
	{
		StringBuilder res = new StringBuilder();
		res.append(String.format("\"Lemma\":\"%s\"", JSONObject.escape(text)));
		if (pronunciation != null)
		{
			res.append(", \"Pronunciation\":\"");
			res.append(JSONObject.escape(pronunciation.toString()));
			res.append("\"");
		}
		return res.toString();
	}
}