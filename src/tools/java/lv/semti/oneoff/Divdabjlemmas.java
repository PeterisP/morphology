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
package lv.semti.oneoff;
import java.io.*;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.*;

public class Divdabjlemmas {
	// Aprisinājums tam, kā piemeklēt verbu lemmas substan

	public static void main(String[] args) throws Exception {
		BufferedReader ieeja = new BufferedReader(
				new InputStreamReader(Divdabjlemmas.class.getClassLoader().getResourceAsStream("labojamie_divdabji.txt"), "UTF-8"));

		Analyzer locītājs = new Analyzer();
		locītājs.paradigmByID(13).lexemes.clear();
		locītājs.paradigmByID(13).lexemesByID.clear();
		locītājs.paradigmByID(13).getLexemesByStem().get(0).clear();
		locītājs.enableGuessing = true;
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF-8"));
		String participle;
		while ((participle = ieeja.readLine()) != null) {
			Word w = locītājs.analyze(participle);
			String lemma = null;
			for (Wordform wf : w.wordforms) {
				if (wf.getValue(AttributeNames.i_Lemma).endsWith("t") || wf.getValue(AttributeNames.i_Lemma).endsWith("ties")) {
					lemma = wf.getValue(AttributeNames.i_Lemma);
				}
			}
			if (lemma == null) {
//				System.out.printf("%s\n", participle);
			} else {
				System.out.printf("update lexemes set data=jsonb_set(data, '{Gram,Flags,Pamatforma}', '\"%s\"', true) where lemma = '%s'\n", lemma, participle);
			}
		}

		izeja.flush();
		izeja.close();
		ieeja.close();
	}

}
