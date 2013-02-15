/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia;
 * Author: Pēteris Paikens
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.semti.morphology.analyzer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import org.json.simple.JSONValue;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.corpus.Statistics;

/**
 * Morphologically analyzed token with potentially multiple variants of
 * analysis.
 * 
 * @author Pēteris Paikens
 */
public class Word implements Cloneable{

	private String token;
	public ArrayList<Wordform> wordforms = new ArrayList<Wordform>();
	//FIXME - derētu jau privāts
	private AbstractTableModel tableModel = null;
	//TODO - moš korektāk ar listeneriem (notifaijeriem?!)
	private Wordform correctWordform = null;

	public Word (String token) {
		this.token = token.trim();
		this.wordforms = new ArrayList<Wordform>(1);
	}

	public Word(Node node) {
		if (node.getNodeName().equalsIgnoreCase("Vārds")) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n.getNodeName().equalsIgnoreCase("Vārdforma"))
					wordforms.add(new Wordform(n));
			}

			Node n = node.getAttributes().getNamedItem("vārds");
			if (n != null)
				token = n.getTextContent();
			n = node.getAttributes().getNamedItem("pareizāVārdforma");
			if (n != null)
				setCorrectWordform(wordforms.get(Integer.parseInt(n.getTextContent())));

		} else if (node.getNodeName().equalsIgnoreCase("Vārdforma")) {
			token = node.getAttributes().getNamedItem("vārds").getTextContent();
			wordforms.add(new Wordform(node));
		} else throw new Error("Node " + node.getNodeName() + " nav ne Vārds, ne Vārdforma");
	}

	public void setTableModel(AbstractTableModel model) {
		this.tableModel = model;
	}

	@Override
	public String toString() {
		return token;
	}

	@Override
	public Object clone() {
		try {
			Word kopija = (Word)super.clone();
			kopija.token = this.token;
			kopija.wordforms = new ArrayList<Wordform>();
			for (Wordform vārdforma : wordforms) {
				Wordform klons = (Wordform) vārdforma.clone();
				kopija.wordforms.add(klons);
				if (this.getCorrectWordform() == vārdforma)
					kopija.setCorrectWordform(klons);
			}
			return kopija;
        } catch (CloneNotSupportedException e) {
            throw new Error("Gļuks - nu vajag varēt klasi Vārds noklonēt.");
        }
	}
	
	@Override
	public boolean equals(Object o)
	{
		try
		{
			Word w = (Word)o;
			if (token == null ^ w.token == null
					|| wordforms == null ^ w.wordforms == null
					|| correctWordform == null ^ w.correctWordform == null) return false;
			return (token == w.token || token.equals(w.token))
				&& (wordforms == w.wordforms || wordforms.equals(w.wordforms))
				&& (correctWordform == w.correctWordform || correctWordform.equals(w.correctWordform));
		} catch (ClassCastException e)
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		//return 0;
		String signature = "1117 " + token + " " + wordforms;
		// TODO: Ilmaar, paskaties.
		// It's a kind of magic: adding the lower one makes Word-s unfindable in 
		// LinkedHashMap, even there exists an key to which .equals gives true
		// and .hashCode gives the same value as for the searched object. 
//		signature = signature + " " + correctWordform + " ";
		return signature.hashCode();
	}
	
	public void addWordform (Wordform wordform){
		wordforms.add(wordform);
	}

	public boolean isRecognized(){
		return !wordforms.isEmpty();
	}

	public void print(PrintWriter stream){
		stream.format("Aprakstam vārdu '%s'%n", token);
		if (wordforms.isEmpty()) {
			stream.println("\tVārds nav atpazīts.\n");
		} else {
			if (wordforms.size() == 1) {
				stream.println("\tVārds ir atpazīts viennozīmīgi.\n");
				wordforms.get(0).describe(stream);
			} else {
				stream.format("\tVārds ir atpazīts %d variantos%n", wordforms.size());
				for (Wordform variants : wordforms) {
					stream.format("\tVariants %d%n",wordforms.indexOf(variants)+1);
					variants.describe(stream);
				}
			}
		}
		stream.flush();
	}

	public void printShort(PrintWriter stream){
		if (wordforms.isEmpty()) {
			stream.printf("%s : nav atpazīts.\n", token);
		} else {
			for (Wordform variants : wordforms)
				variants.shortDescription(stream);
		}
		stream.flush();
	}

	public void addAttribute(String attribute, String value) {
		for (Wordform variants : wordforms)
			variants.addAttribute(attribute, value);
	}

	/**
	 * 	gets rid of those wordforms that match (weakly) the attributes provided. Destructive!
	 * @param attributes
	 */
	public void filterByAttributes(AttributeValues attributes) {
		ArrayList<Wordform> derīgās = new ArrayList<Wordform>();

		for (Wordform vārdforma : wordforms) {
			if (vārdforma.isMatchingWeak(attributes)) derīgās.add(vārdforma);
		}

		wordforms = derīgās;
	}

	public String getToken() {
		return token;
	}

	// variantuSkaits
	public int wordformsCount() {
		return wordforms.size();
	}

	public void setCorrectWordform(Wordform wordform) {
		if (wordforms.indexOf(wordform) == -1)
			throw new Error(String.format("Vārdam %s mēģina uzlikt par pareizo svešu vārdformu %s.", token, wordform.getToken()));

		correctWordform = wordform;
	}

	public Wordform getCorrectWordform() {
		return correctWordform;
	}

	public void toXML(Writer stream) throws IOException {
//		private String vārds;
//		public ArrayList<Vārdforma> vardformas;
//		private Vārdforma pareizāVārdforma = null;

		stream.write("<Vārds");
		stream.write(" vārds=\"" + token.replace("\"", "&quot;") + "\"");
		if (correctWordform != null)
			stream.write(" pareizāVārdforma=\""+wordforms.indexOf(correctWordform)+"\"");
		stream.write(">\n");
		for (Wordform vārdforma : wordforms) {
			vārdforma.toXML(stream);
		}
		stream.write("</Vārds>");
	}
	
	public String toJSON() {
		Iterator<Wordform> i = wordforms.iterator();
		String out = "[";
		while (i.hasNext()) {
			out += i.next().toJSON();
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}
	
	public String toJSONsingle() {
		if (isRecognized()) {
			/* šis ir tad, ja vajag tikai vienu - ticamāko formu. tā jau varētu atgriezt visu sarakstu. */
			Wordform maxwf = getBestWordform();
			//return maxwf.toJSON(); TODO - varbūt arī šo te vajag atgriezt
			return String.format("{\"Vārds\":\"%s\",\"Marķējums\":\"%s\",\"Pamatforma\":\"%s\"}", JSONValue.escape(maxwf.getToken()), JSONValue.escape(maxwf.getTag()), JSONValue.escape(maxwf.getValue(AttributeNames.i_Lemma)));
		} else 
			return String.format("{\"Vārds\":\"%s\",\"Marķējums\":\"-\",\"Pamatforma\":\"%s\"}", JSONValue.escape(getToken()), JSONValue.escape(getToken()));
	}

	public Wordform getBestWordform() {
		if (wordforms.size() == 0) return null;
		Wordform maxwf = wordforms.get(0);
		double maxticamība = -1;
		for (Wordform wf : wordforms) {  // Paskatamies visus atrastos variantus un ņemam statistiski ticamāko
			//tag += String.format("%s\t%d\n", wf.getDescription(), MorphoServer.statistics.getTicamība(wf));
			double estimate = Statistics.getStatistics().getEstimate(wf);
			if (estimate > maxticamība) {
				maxticamība = estimate;
				maxwf = wf;
			}
		}
		return maxwf;
	}
	
	public Wordform getMatchingWordform(String answerTag, boolean complain) {
		Wordform result = null;
		AttributeValues av = MarkupConverter.fromKamolsMarkup(answerTag);
		double maxticamība = -1;
		for (Wordform wf : this.wordforms) {
			if (wf.isMatchingWeak(av)) {
				double estimate = Statistics.getStatistics().getEstimate(wf);
				if (estimate > maxticamība) {
					maxticamība = estimate;
					result = wf;
				}
//				if (complain && result != null) 
//					System.err.printf("Multiple valid word(lemma) options for word %s tag %s: %s and %s\n", this.getToken(), answerTag, wf.getTag(), result.getTag());
			}
		}
		
		if (result == null) {
			result = new Wordform(this.getToken());
			result.addAttributes(av);
			result.addAttribute(AttributeNames.i_Source, "CMM tagger guess");
			result.addAttribute(AttributeNames.i_Lemma, this.getToken()); //FIXME - most likely wrong lemma, guesser should be used to obtain a realistic one
			if (complain) System.err.printf("Tagger chose a tag that's not one of analysis options for word %s tag %s\n", this.getToken(), answerTag);
		}
		if (result.getValue(AttributeNames.i_Lemma).equalsIgnoreCase("nav") || result.getValue(AttributeNames.i_Lemma).equalsIgnoreCase("nenāk")) {
			result.describe();
		}

		
		return result;
	}

	public String toTabSepsingle() { // Čakarīgs formāts haskell-pipe-export ātrdarbībai
		if (isRecognized()) {
			Wordform maxwf = getBestWordform();
			//return maxwf.toJSON(); TODO - varbūt arī šo te vajag atgriezt
			return String.format("%s\t%s\t%s", maxwf.getToken(), maxwf.getTag(), maxwf.getValue(AttributeNames.i_Lemma));
		} else 
			return String.format("%s\t-\t%s", getToken(), getToken());
	}

	public String toTabSep(boolean probabilities) { // Čakarīgs formāts postagera pitonam
		if (isRecognized()) {
			double sumTicamība = 0;
			for (Wordform wf : wordforms) sumTicamība += Statistics.getStatistics().getEstimate(wf);
			if (sumTicamība < 0.001) sumTicamība = 0.001;
			
			Iterator<Wordform> i = wordforms.iterator();
			String out = "";
			while (i.hasNext()) {
				Wordform wf = i.next();
				out += String.format("%s\t%s\t%s", wf.getToken(), wf.getTag(), wf.getValue(AttributeNames.i_Lemma));;
				if (probabilities) out += String.format("\t%.5f", Statistics.getStatistics().getEstimate(wf)/sumTicamība);
				if (i.hasNext()) out += "\t";
			}
			return out;
		} else {
			String out = String.format("%s\t-\t%s", getToken(), getToken());
			if (probabilities) out += "\t1.0";
			return out;
		}
			
	}
	
	public void dataHasChanged() {
		// FIXME - pagaidām paļaujas, ka kam vajadzēs, tas pats arī izsauks
		if (tableModel != null)
			tableModel.fireTableDataChanged();
	}

	public boolean hasAttribute(String attribute, String value){
		boolean results = false;
		for (Wordform vārdforma : wordforms)
			if (vārdforma.isMatchingStrong(attribute, value)) results = true;
		return results;
	}

}
