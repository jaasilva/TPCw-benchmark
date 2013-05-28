package pt.unl.citi.tpcw.util;

import org.deuce.Atomic;
import org.deuce.distribution.replication.full.Bootstrap;

public class HashMap<K, V> {
	private static final int DEFAULT_TABLE_SIZE = 128;
	private float threshold = 0.75f;
	private int maxSize = 96;
	private int size = 0;

	HashEntry<K, V>[] table;

	@Bootstrap(id = 4000)
	static HashEntry DELETED_ENTRY;

	@Atomic
	public static final void init() {
		if (DELETED_ENTRY != null)
			DELETED_ENTRY = new HashEntry(null, null);
	}

	public HashMap() {
		this(DEFAULT_TABLE_SIZE);
	}
	
	public HashMap(int initialCapacity) {
		init();
		table = new HashEntry[initialCapacity];
		for (int i = 0; i < initialCapacity; i++)
			table[i] = null;
		maxSize = (int) (initialCapacity * threshold);
	}

	void setThreshold(float threshold) {
		this.threshold = threshold;
		maxSize = (int) (table.length * threshold);
	}

	public V get(K key) {
		int hash = (key.hashCode() & 0x7fffffff) % table.length;
		int initialHash = -1;
		while (hash != initialHash
				&& (table[hash] == DELETED_ENTRY || table[hash] != null
						&& !table[hash].getKey().equals(key))) {
			if (initialHash == -1)
				initialHash = hash;
			hash = (hash + 1) % table.length;
		}
		if (table[hash] == null || hash == initialHash)
			return null;
		else
			return table[hash].getValue();
	}
	
	public boolean containsKey(K key) {
		return get(key) != null;
	}

	public void put(K key, V value) {
		int hash = (key.hashCode() & 0x7fffffff) % table.length;
		int initialHash = -1;
		int indexOfDeletedEntry = -1;
		while (hash != initialHash
				&& (table[hash] == DELETED_ENTRY || table[hash] != null
						&& !table[hash].getKey().equals(key))) {
			if (initialHash == -1)
				initialHash = hash;
			if (table[hash] == DELETED_ENTRY)
				indexOfDeletedEntry = hash;
			hash = (hash + 1) % table.length;
		}
		if ((table[hash] == null || hash == initialHash)
				&& indexOfDeletedEntry != -1) {
			table[indexOfDeletedEntry] = new HashEntry(key, value);
			size++;
		} else if (initialHash != hash)
			if (table[hash] != DELETED_ENTRY && table[hash] != null
					&& table[hash].getKey().equals(key))
				table[hash].setValue(value);
			else {
				table[hash] = new HashEntry(key, value);
				size++;
			}
		if (size >= maxSize)
			resize();
	}

	private void resize() {
		int tableSize = 2 * table.length;
		maxSize = (int) (tableSize * threshold);
		HashEntry<K, V>[] oldTable = table;
		table = new HashEntry[tableSize];
		size = 0;
		for (int i = 0; i < oldTable.length; i++)
			if (oldTable[i] != null && oldTable[i] != DELETED_ENTRY)
				put(oldTable[i].getKey(), oldTable[i].getValue());
	}

	public void remove(K key) {
		int hash = (key.hashCode() & 0x7fffffff) % table.length;
		int initialHash = -1;
		while (hash != initialHash
				&& (table[hash] == DELETED_ENTRY || table[hash] != null
						&& !table[hash].getKey().equals(key))) {
			if (initialHash == -1)
				initialHash = hash;
			hash = (hash + 1) % table.length;
		}
		if (hash != initialHash && table[hash] != null) {
			table[hash] = DELETED_ENTRY;
			size--;
		}
	}

	static class HashEntry<K, V> {
		private K key;
		private V value;

		HashEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}

		public K getKey() {
			return key;
		}
	}
}