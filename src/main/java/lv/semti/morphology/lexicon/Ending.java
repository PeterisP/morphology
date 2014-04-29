/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia; Author: Pēteris Paikens
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
package lv.semti.morphology.lexicon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import lv.semti.morphology.attributes.*;

/**
 * Satur informāciju par vienu galotni (jeb locīšanas paradigmu)
 *
 * @author Pēteris Paikens
 *
 */
public class Ending extends AttributeValues {
	private int id;			  // numurs pēc kārtas - ID
	private String ending;	  // Pati galotne
	private int mija;		  // kura mija jāpielieto saknei, lai šo galotni tai pieliktu
							  // FIXME - varbūt kādu sasaisti ar miju objektu šeit?

	public int stemID = 1; // kurai no (potenciāli 3) leksēmas saknēm galotne atbilst - darbībasvārdiem tas ir aktuāli
	//FIXME - stemNr vietām numurējas no 1 vietām no 0 - ļoti bīstami!
	private Paradigm paradigm;
	private Ending lemmaEnding = null;

	public Ending () {
		//irok
	}

	public Ending(Paradigm paradigm, Node node) {
		super(node);
		this.paradigm = paradigm;

		if (!node.getNodeName().equalsIgnoreCase("Ending")) throw new Error("Node '" + node.getNodeName() + "' but Ending expected.");

		Node n = node.getAttributes().getNamedItem("ID");
		if (n != null)
			this.setID(Integer.parseInt(n.getTextContent()));

		n = node.getAttributes().getNamedItem("StemChange");
		if (n != null)
			this.setMija(Integer.parseInt(n.getTextContent()));

		n = node.getAttributes().getNamedItem("Ending");
		if (n != null)
			this.setEnding(n.getTextContent());

		n = node.getAttributes().getNamedItem("StemID");
		if (n != null)
			this.stemID = Integer.parseInt(n.getTextContent());

		n = node.getAttributes().getNamedItem("LemmaEnding");
		if (n != null)
			try {
				setLemmaEnding(Integer.parseInt(n.getTextContent()));
				//FIXME - nestrādās, ja pamatformas galotne tiks ielasīta pēc šīs galotnes, nevis pirms..
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				throw new Error(e.getMessage());
			}
	}

	@Override
	public void toXML (Writer pipe) throws IOException {
		pipe.write("<Ending");
		pipe.write(" ID=\""+String.valueOf(id)+"\"");
		pipe.write(" StemChange=\""+String.valueOf(mija)+"\"");
		pipe.write(" Ending=\""+ending+"\"");
		pipe.write(" StemID=\""+String.valueOf(stemID)+"\"");
		if (lemmaEnding != null)
			pipe.write(" LemmaEnding=\""+String.valueOf(lemmaEnding.id)+"\"");
		pipe.write(">");

		super.toXML(pipe); // Īpašības UzXML
		pipe.write("</Ending>\n");
	}

	public int getID() {
		return id;
	}

	public void setID(int nr) {
		this.id = nr;
	}

	public int getMija() {
		return mija;
	}

	public void setMija(int mija) {
		this.mija = mija;
	}

	public String getEnding() {
		return ending;
	}

	public void setEnding(String ending) {
		this.ending = ending;
	}

	// tiek pieņemts, ka izsaucošā funkcija nodrošina, ka vārds tiešām beidzas ar šo galotni.
	public String stem (String vārds) {
		if (!vārds.endsWith(ending))
			throw new Error("Gļuks - vārds (" + vārds + ") nebeidzas ar norādīto galotni (" + ending + ")");
		return vārds.substring(0, vārds.length()- ending.length());
	}

	public Ending getLemmaEnding() {
		if (this.lemmaEnding == null) 
			return this.paradigm.getLemmaEnding();
		return lemmaEnding;
	}

	public void setLemmaEnding(int lemmaEndingNr){
		if (lemmaEndingNr == id) lemmaEnding = this;
			// vajag, jo šai gadījumā pati pamatformas galotne vēl nav vārdgrupā, un vārdgrupa.galotnePēcNr būs null.
		else lemmaEnding = paradigm.endingByNr( lemmaEndingNr );
	}

	public Paradigm getParadigm() {
		return paradigm;
	}

	protected void setParadigm(Paradigm paradigm) {
		this.paradigm = paradigm;
		//FIXME - būtu jālikvidē un jāpārceļ tīri uz konstruktoru
	}

	/* 
	 * Returned Lexemes shouldn't be modified to avoid changing recognition of other words!
	 * Not cloned due to performance and memory usage concerns.
	 */
	public ArrayList<Lexeme> getEndingLexemes(String celms) {
		return paradigm.getLexemesByStem().get(stemID-1).get(celms);
	}
}
