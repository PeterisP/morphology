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
package lv.semti.morphology.attributes;

/**
 *
 * @author Pēteris Paikens
 *
 * Constants: attribute names and allowed values.
 */
public class AttributeNames {
	//FIXME - šeit jāpaliek tikai tām īpašībām, uz kurām nez kāpēc kodā pa tiešo atsaucas ārpus MarkejumaKonventora
	//Pārējo uz XML, un Konvertors loģikai jāstrādā no tā XML.
	//pie vārda pievienošanas dažādās izvēlnes varētu paņemt vnk vajadzīgo sarakstu no XML

	public final static String i_Word = "Vārds";
	//public final static String i_Tag = "Marķējums";

	public final static String v_No = "Nē";
	public final static String v_Yes = "Jā";
	public final static String v_NA = "Nepiemīt";
	public final static String v_Nelokaams = "Nelokāms";

	public final static String i_PartOfSpeech = "Vārdšķira";
	public final static String v_Punctuation = "Pieturzīme";
	public final static String v_Noun = "Lietvārds";
	public final static String v_Verb = "Darbības vārds";
	public final static String v_Participle = "Divdabis";
	public final static String v_Adjective = "Īpašības vārds";
	public final static String v_Pronoun = "Vietniekvārds";
	public final static String v_Adverb = "Apstākļa vārds";
	public final static String v_Preposition = "Prievārds";
	public final static String v_Conjunction = "Saiklis";
	public final static String v_Numeral = "Skaitļa vārds";
	public final static String v_Interjection = "Izsauksmes vārds";
	public final static String v_Abbreviation = "Saīsinājums";
	public final static String v_Particle = "Partikula";
	public final static String v_Residual = "Reziduālis";

	public final static String i_NounType = "Lietvārda tips";
	public final static String v_CommonNoun = "Sugas vārds";
	public final static String v_ProperNoun = "Īpašvārds";

	public final static String i_Gender = "Dzimte";
	public final static String v_Masculine = "Vīriešu";
	public final static String v_Feminine = "Sieviešu";
	public final static String v_Kopdzimte = "Kopdzimte";

	public final static String i_Number = "Skaitlis";
	public final static String v_Singular = "Vienskaitlis";
	public final static String v_Plural = "Daudzskaitlis";
	public final static String i_NumberSpecial = "Skaitlis 2";
	public final static String v_SingulareTantum = "Vienskaitlinieks";
	public final static String v_PlurareTantum = "Daudzskaitlinieks";

	public final static String i_Case = "Locījums";
	public final static String v_Nominative = "Nominatīvs";
	public final static String v_Genitive = "Ģenitīvs";
	public final static String v_Dative = "Datīvs";
	public final static String v_Accusative = "Akuzatīvs";
	public final static String v_Locative = "Lokatīvs";
	public final static String v_Vocative = "Vokatīvs";

	// public final static String i_CaseSpecial = "Locījums 2";

	public final static String i_Declension = "Deklinācija";
	public final static String v_Reflexive = "Atgriezenisks";
	public final static String v_InflexibleGenitive = "Ģenitīvenis";

	public final static String i_VerbType = "Darbības vārda tips";
	public final static String v_MainVerb = "Patstāvīgs darbības vārds";
	public final static String v_PaliigDv = "Palīgdarbības vārds";
	public final static String v_Modaals = "Modāls";
	public final static String v_Faazes = "Fāzes";
	public final static String v_IzpausmesVeida = "Izpausmes veida";
	public final static String v_Buut = "Palīgverbs 'būt'";
	public final static String v_TiktTapt = "Palīgverbi 'tikt' un 'tapt'";
	@Deprecated public final static String v_Nebuut = "'nebūt', 'trūkt' un 'pietikt'"; // Only in old Semti-kamols annotation

	public final static String i_Reflexive = "Atgriezeniskums";

