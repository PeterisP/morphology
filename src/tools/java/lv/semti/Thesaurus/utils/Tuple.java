/*******************************************************************************
 * Copyright 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Lauma Pretkalniņa
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
package lv.semti.Thesaurus.utils;


/**
 * Ordered tuple.
 */
public class Tuple<E, F>
{
	public E first;
	public F second;
	
	public Tuple (E e, F f)
	{
		first = e;
		second = f;
	}
	
	// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
	@Override
	public boolean equals (Object o)
	{
		if (o == null) return false;
		if (this.getClass() != o.getClass()) return false;
		if ((first == null && ((Tuple)o).first == null || first != null && first.equals(((Tuple)o).first))
				&& (second == null && ((Tuple)o).second == null
				|| second != null && second.equals(((Tuple)o).second)))
			return true;
		else return false;
	}
	
	// This is needed for putting Lemmas in hash structures (hasmaps, hashsets).
	@Override
	public int hashCode()
	{
		return 2719 *(first == null ? 1 : first.hashCode())
				+ (second == null ? 1 : second.hashCode());
	}
}