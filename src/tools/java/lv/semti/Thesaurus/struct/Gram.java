/*******************************************************************************
 * Copyright 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Lauma Pretkalniņa
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
package lv.semti.Thesaurus.struct;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.semti.Thesaurus.utils.HasToJSON;
import lv.semti.Thesaurus.utils.MappingSet;
import lv.semti.Thesaurus.utils.Tuple;
import lv.semti.Thesaurus.utils.JSONUtils;


import org.json.simple.JSONObject;
import org.w3c.dom.Node;

/**
 * g (gramatika) field.
 */
public class Gram  implements HasToJSON
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
		res.put("īp.", "Īpašības vārds");
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
		res.put("intr.", "Nepārejošs");
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
		res.put("arh.", "Arhitektūra");
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
		res.put("kino", "Kinematogrāfija");
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
		res.put("TV", "Televīzija");
		res.put("val.", "Valodniecība");
		res.put("vet.", "Veterinārija");
		res.put("zool.", "Zooloģija");
		
		res.put("arābu", "Arābu");
		res.put("arābu", "Vārds svešvalodā");
		res.put("arābu val.", "Arābu");
		res.put("arābu val.", "Vārds svešvalodā");
		res.put("vācu val.", "Vācu");
		res.put("vācu val.", "Vārds svešvalodā");
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
		res.put("liet.", "Lietuviešu");
		res.put("liet.", "Vārds svešvalodā");
		res.put("sengr.", "Sengrieķu");
		res.put("sengr.", "Vārds svešvalodā");
		
		res.put("dial. (augšzemnieku)", "Agušzemnieku");	// Unique.
		res.put("dial. (augšzemnieku)", "Dialekts");	// Unique.
		res.put("latg.", "Latgaliešu");
		res.put("latg.", "Dialekts");
		
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
		res.put("parasti nenoteiksmē", "Parasti nenoteiksmē");
		res.put("parasti nenoteiksmē", "Darbības vārds");
		res.put("parasti pavēles formā", "Parasti pavēles izteiksmē");
		res.put("parasti pavēles formā", "Darbības vārds");
		res.put("parasti pavēles formā.", "Parasti pavēles izteiksmē");
		res.put("parasti pavēles formā.", "Darbības vārds");
		res.put("nelok.", "Nelokāms");
		res.put("subst. noz.", "Lietvārda nozīmē");
		res.put("lietv. nozīmē.", "Lietvārda nozīmē");
		res.put("īp. nozīmē.", "Īpašības vārda nozīmē");
		res.put("ar not. gal.", "Ar noteikto galotni");
		res.put("ar lielo sākumburtu", "Ar lielo sākumburtu");
		res.put("pareti.", "Pareti");
		res.put("pareti", "Pareti");
		res.put("reti.", "Reti");
		res.put("reti", "Reti");
		res.put("retāk", "Retāk");
		
		res.put("hip.", "Hipotēze");
		
		return res;
	}
	
	/*/*
	 * Patterns for identifying (true) grammatical information.
	 */
