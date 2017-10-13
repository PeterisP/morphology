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
package lv.semti.morphology.lexicon;

import java.io.IOException;
import java.io.PrintWriter;
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
	public HashMap <Integer, Lexeme> lexemesByID = new HashMap <Integer, Lexeme>(); //FIXME - nevajag iisti buut public, vajag tikai read-only iterēt
	public ArrayList <Lexeme> lexemes = new ArrayList <Lexeme>();  //FIXME - nevajag iisti buut public, vajag tikai read-only iterēt
	public ArrayList <Ending> endings = new ArrayList <Ending>();  //FIXME - nevajag iisti buut public, vajag tikai read-only iterēt
	private Ending lemmaEnding = null;  // kura no galotnēm uzskatāma par pamatformu
	private int stems = 1;      // cik saknes ir šai vārdgrupai (tipiski 1; darbībasvārdiem 3)
	private char[] allowedGuessEndings = {};
	public String description = "";

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

	/* saglabaa apakshleksikonaa tikai taas lekseemas, kuraam source sakriit ar noraadiito */
	public void toXML_sub(Writer straume, String source) throws IOException {
		straume.write("<Paradigm");
		straume.write(" Stems=\""+String.valueOf(stems)+"\"");
		straume.write(" ID=\""+String.valueOf(id)+"\"");
		if (lemmaEnding != null)
			straume.write(" LemmaEnding=\""+String.valueOf(lemmaEnding.getID())+"\"");
		straume.write(" Description=\""+description+"\"");
		straume.write(">\n");
		super.toXML(straume); // īpašības UzXML

		for (Lexeme leksēma : lexemes) {
			if (leksēma.isMatchingStrong(AttributeNames.i_Source, source))
				leksēma.toXML(straume);			
		}

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
		
		n = node.getAttributes().getNamedItem("AllowedGuessEndings");
		if (n != null)
			this.setAllowedGuessEndings(n.getTextContent());

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Lexeme")) {
			    Lexeme l = new Lexeme(this, nodes.item(i));
				addLexeme(l);
			}
		}
	}
	
	private void setAllowedGuessEndings(String textContent) {
		this.allowedGuessEndings = textContent.toCharArray();
	}

	/***
	 * Takes an XML-sublexicon node of type 'Paradigm', and takes the Lexeme elements from there
	 * @param node
	 */
	public void addLexemesFromXML(Node node) {
		if (!node.getNodeName().equalsIgnoreCase("Paradigm")) throw new Error("Node '" + node.getNodeName() + "' but Paradigm expected.");

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Lexeme")) {
                Lexeme l = new Lexeme(this, nodes.item(i));
                if (l != null) {
                    String frequency = l.getValue("Skaits"); // FIXME - hardcoded value
                    if (frequency == null || Integer.parseInt(frequency) > Lexicon.proper_name_frequency_floor)
                        addLexeme(l);
                }
            }
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
		// Lieto tam, lai jaunu leksēmu ģenerējot, aizpildītu lemma lauku
	}

	public int numberOfEndings () {
		return endings.size();
	}

	public void addLexeme (Lexeme lexeme) {
		lexeme.setParadigm(this);

		if (lexeme.getID() == 0) {
			lexeme.setID( lexicon.newLexemeID());
		} else {
			Lexeme duplicatetest = lexemesByID.get(lexeme.getID());
			if (duplicatetest != null) {
				System.err.println("Lexemes with duplicate IDs:");
				duplicatetest.describe(new PrintWriter(System.err));
				lexeme.describe(new PrintWriter(System.err));
			}
		}
		lexemesByID.put(lexeme.getID(), lexeme);

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

		if (this.id == 29 || this.id == 25) { // Hardcoded un vietniekvārdu paradigma
			String pamatforma = lexeme.getValue(AttributeNames.i_Lemma);
			this.lexicon.hardcodedForms.put(pamatforma, lexeme);
		}
	}

	public void removeLexeme (Lexeme leksēma) {
		// ja nebūs tādas leksēmas, tad nekas arī nenotiks
		lexemes.remove(leksēma);
		lexemesByID.remove(leksēma.getID());
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
		if (this.lemmaEnding == null)
			System.err.printf("Error when loading paradigm %d - cannot find lemma ending %d\n", this.id, lemmaEnding);
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

	// Verifies if this stem is a valid stem for this paradigm, based on the last letter(s?) of that stem 
	public boolean allowedGuess(String stem) {
		if (allowedGuessEndings.length == 0) return true; // FIXME - workaround until all paradigms have this data filled
		if (stem.isEmpty()) return false;
		
		char lastchar = stem.charAt(stem.length()-1);
		for (char c : allowedGuessEndings) 
			if (c==lastchar) return true;
		return false;
	}
}
