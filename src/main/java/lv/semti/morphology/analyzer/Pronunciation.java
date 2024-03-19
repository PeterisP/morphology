package lv.semti.morphology.analyzer;

import lv.semti.morphology.attributes.AttributeNames;

import java.util.*;
import java.util.regex.Pattern;

public class Pronunciation {

    private static Map<Character, Character> SAMPA = new HashMap<>();
    static {
        SAMPA.put('æ', 'E'); // Platais e
        SAMPA.put('ɔ', 'O');
        SAMPA.put('š', 'S');
        SAMPA.put('ž', 'Z');
        SAMPA.put('ķ', 'K');
        SAMPA.put('ģ', 'G');
        SAMPA.put('ļ', 'L');
        SAMPA.put('ņ', 'J');
        SAMPA.put('ŋ', 'N');
    }

    private static Pattern patskaņi = Pattern.compile("([aeiuoæɔ])\1[qx=]? | ([aeuɔi][iu]|uo|ie)[qx=]? | [iu]\\^");
    private static Pattern līdzskaņi = Pattern.compile("[lļrnņŋm][qx=]? | ([bcčdfgģhjkķlļmnŋņprsštvzž])\1? | dd?[zž] | t[cč]");


    public static String voiceAssimilation(String[] sounds, Character change) {
        // Līdzskaņu balsīguma asimilācija, pārbaudi veicam no vārda beigām
        // TODO: Salīdzināt ar fonētikas likumiem.
        String previous = sounds[sounds.length-1];
        if (change != null) previous = String.valueOf(change);

        for (int counter = sounds.length - 1; counter >= 0; counter--) {
            if (previous.matches("([ptkķfsš]).*")) {
                sounds[counter] = sounds[counter].replaceAll("b", "p");
                sounds[counter] = sounds[counter].replaceAll("d", "t");
                sounds[counter] = sounds[counter].replaceAll("ģ", "ķ");
                sounds[counter] = sounds[counter].replaceAll("g", "k");
                sounds[counter] = sounds[counter].replaceAll("z", "s");
                sounds[counter] = sounds[counter].replaceAll("ž", "š");
                sounds[counter] = sounds[counter].replaceAll("v", "f");
            } else if (previous.matches("([bdģgvzž]).*")) {
                sounds[counter] = sounds[counter].replaceAll("p", "b");
                sounds[counter] = sounds[counter].replaceAll("t", "d");
                sounds[counter] = sounds[counter].replaceAll("ķ", "ģ");
                sounds[counter] = sounds[counter].replaceAll("k", "g");
                sounds[counter] = sounds[counter].replaceAll("s", "z");
                sounds[counter] = sounds[counter].replaceAll("š", "ž");
                sounds[counter] = sounds[counter].replaceAll("f", "v");
            }
            previous = sounds[counter];
        }
        return String.join("", sounds);
    }



    public static String applyPhonotactics(String sampa) {
        // Atdalām priedēkļus
        String result = "";
        if (sampa.startsWith("ne") || sampa.startsWith("jā")) {
            result = sampa.substring(0,2);
            sampa = sampa.substring(2,sampa.length());
        }

        // Vārdu saknes mijas
        sampa = sampa.replaceAll("ā", "aa");
        sampa = sampa.replaceAll("ī", "ii");
        sampa = sampa.replaceAll("ū", "uu");
        sampa = sampa.replaceAll("ē", "ee");

        // Vokalizēto līdzskaņu atpakaļ pārveidošana pirms patskaņiem
        sampa = sampa.replaceAll("i\\^(?=[aeiuoæɔ])", "j");
        sampa = sampa.replaceAll("u\\^(?=[aeiuoæɔ])", "v");

        String[] sounds = sampa.split("");
        sampa = voiceAssimilation(sounds, null);


        // Otrās zilbes nebalsīgā līdzskaņa dubultošana
        // TODO: Pārbaudām vai nav norādīts uzsvars uz ne-pirmās zilbes.
        // FIXME: Izstrādāt vispārīgam gadījumam.
        if (!sampa.contains("%") || sounds[0].startsWith("%")) {
            int zilbe = 0;
            int līdzskaņi = 0;
            boolean geminate = false;
            for (int counter = 0; counter < sounds.length; counter++) {
                if (sounds[counter].matches("^[aeæioɔu]+[xq\\^=]?")) {
                    if (zilbe==0 && sounds[counter].matches("^[aeæioɔu][xq\\^=]?")) geminate = true;
                    if (zilbe==1 && geminate & līdzskaņi==1) {
                        sounds[counter-1] = sounds[counter-1]+sounds[counter-1];
                    }
                    zilbe++;
                } else if (sounds[counter].matches("^[ptķksšf]$") && geminate) {
                    līdzskaņi++;
                } else geminate = false;
            }
        }

        result = result + String.join("", sounds);
        return result;
    }


    public static String processAffixes(String lowercase) {
        // TODO: Tehniski priedēkli "ne-" daudzi izrunā kā "næ-", ja seko æ.
        // Bet es pašlaik neesmu (piemēram šajā vārdā) pārliecināts kā to apstrādāt
        lowercase = lowercase.replaceFirst("ne#", "#ne-");
        lowercase = lowercase.replaceFirst("jā#", "#jaa-");
        lowercase = lowercase.replaceFirst("vis#", "#vis");
        return lowercase;
    }


    // locījums.getToken() atgriež lemmas fonētisko izrunu, kas ir apstrādāta izmantojot ortogrāfijas locīšanas likumus.
    // pāris gramatiskos e/æ likumus.
    public static String toSAMPA(Wordform locījums) {
        String phonetic = locījums.getToken();
        String ending = locījums.getEnding().getEnding();

        String orthoStem = locījums.lexeme.getValue(AttributeNames.i_PhonoStem);
        if (orthoStem == null) orthoStem = phonetic.substring(0, phonetic.length()-ending.length());
        phonetic = orthoStem + ending;

        phonetic = applyPhonotactics(phonetic);
        phonetic = processAffixes(phonetic);
        StringBuilder sb = new StringBuilder(phonetic.length());
        for(int i = 0; i< phonetic.length(); ++i) {
            char currentChar = phonetic.charAt(i);
            sb.append(SAMPA.getOrDefault(currentChar, currentChar));
        }
        return sb.toString();
    }


    public static String restoreStem(String base, String ortho) {
        String[] sounds = base.split("");
        Character change = ortho.charAt(ortho.length()-1);
        return voiceAssimilation(sounds, change);
    }
}
