/*******************************************************************************
 * Copyright 2008, 2009, 2014, 2024 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens, Lauma Pretkalniņa
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
package lv.semti.morphology.analyzer;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;


public abstract class Mijas {
	public static ArrayList<Variants> mijuVarianti (String stem, int stemChange, boolean properName) {
		// procedūra, kas realizē visas celmu pārmaiņas - līdzskaņu mijas; darbības vārdu formas, utml.
		// TODO - iznest 'varianti.add(new Variants(... kā miniprocedūriņu.
		// TODO - iekļaut galotnē(?) kā metodi

		ArrayList<Variants> varianti = new ArrayList<>(1);
		if (stem.trim().isEmpty()) return varianti;

		int mija;
		String celms;

		try {
			switch (stemChange) { //TODO - uz normālāku struktūru
				case 4: // vajadzības izteiksmes jā-
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 0;
					} else return varianti;
					break;
				case 5: // vajadzības izteiksme 3. konjugācijai bez mijas
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 9;
					} else return varianti;
					break;
				case 12: // vajadzības izteiksme 3. konjugācijai atgriezeniskai bez mijas
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 8;
					} else return varianti;
					break;
				case 19: // vajadzības_vēlējuma izteiksme 3. konjugācijai bez mijas (jāmācot)
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 2;
					} else return varianti;
					break;
				case 28: // vajadzības_vēlējuma izteiksme 3. konjugācijai ar miju (jāmākot)
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 20;
					} else return varianti;
					break;
				case 29: // vajadzības izteiksme 3. konjugācijai atgriezeniskai ar miju
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 27;
					} else return varianti;
					break;
				case 31: // vajadzības izteiksme 3. konjugācijai ar miju
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 30;
					} else return varianti;
					break;
				case 37: // vajadzības izteiksme 1. konjugācijai ar miju (jāiet)
					if (stem.startsWith("jā") && stem.length() >= 4) {
						celms = stem.substring(2);
						mija = 36;
					} else return varianti;
					break;
				// latgalieši
				case 150: // vajadzības izteiksme 2. konjugācijai
					if (stem.startsWith("juo") && stem.length() >= 5) {
						celms = stem.substring(3);
						mija = 110;
					} else return varianti;
					break;
				case 151: // vajadzības izteiksme 3. konjugācijai -eit ar patskaņu miju bez līdzskaņu mijas
					if (stem.startsWith("juo") && stem.length() >= 5) {
						celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem.substring(3));
						mija = 119;
					} else return varianti;
					break;
				case 152: // vajadzības izteiksme 3. konjugācijai -eit ar patskaņu miju ar līdzskaņu miju
					if (stem.startsWith("juo") && stem.length() >= 5) {
						celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem.substring(3));
						mija = 122;
					} else return varianti;
					break;
				case 153: // vajadzības izteiksme 3. konjugācijai -ēt ar burtu miju bez līdzskaņu un patskaņu mijas
					if (stem.startsWith("juo") && stem.length() >= 5) {
						celms = stem.substring(3);
						mija = 125;
					} else return varianti;
					break;
				case 160: // patskaņu mija 2. konjugācijas supīnam, vēlējuma izteiksmei un pagātnes divdabim
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 114;
					break;
				case 161: // patskaņu mija 2. konjugācijas pagātnes divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 116;
					break;
				case 162: // patskaņu mija 3. konjugācijas standarta -eit tagadnei bez līdzskaņu mijas
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 119;
					break;
				case 163: // patskaņu mija 3. konjugācijas standarta -eit bez līdzskaņu mijas tagadnes divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 120;
					break;
				case 164: // patskaņu mija 3. konjugācijas standarta -eit tagadnei ar līdzskaņu miju
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 122;
					break;
				case 165: // patskaņu mija 3. konjugācijas standarta -eit līdzskaņu mijas tagadnes divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 123;
					break;
				case 166: // patskaņu mija 3. konjugācijas standarta -ēt 1. pers. tagadnei bez līdzskaņu un burtu mijas
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 124;
					break;
				case 167: // patskaņu mija 3. konjugācijas standarta -ēt bez līdzskaņu un burtu mijas tagadnes divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatkaņuMijaAtpakaļlocīšanai(stem);
					mija = 126;
					break;
				default:
					celms = stem;
					mija = stemChange;
			}


			switch (mija) {
				case 0: varianti.add(new Variants(celms)); break;  // nav mijas

				case 1: // lietvārdu līdzskaņu mija
					// sākam ar izņēmumgadījumiem.
					if (celms.endsWith("š")) {
						if (celms.endsWith("kš")) {
							varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "kst", "Mija", "kst -> kš"));
						}
						if (celms.endsWith("nš")) {
							varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "nst", "Mija", "nst -> nš"));
						}
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "s", "Mija", "s -> š"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "t", "Mija", "t -> š"));
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
						else if (celms.endsWith("ņņ")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"nn","Mija","nn -> ņņ"));}
						else if (!(celms.endsWith("zņ") || celms.endsWith("sņ") || celms.endsWith("lņ") || celms.endsWith("ņņ"))) {
							//varianti.add(new Variants(celms.substring(0,celms.length()-1)+"l","Mija", "l -> ņ ??"));
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"n","Mija", "n -> ņ"));
						}
					}
					else if (celms.endsWith("j")) {
						if (celms.endsWith("pj") || celms.endsWith("bj") || celms.endsWith("mj") || celms.endsWith("vj")
								|| celms.endsWith("fj")) { 	//	 ... nj <> n ??
							varianti.add(new Variants(celms.substring(0,celms.length()-1),"Mija","p->pj (u.c.)"));
						/*} else if (celms.endsWith("fj")) { // žirafju -> žirafe; žirafu->žirafe
							varianti.add(new Variants(celms.substring(0,celms.length()-1),"Mija","p->pj (u.c.)"));
							varianti.add(new Variants(celms));*/
						} else varianti.add(new Variants(celms));
					}
					else if (!(celms.endsWith("p") || celms.endsWith("b") || celms.endsWith("m") || celms.endsWith("v") ||
							celms.endsWith("t") || celms.endsWith("d") || celms.endsWith("c") || celms.endsWith("z") ||
							celms.endsWith("s") || celms.endsWith("n") || celms.endsWith("l") || celms.endsWith("f")) )
						varianti.add(new Variants(celms));
					break;
				case 2: //  dv. 3. konjugācijas (bezmiju!) formas, kas noņem celma pēdējo burtu
					varianti.add(new Variants(celms+"ā"));
					varianti.add(new Variants(celms+"ī"));
					varianti.add(new Variants(celms+"ē"));
					break;
				case 3: // īpašības vārdiem -āk- un vis-
					if (celms.endsWith("āk") && celms.length() > 3) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative));
						else varianti.add(new Variants(celms.substring(0,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Comparative));
					}
					varianti.add(new Variants(celms,AttributeNames.i_Degree, AttributeNames.v_Positive));
					break;
				case 6: // 1. konjugācijas nākotne
					if (celms.endsWith("dī") || celms.endsWith("tī") || celms.endsWith("sī")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"s"));
					else if (celms.endsWith("zī") || celms.endsWith("šī")) varianti.add(new Variants(celms.substring(0,celms.length()-1))); // lūzt, griezt
					else if (!celms.endsWith("d") && !celms.endsWith("t") && !celms.endsWith("s") && !celms.endsWith("z")) varianti.add(new Variants(celms));
					break;
				case 7: // 1. konjugācijas 2. personas tagadne
				case 23: // 1. konjugācijas 2. personas tagadne - ja pēc tam seko garā galotne kā -iet
					if (celms.endsWith("s")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š"));   //pievēršu -> pievērs
						varianti.add(new Variants(celms));   //  atnest -> atnes
					}
					else if ((mija == 7) && (celms.endsWith("odi") || celms.endsWith("ūdi") || celms.endsWith("opi") || celms.endsWith("ūpi") ||
							celms.endsWith("oti") || celms.endsWith("ūti") || celms.endsWith("īti") || celms.endsWith("ieti") || celms.endsWith("sti")))
						varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else if ((mija == 23) && (celms.endsWith("od") || celms.endsWith("ūd") || celms.endsWith("op") || celms.endsWith("ūp") ||
							celms.endsWith("ot") || celms.endsWith("ūt") || celms.endsWith("īt") || celms.endsWith("st")))
						varianti.add(new Variants(celms));
					else if (celms.endsWith("t")) {
						// tikai vārdiem 'mest' un 'cirst'. pārējiem visiem 2. personas tagadnei jābūt galā -i, piem. 'krīti', 'plūsti'
						if (celms.endsWith("met") || celms.endsWith("cērt")) varianti.add(new Variants(celms));
						else varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š"));  // pūšu -> pūt, ciešu -> ciet
					}
					else if (celms.endsWith("d")) {
						//tikai attiecīgajiem vārdiem, pārējiem visiem 2. personas tagadnei jābūt galā -i, piem. 'pazūdi', 'atrodi'
						if (celms.endsWith("dod") || celms.endsWith("ved") || celms.endsWith("ēd"))
							varianti.add(new Variants(celms));
						else varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž"));  // kožu -> kod
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
				case 8: // -ams -āms 3. konjugācijai bezmiju gadījumam, un arī mēs/jūs formas
					if (celms.endsWith("inā") || celms.endsWith("sargā")) varianti.add(new Variants(celms)); // nav else, jo piemēram vārdam "mainās" arī ir beigās -inās, bet tam vajag -īties likumu;
					if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ī"));
					if (celms.endsWith("a")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ē"));
						if (!celms.endsWith("ina") && !celms.endsWith("sarga")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā"));
					}
					break;
				case 9: // 3. konjugācija 3. pers. tagadne bez mijas
					if (celms.endsWith("ina") || celms.endsWith("sarga")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā")); // nav else, jo piemēram vārdam "jāmaina" arī ir beigās -ina, bet tam vajag -a nevis -ina likumu;
					if (celms.endsWith("a")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ī"));
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
				case 11: // -uša, arī mijas pie 1. konj noteiktās formas: veicu -> veikušais, beidzu->beigušais; raku -> rakušais; sarūgu -> sarūgušais;
					if (!celms.endsWith("c") && !celms.endsWith("dz")) {
						varianti.add(new Variants(celms));
					}
					if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dz"));
					break;
				case 13: // īpašības vārdiem -āk- un vis-, ar š->s nominatīva formā (zaļš -> zaļāks) ?? Lexicon.xml izskatās tikai pēc apstākļvārdu atvasināšanas?? FIXME, nešķiet tīri
					if (celms.endsWith("āk")) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative)); // FIXME te arī jāskatās vai ir -āk
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
				case 15: // pūst->pūzdams nopūzdamies s -> z mija
					varianti.add(new Variants(celms));  // šis pievienos arī pūst -> pūsdams; taču to pēc tam atpakaļlocīšana (kam būs info par pagātnes celmu) nofiltrēs
					if (celms.endsWith("z")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"s"));
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
				/*
				// 2025-06-12 Baiba izskaidro, ka pēc mūsdienu latviešu valodas
				// normām visiem 4. un 5. deklinācijas vārdiem pienākas viens
				// vienskaitļa vokatīvs, kas sakrīt vienskaitļa nominatīvu
				case 18: // garā sieviešu dzimtes vokatīva forma "laura!" "margrieta!"
					if (syllables(celms) <= 2 && !celms.endsWith("iņ") && !celms.endsWith("īt"))
						varianti.add(new Variants(celms));
					if (syllables(celms) > 1 && (celms.endsWith("kāj") || celms.endsWith("māj")))
						varianti.add(new Variants(celms));
					break;*/
				case 20: //  dv. 3. konjugācijas tagadnes mija 1. personas tagadnei, -ot divdabim un vajadzībai - atšķiras no 26. mijas 'gulēt' un 'tecēt'
					if (celms.endsWith("guļ") || celms.endsWith("gul")) // FIXME - dēļ 'gulošs' pieļaujam formu 'es gulu' ????  FIXME - Varbūt jāņem ārā, jo tagad ir divas paradigmas
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē")); //gulēt -> guļošs un arī gulošs
					if (celms.endsWith("k")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "cī")); //sacīt -> saku
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); //mācēt -> māku
					} else if (celms.endsWith("g") ) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dzī")); //slodzīt -> slogu
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzē")); //vajadzēt -> vajag
					} else if (celms.endsWith("ž") ) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dē")); //sēdēt -> sēžu
					}
					if (celms.endsWith("loc") || celms.endsWith("moc") || celms.endsWith("urc"))
						varianti.add(new Variants(celms+"ī")); // alternatīvā forma
					break;
				case 21: // -is -ušais pārākā un vispārākā pakāpe - visizkusušākais saldējums
					if (celms.startsWith("vis")) {
						varianti.add(new Variants(celms.substring(3), AttributeNames.i_Degree, AttributeNames.v_Superlative));
					} else {
						varianti.add(new Variants(celms, AttributeNames.i_Degree, AttributeNames.v_Comparative));
					}
					break;
				case 22: // jaundzimušais -> jaundzimusī
					if (celms.endsWith("us"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"uš"));
					break;
				case 24: //  analoģiski case 2, bet ar pārāko / vispārāko pakāpi - visizsakošākais
					String pakāpe = AttributeNames.v_Comparative;
					if (celms.startsWith("vis")) {
						pakāpe = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}
					varianti.add(new Variants(celms+"ā", AttributeNames.i_Degree, pakāpe));
					varianti.add(new Variants(celms+"ī", AttributeNames.i_Degree, pakāpe));
					varianti.add(new Variants(celms+"ē", AttributeNames.i_Degree, pakāpe));
					break;
				case 25: // analoģiski #8, bet ar pārākajām pakāpēm priekš -amāks formām
					pakāpe = AttributeNames.v_Comparative;
					if (celms.startsWith("vis")) {
						pakāpe = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}
					if (celms.endsWith("inā") || celms.endsWith("sargā")) varianti.add(new Variants(celms, AttributeNames.i_Degree, pakāpe)); // nav else, jo piemēram vārdam "mainās" arī ir beigās -inās, bet tam vajag -īties likumu;
					if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ī", AttributeNames.i_Degree, pakāpe));
					else if (celms.endsWith("a")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ē", AttributeNames.i_Degree, pakāpe));
						if (!celms.endsWith("ina") && !celms.endsWith("sarga")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā", AttributeNames.i_Degree, pakāpe));
					}
					break;
				case 26: //  dv. 3. konjugācijas miju gadījuma formas - otrās personas tagadne, pavēles izteiksme
					if (celms.endsWith("gul"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē")); // guli -> gulēt
					if (celms.endsWith("tec")) {
						varianti.add(new Variants(celms+"ē")); // teci -> tecēt
					} else if (celms.endsWith("k") && !celms.endsWith("tek")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "cī")); // saki -> sacīt
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); //māki -> mācēt
					} else if (celms.endsWith("g") ) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dzī")); //slogi -> slodzīt
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzē")); //vajag -> vajadzēt
					} else if (celms.endsWith("loc") || celms.endsWith("moc") || celms.endsWith("urc"))
						varianti.add(new Variants(celms+"ī")); // alternatīvā forma
					else varianti.add(new Variants(celms+"ē")); // sēdies -> sēdēties
					break;
				case 27: // -ams -āms 3. konjugācijai miju gadījumam, un arī mēs/jūs formas
					if (celms.endsWith("kā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cī")); //sacīt
					else if (celms.endsWith("gā") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dzī")); //slodzīt -> slogu
					else if (celms.endsWith("ka") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cē")); //mācēt -> mākam
					else if (celms.endsWith("ža") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dē")); //sēdēt -> sēžam
					else if (celms.endsWith("ļa")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"lē")); //gulēt -> guļam
					else if (celms.endsWith("ga")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dzē")); //vajadzēt -> vajag
					break;
				case 30: // 3. konjugācija 3. pers. tagadne ar miju
					if (celms.endsWith("vajadz")) break; //izņēmums - lai korekti ir 'vajadzēt' -> 'vajag'
					else if (celms.endsWith("ka") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cī")); //sacīt
					else if (celms.endsWith("ga"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dzī")); //slodzīt -> sloga
					else if (celms.endsWith("k") )
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē")); //mācēt -> māk
					else if (celms.endsWith("ž") )
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dē")); //sēdēt -> sēž
					else if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē")); // "guļ"->"gulēt"
					else if (celms.endsWith("vajag")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dzē")); //vajadzēt -> vajag
					break;
				case 32: //  analoģiski case 20, bet ar pārāko / vispārāko pakāpi - visizsakošākais
					pakāpe = AttributeNames.v_Comparative;
					if (celms.startsWith("vis")) {
						pakāpe = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}

					if (celms.endsWith("k") ) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "cī", AttributeNames.i_Degree, pakāpe)); //sacīt -> sakošākais
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"cē", AttributeNames.i_Degree, pakāpe)); //mācēt -> mākošākais
					} else if (celms.endsWith("g")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dzī", AttributeNames.i_Degree, pakāpe)); //slodzīt -> slogošākais
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dzē", AttributeNames.i_Degree, pakāpe)); //vajadzēt -> vajagošākais
					} else if (celms.endsWith("ž") ) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dē", AttributeNames.i_Degree, pakāpe)); //sēdēt -> sēžu
					} else if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"lē", AttributeNames.i_Degree, pakāpe)); //gulēt -> guļošākais un arī gulošākais
					break;
				case 33: // analoģiski #27, bet ar pārākajām pakāpēm priekš -amāks formām
					pakāpe = AttributeNames.v_Comparative;
					if (celms.startsWith("vis")) {
						pakāpe = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}
					if (celms.endsWith("kā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cī", AttributeNames.i_Degree, pakāpe)); //sacīt
					else if (celms.endsWith("gā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dzī", AttributeNames.i_Degree, pakāpe)); //slodzīt -> slogu
					else if (celms.endsWith("ka"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"cē", AttributeNames.i_Degree, pakāpe)); //mācēt -> mākam
					else if (celms.endsWith("ga"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dzē", AttributeNames.i_Degree, pakāpe)); //vajadzēt -> vajag
					else if (celms.endsWith("ža") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"dē", AttributeNames.i_Degree, pakāpe)); //sēdēt -> sēžam
					else if (celms.endsWith("guļa"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"lē", AttributeNames.i_Degree, pakāpe)); //gulēt -> guļam
					break;
				case 34: // īpašības vārdiem -āk- un vis- izskaņām kā -ajam: liekam nevis zaļ-š->zaļ-ajam, bet zaļ-š->zaļ-a-jam, bet pēdēj-ais -> pēdē-jam/pēdēj-a-jam
					if (celms.endsWith("āka") && celms.length() > 4) {
						if (celms.startsWith("vis")) varianti.add(new Variants(celms.substring(3,celms.length()-3),AttributeNames.i_Degree,AttributeNames.v_Superlative));
						else varianti.add(new Variants(celms.substring(0,celms.length()-3),AttributeNames.i_Degree,AttributeNames.v_Comparative));
					}
					if (celms.endsWith("a")) // zaļa-jam -> zaļ; pēdēja-jam -> pēdēj
						varianti.add(new Variants(celms.substring(0,celms.length()-1) ,AttributeNames.i_Degree, AttributeNames.v_Positive));
					else if (celms.endsWith("ē")) // pēdē-jam -> pēdēj
						varianti.add(new Variants(celms+"j",AttributeNames.i_Degree, AttributeNames.v_Positive));
					break;
				case 35: // substantivizējušos "īpašības vārdu" izskaņas kā -ajam: liekam nevis zaļ-š->zaļ-ajam, bet zaļ-š->zaļ-a-jam, bet pēdēj-ais -> pēdē-jam/pēdēj-a-jam; bez pārākās/vispārākās pakāpes
					if (celms.endsWith("a")) // zaļa-jam -> zaļ; pēdēja-jam -> pēdēj
						varianti.add(new Variants(celms.substring(0,celms.length()-1) ,AttributeNames.i_Degree, AttributeNames.v_Positive));
					else if (celms.endsWith("ē")) // pēdē-jam -> pēdēj
						varianti.add(new Variants(celms+"j",AttributeNames.i_Degree, AttributeNames.v_Positive));
					// citiem pareiziem variantiem IMHO te nevajadzētu būt
					break;
				case 36: // 'iet' speciālgadījums - normāli 3. personas tagadnei atbilstošais celms būtu 'ej', bet ir 'iet'.
					varianti.add(new Variants(celms));
					if (celms.endsWith("iet"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"ej"));
					break;
				case 38: // Apstākļa vārdi ar gradāciju
					if (celms.endsWith("āk") && celms.length() > 3) {
						if (celms.startsWith("vis")) {
							varianti.add(new Variants(celms.substring(3,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Superlative));
							varianti.add(new Variants(celms.substring(3,celms.length()-2) + "i",AttributeNames.i_Degree,AttributeNames.v_Superlative));
							varianti.add(new Variants(celms.substring(3,celms.length()-2) + "u",AttributeNames.i_Degree,AttributeNames.v_Superlative));
						} else {
							varianti.add(new Variants(celms.substring(0,celms.length()-2),AttributeNames.i_Degree,AttributeNames.v_Comparative));
							varianti.add(new Variants(celms.substring(0,celms.length()-2) + "i",AttributeNames.i_Degree,AttributeNames.v_Comparative));
							varianti.add(new Variants(celms.substring(0,celms.length()-2) + "u",AttributeNames.i_Degree,AttributeNames.v_Comparative));
						}
					} else varianti.add(new Variants(celms,AttributeNames.i_Degree, AttributeNames.v_Positive));
					break;

				// ------ LATGALIAN from here -----
				case 99: // puse no latgaliešu 'burtu mijas' (case 100) - tikai paradigmām, kur ir garantēts, ka bezmijas celms beidzas mīkstu līdzskani
					varianti.add(new Variants(ltgBurtuMijaAtpakaļViennoz(celms), "Mija", "ļņķģ -> lnkg"));
					break;
				case 100: // latgaliešu 'burtu mija', kur pirms -e, -i, -ī, -ē, -ie ļ, ņ, ķ, ģ kļūst par l, n, k, g
					String ltgBurtuMijasCelms = ltgBurtuMijaAtpakaļViennoz(celms);
					varianti.add(new Variants(ltgBurtuMijasCelms, "Mija", "lnkg -> lļnņkķgģ"));
					if (!ltgBurtuMijasCelms.equals(celms))
						varianti.add(new Variants(celms, "Mija", "lnkg -> lļnņkķgģ"));
					break;
				case 101: // latgaliešu līdzskaņu mija lietvārdiem, parastās galotnes (izņemot -i, -e, -ī, -ē, -ie, -ei)
					// Mijas no Leikumas "Vasals!"
					if (celms.endsWith("kš")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "kst", "Mija", "kst -> kš"));
					} else if (celms.endsWith("šļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "sl", "Mija", "sl -> šļ"));
					} else if (celms.endsWith("žļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "zl", "Mija", "zl -> žļ"));
					} else if (celms.endsWith("šm")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "sm", "Mija", "sm -> šm"));
					} else if (celms.endsWith("šņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "sn", "Mija", "sn -> šņ"));
					} else if (celms.endsWith("žņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "zn", "Mija", "zn -> žņ"));
					} else if (celms.endsWith("ļļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ll", "Mija", "ll -> ļļ"));
					} else if (celms.endsWith("ņņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "nn", "Mija", "nn -> ņņ"));
					} else if (celms.endsWith("č")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "c", "Mija", "c -> č"));
					} else if (celms.endsWith("ž")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "d", "Mija", "d -> ž"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "z", "Mija", "z -> ž"));
					} else if (celms.endsWith("š")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "t", "Mija", "t -> š"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "s", "Mija", "s -> š"));
						// Citas mijas
					} else if (celms.endsWith("ķ")) { // Andronovs?
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "k", "Mija", "k -> ķ"));
					} else if (celms.endsWith("ļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "l", "Mija", "l -> ļ"));
					} else if (celms.endsWith("ņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "n", "Mija", "n -> ņ"));
					} else {
						varianti.add(new Variants(celms));
					}
					break;
				case 102: // latgaliešu līdzskaņu mīkstināšana lietvārdiem, e, i, ē, ī, ie galotnes
					if (celms.endsWith("kš")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "kst", "Mija", "kst -> kš"));
					} else // Burtu miju ietekmētās vairāksimbolu mijas
					if (celms.endsWith("šl")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šļ", "Mija", "šļ -> šl"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "sl", "Mija", "sl -> šl"));
					} else if (celms.endsWith("žl")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "žļ", "Mija", "žļ -> žl"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "zl", "Mija", "zl -> žl"));
					} else if (celms.endsWith("šm")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šm", "Mija", "šm -> šm"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "sm", "Mija", "sn -> šn"));
					} else if (celms.endsWith("šn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šņ", "Mija", "šņ -> šn"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "sn", "Mija", "sn -> šn"));
					} else if (celms.endsWith("žn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "žņ", "Mija", "žņ -> žn"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "zn", "Mija", "zn -> žn"));
					} else if (celms.endsWith("ll")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ļļ", "Mija", "ļļ -> ll"));
					} else if (celms.endsWith("nn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ņņ", "Mija", "ņņ -> nn"));
						// Parastās, mijīgās, mijas
					} else if (celms.endsWith("č")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "c", "Mija", "c -> č"));
					} else if (celms.endsWith("š")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "t", "Mija", "t -> š"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "s", "Mija", "s -> š"));
					} else if (celms.endsWith("ž")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "d", "Mija", "d -> ž"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "z", "Mija", "z -> ž"));
						// Burtu miju ietekmētās viensimbola mijas
					} else if (celms.endsWith("l")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ļ", "Mija", "ļ -> l"));
					} else if (celms.endsWith("n")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ņ", "Mija", "ņ -> n"));
					} else if (celms.endsWith("k")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ķ", "Mija", "ķ -> k"));
					} else if (celms.endsWith("g")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ģ", "Mija", "ģ -> g"));
					} else {
						varianti.add(new Variants(celms));
					}
					break;
				case 103: // līdzīgi `case 3` - īpašības vārdiem -uok- un vys- / vysu-
					if (celms.endsWith("uok") && celms.length() > 3) {
						if (celms.startsWith("vysu"))
							varianti.add(new Variants(celms.substring(4, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else if (celms.startsWith("vys"))
							varianti.add(new Variants(celms.substring(3, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else
							varianti.add(new Variants(celms.substring(0, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Comparative)));
					}
					varianti.add(new Variants(celms, ltgDegreeFlags(AttributeNames.v_Positive)));
					break;
				case 104: //  īpašības vārdiem -uok- un vys- / vysu-, pamata pakāpei burtu mija
					if (celms.endsWith("uok") && celms.length() > 3) {
						if (celms.startsWith("vysu"))
							varianti.add(new Variants(celms.substring(4, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else if (celms.startsWith("vys"))
							varianti.add(new Variants(celms.substring(3, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else
							varianti.add(new Variants(celms.substring(0, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Comparative)));
					} else {
						varianti.add(new Variants(ltgBurtuMijaAtpakaļViennoz(celms), ltgDegreeFlags(AttributeNames.v_Positive)));
					}
					break;
				case 105: // līdzīgi kā `case 34` - īpašības vārdiem -uok- un vys- / vysu- izskaņām kā -ajam: liekam nevis moz-s->moz-ajam, bet moz-s->moz-a-jam, bet senej-ais -> sene-jam/senej-a-jam
					if (celms.endsWith("uoka") && celms.length() > 4) {
						if (celms.startsWith("vysu"))
							varianti.add(new Variants(celms.substring(4, celms.length()-4), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else if (celms.startsWith("vys"))
							varianti.add(new Variants(celms.substring(3, celms.length()-4), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else
							varianti.add(new Variants(celms.substring(0, celms.length()-4), ltgDegreeFlags(AttributeNames.v_Comparative)));
					}
					if (celms.endsWith("a")) // moz-jam -> moz; seneja-jam -> senej
						varianti.add(new Variants(celms.substring(0, celms.length()-1), ltgDegreeFlags(AttributeNames.v_Positive)));
					else if (celms.endsWith("ē") || celms.endsWith("e")) // sene-jam -> senej
						varianti.add(new Variants(celms+"j", ltgDegreeFlags(AttributeNames.v_Positive)));
					break;
				case 106: // līdzīgi `case 13` - apstākļa vārdiem -uok- un vys- / vysu-
					if (celms.endsWith("uok") && celms.length() > 3) {
						if (celms.startsWith("vysu"))
							varianti.add(new Variants(celms.substring(4, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else if (celms.startsWith("vys"))
							varianti.add(new Variants(celms.substring(3, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
						else
							varianti.add(new Variants(celms.substring(0, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Comparative)));
					}
					break;
				case 107: // latgaliešu 'burtu mijas' inverss - kad pamatformas galotne ir -e, -i, -ī, -ē, -ie, un l, n, k, g kļūst par ļ, ņ, ķ, ģ pirms citām galotnēm (slapnis)
					varianti.add(new Variants(ltgBurtuMija(celms)));
					break;
				case 108: // 107 + 106 priekš slapnis
					if (celms.endsWith("uok") && celms.length() > 3) {
						if (celms.startsWith("vysu"))
							varianti.add(new Variants(
									ltgBurtuMija(celms.substring(4, celms.length()-3)),
									ltgDegreeFlags(AttributeNames.v_Superlative)));
						else if (celms.startsWith("vys"))
							varianti.add(new Variants(
									ltgBurtuMija(celms.substring(3, celms.length()-3)),
									ltgDegreeFlags(AttributeNames.v_Superlative)));
						else
							varianti.add(new Variants(
									ltgBurtuMija(celms.substring(0, celms.length()-3)),
									ltgDegreeFlags(AttributeNames.v_Comparative)));
					}
					break;
				case 109: // Apstākļa vārdi ar gradāciju, bet bez burtu mijas
					if (celms.endsWith("uok") && celms.length() > 4) {
						if (celms.startsWith("vysu")) {
							varianti.add(new Variants(celms.substring(4, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants(celms.substring(4, celms.length()-3) + "i", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants(celms.substring(4, celms.length()-3) + "a", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants(celms.substring(4, celms.length()-3) + "ai", ltgDegreeFlags(AttributeNames.v_Superlative)));
						} else if (celms.startsWith("vys")) {
							varianti.add(new Variants(celms.substring(3, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants(celms.substring(3, celms.length()-3) + "i", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants(celms.substring(3, celms.length()-3) + "a", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants(celms.substring(3, celms.length()-3) + "ai", ltgDegreeFlags(AttributeNames.v_Superlative)));
						} else {
							varianti.add(new Variants(celms.substring(0, celms.length()-3), ltgDegreeFlags(AttributeNames.v_Comparative)));
							varianti.add(new Variants(celms.substring(0, celms.length()-3) + "i", ltgDegreeFlags(AttributeNames.v_Comparative)));
							varianti.add(new Variants(celms.substring(0, celms.length()-3) + "a", ltgDegreeFlags(AttributeNames.v_Comparative)));
							varianti.add(new Variants(celms.substring(0, celms.length()-3) + "ai", ltgDegreeFlags(AttributeNames.v_Comparative)));
						}
					} else varianti.add(new Variants(celms, ltgDegreeFlags(AttributeNames.v_Positive)));
					break;
				// Te sākas verbu mijas
				case 110: // 2. konjugācija, vienkāršās tagadnes mija
					if (celms.endsWith("e")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ei", "Mija", "ei -> e"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ē", "Mija", "ē -> e"));
					} else if (celms.endsWith("o")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "uo", "Mija", "uo -> o"));
					}
					break;
				case 111: // 2. konjugācija, vienkāršās pagātnes vsk. 1., 2. pers. mija
					if (celms.endsWith("uoj")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1), "Mija", "uo -> uoj"));
					} else if (celms.endsWith("ov")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "uo", "Mija", "uo -> ov"));
					} else if (celms.endsWith("iej")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 3) + "ē", "Mija", "ē -> iej"));
					} else if (celms.endsWith("ej")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ie", "Mija", "ei -> ej"));
					}
					break;
				case 112: // 2. konjugācija, vienkāršās pagātnes dsk. un 3. pers. mija
					if (celms.endsWith("uoj")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1), "Mija", "uo -> uoj"));
					} else if (celms.endsWith("ov")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "uo", "Mija", "uo -> ov"));
					} else if (celms.endsWith("ēj")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1), "Mija", "ē -> ēj"));
					} else if (celms.endsWith("ej")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ie", "Mija", "ei -> ej"));
					}
					break;
				case 113: // 2. konjugācija, vienkāršās nākotnes vsk. 1., 2. pers. mija
					if (celms.endsWith("ie")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ē", "Mija", "ē -> ie"));
					} else if (celms.endsWith("uo") || celms.endsWith("ei")) {
						varianti.add(new Variants(celms));
					}
					break;
				case 114: // 2. konjugācija, vēlējuma izteiksme, supīns un ciešamās kārtas pagātnes (-ts) divdabis, dams divdabis
					if (celms.endsWith("ā")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ē", "Mija", "ē -> ā"));
					} else if (celms.endsWith("uo") || celms.endsWith("ei")) {
						varianti.add(new Variants(celms));
					}
					break;
				case 115: // 2. konjugācija, divdabju formu vispārākā pakāpe (121.) + tagadnes mija (110.)
					String degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}

					if (celms.endsWith("e")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ei", ltgDegreeFlags(degree)));
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ē", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("o")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "uo", ltgDegreeFlags(degree)));
					}
					break;
				case 116: // 2. konjugācija, divdabju formu vispārākā pakāpe (121.) + vēlējuma/supīna mija (114.) (-ts divdabim)
					degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}

					if (celms.endsWith("ā")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ē", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("uo") || celms.endsWith("ei")) {
						varianti.add(new Variants(celms, ltgDegreeFlags(degree)));
					}
					break;
				case 117:  // 2. konjugācija, pagātnes mija -s, -use divdabim (vienkāršota 111.)
					if (celms.endsWith("uo")) {
						varianti.add(new Variants(celms));
					} else if (celms.endsWith("ie")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ē", "Mija", "ē -> ie"));
					} else if (celms.endsWith("e")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ie", "Mija", "ei -> e"));
					}
					break;
				case 118: // 2. konjugācija, divdabju formu vispārākā pakāpe (121.) + pag. mija -s, -use divdabim (117.)
					degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}

					if (celms.endsWith("ie")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ē", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("e")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ie", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("uo")) {
						varianti.add(new Variants(celms, ltgDegreeFlags(degree)));
					}
					break;
				case 119: // 3. konjugācija, standarta -eit, tagadne un pagātne (līdzskaņu mijas nekad nav)
					varianti.add(new Variants(celms + "ei", "Mija", "ei -> "));
					break;
				case 120: // 3. konjugācija, standarta -eit bez līdskaņu mijas, divdabju formu vispārākā pakāpe + tagadnes un pagātnes mija (119.)
					degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}
					varianti.add(new Variants(celms + "ei", ltgDegreeFlags(degree)));
					break;
				case 121: // 3. konjugācija, divdabju formu vispārākā pakāpe bez mijas
					degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}
					varianti.add(new Variants(celms, ltgDegreeFlags(degree)));
					break;
				case 122: // 3. konjugācija, standarta -eit, tagadne ar līdzskaņu miju
					if (celms.endsWith("ld")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ļdei", "Mija", "ļdei -> ld"));
					} else if (celms.endsWith("nd")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ņdei", "Mija", "ņdei -> nd"));
					} else if (celms.endsWith("g")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dzei", "Mija", "dzei -> g"));
					} else if (celms.endsWith("k")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "cei", "Mija", "cei -> k"));
					} else if (celms.endsWith("ļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "lei", "Mija", "lei -> ļ"));
					} else if (celms.endsWith("ņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "nei", "Mija", "nei -> ņ"));
					}
					break;
				case 123: // 3. konjugācija, standarta -eit ar līdskaņu miju, divdabju formu vispārākā pakāpe + tagadnes mija (122.)
					degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}

					if (celms.endsWith("ld")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ļdei", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("nd")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ņdei", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("g")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "dzei", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("k")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "cei", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("ļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "lei", ltgDegreeFlags(degree)));
					} else if (celms.endsWith("ņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "nei", ltgDegreeFlags(degree)));
					}
					break;
				case 124: // 3. konjugācija, standarta -ēt, tagadne un pagātne bez līdskaņu un burtu mijas
					varianti.add(new Variants(celms + "ē", "Mija", "ē -> "));
					break;
				case 125: // 3. konjugācija, standarta -ēt, tagadne bez līdskaņu mijas ar burtu miju
					varianti.add(new Variants(ltgBurtuMija(celms) + "ē", "Mija", "ē -> "));
					break;
				case 126: // 3. konjugācija, standarta -ēt bez līdskaņu un burtu mijas, divdabju formu vispārākā pakāpe + tagadnes un pagātnes mija (119.)
					degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}
					varianti.add(new Variants(celms + "ē", ltgDegreeFlags(degree)));
					break;
				case 127: // 3. konjugācija, standarta -ēt bez līdskaņu mijas ar inverso burtu miju, divdabju formu vispārākā pakāpe + tagadnes un pagātnes mija (119.)
					degree = AttributeNames.v_Comparative;
					if (celms.startsWith("vysu")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(4);
					} else if (celms.startsWith("vys")) {
						degree = AttributeNames.v_Superlative;
						celms = celms.substring(3);
					}
					varianti.add(new Variants(ltgBurtuMija(celms) + "ē", ltgDegreeFlags(degree)));
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

		return varianti;
	}

	public static boolean atpakaļlocīšanasVerifikācija(Variants variants, String stem, int stemChange, String trešāSakne, boolean properName) {
		// Verifikācija, vai variantu izlokot tiešām sanāk tas kas vajag.

		if (Arrays.asList(18,20,34,35).contains(stemChange)) {
			// 18. mijā neierobežojam, jo tur ir nesimetrija - vokatīvu silvij! atpazīstam bet neģenerējam.
			// 20. mijā ir arī alternatīvas - guļošs un gulošs
			// 34/35 mijā - pēdējajam, zaļoksnējajam atpazīstam bet neģenerējam
			return true;
		}

		if (stemChange == 6 && trešāSakne.endsWith("ī")) trešāSakne = trešāSakne.substring(0, trešāSakne.length()-1);
		ArrayList<Variants> atpakaļlocīti = MijasLocīšanai(variants.celms, stemChange, trešāSakne, variants.isMatchingStrong(AttributeNames.i_Degree, AttributeNames.v_Superlative), properName);
		boolean atrasts = false;
		for (Variants locītais : atpakaļlocīti) {
			if (locītais.celms.equalsIgnoreCase(stem))
			{
				atrasts = true;
				break;
			}
		}

		if (!atrasts && Arrays.asList(1,2,5,6,7,8,9,14,15,17,23,26,36,37).contains(stemChange)) {
			if (stemChange == 7 && variants.celms.endsWith("dod")) return true; // izņēmums, ka "dodi" atpazīst bet neģenerē
			if (properName) {
				// pie atpazīšanas properName var būt nepareizs, jo lielie burti ir arī citos gadījumos - teikuma sākumā utt
				return atpakaļlocīšanasVerifikācija(variants, stem, stemChange, trešāSakne, false);
			}

			// System.err.printf("Celmam '%s' ar miju %d sanāca '%s' - noraidījām dēļ atpakaļlocīšanas verifikācijas.\n", stem, stemChange, variants.celms);
			return false;
		} else {
			if (!atrasts) { //debuginfo.
				// FIXME - šo principā vajadzētu realizēt kā karodziņu - ka ieliekam Variant klasē zīmi, ka šis ir neiesakāms, un tad nebrīnamies, ja ģenerācija to neiedod; vai arī lai ģenerācija dod tos variantus ar tādu karodziņu un tad šeit tos ieraugam
				System.err.printf("Celms '%s' ar miju %d sanāca '%s'. Bet atpakaļ lokot:\n", stem, stemChange, variants.celms);
				for (Variants locītais : atpakaļlocīti) {
					System.err.printf("\t'%s'\n", locītais.celms);
				}
			}
			return true;
		}
	}

	private static int syllables(String word) {
		int counter = 0;
		boolean in_vowel = false;
		HashSet<Character> vowels = new HashSet<>( Arrays.asList(new Character[] {'a','ā','e','ē','i','ī','o','u','ū'}));

		for (char c : word.toCharArray()) {
			if (!in_vowel && vowels.contains(c))
				counter++;
			in_vowel = vowels.contains(c);
		}
		return counter;
	}

	/***
	 * procedūra, kas realizē visas celmu pārmaiņas - līdzskaņu mijas; darbības vārdu formas, utml. *vārdu ģenerēšanai*
	 * @param stem
	 * @param stemChange
	 * @param trešāSakne
	 * @param pieliktVisPārākoPak
	 * @param properName
	 * @return masīvs ar variantiem - FIXME - principā vajadzētu būt vienam; izņēmums ir pārākās/vispārākās formas
	 */
	public static ArrayList<Variants> MijasLocīšanai (String stem, int stemChange, String trešāSakne, boolean pieliktVisPārākoPak, boolean properName) {

		ArrayList<Variants> varianti = new ArrayList<>(1);
		if (stem.trim().isEmpty()) return varianti;

		int mija;
		String celms;

		try {
			switch (stemChange) { //TODO - uz normālāku struktūru
				case 4: // vajadzības izteiksmes jā-
					celms = "jā" + stem;
					mija = 0;
					break;
				case 5: // vajadzības izteiksme 3. konjugācijai bez mijas
					celms = "jā" + stem;
					mija = 9;
					break;
				case 12: // vajadzības izteiksme 3. konjugācijai atgriezeniskai bez mijas
					celms = "jā" + stem;
					mija = 8;
					break;
				case 19: // vajadzības_vēlējuma izteiksme 3. konjugācijai bez mijas (jāmācot)
					celms = "jā" + stem;
					mija = 2;
					break;
				case 28: // vajadzības_vēlējuma izteiksme 3. konjugācijai ar miju (jāmākot)
					celms = "jā" + stem;
					mija = 20;
					break;
				case 29: // vajadzības izteiksme 3. konjugācijai atgriezeniskai ar miju
					celms = "jā" + stem;
					mija = 27;
					break;
				case 31: // vajadzības izteiksme 3. konjugācijai ar miju
					celms = "jā" + stem;
					mija = 30;
					break;
				case 37: // vajadzības izteiksme 1. konjugācijai ar miju
					celms = "jā" + stem;
					mija = 36;
					break;
				// latgalieši
				case 150: // vajadzības izteiksme 2. konjugācijai
					celms = "juo" + stem;
					mija = 110;
					break;
				case 151: // vajadzības izteiksme 3. konjugācijai -eit ar patskaņu miju bez līdzskaņu mijas
					celms = "juo" + ltgPatskaņuMijaLocīšanai(stem);
					mija = 119;
					break;
				case 152: // vajadzības izteiksme 3. konjugācijai -eit ar patskaņu miju ar līdzskaņu miju
					celms = "juo" + ltgPatskaņuMijaLocīšanai(stem);
					mija = 122;
					break;
				case 153: // vajadzības izteiksme 3. konjugācijai -ēt ar burtu miju bez līdzskaņu un patskaņu mijas
					celms = "juo" + stem;
					mija = 125;
					break;
				// patskaņu mijas verbiem
				case 160: // patskaņu mija 2. konjugācijas supīnam, vēlējuma izteiksmei un pagātnes divdabim
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 114;
					break;
				case 161: // patskaņu mija 2. konjugācijas divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 116;
					break;
				case 162: // patskaņu mija 3. konjugācijas standarta -eit tagadnei bez līdzskaņu mijas
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 119;
					break;
				case 163: // patskaņu mija 3. konjugācijas standarta -eit bez līdzskaņu mijas grupas tagadnes divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 120;
					break;
				case 164: // patskaņu mija 3. konjugācijas standarta -eit grupas tagadnei ar līdzskaņu miju
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 122;
					break;
				case 165: // patskaņu mija 3. konjugācijas standarta -eit līdzskaņu mijas grupas tagadnes divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 123;
					break;
				case 166: // patskaņu mija 3. konjugācijas standarta -ēt 1. pers. tagadnei bez līdzskaņu un burtu mijas
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 124;
					break;
				case 167: // patskaņu mija 3. konjugācijas standarta -ēt bez līdzskaņu un burtu mijas tagadnes divdabja pārākajai un vispārākajai pakāpei
					celms = ltgPatskaņuMijaLocīšanai(stem);
					mija = 126;
					break;
				default:
					celms = stem;
					mija = stemChange;
			}

			switch (mija) {
				case 0: varianti.add(new Variants(celms)); break;  // nav mijas

				case 1: // lietvārdu līdzskaņu mija
					// Personvārdu mijas - Valdis-Valda; Gatis-Gata. Eglīts - Eglīša.  Vēl ir literatūrā minēts izņēmums -skis -ckis (Čaikovskis, Visockis), taču tiem tāpat viss šķiet ok.
					// 2025-06-02: Gatis un Valdis jau sen iet paradigmā pie tēta un viesa,
					//             Eglīts un Ķezbers iet paradigmā pie suņa.
					if (properName && celms.endsWith("t")) {// && !celms.endsWith("īt")) {
						//varianti.add(new Variants(celms));
						//if (syllables(celms) > 1)
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"š","Mija","t -> š"));
					}
					else if (properName && celms.endsWith("d") ) {
						//if (syllables(celms) > 1)
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ž","Mija","d -> ž"));
						//else varianti.add(new Variants(celms));
					}
					else if (celms.endsWith("s") || celms.endsWith("t")) {
						if (celms.endsWith("kst")) {
							varianti.add(new Variants(celms.substring(0,celms.length()-3)+"kš","Mija","kst -> kš"));
						} else if (celms.endsWith("nst")) { // skansts -> skanšu
							varianti.add(new Variants(celms.substring(0, celms.length() - 3) + "nš", "Mija", "nst -> nš"));
						} else if (celms.endsWith("s")) {
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
						else if (celms.endsWith("ll")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļļ","Mija","ll -> ļļ"));}
						else varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ļ","Mija","l -> ļ"));
					}
					else if (celms.endsWith("n")) {
						if (celms.endsWith("sn")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"šņ","Mija","sn -> šņ"));}
						else if (celms.endsWith("zn")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"žņ","Mija","zn -> žņ"));}
						else if (celms.endsWith("ln")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļņ","Mija","ln -> ļņ"));}
						else if (celms.endsWith("nn")) {varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ņņ","Mija","nn -> ņņ"));}
						else /*if (!(celms.endsWith("zņ") || celms.endsWith("sņ") || celms.endsWith("lņ")))*/ {
							//varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ņ","Mija", "l -> ņ ??"));
							varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ņ","Mija", "n -> ņ"));
						}
					}
					else if (celms.endsWith("p") || celms.endsWith("b") || celms.endsWith("m") || celms.endsWith("v") || celms.endsWith("f")) {
						varianti.add(new Variants(celms+"j","Mija","p->pj (u.c.)"));
					}
					/*
					// Tēzaurā valodnieki liek 2 leksēmas no laika gala.
					else if (celms.endsWith("f")) { // Žirafu -> žirafju, žirafu
						Variants v = new Variants(celms+"j","Mija","p->pj (u.c.)");
						v.addAttribute(AttributeNames.i_Recommended, AttributeNames.v_Yes);
						varianti.add(v);
						varianti.add(new Variants(celms));
					}*/
					else if (!(celms.endsWith("p") || celms.endsWith("b") || celms.endsWith("m") || celms.endsWith("v") ||
							celms.endsWith("t") || celms.endsWith("d") || celms.endsWith("c") || celms.endsWith("z") ||
							celms.endsWith("s") || celms.endsWith("n") || celms.endsWith("l") || celms.endsWith("f")) )
						varianti.add(new Variants(celms));
					break;
				case 2: //  dv. 3. konjugācijas tagadne, kas noņem celma pēdējo burtu
					if (celms.endsWith("ī") || celms.endsWith("inā") || celms.endsWith("sargā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1), "Garā", "ā"));
					else varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					break;
				case 3: // īpašības vārdiem pieliekam -āk- un vis-
					varianti.add(new Variants(celms,AttributeNames.i_Degree,AttributeNames.v_Positive));
					if (!celms.endsWith("āk")) {
						varianti.add(new Variants(celms + "āk", AttributeNames.i_Degree, AttributeNames.v_Comparative));
						if (pieliktVisPārākoPak)
							varianti.add(new Variants("vis" + celms + "āk", AttributeNames.i_Degree, AttributeNames.v_Superlative));
					}
					break;
				case 6: // 1. konjugācijas nākotne
					if (celms.endsWith("s")) {
						if (trešāSakne.endsWith("d")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dī"));
						else if (trešāSakne.endsWith("t")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"tī"));
						else if (trešāSakne.endsWith("s")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"sī"));
						else varianti.add(new Variants(celms));
					} else if (celms.endsWith("z") || celms.endsWith("š")) {
						varianti.add(new Variants(celms+"ī"));
					}
					else varianti.add(new Variants(celms));
					break;
				case 7: // 1. konjugācijas 2. personas tagadne
				case 23: // 1. konjugācijas 2. personas tagadne - ja pēc tam seko garā galotne kā -iet
					if (celms.endsWith("š") && trešāSakne.endsWith("s")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"s"));
					else if (celms.endsWith("š") && trešāSakne.endsWith("t")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"t"));
					else if ( (celms.endsWith("od") && !celms.endsWith("dod")) || celms.endsWith("ūd") || celms.endsWith("op") || celms.endsWith("ūp") || celms.endsWith("ot") || celms.endsWith("ūt") || celms.endsWith("īt") || celms.endsWith("iet")  || celms.endsWith("st")) {
						if (mija == 7)
							varianti.add(new Variants(celms+"i"));
						else varianti.add(new Variants(celms));
					}
					else if (celms.endsWith("ļ")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"l"));
					else if (celms.endsWith("mj") || celms.endsWith("bj") || celms.endsWith("pj"))	varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					else if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					else if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dz"));
					else if (celms.endsWith("ž")) {
						// varianti.add(new Variants(celms.substring(0,celms.length()-1)+"z")); // griez -> griežu
						varianti.add(new Variants(trešāSakne)); // skaužu -> skaud, laužu -> lauz; sanāk atbilstoši pagātnes celmam
					} else varianti.add(new Variants(celms));
					break;
				case 8: // -ams -āms 3. konjugācijai bezmiju gadījums
					if (celms.endsWith("inā") || celms.endsWith("sargā")) varianti.add(new Variants(celms, "Garā", "ā"));
					else if (celms.endsWith("ī")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā", "Garā", "ā"));
					else if (celms.endsWith("ē")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else varianti.add(new Variants(celms));
					break;
				case 9: // 3. konjugācija 3. pers. tagadne bez mijas
					if (celms.endsWith("dā")) varianti.add(new Variants(celms.substring(0,celms.length()-1))); // dzied, raud
					else if (celms.endsWith("ā") || celms.endsWith("ī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a"));
					else varianti.add(new Variants(celms.substring(0,celms.length()-1)));
					break;
				case 10: // īpašības vārds -āk- un vis-, -i apstākļa formai
					varianti.add(new Variants(celms,AttributeNames.i_Degree,AttributeNames.v_Positive));
					varianti.add(new Variants(celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Superlative));
					break;
				case 11: // -uša formas
					if (celms.endsWith("c")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"k"));
					else if (celms.endsWith("dz")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"g"));
					else varianti.add(new Variants(celms));
					break;
				case 13: // īpašības vārdiem -āk-, ar š->s nominatīva formā (zaļš -> zaļāks
					varianti.add(new Variants(celms+"āk", AttributeNames.i_Degree, AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āk",AttributeNames.i_Degree, AttributeNames.v_Superlative));
					break;
				case 14: // 1. konjugācijas "-is" forma
					if (celms.endsWith("k")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"c"));
					else if (celms.endsWith("g")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"dz"));
					else varianti.add(new Variants(celms));
					break;
				case 15: // pūst -> pūzdams nopūzdamies s -> z mija tad, ja 3. sakne (pagātnes sakne) beidzas ar t/d
					if (celms.endsWith("s") && (trešāSakne.endsWith("t") || trešāSakne.endsWith("d"))) {
						varianti.add(new Variants(celms.substring(0,celms.length()-1)+"z"));
					} else varianti.add(new Variants(celms));
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
				/*
				// 2025-06-12 Baiba izskaidro, ka pēc mūsdienu latviešu valodas
				// normām visiem 4. un 5. deklinācijas vārdiem pienākas viens
				// vienskaitļa vokatīvs, kas sakrīt vienskaitļa nominatīvu
				case 18: // garā sieviešu dzimtes vokatīva forma "laura!" "margrieta!"
					if (syllables(celms) < 2 || // NB! te ir < 2 bet pie atpazīšanas <= 2 - ar 2 zilbēm pagaidām atpazīst abus un ģenerē vienu
							!(celms.endsWith("ij") || celms.endsWith("īn") || celms.endsWith("īt") || celms.endsWith("ān") || celms.endsWith("iņ") || celms.endsWith("ēn") || celms.endsWith("niec") || celms.endsWith("āj")) )
						varianti.add(new Variants(celms));
					if (syllables(celms) > 1 && (celms.endsWith("kāj") || celms.endsWith("māj")))
						varianti.add(new Variants(celms));
					break;*/
				case 20: //  dv. 3. konjugācijas tagadnes mija 1. personas tagadnei, -ot divdabim un vajadzībai - atšķiras no 26. mijas 'gulēt' un 'tecēt'
					if (celms.endsWith("gulē")) {
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļ")); //gulēt -> guļu
						// variantu ar -l (gulošs) atpazīstam bet neģenerējam
					} else if (celms.endsWith("cī") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k", "Garā", "ā")); //sacīt
					else if (celms.endsWith("cē") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k")); //mācēt -> māku
					else if (celms.endsWith("dē") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ž")); //sēdēt -> sēžu
					else if (celms.endsWith("dzē") || celms.endsWith("dzī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //vajadzēt -> vajag, slodzīt -> slogu
					break;
				case 21: // divdabju formas ar pārāko/vispārāko pakāpi
					varianti.add(new Variants(celms, AttributeNames.i_Degree, AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms, AttributeNames.i_Degree, AttributeNames.v_Superlative));
					break;
				case 22: // jaundzimušais -> jaundzimusī
					if (celms.endsWith("uš"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"us"));
					break;
				case 24: //  analoģiski 2, bet ar pārākajām / vispārākajām pakāpēm
					if (celms.endsWith("ī") || celms.endsWith("inā") || celms.endsWith("sargā"))
						varianti.add(new Variants(celms.substring(0,celms.length()-1), AttributeNames.i_Degree, AttributeNames.v_Comparative));
					else varianti.add(new Variants(celms.substring(0,celms.length()-1), AttributeNames.i_Degree, AttributeNames.v_Comparative));

					if (pieliktVisPārākoPak) {
						if (celms.endsWith("ī") || celms.endsWith("inā") || celms.endsWith("sargā"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-1), AttributeNames.i_Degree, AttributeNames.v_Superlative));
						else varianti.add(new Variants("vis" + celms.substring(0,celms.length()-1), AttributeNames.i_Degree, AttributeNames.v_Superlative));
					}
					break;
				case 25: //  analoģiski 8, bet ar pārākajām / vispārākajām pakāpēm. DRY :( :(
					if (celms.endsWith("inā") || celms.endsWith("sargā")) varianti.add(new Variants(celms, AttributeNames.i_Degree, AttributeNames.v_Comparative));
					else if (celms.endsWith("ī")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"ā", AttributeNames.i_Degree, AttributeNames.v_Comparative));
					else if (celms.endsWith("ē")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a", AttributeNames.i_Degree, AttributeNames.v_Comparative));
					else if (celms.endsWith("ā")) varianti.add(new Variants(celms.substring(0,celms.length()-1)+"a", AttributeNames.i_Degree, AttributeNames.v_Comparative));
					else varianti.add(new Variants(celms, AttributeNames.i_Degree, AttributeNames.v_Comparative));

					if (pieliktVisPārākoPak) {
						if (celms.endsWith("inā") || celms.endsWith("sargā")) varianti.add(new Variants("vis" + celms, AttributeNames.i_Degree, AttributeNames.v_Superlative));
						else if (celms.endsWith("ī")) varianti.add(new Variants("vis" + celms.substring(0,celms.length()-1)+"ā", AttributeNames.i_Degree, AttributeNames.v_Superlative));
						else if (celms.endsWith("ē")) varianti.add(new Variants("vis" + celms.substring(0,celms.length()-1)+"a", AttributeNames.i_Degree, AttributeNames.v_Superlative));
						else if (celms.endsWith("ā")) varianti.add(new Variants("vis" + celms.substring(0,celms.length()-1)+"a", AttributeNames.i_Degree, AttributeNames.v_Superlative));
						else varianti.add(new Variants("vis" + celms, AttributeNames.i_Degree, AttributeNames.v_Superlative));
					}
					break;
				case 26:  //  dv. 3. konjugācijas miju gadījuma formas - otrās personas tagadne, pavēles izteiksme
					if (celms.endsWith("lē")) varianti.add(new Variants(celms.substring(0,celms.length()-1))); //gulēt -> guli
					else if (celms.endsWith("cī") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k", "Garā", "ā")); //sacīt->saki
					else if (celms.endsWith("tecē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"c")); //tecēt -> teci
					else if (celms.endsWith("cē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k")); //mācēt -> māki
					else if (celms.endsWith("dzē") || celms.endsWith("dzī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //vajadzēt -> vajag, slodzīt -> slogi
					else
						varianti.add(new Variants(celms.substring(0,celms.length()-1))); // sēdē-ties -> sēd-ies
					break;
				case 27: // -ams -āms 3. konjugācijai miju gadījums
					if (celms.endsWith("cī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"kā", "Garā", "ā")); //sacīt->sakām
					else if (celms.endsWith("dzī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"gā")); //slodzīt -> slogām
					else if (celms.endsWith("cē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ka")); //mācēt -> mākam
					else if (celms.endsWith("gulē")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļa")); //gulēt -> guļam
					else if (celms.endsWith("dē") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ža")); //sēdēt -> sēžam
					else if (celms.endsWith("dzē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"ga")); //vajadzēt -> vajagam
					break;
				case 30: // 3. konjugācija 3. pers. tagadne ar miju
					if (celms.endsWith("cī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ka")); // "saka"
					else if (celms.endsWith("dzī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"ga")); //slodzīt -> sloga
					else if (celms.endsWith("cē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k")); //mācēt -> māk
					else if (celms.endsWith("dē") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ž")); //sēdēt -> sēž
					else if (celms.endsWith("dzē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g")); //vajadzēt -> vajag
					else if (celms.endsWith("lē")) varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļ")); //gulēt -> guļ
					break;
				case 32: //  analoģiski 20, bet ar pārākajām / vispārākajām pakāpēm
					if (celms.endsWith("cī") || celms.endsWith("cē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"k", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //sacīt
					else if (celms.endsWith("dzī") || celms.endsWith("dzē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"g", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //slodzīt -> slogu
					else if (celms.endsWith("dē") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ž", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //sēdēt -> sēž
					else if (celms.endsWith("lē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļ", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //gulēt -> guļu
					else
						varianti.add(new Variants(celms.substring(0,celms.length()-1), AttributeNames.i_Degree, AttributeNames.v_Comparative));

					if (pieliktVisPārākoPak) {
						// TODO :( :( DRY
						if (celms.endsWith("cī") || celms.endsWith("cē"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-2)+"k", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //sacīt
						else if (celms.endsWith("vajadzē"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-3)+"g", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //vajadzēt -> vajag
						else if (celms.endsWith("dzī") || celms.endsWith("dzē"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-3)+"g", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //slodzīt -> slogu
						else if (celms.endsWith("dē") )
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-2)+"ž", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //sēdēt -> sēž
						else if (celms.endsWith("gulē"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-2)+"ļ")); //gulēt -> guļu
						else
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-1), AttributeNames.i_Degree, AttributeNames.v_Superlative));
					}
					break;
				case 33: //  analoģiski 27, bet ar pārākajām / vispārākajām pakāpēm. DRY :( :(
					if (celms.endsWith("cī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"kā", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //sacīt
					else if (celms.endsWith("dzī"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"gā", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //slodzīt -> slogu
					else if (celms.endsWith("cē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ka", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //mācēt -> māk
					else if (celms.endsWith("lē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ļa", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //gulēt -> guļam
					else if (celms.endsWith("dē") )
						varianti.add(new Variants(celms.substring(0,celms.length()-2)+"ža", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //sēdēt -> sēž
					else if (celms.endsWith("dzē"))
						varianti.add(new Variants(celms.substring(0,celms.length()-3)+"ga", AttributeNames.i_Degree, AttributeNames.v_Comparative)); //vajadzēt -> vajag

					if (pieliktVisPārākoPak) {
						if (celms.endsWith("cī"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-2)+"kā", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //sacīt
						else if (celms.endsWith("dzī"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-3)+"gā", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //slodzīt -> slogu
						else if (celms.endsWith("cē"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-2)+"ka", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //mācēt -> māk
						else if (celms.endsWith("lē"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-2)+"ļa", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //gulēt -> guļam
						else if (celms.endsWith("dē") )
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-2)+"ža", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //sēdēt -> sēž
						else if (celms.endsWith("dzē"))
							varianti.add(new Variants("vis" + celms.substring(0,celms.length()-3)+"ga", AttributeNames.i_Degree, AttributeNames.v_Superlative)); //vajadzēt -> vajag
					}
					break;
				case 34: // īpašības vārdiem -āk- un vis- izskaņām kā -ajam: liekam nevis zaļ-š->zaļ-ajam, bet zaļ-š->zaļ-a-jam, bet pēdēj-ais -> pēdē-jam/pēdēj-a-jam
					if (celms.endsWith("ēj")) // pēdēj-ais -> pēdē-jam
						varianti.add(new Variants(celms.substring(0, celms.length()-1),AttributeNames.i_Degree,AttributeNames.v_Positive));
					else // zaļ-š -> zaļa-jam
						varianti.add(new Variants(celms+"a",AttributeNames.i_Degree,AttributeNames.v_Positive));

					varianti.add(new Variants(celms + "āka",AttributeNames.i_Degree,AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āka",AttributeNames.i_Degree,AttributeNames.v_Superlative));
					break;
				case 35: // Substantivizējušamies "īpašības vārdiem" izskaņām kā -ajam: liekam nevis zaļ-š->zaļ-ajam, bet zaļ-š->zaļ-a-jam, bet pēdēj-ais -> pēdē-jam/pēdēj-a-jam; bez pārākās/vispārākās pakāpes
					if (celms.endsWith("ēj")) // pēdēj-ais -> pēdē-jam
						varianti.add(new Variants(celms.substring(0, celms.length()-1),AttributeNames.i_Degree,AttributeNames.v_Positive));
					else // zaļ-š -> zaļa-jam
						varianti.add(new Variants(celms+"a",AttributeNames.i_Degree,AttributeNames.v_Positive));
					break;
				case 36: // 'iet' speciālgadījums - normāli 3. personas tagadnei atbilstošais celms būtu 'ej', bet ir 'iet'.
					if (celms.endsWith("ej") && trešāSakne.endsWith("gāj"))
						varianti.add(new Variants(celms.substring(0, celms.length()-2)+"iet"));
					else varianti.add(new Variants(celms));
					break;
				case 38: // apstākļa vārdi ar gradāciju
					varianti.add(new Variants(celms,AttributeNames.i_Degree,AttributeNames.v_Positive));
					if (celms.endsWith("i") || celms.endsWith("u")) {
						celms = celms.substring(0, celms.length()-1);
					}
					varianti.add(new Variants(celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Comparative));
					if (pieliktVisPārākoPak)
						varianti.add(new Variants("vis" + celms + "āk",AttributeNames.i_Degree,AttributeNames.v_Superlative));
					break;

				// ------ LATGALIAN from here -----
				case 99: // puse no latgaliešu 'burtu mijas' (case 100) - tikai paradigmām, kur ir garantēts, ka bezmijas celms beidzas mīkstu līdzskani
					varianti.add(new Variants(ltgBurtuMija(celms)));
					break;
				case 100: // // latgaliešu 'burtu mija', kur pirms -e, -i, -ī, -ē, -ie ļ, ņ, ķ, ģ kļūst par l, n, k, g (bruoļs -> bruoli)
					varianti.add(new Variants(ltgBurtuMija(celms)));
					break;
				case 101: // latgaliešu līdzskaņu mija lietvārdiem, parastās galotnes (izņemot -i, -e, -ī, -ē, -ie, -ei)
					// Mijas no Leikumas "Vasals!"
					if (celms.endsWith("kst")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 3) + "kš"));
					} else if (celms.endsWith("sl")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šļ"));
					} else if (celms.endsWith("zl")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "žļ"));
					} else if (celms.endsWith("sm")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šm"));
					} else if (celms.endsWith("sn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šņ"));
					} else if (celms.endsWith("zn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "žņ"));
					} else if (celms.endsWith("ll")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ļļ"));
					} else if (celms.endsWith("nn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ņņ"));
					} else if (celms.endsWith("c")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "č"));
					} else if (celms.endsWith("d")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ž"));
					} else if (celms.endsWith("s")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "š"));
					} else if (celms.endsWith("t")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "š"));
					} else if (celms.endsWith("z")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ž"));
						// Citas mijas
					} else if (celms.endsWith("k")) { // Andronovs?
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ķ"));
					} else if (celms.endsWith("l")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ļ"));
					} else if (celms.endsWith("n")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ņ"));
					} else {
						varianti.add(new Variants(celms));
					}
					break;
				case 102: // // latgaliešu līdzskaņu mīkstināšana lietvārdiem, e, i, ē, ī, ie galotnes
					if (celms.endsWith("kst")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 3) + "kš"));
					} else // Burtu miju ietekmētās vairāksimbolu mijas
					if (celms.endsWith("šļ") || celms.endsWith("sl")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šl"));
					} else if (celms.endsWith("žļ") || celms.endsWith("zl")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "žl"));
					} else if (celms.endsWith("šm") || celms.endsWith("sm")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šm"));
					} else if (celms.endsWith("šņ") || celms.endsWith("sn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "šn"));
					} else if (celms.endsWith("žņ") || celms.endsWith("zn")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "žn"));
					} else if (celms.endsWith("ļļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ll"));
					} else if (celms.endsWith("ņņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "nn"));
						// Parastās, mijīgās, mijas
					} else if (celms.endsWith("c")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "č"));
					} else if (celms.endsWith("s")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "š"));
					} else if (celms.endsWith("t")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "š"));
					} else if (celms.endsWith("z")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ž"));
					} else if (celms.endsWith("d")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ž"));
						// Burtu miju ietekmētās viensimbola mijas
					} else if (celms.endsWith("ļ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "l"));
					} else if (celms.endsWith("ņ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "n"));
					} else if (celms.endsWith("ķ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "k"));
					} else if (celms.endsWith("ģ")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "g"));
					} else {
						varianti.add(new Variants(celms));
					}
					break;
				case 103: // līdzīgi 'case 3' - īpašības vārdiem pieliekam -uok- un vys- / vysu-
					varianti.add(new Variants(celms, ltgDegreeFlags(AttributeNames.v_Positive)));
					if (!celms.endsWith("uok")) {
						varianti.add(new Variants(celms + "uok", ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 104: // īpašības vārdiem pieliekam -uok- un vys- / vysu- + burtu mija
					varianti.add(new Variants(ltgBurtuMija(celms), ltgDegreeFlags(AttributeNames.v_Positive)));
					if (!celms.endsWith("uok")) {
						varianti.add(new Variants(celms + "uok", ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 105: // līdzīgi kā case 34 -  īpašības vārdiem -uok- un vys- / vysu- izskaņām kā -ajam: liekam nevis moz-s->moz-ajam, bet moz-s->moz-a-jam, bet senej-ais -> sene-jam/sene-a-jam
					if (celms.endsWith("ēj") || celms.endsWith("ej")) // senej-ais -> sene-jam
						varianti.add(new Variants(celms.substring(0, celms.length()-1), ltgDegreeFlags(AttributeNames.v_Positive)));
					else // moz-s -> moza-jam
						varianti.add(new Variants(celms+"a", ltgDegreeFlags(AttributeNames.v_Positive)));

					varianti.add(new Variants(celms + "uoka", ltgDegreeFlags(AttributeNames.v_Comparative)));
					if (pieliktVisPārākoPak) {
						varianti.add(new Variants("vys" + celms + "uoka", ltgDegreeFlags(AttributeNames.v_Superlative)));
						varianti.add(new Variants("vysu" + celms + "uoka", ltgDegreeFlags(AttributeNames.v_Superlative)));
					}
					break;
				case 106: // līdzīgi 'case 13' - apstākļa vārdiem pieliekam -uok- un vys- / vysu- - būtībā 103, bet bez pamatformas
					if (!celms.endsWith("uok")) {
						varianti.add(new Variants(celms + "uok", ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 107: // latgaliešu 'burtu mijas' inverss - kad pamatformas galotne ir -e, -i, -ī, -ē, -ie, un l, n, k, g kļūst par ļ, ņ, ķ, ģ pirms citām galotnēm (slapnis)
					varianti.add(new Variants(ltgBurtuMijaAtpakaļViennoz(celms)));
					break;
				case 108: // 107 + 106 priekš slapnis
					if (!celms.endsWith("uok")) {
						varianti.add(new Variants(ltgBurtuMijaAtpakaļViennoz(celms) + "uok", ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + ltgBurtuMijaAtpakaļViennoz(celms) + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + ltgBurtuMijaAtpakaļViennoz(celms) + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 109: // apstākļa vārdi ar gradāciju, bet bez burtu mijas
					varianti.add(new Variants(celms, ltgDegreeFlags(AttributeNames.v_Positive)));
					if (celms.endsWith("ai")) {
						celms = celms.substring(0, celms.length()-2);
					} else if (celms.endsWith("i") || celms.endsWith("a")) {
						celms = celms.substring(0, celms.length()-1);
					}
					varianti.add(new Variants(celms + "uok", ltgDegreeFlags(AttributeNames.v_Comparative)));
					if (pieliktVisPārākoPak) {
						varianti.add(new Variants("vys" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
						varianti.add(new Variants("vysu" + celms + "uok", ltgDegreeFlags(AttributeNames.v_Superlative)));
					}
					break;
				// Te sākas verbu mijas
				case 110: // 2. konjugācija, vienkāršās tagadnes mija
					if (celms.endsWith("uo")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "o"));
					} else if (celms.endsWith("ei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "e"));
					} else if (celms.endsWith("ē")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "e"));
					}
					break;
				case 111: // 2. konjugācija, vienkāršās pagātnes vsk. 1., 2. pers. mija
					if (celms.endsWith("uo")) {
						varianti.add(new Variants(celms + "j"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ov"));
					} else if (celms.endsWith("ei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ej"));
					} else if (celms.endsWith("ē")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "iej"));
					}
					break;
				case 112: // 2. konjugācija, vienkāršās pagātnes dsk. un 3. pers. mija
					if (celms.endsWith("uo")) {
						varianti.add(new Variants(celms + "j"));
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ov"));
					} else if (celms.endsWith("ei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "ej"));
					} else if (celms.endsWith("ē")) {
						varianti.add(new Variants(celms + "j"));
					}
					break;
				case 113: // 2. konjugācija, vienkāršās nākotnes vsk. 1., 2. pers. mija
					if (celms.endsWith("ē")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ie"));
					} else if (celms.endsWith("ei") || celms.endsWith("uo")) {
						varianti.add(new Variants(celms));
					}
					break;
				case 114: // 2. konjugācija, vēlējuma izteiksme, supīns un ciešamās kārtas pagātnes (-ts) divdabis, -dams divdabis
					if (celms.endsWith("ē")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ā"));
					} else if (celms.endsWith("ei") || celms.endsWith("uo")) {
						varianti.add(new Variants(celms));
					}
					break;
				case 115: // 2. konjugācija, divdabju formu vispārākā pakāpe (121.) + tagadnes mija (110.)
					if (celms.endsWith("uo")) {
						String atvCelms = celms.substring(0, celms.length() - 2) + "o";
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					} else if (celms.endsWith("ei")) {
						String atvCelms = celms.substring(0, celms.length() - 2) + "e";
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					} else if (celms.endsWith("ē")) {
						String atvCelms = celms.substring(0, celms.length() - 1) + "e";
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 116: // 2. konjugācija, divdabju formu vispārākā pakāpe (121.) + vēlējuma/supīna mija (114.) (-ts divdabim)
					if (celms.endsWith("ē")) {
						String atvCelms = celms.substring(0, celms.length() - 1) + "ā";
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					} else if (celms.endsWith("ei") || celms.endsWith("uo")) {
						varianti.add(new Variants(celms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + celms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + celms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 117: // 2. konjugācija, pagātnes mija -s, -use divdabim (vienkāršota 111.)
					if (celms.endsWith("uo")) {
						varianti.add(new Variants(celms));
					} else if (celms.endsWith("ei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2) + "e"));
					} else if (celms.endsWith("ē")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1) + "ie"));
					}
					break;
				case 118: // 2. konjugācija, divdabju formu vispārākā pakāpe (121.) + pag. mija -s, -use divdabim (117.)
					if (celms.endsWith("ei")) {
						String atvCelms = celms.substring(0, celms.length() - 2) + "e";
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					} else if (celms.endsWith("ē")) {
						String atvCelms = celms.substring(0, celms.length() - 1) + "ie";
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					} else if (celms.endsWith("uo")) {
						varianti.add(new Variants(celms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + celms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + celms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 119: // 3. konjugācija, standarta -eit, tagadne un pagātne (līdzskaņu mijas nekad nav)
					if (celms.endsWith("ei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 2)));
					}
					break;
				case 120: // 3. konjugācija, standarta -eit bez līdskaņu mijas, divdabju formu vispārākā pakāpe + tagadnes un pagātnes mija (119.)
					if (celms.endsWith("ei")) {
						String atvCelms = celms.substring(0, celms.length() - 2);
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 121: // 3. konjugācija, divdabju formu vispārākā pakāpe bez mijas
					varianti.add(new Variants(celms, ltgDegreeFlags(AttributeNames.v_Comparative)));
					if (pieliktVisPārākoPak) {
						varianti.add(new Variants("vys" + celms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						varianti.add(new Variants("vysu" + celms, ltgDegreeFlags(AttributeNames.v_Superlative)));
					}
					break;
				case 122: // 3. konjugācija, standarta -eit, tagadne ar līdzskaņu miju
					if (celms.endsWith("ļdei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 4) + "ld"));
					} else if (celms.endsWith("ņdei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 4) + "nd"));
					} else if (celms.endsWith("dzei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 4) + "g"));
					} else if (celms.endsWith("cei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 3) + "k"));
					} else if (celms.endsWith("lei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 3) + "ļ"));
					} else if (celms.endsWith("nei")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 3) + "ņ"));
					}
					break;
				case 123: // 3. konjugācija, standarta -eit ar līdskaņu miju, divdabju formu vispārākā pakāpe + tagadnes mija (122.)
					String atvCelms = celms;
					if (celms.endsWith("ļdei")) {
						atvCelms = celms.substring(0, celms.length() - 4) + "ld";
					} else if (celms.endsWith("ņdei")) {
						atvCelms = celms.substring(0, celms.length() - 4) + "nd";
					} else if (celms.endsWith("dzei")) {
						atvCelms = celms.substring(0, celms.length() - 4) + "g";
					} else if (celms.endsWith("cei")) {
						atvCelms = celms.substring(0, celms.length() - 3) + "k";
					} else if (celms.endsWith("lei")) {
						atvCelms = celms.substring(0, celms.length() - 3) + "ļ";
					} else if (celms.endsWith("nei")) {
						atvCelms = celms.substring(0, celms.length() - 3) + "ņ";
					} else break;

					varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
					if (pieliktVisPārākoPak) {
						varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
					}
					break;
				case 124: // 3. konjugācija, standarta -ēt, tagadne un pagātne bez līdzskaņu un burtu mijas
					if (celms.endsWith("ē")) {
						varianti.add(new Variants(celms.substring(0, celms.length() - 1)));
					}
					break;
				case 125: // 3. konjugācija, standarta -ēt, tagadne bez līdzskaņu mijas ar inverso burtu miju
					if (celms.endsWith("ē")) {
						varianti.add(new Variants(ltgBurtuMijaAtpakaļViennoz(celms.substring(0, celms.length() - 1))));
					}
					break;
				case 126: // 3. konjugācija, standarta -ēt bez līdskaņu un burtu mijas, divdabju formu vispārākā pakāpe + tagadnes un pagātnes mija (119.)
					if (celms.endsWith("ē")) {
						atvCelms = celms.substring(0, celms.length() - 1);
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				case 127: // 3. konjugācija, standarta -ēt bez līdskaņu mijas ar inverso burtu miju, divdabju formu vispārākā pakāpe + tagadnes un pagātnes mija (119.)
					if (celms.endsWith("ē")) {
						atvCelms = ltgBurtuMijaAtpakaļViennoz(celms.substring(0, celms.length() - 1));
						varianti.add(new Variants(atvCelms, ltgDegreeFlags(AttributeNames.v_Comparative)));
						if (pieliktVisPārākoPak) {
							varianti.add(new Variants("vys" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
							varianti.add(new Variants("vysu" + atvCelms, ltgDegreeFlags(AttributeNames.v_Superlative)));
						}
					}
					break;
				default:
					System.err.printf("Invalid StemChange ID, stem '%s', stemchange %d\n", celms, mija);
			}
		} catch (StringIndexOutOfBoundsException e){
			new PrintWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8)).printf(
					"StringIndexOutOfBounds, celms '%s', mija %d\n", stem, stemChange);
			e.printStackTrace();
		}

		return varianti;
	}

	protected static String ltgPatskaņuMijaLocīšanai(String celms)
	{
		Pattern p = Pattern.compile("(.*?)(ai|ei|ui|oi|ie|[aāeēiīouūy])([bcčdfgģhjkķlļmnņprŗsštvzž]+[aāeēiīyoōuū]*)$");
		Matcher m = p.matcher(celms);
		if (m.matches()) {
			switch (m.group(2)) {
				case "a":
					return m.group(1) + 'o' + m.group(3);
				case "e":
					return m.group(1) + 'a' + m.group(3);
				case "ē":
					return m.group(1) + 'ā' + m.group(3);
				case "i":
					return m.group(1) + 'y' + m.group(3);
				default:
					return celms;
			}
		}
		return celms;
	}

	protected static String ltgPatkaņuMijaAtpakaļlocīšanai (String celms)
	{
		Pattern p = Pattern.compile("(.*?)(uo|[aāeēiīouūy]|)([bcčdfgģhjkķlļmnņprŗsštvzž]+[aāeēiīyoōuū]*)$");
		Matcher m = p.matcher(celms);
		if (m.matches()) {
			switch (m.group(2)) {
				case "a":
					return m.group(1) + 'e' + m.group(3);
				case "ā":
					return m.group(1) + 'ē' + m.group(3);
				case "y":
					return m.group(1) + 'i' + m.group(3);
				case "o":
					return m.group(1) + 'a' + m.group(3);
				default:
					return celms;
			}
		}
		return celms;
	}

	/**
	 * Latgaliešu "burtu mija": pirms -e, -i, -ī, -ē, -ie ļ, ņ, ķ, ģ izrunas
	 * īpatnību dēļ kļūst par l, n, k, g (bruoļs -> bruoli).
	 */
	protected static String ltgBurtuMija (String celms)
	{
		if (celms.endsWith("ļļ")) {
			return celms.substring(0, celms.length() - 2) + "ll";
		} else if (celms.endsWith("ņņ")) {
			return celms.substring(0, celms.length() - 2) + "nn";
		} else if (celms.endsWith("ļ")) {
			return celms.substring(0, celms.length() - 1) + "l";
		} else if (celms.endsWith("ņ")) {
			return celms.substring(0, celms.length() - 1) + "n";
		} else if (celms.endsWith("ķ")) {
			return celms.substring(0, celms.length() - 1) + "k";
		} else if (celms.endsWith("ģ")) {
			return celms.substring(0, celms.length() - 1) + "g";
		} else {
			return celms;
		}
	}

	/**
	 * Latgaliešu 'burtu mijas' inverss: pamatformas galotne ir -e, -i, -ī, -ē,
	 * -ie, un l, n, k, g kļūst par ļ, ņ, ķ, ģ pirms citām galotnēm (slapnis),
	 * vai arī viennozīmīgai atpakaļlocīšanai
	 */
	protected static String ltgBurtuMijaAtpakaļViennoz(String celms)
	{
		if (celms.endsWith("ll")) {
			return celms.substring(0, celms.length() - 2) + "ļļ";
		} else if (celms.endsWith("nn")) {
			return celms.substring(0, celms.length() - 2) + "ņņ";
		} else if (celms.endsWith("l")) {
			return celms.substring(0, celms.length() - 1) + "ļ";
		} else if (celms.endsWith("n")) {
			return celms.substring(0, celms.length() - 1) + "ņ";
		} else if (celms.endsWith("k")) {
			return celms.substring(0, celms.length() - 1) + "ķ";
		} else if (celms.endsWith("g")) {
			return celms.substring(0, celms.length() - 1) + "ģ";
		} else {
			return celms;
		}
	}

	/**
	 * For latgalian superlative degree made with `vys` or `vysu` is always
	 * undesirable. The grammatically correct way to make superlative is to make
	 * an analytical form with `pots`.
	 */
	protected static AttributeValues ltgDegreeFlags (String degree)
	{
		AttributeValues feats = new AttributeValues();
		feats.addAttribute(AttributeNames.i_Degree, degree);
		if (degree != null && degree.equals(AttributeNames.v_Superlative))
			feats.addAttribute(AttributeNames.i_Normative, AttributeNames.v_Undesirable);
		return feats;
	}
}
