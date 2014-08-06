package lv.semti.Thesaurus.struct;


import lv.semti.Thesaurus.utils.HasToJSON;

import org.json.simple.JSONObject;
import org.w3c.dom.Node;

/**
 * d (definīcija) field.
 */
public class Gloss implements HasToJSON
{
	/**
	 * t (teksts) field.
	 */
	public String text = null;
	
	public Gloss (Node dNode)
	{
		text = dNode.getTextContent();
	}
	
	public String toJSON()
	{
		return String.format("\"Gloss\":\"%s\"", JSONObject.escape(text));			
	}
}