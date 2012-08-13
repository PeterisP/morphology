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

import java.util.Iterator;

import lv.semti.morphology.attributes.*;

/**
 * Transforms between internal attribute value structure and SemTi-Kamols
 * tagset.
 * TODO - saintegrēt ar klasi ĪpašībuVarianti; lai šī loģika ir iekļauta īpašību variantu XML failā
 * @author Pēteris Paikens
 */
public class MarkupConverter {
	public static boolean defaulti = false;

	/**
	 * Check if attribute-value structure has attribute matching given
	 * attributeValue and if so then set corresponding position in the tag.
	 */
	private static void verifyAndSetKamolsAttribute(
			AttributeValues avs, StringBuilder tag, int index,
			char tagValue, String attribute, String attributeValue) {
		if (avs.isMatchingStrong(attribute, attributeValue))
			tag.setCharAt(index, tagValue);
	}

	/**
	 * Convert internal attribute-value structure to SemTi-Kamols tag.
	 */
	public static String toKamolsMarkup(AttributeValues avs) {
		StringBuilder res = toKamolsMarkup(avs, defaulti);
		if (res.length() < 1) return res.toString();
		
		if (res.charAt(0) == 'v' && res.charAt(1) == '_') res.setCharAt(1, 'm');
		if (res.charAt(0) == 'p' && res.charAt(6) == '_') res.setCharAt(6, 'n');
		if (res.charAt(0) == 'v' && res.charAt(3) != 'p' && res.charAt(10) == '_')
			res.setCharAt(10, 'n');
		return res.toString();
	}
	
	/**
	 * Convert internal attribute-value structure to SemTi-Kamols tag.
	 * No default values are set.
	 */
	public static String toKamolsMarkupNoDefaults(AttributeValues avs)
	{
		return toKamolsMarkup(avs, false).toString();
	}
	
