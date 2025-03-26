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

import lv.semti.morphology.attributes.AttributeValues;

public class Variants extends AttributeValues{
	// variants meklēšanai ar mijām
	public String celms;

	protected Variants (String _celms) {
		celms = _celms;
	}

	protected Variants (String _celms, String īpašība, String vērtība) {
		celms = _celms;
		addAttribute(īpašība, vērtība );
	}

	protected Variants (String _celms, String īpašība, String vērtība, String īpašība2, String vērtība2) {
		celms = _celms;
		addAttribute(īpašība, vērtība);
		addAttribute(īpašība2, vērtība2);
	}

	protected Variants (String _celms, AttributeValues īpašības) {
		celms = _celms;
		addAttributes(īpašības);
	}
}	