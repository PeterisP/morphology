/*******************************************************************************
 * Copyright 2008, 2009, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
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
package lv.semti.morphology.analyzer;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;

import org.w3c.dom.Node;

import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.*;

/**
 * One morphological analysis variant for a word or token.
 * 
 * @author Pēteris
 */
public class Wordform extends AttributeValues implements Serializable{

	private static final long serialVersionUID = 1L;
	private String token;
	private transient Ending ending;
	public transient Lexeme lexeme;
		
	public Wordform (String token, Lexeme lexeme, Ending ending) {
	    this(token, lexeme, ending, null);
	}

	public Wordform(String token, Lexeme lexeme, Ending ending, String originalWord) {
		this.token = token;
		this.lexeme = lexeme;
		this.ending = ending;
		Paradigm paradigm = ending.getParadigm();
		
		addAttribute(AttributeNames.i_Word, token);
		addAttribute(AttributeNames.i_Mija, Integer.toString(ending.getMija()));
		addAttributes(paradigm);
		addAttributes(ending);
		addAttribute(AttributeNames.i_EndingID, Integer.toString(ending.getID()));
		addAttribute(AttributeNames.i_ParadigmID, Integer.toString(paradigm.getID()));
		removeAttribute(AttributeNames.i_ParadigmProperties);
		removeAttribute(AttributeNames.i_ParadigmSupportedDerivations);
		
		boolean fixed_stem;
		if (lexeme != null) {
			addAttributes(lexeme);
			addAttribute(AttributeNames.i_LexemeID, Integer.toString(lexeme.getID()));
			fixed_stem = isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum)  ||
					isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum) ||
					isMatchingStrong(AttributeNames.i_EntryProperties, AttributeNames.v_Plural) ||
					isMatchingStrong(AttributeNames.i_Declension, AttributeNames.v_InflexibleGenitive);
			// || leksēma.isMatchingStrong(AttributeNames.i_Deminutive, "-iņ-")
		} else fixed_stem = true;

		if (isMatchingStrong(AttributeNames.i_EntryProperties, AttributeNames.v_Plural) &&
				isMatchingStrong(AttributeNames.i_Number, AttributeNames.v_Plural)) {
			addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
		}
		
		Ending lemmaEnding = ending.getLemmaEnding();
		// FIXME šis 'if' būtu jāsaprot un jāsakārto - lai ir sakarīgi, bet nesalauž specgadījumus ('vairāk' -> pamatforma 'daudz' utml)
		// TODO - varbūt vienkārši dažām paradigmām vai galotnēm vajag karodziņu par to, ka jāģenerē pamatforma no jauna?
		if (lemmaEnding != null && !(paradigm.isMatchingStrong(AttributeNames.i_ParadigmProperties, AttributeNames.v_HardcodedWordforms) ||
				// paradigm.getName().equalsIgnoreCase("adverb") ||    // 2024.03.01 aizkomentējām jo šķita ka lieks
				paradigm.getName().equalsIgnoreCase("punct"))
				&& !fixed_stem) {
			String thirdStem = null;
			//if (paradigm.getStems() == 3) thirdStem = lexeme.getStem(2);
			if (paradigm.getStems().contains(StemType.STEM3))
				thirdStem = lexeme.getStem(StemType.STEM3);
			String stem = lexeme.getStem(lemmaEnding.stemType);
			ArrayList<Variants> stemsWithChanges = Mijas.MijasLocīšanai(
					stem, lemmaEnding.getMija(), thirdStem, false,
					this.isMatchingStrong(AttributeNames.i_NounType, AttributeNames.v_ProperNoun));
			
			if (!stemsWithChanges.isEmpty()) stem = stemsWithChanges.get(0).celms; // FIXME - nav objektīva pamata ņemt tieši pirmo, netīri
			
			String lemma = stem + lemmaEnding.getEnding();
			if (lexeme.isMatchingStrong(AttributeNames.i_NounType, AttributeNames.v_ProperNoun)) {
				lemma = Character.toUpperCase(lemma.charAt(0)) + lemma.substring(1);
			}
			String originalLemma = lexeme.getValue(AttributeNames.i_Lemma);
            lemma = Lexicon.recapitalize(lemma, originalLemma);
            if (!lemma.equals(originalLemma)) {
            	if (getValue(AttributeNames.i_LemmaOverride) == null) {
					addAttribute(AttributeNames.i_SourceLemma, originalLemma);
					addAttribute(AttributeNames.i_Lemma, lemma);
					// jo var pamatforma atšķirties no leksēmas pamatformas, piem. "otrās" pamatforma ir "otrā" nevis "otrais".
				} else {
					addAttribute(AttributeNames.i_LemmaParadigm, lemma);
				}
			}
		}
	}
	
	public Wordform (String token) {
		this.token = token;
	}

	public Wordform (String token, String lemma, Ending ending, String POS) {
		this.token = token;
		this.setEnding(ending);
		this.addAttribute(AttributeNames.i_Word, token);
		this.addAttribute(AttributeNames.i_PartOfSpeech, POS);
		if (lemma != null) this.addAttribute(AttributeNames.i_Lemma, lemma);
	}

	public Wordform(Node node) {
		super(node);
		if (!node.getNodeName().equalsIgnoreCase("Vārdforma")) throw new Error("Node " + node.getNodeName() + " nav Vārdforma");
		token = node.getAttributes().getNamedItem("vārds").getTextContent();
	}

	public void shortDescription(PrintWriter output) {
		output.printf("%s :\t%s : %s  #%d\n",
				token, getTag(), getValue(AttributeNames.i_Lemma),lexeme.getID());
		//FIXME - nečeko, vai leksēma nav null
	}
	
	/**
	 * For debugging purposes only.
	 */
	public void longDescription(PrintStream output)
	{
		output.println(this.token + ":");
		for (String s : this.attributes.keySet())
		{
			output.println(s + "\t" + attributes.get(s));
		}
	}

	@Override
	public Object clone() {
		Wordform clone;
		try {
			clone = (Wordform)super.clone();
			clone.token = this.token;
			clone.lexeme = this.lexeme;
			clone.ending = this.ending;
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void toXML (Writer output) throws IOException {
		output.write("<Vārdforma");
		output.write(" vārds=\""+token.replace("\"", "&quot;")+"\">\n");
		super.toXML(output); // īpašības UzXML
		output.write("</Vārdforma>\n");
	}

	public Ending getEnding() {
		return ending;
	}

	public void setEnding(Ending ending) {
		this.ending = ending;
	}

	public String getToken() {
		return token;
	}
	
	protected void setToken(String newtoken) {
		token = newtoken;
		addAttribute(AttributeNames.i_Word, newtoken);
	}

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Wordform)) {
            return false;
        }
        Wordform that = (Wordform) other;
        if (this.attributes == null) {
            if (that.attributes != null) return false;
        } else if (!this.attributes.equals(that.attributes))
            return false;

        if (this.token == null) {
            if (that.token != null) return false;
        } else if (!this.token.equals(that.token))
            return false;

        if (this.ending == null) {
            if (that.ending != null) return false;
        } else if (!this.ending.equals(that.ending))
            return false;

        if (this.lexeme == null) {
			return that.lexeme == null;
        } else return this.lexeme.equals(that.lexeme);
	}

    @Override
    public int hashCode() {
        int hashCode = this.attributes.hashCode();
        if (token != null)
            hashCode = hashCode * 37 + token.hashCode();
        if (ending != null)
            hashCode = hashCode * 37 + ending.hashCode();
        if (lexeme != null)
            hashCode = hashCode * 37 + lexeme.hashCode();
        return hashCode;
    }
}