	/**
	 * Convert internal attribute-value structure to SemTi-Kamols tag.
	 * Usage of default tags given as parameter. No additional defaults used.
	 */
	private static StringBuilder toKamolsMarkup(
			AttributeValues avs, boolean defaults) {

		StringBuilder tag = new StringBuilder();

		String pos = avs.getValue(AttributeNames.i_PartOfSpeech);
		if (pos == null) return tag;


		if (pos.equalsIgnoreCase(AttributeNames.v_Noun)) {

			tag.setLength(0);
			if (defaults)
				tag.append("ncmsnn");
			else tag.append("n_____");

			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_NounType,AttributeNames.v_CommonNoun);
			verifyAndSetKamolsAttribute(avs,tag,1,'p',AttributeNames.i_NounType,AttributeNames.v_ProperNoun);
			verifyAndSetKamolsAttribute(avs,tag,2,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetKamolsAttribute(avs,tag,2,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetKamolsAttribute(avs,tag,2,'k',AttributeNames.i_Gender,AttributeNames.v_Kopdzimte); //? nav TagSet
			//Tagset toties ir "Nepiemīt" dzimte
			verifyAndSetKamolsAttribute(avs,tag,3,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetKamolsAttribute(avs,tag,3,'v',AttributeNames.i_NumberSpecial,AttributeNames.v_SingulareTantum);
			verifyAndSetKamolsAttribute(avs,tag,3,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetKamolsAttribute(avs,tag,3,'d',AttributeNames.i_NumberSpecial,AttributeNames.v_PlurareTantum);
			//Tagset toties ir "Nepiemīt" skaitlis
			verifyAndSetKamolsAttribute(avs,tag,4,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetKamolsAttribute(avs,tag,4,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetKamolsAttribute(avs,tag,4,'s',AttributeNames.i_CaseSpecial,AttributeNames.v_InflexibleGenitive);
			verifyAndSetKamolsAttribute(avs,tag,4,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetKamolsAttribute(avs,tag,4,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetKamolsAttribute(avs,tag,4,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetKamolsAttribute(avs,tag,4,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetKamolsAttribute(avs,tag,4,'u',AttributeNames.i_Case,AttributeNames.v_Nelokaams); // ? nav TagSet
			//Tagset toties ir "Nepiemīt" locījums
			
			verifyAndSetKamolsAttribute(avs,tag,5,'1',AttributeNames.i_Declension,"1");
			verifyAndSetKamolsAttribute(avs,tag,5,'2',AttributeNames.i_Declension,"2");
			verifyAndSetKamolsAttribute(avs,tag,5,'3',AttributeNames.i_Declension,"3");
			verifyAndSetKamolsAttribute(avs,tag,5,'4',AttributeNames.i_Declension,"4");
			verifyAndSetKamolsAttribute(avs,tag,5,'5',AttributeNames.i_Declension,"5");
			verifyAndSetKamolsAttribute(avs,tag,5,'6',AttributeNames.i_Declension,"6");
			verifyAndSetKamolsAttribute(avs,tag,5,'r',AttributeNames.i_Declension,AttributeNames.v_Reflexive);
			verifyAndSetKamolsAttribute(avs,tag,5,'0',AttributeNames.i_Declension,AttributeNames.v_NA);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Verb) && !avs.isMatchingStrong(AttributeNames.i_Izteiksme, AttributeNames.v_Participle)) {

			tag.setLength(0);
			if (defaults)
				tag.append("vmni0t1000n");
			//else tag.append("vm_________");
			else tag.append("v__________");

			verifyAndSetKamolsAttribute(avs,tag,1,'m',AttributeNames.i_VerbType,AttributeNames.v_MainVerb);
			verifyAndSetKamolsAttribute(avs,tag,1,'a',AttributeNames.i_VerbType,AttributeNames.v_PaliigDv);
			verifyAndSetKamolsAttribute(avs,tag,1,'o',AttributeNames.i_VerbType,AttributeNames.v_Modaals);
			verifyAndSetKamolsAttribute(avs,tag,1,'p',AttributeNames.i_VerbType,AttributeNames.v_Faazes);
			verifyAndSetKamolsAttribute(avs,tag,1,'e',AttributeNames.i_VerbType,AttributeNames.v_IzpausmesVeida);
			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_VerbType,AttributeNames.v_Buut);
			verifyAndSetKamolsAttribute(avs,tag,1,'t',AttributeNames.i_VerbType,AttributeNames.v_TiktTapt);
			verifyAndSetKamolsAttribute(avs,tag,1,'g',AttributeNames.i_VerbType,AttributeNames.v_Nebuut);

			verifyAndSetKamolsAttribute(avs,tag,2,'n',AttributeNames.i_Reflexive,AttributeNames.v_No);
			verifyAndSetKamolsAttribute(avs,tag,2,'y',AttributeNames.i_Reflexive,AttributeNames.v_Yes);

			verifyAndSetKamolsAttribute(avs,tag,3,'i',AttributeNames.i_Izteiksme,AttributeNames.v_Iisteniibas);
			verifyAndSetKamolsAttribute(avs,tag,3,'r',AttributeNames.i_Izteiksme,AttributeNames.v_Atstaastiijuma);
			verifyAndSetKamolsAttribute(avs,tag,3,'c',AttributeNames.i_Izteiksme,AttributeNames.v_Veeleejuma);
			verifyAndSetKamolsAttribute(avs,tag,3,'d',AttributeNames.i_Izteiksme,AttributeNames.v_Vajadziibas);
			verifyAndSetKamolsAttribute(avs,tag,3,'m',AttributeNames.i_Izteiksme,AttributeNames.v_Paveeles);
			verifyAndSetKamolsAttribute(avs,tag,3,'n',AttributeNames.i_Izteiksme,AttributeNames.v_Nenoteiksme);
			verifyAndSetKamolsAttribute(avs,tag,3,'p',AttributeNames.i_Izteiksme,AttributeNames.v_Participle);

			verifyAndSetKamolsAttribute(avs,tag,4,'p',AttributeNames.i_Laiks,AttributeNames.v_Tagadne);
			verifyAndSetKamolsAttribute(avs,tag,4,'f',AttributeNames.i_Laiks,AttributeNames.v_Naakotne);
			verifyAndSetKamolsAttribute(avs,tag,4,'s',AttributeNames.i_Laiks,AttributeNames.v_Pagaatne);
			verifyAndSetKamolsAttribute(avs,tag,4,'0',AttributeNames.i_Laiks,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,5,'t',AttributeNames.i_Transitivity,AttributeNames.v_Transitive);
			verifyAndSetKamolsAttribute(avs,tag,5,'i',AttributeNames.i_Transitivity,AttributeNames.v_Intransitive);
			verifyAndSetKamolsAttribute(avs,tag,5,'0',AttributeNames.i_Transitivity,AttributeNames.v_NA);  //? Nav tagset

			verifyAndSetKamolsAttribute(avs,tag,6,'1',AttributeNames.i_Konjugaacija,"1");
			verifyAndSetKamolsAttribute(avs,tag,6,'2',AttributeNames.i_Konjugaacija,"2");
			verifyAndSetKamolsAttribute(avs,tag,6,'3',AttributeNames.i_Konjugaacija,"3");
			verifyAndSetKamolsAttribute(avs,tag,6,'i',AttributeNames.i_Konjugaacija,AttributeNames.v_Nekaartns);
			verifyAndSetKamolsAttribute(avs,tag,6,'0',AttributeNames.i_Konjugaacija,AttributeNames.v_NA);  //? Nav tagset

			verifyAndSetKamolsAttribute(avs,tag,7,'1',AttributeNames.i_Person,"1");
			verifyAndSetKamolsAttribute(avs,tag,7,'2',AttributeNames.i_Person,"2");
			verifyAndSetKamolsAttribute(avs,tag,7,'3',AttributeNames.i_Person,"3");
			verifyAndSetKamolsAttribute(avs,tag,7,'0',AttributeNames.i_Person,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,8,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetKamolsAttribute(avs,tag,8,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetKamolsAttribute(avs,tag,8,'0',AttributeNames.i_Number,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,9,'a',AttributeNames.i_Voice,AttributeNames.v_Active);
			verifyAndSetKamolsAttribute(avs,tag,9,'p',AttributeNames.i_Voice,AttributeNames.v_Passive);
			verifyAndSetKamolsAttribute(avs,tag,9,'0',AttributeNames.i_Voice,AttributeNames.v_NA);
			
			verifyAndSetKamolsAttribute(avs,tag,10,'y',AttributeNames.i_Noliegums,AttributeNames.v_Yes);
			verifyAndSetKamolsAttribute(avs,tag,10,'n',AttributeNames.i_Noliegums,AttributeNames.v_No);
			
			//verifyAndSetKamolsAttribute(avs,tag,10,'n',AttributeNames.i_Noliegums,null);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Verb) && avs.isMatchingStrong(AttributeNames.i_Izteiksme, AttributeNames.v_Participle)) {
			tag.setLength(0);
			if (defaults)
				tag.append("vmnpdmsnapn");
			else tag.append("v__p_______");


			verifyAndSetKamolsAttribute(avs,tag,1,'m',AttributeNames.i_VerbType,AttributeNames.v_MainVerb);
			verifyAndSetKamolsAttribute(avs,tag,1,'a',AttributeNames.i_VerbType,AttributeNames.v_PaliigDv);
			verifyAndSetKamolsAttribute(avs,tag,1,'o',AttributeNames.i_VerbType,AttributeNames.v_Modaals);
			verifyAndSetKamolsAttribute(avs,tag,1,'p',AttributeNames.i_VerbType,AttributeNames.v_Faazes);
			verifyAndSetKamolsAttribute(avs,tag,1,'e',AttributeNames.i_VerbType,AttributeNames.v_IzpausmesVeida);
			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_VerbType,AttributeNames.v_Buut);
			verifyAndSetKamolsAttribute(avs,tag,1,'t',AttributeNames.i_VerbType,AttributeNames.v_TiktTapt);
			verifyAndSetKamolsAttribute(avs,tag,1,'g',AttributeNames.i_VerbType,AttributeNames.v_Nebuut);

			verifyAndSetKamolsAttribute(avs,tag,2,'n',AttributeNames.i_Reflexive,AttributeNames.v_No);
			verifyAndSetKamolsAttribute(avs,tag,2,'y',AttributeNames.i_Reflexive,AttributeNames.v_Yes);

			verifyAndSetKamolsAttribute(avs,tag,3,'i',AttributeNames.i_Izteiksme,AttributeNames.v_Iisteniibas);
			verifyAndSetKamolsAttribute(avs,tag,3,'r',AttributeNames.i_Izteiksme,AttributeNames.v_Atstaastiijuma);
			verifyAndSetKamolsAttribute(avs,tag,3,'c',AttributeNames.i_Izteiksme,AttributeNames.v_Veeleejuma);
			verifyAndSetKamolsAttribute(avs,tag,3,'d',AttributeNames.i_Izteiksme,AttributeNames.v_Vajadziibas);
			verifyAndSetKamolsAttribute(avs,tag,3,'m',AttributeNames.i_Izteiksme,AttributeNames.v_Paveeles);
			verifyAndSetKamolsAttribute(avs,tag,3,'n',AttributeNames.i_Izteiksme,AttributeNames.v_Nenoteiksme);
			verifyAndSetKamolsAttribute(avs,tag,3,'p',AttributeNames.i_Izteiksme,AttributeNames.v_Participle);

			verifyAndSetKamolsAttribute(avs,tag,4,'d',AttributeNames.i_Lokaamiiba,AttributeNames.v_Lokaams);
			verifyAndSetKamolsAttribute(avs,tag,4,'p',AttributeNames.i_Lokaamiiba,AttributeNames.v_DaljeejiLokaams);
			verifyAndSetKamolsAttribute(avs,tag,4,'u',AttributeNames.i_Lokaamiiba,AttributeNames.v_Nelokaams);

			verifyAndSetKamolsAttribute(avs,tag,5,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetKamolsAttribute(avs,tag,5,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetKamolsAttribute(avs,tag,5,'0',AttributeNames.i_Gender,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,6,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetKamolsAttribute(avs,tag,6,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetKamolsAttribute(avs,tag,6,'0',AttributeNames.i_Number,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,7,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetKamolsAttribute(avs,tag,7,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetKamolsAttribute(avs,tag,7,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetKamolsAttribute(avs,tag,7,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetKamolsAttribute(avs,tag,7,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetKamolsAttribute(avs,tag,7,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetKamolsAttribute(avs,tag,7,'0',AttributeNames.i_Case,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,8,'a',AttributeNames.i_Voice,AttributeNames.v_Active);
			verifyAndSetKamolsAttribute(avs,tag,8,'p',AttributeNames.i_Voice,AttributeNames.v_Passive);
			verifyAndSetKamolsAttribute(avs,tag,8,'0',AttributeNames.i_Voice,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,9,'p',AttributeNames.i_Laiks,AttributeNames.v_Tagadne);
			verifyAndSetKamolsAttribute(avs,tag,9,'s',AttributeNames.i_Laiks,AttributeNames.v_Pagaatne);
			verifyAndSetKamolsAttribute(avs,tag,9,'f',AttributeNames.i_Laiks,AttributeNames.v_Naakotne);
			verifyAndSetKamolsAttribute(avs,tag,9,'0',AttributeNames.i_Laiks,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,10,'n',AttributeNames.i_Definiteness,AttributeNames.v_Indefinite);
			verifyAndSetKamolsAttribute(avs,tag,10,'y',AttributeNames.i_Definiteness,AttributeNames.v_Definite);
			verifyAndSetKamolsAttribute(avs,tag,10,'0',AttributeNames.i_Definiteness,AttributeNames.v_NA);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Adjective)) {

			tag.setLength(0);
			if (defaults)
				tag.append("afmsnnp");
			else tag.append("a______");


			verifyAndSetKamolsAttribute(avs,tag,1,'f',AttributeNames.i_AdjectiveType,AttributeNames.v_QualificativeAdjective);
			verifyAndSetKamolsAttribute(avs,tag,1,'r',AttributeNames.i_AdjectiveType,AttributeNames.v_RelativeAdjective);

			verifyAndSetKamolsAttribute(avs,tag,2,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetKamolsAttribute(avs,tag,2,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);

			verifyAndSetKamolsAttribute(avs,tag,3,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetKamolsAttribute(avs,tag,3,'p',AttributeNames.i_Number,AttributeNames.v_Plural);

			verifyAndSetKamolsAttribute(avs,tag,4,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetKamolsAttribute(avs,tag,4,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetKamolsAttribute(avs,tag,4,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetKamolsAttribute(avs,tag,4,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetKamolsAttribute(avs,tag,4,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetKamolsAttribute(avs,tag,4,'l',AttributeNames.i_Case,AttributeNames.v_Locative);

			verifyAndSetKamolsAttribute(avs,tag,5,'n',AttributeNames.i_Definiteness,AttributeNames.v_Indefinite);
			verifyAndSetKamolsAttribute(avs,tag,5,'y',AttributeNames.i_Definiteness,AttributeNames.v_Definite);

			verifyAndSetKamolsAttribute(avs,tag,6,'p',AttributeNames.i_Degree,AttributeNames.v_Positive);
			verifyAndSetKamolsAttribute(avs,tag,6,'c',AttributeNames.i_Degree,AttributeNames.v_Comparative);
			verifyAndSetKamolsAttribute(avs,tag,6,'s',AttributeNames.i_Degree,AttributeNames.v_Superlative);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Pronoun)) {

			tag.setLength(0);
			if (defaults)
				tag.append("pp1msnn");
			//else tag.append("p_____n");
			else tag.append("p______");

			verifyAndSetKamolsAttribute(avs,tag,1,'p',AttributeNames.i_VvTips,AttributeNames.v_Personu);
			verifyAndSetKamolsAttribute(avs,tag,1,'x',AttributeNames.i_VvTips,AttributeNames.v_Atgriezeniskie);
			verifyAndSetKamolsAttribute(avs,tag,1,'s',AttributeNames.i_VvTips,AttributeNames.v_Piederiibas);
			verifyAndSetKamolsAttribute(avs,tag,1,'d',AttributeNames.i_VvTips,AttributeNames.v_Noraadaamie);
			verifyAndSetKamolsAttribute(avs,tag,1,'i',AttributeNames.i_VvTips,AttributeNames.v_Nenoteiktie);
			verifyAndSetKamolsAttribute(avs,tag,1,'q',AttributeNames.i_VvTips,AttributeNames.v_Jautaajamie);
			verifyAndSetKamolsAttribute(avs,tag,1,'r',AttributeNames.i_VvTips,AttributeNames.v_AttieksmesVv);
			verifyAndSetKamolsAttribute(avs,tag,1,'g',AttributeNames.i_VvTips,AttributeNames.v_Noteiktie);

			verifyAndSetKamolsAttribute(avs,tag,2,'1',AttributeNames.i_Person,"1");
			verifyAndSetKamolsAttribute(avs,tag,2,'2',AttributeNames.i_Person,"2");
			verifyAndSetKamolsAttribute(avs,tag,2,'3',AttributeNames.i_Person,"3");
			verifyAndSetKamolsAttribute(avs,tag,2,'0',AttributeNames.i_Person,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,3,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetKamolsAttribute(avs,tag,3,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetKamolsAttribute(avs,tag,3,'0',AttributeNames.i_Gender,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,4,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetKamolsAttribute(avs,tag,4,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetKamolsAttribute(avs,tag,4,'0',AttributeNames.i_Number,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,5,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetKamolsAttribute(avs,tag,5,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetKamolsAttribute(avs,tag,5,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetKamolsAttribute(avs,tag,5,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetKamolsAttribute(avs,tag,5,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetKamolsAttribute(avs,tag,5,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetKamolsAttribute(avs,tag,5,'u',AttributeNames.i_Case,AttributeNames.v_Nelokaams);

			verifyAndSetKamolsAttribute(avs,tag,6,'n',AttributeNames.i_Noliegums,AttributeNames.v_No);
			verifyAndSetKamolsAttribute(avs,tag,6,'y',AttributeNames.i_Noliegums,AttributeNames.v_Yes);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Adverb)) {

			tag.setLength(0);
			if (defaults)
				tag.append("rp_");
			else tag.append("r__");

			verifyAndSetKamolsAttribute(avs,tag,1,'r',AttributeNames.i_Degree,AttributeNames.v_Relative);
			verifyAndSetKamolsAttribute(avs,tag,1,'p',AttributeNames.i_Degree,AttributeNames.v_Positive);
			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_Degree,AttributeNames.v_Comparative);
			verifyAndSetKamolsAttribute(avs,tag,1,'s',AttributeNames.i_Degree,AttributeNames.v_Superlative);
			verifyAndSetKamolsAttribute(avs,tag,1,'0',AttributeNames.i_Degree,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,2,'q',AttributeNames.i_ApstTips,AttributeNames.v_Meera);
			verifyAndSetKamolsAttribute(avs,tag,2,'m',AttributeNames.i_ApstTips,AttributeNames.v_Veida);
			verifyAndSetKamolsAttribute(avs,tag,2,'p',AttributeNames.i_ApstTips,AttributeNames.v_Vietas);
			verifyAndSetKamolsAttribute(avs,tag,2,'t',AttributeNames.i_ApstTips,AttributeNames.v_Laika);
			verifyAndSetKamolsAttribute(avs,tag,2,'c',AttributeNames.i_ApstTips,AttributeNames.v_Ceelonja);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Preposition)) {

			tag.setLength(0);
			if (defaults)
				tag.append("sppgn");
			else tag.append("s____");

			verifyAndSetKamolsAttribute(avs,tag,1,'p',AttributeNames.i_Novietojums,AttributeNames.v_Pirms);
			verifyAndSetKamolsAttribute(avs,tag,1,'t',AttributeNames.i_Novietojums,AttributeNames.v_Peec);

			verifyAndSetKamolsAttribute(avs,tag,2,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetKamolsAttribute(avs,tag,2,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetKamolsAttribute(avs,tag,2,'0',AttributeNames.i_Number,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,3,'g',AttributeNames.i_Rekcija,AttributeNames.v_Genitive);
			verifyAndSetKamolsAttribute(avs,tag,3,'d',AttributeNames.i_Rekcija,AttributeNames.v_Dative);
			verifyAndSetKamolsAttribute(avs,tag,3,'a',AttributeNames.i_Rekcija,AttributeNames.v_Accusative);
			verifyAndSetKamolsAttribute(avs,tag,3,'0',AttributeNames.i_Rekcija,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,4,'y',AttributeNames.i_VietasApstNoziime,AttributeNames.v_Yes);
			verifyAndSetKamolsAttribute(avs,tag,4,'n',AttributeNames.i_VietasApstNoziime,AttributeNames.v_No);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Conjunction)) {

			tag.setLength(0);
			if (defaults)
				tag.append("ccs");
			else tag.append("c__");

			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_SaikljaTips,AttributeNames.v_Sakaartojuma);
			verifyAndSetKamolsAttribute(avs,tag,1,'s',AttributeNames.i_SaikljaTips,AttributeNames.v_Pakaartojuma);

			verifyAndSetKamolsAttribute(avs,tag,2,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetKamolsAttribute(avs,tag,2,'d',AttributeNames.i_Uzbuuve,AttributeNames.v_Divkaarshs);
			verifyAndSetKamolsAttribute(avs,tag,2,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
			verifyAndSetKamolsAttribute(avs,tag,2,'r',AttributeNames.i_Uzbuuve,AttributeNames.v_Atkaartots);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Numeral)) {

			tag.setLength(0);
			if (defaults)
				tag.append("mcs_snv");
			else tag.append("m______");

			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_SkaitljaTips,AttributeNames.v_PamataSv);
			verifyAndSetKamolsAttribute(avs,tag,1,'o',AttributeNames.i_SkaitljaTips,AttributeNames.v_Kaartas);
			verifyAndSetKamolsAttribute(avs,tag,1,'f',AttributeNames.i_SkaitljaTips,AttributeNames.v_Daljskaitlis);

			verifyAndSetKamolsAttribute(avs,tag,2,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetKamolsAttribute(avs,tag,2,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
			verifyAndSetKamolsAttribute(avs,tag,2,'j',AttributeNames.i_Uzbuuve,AttributeNames.v_Savienojums);

			verifyAndSetKamolsAttribute(avs,tag,3,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetKamolsAttribute(avs,tag,3,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetKamolsAttribute(avs,tag,3,'0',AttributeNames.i_Gender,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,4,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetKamolsAttribute(avs,tag,4,'p',AttributeNames.i_Number,AttributeNames.v_Plural);

			verifyAndSetKamolsAttribute(avs,tag,5,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetKamolsAttribute(avs,tag,5,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetKamolsAttribute(avs,tag,5,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetKamolsAttribute(avs,tag,5,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetKamolsAttribute(avs,tag,5,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetKamolsAttribute(avs,tag,5,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetKamolsAttribute(avs,tag,5,'0',AttributeNames.i_Case,AttributeNames.v_NA);

			verifyAndSetKamolsAttribute(avs,tag,6,'v',AttributeNames.i_Order,AttributeNames.v_Ones);
			verifyAndSetKamolsAttribute(avs,tag,6,'p',AttributeNames.i_Order,AttributeNames.v_Teens);
			verifyAndSetKamolsAttribute(avs,tag,6,'d',AttributeNames.i_Order,AttributeNames.v_Tens);
			verifyAndSetKamolsAttribute(avs,tag,6,'s',AttributeNames.i_Order,AttributeNames.v_Hundreds);
			verifyAndSetKamolsAttribute(avs,tag,6,'t',AttributeNames.i_Order,AttributeNames.v_Thousands);
			verifyAndSetKamolsAttribute(avs,tag,6,'m',AttributeNames.i_Order,AttributeNames.v_Millions);
			verifyAndSetKamolsAttribute(avs,tag,6,'r',AttributeNames.i_Order,AttributeNames.v_Billions);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Interjection)) {

			tag.setLength(0);
			if (defaults)
				tag.append("is");
			else tag.append("i_");

			verifyAndSetKamolsAttribute(avs,tag,1,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Particle)) {

			tag.setLength(0);
			if (defaults)
				tag.append("qs");
			else tag.append("q_");

			verifyAndSetKamolsAttribute(avs,tag,1,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
		}
		else if (pos.equalsIgnoreCase(AttributeNames.v_Punctuation)) {
			tag.setLength(0);
			if (defaults)
				tag.append("zc");
			else tag.append("z_");

			verifyAndSetKamolsAttribute(avs,tag,1,'c',AttributeNames.i_PieturziimesTips,AttributeNames.v_Komats);
			verifyAndSetKamolsAttribute(avs,tag,1,'q',AttributeNames.i_PieturziimesTips,AttributeNames.v_Peedinja);
			verifyAndSetKamolsAttribute(avs,tag,1,'s',AttributeNames.i_PieturziimesTips,AttributeNames.v_Punkts);
			verifyAndSetKamolsAttribute(avs,tag,1,'b',AttributeNames.i_PieturziimesTips,AttributeNames.v_Iekava);
			verifyAndSetKamolsAttribute(avs,tag,1,'d',AttributeNames.i_PieturziimesTips,AttributeNames.v_Domuziime);
			verifyAndSetKamolsAttribute(avs,tag,1,'o',AttributeNames.i_PieturziimesTips,AttributeNames.v_Kols);
			verifyAndSetKamolsAttribute(avs,tag,1,'x',AttributeNames.i_PieturziimesTips,AttributeNames.v_Cita);
		} else if (pos.equalsIgnoreCase(AttributeNames.v_Abbreviation)) {
			tag.setLength(0);
			tag.append("y");
		} else if (pos.equalsIgnoreCase(AttributeNames.v_Residual)) {
			tag.setLength(0);
			tag.append("xx");
			
			verifyAndSetKamolsAttribute(avs,tag,1,'f',AttributeNames.i_ResidualType,AttributeNames.v_Foreign);
			verifyAndSetKamolsAttribute(avs,tag,1,'t',AttributeNames.i_ResidualType,AttributeNames.v_Typo);
			verifyAndSetKamolsAttribute(avs,tag,1,'n',AttributeNames.i_ResidualType,AttributeNames.v_Number);
			verifyAndSetKamolsAttribute(avs,tag,1,'o',AttributeNames.i_ResidualType,AttributeNames.v_Ordinal);
			verifyAndSetKamolsAttribute(avs,tag,1,'u',AttributeNames.i_ResidualType,AttributeNames.v_URI);
		}

		return tag;
	}


	/**
	 * Check if tag has given value in specified position and if so then set
	 * corresponding attribute-value pair in the given attribute-value
	 * structure.
	 */
	private static void verifyAndSetAVSAttribute(
			String tag, FeatureStructure avs, int index, char tagValue,
			String attribute, String attributeValue) {
		//TODO - šī metode 'silently fails' uz jauniem variantiem/simboliem
		//marķējumā. Normāli atrisinās tikai šīs klases pāreja uz xml
		//konfigfaila apstrādi
		if (index >= tag.length()) return;
		if (tag.charAt(index) == tagValue)
			avs.addAttribute(attribute, attributeValue);
	}

	/**
	 * Remove formating from Kamols-style tag.
	 */
	public static String removeKamolsMarkupFormating(String tag){
		String result = "";
		if (!tag.contains(",")) return "x";

		int depth = 0;
		int commas = 0;
		for (char c : tag.toCharArray()) {
			if (c=='[') depth++;
			if (c==']') depth--;
			if (depth == 1 && c==',') commas++;

			if (commas == 2) result = result + c;
		}

		result = result.replaceAll("_[A-Z0-9]*"   ,   "_");
		result = result.replaceAll("(\\[|\\]|\\,| )","");
		return result;
	}


	/**
	 * Convert SemTi-Kamols markup tag to internal attribute-value structure.
	 */
	public static AttributeValues fromKamolsMarkup(String tag) {
		AttributeValues attributes = new AttributeValues();
		if (tag.equals("")) return attributes;

		switch (tag.charAt(0)) {
		case 'n':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);

			verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_NounType,AttributeNames.v_CommonNoun);
			verifyAndSetAVSAttribute(tag,attributes,1,'p',AttributeNames.i_NounType,AttributeNames.v_ProperNoun);
			verifyAndSetAVSAttribute(tag,attributes,2,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetAVSAttribute(tag,attributes,2,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetAVSAttribute(tag,attributes,2,'k',AttributeNames.i_Gender,AttributeNames.v_Kopdzimte);
			verifyAndSetAVSAttribute(tag,attributes,3,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetAVSAttribute(tag,attributes,3,'v',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetAVSAttribute(tag,attributes,3,'v',AttributeNames.i_NumberSpecial,AttributeNames.v_SingulareTantum);
			verifyAndSetAVSAttribute(tag,attributes,3,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetAVSAttribute(tag,attributes,3,'d',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetAVSAttribute(tag,attributes,3,'d',AttributeNames.i_NumberSpecial,AttributeNames.v_PlurareTantum);
			verifyAndSetAVSAttribute(tag,attributes,4,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetAVSAttribute(tag,attributes,4,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetAVSAttribute(tag,attributes,4,'s',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetAVSAttribute(tag,attributes,4,'s',AttributeNames.i_CaseSpecial,AttributeNames.v_InflexibleGenitive);
			verifyAndSetAVSAttribute(tag,attributes,4,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetAVSAttribute(tag,attributes,4,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetAVSAttribute(tag,attributes,4,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetAVSAttribute(tag,attributes,4,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetAVSAttribute(tag,attributes,5,'1',AttributeNames.i_Declension,"1");
			verifyAndSetAVSAttribute(tag,attributes,5,'2',AttributeNames.i_Declension,"2");
			verifyAndSetAVSAttribute(tag,attributes,5,'3',AttributeNames.i_Declension,"3");
			verifyAndSetAVSAttribute(tag,attributes,5,'4',AttributeNames.i_Declension,"4");
			verifyAndSetAVSAttribute(tag,attributes,5,'5',AttributeNames.i_Declension,"5");
			verifyAndSetAVSAttribute(tag,attributes,5,'6',AttributeNames.i_Declension,"6");
			verifyAndSetAVSAttribute(tag,attributes,5,'r',AttributeNames.i_Declension,AttributeNames.v_Reflexive);
			verifyAndSetAVSAttribute(tag,attributes,5,'0',AttributeNames.i_Declension,AttributeNames.v_NA);
			break;
		case 'v':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);

			if (tag.charAt(3) != 'p') { // nav divdabis
				verifyAndSetAVSAttribute(tag,attributes,1,'m',AttributeNames.i_VerbType,AttributeNames.v_MainVerb);
				verifyAndSetAVSAttribute(tag,attributes,1,'a',AttributeNames.i_VerbType,AttributeNames.v_PaliigDv);
				verifyAndSetAVSAttribute(tag,attributes,1,'o',AttributeNames.i_VerbType,AttributeNames.v_Modaals);
				verifyAndSetAVSAttribute(tag,attributes,1,'p',AttributeNames.i_VerbType,AttributeNames.v_Faazes);
				verifyAndSetAVSAttribute(tag,attributes,1,'e',AttributeNames.i_VerbType,AttributeNames.v_IzpausmesVeida);
				verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_VerbType,AttributeNames.v_Buut);
				verifyAndSetAVSAttribute(tag,attributes,1,'t',AttributeNames.i_VerbType,AttributeNames.v_TiktTapt);
				verifyAndSetAVSAttribute(tag,attributes,1,'g',AttributeNames.i_VerbType,AttributeNames.v_Nebuut);

				verifyAndSetAVSAttribute(tag,attributes,2,'n',AttributeNames.i_Reflexive,AttributeNames.v_No);
				verifyAndSetAVSAttribute(tag,attributes,2,'y',AttributeNames.i_Reflexive,AttributeNames.v_Yes);
				verifyAndSetAVSAttribute(tag,attributes,3,'i',AttributeNames.i_Izteiksme,AttributeNames.v_Iisteniibas);
				verifyAndSetAVSAttribute(tag,attributes,3,'r',AttributeNames.i_Izteiksme,AttributeNames.v_Atstaastiijuma);
				verifyAndSetAVSAttribute(tag,attributes,3,'c',AttributeNames.i_Izteiksme,AttributeNames.v_Veeleejuma);
				verifyAndSetAVSAttribute(tag,attributes,3,'d',AttributeNames.i_Izteiksme,AttributeNames.v_Vajadziibas);
				verifyAndSetAVSAttribute(tag,attributes,3,'m',AttributeNames.i_Izteiksme,AttributeNames.v_Paveeles);
				verifyAndSetAVSAttribute(tag,attributes,3,'n',AttributeNames.i_Izteiksme,AttributeNames.v_Nenoteiksme);
				verifyAndSetAVSAttribute(tag,attributes,3,'p',AttributeNames.i_Izteiksme,AttributeNames.v_Participle);
				verifyAndSetAVSAttribute(tag,attributes,4,'p',AttributeNames.i_Laiks,AttributeNames.v_Tagadne);
				verifyAndSetAVSAttribute(tag,attributes,4,'f',AttributeNames.i_Laiks,AttributeNames.v_Naakotne);
				verifyAndSetAVSAttribute(tag,attributes,4,'s',AttributeNames.i_Laiks,AttributeNames.v_Pagaatne);
				verifyAndSetAVSAttribute(tag,attributes,4,'0',AttributeNames.i_Laiks,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,5,'t',AttributeNames.i_Transitivity,AttributeNames.v_Transitive);
				verifyAndSetAVSAttribute(tag,attributes,5,'i',AttributeNames.i_Transitivity,AttributeNames.v_Intransitive);
				verifyAndSetAVSAttribute(tag,attributes,5,'0',AttributeNames.i_Transitivity,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,6,'1',AttributeNames.i_Konjugaacija,"1");
				verifyAndSetAVSAttribute(tag,attributes,6,'2',AttributeNames.i_Konjugaacija,"2");
				verifyAndSetAVSAttribute(tag,attributes,6,'3',AttributeNames.i_Konjugaacija,"3");
				verifyAndSetAVSAttribute(tag,attributes,6,'i',AttributeNames.i_Konjugaacija,AttributeNames.v_Nekaartns);
				verifyAndSetAVSAttribute(tag,attributes,6,'0',AttributeNames.i_Konjugaacija,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,7,'1',AttributeNames.i_Person,"1");
				verifyAndSetAVSAttribute(tag,attributes,7,'2',AttributeNames.i_Person,"2");
				verifyAndSetAVSAttribute(tag,attributes,7,'3',AttributeNames.i_Person,"3");
				verifyAndSetAVSAttribute(tag,attributes,7,'0',AttributeNames.i_Person,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,8,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
				verifyAndSetAVSAttribute(tag,attributes,8,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
				verifyAndSetAVSAttribute(tag,attributes,8,'0',AttributeNames.i_Number,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,9,'a',AttributeNames.i_Voice,AttributeNames.v_Active);
				verifyAndSetAVSAttribute(tag,attributes,9,'p',AttributeNames.i_Voice,AttributeNames.v_Passive);
				verifyAndSetAVSAttribute(tag,attributes,9,'0',AttributeNames.i_Voice,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,10,'n',AttributeNames.i_Noliegums,AttributeNames.v_No);
				verifyAndSetAVSAttribute(tag,attributes,10,'y',AttributeNames.i_Noliegums,AttributeNames.v_Yes);

			} else { // ir divdabis
				verifyAndSetAVSAttribute(tag,attributes,1,'m',AttributeNames.i_VerbType,AttributeNames.v_MainVerb);
				verifyAndSetAVSAttribute(tag,attributes,1,'a',AttributeNames.i_VerbType,AttributeNames.v_PaliigDv);
				verifyAndSetAVSAttribute(tag,attributes,1,'o',AttributeNames.i_VerbType,AttributeNames.v_Modaals);
				verifyAndSetAVSAttribute(tag,attributes,1,'p',AttributeNames.i_VerbType,AttributeNames.v_Faazes);
				verifyAndSetAVSAttribute(tag,attributes,1,'e',AttributeNames.i_VerbType,AttributeNames.v_IzpausmesVeida);
				verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_VerbType,AttributeNames.v_Buut);
				verifyAndSetAVSAttribute(tag,attributes,1,'t',AttributeNames.i_VerbType,AttributeNames.v_TiktTapt);
				verifyAndSetAVSAttribute(tag,attributes,1,'g',AttributeNames.i_VerbType,AttributeNames.v_Nebuut);

				verifyAndSetAVSAttribute(tag,attributes,2,'n',AttributeNames.i_Reflexive,AttributeNames.v_No);
				verifyAndSetAVSAttribute(tag,attributes,2,'y',AttributeNames.i_Reflexive,AttributeNames.v_Yes);
				verifyAndSetAVSAttribute(tag,attributes,3,'i',AttributeNames.i_Izteiksme,AttributeNames.v_Iisteniibas);
				verifyAndSetAVSAttribute(tag,attributes,3,'r',AttributeNames.i_Izteiksme,AttributeNames.v_Atstaastiijuma);
				verifyAndSetAVSAttribute(tag,attributes,3,'c',AttributeNames.i_Izteiksme,AttributeNames.v_Veeleejuma);
				verifyAndSetAVSAttribute(tag,attributes,3,'d',AttributeNames.i_Izteiksme,AttributeNames.v_Vajadziibas);
				verifyAndSetAVSAttribute(tag,attributes,3,'m',AttributeNames.i_Izteiksme,AttributeNames.v_Paveeles);
				verifyAndSetAVSAttribute(tag,attributes,3,'n',AttributeNames.i_Izteiksme,AttributeNames.v_Nenoteiksme);
				verifyAndSetAVSAttribute(tag,attributes,3,'p',AttributeNames.i_Izteiksme,AttributeNames.v_Participle);
				verifyAndSetAVSAttribute(tag,attributes,4,'d',AttributeNames.i_Lokaamiiba,AttributeNames.v_Lokaams);
				verifyAndSetAVSAttribute(tag,attributes,4,'p',AttributeNames.i_Lokaamiiba,AttributeNames.v_DaljeejiLokaams);
				verifyAndSetAVSAttribute(tag,attributes,4,'u',AttributeNames.i_Lokaamiiba,AttributeNames.v_Nelokaams);
				verifyAndSetAVSAttribute(tag,attributes,5,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
				verifyAndSetAVSAttribute(tag,attributes,5,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
				verifyAndSetAVSAttribute(tag,attributes,5,'0',AttributeNames.i_Gender,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,6,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
				verifyAndSetAVSAttribute(tag,attributes,6,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
				verifyAndSetAVSAttribute(tag,attributes,6,'0',AttributeNames.i_Number,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,7,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
				verifyAndSetAVSAttribute(tag,attributes,7,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
				verifyAndSetAVSAttribute(tag,attributes,7,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
				verifyAndSetAVSAttribute(tag,attributes,7,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
				verifyAndSetAVSAttribute(tag,attributes,7,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
				verifyAndSetAVSAttribute(tag,attributes,7,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
				verifyAndSetAVSAttribute(tag,attributes,7,'0',AttributeNames.i_Case,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,8,'a',AttributeNames.i_Voice,AttributeNames.v_Active);
				verifyAndSetAVSAttribute(tag,attributes,8,'p',AttributeNames.i_Voice,AttributeNames.v_Passive);
				verifyAndSetAVSAttribute(tag,attributes,8,'0',AttributeNames.i_Voice,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,9,'p',AttributeNames.i_Laiks,AttributeNames.v_Tagadne);
				verifyAndSetAVSAttribute(tag,attributes,9,'f',AttributeNames.i_Laiks,AttributeNames.v_Naakotne);
				verifyAndSetAVSAttribute(tag,attributes,9,'s',AttributeNames.i_Laiks,AttributeNames.v_Pagaatne);
				verifyAndSetAVSAttribute(tag,attributes,9,'0',AttributeNames.i_Laiks,AttributeNames.v_NA);
				verifyAndSetAVSAttribute(tag,attributes,10,'n',AttributeNames.i_Definiteness,AttributeNames.v_Indefinite);
				verifyAndSetAVSAttribute(tag,attributes,10,'y',AttributeNames.i_Definiteness,AttributeNames.v_Definite);
				verifyAndSetAVSAttribute(tag,attributes,10,'0',AttributeNames.i_Definiteness,AttributeNames.v_NA);
			}
			break;
		case 'a':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective);

			verifyAndSetAVSAttribute(tag,attributes,1,'f',AttributeNames.i_AdjectiveType,AttributeNames.v_QualificativeAdjective);
			verifyAndSetAVSAttribute(tag,attributes,1,'r',AttributeNames.i_AdjectiveType,AttributeNames.v_RelativeAdjective);
			verifyAndSetAVSAttribute(tag,attributes,2,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetAVSAttribute(tag,attributes,2,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetAVSAttribute(tag,attributes,3,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetAVSAttribute(tag,attributes,3,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetAVSAttribute(tag,attributes,4,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetAVSAttribute(tag,attributes,4,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetAVSAttribute(tag,attributes,4,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetAVSAttribute(tag,attributes,4,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetAVSAttribute(tag,attributes,4,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetAVSAttribute(tag,attributes,4,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetAVSAttribute(tag,attributes,5,'n',AttributeNames.i_Definiteness,AttributeNames.v_Indefinite);
			verifyAndSetAVSAttribute(tag,attributes,5,'y',AttributeNames.i_Definiteness,AttributeNames.v_Definite);
			verifyAndSetAVSAttribute(tag,attributes,6,'p',AttributeNames.i_Degree,AttributeNames.v_Positive);
			verifyAndSetAVSAttribute(tag,attributes,6,'c',AttributeNames.i_Degree,AttributeNames.v_Comparative);
			verifyAndSetAVSAttribute(tag,attributes,6,'s',AttributeNames.i_Degree,AttributeNames.v_Superlative);
			break;
		case 'p':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Pronoun);

			verifyAndSetAVSAttribute(tag,attributes,1,'p',AttributeNames.i_VvTips,AttributeNames.v_Personu);
			verifyAndSetAVSAttribute(tag,attributes,1,'x',AttributeNames.i_VvTips,AttributeNames.v_Atgriezeniskie);
			verifyAndSetAVSAttribute(tag,attributes,1,'s',AttributeNames.i_VvTips,AttributeNames.v_Piederiibas);
			verifyAndSetAVSAttribute(tag,attributes,1,'d',AttributeNames.i_VvTips,AttributeNames.v_Noraadaamie);
			verifyAndSetAVSAttribute(tag,attributes,1,'i',AttributeNames.i_VvTips,AttributeNames.v_Nenoteiktie);
			verifyAndSetAVSAttribute(tag,attributes,1,'q',AttributeNames.i_VvTips,AttributeNames.v_Jautaajamie);
			verifyAndSetAVSAttribute(tag,attributes,1,'r',AttributeNames.i_VvTips,AttributeNames.v_AttieksmesVv);
			verifyAndSetAVSAttribute(tag,attributes,1,'g',AttributeNames.i_VvTips,AttributeNames.v_Noteiktie);

			verifyAndSetAVSAttribute(tag,attributes,2,'1',AttributeNames.i_Person,"1");
			verifyAndSetAVSAttribute(tag,attributes,2,'2',AttributeNames.i_Person,"2");
			verifyAndSetAVSAttribute(tag,attributes,2,'3',AttributeNames.i_Person,"3");
			verifyAndSetAVSAttribute(tag,attributes,2,'0',AttributeNames.i_Person,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,3,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetAVSAttribute(tag,attributes,3,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetAVSAttribute(tag,attributes,3,'0',AttributeNames.i_Gender,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,4,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetAVSAttribute(tag,attributes,4,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetAVSAttribute(tag,attributes,4,'0',AttributeNames.i_Number,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,5,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetAVSAttribute(tag,attributes,5,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetAVSAttribute(tag,attributes,5,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetAVSAttribute(tag,attributes,5,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetAVSAttribute(tag,attributes,5,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetAVSAttribute(tag,attributes,5,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetAVSAttribute(tag,attributes,5,'u',AttributeNames.i_Case,AttributeNames.v_Nelokaams);
			verifyAndSetAVSAttribute(tag,attributes,6,'n',AttributeNames.i_Noliegums,AttributeNames.v_No);
			verifyAndSetAVSAttribute(tag,attributes,6,'y',AttributeNames.i_Noliegums,AttributeNames.v_Yes);
			verifyAndSetAVSAttribute(tag,attributes,7,'a',AttributeNames.i_Anafora,AttributeNames.v_Adjektiivu);
			verifyAndSetAVSAttribute(tag,attributes,7,'s',AttributeNames.i_Anafora,AttributeNames.v_Substantiivu);
			verifyAndSetAVSAttribute(tag,attributes,7,'0',AttributeNames.i_Anafora,AttributeNames.v_NA);
			break;

		case 'r':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adverb);

			verifyAndSetAVSAttribute(tag,attributes,1,'p',AttributeNames.i_Degree,AttributeNames.v_Positive);
			verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_Degree,AttributeNames.v_Comparative);
			verifyAndSetAVSAttribute(tag,attributes,1,'s',AttributeNames.i_Degree,AttributeNames.v_Superlative);
			verifyAndSetAVSAttribute(tag,attributes,1,'r',AttributeNames.i_Degree,AttributeNames.v_Relative);
			verifyAndSetAVSAttribute(tag,attributes,1,'0',AttributeNames.i_Degree,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,2,'q',AttributeNames.i_ApstTips,AttributeNames.v_Meera);
			verifyAndSetAVSAttribute(tag,attributes,2,'m',AttributeNames.i_ApstTips,AttributeNames.v_Veida);
			verifyAndSetAVSAttribute(tag,attributes,2,'p',AttributeNames.i_ApstTips,AttributeNames.v_Vietas);
			verifyAndSetAVSAttribute(tag,attributes,2,'t',AttributeNames.i_ApstTips,AttributeNames.v_Laika);
			verifyAndSetAVSAttribute(tag,attributes,2,'c',AttributeNames.i_ApstTips,AttributeNames.v_Ceelonja);
			break;

		case 's':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Preposition);

			verifyAndSetAVSAttribute(tag,attributes,1,'p',AttributeNames.i_Novietojums,AttributeNames.v_Pirms);
			verifyAndSetAVSAttribute(tag,attributes,1,'t',AttributeNames.i_Novietojums,AttributeNames.v_Peec);
			verifyAndSetAVSAttribute(tag,attributes,2,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetAVSAttribute(tag,attributes,2,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetAVSAttribute(tag,attributes,2,'0',AttributeNames.i_Number,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,3,'g',AttributeNames.i_Rekcija,AttributeNames.v_Genitive);
			verifyAndSetAVSAttribute(tag,attributes,3,'d',AttributeNames.i_Rekcija,AttributeNames.v_Dative);
			verifyAndSetAVSAttribute(tag,attributes,3,'a',AttributeNames.i_Rekcija,AttributeNames.v_Accusative);
			verifyAndSetAVSAttribute(tag,attributes,3,'0',AttributeNames.i_Rekcija,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,4,'y',AttributeNames.i_VietasApstNoziime,AttributeNames.v_Yes);
			verifyAndSetAVSAttribute(tag,attributes,4,'n',AttributeNames.i_VietasApstNoziime,AttributeNames.v_No);

			break;

		case 'c':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Conjunction);

			verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_SaikljaTips,AttributeNames.v_Sakaartojuma);
			verifyAndSetAVSAttribute(tag,attributes,1,'s',AttributeNames.i_SaikljaTips,AttributeNames.v_Pakaartojuma);
			verifyAndSetAVSAttribute(tag,attributes,2,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetAVSAttribute(tag,attributes,2,'d',AttributeNames.i_Uzbuuve,AttributeNames.v_Divkaarshs);
			verifyAndSetAVSAttribute(tag,attributes,2,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
			verifyAndSetAVSAttribute(tag,attributes,2,'r',AttributeNames.i_Uzbuuve,AttributeNames.v_Atkaartots);
			break;

		case 'm':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Numeral);

			verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_SkaitljaTips,AttributeNames.v_PamataSv);
			verifyAndSetAVSAttribute(tag,attributes,1,'o',AttributeNames.i_SkaitljaTips,AttributeNames.v_Kaartas);
			verifyAndSetAVSAttribute(tag,attributes,1,'f',AttributeNames.i_SkaitljaTips,AttributeNames.v_Daljskaitlis);
			verifyAndSetAVSAttribute(tag,attributes,2,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetAVSAttribute(tag,attributes,2,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
			verifyAndSetAVSAttribute(tag,attributes,2,'j',AttributeNames.i_Uzbuuve,AttributeNames.v_Savienojums);
			verifyAndSetAVSAttribute(tag,attributes,3,'m',AttributeNames.i_Gender,AttributeNames.v_Masculine);
			verifyAndSetAVSAttribute(tag,attributes,3,'f',AttributeNames.i_Gender,AttributeNames.v_Feminine);
			verifyAndSetAVSAttribute(tag,attributes,3,'0',AttributeNames.i_Gender,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,4,'s',AttributeNames.i_Number,AttributeNames.v_Singular);
			verifyAndSetAVSAttribute(tag,attributes,4,'p',AttributeNames.i_Number,AttributeNames.v_Plural);
			verifyAndSetAVSAttribute(tag,attributes,5,'n',AttributeNames.i_Case,AttributeNames.v_Nominative);
			verifyAndSetAVSAttribute(tag,attributes,5,'g',AttributeNames.i_Case,AttributeNames.v_Genitive);
			verifyAndSetAVSAttribute(tag,attributes,5,'d',AttributeNames.i_Case,AttributeNames.v_Dative);
			verifyAndSetAVSAttribute(tag,attributes,5,'a',AttributeNames.i_Case,AttributeNames.v_Accusative);
			verifyAndSetAVSAttribute(tag,attributes,5,'v',AttributeNames.i_Case,AttributeNames.v_Vocative);
			verifyAndSetAVSAttribute(tag,attributes,5,'l',AttributeNames.i_Case,AttributeNames.v_Locative);
			verifyAndSetAVSAttribute(tag,attributes,5,'0',AttributeNames.i_Case,AttributeNames.v_NA);
			verifyAndSetAVSAttribute(tag,attributes,6,'v',AttributeNames.i_Order,AttributeNames.v_Ones);
			verifyAndSetAVSAttribute(tag,attributes,6,'p',AttributeNames.i_Order,AttributeNames.v_Teens);
			verifyAndSetAVSAttribute(tag,attributes,6,'d',AttributeNames.i_Order,AttributeNames.v_Tens);
			verifyAndSetAVSAttribute(tag,attributes,6,'s',AttributeNames.i_Order,AttributeNames.v_Hundreds);
			verifyAndSetAVSAttribute(tag,attributes,6,'t',AttributeNames.i_Order,AttributeNames.v_Thousands);
			verifyAndSetAVSAttribute(tag,attributes,6,'m',AttributeNames.i_Order,AttributeNames.v_Millions);
			verifyAndSetAVSAttribute(tag,attributes,6,'r',AttributeNames.i_Order,AttributeNames.v_Billions);
			break;

		case 'i':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Interjection);

			verifyAndSetAVSAttribute(tag,attributes,1,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
			break;

		case 'q':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Particle);

			verifyAndSetAVSAttribute(tag,attributes,1,'s',AttributeNames.i_Uzbuuve,AttributeNames.v_Vienkaarshs);
			verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_Uzbuuve,AttributeNames.v_Salikts);
			break;

		case 'x':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
			verifyAndSetAVSAttribute(tag,attributes,1,'f',AttributeNames.i_ResidualType,AttributeNames.v_Foreign);
			verifyAndSetAVSAttribute(tag,attributes,1,'t',AttributeNames.i_ResidualType,AttributeNames.v_Typo);
			verifyAndSetAVSAttribute(tag,attributes,1,'n',AttributeNames.i_ResidualType,AttributeNames.v_Number);
			verifyAndSetAVSAttribute(tag,attributes,1,'o',AttributeNames.i_ResidualType,AttributeNames.v_Ordinal);
			verifyAndSetAVSAttribute(tag,attributes,1,'u',AttributeNames.i_ResidualType,AttributeNames.v_URI);
			break;

		case 'y':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Abbreviation);
			break;

		case 'z':
			attributes.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Punctuation);
			verifyAndSetAVSAttribute(tag,attributes,1,'c',AttributeNames.i_PieturziimesTips,AttributeNames.v_Komats);
			verifyAndSetAVSAttribute(tag,attributes,1,'q',AttributeNames.i_PieturziimesTips,AttributeNames.v_Peedinja);
			verifyAndSetAVSAttribute(tag,attributes,1,'s',AttributeNames.i_PieturziimesTips,AttributeNames.v_Punkts);
			verifyAndSetAVSAttribute(tag,attributes,1,'b',AttributeNames.i_PieturziimesTips,AttributeNames.v_Iekava);
			verifyAndSetAVSAttribute(tag,attributes,1,'d',AttributeNames.i_PieturziimesTips,AttributeNames.v_Domuziime);
			verifyAndSetAVSAttribute(tag,attributes,1,'o',AttributeNames.i_PieturziimesTips,AttributeNames.v_Kols);
			verifyAndSetAVSAttribute(tag,attributes,1,'x',AttributeNames.i_PieturziimesTips,AttributeNames.v_Cita);
			break;
		}

		return attributes;
	}

	
	// Pārceltas no klases PrologWrapper; jo tām atšķirībā no pārējā PrologWrapper nevajag
	// konstruktorā ielasīt leksikonu.
	
	/**
	 * Converts Kamols-style markup to Prolog list.
	 */
	public static String charsToPrologList(String chars) {
		StringBuilder sb = new StringBuilder(chars.length()*2+1);
		charsToPrologList(chars, sb);
		return sb.toString();
	}

	/**
	 * Converts Kamols-style markup to Prolog list.
	 */
	public static void charsToPrologList(String chars, StringBuilder sb) {

		sb.append('[');
		for (int i = 0; i < chars.length(); i++) {
			if (i > 0)
				sb.append(',');

			//FIXME - ne īstā vieta kristīnes marķējuma loģikai
			//FIXME - aizvākt visas anaforas prom. /Lauma
			if ((chars.startsWith("p") && i == 7) // elements, kurš ir kā saraksts no viena elem jādod
					|| (chars.startsWith("s") && i == 4)
					|| (chars.startsWith("m") && i == 6)  )
				sb.append("[" + chars.charAt(i) + "]");  
			else sb.append(chars.charAt(i));
		}
		sb.append(']');
	}

	/**
	 * Converts Word object to Prolog format necessary for chunker (A-table
	 * record).
	 * @param word			Word to be converted.
	 * @param toolgenerated	how the source for this word should be identified
	 * 						- 'tool' or 'guess'/'dict'.
	 * 						Nobody remembers the practical consequences.
	 * 						Probably this is used, when deciding whether to
	 * 						delete these records from A-table before rechunking.
	 * @return	transformed result.
	 */
	public static String WordToChunkerFormat(Word word, boolean toolgenerated) {
		if (!word.isRecognized())
			return ("[]");

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		
		Iterator<Wordform> i = word.wordforms.iterator();
		while (i.hasNext()) {
			if (sb.length() > 1)
				sb.append(',');
			Wordform vf = i.next();
			String kamolsMarkup = vf.getTag();
			sb.append('[');
			sb.append("'").append(prologEscape(word.getToken())).append("',");
			if (kamolsMarkup != null) 
				MarkupConverter.charsToPrologList(kamolsMarkup, sb);			
			else sb.append("[]");
			sb.append(",'").append(prologEscape(vf.getValue(AttributeNames.i_Lemma))).append("',");
			sb.append(toolgenerated ? "tool" : (vf.isMatchingStrong(AttributeNames.i_Source, "minējums pēc galotnes") ? "guess" : "dict"));
			sb.append(",'").append(prologEscape(vf.getValue("Nozīme 1"))).append("'");
			sb.append(']');
		}
		
		sb.append(']');
		
		return sb.toString();
	}
	
	/**
	 * Escape string in Prolog style.
	 */
	public static String prologEscape(String s) {
		return s != null ? s.replaceAll("\\'", "\\\\'") : null;
	}
	
}