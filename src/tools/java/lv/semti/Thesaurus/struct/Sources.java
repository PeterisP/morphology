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

import java.util.Arrays;
import java.util.LinkedList;

import lv.semti.Thesaurus.utils.HasToJSON;
import lv.semti.Thesaurus.utils.JSONUtils;


import org.w3c.dom.Node;

/**
 * avots field.
 */
public class Sources implements HasToJSON
{
	public String orig;
	public LinkedList<String> s;
	
	public Sources ()
	{
		orig = null; s = null;
	}
	
	public Sources (Node avotsNode)
	{
		orig = avotsNode.getTextContent();
		s = parseSources(orig);
		if (s.size() < 1 && orig.length() > 0)
			System.err.printf(
				"Field 'sources' '%s' can't be parsed!\n", orig);
	}
	
	public boolean isEmpty()
	{
		return s == null;
	}
	
	/**
	 *  Parse sources from string and check if the information isn't already
	 *  filled, to detect possible overwritten data.
	 */
	public void set(String sourcesText)
	{
		if (orig != null || s != null)
		{
			System.err.printf(
				"Duplicate info for field 'sources' : '%s' and '%s'!\n", orig, sourcesText);
		}
		orig = sourcesText;
		s = parseSources(sourcesText);
		if (s.size() < 1 && orig.length() > 0)
			System.err.printf(
				"Field 'sources' '%s' can't be parsed!\n", orig);
	}
	
	// In case of speed problems StringBuilder can be returned.
	public String toJSON()
	{
		StringBuilder res = new StringBuilder();
		if (s != null)
		{
			res.append("\"Sources\":");
			res.append(JSONUtils.simplesToJSON(s));
		}
		return res.toString();
	}
	
	private static LinkedList<String> parseSources (String sourcesText)
	{
		if (sourcesText.startsWith("["))
			sourcesText = sourcesText.substring(1);
		if (sourcesText.endsWith("]"))
			sourcesText = sourcesText.substring(0, sourcesText.length() - 1);
		
		LinkedList<String> res = new LinkedList<String>();
		res.addAll(Arrays.asList(sourcesText.split(",\\s*")));
		return res;
	}
}