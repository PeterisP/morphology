/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia; Author: Pēteris Paikens
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
package lv.semti.morphology.analyzer;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import lv.semti.morphology.attributes.AttributeNames;

public abstract class Mijas {
	public static ArrayList<Variants> mijuVarianti (String stem, int stemChange, boolean properName) {
		// procedūra, kas realizē visas celmu pārmaiņas - līdzskaņu mijas; darbības vārdu formas, utml.
		// TODO - iznest 'varianti.add(new Variants(... kā miniprocedūriņu.
		// TODO - iekļaut galotnē(?) kā metodi

		ArrayList<Variants> varianti = new ArrayList<Variants>(1);
		if (stem.trim().equals("")) return varianti;
		
		int mija;
		String celms;

		try {
			switch (stemChange) { //TODO - uz normālāku struktūru
			case 4: // vajadzības izteiksmes jā-
				if (stem.startsWith("jā") && stem.length() >= 4) {
					celms = stem.substring(2,stem.length());
					mija = 0;
				} else return varianti;
				break;
			case 5: // vajadzības izteiksme 3. konjugācijai
				if (stem.startsWith("jā") && stem.length() >= 4) {
					celms = stem.substring(2,stem.length());
					mija = 9;
				} else return varianti;
				break;
			case 12: // vajadzības izteiksme 3. konjugācijai atgriezeniskai
				if (stem.startsWith("jā") && stem.length() >= 4) {
					celms = stem.substring(2,stem.length());
					mija = 8;
				} else return varianti;
				break;
			case 19: // vajadzības_vēlējuma izteiksme 3. konjugācijai (jāmākot)
				if (stem.startsWith("jā") && stem.length() >= 4) {
					celms = stem.substring(2,stem.length());
					mija = 20;
				} else return varianti;
				break;
			default:
				celms = stem;
				mija = stemChange;
			}
			
			
			switch (mija) {
				case 0: varianti.add(new Variants(celms)); break;  // nav mijas

				case 1: // lietvārdu līdzskaņu mija
					// sākam ar izņēmumgadījumiem.
					// minēti gan pareizie celmi 'strupastis' gan ar neatļauto miju 'strupasša'.
					// .endsWith lietots, lai nav problēmas ar salikteņiem/priedēkļiem - vectētis utml.
					// vārdam 'viesis' ir fiksēts, lai strādā 'sieviete'->'sieviešu', latvietis->latviešu
					if (celms.equalsIgnoreCase("vies") || celms.equalsIgnoreCase("vieš") || celms.equalsIgnoreCase("cēs") || celms.equalsIgnoreCase("cēš") || celms.endsWith("tēt") || celms.endsWith("tēš") ||
							celms.endsWith("ast") || celms.endsWith("asš") || celms.endsWith("mat") || celms.endsWith("maš") ||
							celms.endsWith("skat") || celms.endsWith("skaš") || (celms.endsWith("st")&& ! celms.endsWith("kst")) ||	celms.endsWith("sš")
							 // acs, auss, utml izņēmumi
							 || (celms.endsWith("ac") || celms.endsWith("akti") || celms.endsWith("aus") || celms.equals("as") ||
							     celms.endsWith("bals") || celms.endsWith("brokast") || celms.endsWith("cēs") || celms.endsWith("dakt") ||
							     celms.endsWith("debes") || celms.endsWith("dzelz") || celms.endsWith("kūt") || celms.endsWith("makst") ||
							     celms.endsWith("pirt") || celms.endsWith("šalt") || celms.endsWith("takt") || celms.endsWith("ut") ||
							     celms.endsWith("valst") || celms.endsWith("vēst") || celms.endsWith("zos") || celms.endsWith("žult") ) ) {
						varianti.add(new Variants(celms));
					}
					// Personvārdu mijas - Valdis-Valda; Gatis-Gata.  Vēl ir literatūrā minēts izņēmums -skis -ckis (Čaikovskis, Visockis), taču tiem tāpat viss šķiet ok.
					else if (properName && (celms.endsWith("t") || celms.endsWith("d"))) {
						varianti.add(new Variants(celms));
					}
					// tagad normālie gadījumi mijām
					else if (celms.endsWith("š")) {
						if (celms.endsWith("kš"))
							varianti.add(new Variants(celms.substring(0,celms.length()-2)+"kst","Mija","kst -> kš"));
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"s","Mija","s -> š"));
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"t","Mija","t -> š"));
					}
					else if (celms.endsWith("ž")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"z","Mija","z -> ž"));
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"d","Mija","d -> ž"));
					}
	// ... dž <> dd ?????
					else if (celms.endsWith("č")) {varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c","Mija","c -> č"));}
					else if (celms.endsWith("ļ")) {
						if (celms.endsWith("šļ")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"sl","Mija","sl -> šļ"));}
						else if (celms.endsWith("žļ")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"zl","Mija","zl -> žļ"));}
						else if (celms.endsWith("ļļ")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ll","Mija","ll -> ļļ"));}
						else varianti.add(new Variants(celms.substring(0,celms.length()-1)+"l","Mija","l -> ļ"));
					}
					else if (celms.endsWith("ņ")) {
						if (celms.endsWith("šņ")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"sn","Mija","sn -> šņ"));}
						else if (celms.endsWith("žņ")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"zn","Mija","zn -> žņ"));}
						else if (celms.endsWith("ļņ")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ln","Mija","ln -> ļņ"));}
						else if (!(celms.endsWith("zņ") || celms.endsWith("sņ") || celms.endsWith("lņ"))) {
							//varianti.add(new Variants(celms.substring(0,celms.length()-1)+"l","Mija", "l -> ņ ??"));
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"n","Mija", "n -> ņ"));
						}
					}
					else if (celms.endsWith("j")) {
						if (celms.endsWith("pj") || celms.endsWith("bj") || celms.endsWith("mj") || celms.endsWith("vj"))
	//						 ... nj <> n ??
							{varianti.add(new Variants(celms.substring(0,celms.length()-1),"Mija","p->pj (u.c.)"));}
						else varianti.add(new Variants(celms));
					}
					else if (!(celms.endsWith("p") || celms.endsWith("b") || celms.endsWith("m") || celms.endsWith("v") ||
							   celms.endsWith("t") || celms.endsWith("d") || celms.endsWith("c") || celms.endsWith("z") ||
							   celms.endsWith("s") || celms.endsWith("z") || celms.endsWith("n") || celms.endsWith("l") ) )
						varianti.add(new Variants(celms));
					break;
				case 2: //  dv. 3. konjugācijas 2.pers un citu tagadne, kas noņem celma pēdējo burtu
					varianti.add(new Variants(celms+"ā"));
					
					if (celms.endsWith("sak") || celms.endsWith("slak") || celms.endsWith("slauk") || celms.endsWith("lok") || celms.endsWith("mok") || celms.endsWith("mok") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cī")); //sacīt -> saku
					else if (celms.endsWith("slog") || celms.endsWith("raug") || celms.endsWith("ļog") )
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzī")); //slodzīt -> slogu
					else varianti.add(new Variants(celms+"ī"));
					
					if (celms.endsWith("māk") || celms.endsWith("tek") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); //mācēt -> māku
					else if (celms.endsWith("vajag")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzē")); //vajadzēt -> vajag
					else varianti.add(new Variants(celms+"ē"));
					break;
				case 3: // īpašības vārdiem -āk- un vis-
					if (celms.endsWith("āk") && celms.length() > 3) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative));
						else varianti.add(new Variants(celms.substring(0,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Comparative));
					} else varianti.add(new Variants(celms,AttributeNames.i_Degree, AttributeNames.v_Positive));
					break;
				case 6: // 1. konjugācijas nākotne
					if (celms.endsWith("dī") || celms.endsWith("tī") || celms.endsWith("sī")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"s"));
					else if (celms.endsWith("zī")) varianti.add(new Variants(celms.substring(0,celms.length()-1))); // lūzt, griezt
					else if (!celms.endsWith("d") && !celms.endsWith("t") && !celms.endsWith("s") && !celms.endsWith("z")) varianti.add(new Variants(celms));
					break;
				case 7: // 1. konjugācijas 2. personas tagadne
					if (celms.endsWith("s")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š"));   //pievēršu -> pievērs
						varianti.add(new Variants(celms));   //  atnest -> atnes
					}
					else if (celms.endsWith("odi") || celms.endsWith("ūdi") || celms.endsWith("opi") || celms.endsWith("ūpi") || celms.endsWith("oti") || celms.endsWith("ūti") || celms.endsWith("īti") || celms.endsWith("sti")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else if (celms.endsWith("t")) {
						// tikai vārdiem 'mest' un 'cirst'. pārējiem visiem 2. personas tagadnei jābūt galā -i, piem. 'krīti', 'plūsti'
						if (celms.endsWith("met") || celms.endsWith("cērt")) varianti.add(new Variants(celms));
						else varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š"));  // pūšu -> pūt, ciešu -> ciet
					}
					else if (celms.endsWith("d")) {
						//tikai attiecīgajiem vārdiem, pārējiem visiem 2. personas tagadnei jābūt galā -i, piem. 'pazūdi', 'atrodi'
						if (celms.endsWith("dod") || celms.endsWith("ved") || celms.endsWith("ēd")) varianti.add(new Variants(celms)); 
					}
					else if (celms.endsWith("l")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ļ"));
					else if (celms.endsWith("s")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š")); // elšu -> elsis
					else if (!celms.endsWith("ņem") && (celms.endsWith("m") || celms.endsWith("b") || celms.endsWith("p")))	varianti.add(new Variants(celms+"j")); //stumju -> stum
					else if (celms.endsWith("c")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"k"));  // raku -> racis
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));  // veicu -> veicis
					}
