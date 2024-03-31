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

public class PhoneticTest {
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
            if (wf.isMatchingWeak(testset)) {
                if (!validForm.equalsIgnoreCase(wf.getToken())) {
                    System.err.printf("Found a different form");
                    wf.describe(new PrintWriter(System.err));
                }
                wf.setSAMPA();
                assertEquals(validForm, wf.getSAMPA());
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
            if (wf.isMatchingWeak(testset)) {
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
            analyzer = new Analyzer("Phonetic_v2.xml", false);
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

    // Testi SAMPA fonētisko pierakstu veidošanai balstoties uz Latgalian un Latvian paraugiem.

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
    public void tēls() {
        List<Wordform> tēls = analyzer.generateInflections("#tææls");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(tēls, testset, "#tEElix");
    }


    @Test
    public void leds() {
        List<Wordform> tēls = analyzer.generateInflections("#læts");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(tēls, testset, "#lEdix");
    }


    @Test
    public void lats() {
        List<Wordform> lats = analyzer.generateInflections("#lats");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(lats, testset, "#lats");

        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset2.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset2.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(lats, testset2, "#lattix");
    }

    @Test
    public void džouls() {
        List<Wordform> tēls = analyzer.generateInflections("#džɔuls");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(tēls, testset, "#dZOuluos");
    }

    @Test
    public void vējš() {
        List<Wordform> vējš = analyzer.generateInflections("#veei^š");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(vējš, testset, "#veei^S");

        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset2.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset2.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(vējš, testset2, "#veejaa");
    }

    @Test
    public void ēst() {
        List<Wordform> ēst = analyzer.generateInflections("#eest");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Naakotne);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset.addAttribute(AttributeNames.i_Person, "1");
        assertInflection(ēst, testset, "#eediiSux");

        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset2.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset2.addAttribute(AttributeNames.i_Person, "2");
        assertInflection(ēst, testset2, "#eed");

        AttributeValues testset3 = new AttributeValues();
        testset3.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset3.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset3.addAttribute(AttributeNames.i_Person, "3");
        testset3.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        testset3.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
        assertInflection(ēst, testset3, "#ne-EEd");

        AttributeValues testset4 = new AttributeValues();
        testset4.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset4.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset4.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Atstaastiijuma);
//        testset4.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        assertInflection(ēst, testset4, "#EEduot");

        AttributeValues testset5 = new AttributeValues();
        testset5.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Participle);
        testset5.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
        testset5.addAttribute(AttributeNames.i_Voice, AttributeNames.v_Passive);
        testset5.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Pagaatne);
        testset5.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset5.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        assertInflection(ēst, testset5, "#ne-EEstax");
    }


    @Test
    public void lekt() {
        List<Wordform> lekt = analyzer.generateInflections("#lekt");


        AttributeValues testset5 = new AttributeValues();
        testset5.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset5.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Participle);
        testset5.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset5.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset5.addAttribute(AttributeNames.i_Voice, AttributeNames.v_Active);
        testset5.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Pagaatne);
        testset5.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
        testset5.addAttribute(AttributeNames.i_Definiteness, AttributeNames.v_Indefinite);
        testset5.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        assertInflection(lekt, testset5, "#ne-lEkkuSax");
    }

    @Test
    public void pelt() {
        List<Wordform> tēls = analyzer.generateInflections("#pelt");

        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset2.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset2.addAttribute(AttributeNames.i_Person, "2");
        assertInflection(tēls, testset2, "#pel");


        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset.addAttribute(AttributeNames.i_Person, "1");
        assertInflection(tēls, testset, "#peLux");
    }


    @Test
    public void asimilācija() {
        List<Wordform> augs = analyzer.generateInflections("#auqks");

        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(augs, testset, "#auqguos");

        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset2.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset2.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(augs, testset2, "#auqks");

        List<Wordform> apģērbs = analyzer.generateInflections("#abģæærps");
        AttributeValues testset3 = new AttributeValues();
        testset3.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset3.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset3.addAttribute(AttributeNames.i_Case, AttributeNames.v_Accusative);
        testset3.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(apģērbs, testset3, "#abGEErbux");
    }
}