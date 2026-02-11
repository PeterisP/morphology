package lv.semti.morphology.analyzer;

import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.StemType;

public class GenerationBlacklist {
    public static boolean blacklist(Wordform wordform) {
        if (wordform.lexeme.getStem(StemType.STEM1).endsWith("vajadzē") && wordform.lexeme.getParadigm().getName().equalsIgnoreCase("verb-3b")) {
            if (wordform.isMatchingStrong(AttributeNames.i_Person, "1") ||
                    wordform.isMatchingStrong(AttributeNames.i_Person, "2"))
                return true;
        }
        return false;
    }
}
