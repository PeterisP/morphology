/*******************************************************************************
 * Copyright 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
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
package lv.semti.morphology.analyzer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.lexicon.Ending;

public class AllEndings {
	private ArrayList<Ending> endings;
	private node root = new node(' ');
	
	public AllEndings(List<Ending> endingsource) {
		endings = new ArrayList<Ending>();
		endings.addAll(endingsource);
		for (Ending e : endingsource) { add(e); };
		root.populate(new LinkedList<Ending>());
	}
	
	public List<Ending> matchedEndings(String word) {
		int i=word.length();
		
		node t=root;
		node p=root;
		while(i>1)
		{
			p=t;
			t=t.firstChild;
			while (t!=null) {
				if (t.symbol==word.charAt(i-1)) {break;}
				t=t.nextSibling;
			}
			if (t==null) break;
			i--;
		}
		return p.endings;
	}
	
	public Ending endingByID(int nr) {
		Ending rezults = null;
		for (Ending ending : endings) {
			if (ending.getID() == nr)
				rezults = ending;
		}
		return rezults;
	}
	
	private class node  {
		public char symbol;
		public node firstChild;
		public node nextSibling;
		public ArrayList<Ending> endings;
		
		private node(char c)
		{
			symbol=c;
			firstChild=null;
			nextSibling=null;
			endings = new ArrayList<Ending>();
		}
		
		private void populate(List<Ending> suffixes) {
			endings.addAll(suffixes);
			if (firstChild != null) {firstChild.populate(endings);};
			if (nextSibling != null) {nextSibling.populate(suffixes);};
		}
	}
	
	private void add(Ending e)
	{
		String s = e.getEnding();
		int i=s.length();
		
		node t=root;
		node p=null;
		while(i>0)
		{
			p=t;
			t=t.firstChild;
			while(t!=null)
			{
				if(t.symbol==s.charAt(i-1))
				{
					break;
				}
				t=t.nextSibling;
			}
			if(t==null)
			{
				t=new node(s.charAt(i-1));
				t.nextSibling=p.firstChild;
				p.firstChild=t;
			}
			i--;
		}
		t.endings.add(e);
	}
}
