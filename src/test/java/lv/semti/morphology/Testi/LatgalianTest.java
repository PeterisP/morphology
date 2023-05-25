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

    private void assertInflectionMultiple(List<Wordform> forms, AttributeValues testset, Set<String> validForms) {
        HashSet<String> foundCorrect = new HashSet<>();
        HashSet<String> foundOther = new HashSet<>();
        for (Wordform wf : forms) {
            if (wf.isMatchingWeak(testset)) {
                if (validForms.contains(wf.getToken())) foundCorrect.add(wf.getToken());
                else foundOther.add(wf.getToken());
            }
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
    public void tāvs() {
        List<Wordform> tāvs = analyzer.generateInflections("tāvs");
//        describe(tāvs);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(tāvs, testset, "tāva");
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
        describe(ceļš);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(ceļš, testset, "celim");
    }

    @Test
    public void bruoļs() {
        List<Wordform> bruoļs = analyzer.generateInflections("bruoļs");
        describe(bruoļs);
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
        describe(pasauļs);
        assertTrue(pasauļs != null && !pasauļs.isEmpty());
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Accusative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(pasauļs, testset, "pasauli");
    }

    @Test
    public void kakis() {
        List<Wordform> kakis = analyzer.generateInflections("kakis");
        describe(kakis);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        assertInflection(kakis, testset, "kaķūs");
    }

    @Test
    public void akmiņs() {
        List<Wordform> akmiņs = analyzer.generateInflections("akmiņs");
        describe(akmiņs);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Locative);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflection(akmiņs, testset, "akminī");
    }

    @Test
    public void Jezus() {
        List<Wordform> Jezus = analyzer.generateInflections("Jezus");
        describe(Jezus);
        AttributeValues testset = new AttributeValues();
        testset.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        testset.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
        testset.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        assertInflectionMultiple(Jezus, testset, new HashSet<String>(){{ add("Jezus"); add("Jeza");}});
    }

    // TODO izņemt liekos describe
}

