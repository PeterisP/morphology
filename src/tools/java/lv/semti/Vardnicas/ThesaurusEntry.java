package lv.semti.Vardnicas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Paradigm;

import org.json.simple.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Structured representation of entry header.
 */
public class ThesaurusEntry {
	
	String lemma = null;
	
	/**
	 * Working copy of grammar info.
	 */
	String gram = null;
	
	/**
	 * Original grammar info as it is found in source data.
	 */
	String originalGram = null;
	
	/**
	 * Tidied up grammar info: corrected typos, removed additional
	 * non-grammatical info.
	 * trueGram ~~ originalGram - flags - gramFlags
	 */
	String trueGram = null; //FIXME - outputā jāiekļauj
	
	String source = null; //FIXME - to array
	int paradigm = 0;
	LinkedList<WordSense> senses = new LinkedList<WordSense>();
	
	//TODO - daži flagi ('parasti vsk.' utml) drīzāk ir kā komentāru lauks nevis flagi
	/**
	 * Structured information, extracted from originalGram.
	 */
	LinkedList<String> flags = new LinkedList<String>();
	static Analyzer analyzer = null;
	
	/**
	 * Lemmas identifying entries currently ignored. See also inBlacklist().
	 */
	private static HashSet<String> blacklist = null;
	/**
	 * Known abbreviations and their de-abbreviations.
	 */
	private static HashMap<String,String> gramFlags = null;
	/**
	 * Patterns for identifying (true) grammatical information.
	 */
	private static LinkedList<Pattern> truegramPatterns = null;
	
	// Here the magic magic must happen.
	private void setParadigm() {
		// 1: Lietvārds 1. deklinācija -s
		if (( lemma.endsWith("s") 
			&& gramContains("v.") && !gramContains("-ais")) //FIXME īpašībasvārdi kas nav īpaši norādīti????
			&& !lemma.endsWith("is") && !lemma.endsWith("us") && !gramContains("nenoteiktais vietn.") 
			&& !gramContains("-sāls") && !gramContains("-rudens")
			&& !lemma.endsWith("rudens") && !lemma.endsWith("debess")
			&& !lemma.endsWith("akmens") && !lemma.endsWith("asmens")
			&& !lemma.endsWith("ūdens") && !lemma.endsWith("suns")
			&& !lemma.endsWith("zibens") && !lemma.endsWith("mēness")) {
			if (paradigm > 0)
				System.err.printf(
					"Vārds '%s' gram '%s' atbilst paradigmām %d un %d\n", lemma,
					gram, paradigm, 1);
			
			removeGram("v.");
			removeGram("lietv.");
			paradigm = 1;
		}
					
		// 21: Apstākļa vārds
		//if (gram != null && gram.equalsIgnoreCase("apst.")) return 21;
		
		if (paradigm > 0) {
			// ja gramatikā ir -a, tad pārbaudam vai tiešām izpildās
			assertNounEnding("-a","a", AttributeNames.v_Singular, AttributeNames.v_Genitive);
			assertNounEnding("a","a", AttributeNames.v_Singular, AttributeNames.v_Genitive);
			assertNounEnding("- a","a", AttributeNames.v_Singular, AttributeNames.v_Genitive); //TODO - typo pirmavotā
			assertNounEnding("-ņa","ņa", AttributeNames.v_Singular, AttributeNames.v_Genitive);
			assertNounEnding("-sa","sa", AttributeNames.v_Singular, AttributeNames.v_Genitive);
			assertNounEnding("-ja","ja", AttributeNames.v_Singular, AttributeNames.v_Genitive);
			assertNounEnding("-ļa","ļa", AttributeNames.v_Singular, AttributeNames.v_Genitive);
			assertNounEnding("-ra","ra", AttributeNames.v_Singular, AttributeNames.v_Genitive);
			assertNounEnding("-u","u", AttributeNames.v_Plural, AttributeNames.v_Genitive); //TODO - vai vienmēr tā?
			
			if (gram != null && gram.length() != 0)
				System.err.printf("%s\t('%s' - gram bija %s)\n",gram,lemma,originalGram);
						
			if (analyzer != null) {
				Word analīze = analyzer.analyzeLemma(lemma);
				boolean found = false;
				String paradigmas = "";
				for (Wordform variants : analīze.wordforms) {
					Paradigm paradigmas_variants = variants.getEnding().getParadigm();
					if (paradigmas_variants.getID() == paradigm
						|| (paradigmas_variants.getID() == 13 && paradigm==1)
						|| (paradigmas_variants.getID() == 15 && paradigm==1)) //-iens atvasinājumi
						found = true;
					else paradigmas = paradigmas + " " + String.valueOf(paradigmas_variants.getID());
				}
				if (analīze.isRecognized() && !found) 
					System.err.printf("'%s' - šķiet %d bet leksikonā ir %s\n", lemma, paradigm, paradigmas);

			}
		}
		
		//if (true_gram != null) System.out.printf("Truegram: '%s' out of '%s'\n",true_gram,original_gram);

		//if (gram != null && gram.contains(".:")) System.err.println(original_gram); FIXME - te ir puse typo ...
	}
	
