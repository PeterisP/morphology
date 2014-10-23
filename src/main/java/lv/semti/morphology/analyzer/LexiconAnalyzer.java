/*******************************************************************************
 * Copyright 2013,2014 Institute of Mathematics and Computer Science, University of Latvia
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

import org.w3c.dom.*;

import javax.xml.xpath.*;
import javax.xml.parsers.*;
import java.io.IOException;
import java.util.ArrayList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

public class LexiconAnalyzer {
	public void generateExceptions()	throws ParserConfigurationException, SAXException, IOException, XPathExpressionException 
	{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse("dist/Lexicon.xml");
		
		//atrod visus vārdus ar atstarpēm
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//Lexeme[contains(@Stem1,' ')]");
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		ArrayList<String> exceptions = new ArrayList<String>();
		NodeList nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
			exceptions.add(((Element)nodes.item(i)).getAttribute("Stem1")); 
		}
		
		//atrod visus saīsinājumus
		xpath = XPathFactory.newInstance().newXPath();
		expr = xpath.compile("//Attributes[@Vārdšķira='Saīsinājums']");
		result = expr.evaluate(doc, XPathConstants.NODESET);
		nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
			exceptions.add(((Element)nodes.item(i)).getAttribute("Pamatforma")); 
		}

		
		//atrod visus pieturzīmes, kuru garums pārsniedz vienu simbolu
		String seperators="";
		xpath = XPathFactory.newInstance().newXPath();
		expr = xpath.compile("//Attributes[@Vārdšķira='Pieturzīme']");
		result = expr.evaluate(doc, XPathConstants.NODESET);
		nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
			if(((Element)nodes.item(i)).getAttribute("Pamatforma").length()>1)
			{
				exceptions.add(((Element)nodes.item(i)).getAttribute("Pamatforma")); 
			}
			else
			{
				seperators+=((Element)nodes.item(i)).getAttribute("Pamatforma");
			}
		}
	}
}
