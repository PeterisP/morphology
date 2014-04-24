package lv.semti.Vardnicas;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

public class ThesaurusImport {

	/**
	 * 
	 * @param args File name expected as first argument.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//PrintWriter output = new PrintWriter(new PrintStream(System.out, true, "UTF-8"));
		
		Analyzer analizators = new Analyzer("dist/Lexicon.xml");
		analizators.guessNouns = true;
		analizators.guessParticiples = false;
		analizators.guessVerbs = false;
		analizators.guessAdjectives = false;
		analizators.enableDiminutive = false;
		analizators.enablePrefixes = false;
		analizators.enableGuessing = false;
		analizators.meklētsalikteņus = false;
		analizators.guessInflexibleNouns = true;
		analizators.setCacheSize(0);
		//ThesaurusEntry.analyzer = analizators;
		
		//String thesaurusFile = "/Users/pet/Dropbox/Resursi/Tezaurs/Skaidrojosa Vardnica.xml";
		String thesaurusFile = args[0];
		String goodOutputFile = "tezaurs-good.json";
		String badOutputFile = "tezaurs-bad.json";
		//if (args.length > 1) outputFile = args[1];
		
		// Load Thesaurus file.
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = docBuilder.parse(new File(thesaurusFile));
		Node node = doc.getDocumentElement();
		if (!node.getNodeName().equalsIgnoreCase("tezaurs"))
			throw new Error("Node '" + node.getNodeName() + "' but tezaurs expected!");
		
		//List<ThesaurusEntry> entries = new LinkedList<ThesaurusEntry>();
		
		// Output.
		BufferedWriter goodOut = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(goodOutputFile), "UTF-8"));
		BufferedWriter badOut = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(badOutputFile), "UTF-8"));
		
		// Process each node.
		NodeList thesaurusEntries = node.getChildNodes(); // Thesaurus entries
		int badCount = 0;
		for (int i = 0; i < thesaurusEntries.getLength(); i++)
		{
			Node sNode = thesaurusEntries.item(i);
			if (sNode.getNodeName().equals("s"))
			{
				ThesaurusEntry entry = new ThesaurusEntry(sNode);
				if (!entry.inBlacklist() &&
					(entry.sources == null || entry.sources.s.contains("LLVV"))) //TODO - temporary restriction, focus on LLVV first
				{
					//entries.add(entry);
					if (entry.hasParadigm() && !entry.hasUnparsedGram())
						goodOut.write(entry.toJSON() + "\n");
					else
					{
						badOut.write(entry.toJSON() + "\n");
						badCount++;
					}
				}
			}
			else if (!sNode.getNodeName().equals("#text")) // Text nodes here are ignored.
			{
				goodOut.close();
				badOut.close();				
				throw new Error("Node '" + sNode.getNodeName() + "' but s (šķirklis) expected!");
			}
			if (badCount >= 1000) break;	//Temporary.
		}
		
		goodOut.close();
		badOut.close();
		
		//count_gram(entries); //FIXME - te nečeko papildus gram info (piem, v.; novec. nerāda jo tas jau ir atpazīts...
		
		/*
		BufferedReader ieeja;
		String vārds;
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("/Users/pet/Dropbox/Resursi/T/bezatstarpju visi.txt"), "UTF-8"));
		
		int i = 0;
		while ((vārds = ieeja.readLine()) != null) {
			i++;
			//if (i>10000) break;
			
			vārds = vārds.trim().toLowerCase();
			String Vārds = vārds.substring(0, 1).toUpperCase() + vārds.substring(1,vārds.length());
			Word w = analizators.analyzeLemma(vārds);
			
			if (irLeksikonā(w)) {
				//izeja.println("Vārds '" + w.getToken() + "' jau ir leksikonā!");
			} else {
				
				w = analizators.guessByEnding(vārds);

				izmestNepareizāsParadigmas(w);
				
				AttributeValues filtrs_vsk = new AttributeValues();
				filtrs_vsk.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);

				AttributeValues filtrs_3 = new AttributeValues();
				filtrs_3.addAttribute(AttributeNames.i_ParadigmID, "3");
				
				AttributeValues filtrs_1 = new AttributeValues();
				filtrs_1.addAttribute(AttributeNames.i_ParadigmID, "1");
				
				boolean irVienskaitlis = false;
				for (Wordform wf : w.wordforms) {
					if (wf.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Singular)) irVienskaitlis = true;
				}
				if (irVienskaitlis)	w.filterByAttributes(filtrs_vsk);
				else {
					if (vārds.endsWith("ši") || vārds.endsWith("ži") || vārds.endsWith("či") || vārds.endsWith("šļi") || vārds.endsWith("žļi") || vārds.endsWith("ži") ||
							vārds.endsWith("ņi") || vārds.endsWith("pji") || vārds.endsWith("pji") || vārds.endsWith("bji") || vārds.endsWith("mji") || vārds.endsWith("vji"))
						w.filterByAttributes(filtrs_3);
					else if (vārds.endsWith("i"))
						w.filterByAttributes(filtrs_1);
				}
				
				if (w.wordforms.size() == 0) {					
					if (vārds.endsWith("o") || vārds.endsWith("ē")) {
						Lexeme jaunais = analizators.createLexeme(vārds, 111, source); // Nelokāmie lietvārdi
						jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
						jaunais.addAttribute(AttributeNames.i_Lemma, Vārds);
						//izeja.println("Pielikām leksikonam vārdu '" + w.getVārds() +"'");
						//jaunais.describe(izeja);
						//analizators.analyze(vārds).Aprakstīt(izeja);						
					} else {
						izeja.println("Neuzminējās varianti '" + w.getToken() +"'!");					
					}
				} else if (w.wordforms.size() == 1) {					
					Lexeme jaunais = analizators.createLexeme(vārds, w.wordforms.get(0).getEnding().getID(),source);
					jaunais.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
					jaunais.addAttribute(AttributeNames.i_Lemma, Vārds);
					if (w.wordforms.get(0).isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Plural)) {
						jaunais.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
					}
					//izeja.println("Pielikām leksikonam vārdu '" + w.getToken() +"'");
					//w.print(izeja);
					//jaunais.describe(izeja);
					//analizators.analyze(vārds).Aprakstīt(izeja);
				} else {
					izeja.println("tipa dereetu pielikt leksikonam vārdu '" + w.getToken() +"' bet ir vairāki varianti");
					w.print(izeja);
				}
			}
			izeja.flush();
		}
					
		izeja.flush();
		analizators.toXML_sub("Lexicon_onomastica.xml", source);
		*/
	}

