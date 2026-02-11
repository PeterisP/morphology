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
import java.io.Writer;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lv.semti.morphology.attributes.*;

public class Paradigm extends AttributeValues {
	private Lexicon lexicon;
	private int id = 0;  // numurs pēc kārtas - ID
	public String name = ""; // vārdiskais ID
	private HashMap <StemType, HashMap<String, ArrayList<Lexeme>>> lexemesByStem
		= new HashMap<>();
		// 1-3 hashmapi, kuros pēc saknes var atrast tai atbilstošās leksēmas
		// vajadzētu to (un to apstaigājošās funkcijas) iznest kā klasi)
	public HashMap <Integer, Lexeme> lexemesByID = new HashMap<>(); //FIXME - nevajag iisti buut public, vajag tikai read-only iterēt
	public ArrayList <Lexeme> lexemes = new ArrayList<>();  //FIXME - nevajag iisti buut public, vajag tikai read-only iterēt
	public ArrayList <Ending> endings = new ArrayList<>();  //FIXME - nevajag iisti buut public, vajag tikai read-only iterēt
	private Ending lemmaEnding = null;  // kura no galotnēm uzskatāma par pamatformu
	private HashSet<StemType> stems = Stream.of(StemType.STEM1)
			.collect(Collectors.toCollection(HashSet::new));      // kuras saknes ir šai vārdgrupai (tipiski 1; darbībasvārdiem 3)
	private String allowedGuessEndings = "";
	public String description = "";

	public Paradigm (Lexicon lexicon) {
		this.lexicon = lexicon;
		lexemesByStem.put(StemType.STEM1, new HashMap<>());
	}

	@Override
	public void toXML (Writer output) throws IOException {
		output.write("<Paradigm");
		output.write(" Stems=\""+ stems.size() +"\"");
		output.write(" ID=\""+ id +"\"");
		output.write(" ID=\""+ name +"\"");
		if (lemmaEnding != null)
			output.write(" LemmaEnding=\""+ lemmaEnding.getID() +"\"");
		output.write(" Description=\""+description+"\"");
		output.write(">\n");
		super.toXML(output); // īpašības UzXML

		for (Ending ending : endings)
			ending.toXML(output);
		for (Lexeme leksēma : lexemes)
			leksēma.toXML(output);

		output.write("</Paradigm>\n");
	}

