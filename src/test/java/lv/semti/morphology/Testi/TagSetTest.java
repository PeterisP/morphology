/*******************************************************************************
 * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia; Author: Pēteris Paikens
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
package lv.semti.morphology.Testi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.MarkupConverter;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.attributes.*;
import lv.semti.morphology.lexicon.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class TagSetTest {
	private static Analyzer locītājs;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			locītājs = new Analyzer("dist/Lexicon.xml", false);
		} catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void leksikons() {
		TagSet īpv = TagSet.getTagSet();
		
		for (Paradigm vārdgrupa : locītājs.paradigms) {
			assertEquals(String.format("Nevalidējas vārdgrupa %d", vārdgrupa.getID()), null, īpv.validate(vārdgrupa, "LV"));
			for (Lexeme leksēma : vārdgrupa.lexemes)
				assertEquals(String.format("Nevalidējas leksēma %d", leksēma.getID()),  null, īpv.validate(leksēma, "LV"));
			for (Ending ending : vārdgrupa.endings)
				assertEquals(String.format("Nevalidējas galotne %d", ending.getID()), null, īpv.validate(ending, "LV"));			
		}
	}
	
	@Test
	public void convert_basic() throws UnsupportedEncodingException {
		// validates if MarkupConverter can convert simple tags to AV's and back again, keeping all info
		TagSet tags = TagSet.getTagSet();
		PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
		
		List<String> testcases = Arrays.asList("sppdn","vmnift330an","zc");
		
		Lexicon lexicon = null;
		try {
			//lexicon = new Lexicon("dist/Lexicon.xml");
			
			for (String testcase : testcases) {
				String result = MarkupConverter.toKamolsMarkup(MarkupConverter.fromKamolsMarkup(testcase));
				assertEquals(testcase, result);
				if (!testcase.equalsIgnoreCase(result)) {
					tags.fromTag(testcase).describe(izeja);			
					izeja.println();
					izeja.printf("%s\n%s\n",testcase,result);
					izeja.println();					
				}
			}

			izeja.flush();
		} catch (Exception e) {
			e.printStackTrace();
			fail();       
		}						
	}
	
	@Test
	public void convert_etalons() throws IOException {
		BufferedReader ieeja;
		String rinda;
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("dist/morfoetalons.txt"), "UTF-8"));
		
		LinkedList<Etalons> etaloni = new LinkedList<Etalons>();
		
		while ((rinda = ieeja.readLine()) != null) {
			if (rinda.equalsIgnoreCase("<s>") || rinda.equalsIgnoreCase("</s>")) continue;
			etaloni.add(new Etalons(rinda));
		}
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		TagSet tags = TagSet.getTagSet();

		for (Etalons e : etaloni) {
			AttributeValues av = tags.fromTag(e.tag);
			String converted = tags.toTag(av);
			
			if (!e.tag.equalsIgnoreCase(converted)) {
				izeja.println(e.wordform+"\t"+e.tag+"\n\t"+converted+"\n");
				av.describe(izeja);
				izeja.println();
			}
		}
		
		izeja.flush();
		ieeja.close();
	}
	
	class Etalons {
		String wordform;
		String lemma;
		String tag;
		
		Etalons(String rinda) {
			String[] parse = rinda.split("\t");
			wordform = parse[0];
			tag = parse[1];
			lemma = parse[2];			
		}
	}

	@Test
	public void tildes_konvertors() throws Exception {
		TagSet semti = TagSet.getTagSet();
		TagSet tilde = new TagSet("dist/TagSet_Tilde.xml");
		
		assertEquals("N-msn---------n-------------", tilde.toTag(semti.fromTag("npmsn2"))); //TODO - nečeko capital letters fīčas
//					  N-msn------------s----------
//					  0123456789012345678901234567			
	}
	
	@Test
	public void tildes_output_analīzei() throws Exception {
		TagSet tilde = new TagSet("dist/TagSet_Tilde.xml");
		
		Word jānis = locītājs.analyze("Jānis");
		
		assertEquals("N-msn---------n-----------f-", tilde.toTag(jānis.getBestWordform()));
//					  N-msn------------s----------
//					  0123456789012345678901234567			
	}
	
	@Test
	public void english() {
		TagSet tags = TagSet.getTagSet();
		
		Word cirvis = locītājs.analyze("cirvis");
		assertTrue(cirvis.getBestWordform().isMatchingStrong("Locījums", "Nominatīvs"));
		assertTrue(cirvis.getBestWordform().isMatchingStrong("Vārds", "cirvis"));
		cirvis.getBestWordform().describe();
		AttributeValues english = tags.toEnglish(cirvis.getBestWordform());
		System.out.println("----------------");
		english.describe();
		assertTrue(english.isMatchingStrong("Case", "Nominative"));
		assertTrue(english.isMatchingStrong("Wordform", "cirvis"));
	}

}
