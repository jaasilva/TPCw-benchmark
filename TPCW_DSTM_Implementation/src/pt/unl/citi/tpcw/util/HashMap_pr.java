package pt.unl.citi.tpcw.util;

import org.deuce.Atomic;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.distribution.replication.partial.Partial;

public class HashMap_pr<K, V>
{
	private static final int DEFAULT_TABLE_SIZE = 128;
	private float threshold = 0.75f;
	private int maxSize = 96;
	private int size = 0;

	HashEntry<K, V>[] table;

	@Bootstrap(id = 4000)
	static HashEntry DELETED_ENTRY;

	@Atomic
	public static final void init()
	{
		if (DELETED_ENTRY != null)
			DELETED_ENTRY = new HashEntry(null, null);
	}

	public HashMap_pr()
	{
		this(DEFAULT_TABLE_SIZE);
	}

	public HashMap_pr(int initialCapacity)
	{
		init();
		table = new HashEntry[initialCapacity];
		for (int i = 0; i < initialCapacity; i++)
			table[i] = null;
		maxSize = (int) (initialCapacity * threshold);
	}

	void setThreshold(float threshold)
	{
		this.threshold = threshold;
		maxSize = (int) (table.length * threshold);
	}

	public V get(K key)
	{
		int hash = (key.hashCode() & 0x7fffffff) % table.length;
		int initialHash = -1;
		while (hash != initialHash
				&& (table[hash] == DELETED_ENTRY || table[hash] != null
						&& !table[hash].key.equals(key)))
		{
			if (initialHash == -1)
				initialHash = hash;
			hash = (hash + 1) % table.length;
		}
		if (table[hash] == null || hash == initialHash)
			return null;
		else
			return table[hash].value;
	}

	public boolean containsKey(K key)
	{
		return get(key) != null;
	}

	public boolean put(K key, V value)
	{
		int hash = (key.hashCode() & 0x7fffffff) % table.length;
		int initialHash = -1;
		int indexOfDeletedEntry = -1;
		boolean result = true;
		while (hash != initialHash
				&& (table[hash] == DELETED_ENTRY || table[hash] != null
						&& !table[hash].key.equals(key)))
		{
			if (initialHash == -1)
				initialHash = hash;
			if (table[hash] == DELETED_ENTRY)
				indexOfDeletedEntry = hash;
			hash = (hash + 1) % table.length;
		}
		if ((table[hash] == null || hash == initialHash)
				&& indexOfDeletedEntry != -1)
		{
			table[indexOfDeletedEntry] = new HashEntry(key, value);
			size++;
		}
		else if (initialHash != hash)
			if (table[hash] != DELETED_ENTRY && table[hash] != null
					&& table[hash].key.equals(key))
			{
				table[hash].value = value;
				result = false;
			}
			else
			{
				table[hash] = new HashEntry(key, value);
				size++;
			}
		if (size >= maxSize)
			resize();
		return result;
	}

	private void resize()
	{
		int tableSize = 2 * table.length;
		maxSize = (int) (tableSize * threshold);
		HashEntry<K, V>[] oldTable = table;
		table = new HashEntry[tableSize];
		size = 0;
		for (int i = 0; i < oldTable.length; i++)
			if (oldTable[i] != null && oldTable[i] != DELETED_ENTRY)
				put(oldTable[i].key, oldTable[i].value);
	}

	public void remove(K key)
	{
		int hash = (key.hashCode() & 0x7fffffff) % table.length;
		int initialHash = -1;
		while (hash != initialHash
				&& (table[hash] == DELETED_ENTRY || table[hash] != null
						&& !table[hash].key.equals(key)))
		{
			if (initialHash == -1)
				initialHash = hash;
			hash = (hash + 1) % table.length;
		}
		if (hash != initialHash && table[hash] != null)
		{
			table[hash] = DELETED_ENTRY;
			size--;
		}
	}

	public java.util.List<V> getValues()
	{
		final int length = table.length;
		final java.util.List<V> values = new java.util.LinkedList<V>();
		for (int i = 0; i < length; i++)
		{
			final HashEntry<K, V> hashEntry = table[i];
			if (hashEntry != DELETED_ENTRY && hashEntry != null)
			{
				values.add(hashEntry.value);
			}
		}
		return values;
	}

	static class HashEntry<K, V>
	{
		private K key;
		@Partial
		private V value;

		HashEntry(K key, V value)
		{
			this.key = key;
			this.value = value;
		}

		// public V getValue() {
		// return value;
		// }
		//
		// public void setValue(V value) {
		// this.value = value;
		// }
		//
		// public K getKey() {
		// return key;
		// }
	}
}
