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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.semti.Thesaurus.utils.HasToJSON;
import lv.semti.Thesaurus.utils.MappingSet;
import lv.semti.Thesaurus.utils.Tuple;
import lv.semti.Thesaurus.utils.JSONUtils;
import lv.semti.Thesaurus.struct.gramlogic.*;

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
	 * Collection of rules that can be used as SimpleRule.applyDirect()
	 * This is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these 
	 * rules are long and highly specific and, thus, do not conflict
	 * with other rules.
	 */
	public static final Rule[] simpleRulesDirect =
	{
		/* Paradigm Unknown: Atgriezeniskie lietvārdi -šanās
		 */
		new SimpleRule("ģen. -ās, akuz. -os, instr. -os, dsk. -ās, ģen. -os, akuz. -ās, s.", "šanās", 0,
				new String[] {"Lietvārds", "Atgriezeniskais lietvārds"},
				new String[] {"Sieviešu dzimte"}), //aizbildināšanās
		
		/* Paradigm 16: Darbības vārdi 2. konjugācija tiešie
		 */
		// Rules for both all person and third-person-only cases.
		VerbRule.secondConjDir("-āju, -ā,", "-ā, pag. -āju", "āt"), //aijāt, aizkābāt
		VerbRule.secondConjDir("-ēju, -ē,", "-ē, pag. -ēju", "ēt"), //abonēt, adsorbēt
		VerbRule.secondConjDir("-īju, -ī,", "-ī, pag. -īju", "īt"), //apšķibīt, aizdzirkstīt
		VerbRule.secondConjDir("-oju, -o,", "-o, pag. -oju", "ot"), //aizalvot, aizbangot

		// Single-case rules.
		new SimpleRule(	"-ēju, -ē, -ē, -ējam, -ējat, pag. -ēju, -ējām, -ējāt; pav. -ē, -ējiet", "ēt", 16,
				new String[] {"Darbības vārds"}, null), //adverbializēt
		new SimpleRule("-oju, -o, -o, -ojam, -ojat, pag. -oju; -ojām, -ojāt; pav. -o, -ojiet", "ot", 16,
				new String[] {"Darbības vārds"}, null), //acot
				
				
		/* Paradigm 17: Darbības vārdi 3. konjugācija tiešie
		 */
		// Rules for both all person and third-person-only cases.
		VerbRule.thirdConjDir("-u, -i,", "-a, pag. -īju", "īt"), //aizsūtīt
		VerbRule.thirdConjDir("-inu, -ini,", "-ina, pag. -ināju", "ināt"), //aizsvilināt
				
		// Single-case rules.
		new SimpleRule("parasti 3. pers., -ina, pag. -ināja", "ināt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizducināt
		

		/* Paradigm 18: Darbības vārdi 1. konjugācija atgriezeniski
		 */
		// Rules for both all person and third-person-only cases.
		// Verb-specific rules ordered alphabetically by verb infinitive.
		// A, B
		VerbRule.firstConjRefl("-brēcos, -brēcies,", "-brēcas, pag. -brēcos", "brēkties"), //aizbrēkties
		// C, D
		VerbRule.firstConjRefl("-degos, -dedzies,", "-degas, pag. -degos", "degties"), //aizdegties
		// E
		VerbRule.firstConjRefl("-elšos, -elsies,", "-elšas, pag. -elsos", "elsties"), //aizelsties	
		// F, G
		VerbRule.firstConjRefl("-gārdzos, -gārdzies,", "-gārdzas, pag. -gārdzos", "gārgties"), //aizgārgties
		VerbRule.firstConjRefl("-gūstos, -gūsties,", "-gūstas, pag. -guvos", "gūties"), //aizgūties
		// Ģ,
		VerbRule.firstConjRefl("-ģiedos, -ģiedies,", "-ģiedas, pag. -ģidos", "ģisties"), //apģisties
		// H, I
		VerbRule.firstConjRefl("-ejos, -ejos,", "-ietas, pag. -gājos", "ieties"), //apieties
		// J, K
		VerbRule.firstConjRefl("-kliedzos, -kliedzies,", "-kliedzas, pag. -kliedzos", "kliegties"), //aizkliegties
		VerbRule.firstConjRefl("-krācos, -krācies,", "-krācas, pag. -krācos", "krākties"), //aizkrākties
		// L, M
		VerbRule.firstConjRefl("-mirstos, -mirsties,", "-mirstas, pag. -mirsos", "mirsties"), //aizmirsties	
		// N, O, P, R, S, T, U, V, Z
		
		// Single-case rules.
		// Verb-specific rules ordered alphabetically by verb infinitive.
		// A, B, C, D
		new SimpleRule("parasti 3. pers., -dūcas, pag. -dūcās", "dūkties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"dūkties\""},
				new String[] {"Parasti 3. personā"}),  //aizdūkties
		// E, F, G, H, I, J, K
		new SimpleRule("parasti 3. pers., -kaucas, pag. -kaucās", "kaukties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"kaukties\""},
				new String[] {"Parasti 3. personā"}), //aizkaukties
		// L, M, N, O, P, R, S, Š
		new SimpleRule("parasti 3. pers., -šalcas, pag. -šalcās", "šalkties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"šalkties\""},
				new String[] {"Parasti 3. personā"}), //aizšalkties
		// T, U, V, Z
		
		
		/* Paradigm 19: Darbības vārdi 2. konjugācija atgriezeniski
		 */
		// Rules for both all person and third-person-only cases.
		VerbRule.secondConjRefl("-ojos, -ojies,", "-ojas, pag. -ojos", "oties"), //aiztuntuļoties, apgrēkoties
		VerbRule.secondConjRefl("-ējos, -ējies,", "-ējas, pag. -ējos", "ēties"), //abstrahēties
		VerbRule.secondConjRefl("-ājos, -ājies,", "-ājas, pag. -ājos", "āties"), //aizdomāties
		
		// Single-case rules.
		new SimpleRule("parasti 3. pers., -ējas, pag. -ējās", "ēties", 19,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //absorbēties
		new SimpleRule("parasti 3. pers., -ojas, pag. -ojās", "oties", 19,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //daudzkāršoties
			
		new SimpleRule("-ējos, -ējies, -ējas, -ējamies, -ējaties, pag. -ējos, -ējāmies, -ējāties; pav. -ējies, -ējieties", "ēties", 19,
				new String[] {"Darbības vārds"}, null), //adverbiēties
				
		
		/* Paradigm 20: Darbības vārdi 3. konjugācija atgriezeniski
		 * Rules in form "parasti 3. pers., -ās, pag. -ījās" and
		 * "-os, -ies, -ās, pag. -ījos".
		 */
		// Rules for both all person and third-person-only cases.
		VerbRule.thirdConjRefl("-os, -ies,", "-as, pag. -ējos", "ēties"), //apkaunēties, aizņaudēties
		VerbRule.thirdConjRefl("-inos, -inies,", "-inās, pag. -inājos", "ināties"), //apklaušināties
		VerbRule.thirdConjRefl("-os, -ies,", "-ās, pag. -ījos", "īties"), //apklausīties	
		
		VerbRule.thirdConjRefl("-dziedos, -dziedies,", "-dziedas, pag. -dziedājos", "dziedāties"), //aizdziedāties
		VerbRule.thirdConjRefl("-guļos, -gulies,", "-guļas, pag. -gulējos", "gulēties"), //aizgulēties

		// Single-case rules.
		// Generic ending rules.
		new SimpleRule("parasti 3. pers., -as, pag. -ējās", "ēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizčiepstēties
		new SimpleRule("parasti 3. pers., -inās, pag. -inājās", "ināties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizbubināties
		new SimpleRule("parasti 3. pers., -ās, pag. -ījās", "īties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizbīdīties
		
		// Verb-specific rules ordered alphabetically by verb infinitive.
		// A, B
		new SimpleRule("parasti 3. pers., -brikšķas, pag. -brikšķējās", "brikšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizbrikšķēties
		new SimpleRule("parasti 3. pers., -brikšas, pag. -brikšējās", "brikšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizbrikšēties
		new SimpleRule("parasti 3. pers., -brīkšķas, pag. -brīkšķējās", "brīkšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizbrīkšķēties
		new SimpleRule("parasti 3. pers., -brīkšas, pag. -brīkšējās", "brīkšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizbrīkšēties
		// C, Č
		new SimpleRule("parasti 3. pers., -čabas, pag. -čabējās", "čabēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizčabēties
		new SimpleRule("parasti 3. pers., -čaukstas, pag. -čaukstējās", "čaukstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizčaukstēties
		// D
		new SimpleRule("parasti 3. pers., -dārdas, pag. -dārdējās", "dārdēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizdārdēties
		new SimpleRule("parasti 3. pers., -drebas, pag. -drebējās", "drebēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizdrebēties
		// E, F, G
		new SimpleRule("parasti 3. pers., -grābās, pag. -grābējās", "grabēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizgrabēties
		new SimpleRule("parasti 3. pers., -gurkstas, pag. -gurkstējās", "gurkstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizgurkstēties
		// H, I, J, K
		new SimpleRule("parasti 3. pers., -klabas, pag. -klabējās", "klabēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizklabēties
		new SimpleRule("parasti 3. pers., -klaudzas, pag. -klaudzējās", "klaudzēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizklaudzēties
		new SimpleRule("parasti 3. pers., -klukstas, pag. -klukstējās", "klukstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizklukstēties
		new SimpleRule("parasti 3. pers., -klunkšas, pag. -klunkšējās", "klunkšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizklunkšēties
		new SimpleRule("parasti 3. pers., -klunkšķas, pag. -klunkšķējās", "klunkšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizklunkšķēties
		new SimpleRule("parasti 3. pers., -knakstās, pag. -knakstējās", "knakstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizknakstēties
		new SimpleRule("parasti 3. pers., -knakšas, pag. -knakšējās", "knakšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizknakšēties
		new SimpleRule("parasti 3. pers., -knakšķas, pag. -knakšķējās", "knakšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizknakšķēties
		new SimpleRule("parasti 3. pers., -knaukšas, pag. -knaukšējās", "knaukšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizknaukšēties
		new SimpleRule("parasti 3. pers., -knaukšķas, pag. -knaukšķējās", "knaukšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizknaukšķēties
		new SimpleRule("parasti 3. pers., -knikšas, pag. -knikšējās", "knikšēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizknikšēties
		new SimpleRule("parasti 3. pers., -knikšķas, pag. -knikšķējās", "knikšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizknikšķēties
		new SimpleRule("parasti 3. pers., -krakstas, pag. -krakstējās", "krakstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizkrakstēties
		new SimpleRule("parasti 3. pers., -krakšķas, pag. -krakšķējās", "krakšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizkrakšķēties
		new SimpleRule("parasti 3. pers., -kurkstas, pag. -kurkstējās", "kurkstēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizkurkstēties
		new SimpleRule("parasti 3. pers., -kurkšķas, pag. -kurkšķējās", "kurkšķēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizkurkšķēties
		// L, M
		new SimpleRule("parasti 3. pers., -mirdzas, pag. -mirdzējās", "mirdzēties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}) //aizmirdzēties
		//N, O, P, R, S, T, U, V, Z	
	};
	
	/**
	 * Collection of rules that can be used as SimpleRule.applyOptHyperns()
	 * This is seperated out for readability from
	 * {@link #processBeginingWithPatterns(String, String)} as currently these 
	 * rules are long and highly specific and, thus, do not conflict
	 * with other rules.
	 */
	public static final Rule[] simpleRulesOptHyperns =
	{
		/* Paradigm 11: Lietvārds 6. deklinācija -s
		 * Rules in form "-valsts, dsk. ģen. -valstu, s.", i.e containing full 6th
		 * declension nouns.
		 */
		new SimpleRule("-acs, dsk. ģen. -acu, s.", "acs", 11,
				new String[] {"Lietvārds"},
				new String[] {"Sieviešu dzimte"}), //uzacs, acs
		new SimpleRule("-krāsns, dsk. ģen. -krāšņu, s.", "krāsns", 11,
				new String[] {"Lietvārds"},
				new String[] {"Sieviešu dzimte"}), //aizkrāsns
		new SimpleRule("-valsts, dsk. ģen. -valstu, s.", "valsts", 11,
				new String[] {"Lietvārds"},
				new String[] {"Sieviešu dzimte"}), //agrārvalsts

		/* Paradigm 15: Darbības vārdi 1. konjugācija tiešie + parasti 3. pers.
		 */
		// Rules for both all person and third-person-only cases.
		// Verbs with infinitive homoforms:
		new VerbRule("-aužu, -aud,", "-auž, pag. -audu", "aust", 15,
				new String[] {"Darbības vārds", "Locīt kā \"aust\" (kā zirneklis)"},
				null), //aizaust 2
		new VerbRule("-dedzu, -dedz,", "-dedz, pag. -dedzu", "degt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"degt\" (kādu citu)"},
				null), //aizdegt 1
		new VerbRule("-degu, -dedz,", "-deg, pag. -degu", "degt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"degt\" (pašam)"},
				null), //apdegt, aizdegt 2
		new VerbRule("-dzenu, -dzen,", "-dzen, pag. -dzinu", "dzīt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dzīt\" (kā lopus)"},
				null), //aizdzīt 1	
		new VerbRule("-iru, -ir,", "-ir, pag. -īru", "irt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"irt\" (kā ar airiem)"},
				null), //aizirt 1
		new VerbRule("-minu, -min,", "-min, pag. -minu", "mīt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"mīt\" (kā pedāļus)"},
				null), //aizmīt 1
		new VerbRule("-miju, -mij,", "-mij, pag. -miju", "mīt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"mīt\" (kā naudu)"},
				null), //aizmīt 2
				
		// Verb-specific rules ordered alphabetically by verb infinitive.
		// A
		VerbRule.firstConjDir("-aru, -ar,", "-ar, pag. -aru", "art"), //aizart
		VerbRule.firstConjDir("-augu, -audz,", "-aug, pag. -augu", "augt"), //ieaugt, aizaugt
		// B
		VerbRule.firstConjDir("-bāžu, -bāz,", "-bāž, pag. -bāzu", "bāzt"), //aizbāzt
		VerbRule.firstConjDir("-bēgu, -bēdz,", "-bēg, pag. -bēgu", "bēgt"), //aizbēgt
		VerbRule.firstConjDir("-beru, -ber,", "-ber, pag. -bēru", "bērt"), //aizbērt
		VerbRule.firstConjDir("-bilstu, -bilsti,", "-bilst, pag. -bildu", "bilst"), //aizbilst
		VerbRule.firstConjDir("-birstu, -birsti,", "-birst, pag. -biru", "birt"), //apbirt, aizbirt
		VerbRule.firstConjDir("-braucu, -brauc,", "-brauc, pag. -braucu", "braukt"), //aizbraukt
		VerbRule.firstConjDir("-brāžu, -brāz,", "-brāž, pag. -brāzu", "brāzt"), //aizbrāzt
		VerbRule.firstConjDir("-brienu, -brien,", "-brien, pag. -bridu", "brist"), //aizbrist
		// C
		VerbRule.firstConjDir("-ceļu, -cel,", "-ceļ, pag. -cēlu", "celt"), //aizcelt
		VerbRule.firstConjDir("-cērtu, -cērt,", "-cērt, pag. -cirtu", "cirst"), //aizcirst				
		// D
		VerbRule.firstConjDir("-diebju, -dieb,", "-diebj, pag. -diebu", "diebt"), //aizdiebt
		VerbRule.firstConjDir("-diedzu, -diedz,", "-diedz, pag. -diedzu", "diegt"), //aizdiegt 1,2
		VerbRule.firstConjDir("-dodu, -dod,", "-dod, pag. -devu", "dot"), //aizdot
		VerbRule.firstConjDir("-drāžu, -drāz,", "-drāž, pag. -drāzu", "drāzt"), //aizdrāzt
		VerbRule.firstConjDir("-duru, -dur,", "-dur, pag. -dūru", "durt"), //aizdurt
		VerbRule.firstConjDir("-dūcu, -dūc,", "-dūc, pag. -dūcu", "dūkt"), //atdūkt, aizdūkt
		VerbRule.firstConjDir("-dzeļu, -dzel,", "-dzeļ, pag. -dzēlu", "dzelt"), //atdzelt, aizdzelt
		VerbRule.firstConjDir("-dzeru, -dzer,", "-dzer, pag. -dzēru", "dzert"), //aizdzert
		// E
		VerbRule.firstConjDir("-ēdu, -ēd,", "-ēd, pag. -ēdu", "ēst"), //aizēst
		// F, G
		VerbRule.firstConjDir("-gāžu, -gāz,", "-gāž, pag. -gāzu", "gāzt"), //aizgāzt
		VerbRule.firstConjDir("-glaužu, -glaud,", "-glauž, pag. -glaudu", "glaust"), //aizglaust
		VerbRule.firstConjDir("-grābju, -grāb,", "-grābj, pag. -grābu", "grābt"), //aizgrābt
		VerbRule.firstConjDir("-graužu, -grauz,", "-grauž, pag. -grauzu", "grauzt"), //aizgrauzt
		VerbRule.firstConjDir("-griežu, -griez,", "-griež, pag. -griezu", "griezt"), //aizgriezt 1, 2
		VerbRule.firstConjDir("-grimstu, -grimsti,", "-grimst, pag. -grimu", "grimt"), //atgrimt, aizgrimt
		VerbRule.firstConjDir("-grūžu, -grūd,", "-grūž, pag. -grūdu", "grūst"), //aizgrūst
		VerbRule.firstConjDir("-gūstu, -gūsti,", "-gūst, pag. -guvu", "gūt"), //aizgūt
		// Ģ
		VerbRule.firstConjDir("-ģiedu, -ģied,", "-ģied, pag. -gidu", "ģist"), //apģist
		// H, I
		VerbRule.firstConjDir("-eju, -ej,", "-iet, pag. -gāju", "iet"), //apiet
		// J
		VerbRule.firstConjDir("-jāju, -jāj,", "-jāj, pag. -jāju", "jāt"), //aizjāt
		VerbRule.firstConjDir("-jožu, -joz,", "-jož, pag. -jozu", "jozt"), //aizjozt 1, 2
		VerbRule.firstConjDir("-jūdzu, -jūdz,", "-jūdz, pag. -jūdzu", "jūgt"), //aizjūgt
		// K
		VerbRule.firstConjDir("-kalstu, -kalsti,", "-kalst, pag. -kaltu", "kalst"), //izkalst, aizkalst
		VerbRule.firstConjDir("-kāpju, -kāp,", "-kāpj, pag. -kāpu", "kāpt"), //aizkāpt
		VerbRule.firstConjDir("-karu, -kar,", "-kar, pag. -kāru", "kārt"), //aizkārt
		VerbRule.firstConjDir("-kaucu, -kauc,", "-kauc, pag. -kaucu", "kaukt"), //izkaukt, aizkaukt
		VerbRule.firstConjDir("-kauju, -kauj,", "-kauj, pag. -kāvu", "kaut"), //apkaut
		VerbRule.firstConjDir("-klāju, -klāj,", "-klāj, pag. -klāju", "klāt"), //apklāt
		VerbRule.firstConjDir("-kliedzu, -kliedz,", "-kliedz, pag. -kliedzu", "kliegt"), //aizkliegt
		VerbRule.firstConjDir("-klimstu, -klimsti,", "-klimst, pag. -klimtu", "klimst"), //aizklimst
		VerbRule.firstConjDir("-klīstu, -klīsti,", "-klīst, pag. -klīdu", "klīst"), //aizklīst
		VerbRule.firstConjDir("-kļūstu, -kļūsti,", "-kļūst, pag. -kļuvu", "kļūt"), //aizkļūt
		VerbRule.firstConjDir("-knābju, -knāb,", "-knābj, pag. -knābu", "knābt"), //uzknābt, aizknābt
		VerbRule.firstConjDir("-kožu, -kod,", "-kož, pag. -kodu", "kost"), //aizkost
		VerbRule.firstConjDir("-krāpju, -krāp,", "-krāpj, pag. -krāpu", "krāpt"), //aizkrāpt
		VerbRule.firstConjDir("-krauju, -krauj,", "-krauj, pag. -krāvu", "kraut"), //aizkraut
		VerbRule.firstConjDir("-krītu, -krīti,", "-krīt, pag. -kritu", "krist"), //aizkrist
		VerbRule.firstConjDir("-kuru, -kur,", "-kur, pag. -kūru", "kurt"), //aizkurt
		VerbRule.firstConjDir("-kūstu, -kusti,", "-kūst, pag. -kusu", "kust"), //aizkust
		VerbRule.firstConjDir("-kvēpstu, -kvēpsti,", "-kvēpst, pag. -kvēpu", "kvēpt"), //apkvēpt, aizkvēpt
		// Ķ
		VerbRule.firstConjDir("-ķepu, -ķep,", "-ķep, pag. -ķepu", "ķept"), //apķept, aizķept
		VerbRule.firstConjDir("-ķeru, -ķer,", "-ķer, pag. -ķēru", "ķert"), //aizķert
		// L
		VerbRule.firstConjDir("-laižu, -laid,", "-laiž, pag. -laidu", "laist"), //aizlaist
		VerbRule.firstConjDir("-laužu, -lauz,", "-lauž, pag. -lauzu", "lauzt"), //aizlauzt
		VerbRule.firstConjDir("-lecu, -lec,", "-lec, pag. -lēcu", "lēkt"), //aizlēkt
		VerbRule.firstConjDir("-liedzu, -liedz,", "-liedz, pag. -liedzu", "liegt"), //aizliegt
		VerbRule.firstConjDir("-liecu, -liec,", "-liec, pag. -liecu", "liekt"), //aizliekt
		VerbRule.firstConjDir("-leju, -lej,", "-lej, pag. -lēju", "liet"), //aizliet
		VerbRule.firstConjDir("-lieku, -liec,", "-liek, pag. -liku", "likt"), //aizlikt
		VerbRule.firstConjDir("-līpu, -līpi,", "-līp, pag. -lipu", "lipt"), //aplipt, aizlipt
		VerbRule.firstConjDir("-līkstu, -līksti,", "-līkst, pag. -līku", "līkt"), //nolīkt, aizlīkt
		VerbRule.firstConjDir("-lienu, -lien,", "-lien, pag. -līdu", "līst"), //aizlīst
		VerbRule.firstConjDir("-līstu, -līsti,", "-līst, pag. -liju", "līt"), //aplīt, aizlīt
		VerbRule.firstConjDir("-lobju, -lob,", "-lobj, pag. -lobu", "lobt"), //aizlobt
		VerbRule.firstConjDir("-lūdzu, -lūdz,", "-lūdz, pag. -lūdzu", "lūgt"), //aizlūgt
		VerbRule.firstConjDir("-lūstu, -lūsti,", "-lūst, pag. -lūzu", "lūzt"), //aizlūzt, aizlūzt			
		// M
		VerbRule.firstConjDir("-metu, -met,", "-met, pag. -metu", "mest"), //aizmest
		VerbRule.firstConjDir("-mēžu, -mēz,", "-mēž, pag. -mēzu", "mēzt"), //aizmēzt
		VerbRule.firstConjDir("-miedzu, -miedz,", "-miedz, pag. -miedzu", "miegt"), //aizmiegt
		VerbRule.firstConjDir("-miegu, -miedz,", "-mieg, pag. -migu", "migt"), //aizmigt
		VerbRule.firstConjDir("-mirstu, -mirsti,", "-mirst, pag. -mirsu", "mirst"), //aizmirst
		VerbRule.firstConjDir("-mūku, -mūc,", "-mūk, pag. -muku", "mukt"), //aizmukt
		// N
		VerbRule.firstConjDir("-nesu, -nes,", "-nes, pag. -nesu", "nest"), //aiznest
		VerbRule.firstConjDir("-nirstu, -nirsti,", "-nirst, pag. -niru", "nirt"), //aiznirt
		// Ņ
		VerbRule.firstConjDir("-ņemu, -ņem,", "-ņem, pag. -ņēmu", "ņemt"), //aizņemt
		// O, P
		VerbRule.firstConjDir("-pampstu, -pampsti,", "-pampst, pag. -pampu", "pampt"), //nopampt, aizpampt
		VerbRule.firstConjDir("-pinu, -pin,", "-pin, pag. -pinu", "pīt"), //aizpīt
		VerbRule.firstConjDir("-ploku, -ploc,", "-plok, pag. -plaku", "plakt"), //aizplakt
		VerbRule.firstConjDir("-plaukstu, -plauksti,", "-plaukst, pag. -plauku", "plaukt"), //atplaukt, aizplaukt
		VerbRule.firstConjDir("-plēšu, -plēs,", "-plēš, pag. plēsu", "plēst"), //aizplēst
		VerbRule.firstConjDir("-plīstu, -plīsti,", "-plīst, pag. -plīsu", "plīst"), //applīst, aizplīst
		VerbRule.firstConjDir("-plūcu, -plūc,", "-plūc, pag. -plūcu", "plūkt"), //aizplūkt
		VerbRule.firstConjDir("-plūstu, -plūsti,", "-plūst, pag. -plūdu", "plūst"), //applūst, aizplūst
		VerbRule.firstConjDir("-pļauju, -pļauj,", "-pļauj, pag. -pļāvu", "pļaut"), //aizpļaut
		VerbRule.firstConjDir("-pūšu, -pūt,", "-pūš, pag. -pūtu", "pūst"), //aizpūst
		// R
		VerbRule.firstConjDir("-roku, -roc,", "-rok, pag. -raku", "rakt"), //aizrakt
		// S, Š
		VerbRule.firstConjDir("-šalcu, -šalc,", "-šalc, pag. -šalcu", "šalkt"), //pašalkt, aizšalkt
		// T
		VerbRule.firstConjDir("-tūkstu, -tūksti,", "-tūkst; pag. -tūku", "tūkt"), //aptūkt, aiztūkt
		VerbRule.firstConjDir("-tveru, -tver,", "-tver, pag. -tvēru", "tvert"), //aiztvert
		// U, V, Z
				
		// Single case rules.		
		// Verb specific rules ordered by type and alphabetically by verb infinitive.
		new SimpleRule("-gulstu, -gulsti, -gulst, pag. -gūlu, arī -gulu", "gult", 15,
				new String[] {"Darbības vārds", "Locīt kā \"gult\"", "Paralēlās formas"},
				null), //aizgult
		new SimpleRule("-jumju, -jum, -jumj, pag. -jūmu, arī -jumu", "jumt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"jumt\"", "Paralēlās formas"},
				null), //aizjumt
		new SimpleRule("-plešu, -plet, -pleš, pag. -pletu, arī -plētu", "plest", 15,
				new String[] {"Darbības vārds", "Locīt kā \"plest\"", "Paralēlās formas"},
				null), //aizplest
		new SimpleRule("-tupstu, -tupsti, -tupst, pag. -tupu", "tupt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"tupt\"", "Paralēlās formas"},
				null), //aiztupt
				// TODO tupu/tupstu
				
		// A
		new SimpleRule("parasti 3. pers., -aust, pag. -ausa", "aust", 15,
				new String[] {"Darbības vārds", "Locīt kā \"aust\" (kā gaisma)"},
				new String[] {"Parasti 3. personā"}), //aizaust 1
		// B
		new SimpleRule("parasti 3. pers., -brūk, pag. -bruka", "brukt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"brukt\""},
				new String[] {"Parasti 3. personā"}), //aizbrukt
		// C, D
		new SimpleRule("parasti 3. pers., -dim, pag. -dima", "dimt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dimt\""},
				new String[] {"Parasti 3. personā"}), //aizdimt
		new SimpleRule("parasti 3. pers., -dip, pag. -dipa", "dipt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dipt\""},
				new String[] {"Parasti 3. personā"}), //aizdipt
		new SimpleRule("parasti 3. pers., -dzīst, pag. -dzija", "dzīt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"dzīt\" (kā ievainojumi)"},
				new String[] {"Parasti 3. personā"}), //aizdzīt 2
		// E, F, G
		new SimpleRule("parasti 3. pers., -grūst, pag. -gruva", "grūt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"grūt\""},
				new String[] {"Parasti 3. personā"}), //aizgrūt
		new SimpleRule("3. pers. -guldz, pag. -guldza", "gulgt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"gulgt\""},
				new String[] {"Parasti 3. personā"}), //aizgulgt
		// H, I
		new SimpleRule("parasti 3. pers., -irst, pag. -ira", "irt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"irt\" (kā audums)"},
				new String[] {"Parasti 3. personā"}), //irt 2
		// J, K
		new SimpleRule("parasti 3. pers., -kviec, pag. -kvieca", "kviekt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"kviekt\""},
				new String[] {"Parasti 3. personā"}), //aizkviekt
		// L, M
		new SimpleRule("parasti 3. pers., -milst, pag. -milza", "milzt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"milzt\""},
				new String[] {"Parasti 3. personā"}), //aizmilzt
		// N, Ņ
		new SimpleRule("parasti 3. pers., -ņirb, pag. -ņirba", "ņirbt", 15,
				new String[] {"Darbības vārds", "Locīt kā \"ņirbt\""},
				new String[] {"Parasti 3. personā"}), //aizņirbt
		// O, P, R, S, Š, T, U, V, Z

		/* Paradigm 16: Darbības vārdi 2. konjugācija tiešie
		 */
		// Rules for both all person and third-person-only cases.
		VerbRule.secondConjDir("-dabūju, -dabū,", "-dabū, pag. -dabūju", "dabūt"), //aizdabūt
				
		// Single case rules.
		// Verb-specific rules.
		new SimpleRule("parasti 3. pers., -kūko, pag. -kūkoja", "kūkot", 16,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizkūkot
		new SimpleRule("parasti 3. pers., -mirgo, pag. -mirgoja", "mirgot", 16,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizmirgot
				
		new SimpleRule("-dabūju, -dabū, -dabū, pag. -dabūju", "dabūt", 16,
				new String[] {"Darbības vārds"}, null), //aizdabūt
				
		/* Paradigm 17: Darbības vārdi 3. konjugācija tiešie
		 */
		// Rules for both all person and third-person-only cases.
		// Verb-specific rules ordered alphabetically by verb infinitive.
		// A, B, C, D
		VerbRule.thirdConjDir("-dziedu, -dziedi,", "-dzied, pag. -dziedāju", "dziedāt"), //aizdziedāt
		// E, F, G
		VerbRule.thirdConjDir("-grabu, -grabi,", "-grab, pag. -grabēju", "grabēt"), //sagrabēt, aizgrabēt
		VerbRule.thirdConjDir("-guļu, -guli,", "-guļ, pag. -gulēju", "gulēt"), //aizgulēt
		// H, I, J, K 
		VerbRule.thirdConjDir("-klabu, -klabi,", "-klab, pag. -klabēju", "klabēt"), //paklabēt, aizklabēt		
		VerbRule.thirdConjDir("-klimstu, -klimsti,", "-klimst, pag. -klimstēju", "klimstēt"), //aizklimstēt
		VerbRule.thirdConjDir("-kustu, -kusti,", "-kust, pag. -kustēju", "kustēt"), //aizkustēt
		VerbRule.thirdConjDir("-kūpu, -kūpi,", "-kūp, pag. -kūpēju", "kūpēt"), //apkūpēt, aizkūpēt
		// L
		VerbRule.thirdConjDir("-loku, -loki,", "-loka, pag. -locīju", "locīt"), //aizlocīt
		// M, N, O, P
		VerbRule.thirdConjDir("-peldu, -peldi,", "-peld, pag. -peldēju", "peldēt"), //aizpeldēt
		VerbRule.thirdConjDir("-pilu, -pili,", "-pil, pag. -pilēju", "pilēt"), //appilēt, aizpilēt
		// R, S, T
		VerbRule.thirdConjDir("-turu, -turi,", "-tur, pag. -turēju", "turēt"), //aizturēt
		// U, V, Z
				
		// Single case rules.		
		// Verb specific rules ordered by type and alphabetically by verb infinitive.
		new SimpleRule("-moku, -moki, -moka, arī -mocu, -moci, -moca, pag. -mocīju", "mocīt", 17,
				new String[] {"Darbības vārds", "Paralēlās formas"}, null), //aizmocīt
			
		// A, B
		new SimpleRule("parasti 3. pers., -blākš, pag. -blākšēja", "blākšēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizblākšēt
		new SimpleRule("parasti 3. pers., -blākšķ, pag. -blākšķēja", "blākšķēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizblākšķēt
		// C, Č
		new SimpleRule("parasti 3. pers., -čab, pag. -čabēja", "čabēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizčabēt
		new SimpleRule("parasti 3. pers., -čaukst, pag. -čaukstēja", "čaukstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizčaukstēt
		// D
		new SimpleRule("parasti 3. pers., -dārd, pag. -dārdēja", "dārdēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizdārdēt
		new SimpleRule("parasti 3. pers., -dimd, pag. -dimdēja", "dimdēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizdimdēt
		new SimpleRule("parasti 3. pers., -dip, pag. -dipēja", "dipēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizdipēt
		new SimpleRule("parasti 3. pers., -dun, pag. -dunēja", "dunēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizdunēt
		new SimpleRule("parasti 3. pers., -džinkst, pag. -džinkstēja", "džinkstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizdžinkstēt
		// E, F, G
		new SimpleRule("parasti 3. pers., -gurkst, pag. -gurkstēja", "gurkstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizgurkstēt
		// H, I, J, K
		new SimpleRule("parasti 3. pers., -klakst, pag. -klakstēja", "klakstēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizklakstēt
		new SimpleRule("parasti 3. pers., -klaudz, pag. -klaudzēja", "klaudzēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizklaudzēt
		// L, M
		new SimpleRule("parasti 3. pers., -mirdz, pag. -mirdzēja (retāk -mirdza, 1. konj.)", "mirdzēt", 17,
				new String[] {"Darbības vārds", "Paralēlās formas"},
				new String[] {"Parasti 3. personā"}), //aizmirdzēt
		// N, Ņ
		new SimpleRule("parasti 3. pers., -ņirb, pag. -ņirbēja", "ņirbēt", 17,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizņirbēt
		// O, P
		new SimpleRule("parasti 3. pers., -pelē, arī -pel, pag. -pelēja", "pelēt", 17,
				new String[] {"Darbības vārds", "Paralēlās formas"},
				new String[] {"Parasti 3. personā"}), //aizpelēt
		// R, S, T, U, V, Z
				
		/* Paradigm 18: Darbības vārdi 1. konjugācija atgriezeniski
		 */
		// Rules for both all person and third-person-only cases.
		// Verbs with infinitive homoforms:
		new VerbRule("-iros, -iries,", "-iras, pag. -īros", "irties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"irties\" (kā ar airiem)"},
				null), //aizirties
		new VerbRule("-minos, -minies,", "-minas, pag. -minos", "mīties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"mīties\" (kā pedāļus)"},
				null), //aizmīties
		new VerbRule("-mijos, -mijies,", "-mijas, pag. -mijos", "mīties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"mīties\" (kā naudu)"},
				null), //apmīties
				
		// Verb-specific rules ordered alphabetically by verb infinitive.
		// A , B
		VerbRule.firstConjRefl("-brāžos, -brāzies,", "-brāžas, pag. -brāzos", "brāzties"), //aizbrāzties
		// C
		VerbRule.firstConjRefl("-ciešos, -cieties,", "-ciešas, pag. -cietos", "ciesties"), //aizciesties
		VerbRule.firstConjRefl("-cērtos, -cērties,", "-cērtas, pag. -cirtos", "cirsties"), //aizcirsties				
		// D
		VerbRule.firstConjRefl("-drāžos, -drāzies,", "-drāžas, pag. -drāzos", "drāzties"), //aizdrāzties
		VerbRule.firstConjRefl("-duros, -duries,", "-duras, pag. -dūros", "durties"), //nodurties, aizdurties
		// E, F, G
		VerbRule.firstConjRefl("-gāžos, -gāzies,", "-gāžas, pag. -gāzos", "gāzties"), //apgāzties, aizgāzties
		VerbRule.firstConjRefl("-graužos, -grauzies,", "-graužas, pag. -grauzos", "grauzties"), //izgrauzties, aizgrauzties
		VerbRule.firstConjRefl("-griežos, -griezies,", "-griežas, pag. -griezos", "griezties"), //aizgriezties 1, 2
		// H, I, J
		VerbRule.firstConjRefl("-jūdzos, -jūdzies,", "-jūdzas, pag. -jūdzos", "jūgties"), //aizjūgties
		// K
		VerbRule.firstConjRefl("-karos, -karies,", "-karas, pag. -kāros", "kārties"), //apkārties
		VerbRule.firstConjRefl("-klājos, -klājies,", "-klājas, pag. -klājos", "klāties"), //apklāties
		VerbRule.firstConjRefl("-kuļos, -kulies,", "-kuļas, pag. -kūlos", "kulties"), //aizkulties
		VerbRule.firstConjRefl("-ķēros, -ķeries,", "-ķeras, pag. -ķēros", "ķerties"), //aizķerties
		// L
		VerbRule.firstConjRefl("-laižos, -laidies,", "-laižas, pag. -laidos", "laisties"), //aizlaisties
		VerbRule.firstConjRefl("-laužos, -lauzies,", "-laužas, pag. -lauzos", "lauzties"), //aizlauzties
		VerbRule.firstConjRefl("-liedzos, -liedzies,", "-liedzas, pag. -liedzos", "liegties"), //aizliegties
		VerbRule.firstConjRefl("-liecos, -liecies,", "-liecas, pag. -liecos", "liekties"), //aizliekties
		VerbRule.firstConjRefl("-liekos, -liecies,", "-liekas, pag. -likos", "likties"), //aizlikties
		// M
		VerbRule.firstConjRefl("-metos, -meties,", "-metas, pag. -metos", "mesties"), //aizmesties
		// N
		VerbRule.firstConjRefl("-nesos, -nesies,", "-nesas, pag. -nesos", "nesties"), //aiznesties
		// Ņ
		VerbRule.firstConjRefl("-ņemos, -ņemies,", "-ņemas, pag. -ņemos", "ņemties"), //aizņemties
		// O, P, R, S, T, U, V, Z
				
				
		// Single case rules.
		new SimpleRule("-gulstos, -gulsties, -gulstas, arī -guļos, -gulies, -guļas, pag. -gūlos, arī -gulos", "gulties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"gulties\"", "Paralēlās formas"},
				null), //aizgulties
		new SimpleRule("-plešos, -pleties, -plešas, pag. -pletos, arī -plētos", "plesties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"plesties\"", "Paralēlās formas"},
				null), //ieplesties
		new SimpleRule("-tupstos, -tupsties, -tupstas, pag. -tupos", "tupties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"tupties\"", "Paralēlās formas"},
				null), //aiztupties
				//TODO check paralel forms.
				
		new SimpleRule("parasti 3. pers., -plešas, pag. -pletās, arī -plētās", "plesties", 18,
				new String[] {"Darbības vārds", "Locīt kā \"plesties\"", "Paralēlās formas"},
				new String[] {"Parasti 3. personā"}), //aizplesties

		
		/* Paradigm 20: Darbības vārdi 3. konjugācija atgriezeniski
		 */
		// Rules for both all person and third-person-only cases.
		VerbRule.thirdConjRefl("-dzenos, -dzenies,", "-dzenas, pag. -dzinos", "dzīties"), //aizdzīties
		VerbRule.thirdConjRefl("-kustos, -kusties,", "-kustas, pag. -kustējos", "kustēties"), //aizkustēties
		VerbRule.thirdConjRefl("-peros, -peries,", "-peras, pag. -pēros", "pērties"), //aizpērties
				
		// Single case rules.
		new SimpleRule("-mokos, -mokies, -mokās, arī -mocos, -mocies, -mocās, pag. -mocījos", "mocīties", 20,
				new String[] {"Darbības vārds", "Paralēlās formas"},
				null), //aizmocīties	
		new SimpleRule("parasti 3. pers., -lokās, pag. -locījās", "locīties", 20,
				new String[] {"Darbības vārds"},
				new String[] {"Parasti 3. personā"}), //aizlocīties
	};
	
	/**
	 * Known abbreviations and their de-abbreviations.
	 */
	public static  MappingSet<String, String> knownAbbr = AbbrMap.getAbbrMap();

	
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
		for (Rule s : simpleRulesDirect)
		{
			if (newBegin != -1) break;
			newBegin = s.applyDirect(gramText, lemma, paradigm, flags);
		}
		for (Rule s : simpleRulesOptHyperns)
		{
			if (newBegin != -1) break;
			newBegin = s.applyOptHyperns(gramText, lemma, paradigm, flags);
		}
		
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
			// Paradigm 25: Vietniekvārdi
			if (gramText.matches("ģen\\. -kā, dat\\. -kam, akuz\\., instr\\. -ko([.,;].*)?")) //daudzkas
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