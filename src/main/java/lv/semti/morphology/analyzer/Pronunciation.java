package lv.semti.morphology.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pronunciation {

    private static Map<Character, Character> REPLACEMENTS = new HashMap<>();
    static {
        REPLACEMENTS.put('æ', 'E');
        REPLACEMENTS.put('ɔ', 'O');
        REPLACEMENTS.put('š', 'S');
        REPLACEMENTS.put('ž', 'Z');
        REPLACEMENTS.put('ķ', 'K');
        REPLACEMENTS.put('ģ', 'G');
        REPLACEMENTS.put('ļ', 'L');
        REPLACEMENTS.put('ņ', 'J');
        REPLACEMENTS.put('ŋ', 'N');
    }

    private static List<Character> voiceless = new ArrayList<>();



    public static String processAffixes(String lowercase) {
        // TODO: Tehniski priedēkli "ne-" daudzi izrunā kā "næ-", ja seko æ.
        // Bet es pašlaik neesmu (piemēram šajā vārdā) pārliecināts kā to apstrādāt
        lowercase = lowercase.replaceFirst("ne", "n e ");
        lowercase = lowercase.replaceFirst("jā", "j aa ");
        return lowercase;
    }


    public static String toSAMPA(String lowercase) {
        if (lowercase.endsWith(" ")) lowercase = lowercase.substring(0, lowercase.length()-1);
        lowercase = processAffixes(lowercase);
        StringBuilder sb = new StringBuilder(lowercase.length());
        for(int i = 0;i<lowercase.length();++i) {
            char currentChar = lowercase.charAt(i);
            sb.append(REPLACEMENTS.getOrDefault(currentChar, currentChar));
        }
        return sb.toString();
    }

    public static String applyPhonotactics(String sampa) {

        return sampa;
    }
}
