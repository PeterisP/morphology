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
package lv.semti.Vardnicas;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.lexicon.*;

class Counter<K extends Comparable> extends HashMap<K, Integer> {

	public void add(K key) {
		Integer count = this.get(key);
		if (count==null) count = 0;
		this.put(key, count+1);
	}
	
	class CounterComparator implements Comparator<Entry<K, Integer>> {
        public int compare(Entry<K, Integer> e1, Entry<K, Integer> e2) {
            return e1.getKey().compareTo(e2.getKey());
        }

    }
	
	public void describe(PrintWriter pipe) {
		List<Entry<K, Integer>> list = new ArrayList<Entry<K, Integer>>(this.entrySet());
		java.util.Collections.sort(list, new CounterComparator());
		  
		for (Entry<K, Integer> count : list) {
			pipe.format("\t\t%s = %s%n", count.getKey().toString(), count.getValue());
		}
		for (Entry<K, Integer> count : list) {
			if (count.getValue() > 0)
				pipe.print(count.getKey().toString());
		}
		pipe.println();
		pipe.flush();
	}
}

public class CelmuBeigas {
	
	public static void main(String[] args) throws Exception {
		Analyzer analizators = new Analyzer();
		
		PrintWriter izeja = new PrintWriter(new PrintStream(System.out));
		//List<Integer> paradigmids = Arrays.asList(1,2,3); //TODO - salikt visus
		List<Integer> paradigmids = Arrays.asList(21); 
		for (int pid : paradigmids) {
			Counter<String> counts = new Counter<String>();
			for (char c : "aābcčdeēfgģhiījkķlļmnņoprsštuūvzž".toCharArray()) {
				counts.put(String.valueOf(c), 0);
			}
			
			Paradigm p = analizators.paradigmByID(pid);
			for (Lexeme l : p.lexemes) {
				String beigas = l.getStem(0).substring(l.getStem(0).length()-1);
				if (beigas.equals("š") || beigas.equals("ū") || beigas.equals("ž"))
					l.describe(izeja);
				counts.add(beigas);
			}
			
			counts.describe(izeja);
		}
		
		izeja.flush();
	}

}
