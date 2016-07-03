package net.xprova.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class MultiMap<K1, K2, V> {

	private HashMap<K1, HashMap<K2, V>> map;

	public MultiMap() {

		map = new HashMap<K1, HashMap<K2, V>>();
	}

	public void put(K1 key1, K2 key2, V value) {

		HashMap<K2, V> subMap = map.get(key1);

		if (subMap == null) {

			subMap = new HashMap<K2, V>();
		}

		subMap.put(key2, value);

		map.put(key1, subMap);

	}

	public void put(Pair<K1, K2> key, V value) {

		put(key.first, key.second, value);

	}

	public V get(K1 key1, K2 key2) {

		HashMap<K2, V> subMap = map.get(key1);

		if (subMap == null) {

			return null;
		}

		return subMap.get(key2);

	}

	public V get(Pair<K1, K2> key) {

		return get(key.first, key.second);

	}

	public boolean contains(K1 key1, K2 key2) {

		HashMap<K2, V> subMap = map.get(key1);

		if (subMap == null) {

			return false;
		}

		return subMap.containsKey(key2);
	}

	public boolean contains(Pair<K1, K2> key) {

		return contains(key.first, key.second);

	}

	public V remove(K1 key1, K2 key2) {

		HashMap<K2, V> subMap = map.get(key1);

		return (subMap == null) ? null : subMap.remove(key2);
	}

	public V remove(Pair<K1, K2> key) {

		return remove(key.first, key.second);

	}

	public HashSet<Pair<K1, K2>> getKeys() {

		HashSet<Pair<K1, K2>> keys = new HashSet<Pair<K1, K2>>();

		for (Entry<K1, HashMap<K2, V>> entry1 : map.entrySet()) {

			K1 key1 = entry1.getKey();

			for (K2 key2 : entry1.getValue().keySet()) {

				keys.add(new Pair<K1, K2>(key1, key2));

			}

		}

		return keys;

	}

	public HashSet<V> getValues() {

		HashSet<V> values = new HashSet<V>();

		for (HashMap<K2, V> map2 : map.values()) {

			for (V val : map2.values())
				values.add(val);

		}

		return values;

	}

	public HashSet<Pair<Pair<K1, K2>, V>> entrySet() {

		// HashSet<Pair<K1, K2>> keys = new HashSet<Pair<K1, K2>>();

		HashSet<Pair<Pair<K1, K2>, V>> result = new HashSet<Pair<Pair<K1, K2>, V>>();

		for (Entry<K1, HashMap<K2, V>> entry1 : map.entrySet()) {

			K1 key1 = entry1.getKey();

			for (Entry<K2, V> entry2 : entry1.getValue().entrySet()) {

				// keys.add(new Pair<K1, K2>(key1, key2));

				K2 key2 = entry2.getKey();

				V val = entry2.getValue();

				Pair<K1, K2> p1 = new Pair<K1, K2>(key1, key2);

				Pair<Pair<K1, K2>, V> p2 = new Pair<Pair<K1, K2>, V>(p1, val);

				result.add(p2);

			}

		}

		return result;

	}

}
