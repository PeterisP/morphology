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

import java.util.LinkedList;

import lv.semti.Thesaurus.utils.HasToJSON;
import lv.semti.Thesaurus.utils.JSONUtils;


import org.json.simple.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * piem (piemērs) and fraz (frazeoloģisms) fields.
 */
public class Phrase implements HasToJSON
{
	/**
	 * t (teksts) field.
	 */
	public String text;		

	/**
	 * gram field  is optional here.
	 */
	public Gram grammar;

	/**
	 * n field is optional here.
	 */
	public LinkedList<Sense> subsenses;
	
	public Phrase()
	{
		text = null;
		grammar = null;
		subsenses = null;
	}

	public Phrase (Node piemNode, String lemma)
	{
		text = null;
		grammar = null;
		subsenses = null;
		NodeList fields = piemNode.getChildNodes(); 
		for (int i = 0; i < fields.getLength(); i++) {
			Node field = fields.item(i);
			String fieldname = field.getNodeName();
			if (fieldname.equals("t"))
				text = field.getTextContent();
			else if (fieldname.equals("gram"))
				grammar = new Gram (field, lemma);
			else if (fieldname.equals("n"))
			{
				if (subsenses == null) subsenses = new LinkedList<Sense>();
				subsenses.add(new Sense (field, lemma));
			}
			else if (!fieldname.equals("#text")) // Text nodes here are ignored.
				System.err.printf("piem entry field %s not processed\n", fieldname);
		}			
	}
	
	/**
	 * Not sure if this is the best way to treat paradigms.
	 * Currently to trigger true, paradigm must be set for either header or
	 * at least one sense.
	 */
	public boolean hasParadigm()
	{
		if (grammar != null && grammar.hasParadigm()) return true;
		if (subsenses != null) for (Sense s : subsenses)
		{
			if (s.hasParadigm()) return true;
		}
		return false;
	}
	
	public boolean hasUnparsedGram()
	{
		if (grammar != null && grammar.hasUnparsedGram()) return true;
		if (subsenses != null) for (Sense s : subsenses)
		{
			if (s.hasUnparsedGram()) return true;
		}			
		return false;
	}
	
	public String toJSON()
	{
		StringBuilder res = new StringBuilder();
		
		res.append("\"Phrase\":{");
		boolean hasPrev = false;
		
		if (text != null)
		{
			if (hasPrev) res.append(", ");
			res.append("\"Text\":\"");
			res.append(JSONObject.escape(text));
			res.append("\"");
			hasPrev = true;
		}	
		
		if (grammar != null)
		{
			if (hasPrev) res.append(", ");
			res.append(grammar.toJSON());
			hasPrev = true;
		}
		
		if (subsenses != null)
		{
			if (hasPrev) res.append(", ");
			res.append("\"Senses\":");
			res.append(JSONUtils.objectsToJSON(subsenses));
			hasPrev = true;
		}
		
		res.append("}");			
		return res.toString();
	}
}