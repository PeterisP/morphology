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

import org.w3c.dom.Node;

import lv.semti.morphology.attributes.*;

/**
 *
 * @author Pēteris Paikens
 * Informācija par vienu leksēmu/celmu, kuru var lietot vārdu locīšanai
 *
 */
public class Lexeme extends AttributeValues {
	private int id = 0;		// numurs pēc kārtas - ID
	private ArrayList <String> stems = new ArrayList<String>();    // Saknes - 1 vai 3 eksemplāri.
	private Paradigm paradigm = null;

	protected void setParadigm(Paradigm vārdgrupa) {
		this.paradigm = vārdgrupa;
	}

	public Paradigm getParadigm() {
		return paradigm;
	}

	public int getParadigmID() {
		return paradigm.getID();
	}

	@Override
	public void toXML (Writer pipe) throws IOException {
		pipe.write("<Lexeme");
		pipe.write(" ID=\""+String.valueOf(id)+"\"");
		for (int i=0;i<stems.size();i++) {
			if (stems.get(i).equals("\""))
				pipe.write(" Stem"+String.valueOf(i+1)+"=\"&quot;\"");
			else pipe.write(" Stem"+String.valueOf(i+1)+"=\""+stems.get(i)+"\"");
		}
		pipe.write(">");
		super.toXML(pipe); // īpašības UzXML
		pipe.write("</Lexeme>\n");
	}

	public Lexeme(Paradigm paradigm, Node node) {
		super(node);
		if (!node.getNodeName().equalsIgnoreCase("Lexeme")) throw new Error("Node '" + node.getNodeName() + "' but Lexeme expected");
		this.paradigm = paradigm;
		setStemCount(paradigm.getStems());

		Node n = node.getAttributes().getNamedItem("Stem1");
		if (n != null)
			stems.set(0, n.getTextContent().toLowerCase()); // TODO - supports case-sensitive lietām - saīsinājumiem utml
		n = node.getAttributes().getNamedItem("Stem2");
		if (n != null)
			stems.set(1, n.getTextContent().toLowerCase());
		n = node.getAttributes().getNamedItem("Stem3");
		if (n != null)
			stems.set(2, n.getTextContent().toLowerCase());
		//FIXME - te paļaujas, ka pēc tam pati vārdgrupa 100% izsauks PieliktLeksēmu un sakārtos savus masīvus tādi.

		n = node.getAttributes().getNamedItem("ID");
		if (n != null)
			this.setID(Integer.parseInt(n.getTextContent()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		// uztaisa leksēmas kopiju, kurai var mainīt īpašības, nenočakarējot sākotnējo leksēmu DB.
		Lexeme kopija;
		try {
			kopija = (Lexeme) super.clone();
			kopija.stems = (ArrayList<String>)stems.clone();
			kopija.paradigm = (Paradigm)paradigm.clone();
			kopija.id = id;
	        return kopija;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Lexeme(String stem) {
		// vārdgrupu, numuru, sakņu skaitu utml pieliek procedūra, kas pievieno leksēmu vārdgrupai
		stems.add(stem);
	}

	public Lexeme() {
		// nekas arī nav jādara
	}


	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getStem(int stemNr) {
		if (stemNr < 0 || stemNr >= stems.size()) {
			int sakņuskaits = stems.size();
			throw new RuntimeException(String.format(
			//FIXME - jāpāriet uz ķeramu exception
					"Leksēmai %d ar %d saknēm mēģinam nolasīt sakni nr %d.",id,sakņuskaits,stemNr));
		}
		return stems.get(stemNr);
	}

	public void setStem(int stemNr, String stem) {
		if (stemNr < 0 || stemNr >= stems.size()) {
			int sakņuskaits = stems.size();
			throw new RuntimeException(String.format(
					//FIXME - jāpāriet uz ķeramu exception
					"Leksēmai %d ar %d saknēm mēģinam uzlikt sakni %s nr %d.",id,sakņuskaits,stem,stemNr));
		}
		if (paradigm != null) paradigm.removeLexeme(this);
		stems.set(stemNr, stem);

		// FIXME - neloģiski paļaujas, ka pēc izņemšanas lauks vārdgrupa paliks pa vecam - vajadzētu tak null
		if (paradigm != null) paradigm.addLexeme(this);
	}

	protected void setStemCount (int stemCount) {
		while (stems.size() > stemCount) stems.remove(stems.size()-1);
		while (stems.size() < stemCount) stems.add("");
	}
}
