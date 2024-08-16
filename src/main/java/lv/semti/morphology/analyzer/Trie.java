/*******************************************************************************
 * Copyright 2013,2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Ginta Garkāje, Pēteris Paikens
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

public class Trie {
	public int branchIterator;
	private node iterator;
	private ArrayList<node> branchList;
	private node exception_root;
	private boolean isFirst;

    public Trie () {
        branchList=new ArrayList<node>();

        exception_root = new node();
        add("''", exception_root);
        add("’’", exception_root);
        add("‘’", exception_root);
        add("***", exception_root);

        // Will be ready for use only after initialize_exceptions is called, after any custom exceptions are loaded
    }

    // Create a clone with the same content but a separate iterator
    public Trie(Trie source) {
        branchList = source.branchList;
        exception_root = source.exception_root;
        reset();
    }

	public node n1_dz_initials() {
        /* 1
         * Iniciāļu automāts atpazīst Dz. Dž. UpperCaseLetter.
         */
        node root=new StringNode("D");
        root.firstChild=new StringNode("zžZŽ");
        root.firstChild.nextSibling=new StringNode(".");
        root.firstChild.nextSibling.canEnd=true;
        root.firstChild.firstChild=root.firstChild.nextSibling;
        root.nextSibling=new UCNode();
        root.nextSibling.firstChild=root.firstChild.nextSibling;
        root.setAutomaton_name("n1 - Dz Dž UpperCaseLetter");
        return root;
    }

    public static node n2_a_clock() {
        /*
         * 2a automāts atpazīst pulksteni
         */
        node root=new StringNode("01");
        root.firstChild=new DigitNode();
        root.firstChild.firstChild=new StringNode(":");
        root.firstChild.firstChild.firstChild=new StringNode("012345");
        root.firstChild.firstChild.firstChild.firstChild=new DigitNode();
        root.firstChild.firstChild.firstChild.firstChild.canEnd=true;
        root.firstChild.firstChild.firstChild.firstChild.firstChild=root.firstChild.firstChild;
        root.nextSibling=new StringNode("2");
        root.nextSibling.firstChild=new StringNode("0123");
        root.nextSibling.firstChild.firstChild=root.firstChild.firstChild;
        root.setAutomaton_name("n2a - pulkstenis");
        return root;
    }

    public static node n2_aa_date() {
        /*
         * 2aa atpazīst datumu ISO formāta 2009-12-14 (patiesībā /[0-9][0-9][0-9][0-9][\-.][0-9][0-9][\-.][0-9][0-9]\.?/)
         */
        node root=new DigitNode();
        root.firstChild=new DigitNode();
        root.firstChild.firstChild=new DigitNode();
        root.firstChild.firstChild.firstChild=new DigitNode();
        root.firstChild.firstChild.firstChild.firstChild=new StringNode("-.");
        node root2=root.firstChild.firstChild.firstChild.firstChild;
        root2.firstChild=new DigitNode();
        root2.firstChild.firstChild=new DigitNode();
        root2.firstChild.firstChild.firstChild=new StringNode("-.");
        root2=root2.firstChild.firstChild.firstChild;
        root2.firstChild=new DigitNode();
        root2.firstChild.firstChild=new DigitNode();
        root2.firstChild.firstChild.canEnd=true;
        root2.firstChild.firstChild.firstChild=new StringNode(".");
        root2.firstChild.firstChild.firstChild.canEnd=true;
        root.setAutomaton_name("n2aa - datums");
        return root;
    }

    public static node n2_aaa_houses() {
        // 2aaa atapzīst mājas numurus ( /[0-9]+[A-Z]/)
        node root=new DigitNode();
        root.firstChild=new DigitNode();
        root.firstChild.firstChild=root.firstChild;
        root.firstChild.nextSibling=new LetterNode();
        root.firstChild.nextSibling.canEnd=true;
        root.setAutomaton_name("n1 - māju numuri");
        return root;
    }

    public static node n2_b_numbers() {
        /*
         * 2b automāts
         * atpazīst:
         *  naudas formā 123,-
         * 	pamata skaitļus
         *  kārtas skaitļus
         * 	skaitļus ar decimālatdalītāju (punktu vai komatu)
         * 	skaitļus ar tūkstošu atdalītāju (komatu vai apostrofu)
         * 	daļskaitļus (/ vai \)
         */

        node root=new DigitNode();
        root.canEnd=true;
        root.firstChild=new DigitNode();
        root.firstChild.canEnd = true;
        root.firstChild.firstChild = root.firstChild;

        node tuukstoshi = new StringNode(" '");
        tuukstoshi.firstChild = new DigitNode();
        tuukstoshi.firstChild.firstChild = new DigitNode();
        node vieni = new DigitNode();
        vieni.canEnd = true;
        vieni.firstChild = tuukstoshi;
        tuukstoshi.firstChild.firstChild.firstChild = vieni;
        root.firstChild.nextSibling=tuukstoshi;

        node aizdecimaaldaljas = new DigitNode();
        aizdecimaaldaljas.canEnd = true;
        aizdecimaaldaljas.firstChild = new DigitNode();
        aizdecimaaldaljas.firstChild.canEnd = true;
        aizdecimaaldaljas.firstChild.firstChild = aizdecimaaldaljas.firstChild;
        aizdecimaaldaljas.nextSibling = new StringNode("-‐‑‒–—―'");
        aizdecimaaldaljas.nextSibling.canEnd = true;
        node punkts = new StringNode(".");
        punkts.canEnd = true; // kārtas skaitļi
        punkts.firstChild = aizdecimaaldaljas;
        tuukstoshi.nextSibling = punkts;
        node komats = new StringNode(",");
        komats.firstChild = aizdecimaaldaljas;
        punkts.nextSibling = komats;

        node fractions = new StringNode("/\\");
        fractions.firstChild = new DigitNode();
        fractions.firstChild.firstChild = fractions.firstChild;
        fractions.firstChild.canEnd = true;
        komats.nextSibling = fractions;

//        root.firstChild.nextSibling=new StringNode(".");
//        root.firstChild.nextSibling.canEnd=true;
//        root.firstChild.nextSibling.firstChild=new StringNode("-‐‑‒–—―'");
//        root.firstChild.nextSibling.firstChild.canEnd=true;
//        root.firstChild.nextSibling.nextSibling=new StringNode(",");
//        root.firstChild.nextSibling.nextSibling.firstChild=new StringNode("-‐‑‒–—―'");
//        root.firstChild.nextSibling.nextSibling.firstChild.canEnd=true;
//        root.firstChild.nextSibling.nextSibling.firstChild.nextSibling=root;
//        root.firstChild.nextSibling.nextSibling.firstChild=root.firstChild.nextSibling.firstChild;
//        root.firstChild.nextSibling.nextSibling.nextSibling=new StringNode(" '/\\");
//        root.firstChild.nextSibling.nextSibling.nextSibling.firstChild=root;
//
//        node tmp=root.firstChild.nextSibling.firstChild;
//        tmp.nextSibling=new DigitNode();
//        tmp=tmp.nextSibling;
//        tmp.canEnd=true;
//        tmp.firstChild=new DigitNode();
//        tmp.firstChild.canEnd=true;
//        tmp.firstChild.firstChild=tmp.firstChild;
//        tmp.firstChild.nextSibling=new StringNode(".,");
//        tmp.firstChild.nextSibling.canEnd = true;
//        tmp.firstChild.nextSibling.firstChild=new StringNode("-‐‑‒–—―'");
//        tmp.firstChild.nextSibling.firstChild.canEnd=true;
//        tmp.firstChild.nextSibling.firstChild.nextSibling=tmp;
//        tmp.firstChild.nextSibling.nextSibling=new StringNode(" '/\\");
//        tmp.firstChild.nextSibling.nextSibling.firstChild=tmp;

        root.setAutomaton_name("n2b - skaitļi");
        return root;
    }

    public static node n2_c_paragraphs() {
        // 2c automāts atpazīst paragrāfus formā 1.2.3.4.  kā arī IP adreses
        node root = new DigitNode();
        root.firstChild = root;
        root.nextSibling = new StringNode(".");
        node secondlevel = new DigitNode();
        root.nextSibling.firstChild = secondlevel;
        secondlevel.firstChild = secondlevel;
        secondlevel.nextSibling = new StringNode(".");
        secondlevel.nextSibling.canEnd = true;
        node thirdlevel = new DigitNode();
        thirdlevel.canEnd = true;
        secondlevel.nextSibling.firstChild = thirdlevel;
        thirdlevel.firstChild = thirdlevel;
        thirdlevel.nextSibling = new StringNode(".");
        thirdlevel.nextSibling.canEnd = true;
        thirdlevel.nextSibling.firstChild = thirdlevel;

        root.setAutomaton_name("n2c - paragrāfu numuri");
        return root;
    }

    public static node n3_email() {
        /* 3
         * e-pasta automāts
         */
        node root=new LetterOrDigitNode();
        root.firstChild=new LetterOrDigitNode("_-.");
        root.firstChild.firstChild=root.firstChild;
        root.firstChild.nextSibling=new StringNode("@");
        root.firstChild.nextSibling.firstChild=new LetterOrDigitNode();
        root.firstChild.nextSibling.firstChild.canEnd=true;
        root.firstChild.nextSibling.firstChild.firstChild=root.firstChild.nextSibling.firstChild;
        root.firstChild.nextSibling.firstChild.nextSibling=new StringNode("_-.");
        root.firstChild.nextSibling.firstChild.nextSibling.firstChild=root.firstChild.nextSibling.firstChild;
        root.setAutomaton_name("n3 - epasts");
        return root;
    }

    public static node n4a_url() {
        /* 4
         * web adreses automāts
         * atpazīst adreses kuras sākas ar http(s) ftp(s) www
         * adreses var saturēt  burtus, ciparus un simbolus "/_-@:?=&%."
         * adrese drikst beigties ar burtu vai ciparu vai simbolu "/"
         */

        node root=new StringNode("hH");
        root.firstChild=new StringNode("tT");
        root.firstChild.firstChild=new StringNode("tT");
        root.firstChild.firstChild.firstChild=new StringNode("pP");
        root.firstChild.firstChild.firstChild.firstChild=new StringNode(":");
        root.firstChild.firstChild.firstChild.firstChild.firstChild=new StringNode("/");
        root.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild=new StringNode("/");
        root.firstChild.firstChild.firstChild.firstChild.nextSibling=new StringNode("sS");
        root.firstChild.firstChild.firstChild.firstChild.nextSibling.firstChild=root.firstChild.firstChild.firstChild.firstChild;
        root.nextSibling=new StringNode("fF");
        root.nextSibling.firstChild=new StringNode("tT");
        root.nextSibling.firstChild.firstChild=new StringNode("pP");
        root.nextSibling.firstChild.firstChild.firstChild=root.firstChild.firstChild.firstChild.firstChild;
        root.nextSibling.nextSibling=new StringNode("wW");
        root.nextSibling.nextSibling.firstChild=new StringNode("wW");
        root.nextSibling.nextSibling.firstChild.firstChild=new StringNode("wW");
        root.nextSibling.nextSibling.firstChild.firstChild.firstChild=new StringNode(".");
        root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild=new LetterOrDigitNode("/");
        root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild.canEnd=true;
        root.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild=root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild;
        node root2=root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild;
        root2.firstChild=new LetterOrDigitNode("/");
        root2.firstChild.canEnd=true;
        root2.firstChild.firstChild=root2.firstChild;
        root2.firstChild.firstChild.canEnd=true;
        root2.firstChild.firstChild.nextSibling=new StringNode("_-@:?=&%.");
        root2.firstChild.firstChild.nextSibling.firstChild=root2;

        root.setAutomaton_name("n4a - URI");
        return root;
    }

    public static node n4b_domain(){
        /* 4b
         * domēnvārda automāts
         * burti.lv
         */
        node root=new LetterNode();
        root.firstChild=new LetterNode();
        root.firstChild.firstChild=root.firstChild; // 2+ burti
        root.firstChild.nextSibling=new StringNode(".");
        root.firstChild.nextSibling.firstChild=new StringNode("lL");
        root.firstChild.nextSibling.firstChild.firstChild=new StringNode("vV");
        root.firstChild.nextSibling.firstChild.firstChild.canEnd=true;
        root.setAutomaton_name("n4a - domēnvārds");
        return root;
    }

    public static node n5_punctuation() {
        /* 5
         * Ciklojošo pieturzīmju automāts (atpazīst .?!)
         */
        node root=new StringNode(".?!");
        root.canEnd=true;
        root.firstChild=root;
        root.setAutomaton_name("n5 - pieturzīmes");
        return root;
    }

    public static node n6_spaced() {
        /* 6
         * automāts, kurš atpazīsts vārdus ar atstarpēm
         * piemērām "a t s t a r p e s"
         */
        node root=new LetterNode();
        root.firstChild=new StringNode(" ");
        root.firstChild.firstChild=new LetterNode();
        root.firstChild.firstChild.canEnd=true;
        root.firstChild.firstChild.firstChild=root.firstChild;
        root.setAutomaton_name("n6 - atstarpes");
        return root;
    }

    public static node n7_compound() {
        /* 7
         * automāts, kurš atpazīst virkni
         * no simboliem kuri var atrasties jebkur - burti un cipari
         * no simboliem kuri var atrasties tikai vidū - "_-"
         * no simboliem kuri var atrasties tikai beigās - "'"
         */

        node root=new LetterOrDigitNode();
        root.canEnd=true;
        root.firstChild=new LetterOrDigitNode();
        root.firstChild.canEnd=true;
        root.firstChild.firstChild=root.firstChild;
        root.firstChild.nextSibling=new StringNode("_-");
        root.firstChild.nextSibling.firstChild=root;
        root.firstChild.nextSibling.nextSibling=new StringNode("'");
        root.firstChild.nextSibling.nextSibling.canEnd=true;
        root.setAutomaton_name("n7 - saliktie vārdi");
        return root;
    }

	public static void add(String s, node root)
	{
		int i=0;
		int length=s.length();
		node t=root;
		node p;
		while(i<length) {
			p=t;
			t=t.firstChild;
			while(t!=null) {
				if(t.contain(s.charAt(i))) {
					break;
				}
				t=t.nextSibling;
			}
			if(t==null) {
				t=new StringNode(String.valueOf(s.charAt(i)).toLowerCase()+String.valueOf(s.charAt(i)).toUpperCase());
				t.nextSibling=p.firstChild;
				p.firstChild=t;
			}
			i++;
				
		}
		t.canEnd=true;
	}

	public void addException(String s) {
        if (null == exception_root) {
            //aizkomentēts, jo tēzaura webserviss mēdz pieliek dīvainas pagaidu leksēmas
            //throw new AssertionError("Attempt to add tokenization exceptions after they have been finalized");
        }
        this.add(s, exception_root);
    }

    public void initializeExceptions() {
        branchList.add(exception_root.firstChild);
        exception_root = null;

        // 1. Iniciāļu automāts atpazīst Dz. Dž. UpperCaseLetter.
        branchList.add(n1_dz_initials());

        // 2a automāts atpazīst pulksteni
        branchList.add(n2_a_clock());
        // 2aa atpazīst datumu ISO formāta 2009-12-14 (patiesībā /[0-9][0-9][0-9][0-9][\-.][0-9][0-9][\-.][0-9][0-9]\.?/)
        branchList.add(n2_aa_date());
        // 2aaa atapzīst mājas numurus ( /[0-9]+[A-Z]/)
        branchList.add(n2_aaa_houses());

        // 2c automāts atpazīst paragrāfus formā 1.2.3.4.
        // pirms 2b, lai skaitļu automāts tos nepagrābtu pirmais
        branchList.add(n2_c_paragraphs());

        // 2b automāts atpazīst skaitļus dažādos veidos
        node root=n2_b_numbers();
        branchList.add(root);

//        // optional +- in front
//        node tmp = new StringNode("+-");
//        tmp.firstChild = root;
//        tmp.firstChild.nextSibling = new StringNode(" ");
//        tmp.firstChild.nextSibling.firstChild = root;
//        branchList.add(tmp);


        // 3 e-pasta automāts
        branchList.add(n3_email());

        // 4a web adreses automāts
        branchList.add(n4a_url());
        // 4b domēna automāts
        branchList.add(n4b_domain());

        // 5 Ciklojošo pieturzīmju automāts (atpazīst .?!)
        branchList.add(n5_punctuation());

        // 6 automāts, kurš atpazīst vārdus ar atstarpēm
        branchList.add(n6_spaced());

        // 7 automāts saliktiem vārdiem un to sastāvdaļām
        branchList.add(n7_compound());

        //sagatavojamies pirmajam meklētajam simbolam
    }
	
	public void reset()
	{
		isFirst=true;
		branchIterator=0;
		iterator=branchList.get(0);
	}
	
	public boolean nextBranch()
	{
		isFirst=true;
		branchIterator++;
		if (branchIterator<branchList.size()) {
			iterator=branchList.get(branchIterator);
			return true;
		} else {
			iterator=null;
			return false;
		}
		
	}
	
	public void findNextBranch(char c)
	{
		if(branchIterator>=branchList.size()) {
			return;
		}
		do {
			//ja atrada pirmo potenciālo zaru, ciklu beidz
			if(this.findNext(c)>0) {
				break;
			}
		} while(this.nextBranch());
	}
	
	public int findNext(char c)
	{
		if(iterator==null) {
			return 0;
		}
		if(!isFirst) {
			iterator=iterator.firstChild;
		}
		isFirst=false;
		while(iterator!=null) {
			if(iterator.contain(c)) {
				break;
			}
			iterator=iterator.nextSibling;
		}
		
		return this.status();
	}
	
	public int status()
	{
		if(iterator == null) {
			return 0;
		} else {
			if(iterator.canEnd) {
				return 2;
			} else {
				return 1;
			}
		}
	}

	public boolean match(String sequence) {
        reset();
        int status =0;
        for (char c : sequence.toCharArray()) {
             status = findNext(c);
            if (status==0) return false;
        }
        return (status==2);
    }
}

