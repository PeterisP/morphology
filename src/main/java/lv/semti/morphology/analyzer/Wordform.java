/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia;
 * Author: Pēteris Paikens
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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
		
		Boolean fixed_stem; 
		if (lexeme != null) {
			addAttributes(lexeme);
			addAttribute(AttributeNames.i_SourceLemma, lexeme.getValue(AttributeNames.i_Lemma));  //TODO - šis teorētiski varētu aizstāt visus pārējos SourceLemma pieminējumus (citus varbūt var dzēst)
			addAttribute(AttributeNames.i_LexemeID, Integer.toString(lexeme.getID()));
			fixed_stem = lexeme.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum)  ||
						 lexeme.isMatchingStrong(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum) ||
						 lexeme.isMatchingStrong(AttributeNames.i_CaseSpecial, AttributeNames.v_InflexibleGenitive);
			// || leksēma.isMatchingStrong(AttributeNames.i_Deminutive, "-iņ-")
		} else fixed_stem = true;
		
		Ending pamatformasEnding = ending.getLemmaEnding();
		// FIXME šis 'if' būtu jāsaprot un jāsakārto - lai ir sakarīgi, bet nesalauž specgadījumus ('vairāk' -> pamatforma 'daudz' utml)
		if (pamatformasEnding != null && !(paradigm.getID() == 25 || paradigm.getID() == 29 || paradigm.getID() == 21)
				&& !fixed_stem) {
			String trešāSakne = null; 
			if (paradigm.getStems() == 3) trešāSakne = lexeme.getStem(2);
			String celms = lexeme.getStem(pamatformasEnding.stemID-1);
			ArrayList<Variants> celmi = Mijas.MijasLocīšanai(celms, pamatformasEnding.getMija(), trešāSakne, false, this.isMatchingStrong(AttributeNames.i_NounType, AttributeNames.v_ProperNoun));
			
			if (celmi.size()>0) celms = celmi.get(0).celms; // FIXME - nav objektīva pamata ņemt tieši pirmo, netīri
			
			String pamatforma = celms + pamatformasEnding.getEnding();	
			if (lexeme.isMatchingStrong(AttributeNames.i_NounType, AttributeNames.v_ProperNoun)) {
				pamatforma = Character.toUpperCase(pamatforma.charAt(0)) + pamatforma.substring(1);
			}
			addAttribute(AttributeNames.i_Lemma, pamatforma );
			// jo var pamatforma atšķirties no leksēmas pamatformas, piem. "otrās" pamatforma ir "otrā" nevis "otrais".
			// TODO - varbūt vienkārši dažām paradigmām vai galotnēm vajag karodziņu par to, ka jāģenerē pamatforma no jauna?
		}	
	}
	
	public Wordform (String token) {
		this.token = token;
	}

	public Wordform(Node node) {
		super(node);
		if (!node.getNodeName().equalsIgnoreCase("Vārdforma")) throw new Error("Node " + node.getNodeName() + " nav Vārdforma");
		token = node.getAttributes().getNamedItem("vārds").getTextContent();
	}
	
	//getLocījumuDemo
/*	public String getDemo() {
		return this.getValue("LocījumuDemo");
		// TODO : apskatīt kur lieto un vai vajag
	} //*/

	public void shortDescription(PrintWriter stream) {
		stream.printf("%s :\t%s : %s  #%d\n", token, getTag(), getValue(AttributeNames.i_Lemma),lexeme.getID());
		//FIXME - nečeko, vai leksēma nav null
	}
	
	/**
	 * For debugging purposes only.
	 */
	public void longDescription(PrintStream out)
	{
		out.println(this.token + ":");
		for (String s : this.attributes.keySet())
		{
			out.println(s + "\t" + attributes.get(s));
		}
	}

	@Override
	public Object clone() {
		Wordform kopija;
		try {
			kopija = (Wordform)super.clone();
			kopija.token = this.token;
			kopija.lexeme = this.lexeme;
			kopija.ending = this.ending;
			return kopija;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	@Override
	public void addAttributes(AttributeValues attributes) {
		super.addAttributes(attributes);
		addAttribute(AttributeNames.i_Tag, MarkupConverter.toKamolsMarkup(this));
	}*/
	
	@Override
	public void toXML (Writer stream) throws IOException {
		stream.write("<Vārdforma");
		stream.write(" vārds=\""+token.replace("\"", "&quot;")+"\">\n");
		super.toXML(stream); // īpašības UzXML
		stream.write("</Vārdforma>\n");
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
}
