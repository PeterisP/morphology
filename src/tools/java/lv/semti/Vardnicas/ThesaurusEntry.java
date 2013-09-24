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

public class ThesaurusEntry {
	String lemma = null;
	String gram = null;
	String original_gram = null;
	String true_gram = null; //FIXME - outputā jāiekļauj
	String source = null; //FIXME - to array
	int paradigm = 0;
	LinkedList<WordSense> senses = new LinkedList<WordSense>();
	LinkedList<String> flags = new LinkedList<String>(); //TODO - daži flagi ('parasti vsk.' utml) drīzāk ir kā komentāru lauks nevis flagi
	static Analyzer analyzer = null;
	private static HashSet<String> blacklist = null;
	private static HashMap<String,String> gram_flags = null;
	private static LinkedList<Pattern> truegram_patterns = null;
	
	private void setParadigm() {
		// 1: Lietvārds 1. deklinācija -s
		if (( lemma.endsWith("s") 
				&& gram_contains("v.") && !gram_contains("-ais")) //FIXME īpašībasvārdi kas nav īpaši norādīti????
			&& !lemma.endsWith("is") && !lemma.endsWith("us") && !gram_contains("nenoteiktais vietn.") 
			&& !gram_contains("-sāls") && !gram_contains("-rudens") && !lemma.endsWith("rudens") && !lemma.endsWith("debess") && !lemma.endsWith("akmens") && !lemma.endsWith("asmens") && !lemma.endsWith("ūdens") && !lemma.endsWith("suns") && !lemma.endsWith("zibens") && !lemma.endsWith("mēness")) {
			if (paradigm > 0) System.err.printf("Vārds '%s' gram '%s' atbilst paradigmām %d un %d\n",lemma,gram,paradigm,1);
			
			remove_gram("v.");
			remove_gram("lietv.");
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
				System.err.printf("%s\t('%s' - gram bija %s)\n",gram,lemma,original_gram);
						
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

	private void assertNounEnding(String gram_desc, String ending, String number, String nouncase) {
		if (gram_contains(gram_desc) && analyzer != null) { // assertion to verify if analyzer stemchanges match the dictionary
			Paradigm p = analyzer.paradigmByID(paradigm);
			//FIXME - kā tad šis strādā ar daudzskaitliniekiem?
			
			ArrayList<Wordform> inflections = analyzer.generateInflections(lemma, paradigm);
			for (Wordform wf : inflections) {
				if (wf.isMatchingStrong(AttributeNames.i_Case, nouncase) &&
					wf.isMatchingStrong(AttributeNames.i_Number, number)) {
					
					if (!wf.getToken().endsWith(ending)) 
						System.err.printf("Gram '%s' mismatch - expected to end with -%s but got %s\n", gram_desc, ending, wf.getToken());
				}
			}
		}		
		remove_gram(gram_desc);
	}
	
	// Checks if 'gram' field contains the item
	private boolean gram_contains(String item) {
		if (gram==null || gram.length() < 2) return false;
		//TODO - regexp varētu te būt ātrāki, ja nu ātrdarbība kļūst par sāpi
		return gram.trim().equalsIgnoreCase(item) || gram.endsWith(", "+item) || gram.endsWith("; "+item) || gram.endsWith(": "+item) || gram.startsWith(item+", ") || gram.startsWith(item+"; ") || gram.contains(", "+item+",") || gram.contains(", "+item+";") || gram.contains("; "+item+",") || gram.contains("; "+item+";");
	}
	// Removes the item from 'gram' to consider it as processed.
	private void remove_gram(String item) {
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
	
	// Build a JSON representation, designed to load in Tezaurs2 webapp well
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
		if (gram != null && gram.length() > 0) s.append(String.format(",\"Gram\":\"%s\"", JSONObject.escape(gram)));
		if (flags.size() > 0) {
			s.append(",\"Flags\":[");
			Iterator<String> i = flags.iterator();
			while (i.hasNext()) {
				s.append(JSONObject.escape(i.next()));
				if (i.hasNext()) s.append(", ");
			}
			s.append(']');
		}
		if (source != null) s.append(String.format(",\"Source\":\"%s\"", JSONObject.escape(source)));
		
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
	
	// Formats a list of inflections as an JSON array
	private Object formatInflections(ArrayList<Wordform> inflections) {
		StringBuilder s = new StringBuilder();
		s.append('[');
		
		LinkedList<String> showAttrs = new LinkedList<String>();
		showAttrs.add(AttributeNames.i_Word); showAttrs.add(AttributeNames.i_Case); showAttrs.add(AttributeNames.i_Number);
		
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
				load_v_info(field);
			else if (fieldname.equals("avots")) // source
				setSource( field.getTextContent() );
			else if (fieldname.equals("g_n")) // all senses
				load_sense_info(field);
			else if (fieldname.equals("g_de")) //declined forms
				load_decl_info(field);
			else if (fieldname.equals("g_fraz")) //phraseological forms
				load_phrase_info(field);
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

	/* do the one-time initialization of lookup data */
	private void initStatics() {
		blacklist = new HashSet<String>();
		BufferedReader ieeja;
		try {
			ieeja = new BufferedReader(
					new InputStreamReader(new FileInputStream("/Users/pet/Dropbox/Resursi/Tezaurs/SV errors.txt"), "UTF-8"));
			String rinda;
			while ((rinda = ieeja.readLine()) != null) {
				if (rinda.contains("<s>") || rinda.contains("</s>") || rinda.isEmpty()) continue;
				blacklist.add(rinda.trim());
			}		
			ieeja.close();
		} catch (Exception e) {} //TODO - any IO issues ignored
		
		gram_flags = new HashMap<String,String>();
		
		// katram šim pārītim pārbaudīs 'gram' lauku un ja atradīs, tad ieliks Flags sarakstā / izvāks no gram.
		//TODO - saskaitīt šos flagus, varbūt daļu apvienot (medicīnas, bioloģijas apakšjomas)
		gram_flags.put("anat.", "Anatomija");
		gram_flags.put("arheol.", "Arheoloģija");
		gram_flags.put("arhit.", "Arhitektūra");
		gram_flags.put("astr.", "Astronomija");
		gram_flags.put("av.", "Aviācija");
		gram_flags.put("biol.", "Bioloģija");
		gram_flags.put("biškop.", "Biškopība");
		gram_flags.put("bot.", "Botānika");
		gram_flags.put("būvn.", "Būvniecība");
		gram_flags.put("ekon.", "Ekonomika");
		gram_flags.put("el.", "Elektrotehnika");
		gram_flags.put("etn.", "Etnogrāfija");
		gram_flags.put("farm.", "Farmakoloģija");
		gram_flags.put("filoz.", "Filozofija");	
		gram_flags.put("fin.", "Finansu termins");
		gram_flags.put("fiz.", "Fizika");
		gram_flags.put("fiziol.", "Fizioloģija");
		gram_flags.put("fizk.", "Fiziskā kultūra un sports");
		gram_flags.put("folkl.", "Folklora");
		gram_flags.put("ģeogr.", "Ģeogrāfija");
		gram_flags.put("ģeol.", "Ģeoloģija");
		gram_flags.put("ģeom.", "Ģeometrija");
		gram_flags.put("hidrotehn.", "Hidrotehnika");
		gram_flags.put("inf.", "Informātika");
		gram_flags.put("jur.", "Jurisprudence");
		gram_flags.put("jūrn.", "Jūrniecība");
		gram_flags.put("kap.", "Attiecas uz kapitālistisko iekārtu, kapitālistisko sabiedrību");
		gram_flags.put("kul.", "Kulinārija");
		gram_flags.put("ķīm.", "Ķīmija");
		gram_flags.put("lauks.", "Lauksaimniecība");
		gram_flags.put("literat.", "Literatūrzinātne");
		gram_flags.put("loģ.", "Loģika");
		gram_flags.put("mat.", "Matemātika");
		gram_flags.put("māt.", "Matemātika"); //FIXME - typo pirmavotā
		gram_flags.put("med.", "Medicīna");
		gram_flags.put("medn.", "Medniecība");
		gram_flags.put("meteorol.", "Meteoroloģija");
		gram_flags.put("mežs.", "Mežsaimniecība");
		gram_flags.put("mil.", "Militārās zinātnes");
		gram_flags.put("min.", "Mineraloģija");
		gram_flags.put("mit.", "Mitoloģija");
		gram_flags.put("mūz.", "Mūzika");
		gram_flags.put("pol.", "Politika");
		gram_flags.put("poligr.", "Poligrāfija");
		gram_flags.put("psih.", "Psiholoģija");
		gram_flags.put("ornit.", "Ornitoloģija");
		gram_flags.put("rel.", "Reliģija");
		gram_flags.put("tehn.", "Tehnika");
		gram_flags.put("tekst.", "Tekstilrūpniecība");
		gram_flags.put("val.", "Valodniecība");
		gram_flags.put("vet.", "Veterinārija");
		gram_flags.put("zool.", "Zooloģija");
		
		gram_flags.put("apv.", "Apvidvārds");
		gram_flags.put("novec.", "Novecojis"); //TODO - Novecojis, vēsturisks un neaktuāls apvienot??		
		gram_flags.put("vēst.", "Vēsturisks");
		gram_flags.put("neakt.", "Neaktuāls");
		gram_flags.put("poēt.", "Poētiska stilistiskā nokrāsa");
		gram_flags.put("niev.", "Nievīga ekspresīvā nokrāsa");
		gram_flags.put("iron.", "Ironiska ekspresīvā nokrāsa");
		gram_flags.put("hum.", "Humoristiska ekspresīvā nokrāsa");
		gram_flags.put("vienk.", "Vienkāršrunas stilistiskā nokrāsa");
		gram_flags.put("nevēl.", "Nevēlams"); // TODO - nevēlamos, neliterāros un žargonus apvienot??
		gram_flags.put("nelit.", "Neliterārs");
		gram_flags.put("žarg.", "Žargonvārds");
		gram_flags.put("sar.", "Sarunvaloda");
		
		//TODO - šos drīzāk kā atsevišķu komentāru lauku(s)
		gram_flags.put("parasti vsk.", "Parasti vienskaitlī");
		gram_flags.put("parasti vsk..", "Parasti vienskaitlī"); //FIXME - typo pirmavotā
		gram_flags.put("parasti vsk .", "Parasti vienskaitlī"); //FIXME - typo pirmavotā
		gram_flags.put("-parasti vsk.", "Parasti vienskaitlī"); //FIXME - typo pirmavotā
		gram_flags.put("parasti vsk", "Parasti vienskaitlī");
		gram_flags.put("par. vsk.", "Parasti vienskaitlī");
		gram_flags.put("tikai vsk.", "Tikai vienskaitlī");
		gram_flags.put("vsk.", "Vienskaitlī");
		gram_flags.put("parasti dsk.", "Parasti daudzskaitlī");		
		gram_flags.put("tikai dsk.", "Tikai daudzskaitlī");
		gram_flags.put("pareti.", "Pareti");
		gram_flags.put("pareti", "Pareti");
		gram_flags.put("reti.", "Reti");
		gram_flags.put("reti", "Reti");
		gram_flags.put("retāk", "Retāk");
		
		truegram_patterns = new LinkedList<Pattern>();
		truegram_patterns.add(Pattern.compile("^(.*)(vokatīvs [^ ,;:]+)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(bieži lok\\.: [^ ,;:]+)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(parasti lok\\.: [^ ,;:]+)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(parasti vsk\\. lok\\.: [^ ,;:]+)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(parasti ģen\\.: [^ ,;:]+)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(pamata skait(\\.|ļa vārds) lietv(\\.|ārda) nozīmē\\.?)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(\\(?parasti folkl\\.(\\)\\.)?)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(parasti saistītā valodā\\.)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(apst\\. nozīmē)(.*)$"));
		truegram_patterns.add(Pattern.compile("^(.*)(\\(vācu \"krava\"\\))(.*)$"));
	}

	// Phraseological uses of the word. Not sure what to do with them...
	private void load_phrase_info(Node field) {
		// TODO Auto-generated method stub
		
	}

	// Declination forms - in Lexicon sense, they are separate lexemes, alternate wordforms but with a link to the same dictionary entry. 
	private void load_decl_info(Node field) {
		// TODO Auto-generated method stub
		
	}

	//reads the information about the (multiple) word senses for that entry. NB! they may have their own 'gram.' entries, not sure how best to reconcile
	private void load_sense_info(Node all_senses) {
		NodeList sense_nodes = all_senses.getChildNodes(); 
		for (int i = 0; i < sense_nodes.getLength(); i++) {
			Node sense = sense_nodes.item(i);
			if (sense.getNodeName().equals("n")) // We're ignoring the number of the senses - it's in "nr" field, we assume (not tested) that it matches the order in file
				senses.add(new WordSense(sense));
			else System.err.printf("G_n entry field %s not processed, expected only 'n'.\n", sense.getNodeName());
		}
	}

	// Loads thesaurus field 'v' which contains info about the word form(s) 
	private void load_v_info(Node v_field) {
		NodeList fields = v_field.getChildNodes(); 
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

	// setters that check if the information isn't already filled, to detect possible overwritten data
	private void setSource(String textContent) {
		if (source != null) System.err.printf("Duplicate info for field 'source' : '%s' and '%s'", source, textContent);
		source = textContent;
	}
	private void setGram(String textContent) {
		if (gram != null) System.err.printf("Duplicate info for field 'gram' : '%s' and '%s'", gram, textContent);
		gram = textContent;
		original_gram = textContent;
		
		if (gram.endsWith(";,")) //TODO - typo pirmavotā
			gram = gram.substring(0,gram.length()-2);
		
		if (gram.endsWith(";") || gram.endsWith(",")) //TODO - typo pirmavotā
			gram = gram.substring(0,gram.length()-1);
		
		gram = gram.replace("lietv. -a", "lietv., -a"); //TODO - typo pirmavotā
		gram = gram.replace(";novec", "; novec"); //TODO - typo pirmavotā
		gram = gram.replace("novec. (", "novec.; ("); //TODO - typo pirmavotā
		gram = gram.replace("parasti vsk. med.", "parasti vsk., med."); //TODO - typo pirmavotā
		
		if (gram_flags == null) initStatics();
				
		// katram gram_flags pārītim pārbaudīs 'gram' lauku un ja atradīs, tad ieliks Flags sarakstā / izvāks no gram.
		for (Entry<String, String> entry : gram_flags.entrySet()) {
			if (gram_contains(entry.getKey())) {			
				flags.add(entry.getValue());
				remove_gram(entry.getKey());
			}			
		}		
		
		if (truegram_patterns == null) initStatics();
		for (Pattern p : truegram_patterns) {
			Matcher m = p.matcher(gram); 
			if (m.matches()) {
				String extract = m.replaceAll("$2");
				if (true_gram != null) {
					//System.err.printf("Duplicate info for field 'Truegram' : '%s' and '%s'\n", true_gram, extract);
					true_gram = true_gram+"; "+extract;
				} else true_gram = extract;
				remove_gram(extract);
			}					
		}
		
		//FIXME - laižam vēlreiz, jo dažas dīvainas kombinācijas (koli iekavas utml) tikai tad iztīra...
		for (Entry<String, String> entry : gram_flags.entrySet()) {
			if (gram_contains(entry.getKey())) {			
				flags.add(entry.getValue());
				remove_gram(entry.getKey());
			}			
		}
	}
	
	private void setLemma(String textContent) {
		if (lemma != null) System.err.printf("Duplicate info for field 'lemma' : '%s' and '%s'", lemma, textContent);
		lemma = textContent;
	}

	
	// A single sense of a lexical entry. May contain examples or grammatic info that is specific to that single sense, not the whole entry.
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
					load_sense_info(field); 
				else System.err.printf("Word sense entry field %s not processed\n", fieldname);
			}
		}
		
		public String toJSON() {
			StringBuilder s = new StringBuilder();
			s.append('{');
			s.append(String.format("\"Definitions\":\"%s\"", JSONObject.escape(definition.trim())));
			if (gram != null) s.append(String.format(",\"Gram\":\"%s\"", JSONObject.escape(gram)));
			if (examples.length() > 0) s.append(String.format(",\"Examples\":\"%s\"", JSONObject.escape(examples.trim())));
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
		
		// setters that check if the information isn't already filled, to detect possible overwritten data
		private void setGram(String textContent) {
			if (gram != null) System.err.printf("Duplicate info for word sense field 'gram' : '%s' and '%s'", gram, textContent);
			gram = textContent;
		}
		
		//reads the information about the (possibly multiple) subsenses for that sense
		private void load_sense_info(Node all_senses) {
			NodeList sense_nodes = all_senses.getChildNodes(); 
			for (int i = 0; i < sense_nodes.getLength(); i++) {
				Node sense = sense_nodes.item(i);
				if (sense.getNodeName().equals("n")) // We're ignoring the number of the senses - it's in "nr" field, we assume (not tested) that it matches the order in file
					subsenses.add(new WordSense(sense));
				else System.err.printf("Subsense entry field %s not processed, expected only 'n'.\n", sense.getNodeName());
			}
		}
	}

}
