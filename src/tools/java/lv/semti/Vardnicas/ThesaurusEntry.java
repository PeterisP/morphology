package lv.semti.Vardnicas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Paradigm;

import org.json.simple.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Structured representation of entry header.
 */
public class ThesaurusEntry
{
	/**
	 * i field.
	 */
	public String homId;

	/**
	 * avots field.
	 */
	public Sources sources;

	/**
	 * Lemma and all-entry related grammar information.
	 */
	public Header head;

	/**
	 * g_n (nozīmju grupa) field.
	 */
	public LinkedList<Sense> senses;
	
	/**
	 * g_fraz (frazeoloģismu grupa) field.
	 */
	public LinkedList<Phrase> phrases;
	
	/**
	 * g_de (atvasinājumu grupa) field.
	 */
	public LinkedList<Header> derivs;
	
	/**
	 * Lemmas identifying entries currently ignored. See also inBlacklist().
	 */
	private static HashSet<String> blacklist = initBlacklist();

	public ThesaurusEntry()
	{
		head = null;
		sources = null;
		senses = null;
		phrases = null;
		homId = null;
	}
	
	// Reads data of a single thesaurus entry from the XML format
	public ThesaurusEntry(Node sNode)
	{
		NodeList fields = sNode.getChildNodes();
		LinkedList<Node> postponed = new LinkedList<Node>();
		for (int i = 0; i < fields.getLength(); i++)
		{
			Node field = fields.item(i);
			String fieldname = field.getNodeName();
			if (fieldname.equals("v")) // word info
			{
				if (head != null)
					System.err.printf("Entry \"%s\" contains more than one \'v\'\n", head.lemma.text);
				head = new Header (field);
			}
			else if (!fieldname.equals("#text")) // Text nodes here are ignored.
				postponed.add(field);
		}
		for (Node field : postponed)
		{
			String fieldname = field.getNodeName();
			if (fieldname.equals("avots")) // source
				sources = new Sources (field);
			else if (fieldname.equals("g_n")) // all senses
				senses = Utils.loadSenses(field, head.lemma.text);
			else if (fieldname.equals("g_fraz")) //phraseological forms
				phrases = Utils.loadPhrases(field, head.lemma.text, "fraz");
			else if (fieldname.equals("g_de")) //derived forms
				loadDerivs(field);
			else
				System.err.printf("Entry - s - field %s not processed\n", fieldname);
		}
		
		homId = ((org.w3c.dom.Element)sNode).getAttribute("i");
		
		//if (inBlacklist()) return;
		
		if (head == null)
			System.err.printf("Thesaurus entry without a lemma/header :(\n");	
	}
	
	/**
	 * Process g_de field.
	 * Derived forms - in Lexicon sense, they are separate lexemes, alternate
	 * wordforms but with a link to the same dictionary entry. 
	 */
	private void loadDerivs(Node allDerivs)
	{
		if (derivs == null) derivs = new LinkedList<Header>();
		NodeList derivNodes = allDerivs.getChildNodes(); 
		for (int i = 0; i < derivNodes.getLength(); i++)
		{
			Node deriv = derivNodes.item(i);
			if (deriv.getNodeName().equals("de"))
			{
				NodeList derivSubNodes = deriv.getChildNodes(); 
				for (int j = 0; j < derivSubNodes.getLength(); j++)
				{
					Node derivSubNode = derivSubNodes.item(j);
					if (derivSubNode.getNodeName().equals("v"))
						derivs.add(new Header(derivSubNode));
					else if (!derivSubNode.getNodeName().equals("#text")) // Text nodes here are ignored.
						System.err.printf(
							"g_de/de entry field %s not processed, expected only 'v'.\n",
							derivSubNode.getNodeName());
				}
			}
			else if (!deriv.getNodeName().equals("#text")) // Text nodes here are ignored.
				System.err.printf(
					"g_de entry field %s not processed, expected only 'de'.\n",
					deriv.getNodeName());
		}		
	}	

	public boolean inBlacklist()
	{
		if (sources == null || !sources.s.contains("LLVV")) return true; // FIXME - temporary restriction to focus on LLVV first
		return blacklist.contains(head.lemma.text);
	}
	
	/**
	 * Constructing a list of lemmas to ignore - basically meant to ease
	 * development and testing.
	 */
	private static HashSet<String> initBlacklist()
	{
		HashSet<String> blist = new HashSet<String>();
		BufferedReader ieeja;
		try {
			// Blacklist file format - one word (lemma) per line.
			ieeja = new BufferedReader(
					new InputStreamReader(
					new FileInputStream("blacklist.txt"), "UTF-8"));
			String rinda;
			while ((rinda = ieeja.readLine()) != null)
			{
				//if (rinda.contains("<s>") || rinda.contains("</s>") || rinda.isEmpty())
				//	continue;
				blist.add(rinda.trim());
			}		
			ieeja.close();
		} catch (Exception e)
		{
			System.err.println("Blacklist was not loaded.");
		} //TODO - any IO issues ignored
		return blist;
	}
	
	/**
	 * Not sure if this is the best way to treat paradigms.
	 * Currently to trigger true, paradigm must be set for all derivatives and
	 * either for header or at least one sense.
	 */
	public boolean hasParadigm()
	{
		boolean res = head.hasParadigm();
		//if (head.hasParadigm()) return true;
		if (senses != null) for (Sense s : senses)
		{
			if (s != null && s.hasParadigm()) res = true; //return true;
		}
		//for (Phrase e : phrases)
		//{
		//	if (e.hasParadigm()) return true;
		//}
		
		if (derivs != null) for (Header d : derivs)
		{
			if (!d.hasParadigm()) res = false;
		}
		return res;
	}
		
	public boolean hasUnparsedGram()
	{
		if (head != null && head.hasUnparsedGram()) return true;
		if (senses != null) for (Sense s : senses)
		{
			if (s.hasUnparsedGram()) return true;
		}
		if (phrases != null) for (Phrase e : phrases)
		{
			if (e.hasUnparsedGram()) return true;
		}
		if (derivs != null) for (Header h : derivs)
		{
			if (h.hasUnparsedGram()) return true;
		}
		return false;
	}
	
	/**
	 * Build a JSON representation, designed to load in Tezaurs2 webapp well.
	 * @return JSON representation
	 */
	public String toJSON()
	{
		StringBuilder s = new StringBuilder();
		s.append('{');
		s.append(head.toJSON());
		/*if (paradigm != 0) {
			s.append(String.format(",\"Paradigm\":%d", paradigm));
			if (analyzer != null) {
				// generate a list of inflected wordforms and format them as JSON array
				ArrayList<Wordform> inflections = analyzer.generateInflections(lemma.l, paradigm);
				s.append(String.format(",\"Inflections\":%s", formatInflections(inflections) )); 
			}
		}//*/
		
		if (homId != null && !homId.equals(""))
		{
			s.append(", \"ID\":\"");
			s.append(JSONObject.escape(homId.toString()));
			s.append("\"");
		}
		
		s.append(", \"Senses\":");
		s.append(Utils.objectsToJSON(senses));
		
		if (phrases != null)
		{
			s.append(", \"Phrases\":");
			s.append(Utils.objectsToJSON(phrases));
		}
		
		if (derivs != null)
		{
			s.append(", \"Derivatives\":");
			s.append(Utils.objectsToJSON(derivs));
		}
		
		if (sources != null && !sources.isEmpty())
		{
			s.append(",");
			s.append(sources.toJSON());
		}		
		s.append('}');
		return s.toString();
	}
	
	/**
	 * v (vārds) field.
	 */
	public static class Header implements HasToJSON
	{
		/**
		 * vf (vārdforma) field.
		 */
		public Lemma lemma;
		/**
		 * gram (gamatika) field, optional here.
		 */
		public Gram gram;
		
		
		public Header ()
		{
			lemma = null;
			gram = null;
		}
		
		public Header (Node vNode)
		{
			NodeList fields = vNode.getChildNodes();
			LinkedList<Node> postponed = new LinkedList<Node>();
			for (int i = 0; i < fields.getLength(); i++)
			{
				Node field = fields.item(i);
				String fieldname = field.getNodeName();
				if (fieldname.equals("vf")) // lemma
				{
					if (lemma != null)
						System.err.printf("vf with lemma \"%s\" contains more than one \'vf\'\n", lemma.text);
					lemma = new Lemma(field);
				}
				else if (!fieldname.equals("#text")) // Text nodes here are ignored.
					postponed.add(field);
			}
			if (lemma == null)
				System.err.printf("Thesaurus v-entry without a lemma :(\n");
			
			for (Node field : postponed)
			{
				String fieldname = field.getNodeName();
				if (fieldname.equals("gram")) // grammar
					gram = new Gram (field, lemma.text);
				else System.err.printf(
						"v entry field %s not processed\n", fieldname);				
			}				
		}
		
