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
package lv.semti.morphology.lexicon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import lv.semti.morphology.attributes.*;

/**
 *
 * @author Pēteris Paikens
 * Informācija par vienu leksēmu/celmu, kuru var lietot vārdu locīšanai
 * Information about a single lexicon element - lexeme / a stem that can be used for generation of inflectional forms
 */
public class Lexeme extends AttributeValues {
	private int id = 0;		// numurs pēc kārtas - ID
	private ArrayList <String> stems = new ArrayList<String>();    // Saknes - 1 vai 3 eksemplāri.
	private Paradigm paradigm = null;

	protected void setParadigm(Paradigm paradigm) {
		this.paradigm = paradigm;
	}

	public Paradigm getParadigm() {
		return paradigm;
	}

	@Override
	public void toXML (Writer pipe) throws IOException {
		pipe.write("<Lexeme");
		pipe.write(" ID=\""+String.valueOf(id)+"\"");
		for (int i=0;i<stems.size();i++) {
			String stem = stems.get(i);
			stem = stem.replace("\"", "&quot;").replace("&", "&amp;");
			pipe.write(" Stem"+String.valueOf(i+1)+"=\""+stem+"\"");
		}
		pipe.write(">");
		super.toXML(pipe); // īpašības UzXML
		pipe.write("</Lexeme>\n");
	}

    /**
     * Constructs a lexeme from an XML node
     * @param paradigm
     * @param node
     */
	public Lexeme(Paradigm paradigm, Node node) {
		super(node);
		if (!node.getNodeName().equalsIgnoreCase("Lexeme")) throw new Error("Node '" + node.getNodeName() + "' but Lexeme expected");
		this.paradigm = paradigm;
		setStemCount(paradigm.getStems());

        Node n = node.getAttributes().getNamedItem("Stem1");
		if (n != null)
			stems.set(0, n.getTextContent().toLowerCase()); // TODO - supports case-sensitive lietām - saīsinājumiem utml
		n = node.getAttributes().getNamedItem("Stem2");
		if (n != null)
			stems.set(1, n.getTextContent().toLowerCase());
		n = node.getAttributes().getNamedItem("Stem3");
		if (n != null)
			stems.set(2, n.getTextContent().toLowerCase());
		//FIXME - te paļaujas, ka pēc tam pati vārdgrupa 100% izsauks PieliktLeksēmu un sakārtos savus masīvus tādi.

		n = node.getAttributes().getNamedItem("ID");
		if (n != null)
			this.setID(Integer.parseInt(n.getTextContent()));
	}

    /**
     * Constructs a lexeme from a JSON object
     * @param json
     */
    public Lexeme(JSONObject json, Lexicon lexicon) {
        if (json.get("paradigm") == null)
            throw new Error("Nav paradigmas leksēmai " + json.toJSONString());

        int paradigmID = ((Long)json.get("paradigm")).intValue();
        addAttribute(AttributeNames.i_ParadigmID, ((Long)json.get("paradigm")).toString());
        paradigm = lexicon.paradigmByID(paradigmID);
        setStemCount(this.paradigm.getStems());

        if (json.get("lexeme_id") != null) {
            setID(((Long) json.get("lexeme_id")).intValue());
            addAttribute(AttributeNames.i_LexemeID, ((Long)json.get("lexeme_id")).toString());
        }
        if (json.get("entry_id") != null)
            addAttribute(AttributeNames.i_EntryID, ((Long)json.get("entry_id")).toString());
        if (json.get("human_id") != null)
            addAttribute(AttributeNames.i_EntryName, (String)json.get("human_id"));
        if (json.get("lemma") != null)
            addAttribute(AttributeNames.i_Lemma, (String)json.get("lemma"));
        if (json.get("stem1") != null)
            stems.set(0, (String)json.get("stem1"));
        if (json.get("stem2") != null)
            stems.set(1, (String)json.get("stem2"));
        if (json.get("stem3") != null)
            stems.set(2, (String)json.get("stem3"));
        if (json.get("attributes") != null) {
            JSONObject attrs = (JSONObject)json.get("attributes");
            for (Object key : attrs.keySet()) {
                Object value_obj = attrs.get(key);
                String value;
                if (value_obj instanceof String) {
                    value = (String) value_obj;
                } else {
                    value = value_obj.toString();
                }
                this.addAttribute((String)key, value);
            }
        }

        if (isMatchingStrong(AttributeNames.i_Gender, AttributeNames.v_Kopdzimte) &&
                paradigm.getValue(AttributeNames.i_Gender) != null) {
            removeAttribute(AttributeNames.i_Gender);
            addAttribute(AttributeNames.v_Kopdzimte, AttributeNames.v_Yes);
        }

        if (stems.get(0).isEmpty() && getValue(AttributeNames.i_Lemma) != null) {
            String lemma = getValue(AttributeNames.i_Lemma);
            if (isMatchingStrong(AttributeNames.i_FormRestrictions, AttributeNames.v_Plural)) {
                addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
                constructor_try_plural();
            } else {
                try {
                    String stem = paradigm.getLemmaEnding().stem(lemma);
                    stems.set(0, stem);
                } catch (Ending.WrongEndingException exc) {
                    constructor_try_plural();
                }
            }
        }

//        if (this.getValue(AttributeNames.i_Lemma).equalsIgnoreCase("celt")) {
//            System.out.print(json.toJSONString());
//            this.describe();
//            System.out.print(stems);
//        }

        paradigm.addLexeme(this);
    }

