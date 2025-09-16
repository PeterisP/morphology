package lv.semti.demo;

import java.io.*;
import java.util.*;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.Lexeme;
import lv.semti.morphology.lexicon.Paradigm;

public class MorphDemo {

	private void analyze(Analyzer morph, String fin, String fout) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fin), "UTF-8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout), "UTF-8"));

		String line = null;
		while ((line = in.readLine()) != null) {
			String[] sentence = line.split(" ");

			for (String word : sentence) {
				// Get the potentially ambiguous results of analysis for the current word form. 
				ArrayList<Wordform> proposals = morph.analyze(word).wordforms;

				if (proposals.size() > 0) {
					for (Wordform form : proposals) {
						// Get the base form and the list of morphological features for the given word form.
						// Positions in the list (tag) are described in doc/TagSet.pdf
						// All the features can be acquired also individually - by calling Wordform.getValue(AttributeNames.i_xxx).
						out.write(word + "\t"
								+ form.getValue(AttributeNames.i_Lemma).toUpperCase()
								+ "\t[" + form.getValue(AttributeNames.i_PartOfSpeech)
								+ ", " + form.getTag() + "]");
						out.newLine();
					}
				} else {
					out.write(word + "\t" + "UNRECOGNIZED");
					out.newLine();
				}

				out.newLine();
			}
		}

		out.close();
		in.close();
	}
	
	private void add(Analyzer morph, String fin, String fout) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fin), "UTF-8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout), "UTF-8"));
		
		String lemma = null;
		while ((lemma = in.readLine()) != null) {
			lemma = lemma.trim();
			morph.analyze(lemma);
			
			System.out.println("\n" + lemma.toUpperCase());
			for (Paradigm p : morph.paradigms) {
				try {
					if (lemma.endsWith(p.getLemmaEnding().getEnding()) && p.getID() <= 20) {
						// 1-12: noun groups
						// 13-14: adjective groups
						// 15-20: verb groups (normally, only 2nd vs. 3rd conjugation should be considered)
						System.out.println("\t" + p.getID() + ": " + p.getName());
					}
				} catch (NullPointerException e) {
					// FIXME: The NullPointerException should not appear, but it does...
				}
			}
			
			BufferedReader prompt = new BufferedReader(new InputStreamReader(System.in));
			// See http://valoda.ailab.lv/latval/vidusskolai/morfol/
			System.out.print("Select the correct word group (or 0, if none): ");
			int group_id = Integer.parseInt(prompt.readLine());
			
			String attribute = null;
			String[] value = new String[2];
			int feature_id = 0;
			
			if (group_id > 0) {
				if (1 <= group_id && group_id <= 12) {
					System.out.println(AttributeNames.i_NounType);
					System.out.println("\t0: " + AttributeNames.v_CommonNoun);
					System.out.println("\t1: " + AttributeNames.v_ProperNoun);
					attribute = AttributeNames.i_NounType;
					value[0] = AttributeNames.v_CommonNoun;
					value[1] = AttributeNames.v_ProperNoun;
				}
				else if (13 <= group_id && group_id <=14) {
					System.out.println(AttributeNames.i_AdjectiveType);
					System.out.println("\t0: " + AttributeNames.v_QualificativeAdjective);
					System.out.println("\t1: " + AttributeNames.v_RelativeAdjective);
					attribute = AttributeNames.i_AdjectiveType;
					value[0] = AttributeNames.v_QualificativeAdjective;
					value[1] = AttributeNames.v_RelativeAdjective;
				}
				else if (15 <= group_id && group_id <= 20) {
					System.out.println(AttributeNames.i_Transitivity);
					System.out.println("\t0: " + AttributeNames.v_Transitive);
					System.out.println("\t1: " + AttributeNames.v_Intransitive);
					attribute = AttributeNames.i_Transitivity;
					value[0] = AttributeNames.v_Transitive;
					value[1] = AttributeNames.v_Intransitive;
				}
				
				System.out.print("Choose the appropriate value the extra attribute: ");
				feature_id = Integer.parseInt(prompt.readLine());
				
				// Checks whether the given lexeme is not already added to the lexicon.
				// TODO: In the case of nouns, the attribute NounType should be taken into account as well.
				String ending = morph.paradigmByID(group_id).getLemmaEnding().getEnding();
				String stem = lemma.toLowerCase().substring(0, lemma.length() - ending.length());
				ArrayList<Lexeme> duplicates = morph.paradigmByID(group_id).getLexemesByStem().get(0).get(stem);

				if (duplicates == null || duplicates.size() == 0) {
					Lexeme new_lexeme = morph.createLexeme(
						lemma.toLowerCase(), 									// Base form
						//morph.paradigmByID(group_id).getLemmaEnding().getID(),	// ID of the ending in the inflectional paradigm
						morph.paradigmByID(group_id).getLemmaEnding(),	// ID of the ending in the inflectional paradigm
						"Demo (" + new Date() + ")"								// Source
					);
					// P.S. A java.lang.Error is thrown, if the provided group_id does not match with any of the suggested word groups.
					// Typically, the compliance is automatically ensured via GUI (e.g., by using JComboBox).
					
					
					// Add some additional attributes.
					new_lexeme.addAttribute(attribute, value[feature_id]);
					if (15 <= group_id && group_id <= 20) {
						// In the normal case, only main verbs should be added by third parties.
						new_lexeme.addAttribute(AttributeNames.i_VerbType, AttributeNames.v_MainVerb);
					}
					
					out.write(lemma + "\t" + group_id + "\t" + value[feature_id]);
				} else {
					System.out.println("Such entry is already present in the lexicon");
					out.write(lemma + "\tDUPLICATE");
				}
			}
			
			out.newLine();
		}

		out.close();
		in.close();
	}
	
	public static void main(String[] args) throws Exception {
		// Create an instance of the analyzer.
		Analyzer morph = new Analyzer();
		
		// Derivation rules for the diminutive forms.
		morph.enableDiminutive = true;
		// Derivation rules for the prefixed forms.
		morph.enablePrefixes = true;
		// Consider vocative cases.
		morph.enableVocative = false;
		// Try to guess unknown words that are not covered by the lexicon and the rule set.
		// This is not recommended currently - guessing is too robust for practical use.
		morph.enableGuessing = false;

		MorphDemo demo = new MorphDemo();
		if (args[1].equals("analyze")) {
			// Morphological analysis.
			demo.analyze(morph, args[2], args[3]);
		}
		if (args[1].equals("add")) {
			// Extension of the lexicon.
			// TODO: syncronization in the case of a multi-user application.
			demo.add(morph, args[2], args[3]);

			System.out.println("\nSaving the lexicon...");
			if (args[0].equals("file")) {
				morph.toXML("lib/lexicon.xml");
			}
			if (args[0].equals("stream")) {
				File file = new File("lib/lexicon.xml");
				File new_file = new File("lib/lexicon.xml.new");
				File bak_file = new File("lib/lexicon.xml.bak");
				
				OutputStream stream = new FileOutputStream(new_file);
				morph.toXML(stream);
				stream.close();
				
				if (bak_file.exists()) bak_file.delete();
				if (file.exists()) file.renameTo(bak_file);
				new_file.renameTo(file);
			}
		}
	}

}
