package lv.semti.morphology.Testi;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;
import lv.semti.morphology.lexicon.Paradigm;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JSONLexiconTest {
    @Test
    public void smoketest() {
        try {
            Analyzer analyzer_json = new Analyzer("Lexicon_v2.xml");
            Word cirvis = analyzer_json.analyze("cirvis");
            assertTrue(cirvis.isRecognized());
            assertEquals("ncmsn2", cirvis.getBestWordform().getTag());

            Word xyzzy = analyzer_json.analyze("xyzzy");
            assertFalse(xyzzy.isRecognized());

            List<Wordform> lasis = analyzer_json.generateInflections("lasis");
            for (Wordform wf : lasis) {
//                wf.describe();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void comparison() {
        try {
            Analyzer analyzer_xml = new Analyzer("Lexicon.xml", false);
            analyzer_xml.enableGuessing = false;
            Analyzer analyzer_json = new Analyzer("Lexicon_v2.xml");
            analyzer_json.enableGuessing = false;

            compare_lexicons(analyzer_xml, "lexicon", analyzer_json, "tēzaurs");
            compare_lexicons(analyzer_json, "tēzaurs", analyzer_xml, "lexicon");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compare_lexicons(Analyzer analyzer1, String name1, Analyzer analyzer2, String name2) {
        int total = 0;
        int not_found = 0;
        for (Paradigm p : analyzer1.paradigms) {
            for (Lexeme l : p.lexemes) {
                String lemma = l.getValue(AttributeNames.i_Lemma);
                Word j_word = analyzer2.analyzeLemma(lemma);
                total ++;
                if (!j_word.isRecognized()) {
//                    System.out.println(String.format("Word '%s' in %s but not in %s", lemma, name1, name2));
                    not_found ++;
                }
            }
        }
        System.out.println(String.format("%4.1f%% of %s words (%d / %d) not found in %s data", not_found*100.0/total, name1, not_found, total, name2));
    }
}