	/**
	 * Stores in sublexicon only lexemes with the given source.
	 */
	public void toXML_sub(Writer output, String source) throws IOException {
		output.write("<Paradigm");
		output.write(" Stems=\""+ stems.size() +"\"");
		output.write(" ID=\""+ id +"\"");
		if (lemmaEnding != null)
			output.write(" LemmaEnding=\""+ lemmaEnding.getID() +"\"");
		output.write(" Description=\""+description+"\"");
		output.write(">\n");
		super.toXML(output); // īpašības UzXML

		for (Lexeme lexeme : lexemes) {
			if (lexeme.isMatchingStrong(AttributeNames.i_Source, source))
				lexeme.toXML(output);
		}

		output.write("</Paradigm>\n");
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

		n = node.getAttributes().getNamedItem("Name");
		if (n != null)
			this.name = n.getTextContent();

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
			this.allowedGuessEndings = n.getTextContent();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Lexeme")) {
			    Lexeme l = new Lexeme(this, nodes.item(i));
				addLexeme(l);
			}
		}
	}

	/**
	 * Takes an XML-sublexicon node of type 'Paradigm', and takes the Lexeme elements from there
	 */
	public void addLexemesFromXML(Node node) {
		if (!node.getNodeName().equalsIgnoreCase("Paradigm")) throw new Error("Node '" + node.getNodeName() + "' but Paradigm expected.");

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Lexeme")) {
                Lexeme l = new Lexeme(this, nodes.item(i));
				String frequency = l.getValue("Skaits"); // FIXME - hardcoded value
				if (frequency == null || Integer.parseInt(frequency) > Lexicon.proper_name_frequency_floor)
					addLexeme(l);
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		// uztaisa paradigmas kopiju, kurai var mainīt īpašības, nenočakarējot sākotnējo leksēmu DB.
		Paradigm clone;
		try {
			clone = (Paradigm) super.clone();
			clone.lexemesByStem = (HashMap<StemType, HashMap<String, ArrayList<Lexeme>>>)lexemesByStem.clone();
			clone.lexemes = (ArrayList <Lexeme>)lexemes.clone();
			clone.endings = (ArrayList <Ending>)endings.clone();
			clone.id = id;
	        return clone;
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
		} //else {
// TODO - principā jau šī pārbaude ir OK
//			Lexeme duplicatetest = lexemesByID.get(lexeme.getID());
//			if (duplicatetest != null) {
//				System.err.println("Lexemes with duplicate IDs:");
//				duplicatetest.describe(new PrintWriter(System.err));
//				lexeme.describe(new PrintWriter(System.err));
//			}
		//}
		lexemesByID.put(lexeme.getID(), lexeme);

		lexeme.setAllowedStems(stems);

		for (StemType stemType : stems)
		{
			ArrayList<Lexeme> existing = lexemesByStem.get(stemType)
					.computeIfAbsent(lexeme.getStem(stemType), k -> new ArrayList<>());
			existing.add(lexeme);
		}
		lexemes.add(lexeme);

		if (lexeme.getValue(AttributeNames.i_Lemma) == null && getLemmaEnding() != null)
			lexeme.addAttribute(
					AttributeNames.i_Lemma,
					lexeme.getStem(getLemmaEnding().stemType) + getLemmaEnding().getEnding());

		if (this.isMatchingStrong(
				AttributeNames.i_ParadigmProperties,
				AttributeNames.v_HardcodedWordforms)) { // Hardcoded un vietniekvārdu paradigma
			this.lexicon.hardcodedForms.put(lexeme.getID(), lexeme);
		}

		String lemma = lexeme.getValue(AttributeNames.i_Lemma);
        if (lemma.matches(".*[ ./'\\d]+.*") && lemma.length() > 1
				&& !lemma.matches("\\.+")) {
		    this.lexicon.automats.addException(lemma);
        }
	}

	public void removeLexeme (Lexeme lexeme) {
		// ja nebūs tādas leksēmas, tad nekas arī nenotiks
		lexemes.remove(lexeme);
		lexemesByID.remove(lexeme.getID());
		for (StemType stemType : stems)
		{
			ArrayList<Lexeme> matchingstems = lexemesByStem.get(stemType).get(lexeme.getStem(stemType));
			if (matchingstems != null) {
				matchingstems.remove(lexeme);
				if (matchingstems.isEmpty())
					lexemesByStem.get(stemType).remove(lexeme.getStem(stemType));
			}
		}
		this.lexicon.hardcodedForms.remove(lexeme.getID(), lexeme);
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

	/**
	 * Return a list of endings matching given search criterion - an attribute
	 * value pair.
	 * @param attribute	LV name of the attribute to search by
	 * @param value		LV name of the attribute value to search by
	 * @return			list of all found Ending objects (empty, if none found)
	 */
	public ArrayList<Ending> getEndingsByAttribute (String attribute, String value)
	{
		ArrayList<Ending> result = new ArrayList<>();
		for (Ending e : this.endings)
		{
			if (e.isMatchingStrong(attribute, value))
				result.add(e);
		}
		return result;
	}

	/**
	 * Return a list of endings matching given search criterion - an attribute
	 * value pair.
	 * @param filterSet	set of attributes + values to filter by
	 * @return			list of all found Ending objects (empty, if none found)
	 */
	public ArrayList<Ending> getEndingsByAttributes (AttributeValues filterSet)
	{
		ArrayList<Ending> result = new ArrayList<>();
		for (Ending e : this.endings)
		{
			if (e.isMatchingStrongOneSide(filterSet))
				result.add(e);
		}
		return result;
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

	public Set<StemType> getStems() {
		return stems;
	}

	public void setStems(int stemCount) {
		stems = new HashSet<>();
		for (int i = 1; i <= stemCount; i++)
			stems.add(StemType.getFromXmlId(i));

		for (StemType stemType : stems)
			if (!lexemesByStem.containsKey(stemType)) lexemesByStem.put(stemType, new HashMap<>());
		for (StemType stemType : lexemesByStem.keySet())
			if (!stems.contains(stemType)) lexemesByStem.remove(stemType);

		//FIXME - tā, a ko ar leksēmu sakņu skaitiem ta darīt tagad??
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Ending endingByNr(int endingNr) {
		for (Ending ending : endings)
			if (ending.getID() == endingNr) return ending;

		return null;
	}

	public HashMap<String, ArrayList<Lexeme>> getLexemesByStem(StemType stemType) {
		//TODO - jāprotektē
		return lexemesByStem.get(stemType);
	}

	/**
	 * 	Verifies if this stem is a valid stem for this paradigm, based on the
	 * 	last letter(s?) of that stem.
 	 */
	public boolean allowedGuess(String stem) {
		if (allowedGuessEndings.isEmpty()) return true; // FIXME - workaround until all paradigms have this data filled
		if ((allowedGuessEndings.indexOf('!') >= 0) && !this.lexicon.guessAllParadigms) return false;
		if (stem.isEmpty()) return false;

		if (this.id == 12 && stem.endsWith("as")) return true; // FIXME Hardcoded -as inflexible nouns like Lithuanian derived surnames Arvydas etc
		
		char lastchar = stem.charAt(stem.length()-1);
		return (allowedGuessEndings.indexOf(lastchar) >= 0);
	}
}
