package lv.semti.tagsets;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.attributes.TagSet;

public class TagsetConverter {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws Exception {
		BufferedReader ieeja;
		// ieeja = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("dist/morfoetalons.txt"), "UTF-8"));

		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		
		TagSet inputTags = TagSet.getTagSet();
		TagSet outputTags = new TagSet("dist/TagSet_Tilde.xml");

		String rinda;		
		while ((rinda = ieeja.readLine()) != null) {
			if (rinda.equalsIgnoreCase("<s>") || rinda.equalsIgnoreCase("</s>")) continue;
			String[] parse = rinda.split("\t");
			String wordform = parse[0];
			String tag = parse[1];
			String lemma = parse[2];			
			
			AttributeValues av = inputTags.fromTag(tag);
			String converted = outputTags.toTag(av);
			izeja.println(wordform+"\t"+tag+"\n\t"+converted+"\n");
		}
		ieeja.close();
		izeja.close();
	}
	
}