/*	private static void countGram(List<ThesaurusEntry> entries) {
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		for (ThesaurusEntry entry : entries) {
			//if (entry.getParadigm() != 0) continue; // counting only those we don't understand
			//String key = entry.original_gram + "\t" + entry.gram;
			String key = entry.gram;
			Integer count = counter.get(key);
			if (count == null) count = 0;
			count+=1;
			counter.put(key, count);
		}
		
		for (Entry<String, Integer> count : counter.entrySet()) {
			if (count.getValue() > 100) // arbitrary cutoff to show important stuff 
				System.out.printf("%s:\t%d\n", count.getKey(), count.getValue());
		}
	}//*/

	/**
	 * Remove rare paradigms that should not be guessed.
	 */
	private static void removeRareParadigms(Word w) {
		LinkedList<Wordform> forRemoval = new LinkedList<Wordform>();
		for (Wordform wf : w.wordforms) {
			if (wf.getValue(AttributeNames.i_ParadigmID).equals("4") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("5") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("8") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("10") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("11") ||
				wf.getValue(AttributeNames.i_ParadigmID).equals("12") ||				
				!wf.isMatchingWeak(AttributeNames.i_Case, AttributeNames.v_Nominative))
			{
					forRemoval.add(wf);
			}
			
		}
		for (Wordform removeMe : forRemoval)
			w.wordforms.remove(removeMe);
	}
	// No-one remembers the motivation.
	private static void removePlurals(Word w) {
		LinkedList<Wordform> forRemoval = new LinkedList<Wordform>();
		for (Wordform wf : w.wordforms) {
			if (!wf.isMatchingWeak(AttributeNames.i_Number, AttributeNames.v_Singular))
					forRemoval.add(wf);
		}
		for (Wordform removeMe : forRemoval)
			w.wordforms.remove(removeMe);
	}

	private static boolean isInLexicon(Word w) {
		for (Wordform wf : w.wordforms) {
			if (wf.isMatchingWeak(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun) ||
				wf.isMatchingWeak(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adjective))
				return true;
		}
		return false;
	}

}