	public final static String i_Izteiksme = "Izteiksme";
	public final static String v_Iisteniibas = "Īstenības";
	public final static String v_Atstaastiijuma = "Atstāstījuma";
	public final static String v_Veeleejuma = "Vēlējuma";
	public final static String v_Vajadziibas = "Vajadzības";
	public final static String v_VajadziibasAtstaastiijuma = "Vajadzības, atstāstījuma paveids";
	public final static String v_Paveeles = "Pavēles";
	public final static String v_Nenoteiksme = "Nenoteiksme";

	public final static String i_Laiks = "Laiks";
	public final static String v_Tagadne = "Tagadne";
	public final static String v_Naakotne = "Nākotne";
	public final static String v_Pagaatne = "Pagātne";

	public final static String i_Transitivity = "Transitivitāte";
	public final static String v_Transitive = "Pārejošs";
	public final static String v_Intransitive = "Nepārejošs";

	public final static String i_Konjugaacija = "Konjugācija";
	public final static String v_Nekaartns = "Nekārtns";

	public final static String i_Person = "Persona";

	public final static String i_Voice = "Kārta";
	public final static String v_Active = "Darāmā";
	public final static String v_Passive = "Ciešamā";

	public final static String i_Noliegums = "Noliegums";

	public final static String i_Lokaamiiba = "Lokāmība";
	public final static String v_Lokaams = "Lokāms";
	public final static String v_DaljeejiLokaams = "Daļēji lokāms";

	public final static String i_Definiteness = "Noteiktība";
	public final static String v_Indefinite = "Nenoteiktā";
	public final static String v_Definite = "Noteiktā";

	public final static String i_AdjectiveType = "Īpašības vārda tips";
	public final static String v_QualificativeAdjective = "Kādības";
	public final static String v_RelativeAdjective = "Attieksmes";

	public final static String i_Degree = "Pakāpe";
	public final static String v_Positive = "Pamata";
	public final static String v_Comparative = "Pārākā";
	public final static String v_Superlative = "Vispārākā";
	public static final String v_Relative = "Relatīvais"; //tikai apst.v

	public final static String i_VvTips = "Vietniekvārda tips";
	public final static String v_Personu = "Personu";
	public final static String v_Atgriezeniskie = "Atgriezeniskie";
	public final static String v_Piederiibas = "Piederības";
	public final static String v_Noraadaamie = "Norādāmie";
	public final static String v_Nenoteiktie = "Nenoteiktie";
	public final static String v_Jautaajamie = "Jautājamie";
	public final static String v_AttieksmesVv = "Attieksmes";
	public final static String v_Noteiktie = "Noteiktie";
	//public final static String v_Noliegtie = "Noliegtie";

	public final static String i_ApstTips = "Apstākļa vārda tips";
	public final static String v_Meera = "Mēra";
	public final static String v_Veida = "Veida";
	public final static String v_Vietas = "Vietas";
	public final static String v_Laika = "Laika";
	public final static String v_Ceelonja = "Cēloņa/nolūka";

	public final static String i_Novietojums = "Novietojums";
	public final static String v_Pirms = "Pirms";
	public final static String v_Peec = "Pēc";

	public final static String i_Rekcija = "Rekcija";

	public final static String i_SaikljaTips = "Saikļa tips";
	public final static String v_Sakaartojuma = "Sakārtojuma";
	public final static String v_Pakaartojuma = "Pakārtojuma";

	public final static String i_Uzbuuve = "Uzbūve";
	public final static String v_Vienkaarshs = "Vienkāršs";
	public static final String v_Divkaarshs = "Divkāršs";
	public final static String v_Salikts = "Salikts";
	public final static String v_Atkaartots = "Atkārtots";
	public final static String v_Savienojums = "Savienojums";

	public final static String i_SkaitljaTips = "Skaitļa vārda tips";
	public final static String v_PamataSv = "Pamata";
	public final static String v_Kaartas = "Kārtas";
	public final static String v_Daljskaitlis = "Daļskaitlis";