    private void constructor_try_plural() {
        // Check if maybe it's plurare tantum
        String lemma = this.getValue(AttributeNames.i_Lemma);

        AttributeValues filter = new AttributeValues();
        filter.addAttribute(AttributeNames.i_Case, AttributeNames.v_Nominative);
        filter.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        for (Ending e : paradigm.endings) {
            if (e.isMatchingWeak(filter)) {
                try {
                    String stem = e.stem(lemma);
                    stems.set(0, stem);
                } catch (Ending.WrongEndingException exc2) { /*pass*/ }
            }
        }
        if (stems.get(0).isEmpty()) {
            System.err.println(String.format("Leksēmai '%s' #%d galotne neatbilst paradigmai", lemma, this.id));
            this.describe();
        } else {
            addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
        }
    }

    @Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		// uztaisa leksēmas kopiju, kurai var mainīt īpašības, nenočakarējot sākotnējo leksēmu DB.
		Lexeme kopija;
		try {
			kopija = (Lexeme) super.clone();
			kopija.stems = (ArrayList<String>)stems.clone();
			kopija.paradigm = (Paradigm)paradigm.clone();
			kopija.id = id;
	        return kopija;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Lexeme(String stem) {
		// vārdgrupu, numuru, sakņu skaitu utml pieliek procedūra, kas pievieno leksēmu vārdgrupai
		stems.add(stem);
	}

    public Lexeme() {
        // nothing to do here
    }

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getStem(int stemNr) {
		if (stemNr < 0 || stemNr >= stems.size()) {
			int sakņuskaits = stems.size();
			throw new RuntimeException(String.format(
			//FIXME - jāpāriet uz ķeramu exception
					"Leksēmai %d ar %d saknēm mēģinam nolasīt sakni nr %d.",id,sakņuskaits,stemNr));
		}
		return stems.get(stemNr);
	}

	public void setStem(int stemNr, String stem) {
		if (stemNr < 0 || stemNr >= stems.size()) {
			int sakņuskaits = stems.size();
			throw new RuntimeException(String.format(
					//FIXME - jāpāriet uz ķeramu exception
					"Leksēmai %d ar %d saknēm mēģinam uzlikt sakni %s nr %d.",id,sakņuskaits,stem,stemNr));
		}
		if (paradigm != null) paradigm.removeLexeme(this);
		stems.set(stemNr, stem);

		// FIXME - neloģiski paļaujas, ka pēc izņemšanas lauks vārdgrupa paliks pa vecam - vajadzētu tak null
		if (paradigm != null) paradigm.addLexeme(this);
	}

	protected void setStemCount (int stemCount) {
		while (stems.size() > stemCount) stems.remove(stems.size()-1);
		while (stems.size() < stemCount) stems.add("");
	}
}
