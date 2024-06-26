/*******************************************************************************
 * Copyright 2008, 2009, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
 * 
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
			locītājs = new Analyzer(false);
		} catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void leksikons() {
		TagSet tagset = TagSet.getTagSet();

		int lexeme_errors = 0;
		for (Paradigm paradigm : locītājs.paradigms) {
			assertEquals(String.format("Nevalidējas vārdgrupa %d", paradigm.getID()), null, tagset.validate(paradigm, "LV"));
			for (Lexeme lexeme : paradigm.lexemes) {
			    String error = tagset.validate(lexeme, "LV");
			    if (error != null) {
			        System.err.println(String.format("Nevalidējas leksēma %d - %s", lexeme.getID(), error));
			        lexeme.describe();
			        lexeme_errors ++;
                }
            }
			for (Ending ending : paradigm.endings)
				assertEquals(String.format("Nevalidējas galotne %d", ending.getID()), null, tagset.validate(ending, "LV"));
		}
		assertEquals("Not all lexemes validated!", 0, lexeme_errors);
	}
	
	@Test
	public void convert_basic() throws UnsupportedEncodingException {
		// validates if MarkupConverter can convert simple tags to AV's and back again, keeping all info
		TagSet tags = TagSet.getTagSet();
		PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
		
		List<String> testcases = Arrays.asList("sppdn","vmnift330an","zc");
		
		try {
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
				new InputStreamReader(getClass().getClassLoader().getResourceAsStream("all.txt"), "UTF-8"));
		
		LinkedList<Etalons> etaloni = new LinkedList<Etalons>();
		
		while ((rinda = ieeja.readLine()) != null) {
			if (rinda.equalsIgnoreCase("<s>") || rinda.equalsIgnoreCase("</s>") || rinda.equalsIgnoreCase("<g />")) continue;
			etaloni.add(new Etalons(rinda));
		}
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		TagSet tags = TagSet.getTagSet();

		boolean na_tags = false;
		boolean bad_tags = false;
		for (Etalons e : etaloni) {
		    if (e.tag.equalsIgnoreCase("N/A")) {
		        na_tags = true;
		        continue;
            }
			AttributeValues av = tags.fromTag(e.tag);
			String converted = tags.toTag(av);
			
			if (!e.tag.equalsIgnoreCase(converted)) {
			    bad_tags = true;
				izeja.println("Korpusā ir  \t"+e.tag+"\t("+e.wordform+")\nDekodējas uz\t"+converted+"\n");
				av.describe(izeja);
				izeja.println();
			}
		}

		assertFalse("Morphocorpus (all.txt) contains tags that aren't valid according to TagSet.xml", bad_tags);
        assertFalse("Morphocorpus (all.txt) contains N/A tags", na_tags);
		
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
		TagSet tilde = new TagSet("TagSet_Tilde.xml");
		
		assertEquals("N-msn---------n-------------", tilde.toTag(semti.fromTag("npmsn2"))); //TODO - nečeko capital letters fīčas
//					  N-msn------------s----------
//					  0123456789012345678901234567			
	}
	
	@Test
	public void tildes_output_analīzei() throws Exception {
		TagSet tilde = new TagSet("TagSet_Tilde.xml");
		
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
//		cirvis.getBestWordform().describe();
		AttributeValues english = tags.toEnglish(cirvis.getBestWordform());
//		System.out.println("----------------");
//		english.describe();
		assertTrue(english.isMatchingStrong("Case", "Nominative"));
		assertTrue(english.isMatchingStrong("Wordform", "cirvis"));
	}

	@Test
    public void nulls() {
        TagSet tags = TagSet.getTagSet();
        assertEquals("-", tags.toTag(null));
        assertEquals(0, tags.fromTag(null).size());
    }

	@Test
	public void partly_declinable() {
		TagSet tags = TagSet.getTagSet();
		AttributeValues av = tags.fromTag("voyppmsnap00n");
		assertEquals(AttributeNames.v_DaljeejiLokaams, av.getValue(AttributeNames.i_Lokaamiiba));
	}

}