//	public static LinkedList<Pattern> knownPatterns = generateKnownPatterns();
/*	private static LinkedList<Pattern> generateKnownPatterns()
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
	}//*/
	
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
		correctedGram = processBeginingWithPatterns(correctedGram, lemma);
		
		String[] subGrams = correctedGram.split("\\s*;\\s*");
		leftovers = new LinkedList<LinkedList<String>> ();
		
		// Process each semicolon-separated substring.
		for (String subGram : subGrams)	
		{
			subGram = processWithNoSemicolonPatterns(subGram, lemma);
			String[] gramElems = subGram.split("\\s*,\\s*");
			LinkedList<String> toDo = new LinkedList<String> ();
			
			// Process each comma-separated substring.
			for (String gramElem : gramElems) 
			{
				gramElem = gramElem.trim();
				// Check for abbreviations.
				if (knownAbbr.containsKey(gramElem))
					flags.addAll(knownAbbr.getAll(gramElem));
				else
				{
					// Check for matches regular expressions.
					gramElem = processWithNoCommaPatterns(gramElem, lemma);
					// Unprocessed leftovers. 
					if (!gramElem.equals(""))
						toDo.add(gramElem);	
				}
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
	 * These patterns are meant for using on the beginning of the
	 * unsegmented grammar string.
	 * Thus,e.g., if there was no plural-only nouns with ending -ļas, then
	 * there is no rule for processing such words (at least in most cases).
	 * @param lemma is used for grammar parsing.
	 */
	private String processBeginingWithPatterns(String gramText, String lemma)
	{
		gramText = gramText.trim();
		int newBegin = -1;
		
		// Blocks of rules.
		if (newBegin == -1) newBegin = firstConjDirVerb3PersRules(gramText, lemma);
		if (newBegin == -1) newBegin = firstConjDirVerbAllPersRules(gramText, lemma);
		if (newBegin == -1) newBegin = secondConjDirVerbRules(gramText, lemma);
		if (newBegin == -1) newBegin = thirdConjDir3PersVerbRules(gramText, lemma);
		if (newBegin == -1) newBegin = thirdConjDirAllPersVerbRules(gramText, lemma);
		
		if (newBegin == -1) newBegin = firstConjRef3PersVerbRules(gramText, lemma);
		if (newBegin == -1) newBegin = firstConjRefAllPersVerbRules(gramText, lemma);
		if (newBegin == -1) newBegin = secondConjRefVerbRules(gramText, lemma);
		if (newBegin == -1) newBegin = thirdConjRef3PersVerbRules(gramText, lemma);
		if (newBegin == -1) newBegin = thirdConjRefAllPersVerbRules(gramText, lemma);
		
		if (newBegin == -1) newBegin = sixthDeclNounFullWordRules(gramText, lemma);
		
		// Complicated rules: grammar contains lemma variation spelled out.
		if (newBegin == -1)
		{
			// Super-complicated case: pronunciation included.			
			// Paradigm 1: Lietvārds 1. deklinācija -s
			// Changed in new version
			/*if (lemma.endsWith("di") &&
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
			}//*/

			// Paradigm 2: Lietvārds 1. deklinācija -š
			if (lemma.endsWith("ņi") &&
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
			else if (gramText.matches("-ņa, dsk\\. ģen\\. -ņu([;,.].*)?")) // afroamerikāņi
			{
				newBegin = "-ņa, dsk. ģen. -ņu".length();
				
				if (lemma.endsWith("ņi"))
				{
					paradigm.add(3);
					flags.add("Šķirkļavārds daudzskaitlī");
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
			else if (gramText.matches("-es, dsk\\. ģen\\. -šņu([;,.].*)?")) //aizkrāsne
			{
				newBegin = "-es, dsk. ģen. -šņu".length();
				if (lemma.matches(".*[šs][nņ]e"))
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
			// Ending rules
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
			
			// Grammar includes endings for other lemma variants. 
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
			else if (gramText.startsWith("-ās, s.")) //pirmdzimtā, -šanās
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
			else if (gramText.matches("s\\. -ā([.;].*)?")) //agrākais
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
		
			// Paradigm Unknown: Divdabis
			// Grammar includes endings for other lemma variants. 
			else if (gramText.matches("-gušais; s\\. -gusi, -gusī([.;].*)?")) //aizdudzis
			{
				newBegin = "-gušais; s. -gusi, -gusī".length();
				if (lemma.endsWith("dzis"))
				{
					Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 4) + "gusi");
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Sieviešu dzimte");
					altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					
					paradigm.add(0);
					flags.add("Divdabis");
					flags.add("Lokāmais darāmās kārtas pagātnes divdabis (-is, -usi, -ies, -usies)");
					flags.add("Vīriešu dzimte");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 0 (Divdabis)\n", lemma);
					newBegin = 0;
				}
			}
			else if (gramText.matches("-ušais; s. -usi\\, -usī([.;].*)?")) //aizkūpis
			{
				newBegin = "-ušais; s. -usi, -usī".length();
				if (lemma.matches(".*[cdjlmprstv]is"))
				{
					Lemma altLemma = new Lemma (lemma.substring(0, lemma.length() - 3) + "usi");
					HashSet<String> altParams = new HashSet<String> ();
					altParams.add("Sieviešu dzimte");
					altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					
					paradigm.add(0);
					flags.add("Divdabis");
					flags.add("Lokāmais darāmās kārtas pagātnes divdabis (-is, -usi, -ies, -usies)");
					flags.add("Vīriešu dzimte");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" with paradigm 0 (Divdabis)\n", lemma);
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
		
		if (newBegin > 0 && newBegin <= gramText.length())
			gramText = gramText.substring(newBegin);
		else if (newBegin > gramText.length())
		{
			System.err.printf("Problem with processing lemma \"%s\" and grammar \"%s\": obtained cut index \"%d\"",
					lemma, gramText, newBegin);
		}
		if (gramText.matches("[.,;].*")) gramText = gramText.substring(1);
		return gramText;
	}
	
	
	/**
	 * This method contains collection of patterns with no commas in them -
	 * these patterns can be applied to any segmented grammar substring, not
	 * only on the beginning of the grammar. Only patterns found in data are
	 * given. Thus,e.g., if there was no plural-only nouns with ending -ļas,
	 * then there is no rule for processing such words (at least in most
	 * cases).
	 * @param lemma is used for grammar parsing.
	 * @return leftovers (unprocessed part of string)
	 */
	private String processWithNoCommaPatterns(String gramText, String lemma)
	{
		gramText = gramText.trim();
		int newBegin = -1;
		
		// Alternative form processing.
		if (gramText.matches("parasti divd\\. formā: (\\w+)")) //aizdzert->aizdzerts
		{
			Matcher m = Pattern.compile("(parasti divd\\. formā: (\\w+))([.;].*)?").matcher(gramText);
			m.matches();
			String newLemma = m.group(2);
			Lemma altLemma = new Lemma (newLemma);
			HashSet<String> altParams = new HashSet<String> ();
			altParams.add("Divdabis");
			altParams.add("Cita paradigma");
			
			newBegin = m.group(1).length();
			if (newLemma.endsWith("ts")) // aizdzert->aizdzerts
			{
				altParams.add("Lokāmais ciešamās kārtas pagātnes divdabis (-ts, -ta)");
				altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
				
				flags.add("Darbības vārds");
				flags.add("Parasti divdabja formā");
				flags.add("Parasti lokāmā ciešamās kārtas pagātnes divdabja formā");
			}
			else if (newLemma.endsWith("is") || newLemma.endsWith("ies")) // aizmakt->aizsmacis, pieriesties->pieriesies
			{
				altParams.add("Lokāmais darāmās kārtas pagātnes divdabis (-is, -usi, -ies, -usies)");
				altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
				
				flags.add("Darbības vārds");
				flags.add("Parasti divdabja formā");
				flags.add("Parasti lokāmā darāmās kārtas pagātnes divdabja formā");
			}
			else if (newLemma.endsWith("damies")) //aizvilkties->aizvilkdamies
			{
				altParams.add("Daļēji lokāmais divdabis (-dams, -dama, -damies, -damās)");
				altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
				
				flags.add("Darbības vārds");
				flags.add("Parasti divdabja formā");
				flags.add("Parasti daļēji lokāmā divdabja formā");
			}
			else
			{
				System.err.printf("Problem matching \"%s\" in entry \"%s\" with paradigm 0 (Divdabis)\n",
						newLemma, lemma);
				newBegin = 0;
			}
		} else if (gramText.matches("bieži lok\\.: (\\w+)")) // agrums->agrumā
		{
			Matcher m = Pattern.compile("(bieži lok\\.: (\\w+))([.;].*)?").matcher(gramText);
			newBegin = m.group(1).length();
			flags.add("Bieži lokatīva formā");
		}
		
		if (newBegin > 0) gramText = gramText.substring(newBegin);
		return gramText;
	}
	
	/**
	 * This method contains collection of patterns with no semicolon in them -
	 * these patterns can be applied to grammar segmented on ';', but not
	 * segmented on ','. Only patterns found in data are
	 * given. Thus,e.g., if there was no plural-only nouns with ending -ļas,
	 * then there is no rule for processing such words (at least in most
	 * cases).
	 * @param lemma is used for grammar parsing.
	 * @return leftovers (unprocessed part of string)
	 */
	private String processWithNoSemicolonPatterns(String gramText, String lemma)
	{
		gramText = gramText.trim();
		int newBegin = -1;
		
		// Alternative form processing.
		if (gramText.matches("parasti divd\\. formā: (\\w+), (\\w+)")) //aizelsties->aizelsies, aizelsdamies
		{
			Matcher m = Pattern.compile("(parasti divd\\. formā: (\\w+), (\\w+))([.;].*)?")
					.matcher(gramText);
			m.matches();
			String[] newLemmas = {m.group(2), m.group(3)};
			newBegin = m.group(1).length();
			for (String newLemma : newLemmas)
			{
				Lemma altLemma = new Lemma (newLemma);
				HashSet<String> altParams = new HashSet<String> ();
				altParams.add("Divdabis");
				altParams.add("Cita paradigma");
				
				if (newLemma.endsWith("ts")) // noliegt->noliegts
				{
					altParams.add("Lokāmais ciešamās kārtas pagātnes divdabis (-ts, -ta)");
					altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					
					flags.add("Darbības vārds");
					flags.add("Parasti divdabja formā");
					flags.add("Parasti lokāmā ciešamās kārtas pagātnes divdabja formā");
				}
				else if (newLemma.endsWith("is") || newLemma.endsWith("ies")) // aizelsties->aizelsies
				{
					altParams.add("Lokāmais darāmās kārtas pagātnes divdabis (-is, -usi, -ies, -usies)");
					altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					
					flags.add("Darbības vārds");
					flags.add("Parasti divdabja formā");
					flags.add("Parasti lokāmā darāmās kārtas pagātnes divdabja formā");
				}
				else if (newLemma.endsWith("ams") || newLemma.endsWith("āms")) // noliegt->noliedzams
				{
					altParams.add("Lokāmais ciešamās kārtas tagadnes divdabis (-ams, -ama, -āms, -āma)");
					altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					
					flags.add("Darbības vārds");
					flags.add("Parasti divdabja formā");
					flags.add("Parasti lokāmā ciešamās kārtas tagadnes divdabja formā");
				}
				else if (newLemma.endsWith("damies")) //aizelsties->aizelsdamies
				{
					altParams.add("Daļēji lokāmais divdabis (-dams, -dama, -damies, -damās)");
					altLemmas.put(0, new Tuple<Lemma, HashSet<String>>(altLemma, altParams));
					
					flags.add("Darbības vārds");
					flags.add("Parasti divdabja formā");
					flags.add("Parasti daļēji lokāmā divdabja formā");
				}
				else
				{
					System.err.printf("Problem matching \"%s\" in entry \"%s\" with paradigm 0 (Divdabis)\n",
							newLemma, lemma);
					newBegin = 0;
				}
			}
		}

		if (newBegin > 0) gramText = gramText.substring(newBegin);
		return gramText;
	}
	
	/**
	 * Simple rule - tries to match grammar text to given string and lemma
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
	 * The same as simple rule, but hyperns ar optional. It tries to match
	 * grammar text to given pattern and lemma ending. If matched, adds a single
	 * paradigm.
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
	private int simpleRuleOptHyperns (
			String pattern, String requiredEnding, int paradigmId,
			String[] positiveFlags, String[] alwaysFlags,
			String gramText, String lemma)
	{
		int newBegin = -1;
		pattern = pattern.replace("-", "\\E-?\\Q");
		pattern = "(\\Q" + pattern + "\\E)([;,.].*)?";
		Matcher m = Pattern.compile(pattern).matcher(gramText);
		if (m.matches())
		{
			newBegin = m.group(1).length();
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
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
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
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
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
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
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
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
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
				flags.add("Šķirkļavārds daudzskaitlī");
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
				flags.add("Šķirkļavārds daudzskaitlī");
			}
			else if (lemma.endsWith("ieši"))
			{
				paradigm.add(3);
				paradigm.add(5);
				flags.add("Lietvārds");
				flags.add("Šķirkļavārds daudzskaitlī");
				flags.add("Neviennozīmīga paradigma");

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
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Neviennozīmīga paradigma");
				}
				else if (lemma.matches(".*[vpm]ji"))	// looks like these are predefined sound changes always
				{
					paradigm.add(3);
					paradigm.add(5);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
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
					flags.add("Šķirkļavārds daudzskaitlī");
					flags.add("Neviennozīmīga paradigma");
				}
				else if (lemma.matches(".*[cdlmnpvz]i"))	// there is no sound change
				{
					paradigm.add(1);
					paradigm.add(2);
					flags.add("Lietvārds");
					flags.add("Šķirkļavārds daudzskaitlī");
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
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
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
	 * // Paradigm 11: Lietvārds 6. deklinācija -s
	 * Rules in form "-valsts, dsk. ģen. -valstu, s.", i.e containing full 6th
	 * ceclension nouns.
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int sixthDeclNounFullWordRules (String gramText, String lemma)
	{
		int newBegin = -1;
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-acs, dsk. ģen. -acu, s.", "acs", 11,
				new String[] {"Lietvārds"},
				new String[] {"Sieviešu dzimte"},
				gramText, lemma); //uzacs, acs
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-krāsns, dsk. ģen. -krāšņu, s.", "krāsns", 11,
				new String[] {"Lietvārds"},
				new String[] {"Sieviešu dzimte"},
				gramText, lemma); //aizkrāsns
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-valsts, dsk. ģen. -valstu, s.", "valsts", 11,
				new String[] {"Lietvārds"},
				new String[] {"Sieviešu dzimte"},
				gramText, lemma); //agrārvalsts
		return newBegin;
	}
	/**
	 * Paradigm 15: Darbības vārdi 1. konjugācija tiešie
	 * Rules in form "parasti 3. pers., -šalc, pag. -šalca".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int firstConjDirVerb3PersRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Rules ordered alphabetically by verb infinitive.
		// A
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -aug, pag. -auga", "augt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"augt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizaugt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -aust, pag. -ausa", "aust", 15,
				new String[] {"Darbības vārds", "Locīt kā \"aust\" (kā gaisma)"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizaust 1
		// B
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -birst, pag. -bira", "birt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"birt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizbirt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -brūk, pag. -bruka", "brukt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"brukt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizbrukt
		// C
		// D
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -deg, pag. -dega", "degt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"degt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdegt 2
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dim, pag. -dima", "dimt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dimt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdimt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dip, pag. -dipa", "dipt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dipt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdipt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dūc, pag. -dūca", "dūkt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dūkt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdūkt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dzeļ, pag. -dzēla", "dzelt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dzelt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdzelt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dzīst, pag. -dzija", "dzīt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dzīt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdzīt 2
		// E, F
		// G
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -grimst, pag. -grima", "grimt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"grimt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgrimt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -grūst, pag. -gruva", "grūt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"grūt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgrūt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"3. pers. -guldz, pag. -guldza", "gulgt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"gulgt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgulgt
		// H
		// I
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -irst, pag. -ira", "irt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"irt\" (kā audums)"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //irt 2
		// J
		// K
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -kalst, pag. -kalta", "kalst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kalst\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkalst
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -kauc, pag. -kauca", "kaukt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kaukt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkaukt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -knābj, pag. -knāba", "knābt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"knābt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknābt
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -kvēpst, pag. -kvēpa", "kvēpt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kvēpt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkvēpt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -kviec, pag. -kvieca", "kviekt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kviekt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkviekt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -ķep, pag. -ķepa", "ķept", 15,
				new String[] {"Darbības vārds", "Locīt kā \"ķept\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizķept
		// L
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -līkst, pag. -līka", "līkt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"līkt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizlīkt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -līp, pag. -lipa", "lipt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"lipt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizlipt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -līst, pag. -lija", "līt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"līt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizlīt
		// M, N, O, P, R
		// S, Š
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -šalc, pag. -šalca", "šalkt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"šalkt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizšalkt
		// T
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -tūkst, pag. -tūka", "tūkt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"tūkt\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aiztūkt
		// U, V, Z
		return newBegin;
	}
	
	/**
	 * Paradigm 15: Darbības vārdi 1. konjugācija tiešie
	 * Rules in form "-tupstu, -tupsti, -tupst, pag. -tupu".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int firstConjDirVerbAllPersRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Rules ordered alphabetically by verb infinitive.
		// A
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-aru, -ar, -ar, pag. -aru", "art", 15,
				new String[] {"Darbības vārds", "Locīt kā \"art\""},
				null, gramText, lemma); //aizart
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-aužu, -aud, -auž, pag. -audu", "aust", 15,
				new String[] {"Darbības vārds", "Locīt kā \"aust\" (kā zirneklis)"},
				null, gramText, lemma); //aizaust 2
		// B
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-bāžu, -bāz, -bāž, pag. -bāzu", "bāzt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"bāzt\""},
				null, gramText, lemma); //aizbāzt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-bēgu, -bēdz, -bēg, pag. -bēgu", "bēgt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"bēgt\""},
				null, gramText, lemma); //aizbēgt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-beru, -ber, -ber, pag. -bēru", "bērt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"bērt\""},
				null, gramText, lemma); //aizbērt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-bilstu, -bilsti, -bilst, pag. -bildu", "bilst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"bilst\""},
				null, gramText, lemma); //aizbilst
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-braucu, -brauc, -brauc, pag. -braucu", "braukt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"braukt\""},
				null, gramText, lemma); //aizbraukt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-brāžu, -brāz, -brāž, pag. -brāzu", "brāzt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"brāzt\""},
				null, gramText, lemma); //aizbrāzt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-brienu, -brien, -brien, pag. -bridu", "brist", 15,
				new String[] {"Darbības vārds", "Locīt kā \"brist\""},
				null, gramText, lemma); //aizbrist
		// C
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-ceļu, -cel, -ceļ, pag. -cēlu", "celt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"celt\""},
				null, gramText, lemma); //aizcelt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-cērtu, -cērt, -cērt, pag. -cirtu", "cirst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"cirst\""},
				null, gramText, lemma); //aizcirst
		// D
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-dedzu, -dedz, -dedz, pag. -dedzu", "degt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"degt\""},
				null, gramText, lemma); //aizdegt 1
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-diebju, -dieb, -diebj, pag. -diebu", "diebt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"diebt\""},
				null, gramText, lemma); //aizdiebt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-diedzu, -diedz, -diedz, pag. -diedzu", "diegt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"diegt\""},
				null, gramText, lemma); //aizdiegt 1
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-dodu, -dod, -dod, pag. -devu", "dot", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dot\""},
				null, gramText, lemma); //aizdot
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-drāžu, -drāz, -drāž, pag. -drāzu", "drāzt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"drāzt\""},
				null, gramText, lemma); //aizdrāzt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-duru, -dur, -dur, pag. -dūru", "durt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"durt\""},
				null, gramText, lemma); //aizdurt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-dzeru, -dzer, -dzer, pag. -dzēru", "dzert", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dzert\""},
				null, gramText, lemma); //aizdzert
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-dzenu, -dzen, -dzen, pag. -dzinu", "dzīt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dzīt\""},
				null, gramText, lemma); //aizdzīt 1
		// E
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-ēdu, -ēd, -ēd, pag. -ēdu", "ēst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"ēst\""},
				null, gramText, lemma); //aizēst
		// F
		// G
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-gāžu, -gāz, -gāž, pag. -gāzu", "gāzt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"gāzt\""},
				null, gramText, lemma); //aizgāzt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-glaužu, -glaud, -glauž, pag. -glaudu", "glaust", 15,
				new String[] {"Darbības vārds", "Locīt kā \"glaust\""},
				null, gramText, lemma); //aizglaust
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-grābju, -grāb, -grābj, pag. -grābu", "grābt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"grābt\""},
				null, gramText, lemma); //aizgrābt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-graužu, -grauz, -grauž, pag. -grauzu", "grauzt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"grauzt\""},
				null, gramText, lemma); //aizgrauzt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-griežu, -griez, -griež, pag. -griezu", "griezt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"griezt\""},
				null, gramText, lemma); //aizgriezt 2
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-grūžu, -grūd, -grūž, pag. -grūdu", "grūst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"grūst\""},
				null, gramText, lemma); //aizgrūst
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-gulstu, -gulsti, -gulst, pag. -gūlu, arī -gulu", "gult", 15,
				new String[] {"Darbības vārds", "Locīt kā \"gult\"", "Paralēlās formas"},
				null, gramText, lemma); //aizgult
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-gūstu, -gūsti, -gūst, pag. -guvu", "gūt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"gūt\""},
				null, gramText, lemma); //aizgūt
		// Ģ
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-ģiedu, -ģied, -ģied, pag. -gidu", "ģist", 15,
				new String[] {"Darbības vārds", "Locīt kā \"ģist\""},
				null, gramText, lemma); //apģist
		// H
		// I
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-eju, -ej, -iet, pag. -gāju", "iet", 15,
				new String[] {"Darbības vārds", "Locīt kā \"iet\""},
				null, gramText, lemma); //apiet
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-iru, -ir, -ir, pag. -īru", "irt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"irt\" (kā ar airiem)"},
				null, gramText, lemma); //aizirt 1
		// J
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-jāju, -jāj, -jāj, pag. -jāju", "jāt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"jāt\""},
				null, gramText, lemma); //aizjāt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-jožu, -joz, -jož, pag. -jozu", "jozt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"jozt\""},
				null, gramText, lemma); //aizjozt 1, 2
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-jūdzu, -jūdz, -jūdz, pag. -jūdzu", "jūgt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"jūgt\""},
				null, gramText, lemma); //aizjūgt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-jumju, -jum, -jumj, pag. -jūmu, arī -jumu", "jumt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"jumt\"", "Paralēlās formas"},
				null, gramText, lemma); //aizjumt
		// K
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kāpju, -kāp, -kāpj, pag. -kāpu", "kāpt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kāpt\""},
				null, gramText, lemma); //aizkāpt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-karu, -kar, -kar, pag. -kāru", "kārt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kārt\""},
				null, gramText, lemma); //aizkārt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kauju, -kauj, -kauj, pag. -kāvu", "kaut", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kaut\""},
				null, gramText, lemma); //apkaut
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-klāju, -klāj, -klāj, pag. -klāju", "klāt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"klāt\""},
				null, gramText, lemma); //apklāt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kliedzu, -kliedz, -kliedz, pag. -kliedzu", "kliegt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kliegt\""},
				null, gramText, lemma); //aizkliegt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-klimstu, -klimsti, -klimst, pag. -klimtu", "klimst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"klimst\""},
				null, gramText, lemma); //aizklimst
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-klīstu, -klīsti, -klīst, pag. -klīdu", "klīst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"klīst\""},
				null, gramText, lemma); //aizklīst
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kļūstu, -kļūsti, -kļūst, pag. -kļuvu", "kļūt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kļūt\""},
				null, gramText, lemma); //aizkļūt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kožu, -kod, -kož, pag. -kodu", "kost", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kost\""},
				null, gramText, lemma); //aizkost
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-krāpju, -krāp, -krāpj, pag. -krāpu", "krāpt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"krāpt\""},
				null, gramText, lemma); //aizkrāpt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-krauju, -krauj, -krauj, pag. -krāvu", "kraut", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kraut\""},
				null, gramText, lemma); //aizkraut
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-krītu, -krīti, -krīt, pag. -kritu", "krist", 15,
				new String[] {"Darbības vārds", "Locīt kā \"krist\""},
				null, gramText, lemma); //aizkrist
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kuru, -kur, -kur, pag. -kūru", "kurt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kurt\""},
				null, gramText, lemma); //aizkurt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kūstu, -kusti, -kūst, pag. -kusu", "kust", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kust\""},
				null, gramText, lemma); //aizkust
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-ķeru, -ķer, -ķer, pag. -ķēru", "ķert", 15,
				new String[] {"Darbības vārds", "Locīt kā \"ķert\""},
				null, gramText, lemma); //aizķert
		// L
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-laižu, -laid, -laiž, pag. -laidu", "laist", 15,
				new String[] {"Darbības vārds", "Locīt kā \"laist\""},
				null, gramText, lemma); //aizlaist
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-laužu, -lauz, -lauž, pag. -lauzu", "lauzt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"lauzt\""},
				null, gramText, lemma); //aizlauzt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-lecu, -lec, -lec, pag. -lēcu", "lēkt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"lēkt\""},
				null, gramText, lemma); //aizlēkt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-liedzu, -liedz, -liedz, pag. -liedzu", "liegt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"liegt\""},
				null, gramText, lemma); //aizliegt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-leju, -lej, -lej, pag. -lēju", "liet", 15,
				new String[] {"Darbības vārds", "Locīt kā \"liet\""},
				null, gramText, lemma); //aizliet
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-lieku, -liec, -liek, pag. -liku", "likt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"likt\""},
				null, gramText, lemma); //aizlikt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-lienu, -lien, -lien, pag. -līdu", "līst", 15,
				new String[] {"Darbības vārds", "Locīt kā \"līst\""},
				null, gramText, lemma); //aizlīst
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-lobju, -lob, -lobj, pag. -lobu", "lobt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"lobt\""},
				null, gramText, lemma); //aizlobt
		
		// M, N, O, P, R, S
		// T
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-tupstu, -tupsti, -tupst, pag. -tupu", "tupt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"tupt\"", "Paralēlās formas"},
				null, gramText, lemma); //aiztupt
				// TODO tupu/tupstu
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-tveru, -tver, -tver, pag. -tvēru", "tvert", 15,
				new String[] {"Darbības vārds", "Locīt kā \"tvert\""},
				null, gramText, lemma); //aiztvert
		// U, V, Z
		
		return newBegin;
	}

	/**
	 * Paradigm 16: Darbības vārdi 2. konjugācija tiešie
	 * Rules in form "parasti 3. pers., -o, pag. -oja",
	 * "-oju, -o, -o, -ojam, -ojat, pag. -oju; -ojām, -ojāt; pav. -o, -ojiet"
	 * and "-ēju, -ē, -ē, pag. -ēju".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int secondConjDirVerbRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Paradigm 16: Darbības vārdi 2. konjugācija tiešie
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -kūko, pag. -kūkoja", "kūkot", 16,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkūkot
		
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -ā, pag. -āja", "āt", 16,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkābāt
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -ē, pag. -ēja", "ēt", 16,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //adsorbēt
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -o, pag. -oja", "ot", 16,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizšalkot, aizbangot
		
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-dabūju, -dabū, -dabū, pag. -dabūju", "dabūt", 16,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizdabūt
		
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
				"-āju, -ā, -ā, pag. -āju", "āt", 16,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aijāt
		if (newBegin == -1) newBegin = simpleRule(
				"-ēju, -ē, -ē, pag. -ēja", "ēt", 16,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizdelverēt
		if (newBegin == -1) newBegin = simpleRule(
				"-ēju, -ē, -ē, pag. -ēju", "ēt", 16,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //absolutizēt
		if (newBegin == -1) newBegin = simpleRule(
				"-oju, -o, -o, pag. -oju", "ot", 16,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aiztuntuļot
		
		return newBegin;
	}
	
	/**
	 * Paradigm 17: Darbības vārdi 3. konjugācija tiešie
	 * Rules in form "parasti 3. pers., -blākš, pag. -blākšēja"
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int thirdConjDir3PersVerbRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Verb-specific rules.
		// Rules ordered alphabetically by verb infinitive.
		// A
		// B
		newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -blākš, pag. -blākšēja", "blākšēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizblākšēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -blākšķ, pag. -blākšķēja", "blākšķēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizblākšķēt
		// C, Č
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -čab, pag. -čabēja", "čabēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizčabēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -čaukst, pag. -čaukstēja", "čaukstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizčaukstēt
		// D
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dārd, pag. -dārdēja", "dārdēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdārdēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dimd, pag. -dimdēja", "dimdēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdimdēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dip, pag. -dipēja", "dipēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdipēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -dun, pag. -dunēja", "dunēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdunēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -džinkst, pag. -džinkstēja", "džinkstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdžinkstēt
		// E, F
		// G
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -grab, pag. -grabēja", "grabēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgrabēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -gurkst, pag. -gurkstēja", "gurkstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgurkstēt
		// H, I, J
		// K
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -klab, pag, -klabēja", "klabēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklabēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -klakst, pag. -klakstēja", "klakstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklakstēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -klaudz, pag. -klaudzēja", "klaudzēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklaudzēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -kūp, pag. -kūpēja", "kūpēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkūpēt
		// L, M, N, O, P, R, S, T, U, V, Z
		
		// Generic ending rules.
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -ī, pag. -īja", "īt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdzirkstīt
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -ina, pag. -ināja", "ināt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizducināt

		return newBegin;
	}
	
	/**
	 * Paradigm 17: Darbības vārdi 3. konjugācija tiešie
	 * Rules in form "-dziedu, -dziedi, -dzied, pag. -dziedāju" and
	 * "-u, -i, -a, pag. -īju".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int thirdConjDirAllPersVerbRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Verb-specific rules.
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-dziedu, -dziedi, -dzied, pag. -dziedāju", "dziedāt", 17,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizdziedāt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-guļu, -guli, -guļ, pag. -gulēju", "gulēt", 17,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizgulēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-klimstu, -klimsti, -klimst, pag. -klimstēju", "klimstēt", 17,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizklimstēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kustu, -kusti, -kust, pag. -kustēju", "kustēt", 17,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizkustēt
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-turu, -turi, -tur, pag. -turēju", "turēt", 17,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizturēt
		
		// Generic ending rules.
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
	 * Rules in form "parasti 3. pers., -šalcas, pag. -šalcās".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int firstConjRef3PersVerbRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Rules ordered alphabetically by verb infinitive.
		// A, B, C
		// D
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -dūcas, pag. -dūcās", "dūkties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"dūkties\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdūkties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -duras, pag. -dūrās", "durties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"durties\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizdurties
		// E, F
		// G
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -gāžas, pag. -gāzās", "gāzties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"gāzties\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgāzties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -graužas, pag. -grauzās", "grauzties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"grauzties\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgrauzties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"parasti 3. pers., -griežas, pag. -griezās", "griezties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"griezties\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgriezties 2
		// H, I, J
		// K
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -kaucas, pag. -kaucās", "kaukties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"kaukties\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkaukties
		// L, M, N, O, P, R
		// S, Š
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -šalcas, pag. -šalcās", "šalkties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"šalkties\""},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizšalkties
		// T, U, V, Z
		return newBegin;
	}
	

	/**
	 * Paradigm 18: Darbības vārdi 1. konjugācija atgriezeniski
	 * Rules in form "-tupstos, -tupsties, -tupstas, pag. -tupos".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int firstConjRefAllPersVerbRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Rules ordered alphabetically by verb infinitive.
		// A
		// B
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-brāžos, -brāzies, -brāžas, pag. -brāžos", "brāzties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"brāzties\""},
				null, gramText, lemma); //aizbrāzties
		if (newBegin == -1) newBegin = simpleRule(
				"-brēcos, -brēcies, -brēcas, pag. -brēcos", "brēkties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"brēkties\""},
				null, gramText, lemma); //aizbrēkties
		// C
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-ciešos, -cieties, -ciešas, pag. -cietos", "ciesties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"ciesties\""},
				null, gramText, lemma); //aizciesties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-cērtos, -cērties, -cērtas, pag. -cirtos", "cirsties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"cirsties\""},
				null, gramText, lemma); //aizcirsties
		// D
		if (newBegin == -1) newBegin = simpleRule(
				"-degos, -dedzies, -degas, pag. -degos", "degties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"degties\""},
				null, gramText, lemma); //aizdegties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-drāžos, -drāzies, -drāžas, pag. -drāzos", "drāzties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"drāzties\""},
				null, gramText, lemma); //aizdrāzties
		// E
		if (newBegin == -1) newBegin = simpleRule(
				"-elšos, -elsies, -elšas, pag. -elsos", "elsties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"elsties\""},
				null, gramText, lemma); //aizelsties
		// F, 
		// G
		if (newBegin == -1) newBegin = simpleRule(
				"-gārdzos, -gārdzies, -gārdzas, pag. -gārdzos", "gārgties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"gārgties\""},
				null, gramText, lemma); //aizgārgties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-griežos, -griezies, -griežas, pag. -griezos", "griezties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"griezties\""},
				null, gramText, lemma); //aizgriezties 1
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-gulstos, -gulsties, -gulstas, arī -guļos, -gulies, -guļas, pag. -gūlos, arī -gulos", "gulties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"gulties\"", "Paralēlās formas"},
				null, gramText, lemma); //aizgulties
		if (newBegin == -1) newBegin = simpleRule(
				"-gūstos, -gūsties, -gūstas, pag. -guvos", "gūties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"gūties\""},
				null, gramText, lemma); //aizgūties
		// Ģ,
		if (newBegin == -1) newBegin = simpleRule(
				"-ģiedos, -ģiedies, -ģiedas, pag. -gidos", "ģisties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"ģisties\""},
				null, gramText, lemma); //apģisties
		// H
		// I
		if (newBegin == -1) newBegin = simpleRule(
				"-ejos, -ejos, -ietas, pag. -gājos", "ieties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"ieties\""},
				null, gramText, lemma); //apieties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-iros, -iries, -iras, pag. -īros", "irties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"irties\" (kā ar airiem)"},
				null, gramText, lemma); //aizirties
		// J
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-jūdzos, -jūdzies, -jūdzas, pag. -jūdzos", "jūgties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"jūgties\""},
				null, gramText, lemma); //aizjūgties
		// K
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-karos, -karies, -karas, pag. -kāros", "kārties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"kārties\""},
				null, gramText, lemma); //apkārties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-klājos, -klājies, -klājas, pag. -klājos", "klāties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"klāties\""},
				null, gramText, lemma); //apklāties
		if (newBegin == -1) newBegin = simpleRule(
				"-kliedzos, -kliedzies, -kliedzas, pag. -kliedzos", "kliegties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"kliegties\""},
				null, gramText, lemma); //aizkliegties
		if (newBegin == -1) newBegin = simpleRule(
				"-krācos, -krācies, -krācas, pag. -krācos", "krākties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"krākties\""},
				null, gramText, lemma); //aizkrākties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-kuļos, -kulies, -kuļas, pag. -kūlos", "kulties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"kulties\""},
				null, gramText, lemma); //aizkulties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-ķēros, -ķeries, -ķeras, pag. -ķēros", "ķerties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"ķerties\""},
				null, gramText, lemma); //aizķerties
		// L
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-laižos, -laidies, -laižas, pag. -laidos", "laisties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"laisties\""},
				null, gramText, lemma); //aizlaisties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-laužos, -lauzies, -laužas, pag. -lauzās", "lauzties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"lauzties\""},
				null, gramText, lemma); //aizlauzties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-liedzos, -liedzies, -liedzas, pag. -liedzos", "liegties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"liegties\""},
				null, gramText, lemma); //aizliegties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-liecos, -liecies, -liecas, pag. -liecos", "liekties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"liekties\""},
				null, gramText, lemma); //aizliekties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-liekos, -liecies, -liekas, pag. -likos", "likties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"likties\""},
				null, gramText, lemma); //aizlikties
		// M, N, O, P, R, S
		// T
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-tupstos, -tupsties, -tupstas, pag. -tupos", "tupties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"tupties\"", "Paralēlās formas"},
				null, gramText, lemma); //aiztupties
				//TODO check paralel forms.
		// U, V, Z
		return newBegin;
	}
	
	/**
	 * Paradigm 19: Darbības vārdi 2. konjugācija atgriezeniski
	 * Rules in form "parasti 3. pers., -ējas, pag. -ējās",
	 * "-ējos, -ējies, -ējas, -ējamies, -ējaties, pag. -ējos, -ējāmies, -ējāties; pav. -ējies, -ējieties",
	 *  and "-ojos, -ojies, -ojas, pag. -ojos".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
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
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int thirdConjRef3PersVerbRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Verb-specific rules.
		// Rules ordered alphabetically by verb infinitive.
		// A
		// B
		if (newBegin == -1) newBegin = simpleRule(
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
		// C, Č
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
		// D
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
		// E, F
		// G
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -grābās, pag. -grābējās", "grabēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgrabēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -gurkstas, pag. -gurkstējās", "gurkstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizgurkstēties
		// H, I, J
		// K
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -klabas, pag. -klabējās", "klabēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklabēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -klaudzas, pag. -klaudzējās", "klaudzēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklaudzēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -klukstas, pag. -klukstējās", "klukstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklukstēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -klunkšas, pag. -klunkšējās", "klunkšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklunkšēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -klunkšķas, pag. -klunkšķējās", "klunkšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizklunkšķēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -knakstās, pag. -knakstējās", "knakstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknakstēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -knakšas, pag. -knakšējās", "knakšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknakšēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -knakšķas, pag. -knakšķējās", "knakšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknakšķēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -knaukšas, pag. -knaukšējās", "knaukšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknaukšēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -knaukšķas, pag. -knaukšķējās", "knaukšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknaukšķēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -knikšas, pag. -knikšējās", "knikšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknikšēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -knikšķas, pag. -knikšķējās", "knikšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizknikšķēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -krakstas, pag. -krakstējās", "krakstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkrakstēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -krakšķas, pag. -krakšķējās", "krakšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkrakšķēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -kurkstas, pag. -kurkstējās", "kurkstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkurkstēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -kurkšķas, pag. -kurkšķējās", "kurkšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizkurkšķēties
		// L, M, N, O, P, R, S, T, U, V, Z
		
		// Generic ending rules.
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -as, pag. -ējās", "ēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizčiepstēties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -inās, pag. -inājās", "ināties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizbubināties
		if (newBegin == -1) newBegin = simpleRule(
				"parasti 3. pers., -ās, pag. -ījās", "īties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"},
				gramText, lemma); //aizbīdīties
		return newBegin;
	}
	
	/**
	 * Paradigm 20: Darbības vārdi 3. konjugācija atgriezeniski
	 * Rules in form "parasti 3. pers., -ās, pag. -ījās" and
	 * "-os, -ies, -ās, pag. -ījos".
	 * This function is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these rules
	 * for verbs are long and highly specific and, thus, do not conflict
	 * with other rules.
	 * @return new begining for gram string if one of these rulles matched,
	 * -1 otherwise.
	 */
	private int thirdConjRefAllPersVerbRules (String gramText, String lemma)
	{
		int newBegin = -1;
		// Verb-specific rules.
		if (newBegin == -1) newBegin = simpleRule(
				"-dziedos, -dziedies, -dziedas, pag. -dziedājos", "dziedāties", 20,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizdziedāties
		if (newBegin == -1) newBegin = simpleRuleOptHyperns(
				"-dzenos, -dzenies, -dzenas, pag. -dzinos", "dzīties", 20,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizdzīties
		if (newBegin == -1) newBegin = simpleRule(
				"-guļos, -gulies, -guļas, pag. -gulējos", "gulēties", 20,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizgulēties
		if (newBegin == -1) newBegin = simpleRule(
				"-kustos, -kusties, -kustas, pag. -kustējos", "kustēties", 20,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //aizkustēties
		
		// Generic ending rules.
		if (newBegin == -1) newBegin = simpleRule(
				"-os, -ies, -as, pag. -ējos", "ēties", 20,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //apkaunēties
		if (newBegin == -1) newBegin = simpleRule(
				"-inos, -inies, -inās, pag. -inājos", "ināties", 20,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //apklaušināties
		if (newBegin == -1) newBegin = simpleRule(
				"-os, -ies, -ās, pag. -ījos", "īties", 20,
				new String[] {"Darbības vārds"},
				null, gramText, lemma); //apklausīties
		
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
		
		if (flags.contains("Darbības vārds"))
		{
			if (lemma.endsWith("īt") || lemma.endsWith("ināt")) paradigm.add(17);
			if (lemma.endsWith("īties") || lemma.endsWith("ināties")) paradigm.add(20);
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
		//Inconsequences in data
				
		//gramText = gramText.replaceAll("^māt\\.", "mat\\.");
		//gramText = gramText.replace(" māt.", " mat.");
		//gramText = gramText.replace("vsk..", "vsk.");
		//gramText = gramText.replace("vsk .", "vsk.");
		//gramText = gramText.replaceAll("^gen\\.", "ģen\\.");
		//gramText = gramText.replace(" gen.", " ģen.");
		//gramText = gramText.replaceAll("^trans;", "trans\\.;");
		//gramText = gramText.replace(" trans;", " trans.;");
		
		//gramText = gramText.replace("-ais; s. -a: -ā;", "-ais; s. -a, -ā;"); //apgrēcīgs
		
		

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
			res.append(JSONUtils.simplesToJSON(paradigm));
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
							res.append(JSONUtils.simplesToJSON(alt.second));
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
			res.append(JSONUtils.simplesToJSON(flags));
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
					res.append(JSONUtils.simplesToJSON(next));
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