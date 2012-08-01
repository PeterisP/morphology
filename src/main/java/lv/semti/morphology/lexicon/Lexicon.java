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

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lv.semti.morphology.analyzer.AllEndings;
import lv.semti.morphology.analyzer.Mijas;
import lv.semti.morphology.analyzer.Variants;
import lv.semti.morphology.attributes.AttributeValues;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Satur leksikona datus - leksēmu sarakstu un to locīšanas informāciju
 * Piedāvā funkcijas tos pārveidot uz/no XML, un papildināt/izdzēst ierakstus.
 *
 * @author Pēteris Paikens
 *
 */
public class Lexicon {

	public final static String DEFAULT_LEXICON_FILE = "Morphology.xml";
	private final static String[] DEFAULT_LEXICON_LOCATIONS = {
		System.getProperty("lv.semti.morphology.lexicon.path"),
		".", "..", "../..", "resources", "src/main/resources", "dist"};

	private String filename;
	private String revision;
	private String licence;
	
	public ArrayList<Paradigm> paradigms; //TODO - nebūtu jābūt publiskam
	private AllEndings allEndings = null;
	protected ArrayList<String> prefixes;
	private ArrayList<String> corpusFileNames = new ArrayList<String>();

	/**
	 * Izveido leksikona objektu no XML faila pēc noklusēšanas
	 *
	 * @throws Exception	parsēšanas kļūdas
	 */
	public Lexicon() throws Exception {
		for (String location : DEFAULT_LEXICON_LOCATIONS) {
			if (location == null) continue;

			File file = new File(location);
			if (file.isDirectory()) file = new File(location + "/" + DEFAULT_LEXICON_FILE);
			System.out.println("Trying to load from '" + file.getCanonicalPath() + "'");
			if (file.exists() && file.isFile()) {
				init(file.getCanonicalPath(), true);
				return;
			}
		}
		
		throw new IOException("Can't find '" + DEFAULT_LEXICON_FILE + "'.");
	}
	
	/**
	 * Izveido leksikona objektu no XML faila
	 *
	 * @param failaVārds	faila vārds, kurā meklēt leksikonu
	 * @throws Exception	parsēšanas kļūdas
	 */
	public Lexicon(String failaVārds) throws Exception {
		init(failaVārds, true);
	}
	
	/**
	 * Izveido leksikona objektu no XML faila
	 *
	 * @param failaVārds	faila vārds, kurā meklēt leksikonu
	 * @param useAuxiliaryLexicons vai lietot papildvārdnīces 
	 * @throws Exception	parsēšanas kļūdas
	 */
	public Lexicon(String failaVārds, boolean useAuxiliaryLexicons) throws Exception {
		init(failaVārds, useAuxiliaryLexicons);
	}
	
	/**
	 * Izveido leksikona objektu no XML plūsmas
	 *
	 * @param failaVārds	plūsma, pa kuru tiek padots leksikons
	 * @throws Exception	parsēšanas kļūdas
	 */
	public Lexicon(InputStream plusma) throws Exception {
		init(plusma);
	}
	
	/**
	 * @return null, if the lexicon is read from an input stream.
	 */
	public String getFilename() {
		return filename;
	}
	
	public String getRevision() {
		return revision;
	}
	
	public int getRevisionNumber() {
		if (revision == null)
			return 0;
		StringTokenizer st = new StringTokenizer(revision, " :$");
		if (st.countTokens() >= 2) {
			st.nextToken();
			String number = st.nextToken();
			try { return Integer.parseInt(number); } catch (java.lang.NumberFormatException e) {} 
		}
		return 0;
	}
	
	protected AllEndings getAllEndings(){
		if (allEndings == null) {
			ArrayList<Ending> endings = new ArrayList<Ending>();
			for (Paradigm paradigm : paradigms) {
				endings.addAll(paradigm.endings);
			}
			allEndings = new AllEndings(endings);
		}
		
		return allEndings;
	}
	
	protected void invalidateAllEndings() {
		allEndings = null;
	}
	
	private void init(String failaVārds, boolean useAuxiliaryLexicons) throws Exception {
		System.err.println("Loading " + failaVārds);
		
		this.filename = failaVārds;

		Document doc = null;
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = docBuilder.parse(new File(failaVārds));

		init_main(doc, new File(failaVārds).getParent(), useAuxiliaryLexicons);
	}
	