	// What is this?
	// This is for test purposes.
	private void assertNounEnding(
		String gramDesc, String ending, String number, String nouncase)
	{
		// Assertion to verify if analyzer stemchanges match the dictionary.
		if (gramContains(gramDesc) && analyzer != null) { 
			Paradigm p = analyzer.paradigmByID(paradigm);
			//FIXME - kā tad šis strādā ar daudzskaitliniekiem?
			
			ArrayList<Wordform> inflections = analyzer.generateInflections(
				lemma, paradigm);
			for (Wordform wf : inflections) {
				if (wf.isMatchingStrong(AttributeNames.i_Case, nouncase) &&
					wf.isMatchingStrong(AttributeNames.i_Number, number)) {
					
					if (!wf.getToken().endsWith(ending)) 
						System.err.printf(
							"Gram '%s' mismatch - expected to end with -%s but got %s\n",
							gramDesc, ending, wf.getToken());
				}
			}
		}		
		removeGram(gramDesc);
	}
	
	/**
	 * Checks if 'gram' field contains the item.
	 * @param item - what to search.
	 * @return - true, if item found.
	 */
	private boolean gramContains(String item) {
		if (gram==null || gram.length() < 2) return false;
		//TODO - regexp varētu te būt ātrāki, ja nu ātrdarbība kļūst par sāpi
		return gram.trim().equalsIgnoreCase(item) || gram.endsWith(", "+item)
			|| gram.endsWith("; "+item) || gram.endsWith(": "+item)
			|| gram.startsWith(item+", ") || gram.startsWith(item+"; ")
			|| gram.contains(", "+item+",") || gram.contains(", "+item+";")
			|| gram.contains("; "+item+",") || gram.contains("; "+item+";");
	}
	
	/**
	 * Removes the item from 'gram' to consider it as processed.
	 * @param item - item to remove.
	 */
	private void removeGram(String item) {
		if (gram==null) return;
		if (gram.trim().equals(item)) 
			gram="";
		else if (gram.endsWith(", "+item) || gram.endsWith("; "+item) || gram.endsWith(": "+item))
			gram = gram.substring(0, gram.length()-item.length()-2);
		else if (gram.startsWith(item+", ") || gram.startsWith(item+"; "))
			gram = gram.substring(item.length()+2);
		else if (gram.contains(", "+item+","))
			gram = gram.replace(", "+item+"," , ",");
		else if (gram.contains(", "+item+";"))
			gram = gram.replace(", "+item+";" , ";");
		else if (gram.contains("; "+item+";"))
			gram = gram.replace("; "+item+";" , ";");
		else if (gram.contains("; "+item+","))
			gram = gram.replace("; "+item+"," , ";");
	}
	
	/**
	 * Build a JSON representation, designed to load in Tezaurs2 webapp well.
	 * @return JSON representation
	 */
	public String toJSON() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		s.append(String.format("\"Lemma\":\"%s\"", JSONObject.escape(lemma)));
		if (paradigm != 0) {
			s.append(String.format(",\"Paradigm\":%d", paradigm));
			if (analyzer != null) {
				// generate a list of inflected wordforms and format them as JSON array
				ArrayList<Wordform> inflections = analyzer.generateInflections(lemma, paradigm);
				s.append(String.format(",\"Inflections\":%s", formatInflections(inflections) )); 
			}
		}
		if (gram != null && gram.length() > 0)
			s.append(String.format(",\"Gram\":\"%s\"", JSONObject.escape(gram)));
		if (flags.size() > 0) {
			s.append(",\"Flags\":[");
			Iterator<String> i = flags.iterator();
			while (i.hasNext()) {
				s.append(JSONObject.escape(i.next()));
				if (i.hasNext()) s.append(", ");
			}
			s.append(']');
		}
		if (source != null)
			s.append(String.format(",\"Source\":\"%s\"", JSONObject.escape(source)));
		
