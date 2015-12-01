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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import lv.semti.morphology.analyzer.*;

public class MorphoSpeedTest {
	private static Analyzer locītājs;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		locītājs = new Analyzer();
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
		locītājs.setCacheSize(1);
		
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
		locītājs.setCacheSize(1);
		
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
	
	@Ignore("Hardcoded path") @Test
	public void balanced_corpus() throws IOException {
		BufferedReader ieeja;
		String rinda;
						
		long sākums = System.currentTimeMillis();
		locītājs.setCacheSize(100000);
		long skaits = 0;
		
		ieeja = new BufferedReader(
				new InputStreamReader(new FileInputStream("/Users/Pet/Documents/Balanseetais 3.5m/balansetais3.5m.txt"), "UTF-8"));
		while ((rinda = ieeja.readLine()) != null) {
			List<Word> tokens = Splitting.tokenize(locītājs, rinda);
			skaits += tokens.size();	
			//if (skaits>2000000) break;
		}		

		long beigas = System.currentTimeMillis();
		long starpība = beigas - sākums;
		System.out.printf("3.5m balansētais korpuss ar tokenizāciju: pagāja %d ms\n%d pieprasījumi sekundē\n", starpība, skaits*1000/starpība);
		System.out.println("\tBenchmark (Pētera iMac)");
		System.out.printf("\tBez cache - 57sec, 80 000 rq/sec\n");
		System.out.printf("\tAr cache 10k - 38sec, 120 000 rq/sec\n");
		System.out.printf("\tAr cache 100k - 27sec, 160 000 rq/sec\n");
		ieeja.close();
	}
}
