package lv.semti.Thesaurus.struct;

import java.util.LinkedList;

import lv.semti.Thesaurus.utils.HasToJSON;
import lv.semti.Thesaurus.utils.Loaders;
import lv.semti.Thesaurus.utils.JSONUtils;


import org.json.simple.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * n (nozīme / nozīmes nianse) field.
 */
public class Sense implements HasToJSON
{
	
	/**
	 * gram field  is optional here.
	 */
	public Gram grammar;
	
	/**
	 * d (definīcija) field.
	 */
	public Gloss gloss;
	
	/**
	 * id field.
	 */
	public String ordNumber;
	
	/**
	 * g_piem (piemēru grupa) field, optional here.
	 */
	
	public LinkedList<Phrase> examples = null;
	/**
	 * g_an (apakšnozīmju grupa) field, optional here.
	 */
	public LinkedList<Sense> subsenses = null;
			
	public Sense ()
	{
		grammar = null;
		gloss = null;
		examples = null;
		subsenses = null;
		ordNumber = null;
	}
	
	/**
	 * @param lemma is used for grammar parsing.
	 */
	public Sense (Node nNode, String lemma)
	{
		NodeList fields = nNode.getChildNodes(); 
		for (int i = 0; i < fields.getLength(); i++)
		{
			Node field = fields.item(i);
			String fieldname = field.getNodeName();
			if (fieldname.equals("gram"))
				grammar = new Gram (field, lemma);
			else if (fieldname.equals("d"))
			{
				NodeList glossFields = field.getChildNodes();
				for (int j = 0; j < glossFields.getLength(); j++)
				{
					Node glossField = glossFields.item(j);
					String glossFieldname = glossField.getNodeName();
					if (glossFieldname.equals("t"))
					{
						if (gloss != null)
							System.err.println("d entry contains more than one \'t\'");
						gloss = new Gloss (glossField);
					}
					else if (!glossFieldname.equals("#text")) // Text nodes here are ignored.
						System.err.printf("d entry field %s not processed\n", glossFieldname);
				}
			}
			else if (fieldname.equals("g_piem"))
				examples = Loaders.loadPhrases(field, lemma, "piem");
			else if (fieldname.equals("g_an"))
				subsenses = Loaders.loadSenses(field, lemma);
			else if (!fieldname.equals("#text")) // Text nodes here are ignored.
				System.err.printf("n entry field %s not processed\n", fieldname);
		}
		ordNumber = ((org.w3c.dom.Element)nNode).getAttribute("nr");
		if ("".equals(ordNumber)) ordNumber = null;
	}
	
	/**
	 * Not sure if this is the best way to treat paradigms.
	 * Currently only grammar paradigm is considered.
	 */
	public boolean hasParadigm()
	{
		if (grammar == null) return false;
		return grammar.hasParadigm();
		//if (grammar.hasParadigm()) return true;
		//for (Phrase e : examples)
		//{
		//	if (e.hasParadigm()) return true;
		//}
		//for (Sense s : subsenses)
		//{
		//	if (s.hasParadigm()) return true;
		//}
		//return false;
	}
	
	public boolean hasUnparsedGram()
	{
		if (grammar != null && grammar.hasUnparsedGram()) return true;
		if (examples != null) for (Phrase e : examples)
		{
			if (e.hasUnparsedGram()) return true;
		}
		if (subsenses != null) for (Sense s : subsenses)
		{
			if (s.hasUnparsedGram()) return true;
		}			
		return false;
	}
	
	public String toJSON()
	{
		StringBuilder res = new StringBuilder();
		
		boolean hasPrev = false;
		
		if (ordNumber != null)
		{
			res.append("\"SenseID\":\"");
			res.append(JSONObject.escape(ordNumber.toString()));
			res.append("\"");
			hasPrev = true;
		}
		
		if (grammar != null)
		{
			if (hasPrev) res.append(", ");
			res.append(grammar.toJSON());
			hasPrev = true;
		}
		
		if (gloss != null)
		{
			if (hasPrev) res.append(", ");
			res.append(gloss.toJSON());
			hasPrev = true;
		}
		
		if (examples != null && !examples.isEmpty())
		{
			if (hasPrev) res.append(", ");
			res.append("\"Examples\":");
			res.append(JSONUtils.objectsToJSON(examples));
			hasPrev = true;
		}
		
		if (subsenses != null && !subsenses.isEmpty())
		{
			if (hasPrev) res.append(", ");
			res.append("\"Senses\":");
			res.append(JSONUtils.objectsToJSON(subsenses));
			hasPrev = true;
		}
		
		return res.toString();
	}
}