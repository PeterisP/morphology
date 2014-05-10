package lv.semti.Vardnicas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
		if ("".equals(homId)) homId = null;
		
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
		//if (sources == null || !sources.s.contains("LLVV")) return true; // FIXME - temporary restriction to focus on LLVV first
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
		
		if (homId != null)
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
		public Lemma (String lemma)
		{
			text = lemma;
			pronunciation = null;
		}		
		public Lemma (Node vfNode)
		{
			text = vfNode.getTextContent();
			pronunciation = ((org.w3c.dom.Element)vfNode).getAttribute("ru");
			if ("".equals(pronunciation)) pronunciation = null;
			if (pronunciation == null) return;
			if (pronunciation.startsWith("["))
				pronunciation = pronunciation.substring(1);
			if (pronunciation.endsWith("]"))
				pronunciation = pronunciation.substring(0, pronunciation.length() - 1);
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
		
		// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
		@Override
		public boolean equals (Object o)
		{
			if (o == null) return false;
			if (this.getClass() != o.getClass()) return false;
			if ((text == null && ((Lemma)o).text == null || text != null && text.equals(((Lemma)o).text))
					&& (pronunciation == null && ((Lemma)o).pronunciation == null
					|| pronunciation != null && pronunciation.equals(((Lemma)o).pronunciation)))
				return true;
			else return false;
		}
		
		// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
		@Override
		public int hashCode()
		{
			return 1721 *(text == null ? 1 : text.hashCode())
					+ (pronunciation == null ? 1 : pronunciation.hashCode());
		}
		
		public String toJSON()
		{
			StringBuilder res = new StringBuilder();
			res.append(String.format("{\"Lemma\":\"%s\"", JSONObject.escape(text)));
			if (pronunciation != null)
			{
				res.append(", \"Pronunciation\":\"");
				res.append(JSONObject.escape(pronunciation.toString()));
				res.append("\"");
			}
			res.append('}');
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
		 * If grammar contains additional information about lemmas, it is
		 * collected here. Mapping from paradigms to lemma-flagset tuples.
		 * Flag set contains only flags for which alternate lemma differs from
		 * general flags given in "flags" field in this grammar.
		 */
		public MappingSet<Integer, Tuple<Lemma, HashSet<String>>> altLemmas;
		
		/**
		 * Known abbreviations and their de-abbreviations.
		 */
		public static MappingSet<String, String> knownAbbr = generateKnownAbbr();
		private static MappingSet<String, String> generateKnownAbbr()
		{
			MappingSet<String, String> res = new MappingSet<String, String>();
			
			// TODO Sort out this mess.
			// Source: LLVV, data.
			
			res.put("adj.", "Īpašības vārds");
			res.put("adv.", "Apstākļa vārds");
			res.put("apst.", "Apstākļa vārds");
			res.put("divd.", "Divdabis");
			res.put("Divd.", "Divdabis");
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
			res.put("vietniekv.", "Vietniekvārds");	// ?
			res.put("vispārin.", "Vispārināmais vietniekvārds");
			res.put("saīs.", "Saīsinājums");
			res.put("simb.", "Saīsinājums");	// ?
			res.put("salikteņu pirmā daļa.", "Salikteņu daļa");
			res.put("salikteņu pirmā daļa", "Salikteņu daļa");
			res.put("salikteņa pirmā daļa.", "Salikteņu daļa");
			res.put("salikteņa pirmā daļa", "Salikteņu daļa");
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
			res.put("kopdz.", "Kopdzimte");
			
			res.put("intrans.", "Nepārejošs");
			res.put("trans.", "Pārejošs");
			// TODO vai šie vienmēr ir darbības vārdi?

			res.put("konj.", "Konjugācija");
			res.put("pers.", "Persona");

			//res.put("atgr.", "Atgriezensisks (vietniekvārds?)"); //not present
			res.put("dem.", "Deminutīvs");
			res.put("Dem.", "Deminutīvs");
			res.put("imperf.", "Imperfektīva forma"); //???
			res.put("nelok.", "Nelokāms vārds");
			res.put("Nol.", "Noliegums"); // Check with other sources!
			res.put("refl.", "Refleksīvs");
			res.put("refl.", "Darbības vārds");			
			res.put("Refl.", "Refleksīvs");
			res.put("Refl.", "Darbības vārds");			

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
			res.put("fin.", "Finanses");
			res.put("fiz.", "Fizika");
			res.put("fiziol.", "Fizioloģija");
			res.put("fizk.", "Fiziskā kultūra un sports");
			res.put("folkl.", "Folklora");
			res.put("ģenēt.", "Ģenētika");	// ?
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
			res.put("matem.", "Matemātika");	// ?
			res.put("med.", "Medicīna");
			res.put("medn.", "Medniecība");
			res.put("met.", "Meteoroloģija");		// ?
			res.put("metal.", "Metalurģija");
			res.put("metāl.", "Metālapstrāde");		// ?
			res.put("meteorol.", "Meteoroloģija");
			res.put("mež.", "Mežniecība");		// ?
			res.put("mežr.", "Mežrūpniecība");
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
			res.put("telek.", "Telekomunikācijas");	// ?
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
			res.put("vēst.", "Vēsturisks");
			res.put("novec.", "Novecojis");		
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
			res.put("subst. noz.", "Lietvārda nozīmē");
			res.put("lietv. nozīmē.", "Lietvārda nozīmē");
			res.put("ar not. gal.", "Ar noteikto galotni");
			res.put("dial. (augšzemnieku)", "Agušzemnieku dialekts");	// Unique.
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
			altLemmas = null;
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
			altLemmas = null;
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
			altLemmas = null;
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
			altLemmas = new MappingSet<Integer, Tuple<Lemma, HashSet<String>>>();
			
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
			// TODO cleanup altLemmas;
		}
		
		/**
		 * This method contains collection of ending patterns, found in data.
		 * Thus,e.g., if there was no plural-only nouns with ending -ļas, then
		 * there is no rule for processing such words (at least in most cases).
		 * @param lemma is used for grammar parsing.
		 */
		private String processWithPatterns(String gramText, String lemma)
		{
			gramText = gramText.trim();
			int newBegin = -1;
			
			// Blocks of rules.
			
			newBegin = firstConjDirVerbRules(gramText, lemma);
			if (newBegin == -1) newBegin = secondConjDirVerbRules(gramText, lemma);
			if (newBegin == -1) newBegin = thirdConjDirVerbRules(gramText, lemma);
			
			if (newBegin == -1) newBegin = firstConjRefVerbRules(gramText, lemma);
			if (newBegin == -1) newBegin = secondConjRefVerbRules(gramText, lemma);
			if (newBegin == -1) newBegin = thirdConjRefVerbRules(gramText, lemma);
			
			// Complicated rules: grammar contains lemma variation spelled out.
			if (newBegin == -1)
			{
				// Super-complicated case: pronunciation included.			
				// Paradigm 1: Lietvārds 1. deklinācija -s
				if (lemma.endsWith("di") &&
					gramText.matches("(-u, vsk\\. (\\Q"
							+ lemma.substring(0, lemma.length() - 1)
							+ "s\\E) \\[([^\\]]*?)\\] -a, v\\.)(.*)?")) // ābeļziedi: -u, vsk. ābeļzieds [a^be`ļzie^c] -a, v.
				{
					Pattern pattern = Pattern.compile("(-u, vsk\\. (\\Q"
							+ lemma.substring(0, lemma.length() - 1)
							+ "s\\E) \\[([^\\]]*?)\\] -a, v\\.)(.*)?");
					Matcher matcher = pattern.matcher(gramText);
					if (!matcher.matches()) 
						System.err.printf("Problem matching \"%s\" with \"ābeļzieds\" rule\n", lemma);
					newBegin = matcher.group(1).length();
					Lemma altLemma = new Lemma(matcher.group(2));
					altLemma.pronunciation = matcher.group(3);
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Šķirkļavārds vienskaitlī");
					altLemmas.put(1, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					
					paradigm.add(1);
					flags.add("Vīriešu dzimte");
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}

				// Paradigm 2: Lietvārds 1. deklinācija -š
				else if (lemma.endsWith("ņi") &&
					gramText.startsWith("-ņu, vsk. "
							+ lemma.substring(0, lemma.length() - 2)
							+ "ņš, -ņa, v.")) // dižtauriņi: -ņu, vsk. dižtauriņš, -ņa, v.
				{
					newBegin = ("-ņu, vsk. "+ lemma.substring(0, lemma.length() - 2) + "ņš, -ņa, v.").length();
					Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 2) + "ņš");
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Šķirkļavārds vienskaitlī");
					altLemmas.put(2, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					paradigm.add(2);
					flags.add("Vīriešu dzimte");
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				// Paradigm 3: Lietvārds 2. deklinācija -is
				else if (lemma.endsWith("ņi") &&
					gramText.startsWith("-ņu, vsk. "
							+ lemma.substring(0, lemma.length() - 2)
							+ "nis, -ņa, v.")) // aizvirtņi: -ņu, vsk. aizvirtnis, -ņa, v.
				{
					newBegin = ("-ņu, vsk. "+ lemma.substring(0, lemma.length() - 2)+"nis, -ņa, v.").length();
					Lemma altLemma = new Lemma(lemma.substring(0, lemma.length() - 2) + "nis");
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Šķirkļavārds vienskaitlī");
					altLemmas.put(3, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					paradigm.add(3);
					flags.add("Vīriešu dzimte");
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else if (lemma.endsWith("ņi") &&
					gramText.startsWith("-ņu, vsk. "
							+ lemma.substring(0, lemma.length() - 3)
							+ "lnis, -ļņa, v.")) // starpviļņi: -ņu, vsk. starpvilnis, -ļņa, v.
				{
					newBegin = ("-ņu, vsk. "+ lemma.substring(0, lemma.length() - 3)+"lnis, -ļņa, v.").length();
					Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 3) + "lnis");
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Šķirkļavārds vienskaitlī");
					altLemmas.put(3, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					paradigm.add(3);
					flags.add("Vīriešu dzimte");
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else if (lemma.endsWith("ji") &&
					gramText.startsWith("-u, vsk. " + lemma + "s, -ja, v.")) // airkāji: -u, vsk. airkājis, -ja, v.
				{
					newBegin = ("-u, vsk. " + lemma + "s, -ja, v.").length();
					Lemma altLemma = new Lemma (lemma + "s");
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Šķirkļavārds vienskaitlī");
					altLemmas.put(3, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					paradigm.add(3);
					flags.add("Vīriešu dzimte");
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}

				// Paradigm 1: Lietvārds 1. deklinācija -s		
				else if (lemma.endsWith("i") &&
					gramText.startsWith("-u, vsk. "
							+ lemma.substring(0, lemma.length() - 1)
							+ "s, -a, v.")) // aizkari: -u, vsk. aizkars, -a, v.
				{
					newBegin = ("-u, vsk. " + lemma.substring(0, lemma.length() - 1) + "s, -a, v.").length();
					Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 1) + "s");
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Šķirkļavārds vienskaitlī");
					altLemmas.put(1, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					paradigm.add(1);
					flags.add("Vīriešu dzimte");
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}			
			}
			
			// "-es, dsk. ģen. -??u, s."
			if (newBegin == -1) newBegin = esEndingPluralGenUEndingFemRules(gramText, lemma);
			
			// More rules
			if (newBegin == -1)
			{
				// Long, specific patterns.
				// Paradigm Unknown: Atgriezeniskie lietvārdi -šanās
				if (gramText.startsWith("ģen. -ās, akuz. -os, instr. -os, dsk. -ās, ģen. -os, akuz. -ās, s.")) //aizbildināšanās
				{
					newBegin = "ģen. -ās, akuz. -os, instr. -os, dsk. -ās, ģen. -os, akuz. -ās, s.".length();
					if (lemma.endsWith("šanās"))
					{
						paradigm.add(0);
						flags.add("Lietvārds");	
						flags.add("Atgriezeniskais lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm -šanās\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				// Paradigm 25: Vietniekvārdi
				else if (gramText.matches("ģen\\. -kā, dat\\. -kam, akuz\\., instr\\. -ko([.,;].*)?")) //daudzkas
				{
					newBegin = "ģen. -kā, dat. -kam, akuz., instr. -ko".length();
					if (lemma.endsWith("kas"))
					{
						paradigm.add(25);
						flags.add("Vietniekvārds");
						flags.add("Locīt kā \"kas\"");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 25\n", lemma);
						newBegin = 0;
					}
				}
				// Paradigm 7: Lietvārds 4. deklinācija -a siev. dz.
				// Paradigm 8: Lietvārds 4. deklinācija -a vīr. dz.
				else if (gramText.startsWith("ģen. -as, v. dat. -am, s. dat. -ai, kopdz."))
				{
					newBegin = "ģen. -as, v. dat. -am, s. dat. -ai, kopdz.".length();
					if (lemma.endsWith("a"))
					{
						paradigm.add(7);
						paradigm.add(8);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 7, 8\n", lemma);
						newBegin = 0;
					}
					flags.add("Kopdzimte");
				}
				
				// Paradigm 1: Lietvārds 1. deklinācija -s
				// Paradigm 2: Lietvārds 1. deklinācija -š
				else if (gramText.startsWith("lietv. -a, v.")) // aerobs 
				{
					newBegin = "lietv. -a, v.".length();
					//if (lemma.matches(".*[ģjķr]is")) paradigm.add(3);
					//else
					//{
						//if (lemma.matches(".*[aeiouāēīōū]s") || lemma.matches(".*[^sš]"))
						//	System.err.printf("Problem matching \"%s\" with paradigms 1, 2, 3\n", lemma);
						
						if (lemma.endsWith("š")) paradigm.add(2);
						else if (lemma.matches(".*[^aeiouāēīōū]s")) paradigm.add(1);
						else
						{
							System.err.printf("Problem matching \"%s\" with paradigms 1, 2, 3\n", lemma);
							newBegin = 0;
						}
					//}
					flags.add("Vīriešu dzimte");
					flags.add("Lietvārds");
				}
				else if (gramText.startsWith("vsk. -a, v.")) // acteks
				{
					newBegin = "vsk. -a, v.".length();
					
					if (lemma.endsWith("š"))
					{
						paradigm.add(2);
						flags.add("Lietvārds");
					}
					else if (lemma.matches(".*[^aeiouāēīōū]s"))
					{
						paradigm.add(1);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigms 1, 2\n", lemma);
						newBegin = 0;
					}
					flags.add("Vīriešu dzimte");
					flags.add("Vienskaitlis");
				}
				
				// Paradigm 3: Lietvārds 2. deklinācija -is
				else if (gramText.startsWith("-ņa, dsk. ģen. -ņu, v.")) // bizmanis
				{
					newBegin = "-ņa, dsk. ģen. -ņu, v.".length();
					
					if (lemma.endsWith("nis"))
					{
						paradigm.add(3);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigms 3\n", lemma);
						newBegin = 0;
					}
					flags.add("Vīriešu dzimte");
				}
				
				// Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
				else if (gramText.matches("-es, s\\., dsk\\. ģen\\. -bju([;,.].*)?")) //acetilsalicilskābe
				{
					newBegin = "-es, s., dsk. ģen. -bju".length();
					if (lemma.endsWith("be"))
					{
						paradigm.add(9);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				else if (gramText.matches("-es, dsk\\. ģen\\. -ru([;,.].*)?")) //ādere
				{
					newBegin = "-es, dsk. ģen. -ru".length();
					if (lemma.endsWith("re"))
					{
						paradigm.add(9);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				else if (gramText.startsWith("-es, s.")) //aizture
				{
					newBegin = "-es, s.".length();
					if (lemma.endsWith("e"))
					{
						paradigm.add(9);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				
				// Paradigm 11: Lietvārds 6. deklinācija -s
				else if (gramText.startsWith("-valsts, dsk. ģen. -valstu, s.")) //agrārvalsts
				{
					newBegin = "-valsts, dsk. ģen. -valstu, s.".length();
					if (lemma.endsWith("valsts"))
					{
						paradigm.add(11);
						flags.add("Lietvārds");					
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 11\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				else if (gramText.matches("-ts, -šu([;,.].*)?")) //abonentpults
				{
					newBegin = "-ts, -šu".length();
					if (lemma.endsWith("ts"))
					{
						paradigm.add(11);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 11\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				else if (gramText.matches("-vs, -vju([;,.].*)?")) //adatzivs
				{
					newBegin = "-vs, -vju".length();
					if (lemma.endsWith("vs"))
					{
						paradigm.add(11);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 11\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				
				// Paradigm 7: Lietvārds 4. deklinācija -a siev. dz.
				// Paradigm 11: Lietvārds 6. deklinācija -s siev. dz.
				else if (gramText.startsWith("-as, s.")) //aberācija, milns
				{
					newBegin = "-as, s.".length();
					if (lemma.matches(".*[^aeiouāēīōū]s"))
					{
						paradigm.add(11);
						flags.add("Lietvārds");
					} 
					else if (lemma.endsWith("a"))
					{
						paradigm.add(7);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 7, 11\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				
				// Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
				// Paradigm 11: Lietvārds 6. deklinācija -s
				else if (gramText.startsWith("dsk. ģen. -ņu, s.")) //ādmine, bākuguns, bārkšsaknes
				{
					newBegin = "dsk. ģen. -ņu, s.".length();
					if (lemma.endsWith("ns"))
					{
						paradigm.add(11);
						flags.add("Lietvārds");
					}
					else if (lemma.endsWith("nes"))
					{
						paradigm.add(9);
						flags.add("Lietvārds");
						flags.add("Šķirkļavārds daudzskaitlī");
					}
					else if (lemma.endsWith("ne"))
					{
						paradigm.add(9);
						flags.add("Lietvārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 9, 11\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				
				// Grammar includes endings for otherl lemma variants. 
				// Paradigm 1: Lietvārds 1. deklinācija -s
				// Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
				else if (gramText.matches("s\\. -te, -šu([;.].*)?")) //abstinents
				{
					newBegin = "s. -te, -šu".length();
					if (lemma.endsWith("ts"))
					{
						Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 1) + "e");
						HashSet<String> altParams = new HashSet<String> ();
						altParams.add("Sieviešu dzimte");
						altParams.add("Cita paradigma");
						altLemmas.put(9, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
						
						paradigm.add(1);
						flags.add("Lietvārds");
						flags.add("Vīriešu dzimte");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 1 & 5\n", lemma);
						newBegin = 0;
					}
				}
				// Paradigm 3: Lietvārds 2. deklinācija -is
				// Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
				else if (gramText.matches("-ķa; s\\. -ķe -ķu([;.].*)?")) //agonistiķis
				{
					newBegin = "-ķa; s. -ķe -ķu".length();
					if (lemma.endsWith("ķis"))
					{
						Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 2) + "e");
						HashSet<String> altParams = new HashSet<String> ();
						altParams.add("Sieviešu dzimte");
						altParams.add("Cita paradigma");
						altLemmas.put(9, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
						
						paradigm.add(2);
						flags.add("Lietvārds");
						flags.add("Vīriešu dzimte");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 3 & 5\n", lemma);
						newBegin = 0;
					}
				}
				else if (gramText.matches("-ša; s. -te, -šu([;.].*)?")) //aiolietis
				{
					newBegin = "-ša; s. -te, -šu".length();
					if (lemma.endsWith("tis"))
					{
						Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 2) + "e");
						HashSet<String> altParams = new HashSet<String> ();
						altParams.add("Sieviešu dzimte");
						altParams.add("Cita paradigma");
						altLemmas.put(9, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
						
						paradigm.add(2);
						flags.add("Lietvārds");
						flags.add("Vīriešu dzimte");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 3 & 5\n", lemma);
						newBegin = 0;
					}
				}
				// Paradigm 13: Īpašības vārdi ar -s
				// Paradigm 14: Īpašības vārdi ar -š
				else if (gramText.matches("īp\\. v\\. -ais; s\\. -a, -ā([;,.].*)?")) //aerobs
				{
					newBegin = "īp. v. -ais; s. -a, -ā".length();
					if (lemma.matches(".*[^aeiouāēīōū]š"))
					{
						paradigm.add(14);
						flags.add("Īpašības vārds");
					}
					else if (lemma.matches(".*[^aeiouāēīōū]s"))
					{
						paradigm.add(13);
						flags.add("Īpašības vārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigms 13, 14\n", lemma);
						newBegin = 0;
					}
				}
				else if (gramText.matches("-ais[;,] s\\. -a, -ā([;,.].*)?")) //abējāds, acains, agāms
				{
					newBegin = "-ais; s. -a, -ā".length();
					if (lemma.matches(".*[^aeiouāēīōū]š"))
					{
						paradigm.add(14);
						flags.add("Īpašības vārds");
					}
					else if (lemma.matches(".*[^aeiouāēīōū]s"))
					{
						paradigm.add(13);
						flags.add("Īpašības vārds");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigms 13, 14\n", lemma);
						newBegin = 0;
					}
				}
				
				// Paradigm 13-14: plural forms
				else if (gramText.startsWith("s. -as; adj.")) //abēji 2
				{
					newBegin = "s. -as; adj.".length();
					if (lemma.endsWith("i"))
					{
						paradigm.add(13);
						paradigm.add(14);
						flags.add("Īpašības vārds");
						flags.add("Šķirkļavārds daudzskaitlī");
						flags.add("Neviennozīmīga paradigma");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigms 13-14\n", lemma);
						newBegin = 0;
					}
	
				}
				else if (gramText.startsWith("s. -as; tikai dsk.")) //abēji 1
				{
					// This exception is on purpose! this way "tikai dsk." is later
					// transformed to appropriate flag.
					newBegin = "s. -as;".length();
					if (lemma.endsWith("i"))
					{
						paradigm.add(13);
						paradigm.add(14);
						flags.add("Īpašības vārds");
						flags.add("Šķirkļavārds daudzskaitlī");
						flags.add("Neviennozīmīga paradigma");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigms 13-14\n", lemma);
						newBegin = 0;
					}
				}
				// Paradigm 25: Vietniekvārdi
				else if (gramText.startsWith("s. -as; vietniekv."))		// abi
				{
					newBegin = "s. -as; vietniekv.".length();
					if (lemma.endsWith("i"))
					{
						paradigm.add(25);
						flags.add("Šķirkļavārds daudzskaitlī");
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 25\n", lemma);
						newBegin = 0;
					}
					flags.add("Vietniekvārds");
				}
				
				// Paradigm 30: jaundzimušais, pēdējais
				else if (gramText.startsWith("-šā, v. -šās, s.")) //iereibušais
				{
					newBegin = "-šā, v. -šās, s.".length();
					if (lemma.endsWith("ušais"))
					{
						paradigm.add(30);
						flags.add("Īpašības vārds");			
						flags.add("Lietvārds");	
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 30\n", lemma);
						newBegin = 0;
					}
				}
				else if (gramText.startsWith("-ā, v.")) //pirmdzimtais
				{
					newBegin = "-ā, v.".length();
					if (lemma.endsWith("ais"))
					{
						paradigm.add(30);
						flags.add("Īpašības vārds");			
						flags.add("Lietvārds");	
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 30\n", lemma);
						newBegin = 0;
					}
					flags.add("Vīriešu dzimte");
				}
				else if (gramText.startsWith("-ās, s.")) //pirmdzimtā
				{
					newBegin = "-ās, s.".length();
					if (lemma.endsWith("šanās"))
					{
						paradigm.add(0);
						flags.add("Atgriezeniskais lietvārds");
						flags.add("Lietvārds");	
	
					}
					else if (lemma.endsWith("ā"))
					{
						paradigm.add(30);
						flags.add("Īpašības vārds");
						flags.add("Lietvārds");	
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigms 30, -šanās\n", lemma);
						newBegin = 0;
					}
					flags.add("Sieviešu dzimte");
				}
				else if (gramText.matches("s. -ā([.;].*)?")) //agrākais
				{
					newBegin = "s. -ā".length();
					if (lemma.endsWith("ais"))
					{
						paradigm.add(30);
						flags.add("Īpašības vārds");					
					}
					else
					{
						System.err.printf("Problem matching \"%s\" with paradigm 30\n", lemma);
						newBegin = 0;
					}
				}
			}
			
			// "-??a, v."
			if (newBegin == -1) newBegin = aEndingMascRules(gramText, lemma);
			// "-??u, v."
			if (newBegin == -1) newBegin = uEndingMascRules(gramText, lemma);
			// "-??u, s."
			if (newBegin == -1) newBegin = uEndingFemRules(gramText, lemma);
			
			// === Risky rules =================================================
			// These rules matches prefix of some other rule.
			if (newBegin == -1) newBegin = singleEndingOnlyRules(gramText, lemma);
			
			if (newBegin > 0) gramText = gramText.substring(newBegin);
			if (gramText.matches("[.,;].*")) gramText = gramText.substring(1);
			return gramText;
		}
		
		/**
		 * Simple rule - tries to match grammar text to given pattern and lemma
		 * ending. If matched, adds a single paradigm.
		 * @param pattern	Unescaped ending string grammar text must begin with
		 * 					to apply this rule.
		 * @param requiredEnding	Required ending for the lemma to apply this
		 * 							rule.
		 * @param paradigmId	Paradigm ID to set if rule matched.
		 * @param positiveFlags	These flags are added if rule and lemma ending
		 * 						matched.
		 * @param alwaysFlags	These flags are added if rule matched.
		 * @param gramText	Grammar string currently being processed.
		 * @param lemma		Lemma string for this header.
		 * @return New begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int simpleRule (
				String pattern, String requiredEnding, int paradigmId,
				String[] positiveFlags, String[] alwaysFlags,
				String gramText, String lemma)
		{
			int newBegin = -1;
			if (gramText.matches("\\Q" + pattern + "\\E([;,.].*)?"))
			{
				newBegin = pattern.length();
				if (lemma.endsWith(requiredEnding))
				{
					paradigm.add(paradigmId);
					if (positiveFlags != null)
						flags.addAll(Arrays.asList(positiveFlags));
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm %s\n", lemma, paradigmId);
					newBegin = 0;
				}
				if (alwaysFlags != null) flags.addAll(Arrays.asList(alwaysFlags));
			}
			return newBegin;
		}
		
		/**
		 * Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
		 * Rules in form "-es, dsk. ģen. -ču, s.".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int esEndingPluralGenUEndingFemRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
			if (gramText.startsWith("-es, dsk. ģen. -ču, s.")) //ābece
			{
				newBegin = "-es, dsk. ģen. -ču, s.".length();
				if (lemma.matches(".*[cč]e"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ļu, s.")) //ābele
			{
				newBegin = "-es, dsk. ģen. -ļu, s.".length();
				if (lemma.endsWith("le"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");					
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -šu, s.")) //abate
			{
				newBegin = "-es, dsk. ģen. -šu, s.".length();
				if (lemma.matches(".*[tsš]e"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ņu, s.")) //ābolaine
			{
				newBegin = "-es, dsk. ģen. -ņu, s.".length();
				if (lemma.endsWith("ne"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -žu, s.")) //ābolmaize
			{
				newBegin = "-es, dsk. ģen. -žu, s.".length();
				if (lemma.matches(".*[zd]e"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}				
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ru, s.")) //administratore
			{
				newBegin = "-es, dsk. ģen. -ru, s.".length();
				if (lemma.endsWith("re"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -stu, s.")) //abolicioniste
			{
				newBegin = "-es, dsk. ģen. -stu, s.".length();
				if (lemma.endsWith("ste"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ģu, s.")) //aeroloģe
			{
				newBegin = "-es, dsk. ģen. -ģu, s.".length();
				if (lemma.endsWith("ģe"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -vju, s.")) //agave
			{
				newBegin = "-es, dsk. ģen. -vju, s.".length();
				if (lemma.endsWith("ve"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -ķu, s.")) //agnostiķe
			{
				newBegin = "-es, dsk. ģen. -ķu, s.".length();
				if (lemma.endsWith("ķe"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -mju, s.")) //agronome
			{
				newBegin = "-es, dsk. ģen. -mju, s.".length();
				if (lemma.endsWith("me"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-es, dsk. ģen. -pju, s.")) //aitkope
			{
				newBegin = "-es, dsk. ģen. -pju, s.".length();
				if (lemma.endsWith("pe"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			return newBegin;
		}
		
		/**
		 * Paradigm 7: Lietvārds 4. deklinācija -a siev. dz.
		 * Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
		 * Paradigm 11: Lietvārds 6. deklinācija -s
		 * Rules in form "-šu, s." and "-u, s.".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int uEndingFemRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigms: 7, 9, 11
			if (gramText.startsWith("-šu, s.")) //ahajiete, aizkulises, bikses, klaušas
			{
				newBegin = "-šu, s.".length();
				if (lemma.endsWith("te"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else if (lemma.endsWith("šas"))
				{
					paradigm.add(7);
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Lietvārds");
				}
				else if (lemma.endsWith("tis"))
				{
					paradigm.add(11);
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Lietvārds");
				}
				else if (lemma.matches(".*[st]es"))
				{
					paradigm.add(9);
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 7, 9, 11\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			// Paradigms: 7, 9
			else if (gramText.startsWith("-žu, s.")) //mirādes, graizes, bažas
			{
				newBegin = "-žu, s.".length();
				if (lemma.endsWith("žas"))
				{
					paradigm.add(7);
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Lietvārds");
				}
				else if (lemma.matches(".*[dz]es"))
				{
					paradigm.add(9);
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 7, 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-ņu, s.")) //acenes, iemaņas
			{
				newBegin = "-ņu, s.".length();
				if (lemma.endsWith("ņas"))
				{
					paradigm.add(7);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else if (lemma.endsWith("nes"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 7, 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			else if (gramText.startsWith("-u, s.")) // aijas, zeķes
			{
				newBegin = "-u, s.".length();
				if (lemma.endsWith("as"))
				{
					paradigm.add(7);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else if (lemma.endsWith("a"))
				{
					paradigm.add(7);
					flags.add("Lietvārds");
				}
				else if (lemma.matches(".*[ķ]es"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 7, 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			// Paradigms: 9
			else if (gramText.startsWith("-ļu, s.")) //bailes
			{
				newBegin = "-ļu, s.".length();
				if (lemma.endsWith("les"))
				{
					paradigm.add(9);
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
				flags.add("Sieviešu dzimte");
			}
			return newBegin;
		}
		
		/**
		 * Paradigm 1: Lietvārds 1. deklinācija -s
		 * Paradigm 2: Lietvārds 1. deklinācija -š
		 * Paradigm 3: Lietvārds 2. deklinācija -is
		 * Paradigm 5: Lietvārds 2. deklinācija -suns
		 * Rules in form "-ļa, v." and "-a, v.".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int aEndingMascRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigms: 3, 5
			if (gramText.startsWith("-ļa, v.")) // acumirklis, durkls
			{
				newBegin = "-ļa, v.".length();
				if (lemma.endsWith("ls"))
				{
					paradigm.add(5);
					flags.add("Lietvārds");
				}
				else if (lemma.endsWith("lis"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3, 5\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			else if (gramText.startsWith("-ša, v.")) // abrkasis, lemess
			{
				newBegin = "-ša, v.".length();
				if (lemma.endsWith("ss"))
				{
					paradigm.add(5);
					flags.add("Lietvārds");
				}
				else if (lemma.matches(".*[stš]is"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3, 5\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			// Paradigm 3
			else if (gramText.startsWith("-ķa, v.")) // agnostiķis
			{
				newBegin = "-ķa, v.".length();
				if (lemma.matches(".*[ķ]is"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			else if (gramText.startsWith("-pja, v.")) // aitkopis
			{
				newBegin = "-pja, v.".length();
				if (lemma.endsWith("pis"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			else if (gramText.startsWith("-žņa, v.")) // aizbāznis
			{
				newBegin = "-žņa, v.".length();
				if (lemma.endsWith("znis"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			else if (gramText.startsWith("-ža, vsk.")) // ādgrauzis
			{
				newBegin = "-ža, vsk.".length();
				if (lemma.matches(".*[zd]is"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			//Paradigms: 1, 3
			else if (gramText.matches("-ra[,;] v.(.*)?")) // airis, mūrniekmeistars
			{
				newBegin = "-ra, v.".length();
				if (lemma.endsWith("ris"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else if (lemma.endsWith("rs"))
				{
					paradigm.add(1);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			// Paradigms: 2, 3, 5
			else if (gramText.startsWith("-ņa, v.")) // abesīnis
			{
				newBegin = "-ņa, v.".length();
				
				if (lemma.endsWith("suns"))
				{
					paradigm.add(5);
					flags.add("Lietvārds");
				}
				else if (lemma.endsWith("ņš"))
				{
					paradigm.add(2);
					flags.add("Lietvārds");
				}
				else if (lemma.endsWith("nis"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigms 2, 3, 5\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			// Paradigms 1, 2, 3 (ja nav miju)
			else if (gramText.startsWith("-a, v.")) // abats, akustiķis
			{
				newBegin = "-a, v.".length();
				if (lemma.matches(".*[ģjķr]is"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
					
				}
				else if (lemma.endsWith("š"))
				{
					paradigm.add(2);
					flags.add("Lietvārds");
				}
				else if (lemma.matches(".*[^aeiouāēīōū]s"))
				{
					paradigm.add(1);
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigms 1, 2, 3\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			return newBegin;
		}
		
		/**
		 * Paradigm 1: Lietvārds 1. deklinācija -s
		 * Paradigm 2: Lietvārds 1. deklinācija -š
		 * Paradigm 3: Lietvārds 2. deklinācija -is
		 * Paradigm 4: Lietvārds 2. deklinācija -s (piem., mēness) (vsk. nom. = vsk. gen)
		 * Paradigm 5: Lietvārds 2. deklinācija -suns
		 * Paradigm 32: Lietvārds 6. deklinācija - ļaudis
		 * Rules in form "-žu, v." and "-u, v.".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int uEndingMascRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 32
			if (gramText.startsWith("-žu, v.")) //ļaudis
			{
				newBegin = "-žu, v.".length();
				if (lemma.endsWith("ļaudis"))
				{
					paradigm.add(11);
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Lietvārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 32\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");					
				// TODO Daudzskaitlinieks?
			}
			// Paradigms: 1-5 (plural forms)
			else if (gramText.startsWith("-ņu, v.")) // bretoņi
			{
				newBegin = "-ņu, v.".length();
				if (lemma.endsWith("ņi"))
				{
					paradigm.add(1);
					paradigm.add(2);
					paradigm.add(3);
					paradigm.add(4);
					paradigm.add(5);
					flags.add("Lietvārds");
					flags.add("Neviennozīmīga paradigma");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigms 1-5\n", lemma);
					newBegin = 0;
				}
				flags.add("Vīriešu dzimte");
			}
			else if (gramText.startsWith("-u, v.")) // abesīņi, abhāzi, ādgrauži, adigejieši, adžāri, alimenti, angļi, antinukloni, apakšbrunči
			{
				newBegin = "-u, v.".length();
				if (lemma.endsWith("nieki") || lemma.endsWith("umi")
						|| lemma.endsWith("otāji"))
				{
					paradigm.add(1);
					flags.add("Lietvārds");
				}
				else
				{
					if (lemma.matches(".*[ņš]i"))	// akmeņi, mēneši etc.
					{
						paradigm.add(1);
						paradigm.add(2);
						paradigm.add(3);
						paradigm.add(4);
						paradigm.add(5);
						flags.add("Lietvārds");
						flags.add("Neviennozīmīga paradigma");
					}
					else if (lemma.matches(".*[vpm]ji"))	// looks like these are predefined sound changes always
					{
						paradigm.add(3);
						paradigm.add(5);
						flags.add("Lietvārds");
						flags.add("Neviennozīmīga paradigma");
					}
					else if (lemma.matches(".*[bgkhrstčģķļž]i")
							|| lemma.matches(".*[aeiouāēīōū]ji"))	// can't determine if there is sound change (t - tēti, s - viesi, j - airkāji)
					{
						paradigm.add(1);
						paradigm.add(2);
						paradigm.add(3);
						paradigm.add(5);
						flags.add("Lietvārds");
						flags.add("Neviennozīmīga paradigma");
					}
					else if (lemma.matches(".*[cdlmnpvz]i"))	// there is no sound change
					{
						paradigm.add(1);
						paradigm.add(2);
						flags.add("Lietvārds");
						flags.add("Neviennozīmīga paradigma");
					}
					else 
					{
						System.err.printf("Problem matching \"%s\" with paradigms 1-5\n", lemma);
						newBegin = 0;						
					}
				}
				flags.add("Vīriešu dzimte");
			}			
			return newBegin;
		}
		/**
		 * Paradigm 3: Lietvārds 2. deklinācija -is
		 * Paradigm 9: Lietvārds 5. deklinācija -e siev. dz.
		 * Rules containing single ending with no other information, e.g. "-ņu".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int singleEndingOnlyRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 9
			if (gramText.matches("-žu([;.].*)?")) //abioģenēze, ablumozes, akolāde, nematodes
			{
				newBegin = "-žu".length();
				if (lemma.matches(".*[dz]es"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
					flags.add("Sieviešu dzimte");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else if (lemma.matches(".*[dz]e"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
					flags.add("Sieviešu dzimte");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-ņu([;.].*)?")) //agrene, aizlaidnes
			{
				newBegin = "-ņu".length();
				if (lemma.endsWith("nes"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
					flags.add("Sieviešu dzimte");
					flags.add("Šķirkļavārds daudzskaitlī");
				}
				else if (lemma.endsWith("ne"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
					flags.add("Sieviešu dzimte");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 9\n", lemma);
					newBegin = 0;
				}
			}
			// Paradigm 3
			else if (gramText.matches("-ņa([;,.].*)?")) //ābolainis
			{
				newBegin = "-ņa".length();
				if (lemma.endsWith("nis"))
				{
					paradigm.add(3);
					flags.add("Lietvārds");
					flags.add("Vīriešu dzimte");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 3\n", lemma);
					newBegin = 0;
				}
			}
			return newBegin;
		}
		
		/**
		 * Paradigm 15: Darbības vārdi 1. konjugācija tiešie
		 * Rules in form "parasti 3. pers., -šalc, pag. -šalca" and
		 * "-tupstu, -tupsti, -tupst, pag. -tupu".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int firstConjDirVerbRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 15: Darbības vārdi 1. konjugācija tiešie
			if (gramText.matches("parasti 3\\. pers\\., -šalc, pag\\. -šalca([;,.].*)?")) //aizšalkt
			{
				newBegin = "parasti 3. pers., -šalc, pag. -šalca".length();
				if (lemma.endsWith("šalkt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"šalkt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -tūkst, pag\\. -tūka([;,.].*)?")) //aiztūkt
			{
				newBegin = "parasti 3. pers., -tūkst, pag. -tūka".length();
				if (lemma.endsWith("tūkt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"tūkt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -aug, pag\\. -auga([;,.].*)?")) //aizaugt
			{
				newBegin = "parasti 3. pers., -aug, pag. -auga".length();
				if (lemma.endsWith("augt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"augt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -aust, pag\\. -ausa([;,.].*)?")) //aizaust 1
			{
				newBegin = "parasti 3. pers., -aust, pag. -ausa".length();
				if (lemma.endsWith("aust"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"aust\" (kā gaisma)");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -birst, pag\\. -bira([;,.].*)?")) //aizbirt
			{
				newBegin = "parasti 3. pers., -birst, pag. -bira".length();
				if (lemma.endsWith("birt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"birt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -brūk, pag\\. -bruka([;,.].*)?")) //aizbrukt
			{
				newBegin = "parasti 3. pers., -brūk, pag. -bruka".length();
				if (lemma.endsWith("brukt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"brukt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("-eju, -ej, -iet, pag\\. -gāju([.,;].*)?")) //apiet
			{
				newBegin = "-eju, -ej, -iet, pag. -gāju".length();
				if (lemma.endsWith("iet"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"iet\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-tupstu, -tupsti, -tupst, pag\\. -tupu([;,.].*)?")) //aiztupt
			{
				newBegin = "-tupstu, -tupsti, -tupst, pag. -tupu".length();
				if (lemma.endsWith("tupt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"tupt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}	
			else if (gramText.matches("-tveru, -tver, -tver, pag\\. -tvēru([;,.].*)?")) //aiztvert
			{
				newBegin = "-tveru, -tver, -tver, pag. -tvēru".length();
				if (lemma.endsWith("tvert"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"tvert\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-griežu, -griez, -griež, pag\\. -griezu([;,.].*)?")) //apgriezt
			{
				newBegin = "-griežu, -griez, -griež, pag. -griezu".length();
				if (lemma.endsWith("griezt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"griezt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-ģiedu, -ģied, -ģied, pag\\. -gidu([;,.].*)?")) //apģist
			{
				newBegin = "-ģiedu, -ģied, -ģied, pag. -gidu".length();
				if (lemma.endsWith("ģist"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"ģist\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-klāju, -klāj, -klāj, pag\\. -klāju([;,.].*)?")) //apklāt
			{
				newBegin = "-klāju, -klāj, -klāj, pag. -klāju".length();
				if (lemma.endsWith("klāt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"klāt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-kauju, -kauj, -kauj, pag\\. -kāvu([;,.].*)?")) //apkaut
			{
				newBegin = "-kauju, -kauj, -kauj, pag. -kāvu".length();
				if (lemma.endsWith("kaut"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"kaut\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-aru, -ar, -ar, pag\\. -aru([;,.].*)?")) //aizart
			{
				newBegin = "-aru, -ar, -ar, pag. -aru".length();
				if (lemma.endsWith("art"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"art\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-aužu, -aud, -auž, pag\\. -audu([;,.].*)?")) //aizaust 2
			{
				newBegin = "-aužu, -aud, -auž, pag. -audu".length();
				if (lemma.endsWith("aust"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"aust\" (kā zirneklis)");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-bāžu, -bāz, -bāž, pag\\. -bāzu([;,.].*)?")) //aizbāzt
			{
				newBegin = "-bāžu, -bāz, -bāž, pag. -bāzu".length();
				if (lemma.endsWith("bāzt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"bāzt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-bēgu, -bēdz, -bēg, pag\\. -bēgu([;,.].*)?")) //aizbēgt
			{
				newBegin = "-bēgu, -bēdz, -bēg, pag. -bēgu".length();
				if (lemma.endsWith("bēgt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"bēgt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-beru, -ber, -ber, pag. -bēru([;,.].*)?")) //aizbērt
			{
				newBegin = "-beru, -ber, -ber, pag. -bēru".length();
				if (lemma.endsWith("bērt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"bērt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-bilstu, -bilsti, -bilst, pag\\. -bildu([;,.].*)?")) //aizbilst
			{
				newBegin = "-bilstu, -bilsti, -bilst, pag. -bildu".length();
				if (lemma.endsWith("bilst"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"bilst\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-braucu, -brauc, -brauc, pag\\. -braucu([;,.].*)?")) //aizbraukt
			{
				newBegin = "-braucu, -brauc, -brauc, pag. -braucu".length();
				if (lemma.endsWith("braukt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"braukt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-brāžu, -brāz, -brāž, pag\\. -brāzu([;,.].*)?")) //aizbrāzt
			{
				newBegin = "-brāžu, -brāz, -brāž, pag. -brāzu".length();
				if (lemma.endsWith("brāzt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"brāzt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-brienu, -brien, -brien, pag\\. -bridu([;,.].*)?")) //aizbrist
			{
				newBegin = "-brienu, -brien, -brien, pag. -bridu".length();
				if (lemma.endsWith("brist"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"brist\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-ceļu, -cel, -ceļ, pag\\. -cēlu([;,.].*)?")) //aizcelt
			{
				newBegin = "-ceļu, -cel, -ceļ, pag. -cēlu".length();
				if (lemma.endsWith("celt"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"celt\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-cērtu, -cērt, -cērt, pag\\. -cirtu([;,.].*)?")) //aizcirst
			{
				newBegin = "-cērtu, -cērt, -cērt, pag. -cirtu".length();
				if (lemma.endsWith("cirst"))
				{
					paradigm.add(15);
					flags.add("Darbības vārds");
					flags.add("Locīt kā \"cirst\"");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 15\n", lemma);
					newBegin = 0;
				}
			}
			return newBegin;
		}
		
		/**
		 * Paradigm 16: Darbības vārdi 2. konjugācija tiešie
		 * Rules in form "parasti 3. pers., -o, pag. -oja",
		 * "-oju, -o, -o, -ojam, -ojat, pag. -oju; -ojām, -ojāt; pav. -o, -ojiet"
		 * and "-ēju, -ē, -ē, pag. -ēju".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int secondConjDirVerbRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 16: Darbības vārdi 2. konjugācija tiešie
			if (gramText.matches("parasti 3\\. pers\\., -o, pag\\. -oja([;,.].*)?")) //aizšalkot, aizbangot
			{
				newBegin = "parasti 3. pers., -o, pag. -oja".length();
				if (lemma.endsWith("ot"))
				{
					paradigm.add(16);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("parasti 3\\. pers\\., -ē, pag\\. -ēja([;,.].*)?")) //adsorbēt
			{
				newBegin = "parasti 3. pers., -ē, pag. -ēja".length();
				if (lemma.endsWith("ēt"))
				{
					paradigm.add(16);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("-oju, -o, -o, -ojam, -ojat, pag\\. -oju; -ojām, -ojāt; pav\\. -o, -ojiet([,;.].*)?")) //acot
			{
				newBegin = "-oju, -o, -o, -ojam, -ojat, pag. -oju; -ojām, -ojāt; pav. -o, -ojiet".length();
				if (lemma.endsWith("ot"))
				{
					paradigm.add(16);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-ēju, -ē, -ē, -ējam, -ējat, pag\\. -ēju, -ējām, -ējāt; pav\\. -ē, -ējiet([,;.].*)?")) //adverbializēt
			{
				newBegin = "-ēju, -ē, -ē, -ējam, -ējat, pag. -ēju, -ējām, -ējāt; pav. -ē, -ējiet".length();
				if (lemma.endsWith("ēt"))
				{
					paradigm.add(16);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-ēju, -ē, -ē, pag\\. -ēju([;,.].*)?")) //absolutizēt
			{
				newBegin = "-ēju, -ē, -ē, pag. -ēju".length();
				if (lemma.endsWith("ēt"))
				{
					paradigm.add(16);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-oju, -o, -o, pag\\. -oju([;,.].*)?")) //aiztuntuļot
			{
				newBegin = "-oju, -o, -o, pag. -oju".length();
				if (lemma.endsWith("ot"))
				{
					paradigm.add(16);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-āju, -ā, -ā, pag\\. -āju([;,.].*)?")) //aijāt
			{
				newBegin = "-āju, -ā, -ā, pag. -āju".length();
				if (lemma.endsWith("āt"))
				{
					paradigm.add(16);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 16\n", lemma);
					newBegin = 0;
				}
			}
			return newBegin;
		}
		
		/**
		 * Paradigm 17: Darbības vārdi 3. konjugācija tiešie
		 * Rules in form "-u, -i, -a, pag. -īju"
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int thirdConjDirVerbRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 17: Darbības vārdi 3. konjugācija tiešie
			if (gramText.matches("parasti 3. pers\\., -blākš, pag\\. -blākšēja([;,.].*)?")) //aizblākšēt
			{
				newBegin = "parasti 3. pers., -blākš, pag. -blākšēja".length();
				if (lemma.endsWith("blākšēt"))
				{
					paradigm.add(17);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("parasti 3\\. pers\\., -blākšķ, pag\\. -blākšķēja([;,.].*)?")) //aizblākšķēt
			{
				newBegin = "parasti 3. pers., -blākšķ, pag. -blākšķēja".length();
				if (lemma.endsWith("blākšķēt"))
				{
					paradigm.add(17);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
					newBegin = 0;
				}
				flags.add("Parasti 3. personā");
			}
			else if (gramText.matches("-turu, -turi, -tur, pag\\. -turēju([;,.].*)?")) //aizturēt
			{
				newBegin = "-turu, -turi, -tur, pag. -turēju".length();
				if (lemma.endsWith("turēt"))
				{
					paradigm.add(17);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-u, -i, -a, pag\\. -īju([;,.].*)?")) //aizsūtīt
			{
				newBegin = "-u, -i, -a, pag. -īju".length();
				if (lemma.endsWith("īt"))
				{
					paradigm.add(17);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-inu, -ini, -ina, pag\\. -ināju([;,.].*)?")) //aizsvilināt
			{
				newBegin = "-inu, -ini, -ina, pag. -ināju".length();
				if (lemma.endsWith("ināt"))
				{
					paradigm.add(17);
					flags.add("Darbības vārds");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 17\n", lemma);
					newBegin = 0;
				}
			}
			return newBegin;			
		}

		/**
		 * Paradigm 18: Darbības vārdi 1. konjugācija atgriezeniski
		 * Rules in form "parasti 3. pers., -šalcas, pag. -šalcās" and
		 * "-tupstos, -tupsties, -tupstas, pag. -tupos".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int firstConjRefVerbRules (String gramText, String lemma)
		{
			int newBegin = -1;
			
			// Paradigm 18: Darbības vārdi 1. konjugācija atgriezeniski
			newBegin = simpleRule(
					"parasti 3. pers., -šalcas, pag. -šalcās", "šalkties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"šalkties\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizšalkties
			if (newBegin == -1) newBegin = simpleRule(
					"-ejos, -ejos, -ietas, pag. -gājos", "ieties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"ieties\""},
					null, gramText, lemma); //apieties
			if (newBegin == -1) newBegin = simpleRule(
					"-tupstos, -tupsties, -tupstas, pag. -tupos", "tupties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"tupties\""},
					null, gramText, lemma); //aiztupties
					//TODO check paralel forms.
			if (newBegin == -1) newBegin = simpleRule(
					"-ģiedos, -ģiedies, -ģiedas, pag. -gidos", "ģisties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"ģisties\""},
					null, gramText, lemma); //apģisties
			if (newBegin == -1) newBegin = simpleRule(
					"-klājos, -klājies, -klājas, pag. -klājos", "klāties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"klāties\""},
					null, gramText, lemma); //apklāties
			if (newBegin == -1) newBegin = simpleRule(
					"-karos, -karies, -karas, pag. -kāros", "kārties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"kārties\""},
					null, gramText, lemma); //apkārties
			if (newBegin == -1) newBegin = simpleRule(
					"-brāžos, -brāzies, -brāžas, pag. -brāžos", "brāzties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"brāzties\""},
					null, gramText, lemma); //aizbrāzties
			if (newBegin == -1) newBegin = simpleRule(
					"-brēcos, -brēcies, -brēcas, pag. -brēcos", "brēkties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"brēkties\""},
					null, gramText, lemma); //aizbrēkties
			if (newBegin == -1) newBegin = simpleRule(
					"-ciešos, -cieties, -ciešas, pag. -cietos", "ciesties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"ciesties\""},
					null, gramText, lemma); //aizciesties
			if (newBegin == -1) newBegin = simpleRule(
					"-cērtos, -cērties, -cērtas, pag. -cirtos", "cirsties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"cirsties\""},
					null, gramText, lemma); //aizcirsties
			
			return newBegin;
		}
		
		/**
		 * Paradigm 19: Darbības vārdi 2. konjugācija atgriezeniski
		 * Rules in form "parasti 3. pers., -ējas, pag. -ējās",
		 * "-ējos, -ējies, -ējas, -ējamies, -ējaties, pag. -ējos, -ējāmies, -ējāties; pav. -ējies, -ējieties",
		 *  and "-ojos, -ojies, -ojas, pag. -ojos".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int secondConjRefVerbRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 19: Darbības vārdi 2. konjugācija atgriezeniski
			newBegin = simpleRule(
					"parasti 3. pers., -ējas, pag. -ējās", "ēties", 19,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //absorbēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -ojas, pag. -ojās", "oties", 19,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //daudzkāršoties
			if (newBegin == -1) newBegin = simpleRule(
					"-ējos, -ējies, -ējas, -ējamies, -ējaties, pag. -ējos, -ējāmies, -ējāties; pav. -ējies, -ējieties",
					"ēties", 19,
					new String[] {"Darbības vārds"}, null,
					gramText, lemma); //adverbiēties
			if (newBegin == -1) newBegin = simpleRule(
					"-ojos, -ojies, -ojas, pag. -ojos", "oties", 19,
					new String[] {"Darbības vārds"}, null,
					gramText, lemma); //aiztuntuļoties, apgrēkoties
			if (newBegin == -1) newBegin = simpleRule(
					"-ējos, -ējies, -ējas, pag. -ējos", "ēties", 19,
					new String[] {"Darbības vārds"}, null,
					gramText, lemma); //abstrahēties
			return newBegin;
		}
		
		/**
		 * Paradigm 20: Darbības vārdi 3. konjugācija atgriezeniski
		 * Rules in form "parasti 3. pers., -ās, pag. -ījās" and
		 * "-os, -ies, -ās, pag. -ījos".
		 * This function is seperated out for readability from
		 * {@link #processWithPatterns(String, String)} as currently these rules
		 * for verbs are long and highly specific and, thus, do not conflict
		 * with other rules.
		 * @return new begining for gram string if one of these rulles matched,
		 * -1 otherwise.
		 */
		private int thirdConjRefVerbRules (String gramText, String lemma)
		{
			int newBegin = -1;
			// Paradigm 20: Darbības vārdi 3. konjugācija atgriezeniski
			newBegin = simpleRule(
					"parasti 3. pers., -brikšķas, pag. -brikšķējās",
					"brikšķēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbrikšķēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -brikšas, pag. -brikšējās",
					"brikšēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbrikšēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -brīkšķas, pag. -brīkšķējās",
					"brīkšķēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbrīkšķēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -brīkšas, pag. -brīkšējās",
					"brīkšēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbrīkšēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -ās, pag. -ījās", "īties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbīdīties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -inās, pag. -inājās", "ināties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbubināties
			if (newBegin == -1) newBegin = simpleRule(
					"-os, -ies, -ās, pag. -ījos", "īties", 20,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //apklausīties
			if (newBegin == -1) newBegin = simpleRule(
					"-inos, -inies, -inās, pag. -inājos", "ināties", 20,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //apklaušināties
			if (newBegin == -1) newBegin = simpleRule(
					"-os, -ies, -as, pag. -ējos", "ēties", 20,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //apkaunēties

			return newBegin;
		}
		
		/**
		 * @param lemma is used for paradigm detection in cases where endings
		 * matter.
		 */
		private void paradigmFromFlags(String lemma)
		{
			if (flags.contains("Īpašības vārds") )
			{
				if (lemma.endsWith("ais") || lemma.endsWith("ā")) paradigm.add(30);
				else if (lemma.matches(".*[^aeiouāēīōū]s")) paradigm.add(13);
				else if (lemma.matches(".*[^aeiouāēīōū]š")) paradigm.add(14);				
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
			
			if (altLemmas != null && !altLemmas.isEmpty())
			{
				if (hasPrev) res.append(", ");
				res.append("\"AltLemmas\":{");
				Iterator<Integer> it = altLemmas.keySet().iterator();
				while (it.hasNext())
				{
					Integer next = it.next();
					if (!altLemmas.getAll(next).isEmpty())
					{
						res.append("\"");
						res.append(JSONObject.escape(next.toString()));
						res.append("\":[");
						Iterator<Tuple<Lemma, HashSet<String>>> flagIt = altLemmas.getAll(next).iterator();
						while (flagIt.hasNext())
						{
							Tuple<Lemma, HashSet<String>> alt = flagIt.next();
							res.append("{\"Lemma\":");
							res.append(alt.first.toJSON());
							if (alt.second != null && !alt.second.isEmpty())
							{
								res.append(", \"Flags\":");
								res.append(Utils.simplesToJSON(alt.second));
							}
							res.append("}");
							if (flagIt.hasNext()) res.append(", ");
						}
						
						res.append("]");
						if (it.hasNext()) res.append(", ");
					}
				}
				res.append("}");
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
			if ("".equals(ordNumber)) ordNumber = null;
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
			
			if (ordNumber != null)
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
	 * Ordered tuple.
	 */
	public static class Tuple<E, F>
	{
		public E first;
		public F second;
		
		public Tuple (E e, F f)
		{
			first = e;
			second = f;
		}
		
		// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
		@Override
		public boolean equals (Object o)
		{
			if (o == null) return false;
			if (this.getClass() != o.getClass()) return false;
			if ((first == null && ((Tuple)o).first == null || first != null && first.equals(((Tuple)o).first))
					&& (second == null && ((Tuple)o).second == null
					|| second != null && second.equals(((Tuple)o).second)))
				return true;
			else return false;
		}
		
		// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
		@Override
		public int hashCode()
		{
			return 2719 *(first == null ? 1 : first.hashCode())
					+ (second == null ? 1 : second.hashCode());
		}
	}
	
	/**
	 * Limited use multimap. Incomplete interface, might need additional
	 * methods later.
	 */
	public static class MappingSet<K, V>
	{
		private HashMap<K, HashSet<V>> map = new HashMap<K, HashSet<V>>();
		
		public void put (K key, V value)
		{
			HashSet<V> values = new HashSet<V>();
			if (map.containsKey(key))
			{
				values = map.get(key);
			}
			values.add(value);
			map.put(key, values);
		}
		
		public HashSet<V> getAll(K key)
		{
			return map.get(key);
		}
		
		public boolean containsKey(K key)
		{
			return map.containsKey(key);
		}
		
		public boolean isEmpty()
		{
			return map.isEmpty();
		}
		
		public Set<K> keySet()
		{
			return map.keySet();
		}
		
	}
	
	public static interface HasToJSON
	{
		public String toJSON();
	}
}