		public boolean hasParadigm()
		{
			if (gram == null) return false;
			return gram.hasParadigm();
		}
		
		public boolean hasUnparsedGram()
		{
			if (gram == null) return false;
			return gram.hasUnparsedGram();
		}
		
		public String toJSON()
		{
			StringBuilder res = new StringBuilder();
			
			res.append("\"Header\":{");
			res.append(lemma.toJSON());

			if (gram != null)
			{
				res.append(", ");
				res.append(gram.toJSON());
			}
			
			res.append("}");
			return res.toString();
		}
		
	}

	/**
	 * vf (vārdforma) field.
	 */
	public static class Lemma implements HasToJSON
	{
		public String text;
		/**
		 * ru (runa) field, optional here.
		 */
		public String pronunciation;
		
		public Lemma ()
		{
			text = null;
			pronunciation = null;
		}
		public Lemma (Node vfNode)
		{
			text = vfNode.getTextContent();
			pronunciation = ((org.w3c.dom.Element)vfNode).getAttribute("ru");
		}
		
		/**
		 *  Set lemma and check if the information isn't already filled, to
		 *  detect possible overwritten data.
		 */
		public void set(String lemmaText) {
			if (text != null)
				System.err.printf(
					"Duplicate info for field 'lemma' : '%s' and '%s'", text,
					lemmaText);
			text = lemmaText;
		}
		
		public String toJSON()
		{
			StringBuilder res = new StringBuilder();
			res.append(String.format("\"Lemma\":\"%s\"", JSONObject.escape(text)));
			if (pronunciation != null && !pronunciation.equals(""))
			{
				res.append(", \"Pronunciation\":\"");
				res.append(JSONObject.escape(pronunciation.toString()));
				res.append("\"");
			}
			return res.toString();
		}
	}
	
	/**
	 * g (gramatika) field.
	 */
	public static class Gram  implements HasToJSON
	{
		public String orig;
		public HashSet<String> flags;
		public LinkedList<LinkedList<String>> leftovers;
		public HashSet<Integer> paradigm;
		
		/**
		 * Known abbreviations and their de-abbreviations.
		 */
		public static FlagMap knownAbbr = generateKnownAbbr();
		private static FlagMap generateKnownAbbr()
		{
			FlagMap res = new FlagMap();
			
			// TODO Sort out this mess.
			// Source: LLVV, data.
			
			res.put("adj.", "Īpašības vārds");
			res.put("apst.", "Apstākļa vārds");
			res.put("divd.", "Divdabis");
			res.put("Divd.", "Divdabis");
			res.put("divd.", "Divdabis");
			res.put("interj.", "Izsauksmes vārds");
			res.put("īp. v.", "Īpašības vārds");
			res.put("izsauk.", "Izsauksmes vārds");
			res.put("jaut.", "Jautājamais vietniekvārds");
			res.put("lietv.", "Lietvārds");
			res.put("noliedz.", "Noliedzamais vietniekvārds");
			res.put("norād.", "Norādāmais vietniekvārds");
			res.put("noteic.", "Noteicamais vietniekvārds");
			res.put("part.", "Partikula");
			res.put("pieder.", "Piederības vietniekvārds");
			res.put("pried.", "Priedēklis"); // Specific processing needed.
			res.put("priev.", "Prievārds");
			res.put("skait.", "Skaitļa vārds");
			res.put("vietn.", "Vietniekvārds");
			res.put("vietniekv.", "Vietniekvārds");
			res.put("vispārin.", "Vispārināmais vietniekvārds");
			res.put("saīs.", "Saīsinājums");
			res.put("simb.", "Saīsinājums");	// ?
			res.put("salikteņu pirmā daļa.", "Salikteņu daļa");
			res.put("salikteņu pirmā daļa", "Salikteņu daļa");
			res.put("salikteņu daļa.", "Salikteņu daļa");
			res.put("salikteņu daļa", "Salikteņu daļa");
			
			res.put("priev. ar ģen.", "Prievārds");
			res.put("priev. ar ģen.", "Lieto ar ģenetīvu");
			res.put("ar ģen.", "Prievārds"); // It seems that without additional comments this is used for prepositions only
			res.put("ar ģen.", "Lieto ar ģenetīvu");			
			res.put("priev. ar dat.", "Prievārds");
			res.put("priev. ar dat.", "Lieto ar datīvu");
			
			res.put("persv.", "Personvārds");
			res.put("vietv.", "Vietvārds");

			res.put("akuz.", "Akuzatīvs");
			res.put("dat.", "Datīvs");
			res.put("ģen.", "Ģenitīvs");
			res.put("instr.", "Instrumentālis");
			res.put("lok.", "Lokatīvs");
			res.put("nom.", "Nominatīvs");


			res.put("divsk.", "Divskaitlis"); // Do we really still have one of these?!
			res.put("dsk.", "Daudzskaitlis");
			res.put("vsk.", "Vienskaitlis");
			
			res.put("nāk.", "Nākotne");
			res.put("pag.", "Pagātne");
			res.put("tag.", "Tagadne");
			
			res.put("nenot.", "Nenoteiktā galotne");
			res.put("not.", "Noteiktā galotne");
			
			res.put("s.", "Sieviešu dzimte");
			res.put("v.", "Vīriešu dzimte");
			res.put("kopdz.", "Kopdzimtes vārds");
			
			res.put("intrans.", "Intransitīvs"); //???
			res.put("trans.", "Transitīvs"); //???

			res.put("konj.", "Konjugācija");
			res.put("pers.", "Persona");

			res.put("atgr.", "Atgriezensisks (vietniekvārds?)");
			res.put("dem.", "Deminutīvs");
			res.put("Dem.", "Deminutīvs");
			res.put("imperf.", "Imperfektīva forma"); //???
			res.put("nelok.", "Nelokāms vārds");
			res.put("Nol.", "Noliegums"); // Check with other sources!
			res.put("refl.", "Refleksīvs darbības vārds");
			res.put("Refl.", "Refleksīvs darbības vārds");

			res.put("aeron.", "Aeronautika");	// ?
			res.put("anat.", "Anatomija");
			res.put("arheol.", "Arheoloģija");
			res.put("arhit.", "Arhitektūra");
			res.put("astr.", "Astronomija");
			res.put("av.", "Aviācija");
			res.put("biol.", "Bioloģija");
			res.put("biškop.", "Biškopība");
			res.put("bot.", "Botānika");
			res.put("būvn.", "Būvniecība");
			res.put("ek.", "Ekonomika");
			res.put("ekol.", "Ekoloģija");		// ?
			res.put("ekon.", "Ekonomika");
			res.put("el.", "Elektrotehnika");
			res.put("etn.", "Etnogrāfija");
			res.put("farm.", "Farmakoloģija");
			res.put("filoz.", "Filozofija");	
			res.put("fin.", "Finansu termins");
			res.put("fiz.", "Fizika");
			res.put("fiziol.", "Fizioloģija");
			res.put("fizk.", "Fiziskā kultūra un sports");
			res.put("folkl.", "Folklora");
			res.put("ģenēt.", "Ģenētika");
			res.put("ģeod.", "Ģeodēzija");
			res.put("ģeogr.", "Ģeogrāfija");
			res.put("ģeol.", "Ģeoloģija");
			res.put("ģeom.", "Ģeometrija");
			res.put("grāmatv.", "Grāmatvedība");
			res.put("hidr.", "Hidroloģija");
			res.put("hidrotehn.", "Hidrotehnika");
			res.put("inf.", "Informātika");
			res.put("jur.", "Jurisprudence");
			res.put("jūrn.", "Jūrniecība");
			res.put("kap.", "Attiecas uz kapitālistisko iekārtu, kapitālistisko sabiedrību");
			res.put("kardioloģijā", "Kardioloģija");
			res.put("kart.", "Kartogrāfija");		// ?
			res.put("kibern.", "Kibernētika");
			res.put("kokapstr.", "Kokapstrāde");	// ?
			res.put("kul.", "Kulinārija");
			res.put("ķīm.", "Ķīmija");
			res.put("lauks.", "Lauksaimniecība");
			res.put("lauks. tehn.", "Lauksaimniecības tehnika");	// ?
			res.put("literat.", "Literatūrzinātne");
			res.put("loģ.", "Loģika");
			res.put("lopk.", "Lopkopība");
			res.put("mat.", "Matemātika");
			res.put("matem.", "Matemātika");
			res.put("med.", "Medicīna");
			res.put("medn.", "Medniecība");
			res.put("met.", "Meteoroloģija");		// ?
			res.put("metal.", "Metalurģija");
			res.put("metāl.", "Metālapstrāde");		// ?
			res.put("meteorol.", "Meteoroloģija");
			res.put("mež.", "Mežsaimniecība");		// ?	
			res.put("mežs.", "Mežsaimniecība");
			res.put("mil.", "Militārās zinātnes");
			res.put("min.", "Mineraloģija");
			res.put("mit.", "Mitoloģija");
			res.put("mūz.", "Mūzika");
			res.put("oftalmoloģijā", "Oftalmoloģija");
			res.put("ornit.", "Ornitoloģija");
			res.put("pol.", "Politika");
			res.put("poligr.", "Poligrāfija");
			res.put("psih.", "Psiholoģija");
			res.put("rel.", "Reliģija");
			res.put("social.", "Socioloģija");	// ?
			res.put("sociol.", "Socioloģija");
			res.put("tehn.", "Tehnika");
			res.put("tehnol.", "Tehnoloģija");
			res.put("telek.", "Telekomunikācijas");
			res.put("tekst.", "Tekstilrūpniecība");
			res.put("tekstilr.", "Tekstilrūpniecība");	// ?
			res.put("val.", "Valodniecība");
			res.put("vet.", "Veterinārija");
			res.put("zool.", "Zooloģija");
			
			res.put("arābu", "Arābu");
			res.put("arābu", "Vārds svešvalodā");			
			res.put("fr.", "Franču");
			res.put("fr.", "Vārds svešvalodā");
			res.put("grieķu", "Grieķu");
			res.put("grieķu", "Vārds svešvalodā");
			res.put("gr.", "Grieķu");
			res.put("gr.", "Vārds svešvalodā");
			res.put("it.", "Itāliešu"); //Muz
			res.put("it.", "Vārds svešvalodā");
			res.put("lat.", "Latīņu");
			res.put("lat.", "Vārds svešvalodā");
			res.put("sengr.", "Sengrieķu");
			res.put("sengr.", "Vārds svešvalodā");
			
			res.put("apv.", "Apvidvārds");
			res.put("novec.", "Novecojis"); //TODO - Novecojis, vēsturisks un neaktuāls apvienot??		
			res.put("vēst.", "Vēsturisks");
			res.put("neakt.", "Neaktuāls");
			res.put("poēt.", "Poētiska stilistiskā nokrāsa");
			res.put("niev.", "Nievīga ekspresīvā nokrāsa");
			res.put("iron.", "Ironiska ekspresīvā nokrāsa");
			res.put("hum.", "Humoristiska ekspresīvā nokrāsa");
			res.put("vienk.", "Vienkāršrunas stilistiskā nokrāsa");
			res.put("pārn.", "Pārnestā nozīmē");
			res.put("nevēl.", "Nevēlams"); // TODO - nevēlamos, neliterāros un žargonus apvienot??
			res.put("nelit.", "Neliterārs");
			res.put("žarg.", "Žargonvārds");
			res.put("sar.", "Sarunvaloda");
			res.put("vulg.", "Vulgārisms");	// ?
			
			//TODO - šos drīzāk kā atsevišķu komentāru lauku(s)
			res.put("arī vsk.", "Arī vienskaitlī");		// Ļaunums.
			res.put("parasti vsk.", "Parasti vienskaitlī");
			res.put("parasti vsk", "Parasti vienskaitlī");
			res.put("par. vsk.", "Parasti vienskaitlī");
			res.put("tikai vsk.", "Tikai vienskaitlī");
			res.put("parasti dsk.", "Parasti daudzskaitlī");		
			res.put("tikai dsk.", "Tikai daudzskaitlī");
			res.put("parasti 3. pers.", "Parasti 3. personā");
			res.put("lietv. nozīmē.", "Lietvārda nozīmē");
			res.put("pareti.", "Pareti");
			res.put("pareti", "Pareti");
			res.put("reti.", "Reti");
			res.put("reti", "Reti");
			res.put("retāk", "Retāk");
			
			return res;
		}
		
