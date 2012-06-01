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

import java.io.OutputStreamWriter;
import java.io.PrintStream;
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

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.*;
import lv.semti.morphology.lexicon.*;

public class MorphologyTest {
	private static Analyzer locītājs;
	//PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		locītājs = new Analyzer("dist/Lexicon.xml");
	}
	
	@Before
	public void defaultsettings() { 
		locītājs.enableVocative = false;
		locītājs.enableDiminutive = false;
		locītājs.enablePrefixes = false;
		locītājs.enableGuessing = false;
		locītājs.enableAllGuesses = false;
		locītājs.meklētsalikteņus = false;
		locītājs.setCacheSize(0);
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
					fail("Atkārtojas leksēmas nr " + leksēma.getID());
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
		
		Word cukuri = locītājs.analyze("cukuri");
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
		Word cirvis = locītājs.analyze("cirvītis");
		Word pļava = locītājs.analyze("pļaviņa");
		
		assertTrue(cirvis.isRecognized());
		assertTrue(pļava.isRecognized());
		
		boolean irPareizā = false;
		for (Wordform vārdforma : cirvis.wordforms) {
			if (vārdforma.getValue(AttributeNames.i_Lemma).equals("cirvītis")) 
				irPareizā = true;			
		}
		assertEquals(true, irPareizā);
		assertEquals(AttributeNames.v_Deminutive, cirvis.wordforms.get(0).getValue(AttributeNames.i_Guess));
		
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
		
		Word nevarēt = locītājs.analyze("nevarēt");
		assertTrue(nevarēt.isRecognized());
		assertEquals("nevarēt", nevarēt.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		assertEquals("varēt", nevarēt.wordforms.get(0).getValue(AttributeNames.i_SourceLemma));
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
		assertEquals("x_", plus.wordforms.get(0).getTag());
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
	public void ģenerēšana() throws UnsupportedEncodingException {
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		List<Wordform> formas = locītājs.generateInflections("lūzt");
		
		System.out.println();
		
		for (Wordform wf : formas) {
			System.out.printf("\t%s\t%s\n", wf.getTag(), wf.getToken());
			//wf.describe(izeja);
		}
		izeja.flush();
	}
}
