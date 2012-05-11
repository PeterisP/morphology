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
import java.util.LinkedList;

import lv.semti.morphology.Testi.MorphoEvaluate.Etalons;
import lv.semti.morphology.analyzer.MarkupConverter;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.*;
import lv.semti.morphology.lexicon.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class TagSetTest {

	@Test
	public void leksikons() {
		TagSet īpv = TagSet.getTagSet();
		
		Lexicon lexicon = null;
		try {
			lexicon = new Lexicon("dist/Lexicon.xml");
			for (Paradigm vārdgrupa : lexicon.paradigms) {
				assertEquals(String.format("Nevalidējas vārdgrupa %d", vārdgrupa.getID()), null, īpv.validate(vārdgrupa, "LV"));
				for (Lexeme leksēma : vārdgrupa.lexemes)
					assertEquals(String.format("Nevalidējas leksēma %d", leksēma.getID()),  null, īpv.validate(leksēma, "LV"));
				for (Ending ending : vārdgrupa.endings)
					assertEquals(String.format("Nevalidējas galotne %d", ending.getID()), null, īpv.validate(ending, "LV"));			
			}
			
		} catch (Exception e) {
			fail();       
		}						
	}
	
	@Test
	public void convert_basic() throws UnsupportedEncodingException {
		TagSet tags = TagSet.getTagSet();
		PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
		
		Lexicon lexicon = null;
		try {
			lexicon = new Lexicon("dist/Lexicon.xml");
/*			
			tags.fromKamolsMarkup("ncmsn2").describe(izeja);			
			izeja.println();
			izeja.println(tags.toKamolsMarkup(tags.fromKamolsMarkup("ncmsn2")));
			izeja.println();

			tags.fromKamolsMarkup("vmnpdmsnasn").describe(izeja);
			izeja.println();
			izeja.println(tags.toKamolsMarkup(tags.fromKamolsMarkup("vmnpdmsnasn")));
			izeja.println();
*/
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
			etaloni.add(new Etalons(rinda));
		}
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		TagSet tags = TagSet.getTagSet();

		for (Etalons e : etaloni) {
			AttributeValues av = tags.fromKamolsMarkup(e.tag);
			String converted = tags.toKamolsMarkup(av);
			
			if (!e.tag.equalsIgnoreCase(converted)) {
				izeja.println(e.wordform+"\t"+e.tag+"\n\t"+converted+"\n");
				av.describe(izeja);
				izeja.println();
			}
		}
		
		izeja.flush();
	}
	
	class Etalons {
		String wordform;
		String lemma;
		String tag;
		
		Etalons(String rinda) {
			String[] parse = rinda.split("\t");
			wordform = parse[0];
			lemma = parse[1];
			tag = parse[2];
		}
	}
	
}
