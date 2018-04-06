package lv.semti.morphology.Testi;

import lv.semti.morphology.analyzer.Analyzer;
import org.junit.Test;

public class JSONLexiconTest {
    @Test
    public void smoketest() {
        try {
            Analyzer analyzer_json = new Analyzer("Lexicon_v2.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