	public final static String i_Order = "Skaitļu kārta";
	public final static String v_Ones = "Vieni";
	public final static String v_Teens = "Padsmiti";
	public final static String v_Tens = "Desmiti";
	public final static String v_Hundreds = "Simti";
	public final static String v_Thousands = "Tūkstoši";
	public final static String v_Millions = "Miljoni";
	public final static String v_Billions = "Miljardi";

	public final static String i_VietasApstNoziime = "Vietas apstākļa nozīme";

	public static final String i_Anafora = "Anafora";
	public static final String v_Adjektiivu = "Aizstāj adjektīvu";
	public static final String v_Substantiivu = "Aizstāj substantīvu";

	public static final String i_PieturziimesTips = "Pieturzīmes tips";
	public static final String v_Komats = "Komats";
	public static final String v_Peedinja = "Pēdiņa";
	public static final String v_Punkts = "Punkts";
	public static final String v_Iekava = "Iekava";
	public static final String v_Domuziime = "Domuzīme";
	public static final String v_Kols = "Kols";
	public static final String v_Cita = "Cita";

	public static final String i_ResidualType = "Reziduāļa tips";
	public static final String v_Foreign = "Vārds svešvalodā";
	public static final String v_Typo = "Drukas kļūda";
	public static final String v_Number = "Skaitlis cipariem";
	public static final String v_Ordinal = "Kārtas skaitlis cipariem";
	public static final String v_URI = "URI";
	
	public static final String i_Guess = "Minēšana";
	public static final String v_NoGuess = "Nav";
	public static final String v_Deminutive = "Deminutīvu atvasināšana";
	public static final String v_Prefix = "Priedēkļu atvasināšana";
	public static final String v_Ending = "Minēšana pēc galotnes";
	
	public static final String i_CapitalLetters = "Lielo burtu lietojums";
	public static final String v_Lowercase = "Mazie burti";
	public static final String v_FirstUpper = "Sākas ar lielo burtu";
	public static final String v_AllUpper = "Rakstīts ar lielajiem burtiem";

	public static final String i_Lemma = "Pamatforma";
	public static final String i_SourceLemma = "Avota pamatforma";
    public static final String i_EntryID = "Šķirkļa ID";
    public static final String i_EntryName = "Šķirkļa cilvēklasāmais ID";
	public static final String i_Source = "Avots";   
	public static final String i_Role = "Loma";
	public static final String i_Prefix = "Priedēklis";
	public static final String i_Deminutive = "Deminutīvs";
	public static final String i_Mija = "Mija";
	
	public static final String i_ParadigmID = "Vārdgrupas nr";
	public static final String i_LexemeID = "Leksēmas nr";
	public static final String i_EndingID = "Galotnes nr";

	public static final String i_Generate = "Ģenerēt";

    public static final String i_WhitespaceBefore = "Atstarpes pirms";
    public static final String i_Offset = "Nobīde rindkopā";

    public static final String i_Frequency = "Biežums";
	public static final String v_Rare = "Reti";

    public static final String i_Usage = "Lietojums";
    public static final String v_Regional = "Apvidvārds";

    public static final String i_FormRestrictions = "Lieto tikai noteiktā formā/atvasinājumā";
    public static final String i_EntryProperties = "Šķirkļavārda īpatnības";
    public static final String i_TezaursCategory = "Kategorija";
    public static final String i_Domain = "Nozare";

    public static final String i_Other = "Citi";
    public static final String i_ProperNounType = "Īpašvārda veids";
    public static final String v_GivenName = "Priekšvārds";
    public static final String v_Surname = "Uzvārds";
    public static final String v_Toponym = "Vietvārds";

	// Vērtības, kuras īslaicīgi piekabina vārda objektam, lai GUI to īpaši atzīmētu
	public static final String i_Recommended = "Rādīt zaļu - čunkera rekomendācija";
	public static final String i_Tagged = "Tagera rekomendācija";

}