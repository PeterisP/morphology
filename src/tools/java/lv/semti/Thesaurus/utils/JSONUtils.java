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
package lv.semti.Thesaurus.utils;

import java.util.Iterator;
import org.json.simple.JSONObject;

public class JSONUtils
{
	public static <E extends HasToJSON> String objectsToJSON(Iterable<E> l)
	{
		if (l == null) return "[]";
		StringBuilder res = new StringBuilder();
		res.append("[");
		Iterator<E> i = l.iterator();
		while (i.hasNext())
		{
			res.append("{");
			res.append(i.next().toJSON());
			res.append("}");
			if (i.hasNext()) res.append(", ");
		}
		res.append("]");			
		return res.toString();
	}
	
	public static<E> String simplesToJSON(Iterable<E> l)
	{
		if (l == null) return "[]";
		StringBuilder res = new StringBuilder();
		res.append("[");
		Iterator<E> i = l.iterator();
		while (i.hasNext())
		{
			res.append("\"");
			res.append(JSONObject.escape(i.next().toString()));
			res.append("\"");
			if (i.hasNext()) res.append(", ");
		}
		res.append("]");			
		return res.toString();
	}
	
}