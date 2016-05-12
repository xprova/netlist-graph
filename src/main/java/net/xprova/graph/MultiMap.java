package net.xprova.graph;

import java.util.HashMap;

public class MultiMap<K1, K2, V> {

	private HashMap<K1, HashMap<K2, V>> map;

	public MultiMap() {

		map = new HashMap<K1, HashMap<K2, V>>();
	}

	public MultiMap(MultiMap<K1, K2, V> pins) {

		// TODO: implement a shallow copy constructor

		map = pins.map;

	}

	public void put(K1 key1, K2 key2, V value) {

		HashMap<K2, V> subMap = map.get(key1);

		if (subMap == null) {

			subMap = new HashMap<K2, V>();
		}

		subMap.put(key2, value);

		map.put(key1, subMap);

	}

	public V get(K1 key1, K2 key2) {

		HashMap<K2, V> subMap = map.get(key1);

		if (subMap == null) {

			return null;
		}

		return subMap.get(key2);

	}

	public boolean contains(K1 key1, K2 key2) {

		HashMap<K2, V> subMap = map.get(key1);

		if (subMap == null) {

			return false;
		}

		return subMap.containsKey(key2);
	}

	public V remove(K1 key1, K2 key2) {

		HashMap<K2, V> subMap = map.get(key1);

		return (subMap == null) ? null : subMap.remove(key2);
	}

}
