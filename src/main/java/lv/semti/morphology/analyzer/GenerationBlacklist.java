package lv.semti.morphology.analyzer;

import lv.semti.morphology.attributes.AttributeNames;

public class GenerationBlacklist {
    public static boolean blacklist(Wordform wordform) {
        if (wordform.lexeme.getStem(0).endsWith("vajadzē") && wordform.lexeme.getParadigm().getID()==45) {
            if (wordform.isMatchingStrong(AttributeNames.i_Person, "1") ||
                    wordform.isMatchingStrong(AttributeNames.i_Person, "2"))
                return true;
        }
        return false;
    }
}
