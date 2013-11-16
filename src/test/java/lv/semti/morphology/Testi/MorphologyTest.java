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
package lv.semti.morphology.Testi;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lv.semti.morphology.Testi.MorphoEvaluate.Etalons;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.*;
import lv.semti.morphology.lexicon.*;

public class MorphologyTest {
	private static Analyzer locītājs;

	private void assertNounInflection(List<Wordform> forms, String number, String nounCase, String gender, String validForm) {
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_Case, nounCase);
		testset.addAttribute(AttributeNames.i_Number, number);
		if (!gender.isEmpty()) testset.addAttribute(AttributeNames.i_Gender, gender);
		
		assertInflection(forms, testset, validForm);
	}
	
	private void assertInflection(List<Wordform> forms, AttributeValues testset, String validForm) {
		boolean found = false;
		for (Wordform wf : forms) {
			if (wf.isMatchingWeak(testset)) {
				assertEquals(validForm, wf.getToken());
				found = true;
				break;
			}
		}
		assertTrue(found);		
	}

	@SuppressWarnings("unused")
	private void describe(List<Wordform> formas) {
		PrintWriter izeja;
		try {
			izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
			for (Wordform forma : formas)  {
				forma.describe(izeja);
				izeja.println();
			}
			izeja.flush();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			locītājs = new Analyzer("dist/Lexicon.xml", false);
		} catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	@Before
	public void defaultsettings() { 
		locītājs.defaultSettings();
		locītājs.setCacheSize(0);
		locītājs.clearCache();
    }
	
	//FIXME - jāpārtaisa uz parametrizētiem testiem...
	
	@Test
	public void cirvis() {
		Word cirvis = locītājs.analyze("cirvis");
		assertTrue(cirvis.isRecognized());		
		assertEquals("ncmsn2", cirvis.wordforms.get(0).getTag());
	}
	
	@Test
	public void pīrādziņi() {
		//2008-09-06 atrasts gļuks, ka "pīrādziņi" analīzē pamatforma bija "pīrāgš"
		//2012-02-10 - vairs nav aktuāls 'pīrāgs', jābūt 'pīrādziņš'
		locītājs.enableDiminutive = true;
		Word pīrādziņi = locītājs.analyze("pīrādziņi");
		assertTrue(pīrādziņi.isRecognized());		
		assertEquals("pīrādziņš", pīrādziņi.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals("pīrāgs", pīrādziņi.wordforms.get(0).getValue(AttributeNames.i_SourceLemma));
	}

	@Test
	public void meitenīte() {
		//2008-09-06 atrasts gļuks, ka "meitenīte" analīzē ir 2 varianti - 
		// gan tīri no celma 'meitenīte', gan arī ar deminutīvu no 'meitene'
		Word meitenīte = locītājs.analyze("meitenīte");
		assertTrue(meitenīte.isRecognized());		
		assertEquals(1, meitenīte.wordformsCount());
	}	
	
	@Test
	public void otrajās() {
		//2008-09-07 atrasts gļuks, ka "otrajās" analīzē kā pamatforma ir "otrais" nevis "otrā"
		Word otrajās = locītājs.analyze("otrajās");
		assertTrue(otrajās.isRecognized());		
		assertEquals("otrā", otrajās.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}	

	@Test
	public void simtiem() {
		//2008-09-07 atrasts gļuks, ka "simtiem" analīzē kā pamatforma ir "simti" nevis "simts"
		Word simtiem = locītājs.analyze("simtiem");
		assertTrue(simtiem.isRecognized());		
		assertEquals("simts", simtiem.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void ēdīs() {
		//2008-09-08 atrasts gļuks, ka pie "ēdīs" analīzes uzkaras
		Word ēdīs = locītājs.analyze("ēdīs");
		assertTrue(ēdīs.isRecognized());		
	}
	
	@Test
	public void ceļu() {
		//2008-09-12 atrasts gļuks, ka "ceļu" analīzē ir tikai lietvārda varianti, bet nav darbības vārda forma
		Word ceļu = locītājs.analyze("ceļu");
		assertTrue(ceļu.isRecognized());
		
		AttributeValues verbs = new AttributeValues();
		verbs.addAttribute("Vārdšķira", "Darbības vārds");
		
		ceļu.filterByAttributes(verbs);
		assertTrue(ceļu.isRecognized());
	}
	
	@Test
	public void sniga() {
		//2008-09-11 atrasts gļuks, ka intransitīvajiem verbiem īpašībā raksta 'netransitīvs'.
		//likvidēta šī īpašība
		Word sniga = locītājs.analyze("sniga");
		assertTrue(sniga.isRecognized());		
		assertEquals(null, sniga.wordforms.get(0).getValue("Verbu grupa no vecā projekta"));
	}
	
	@Test
	public void bieži() {
		//2008-09-24 atrasts gļuks, ka "bieži" analīzē pamatforma bija "biež"
		Word bieži = locītājs.analyze("bieži");
		assertTrue(bieži.isRecognized());
		
		boolean irPareizā = false;
		for (Wordform vārdforma : bieži.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("bieži")) 
				irPareizā = true;			
		}
		
		assertEquals(true, irPareizā);
	}

	@Test
	public void zaļāk() {
		//2008-09-15 atrasts gļuks, ka apstākļvārdiem pārākā/vispārākā ir sajaukta vietām
		Word zaļāk = locītājs.analyze("zaļāk");
		assertTrue(zaļāk.isRecognized());		
		assertEquals("Pārākā", zaļāk.wordforms.get(0).getValue("Pakāpe"));

		Word viszaļāk = locītājs.analyze("viszaļāk");
		assertTrue(viszaļāk.isRecognized());		
		assertEquals("Vispārākā", viszaļāk.wordforms.get(0).getValue("Pakāpe"));	
	}
	
	@Test
	public void ātrākVisātrāk() {
		//Ticket #6 - nepareizi analizē pārāko/vispārāko pakāpi
		Word ātrāks = locītājs.analyze("ātrāks");
		assertTrue(ātrāks.isRecognized());
		assertEquals("Pārākā", ātrāks.wordforms.get(0).getValue("Pakāpe"));

		Word visātrākais = locītājs.analyze("visātrākais");
		assertTrue(visātrākais.isRecognized());
		assertEquals("Vispārākā", visātrākais.wordforms.get(0).getValue("Pakāpe"));				
	}
	
	@Test
	public void pieveicis() {
		locītājs.enablePrefixes = true;
		Word pieveicis = locītājs.analyze("pieveicis");
		assertTrue(pieveicis.isRecognized());
		assertEquals(AttributeNames.v_Prefix, pieveicis.wordforms.get(0).getValue(AttributeNames.i_Guess));
		assertEquals("vmnpdmsnasn", pieveicis.wordforms.get(0).getTag());
	}

	@Test
	public void paņēmis() {
		Word paņēmis = locītājs.analyze("paņēmis");
		assertTrue(paņēmis.isRecognized());		
		assertEquals("vmnpdmsnasn", paņēmis.wordforms.get(0).getTag());
	}
	
	@Test
	public void numuri(){
		// integritāte - vai nav dubulti numuri
		HashMap <Integer, Paradigm> vārdgrupuNr = new HashMap <Integer, Paradigm>();
		HashMap <Integer, Lexeme> leksēmuNr = new HashMap <Integer, Lexeme>();
		HashMap <Integer, Ending> galotņuNr = new HashMap <Integer, Ending>();
		
		for (Paradigm vārdgrupa : locītājs.paradigms) {
			if (vārdgrupuNr.get(vārdgrupa.getID()) != null) 
				fail("Atkārtojas vārdgrupas nr " + vārdgrupa.getID());
			vārdgrupuNr.put(vārdgrupa.getID(), vārdgrupa);
			
			for (Lexeme leksēma : vārdgrupa.lexemes) {
				if (leksēmuNr.get(leksēma.getID()) != null) 
					fail(String.format("Atkārtojas leksēmas nr %d : '%s' un '%s'", leksēma.getID(), leksēma.getStem(0), leksēmuNr.get(leksēma.getID()).getStem(0)));
				leksēmuNr.put(leksēma.getID(), leksēma);				
			}

			for (Ending ending : vārdgrupa.endings) {
				if (galotņuNr.get(ending.getID()) != null) 
					fail("Atkārtojas galotnes nr " + ending.getID());
				galotņuNr.put(ending.getID(), ending);				
			}
		}
	}
	
	@Test
	public void crap() {
		Word crap = locītājs.analyze("crap");
		assertFalse(crap.isRecognized());		
		locītājs.enableGuessing = true;
		locītājs.enableAllGuesses = true;
		locītājs.guessInflexibleNouns = true;
		crap = locītājs.analyze("crap");
		assertTrue(crap.isRecognized());
		assertEquals(AttributeNames.v_Ending, crap.wordforms.get(0).getValue(AttributeNames.i_Guess));
	}
	
	@Test
	public void ātrums() {
		long sākums = System.currentTimeMillis();
		
		locītājs.enableVocative = true;
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = false;
		locītājs.enableGuessing = false;
		locītājs.enableAllGuesses = true;
		locītājs.meklētsalikteņus = false; 
		
		int skaits = 0;
		for (int i = 1; i<100; i++) {
			locītājs.analyze("cirvis");
			locītājs.analyze("roku");
			locītājs.analyze("nepadomājot");
			locītājs.analyze("Kirils");
			locītājs.analyze("parakt");
			locītājs.analyze("bundziņas");
			locītājs.analyze("pokemonizēt");
			locītājs.analyze("xyzzyt");
			locītājs.analyze("žvirblis");
			locītājs.analyze("Murgainšteineniem");
			skaits += 10;
		}
		
		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		System.out.printf("Pagāja %d ms\n%d pieprasījumi sekundē", starpība, skaits*1000/starpība);
	}
	
	//TODO - dubulto leksēmu tests jāuztaisa
	@Test
	public void dubultLeksēmas() throws UnsupportedEncodingException {
		PrintWriter izeja = null;
		izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
		
		for (Paradigm vārdgrupa : locītājs.paradigms) {
			for (ArrayList<Lexeme> leksēmas : vārdgrupa.getLexemesByStem().get(0).values()) {
				for (int i = 0; i < leksēmas.size(); i++) {
					for (int j = i+1; j < leksēmas.size(); j++) {
						Lexeme l1 = leksēmas.get(i);
						Lexeme l2 = leksēmas.get(j);
						
						boolean sakrīt = true;
						for (int s = 0; s < vārdgrupa.getStems(); s++) 
							if (!l1.getStem(s).equals(l2.getStem(s))) sakrīt = false;
						
						for (Entry<String,String> pāris : l1.entrySet()) {
							if (pāris.getKey().equals("Leksēmas nr")) continue;
							String otraVērtība = l1.getValue(pāris.getKey());
							if (!pāris.getValue().equals(otraVērtība))
								sakrīt = false;
						}
						
						for (Entry<String,String> pāris : l2.entrySet()) {
							if (pāris.getKey().equals("Leksēmas nr")) continue;
							String otraVērtība = l1.getValue(pāris.getKey()); 
							if (!pāris.getValue().equals(otraVērtība))
								sakrīt = false;
						}		
						
						/*
						izeja.printf("Salīdzinam leksēmas %d un %d - %s\n", l1.getNr(), l2.getNr(),
								(sakrīt) ? "sakrīt!" : "nesakrīt!");
						l1.aprakstīt(izeja);
						
						l2.aprakstīt(izeja);
						*/
						
						if (sakrīt) {
							//fail(String.format("Sakrīt leksēmas %d un %d!", l1.getNr(), l2.getNr()));
							izeja.printf("leksēma = analizators.leksikons.leksēmaPēcID(%d); //%d\n" +
									"leksēma.getVārdgrupa().izņemtLeksēmu(leksēma);\n", l2.getID(), l1.getID());
						}
					}
				}
			}
		}
		izeja.flush();
	}
	
	@Test
	public void ticket9() {
		// Ticket #9 - vienskaitlinieki, daudzskaitlinieki, ģenitīveņi
		
		Word cukurs = locītājs.analyze("cukurs");
		assertTrue(cukurs.isRecognized());		
		assertEquals("cukurs", cukurs.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		Word cukuri = locītājs.analyze("cukuriem");
		assertFalse(cukuri.isRecognized());
		
		Word bikses = locītājs.analyze("bikses");
		assertTrue(bikses.isRecognized());		
		assertEquals("bikses", bikses.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		Word bikse = locītājs.analyze("bikse");
		assertFalse(bikse.isRecognized());
		
		Word augstpapēžu = locītājs.analyze("augstpapēžu");
		assertTrue(augstpapēžu.isRecognized());		
		assertEquals("augstpapēžu", augstpapēžu.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		Word augstpapēdis = locītājs.analyze("augstpapēdis");
		assertFalse(augstpapēdis.isRecognized());
	}
	
	@Test
	public void ticket29() {
		// Ticket #29 - noliegtie vietniekvārdi
		// 2012. 9. janvaaris - Gunta saka ka nevajag vinjus par noliegtajiem saukt
		Word neviens = locītājs.analyze("neviens");
		assertTrue(neviens.isRecognized());		
		//assertEquals("p_0msny0", neviens.wordforms.get(0).getValue(AttributeNames.i_Tag));
	}

	@Test
	public void ticket37() {
		// Ticket #37 - 'panest' taču nav noliegts
		locītājs.enablePrefixes = true;
		Word panest = locītājs.analyze("panest");
		assertTrue(panest.isRecognized());		
		assertEquals("vmnn0t1000n", panest.wordforms.get(0).getTag());
	}

	
	@Test
	public void ticket16() {
		// Ticket #16 - 'trūkst' kļūdaini analizējās kā arī 2. personas forma
		Word trūkst = locītājs.analyze("trūkst");
		assertTrue(trūkst.isRecognized());
		for (Wordform wordform : trūkst.wordforms)
			assertFalse(wordform.isMatchingStrong(AttributeNames.i_Person, "2"));		
	}	
	
	@Test
	public void ticket65() {
		// Ticket #65 - neuzrāda noliegumu un neuzrāda kārtu atgriezeniskajiem verbiem 
		Word dodas = locītājs.analyze("dodas");
		assertTrue(dodas.isRecognized());
		assertEquals(AttributeNames.v_Active, dodas.wordforms.get(0).getValue(AttributeNames.i_Voice));		
		assertEquals("vmyipti30an", dodas.wordforms.get(0).getTag());
	}

	
	@Test
	public void ticket76() {
		// Ticket #76 - skaitļa vārdiem neaiziet uz marķējumu skaitļa vārda kārta  
		Word simt = locītājs.analyze("simt");
		assertTrue(simt.isRecognized());
		assertEquals(AttributeNames.v_Hundreds, simt.wordforms.get(0).getValue(AttributeNames.i_Order));		
		assertEquals("mcs_p0s", simt.wordforms.get(0).getTag());
	}

	@Test
	public void ticket84() {
		// Ticket #84 - gļuks ar 1. konjugācijas darbības vārdu divdabjiem -is -usi un atgriezeniskajiem -ies -usies  
		Word griezis = locītājs.analyze("griezis");
		assertTrue(griezis.isRecognized());
		
		boolean atrasts = false;
		for (Wordform wordform : griezis.wordforms)
			if (wordform.isMatchingStrong(AttributeNames.i_Izteiksme, AttributeNames.v_Participle)) atrasts = true;		
		assertTrue(atrasts);

		Word griezies = locītājs.analyze("griezies");
		assertTrue(griezies.isRecognized());
		
		atrasts = false;
		for (Wordform wordform : griezis.wordforms)
			if (wordform.isMatchingStrong(AttributeNames.i_Izteiksme, AttributeNames.v_Participle)) atrasts = true;		
		assertTrue(atrasts);				
	}

	@Test
	public void tuStum() {
		// 2011-06-09 Laumas reportēts, ka neatpazīst "stumt" formu "stum"  
		Word stum = locītājs.analyze("stum");
		assertTrue(stum.isRecognized());
		
		assertEquals("2", stum.wordforms.get(0).getValue(AttributeNames.i_Person));		
		assertEquals(AttributeNames.v_Tagadne, stum.wordforms.get(0).getValue(AttributeNames.i_Laiks));				
	}
	
	@Test
	public void man() {
		// 2011-12-29 "man" pamatformu uzdod "man", vajag "es"
		Word man = locītājs.analyze("man");
		assertTrue(man.isRecognized());
		assertEquals("es", man.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}	
	@Test
	public void vairāki() {
		// 2011-12-29 "vairāki" analizē kā skaitļa vārdu, vajag kā vietniekvārdu
		// vietniekv., nenoteiktais, nav personas, vīr.dz., daudzsk., nominatīvs, nav noliegtais
		Word vairāki = locītājs.analyze("vairāki");
		assertTrue(vairāki.isRecognized());
		
		assertEquals(AttributeNames.v_Pronoun, vairāki.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
	}
	@Test
	public void daudzus() {
		// 2011-12-29 "daudzus" analizē kā skaitļa vārdu, vajag kā vietniekvārdu
		// vietniekv., nenoteiktais, nav personas, vīr.dz., daudzsk., datīvs, nav noliegtais
		Word daudzus = locītājs.analyze("daudzus");
		assertTrue(daudzus.isRecognized());
		
		assertEquals(AttributeNames.v_Pronoun, daudzus.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
	}
	@Test
	public void jāpasaka() {
		// 2011-12-29 "jāpasaka" neatpazīst
		//vmnd0t300an
		Word jāpasaka = locītājs.analyze("jāpasaka");
		assertTrue(jāpasaka.isRecognized());
	}
	
	@Test
	public void vajag() {
		// 2012-01-03 "vajag" neatpazīst		
		Word vajag = locītājs.analyze("vajag");
		assertTrue(vajag.isRecognized());
	}
	
	@Test
	public void Vilis() {
		// pie 'viņi' un 'viņiem' atrod vārdu ar pamatformu 'Vilis'
		Word viņi = locītājs.analyze("viņi");
		assertTrue(viņi.isRecognized());		
		assertEquals(2, viņi.wordformsCount());
	}
	
	@Test
	public void atgādinām() {
		// Ticket #226
		// 3.konjugācijas darbības vārdi ar -īt, -īties, -ināt, -ināties
		// locās ar garajiem burtiem izskaņās.
		Word atgādinām = locītājs.analyze("atgādinām");
		assertTrue(atgādinām.isRecognized());		
		assertEquals(2, atgādinām.wordformsCount());
		
		Word atgādināt = locītājs.analyze("atgādināt");
		assertTrue(atgādināt.isRecognized());		
		assertEquals(2, atgādināt.wordformsCount());
		
		Word atgādinat = locītājs.analyze("atgādinat");
		assertFalse(atgādinat.isRecognized());
		
		Word atgādinam = locītājs.analyze("atgādinam");
		assertFalse(atgādinam.isRecognized());
	}
	
	@Test
	public void bijušais() {
		// Ticket #255: Neatpazīst "bijušais" dažādos locījumos.
		Word bijušais = locītājs.analyze("bijušais");
		assertTrue(bijušais.isRecognized());
		
		Word bijusī = locītājs.analyze("bijusī");
		assertTrue(bijusī.isRecognized());	
		
		Word bijušajiem = locītājs.analyze("bijušajiem");
		assertTrue(bijušajiem.isRecognized());
	}
	
	@Test
	public void video() {
		// Ticket #245: nelokāmie lietvārdi vienmēr ir nominatīvā.
		Word video = locītājs.analyze("video");
		assertTrue(video.isRecognized());
		
		assertTrue(video.wordformsCount() == 1);
	}
	
	@Test
	public void neviens() {
		// Ticket #259: Neviens, nekas, nekāds ir nenoteiktais vietniekvārdi
		// ar noliegumu yes.
		Word neviens = locītājs.analyze("neviens");
		assertTrue(neviens.isRecognized());
		
		assertEquals(AttributeNames.v_Pronoun, neviens.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals(AttributeNames.v_Yes, neviens.wordforms.get(0).getValue(AttributeNames.i_Noliegums));
		assertEquals(AttributeNames.v_Nenoteiktie, neviens.wordforms.get(0).getValue(AttributeNames.i_VvTips));

		Word nekas = locītājs.analyze("nekas");
		assertTrue(nekas.isRecognized());
		
		assertEquals(AttributeNames.v_Pronoun, nekas.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals(AttributeNames.v_Yes, nekas.wordforms.get(0).getValue(AttributeNames.i_Noliegums));
		assertEquals(AttributeNames.v_Nenoteiktie, nekas.wordforms.get(0).getValue(AttributeNames.i_VvTips));

		Word nekāds = locītājs.analyze("nekāds");
		assertTrue(nekāds.isRecognized());
		
		int ind = 0;
		while (ind < nekāds.wordformsCount() && 
				!AttributeNames.v_Pronoun.equalsIgnoreCase(
					nekāds.wordforms.get(ind).getValue(AttributeNames.i_PartOfSpeech))) {
			ind++;
		}
		
		assertTrue(ind < nekāds.wordformsCount());
		//assertEquals(AttributeNames.v_Pronoun, nekāds.wordforms.get(ind).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals(AttributeNames.v_Yes, nekāds.wordforms.get(ind).getValue(AttributeNames.i_Noliegums));
		//assertEquals(AttributeNames.v_Nenoteiktie, nekāds.wordforms.get(ind).getValue(AttributeNames.i_VvTips));
	}
	
	@Test
	public void atnes() {
		// Lauras sūdzība - nesaprot 'atnes' pavēles formu
		Word atnes = locītājs.analyze("atnes");
		assertTrue(atnes.isRecognized());
	
		AttributeValues filtrs = new AttributeValues();
		filtrs.addAttribute("Izteiksme", "Pavēles");
		
		atnes.filterByAttributes(filtrs);
		assertTrue(atnes.isRecognized());
	}
	
	@Test
	public void jāatceras() {
		// Lauras sūdzība - neatpazīst 'jāatceras'
		Word jāatceras = locītājs.analyze("jāatceras");
		assertTrue(jāatceras.isRecognized());
	
		AttributeValues filtrs = new AttributeValues();
		filtrs.addAttribute("Izteiksme", "Vajadzības");
		
		jāatceras.filterByAttributes(filtrs);
		assertTrue(jāatceras.isRecognized());
	}
	
	@Test
	public void jāmāk() {
		// Lauras sūdzība - neatpazīst 'jāmāk'
		Word jāmāk = locītājs.analyze("jāmāk");
		assertTrue(jāmāk.isRecognized());
	
		AttributeValues filtrs = new AttributeValues();
		filtrs.addAttribute("Izteiksme", "Vajadzības");
		
		jāmāk.filterByAttributes(filtrs);
		assertTrue(jāmāk.isRecognized());
	}
	
	@Test
	public void vislabāk() {
		// 2012. 3.feb Gunta saka ka 'vislabāk' pamatforma ir 'labi' 
		Word vislabāk = locītājs.analyze("vislabāk");
		
		assertTrue(vislabāk.isRecognized());
		boolean irPareizā = false;
		for (Wordform vārdforma : vislabāk.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("labi")) 
				irPareizā = true;			
		}
		
		assertEquals(true, irPareizā);
	}
	
	@Test
	public void vairāk() {
		// 2012. 3.feb Gunta saka ka 'vairāk' pamatforma ir 'daudz' 
		Word vairāk = locītājs.analyze("vairāk");
				
		assertTrue(vairāk.isRecognized());
		boolean irPareizā = false;
		for (Wordform vārdforma : vairāk.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("daudz")) 
				irPareizā = true;			
		}
		
		assertEquals(true, irPareizā);
	}
	
	@Test
	public void deminutive() {
		// 2012. 10.feb Vienojāmies ar valodniecēm ka deminutīviem lemmas arī ir deminutīvā
		
		locītājs.enableDiminutive = true;
		Word cirvītis = locītājs.analyze("cirvītis");
		Word pļava = locītājs.analyze("pļaviņa");
		
		assertTrue(cirvītis.isRecognized());
		assertTrue(pļava.isRecognized());
		
		boolean irPareizā = false;
		for (Wordform vārdforma : cirvītis.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("cirvītis")) {
				irPareizā = true;			
				assertEquals(AttributeNames.v_Deminutive, vārdforma.getValue(AttributeNames.i_Guess));				
			}
		}
		assertEquals(true, irPareizā);		
		
		irPareizā = false;
		for (Wordform vārdforma : pļava.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("pļaviņa")) 
				irPareizā = true;			
		}
		assertEquals(true, irPareizā);
	}
	
	@Test
	public void riebties() {
		// 2012-03-14 "riebties" neatpazīstot
		
		locītājs.enableGuessing = true;
		Word riebties = locītājs.analyze("riebties");
		assertTrue(riebties.isRecognized());
		assertEquals("riebties", riebties.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}	
	
	@Test
	public void sa() {
		// 2012-03-16 esot crash
		
		locītājs.enablePrefixes = true;
		Word sa = locītājs.analyze("");
		assertFalse(sa.isRecognized());
	}
	
	@Test
	public void noliegumu_lemma() {
		locītājs.enablePrefixes = true;
		// Noliegumu atvasinājumiem lai ir oriģinālā pamatforma atvasināta  
		Word nenest = locītājs.analyze("nenesāt");
		assertTrue(nenest.isRecognized());
		assertEquals("nenest", nenest.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals("nest", nenest.wordforms.get(0).getValue(AttributeNames.i_SourceLemma));		
	}
	
	@Test
	public void kususi() {
		// 2012-03-27 - pie priedēkļu atpazīšanas vārdiem noplēsa priedēkļus
		
		locītājs.enablePrefixes = true;
		List<Word> tokens = Splitting.tokenize(locītājs, "Vai esi piekususi?");
		Word piekususi = tokens.get(2);
		assertTrue(piekususi.isRecognized());
		assertEquals("piekususi", piekususi.getToken());
		//if (! piekususi.getCorrectWordform().getToken().equalsIgnoreCase("piekususi")) {
		//	PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
		//	piekususi.getCorrectWordform().describe(izeja);
		//}
		assertEquals("piekususi", piekususi.wordforms.get(0).getToken());
	}
	
	@Test
	public void tokenizesafety() {
		// 2012-03-27 atrasts bug ka tokenizators reizēm izmainīja vārdus
		String text = "Vīrs ar cirvi piekusa joklmnasdasd1239612321 *(&(*^)@!!@# /t/txxx/n\t\nasdas cimdiņi cimdiņzeķītes";
		LinkedList<Word> tokens = Splitting.tokenize(locītājs, text);
		String wordtokens = "";
		for (Word w : tokens) {
			wordtokens += w.getToken();
			for (Wordform wf : w.wordforms) {
				assertEquals( w.getToken(), wf.getToken());
			}
		}
		assertEquals( text.replaceAll(" ","").replaceAll("\t","").replaceAll("\n",""), wordtokens);
		
		locītājs.enableVocative = true;
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = true;
		locītājs.enableGuessing = true;
		locītājs.enableAllGuesses = true;
		locītājs.meklētsalikteņus = true; 
		
		tokens = Splitting.tokenize(locītājs, text);
		wordtokens = "";
		for (Word w : tokens) {
			wordtokens += w.getToken();
			for (Wordform wf : w.wordforms) {
				assertEquals( w.getToken(), wf.getToken());
			}
		}
		assertEquals( text.replaceAll(" ","").replaceAll("\t","").replaceAll("\n",""), wordtokens);
	}

	
	@Test
	public void saīsinājumi() {
		Word uc = locītājs.analyze("u.c.");
		assertTrue(uc.isRecognized());
		assertEquals("y", uc.wordforms.get(0).getTag());
	}
	
	@Test
	public void nopūzdamās() {
		// 2012-03-28 - nesaprot 'nopūzdamās', saprot 'nopūsdamās'
		Word nopūzdamās = locītājs.analyze("pūzdamās");
		assertTrue(nopūzdamās.isRecognized());
		
		Word nopūsdamās = locītājs.analyze("pūsdamās");
		assertFalse(nopūsdamās.isRecognized());
		
		Word grūzdams = locītājs.analyze("grūzdams");
		assertTrue(grūzdams.isRecognized());
		
		Word mezdams = locītājs.analyze("mezdams");
		assertTrue(mezdams.isRecognized());
		
		Word elsdams = locītājs.analyze("elsdams");
		assertTrue(elsdams.isRecognized());
		
		Word milzdams = locītājs.analyze("milzdams");
		assertTrue(milzdams.isRecognized());
		
		Word nesdams = locītājs.analyze("nesdams");
		assertTrue(nesdams.isRecognized());
	}
	
	@Test
	public void ts() {
		Word nopūsts = locītājs.analyze("pūsts");
		assertTrue(nopūsts.isRecognized());
		
		Word grūsts = locītājs.analyze("grūsts");
		assertTrue(grūsts.isRecognized());
		
		Word mests = locītājs.analyze("mests");
		assertTrue(mests.isRecognized());
		
		Word elsts = locītājs.analyze("elsts");
		assertTrue(elsts.isRecognized());
		
		Word mēzts = locītājs.analyze("mēzts");
		assertTrue(mēzts.isRecognized());
		
		Word nests = locītājs.analyze("nests");
		assertTrue(nests.isRecognized());
	}
	
	@Test
	public void source_lemma() {
		// Atvasinājumiem lai ir oriģinālā vārda pamatforma redzama (pēc tās var meklēt vārdnīcas šķirkli)
		Word balta = locītājs.analyze("baltas");
		assertTrue(balta.isRecognized());
		assertEquals("balta", balta.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals("balts", balta.wordforms.get(0).getValue(AttributeNames.i_SourceLemma));
		
		locītājs.enablePrefixes = false;
		Word miršana = locītājs.analyze("miršana");
		assertTrue(miršana.isRecognized());
		assertEquals("miršana", miršana.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals("mirt", miršana.wordforms.get(0).getValue(AttributeNames.i_SourceLemma));
	}
	
	@Test
	public void residuals() {
		// Bezmorfoloģijas elementu klasifikācija
		Word slīpsvītra = locītājs.analyze("/");
		assertTrue(slīpsvītra.isRecognized());
		assertEquals("zx", slīpsvītra.wordforms.get(0).getTag());
		
		Word dr = locītājs.analyze("dr.");
		assertTrue(dr.isRecognized());
		assertEquals("y", dr.wordforms.get(0).getTag());
		
		Word plus = locītājs.analyze("+");
		assertTrue(plus.isRecognized());
		assertEquals("xx", plus.wordforms.get(0).getTag());
	}
	
	@Test
	public void numbers() {
		// Ciparu atpazīšana
		Word num = locītājs.analyze("123456");
		assertTrue(num.isRecognized());
		assertEquals("xn", num.wordforms.get(0).getTag());
		assertEquals("123456", num.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		Word ord = locītājs.analyze("15.");
		assertTrue(ord.isRecognized());
		assertEquals("xo", ord.wordforms.get(0).getTag());
	}
	
	@Test
	public void pieci() {
		Word pieci = locītājs.analyze("pieci");
		assertTrue(pieci.isRecognized());
		assertEquals("pieci", pieci.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		pieci = locītājs.analyze("5");
		assertTrue(pieci.isRecognized());
		assertEquals("5", pieci.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void iejāt() {
		// 2012-03-30 iejāt neatpazina dēļ buga
		locītājs.enablePrefixes = true;
		
		Word iejāt = locītājs.analyze("iejāt");
		assertTrue(iejāt.isRecognized());
		assertEquals("iejāt", iejāt.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals("jāt", iejāt.wordforms.get(0).getValue(AttributeNames.i_SourceLemma));
	}
	
	@Test
	public void labākais() {
		//Vispārāko pakāpju alternatīvas... Bet īsti nerullē, labākais ir vārdam 'labāks' noteiktā forma, nevis vispārākā pakāpe
		Word ātrāks = locītājs.analyze("labāks");
		assertTrue(ātrāks.isRecognized());
		assertEquals("Pārākā", ātrāks.wordforms.get(0).getValue("Pakāpe"));

		Word visātrākais = locītājs.analyze("labākais");
		assertTrue(visātrākais.isRecognized());
		//assertEquals("Vispārākā", visātrākais.wordforms.get(0).getValue("Pakāpe"));				
	}
	
	@Test
	public void reziduāļi() {
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = true;
		locītājs.enableGuessing = true;
		locītājs.enableAllGuesses = true;
		locītājs.meklētsalikteņus = true;
		
		//Atsevišķus burtus nevajadzētu minēt kā reālus vārdus
		
		Word ž = locītājs.analyze("ž");
		assertFalse(ž.isRecognized());
		
		Word m = locītājs.analyze("M.");
		assertTrue(m.isRecognized());
		assertEquals(AttributeNames.v_Abbreviation, m.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
	}
	
	@Test
	public void atstarpes() {
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = true;
		locītājs.enableGuessing = true;
		locītājs.enableAllGuesses = true;
		locītājs.meklētsalikteņus = true;
		
		//Atsevišķus burtus nevajadzētu minēt kā reālus vārdus
		
		Word ne = locītājs.analyze("ne ");
		assertTrue(ne.isRecognized());	
		assertEquals("ne", ne.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void gunta2012mai() {
		// Guntas reportētie neatpazītie vārdi
		
		Word atguvies = locītājs.analyze("atguvies");
		assertTrue(atguvies.isRecognized());

		Word sizdams = locītājs.analyze("sizdams");
		assertTrue(sizdams.isRecognized());	
		
		Word sēzdamies = locītājs.analyze("sēzdamies");
		assertTrue(sēzdamies.isRecognized());	

		Word sarūdzis = locītājs.analyze("sarūdzis");
		assertTrue(sarūdzis.isRecognized());	
		
		Word irties = locītājs.analyze("irties");
		assertTrue(irties.isRecognized());
		
		Word tekalēt = locītājs.analyze("tekalēt");
		assertTrue(tekalēt.isRecognized());
		
		Word kļūt = locītājs.analyze("kļūt");
		assertTrue(kļūt.isRecognized());
		
		Word proti = locītājs.analyze("proti");
		assertTrue(proti.isRecognized());
	}
	
	@Test
	public void lūzīs() {
		Word lūzīs = locītājs.analyze("lūzīs");
		assertTrue(lūzīs.isRecognized());
		assertEquals("lūzt", lūzīs.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void ģenerēšana() {
		List<Wordform> formas = locītājs.generateInflections("Valdis");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "valda");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "valdim");
			
		formas = locītājs.generateInflections("Raitis");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "raita");

		formas = locītājs.generateInflections("cerēt");
		// TODO - salikt verbiem testpiemērus			
	}
	
	@Test
	public void ģenerēšanaNezināmiem() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;

		assertTrue("Valdis".matches("\\p{Lu}.*"));
		assertTrue("Ādolfs".matches("\\p{Lu}.*"));
		assertFalse("valdis".matches("\\p{Lu}.*"));
		assertFalse("ādolfs".matches("\\p{Lu}.*"));
				
		Word zolā = locītājs.analyze("Zolā");
		assertTrue(zolā.isRecognized());
		assertEquals(AttributeNames.v_Noun, zolā.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		
		ArrayList<Wordform> formas = locītājs.generateInflections("Zolā");
		assertTrue(formas.size() > 0);
	}
	
	@Test
	public void vešana() {
		Word vešana = locītājs.analyze("vešana");
		assertTrue(vešana.isRecognized());
		assertEquals("vest", vešana.wordforms.get(0).getValue(AttributeNames.i_SourceLemma));

		Word vesšana = locītājs.analyze("vesšana");
		assertFalse(vesšana.isRecognized());

		Word mēzšana = locītājs.analyze("mēzšana");
		assertFalse(mēzšana.isRecognized());
	}
	
	@Test
	public void nelokaamie() {
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = true;
		locītājs.enableGuessing = true;
		locītājs.enableAllGuesses = true;
		locītājs.meklētsalikteņus = true;
		locītājs.guessInflexibleNouns = true;

		Word vārds = locītājs.analyze("TrrT");
		assertTrue(vārds.isRecognized());
		assertEquals("trrt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("GAIZINAISI-Ā3");
		assertTrue(vārds.isRecognized());
		assertEquals("gaizinaisi-ā3", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals(AttributeNames.v_Residual, vārds.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));

		vārds = locītājs.analyze("0.40");
		assertTrue(vārds.isRecognized());
		assertEquals("0.40", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals(AttributeNames.v_Residual, vārds.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals(AttributeNames.v_Number, vārds.wordforms.get(0).getValue(AttributeNames.i_ResidualType));
		
		vārds = locītājs.analyze("6/7");
		assertTrue(vārds.isRecognized());
		assertEquals("6/7", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals(AttributeNames.v_Residual, vārds.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals(AttributeNames.v_Number, vārds.wordforms.get(0).getValue(AttributeNames.i_ResidualType));
		
		vārds = locītājs.analyze("....");
		assertTrue(vārds.isRecognized());
		for (Wordform wf : vārds.wordforms) {
			assertEquals("....", wf.getValue(AttributeNames.i_Lemma));
		}
	}
	
	@Test
	public void personvārdi_Varis() {
		// 2012.06.08 sūtītie komentāri par locīšanas defektiem.
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Valdis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "valda");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "valdim");
		
		formas = locītājs.generateInflections("Čaikovskis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "čaikovska");
		
		formas = locītājs.generateInflections("Cēsis", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "cēsu");
		
		formas = locītājs.generateInflections("Raitis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "raita");

		formas = locītājs.generateInflections("Auziņš", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "auziņu");

		formas = locītājs.generateInflections("Ivis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "ivja");
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "ivju");
		
		formas = locītājs.generateInflections("Eglīts", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "eglīša");
		
		formas = locītājs.generateInflections("Švirkste", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, AttributeNames.v_Feminine, "švirkstes");
		
		formas = locītājs.generateInflections("Taļikova", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, AttributeNames.v_Feminine, "taļikovas");
		
		formas = locītājs.generateInflections("Bērziņš", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Nominative, AttributeNames.v_Masculine, "bērziņš");
		
		formas = locītājs.generateInflections("Dīcis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "dīcim");

		formas = locītājs.generateInflections("Asna", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "asnai");		
		
		formas = locītājs.generateInflections("Lielais", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "lielajam");
		
		formas = locītājs.generateInflections("Mazā", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "mazajai");

		formas = locītājs.generateInflections("Zaļais", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "zaļajam");		
		
		formas = locītājs.generateInflections("Santis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, AttributeNames.v_Masculine, "santa");
	}
	
	@Test
	public void no_iepirkšanās() {
		Word vārds = locītājs.analyze("no iepirkšanās");
		assertFalse(vārds.isRecognized());
		
		vārds = locītājs.analyze("uz kino");
		assertFalse(vārds.isRecognized());
		
		vārds = locītājs.analyze("nocirvis");
		assertFalse(vārds.isRecognized());
	}
	
	@Test
	public void cache() {
		locītājs.setCacheSize(1000);
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		Word vārds = locītājs.analyze("sacelt");
		assertTrue(vārds.isRecognized());
		assertEquals("sacelt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("celt");
		assertTrue(vārds.isRecognized());
		assertEquals("celt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void mazajai() {
		Word mazajai = locītājs.analyze("mazajai");
		assertTrue(mazajai.isRecognized());		
		assertEquals("maza", mazajai.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void personvārdi_Varis2() {
		// 2012.07.05 sūtītie komentāri par vokatīvu defektiem.
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Pauls", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "paul");
		
		formas = locītājs.generateInflections("Laura", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "laura");
		
		formas = locītājs.generateInflections("Lauriņa", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "lauriņ");

		formas = locītājs.generateInflections("Made", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "made");		

		formas = locītājs.generateInflections("Kristīnīte", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "kristīnīt"); 

		formas = locītājs.generateInflections("Margrieta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "margrieta"); // principā der gan viens, gan otrs, ģenerē arī abus, bet 'margrieta' ir pirmais
	}
	
	@Test
	public void leksikoni() {
		Word pokemons = locītājs.analyze("Bisjakovs");
		assertFalse(pokemons.isRecognized());		
	}
	
	@Test
	public void daudzskaitlinieki() {
		// analyzeLemma nestrādā
		Word augstpapēžu = locītājs.analyzeLemma("augstpapēžu");
		assertTrue(augstpapēžu.isRecognized());
	}

	@Test
	public void personvārdi_Varis3() {
		// 2012.07.14 sūtītie komentāri par vokatīvu defektiem.
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Auziņš", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "auziņ");
		
		formas = locītājs.generateInflections("Miervaldis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "miervalža");
	}
	
	
	@Test
	public void Laura10Aug()
	{
		Word vārds = locītājs.analyze("vienai");
		assertTrue(vārds.isRecognized());
		assertEquals("viena", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("pirmajai");
		assertTrue(vārds.isRecognized());
		assertEquals("pirmā", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("trešās");
		assertTrue(vārds.isRecognized());
		assertEquals("trešā", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("piecsimt");
		assertTrue(vārds.isRecognized());
		assertEquals("mcc0p0s", vārds.wordforms.get(0).getTag());
	}

	@Test
	public void personvārdi_Varis4() {
		// 2012.08.13 P33 vokatīvu shēma
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Jēkabs");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "jēkab");
		
		formas = locītājs.generateInflections("Mārtiņš");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "mārtiņ");
		
		formas = locītājs.generateInflections("Mikus");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "miku");
		
		formas = locītājs.generateInflections("Ingus");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "ingu");
		
		formas = locītājs.generateInflections("Kalns");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "kaln");
		
		formas = locītājs.generateInflections("Liepiņš");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "liepiņ");
		
		formas = locītājs.generateInflections("Zaķis");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "zaķi");
		
		formas = locītājs.generateInflections("Ledus");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "ledu");
		
		formas = locītājs.generateInflections("Platais");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "platais");
		
		formas = locītājs.generateInflections("Lielais");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "lielais");
		
		formas = locītājs.generateInflections("Biezais");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "biezais");
		
		formas = locītājs.generateInflections("Silvija");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "silvij");
		
		formas = locītājs.generateInflections("Kadrije"); //hipotētiski
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "kadrij");
		
		formas = locītājs.generateInflections("Karlīne");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "karlīn");
		
		formas = locītājs.generateInflections("Vilhelmīne");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "vilhelmīn");
		
		formas = locītājs.generateInflections("Skaidrīte");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "skaidrīt");
		
		formas = locītājs.generateInflections("Juliāna");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "juliān");
		
		formas = locītājs.generateInflections("Eglīte");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "eglīt");
		
		formas = locītājs.generateInflections("Lapsiņa");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "lapsiņ");
		
		formas = locītājs.generateInflections("Pilsētniece");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "pilsētniec");
		
		formas = locītājs.generateInflections("Salnāja");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "salnāj");
		
		formas = locītājs.generateInflections("Garkāje");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "garkāje");
		
		formas = locītājs.generateInflections("Zeidmane");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "zeidmane");
		
		formas = locītājs.generateInflections("Kreice");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "kreice");
		
		formas = locītājs.generateInflections("Kreija");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "kreija");
		
		formas = locītājs.generateInflections("Kreitenberga");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "kreitenberga");
		
		//Nav norealizēts: Par salikteņiem Ja salikteņa 2.  daļa atsevišķi kvalificējas īsajai formai, tad arī saliktenis kvalificējas īsajai formai. 
	}

	@Test
	public void personvārdi_Varis5() {
		// 2012.08.13 Vara komentāri
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Arvydas", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "arvydas");
		
		formas = locītājs.generateInflections("Rīta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "rīta");
		
		formas = locītājs.generateInflections("rīta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "rīta");
	}
	
	@Test
	public void laura_Aug13() {
		// 2012.08.13 Lauras samarķētā atšķirību analīze 
		List<Wordform> formas = locītājs.generateInflections("Fredis");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "freda");
		
		formas = locītājs.generateInflections("Freda");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "fredas");
		
		
		Word freda = locītājs.analyze("Freda");		
		assertTrue(freda.isRecognized());
		
		boolean irPareizā = false;
		for (Wordform vārdforma : freda.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("Fredis")) {
				irPareizā = true;			
			}
		}
		assertEquals(true, irPareizā);
		
		Word sia = locītājs.analyze("SIA");		
		assertTrue(sia.isRecognized());
		
		Word numur = locītājs.analyze("numur");		
		assertTrue(numur.isRecognized());
		irPareizā = false;
		for (Wordform vārdforma : numur.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("numurs") && vārdforma.isMatchingStrong(AttributeNames.i_Case, AttributeNames.v_Nominative)) {
				irPareizā = true;			
			}
		}
		assertEquals(true, irPareizā);
	}
	
	@Test
	public void GuntaAug22() {
	// 2012.08.22 Gunta saka, ka "pazūd" atpazīst kā 2. personas vārdu; tas ir fail 7. mijā
		Word vārds = locītājs.analyze("ēd");
		assertTrue(vārds.isRecognized());
		boolean irPareizā = false;
		for (Wordform vārdforma : vārds.wordforms) {
			if (vārdforma.isMatchingStrong(AttributeNames.i_Person, "2")) irPareizā = true;
		}
		assertTrue(irPareizā);
		
		vārds = locītājs.analyze("pazūd");
		assertTrue(vārds.isRecognized());
		for (Wordform vārdforma : vārds.wordforms) {
			assertFalse(vārdforma.isMatchingStrong(AttributeNames.i_Person, "2"));
		}
	}
	
	@Test
	public void LocīšanaSep4() {
	// 2012.09.04 konstatēts, ka lokot dažiem vārdiem nepareizi mijas strādā
		List<Wordform> formas = locītājs.generateInflections("iemācīties");

		AttributeValues testset = new AttributeValues();		
		testset.addAttribute(AttributeNames.i_EndingID, "1057");
		assertInflection(formas, testset, "iemācoties");
		testset.addAttribute(AttributeNames.i_EndingID, "1027");
		assertInflection(formas, testset, "jāiemācās");
		testset.addAttribute(AttributeNames.i_EndingID, "1210");
		assertInflection(formas, testset, "jāiemācoties");
				
		formas = locītājs.generateInflections("mācīt");
		testset.addAttribute(AttributeNames.i_EndingID, "472");
		assertInflection(formas, testset, "mācām");
		testset.addAttribute(AttributeNames.i_EndingID, "474");
		assertInflection(formas, testset, "māca");
		testset.addAttribute(AttributeNames.i_EndingID, "487");
		assertInflection(formas, testset, "jāmāca");
		testset.addAttribute(AttributeNames.i_EndingID, "1204");
		assertInflection(formas, testset, "jāmācot");
		
		formas = locītājs.generateInflections("mācēt");
		testset.addAttribute(AttributeNames.i_EndingID, "469");
		assertInflection(formas, testset, "māku");
		testset.addAttribute(AttributeNames.i_EndingID, "472");
		assertInflection(formas, testset, "mākam");		
		testset.addAttribute(AttributeNames.i_EndingID, "474");
		assertInflection(formas, testset, "māk");
		testset.addAttribute(AttributeNames.i_EndingID, "487");
		assertInflection(formas, testset, "jāmāk");
		testset.addAttribute(AttributeNames.i_EndingID, "1204");
		assertInflection(formas, testset, "jāmākot");

		formas = locītājs.generateInflections("gulēt");
		testset.addAttribute(AttributeNames.i_EndingID, "470");
		assertInflection(formas, testset, "guli");
		testset.addAttribute(AttributeNames.i_EndingID, "474");
		assertInflection(formas, testset, "guļ");
		testset.addAttribute(AttributeNames.i_EndingID, "494");
		assertInflection(formas, testset, "guliet");
		testset.addAttribute(AttributeNames.i_EndingID, "1204");
		assertInflection(formas, testset, "jāguļot");

		formas = locītājs.generateInflections("aizgulēties");
		testset.addAttribute(AttributeNames.i_EndingID, "1009");
		assertInflection(formas, testset, "aizguļos");
		
		formas = locītājs.generateInflections("vajadzēt");
		testset.addAttribute(AttributeNames.i_EndingID, "469");
		assertInflection(formas, testset, "vajagu");
		testset.addAttribute(AttributeNames.i_EndingID, "472");
		assertInflection(formas, testset, "vajagam");		
		testset.addAttribute(AttributeNames.i_EndingID, "474");
		assertInflection(formas, testset, "vajag");
		testset.addAttribute(AttributeNames.i_EndingID, "487");
		assertInflection(formas, testset, "jāvajag");
		testset.addAttribute(AttributeNames.i_EndingID, "1204");
		assertInflection(formas, testset, "jāvajagot");

		formas = locītājs.generateInflections("mocīt");
		testset.addAttribute(AttributeNames.i_EndingID, "493");
		assertInflection(formas, testset, "moki");		

		formas = locītājs.generateInflections("slodzīt");
		testset.addAttribute(AttributeNames.i_EndingID, "469");
		assertInflection(formas, testset, "slogu");
		
		formas = locītājs.generateInflections("mesties");
		testset.addAttribute(AttributeNames.i_EndingID, "1072");
		assertInflection(formas, testset, "mešanās");
		
		formas = locītājs.generateInflections("pūsties");
		testset.addAttribute(AttributeNames.i_EndingID, "1087");
		assertInflection(formas, testset, "pūties");

		Word vārds = locītājs.analyze("gulošs");
		assertTrue(vārds.isRecognized());		
		vārds = locītājs.analyze("guļošs");
		assertTrue(vārds.isRecognized());
	}
	
	
	@Test
	public void gunta_20120911() {
		//korpusā vārdi "ness" un "vess" ir marķēti kā verbu "nest" un "vest" formas

		Word vārds = locītājs.analyze("nest");
		assertTrue(vārds.isRecognized());

		vārds = locītājs.analyze("nesīs");
		assertTrue(vārds.isRecognized());
		
		vārds = locītājs.analyze("vest");
		assertTrue(vārds.isRecognized());
		
		vārds = locītājs.analyze("vedīs");
		assertTrue(vārds.isRecognized());
		
		vārds = locītājs.analyze("vess");
		assertFalse(vārds.isRecognized());
		
		vārds = locītājs.analyze("vesīs");
		//assertFalse(vārds.isRecognized()); // FIXME - tur palīdzētu mijām čekošana, vai uzminētais sakrīt ar izlocīto; vai arī post-processing check par 3o sakni 6. mijai.... 
		
		vārds = locītājs.analyze("ness");
		assertFalse(vārds.isRecognized());
	}
	
	@Test
	public void pazūdi() {
		// 2012.09.12 konstatēts ka mija pareizi neloka šo formu
		Word vārds = locītājs.analyze("pazūdi");		
		assertTrue(vārds.isRecognized());
		
		boolean irPareizā = false;
		for (Wordform vārdforma : vārds.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_EndingID).equals("790")) {
				irPareizā = true;			
			}
		}
		assertEquals(true, irPareizā);
		
		List<Wordform> formas = locītājs.generateInflections("pazust");

		AttributeValues testset = new AttributeValues();		
		testset.addAttribute(AttributeNames.i_EndingID, "790");
		assertInflection(formas, testset, "pazūdi");
		
		formas = locītājs.generateInflections("atrast");
		testset.addAttribute(AttributeNames.i_EndingID, "790");
		assertInflection(formas, testset, "atrodi");		
	}

	@Test
	public void vajadzības_minēšana() {
		locītājs.enablePrefixes = true;
		
		//Priedēkļu atvasināšana nestrādā, ja ir vajadzības izteiksme
		
		Word vārds = locītājs.analyze("rakt");
		assertTrue(vārds.isRecognized());	
		assertEquals("rakt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("aizrakt");
		assertTrue(vārds.isRecognized());	
		assertEquals("aizrakt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("jārok");
		assertTrue(vārds.isRecognized());	
		assertEquals("rakt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("jāaizrok");
		assertTrue(vārds.isRecognized());	
		assertEquals("aizrakt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void divdabju_pārākās_formas() {
		Word vārds = locītājs.analyze("izkusušais");
		assertTrue(vārds.isRecognized());	
		assertEquals("izkust", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		//vārds.wordforms.get(0).describe();
		
		vārds = locītājs.analyze("visizkusušākais");
		assertTrue(vārds.isRecognized());	
		assertEquals("izkust", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}

	@Test
	public void normunds20130128() {
		Word vārds = locītājs.analyze("māc");
		assertTrue(vārds.isRecognized());	
		assertEquals("mākt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
//		for (Wordform forma : vārds.wordforms) {
//			forma.describe();
//			System.out.println();
//		}
		assertEquals(3, vārds.wordforms.size());
		
		List<Wordform> formas = locītājs.generateInflections("pļaut");		
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
		testset.addAttribute(AttributeNames.i_Person, "3");
		testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
		assertInflection(formas, testset, "pļauj");

		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Pagaatne);
		assertInflection(formas, testset, "pļāva");
		
		formas = locītājs.generateInflections("kļaut");
		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
		assertInflection(formas, testset, "kļauj");
		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Pagaatne);
		assertInflection(formas, testset, "kļāva");
		
		formas = locītājs.generateInflections("iekļaut");
		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
		assertInflection(formas, testset, "iekļauj");
		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Pagaatne);
		assertInflection(formas, testset, "iekļāva");
	}
	
	@Test
	public void vienādās_nenoteiksmes() {
		Paradigm pirmā = locītājs.paradigmByID(15);
		Paradigm otrā = locītājs.paradigmByID(16);
		Paradigm trešā = locītājs.paradigmByID(17);
		LinkedList<Lexeme> leksēmas = new LinkedList<Lexeme>();
		leksēmas.addAll(pirmā.lexemes);
		leksēmas.addAll(otrā.lexemes);
		leksēmas.addAll(trešā.lexemes);
		for (Lexeme lex : leksēmas) {
			LinkedList<Lexeme> alternatīvas = new LinkedList<Lexeme>();
			ArrayList<Lexeme> xx = pirmā.getLexemesByStem().get(0).get(lex.getStem(0)); 
			if (xx != null) alternatīvas.addAll(xx);
			xx = otrā.getLexemesByStem().get(0).get(lex.getStem(0)); 
			if (xx != null) alternatīvas.addAll(xx);
			xx = trešā.getLexemesByStem().get(0).get(lex.getStem(0)); 
			if (xx != null) alternatīvas.addAll(xx);
			for (Lexeme alternatīva : alternatīvas) {
				if (lex.getID() < alternatīva.getID()) {
					if (lex.getParadigm() != alternatīva.getParadigm()) {
						//System.out.printf("%st: %s un %s konjugācijas\n", lex.getStem(0), lex.getParadigm().getValue(AttributeNames.i_Konjugaacija), alternatīva.getParadigm().getValue(AttributeNames.i_Konjugaacija));
					}
					if (lex.getParadigm() == pirmā && alternatīva.getParadigm() == pirmā && (!lex.getStem(1).equalsIgnoreCase(alternatīva.getStem(1)) || !lex.getStem(2).equalsIgnoreCase(alternatīva.getStem(2)))) {
						//System.out.printf("%st: %su %su vai %su %su\n", lex.getStem(0), lex.getStem(1), lex.getStem(2), alternatīva.getStem(1), alternatīva.getStem(2));						
					}
				}				
			}
		}
	}

	@Test
	public void personvārdi_Varis6() {
		// 2013.02.05 Vara komentāri
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Edvards", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "edvarda");
		
		formas = locītājs.generateInflections("Ludis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "luda");
		
		formas = locītājs.generateInflections("Krists", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "kristam");
		
		formas = locītājs.generateInflections("Staņislava", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "staņislavai");
		
		formas = locītājs.generateInflections("Raisa", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "raisai");
		
		formas = locītājs.generateInflections("Alberta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "albertai");
		
		formas = locītājs.generateInflections("Gunta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "guntai");		
	}
	
	@Test
	public void gunta19dec_3() {
		// Guntas sūdzības pa skype 2012.12.19 - retās deklinācijas
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		Word vārds = locītājs.analyze("ragus");
		assertTrue(vārds.isRecognized());	
		assertEquals("rags", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
			
		vārds = locītājs.analyze("dermatovenerologi");
		assertTrue(vārds.isRecognized());	
		assertEquals("dermatovenerologs", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void normunds_2013feb25() {
		List<Wordform> formas = locītājs.generateInflections("dziedāt");
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
		testset.addAttribute(AttributeNames.i_Person, "1");
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
		assertInflection(formas, testset, "dziedam");
		
		testset.removeAttribute(AttributeNames.i_Number);
		testset.addAttribute(AttributeNames.i_Person, "3");
		assertInflection(formas, testset, "dzied");
		
		Word vārds = locītājs.analyze("dziedam");
		assertTrue(vārds.isRecognized());	
		assertEquals("dziedāt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("dzied");
		assertTrue(vārds.isRecognized());	
		assertEquals("dziedāt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("dziedām");
		assertFalse(vārds.isRecognized());
	}
	
	@Test
	public void pp20130313() {
		// aizdomas par 5. mijas bugiem
		
		List<Wordform> formas = locītājs.generateInflections("rakt");		
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
		testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Vajadziibas);
		assertInflection(formas, testset, "jārok");
	}
	
	@Test
	public void varis20130221() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Liepa", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "liepai");
		
		AttributeValues filter = new AttributeValues();
		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
		
		formas = locītājs.generateInflections("Liepa", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "liepam");

		formas = locītājs.generateInflections("Lielais", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "lielajam");
		
		formas = locītājs.generateInflections("Valdīšana", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "valdīšanam");
		
		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		formas = locītājs.generateInflections("Dzelzs", true, filter);	
		//for (Wordform forma:formas) forma.describe();		
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "dzelzij");
		
		formas = locītājs.generateInflections("Mazā", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "mazajai");
		
		formas = locītājs.generateInflections("Valdīšana", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "valdīšanai");		
	}

	@Test
	public void varis20130317() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticibles = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		assertTrue("Biezā".matches("\\p{Lu}.*"));
		assertTrue("BIEZĀ".matches("\\p{Lu}.*"));
		
		List<Wordform> formas = locītājs.generateInflections("Biezā", true); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "biezajai");
		
		formas = locītājs.generateInflections("BIEZĀ", true); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "biezajai");
		
		AttributeValues filter = new AttributeValues();
		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);	
		formas = locītājs.generateInflections("VĪTOLA", true, filter); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "vītolai");
		
		formas = locītājs.generateInflections("BAGĀTĀ", true, filter);
		assertTrue(formas.size() > 0);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "bagātajai");
		
		formas = locītājs.generateInflections("Vītola", true, filter); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "vītolai");

		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);	
		formas = locītājs.generateInflections("Kirill", true); 
		assertTrue(formas.size() > 0);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "kirill");
		
		formas = locītājs.generateInflections("Andrej", true); 
		assertTrue(formas.size() > 0);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "andrej");
	}
	
	@Test
	public void laura_20130605() {
		// Vietniekvārdiem neieliek pēdējo pozīciju tagā (noliegumu); -šana atvasinātās formas nav ok
		Word viņš = locītājs.analyze("viņš");
		assertTrue(viņš.isRecognized());		
		assertEquals("pp3msnn", viņš.wordforms.get(0).getTag());
		
		Word ciršana = locītājs.analyze("ciršana");
		assertTrue(ciršana.isRecognized());		
		assertEquals("ncfsn4", ciršana.wordforms.get(0).getTag());
		assertEquals("ciršana", ciršana.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		Word mazgāšanās = locītājs.analyze("mazgāšanos");
		assertTrue(mazgāšanās.isRecognized());		
		assertEquals("ncfsar", mazgāšanās.getBestWordform().getTag());
		
		locītājs.enableGuessing = true;
		Word izpaudusies = locītājs.analyze("izpaudusies");
		assertTrue(izpaudusies.isRecognized());	
	}
	
	@Test
	public void gunta_20130605() {
		// LVK2013 Korpuss saka, ka verba "attiecas" lemma ir "attiecties"
		Word attiecas = locītājs.analyze("attiecas");
		assertTrue(attiecas.isRecognized());
		assertEquals("attiekties", attiecas.wordforms.get(0).getValue(AttributeNames.i_Lemma));

		Word esošo = locītājs.analyze("esošo");
		assertTrue(esošo.isRecognized());
		assertEquals("būt", esošo.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		Word mācās = locītājs.analyze("mācās");
		assertTrue(mācās.isRecognized());
		boolean found = false;
		for (Wordform wf : mācās.wordforms) {
			if (wf.isMatchingStrong(AttributeNames.i_Lemma, "mācīties")) found = true;
		}
		assertTrue(found);

		Word acīmredzot = locītājs.analyze("acīmredzot");
		assertTrue(acīmredzot.isRecognized());
		assertEquals("acīmredzot", acīmredzot.wordforms.get(0).getValue(AttributeNames.i_Lemma));

		Word lielākoties = locītājs.analyze("lielākoties");
		assertTrue(lielākoties.isRecognized());
		assertEquals("lielākoties", lielākoties.wordforms.get(0).getValue(AttributeNames.i_Lemma));

	}
	
	/**
	 * Korpusa analīze - vārdi, kuriem analizators neiedeva nevienu sakarīgu variantu
	 */
	@Test
	public void korpuss_20130605() {
		Word ņem = locītājs.analyze("ņem");
		assertTrue(ņem.isRecognized());
		assertEquals("ņemt", ņem.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		List<Wordform> formas = locītājs.generateInflections("ņemt");
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
		testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
		testset.addAttribute(AttributeNames.i_Person, "2");
		testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
		assertInflection(formas, testset, "ņem");
		
		boolean found = false;
		for (Wordform wf : ņem.wordforms) {
			if (wf.isMatchingStrong(AttributeNames.i_Person, "2")) found = true;
		}
		assertTrue(found);
		
		formas = locītājs.generateInflections("pāriet");
		testset.addAttribute(AttributeNames.i_Person, "3");
		assertInflection(formas, testset, "pāriet");		
	}
	
	@Test
	public void korpuss_20130606() {
		Word acs = locītājs.analyze("acs");
		assertTrue(acs.isRecognized());
		assertEquals("acs", acs.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals(AttributeNames.v_Feminine, acs.wordforms.get(0).getValue(AttributeNames.i_Gender));	
		
		List<Wordform> formas = locītājs.generateInflections("atkāpties");
		describe(formas);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
		testset.addAttribute(AttributeNames.i_Person, "2");
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Paveeles);		
		assertInflection(formas, testset, "atkāpies");
	}
	
	@Test
	public void uri() {
		Word url = locītājs.analyze("www.pillar.lv");
		assertTrue(url.isRecognized());
		assertEquals("xu", url.wordforms.get(0).getTag());		
	}
	
	@Test
	public void obligātiatpazīstamie() throws IOException {
				{
			BufferedReader ieeja;
			String rinda;
			ieeja = new BufferedReader(
					new InputStreamReader(new FileInputStream("dist/mandatory.txt"), "UTF-8"));
			
			while ((rinda = ieeja.readLine()) != null) {
				if (rinda.contains("#") || rinda.isEmpty()) continue;
				List<Word> vārdi = Splitting.tokenize(locītājs, rinda);
				for (Word vārds : vārdi) {
					if (!vārds.isRecognized()) {
						System.err.printf("Neatpazīts vārds '%s' frāzē '%s'\n", vārds.getToken(), rinda);
						//assertTrue(false);
					}
				}
			}		
			ieeja.close();
		}
	}
}
