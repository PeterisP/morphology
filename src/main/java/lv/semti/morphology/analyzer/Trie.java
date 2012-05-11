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
		root=new node("D");
		branchList.add(root);
		root.canEnd=true;
		root.firstChild=new node("zžZŽ");
		root.firstChild.nextSibling=new node(".");
		root.firstChild.nextSibling.canEnd=true;
		root.firstChild.firstChild=root.firstChild.nextSibling;
		root.nextSibling=new node(symbols.UC);
		root.nextSibling.firstChild=root.firstChild.nextSibling;
		
		/* 2
		 * automāts, kurš atpazīst pamata skaitļus, kārtas skaitļus, 
		 * skaitļus ar decimālatdalītāju (punktu vai komatu)
		 * skaitļus ar tūkstošu atdalītāju (komatu vai apostrofu)
		 * daļsakitļus (/ vai \)
		 */
		root=new node(symbols.DIGITS);
		branchList.add(root);
		root.canEnd=true;
		root.firstChild=new node(symbols.DIGITS);
		root.firstChild.canEnd=true;
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new node(".");
		root.firstChild.nextSibling.canEnd=true;
		root.firstChild.nextSibling.firstChild=root.firstChild;
		root.firstChild.nextSibling.nextSibling=new node(",/\'");
		root.firstChild.nextSibling.nextSibling.firstChild=root;
		
		/* 3
		 * e-pasta automāts
		 */
		root=new node(symbols.DIGITS+symbols.LETTER);
		branchList.add(root);
		root.firstChild=new node(symbols.DIGITS+symbols.LETTER+"_-.");
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new node("@");
		root.firstChild.nextSibling.firstChild=new node(symbols.DIGITS+symbols.LETTER);
		root.firstChild.nextSibling.firstChild.canEnd=true;
		root.firstChild.nextSibling.firstChild.firstChild=root.firstChild.nextSibling.firstChild;
		root.firstChild.nextSibling.firstChild.nextSibling=new node("_-.");
		root.firstChild.nextSibling.firstChild.nextSibling.firstChild=root.firstChild.nextSibling.firstChild;
		
		/* 4
		 * web adreses automāts 
		 * atpazīst adreses kuras sākas ar http(s) ftp(s) www
		 * adreses var saturēt  burtus, ciparus un simbolus "/_-@:?=&%."
		 * adrese dr
		 * ikst beigties ar burtu vai ciparu vai simbolu "/"
		 */
		
		root=new node("hH");
		branchList.add(root);
		root.firstChild=new node("tT");
		root.firstChild.firstChild=new node("tT");
		root.firstChild.firstChild.firstChild=new node("pP");
		root.firstChild.firstChild.firstChild.firstChild=new node(":");
		root.firstChild.firstChild.firstChild.firstChild.firstChild=new node("/");
		root.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild=new node("/");
		root.firstChild.firstChild.firstChild.firstChild.nextSibling=new node("sS");
		root.firstChild.firstChild.firstChild.firstChild.firstChild=root.firstChild.firstChild.firstChild.firstChild;
		root.nextSibling=new node("fF");
		root.nextSibling.firstChild=new node("tT");
		root.nextSibling.firstChild.firstChild=new node("pP");
		root.nextSibling.firstChild.firstChild.firstChild=root.firstChild.firstChild.firstChild.firstChild;
		root.nextSibling.nextSibling=new node("wW");
		root.nextSibling.nextSibling.firstChild=new node("wW");
		root.nextSibling.nextSibling.firstChild.firstChild=new node("wW");
		root.nextSibling.nextSibling.firstChild.firstChild.firstChild=new node(".");
		root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild=new node("/"+symbols.DIGITS+symbols.LETTER);
		root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild.canEnd=true;
		root.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild=root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild;
		root=root.nextSibling.nextSibling.firstChild.firstChild.firstChild.firstChild;
		root.firstChild=new node("/"+symbols.DIGITS+symbols.LETTER);
		root.firstChild.canEnd=true;
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.firstChild.canEnd=true;
		root.firstChild.firstChild.nextSibling=new node("_-@:?=&%.");
		root.firstChild.firstChild.nextSibling.firstChild=root;
		
		/* 5
		 * Ciklojošo pieturzīmju automāts (atpazīst .?!)
		 */
		
		root=new node(".?!");
		root.canEnd=true;
		root.firstChild=root;
		branchList.add(root);
		
		/* 6
		 * automāts, kurš atpazīst virkni 
		 * no simboliem kuri var atrasties jebkur - burti un cipari
		 * no simboliem kuri var atrasties tikai vidū - "_-"
		 * no simboliem kuri var atrasties tikai beigās - "'"   
		 */
		
		root=new node(symbols.LETTER+symbols.DIGITS);
		branchList.add(root);
		root.canEnd=true;
		root.firstChild=new node(symbols.LETTER+symbols.DIGITS);
		root.firstChild.canEnd=true;
		root.firstChild.firstChild=root.firstChild;
		root.firstChild.nextSibling=new node("_-");
		root.firstChild.nextSibling.firstChild=root;
		root.firstChild.nextSibling.nextSibling=new node("'");
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
				t=new node(String.valueOf(s.charAt(i)).toLowerCase()+String.valueOf(s.charAt(i)).toUpperCase());
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
	public String symbol;
	public node firstChild;
	public node nextSibling;
	public boolean canEnd;
	public node(String c)
	{
		symbol=c;
		firstChild=null;
		nextSibling=null;
		canEnd=false;
	}
	
	public boolean contain(char c)
	{
		return this.symbol.contains(String.valueOf(c));
	}
	
	public node()
	{
		symbol=null;
		firstChild=null;
		nextSibling=null;
	}
}

//dažādu simbolu kopu definēšana
interface symbols
{
	public static final String UC="AĀBCČDEĒFGHIĪJKĶLĻMNŅOŌPQRŖSŠTUŪVWXYZŽ";
	public static final String LC="aābcčdeēfghiījkķlļmnņoōpqrŗsštuūvwxyzž";
	public static final String LETTER="AĀBCČDEĒFGHIĪJKĶLĻMNŅOŌPQRŖSŠTUŪVWXYZŽaābcčdeēfghiījkķlļmnņoōpqrŗsštuūvwxyzž";
	public static final String DIGITS="0123456789";
	
}
