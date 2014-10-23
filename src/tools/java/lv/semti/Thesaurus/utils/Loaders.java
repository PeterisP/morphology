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
