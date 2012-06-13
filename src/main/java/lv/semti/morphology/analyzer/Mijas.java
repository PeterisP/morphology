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
	public static ArrayList<Variants> mijuVarianti (String celms, int mija) {
		return mijuVarianti(celms, mija, false);
	}
	
	public static ArrayList<Variants> mijuVarianti (String celms, int mija, boolean properName) {
		// procedūra, kas realizē visas celmu pārmaiņas - līdzskaņu mijas; darbības vārdu formas, utml.
		// TODO - iznest 'varianti.add(new Variants(... kā miniprocedūriņu.
		// TODO - iekļaut galotnē(?) kā metodi

		ArrayList<Variants> varianti = new ArrayList<Variants>(1);
		if (celms.trim().equals("")) return varianti;

		try {
			switch (mija) {
				case 0: varianti.add(new Variants(celms)); break;  // nav mijas

				case 1: // lietvārdu līdzskaņu mija
					// sākam ar izņēmumgadījumiem.
					// minēti gan pareizie celmi 'strupastis' gan ar neatļauto miju 'strupasša'.
					// .endsWith lietots, lai nav problēmas ar salikteņiem/priedēkļiem - vectētis utml.
					// vārdam 'viesis' ir fiksēts, lai strādā 'sieviete'->'sieviešu', latvietis->latviešu
					if (celms.equalsIgnoreCase("vies") || celms.equalsIgnoreCase("vieš") || celms.equalsIgnoreCase("cēs") || celms.equalsIgnoreCase("cēš") || celms.endsWith("tēt") || celms.endsWith("tēš") ||
							celms.endsWith("ast") || celms.endsWith("asš") || celms.endsWith("mat") || celms.endsWith("maš") ||
							celms.endsWith("skat") || celms.endsWith("skaš") || (celms.endsWith("st")&& ! celms.endsWith("kst")) ||	celms.endsWith("sš")) {
						varianti.add(new Variants(celms));
					}
					// Personvārdu mijas - Valdis-Valda; Gatis-Gata.  Vēl ir literatūrā minēts izņēmums -skis -ckis (Čaikovskis, Visockis), taču tiem tāpat viss šķiet ok.
					else if (properName && (celms.endsWith("t") || celms.endsWith("d"))) {
						varianti.add(new Variants(celms));
					}
					// tagad normālie gadījumi mijām
					else if (celms.endsWith("š")) {
						if (celms.endsWith("kš"))  varianti.add(new Variants(celms.substring(0,celms.length()-2)+"kst","Mija","kst -> kš"));
						else {
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"s","Mija","s -> š"));
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"t","Mija","t -> š"));
						}
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
							   celms.endsWith("s") || celms.endsWith("z") || celms.endsWith("n") || celms.endsWith("l") )
							 // acs, auss, utml izņēmumi
							 || (celms.endsWith("ac") || celms.endsWith("akti") || celms.endsWith("aus") || celms.equals("as") ||
							     celms.endsWith("bals") || celms.endsWith("brokast") || celms.endsWith("cēs") || celms.endsWith("dakt") ||
							     celms.endsWith("debes") || celms.endsWith("dzelz") || celms.endsWith("kūt") || celms.endsWith("makst") ||
							     celms.endsWith("pirt") || celms.endsWith("šalt") || celms.endsWith("takt") || celms.endsWith("ut") ||
							     celms.endsWith("valst") || celms.endsWith("vēst") || celms.endsWith("zos") || celms.endsWith("žult") ) )
						varianti.add(new Variants(celms));
					break;
				case 2: //  dv. 3. konjugācijas tagadne, kas noņem celma pēdējo burtu
					varianti.add(new Variants(celms+"ā"));
					
					if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cī")); //sacīt -> saku
					else varianti.add(new Variants(celms+"ī"));
					
					if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē")); //gulēt -> guļu
					else varianti.add(new Variants(celms+"ī"));
					
					varianti.add(new Variants(celms+"ē"));
					break;
				case 3: // īpašības vārdiem -āk- un vis-
					if (celms.endsWith("āk")) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative));
						else varianti.add(new Variants(celms.substring(0,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Comparative));
					} else varianti.add(new Variants(celms,AttributeNames.i_Degree, AttributeNames.v_Positive));
					break;
				case 4: // vajadzības izteiksmes jā-
					if (celms.startsWith("jā")) {varianti.add(new Variants(celms.substring(2,celms.length())));}
					break;
				case 5: // vajadzības izteiksme 3. konjugācijai
					if (celms.startsWith("jā") & celms.length()>3) {
						if (celms.endsWith("a")) {
							if (celms.endsWith("ina")) varianti.add(new Variants(celms.substring(2,celms.length()-1)+"ā"));
							else if (celms.endsWith("ka")) varianti.add(new Variants(celms.substring(2,celms.length()-2)+"cī")); // "saka"
							else varianti.add(new Variants(celms.substring(2,celms.length()-1)+"ī"));
						} else
							if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(2,celms.length()-1)+"cē")); // "māk->mācēt"
						    else if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(2,celms.length()-1)+"lē")); // "guļ"->"gulēt"
						    else varianti.add(new Variants(celms.substring(2,celms.length())+"ē"));
					}
					break;
				case 6: // 1. konjugācijas nākotne
					if (celms.endsWith("dī") || celms.endsWith("tī")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"s"));
					if (celms.endsWith("zī")) varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else varianti.add(new Variants(celms));
					break;
				case 7: // 1. konjugācijas 2. personas tagadne
					if (celms.endsWith("pi") || celms.endsWith("di") || celms.endsWith("ti"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					
					if (celms.endsWith("s")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š"));   //? gribētos piemēru
						varianti.add(new Variants(celms));   //  atnest -> atnes
					}
					else if (celms.endsWith("t")) {
						// tikai vārdiem 'mest' un 'cirst'. pārējiem visiem 2. personas tagadnei jābūt galā -i, piem. 'krīti', 'plūsti'
						if (celms.endsWith("met") || celms.endsWith("cērt")) varianti.add(new Variants(celms));
					}
					else if (celms.endsWith("d")) {
						//varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž")); // sēžu -> sēdi !!! nav 1. konjug!
						varianti.add(new Variants(celms)); // ēdu -> ēd
					}
					else if (celms.endsWith("l")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ļ"));
					else if (celms.endsWith("m") || celms.endsWith("b") || celms.endsWith("p"))	varianti.add(new Variants(celms+"j"));
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
				case 8: // -ams -āms 3. konjugācijai

					if (celms.endsWith("inā")) varianti.add(new Variants(celms)); // vārdam "mainās" ir beigās -inās, bet tam vajag -ā likumu
					if (celms.endsWith("kā")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cī")); //sacīt
					else if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ī"));
					else if (celms.endsWith("a")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ē"));
					break;
				case 9: // 3. konjugācija 3. pers. tagadne
					if (celms.endsWith("vajag")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzē"));
					else if (celms.endsWith("vajadz")) break; //izņēmums - lai korekti ir 'vajadzēt' -> 'vajag'
					else if (celms.endsWith("ina")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā"));
					else if (celms.endsWith("ka")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cī")); // "saka"
					else if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); // "māk->mācēt"
					else if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē")); // "guļ"->"gulēt"
					else if (celms.endsWith("a")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ī"));
					else varianti.add(new Variants(celms+"ē")); // if (celms.endsWith("i")) varianti.add(celms.substring(0,celms.length()-1)+"ē");
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
				case 12: // vajadzības izteiksme 3. konjugācijai atgriezeniskai
					if (celms.startsWith("jā") & celms.length()>3) {
						if (celms.endsWith("ā")) {
							if (celms.endsWith("inā")) varianti.add(new Variants(celms.substring(2,celms.length())));
							else varianti.add(new Variants(celms.substring(2,celms.length()-1)+"ī"));
						} else if (celms.endsWith("a"))
							varianti.add(new Variants(celms.substring(2,celms.length()-1)+"ē")); // bija bez -1 pie length. pielabots lai 'atcerēties' -> 'jāatceras' strādātu
					}
					break;
				case 13: // īpašības vārdiem -āk- un vis-, ar š->s nominatīva formā (zaļš -> zaļāks
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
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"g")); // ? kautkam ir bijis
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
			}
		} catch (StringIndexOutOfBoundsException e){
			try {
				new PrintStream(System.err, true, "UTF-8").printf(
						"StringIndexOutOfBounds, celms '%s', mija %d\n", celms, mija);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}

		return varianti;
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

	public static ArrayList<Variants> MijasLocīšanai (String celms, int mija, String trešāSakne, boolean pieliktVisPārākoPak, boolean properName) {
		// procedūra, kas realizē visas celmu pārmaiņas - līdzskaņu mijas; darbības vārdu formas, utml.
		// FIXME - nafig trešo sakni vajag???

		ArrayList<Variants> varianti = new ArrayList<Variants>(1);
		if (celms.trim().equals("")) return varianti;

		try {
			switch (mija) {
				case 0: varianti.add(new Variants(celms)); break;  // nav mijas

				case 1: // lietvārdu līdzskaņu mija
					if (celms.endsWith("vies") || (celms.endsWith("vieš") && !celms.endsWith("evieš")) || celms.equalsIgnoreCase("cēs") || celms.endsWith("tēt") || celms.endsWith("tēš") ||
							celms.endsWith("ast") || celms.endsWith("asš") || celms.endsWith("mat") || celms.endsWith("maš") ||
							celms.endsWith("skat") || celms.endsWith("skaš") || (celms.endsWith("st") && ! celms.endsWith("kst")) ||	celms.endsWith("sš")) {
						varianti.add(new Variants(celms));
					}
					// Personvārdu mijas - Valdis-Valda; Gatis-Gata. Eglīts - Eglīša.  Vēl ir literatūrā minēts izņēmums -skis -ckis (Čaikovskis, Visockis), taču tiem tāpat viss šķiet ok.
					else if (properName && celms.endsWith("t") && !celms.endsWith("īt")) {
						varianti.add(new Variants(celms));
						if (syllables(celms) > 1) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š","Mija","t -> š"));
					}
					else if (properName && celms.endsWith("d") ) {
						varianti.add(new Variants(celms));
						if (syllables(celms) > 1) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž","Mija","d -> ž"));
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
							   celms.endsWith("s") || celms.endsWith("z") || celms.endsWith("n") || celms.endsWith("l") )
							 // acs, auss, utml izņēmumi
							 || (celms.endsWith("ac") || celms.endsWith("akti") || celms.endsWith("aus") || celms.endsWith("as") ||
							     celms.endsWith("bals") || celms.endsWith("brokast") || celms.endsWith("cēs") || celms.endsWith("dakt") ||
							     celms.endsWith("debes") || celms.endsWith("dzelz") || celms.endsWith("kūt") || celms.endsWith("makst") ||
							     celms.endsWith("pirt") || celms.endsWith("šalt") || celms.endsWith("takt") || celms.endsWith("ut") ||
							     celms.endsWith("valst") || celms.endsWith("vēst") || celms.endsWith("zos") || celms.endsWith("žult") ) )
						varianti.add(new Variants(celms));
					break;
				case 2: //  dv. 3. konjugācijas tagadne, kas noņem celma pēdējo burtu
					//varianti.add(new Variants(celms+"ā"))
					//if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					if (celms.endsWith("cī")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k", "Garā", "ā")); //sacīt
					else if (celms.endsWith("ī") || celms.endsWith("inā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1), "Garā", "ā"));
					else varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					//varianti.add(new Variants(celms+"ē"));
					break;
				case 3: // īpašības vārdiem pieliekam -āk- un vis-
					varianti.add(new Variants(celms,AttributeNames.i_Degree,AttributeNames.v_Positive));
					varianti.add(new Variants(celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Superlative));
					break;
				case 4: // vajadzības izteiksmes jā-
					varianti.add(new Variants("jā" + celms));
					break;
				case 5: // vajadzības izteiksme 3. konjugācijai
					if (celms.endsWith("dā")) varianti.add(new Variants("jā" + celms.substring(0,celms.length()-1)));
					else if (celms.endsWith("ā")) varianti.add(new Variants("jā" + celms.substring(0,celms.length()-1)+"a"));
					else if (celms.endsWith("cī")) varianti.add(new Variants("jā" + celms.substring(0,celms.length()-2)+"ka")); // "saka"
					else if (celms.endsWith("ī")) varianti.add(new Variants("jā" + celms.substring(0,celms.length()-1)+"a"));
					else varianti.add(new Variants("jā" + celms.substring(0,celms.length()-1)));
					break;
				case 6: // 1. konjugācijas nākotne
					if (celms.endsWith("s")) {
						if (trešāSakne.endsWith("d")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dī"));
						else if (trešāSakne.endsWith("t")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"tī"));
						else if (trešāSakne.endsWith("s")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"sī"));
						else varianti.add(new Variants(celms));
					} else if (celms.endsWith("lūz")) {
						varianti.add(new Variants(celms+"ī"));
					}
					else varianti.add(new Variants(celms));
					break;
				case 7: // 1. konjugācijas 2. personas tagadne
					/*if (celms.endsWith("pi") || celms.endsWith("di") || celms.endsWith("ti"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else varianti.add(new Variants(celms));*/

					if (celms.endsWith("š") && trešāSakne.endsWith("s")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"s"));
					else if (celms.endsWith("š") && trešāSakne.endsWith("t")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š"));
					else if (celms.endsWith("ž")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"d"));
					else if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"l"));
					else if (celms.endsWith("mj") || celms.endsWith("bj") || celms.endsWith("pj"))	varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					else if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dz"));
//impossible  imho					else if (celms.endsWith("ž")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"z"));
					break;
				case 8: // -ams -āms 3. konjugācijai

					if (celms.endsWith("inā")) varianti.add(new Variants(celms, "Garā", "ā"));
					else if (celms.endsWith("cī")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"kā")); //sacīt
					else if (celms.endsWith("ī")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā", "Garā", "ā"));
					else if (celms.endsWith("ē")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else varianti.add(new Variants(celms));
					break;
				case 9: // 3. konjugācija 3. pers. tagadne
					if (celms.endsWith("dā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else if (celms.endsWith("cī")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ka")); // "saka"
					else if (celms.endsWith("ī")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else varianti.add(new Variants(celms.substring(0,celms.length()-1))); // if (celms.endsWith("i")) varianti.add(celms.substring(0,celms.length()-1)+"ē");
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
					//FIXME - g-dz laikam vajag arī 2. un 8. un 9. mijai...
					break;
				case 12: // vajadzības izteiksme 3. konjugācijai atgriezeniskai
					if (celms.endsWith("cī")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"kā", "Garā", "ā"));
					else if (celms.endsWith("ī") || celms.endsWith("inā"))
						varianti.add(new Variants("jā" + celms.substring(0,celms.length()-1)+"ā", "Garā", "ā"));
					else varianti.add(new Variants("jā" + celms.substring(0,celms.length()-1)+"a"));
					break;
				case 13: // īpašības vārdiem -āk-, ar š->s nominatīva formā (zaļš -> zaļāks
					varianti.add(new Variants(celms+"āk"));
					break;	
				case 14: // 1. konjugācijas "-is" forma
					if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
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
			}
		} catch (StringIndexOutOfBoundsException e){
			try {
				new PrintWriter(new OutputStreamWriter(System.err, "UTF-8")).printf(
						"StringIndexOutOfBounds, celms '%s', mija %d\n", celms, mija);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}

		return varianti;
	}
}