//					else if (celms.endsWith("dz")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"g"));
					else if (celms.endsWith("z") && !celms.endsWith("dz")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž"));

					//TODO - šī nākamā rinda ir jātestē vai tok visos gadījumos. pielikta, jo "rok" overģenerēja kā arī bez mijas.
					else if (!(celms.endsWith("š") || celms.endsWith("ž") || celms.endsWith("ļ") //|| celms.endsWith("j")
							|| celms.endsWith("k") || celms.endsWith("g") ))
						varianti.add(new Variants(celms));
					break;
				case 8: // -ams -āms 3. konjugācijai, un arī mēs/jūs formas
					if (celms.endsWith("inā")) varianti.add(new Variants(celms)); // nav else, jo piemēram vārdam "mainās" arī ir beigās -inās, bet tam vajag -īties likumu;  
					if (celms.endsWith("sakā") || celms.endsWith("slakā") || celms.endsWith("slaukā") || celms.endsWith("lokā") || celms.endsWith("mokā") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cī")); //sacīt
					else if (celms.endsWith("slogā") || celms.endsWith("raugā") || celms.endsWith("ļogā") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dzī")); //slodzīt -> slogu
					else if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ī"));
					else if (celms.endsWith("māka") || celms.endsWith("teka") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); //mācēt -> mākam
					else if (celms.endsWith("guļa")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"lē")); //gulēt -> guļam
					else if (celms.endsWith("vajaga")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dzē")); //vajadzēt -> vajag
					else if (celms.endsWith("a")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ē"));
						if (!celms.endsWith("ina")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā"));
					}
					break;
				case 9: // 3. konjugācija 3. pers. tagadne
					if (celms.endsWith("vajadz")) break; //izņēmums - lai korekti ir 'vajadzēt' -> 'vajag'
					else if (celms.endsWith("ina")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā"));
					else if (celms.endsWith("saka") || celms.endsWith("slaka") || celms.endsWith("slauka") || celms.endsWith("loka") || celms.endsWith("moka") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cī")); //sacīt
					else if (celms.endsWith("sloga") || celms.endsWith("rauga") || celms.endsWith("ļoga") )
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzī")); //slodzīt -> slogu
					else if (celms.endsWith("māk") || celms.endsWith("tek") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); //mācēt -> māku
					else if (celms.endsWith("guļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē")); // "guļ"->"gulēt"
					else if (celms.endsWith("vajag")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzē")); //vajadzēt -> vajag
					else if (celms.endsWith("a")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ī"));
					else {
						varianti.add(new Variants(celms+"ē")); // if (celms.endsWith("i")) varianti.add(celms.substring(0,celms.length()-1)+"ē");
						varianti.add(new Variants(celms+"ā"));
					}
					break;
				case 10: // īpašības vārds -āk- un vis-, -i apstākļa formai
					if (celms.endsWith("i")) varianti.add(new Variants(celms.substring(0,celms.length()-1),AttributeNames.i_Degree, AttributeNames.v_Positive));
					if (celms.endsWith("āk")) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative));
						else varianti.add(new Variants(celms.substring(0,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Comparative));
					}
					break;
				case 11: // 3. konjugācijai -uša
					varianti.add(new Variants(celms));
					if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dz"));
					//FIXME - g-dz laikam vajag arī 2. un 8. un 9. mijai...
					break;
				case 13: // īpašības vārdiem -āk- un vis-, ar š->s nominatīva formā (zaļš -> zaļāks) ?? Lexicon.xml izskatās tikai pēc apstākļvārdu atvasināšanas?? FIXME, nešķiet tīri
					if (celms.endsWith("āk")) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative));
						else varianti.add(new Variants(celms.substring(0,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Comparative));
					}
					break;
				case 14: // 1. konjugācijas "-is" forma
					if (celms.endsWith("c")) { 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"k"));  // raku -> racis
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));  // veicu -> veicis
					} 
					else if (celms.endsWith("dz")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"g")); // sarūgu -> sarūdzis
						varianti.add(new Variants(celms)); // lūdzu -> lūdzis
					}
					else varianti.add(new Variants(celms));
					break;
				case 15: // pūzdams nopūzdamies t,d -> z mija
					varianti.add(new Variants(celms));
					if (celms.endsWith("z")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"t"));
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"d"));
					}					
					break;
				case 16: // 1. konjugācijas "-šana" atvasināšana
					if (!celms.endsWith("s") && !celms.endsWith("z")) {
						varianti.add(new Variants(celms));
						varianti.add(new Variants(celms+"s"));  // nest -> nešana
						varianti.add(new Variants(celms+"z"));  // mēzt -> mēšana
					}
					break;								
				case 17: // īsā sieviešu dzimtes vokatīva forma "kristīnīt!" "margriet!"
					if (syllables(celms) >= 2 || celms.endsWith("iņ") || celms.endsWith("īt")) 
						varianti.add(new Variants(celms));
					break;								
				case 18: // garā sieviešu dzimtes vokatīva forma "laura!" "margrieta!"
					if (syllables(celms) <= 2 && !celms.endsWith("iņ") && !celms.endsWith("īt")) 
						varianti.add(new Variants(celms));
					if (syllables(celms) > 1 && (celms.endsWith("kāj") || celms.endsWith("māj"))) 
						varianti.add(new Variants(celms));	
					break;								
				case 20: //  dv. 3. konjugācijas tagadne 1. personai un vajadzībai - atšķiras no 2. mijas tikai vārdam 'gulēt'
					varianti.add(new Variants(celms+"ā"));
					
					if (celms.endsWith("sak") || celms.endsWith("slak") || celms.endsWith("slauk") || celms.endsWith("lok") || celms.endsWith("mok") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cī")); //sacīt -> saku
					else if (celms.endsWith("slog") || celms.endsWith("raug") || celms.endsWith("ļog") )
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzī")); //slodzīt -> slogu
					else varianti.add(new Variants(celms+"ī"));
					
					if (celms.endsWith("guļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē")); //gulēt -> guļošs un arī gulošs
					if (celms.endsWith("māk") || celms.endsWith("tek") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); //mācēt -> māku
					else if (celms.endsWith("vajag")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzē")); //vajadzēt -> vajag
					else varianti.add(new Variants(celms+"ē"));
					break;
				/* TODO - nav realizēts
				case 21: // 3. konjugācijai -ušais noteiktā forma
					if (celms.endsWith("āk")) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative));
						else varianti.add(new Variants(celms.substring(0,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Comparative));
					}
					
					varianti.add(new Variants(celms));
					if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dz"));					
					
					break;
				*/
				case 22: // jaundzimušais -> jaundzimusī
					if (celms.endsWith("us")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"uš"));
					break;
				default:
					System.err.printf("Invalid StemChange ID, stem '%s', stemchange %d\n", celms, mija);
			}
		} catch (StringIndexOutOfBoundsException e){
			try {
				new PrintStream(System.err, true, "UTF-8").printf(
						"StringIndexOutOfBounds, celms '%s', mija %d\n", stem, stemChange);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}

		//verifikācija, vai variantu izlokot tiešām sanāk tas kas vajag. Varbūt ir slikts, jo čakarē performanci? TODO - performance check
		ArrayList<Variants> labieVarianti = new ArrayList<Variants>(varianti.size());
		for (Variants variants : varianti) {
			String trešāSakne = stem;
			if (stemChange == 6 && trešāSakne.endsWith("ī")) trešāSakne = trešāSakne.substring(0,trešāSakne.length()-1);
			ArrayList<Variants> atpakaļlocīti = MijasLocīšanai(variants.celms, stemChange, trešāSakne, variants.isMatchingStrong(AttributeNames.i_Degree, AttributeNames.v_Superlative), properName);
			boolean atrasts = false;
			for (Variants locītais : atpakaļlocīti) {
				if (locītais.celms.equalsIgnoreCase(stem)) atrasts = true;
			}
			
			if (!atrasts && Arrays.asList(1,2,5,7,8,9,17).contains(stemChange)) { //FIXME - varbūt performance dēļ tikai šiem stemChange ir jāloka varianti
				// System.err.printf("Celmam '%s' ar miju %d sanāca '%s' - noraidījām dēļ atpakaļlocīšanas verifikācijas.\n", stem, stemChange, variants.celms);
			} else {
				labieVarianti.add(variants);
				
				if (!atrasts && stemChange != 18 && stemChange != 20) { //debuginfo.     FIXME - 18. mijā neierobežojam, jo tur ir nesimetrija - vokatīvu silvij! atpazīstam bet neģenerējam. 20. mijā ir arī alternatīvas - guļošs un gulošs
					System.err.printf("Celms '%s' ar miju %d sanāca '%s'. Bet atpakaļ lokot:\n", stem, stemChange, variants.celms);
					for (Variants locītais : atpakaļlocīti) {
						System.err.printf("\t'%s'\n", locītais.celms);
					}
					if (varianti.size() <= 1) System.err.printf("\tCitu variantu nav.\n");
					else for (Variants variants2 : varianti) {
						if (variants != variants2) System.err.printf("\tVēl variants : '%s'\n", variants.celms); 
					}
				}				
			}			
		}
		
		return labieVarianti;
	}
	
	private static int syllables(String word) {
		int counter = 0;
		boolean in_vowel = false;
		HashSet<Character> vowels = new HashSet<Character>( Arrays.asList(new Character[] {'a','ā','e','ē','i','ī','o','u','ū'}));
		
		for (char c : word.toCharArray()) {
			if (!in_vowel && vowels.contains(c))
				counter++;
			in_vowel = vowels.contains(c);
		}
 		return counter;
	}

	/***
	 * procedūra, kas realizē visas celmu pārmaiņas - līdzskaņu mijas; darbības vārdu formas, utml.
	 * @param stem
	 * @param stemChange
	 * @param trešāSakne
	 * @param pieliktVisPārākoPak
	 * @param properName
	 * @return masīvs ar variantiem - FIXME - principā vajadzētu būt vienam; izņēmums ir pārākās/vispārākās formas
	 */
	public static ArrayList<Variants> MijasLocīšanai (String stem, int stemChange, String trešāSakne, boolean pieliktVisPārākoPak, boolean properName) {

		ArrayList<Variants> varianti = new ArrayList<Variants>(1);
		if (stem.trim().equals("")) return varianti;
		
		int mija;
		String celms;

		try {
			switch (stemChange) { //TODO - uz normālāku struktūru
			case 4: // vajadzības izteiksmes jā-
				celms = "jā" + stem;
				mija = 0;
				break;
			case 5: // vajadzības izteiksme 3. konjugācijai
				celms = "jā" + stem;
				mija = 9;
				break;
			case 12: // vajadzības izteiksme 3. konjugācijai atgriezeniskai
				celms = "jā" + stem;
				mija = 8;
				break;
			case 19: // vajadzības_vēlējuma izteiksme 3. konjugācijai (jāmākot)
				celms = "jā" + stem;
				mija = 20;
				break;
			default:
				celms = stem;
				mija = stemChange;
			}			
			
			switch (mija) {
				case 0: varianti.add(new Variants(celms)); break;  // nav mijas

				case 1: // lietvārdu līdzskaņu mija
					if ( (celms.endsWith("vies") && !celms.endsWith("kvies")) || (celms.endsWith("vieš") && !celms.endsWith("evieš")) || celms.equalsIgnoreCase("cēs") || celms.endsWith("tēt") || celms.endsWith("tēš") ||
							celms.endsWith("ast") || celms.endsWith("asš") || celms.endsWith("mat") || celms.endsWith("maš") ||
							celms.endsWith("skat") || celms.endsWith("skaš") || (celms.endsWith("st") && ! celms.endsWith("kst")) || celms.endsWith("sš")
							 // acs, auss, utml siev. dz. izņēmumi
							 || (celms.endsWith("ac") || celms.endsWith("akti") || celms.endsWith("aus") || celms.endsWith("as") ||
							     celms.endsWith("bals") || celms.endsWith("brokast") || celms.endsWith("cēs") || celms.endsWith("dakt") ||
							     celms.endsWith("debes") || celms.endsWith("dzelz") || celms.endsWith("kūt") || celms.endsWith("makst") ||
							     celms.endsWith("pirt") || celms.endsWith("šalt") || celms.endsWith("takt") || (celms.endsWith("ut") && !celms.endsWith("but"))||
							     celms.endsWith("valst") || celms.endsWith("vēst") || celms.endsWith("zos") || celms.endsWith("žult") )
							     ) {
						varianti.add(new Variants(celms));
					}
					// Personvārdu mijas - Valdis-Valda; Gatis-Gata. Eglīts - Eglīša.  Vēl ir literatūrā minēts izņēmums -skis -ckis (Čaikovskis, Visockis), taču tiem tāpat viss šķiet ok.
					else if (properName && celms.endsWith("t") && !celms.endsWith("īt")) {
						varianti.add(new Variants(celms));
						if (syllables(celms) > 1) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š","Mija","t -> š"));
					}
					else if (properName && celms.endsWith("d") ) {						
						if (syllables(celms) > 1) 
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž","Mija","d -> ž"));
						else varianti.add(new Variants(celms)); 
					}
					else if (celms.endsWith("s") || celms.endsWith("t")) {
						if (celms.endsWith("kst"))  varianti.add(new Variants(celms.substring(0,celms.length()-3)+"kš","Mija","kst -> kš"));
						else if (celms.endsWith("s")) {
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š","Mija","s -> š"));
						}
						else if (celms.endsWith("t")) {
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š","Mija","t -> š"));
						}
					}
					else if (celms.endsWith("z")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž","Mija","z -> ž"));
					}
					else if (celms.endsWith("d")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž","Mija","d -> ž"));
					}
					else if (celms.endsWith("c")) {varianti.add(new Variants(celms.substring(0,celms.length()-1)+"č","Mija","c -> č"));}
					else if (celms.endsWith("l")) {
						if (celms.endsWith("sl")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"šļ","Mija","sl -> šļ"));}
						else if (celms.endsWith("zl")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"žļ","Mija","zl -> žļ"));}
						else varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ļ","Mija","l -> ļ"));
					}
					else if (celms.endsWith("n")) {
						if (celms.endsWith("sn")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"šņ","Mija","sn -> šņ"));}
						else if (celms.endsWith("zn")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"žņ","Mija","zn -> žņ"));}
						else if (celms.endsWith("ln")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļņ","Mija","ln -> ļņ"));}
						else /*if (!(celms.endsWith("zņ") || celms.endsWith("sņ") || celms.endsWith("lņ")))*/ {
							//varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ņ","Mija", "l -> ņ ??"));
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ņ","Mija", "n -> ņ"));
						}
					}
					else if (celms.endsWith("p") || celms.endsWith("b") || celms.endsWith("m") || celms.endsWith("v")) {
						varianti.add(new Variants(celms+"j","Mija","p->pj (u.c.)"));
					}
					else if (!(celms.endsWith("p") || celms.endsWith("b") || celms.endsWith("m") || celms.endsWith("v") ||
							   celms.endsWith("t") || celms.endsWith("d") || celms.endsWith("c") || celms.endsWith("z") ||
							   celms.endsWith("s") || celms.endsWith("z") || celms.endsWith("n") || celms.endsWith("l") ) )
						varianti.add(new Variants(celms));
					break;
				case 2: //  dv. 3. konjugācijas tagadne, kas noņem celma pēdējo burtu
					if (celms.endsWith("sacī") || celms.endsWith("slacī") || celms.endsWith("slaucī") || celms.endsWith("locī") || celms.endsWith("mocī")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k", "Garā", "ā")); //sacīt
					else if (celms.endsWith("slodzī") || celms.endsWith("raudzī") || celms.endsWith("ļodzī") )
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //slodzīt -> slogu
					else if (celms.endsWith("mācē") || celms.endsWith("tecē") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k")); //mācēt -> māku
					else if (celms.endsWith("vajadzē")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //vajadzēt -> vajag
					else if (celms.endsWith("ī") || celms.endsWith("inā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1), "Garā", "ā"));
					else varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					break;
				case 3: // īpašības vārdiem pieliekam -āk- un vis-
					varianti.add(new Variants(celms,AttributeNames.i_Degree,AttributeNames.v_Positive));
					varianti.add(new Variants(celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Superlative));
					break;
				case 6: // 1. konjugācijas nākotne
					if (celms.endsWith("s")) {
						if (trešāSakne.endsWith("d")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dī"));
						else if (trešāSakne.endsWith("t")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"tī"));
						else if (trešāSakne.endsWith("s")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"sī"));
						else varianti.add(new Variants(celms));
					} else if (celms.endsWith("z")) {
						varianti.add(new Variants(celms+"ī"));
					}
					else varianti.add(new Variants(celms));
					break;
				case 7: // 1. konjugācijas 2. personas tagadne
					if (celms.endsWith("š") && trešāSakne.endsWith("s")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"s"));
					else if (celms.endsWith("š") && trešāSakne.endsWith("t")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"t"));
					else if (celms.endsWith("od") || celms.endsWith("ūd") || celms.endsWith("op") || celms.endsWith("ūp") || celms.endsWith("ot") || celms.endsWith("ūt") || celms.endsWith("īt") || celms.endsWith("st")) 
						varianti.add(new Variants(celms+"i"));
					else if (celms.endsWith("ž")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"d"));
					else if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"l"));
					else if (celms.endsWith("mj") || celms.endsWith("bj") || celms.endsWith("pj"))	varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					else if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dz"));
