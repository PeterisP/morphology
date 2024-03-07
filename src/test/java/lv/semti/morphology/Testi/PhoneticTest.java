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
            analyzer = new Analyzer("Phonetic.xml", false);
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
        List<Wordform> tēls = analyzer.generateInflections("t ææ l s");
//        describe(viejs);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(tēls, testset, "t EE l ix");
    }

    @Test
    public void džouls() {
        List<Wordform> tēls = analyzer.generateInflections("dž ɔu l s");
//        describe(viejs);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(tēls, testset, "dZ Ou l uo s");
    }

    @Test
    public void ēst() {
        List<Wordform> tēls = analyzer.generateInflections("ee s t");
//        describe(viejs);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset.addAttribute(AttributeNames.i_Person, "1");
        assertInflection(tēls, testset, "EE d ux");

        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset2.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset2.addAttribute(AttributeNames.i_Person, "2");
        assertInflection(tēls, testset2, "ee d");

        AttributeValues testset3 = new AttributeValues();
        testset3.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset3.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset3.addAttribute(AttributeNames.i_Person, "3");
        testset3.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        assertInflection(tēls, testset3, "n e EE d");
    }

    @Test
    public void pelt() {
        List<Wordform> tēls = analyzer.generateInflections("p e l t");
//        describe(viejs);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset.addAttribute(AttributeNames.i_Person, "1");
        assertInflection(tēls, testset, "p e L ux");

        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        testset2.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        testset2.addAttribute(AttributeNames.i_Person, "2");
        assertInflection(tēls, testset2, "p e l");
    }


    @Test
    public void intonācijas() {
        List<Wordform> stiepta = analyzer.generateInflections("z aa= l ex");
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(stiepta, testset, "z aa= l ee s");

        List<Wordform> lauzta = analyzer.generateInflections("z aaq l ex");
        AttributeValues testset2 = new AttributeValues();
        testset2.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset2.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
        testset2.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset2.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(lauzta, testset2, "z aaq l ee s");
    }
}