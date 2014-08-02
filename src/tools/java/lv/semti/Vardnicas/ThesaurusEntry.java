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
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;
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
		
		
		public void addToLexicon(Analyzer analizators, String importSource, Sources sources) {
			if (this.gram != null && this.gram.flags.contains("Apvidvārds"))
				return; // Apvidvārdus neieliekam 'labajā' leksikonā
			String lemma = this.lemma.text;
			
			try {								
				Word w = analizators.analyzeLemma(lemma);
				if (w.isRecognized()) 
					return; //throw new Exception(String.format("Vārds %s jau ir leksikonā", lemma));

				if (this.gram == null) throw new Exception(String.format("Vārdam %s nav gramatikas", lemma));
				if (this.gram.paradigm == null) throw new Exception(String.format("Vārdam %s nav atrastas paradigmas", lemma));
				HashSet<Integer> paradigms = this.gram.paradigm;
				if (paradigms.size() != 1) throw new Exception(String.format("Vārdam %s ir %d paradigmas", lemma, paradigms.size()));
				int paradigmID = paradigms.iterator().next();
				if (paradigmID == 0) // 0 ir tās, kuras nav saprastas 
					throw new Exception(String.format("Vārdam %s 0 paradigma - nesaprasts", lemma));
				
				Lexeme l = analizators.createLexemeFromParadigm(lemma, paradigmID, importSource);
				if (l == null) throw new Exception(String.format("createLexemeFromParadigm nofailoja uz %s / %d", lemma, paradigmID));
				if (this.gram != null) 
					l.addAttributes(this.gram.describeFlags());
				if (sources != null) 
					l.addAttribute("Pirmavots", sources.describeSources());
				
				if (lemma.equalsIgnoreCase("pirmāks")) 
					throw new Exception("'pirmāks' - īpaši slikts vārds");
				
				if (l.getParadigmID() == 29) { // Hardcoded unflexible words
					l.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
					if (this.gram.flags.contains("Saīsinājums"))
						l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Abbreviation);
					else if (this.gram.flags.contains("Vārds svešvalodā"))
						l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Foreign); 
					else if (this.gram.flags.contains("Izsauksmes vārds"))
						l.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Interjection); 
					
				}
				//System.out.printf("Jess %s\n", lemma);
			} catch (Exception e) {
				System.err.printf("Nesanāca ielikt leksēmu :( %s\n",e.getMessage());
				if (e.getMessage() == null) e.printStackTrace();
			}
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
			res.append(String.format("\"Lemma\":\"%s\"", JSONObject.escape(text)));
			if (pronunciation != null)
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
			res.put("priev. ar ģen.", "Lieto ar ģenitīvu");
			res.put("ar ģen.", "Prievārds"); // It seems that without additional comments this is used for prepositions only
			res.put("ar ģen.", "Lieto ar ģenitīvu");			
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
			res.put("parasti saliktajos laikos", "Parasti saliktajos laikos");
			res.put("parasti saliktajos laikos.", "Parasti saliktajos laikos");
			res.put("subst. noz.", "Lietvārda nozīmē");
			res.put("lietv. nozīmē.", "Lietvārda nozīmē");
			res.put("īp. nozīmē.", "Īpašības vārda nozīmē");
			res.put("ar not. gal.", "Ar noteikto galotni");
			res.put("dial. (augšzemnieku)", "Agušzemnieku dialekts");	// Unique.
			res.put("pareti.", "Pareti");
			res.put("pareti", "Pareti");
			res.put("reti.", "Reti");
			res.put("reti", "Reti");
			res.put("retāk", "Retāk");
			
			return res;
		}
		
		/* Semantic groups of flags, to be translated as descriptive fields */
		
		// Redundant - duplicates the better structured information in morphology data 
		public static Set<String> redundant = new HashSet<>(Arrays.asList("Vārds svešvalodā", "Saīsinājums", "Apstākļa vārds", "Prievārds", "Partikula", "Darbības vārds", "Īpašības vārds", "Lietvārds", "Izsauksmes vārds", "Vienskaitlis", "Daudzskaitlis", "Vīriešu dzimte", "Sieviešu dzimte", "Refleksīvs", "Šķirkļavārds daudzskaitlī", "Šķirkļavārds vienskaitlī"));
		
		// Foreign language names
		public static Set<String> languages = new HashSet<>(Arrays.asList("Arābu","Latīņu","Franču","Grieķu","Sengrieķu","Itāliešu"));
		
		// Domains / topics
		public static Set<String> domains = new HashSet<>(Arrays.asList(
				"Aeronautika","Anatomija","Arheoloģija","Arhitektūra","Astronomija","Aviācija","Bioloģija","Biškopība","Botānika","Būvniecība",
				"Ekonomika","Ekoloģija","Ekonomika","Elektrotehnika","Etnogrāfija","Farmakoloģija","Filozofija","Finanses","Fizika","Fizioloģija",
				"Fiziskā kultūra un sports","Folklora","Ģenētika","Ģeodēzija","Ģeogrāfija","Ģeoloģija","Ģeometrija","Grāmatvedība","Hidroloģija",
				"Hidrotehnika","Informātika","Jurisprudence","Jūrniecība","Attiecas uz kapitālistisko iekārtu, kapitālistisko sabiedrību","Kardioloģija",
				"Kartogrāfija","Kibernētika","Kokapstrāde","Kulinārija","Ķīmija","Lauksaimniecība","Lauksaimniecības tehnika","Literatūrzinātne",
				"Loģika","Lopkopība","Matemātika","Matemātika","Medicīna","Medniecība","Meteoroloģija","Metalurģija","Metālapstrāde","Meteoroloģija",
				"Mežniecība","Mežrūpniecība","Mežsaimniecība","Militārās zinātnes","Mineraloģija","Mitoloģija","Mūzika","Oftalmoloģija","Ornitoloģija",
				"Politika","Poligrāfija","Psiholoģija","Reliģija","Socioloģija","Socioloģija","Tehnika","Tehnoloģija","Telekomunikācijas",
				"Tekstilrūpniecība","Tekstilrūpniecība","Valodniecība","Veterinārija","Zooloģija"));
		
		// Miscellaneous notes
		public static Set<String> notes = new HashSet<>(Arrays.asList("Parasti vienskaitlī", "Tikai vienskaitlī", "Parasti daudzskaitlī", "Tikai daudzskaitlī", "Parasti 3. personā", "Lieto ar datīvu", "Parasti saliktajos laikos", "Lieto ar ģenitīvu"));
		
		// Style / usage
		public static Set<String> style = new HashSet<>(Arrays.asList("Nievīga ekspresīvā nokrāsa", "Poētiska stilistiskā nokrāsa", "Vienkāršrunas stilistiskā nokrāsa", "Ironiska ekspresīvā nokrāsa", "Humoristiska ekspresīvā nokrāsa"));
		public static Set<String> usage = new HashSet<>(Arrays.asList("Sarunvaloda", "Žargonvārds", "Vulgārisms", "Novecojis", "Vēsturisks", "Nevēlams"));
		public static Set<String> frequency = new HashSet<>(Arrays.asList("Pareti", "Reti", "Retāk", "Neaktuāls"));
		
		public static Set<String> proper = new HashSet<>(Arrays.asList("Vietvārds"));
		
		public HashMap<String, String> describeFlags() {
			HashMap<String, Set<String>> mappings = new HashMap<>();
			mappings.put("Valoda",languages);
			mappings.put("Nozare",domains);
			mappings.put("Piezīmes",notes);
			mappings.put("Stils",style);
			mappings.put("Lietojums",usage);
			mappings.put("Biežums",frequency);
			mappings.put("Īpašvārda veids",proper);
			
			HashMap<String, String> description = new HashMap<>();
			flagloop: for (String flag : this.flags) {
				if (redundant.contains(flag)) continue;
				if (flag.equalsIgnoreCase("Pārejošs") || flag.equalsIgnoreCase("Nepārejošs")) { 
					description.put(AttributeNames.i_Transitivity, flag);
					continue;
				}
				
				if (flag.startsWith("Locīt kā")) { 
					description.put("Locīšana", flag);
					continue;
				}

				
				for (Entry<String, Set<String>> e : mappings.entrySet()) {
					if (e.getValue().contains(flag)) {   // ja flags ir šajā vārdu sarakstā tad...
						if (description.get(e.getKey()) != null) // paskatamies vai neatkārtojas description key vērtība
							System.err.printf("Dubultojas flagi: %s un %s\n", flag, description.get(e.getKey()));
						description.put(e.getKey(), flag); // un ieliekam to flagu iekš description ar attiecīgo atslēgu
						continue flagloop;
					}
				}
				System.out.printf("Nesaprasts flags '%s'\n", flag);
			}
			return description;
		}

		/**
		 * Patterns for identifying (true, explicitly listed) grammatical information.
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
				else if (gramText.startsWith("-as, s.")) //aberācija, milns, najādas
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
					else if (lemma.matches(".*[^aeiouāēīōū]as"))
					{
						paradigm.add(7);
						flags.add("Šķirkļavārds daudzskaitlī");
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
			else if (gramText.startsWith("-es, dsk. ģen. -pju, s.")) //aitkope, tūsklapes
			{
				newBegin = "-es, dsk. ģen. -pju, s.".length();
				if (lemma.endsWith("pe"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
				}
				else if (lemma.endsWith("pes"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
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
				else if (lemma.endsWith("ne"))
				{
					paradigm.add(9);
					flags.add("Lietvārds");
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
		 * Paradigm 4: Lietvārds 2. deklinācija -s (nom. == ģen.)
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
			// Paradigms: 1, 2, 3 (if no sound changes), 1-5 (if plural)
			else if (gramText.startsWith("-a, v.")) // abats, akustiķis, sparguļi, skostiņi
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
				else if (lemma.matches(".*[ņ]i"))
				{
					paradigm.add(1);
					paradigm.add(2);
					paradigm.add(3);
					paradigm.add(4);
					paradigm.add(5);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Neviennozīmīga paradigma");
				}
				else if (lemma.matches(".*[ļ]i"))
				{
					paradigm.add(1);
					paradigm.add(2);
					paradigm.add(3);
					paradigm.add(5);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Neviennozīmīga paradigma");
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
			newBegin = simpleRule(
					"parasti 3. pers., -šalc, pag. -šalca", "šalkt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"šalkt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizšalkt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -tūkst, pag. -tūka", "tūkt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"tūkt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aiztūkt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -aug, pag. -auga", "augt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"augt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizaugt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -aust, pag. -ausa", "aust", 15,
					new String[] {"Darbības vārds", "Locīt kā \"aust\" (kā gaisma)"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizaust 1
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -birst, pag. -bira", "birt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"birt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbirt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -brūk, pag. -bruka", "brukt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"brukt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizbrukt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -deg, pag. -dega", "degt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"degt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdegt 2
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dim, pag. -dima", "dimt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dimt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdimt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dip, pag. -dipa", "dipt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dipt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdipt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dūc, pag. -dūca", "dūkt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dūkt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdūkt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dzeļ, pag. -dzēla", "dzelt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dzelt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdzelt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dzīst, pag. -dzija", "dzīt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dzīt\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdzīt 2
			
			if (newBegin == -1) newBegin = simpleRule(
					"-eju, -ej, -iet, pag. -gāju", "iet", 15,
					new String[] {"Darbības vārds", "Locīt kā \"iet\""},
					null, gramText, lemma); //apiet
			if (newBegin == -1) newBegin = simpleRule(
					"-tupstu, -tupsti, -tupst, pag. -tupu", "tupt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"tupt\""},
					null, gramText, lemma); //aiztupt
					// TODO tupu/tupstu
			if (newBegin == -1) newBegin = simpleRule(
					"-griežu, -griez, -griež, pag. -griezu", "griezt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"griezt\""},
					null, gramText, lemma); //apgriezt
			if (newBegin == -1) newBegin = simpleRule(
					"-ģiedu, -ģied, -ģied, pag. -gidu", "ģist", 15,
					new String[] {"Darbības vārds", "Locīt kā \"ģist\""},
					null, gramText, lemma); //apģist
			if (newBegin == -1) newBegin = simpleRule(
					"-klāju, -klāj, -klāj, pag. -klāju", "klāt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"klāt\""},
					null, gramText, lemma); //apklāt
			if (newBegin == -1) newBegin = simpleRule(
					"-kauju, -kauj, -kauj, pag. -kāvu", "kaut", 15,
					new String[] {"Darbības vārds", "Locīt kā \"kaut\""},
					null, gramText, lemma); //apkaut
			if (newBegin == -1) newBegin = simpleRule(
					"-aru, -ar, -ar, pag. -aru", "art", 15,
					new String[] {"Darbības vārds", "Locīt kā \"art\""},
					null, gramText, lemma); //aizart
			if (newBegin == -1) newBegin = simpleRule(
					"-aužu, -aud, -auž, pag. -audu", "aust", 15,
					new String[] {"Darbības vārds", "Locīt kā \"aust\" (kā zirneklis)"},
					null, gramText, lemma); //aizaust 2
			if (newBegin == -1) newBegin = simpleRule(
					"-bāžu, -bāz, -bāž, pag. -bāzu", "bāzt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"bāzt\""},
					null, gramText, lemma); //aizbāzt
			if (newBegin == -1) newBegin = simpleRule(
					"-bēgu, -bēdz, -bēg, pag. -bēgu", "bēgt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"bēgt\""},
					null, gramText, lemma); //aizbēgt
			if (newBegin == -1) newBegin = simpleRule(
					"-beru, -ber, -ber, pag. -bēru", "bērt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"bērt\""},
					null, gramText, lemma); //aizbērt
			if (newBegin == -1) newBegin = simpleRule(
					"-bilstu, -bilsti, -bilst, pag. -bildu", "bilst", 15,
					new String[] {"Darbības vārds", "Locīt kā \"bilst\""},
					null, gramText, lemma); //aizbilst
			if (newBegin == -1) newBegin = simpleRule(
					"-braucu, -brauc, -brauc, pag. -braucu", "braukt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"braukt\""},
					null, gramText, lemma); //aizbraukt
			if (newBegin == -1) newBegin = simpleRule(
					"-brāžu, -brāz, -brāž, pag. -brāzu", "brāzt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"brāzt\""},
					null, gramText, lemma); //aizbrāzt
			if (newBegin == -1) newBegin = simpleRule(
					"-brienu, -brien, -brien, pag. -bridu", "brist", 15,
					new String[] {"Darbības vārds", "Locīt kā \"brist\""},
					null, gramText, lemma); //aizbrist
			if (newBegin == -1) newBegin = simpleRule(
					"-ceļu, -cel, -ceļ, pag. -cēlu", "celt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"celt\""},
					null, gramText, lemma); //aizcelt
			if (newBegin == -1) newBegin = simpleRule(
					"-cērtu, -cērt, -cērt, pag. -cirtu", "cirst", 15,
					new String[] {"Darbības vārds", "Locīt kā \"cirst\""},
					null, gramText, lemma); //aizcirst
			if (newBegin == -1) newBegin = simpleRule(
					"-dabūju, -dabū, -dabū, pag. -dabūju", "dabūt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dabūt\""},
					null, gramText, lemma); //aizdabūt
			if (newBegin == -1) newBegin = simpleRule(
					"-dedzu, -dedz, -dedz, pag. -dedzu", "degt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"degt\""},
					null, gramText, lemma); //aizdegt 1
			if (newBegin == -1) newBegin = simpleRule(
					"-diebju, -dieb, -diebj, pag. -diebu", "diebt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"diebt\""},
					null, gramText, lemma); //aizdiebt
			if (newBegin == -1) newBegin = simpleRule(
					"-diedzu, -diedz, -diedz, pag. -diedzu", "diegt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"diegt\""},
					null, gramText, lemma); //aizdiegt 1
			if (newBegin == -1) newBegin = simpleRule(
					"-dodu, -dod, -dod, pag. -devu", "dot", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dot\""},
					null, gramText, lemma); //aizdot
			if (newBegin == -1) newBegin = simpleRule(
					"-drāžu, -drāz, -drāž, pag. -drāzu", "drāzt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"drāzt\""},
					null, gramText, lemma); //aizdrāzt
			if (newBegin == -1) newBegin = simpleRule(
					"-duru, -dur, -dur, pag. -dūru", "durt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"durt\""},
					null, gramText, lemma); //aizdurt
			if (newBegin == -1) newBegin = simpleRule(
					"-dzeru, -dzer, -dzer, pag. -dzēru", "dzert", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dzert\""},
					null, gramText, lemma); //aizdzert
			if (newBegin == -1) newBegin = simpleRule(
					"-dzenu, -dzen, -dzen, pag. -dzinu", "dzīt", 15,
					new String[] {"Darbības vārds", "Locīt kā \"dzīt\""},
					null, gramText, lemma); //aizdzīt 1
			if (newBegin == -1) newBegin = simpleRule(
					"-ēdu, -ēd, -ēd, pag. -ēdu", "ēst", 15,
					new String[] {"Darbības vārds", "Locīt kā \"ēst\""},
					null, gramText, lemma); //aizēst
			
			if (newBegin == -1) newBegin = simpleRule(
					"-tveru, -tver, -tver, pag. -tvēru", "tvert", 15,
					new String[] {"Darbības vārds", "Locīt kā \"tvert\""},
					null, gramText, lemma); //aiztvert
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
			
			newBegin = simpleRule(
					"parasti 3. pers., -o, pag. -oja", "ot", 16,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizšalkot, aizbangot
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -ē, pag. -ēja", "ēt", 16,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //adsorbēt
			if (newBegin == -1) newBegin = simpleRule(
					"-oju, -o, -o, -ojam, -ojat, pag. -oju; -ojām, -ojāt; pav. -o, -ojiet",
					"ot", 16,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //acot
			if (newBegin == -1) newBegin = simpleRule(
					"-ēju, -ē, -ē, -ējam, -ējat, pag. -ēju, -ējām, -ējāt; pav. -ē, -ējiet",
					"ēt", 16,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //adverbializēt
			if (newBegin == -1) newBegin = simpleRule(
					"-ēju, -ē, -ē, pag. -ēju", "ēt", 16,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //absolutizēt
			if (newBegin == -1) newBegin = simpleRule(
					"-oju, -o, -o, pag. -oju", "ot", 16,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //aiztuntuļot
			if (newBegin == -1) newBegin = simpleRule(
					"-āju, -ā, -ā, pag. -āju", "āt", 16,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //aijāt
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
			newBegin = simpleRule(
					"parasti 3. pers., -blākš, pag. -blākšēja", "blākšēt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizblākšēt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -blākšķ, pag. -blākšķēja", "blākšķēt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizblākšķēt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -čab, pag. -čabēja", "čabēt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizčabēt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -čaukst, pag. -čaukstēja", "čaukstēt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizčaukstēt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dārd, pag. -dārdēja", "dārdēt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdārdēt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dimd, pag. -dimdēja", "dimdēt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdimdēt
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dip, pag. -dipēja", "dipēt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdipēt
			
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -ina, pag. -ināja", "ināt", 17,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizducināt
			
			
			if (newBegin == -1) newBegin = simpleRule(
					"-turu, -turi, -tur, pag. -turēju", "turēt", 17,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //aizturēt
			if (newBegin == -1) newBegin = simpleRule(
					"-u, -i, -a, pag. -īju", "īt", 17,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //aizsūtīt
			if (newBegin == -1) newBegin = simpleRule(
					"-inu, -ini, -ina, pag. -ināju", "ināt", 17,
					new String[] {"Darbības vārds"},
					null, gramText, lemma); //aizsvilināt
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
					"parasti 3. pers., -dūcas, pag. -dūcās", "dūkties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"dūkties\""},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdūkties
			
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
			if (newBegin == -1) newBegin = simpleRule(
					"-degos, -dedzies, -degas, pag. -degos", "degties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"degties\""},
					null, gramText, lemma); //aizdegties
			if (newBegin == -1) newBegin = simpleRule(
					"-drāžos, -drāzies, -drāžas, pag. -drāzos", "drāzties", 18,
					new String[] {"Darbības vārds", "Locīt kā \"drāzties\""},
					null, gramText, lemma); //aizdrāzties
			
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
			if (newBegin == -1) newBegin = simpleRule(
					"-ājos, -ājies, -ājas, pag. -ājos", "āties", 19,
					new String[] {"Darbības vārds"}, null,
					gramText, lemma); //aizdomāties
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
					"parasti 3. pers., -čabas, pag. -čabējās", "čabēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizčabēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -čaukstas, pag. -čaukstējās",
					"čaukstēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizčaukstēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -dārdas, pag. -dārdējās", "dārdēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdārdēties
			if (newBegin == -1) newBegin = simpleRule(
					"parasti 3. pers., -drebas, pag. -drebējās", "drebēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizdrebēties
			
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
					"parasti 3. pers., -as, pag. -ējās", "ēties", 20,
					new String[] {"Darbības vārds"},
					new String[] {"Parasti 3. personā"},
					gramText, lemma); //aizčiepstēties
			
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
							res.append("{");
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
		
		/**
		 * Mapping of source dictionary codes to their full names 
		 */
		public static HashMap<String, String> sourceDictionaries = generateSourceDictionaries();
		private static HashMap<String, String> generateSourceDictionaries()
		{
			HashMap<String, String> res = new HashMap<>();
			res.put("AB1", "Bīlenšteins A. Latviešu koka celtnes. Rīga, Jumava, 2001.");
			res.put("AB2", "Bīlenšteins A. Latviešu koka iedzīves priekšmeti. Rīga, Jumava, 2007.");
			res.put("AkT", "Švinks U. Akrobātikas terminoloģija. 1.-3., Rīga, Latvijas Sporta pedagoģijas akadēmija, 2002.");
			res.put("AmS", "Angļu un metriskās sistēmas mēri un svari un to pārrēķināšanas tabulas. Vestfāles Hallē, T. Dārziņa grāmatu apgāds, 1946.");
			res.put("Ang", "Baldunčiks J. Anglicismi latviešu valodā. Rīga, Zinātne, 1989.");
			res.put("ALV", "Angļu-latviešu vārdnīca. Rīga, Jāņa sēta, 1995.");
			res.put("AL07", "Angļu-latviešu vārdnīca. Rīga, Avots, 2007.");
			res.put("AtL", "Autortiesību likums. Rīga, Latvijas Vēstnesis, 148/150 (2059/2061), 27.04.2000.");
			res.put("AtV", "Arhitektūras terminu vārdnīca. Liepāja, 1998.");
			res.put("AtvL", "Administratīvo teritoriju un apdzīvoto vietu likums. Rīga, Latvijas Vēstnesis, 202 (3986), 30.12.2008., ar grozījumiem 23 (4215), 10.02.2010. un 149 (4341), 21.09.2010.");
			res.put("Aug", "Pētersone A., Birkmane K. Latvijas PSR augu noteicējs. Rīga, Zvaigzne, 1980.");
			res.put("Aug2", "Ēdelmane I., Ozola Ā. Latviešu valodas augu nosaukumi. Rīga, SIA «Augsburgas institūts», 2003.");
			res.put("AV", "Antropoloģijas vārdnīca. http://antropologubiedriba.wikidot.com/antropologijas-termini-latviesu-valoda");
			res.put("BB", "Bušmane B. Piena vārdi. Rīga, LU LVI, 2007.");
			res.put("BFvL", "Likums par budžetu un finanšu vadību. Rīga, Latvijas Vēstnesis, 41(172), 06.04.1994.");
			res.put("BjT", "Bieži lietoti jēdzieni un termini. Rīga, Avots, 2004.");
			res.put("BjV", "Bioloģijas jēdzienu skaidrojoā vārdnīca. Rīga, Mācību apgāds NT, 1997.");
			res.put("BLi", "Blinkena A. Latviešu interpunkcija. Rīga, Zinātne, 1969; Zvaigzne ABC, 2009.");
			res.put("BnK", "Balvu novads. Karte 1:100000. Rīga, Jāņa sēta, 2011.");
			res.put("BsV", "Kavacis A. Baltu senvēsture. Rīga, Vieda, 2011.");
			res.put("BtsV", "Bībeles terminu skaidrojošā vārdnīca. Rīga, Daugava, 2005.");
			res.put("CaF", "Aldersons J. Cilvēka anatomijas, fizioloģijas, higiēnas skaidrojošā vārdnīca. Rīga, Zvaigzne ABC, 2000. // Bioloģija pamatskolai. Cilvēks skaidrojošā vārdnīca (ar interneta atslēgvārdiem). Anatomija. Fizioloģija. Higiena. Rīga, Zvaigzne ABC, 2011.");
			res.put("D", "Laikraksts «Diena». (pirmais skaitlis norāda datumu, otrais - lappusi)");
			res.put("D1", "Angļu–krievu–latviešu skaidrojošā vārdnīca «Datu apstrādes un pārraides sistēmas». Rīga, A/s SWH Informatīvās sistēmas, 1995.");
			res.put("D2", "Personālie datori. Angļu–latviešu–krievu skaidrojošā vārdnīca. Rīga, A/s DATI, 1998.");
			res.put("D3", "Angļu–latviešu–krievu informātikas vārdnīca: datori, datu apstrāde un pārraide. Rīga, Avots, 2001.");
			res.put("D89", "Īsa krievu-latviešu-angļu ražošanas terminu skaidrojošā vārdnīca «Informātika». Rīga, Zvaigzne, 1989.");
			res.put("DĒ", "Dienas Ēdieni. Rīga, Dienas žurnāli, 2008.-2012. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("delf", "www.delfi.lv ( - publicēšanas datums).");
			res.put("DeW", "Votsa L. Dabas enciklopēdija. Rīga, Zvaigzne ABC, 1996.");
			res.put("DĢ", "Ancāne I. Dabas ģeogrāfija, skaidrojošā vārdnīca. Rīga, Zvaigzne ABC, 2000.");
			res.put("DtsV", "Diagnostikas terminu skaidrojošā vārdnīca tiem, kas rūpējas par bērniem slimnīcā. Rīga, VSIA «Bērnu klīniskā universitātes slimnīca», 2008.");
			res.put("Dz", "Latvijas PSR dzīvnieku noteicējs. 1.–2. Rīga, LVI, 1956.");
			res.put("DzS", "Džoiss Dž. Uliss. Tulkojis Dzintars Sodums. Rīga, 2012.");
			res.put("DzV", "Kursīte J. Dzejas vārdnīca. Rīga, Zinātne, 2002.");
			res.put("DžP", "Džūkstes pasakas. Rīga, Zinātne, 1980.");
			res.put("EBA", "Encyclopaedia Britannica Almanac 2003. London, 2002.");
			res.put("EbV", "Skaidrojošā vārdnīca ekonomikas bakalauram. Rīga, SolVita, 1994.");
			res.put("EepV", "Elektroenerģētikas pamatterminu skaidrojošā vārdnīca. Rīga, Jumava, 1997.");
			res.put("EF", "Ekonomikas un finanšu vārdnīca. Rīga, Norden AB, 2003.");
			res.put("EH", "Endzelīns J., Hauzenberga E. Papildinājumi un labojumi K. Mīlenbaha Latviešu valodas vārdnīcai. Rīga, Kultūras fonds; VAPP, 1934.–1946.");
			res.put("ĒiV", "Kagaine E., Raģe S. Ērģemes izloksnes vārdnīca. 1.-3. Rīga, Zinātne, 1977.-1983.");
			res.put("EpV", "Eponīmu vārdnīca. Sast. B. Bankava, Rīga, Madonas poligrāfists, 2007. Jaunā eponīmu vārdnīca. Precizēts un papildināts izdevums. Sast. A. Bankava, Rīga, Latviešu valodas aģentūra, 2012.");
			res.put("EStV", "Eiropas Savienības terminu vārdnīca. Rīga, UNDP, 2004.");
			res.put("EsV", "Ekonomikas skaidrojošā vārdnīca. Rīga, Zinātne, 2000.");
			res.put("EV", "Enciklopēdiskā vārdnīca 2 sējumos. Rīga, Latvijas Enciklopēdiju redakcija, 1991.");
			res.put("FA", "Filozofijas atlants. Rīga, Zvaigzne ABC, 2000.");
			res.put("FF", "Rolovs B. Par fiziku un fiziķiem. Rīga, Zinātne, 1989.");
			res.put("FV", "Ligers Z. Vāciski-latviska un latviski-vāciska karavīra vārdnīca lietošanai frontē. 3. iespiedums, Aufbau-Verlag, Rīga, 1944.");
			res.put("Geo", "GEO. Rīga, SIA Aģentūra Lilita, 2008.-2014. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("GT", "Garīgo terminu skaidrojošā vārdnīca. Rīga, Zvaigzne ABC, 2002.");
			res.put("GtV", "Grāmatvedības terminu vārdnīca. Rīga, Latvijas Universitāte, 1998.");
			res.put("GrV", "Grāmatvedības jēdzienu skaidrojošā vārdnīca. Rīga, Avots, 2005.");
			res.put("GV", "Latviešu, vācu un krievu grāmatrūpniecības vārdnīca. Rīga, Latvju Grāmata, 1942.");
			res.put("ĢmV", "Ģeomātikas terminu skaidrojošā vārdnīca. Rīga, RTU Izdevniecība, 2009.");
			res.put("ĢT", "Ģenētikas terminu skaidrojošā vārdnīca. Rīga, Galvenā enciklopēdiju redakcija, 1981.");
			res.put("HeL", "Lancmanis I. Heraldika. Rīga, Neputns, 2007.");
			res.put("i1", "Latviešu valodas saīsinājumu vārdnīca. Rīga, Avots, 1994.");
			res.put("i2", "Latviešu valodas saīsinājumu vārdnīca. Rīga, Avots, 2003.");
			res.put("IdV", "Ideju vārdnīca. Rīga, Zvaigzne ABC, 1999.");
			res.put("IfV", "Oksleids K., Stoklia K., Vertheima Dž. Ilustrētā fizikas vārdnīca. Rīga, Zvaigzne ABC, 1997.");
			res.put("Ig", "Abens K. Igauņu-latviešu vārdnīca. Rīga, Liesma, 1967.");
			res.put("IL", "Izglītības likums. Rīga, Latvijas Vēstnesis, 343/344 (1404/1405), 29.10.1998.");
			res.put("IPV", "Ilustrētā Pasaules Vēsture. Rīga, Dienas žurnāli, 2008.-2013. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("Ir", "Ir. Nedēļas žurnāls. Rīga, a/s Cits medijs, 2010.-2014. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("īsz", "Īsziņu vārdnīca. Rīga, Avots, 2002.");
			res.put("Itk", "LZA Terminoloģijas komisijas Informātikas terminoloģijas apakškomisijas protokols. (skaitlis norāda protokola numuru)");
			res.put("ItsV", "Broks A., Buligina I., Koķe T., Špona A., Šūmane M., Upmane M. Izglītības terminu skaidrojošā vārdnīca. Rīga, 1988.");
			res.put("IuB", "Iepirkumu uzraudzības biroja sludinājumi interneta vietnē http://www.iub.gov.lv");
			res.put("IZ", "Ilustrētā Zinātne. Rīga, Dienas žurnāli, 2005.-2014. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("J03", "Jansone I. Galvas segas un plecu segas: lingvistiskais apskats latviešu valodā. Rīga, LU LVI, 2003.");
			res.put("JkV", "Johansons A. Latvijas kultūras vēsture, 1710-1800. Rīga, Jumava, 2011.");
			res.put("JLA1", "Jaunā latviešu–angļu vārdnīca. Red. Veisbergs A., Rīga, SIA «EKLV», 2001.");
			res.put("JLA", "Veisbergs A. Jaunā latviešu–angļu vārdnīca. Rīga, Zvaigzne ABC, 2005.");
			res.put("JlV", "Šlāpins I., Jauno latviešu valoda. Rīga, Ascendum, 2013.");
			res.put("JnK", "Jēkabpils pilsēta, Aknīstes, Jēkabpils, Krustpils, Salas un Viesītes novads. Karte 1:100000. Rīga, Jāņa sēta, 2010.");
			res.put("Jt1", "Juridiski terminoloģiskā skaidrojošā vārdnīca. Sast. Jakubaņecs V., Rīga, P&K, 2005.");
			res.put("JtU", "Juridisko terminu vārdnīca uzņēmējdarbībai. Rīga, Ekonomisko reformu institūts, 1997.");
			res.put("JtV", "Latviešu-angļu, angļu-latviešu juridisko terminu vārdnīca. Rīga, Multineo, 2009.");
			res.put("JūV", "Jūrniecības terminu skaidrojošā vārdnīca. Rīga, Latvijas Jūras akadēmija, 2001.");
			res.put("KiA", "Balode S. Kalncempju pagasta Kalnamuižas daļas izloksnes apraksts. Rīga, LU LVI, 2000.");
			res.put("KiV", "Reķēna A. Kalupes izloksnes vārdnīca. 1.-2. Rīga, LU LVI, 1998.");
			res.put("KKK", "Kas, kur, kad: populārākie ikdienas notikumi, cilvēki un fakti Latvijā un pasaulē. Rīga, Jumava, 2006.");
			res.put("KLV", "Krievu-latviešu vārdnīca. 1.-2., Rīga, LVI, 1959; Avots, 2006.");
			res.put("Kmj", "Zanders O. Ko Kurzemes meži un jūra šalc. Jumava, 2012.");
			res.put("KnG", "Balode S. Kalnienas grāmata. Rīga, LU LVI, 2008.");
			res.put("KrK", "Krāslavas rajons. Karte 1:100000. Rīga, Jāņa sēta, 2006.");
			res.put("KrV", "Latviešu–krievu–vācu vārdnīca izdota no Tautas Apgaismošanas Ministerijas. Sast. Valdemārs Kr., Maskava, 1879.");
			res.put("KsV", "Klauss A. Kontrolings. A-Z skaidrojošā vārdnīca. Rīga, Preses nams, 2000.");
			res.put("KtJ", "Judins A. Krimināltiesību terminu skaidrojošā vārdnīca. Rīga, RaKa, 1999.");
			res.put("KtMS", "Meikališa Ā., Strada K. Kriminālprocesuālo terminu skaidrojošā vārdnīca. Rīga, RaKa, 2000.");
			res.put("KuL", "Vīķe-Freiberga V. Kultūra un latvietība. Rīga, Karogs, 2010.");
			res.put("KV", "Latviešu Konversācijas vārdnīca. 1.–21. Rīga, A. Gulbja apgāds, 1927.–1940.; papildināts faksimilizdevums 1.–22. Rīga, 2000.–2004.");
			res.put("KW", "Konversācijas vārdnīca. 1.–4. Rīga, RLB, 1906.–1921.");
			res.put("KW1", "Dravnieks J. Konversācijas vārdnīca. Pirmais sējums A-I. Jelgava, 1891-1893.");
			res.put("KZ", "Tā runā zonā. Latvijas argo – kriminālvides žargona vārdnīca. Rīga, Valters un Rapa, 2002.");
			res.put("La1", "Lielais Latvijas atlants. Rīga, Karšu izdevniecība Jāņa sēta, 2012.");
			res.put("LaST", "Latvieši latviešu acīm: Sibīrija. Timofejevka. Rakstu krājums. Rīga, LU Literatūras, folkloras un mākslas institūts, 2011.");
			res.put("LatV", "Likumdošanas aktu terminu vārdnīca. Rīga, Senders R, 1999.");
			res.put("LaA", "Mazais Latvijas autoceļu atlants. Rīga, Jāņa sēta, 2011.");
			res.put("LaV", "Latviešu-angļu vārdnīca. Sakārtojuši K. Brants un Dr. phil. V. K. Matiuss. Rediģējis Prof. P. Šmits. Rīga, Izdevis A. Gulbis. 1930.");
			res.put("LC", "Latvijas ciemi. Nosaukumi, ģeogrāfiskais izvietojums. Rīga, Latvijas Ģeotelpiskās informācijas aģentūra, 2007.");
			res.put("LD", "Latvijas daba. Enciklopēdija. 1.–6. Rīga, 1994.–1998.");
			res.put("LDzN", "Latvijas PSR dzīvnieku noteicējs. 1.-2. Rīga, LVI, 1956.-1957.");
			res.put("LdsV", "Lingvodidaktikas terminu skaidrojošā vārdnīca. Latviešu valodas aģentūra, Rīga, 2011.");
			res.put("LE", "Latvju enciklopēdija. Stokholma, Trīs zvaigznes, 1950.");
			res.put("LE1", "Latvijas Enciklopēdija. 1.-5. Rīga, Valērija Belokoņa izdevniecība, 2002.-2009.");
			res.put("Leģ", "Leģendas. Rīga, Dienas žurnāli, 2007.-2014. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("LeK", "Latviešu valodas eksperu komisijas lēmumi. http://www.valoda.lv/Konsultacijas/Latviesu_valodas_ekspertu_komisijas_LVEK_lemumi/836/mid_605 (skaitlis norāda protokola numuru)");
			res.put("LEV", "Lielā enciklopēdiskā vārdnīca. Rīga, Jumava, 2003.");
			res.put("LģE", "Lielā ģeogrāfijas enciklopēdija. Rīga, Jumava, 2008.");
			res.put("LivP", "Latviešu izlokšņu vārdnīca. Prospekts. Rīga, LU Latviešu valodas institūts, 2005.");
			res.put("LL1", "Latīņu-latviešu vārdnīca. Rīga, Zvaigzne, 1994.");
			res.put("LLe", "Lielā Latvijas enciklopēdija. Rīga, Zvaigzne ABC, 2005.");
			res.put("LLkž", "Balkevičius J., Kabelka J. Latviu - lietuviu kalbu žodynas. Vilnius, Mokslas, 1977.");
			res.put("LLL", "Latīņu-latviešu vārdnīca. Red. Lukstiņš G., Rīga, LVI, 1955.");
			res.put("LLV", "Švarcbachs R., Bištēviņš E. Latīņu - latvju vārdnīca. Rīga, Valters un Rapa, 1928.");
			res.put("LLVV", "Latviešu literārās valodas vārdnīca. 1.–8. Rīga, Zinātne, 1972.–1996.");
			res.put("LLx", "Lauksaimniecības leksikons. 1.-3. Rīga, Zelta grauds, 1937.-1939.");
			res.put("LME", "Latvju mazā enciklopēdija. Rīga, Grāmatu Draugs, 1935.");
			res.put("Lo", "Latviešu-krievu vārdnīca. Sastādījis Prof. J. Loja., Maskava, OGIZ, 1946");
			res.put("LP", "Baumanis J., Blūms P. Latvijas putni. Rīga, Liesma, 1969.");
			res.put("LPag", "Latvijas pagasti. Enciklopēdija. 1.-2. Rīga, Preses nams, 2001.-2002. ");
			res.put("LPil", "Latvijas pilsētas. Rīga, Preses nams, 1999. ");
			res.put("LpA", "Lielais pasaules atlants. Rīga, Karšu izdevniecība Jāņa sēta, 2008.");
			res.put("LPE", "Latvijas padomju enciklopēdija. 1.–10/2. Rīga, Galvenā enciklopēdiju redakcija, 1981.–1988.");
			res.put("LpmE", "Latvijas PSR mazā enciklopēdija. 1.-3. Rīga, Zinātne, 1967.–1970.");
			res.put("LvpF", "Muižniece L. Latviešu valodas praktiskā fonoloģija. Rīga, Rasa ABC, 2002.");
			res.put("LvV", "Dravnieks J. Latvju-vācu vārdnīca. Rīga, Valters un Rapa, 1927.");
			res.put("LV06", "Latviešu valodas vārdnīca. Rīga, Avots, 2006.");
			res.put("Lv26", "Latviski-vāciska vārdnīca. Sakārtojis Ed. Ozoliņš, Rīga, A. Gulbja grāmatu spiestuve, 1926.");
			res.put("Lv42", "Latviski-vāciska vārdnīca. Sakārtojis Ed. Ozoliņš, Rīga, A. Gulbja grāmatu apgādniecība, 1942.");
			res.put("LV87", "Latviešu valodas vārdnīca. Rīga, Avots, 1987.");
			res.put("LV93", "Latviešu valodas vārdnīca. Amerikas Latviešu apvienība, 1993.");
			res.put("LxE", "Lauksaimniecības enciklopēdija. 1.-4. Rīga, LVI, 1962.-1971.");
			res.put("MalV", "Markus D., Raipulis J. Radošie malēnieši un viņu valoda. Rīga, Apgāds «Latvijas Zinātņu Akadēmijas Vēstis», 2010.");
			res.put("MC", "Nītiņa D. Moderna cilvēka valoda. Rīga, VVA, 2004.");
			res.put("MD", "Mini Diena. Laikraksta «Diena» pielikums.");
			res.put("ME", "Mīlenbahs K. Latviešu valodas vārdnīca. Rediģējis, papildinājis, turpinājis J. Endzelīns. Rīga, Kultūras fonds, 1923.–1932.");
			res.put("MED", "Macmillan English Dictionary. Oxford, Bloomsbury Publishing Plc, 2002.");
			res.put("MeE", "Mazā erotikas enciklopēdija. Rīga, Jumava, 2001.");
			res.put("MetV", "Pandalons V., Pelēce I. Meteoroloģijas terminu vārdnīca. Jelgava, LLU, 2003.");
			res.put("MfV", "Rolovs B. Mazā fizikas vārdnīca. Rīga, Liesma, 1971.");
			res.put("MIL", "Justs F. Militāro jēdzienu skaidrojošā vārdnīca ar pamatterminu tulkojumu angļu valodā: 15 300 terminu un vārdkopu. Rīga, Avots, 2008.");
			res.put("Mit", "Mitoloģijas vārdnīca. Rīga, Avots, 2004.");
			res.put("MitE", "Mitoloģijas enciklopēdija. 1.-2. Rīga, Latvijas enciklopēdija, 1993.-1994.");
			res.put("MitJ", "Lielā mitoloģijas enciklopēdija. Rīga, Jumava, 2006. ");
			res.put("MiV", "Strautiņa M. Mārcienas izloksne. Rīga, LU Latviešu valodas institūts, 2007.");
			res.put("MkV", "Aldersons J. Mākslas un kultūras vārdnīca ar interneta atslēgvārdiem. Rīga, Zvaigzne ABC, 2011.");
			res.put("ML", "Kārkliņš L. Mūzikas leksikons. Rīga, Zvaigzne, 1990.; RaKa, 2006.");
			res.put("MLVV", "Mūsdienu latviešu valodas vārdnīca. http://www.tezaurs.lv/mlvv, LU Latviešu valodas institūts, 2004.-2014.  ");
			res.put("MsL", "Zemzaris J. Mērs un svars Latvijā 13.-19. gs. Rīga, Zinātne, 1981.");
			res.put("MSV", "Medicīnas svešvārdu vārdnīca. Rīga, Avots, 2007.");
			res.put("MtV", "Blūma D. Mazā mākslas vēstures terminu vārdnīca. Rīga, Zvaigzne ABC, 2005.");
			res.put("MūV", "Torgāns J. Mūzikas terminu vārdenīte. Rīga, Zinātne, 2010.");
			res.put("Mūz", "Mūzikas terminu vārdnīca. Rīga, LVI, 1962.");
			res.put("MV", "Mīlestības vārdnīca. Rīga, Avots, 2003.");
			res.put("NeV", "Kursīte J. Neakadēmiskā latviešu valodas vārdnīca jeb novadu vārdene. Rīga, Madris, 2007.");
			res.put("NGL", "National Geographic Latvija. Rīga, SIA «ALG periodika LV», 2012.-2013. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("NīL", "Nekustamā īpašuma valsts kadastra likums. Rīga, Latvijas Vēstnesis, 205 (3363), 22.12.2005.");
			res.put("NlV", "Vēciņš Ē. Naudas lietas. Skaidrojošā vārdnīca. Rīga, Zvaigzne, 1993.");
			res.put("Ox", "Oxford Dictionary and Thesaurus. Oxford, 1997.");
			res.put("OxL", "Matthews P. H., The Concise Oxford Dictionary of Linguistics. Oxford University Press, 2005.");
			res.put("OxW", "Oxford Dictionary of English. http://en.wikipedia.org/wiki/Oxford_Dictionary_of_English");
			res.put("P35", "Salnais V., Maldups A. Pagastu apraksti. (Pēc 1935. gada tautas skaitīšanas materiāliem). Rīga, Valsts statistikas pārvalde, 1935.");
			res.put("PDE", "Populārā dabas enciklopēdija. Sast. Kavacs G. Rīga, Jumava, 2007.");
			res.put("Ped", "Pedagoģijas terminu skaidrojošā vārdnīca. Rīga, Zvaigzne ABC, 2000.");
			res.put("PIeL", "Publisko iepirkumu likums. Rīga, Latvijas Vēstnesis, 65 (3433), 25.04.2006.");
			res.put("PkE", "Džads T. Pēc kara. Eiropas vēsture pēc 1945. gada. Dienas Grāmata, Rīga, 2007.");
			res.put("PL", "Preses lasītāja svešvārdu vārdnīca. Rīga, Nordik, 2004.");
			res.put("PlN", "Planētas noslēpumi. Rīga, AS «Lauku Avīze», 2014. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("PmE", "Populārā medicīnas enciklopēdija. Rīga, Zinātne, 1976.");
			res.put("PmV", "Pasaules mītu vārdnīca. Rīga, Zvaigzne ABC, 2005.");
			res.put("PnL", "Baumanis j., Klimpiņš V. Putni Latvijā. Palīgs putnu novērošanai dabā. Rīga, Zvaigzne ABC, 2003.");
			res.put("PnR", "Iestāžu publikāciju noformēšanas rokasgrāmata. Luksemburga: Eiropas Savienības Publikāciju birojs. 2011. (http://publications.europa.eu/code/lv/lv-000100.htm)");
			res.put("PP", "Ceplītis L., Miķelsone A., Porīte T., Raģe S. Latviešu valodas pareizrakstības un pareizrunas vārdnīca. Rīga, Avots, 1995.");
			res.put("Ppk", "Pasaules politiskā karte. Rīga, Jāņa sēta, 2007.");
			res.put("PsA", "Benešs H. Psiholoģijas atlants. 1. un 2. daļa. Rīga, Zvaigzne ABC, 2001.");
			res.put("Psh", "Psiholoģijas vārdnīca. Rīga, Mācību grāmata, 1999.");
			res.put("PuA", "Pasaules uzziņu atlants. Rīga, Karšu izdevniecība Jāņa sēta, 2010.");
			res.put("PV", "Ašmanis M. Politikas terminu vārdnīca. Rīga, Zvaigzne ABC, 1999.");
			res.put("PvV", "Grava S. Pilsētvides vārdnīca. Rīga, Jāņa Rozes apgāds, 2006.");
			res.put("PZT", "Pasaules zemes un tautas. Ģeogrāfijas vārdnīca. Rīga, Zvaigzne, 1978.");
			res.put("R1", "Pasaules reliģijas. Rīga, Zvaigzne ABC, 2000.");
			res.put("RiE", "Rīgas ielas. Enciklopēdija. 1.-3., Rīga, 2001.-2009.  ");
			res.put("RL", "Rīgas Laiks. Rīga, SIA «Rīgas Laiks», 2013 (-gads.mēnesis-lappuse)");
			res.put("RtV", "Hiršs I., Hirša S. Reliģisko terminu vārdnīca. Rīga, Kristīgās vadības koledža, 2008.");
			res.put("SbaL", "Sugu un biotopu aizsardzības likums. Rīga, Latvijas Vēstnesis, 121/122 (2032/2033), 05.04.2000.");
			res.put("SbV", "Roldugins V. Starptautiskā biznesa skaidrojošā vārdnīca. Rīga, Jumava, 2005.");
			res.put("SD", "Sestdiena. Rīga, Dienas žurnāli, 2013.-2014. (Pēdējais skaitlis norāda lappusi) ");
			res.put("SdtV", "Sociālā darba terminoloģijas vārdnīca. Rīga, Sociālā darba un sociālās pedagoģijas augsskola «Attīstība», 2000.");
			res.put("Sen", "Ruberte I. Senvārdu vārdnīca. Rīga, Zvaigzne ABC, 2004.");
			res.put("SG", "Rampa T. L. Senču gudrība. Rīga, Alberts XII, 1998.");
			res.put("Sin", "Grīnberga E., Kalnciems O., Lukstiņš G., Ozols J. Latviešu valodas sinonīmu vārdnīca. 2. izd. Rīga, Liesma, 1972.; 3. izd. Rīga, Avots, 2002.");
			res.put("Sin4", "Latviešu valodas sinonīmu vārdnīca. Dorisas Šnē redakcijā. Rīga, Avots, 2012.");
			res.put("SiV", "Putniņa M., Timuška A. Sinoles izloksnes salīdzinājumu vārdnīca. Rīga, LU LVI, 2001.");
			res.put("SLG", "Bušs O., Ernstsone V. Latviešu valodas slenga vārdnīca. Rīga, Norden AB, 2006.");
			res.put("SLV", "Spāņu-latviešu vārdnīca. Rīga, Avots, 2004.");
			res.put("SpT", "Kupčs J., Knipše G. Sporta terminu skaidrojošā vārdnīca. Rīga, LSPA, 1992.");
			res.put("SsV", "Socioloģijas skaidrojošā vārdnīca. Skolām un pašmācībai. Rīga, LU, 1997.");
			res.put("Str", "Lācītis V. Stroika ar skatu uz Londonu. Rīga, Mansards, 2010.");
			res.put("SV04", "Baldunčiks J., Pokrotniece K. Svešvārdu vārdnīca. Rīga, Jumava, 2005.");
			res.put("SV05", "Ilustrētā svešvārdu vārdnīca. Rīga, Avots, 2005.");
			res.put("SV06", "Dravnieks J. Svešu vārdu grāmata. Jelgava, H. Alunāna apgāds, 1906.");
			res.put("SV08", "Svešvārdu vārdnīca. 25000 vārdu un terminu. Rīga, Avots, 2008.");
			res.put("SV11", "Vidiņš J. Svešvārdu grāmata. Rīga, Sinatne, 1911.");
			res.put("SV12", "Svešvārdu vārdnīca. Rīga, A. Raņķa apgāds, 1912.");
			res.put("SV14", "Dravnieks J. Svešvārdu grāmata. Jelgava, H. Alunāna apgāds, 1914.");
			res.put("SV21", "Ducmanis K. Politiska un vispārēja svešvārdu grāmata. Rīga, Daile un darbs, 1921.");
			res.put("SV26", "Svešvārdu vārdnīca. Rīga, A. Gulbja apgāds, 1926.");
			res.put("SV33", "Vidiņš J. Svešvārdu grāmata. Rīga, Valters un Rapa, 1933.");
			res.put("SV34", "Svešvārdu vārdnīca. Rīga, A. Gulbja grāmatu spiestuve, 1934.");
			res.put("SV51", "Svešvārdu vārdnīca. Rīga, Latvijas Valsts izdevniecība, 1951.");
			res.put("SV58", "Svešvārdu vārdnīca. Vācija, A. Ozoliņa apgāds, 1958.");
			res.put("SV69", "Svešvārdu vārdnīca. Rīga, Liesma, 1969.");
			res.put("SV78", "Svešvārdu vārdnīca. Rīga, Liesma, 1978.");
			res.put("SV96", "Svešvārdu vārdnīca. Rīga, Norden, 1996.");
			res.put("SV99", "Svešvārdu vārdnīca. Rīga, Jumava, 1999.");
			res.put("SW08", "Lībknehts V. Politisku un vispārīgu svešvārdu grāmata. 2. papild. un pārlab. izd. Rīga, Apīnis, 1908.");
			res.put("SW78", "Mekons Fr. Svešu vārdu grāmata. Rīga, 1878.");
			res.put("SW86", "Dravnieks J. Svešu vārdu grāmata. Grāmatniekiem un laikrakstu lasītājiem. Jelgava, apgādājis H. Alunāns, 1886.");
			res.put("SW99", "Dravnieks J. Svešu vārdu grāmata. Grāmatniekiem un laikrakstu lasītājiem. Otrs papildināts izdevums. Jelgava, Apgādājis un drukājis H. Alunāns, 1899.");
			res.put("T84", "Latvijas PSR administratīvi teritoriālais iedalījums (1984. gads). Rīga, Avots, 1984. ");
			res.put("TA", "Stradiņš J. Trešā atmoda. Raksti un runas 1988.-1990. gadā Latvijā un par Latviju. Rīga, Zinātne, 1992.");
			res.put("TE", "Fermēlens N. Telpaugi. Enciklopēdija. Rīga, Zvaigzne ABC, 2003.");
			res.put("TĢ", "Tacits G. K. Ģermānija. Par Ģermānijas atrašanās vietu un tautām. Rīga, Vēstures izpētes un popularizēšanas biedrība, 2011.");
			res.put("TiA", "Ūsele V. Tilžas izloksnes apraksts. Rīga, LU LVI, 1998.");
			res.put("TK58", "LZA Terminoloģijas komisijas lēmums Nr. 58. Pieņemts 06.02.2007.; prot. Nr. 1/1073. Rīga, Latvijas Vēstnesis, 115 (4101), 22.07.2009.");
			res.put("TK80", "LZA Terminoloģijas komisijas lēmums Nr. 80. Pieņemts 02.12.2008.; prot. Nr. 8/1089. Rīga, Latvijas Vēstnesis, 3 (4194), 07.01.2010.");
			res.put("TkDB", "LZA Terminoloģijas komisijas datu bāze. http://termini.lza.lv");
			res.put("TlV", "Kursīte J. Tautlietu vārdene. Rīga, Nemateriālā kultūras mantojuma valsts aģentūra, 2009.");
			res.put("TM", "Rudzītis K. Terminologia medica. Rīga, 1973.");
			res.put("TM5", "Rudzītis K. Terminologia medica. Latīņu - latviešu medicīnas terminu vārdnīca. 2003.-2005. gada pārstrādāts un papildināts izdevums. Rīga, 2005.");
			res.put("Tr", "Terra. Latvijas Universitāte, 2000.-2010. (pirmais skaitlis norāda žurnāla numuru, otrais - lappusi)");
			res.put("TtP", "Krastiņš I. Tiesību teorijas pamatjēdzieni. Rīga, LU, 1996.");
			res.put("TvV", "Tūrisma un viesmīlības terminu skaidrojošā vārdnīca. Rīga, LR Ekonomikas ministrija, 2008.");
			res.put("TV", "Tautsaimniecības vārdnīca. Rīga, Universitāte Rīgā, 1943.-1944.");
			res.put("ULW", "Lettisches Wörterbuch. Erster Theil. Lettisch-deutches Wörterbuch von Bishof Dr. Carl Christian Ulmann. Riga, 1872. Zweiter Theil. Deutsch-lettisches Wörterbuch mit Zugrundelengung des von Bishof Dr. Carl Christian Ulmann zurückgelassenen Manuscriptes bearbeitet von Gustav Braže, Pastor emer. Riga u. Leipzig, 1880.");
			res.put("V1", "Dambe V. Latvijas apdzīvoto vietu un to iedzīvotāju nosaukumi. Rīga, Zinātne, 1990.");
			res.put("Va", "Valodas aktualitātes. Rīga, Valsts valodas aģentūra, 2008.");
			res.put("VdK", "Vārdadienu kalendārs 2000.-2003. Rīga, Valsts valodas centrs, 1999.");
			res.put("VE", "Veselības enciklopēdija. Rīga, SIA Nacionālais apgāds, 2009.");
			res.put("VeAT", "Šķirkļa skaidrojumu no vācu valodas tulkoja Ventspils Augstskolas Tulkošanas studiju fakultātes studenti. Prakses vadītāja Silga Sviķe.");
			res.put("VfV", "Vilks A. Vārdnīca filozofijā vidusskolām. Rīga, RaKa, 2000.");
			res.put("VIL", "Vispārējās izglītības likums. Rīga, Latvijas Vēstnesis, 213/215 (1673/1675), 30.06.1999.");
			res.put("ViV", "Ādamsons E., Kagaine E. Vainižu izloksnes vārdnīca. A-M, N-Ž. Rīga, LU LVI, 2000.");
			res.put("VjV", "Skangale L. Vēsturisko jēdzienu skaidrojošā vārdnīca. Rīga, RaKa, 2005.");
			res.put("VkG", "Šmidts P. Valuodas kļūdas un gŗūtumi. Rīga, Izdevis A. Gulbis, 1921.");
			res.put("VL41", "Vāciski-latviska vārdnīca. Sakārtojis Ozoliņš Ed., Rīga, Latvju grāmata, 1941.");
			res.put("VL42", "Vāciski-latviska vārdnīca. Sakārtojis Ed. Ozoliņš, Rīga, A. Gulbja grāmatu apgādniecība, 1942.");
			res.put("VL44", "Dravnieks J. Vāciski latviska vārdnīca. Rīga, VAPP, 1944.");
			res.put("VL96", "Granta K., Pampe E. Vācu - latviešu vārdnīca. Rīga, Avots, 1996.");
			res.put("VlO", "Verzeichnis lettländischer Orstnamen. Herausgegeben von Hans Feldmann, Verlag von E. Bruhns, Riga, 1938.");
			res.put("VmV", "Cielava S. Vispārīgā mākslas vēsture. 1.-4. Rīga, Zvaigzne ABC, 1998.–2001.");
			res.put("Vng", "Vanags J. Medības, atziņas un patiesības. Rīga, Autora izdevums, 2010.");
			res.put("VpJ", "Valodas prakses jautājumi. Rīga, Ramave, 1935.");
			res.put("VpV", "Valodas prakse: vērojumi un ieteikumi. Rīga, 1.-4. LU Akadēmiskais apgāds, 2005.-2009. g.; 5.-7. Latviešu valodas aģentūra, 2010.-2012. g. (pirmais skaitlis norāda izdevuma numuru, otrais - lappusi)");
			res.put("VrJ", "Valodas un rakstības jautājumi. Rīga, Ramaves apgāds, 1940.");
			res.put("VSL", "Valsts statistikas likums. Rīga, Latvijas Vēstnesis, 306/307 (1021/1022), 25.11.1997.");
			res.put("VsV", "Valodniecības pamatterminu skaidrojošā vārdnīca. Rīga, LU LVI, 2007.");
			res.put("VtV", "Valsts un tiesību vēsture jēdzienos un terminos. Rīga, SIA «Divergens», 2001. ");
			res.put("VV", "Kursīte J. Virtuves vārdene. Rīga, Rundas, 2012.");
			res.put("VzsV", "Vides zinību skaidrojošā vārdnīca. Rīga, Jumava, 1999.");
			res.put("WD", "The Wordsworth Dictionary of Phrase and Fable. Wordsworth Editions Ltd., 2001.");
			res.put("Wi", "Vikipēdija. http://www.wikipedia.org");
			res.put("WL10", "Vācu - latviešu vārdnīca. Sastādījis J. Dravnieks, Rīgā, 1910.");
			res.put("Wp", "Places in the World. http://www.places-in-the-world.com");
			res.put("zo37", "Par simpātisko degu. http://www.zooeksperts.lv/lv/?p=37.");
			res.put("ZTV", "Zinātnes un tehnoloģijas vārdnīca. Rīga, Norden AB, 2001.");
			res.put("Ztv1", "Zinātniskās terminoloģijas vārdnīca. Rīga, Izglītības ministrijas izdevums, 1922.");
			res.put("ZvD", "Zvaigžņotā Debess. Rīga, Mācību grāmata, 2013. (pirmais skaitlis norāda izdevuma numuru, otrais - lappusi)");
			res.put("ŽoN", "Kurzemniece I. Žogu nosaukumi latviešu valodas izloksnēs. LU Latviešu valodas institūts, 2008.");
			res.put("Žrg", "Latviešu valodas žargona vārdnīca. Austrālijā, Apgāds AIVA, 1990., 1996., Rīga, Avots, 2005.");
			return res;
		}
		
		public Sources ()
		{
			orig = null; s = null;
		}
		
		public String describeSources() {
			StringBuilder result = new StringBuilder();
			boolean hasPrev = false;
			for (String source : this.s) {
				if (hasPrev) result.append(", ");
				
				if (sourceDictionaries.get(source) != null) {
					result.append(sourceDictionaries.get(source));
				} else {
					System.err.printf("Avota kods %s nav avotu sarakstā", source);
					result.append(source);
				}
				hasPrev = true;
			}
			return result.toString();
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
				res.append("{");
				res.append(i.next().toJSON());
				res.append("}");
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
		// Returns only the inside of object - without enclosing { }
		public String toJSON();
	}

	public void addToLexicon(Analyzer analizators, String importSource) {
		this.head.addToLexicon(analizators, importSource, this.sources);
		if (this.derivs != null)
			for (Header h : this.derivs)
				h.addToLexicon(analizators, importSource, this.sources);		
	}

}
