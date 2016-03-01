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


import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lv.semti.morphology.Testi.MorphoEvaluate.Etalons;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.*;
import lv.semti.morphology.lexicon.*;

public class VardadienasTest {
	private static Analyzer locītājs;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			locītājs = new Analyzer(false);
		} catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	@Before
	public void defaultsettings() { 
		locītājs.defaultSettings();
		locītājs.setCacheSize(0);
		locītājs.clearCache();
    }
		
	@Test
	public void vardadienas() throws IOException {
		BufferedReader ieeja;
		String rinda;
		ieeja = new BufferedReader(
				new InputStreamReader(getClass().getClassLoader().getResourceAsStream("vardadienas.txt"), "UTF-8"));
				
		while ((rinda = ieeja.readLine()) != null) {
			Word vārds = locītājs.analyze(rinda);
			if (!vārds.isRecognized()) {
				System.out.println(vārds);
			}
		}
		
		ieeja.close();
	}
	
}
