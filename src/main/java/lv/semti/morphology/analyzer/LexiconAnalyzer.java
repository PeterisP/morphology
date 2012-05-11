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
