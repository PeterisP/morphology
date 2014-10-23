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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Limited use multimap. Incomplete interface, might need additional
 * methods later.
 */
public class MappingSet<K, V>
{
	private HashMap<K, HashSet<V>> map = new HashMap<K, HashSet<V>>();
	
	public void put (K key, V value)
	{
		HashSet<V> values = new HashSet<V>();
		if (map.containsKey(key))
		{
			values = map.get(key);
		}
		values.add(value);
		map.put(key, values);
	}
	
	public HashSet<V> getAll(K key)
	{
		return map.get(key);
	}
	
	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}
	
	public boolean isEmpty()
	{
		return map.isEmpty();
	}
	
	public Set<K> keySet()
	{
		return map.keySet();
	}
	
}