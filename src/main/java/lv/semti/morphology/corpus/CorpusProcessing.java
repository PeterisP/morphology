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
package lv.semti.morphology.corpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.*;

public class CorpusProcessing {
	
	/**
	 * Utility function to update files marked with historical pre-2008 standard of morphological tagging to the newest one
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 * @throws IOException
	 */
	public static void legacyTransform(String inputFileName, String outputFileName) throws IOException {
		BufferedReader ieeja = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF-8"));
		BufferedWriter izeja = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "UTF-8"));

		String rinda;
		while ((rinda = ieeja.readLine()) != null) {
			if (rinda.trim().equals("")) continue;
			String vārds;
			String marķējums;
			String pamatforma;

			if (rinda.indexOf('<') > -1) {
				vārds = rinda.substring(0,rinda.indexOf('<')-1);
				marķējums = rinda.substring(rinda.indexOf('<')+1,rinda.indexOf('>')).toLowerCase();
				pamatforma = rinda.substring(rinda.indexOf('>')+2);

				if (marķējums.startsWith("v") && marķējums.charAt(3) != 'p')   //izvākt pabeigtību
					marķējums = marķējums.substring(0,6) + marķējums.substring(7);
				//TODO - jāiztiek bez šitiem!
				if (marķējums.startsWith("v") && marķējums.charAt(3) != 'p')   //wildcard tips
					marķējums = marķējums.substring(0,1) + "_" + marķējums.substring(2);

				if (marķējums.startsWith("v") && marķējums.charAt(3) != 'p')   //wildcard transitivitāte
					marķējums = marķējums.substring(0,5) + "_" + marķējums.substring(6);

				if (marķējums.startsWith("v") && marķējums.charAt(3) != 'p' && marķējums.charAt(7) == '3')
					marķējums = marķējums.substring(0,8) + "0" + marķējums.substring(9); // 3. personai nav skaitļa vairs

				if (marķējums.startsWith("v") && marķējums.charAt(3) == 'c')
					marķējums = marķējums.substring(0,4) + "0" + marķējums.substring(5); // vēlējuma izteiksmei nepiemīt laiks

				if (marķējums.startsWith("v") && marķējums.charAt(3) == 'd')  // vajadzības izteiksmei nepiemīt laiks, persona, skaitlis
					marķējums = marķējums.substring(0,4) + "0" + marķējums.substring(5,7) + "00" + marķējums.substring(9);

				if (pamatforma.equalsIgnoreCase("nebūt")) // nezinam vai jāmarķē 'c' vai 'g'
					marķējums = marķējums.substring(0,1) + "_" + marķējums.substring(2);

				if (pamatforma.equalsIgnoreCase("būt")) // nezinam vai jāmarķē 'c' vai 'm'
					marķējums = marķējums.substring(0,1) + "_" + marķējums.substring(2);

				if (pamatforma.equalsIgnoreCase("gribēt")) // nezinam vai jāmarķē 'o' vai 'm'
					marķējums = marķējums.substring(0,1) + "_" + marķējums.substring(2);

				if (pamatforma.equalsIgnoreCase("varēt")) // mainās d.v. tips
					marķējums = marķējums.substring(0,1) + "o" + marķējums.substring(2);

				if (pamatforma.equalsIgnoreCase("nespēt")) // mainās d.v. tips
					marķējums = marķējums.substring(0,1) + "o" + marķējums.substring(2);

				if (pamatforma.equalsIgnoreCase("tikt")) // mainās d.v. tips
					marķējums = marķējums.substring(0,1) + "t" + marķējums.substring(2);

				if (pamatforma.equalsIgnoreCase("varēt")) // mainās transitivitāte
					marķējums = marķējums.substring(0,5) + "t" + marķējums.substring(6);

				if (pamatforma.equalsIgnoreCase("palīdzēt")) // mainās transitivitāte
					marķējums = marķējums.substring(0,5) + "t" + marķējums.substring(6);

				if (pamatforma.equalsIgnoreCase("censties")) // mainās transitivitāte
					marķējums = marķējums.substring(0,5) + "t" + marķējums.substring(6);

				if (pamatforma.equalsIgnoreCase("gribēt")) // mainās transitivitāte
					marķējums = marķējums.substring(0,5) + "t" + marķējums.substring(6);

				if (marķējums.startsWith("v") && marķējums.charAt(3) == 'n') // nenoteiksme
					marķējums = marķējums.substring(0,4) + "0" + marķējums.substring(5,7) + "000" + marķējums.substring(10);

			} else {
				vārds = rinda.trim();
				pamatforma = rinda.trim();
				marķējums = "z";

				if (vārds.equalsIgnoreCase(",")) marķējums += "c";
				else if (vārds.equalsIgnoreCase(";")) marķējums += "c";
				else if (vārds.equalsIgnoreCase("\"")) marķējums += "q";
				else if (vārds.equalsIgnoreCase(".")) marķējums += "s";
				else if (vārds.equalsIgnoreCase("?")) marķējums += "s";
				else if (vārds.equalsIgnoreCase("...")) marķējums += "s";
				else if (vārds.equalsIgnoreCase("!")) marķējums += "s";
				else if (vārds.equalsIgnoreCase("(")) marķējums += "b";
				else if (vārds.equalsIgnoreCase(")")) marķējums += "b";
				else if (vārds.equalsIgnoreCase("-")) marķējums += "d";
				else if (vārds.equalsIgnoreCase("\\")) marķējums += "q";
				else if (vārds.equalsIgnoreCase(":")) marķējums += "o";
				else System.out.println(vārds);
			}

			izeja.write(vārds+" <[");
			for (int i=0;i<marķējums.length();i++) {
				izeja.write(marķējums.charAt(i));
				if (i<marķējums.length()-1) izeja.write(",");
			}
			izeja.write("],'"+pamatforma+"',''>\n");
		}

		izeja.flush();
	}

	/**
	 * Processes an annotated corpus file; 
	 * Performs a comparison with current morphology analyzer to evaluate the performance of morphology as compared to 'golden standard'
	 * Creates a statistics file with frequencies of various stems/endings, so it can be used in probabilistic disambiguation of different possible morphological options 
	 * 
	 * @param analyzer
	 * @param fileName
	 */
	public static void processCorpus(Analyzer analyzer, String fileName) {
		BufferedReader ieeja = null;
		Statistics statistics = new Statistics();

		try {
			//pārveidotPlānoLedu();
			//String failaVārds = "etalons_Sofija.txt";
			//String failaVārds = "etalons_Ledus.txt";
			String failaVārds = fileName;

			ieeja = new BufferedReader(new InputStreamReader(new FileInputStream(failaVārds), "UTF-8"));
			PrintWriter izeja = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));

			String rinda;
			String vārds;
			String marķējums;
			String pamatforma;
			int vārdi = 0;
			int sakrītošie = 0;
			int derīgie = 0;

		    while ((rinda = ieeja.readLine()) != null) {
		    	if (rinda.trim().length() == 0) continue;
		    	vārds = rinda.substring(0, rinda.indexOf("<")).trim();
		    	if (rinda.contains("[")) {
		    		marķējums = rinda.substring(rinda.indexOf("["), rinda.lastIndexOf("]")+1).trim();
		    		marķējums = marķējums.replaceAll("(\\[|\\]|\\,| )","");
		    		//marķējums = Konvertors.noņemtMarķējumaFormatējumu(marķējums);
		    	} else {
		    		marķējums = "";
		    	}

		    	pamatforma = rinda.substring(rinda.indexOf("'")+1, rinda.indexOf("'", rinda.indexOf("'")+1));

		    	if (vārds.equalsIgnoreCase("prievārdeklis") ||
		    			vārds.equalsIgnoreCase("nomināls_izteicējs") ||
		    			vārds.equalsIgnoreCase("modāls_izteicējs") ||
		    			vārds.equalsIgnoreCase("adverbiāls_izteicējs") ||
		    			marķējums.equals("") ||
		    			marķējums.equalsIgnoreCase("x"))
		    		continue;

	    		AttributeValues vajadzīgās = MarkupConverter.fromKamolsMarkup(marķējums);

		    	Word analīze = analyzer.analyze(vārds);
		    	boolean vaiKādsSakrīt = false;
		    	boolean vaiKādsDer = false;
		    	String marķējumi = "";
		    	for (Wordform variants : analīze.wordforms) {
		    		marķējumi = marķējumi + "|" + variants.getTag() + "|";
		    		if (variants.getTag().equalsIgnoreCase(marķējums)) {
		    			vaiKādsSakrīt = true;
		    			statistics.addLexeme(Integer.parseInt(variants.getValue(AttributeNames.i_LexemeID)));
		    			statistics.addEnding(Integer.parseInt(variants.getValue(AttributeNames.i_EndingID)));
		    		}

		    		if (variants.isMatchingWeak(vajadzīgās))
		    			vaiKādsDer = true;
		    	}

		    	vārdi++;
		    	if (vaiKādsSakrīt) sakrītošie++;
		    	if (vaiKādsDer) {
		    		derīgie++;
		    	} else {
		    		izeja.println("vārds: |" + vārds + "|, marķējums: |" + marķējums + "|, pamatforma: |" + pamatforma + "|\nAtrastie marķējumi: " + marķējumi);
		    	}
		    }
		    izeja.printf("Sakrīt %d/%d : %.1f%%\n",sakrītošie,vārdi,100.0*sakrītošie/vārdi);
		    izeja.printf("Der    %d/%d : %.1f%%\n",derīgie,vārdi,100.0*derīgie/vārdi);
		    izeja.flush();

			Writer straume = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Statistics.DEFAULT_STATISTICS_FILE), "UTF-8"));
			statistics.toXML(straume);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Provide list of filenames to be processed as arguments");
			return;
		}
		Analyzer analyzer = new Analyzer();
		for (String filename: args) {
			processCorpus(analyzer, filename);
			//TODO - lai statistiku savāc pa visiem failiem kopā vienu
		}

	}
}