class node  {
	public node firstChild = null;
	public node nextSibling = null;
	public boolean canEnd = false;
	public String automaton_name = "";
	
	public boolean contain(char c) {
		return false;
	}
	public void setAutomaton_name(String name) {
	    if (name.equalsIgnoreCase(automaton_name))
	        return;
	    automaton_name = name;
	    if (firstChild != null)
            firstChild.setAutomaton_name(name);
        if (nextSibling != null)
            nextSibling.setAutomaton_name(name);
    }
}

class StringNode extends node {
	public String symbol;
	
	public StringNode(String c)	{
		symbol=c;
	}
	
	public boolean contain(char c) {
		return this.symbol.contains(String.valueOf(c));
	}	
}

class UCNode extends node {
	public boolean contain(char c) {
		return Character.isUpperCase(c);
	}
}

class LCNode extends node {
	public boolean contain(char c) {
		return Character.isLowerCase(c);
	}
}

class LetterNode extends node {
	public boolean contain(char c) {
		return Character.isLetter(c) || c == '\u00AD';
	}
}

class DigitNode extends node {
	public boolean contain(char c) {
		return Character.isDigit(c);
	}
}

class LetterOrDigitNode extends node {
	public String symbol="";
	
	public boolean contain(char c) {
		return ( Character.isLetterOrDigit(c) || c == '\u00AD' || this.symbol.contains(String.valueOf(c)));
	}
	
	public LetterOrDigitNode(String c)	{
		symbol=c;
	}
	
	public LetterOrDigitNode()	{}
}