		/**
		 * Patterns for identifying (true) grammatical information.
		 */
		public static LinkedList<Pattern> knownPatterns = generateKnownPatterns();
		private static LinkedList<Pattern> generateKnownPatterns()
		{
			LinkedList<Pattern> res = new LinkedList<Pattern>();
			res.add(Pattern.compile("^(.*)(vokatīvs [^ ,;:]+)(.*)$"));
			res.add(Pattern.compile("^(.*)(bieži lok\\.: [^ ,;:]+)(.*)$"));
			res.add(Pattern.compile("^(.*)(parasti lok\\.: [^ ,;:]+)(.*)$"));
			res.add(Pattern.compile("^(.*)(parasti vsk\\. lok\\.: [^ ,;:]+)(.*)$"));
			res.add(Pattern.compile("^(.*)(parasti ģen\\.: [^ ,;:]+)(.*)$"));
			res.add(Pattern.compile("^(.*)(pamata skait(\\.|ļa vārds) lietv(\\.|ārda) nozīmē\\.?)(.*)$"));
			res.add(Pattern.compile("^(.*)(\\(?parasti folkl\\.(\\)\\.)?)(.*)$"));
			res.add(Pattern.compile("^(.*)(parasti saistītā valodā\\.)(.*)$"));
			res.add(Pattern.compile("^(.*)(apst\\. nozīmē)(.*)$"));
			res.add(Pattern.compile("^(.*)(\\(vācu \"krava\"\\))(.*)$"));
			return res;
		}
		
		public Gram ()
		{
			orig = null;
			flags = null;
			leftovers = null;
			paradigm = null;
		}
		/**
		 * @param lemma is used for grammar parsing.
		 */
		public Gram (Node gramNode, String lemma)
		{
			orig = gramNode.getTextContent();
			leftovers = null;
			flags = new HashSet<String> ();
			paradigm = new HashSet<Integer>();
			parseGram(lemma);
		}
		/**
		 * @param lemma is used for grammar parsing.
		 */
		public void set (String gramText, String lemma)
		{
			orig = gramText;
			leftovers = null;
			flags = new HashSet<String> ();
			paradigm = new HashSet<Integer>();
			parseGram(lemma);
		}
		
		public boolean hasParadigm()
		{
			return !paradigm.isEmpty();
		}
		
		/**
		 * Only works correctly, if cleanupLeftovers is used, when needed.
		 */
		public boolean hasUnparsedGram()
		{
			//cleanupLeftovers();		// What is better - unexpected side effects or not working, when used incorrectly?
			return !leftovers.isEmpty();
		}
		
		/**
		 * @param lemma is used for grammar parsing.
		 */
		private void parseGram(String lemma)
		{
			String correctedGram = correctOCRErrors(orig);
			
			// First process ending patterns, usually located in the beginning
			// of the grammar string.
			correctedGram = processWithPatterns(correctedGram, lemma);
			
			String[] subGrams = correctedGram.split("\\s*;\\s*");
			leftovers = new LinkedList<LinkedList<String>> ();
			
			// Process each semicolon-separated substring.
			for (String subGram : subGrams)	
			{
				String[] gramElems = subGram.split("\\s*,\\s*");
				LinkedList<String> toDo = new LinkedList<String> ();
				
				// Process each comma-separated substring.
				for (String gramElem : gramElems) 
				{
					gramElem = gramElem.trim();
					if (knownAbbr.containsKey(gramElem))
						flags.addAll(knownAbbr.getAll(gramElem));
					else if (!gramElem.equals(""))
						toDo.add(gramElem);	
				}
				
				// TODO: magical patterns for processing endings.
				
				leftovers.add(toDo);
			}
			
			// Try to deduce paradigm from flags.
			paradigmFromFlags(lemma);
			
			cleanupLeftovers();
		}
		