		s.append(",\"Senses\":[");
		Iterator<WordSense> i = senses.iterator();
		while (i.hasNext()) {
			s.append(i.next().toJSON());
			if (i.hasNext()) s.append(", ");
		}
		s.append(']');
		
		s.append('}');
		return s.toString();
	}
	
	/**
	 *  Formats a list of inflections as an JSON array.
	 */
	private Object formatInflections(ArrayList<Wordform> inflections) {
		StringBuilder s = new StringBuilder();
		s.append('[');
		
		LinkedList<String> showAttrs = new LinkedList<String>();
		showAttrs.add(AttributeNames.i_Word);
		showAttrs.add(AttributeNames.i_Case);
		showAttrs.add(AttributeNames.i_Number);
		
		Iterator<Wordform> i = inflections.iterator();
		while (i.hasNext()) {
			Wordform wf = i.next();
			wf.filterAttributes(showAttrs);
			s.append(wf.toJSON());
			if (i.hasNext()) s.append(", ");
		}
		s.append(']');
		return s.toString();
	}

	// Reads data of a single thesaurus entry from the XML format
	public ThesaurusEntry(Node entry) {
		NodeList fields = entry.getChildNodes(); 
		for (int i = 0; i < fields.getLength(); i++) {
			Node field = fields.item(i);
			String fieldname = field.getNodeName();
			if (fieldname.equals("v")) // word info
				loadVInfo(field);
			else if (fieldname.equals("avots")) // source
				setSource( field.getTextContent() );
			else if (fieldname.equals("g_n")) // all senses
				loadSenseInfo(field);
			else if (fieldname.equals("g_de")) //declined forms
				loadDeclInfo(field);
			else if (fieldname.equals("g_fraz")) //phraseological forms
				loadPhraseInfo(field);
			else System.err.printf("Entry field %s not processed\n", fieldname);
		}
		
		if (inBlacklist()) return;
		
		if (lemma == null)
			System.err.printf("Thesaurus entry without a lemma :(\n");	
		setParadigm();
	}

	private boolean inBlacklist() {
		if (source == null || !source.contains("LLVV")) return true; // FIXME - temporary restriction to focus on LLVV first
		if (blacklist == null)
			initStatics();
		return blacklist.contains(lemma);
	}

	/**
	 * Do the one-time initialization of lookup data.
	 */
	private void initStatics() {
		blacklist = new HashSet<String>();
		BufferedReader ieeja;
		// Removed temporarily as too specific.
		/*
		try {
			ieeja = new BufferedReader(
					new InputStreamReader(
					new FileInputStream("/Users/pet/Dropbox/Resursi/Tezaurs/SV errors.txt"), "UTF-8"));
			String rinda;
			while ((rinda = ieeja.readLine()) != null) {
				if (rinda.contains("<s>") || rinda.contains("</s>") || rinda.isEmpty())
					continue;
				blacklist.add(rinda.trim());
			}		
			ieeja.close();
		} catch (Exception e) {} //TODO - any IO issues ignored
		*/
		
		gramFlags = new HashMap<String,String>();
		
		// Katram šim pārītim pārbaudīs 'gram' lauku un ja atradīs, tad ieliks
		// gramFlags sarakstā / izvāks no gram.
		//TODO - saskaitīt šos flagus, varbūt daļu apvienot (medicīnas, bioloģijas apakšjomas)
		gramFlags.put("anat.", "Anatomija");
		gramFlags.put("arheol.", "Arheoloģija");
		gramFlags.put("arhit.", "Arhitektūra");
		gramFlags.put("astr.", "Astronomija");
		gramFlags.put("av.", "Aviācija");
		gramFlags.put("biol.", "Bioloģija");
		gramFlags.put("biškop.", "Biškopība");
		gramFlags.put("bot.", "Botānika");
		gramFlags.put("būvn.", "Būvniecība");
		gramFlags.put("ekon.", "Ekonomika");
		gramFlags.put("el.", "Elektrotehnika");
		gramFlags.put("etn.", "Etnogrāfija");
		gramFlags.put("farm.", "Farmakoloģija");
		gramFlags.put("filoz.", "Filozofija");	
		gramFlags.put("fin.", "Finansu termins");
		gramFlags.put("fiz.", "Fizika");
		gramFlags.put("fiziol.", "Fizioloģija");
		gramFlags.put("fizk.", "Fiziskā kultūra un sports");
		gramFlags.put("folkl.", "Folklora");
		gramFlags.put("ģeogr.", "Ģeogrāfija");
		gramFlags.put("ģeol.", "Ģeoloģija");
		gramFlags.put("ģeom.", "Ģeometrija");
		gramFlags.put("hidrotehn.", "Hidrotehnika");
		gramFlags.put("inf.", "Informātika");
		gramFlags.put("jur.", "Jurisprudence");
		gramFlags.put("jūrn.", "Jūrniecība");
		gramFlags.put("kap.", "Attiecas uz kapitālistisko iekārtu, kapitālistisko sabiedrību");
		gramFlags.put("kul.", "Kulinārija");
		gramFlags.put("ķīm.", "Ķīmija");
		gramFlags.put("lauks.", "Lauksaimniecība");
		gramFlags.put("literat.", "Literatūrzinātne");
		gramFlags.put("loģ.", "Loģika");
		gramFlags.put("mat.", "Matemātika");
		gramFlags.put("māt.", "Matemātika"); //FIXME - typo pirmavotā
		gramFlags.put("med.", "Medicīna");
		gramFlags.put("medn.", "Medniecība");
		gramFlags.put("meteorol.", "Meteoroloģija");
		gramFlags.put("mežs.", "Mežsaimniecība");
		gramFlags.put("mil.", "Militārās zinātnes");
		gramFlags.put("min.", "Mineraloģija");
		gramFlags.put("mit.", "Mitoloģija");
		gramFlags.put("mūz.", "Mūzika");
		gramFlags.put("pol.", "Politika");
		gramFlags.put("poligr.", "Poligrāfija");
		gramFlags.put("psih.", "Psiholoģija");
		gramFlags.put("ornit.", "Ornitoloģija");
		gramFlags.put("rel.", "Reliģija");
		gramFlags.put("tehn.", "Tehnika");
		gramFlags.put("tekst.", "Tekstilrūpniecība");
		gramFlags.put("val.", "Valodniecība");
		gramFlags.put("vet.", "Veterinārija");
		gramFlags.put("zool.", "Zooloģija");
		
		gramFlags.put("apv.", "Apvidvārds");
		gramFlags.put("novec.", "Novecojis"); //TODO - Novecojis, vēsturisks un neaktuāls apvienot??		
		gramFlags.put("vēst.", "Vēsturisks");
		gramFlags.put("neakt.", "Neaktuāls");
		gramFlags.put("poēt.", "Poētiska stilistiskā nokrāsa");
		gramFlags.put("niev.", "Nievīga ekspresīvā nokrāsa");
		gramFlags.put("iron.", "Ironiska ekspresīvā nokrāsa");
		gramFlags.put("hum.", "Humoristiska ekspresīvā nokrāsa");
		gramFlags.put("vienk.", "Vienkāršrunas stilistiskā nokrāsa");
		gramFlags.put("nevēl.", "Nevēlams"); // TODO - nevēlamos, neliterāros un žargonus apvienot??
		gramFlags.put("nelit.", "Neliterārs");
		gramFlags.put("žarg.", "Žargonvārds");
		gramFlags.put("sar.", "Sarunvaloda");
		
		//TODO - šos drīzāk kā atsevišķu komentāru lauku(s)
		gramFlags.put("parasti vsk.", "Parasti vienskaitlī");
		gramFlags.put("parasti vsk..", "Parasti vienskaitlī"); //FIXME - typo pirmavotā
		gramFlags.put("parasti vsk .", "Parasti vienskaitlī"); //FIXME - typo pirmavotā
		gramFlags.put("-parasti vsk.", "Parasti vienskaitlī"); //FIXME - typo pirmavotā
		gramFlags.put("parasti vsk", "Parasti vienskaitlī");
		gramFlags.put("par. vsk.", "Parasti vienskaitlī");
		gramFlags.put("tikai vsk.", "Tikai vienskaitlī");
		gramFlags.put("vsk.", "Vienskaitlī");
		gramFlags.put("parasti dsk.", "Parasti daudzskaitlī");		
		gramFlags.put("tikai dsk.", "Tikai daudzskaitlī");
		gramFlags.put("pareti.", "Pareti");
		gramFlags.put("pareti", "Pareti");
		gramFlags.put("reti.", "Reti");
		gramFlags.put("reti", "Reti");
		gramFlags.put("retāk", "Retāk");
		
		truegramPatterns = new LinkedList<Pattern>();
		truegramPatterns.add(Pattern.compile("^(.*)(vokatīvs [^ ,;:]+)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(bieži lok\\.: [^ ,;:]+)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(parasti lok\\.: [^ ,;:]+)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(parasti vsk\\. lok\\.: [^ ,;:]+)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(parasti ģen\\.: [^ ,;:]+)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(pamata skait(\\.|ļa vārds) lietv(\\.|ārda) nozīmē\\.?)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(\\(?parasti folkl\\.(\\)\\.)?)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(parasti saistītā valodā\\.)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(apst\\. nozīmē)(.*)$"));
		truegramPatterns.add(Pattern.compile("^(.*)(\\(vācu \"krava\"\\))(.*)$"));
	}

	/**
	 *  Phraseological uses of the word. Not sure what to do with them...
	 */
	private void loadPhraseInfo(Node field) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Declination forms - in Lexicon sense, they are separate lexemes,
	 * alternate wordforms but with a link to the same dictionary entry. 
	 */
	private void loadDeclInfo(Node field) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * reads the information about the (multiple) word senses for that entry.
	 * NB! they may have their own 'gram.' entries, not sure how best to
	 * reconcile.
	 */
	private void loadSenseInfo(Node allSenses) {
		NodeList senseNodes = allSenses.getChildNodes(); 
		for (int i = 0; i < senseNodes.getLength(); i++) {
			Node sense = senseNodes.item(i);
			
			// We're ignoring the number of the senses - it's in "nr" field, we
			//assume (not tested) that it matches the order in file
			if (sense.getNodeName().equals("n"))
				senses.add(new WordSense(sense));
			else
				System.err.printf(
					"G_n entry field %s not processed, expected only 'n'.\n",
					sense.getNodeName());
		}
	}

	/**
	 * Loads thesaurus field 'v' which contains info about the word form(s) 
	 */
	private void loadVInfo(Node vField) {
		NodeList fields = vField.getChildNodes(); 
		for (int i = 0; i < fields.getLength(); i++) {
			Node field = fields.item(i);
			String fieldname = field.getNodeName();
			if (fieldname.equals("vf"))
				setLemma( field.getTextContent() );
			else if (fieldname.equals("gram"))
				setGram( field.getTextContent() );
			else System.err.printf("V entry field %s not processed\n", fieldname);
		}
	}

	// Setters that check if the information isn't already filled, to detect
	// possible overwritten data.
	private void setSource(String textContent) {
		if (source != null)
			System.err.printf("Duplicate info for field 'source' : '%s' and '%s'", source, textContent);
		source = textContent;
	}
	
	private void setGram(String textContent) {
		if (gram != null)
			System.err.printf("Duplicate info for field 'gram' : '%s' and '%s'", gram, textContent);
		gram = textContent;
		originalGram = textContent;
		
		if (gram.endsWith(";,")) //TODO - typo pirmavotā
			gram = gram.substring(0,gram.length()-2);
		
		if (gram.endsWith(";") || gram.endsWith(",")) //TODO - typo pirmavotā
			gram = gram.substring(0,gram.length()-1);
		
		gram = gram.replace("lietv. -a", "lietv., -a"); //TODO - typo pirmavotā
		gram = gram.replace(";novec", "; novec"); //TODO - typo pirmavotā
		gram = gram.replace("novec. (", "novec.; ("); //TODO - typo pirmavotā
		gram = gram.replace("parasti vsk. med.", "parasti vsk., med."); //TODO - typo pirmavotā
		
		if (gramFlags == null) initStatics();
				
		// katram gram_flags pārītim pārbaudīs 'gram' lauku un ja atradīs, tad
		// ieliks Flags sarakstā / izvāks no gram.
		for (Entry<String, String> entry : gramFlags.entrySet()) {
			if (gramContains(entry.getKey())) {			
				flags.add(entry.getValue());
				removeGram(entry.getKey());
			}			
		}		
		
		if (truegramPatterns == null) initStatics();
		for (Pattern p : truegramPatterns) {
			Matcher m = p.matcher(gram); 
			if (m.matches()) {
				String extract = m.replaceAll("$2");
				if (trueGram != null) {
					//System.err.printf("Duplicate info for field 'Truegram' : '%s' and '%s'\n", true_gram, extract);
					trueGram = trueGram+"; "+extract;
				} else trueGram = extract;
				removeGram(extract);
			}					
		}
		
		//FIXME - laižam vēlreiz, jo dažas dīvainas kombinācijas (koli iekavas utml) tikai tad iztīra...
		for (Entry<String, String> entry : gramFlags.entrySet()) {
			if (gramContains(entry.getKey())) {			
				flags.add(entry.getValue());
				removeGram(entry.getKey());
			}			
		}
	}
	
	private void setLemma(String textContent) {
		if (lemma != null)
			System.err.printf(
				"Duplicate info for field 'lemma' : '%s' and '%s'", lemma,
				textContent);
		lemma = textContent;
	}

	
	/**
	 * A single sense of a lexical entry. May contain examples or grammatic info
	 * that is specific to that single sense, not the whole entry.
	 */
	public class WordSense {
		String definition = ""; // FIXME - needs more structure
		String examples = "";  // FIXME - needs more structure 
	 	String gram = null;
	 	LinkedList<WordSense> subsenses = new LinkedList<WordSense>();
		
		public WordSense(Node sense) {
			NodeList fields = sense.getChildNodes(); 
			for (int i = 0; i < fields.getLength(); i++) {
				Node field = fields.item(i);
				String fieldname = field.getNodeName();
				if (fieldname.equals("d"))  // definition
					definition += field.getTextContent() + "\n"; //FIXME - not checking for subfields
				else if (fieldname.equals("gram"))  // grammatical info
					this.setGram(field.getTextContent());
				else if (fieldname.equals("g_piem"))  // Usage examples
					examples += field.getTextContent() + "\n"; //FIXME - not checking for subfields
				else if (fieldname.equals("g_an"))  // Subsenses
					loadSenseInfo(field); 
				else System.err.printf("Word sense entry field %s not processed\n", fieldname);
			}
		}
		
		public String toJSON() {
			StringBuilder s = new StringBuilder();
			s.append('{');
			s.append(String.format("\"Definitions\":\"%s\"", JSONObject.escape(definition.trim())));
			if (gram != null)
				s.append(String.format(",\"Gram\":\"%s\"", JSONObject.escape(gram)));
			if (examples.length() > 0)
				s.append(String.format(",\"Examples\":\"%s\"", JSONObject.escape(examples.trim())));
			if (subsenses.size() > 0) {
				s.append(",\"Subsenses\":[");
				Iterator<WordSense> i = subsenses.iterator();
				while (i.hasNext()) {
					s.append(i.next().toJSON());
					if (i.hasNext()) s.append(", ");
				}
				s.append(']');
			}
			s.append('}');
			return s.toString();
		}
		
		/**
		 * Setters checks if the information isn't already filled, to detect
		 * possible overwritten data.
		 */
		private void setGram(String textContent) {
			if (gram != null)
				System.err.printf(
					"Duplicate info for word sense field 'gram' : '%s' and '%s'",
					gram, textContent);
			gram = textContent;
		}
		
		/**
		 * Reads the information about the (possibly multiple) subsenses for
		 * that sense.
		 */
		private void loadSenseInfo(Node allSenses) {
			NodeList senseNodes = allSenses.getChildNodes(); 
			for (int i = 0; i < senseNodes.getLength(); i++) {
				Node sense = senseNodes.item(i);
				// We're ignoring the number of the senses - it's in "nr" field,
				// we assume (not tested) that it matches the order in file
				if (sense.getNodeName().equals("n"))
					subsenses.add(new WordSense(sense));
				else
					System.err.printf(
						"Subsense entry field %s not processed, expected only 'n'.\n",
						sense.getNodeName());
			}
		}
	}

}
