package lv.semti.Thesaurus.utils;

import java.util.Iterator;
import java.util.LinkedList;

import lv.semti.Thesaurus.struct.Phrase;
import lv.semti.Thesaurus.struct.Sense;


import org.json.simple.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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