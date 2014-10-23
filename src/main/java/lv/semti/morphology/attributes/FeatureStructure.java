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
package lv.semti.morphology.attributes;
//FIXME - jānosauc savādāk
import java.util.HashMap;
import java.util.Map.Entry;

public interface FeatureStructure {
	public void addAttribute(String attribute, String value);
	public void addAttributes(HashMap<String,String> attributes);
	public String getValue(String attribute);
	public boolean isMatchingStrong (String attribute, String value);
	public Entry<String,String> get(int nr);
	public int size();
}
