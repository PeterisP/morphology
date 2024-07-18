/******************************************************************************
 Copyright 2008, 2009, 2014 Institute of Mathematics and Computer Science, University of Latvia
 Author: Pēteris Paikens

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package lv.semti.morphology.Testi;


import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.Ending;
import lv.semti.morphology.lexicon.Lexeme;
import lv.semti.morphology.lexicon.Paradigm;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class LatgalianTest {
	private static Analyzer analyzer;

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
			if (wf.isMatchingStrongOneSide(testset)) {
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

	private void assertInflectionMultiple(List<Wordform> forms, AttributeValues testset, Set<String> validForms) {
		HashSet<String> foundCorrect = new HashSet<>();
		HashSet<String> foundOther = new HashSet<>();
		for (Wordform wf : forms) {
			if (wf.isMatchingStrongOneSide(testset)) {
				if (validForms.contains(wf.getToken())) foundCorrect.add(wf.getToken());
				else foundOther.add(wf.getToken());
			}
		}

		if (!foundOther.isEmpty())
		{
			System.err.print("assertInflectionMultiple failed with spare forms:\n");
			System.err.println (foundOther);
		}
		assertTrue(foundOther.isEmpty());
		assertEquals(validForms.size(), foundCorrect.size());
	}

	private void assertLemma(String word, String expectedLemma) {
		Word analysis = analyzer.analyze(word);
		if (!analysis.isRecognized())
			System.err.printf("'%s' should be recognizable", word);
		assertTrue(analysis.isRecognized());
		Wordform forma = analysis.getBestWordform();
		assertEquals(expectedLemma, forma.getValue(AttributeNames.i_Lemma));
	}

	@SuppressWarnings("unused")
	private void describe(List<Wordform> formas) {
		PrintWriter izeja;
		try {
			izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
			for (Wordform forma : formas) {
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
			analyzer = new Analyzer("Latgalian.xml", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Before
	public void defaultsettings() {
		analyzer.defaultSettings();
		analyzer.setCacheSize(0);
		analyzer.clearCache();
	}

	//FIXME - jāpārtaisa uz parametrizētiem testiem...

	// Testi latgaliešu vārdu locīšanai atbilstoši http://genling.spbu.ru/baltist/Publicat/LatgVol1.pdf

	@Test
	public void idIntegrity() {
		// integritāte - vai nav dubulti numuri
		HashMap<Integer, Paradigm> vārdgrupuNr = new HashMap<Integer, Paradigm>();
		HashMap<Integer, Lexeme> leksēmuNr = new HashMap<Integer, Lexeme>();
		HashMap<Integer, Ending> galotņuNr = new HashMap<Integer, Ending>();

		for (Paradigm vārdgrupa : analyzer.paradigms) {
			if (vārdgrupuNr.get(vārdgrupa.getID()) != null)
				fail("Atkārtojas vārdgrupas nr " + vārdgrupa.getID());
			vārdgrupuNr.put(vārdgrupa.getID(), vārdgrupa);

			// Lexeme ID test gets triggered by Tēzaurs export, when Tēzaurs contains exception-form
			// Lexeme ID test also gets triggered when a single verb can have several verb types
			/*for (Lexeme leksēma : vārdgrupa.lexemes) {
				if (leksēmuNr.get(leksēma.getID()) != null) {
					leksēma.describe(new PrintWriter(System.err));
					leksēmuNr.get(leksēma.getID()).describe(new PrintWriter(System.err));
					fail(String.format("Atkārtojas leksēmas nr %d : '%s' un '%s'", leksēma.getID(), leksēma.getStem(0), leksēmuNr.get(leksēma.getID()).getStem(0)));
				}
				leksēmuNr.put(leksēma.getID(), leksēma);
			}//*/

			for (Ending ending : vārdgrupa.endings) {
				if (galotņuNr.get(ending.getID()) != null)
					fail("Atkārtojas galotnes nr " + ending.getID());
				galotņuNr.put(ending.getID(), ending);
			}
		}
	}

	@Test
	public void viejs() {
		List<Wordform> viejs = analyzer.generateInflections("viejs");
//        describe(viejs);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Vocative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflection(viejs, testset, "viej");
	}

	@Test
	public void ceļš() {
		List<Wordform> ceļš = analyzer.generateInflections("ceļš");
		//describe(ceļš);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(ceļš, testset, "celim");
	}

	@Test
	public void bruoļs() {
		List<Wordform> bruoļs = analyzer.generateInflections("bruoļs");
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(bruoļs, testset, "bruolim");
	}

	@Test
	public void pasauļs() {
		analyzer.enableGuessing = true;
		List<Wordform> pasauļs = analyzer.generateInflectionsFromParadigm("pasauļs", 4);
		//describe(pasauļs);
		assertTrue(pasauļs != null && !pasauļs.isEmpty());
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Accusative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflection(pasauļs, testset, "pasauli");
	}

	@Test
	public void kakis() {
		List<Wordform> kakis = analyzer.generateInflectionsFromParadigm("kakis", 5);
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		assertInflection(kakis, testset, "kaķūs");
	}

	@Test
	public void akmiņs() {
		List<Wordform> akmiņs = analyzer.generateInflections("akmiņs");
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflection(akmiņs, testset, "akminī");
	}

	@Test
	public void Jezus() {
		List<Wordform> Jezus = analyzer.generateInflections("Jezus");
		AttributeValues testset = new AttributeValues();
		testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		assertInflectionMultiple(Jezus, testset, new HashSet<String>(){{ add("Jezus"); add("Jeza");}});
	}

	@Test
	public void dekl1()
	{
		AttributeValues vsk_gen = new AttributeValues();
		vsk_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		vsk_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		vsk_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);

		List<Wordform> muosa = analyzer.generateInflectionsFromParadigm("tāvs", 1);
		assertInflection(muosa, vsk_gen, "tāva");

		List<Wordform> muote = analyzer.generateInflectionsFromParadigm("ols", 15);
		assertInflectionMultiple(muote, vsk_gen, new HashSet<String>(){{ add("ols"); add("ola");}});
	}


	@Test
	public void dekl4() {
		AttributeValues vsk_gen = new AttributeValues();
		vsk_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		vsk_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		vsk_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);

		List<Wordform> muosa = analyzer.generateInflectionsFromParadigm("muosa", 7);
		assertInflection(muosa, vsk_gen, "muosys");

		List<Wordform> kuoja = analyzer.generateInflectionsFromParadigm("kuoja", 8);
		assertInflection(kuoja, vsk_gen, "kuojis");

		List<Wordform> puika = analyzer.generateInflectionsFromParadigm("puika", 16);
		assertInflectionMultiple(puika, vsk_gen, new HashSet<String>(){{ add("puikys"); add("puikas");}});

		List<Wordform> bļuzņa = analyzer.generateInflectionsFromParadigm("bļuzņa", 30);
		assertInflectionMultiple(bļuzņa, vsk_gen, new HashSet<String>(){{ add("bļuznis"); add("bļuzņas");}});
	}

	@Test
	public void dekl5() {
		AttributeValues vsk_loc = new AttributeValues();
		vsk_loc.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		vsk_loc.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		vsk_loc.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
		AttributeValues dsk_gen = new AttributeValues();
		dsk_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		dsk_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		dsk_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);

		List<Wordform> muote = analyzer.generateInflectionsFromParadigm("muote", 9);
		assertInflectionMultiple(muote, vsk_loc, new HashSet<String>(){{ add("muotē"); add("muotie"); add("muotī");}});
		assertInflection(muote, dsk_gen, "muošu");

		List<Wordform> šaļte = analyzer.generateInflectionsFromParadigm("šaļte", 17);
		assertInflection(šaļte, dsk_gen, "šaļtu");
	}

	@Test
	public void dekl6()
	{
		AttributeValues dsk_gen = new AttributeValues();
		dsk_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		dsk_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		dsk_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);

		List<Wordform> sirds = analyzer.generateInflectionsFromParadigm("sirds", 11);
		assertInflection(sirds, dsk_gen, "siržu");

		List<Wordform> zūss = analyzer.generateInflectionsFromParadigm("zūss", 12);
		assertInflection(zūss, dsk_gen, "zūsu");
	}

	@Test
	public void adj()
	{
		AttributeValues sg_nom_masc_comp = new AttributeValues();
		sg_nom_masc_comp.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective);
		sg_nom_masc_comp.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		sg_nom_masc_comp.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
		sg_nom_masc_comp.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
		sg_nom_masc_comp.addAttribute(AttributeNames.i_Degree, AttributeNames.v_Comparative);

		AttributeValues sg_gen_fem_pos_indef = new AttributeValues();
		sg_gen_fem_pos_indef.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective);
		sg_gen_fem_pos_indef.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		sg_gen_fem_pos_indef.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		sg_gen_fem_pos_indef.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		sg_gen_fem_pos_indef.addAttribute(AttributeNames.i_Degree, AttributeNames.v_Positive);
		sg_gen_fem_pos_indef.addAttribute(AttributeNames.i_Definiteness, AttributeNames.v_Indefinite);

		List<Wordform> lobs = analyzer.generateInflectionsFromParadigm("lobs", 20);
		assertInflection(lobs, sg_nom_masc_comp, "lobuoks");
		assertInflectionMultiple(lobs, sg_gen_fem_pos_indef, new HashSet<String>(){{ add("lobys"); add("lobas");}});

		List<Wordform> agrys = analyzer.generateInflectionsFromParadigm("agrys", 21);
		assertInflection(agrys, sg_nom_masc_comp, "agruokys");
		assertInflectionMultiple(agrys, sg_gen_fem_pos_indef, new HashSet<String>(){{ add("agrys"); add("agras");}});

		List<Wordform> slapnis = analyzer.generateInflectionsFromParadigm("slapnis", 22);
		assertInflection(slapnis, sg_nom_masc_comp, "slapņuoks");
		assertInflectionMultiple(slapnis, sg_gen_fem_pos_indef, new HashSet<String>(){{ add("slapnis"); add("slapņas");}});
	}

	@Test
	public void numerals()
	{
		AttributeValues sg_masc_gen = new AttributeValues();
		sg_masc_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Numeral);
		sg_masc_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		sg_masc_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		sg_masc_gen.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);

		AttributeValues pl_masc_gen = new AttributeValues();
		pl_masc_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Numeral);
		pl_masc_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		pl_masc_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		pl_masc_gen.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);

		List<Wordform> pīci = analyzer.generateInflectionsFromParadigm("pīci", 25);
		assertInflection(pīci, pl_masc_gen, "pīcu");

		List<Wordform> trešs = analyzer.generateInflectionsFromParadigm("trešs", 26);
		assertInflection(trešs, sg_masc_gen, "treša");
		assertInflection(trešs, pl_masc_gen, "trešu");

		List<Wordform> pyrmais = analyzer.generateInflectionsFromParadigm("pyrmais", 27);
		assertInflection(pyrmais, sg_masc_gen, "pyrmuo");
		assertInflectionMultiple(pyrmais, pl_masc_gen, new HashSet<String>(){{ add("pyrmū"); add("pyrmūs");}});

	}

	@Test
	public void adv()
	{
		AttributeValues comp = new AttributeValues();
		comp.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adverb);
		comp.addAttribute(AttributeNames.i_Degree, AttributeNames.v_Comparative);

		List<Wordform> cīši = analyzer.generateInflectionsFromParadigm("cīši", 32);
		assertInflection(cīši, comp, "cīšuok");

		List<Wordform> slapni = analyzer.generateInflectionsFromParadigm("slapni", 33);
		assertInflection(slapni, comp, "slapņuok");

	}

	@Test
	public void prep()
	{
		AttributeValues prep = new AttributeValues();
		prep.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Preposition);

		List<Wordform> da = analyzer.generateInflectionsFromParadigm("da", 34);
		assertInflection(da, prep, "da");
	}

	@Test
	public void iuzys()
	{
		AttributeValues dsk_nom = new AttributeValues();
		dsk_nom.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		dsk_nom.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		dsk_nom.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);

		// Testing recognising plurare tantum
		List<Wordform> iuza_bad = analyzer.generateInflectionsFromParadigm("iuza", 7);
		assertInflection(iuza_bad, dsk_nom, "iuzys");

		AttributeValues plTan = new AttributeValues();
		plTan.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
		plTan.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);

		List<Wordform> forms = analyzer.generateInflectionsFromParadigm("iuzys", 7, plTan);
		for (Wordform form : forms) {
			assertFalse(form.isMatchingStrong(AttributeNames.i_Number, AttributeNames.v_Singular));
			assertTrue(form.isMatchingStrong(AttributeNames.i_Gender, AttributeNames.v_Feminine));
		}
		assertFalse(forms.isEmpty());

	}

	@Test
	public void valodasNormēšana()
	{
		// Tests, ka ir formas, kam ir norādīts `Valodas_normēšana="Ieteicams"`
		// Tests jāpamaina, ja mainās, kurām formām šo vajag.
		AttributeValues sg_fem_gen_rec = new AttributeValues();
		sg_fem_gen_rec.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		sg_fem_gen_rec.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		sg_fem_gen_rec.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		sg_fem_gen_rec.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		sg_fem_gen_rec.addAttribute(AttributeNames.i_Normative, AttributeNames.v_Recommended);

		List<Wordform> muosa = analyzer.generateInflectionsFromParadigm("muosa", 7);
		assertInflection(muosa, sg_fem_gen_rec, "muosys");

		// Tests, ka ir formas, kam ir norādīts `Valodas_normēšana="Nevēlams"`
		// Tests jāpamaina, ja mainās, kurām formām šo vajag.
		AttributeValues sg_fem_loc_und = new AttributeValues();
		sg_fem_loc_und.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		sg_fem_loc_und.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		sg_fem_loc_und.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
		sg_fem_loc_und.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		sg_fem_loc_und.addAttribute(AttributeNames.i_Normative, AttributeNames.v_Undesirable);

		List<Wordform> muote = analyzer.generateInflectionsFromParadigm("muote", 9);
		assertInflectionMultiple(muote, sg_fem_loc_und, new HashSet<String>(){{ add("muotie"); add("muotī");}});

	}

	@Test
	public void mijas() {
		AttributeValues vsk_gen = new AttributeValues();
		vsk_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		vsk_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		vsk_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
		AttributeValues vsk_acc = new AttributeValues();
		vsk_acc.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		vsk_acc.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
		vsk_acc.addAttribute(AttributeNames.i_Case, AttributeNames.v_Accusative);
		AttributeValues dsk_dat = new AttributeValues();
		dsk_dat.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		dsk_dat.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		dsk_dat.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);

		List<Wordform> kačs = analyzer.generateInflectionsFromParadigm("kačs", 4);
		assertInflection(kačs, vsk_gen, "kača");
		assertInflection(kačs, vsk_acc, "kači");
		assertInflection(kačs, dsk_dat, "kačim");
		List<Wordform> bruoļs = analyzer.generateInflectionsFromParadigm("bruoļs", 4);
		assertInflection(bruoļs, vsk_gen, "bruoļa");
		assertInflection(bruoļs, vsk_acc, "bruoli");
		assertInflection(bruoļs, dsk_dat, "bruolim");
		List<Wordform> vecs = analyzer.generateInflectionsFromParadigm("vecs", 4);
		assertInflection(vecs, vsk_gen, "veča");
		assertInflection(vecs, vsk_acc, "veci");
		assertInflection(vecs, dsk_dat, "večim");
		List<Wordform> bruoleits = analyzer.generateInflectionsFromParadigm("bruoleits", 4);
		assertInflection(bruoleits, vsk_gen, "bruoleiša");
		assertInflection(bruoleits, vsk_acc, "bruoleiti");
		assertInflection(bruoleits, dsk_dat, "bruoleišim");
		List<Wordform> eļksnis = analyzer.generateInflectionsFromParadigm("eļksnis", 5);
		assertInflection(eļksnis, vsk_gen, "eļkšņa");
		assertInflection(eļksnis, vsk_acc, "eļksni");
		assertInflection(eļksnis, dsk_dat, "eļkšnim");
	}

	@Test // Bugreport, ka varbanis nepaņem pareizo celmu un izloka
	public void vargani() {
		AttributeValues plTan = new AttributeValues();
		plTan.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);

		ArrayList<Wordform> vargani = analyzer.generateInflectionsFromParadigm("vargani", 1, "vargan", "", "", plTan);
//		describe(vargani);
		AttributeValues dsk_gen = new AttributeValues();
		dsk_gen.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
		dsk_gen.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
		dsk_gen.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
//		assertInflection(vargani, dsk_gen, "varganu");
	}

	@Test
	public void ticket_138() {
		ArrayList<Wordform> formas = analyzer.generateInflectionsFromParadigm("es", 29);
		describe(formas);
		assertTrue("Jābūt vairākām formām 'es' tabulai no hardcoded", formas.size()>4);
	}

}

