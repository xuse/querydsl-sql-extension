package com.github.xuse.querydsl.spring.core.resource;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.github.xuse.querydsl.util.Assert;

public class ConcurrentReferenceHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	private static final ReferenceType DEFAULT_REFERENCE_TYPE = ReferenceType.SOFT;

	private static final int MAXIMUM_CONCURRENCY_LEVEL = 1 << 16;

	private static final int MAXIMUM_SEGMENT_SIZE = 1 << 30;

	private final Segment[] segments;

	private final float loadFactor;

	private final ReferenceType referenceType;

	private final int shift;

	private volatile Set<Map.Entry<K, V>> entrySet;

	public ConcurrentReferenceHashMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, DEFAULT_REFERENCE_TYPE);
	}

	public ConcurrentReferenceHashMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, DEFAULT_REFERENCE_TYPE);
	}

	public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL, DEFAULT_REFERENCE_TYPE);
	}

	public ConcurrentReferenceHashMap(int initialCapacity, int concurrencyLevel) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR, concurrencyLevel, DEFAULT_REFERENCE_TYPE);
	}

	public ConcurrentReferenceHashMap(int initialCapacity, ReferenceType referenceType) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, referenceType);
	}

	public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
		this(initialCapacity, loadFactor, concurrencyLevel, DEFAULT_REFERENCE_TYPE);
	}

	@SuppressWarnings("unchecked")
	public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, ReferenceType referenceType) {
		Assert.isTrue(initialCapacity >= 0, "Initial capacity must not be negative");
		Assert.isTrue(loadFactor > 0f, "Load factor must be positive");
		Assert.isTrue(concurrencyLevel > 0, "Concurrency level must be positive");
		Assert.notNull(referenceType, "Reference type must not be null");
		this.loadFactor = loadFactor;
		this.shift = calculateShift(concurrencyLevel, MAXIMUM_CONCURRENCY_LEVEL);
		int size = 1 << this.shift;
		this.referenceType = referenceType;
		int roundedUpSegmentCapacity = (int) ((initialCapacity + size - 1L) / size);
		int initialSize = 1 << calculateShift(roundedUpSegmentCapacity, MAXIMUM_SEGMENT_SIZE);
		Segment[] segments = (Segment[]) Array.newInstance(Segment.class, size);
		int resizeThreshold = (int) (initialSize * getLoadFactor());
		for (int i = 0; i < segments.length; i++) {
			segments[i] = new Segment(initialSize, resizeThreshold);
		}
		this.segments = segments;
	}

	protected final float getLoadFactor() {
		return this.loadFactor;
	}

	protected final int getSegmentsSize() {
		return this.segments.length;
	}

	protected final Segment getSegment(int index) {
		return this.segments[index];
	}

	protected ReferenceManager createReferenceManager() {
		return new ReferenceManager();
	}

	protected int getHash(Object o) {
		int hash = (o != null ? o.hashCode() : 0);
		hash += (hash << 15) ^ 0xffffcd7d;
		hash ^= (hash >>> 10);
		hash += (hash << 3);
		hash ^= (hash >>> 6);
		hash += (hash << 2) + (hash << 14);
		hash ^= (hash >>> 16);
		return hash;
	}

	@Override
	public V get(Object key) {
		Reference<K, V> ref = getReference(key, Restructure.WHEN_NECESSARY);
		Entry<K, V> entry = (ref != null ? ref.get() : null);
		return (entry != null ? entry.getValue() : null);
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		Reference<K, V> ref = getReference(key, Restructure.WHEN_NECESSARY);
		Entry<K, V> entry = (ref != null ? ref.get() : null);
		return (entry != null ? entry.getValue() : defaultValue);
	}

	@Override
	public boolean containsKey(Object key) {
		Reference<K, V> ref = getReference(key, Restructure.WHEN_NECESSARY);
		Entry<K, V> entry = (ref != null ? ref.get() : null);
		return (entry != null && Objects.equals(entry.getKey(), key));
	}

	protected final Reference<K, V> getReference(Object key, Restructure restructure) {
		int hash = getHash(key);
		return getSegmentForHash(hash).getReference(key, hash, restructure);
	}

	@Override
	public V put(K key, V value) {
		return put(key, value, true);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return put(key, value, false);
	}

	private V put(final K key, final V value, final boolean overwriteExisting) {
		return doTask(key, new Task<V>(TaskOption.RESTRUCTURE_BEFORE, TaskOption.RESIZE) {

			@Override
			protected V execute(Reference<K, V> ref, Entry<K, V> entry, Entries<V> entries) {
				if (entry != null) {
					V oldValue = entry.getValue();
					if (overwriteExisting) {
						entry.setValue(value);
					}
					return oldValue;
				}
				Assert.notNull(entries, "No entries segment");
				entries.add(value);
				return null;
			}
		});
	}

	@Override
	public V remove(Object key) {
		return doTask(key, new Task<V>(TaskOption.RESTRUCTURE_AFTER, TaskOption.SKIP_IF_EMPTY) {

			@Override
			protected V execute(Reference<K, V> ref, Entry<K, V> entry) {
				if (entry != null) {
					if (ref != null) {
						ref.release();
					}
					return entry.value;
				}
				return null;
			}
		});
	}

	@Override
	public boolean remove(Object key, final Object value) {
		Boolean result = doTask(key, new Task<Boolean>(TaskOption.RESTRUCTURE_AFTER, TaskOption.SKIP_IF_EMPTY) {

			@Override
			protected Boolean execute(Reference<K, V> ref, Entry<K, V> entry) {
				if (entry != null && Objects.equals(entry.getValue(), value)) {
					if (ref != null) {
						ref.release();
					}
					return true;
				}
				return false;
			}
		});
		return (Boolean.TRUE.equals(result));
	}

	@Override
	public boolean replace(K key, final V oldValue, final V newValue) {
		Boolean result = doTask(key, new Task<Boolean>(TaskOption.RESTRUCTURE_BEFORE, TaskOption.SKIP_IF_EMPTY) {

			@Override
			protected Boolean execute(Reference<K, V> ref, Entry<K, V> entry) {
				if (entry != null && Objects.equals(entry.getValue(), oldValue)) {
					entry.setValue(newValue);
					return true;
				}
				return false;
			}
		});
		return (Boolean.TRUE.equals(result));
	}

	@Override
	public V replace(K key, final V value) {
		return doTask(key, new Task<V>(TaskOption.RESTRUCTURE_BEFORE, TaskOption.SKIP_IF_EMPTY) {

			@Override
			protected V execute(Reference<K, V> ref, Entry<K, V> entry) {
				if (entry != null) {
					V oldValue = entry.getValue();
					entry.setValue(value);
					return oldValue;
				}
				return null;
			}
		});
	}

	@Override
	public void clear() {
		for (Segment segment : this.segments) {
			segment.clear();
		}
	}

	public void purgeUnreferencedEntries() {
		for (Segment segment : this.segments) {
			segment.restructureIfNecessary(false);
		}
	}

	@Override
	public int size() {
		int size = 0;
		for (Segment segment : this.segments) {
			size += segment.getCount();
		}
		return size;
	}

	@Override
	public boolean isEmpty() {
		for (Segment segment : this.segments) {
			if (segment.getCount() > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> entrySet = this.entrySet;
		if (entrySet == null) {
			entrySet = new EntrySet();
			this.entrySet = entrySet;
		}
		return entrySet;
	}

	private <T> T doTask(Object key, Task<T> task) {
		int hash = getHash(key);
		return getSegmentForHash(hash).doTask(hash, key, task);
	}

	private Segment getSegmentForHash(int hash) {
		return this.segments[(hash >>> (32 - this.shift)) & (this.segments.length - 1)];
	}

	protected static int calculateShift(int minimumValue, int maximumValue) {
		int shift = 0;
		int value = 1;
		while (value < minimumValue && value < maximumValue) {
			value <<= 1;
			shift++;
		}
		return shift;
	}

	/**
	 *    Various reference types supported by this map.
	 */
	public enum ReferenceType {

		/**
		 *   Use {@link SoftReference SoftReferences}.
		 */
		SOFT,
		/**
		 *   Use {@link WeakReference WeakReferences}.
		 */
		WEAK
	}

	@SuppressWarnings("serial")
	protected final class Segment extends ReentrantLock {

		private final ReferenceManager referenceManager;

		private final int initialSize;

		private volatile Reference<K, V>[] references;

		private final AtomicInteger count = new AtomicInteger();

		private int resizeThreshold;

		public Segment(int initialSize, int resizeThreshold) {
			this.referenceManager = createReferenceManager();
			this.initialSize = initialSize;
			this.references = createReferenceArray(initialSize);
			this.resizeThreshold = resizeThreshold;
		}

		public Reference<K, V> getReference(Object key, int hash, Restructure restructure) {
			if (restructure == Restructure.WHEN_NECESSARY) {
				restructureIfNecessary(false);
			}
			if (this.count.get() == 0) {
				return null;
			}
			// Use a local copy to protect against other threads writing
			Reference<K, V>[] references = this.references;
			int index = getIndex(hash, references);
			Reference<K, V> head = references[index];
			return findInChain(head, key, hash);
		}

		public <T> T doTask(final int hash, final Object key, final Task<T> task) {
			boolean resize = task.hasOption(TaskOption.RESIZE);
			if (task.hasOption(TaskOption.RESTRUCTURE_BEFORE)) {
				restructureIfNecessary(resize);
			}
			if (task.hasOption(TaskOption.SKIP_IF_EMPTY) && this.count.get() == 0) {
				return task.execute(null, null, null);
			}
			lock();
			try {
				final int index = getIndex(hash, this.references);
				final Reference<K, V> head = this.references[index];
				Reference<K, V> ref = findInChain(head, key, hash);
				Entry<K, V> entry = (ref != null ? ref.get() : null);
				Entries<V> entries = value -> {
					@SuppressWarnings("unchecked")
					Entry<K, V> newEntry = new Entry<>((K) key, value);
					Reference<K, V> newReference = Segment.this.referenceManager.createReference(newEntry, hash, head);
					Segment.this.references[index] = newReference;
					Segment.this.count.incrementAndGet();
				};
				return task.execute(ref, entry, entries);
			} finally {
				unlock();
				if (task.hasOption(TaskOption.RESTRUCTURE_AFTER)) {
					restructureIfNecessary(resize);
				}
			}
		}

		public void clear() {
			if (this.count.get() == 0) {
				return;
			}
			lock();
			try {
				this.references = createReferenceArray(this.initialSize);
				this.resizeThreshold = (int) (this.references.length * getLoadFactor());
				this.count.set(0);
			} finally {
				unlock();
			}
		}

		protected final void restructureIfNecessary(boolean allowResize) {
			int currCount = this.count.get();
			boolean needsResize = allowResize && (currCount > 0 && currCount >= this.resizeThreshold);
			Reference<K, V> ref = this.referenceManager.pollForPurge();
			if (ref != null || (needsResize)) {
				restructure(allowResize, ref);
			}
		}

		private void restructure(boolean allowResize, Reference<K, V> ref) {
			boolean needsResize;
			lock();
			try {
				int countAfterRestructure = this.count.get();
				Set<Reference<K, V>> toPurge = Collections.emptySet();
				if (ref != null) {
					toPurge = new HashSet<>();
					while (ref != null) {
						toPurge.add(ref);
						ref = this.referenceManager.pollForPurge();
					}
				}
				countAfterRestructure -= toPurge.size();
				// Recalculate taking into account count inside lock and items that
				// will be purged
				needsResize = (countAfterRestructure > 0 && countAfterRestructure >= this.resizeThreshold);
				boolean resizing = false;
				int restructureSize = this.references.length;
				if (allowResize && needsResize && restructureSize < MAXIMUM_SEGMENT_SIZE) {
					restructureSize <<= 1;
					resizing = true;
				}
				// Either create a new table or reuse the existing one
				Reference<K, V>[] restructured = (resizing ? createReferenceArray(restructureSize) : this.references);
				// Restructure
				for (int i = 0; i < this.references.length; i++) {
					ref = this.references[i];
					if (!resizing) {
						restructured[i] = null;
					}
					while (ref != null) {
						if (!toPurge.contains(ref)) {
							Entry<K, V> entry = ref.get();
							if (entry != null) {
								int index = getIndex(ref.getHash(), restructured);
								restructured[index] = this.referenceManager.createReference(entry, ref.getHash(), restructured[index]);
							}
						}
						ref = ref.getNext();
					}
				}
				// Replace volatile members
				if (resizing) {
					this.references = restructured;
					this.resizeThreshold = (int) (this.references.length * getLoadFactor());
				}
				this.count.set(Math.max(countAfterRestructure, 0));
			} finally {
				unlock();
			}
		}

		private Reference<K, V> findInChain(Reference<K, V> ref, Object key, int hash) {
			Reference<K, V> currRef = ref;
			while (currRef != null) {
				if (currRef.getHash() == hash) {
					Entry<K, V> entry = currRef.get();
					if (entry != null) {
						K entryKey = entry.getKey();
						if (Objects.equals(entryKey, key)) {
							return currRef;
						}
					}
				}
				currRef = currRef.getNext();
			}
			return null;
		}

		@SuppressWarnings({ "unchecked" })
		private Reference<K, V>[] createReferenceArray(int size) {
			return new Reference[size];
		}

		private int getIndex(int hash, Reference<K, V>[] references) {
			return (hash & (references.length - 1));
		}

		public final int getSize() {
			return this.references.length;
		}

		public final int getCount() {
			return this.count.get();
		}
	}

	protected interface Reference<K, V> {

		Entry<K, V> get();

		int getHash();

		Reference<K, V> getNext();

		void release();
	}

	protected static final class Entry<K, V> implements Map.Entry<K, V> {

		private final K key;

		private volatile V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V value) {
			V previous = this.value;
			this.value = value;
			return previous;
		}

		@Override
		public String toString() {
			return (this.key + "=" + this.value);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public final boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof Map.Entry)) {
				return false;
			}
			Map.Entry otherEntry = (Map.Entry) other;
			return (Objects.equals(getKey(), otherEntry.getKey()) && Objects.equals(getValue(), otherEntry.getValue()));
		}

		@Override
		public final int hashCode() {
			return (Objects.hashCode(this.key) ^ Objects.hashCode(this.value));
		}
	}

	private abstract class Task<T> {

		private final EnumSet<TaskOption> options;

		public Task(TaskOption... options) {
			this.options = (options.length == 0 ? EnumSet.noneOf(TaskOption.class) : EnumSet.of(options[0], options));
		}

		public boolean hasOption(TaskOption option) {
			return this.options.contains(option);
		}

		protected T execute(Reference<K, V> ref, Entry<K, V> entry, Entries<V> entries) {
			return execute(ref, entry);
		}

		protected T execute(Reference<K, V> ref, Entry<K, V> entry) {
			return null;
		}
	}

	/**
	 *    Various options supported by a {@code Task}.
	 */
	private enum TaskOption {

		RESTRUCTURE_BEFORE, RESTRUCTURE_AFTER, SKIP_IF_EMPTY, RESIZE
	}

	private interface Entries<V> {

		void add(V value);
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean contains(Object o) {
			if (o instanceof Map.Entry<?, ?>) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
				Reference<K, V> ref = ConcurrentReferenceHashMap.this.getReference(entry.getKey(), Restructure.NEVER);
				Entry<K, V> otherEntry = (ref != null ? ref.get() : null);
				if (otherEntry != null) {
					return Objects.equals(otherEntry.getValue(), otherEntry.getValue());
				}
			}
			return false;
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof Map.Entry<?, ?>) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
				return ConcurrentReferenceHashMap.this.remove(entry.getKey(), entry.getValue());
			}
			return false;
		}

		@Override
		public int size() {
			return ConcurrentReferenceHashMap.this.size();
		}

		@Override
		public void clear() {
			ConcurrentReferenceHashMap.this.clear();
		}
	}

	private class EntryIterator implements Iterator<Map.Entry<K, V>> {

		private int segmentIndex;

		private int referenceIndex;

		private Reference<K, V>[] references;

		private Reference<K, V> reference;

		private Entry<K, V> next;

		private Entry<K, V> last;

		public EntryIterator() {
			moveToNextSegment();
		}

		@Override
		public boolean hasNext() {
			getNextIfNecessary();
			return (this.next != null);
		}

		@Override
		public Entry<K, V> next() {
			getNextIfNecessary();
			if (this.next == null) {
				throw new NoSuchElementException();
			}
			this.last = this.next;
			this.next = null;
			return this.last;
		}

		private void getNextIfNecessary() {
			while (this.next == null) {
				moveToNextReference();
				if (this.reference == null) {
					return;
				}
				this.next = this.reference.get();
			}
		}

		private void moveToNextReference() {
			if (this.reference != null) {
				this.reference = this.reference.getNext();
			}
			while (this.reference == null && this.references != null) {
				if (this.referenceIndex >= this.references.length) {
					moveToNextSegment();
					this.referenceIndex = 0;
				} else {
					this.reference = this.references[this.referenceIndex];
					this.referenceIndex++;
				}
			}
		}

		private void moveToNextSegment() {
			this.reference = null;
			this.references = null;
			if (this.segmentIndex < ConcurrentReferenceHashMap.this.segments.length) {
				this.references = ConcurrentReferenceHashMap.this.segments[this.segmentIndex].references;
				this.segmentIndex++;
			}
		}

		@Override
		public void remove() {
			Assert.isTrue(this.last != null, "No element to remove");
			ConcurrentReferenceHashMap.this.remove(this.last.getKey());
		}
	}

	/**
	 *    The types of restructuring that can be performed.
	 */
	protected enum Restructure {

		WHEN_NECESSARY, NEVER
	}

	protected class ReferenceManager {

		private final ReferenceQueue<Entry<K, V>> queue = new ReferenceQueue<>();

		public Reference<K, V> createReference(Entry<K, V> entry, int hash, Reference<K, V> next) {
			if (ConcurrentReferenceHashMap.this.referenceType == ReferenceType.WEAK) {
				return new WeakEntryReference<>(entry, hash, next, this.queue);
			}
			return new SoftEntryReference<>(entry, hash, next, this.queue);
		}

		@SuppressWarnings("unchecked")
		public Reference<K, V> pollForPurge() {
			return (Reference<K, V>) this.queue.poll();
		}
	}

	private static final class SoftEntryReference<K, V> extends SoftReference<Entry<K, V>> implements Reference<K, V> {

		private final int hash;

		private final Reference<K, V> nextReference;

		public SoftEntryReference(Entry<K, V> entry, int hash, Reference<K, V> next, ReferenceQueue<Entry<K, V>> queue) {
			super(entry, queue);
			this.hash = hash;
			this.nextReference = next;
		}

		@Override
		public int getHash() {
			return this.hash;
		}

		@Override
		public Reference<K, V> getNext() {
			return this.nextReference;
		}

		@Override
		public void release() {
			enqueue();
			clear();
		}
	}

	private static final class WeakEntryReference<K, V> extends WeakReference<Entry<K, V>> implements Reference<K, V> {

		private final int hash;

		private final Reference<K, V> nextReference;

		public WeakEntryReference(Entry<K, V> entry, int hash, Reference<K, V> next, ReferenceQueue<Entry<K, V>> queue) {
			super(entry, queue);
			this.hash = hash;
			this.nextReference = next;
		}

		@Override
		public int getHash() {
			return this.hash;
		}

		@Override
		public Reference<K, V> getNext() {
			return this.nextReference;
		}

		@Override
		public void release() {
			enqueue();
			clear();
		}
	}
}
