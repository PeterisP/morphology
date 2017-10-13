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
package lv.semti.morphology.Testi;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.*;
import lv.semti.morphology.lexicon.*;
import org.w3c.dom.Attr;

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
                if (!validForm.equalsIgnoreCase(wf.getToken())) {
                    System.err.printf("Found a different form");
                    wf.describe(new PrintWriter(System.err));
                }
				assertEquals(validForm, wf.getToken());
				found = true;
				break;
			}
		}
		if (!found) {
			System.err.printf("assertInflection failed: looking for '%s'\n", validForm);
			testset.describe(new PrintWriter(System.err));
			System.err.println("In:");
			for (Wordform wf : forms) {
				wf.describe(new PrintWriter(System.err));
				System.err.println("\t---");
			}
		}
		assertTrue(found);		
	}

	private void assertLemma(String word, String expectedLemma) {
        Word analysis= locītājs.analyze(word);
        assertTrue(analysis.isRecognized());
        Wordform forma = analysis.getBestWordform();
        assertEquals(expectedLemma, forma.getValue(AttributeNames.i_Lemma));
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
			locītājs = new Analyzer(false);
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
	public void nadziņi() {
		//2008-09-06 atrasts gļuks, ka "pīrādziņi" analīzē pamatforma bija "pīrāgš"
		//2012-02-10 - vairs nav aktuāls 'pīrāgs', jābūt 'pīrādziņš'
		//2015-08-03 failo, jo atrod LĢIS apdzīvoto vietu "Pīrāgi", nomainīts uz nadziņiem 
		locītājs.enableDiminutive = true;
		Word nadziņi = locītājs.analyze("nadziņi");
		assertTrue(nadziņi.isRecognized());		
		Wordform forma = nadziņi.getBestWordform();
		assertEquals("nadziņš", forma.getValue(AttributeNames.i_Lemma));
		assertEquals("nags", forma.getValue(AttributeNames.i_SourceLemma));
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
	public void durkls() {
		// 2015-08-26 tēzaura apstrādes gaitā mainījās priekšstats šī vārda paradigmu.
		Word durkls = locītājs.analyze("durkls");
		if (durkls.isRecognized())
			assertEquals("1", durkls.wordforms.get(0).getValue(AttributeNames.i_ParadigmID));
	}
	
	@Test
	public void lasis(){
		// 2016-02-03 atklāta ģenerēšanas kļūda - trūkst mijas.
		List<Wordform> lasis = locītājs.generateInflections("lasis");
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflection(lasis, testset, "laša");
		
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(lasis, testset, "lašiem");
		
		Word w = locītājs.analyze("lasiem");
		assertFalse(w.isRecognized());
	}

	@Test
	public void skansts(){
		List<Wordform> skansts = locītājs.generateInflections("skansts");
		assertNotEquals(skansts.size(), 0);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(skansts, testset, "skanšu");
	}
	
	@Test
	public void debesis(){
		// 2016-02-03 ir divu veidu debesis - 3. un 6. deklinācija
		// 3. deklinācijā lokās pēc standarta, bet 6. deklinācijā bez mijas
		List<Wordform> debesis = locītājs.generateInflectionsFromParadigm("debesis", 3);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
		assertInflection(debesis, testset, "debeša");
		
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(debesis, testset, "debešiem");
		
		List<Wordform> debess = locītājs.generateInflectionsFromParadigm("debess", 35);
		testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		assertInflection(debess, testset, "debess");
		
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(debess, testset, "debesu");

		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
		assertInflection(debess, testset, "debesīm");
		
		//Word w = locītājs.analyze("debesis");
		//assertTrue(w.isRecognized());
		//assertEquals(w.wordforms.size(), 3); // siev. dz. dsk. nom., akuz., vīr. dz. vsk. nom.
	}
	
	@Test
	public void balss(){
		// 2016-02-03 Tā kā "debesis" kļūda visticamāk ir saistīta ar 6.dekl. izņēmumiem, tad papildus tests uz tiem.
		List<Wordform> balss = locītājs.generateInflections("balss");
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(balss, testset, "balsu");
				
		Word w = locītājs.analyze("balšu");
		assertFalse(w.isRecognized());
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
				if (leksēmuNr.get(leksēma.getID()) != null) {
					leksēma.describe(new PrintWriter(System.err));
					leksēmuNr.get(leksēma.getID()).describe(new PrintWriter(System.err));
					fail(String.format("Atkārtojas leksēmas nr %d : '%s' un '%s'", leksēma.getID(), leksēma.getStem(0), leksēmuNr.get(leksēma.getID()).getStem(0)));
				}
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
		System.out.printf("%d pieprasījumi sekundē (%d ms)\n", skaits*1000/starpība, starpība);
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
							System.err.println("Atkārtojas leksēmas:");
							l1.describe(new PrintWriter(System.err));
							l2.describe(new PrintWriter(System.err));
//							izeja.printf("leksēma = analizators.leksikons.leksēmaPēcID(%d); //%d\n" +
//									"leksēma.getVārdgrupa().izņemtLeksēmu(leksēma);\n", l2.getID(), l1.getID());
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
		assertEquals("mcs_p0", simt.wordforms.get(0).getTag());
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
		assertEquals(1, viņi.wordformsCount());
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
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Valda");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Valdim");
			
		formas = locītājs.generateInflections("Raitis");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Raita");

		formas = locītājs.generateInflections("cerēt");
		// TODO - salikt verbiem testpiemērus			
	}
	
	@Test
	public void ģenerēšanaNezināmiem() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
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
		assertEquals("Trrt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("GAIZINAISI-Ā3");
		assertTrue(vārds.isRecognized());
		assertEquals("Gaizinaisi-Ā3", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
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
	public void personvaardi_Varis() {
		// 2012.06.08 sūtītie komentāri par locīšanas defektiem.
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Valdis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Valda");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Valdim");
		
		formas = locītājs.generateInflections("Čaikovskis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Čaikovska");
		
		formas = locītājs.generateInflections("Cēsis", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "Cēsu");
		
		formas = locītājs.generateInflections("Raitis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Raita");

		formas = locītājs.generateInflections("Auziņš", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "Auziņu");

		formas = locītājs.generateInflections("Ivis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Ivja");
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "Ivju");
		
		formas = locītājs.generateInflections("Eglīts", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Eglīša");
		
		formas = locītājs.generateInflections("Švirkste", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, AttributeNames.v_Feminine, "Švirkstes");
		
		formas = locītājs.generateInflections("Taļikova", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, AttributeNames.v_Feminine, "Taļikovas");
		
		formas = locītājs.generateInflections("Bērziņš", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Nominative, AttributeNames.v_Masculine, "Bērziņš");
		
		formas = locītājs.generateInflections("Dīcis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "Dīcim");

		formas = locītājs.generateInflections("Asna", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Asnai");		
		
		formas = locītājs.generateInflections("Lielais", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "Lielajam");
		
		formas = locītājs.generateInflections("Mazā", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Mazajai");

		formas = locītājs.generateInflections("Zaļais", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "Zaļajam");		
		
		formas = locītājs.generateInflections("Santis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, AttributeNames.v_Masculine, "Santa");
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
		locītājs.guessParticiples = false;
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
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Pauls", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Paul");
		
		formas = locītājs.generateInflections("Laura", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Laura");
		
		formas = locītājs.generateInflections("Lauriņa", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Lauriņ");

		formas = locītājs.generateInflections("Made", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Made");		

		formas = locītājs.generateInflections("Kristīnīte", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Kristīnīt"); 

		formas = locītājs.generateInflections("Margrieta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Margrieta"); // principā der gan viens, gan otrs, ģenerē arī abus, bet 'margrieta' ir pirmais
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
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Auziņš", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Auziņ");
		
		formas = locītājs.generateInflections("Miervaldis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Miervalža");
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
		assertEquals("mcc0p0", vārds.wordforms.get(0).getTag());
	}

	@Test
	public void personvārdi_Varis4() {
		// 2012.08.13 P33 vokatīvu shēma
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Jēkabs");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Jēkab");
		
		formas = locītājs.generateInflections("Mārtiņš");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Mārtiņ");
		
		formas = locītājs.generateInflections("Mikus");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Miku");
		
		formas = locītājs.generateInflections("Ingus");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Ingu");
		
		formas = locītājs.generateInflections("Kalns");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Kaln");
		
		formas = locītājs.generateInflections("Liepiņš");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Liepiņ");
		
		formas = locītājs.generateInflections("Zaķis");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Zaķi");
		
		formas = locītājs.generateInflections("Ledus");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Ledu");
		
		formas = locītājs.generateInflections("Platais");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Platais");
		
		formas = locītājs.generateInflections("Lielais");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Lielais");
		
		formas = locītājs.generateInflections("Biezais");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Biezais");
		
		formas = locītājs.generateInflections("Silvija");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Silvij");
		
		formas = locītājs.generateInflections("Kadrije"); //hipotētiski
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Kadrij");
		
		formas = locītājs.generateInflections("Karlīne");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Karlīn");
		
		formas = locītājs.generateInflections("Vilhelmīne");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Vilhelmīn");
		
		formas = locītājs.generateInflections("Skaidrīte");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Skaidrīt");
		
		formas = locītājs.generateInflections("Juliāna");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Juliān");
		
		formas = locītājs.generateInflections("Eglīte");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Eglīt");
		
		formas = locītājs.generateInflections("Lapsiņa");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Lapsiņ");
		
		formas = locītājs.generateInflections("Pilsētniece");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Pilsētniec");
		
		formas = locītājs.generateInflections("Salnāja");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Salnāj");
		
		formas = locītājs.generateInflections("Garkāje");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Garkāje");
		
		formas = locītājs.generateInflections("Zeidmane");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Zeidmane");
		
		formas = locītājs.generateInflections("Kreice");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Kreice");
		
		formas = locītājs.generateInflections("Kreija");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Kreija");
		
		formas = locītājs.generateInflections("Kreitenberga");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Kreitenberga");
		
		//Nav norealizēts: Par salikteņiem Ja salikteņa 2.  daļa atsevišķi kvalificējas īsajai formai, tad arī saliktenis kvalificējas īsajai formai. 
	}

	@Test
	public void personvārdi_Varis5() {
		// 2012.08.13 Vara komentāri
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Arvydas", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Arvydas");
		
		formas = locītājs.generateInflections("Rīta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "Rīta");
		
		formas = locītājs.generateInflections("rīta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "rīta"); 
	}
	
	@Test
	public void laura_Aug13() {
		locītājs.enableGuessing = true;
		// 2012.08.13 Lauras samarķētā atšķirību analīze 
		List<Wordform> formas = locītājs.generateInflections("Fredis");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Freda");
	
		formas = locītājs.generateInflections("Freda");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Fredas");
		
		
		Word freda = locītājs.analyze("Freda");
//        freda.describe(new PrintWriter(System.err));

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
	public void InflectionSep4() {
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
		testset.addAttribute(AttributeNames.i_EndingID, "1779");
		assertInflection(formas, testset, "māku");
		testset.addAttribute(AttributeNames.i_EndingID, "1781");
		assertInflection(formas, testset, "mākam");		
		testset.addAttribute(AttributeNames.i_EndingID, "1783");
		assertInflection(formas, testset, "māk");
		testset.addAttribute(AttributeNames.i_EndingID, "1794");
		assertInflection(formas, testset, "jāmāk");
		testset.addAttribute(AttributeNames.i_EndingID, "2328");
		assertInflection(formas, testset, "jāmākot");

		formas = locītājs.generateInflections("gulēt");
		testset.addAttribute(AttributeNames.i_EndingID, "1780");
		assertInflection(formas, testset, "guli");
		testset.addAttribute(AttributeNames.i_EndingID, "1783");
		assertInflection(formas, testset, "guļ");
		testset.addAttribute(AttributeNames.i_EndingID, "1798");
		assertInflection(formas, testset, "guliet");
		testset.addAttribute(AttributeNames.i_EndingID, "2328");
		assertInflection(formas, testset, "jāguļot");

		formas = locītājs.generateInflections("aizgulēties");
		testset.addAttribute(AttributeNames.i_EndingID, "2337");
		assertInflection(formas, testset, "aizguļos");
		
		formas = locītājs.generateInflections("vajadzēt");
		testset.addAttribute(AttributeNames.i_EndingID, "1779");
		assertInflection(formas, testset, "vajagu");
		testset.addAttribute(AttributeNames.i_EndingID, "1781");
		assertInflection(formas, testset, "vajagam");		
		testset.addAttribute(AttributeNames.i_EndingID, "1783");
		assertInflection(formas, testset, "vajag");
		testset.addAttribute(AttributeNames.i_EndingID, "1794");
		assertInflection(formas, testset, "jāvajag");
		testset.addAttribute(AttributeNames.i_EndingID, "2328");
		assertInflection(formas, testset, "jāvajagot");

		formas = locītājs.generateInflections("mocīt");
		testset.addAttribute(AttributeNames.i_EndingID, "1780");
		assertInflection(formas, testset, "moki");		

		formas = locītājs.generateInflections("slodzīt");
		testset.addAttribute(AttributeNames.i_EndingID, "1779");
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
		
		vārds = locītājs.analyze("izkusušākais");	
		assertTrue(vārds.isRecognized());	
		assertEquals("izkust", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("visizkusušākais");
		assertTrue(vārds.isRecognized());	
		assertEquals("izkust", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));

		vārds = locītājs.analyze("visveiktākais");				
		assertTrue(vārds.isRecognized());	
		assertEquals("veikt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("vislasītākais");				
		assertTrue(vārds.isRecognized());	
		assertEquals("lasīt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));

		vārds = locītājs.analyze("veicu");				
		assertTrue(vārds.isRecognized());	
		assertEquals("veikt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		vārds = locītājs.analyze("veikušais");				
		assertTrue(vārds.isRecognized());	
		assertEquals("veikt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		vārds = locītājs.analyze("veicušais");				
		assertFalse(vārds.isRecognized());	

		vārds = locītājs.analyze("sarūgu");				
		assertTrue(vārds.isRecognized());	
		assertEquals("sarūgt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		vārds = locītājs.analyze("sarūgušais");				
		assertTrue(vārds.isRecognized());	
		assertEquals("sarūgt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		vārds = locītājs.analyze("sarūdzušais");				
		assertFalse(vārds.isRecognized());		

		// tas pats 2. un 3. konjug.
		vārds = locītājs.analyze("zaigojušāks");	
		assertTrue(vārds.isRecognized());	
		assertEquals("zaigot", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		vārds = locītājs.analyze("zaigojošāks");	
		assertTrue(vārds.isRecognized());	
		assertEquals("zaigot", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("vislasījušākais");	
		assertTrue(vārds.isRecognized());	
		assertEquals("lasīt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		vārds = locītājs.analyze("lasošāks");	
		assertTrue(vārds.isRecognized());	
		assertEquals("lasīt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("vislasāmākais");	
		assertTrue(vārds.isRecognized());	
		assertEquals("lasīt", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		vārds = locītājs.analyze("saprotamāks");	
		assertTrue(vārds.isRecognized());	
		assertEquals("saprast", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		vārds = locītājs.analyze("viszaigojošāk");	
		assertTrue(vārds.isRecognized());	
		assertEquals("zaigojoši", vārds.wordforms.get(0).getValue(AttributeNames.i_Lemma));
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
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Edvards", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Edvarda");
		
		formas = locītājs.generateInflections("Ludis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Luda");
		
		formas = locītājs.generateInflections("Krists", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Kristam");
		
		formas = locītājs.generateInflections("Staņislava", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Staņislavai");
		
		formas = locītājs.generateInflections("Raisa", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Raisai");
		
		formas = locītājs.generateInflections("Alberta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Albertai");
		
		formas = locītājs.generateInflections("Gunta", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Guntai");		
	}
	
	@Test
	public void gunta19dec_3() {
		// Guntas sūdzības pa skype 2012.12.19 - retās deklinācijas
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
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
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Liepa", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Liepai");
		
		AttributeValues filter = new AttributeValues();
		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
		
		formas = locītājs.generateInflections("Liepa", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Liepam");

		formas = locītājs.generateInflections("Lielais", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "Lielajam");
		
		formas = locītājs.generateInflections("Valdīšana", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Masculine, "Valdīšanam");
		
		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		formas = locītājs.generateInflections("Dzelzs", true, filter);	
		//for (Wordform forma:formas) forma.describe();		
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Dzelzij");
		
		formas = locītājs.generateInflections("Mazā", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Mazajai");
		
		formas = locītājs.generateInflections("Valdīšana", true, filter);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, AttributeNames.v_Feminine, "Valdīšanai");		
	}

	@Test
	public void varis20130317() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		assertTrue("Biezā".matches("\\p{Lu}.*"));
		assertTrue("BIEZĀ".matches("\\p{Lu}.*"));
		
		List<Wordform> formas = locītājs.generateInflections("Biezā", true); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Biezajai");
		
		formas = locītājs.generateInflections("BIEZĀ", true); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "BIEZAJAI");
		
		AttributeValues filter = new AttributeValues();
		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);	
		formas = locītājs.generateInflections("VĪTOLA", true, filter); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "VĪTOLAI");
		
		formas = locītājs.generateInflections("BAGĀTĀ", true, filter);
		assertTrue(formas.size() > 0);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "BAGĀTAJAI");
		
		formas = locītājs.generateInflections("Vītola", true, filter); 
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Vītolai");

		filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);	
		formas = locītājs.generateInflections("Kirill", true); 
		assertTrue(formas.size() > 0);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Kirill");
		
		formas = locītājs.generateInflections("Andrej", true); 
		assertTrue(formas.size() > 0);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "Andrej");
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
		describe(url.wordforms);
		assertEquals("xu", url.wordforms.get(0).getTag());		
	}
	
	@Test
	public void obligātiatpazīstamie() throws IOException {
				{
			BufferedReader ieeja;
			String rinda;
			ieeja = new BufferedReader(
					new InputStreamReader(getClass().getClassLoader().getResourceAsStream("mandatory.txt"), "UTF-8"));
			
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
	
	@Test
	public void lociishanas_lielie_burti() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("Valdis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Valda");
		
		formas = locītājs.generateInflections("Vīķe-Freiberga", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Vīķes-Freibergas");
		
		formas = locītājs.generateInflections("Žverelo-Freiberga", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Žverelo-Freibergas");
		
//		formas = locītājs.generateInflectionsFromParadigm("Freiberga-Žverelo", true);
//		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Freibergas-Žverelo");
		
		formas = locītājs.generateInflections("Rīga-Best", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Rīga-Best");
		
		formas = locītājs.generateInflections("Best-Rīga", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Best-Rīgas");
		
		formas = locītājs.generateInflections("Rudaus-Rudovskis", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Rudaus-Rudovska");
		
		formas = locītājs.generateInflections("Pavļuta-Deslandes", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "Pavļutas-Deslandes");
		
	}
	
	@Test
	public void jaundzimushais() {
		Word w = locītājs.analyze("jaundzimušajam");
		assertTrue(w.isRecognized());
		assertEquals(AttributeNames.v_Adjective, w.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals("jaundzimušais", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));

		w = locītājs.analyze("jaundzimusī");
		assertTrue(w.isRecognized());
		assertEquals(AttributeNames.v_Adjective, w.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals("jaundzimusī", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		w = locītājs.analyze("galvenajam");
		assertTrue(w.isRecognized());
		assertEquals(AttributeNames.v_Adjective, w.wordforms.get(0).getValue(AttributeNames.i_PartOfSpeech));
		assertEquals("galvenais", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));
	}
	
	@Test
	public void guessinglimits() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = false;
		locītājs.guessVerbs = false;
		locītājs.guessNouns = true;
		locītājs.enableAllGuesses = true;
//		locītājs.describe(new PrintWriter(System.out));
		Word w = locītājs.analyze("xxxbs");		
		assertTrue(w.isRecognized());
		
		w = locītājs.analyze("xxxes");
		w.describe(new PrintWriter(System.out));
		for (Wordform wf : w.wordforms) {
			assertFalse(wf.isMatchingStrong(AttributeNames.i_Declension, "1"));
		}
	}
	
	@Test
	public void izskanjas() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = false;
		locītājs.guessVerbs = true;
		locītājs.enableAllGuesses = true;
		
		Word austrumlatvija = locītājs.analyze("Austrumlatvija");
		assertTrue(austrumlatvija.isRecognized());

		Word w = locītājs.analyze("mirušais");
		assertTrue(w.isRecognized());
	}
	
	@Test
	/**
	 * 2014-03-31 bug - autocreated lexemes from generateInflectionsFromParadigm pollute future analysis results
	 */
	public void inflect_garbage_collection(){
		locītājs.generateInflections("Šašliki");
		Word bulduri = locītājs.analyze("Šašliki");
		assertTrue(bulduri.isRecognized());		
		for (Wordform wf : bulduri.wordforms) {
			assertEquals("šašliks", wf.getValue(AttributeNames.i_Lemma));
		}
	}
	
	@Test
	/**
	 * LETA lietvārdu locījumu pārbaude - nekorektas mijas 6. dekl
	 */
	public void mijas6dekl(){
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("acs", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "acu");
		
		formas = locītājs.generateInflections("auss", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "ausu");
		formas = locītājs.generateInflections("kūts", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "kūtu");
		formas = locītājs.generateInflections("zoss", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "zosu");
		formas = locītājs.generateInflections("dakts", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "daktu");
		formas = locītājs.generateInflections("šalts", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "šaltu");
		formas = locītājs.generateInflections("maksts", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "makstu");
	}
	
	
	@Ignore("nav skaidra pozīcija par vokatīviem") @Test
	/**
	 * LETA lietvārdu locījumu pārbaude - defaultajai formai lokot jābūt ar galotni
	 */
	public void vokatiivi(){
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("koks", true);
		describe(formas);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "koks");
		
		formas = locītājs.generateInflections("paziņa", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "paziņa");
	}

	@Test
	public void kviesis() {
		Word w = locītājs.analyze("kvieši");
		assertTrue(w.isRecognized());
		assertEquals("kviesis", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));

		List<Wordform> formas = locītājs.generateInflections("kviesis", true);
		assertNounInflection(formas, AttributeNames.v_Plural, AttributeNames.v_Genitive, "", "kviešu");
	}
	
	@Test
	/**
	 * LETA lietvārdu locījumu pārbaude - citi gljuki
	 */
	public void gljuki20140401(){
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("mēness", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "mēness");
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Vocative, "", "mēnes");
		
	}
	
	@Test
	/**
	 * Treat out-of-vocabulary acronyms as not flexive - e.g. NATO, FMS, IMS etc 
	 */
	public void acronyms() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessAdjectives = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;
		
		List<Wordform> formas = locītājs.generateInflections("FMS", true);
		assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Genitive, "", "FMS");
	}
	
	@Test 
	/**
	 * 2014.08.01 Bug with verb stem changes -> rakt -> *rakis (racis); *rakiens (raciens)
	 */
	public void rakiens() {
		Word w = locītājs.analyze("racis");
		assertTrue(w.isRecognized());
		assertEquals("rakt", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		w = locītājs.analyze("rakis");
		assertFalse(w.isRecognized());
		
		w = locītājs.analyze("veicis");  // lai nesalauž šo
		assertTrue(w.isRecognized());
		assertEquals("veikt", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		List<Wordform> formas = locītājs.generateInflections("rakt", false);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
		testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflection(formas, testset, "raciens");
	}
	
	@Test 
	/**
	 * 2014.08.25 Bug with verb stem changes
	 */
	public void lecdams() {
		Word w = locītājs.analyze("lekdams");
		assertTrue(w.isRecognized());
		assertEquals("lekt", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		w = locītājs.analyze("lecdams");
		assertFalse(w.isRecognized());
		
		List<Wordform> formas = locītājs.generateInflections("lekt", false);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
		testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Participle);
		testset.addAttribute(AttributeNames.i_Lokaamiiba, AttributeNames.v_DaljeejiLokaams);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
		testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflection(formas, testset, "lekdams");
	}
	
	@Test 
	/**
	 * 2014.08.25 Bug with verb 'līt'
	 */
	public void līstiiet() {
		Word w = locītājs.analyze("līstiet");
		assertTrue(w.isRecognized());
		assertEquals("līt", w.wordforms.get(0).getValue(AttributeNames.i_Lemma));
		
		w = locītājs.analyze("līstiiet");
		assertFalse(w.isRecognized());
		
		// un vēl bija "lijdams" gļukforma		
		List<Wordform> formas = locītājs.generateInflections("līt", false);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
		testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Participle);
		testset.addAttribute(AttributeNames.i_Lokaamiiba, AttributeNames.v_DaljeejiLokaams);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
		testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflection(formas, testset, "līdams");
	}
	
	@Test
	/**
	 * Local dialectal words from thesaurus (http://tezaurs.lv/sv) should not be in default lexicon
	 */
	public void apvidvārdi() {
		Word w = locītājs.analyze("īstāis");
		assertFalse(w.isRecognized());
	}
	
	@Test
	/**
	 * In case if there is any ambiguity between a normal word and a lexeme flagged as rare, the rare option should be excluded completely by default ('ar' -> 'art'; 'ir' -> 'irt')
	 */
	public void retie() {
		Word w = locītājs.analyze("aršana");
		assertTrue(w.isRecognized());
		
		w = locītājs.analyze("ar");
		for (Wordform wf : w.wordforms)
			assertFalse(wf.isMatchingStrong(AttributeNames.i_Lemma, "art"));
	}
	
	@Test
	public void turlais() {
		locītājs.enableGuessing = true;
		locītājs.enableVocative = true;
		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
		locītājs.guessInflexibleNouns = true;
		locītājs.enableAllGuesses = true;

		Word w = locītājs.guessByEnding("turlais", "Turlais");
		assertTrue(w.isRecognized());
		for (Wordform wf : w.wordforms)
			assertFalse(wf.isMatchingStrong(AttributeNames.i_Lemma, "art"));
	}
	
	@Test
	public void apstākļa_vārdu_ģenerēšana() {
		List<Wordform> formas = locītājs.generateInflections("labi");
		assertEquals(1, formas.size());
	}
	
	@Test
	public void rozā() {
		List<Wordform> formas = locītājs.generateInflections("rozā");
		assertEquals(1, formas.size());
		assertTrue(formas.get(0).isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective));
	}
	
	@Test // klausītājies, vēlējumies - http://valoda.ailab.lv/latval/vidusskolai/morfol/lietv-atgr.htm 
	public void reflexive_nouns() {
		Word klausītājies = locītājs.analyze("klausītājies");
		assertTrue(klausītājies.isRecognized());
		
		Word vēlējumies = locītājs.analyze("vēlējumies");
		assertTrue(vēlējumies.isRecognized());
		
		Word acīsskatīšanās = locītājs.analyze("acīsskatīšanās");
		assertTrue(acīsskatīšanās.isRecognized());
		
		Word pakaļdzinējies = locītājs.analyze("pakaļdzinējies");
		assertTrue(pakaļdzinējies.isRecognized());
//		assertEquals(AttributeNames.v_Ending, crap.wordforms.get(0).getValue(AttributeNames.i_Guess));
//
//		List<Wordform> formas = locītājs.generateInflectionsFromParadigm("rozā");
//		assertEquals(1, formas.size());
//		assertTrue(formas.get(0).isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective));
	}
	
	@Test // https://github.com/PeterisP/morphology/issues/7 
	public void mijas_3_konj() {
		// mīcīt -> mīcu
		Word test = locītājs.analyze("mīcu");
		assertTrue(test.isRecognized());
		test = locītājs.analyze("mīku");
		assertFalse(test.isRecognized());
		
		test = locītājs.analyze("mācu");
		assertTrue(test.isRecognized());
		test = locītājs.analyze("māku");
		assertFalse(test.getBestWordform().getValue(AttributeNames.i_Lemma).equalsIgnoreCase("mācīt"));
		
		test = locītājs.analyze("tūcu");
		assertTrue(test.isRecognized());
		test = locītājs.analyze("tūku");
		assertFalse(test.getBestWordform().getValue(AttributeNames.i_Lemma).equalsIgnoreCase("tūcīt"));
		
		// sacīt -> saku
		test = locītājs.analyze("sacu");
		assertFalse(test.isRecognized());
		test = locītājs.analyze("saku");
		assertTrue(test.isRecognized());

        // sacīt -> saku
        test = locītājs.analyze("izsacos");
        assertFalse(test.isRecognized());
        test = locītājs.analyze("izsakos");
        assertTrue(test.isRecognized());

        test = locītājs.analyze("slaucu");
        assertTrue(test.isRecognized());
		assertFalse(test.getBestWordform().getValue(AttributeNames.i_Lemma).equalsIgnoreCase("slaucīt"));
        assertTrue(test.getBestWordform().getValue(AttributeNames.i_Lemma).equalsIgnoreCase("slaukt"));
		test = locītājs.analyze("slauku");
		assertTrue(test.isRecognized());

		test = locītājs.analyze("braucu");
		assertFalse(test.getBestWordform().getValue(AttributeNames.i_Lemma).equalsIgnoreCase("braucīt"));
		test = locītājs.analyze("brauku");
		assertTrue(test.isRecognized());

        test = locītājs.analyze("uzbraucu");
        assertFalse(test.getBestWordform().getValue(AttributeNames.i_Lemma).equalsIgnoreCase("uzbraucīt"));
        test = locītājs.analyze("uzbrauku");
        assertTrue(test.isRecognized());

//		test = locītājs.analyze("izšļaucu");
//		assertFalse(test.getBestWordform().getValue(AttributeNames.i_Lemma).equalsIgnoreCase("izšļaucīt"));
//		test = locītājs.analyze("izšļauku");
//		assertTrue(test.isRecognized());
		
		// ņurcīt -> ņurcu un ņurku
		test = locītājs.analyze("ņurcu");
		assertTrue(test.isRecognized());
		test = locītājs.analyze("ņurku");
		assertTrue(test.isRecognized());
		
		test = locītājs.analyze("murcu");
		assertTrue(test.isRecognized());
		test = locītājs.analyze("murku");
		assertTrue(test.isRecognized());
		
		test = locītājs.analyze("mocu");
		assertTrue(test.isRecognized());
		test = locītājs.analyze("moku");
		assertTrue(test.isRecognized());
	}

	@Test // pēc analoģijas ar visu citu būtu jābūt sēžošs bet ir sēdošs
	public void sēdošs() {
		Word sēdošs = locītājs.analyze("sēdošs");
		assertTrue(sēdošs.isRecognized());

	}

	@Test // izmaiņas izteiksmju sarakstā
	public void vajadzībasatstāstījuma() {
		Word jārokot = locītājs.analyze("jārokot");
		assertTrue(jārokot.isRecognized());
		assertEquals(AttributeNames.v_VajadziibasAtstaastiijuma, jārokot.wordforms.get(0).getValue(AttributeNames.i_Izteiksme));
	}

	@Test // Tezauram locīšanai - lai nelokam to, kas nav leksikonā bez minēšānas
	public void nelocīt() throws UnsupportedEncodingException {
		List<Wordform> formas = locītājs.generateInflections("xxx");
		assertEquals(0, formas.size());

		locītājs.guessVerbs = false;
		locītājs.guessParticiples = false;
//		locītājs.describe(new PrintWriter(new OutputStreamWriter(System.out, "UTF-8")));

		formas = locītājs.generateInflections("vārāms");
		assertEquals(0, formas.size());

        formas = locītājs.generateInflections("nav");
        assertEquals(0, formas.size());
	}

	@Test // Crash uz sliktu locīšanu
	public void locīt_ar_sliktu_paradigmu() {
		List<Wordform> formas = locītājs.generateInflectionsFromParadigm("vārāms", 16);
		assertTrue(true);
	}

	@Test // izmaiņas ar substantivizējušamies divdabjiem un īpašībasvārdiem
	public void adjektīviskā_deklinācija() {
        List<Wordform> formas = locītājs.generateInflections("mēnessērdzīgais", true);
        assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "mēnessērdzīgajam");

        formas = locītājs.generateInflections("mēnessērdzīgā", true);
        assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "mēnessērdzīgajai");

        formas = locītājs.generateInflections("cietušais", true);
        assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "cietušajam");

        formas = locītājs.generateInflections("cietusī", true);
        assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "cietušajai");

        formas = locītājs.generateInflections("dzeramais", true);
        assertNounInflection(formas, AttributeNames.v_Singular, AttributeNames.v_Dative, "", "dzeramajam");
    }

    @Test // Hardcoded vārdu locīšana
    public void inflect_hardcoded() {
        List<Wordform> formas = locītājs.generateInflections("būt");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_Person, "3");
        testset.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
        testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        assertInflection(formas, testset, "ir");

        formas = locītājs.generateInflections("viņš");

        testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
        assertInflection(formas, testset, "viņam");
    }

	@Test // Bija vārdiem simts, miljons utml sieviešu dzimtes formas arī. Pārklājas ar https://github.com/PeterisP/morphology/issues/10
	public void simtas() {
		List<Wordform> formas = locītājs.generateInflections("simts");
        for (Wordform forma : formas) {
        	if (forma.getToken().equalsIgnoreCase("simtas")) {
        	    forma.describe();
            }
            assertNotEquals("simtas", forma.getToken());
        }

        Word simtas = locītājs.analyze("simtas");
        assertFalse(simtas.isRecognized());
    }

    @Test // Problēma ar vārdu krāties, kur bija formas 'krāos' u.c.
    public void krāties() {
        List<Wordform> formas = locītājs.generateInflections("krāties");
        for (Wordform forma : formas) {
            assertNotEquals("krāos", forma.getToken());
        }

        Word krāos = locītājs.analyze("krāos");
        assertFalse(krāos.isRecognized());
    }

    @Test // Locījumu ģenerēšanai jādarbojas ar vairākiem celmiem 1. konjugācijas gadījumā
    public void multistem_generateinflections() {
        List<Wordform> sairšana = locītājs.generateInflectionsFromParadigm("irt", 15, "ir", "irst", "ir");
        List<Wordform> laivas_iršana = locītājs.generateInflectionsFromParadigm("irt", 15, "ir", "ir", "īr");

        AttributeValues pagaatne = new AttributeValues();
        pagaatne.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        pagaatne.addAttribute(AttributeNames.i_Person, "3");
        pagaatne.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
        pagaatne.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Pagaatne);
        assertInflection(sairšana, pagaatne, "ira");
        assertInflection(laivas_iršana, pagaatne, "īra");

        AttributeValues tagadne = new AttributeValues();
        tagadne.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        tagadne.addAttribute(AttributeNames.i_Person, "1");
        tagadne.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
        tagadne.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        assertInflection(sairšana, tagadne, "irstu");
        assertInflection(laivas_iršana, tagadne, "iru");
    }

	@Test // Problēma ar daudzskaitlinieku locīšanu
	public void ļaudis() {
        AttributeValues attrs = new AttributeValues();
        attrs.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
        attrs.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);

        List<Wordform> formas = locītājs.generateInflectionsFromParadigm("ļaudis", 11, attrs);
		for (Wordform forma : formas) {
			assertNotEquals("ļaudiij", forma.getToken());
            assertFalse(forma.isMatchingStrong(AttributeNames.i_Number, AttributeNames.v_Singular));
            assertTrue(forma.isMatchingStrong(AttributeNames.i_Gender, AttributeNames.v_Masculine));
		}

		Word ļaudiij = locītājs.analyze("ļaudiij");
		assertFalse(ļaudiij.isRecognized());
	}

    @Test // https://github.com/PeterisP/morphology/issues/15
    public void griedt() {
        List<Wordform> formas = locītājs.generateInflections("griezt");
        for (Wordform forma : formas) {
            assertNotEquals("gried", forma.getToken());
            assertNotEquals("griediet", forma.getToken());
        }

        assertFalse( locītājs.analyze("gried").isRecognized() );
        assertFalse( locītājs.analyze("griediet").isRecognized() );
        assertTrue( locītājs.analyze("griez").isRecognized() );
        assertTrue( locītājs.analyze("grieziet").isRecognized() );
    }

    @Test
    public void lemmas2017mar() {
        assertLemma("izpaužas", "izpausties");
        assertLemma("finanšu", "finanses");
        assertLemma("tūkstošus", "tūkstotis");
        assertLemma("slēpjas", "slēpties");
//        assertLemma("pārējie", "pārējais");
        locītājs.enableGuessing = true;
        assertLemma("Pētera", "Pēteris");
        assertLemma("NATO", "NATO");
        assertLemma("lībiešu", "lībietis");
    }

    @Test
    public void turpms() {
        assertLemma("turpmākiem", "turpmāks");
    }

//    https://github.com/PeterisP/morphology/issues/12
    @Test
    public void pēdējajam() {
        List<Wordform> formas = locītājs.generateInflections("pēdējs");
        for (Wordform forma : formas) {
            if (forma.getToken().equalsIgnoreCase("pēdējajam"))
                describe(new LinkedList<Wordform>(Arrays.asList(forma)));
            assertNotEquals("pēdējajam", forma.getToken()); // šo formu nedrīkst ģenerēt
        }
        assertLemma("pēdējam", "pēdējs");
        assertLemma("pēdējajam", "pēdējais");  // bet drīkst atpazīt
    }

    @Test public void pase() {
        List<Wordform> pase = locītājs.generateInflections("pase");
        List<Wordform> kase = locītājs.generateInflections("kase");
        List<Wordform> rase = locītājs.generateInflections("rase");

        AttributeValues dskg = new AttributeValues();
        dskg.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        dskg.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        dskg.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
        assertInflection(pase, dskg, "pasu");
        assertInflection(kase, dskg, "kasu");
        assertInflection(rase, dskg, "rasu");

        // vēl pase, kase, apaļmute, diplomande, artiste, amtisemīte, autobāze, mufe
        // manšete, torte, cunfte, dzeņaukste, plekste, lete, mufe ir ar optionālo miju
        // aste, balle, gāze ir bez mijas
    }

    @Test public void frequencies() {
        assertTrue( locītājs.analyze("Kaspars").isRecognized() );
        assertFalse( locītājs.analyze("Induls").isRecognized() );
    }
}
