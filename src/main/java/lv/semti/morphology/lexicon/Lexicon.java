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

import lv.semti.morphology.analyzer.AllEndings;
import lv.semti.morphology.analyzer.Mijas;
import lv.semti.morphology.analyzer.Trie;
import lv.semti.morphology.analyzer.Variants;
import lv.semti.morphology.attributes.AttributeNames;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Satur leksikona datus - leksēmu sarakstu un to locīšanas informāciju
 * Piedāvā funkcijas tos pārveidot uz/no XML, un papildināt/izdzēst ierakstus.
 *
 * @author Pēteris Paikens
 *
 */
public class Lexicon {
	public final static String DEFAULT_LEXICON_FILE = "Lexicon_v2.xml";
	public static int proper_name_frequency_floor = 2; // When loading proper name lexemes, entries that have a frequency ("Skaits") field will be ignored and not loaded

	protected String filename;
	protected String NEGATION_PREFIX = "ne";
	protected String DEBITIVE_PREFIX = "jā";
	protected String SUPERLATIVE_PREFIX = "vis";

	public String getRevision() {
		return revision;
	}

	private String revision;
	private String licence;
	
	public ArrayList<Paradigm> paradigms; //TODO - nebūtu jābūt publiskam, vajag tikai read-only iterēt
	private AllEndings allEndings = null;
	protected ArrayList<String> prefixes;
	private ArrayList<String> corpusFileNames = new ArrayList<String>();

	// Vārdu lielo/mazo burtu nošķiršana
	protected static Pattern p_firstcap = Pattern.compile("\\p{Lu}.*");
	protected static Pattern p_allcaps = Pattern.compile("(\\p{Lu})*");
	protected static Pattern p_doublesurname = Pattern.compile("\\p{Lu}.+-\\p{Lu}.+");

	protected Multimap<Integer, Lexeme> hardcodedForms = ArrayListMultimap.create();
	public Trie automats = new Trie();

	public boolean guessAllParadigms = false; // Attempt guessing words even in paradigms where AllowedGuessEndings are marked with !
	/**
	 * Creates a lexicon object from the default location in JAR resources
	 *
	 * @throws Exception	parsing errors
	 */
	public Lexicon() throws Exception {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(DEFAULT_LEXICON_FILE);
		if (stream != null) {
			init(stream, true);
		}
		else throw new IOException("Can't find '" + DEFAULT_LEXICON_FILE + "'.");
	}

	public Lexicon(boolean useAuxiliaryLexicons) throws Exception {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(DEFAULT_LEXICON_FILE);
		if (stream != null) {
			init(stream, useAuxiliaryLexicons);
		}
		else throw new IOException("Can't find '" + DEFAULT_LEXICON_FILE + "'.");
	}
	
	/**
	 * Izveido leksikona objektu no XML faila
	 *
	 * @param filename	faila vārds, kurā meklēt leksikonu
	 * @throws Exception	parsēšanas kļūdas
	 */
	public Lexicon(String filename) throws Exception {
		init(filename, true);
	}
	
	/**
	 * Izveido leksikona objektu no XML faila
	 *
	 * @param lexiconFileName	faila vārds, kurā meklēt leksikonu
	 * @param useAuxiliaryLexicons vai lietot papildvārdnīces 
	 * @throws Exception	parsēšanas kļūdas
	 */
	public Lexicon(String lexiconFileName, boolean useAuxiliaryLexicons) throws Exception {
		init(lexiconFileName, useAuxiliaryLexicons);
	}

	public Lexicon(String lexiconFileName, ArrayList<String> blacklist) throws Exception{
		init(lexiconFileName, blacklist);
	}

	/**
	 * @return null, if the lexicon is read from an input stream.
	 */
	public String getFilename() {
		return filename;
	}

	protected AllEndings getAllEndings(){
		if (allEndings == null) {
			ArrayList<Ending> endings = new ArrayList<Ending>();
			for (Paradigm paradigm : paradigms) {
				if (!paradigm.isMatchingStrong(AttributeNames.i_ParadigmProperties, AttributeNames.v_OnlyHardcodedWordforms))
					endings.addAll(paradigm.endings);
			}
			allEndings = new AllEndings(endings);
		}
		
		return allEndings;
	}
	
	void invalidateAllEndings() {
		allEndings = null;
	}
	