	private void init(InputStream plusma) throws Exception {
		System.err.println("Loading the lexicon from an input stream...");
		
		Document doc = null;
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = docBuilder.parse(plusma);

		init_main(doc, "", false);
	}
	
	
	private void init_main(Document doc, String path, boolean useAuxiliaryLexicons) throws Exception {
		Node node = doc.getDocumentElement();
		if (!node.getNodeName().equalsIgnoreCase("Morphology")) throw new Error("Node '" + node.getNodeName() + "' but Morphology expected!");

		Node nodeRevision = node.getAttributes().getNamedItem("revision");
		if (nodeRevision != null)
			revision = nodeRevision.getTextContent();
		
		Node nodeLicence = node.getAttributes().getNamedItem("licence");
		if (nodeLicence != null)
			licence = nodeLicence.getTextContent();
		
		NodeList nodes = node.getChildNodes();

		paradigms = new ArrayList<Paradigm>();

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("Paradigm"))
				addParadigm(new Paradigm(this, nodes.item(i)));
			if (nodes.item(i).getNodeName().equals("Corpus")) {
				Node corpusFileName = nodes.item(i).getAttributes().getNamedItem("FileName");
				Node lexiconType = nodes.item(i).getAttributes().getNamedItem("Type");
				boolean isCore = false;
				if (lexiconType != null) isCore = lexiconType.getTextContent().equalsIgnoreCase("core");
				
				if (corpusFileName != null && (useAuxiliaryLexicons || isCore))
					corpusFileNames.add(corpusFileName.getTextContent());
			}
		}
		
		for (String filename : corpusFileNames) {
			Document doc2 = null;
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc2 = docBuilder.parse(new File(path + java.io.File.separatorChar + filename));

			init_sub(doc2);
		}

		//TODO - nav īsti smuki šāds hardcoded saraksts.
		prefixes = new ArrayList<String>();
		prefixes.add("aiz");
		prefixes.add("ap");
		prefixes.add("at");
		prefixes.add("ie");
		prefixes.add("iz");
		prefixes.add("ne");
		prefixes.add("no");
		prefixes.add("pa");
		prefixes.add("pār");
		prefixes.add("pie");
		prefixes.add("sa");
		prefixes.add("uz");
		
		System.err.println("Lexicon " + (revision != null ? revision : "") + " loaded");
	}

	private void init_sub(Document doc) throws Exception {
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
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void toXML(String failaVārds) throws FileNotFoundException,
	//TODO - būtu nevis faila vārds jāņem, bet outputstream.
			UnsupportedEncodingException, IOException {
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
	 * Saglabā XML formātā apakšleksikona leksēmas
	 *
	 * @param failaVārds 	Faila vārds, kurā saglabāt.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void toXML_sub(String failaVārds, String source) throws FileNotFoundException,
			UnsupportedEncodingException, IOException {	
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
	 * @param failaVārds 	Faila vārds, kurā saglabāt.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
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
		// TODO - kā ar dubultid čekošanu?
		Lexeme rezults = null;
		for (Paradigm vārdgrupa : paradigms) {
			for (Lexeme leksēma : vārdgrupa.lexemes)
				if (leksēma.getID() == nr)
					rezults = leksēma;
		}
		return rezults;
	}

	/**
	 * Atrod leksikonā lielāko šobrīd esošo leksēmas numuru
	 *
	 * @return	lielākais leksēmas numurs, vai 0, ja nav nevienas leksēmas.
	 */
	public int maxLexemeID() {
		int result = 0;
		for (Paradigm paradigm : paradigms) {
			for (Lexeme lexeme : paradigm.lexemes)
				if (lexeme.getID() > result)
					result = lexeme.getID();
		}
		return result;
	}

	/**
	 * Izveido jaunu leksēmu ar norādītajiem parameteriem, un pievieno to leksikonam.
	 * Gadījumā, ja jaunā vārdgrupa ir daudzsakņu, tad pievienos tikai aktuālo sakni, bet pārējās saknes būs tukšas.
	 *
	 * @param word			pilns jaunais pievienojamais vārds
	 * @param endingNr	numurs galotnei, kas ir vārda beigās
	 * @param source			teksts, kas tiks pielikts īpašībā Avots
	 * @return				jaunizveidotā leksēma, vai null, ja nevar izveidot
	 */
	public Lexeme createLexeme(String word, int endingNr, String source) {
	//FIXME - vajadzētu galotnes objektu, nevis numuru.
		Ending ending = endingByID(endingNr);
		String stem;
		try {
			stem = ending.stem(word.toLowerCase());
			ArrayList<Variants> celmi = Mijas.mijuVarianti(ending.stem(word.toLowerCase()), ending.getMija());
			if (celmi.size() == 0) return null; // acīmredzot neder ar miju
			stem = celmi.get(0).celms;
		} catch (Exception e) {
			return null;
		}

		Lexeme rezults = new Lexeme();
		rezults.setStemCount(ending.getParadigm().getStems());
		rezults.setStem(ending.stemID-1, stem); 
		ending.getParadigm().addLexeme(rezults);

		rezults.addAttribute("Avots", source);
		return rezults;
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
	public int maxEndingID() {
		int result = 0;
		for (Paradigm paradigm : paradigms) {
			for (Ending ending : paradigm.endings)
				if (ending.getID() > result)
					result = ending.getID();
		}
		return result;
	}
}
