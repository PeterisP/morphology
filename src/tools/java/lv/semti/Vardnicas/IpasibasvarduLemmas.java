package lv.semti.Vardnicas;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class IpasibasvarduLemmas {
    public static void main(String[] args) throws Exception {
        Analyzer analyzer = new Analyzer(false);
        analyzer.enableGuessing = true;
        PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));

        BufferedReader ieeja;
        String rinda;
        ieeja = new BufferedReader(
                new InputStreamReader(IpasibasvarduLemmas.class.getClassLoader().getResourceAsStream("all.txt"), "UTF-8"));
        // NB - all.txt needs to be in src/main/resources not src/test/resources for this to work

        Set<String> set = new HashSet<String>();

        while ((rinda = ieeja.readLine()) != null) {
            if (rinda.contains("<s>") || rinda.contains("</s>") || rinda.equalsIgnoreCase("<g />") || rinda.isEmpty()) continue;
            String[] parse = rinda.split("\t");
            String wordform = parse[0];
            String tag = parse[1];
            String lemma = parse[2];

            if (tag.matches("a.f....") || tag.matches("m..f..FIXME skaitļa vārdiem neiet")) {
                Wordform wf = analyzer.analyzeLemma(lemma).getBestWordform();
//                w.describe(izeja);
                if (wf != null) {
                    set.add(String.format("%s\t%s\n", lemma, wf.getValue(AttributeNames.i_SourceLemma)));
                } else {
//                    izeja.println(lemma);
                }
            }
        }
        ieeja.close();

        for (String s : set) {
            izeja.print(s);
        }

        izeja.println("Done!");
        izeja.flush();
    }

}