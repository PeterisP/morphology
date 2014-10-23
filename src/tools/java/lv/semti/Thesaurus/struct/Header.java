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

import java.util.HashSet;
import java.util.LinkedList;

import lv.semti.Thesaurus.utils.HasToJSON;
import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * v (vārds) field.
 */
public class Header implements HasToJSON
{
	/**
	 * vf (vārdforma) field.
	 */
	public Lemma lemma;
	/**
	 * gram (gamatika) field, optional here.
	 */
	public Gram gram;
	
	
	public Header ()
	{
		lemma = null;
		gram = null;
	}
	
	public Header (Node vNode)
	{
		NodeList fields = vNode.getChildNodes();
		LinkedList<Node> postponed = new LinkedList<Node>();
		for (int i = 0; i < fields.getLength(); i++)
		{
			Node field = fields.item(i);
			String fieldname = field.getNodeName();
			if (fieldname.equals("vf")) // lemma
			{
				if (lemma != null)
					System.err.printf("vf with lemma \"%s\" contains more than one \'vf\'\n", lemma.text);
				lemma = new Lemma(field);
			}
			else if (!fieldname.equals("#text")) // Text nodes here are ignored.
				postponed.add(field);
		}
		if (lemma == null)
			System.err.printf("Thesaurus v-entry without a lemma :(\n");
		
		for (Node field : postponed)
		{
			String fieldname = field.getNodeName();
			if (fieldname.equals("gram")) // grammar
				gram = new Gram (field, lemma.text);
			else System.err.printf(
					"v entry field %s not processed\n", fieldname);				
		}				
	}
	
	public boolean hasParadigm()
	{
		if (gram == null) return false;
		return gram.hasParadigm();
	}
	
	public boolean hasUnparsedGram()
	{
		if (gram == null) return false;
		return gram.hasUnparsedGram();
	}
	
	public String toJSON()
	{
		StringBuilder res = new StringBuilder();
		
		res.append("\"Header\":{");
		res.append(lemma.toJSON());

		if (gram != null)
		{
			res.append(", ");
			res.append(gram.toJSON());
		}
		
		res.append("}");
		return res.toString();
	}
	
	
	public void addToLexicon(Analyzer analizators, String importSource) {
		try {
			String lemma = this.lemma.text;
			Word w = analizators.analyzeLemma(lemma);
			if (w.isRecognized()) 
				return; //throw new Exception(String.format("Vārds %s jau ir leksikonā", lemma));
			
			if (this.gram == null) throw new Exception(String.format("Vārdam %s nav gramatikas", lemma));
			if (this.gram.paradigm == null) throw new Exception(String.format("Vārdam %s nav atrastas paradigmas", lemma));
			HashSet<Integer> paradigms = this.gram.paradigm;
			if (paradigms.size() != 1) throw new Exception(String.format("Vārdam %s ir %d paradigmas", lemma, paradigms.size()));
			int paradigmID = paradigms.iterator().next();
						
			Lexeme l = analizators.createLexemeFromParadigm(lemma, paradigmID, importSource);
			if (l == null) throw new Exception(String.format("createLexemeFromParadigm nofailoja uz %s / %d", lemma, paradigmID));
			if (l.getParadigmID() == 29) { // Hardcoded unflexible words
				l.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
				if (this.gram.flags.contains("Saīsinājums"))
					l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Abbreviation);
				else if (this.gram.flags.contains("Vārds svešvalodā"))
					l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Foreign); 
				else if (this.gram.flags.contains("Izsauksmes vārds"))
					l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Interjection); 
				
			}
			//System.out.printf("Jess %s\n", lemma);
		} catch (Exception e) {
			System.err.printf("Nesanāca ielikt leksēmu :(%s\n",e.getMessage());
			if (e.getMessage() == null) e.printStackTrace();
		}
	}

}