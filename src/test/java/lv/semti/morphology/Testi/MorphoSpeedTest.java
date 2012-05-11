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


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.lexicon.Ending;

public class MorphoSpeedTest {
	private static Analyzer locītājs;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		locītājs = new Analyzer("dist/Lexicon.xml");
	}
	
	@Before
	public void defaultsettings() { 
		locītājs.enableVocative = false;
		locītājs.enableDiminutive = false;
		locītājs.enablePrefixes = false;
		locītājs.enableGuessing = false;
		locītājs.enableAllGuesses = false;
		locītājs.meklētsalikteņus = false;
    }	
	
	@Test
	public void rawspeed() {
		long sākums = System.currentTimeMillis();
		
		locītājs.enableVocative = true;
		locītājs.enableDiminutive = false;
		locītājs.enablePrefixes = false;
		locītājs.enableGuessing = false;
		locītājs.enableAllGuesses = false;
		locītājs.meklētsalikteņus = false; 
		
		locītājs.analyze("cirvis");
		
		int skaits = 0;
		for (int i = 1; i<2000; i++) {
			locītājs.analyze("cirvis");
			locītājs.analyze("roku");
			locītājs.analyze("nepadomājot");
			locītājs.analyze("Kirils");
			locītājs.analyze("parakt");
			locītājs.analyze("bundziņas");
			locītājs.analyze("pokemonizēt");
			locītājs.analyze("xyzzyt");
			locītājs.analyze("žvirblis");
			locītājs.analyze("Murgainšteineniem");
			skaits += 10;
		}
		
		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		System.out.printf("Tīrā atpazīšana: pagāja %d ms\n%d pieprasījumi sekundē\n", starpība, skaits*1000/starpība);
		System.out.printf("\tPirmsuzlabojumu benchmark (Pētera iMac) - 850 rq/sec\n");
		System.out.printf("\tUzlabojumu #2 benchmark (Pētera iMac) - 15 000 rq/sec\n");
	}
	
	@Test
	public void Guessspeed() {
		long sākums = System.currentTimeMillis();
		
		locītājs.enableVocative = true;
		locītājs.enableDiminutive = true;
		locītājs.enablePrefixes = true;
		locītājs.enableGuessing = true;
		locītājs.enableAllGuesses = true;
		locītājs.meklētsalikteņus = true; 
		
		int skaits = 0;
		for (int i = 1; i<2000; i++) {
			locītājs.analyze("cirvis");
			locītājs.analyze("roku");
			locītājs.analyze("nepadomājot");
			locītājs.analyze("Kirils");
			locītājs.analyze("parakt");
			locītājs.analyze("bundziņas");
			locītājs.analyze("pokemonizēt");
			locītājs.analyze("xyzzyt");
			locītājs.analyze("žvirblis");
			locītājs.analyze("Murgainšteineniem");
			skaits += 10;
		}
		
		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		System.out.printf("Pilnā minēšana: pagāja %d ms\n%d pieprasījumi sekundē\n", starpība, skaits*1000/starpība);
		System.out.printf("\tPirmsuzlabojumu benchmark (Pētera iMac) - 400 rq/sec\n");
		System.out.printf("\tUzlabojumu benchmark (Pētera iMac) - 12 000 rq/sec\n");
	}
	
	@Test
	public void allendings() {
		/*
		Ending e1 = locītājs.endingByID(7);  // ''
		Ending e2 = locītājs.endingByID(1);  // 's'
		Ending e3 = locītājs.endingByID(6);  // 's'
		Ending e4 = locītājs.endingByID(10);  // 'iem'
		*/
		//TODO - unittest ka allendingi korekti darbojas
	}
}
