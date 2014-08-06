package lv.semti.Thesaurus.utils;

import java.util.LinkedList;

import lv.semti.Thesaurus.struct.Phrase;
import lv.semti.Thesaurus.struct.Sense;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Loaders
{
	/**
	 * Loads contents of g_n or g_an field into LinkedList.
	 * Reads the information about the (multiple) word senses for that entry.
	 * NB! they may have their own 'gram.' entries, not sure how best to
	 * reconcile.
	 * @param lemma is used for grammar parsing.
	 */
	public static LinkedList<Sense> loadSenses(Node allSenses, String lemma)
	{
		//if (senses == null) senses = new LinkedList<Sense>();
		LinkedList<Sense> res = new LinkedList<Sense>();
		NodeList senseNodes = allSenses.getChildNodes(); 
		for (int i = 0; i < senseNodes.getLength(); i++)
		{
			Node sense = senseNodes.item(i);
			
			// We're ignoring the number of the senses - it's in "nr" field, we
			//assume (not tested) that it matches the order in file
			if (sense.getNodeName().equals("n"))
				res.add(new Sense(sense, lemma));
			else if (!sense.getNodeName().equals("#text")) // Text nodes here are ignored.
				System.err.printf(
					"%s entry field %s not processed, expected only 'n'.\n",
					allSenses.getNodeName(), sense.getNodeName());
		}
		return res;
	}
	
	/**
	 * Load contents of g_fraz or g_piem field into LinkedList.
	 * @param lemma is used for grammar parsing.
	 */
	public static LinkedList<Phrase> loadPhrases(
			Node allPhrases, String lemma, String subElemName)
	{
		LinkedList<Phrase> res = new LinkedList<Phrase>();
		NodeList phraseNodes = allPhrases.getChildNodes(); 
		for (int i = 0; i < phraseNodes.getLength(); i++)
		{
			Node phrase = phraseNodes.item(i);
			if (phrase.getNodeName().equals(subElemName))
				res.add(new Phrase(phrase, lemma));
			else if (!phrase.getNodeName().equals("#text")) // Text nodes here are ignored.
				System.err.printf(
					"%s entry field %s not processed, expected only '%s'.\n",
					allPhrases.getNodeName(), phrase.getNodeName(), subElemName);
		}
		return res;
	}
}
