/*******************************************************************************
 * Copyright 2012 Institute of Mathematics and Computer Science, University of Latvia; Author: Pēteris Paikens
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
package lv.semti.Vardnicas;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.lexicon.*;

public class VarduSaraksts {

	public static void main(String[] args) throws Exception {
		Analyzer analizators = new Analyzer("dist/Lexicon.xml",true);
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out, true, "UTF8"));
		//BufferedWriter izeja = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("vaardi.txt"), "UTF-8"));
		
		for (Paradigm p : analizators.paradigms)
			for (Lexeme l : p.lexemes) {
				//if (p.getID() != 20 /*&& p.getID() != 17*/) continue;
				if (p.getID() != 1) continue;
				//if (!l.getStem(0).equalsIgnoreCase("pilnmēnes")) continue;
				if (!l.getStem(0).endsWith("i")) continue;
				
				izeja.append(l.getValue(AttributeNames.i_Lemma)+"\n");
				//izeja.append(l.getStem(1)+"\n");
				ArrayList<Wordform> formas = analizators.generateInflections(l);
				for (Wordform forma : formas) {
					forma.removeNonlexicalAttributes();
					//forma.removeAttribute(AttributeNames.i_LexemeID);
					//forma.removeAttribute(AttributeNames.i_EndingID);
					forma.removeAttribute(AttributeNames.i_ParadigmID);
					forma.removeAttribute(AttributeNames.i_SourceLemma);
					//forma.removeAttribute(AttributeNames.i_Mija);
					//izeja.append(String.format("%s\t%s\n",forma.getToken(),forma.toJSON()));
				}
				//break;
			}
	
		izeja.flush();
		izeja.close();
	}

}
