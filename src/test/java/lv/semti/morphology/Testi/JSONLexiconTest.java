package lv.semti.morphology.Testi;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
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
}
