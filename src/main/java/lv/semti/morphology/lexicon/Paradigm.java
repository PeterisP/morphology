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
import java.util.HashMap;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lv.semti.morphology.attributes.*;

public class Paradigm extends AttributeValues {
	private Lexicon lexicon;
	private int id = 0;  // numurs pēc kārtas - ID
	private ArrayList < HashMap < String, ArrayList<Lexeme> > > lexemesByStem
		= new ArrayList <HashMap <String, ArrayList<Lexeme>>>();
		// 1-3 hashmapi, kuros pēc saknes var atrast tai atbilstošās leksēmas
		// vajadzētu to (un to apstaigājošās funkcijas) iznest kā klasi)
	public ArrayList <Lexeme> lexemes = new ArrayList <Lexeme>();  //FIXME - nevajag iisti buut public
	public ArrayList <Ending> endings = new ArrayList <Ending>();  //FIXME - nevajag iisti buut public
	private Ending lemmaEnding = null;  // kura no galotnēm uzskatāma par pamatformu
	private int stems = 1;      // cik saknes ir šai vārdgrupai (tipiski 1; darbībasvārdiem 3)
	private String description = "";

	public Paradigm (Lexicon lexicon) {
		this.lexicon = lexicon;
		lexemesByStem.add(new HashMap<String,ArrayList<Lexeme>>());
	}

	@Override
	public void toXML (Writer straume) throws IOException {
		straume.write("<Paradigm");
		straume.write(" Stems=\""+String.valueOf(stems)+"\"");
		straume.write(" ID=\""+String.valueOf(id)+"\"");
		if (lemmaEnding != null)
			straume.write(" LemmaEnding=\""+String.valueOf(lemmaEnding.getID())+"\"");
		straume.write(" Description=\""+description+"\"");
		straume.write(">\n");
		super.toXML(straume); // īpašības UzXML

		for (Ending ending : endings)
			ending.toXML(straume);
		for (Lexeme leksēma : lexemes)
			leksēma.toXML(straume);

		straume.write("</Paradigm>\n");
	}

	public Paradigm(Lexicon lexicon, Node node) {
		super(node);
		if (!node.getNodeName().equalsIgnoreCase("Paradigm")) throw new Error("Node '" + node.getNodeName() + "' but Paradigm expected.");
		this.lexicon = lexicon;

		Node n = node.getAttributes().getNamedItem("Stems");
		if (n != null)
			this.setStems(Integer.parseInt(n.getTextContent()));

		n = node.getAttributes().getNamedItem("ID");
		if (n != null)
			this.setID(Integer.parseInt(n.getTextContent()));

		n = node.getAttributes().getNamedItem("Description");
		if (n != null)
			this.setDescription(n.getTextContent());

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Ending"))
				addEnding(new Ending(this, nodes.item(i)));
		}

		n = node.getAttributes().getNamedItem("LemmaEnding");
		if (n != null)
			this.setLemmaEnding(Integer.parseInt(n.getTextContent()));

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Lexeme"))
				addLexeme(new Lexeme(this,nodes.item(i)));
		}
	}
	
	/***
	 * Takes an XML-sublexicon node of type 'Paradigm', and takes the Lexeme elements from there
	 * @param node
	 */
	public void addLexemesFromXML(Node node) {
		if (!node.getNodeName().equalsIgnoreCase("Paradigm")) throw new Error("Node '" + node.getNodeName() + "' but Paradigm expected.");

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Lexeme"))
				addLexeme(new Lexeme(this,nodes.item(i)));
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		// uztaisa paradigmas kopiju, kurai var mainīt īpašības, nenočakarējot sākotnējo leksēmu DB.
		Paradigm kopija;
		try {
			kopija = (Paradigm) super.clone();
			kopija.lexemesByStem = (ArrayList <HashMap <String, ArrayList<Lexeme>>>)lexemesByStem.clone();
			kopija.lexemes = (ArrayList <Lexeme>)lexemes.clone();
			kopija.endings = (ArrayList <Ending>)endings.clone();
			kopija.id = id;
	        return kopija;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int numberOfLexemes () {
		return lexemes.size();
	}

	public Ending getLemmaEnding(){
		return lemmaEnding;
		//FIXME - kur to lieto? varbūt vajag vienu no alternatīvajām pamatformām
	}

	public int numberOfEndings () {
		return endings.size();
	}

	public void addLexeme (Lexeme lexeme) {
		lexeme.setParadigm(this);

		if (lexeme.getID() == 0) {
			lexeme.setID( lexicon.maxLexemeID() + 1 );
		}

		lexeme.setStemCount(stems);

		for (int i = 0; i < stems; i++) {
			// pieliekam leksēmas 1-3 saknes vārdgrupas masīvos
			ArrayList<Lexeme> esošās = lexemesByStem.get(i).get(lexeme.getStem(i));
			if (esošās == null) {
				esošās = new ArrayList<Lexeme>();
				lexemesByStem.get(i).put(lexeme.getStem(i), esošās);
			}
			esošās.add(lexeme);
		}
		lexemes.add(lexeme);

		if (lexeme.getValue(AttributeNames.i_Lemma) == null && getLemmaEnding() != null)
			lexeme.addAttribute(AttributeNames.i_Lemma, lexeme.getStem(getLemmaEnding().stemID-1) + getLemmaEnding().getEnding());
	}

	public void removeLexeme (Lexeme leksēma) {
		// ja nebūs tādas leksēmas, tad nekas arī nenotiks
		lexemes.remove(leksēma);
		for (int i = 0; i < stems; i++) {
			ArrayList<Lexeme> esošās = lexemesByStem.get(i).get(leksēma.getStem(i));
			if (esošās != null) {
				esošās.remove(leksēma);
				if (esošās.size()==0) lexemesByStem.get(i).remove(leksēma.getStem(i));
			}
		}
	}

	public void addEnding (Ending ending) {
		if (ending.getID() == 0) {
			ending.setID( lexicon.maxEndingID() + 1 );
		}
		ending.setParadigm(this);
		endings.add(ending);
		lexicon.invalidateAllEndings();
	}

	public void removeEnding (Ending ending) {
		// ja nebūs tādas galotnes, tad nekas arī nenotiks
		endings.remove(ending);
		lexicon.invalidateAllEndings();
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setLemmaEnding(int lemmaEnding) {
		this.lemmaEnding = endingByNr(lemmaEnding);
	}

	public int getStems() {
		return stems;
	}

	public void setStems(int stems) {
		this.stems = stems;

		while (lexemesByStem.size() > stems) lexemesByStem.remove(lexemesByStem.size()-1);
		while (lexemesByStem.size() < stems) lexemesByStem.add(new HashMap<String, ArrayList<Lexeme>>());

		//FIXME - tā, a ko ar leksēmu sakņu skaitiem ta darīt tagad??
	}

	public String getName() {
		return description;
	}

	public void setDescription(String name) {
		this.description = name;
	}

	public Ending endingByNr(int endingNr) {
		for (Ending ending : endings)
			if (ending.getID() == endingNr) return ending;

		return null;
	}

	public ArrayList<HashMap<String, ArrayList<Lexeme>>> getLexemesByStem() {
		//TODO - jāprotektē
		return lexemesByStem;
	}
}