//impossible  imho					else if (celms.endsWith("ž")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"z"));
					else varianti.add(new Variants(celms));
					break;
				case 8: // -ams -āms 3. konjugācijai
					if (celms.endsWith("inā")) varianti.add(new Variants(celms, "Garā", "ā"));
					else if (celms.endsWith("sacī") || celms.endsWith("slacī") || celms.endsWith("slaucī") || celms.endsWith("locī") || celms.endsWith("mocī")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"kā", "Garā", "ā")); //sacīt
					else if (celms.endsWith("slodzī") || celms.endsWith("raudzī") || celms.endsWith("ļodzī") )
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"gā")); //slodzīt -> slogu
					else if (celms.endsWith("ī")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā", "Garā", "ā"));
					else if (celms.endsWith("mācē") || celms.endsWith("tecē") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ka")); //mācēt -> māk
					else if (celms.endsWith("gulē")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļa")); //gulēt -> guļam
					else if (celms.endsWith("vajadzē")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"ga")); //vajadzēt -> vajag					
					else if (celms.endsWith("ē")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else varianti.add(new Variants(celms));
					break;
				case 9: // 3. konjugācija 3. pers. tagadne
					if (celms.endsWith("dā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else if (celms.endsWith("sacī") || celms.endsWith("slacī") || celms.endsWith("slaucī") || celms.endsWith("locī") || celms.endsWith("mocī")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ka")); // "saka"
					else if (celms.endsWith("slodzī") || celms.endsWith("raudzī") || celms.endsWith("ļodzī") )
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"ga")); //slodzīt -> slogu
					else if (celms.endsWith("ī")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else if (celms.endsWith("mācē") || celms.endsWith("tecē") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k")); //mācēt -> māk
					else if (celms.endsWith("vajadzē")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //vajadzēt -> vajag					
					else if (celms.endsWith("gulē")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļ")); //gulēt -> guļ
					else varianti.add(new Variants(celms.substring(0,celms.length()-1))); 
					break;
				case 10: // īpašības vārds -āk- un vis-, -i apstākļa formai
					varianti.add(new Variants(celms,AttributeNames.i_Degree,AttributeNames.v_Positive));
					varianti.add(new Variants(celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Superlative));
					break;
				case 11: // 3. konjugācijai -uša
					if (celms.endsWith("c")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"k"));
					else if (celms.endsWith("dz")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"g"));
					else varianti.add(new Variants(celms));
					//FIXME - g-dz jānočeko
					break;
				case 13: // īpašības vārdiem -āk-, ar š->s nominatīva formā (zaļš -> zaļāks
					varianti.add(new Variants(celms+"āk"));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Superlative));
					break;	
				case 14: // 1. konjugācijas "-is" forma
					if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dz"));
					else varianti.add(new Variants(celms));
					break;			
				case 15: // pūzdams nopūzdamies t,d -> z mija
					if (celms.endsWith("t")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"z"));
					else if (celms.endsWith("d")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"z"));
					else varianti.add(new Variants(celms));
					break;
				case 16: // 1. konjugācijas "-šana" atvasināšana
					if (celms.endsWith("s") || celms.endsWith("z")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-1)));    // nest -> nešana
					else varianti.add(new Variants(celms)); 
					break;								
				case 17: // īsā sieviešu dzimtes vokatīva forma "kristīnīt!" "margriet!"
					if (syllables(celms) >= 2 && 
					  !(celms.endsWith("kāj") || celms.endsWith("māj")) ) 
						varianti.add(new Variants(celms));
					break;								
				case 18: // garā sieviešu dzimtes vokatīva forma "laura!" "margrieta!"
					if (syllables(celms) < 2 || // NB! te ir < 2 bet pie atpazīšanas <= 2 - ar 2 zilbēm pagaidām atpazīst abus un ģenerē vienu
						!(celms.endsWith("ij") || celms.endsWith("īn") || celms.endsWith("īt") || celms.endsWith("ān") || celms.endsWith("iņ") || celms.endsWith("ēn") || celms.endsWith("niec") || celms.endsWith("āj")) )	
						varianti.add(new Variants(celms));
					if (syllables(celms) > 1 && (celms.endsWith("kāj") || celms.endsWith("māj"))) 
						varianti.add(new Variants(celms));		
					break;								
				case 20: //  dv. 3. konjugācijas tagadne 1. personai un vajadzībai - atšķiras no 2. mijas tikai vārdam 'gulēt'
					if (celms.endsWith("sacī") || celms.endsWith("slacī") || celms.endsWith("slaucī") || celms.endsWith("locī") || celms.endsWith("mocī")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k", "Garā", "ā")); //sacīt
					else if (celms.endsWith("gulē")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļ")); //gulēt -> guļu
					else if (celms.endsWith("mācē") || celms.endsWith("tecē") ) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k")); //mācēt -> māku
					else if (celms.endsWith("vajadzē")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //vajadzēt -> vajag					
					else if (celms.endsWith("slodzī") || celms.endsWith("raudzī") || celms.endsWith("ļodzī") )
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //slodzīt -> slogu
					else if (celms.endsWith("ī") || celms.endsWith("inā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1), "Garā", "ā"));
					else varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					break;				
				case 22: // jaundzimušais -> jaundzimusī
					if (celms.endsWith("uš")) 
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"us"));
					break;
				default:
					System.err.printf("Invalid StemChange ID, stem '%s', stemchange %d\n", celms, mija);
			}
		} catch (StringIndexOutOfBoundsException e){
			try {
				new PrintWriter(new OutputStreamWriter(System.err, "UTF-8")).printf(
						"StringIndexOutOfBounds, celms '%s', mija %d\n", stem, stemChange);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}

		return varianti;
	}
}
