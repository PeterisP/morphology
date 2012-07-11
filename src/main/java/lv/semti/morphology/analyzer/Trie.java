package lv.semti.morphology.analyzer;

import java.io.*;
import java.util.ArrayList;


public class Trie {
	public int branchIterator;
	private node iterator;
	private ArrayList<node> branchList;
	private boolean isFirst;
	
	
	
	public Trie(String filename) {
		node root;
		branchList=new ArrayList<node>();
		//izveido exception Trie
		root=new node();

		try
		{
			FileInputStream fstream = new FileInputStream(filename);
	
			InputStreamReader in = new InputStreamReader(fstream,"UTF-8");
			BufferedReader br = new BufferedReader(in);
			String strLine;
			
			while ((strLine = br.readLine()) != null)
			{
				this.add(strLine, root);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//pievieno Exception trie brach listam
		root=root.firstChild;
		branchList.add(root);
		
		/* 1
		 * Iniciāļu automāts atpazīst Dz. Dž. UpperCaseLetter.
		 */
		root=new StringNode("D");
		branchList.add(root);
		root.canEnd=true;
		root.firstChild=new StringNode("zžZŽ");
		root.firstChild.nextSibling=new StringNode(".");
		root.firstChild.nextSibling.canEnd=true;
		root.firstChild.firstChild=root.firstChild.nextSibling;
		root.nextSibling=new UCNode();
		root.nextSibling.firstChild=root.firstChild.nextSibling;
		
		/* 2a
		 * automāts, kurš atpazīst naudas formā 123,-
		 */
		root=new DigitNode();
		branchList.add(root);
		root.firstChild=new DigitNode();
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new StringNode(".,");
		root.firstChild.nextSibling.firstChild=new StringNode("-‐‑‒–—―'");  
		root.firstChild.nextSibling.firstChild.canEnd=true;
		root.firstChild.nextSibling.firstChild.nextSibling=root;
		root.firstChild.nextSibling.nextSibling=new StringNode(" '");
		root.firstChild.nextSibling.nextSibling.firstChild=root;

		/* 2b 
		 * atsevišķs automāts kārtas skaitļiem
		 */
		root=new DigitNode();
		branchList.add(root);
		root.firstChild=new DigitNode();
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new StringNode(".");
		root.firstChild.nextSibling.canEnd=true;

		/* 2c
		 * automāts, kurš atpazīst pamata skaitļus 
		 * skaitļus ar decimālatdalītāju (punktu vai komatu)
		 * skaitļus ar tūkstošu atdalītāju (komatu vai apostrofu)
		 * daļskaitļus (/ vai \)
		 */
		root=new DigitNode();
		branchList.add(root);
		root.canEnd=true;
		root.firstChild=new DigitNode();
		root.firstChild.canEnd=true;
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new StringNode(".,");
		root.firstChild.nextSibling.firstChild=root.firstChild;
		root.firstChild.nextSibling.nextSibling=new StringNode("/\'");
		root.firstChild.nextSibling.nextSibling.firstChild=root;
				
		/* 3
		 * e-pasta automāts
		 */
		root=new LetterOrDigitNode();
		branchList.add(root);
		root.firstChild=new LetterOrDigitNode("_-.");
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new StringNode("@");
		root.firstChild.nextSibling.firstChild=new LetterOrDigitNode();
		root.firstChild.nextSibling.firstChild.canEnd=true;
		root.firstChild.nextSibling.firstChild.firstChild=root.firstChild.nextSibling.firstChild;
		root.firstChild.nextSibling.firstChild.nextSibling=new StringNode("_-.");
		root.firstChild.nextSibling.firstChild.nextSibling.firstChild=root.firstChild.nextSibling.firstChild;
		
		/* 4
		 * web adreses automāts 
		 * atpazīst adreses kuras sākas ar http(s) ftp(s) www
		 * adreses var saturēt  burtus, ciparus un simbolus "/_-@:?=&%."
		 * adrese dr
		 * ikst beigties ar burtu vai ciparu vai simbolu "/"
		 */
		
		root=new StringNode("hH");
		branchList.add(root);
		root.firstChild=new StringNode("tT");
		root.firstChild.firstChild=new StringNode("tT");
		root.firstChild.firstChild.firstChild=new StringNode("pP");
		root.firstChild.firstChild.firstChild.firstChild=new StringNode(":");
		root.firstChild.firstChild.firstChild.firstChild.firstChild=new StringNode("/");
		root.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild=new StringNode("/");
		root.firstChild.firstChild.firstChild.firstChild.nextSibling=new StringNode("sS");
		root.firstChild.firstChild.firstChild.firstChild.firstChild=root.firstChild.firstChild.firstChild.firstChild;
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
		root=root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild;
		root.firstChild=new LetterOrDigitNode("/");
		root.firstChild.canEnd=true;
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.firstChild.canEnd=true;
		root.firstChild.firstChild.nextSibling=new StringNode("_-@:?=&%.");
		root.firstChild.firstChild.nextSibling.firstChild=root;
		
		/* 5
		 * Ciklojošo pieturzīmju automāts (atpazīst .?!)
		 */
		
		root=new StringNode(".?!");
		root.canEnd=true;
		root.firstChild=root;
		branchList.add(root);
		
		/* 6
		 * automāts, kurš atpazīsts vārdus ar atstarpēm
		 * piemērām "a t s t a r p e s"
		 */
		root=new LetterNode();
		branchList.add(root);
		root.firstChild=new StringNode(" ");
		root.firstChild.firstChild=new LetterNode();
		root.firstChild.firstChild.canEnd=true;
		root.firstChild.firstChild.firstChild=root.firstChild;
		
		/* 7
		 * automāts, kurš atpazīst virkni 
		 * no simboliem kuri var atrasties jebkur - burti un cipari
		 * no simboliem kuri var atrasties tikai vidū - "_-"
		 * no simboliem kuri var atrasties tikai beigās - "'"   
		 */
		
		root=new LetterOrDigitNode();
		branchList.add(root);
		root.canEnd=true;
		root.firstChild=new LetterOrDigitNode();
		root.firstChild.canEnd=true;
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new StringNode("_-");
		root.firstChild.nextSibling.firstChild=root;
		root.firstChild.nextSibling.nextSibling=new StringNode("'");
		root.firstChild.nextSibling.nextSibling.canEnd=true;
		

		
		//sagatavojamies pirmajam meklētajam simbolam
		this.reset();
		
	}
	
	public void add(String s, node root)
	{
		int i=0;
		int length=s.length();
		node t=root;
		node p=null;
		while(i<length)
		{
			p=t;
			t=t.firstChild;
			while(t!=null)
			{
				if(t.contain(s.charAt(i)))
				{
					break;
				}
				t=t.nextSibling;
			}
			if(t==null)
			{
				t=new StringNode(String.valueOf(s.charAt(i)).toLowerCase()+String.valueOf(s.charAt(i)).toUpperCase());
				t.nextSibling=p.firstChild;
				p.firstChild=t;
			}
			i++;
				
		}
		t.canEnd=true;
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
		if(branchIterator<branchList.size())
		{
			iterator=branchList.get(branchIterator);
			return true;
		}
		else
		{
			iterator=null;
			return false;
		}
		
	}
	
	public void findNextBranch(char c)
	{
		if(branchIterator>=branchList.size())
		{
			return;
		}
		do
		{
			//ja atrada pirmo potenciālo zaru, ciklu beidz
			if(this.findNext(c)>0)
			{
				break;
			}
		}while(this.nextBranch());
	}
	
	public int findNext(char c)
	{
		if(iterator==null)
		{
			return 0;
		}
		if(!isFirst)
		{
			iterator=iterator.firstChild;
		}
		isFirst=false;
		while(iterator!=null)
		{
			if(iterator.contain(c))
			{
				break;
			}
			iterator=iterator.nextSibling;
		}
		
		return this.status();
	}
	
	public int status()
	{
		if(iterator==null)
		{
			return 0;
		}
		else
		{
			if(iterator.canEnd)
			{
				return 2;
			}
			else
			{
				return 1;
			}
		}
	}
}

class node  {
	public node firstChild = null;
	public node nextSibling = null;
	public boolean canEnd = false;
	
	public boolean contain(char c) {
		return false;
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
		return Character.isLetter(c);
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
		return ( Character.isLetterOrDigit(c) || this.symbol.contains(String.valueOf(c)));
	}
	
	public LetterOrDigitNode(String c)	{
		symbol=c;
	}
	
	public LetterOrDigitNode()	{}
}