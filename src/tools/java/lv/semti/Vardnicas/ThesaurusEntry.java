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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Structured representation of entry header.
 */
public class ThesaurusEntry
{
	
	//Lemma lemma;
	//Gram grammar;
	Sources sources;

	/**
	 * Lemma and all-entry related grammar information.
	 */
	Header head;

	/**
	 * g_n (nozīmju grupa) field.
	 */
	LinkedList<Sense> senses;
	
	/**
	 * g_fraz (frazeoloģismu grupa) field.
	 */
	LinkedList<Phrase> phrases;
	
	/**
	 * g_de (atvasinājumu grupa) field.
	 */
	LinkedList<Header> derivs;
	
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
	}
	
	// Reads data of a single thesaurus entry from the XML format
	public ThesaurusEntry(Node sNode)
	{
		NodeList fields = sNode.getChildNodes(); 
		for (int i = 0; i < fields.getLength(); i++)
		{
			Node field = fields.item(i);
			String fieldname = field.getNodeName();
			if (fieldname.equals("v")) // word info
				head = new Header (field);
			else if (fieldname.equals("avots")) // source
				sources = new Sources (field);
			else if (fieldname.equals("g_n")) // all senses
				senses = Utils.loadSenses(field);
			else if (fieldname.equals("g_fraz")) //phraseological forms
				phrases = Utils.loadPhrases(field, "fraz");
			else if (fieldname.equals("g_de")) //derived forms
				loadDerivs(field);
			else if (!fieldname.equals("#text")) // Text nodes here are ignored.
				System.err.printf("Entry - s - field %s not processed\n", fieldname);
		}
		
		if (inBlacklist()) return;
		
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

	private boolean inBlacklist()
	{
		if (sources == null || !sources.s.contains("LLVV")) return true; // FIXME - temporary restriction to focus on LLVV first
		return blacklist.contains(head.lemma);
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
			while ((rinda = ieeja.readLine()) != null) {
				if (rinda.contains("<s>") || rinda.contains("</s>") || rinda.isEmpty())
					continue;
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
		if (derivs != null) for (Header h : derivs)
		{
			//if (h.hasParadigm()) return true;
			if (!h.hasParadigm()) res = false;
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
		
		if (sources != null && !sources.isEmpty())
		{
			s.append(",");
			s.append(sources.toJSON());
		}
		
		s.append(", \"Senses\":");
		s.append(Utils.objectsToJSON(senses));
		
		s.append(", \"Phrases\":");
		s.append(Utils.objectsToJSON(phrases));
		
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
			for (int i = 0; i < fields.getLength(); i++)
			{
				Node field = fields.item(i);
				String fieldname = field.getNodeName();
				if (fieldname.equals("vf")) // lemma
					lemma = new Lemma(field);
				else if (fieldname.equals("gram")) // grammar
					gram = new Gram (field);
				else if (!fieldname.equals("#text")) // Text nodes here are ignored.
					System.err.printf(
						"v entry field %s not processed\n", fieldname);
			}
			if (lemma == null)
				System.err.printf("Thesaurus v-entry without a lemma :(\n");				
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
		public String l;
		
		public Lemma () { l = null; }
		public Lemma (Node vfNode) { l = vfNode.getTextContent(); }
		
		/**
		 *  Set lemma and check if the information isn't already filled, to
		 *  detect possible overwritten data.
		 */
		public void set(String lemmaText) {
			if (l != null)
				System.err.printf(
					"Duplicate info for field 'lemma' : '%s' and '%s'", l,
					lemmaText);
			l = lemmaText;
		}
		
		public String toJSON()
		{
			return String.format("\"Lemma\":\"%s\"", JSONObject.escape(l));
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
		public static HashMap<String, String> knownAbbr = generateKnownAbbr();
		private static HashMap<String, String> generateKnownAbbr()
		{
			HashMap<String, String> res = new HashMap<String, String>();
			
			// TODO Sort out this mess.
			// Source: LLVV.
			
			res.put("adj.", "Īpašības vārds");
			res.put("apst.", "Apstākļa vārds");
			res.put("divd.", "Divdabis");
			res.put("Divd.", "Divdabis");
			res.put("divd.", "Divdabis");
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
			res.put("vispārin.", "Vispārināmais vietniekvārds");
			res.put("saīs.", "Saīsinājums");

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
			
			res.put("intrans.", "Intransitīvs"); //???
			res.put("trans.", "Transitīvs"); //???

			res.put("konj.", "Konjugācija");
			res.put("pers.", "Persona");

			res.put("atgr.", "Atgriezensisks (vietniekvārds?)");
			res.put("dem.", "Deminutīvs");
			res.put("Dem.", "Deminutīvs");
			res.put("imperf.", "Imperfekta forma"); //???
			res.put("nelok.", "Nelokāms vārds");
			res.put("Nol.", "Noliegums"); // Check with other sources!
			res.put("refl.", "Refleksīvs darbības vārds");
			res.put("Refl.", "Refleksīvs darbības vārds");

			res.put("anat.", "Anatomija");
			res.put("arheol.", "Arheoloģija");
			res.put("arhit.", "Arhitektūra");
			res.put("astr.", "Astronomija");
			res.put("av.", "Aviācija");
			res.put("biol.", "Bioloģija");
			res.put("biškop.", "Biškopība");
			res.put("bot.", "Botānika");
			res.put("būvn.", "Būvniecība");
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
			res.put("ģeogr.", "Ģeogrāfija");
			res.put("ģeol.", "Ģeoloģija");
			res.put("ģeom.", "Ģeometrija");
			res.put("hidrotehn.", "Hidrotehnika");
			res.put("inf.", "Informātika");
			res.put("jur.", "Jurisprudence");
			res.put("jūrn.", "Jūrniecība");
			res.put("kap.", "Attiecas uz kapitālistisko iekārtu, kapitālistisko sabiedrību");
			res.put("kul.", "Kulinārija");
			res.put("ķīm.", "Ķīmija");
			res.put("lauks.", "Lauksaimniecība");
			res.put("literat.", "Literatūrzinātne");
			res.put("loģ.", "Loģika");
			res.put("mat.", "Matemātika");
			res.put("med.", "Medicīna");
			res.put("medn.", "Medniecība");
			res.put("meteorol.", "Meteoroloģija");
			res.put("mežs.", "Mežsaimniecība");
			res.put("mil.", "Militārās zinātnes");
			res.put("min.", "Mineraloģija");
			res.put("mit.", "Mitoloģija");
			res.put("mūz.", "Mūzika");
			res.put("pol.", "Politika");
			res.put("poligr.", "Poligrāfija");
			res.put("psih.", "Psiholoģija");
			res.put("ornit.", "Ornitoloģija");
			res.put("rel.", "Reliģija");
			res.put("tehn.", "Tehnika");
			res.put("tekst.", "Tekstilrūpniecība");
			res.put("val.", "Valodniecība");
			res.put("vet.", "Veterinārija");
			res.put("zool.", "Zooloģija");
			
			res.put("apv.", "Apvidvārds");
			res.put("novec.", "Novecojis"); //TODO - Novecojis, vēsturisks un neaktuāls apvienot??		
			res.put("vēst.", "Vēsturisks");
			res.put("neakt.", "Neaktuāls");
			res.put("poēt.", "Poētiska stilistiskā nokrāsa");
			res.put("niev.", "Nievīga ekspresīvā nokrāsa");
			res.put("iron.", "Ironiska ekspresīvā nokrāsa");
			res.put("hum.", "Humoristiska ekspresīvā nokrāsa");
			res.put("vienk.", "Vienkāršrunas stilistiskā nokrāsa");
			res.put("nevēl.", "Nevēlams"); // TODO - nevēlamos, neliterāros un žargonus apvienot??
			res.put("nelit.", "Neliterārs");
			res.put("žarg.", "Žargonvārds");
			res.put("sar.", "Sarunvaloda");
			
			//TODO - šos drīzāk kā atsevišķu komentāru lauku(s)
			res.put("parasti vsk.", "Parasti vienskaitlī");
			res.put("parasti vsk", "Parasti vienskaitlī");
			res.put("par. vsk.", "Parasti vienskaitlī");
			res.put("tikai vsk.", "Tikai vienskaitlī");
			res.put("parasti dsk.", "Parasti daudzskaitlī");		
			res.put("tikai dsk.", "Tikai daudzskaitlī");
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
		
		public Gram (Node gramNode)
		{
			orig = gramNode.getTextContent();
			leftovers = null;
			flags = new HashSet<String> ();
			paradigm = new HashSet<Integer>();
			parseGram();
		}
		
		public void set (String gramText)
		{
			orig = gramText;
			leftovers = null;
			flags = new HashSet<String> ();
			paradigm = new HashSet<Integer>();
			parseGram();
		}
		
		public boolean hasParadigm()
		{
			return paradigm.isEmpty();
		}
		
		/**
		 * Only works correctly, if cleanupLeftovers is used, when needed.
		 */
		public boolean hasUnparsedGram()
		{
			//cleanupLeftovers();		// What is better - unexpected side effects or not working, when used incorrectly?
			return leftovers.isEmpty();
		}
		
		private void parseGram()
		{
			String correctedGram = correctOCRErrors(orig);
			String[] subGrams = correctedGram.split("\\s*;\\s*");
			leftovers = new LinkedList<LinkedList<String>> ();
			
			// First process ending patterns, usually located in the beginning
			// of the grammar string.
			// TODO
			
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
						flags.add(knownAbbr.get(gramElem));
					else if (!gramElem.equals(""))
						toDo.add(gramElem);	
				}
				
				// TODO: magical patterns for processing endings.
				
				leftovers.add(toDo);
			}
			
			// TODO: get paradigm from flags.
			
			cleanupLeftovers();
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
		 * Checks if gramText contains the target, taking into account
		 * delimiters.
		 * @param gramText - where to search
		 * @param target - what to search.
		 * @return - true if target found.
		 */
/*		private static boolean gramContains(String gramText, String target)
		{
			if (gramText==null || gramText.length() < 2) return false;
			//TODO - regexp varētu te būt ātrāki, ja nu ātrdarbība kļūst par sāpi
			return gramText.trim().equalsIgnoreCase(target)
				|| gramText.endsWith(", " + target)
				|| gramText.endsWith("; " + target)
				|| gramText.endsWith(": " + target)
				|| gramText.startsWith(target + ", ")
				|| gramText.startsWith(target + "; ")
				|| gramText.contains(", " + target + ",")
				|| gramText.contains(", " + target + ";")
				|| gramText.contains("; " + target + ",")
				|| gramText.contains("; " + target + ";");
		}*/
		
		/**
		 * Removes the target from gramText to consider it as processed, taking
		 * into account delimiters.
		 * @param gramText - where to remove
		 * @param target - what to remove
		 * @return cleaned gramText.
		 */
/*		private String removeFromGram(String gramText, String target)
		{
			if (gramText == null || gramText.trim().equals("")) return gramText;
			if (gramText.trim().equals(target))
				gramText="";
			else if (gramText.endsWith(", " + target)
					|| gramText.endsWith("; " + target)
					|| gramText.endsWith(": " + target))
				gramText = gramText.substring(0, gramText.length() - target.length() - 2);
			else if (gramText.startsWith(target + ", ")
					|| gramText.startsWith(target + "; "))
				gramText = gramText.substring(target.length() + 2);
			else if (gramText.contains(", " + target + ","))
				gramText = gramText.replace(", " + target + ",", ",");
			else if (gramText.contains(", " + target + ";"))
				gramText = gramText.replace(", " + target + ";", ";");
			else if (gramText.contains("; " + target + ";"))
				gramText = gramText.replace("; " + target + ";" , ";");
			else if (gramText.contains("; " + target + ","))
				gramText = gramText.replace("; " + target + "," , ";");
			return gramText.trim();
		}*/

		
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
				res.append("\"Original\":");
				res.append(JSONObject.escape(orig));
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
		
		public Definition def;
		
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
			def = null;
			examples = null;
			subsenses = null;
		}
		
		public Sense (Node nNode)
		{
			NodeList fields = nNode.getChildNodes(); 
			for (int i = 0; i < fields.getLength(); i++)
			{
				Node field = fields.item(i);
				String fieldname = field.getNodeName();
				if (fieldname.equals("gram"))
					grammar = new Gram (field);
				else if (fieldname.equals("d"))
				{
					NodeList defFields = field.getChildNodes();
					for (int j = 0; j < defFields.getLength(); j++)
					{
						Node defField = defFields.item(j);
						String defFieldname = defField.getNodeName();
						if (defFieldname.equals("t"))
						{
							if (def != null)
								System.err.println("d entry contains more than one \'t\'");
							def = new Definition (defField);
						}
						else if (!defFieldname.equals("#text")) // Text nodes here are ignored.
							System.err.printf("d entry field %s not processed\n", defFieldname);
					}
				}
				else if (fieldname.equals("g_piem"))
					examples = Utils.loadPhrases(field, "piem");
				else if (fieldname.equals("g_an"))
					subsenses = Utils.loadSenses(field);
				else if (!fieldname.equals("#text")) // Text nodes here are ignored.
					System.err.printf("n entry field %s not processed\n", fieldname);
			}
		}
		
		/**
		 * Not sure if this is the best way to treat paradigms.
		 * Currently only grammar paradigm is considered.
		 */
		public boolean hasParadigm()
		{
			if (grammar == null) return false;
			if (grammar.hasParadigm()) return true;
			//for (Phrase e : examples)
			//{
			//	if (e.hasParadigm()) return true;
			//}
			//for (Sense s : subsenses)
			//{
			//	if (s.hasParadigm()) return true;
			//}
			return false;
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
			
			if (grammar != null)
			{
				if (hasPrev) res.append(", ");
				res.append(grammar.toJSON());
				hasPrev = true;
			}
			
			if (def != null)
			{
				if (hasPrev) res.append(", ");
				res.append(def.toJSON());
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
	public static class Definition implements HasToJSON
	{
		/**
		 * t (teksts) field.
		 */
		public String text = null;
		
		public Definition (Node dNode)
		{
			text = dNode.getTextContent();
		}
		
		public String toJSON()
		{
			return String.format("\"Definition\":\"%s\"", JSONObject.escape(text));			
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

		public Phrase (Node piemNode)
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
					grammar = new Gram (field);
				else if (fieldname.equals("n"))
				{
					if (subsenses == null) subsenses = new LinkedList<Sense>();
					subsenses.add(new Sense (field));
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

	/**
	 * Do the one-time initialization of lookup data.
	 */
/*	private void initStatics() {
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
		
/*		gramFlags = new HashMap<String,String>();
		
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
	}//*/
	
/*	private void setGram(String textContent) {
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
	}//*/
	
	private static class Utils
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
				res.append(JSONObject.escape(i.next().toString()));
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
		 */
		public static LinkedList<Sense> loadSenses(Node allSenses)
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
					res.add(new Sense(sense));
				else if (!sense.getNodeName().equals("#text")) // Text nodes here are ignored.
					System.err.printf(
						"%s entry field %s not processed, expected only 'n'.\n",
						allSenses.getNodeName(), sense.getNodeName());
			}
			return res;
		}
		
		/**
		 *  Load contents of g_fraz or g_piem field into LinkedList.
		 */
		public static LinkedList<Phrase> loadPhrases(
				Node allPhrases, String subElemName)
		{
			LinkedList<Phrase> res = new LinkedList<Phrase>();
			NodeList phraseNodes = allPhrases.getChildNodes(); 
			for (int i = 0; i < phraseNodes.getLength(); i++)
			{
				Node phrase = phraseNodes.item(i);
				if (phrase.getNodeName().equals(subElemName))
					res.add(new Phrase(phrase));
				else if (!phrase.getNodeName().equals("#text")) // Text nodes here are ignored.
					System.err.printf(
						"%s entry field %s not processed, expected only '%s'.\n",
						allPhrases.getNodeName(), phrase.getNodeName(), subElemName);
			}
			return res;
		}
	}
	
	public static interface HasToJSON
	{
		public String toJSON();
	}
}
