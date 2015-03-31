package lv.semti.Thesaurus.struct.gramlogic;

import lv.semti.Thesaurus.utils.MappingSet;

/**
 * Singleton class containing all known abbreviations and their
 * de-abbreviations.
 * @author Lauma
 *
 */
public class AbbrMap {
	protected static MappingSet<String, String> am;
	
	public static MappingSet<String, String> getAbbrMap()
	{
		if (am == null) am = populateMap();
		return am;
	}
	
	protected AbbrMap() {};
	
	protected static MappingSet<String, String> populateMap()
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
		res.put("agr.", "Agronomija");
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
		res.put("fotogr.", "Fotogrāfija");
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
		res.put("telev.", "Televīzija");
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
}