		/**
		 * @param lemma is used for grammar parsing.
		 */
		private String processWithPatterns(String gramText, String lemma)
		{
			gramText = gramText.trim();
			//if (!gramText.contains("-")) return gramText;
			//String beggining = gramText.substring(0, gramText.indexOf('-'));
			//gramText = gramText.substring(gramText.indexOf('-'));
			int newBegin = 0;
			
			// Paradigm 1: Lietvārds 1. deklinācija -s
			// Paradigm 2: Lietvārds 1. deklinācija -š
			// Paradigm 3: Lietvārds 2. deklinācija -is (ja nav miju)
			if (gramText.startsWith("lietv. -a, v.")) // aerobs
			{
				newBegin = "lietv. -a, v.".length();
				if (lemma.matches(".*[ģjķr]is")) paradigm.add(3);
				else
				{
					if (lemma.matches(".*[aeiouāēīōū]s") || lemma.matches(".*[^sš]"))
						System.err.printf("Problem matching \"%s\" with paradigms 1, 2, 3\n", lemma);
					
					if (lemma.endsWith("š")) paradigm.add(2);
					else paradigm.add(1);
				}
				flags.add("Vīriešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-a, v.")) // abats, akustiķis
			{
				newBegin = "-a, v.".length();
				if (lemma.matches(".*[ģjķr]is")) paradigm.add(3);
				else
				{
					if (lemma.matches(".*[aeiouāēīōū]s") || lemma.matches(".*[^sš]"))
						System.err.printf("Problem matching \"%s\" with paradigms 1, 2, 3\n", lemma);
					
					if (lemma.endsWith("š")) paradigm.add(2);
					else paradigm.add(1);
				}
				
				flags.add("Vīriešu dzimte");
				flags.add("Lietvārds");
			}
			
			// Paradigm 2: Lietvārds 1. deklinācija -š
			// Paradigm 3: Lietvārds 2. deklinācija -is
			// Paradigm 5: Lietvārds 2. deklinācija -suns
			else if (gramText.startsWith("-ņa, v.")) // abesīnis
			{
				newBegin = "-ņa, v.".length();
				if (!lemma.endsWith("nis") && !lemma.endsWith("ņš") && !lemma.endsWith("suns"))
					System.err.printf("Problem matching \"%s\" with paradigms 2, 3, 5\n", lemma);
				
				if (lemma.endsWith("suns")) paradigm.add(5);
				else if (lemma.endsWith("ņš")) paradigm.add(2);
				else paradigm.add(3);
				flags.add("Vīriešu dzimte");
				flags.add("Lietvārds");
			}
			
			// Paradigm 3: Lietvārds 2. deklinācija -is
			// Paradigm 5: Lietvārds 2. deklinācija -suns
			else if (gramText.startsWith("-ļa, v.")) // acumirklis, durkls
			{
				newBegin = "-ļa, v.".length();
				if (!lemma.endsWith("lis") && !lemma.endsWith("ls"))
					System.err.printf("Problem matching \"%s\" with paradigm 3, 5\n", lemma);
				if (lemma.endsWith("ls")) paradigm.add(5);
				else paradigm.add(3);
				flags.add("Vīriešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-ša, v.")) // abrkasis, lemess
			{
				newBegin = "-ša, v.".length();
				if (!lemma.endsWith("sis") && !lemma.endsWith("tis")
						&& !lemma.endsWith("šis") && !lemma.endsWith("ss"))
					System.err.printf("Problem matching \"%s\" with paradigm 3\n", lemma);
				if (lemma.endsWith("ss")) paradigm.add(5);
				else paradigm.add(3);
				flags.add("Vīriešu dzimte");
				flags.add("Lietvārds");
			}


			
			// Paradigm 7: Lietvārds 4. deklinācija -a siev. dz.
			// Paradigm 11: Lietvārds 6. deklinācija -s siev. dz.
			else if (gramText.startsWith("-as, s.")) //aberācija, milns
			{
				newBegin = "-as, s.".length();
				if (!lemma.endsWith("a") && !lemma.matches(".*[^aeiouāēīōū]s"))
					System.err.printf("Problem matching \"%s\" with paradigm 7, 11\n", lemma);
				if (lemma.endsWith("s")) paradigm.add(11);
				else paradigm.add(7);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			
			// Paradigm 9: Lietvārds 5. deklinācija -a siev. dz.
			else if (gramText.startsWith("-es, dsk. ģen. -ču, s.")) //ābece
			{
				newBegin = "-es, dsk. ģen. -ču, s.".length();
				if (!lemma.endsWith("ce") && !lemma.endsWith("če"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ļu, s.")) //ābele
			{
				newBegin = "-es, dsk. ģen. -ļu, s.".length();
				if (!lemma.endsWith("le"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -šu, s.")) //abate
			{
				if (!lemma.endsWith("te") && !lemma.endsWith("se") && !lemma.endsWith("še"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				newBegin = "-es, dsk. ģen. -šu, s.".length();
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ņu, s.")) //ābolaine
			{
				newBegin = "-es, dsk. ģen. -ņu, s.".length();
				if (!lemma.endsWith("ne"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -žu, s.")) //ābolmaize
			{
				newBegin = "-es, dsk. ģen. -žu, s.".length();
				if (!lemma.endsWith("ze") && !lemma.endsWith("de"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ru, s.")) //administratore
			{
				newBegin = "-es, dsk. ģen. -ru, s.".length();
				if (!lemma.endsWith("re"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -stu, s.")) //abolicioniste
			{
				newBegin = "-es, dsk. ģen. -stu, s.".length();
				if (!lemma.endsWith("ste"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.matches("-es, s\\., dsk\\. ģen\\. -bju([;,.].*)?")) //acetilsalicilskābe
			{
				newBegin = "-es, s., dsk. ģen. -bju".length();
				if (!lemma.endsWith("be"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.matches("-es, dsk\\. ģen\\. -ru([;,.].*)?")) //ādere
			{
				newBegin = "-es, dsk. ģen. -ru".length();
				if (!lemma.endsWith("re"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			else if (gramText.startsWith("-es, s.")) //aizture
			{
				newBegin = "-es, s.".length();
				if (!lemma.endsWith("e"))
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
				paradigm.add(9);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");
			}
			
			// Paradigm 13: Īpašības vārdi ar -s
			// Paradigm 14: Īpašības vārdi ar -š
			else if (gramText.matches("īp\\. v\\. -ais; s\\. -a, -ā([;,.].*)?")) //aerobs
			{
				newBegin = "īp. v. -ais; s. -a, -ā".length();
				if (lemma.matches(".*[^sš]") || lemma.matches(".*[aeiouāēīōū][sš]"))
					System.err.printf("Problem matching \"%s\" with paradigms 13, 14\n", lemma);
				if (lemma.endsWith("š")) paradigm.add(14);
				else paradigm.add(13);
				flags.add("Īpašības vārds");
			}
			else if (gramText.matches("-ais; s\\. -a, -ā([;,.].*)?")) //abējāds, acains
			{
				newBegin = "-ais; s. -a, -ā".length();
				if (lemma.matches(".*[^sš]"))
					System.err.printf("Problem matching \"%s\" with paradigms 13, 14\n", lemma);
				if (lemma.endsWith("š")) paradigm.add(14);
				else paradigm.add(13);
				flags.add("Īpašības vārds");
			}
			
			// Paradigm 15: Darbības vārdi 1. konjugācija tiešie
			else if (gramText.matches("parasti 3\\. pers\\., -šalc, pag\\. -šalca([;,.].*)?")) //aizšalkt
			{
				newBegin = "parasti 3. pers., -šalc, pag. -šalca".length();
				if (!lemma.endsWith("šalkt"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Parasti 3. personā");
				flags.add("Locīt kā \"šalkt\"");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -tūkst, pag\\. -tūka([;,.].*)?")) //aiztūkt
			{
				newBegin = "parasti 3. pers., -tūkst, pag. -tūka".length();
				if (!lemma.endsWith("tūkt"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Parasti 3. personā");
				flags.add("Locīt kā \"tūkt\"");
			}
			else if (gramText.matches("-eju, -ej, -iet, pag\\. -gāju([.,;].*)?")) //apiet
			{
				newBegin = "-eju, -ej, -iet, pag. -gāju".length();
				if (!lemma.endsWith("iet"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"iet\"");
			}
			else if (gramText.matches("-tupstu, -tupsti, -tupst, pag\\. -tupu([;,.].*)?")) //aiztupt
			{
				newBegin = "-tupstu, -tupsti, -tupst, pag. -tupu".length();
				if (!lemma.endsWith("tupt"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"tupt\"");
				//TODO check paralel forms.
			}	
			else if (gramText.matches("-tveru, -tver, -tver, pag\\. -tvēru([;,.].*)?")) //aiztvert
			{
				newBegin = "-tveru, -tver, -tver, pag. -tvēru".length();
				if (!lemma.endsWith("tvert"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"tvert\"");
			}
			else if (gramText.matches("-griežu, -griez, -griež, pag\\. -griezu([;,.].*)?")) //apgriezt
			{
				newBegin = "-griežu, -griez, -griež, pag. -griezu".length();
				if (!lemma.endsWith("griezt"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"griezt\"");
			}
			else if (gramText.matches("-ģiedu, -ģied, -ģied, pag\\. -gidu([;,.].*)?")) //apģist
			{
				newBegin = "-ģiedu, -ģied, -ģied, pag. -gidu".length();
				if (!lemma.endsWith("ģist"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"ģist\"");
			}
			else if (gramText.matches("-klāju, -klāj, -klāj, pag\\. -klāju([;,.].*)?")) //apklāt
			{
				newBegin = "-klāju, -klāj, -klāj, pag. -klāju".length();
				if (!lemma.endsWith("klāt"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"klāt\"");
			}
			else if (gramText.matches("-kauju, -kauj, -kauj, pag\\. -kāvu([;,.].*)?")) //apkaut
			{
				newBegin = "-kauju, -kauj, -kauj, pag. -kāvu".length();
				if (!lemma.endsWith("kaut"))
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
				paradigm.add(15);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"kaut\"");
			}			

			// Paradigm 16: Darbības vārdi 2. konjugācija tiešie
			else if (gramText.matches("parasti 3\\. pers\\., -o , pag\\. -oja([;,.].*)?")) //aizšalkot
			{
				newBegin = "parasti 3. pers., -o , pag. -oja".length();
				if (!lemma.endsWith("ot"))
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
				paradigm.add(16);
				flags.add("Darbības vārds");
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -ē, pag\\. -ēja([;,.].*)?")) //adsorbēt
			{
				newBegin = "parasti 3. pers., -ē, pag. -ēja".length();
				if (!lemma.endsWith("ēt"))
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
				paradigm.add(16);
				flags.add("Darbības vārds");
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("-oju, -o, -o, -ojam, -ojat, pag\\. -oju; -ojām, -ojāt; pav\\. -o, -ojiet([,;.].*)?")) //acot
			{
				newBegin = "-oju, -o, -o, -ojam, -ojat, pag. -oju; -ojām, -ojāt; pav. -o, -ojiet".length();
				if (!lemma.endsWith("ot"))
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
				paradigm.add(16);
				flags.add("Darbības vārds");
			}
			else if (gramText.matches("-ēju, -ē, -ē, -ējam, -ējat, pag\\. -ēju, -ējām, -ējāt; pav\\. -ē, -ējiet([,;.].*)?")) //adverbializēt
			{
				newBegin = "-ēju, -ē, -ē, -ējam, -ējat, pag. -ēju, -ējām, -ējāt; pav. -ē, -ējiet".length();
				if (!lemma.endsWith("ēt"))
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
				paradigm.add(16);
				flags.add("Darbības vārds");
			}
			else if (gramText.matches("-ēju, -ē, -ē, pag\\. -ēju([;,.].*)?")) //absolutizēt
			{
				newBegin = "-ēju, -ē, -ē, pag. -ēju".length();
				if (!lemma.endsWith("ēt"))
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
				paradigm.add(16);
				flags.add("Darbības vārds");
			}
			else if (gramText.matches("-oju, -o, -o, pag\\. -oju([;,.].*)?")) //aiztuntuļot
			{
				newBegin = "-oju, -o, -o, pag. -oju".length();
				if (!lemma.endsWith("ot"))
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
				paradigm.add(16);
				flags.add("Darbības vārds");
			}			

			// Paradigm 17: Darbības vārdi 3. konjugācija tiešie
			else if (gramText.matches("-turu, -turi, -tur, pag\\. -turēju([;,.].*)?")) //aizturēt
			{
				newBegin = "-turu, -turi, -tur, pag. -turēju".length();
				if (!lemma.endsWith("turēt"))
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
				paradigm.add(17);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"turēt\"");
			}
			else if (gramText.matches("-u, -i, -a, pag\\. -īju([;,.].*)?")) //aizsūtīt
			{
				newBegin = "-u, -i, -a, pag. -īju".length();
				if (!lemma.endsWith("īt"))
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
				paradigm.add(17);
				flags.add("Darbības vārds");
			}
			else if (gramText.matches("-inu, -ini, -ina, pag\\. -ināju([;,.].*)?")) //aizsvilināt
			{
				newBegin = "-inu, -ini, -ina, pag. -ināju".length();
				if (!lemma.endsWith("ināt"))
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
				paradigm.add(17);
				flags.add("Darbības vārds");
			}
			
			// Paradigm 18: Darbības vārdi 1. konjugācija atgriezeniski
			else if (gramText.matches("parasti 3\\. pers\\., -šalcas, pag\\. -šalcās([;,.].*)?")) //aizšalkties
			{
				newBegin = "parasti 3. pers., -šalcas, pag. -šalcās".length();
				if (!lemma.endsWith("šalkties"))
					System.err.printf("Problem matching \"%s\" with paradigm 18\n", lemma);
				paradigm.add(18);
				flags.add("Darbības vārds");
				flags.add("Parasti 3. personā");
				flags.add("Locīt kā \"šalkties\"");
			}
			else if (gramText.matches("-ejos, -ejos, -ietas, pag\\. -gājos([;,.].*)?")) //apieties
			{
				newBegin = "-ejos, -ejos, -ietas, pag. -gājos".length();
				if (!lemma.endsWith("ieties"))
					System.err.printf("Problem matching \"%s\" with paradigm 18\n", lemma);
				paradigm.add(18);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"ieties\"");
			}
			else if (gramText.matches("-tupstos, -tupsties, -tupstas, pag\\. -tupos([;,.].*)?")) //aiztupties
			{
				newBegin = "-tupstos, -tupsties, -tupstas, pag. -tupos".length();
				if (!lemma.endsWith("tupties"))
					System.err.printf("Problem matching \"%s\" with paradigm 18\n", lemma);
				paradigm.add(18);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"tupties\"");
				//TODO check paralel forms.
			}
			else if (gramText.matches("-ģiedos, -ģiedies, -ģiedas, pag\\. -gidos([;,.].*)?")) //apģisties
			{
				newBegin = "-ģiedos, -ģiedies, -ģiedas, pag. -gidos".length();
				if (!lemma.endsWith("ģisties"))
					System.err.printf("Problem matching \"%s\" with paradigm 18\n", lemma);
				paradigm.add(18);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"ģisties\"");
			}
			else if (gramText.matches("-klājos, -klājies, -klājas, pag\\. -klājos([;,.].*)?")) //apklāties
			{
				newBegin = "-klājos, -klājies, -klājas, pag. -klājos".length();
				if (!lemma.endsWith("klāties"))
					System.err.printf("Problem matching \"%s\" with paradigm 18\n", lemma);
				paradigm.add(18);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"klāties\"");
			}
			else if (gramText.matches("-karos, -karies, -karas, pag\\. -kāros([.,;].*)?")) //apkārties
			{
				newBegin = "-karos, -karies, -karas, pag. -kāros".length();
				if (!lemma.endsWith("kārties"))
					System.err.printf("Problem matching \"%s\" with paradigm 18\n", lemma);
				paradigm.add(18);
				flags.add("Darbības vārds");
				flags.add("Locīt kā \"kārties\"");
			}
			
			// Paradigm 19: Darbības vārdi 2. konjugācija atgriezeniski
			else if (gramText.matches("parasti 3\\. pers\\., -ējas, pag\\. -ējās([;,.].*)?")) //absorbēties
			{
				newBegin = "parasti 3. pers., -ējas, pag. -ējās".length();
				if (!lemma.endsWith("ēties"))
					System.err.printf("Problem matching \"%s\" with paradigm 19\n", lemma);
				paradigm.add(19);
				flags.add("Darbības vārds");
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -ojas, pag\\. -ojās([;,.].*)?")) //daudzkāršoties
			{
				newBegin = "parasti 3. pers., -ojas, pag. -ojās".length();
				if (!lemma.endsWith("oties"))
					System.err.printf("Problem matching \"%s\" with paradigm 19\n", lemma);
				paradigm.add(19);
				flags.add("Darbības vārds");
				flags.add("Parasti 3. personā");
			}			
			else if (gramText.matches("-ējos, -ējies, -ējas, -ējamies, -ējaties, pag\\. -ējos, -ējāmies, -ējāties; pav\\. -ējies, -ējieties([.,;].*)?")) //adverbiēties
			{
				newBegin = "-ējos, -ējies, -ējas, -ējamies, -ējaties, pag. -ējos, -ējāmies, -ējāties; pav. -ējies, -ējieties".length();
				if (!lemma.endsWith("ēties"))
					System.err.printf("Problem matching \"%s\" with paradigm 19\n", lemma);
				paradigm.add(19);
				flags.add("Darbības vārds");				
			}
			else if (gramText.matches("-ojos, -ojies, -ojas, pag\\. -ojos([.,;].*)?")) //aiztuntuļoties, apgrēkoties
			{
				newBegin = "-ojos, -ojies, -ojas, pag. -ojos".length();
				if (!lemma.endsWith("oties"))
					System.err.printf("Problem matching \"%s\" with paradigm 19\n", lemma);
				paradigm.add(19);
				flags.add("Darbības vārds");				
			}
			else if (gramText.matches("-ējos, -ējies, -ējas, pag\\. -ējos([;,.].*)?")) //abstrahēties
			{
				newBegin = "-ējos, -ējies, -ējas, pag. -ējos".length();
				if (!lemma.endsWith("ēties"))
					System.err.printf("Problem matching \"%s\" with paradigm 19\n", lemma);
				paradigm.add(19);
				flags.add("Darbības vārds");				
			}
			
			// Paradigm 20: Darbības vārdi 3. konjugācija atgriezeniski
			else if (gramText.matches("-os, -ies, -ās, pag\\. -ījos([;,.].*)?")) //apklausīties
			{
				newBegin = "-os, -ies, -ās, pag. -ījos".length();
				if (!lemma.endsWith("īties"))
					System.err.printf("Problem matching \"%s\" with paradigm 20\n", lemma);
				paradigm.add(20);
				flags.add("Darbības vārds");				
			}
			else if (gramText.matches("-inos, -inies, -inās, pag\\. -inājos([;,.].*)?")) //apklaušināties
			{
				newBegin = "-inos, -inies, -inās, pag. -inājos".length();
				if (!lemma.endsWith("ināties"))
					System.err.printf("Problem matching \"%s\" with paradigm 20\n", lemma);
				paradigm.add(20);
				flags.add("Darbības vārds");				
			}
			else if (gramText.matches("-os, -ies, -as, pag\\. -ējos([;,.].*)?")) //apkaunēties
			{
				newBegin = "-os, -ies, -as, pag. -ējos".length();
				if (!lemma.endsWith("ēties"))
					System.err.printf("Problem matching \"%s\" with paradigm 20\n", lemma);
				paradigm.add(20);
				flags.add("Darbības vārds");				
			}

			// Paradigm 25: Vietniekvārdi
			else if (gramText.matches("ģen\\. -kā, dat\\. -kam, akuz\\., instr\\. -ko([.,;].*)?")) //apkārties
			{
				newBegin = "ģen. -kā, dat. -kam, akuz., instr. -ko".length();
				if (!lemma.endsWith("kas"))
					System.err.printf("Problem matching \"%s\" with paradigm 25\n", lemma);
				paradigm.add(25);
				flags.add("Vietniekvārds");
				flags.add("Locīt kā \"kas\"");
			}		
			
			// Paradigm 30: jaundzimušais, pēdējais
			else if (gramText.startsWith("-šā, v. -šās, s.")) //iereibušais
			{
				newBegin = "-šā, v. -šās, s.".length();
				if (!lemma.endsWith("ušais"))
					System.err.printf("Problem matching \"%s\" with paradigm 30\n", lemma);
				paradigm.add(30);
				flags.add("Īpašības vārds");			
				flags.add("Lietvārds");			
			}
			else if (gramText.startsWith("-ā, v.")) //pirmdzimtais
			{
				newBegin = "-ā, v.".length();
				if (!lemma.endsWith("ais"))
					System.err.printf("Problem matching \"%s\" with paradigm 30\n", lemma);
				paradigm.add(30);
				flags.add("Vīriešu dzimte");
				flags.add("Lietvārds");	
				flags.add("Īpašības vārds");	
			}
			else if (gramText.startsWith("-ās, s.")) //pirmdzimtā
			{
				newBegin = "-ās, s.".length();
				if (!lemma.endsWith("ā") && !lemma.endsWith("šanās"))
					System.err.printf("Problem matching \"%s\" with paradigms 30, -šanās\n", lemma);
				if (lemma.endsWith("šanās"))
				{
					paradigm.add(0);
					flags.add("Atgriezeniskais lietvārds");	
				}
				else 
				{
					paradigm.add(30);
					flags.add("Īpašības vārds");	
				}
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");	
			}
			
			// Paradigm Unknown: Atgriezeniskie lietvārdi -šanās
			else if (gramText.startsWith("ģen. -ās, akuz. -os, instr. -os, dsk. -ās, ģen. -os, akuz. -ās, s.")) //aizbildināšanās
			{
				newBegin = "ģen. -ās, akuz. -os, instr. -os, dsk. -ās, ģen. -os, akuz. -ās, s.".length();
				if (!lemma.endsWith("šanās"))
					System.err.printf("Problem matching \"%s\" with paradigm -šanās\n", lemma);
				paradigm.add(0);
				flags.add("Sieviešu dzimte");
				flags.add("Lietvārds");	
				flags.add("Atgriezeniskais lietvārds");	
			}			
			
			String res = gramText.substring(newBegin);
			if (res.matches("[.,;].*")) res = res.substring(1);
			return res;
		}
		
		/**
		 * @param lemma is used for paradigm detection in cases where endings
		 * matter.
		 */
		private void paradigmFromFlags(String lemma)
		{
			if (flags.contains("Īpašības vārds") )
			{
				if (!lemma.endsWith("ais") && (lemma.matches(".*[^sšā]")
						|| lemma.matches(".*[aeiouāēīōū][sš]")))
					System.err.printf("Problem matching \"%s\" with paradigms 13, 14, 30 by flags\n", lemma);
				if (lemma.endsWith("ais") || lemma.endsWith("ā")) paradigm.add(30);
				if (lemma.endsWith("s")) paradigm.add(13);
				if (lemma.endsWith("š")) paradigm.add(14);
				
			}
			
			if (flags.contains("Apstākļa vārds")) paradigm.add(21);
			if (flags.contains("Partikula")) paradigm.add(28);
			if (flags.contains("Prievārds")) paradigm.add(26);
			
			if (flags.contains("Izsauksmes vārds")) paradigm.add(29); // Hardcoded
			if (flags.contains("Saīsinājums")) paradigm.add(29); // Hardcoded
			if (flags.contains("Vārds svešvalodā")) paradigm.add(29);
			
			if (flags.contains("Vietniekvārds")) paradigm.add(25);
			if (flags.contains("Jautājamais vietniekvārds")) paradigm.add(25);
			if (flags.contains("Noliedzamais vietniekvārds")) paradigm.add(25);
			if (flags.contains("Norādāmais vietniekvārds")) paradigm.add(25);
			if (flags.contains("Noteicamais vietniekvārds")) paradigm.add(25);
			if (flags.contains("Piederības vietniekvārds")) paradigm.add(25);
			if (flags.contains("Vispārināmais vietniekvārds")) paradigm.add(25);

			if (flags.contains("Priedēklis")) paradigm.add(0); //Prefixes are not words.
			if (flags.contains("Salikteņu daļa")) paradigm.add(0); //Prefixes are not words.
		}
		
		/**
		 * This should be called after something is removed from leftovers.
		 */
		public void cleanupLeftovers()
		{
			for (int i = leftovers.size() - 1; i >= 0; i--)
			{
				if (leftovers.get(i).isEmpty()) leftovers.remove(i);
			}
		}
		
		/**
		 * Hopefully, this method will be empty for final data ;)
		 */
		private String correctOCRErrors(String gramText)
		{
			//if (gramText.endsWith(";,"))
			//	gramText = gramText.substring(0,gramText.length()-2);
			
			//if (gramText.endsWith(";") || gramText.endsWith(","))
			//	gramText = gramText.substring(0,gramText.length()-1);
			
			//gramText = gramText.replace("lietv. -a", "lietv., -a");
			gramText = gramText.replace(";novec", "; novec");
			//gramText = gramText.replace("novec. (", "novec.; (");
			//gramText = gramText.replace("parasti vsk. med.", "parasti vsk., med.");
			
			gramText = gramText.replaceAll("^māt\\.", "mat\\.");
			gramText = gramText.replace(" māt.", " mat.");
			gramText = gramText.replace("vsk..", "vsk.");
			gramText = gramText.replace("vsk .", "vsk.");
			gramText = gramText.replaceAll("^gen\\.", "ģen\\.");
			gramText = gramText.replace(" gen.", " ģen.");
			gramText = gramText.replaceAll("^trans;", "trans\\.;");
			gramText = gramText.replace(" trans;", " trans.;");
			
			gramText = gramText.replace("-ēju, -ē, -ē, pag. -eju;", "-ēju, -ē, -ē, pag. -ēju;"); //abonēt
			gramText = gramText.replace("parasti 3. pers., -ē, pag. -eja;", "parasti 3. pers., -ē, pag. -ēja;"); //absorbēt
			gramText = gramText.replace("-ais; s. -a: -ā;", "-ais; s. -a, -ā;"); //apgrēcīgs
			//gramText = gramText.replace("-šā, v.", "-ša, v."); //abesīnietis, ābolītis, iereibušais
			gramText = gramText.replace("parasti 3. pers., -ējas, pag. -ejās;", "parasti 3. pers., -ējas, pag. -ējās;"); //adaptēties
			
			//Inconsequences in data
			//gramText = gramText.replace("-ā.;", "-ā;");

			return gramText;
			
		}
	
		public String toJSON()
		{
			return toJSON(true);
		}
		
		// In case of speed problems StringBuilder can be returned.
		public String toJSON (boolean printOrig)
		{
			StringBuilder res = new StringBuilder();
			
			res.append("\"Gram\":{");
			boolean hasPrev = false;
			
			if (paradigm != null && !paradigm.isEmpty())
			{
				if (hasPrev) res.append(", ");
				res.append("\"Paradigm\":");
				res.append(Utils.simplesToJSON(paradigm));
				hasPrev = true;
			}
			
			if (flags != null && !flags.isEmpty())
			{
				if (hasPrev) res.append(", ");
				res.append("\"Flags\":");
				res.append(Utils.simplesToJSON(flags));
				hasPrev = true;
			}
			
			if (leftovers != null && leftovers.size() > 0)
			{
				if (hasPrev) res.append(", ");
				res.append("\"Leftovers\":[");
				
				Iterator<LinkedList<String>> it = leftovers.iterator();
				while (it.hasNext())
				{
					LinkedList<String> next = it.next();
					if (!next.isEmpty())
					{
						res.append(Utils.simplesToJSON(next));
						if (it.hasNext()) res.append(", ");
					}
				}
				res.append("]");
				hasPrev = true;
			}
			
			if (printOrig && orig != null && orig.length() > 0)
			{
				if (hasPrev) res.append(", ");
				res.append("\"Original\":\"");
				res.append(JSONObject.escape(orig));
				res.append("\"");
				hasPrev = true;
			}
			
			res.append("}");
			return res.toString();
		}
	}
	
	/**
	 * avots field.
	 */
	public static class Sources implements HasToJSON
	{
		public String orig;
		public LinkedList<String> s;
		
		public Sources ()
		{
			orig = null; s = null;
		}
		
		public Sources (Node avotsNode)
		{
			orig = avotsNode.getTextContent();
			s = parseSources(orig);
			if (s.size() < 1 && orig.length() > 0)
				System.err.printf(
					"Field 'sources' '%s' can't be parsed!\n", orig);
		}
		
		public boolean isEmpty()
		{
			return s == null;
		}
		
		/**
		 *  Parse sources from string and check if the information isn't already
		 *  filled, to detect possible overwritten data.
		 */
		public void set(String sourcesText)
		{
			if (orig != null || s != null)
			{
				System.err.printf(
					"Duplicate info for field 'sources' : '%s' and '%s'!\n", orig, sourcesText);
			}
			orig = sourcesText;
			s = parseSources(sourcesText);
			if (s.size() < 1 && orig.length() > 0)
				System.err.printf(
					"Field 'sources' '%s' can't be parsed!\n", orig);
		}
		
		// In case of speed problems StringBuilder can be returned.
		public String toJSON()
		{
			StringBuilder res = new StringBuilder();
			if (s != null)
			{
				res.append("\"Sources\":");
				res.append(Utils.simplesToJSON(s));
			}
			return res.toString();
		}
		
		private static LinkedList<String> parseSources (String sourcesText)
		{
			if (sourcesText.startsWith("["))
				sourcesText = sourcesText.substring(1);
			if (sourcesText.endsWith("]"))
				sourcesText = sourcesText.substring(0, sourcesText.length() - 1);
			
			LinkedList<String> res = new LinkedList<String>();
			res.addAll(Arrays.asList(sourcesText.split(",\\s*")));
			return res;
		}
	}
	
	/**
	 * n (nozīme / nozīmes nianse) field.
	 */
	public static class Sense implements HasToJSON
	{
		
		/**
		 * gram field  is optional here.
		 */
		public Gram grammar;
		
		/**
		 * d (definīcija) field.
		 */
		public Gloss gloss;
		
		/**
		 * id field.
		 */
		public String ordNumber;
		
		/**
		 * g_piem (piemēru grupa) field, optional here.
		 */
		
		public LinkedList<Phrase> examples = null;
		/**
		 * g_an (apakšnozīmju grupa) field, optional here.
		 */
		public LinkedList<Sense> subsenses = null;
				
		public Sense ()
		{
			grammar = null;
			gloss = null;
			examples = null;
			subsenses = null;
			ordNumber = null;
		}
		
		/**
		 * @param lemma is used for grammar parsing.
		 */
		public Sense (Node nNode, String lemma)
		{
			NodeList fields = nNode.getChildNodes(); 
			for (int i = 0; i < fields.getLength(); i++)
			{
				Node field = fields.item(i);
				String fieldname = field.getNodeName();
				if (fieldname.equals("gram"))
					grammar = new Gram (field, lemma);
				else if (fieldname.equals("d"))
				{
					NodeList glossFields = field.getChildNodes();
					for (int j = 0; j < glossFields.getLength(); j++)
					{
						Node glossField = glossFields.item(j);
						String glossFieldname = glossField.getNodeName();
						if (glossFieldname.equals("t"))
						{
							if (gloss != null)
								System.err.println("d entry contains more than one \'t\'");
							gloss = new Gloss (glossField);
						}
						else if (!glossFieldname.equals("#text")) // Text nodes here are ignored.
							System.err.printf("d entry field %s not processed\n", glossFieldname);
					}
				}
				else if (fieldname.equals("g_piem"))
					examples = Utils.loadPhrases(field, lemma, "piem");
				else if (fieldname.equals("g_an"))
					subsenses = Utils.loadSenses(field, lemma);
				else if (!fieldname.equals("#text")) // Text nodes here are ignored.
					System.err.printf("n entry field %s not processed\n", fieldname);
			}
			ordNumber = ((org.w3c.dom.Element)nNode).getAttribute("nr");
		}
		
		/**
		 * Not sure if this is the best way to treat paradigms.
		 * Currently only grammar paradigm is considered.
		 */
		public boolean hasParadigm()
		{
			if (grammar == null) return false;
			return grammar.hasParadigm();
			//if (grammar.hasParadigm()) return true;
			//for (Phrase e : examples)
			//{
			//	if (e.hasParadigm()) return true;
			//}
			//for (Sense s : subsenses)
			//{
			//	if (s.hasParadigm()) return true;
			//}
			//return false;
		}
		
		public boolean hasUnparsedGram()
		{
			if (grammar != null && grammar.hasUnparsedGram()) return true;
			if (examples != null) for (Phrase e : examples)
			{
				if (e.hasUnparsedGram()) return true;
			}
			if (subsenses != null) for (Sense s : subsenses)
			{
				if (s.hasUnparsedGram()) return true;
			}			
			return false;
		}
		
		public String toJSON()
		{
			StringBuilder res = new StringBuilder();
			
			res.append("\"Sense\":{");
			boolean hasPrev = false;
			
			if (ordNumber != null && !ordNumber.equals(""))
			{
				res.append("\"SenseID\":\"");
				res.append(JSONObject.escape(ordNumber.toString()));
				res.append("\"");
				hasPrev = true;
			}
			
			if (grammar != null)
			{
				if (hasPrev) res.append(", ");
				res.append(grammar.toJSON());
				hasPrev = true;
			}
			
			if (gloss != null)
			{
				if (hasPrev) res.append(", ");
				res.append(gloss.toJSON());
				hasPrev = true;
			}
			
			if (examples != null && !examples.isEmpty())
			{
				if (hasPrev) res.append(", ");
				res.append("\"Examples\":");
				res.append(Utils.objectsToJSON(examples));
				hasPrev = true;
			}
			
			if (subsenses != null && !subsenses.isEmpty())
			{
				if (hasPrev) res.append(", ");
				res.append("\"Senses\":");
				res.append(Utils.objectsToJSON(subsenses));
				hasPrev = true;
			}
			
			res.append("}");
			return res.toString();
		}
	}
	
	/**
	 * d (definīcija) field.
	 */
	public static class Gloss implements HasToJSON
	{
		/**
		 * t (teksts) field.
		 */
		public String text = null;
		
		public Gloss (Node dNode)
		{
			text = dNode.getTextContent();
		}
		
		public String toJSON()
		{
			return String.format("\"Gloss\":\"%s\"", JSONObject.escape(text));			
		}
	}
	
	/**
	 * piem (piemērs) and fraz (frazeoloģisms) fields.
	 */
	public static class Phrase implements HasToJSON
	{
		/**
		 * t (teksts) field.
		 */
		public String text;		

		/**
		 * gram field  is optional here.
		 */
		public Gram grammar;

		/**
		 * n field is optional here.
		 */
		public LinkedList<Sense> subsenses;
		
		public Phrase()
		{
			text = null;
			grammar = null;
			subsenses = null;
		}

		public Phrase (Node piemNode, String lemma)
		{
			text = null;
			grammar = null;
			subsenses = null;
			NodeList fields = piemNode.getChildNodes(); 
			for (int i = 0; i < fields.getLength(); i++) {
				Node field = fields.item(i);
				String fieldname = field.getNodeName();
				if (fieldname.equals("t"))
					text = field.getTextContent();
				else if (fieldname.equals("gram"))
					grammar = new Gram (field, lemma);
				else if (fieldname.equals("n"))
				{
					if (subsenses == null) subsenses = new LinkedList<Sense>();
					subsenses.add(new Sense (field, lemma));
				}
				else if (!fieldname.equals("#text")) // Text nodes here are ignored.
					System.err.printf("piem entry field %s not processed\n", fieldname);
			}			
		}
		
		/**
		 * Not sure if this is the best way to treat paradigms.
		 * Currently to trigger true, paradigm must be set for either header or
		 * at least one sense.
		 */
		public boolean hasParadigm()
		{
			if (grammar != null && grammar.hasParadigm()) return true;
			if (subsenses != null) for (Sense s : subsenses)
			{
				if (s.hasParadigm()) return true;
			}
			return false;
		}
		
		public boolean hasUnparsedGram()
		{
			if (grammar != null && grammar.hasUnparsedGram()) return true;
			if (subsenses != null) for (Sense s : subsenses)
			{
				if (s.hasUnparsedGram()) return true;
			}			
			return false;
		}
		
		public String toJSON()
		{
			StringBuilder res = new StringBuilder();
			
			res.append("\"Phrase\":{");
			boolean hasPrev = false;
			
			if (text != null)
			{
				if (hasPrev) res.append(", ");
				res.append("\"Text\":\"");
				res.append(JSONObject.escape(text));
				res.append("\"");
				hasPrev = true;
			}	
			
			if (grammar != null)
			{
				if (hasPrev) res.append(", ");
				res.append(grammar.toJSON());
				hasPrev = true;
			}
			
			if (subsenses != null)
			{
				if (hasPrev) res.append(", ");
				res.append("\"Senses\":");
				res.append(Utils.objectsToJSON(subsenses));
				hasPrev = true;
			}
			
			res.append("}");			
			return res.toString();
		}
	}

	
	
	// Here the magic magic must happen.
/*	private void setParadigm() {
		// 1: Lietvārds 1. deklinācija -s
		if (( lemma.l.endsWith("s") 
			&& gramContains("v.") && !gramContains("-ais")) //FIXME īpašībasvārdi kas nav īpaši norādīti????
			&& !lemma.l.endsWith("is") && !lemma.l.endsWith("us") && !gramContains("nenoteiktais vietn.") 
			&& !gramContains("-sāls") && !gramContains("-rudens")
			&& !lemma.l.endsWith("rudens") && !lemma.l.endsWith("debess")
			&& !lemma.l.endsWith("akmens") && !lemma.l.endsWith("asmens")
			&& !lemma.l.endsWith("ūdens") && !lemma.l.endsWith("suns")
			&& !lemma.l.endsWith("zibens") && !lemma.l.endsWith("mēness")) {
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
				System.err.printf("%s\t('%s' - gram bija %s)\n",gram,lemma.l,originalGram);
						
			if (analyzer != null) {
				Word analīze = analyzer.analyzeLemma(lemma.l);
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
	}//*/
	
	// What is this?
	// This is for test purposes.
/*	private void assertNounEnding(
		String gramDesc, String ending, String number, String nouncase)
	{
		// Assertion to verify if analyzer stemchanges match the dictionary.
		if (gramContains(gramDesc) && analyzer != null) { 
			Paradigm p = analyzer.paradigmByID(paradigm);
			//FIXME - kā tad šis strādā ar daudzskaitliniekiem?
			
			ArrayList<Wordform> inflections = analyzer.generateInflections(
				lemma.l, paradigm);
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
	}//*/
	
	
	/**
	 *  Formats a list of inflections as an JSON array.
	 */
	private static Object formatInflections(ArrayList<Wordform> inflections) {
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
	
	public static class Utils
	{
		public static <E extends HasToJSON> String objectsToJSON(Iterable<E> l)
		{
			if (l == null) return "[]";
			StringBuilder res = new StringBuilder();
			res.append("[");
			Iterator<E> i = l.iterator();
			while (i.hasNext())
			{
				res.append(i.next().toJSON());
				if (i.hasNext()) res.append(", ");
			}
			res.append("]");			
			return res.toString();
		}
		
		public static<E> String simplesToJSON(Iterable<E> l)
		{
			if (l == null) return "[]";
			StringBuilder res = new StringBuilder();
			res.append("[");
			Iterator<E> i = l.iterator();
			while (i.hasNext())
			{
				res.append("\"");
				res.append(JSONObject.escape(i.next().toString()));
				res.append("\"");
				if (i.hasNext()) res.append(", ");
			}
			res.append("]");			
			return res.toString();
		}
		
		/**
		 * Loads contents of g_n or g_an field into LinkedList.
		 * Reads the information about the (multiple) word senses for that entry.
		 * NB! they may have their own 'gram.' entries, not sure how best to
		 * reconcile.
		 * @param lemma is used for grammar parsing.
		 */
		public static LinkedList<Sense> loadSenses(Node allSenses, String lemma)
		{
			//if (senses == null) senses = new LinkedList<Sense>();
			LinkedList<Sense> res = new LinkedList<Sense>();
			NodeList senseNodes = allSenses.getChildNodes(); 
			for (int i = 0; i < senseNodes.getLength(); i++)
			{
				Node sense = senseNodes.item(i);
				
				// We're ignoring the number of the senses - it's in "nr" field, we
				//assume (not tested) that it matches the order in file
				if (sense.getNodeName().equals("n"))
					res.add(new Sense(sense, lemma));
				else if (!sense.getNodeName().equals("#text")) // Text nodes here are ignored.
					System.err.printf(
						"%s entry field %s not processed, expected only 'n'.\n",
						allSenses.getNodeName(), sense.getNodeName());
			}
			return res;
		}
		
		/**
		 * Load contents of g_fraz or g_piem field into LinkedList.
		 * @param lemma is used for grammar parsing.
		 */
		public static LinkedList<Phrase> loadPhrases(
				Node allPhrases, String lemma, String subElemName)
		{
			LinkedList<Phrase> res = new LinkedList<Phrase>();
			NodeList phraseNodes = allPhrases.getChildNodes(); 
			for (int i = 0; i < phraseNodes.getLength(); i++)
			{
				Node phrase = phraseNodes.item(i);
				if (phrase.getNodeName().equals(subElemName))
					res.add(new Phrase(phrase, lemma));
				else if (!phrase.getNodeName().equals("#text")) // Text nodes here are ignored.
					System.err.printf(
						"%s entry field %s not processed, expected only '%s'.\n",
						allPhrases.getNodeName(), phrase.getNodeName(), subElemName);
			}
			return res;
		}
	}
	
	/**
	 * Multimap for grammar flags. Incomplete interface, might need additional
	 * methods later.
	 */
	public static class FlagMap
	{
		private HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		
		public void put (String key, String value)
		{
			HashSet<String> values = new HashSet<String>();
			if (map.containsKey(key))
			{
				values = map.get(key);
			}
			values.add(value);
			map.put(key, values);
		}
		
		public HashSet<String> getAll(String key)
		{
			return map.get(key);
		}
		
		public boolean containsKey(String key)
		{
			return map.containsKey(key);
		}
		
	}
	
	public static interface HasToJSON
	{
		public String toJSON();
	}
}
