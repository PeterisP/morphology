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