	private void init(String fileName, boolean useAuxiliaryLexicons) throws Exception {
		System.err.println("Loading " + fileName);
		this.filename = fileName;
        Document doc;
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (stream != null) {
            doc = docBuilder.parse(stream);
        } else doc = docBuilder.parse(new File(fileName));

		init_main(doc, new File(fileName).getParent(), useAuxiliaryLexicons);
	}
	private void init(String fileName, ArrayList<String> blacklist) throws Exception {
		System.err.println("Loading " + fileName);
		this.filename = fileName;
		Document doc;
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (stream != null) {
            doc = docBuilder.parse(stream);
        } else doc = docBuilder.parse(new File(fileName));

		init_main(doc, new File(fileName).getParent(), blacklist);
	}	

	private void init(InputStream plusma, boolean useAuxiliaryLexicons) throws Exception {
		System.err.println("Loading the lexicon from an input stream...");
		
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = docBuilder.parse(plusma);

		init_main(doc, null, useAuxiliaryLexicons);
	}

	private void init_main(Document doc, String path, boolean useAuxiliaryLexicons) throws Exception {
		init_main(doc, path, useAuxiliaryLexicons, true, null);
	}
	
	private void init_main(Document doc, String path, ArrayList<String> blacklist) throws Exception{
		init_main(doc, path, true, true, blacklist);		
	}
	
	private void init_main(Document doc, String path, boolean useAuxiliaryLexicons, boolean useCore, ArrayList<String> blacklist) throws Exception {
		Node node = doc.getDocumentElement();
		if (!node.getNodeName().equalsIgnoreCase("Morphology")) throw new Error("Node '" + node.getNodeName() + "' but Morphology expected!");

		Node nodeRevision = node.getAttributes().getNamedItem("revision");
		if (nodeRevision != null)
			revision = nodeRevision.getTextContent();
		
		Node nodeLicence = node.getAttributes().getNamedItem("licence");
		if (nodeLicence != null)
			licence = nodeLicence.getTextContent();

		NodeList nodes = node.getChildNodes();

		prefixes = new ArrayList<String>();
		paradigms = new ArrayList<Paradigm>();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Paradigm"))
				addParadigm(new Paradigm(this, nodes.item(i)));
			if (nodes.item(i).getNodeName().equals("Corpus")) {
				Node corpusFileName = nodes.item(i).getAttributes().getNamedItem("FileName");
				Node lexiconType = nodes.item(i).getAttributes().getNamedItem("Type");
				boolean isCore = false;
				if (lexiconType != null) isCore = lexiconType.getTextContent().equalsIgnoreCase("core");
				
				if (corpusFileName != null && (useAuxiliaryLexicons || (isCore && useCore)))
					corpusFileNames.add(corpusFileName.getTextContent());
			}
			if (nodes.item(i).getNodeName().equals("Prefixes")) {
				this.loadPrefixes(nodes.item(i));
			}
		}
		
		for (String filename : corpusFileNames) {
			if (blacklist != null && blacklist.contains(filename)) continue; //FIXME - case sensitivity?

            if (filename.endsWith(".xml")) {
                Document doc2;
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                if (path != null) {
                    String fullname = path + java.io.File.separatorChar + filename;
                    doc2 = docBuilder.parse(new File(fullname));
                } else {
                    doc2 = docBuilder.parse(getClass().getClassLoader().getResourceAsStream(filename));
                }
                load_sublexicon_xml(doc2);
            } else if (filename.endsWith(".json")) {
                if (path != null) {
                    String fullname = path + java.io.File.separatorChar + filename;
                    load_sublexicon_json(new FileInputStream(new File(fullname)));
                } else {
                    load_sublexicon_json(getClass().getClassLoader().getResourceAsStream(filename));
                }
            } else throw new Error(String.format("Unsupported file format for sublexicon '%s'", filename));
		}

		automats.initializeExceptions();
		System.err.println("Lexicon " + (revision != null ? revision : "") + " loaded");
	}

	private void loadPrefixes(Node node) {
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Negation")) {
				this.NEGATION_PREFIX = nodes.item(i).getTextContent();
			}
			if (nodes.item(i).getNodeName().equals("Superlative")) {
				this.SUPERLATIVE_PREFIX = nodes.item(i).getTextContent();
			}
			if (nodes.item(i).getNodeName().equals("Debitive")) {
				this.DEBITIVE_PREFIX = nodes.item(i).getTextContent();
			}
			if (nodes.item(i).getNodeName().equals("VerbPrefix")) {
				this.prefixes.add(nodes.item(i).getTextContent());
			}
		}
	}

	private void load_sublexicon_json(InputStream stream) throws ParseException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        JSONParser parser = new JSONParser();
        String json_row;
        try {
            while ((json_row = reader.readLine()) != null) {
                Lexeme l = new Lexeme((JSONObject) parser.parse(json_row), this);
                if (l.isMatchingStrong(AttributeNames.i_EntryName, "irt:1")
						|| l.isMatchingStrong(AttributeNames.i_EntryName, "irt")
						|| l.isMatchingStrong(AttributeNames.i_EntryName, "art:1")
                        || l.isMatchingStrong(AttributeNames.i_EntryName, "art")) {
                    l.addAttribute(AttributeNames.i_Frequency, AttributeNames.v_Rare);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

    private void load_sublexicon_xml(Document doc) throws Exception {
		Node node = doc.getDocumentElement();
		if (!node.getNodeName().equalsIgnoreCase("Lexicon")) throw new Error("Node '" + node.getNodeName() + "' but Lexicon expected!");
	
		NodeList nodes = node.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Paradigm")) {
				Node n = nodes.item(i).getAttributes().getNamedItem("ID");
				if (n != null) {
					int paradigmID = Integer.parseInt(n.getTextContent());
					Paradigm paradigm = this.paradigmByID(paradigmID);
					if (paradigm != null) paradigm.addLexemesFromXML(nodes.item(i));
					else throw new Exception(String.format("When loading subcorpus, cannot find paradigm %d in main morphology", paradigmID));
				}
			}
		}
	}
	
	/**
	 * Saglabā visus leksikona datus, vārdgrupas un galotnes ieskaitot, XML formātā.
	 * Teksta kodējums tiek likts UTF-8, un pietiek ar XML 1.0, ja XML parseris ir korekts,
	 * lai var būt latviešu burti atribūtos.
	 *
	 * @param failaVārds 	Faila vārds, kurā saglabāt.
	 * @throws IOException
	 */
	public void toXML(String failaVārds) throws IOException {
	//TODO - būtu nevis faila vārds jāņem, bet outputstream.
		System.out.println("Warning! XML saving possibly obsolete after multuple-lexicon changes");
		
		File file = new File(failaVārds);
		File newfile = new File(failaVārds + ".new");
		File backupfile = new File(failaVārds + ".bak");
		
		Writer straume = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(newfile), "UTF-8"));
		straume.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		straume.write("<Lexicon revision=\"" + (revision != null ? revision : "") + "\" licence=\"" + (licence != null ? licence : "") + "\">\n");
		for (Paradigm paradigm : paradigms) {
			paradigm.toXML(straume);
		}
		straume.write("</Lexicon>");
		straume.close();
		
		// remove old backup file
		if (backupfile.exists())
			backupfile.delete();
		
		// backup existing file
		if (file.exists())
			file.renameTo(backupfile);
		
		newfile.renameTo(file);
	}
	
	
	/**
	 * Saglabā XML formātā apakšleksikona leksēmas - tikai taas, kuraam source sakriit ar noraadiito
	 *
	 * @param failaVārds 	Faila vārds, kurā saglabāt.
	 * @throws IOException
	 */
	public void toXML_sub(String failaVārds, String source) throws IOException {
		File file = new File(failaVārds);
		File newfile = new File(failaVārds + ".new");
		File backupfile = new File(failaVārds + ".bak");
		
		Writer straume = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(newfile), "UTF-8"));
		straume.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		straume.write("<Lexicon revision=\"" + (revision != null ? revision : "") + "\" licence=\"" + (licence != null ? licence : "") + "\">\n");
		for (Paradigm paradigm : paradigms) {
			paradigm.toXML_sub(straume, source);
		}
		straume.write("</Lexicon>");
		straume.close();
		
		// remove old backup file
		if (backupfile.exists())
			backupfile.delete();
		
		// backup existing file
		if (file.exists())
			file.renameTo(backupfile);
		
		newfile.renameTo(file);
	}
	
	/**
	 * Saglabā visus leksikona datus, vārdgrupas un galotnes ieskaitot, XML formātā.
	 * Teksta kodējums tiek likts UTF-8, un pietiek ar XML 1.0, ja XML parseris ir korekts,
	 * lai var būt latviešu burti atribūtos.
	 *
	 * @param plusma 	Faila vārds, kurā saglabāt.
	 * @throws IOException
	 */
	public void toXML(OutputStream plusma) throws IOException {
		System.out.println("Warning! XML saving possibly obsolete after multuple-lexicon changes");
		
		Writer straume = new BufferedWriter(new OutputStreamWriter(plusma, "UTF-8"));
		straume.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		straume.write("<Lexicon revision=\"" + (revision != null ? revision : "") + "\" licence=\"" + (licence != null ? licence : "") + "\">\n");
		for (Paradigm paradigm : paradigms) {
			paradigm.toXML(straume);
		}
		straume.write("</Lexicon>");
		straume.close();
	}

	
	/**
	 * Sameklē leksikonā vārdgrupu ar norādīto numuru.
	 *
	 * @param nr	vārdgrupas numurs.
	 * @return		atrastā vārdgrupa, vai arī null, ja nav atrasts.
	 */
	public Paradigm paradigmByID(int nr) {
		//FIXME - vispār vajadzētu likvidēt to atsauci uz numuriem pēc iespējas.
		Paradigm rezults = null;
		for (Paradigm vārdgrupa : paradigms) {
			if (vārdgrupa.getID() == nr)
				rezults = vārdgrupa;
		}
		return rezults;
	}

	/**
	 * Sameklē leksikonā vārdgrupu ar norādīto numuru.
	 *
	 * @param name	vārdgrupas vārdiskais identifikators
	 * @return		atrastā vārdgrupa, vai arī null, ja nav atrasts.
	 */
	public Paradigm paradigmByName(String name) {
		Paradigm rezults = null;
		for (Paradigm vārdgrupa : paradigms) {
			if (vārdgrupa.getName().equalsIgnoreCase(name))
				rezults = vārdgrupa;
		}
		return rezults;
	}

	/**
	 * Sameklē leksikonā galotni ar norādīto numuru.
	 *
	 * @param nr	galotnes numurs.
	 * @return		atrastā galotne, vai arī null, ja nav atrasts.
	 */
	public Ending endingByID(int nr) {
		return getAllEndings().endingByID(nr);
	}

	/**
	 * Sameklē leksikonā leksēmu ar norādīto numuru.
	 * Ja nu ir vairākas gadījušās ar vienādu numuru, tad atrod vienu no tām.
	 *
	 * @param nr	leksēmas numurs.
	 * @return		atrastā leksēma, vai arī null, ja nav atrasts.
	 */
	public Lexeme lexemeByID(int nr) {
		Lexeme rezults = null;
		for (Paradigm paradigm : paradigms) {
			if (paradigm.lexemesByID.get(nr) != null) {
				// TODO - hmm, nepamanīs ja ir vienādi ID dažādās paradigmās
				rezults = paradigm.lexemesByID.get(nr);
			}
		}
		return rezults;
	}

	/**
	 * Iedod jaunu unikālu leksēmas numuru
	 *
	 * @return	jauns leksēmas numurs
	 */
	private int lexeme_id_counter = 1100000;
	int newLexemeID() {
		lexeme_id_counter += 1;
		while (lexemeByID(lexeme_id_counter) != null)
			lexeme_id_counter += 1; // ja nu ir ielādēts jau kāds virs miljona, tad būs lēni bet vismaz korekti
		return lexeme_id_counter;
	}

	/**
	 * Creates a new lexeme based on a wordform with a known ending ID, and appends it to the lexicon
	 * NB! If the paradigm needs multiple stems (1st conjugation verbs) then only the lemma stem will be added, and the other stems will be empty and need to be filled later
	 *
	 * @param word		full wordform of the word to be added
	 * @param ending	ending object of the word's lemma
	 * @param source	Description field of the lexeme source
	 * @return			The created lexeme or NULL if it couldn't be created
	 */
	public Lexeme createLexeme(String word, Ending ending, String source) {
		String stem;
		try {
			stem = ending.stem(word.toLowerCase());
			int mija = ending.getMija();
			if (mija != 0 && mija != 3) { // don't try to apply comparative and superlative forms
				ArrayList<Variants> celmi = Mijas.mijuVarianti(stem, mija, word.matches("\\p{Lu}.*"));
				if (celmi.size() == 0) return null; // acīmredzot neder ar miju
				// FIXME ! Nevajadzētu te būt iespējai uz null!
				stem = celmi.get(0).celms;
				// FIXME - vai te ir ok naivi ņemt pirmo variantu ?
			}
		} catch (Exception e) {
            System.err.print(word + Integer.toString(ending.getID()) + source);
			System.err.print(e.getStackTrace());
			return null;
		}

		Lexeme rezults = new Lexeme();
		rezults.setStemCount(ending.getParadigm().getStems());
		rezults.setStem(ending.stemID-1, stem); 
		ending.getParadigm().addLexeme(rezults); // At this moment the actual lemma is generated
		String lemma = rezults.getValue(AttributeNames.i_Lemma);
		lemma = recapitalize(lemma, word);
		rezults.addAttribute(AttributeNames.i_Lemma, lemma);

		rezults.addAttribute(AttributeNames.i_Source, source);
		clearCache();
		return rezults;
	}
	
	/**
	 * Creates a new lexeme based on a wordform with a known paradigm ID, and appends it to the lexicon
	 * NB! If the paradigm needs multiple stems (1st conjugation verbs) then only the lemma stem will be added, and the other stems will be empty and need to be filled later
	 *
	 * @param word		full wordform of the word to be added
	 * @param paradigmID	ID of the paradigm
	 * @param source	Description field of the lexeme source
	 * @return			The created lexeme or NULL if it couldn't be created
	 */
	public Lexeme createLexemeFromParadigm(String word, int paradigmID, String source) throws Exception{
		Paradigm p = this.paradigmByID(paradigmID);
		if (p==null)
			throw new Exception(String.format("createLexemeFromParadigm - invalid paradigm id %d passed for lexeme %s", paradigmID, word));
		if (word==null)
			throw new Exception("createLexemeFromParadigm - null lexeme string passed");
		if (p.getLemmaEnding()==null)
			throw new Exception(String.format("createLexemeFromParadigm - null lemma ending at paradigm id %d for lexeme %s", paradigmID, word));
		
		if (word.endsWith(p.getLemmaEnding().getEnding())) // If we've been passed the appropriate lemma already 
			return this.createLexeme(word, p.getLemmaEnding(), source);
		
		// if there's some other wordform, then we'll try to find it. 
		// TODO - this assumes that the lemma will be the same regardless of which wordform we choose. Maybe that's not true for some stemchanges.
		for (Ending e : p.endings) {
			if (e.isMatchingStrong(AttributeNames.i_Case, AttributeNames.v_Vocative))
				continue;
			if (word.endsWith(e.getEnding()))
				return this.createLexeme(word, e, source);
		}
		
		throw new Exception(String.format("createLexemeFromParadigm - couldn't create lexeme %s with paradigm %d", word, paradigmID));
	}

	/**
	 * Pieliek norādīto vārdgrupu leksikonam.
	 * Ja vārdgrupai numurs ir 0, tad arī uzģenerē tai jaunu numuru.
	 *
	 * @param paradigm	vārdgrupa, kuru pielikt
	 */
	public void addParadigm (Paradigm paradigm) {
		if (paradigm.getID() == 0) {
			int maxnr = 0;
			for (Paradigm vārdgrupa : paradigms) {
				if (vārdgrupa.getID() > maxnr) maxnr = vārdgrupa.getID();
			}
			paradigm.setID (maxnr + 1);
		}

		paradigms.add(paradigm);
	}

	/**
	 * Izņem norādīto vārdgrupu no leksikona datiem.
	 *
	 * @param paradigm
	 */
	public void removeParadigm (Paradigm paradigm) {
		paradigms.remove(paradigm);
	}

	/**
	 * Atrod leksikonā lielāko šobrīd esošo galotnes numuru
	 *
	 * @return	lielākais galotnes numurs, vai 0, ja nav nevienas leksēmas.
	 */
    int maxEndingID() {
		int result = 0;
		for (Paradigm paradigm : paradigms) {
			for (Ending ending : paradigm.endings)
				if (ending.getID() > result)
					result = ending.getID();
		}
		return result;
	}
	
	/**
	 * Clears cache, if any.
	 */
	public void clearCache () {}

	public static String recapitalize(String word, String originalWord) {
		if (p_firstcap.matcher(originalWord).matches())
			word = word.substring(0, 1).toUpperCase() + word.substring(1,word.length());
		if (p_allcaps.matcher(originalWord).matches())
			word = word.toUpperCase();
		if (p_doublesurname.matcher(originalWord).matches()) {
			int otrslielais = word.indexOf("-")+1;
			if (otrslielais > -1) // nočekojam gadījumam ja nu originalWord'ā ir '-' bet lemmā nav
				word = word.substring(0, otrslielais) + word.substring(otrslielais, otrslielais+1).toUpperCase() + word.substring(otrslielais+1,word.length());
		}
		return word;
	}

}
