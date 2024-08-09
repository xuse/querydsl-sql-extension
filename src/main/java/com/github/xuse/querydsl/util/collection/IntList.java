package com.github.xuse.querydsl.util.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 提供和{@link List}接口相同的功能，但是免去了int类型的装箱和拆箱，从而得到性能上的改善。 适用于大规模操作int基本类型数组的情况。
 *
 * 从实际性能测试来看，平均大约是ArrayList的4倍左右
 *
 * @author Joey
 */
public final class IntList {

	private int[] list;

	private int size = 0;

	public IntList(int[] data) {
		this((int) (data.length * 1.3333));
		this.size = data.length;
		System.arraycopy(data, 0, list, 0, data.length);
	}

	public IntList() {
		this(16);
	}

	public IntList(int size) {
		this.list = new int[size];
	}

	/**
	 *  Returns the number of elements in this collection. If this collection
	 *  contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 *  <tt>Integer.MAX_VALUE</tt>.
	 *
	 *  @return the number of elements in this collection
	 */
	public int size() {
		return size;
	}

	/**
	 *  Returns <tt>true</tt> if this collection contains no elements.
	 *
	 *  @return <tt>true</tt> if this collection contains no elements
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 *  Returns <tt>true</tt> if this collection contains all of the elements in the
	 *  specified collection.
	 *
	 *  @param c collection to be checked for containment in this collection
	 *  @return <tt>true</tt> if this collection contains all of the elements in the
	 *          specified collection
	 *  @throws ClassCastException   if the types of one or more elements in the
	 *                               specified collection are incompatible with this
	 *                               collection (optional)
	 *  @throws NullPointerException if the specified collection contains one or more
	 *                               null elements and this collection does not
	 *                               permit null elements (optional), or if the
	 *                               specified collection is null
	 */
	public boolean containsAll(IntList c) {
		for (int i = 0; i < c.size; i++) {
			int key = c.list[i];
			if (!contains(key)) {
				return false;
			}
		}
		return true;
	}

	/**
	 *  Returns <tt>true</tt> if this list contains all of the elements of the
	 *  specified collection.
	 *
	 *  @param c collection to be checked for containment in this list
	 *  @return <tt>true</tt> if this list contains all of the elements of the
	 *          specified collection
	 *  @throws ClassCastException   if the types of one or more elements in the
	 *                               specified collection are incompatible with this
	 *                               list (optional)
	 *  @throws NullPointerException if the specified collection contains one or more
	 *                               null elements and this list does not permit null
	 *                               elements (optional), or if the specified
	 *                               collection is null
	 */
	public boolean containsAll(int[] c) {
		for (int i = 0; i < c.length; i++) {
			int key = c[i];
			if (!contains(key)) {
				return false;
			}
		}
		return true;
	}

	/**
	 *  Removes all of the elements from this collection (optional operation). The
	 *  collection will be empty after this method returns.
	 *
	 *  @throws UnsupportedOperationException if the <tt>clear</tt> operation is not
	 *                                        supported by this collection
	 */
	public void clear() {
		size = 0;
	}

	/**
	 *  Returns <tt>true</tt> if this list contains the specified element. More
	 *  formally, returns <tt>true</tt> if and only if this list contains at least
	 *  one element <tt>e</tt> such that
	 *  <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 *  @param o element whose presence in this list is to be tested
	 *  @return <tt>true</tt> if this list contains the specified element
	 *  @throws ClassCastException   if the type of the specified element is
	 *                               incompatible with this list (optional)
	 *  @throws NullPointerException if the specified element is null and this list
	 *                               does not permit null elements (optional)
	 */
	public boolean contains(int o) {
		for (int i = 0; i < size; i++) {
			if (list[i] == o) {
				return true;
			}
		}
		return false;
	}

	/**
	 *  将内容转换为数组
	 *
	 *  @return array
	 */
	public int[] toArray() {
		int[] array = new int[size];
		if (size > 0) {
			System.arraycopy(list, 0, array, 0, size);
		}
		return array;
	}

	/**
	 *  将内容转换为数组，如果数组当前大小和数组容器一致，则直接返回容器。 这时可以省去数组拷贝的开销，相应的带来容器暴露的风险
	 *  使用者必须清楚对容器中数据的更改可能造成IntList无法正确工作。 这个方法适用于用户取走数据后，IntList已经没有用处的场合。
	 *
	 *  @return array
	 */
	public int[] toArrayUnsafe() {
		if (size == list.length) {
			return list;
		} else {
			int[] array = new int[size];
			System.arraycopy(list, 0, array, 0, size);
			return array;
		}
	}

	/**
	 *  直接获取数组容器。配合size可以将数组直接传递给目标使用。 容器一旦被取走使用，建议不要再使用此IntList对象。
	 *
	 *  @return array
	 */
	public int[] getArrayUnsafe() {
		return list;
	}

	// Modification Operations
	/**
	 *  Appends the specified element to the end of this list (optional operation).
	 *
	 *  <p>
	 *  Lists that support this operation may place limitations on what elements may
	 *  be added to this list. In particular, some lists will refuse to add null
	 *  elements, and others will impose restrictions on the type of elements that
	 *  may be added. List classes should clearly specify in their documentation any
	 *  restrictions on what elements may be added.
	 *
	 *  @param e element to be appended to this list
	 *  @throws UnsupportedOperationException if the <tt>add</tt> operation is not
	 *                                        supported by this list
	 *  @throws ClassCastException            if the class of the specified element
	 *                                        prevents it from being added to this
	 *                                        list
	 *  @throws NullPointerException          if the specified element is null and
	 *                                        this list does not permit null elements
	 *  @throws IllegalArgumentException      if some property of this element
	 *                                        prevents it from being added to this
	 *                                        list
	 */
	public void add(int e) {
		// Increments modCount!!
		ensureCapacity(size + 1);
		list[size++] = e;
	}

	// Bulk Modification Operations
	public void add(int index, int element) {
		ensureCapacity(size + 1);
		if (index > size)
			throw new ArrayIndexOutOfBoundsException();
		int movelen = size - index;
		if (movelen > 0) {
			System.arraycopy(list, index, list, index + 1, movelen);
		}
		list[index] = element;
		size++;
	}

	/**
	 *  Inserts all of the elements in the specified collection into this list at the
	 *  specified position (optional operation). Shifts the element currently at that
	 *  position (if any) and any subsequent elements to the right (increases their
	 *  indices). The new elements will appear in this list in the order that they
	 *  are returned by the specified collection's iterator. The behavior of this
	 *  operation is undefined if the specified collection is modified while the
	 *  operation is in progress. (Note that this will occur if the specified
	 *  collection is this list, and it's nonempty.)
	 *
	 *  @param index index at which to insert the first element from the specified
	 *               collection
	 *  @param in    collection containing elements to be added to this list
	 *  @throws UnsupportedOperationException if the <tt>addAll</tt> operation is not
	 *                                        supported by this list
	 *  @throws ClassCastException            if the class of an element of the
	 *                                        specified collection prevents it from
	 *                                        being added to this list
	 *  @throws NullPointerException          if the specified collection contains
	 *                                        one or more null elements and this list
	 *                                        does not permit null elements, or if
	 *                                        the specified collection is null
	 *  @throws IllegalArgumentException      if some property of an element of the
	 *                                        specified collection prevents it from
	 *                                        being added to this list
	 *  @throws IndexOutOfBoundsException     if the index is out of range
	 *                                        (<tt>index &lt; 0 || index &gt; size()</tt>)
	 */
	public void addAll(int index, int[] in) {
		if (in.length == 0)
			return;
		ensureCapacity(size + in.length);
		int movelen = size - index;
		if (movelen > 0) {
			System.arraycopy(list, index, list, index + in.length, movelen);
		}
		System.arraycopy(in, 0, list, index, in.length);
		size += in.length;
	}

	/**
	 *  将所有数组元素加入对象
	 *
	 *  @param in input array
	 */
	public void addAll(int[] in) {
		addAll(size, in);
	}

	/**
	 *  Removes from this list all of its elements that are contained in the
	 *  specified collection (optional operation).
	 *
	 *  @param c collection containing elements to be removed from this list
	 *  @return <tt>true</tt> if this list changed as a result of the call
	 *  @throws UnsupportedOperationException if the <tt>removeAll</tt> operation is
	 *                                        not supported by this list
	 *  @throws ClassCastException            if the class of an element of this list
	 *                                        is incompatible with the specified
	 *                                        collection (optional)
	 *  @throws NullPointerException          if this list contains a null element
	 *                                        and the specified collection does not
	 *                                        permit null elements (optional), or if
	 *                                        the specified collection is null
	 */
	public boolean removeAll(IntList c) {
		boolean b = false;
		for (int i = 0; i < c.size; i++) {
			b |= remove(c.list[i]);
		}
		return b;
	}

	/**
	 *  Returns the element at the specified position in this list.
	 *
	 *  @param index index of the element to return
	 *  @return the element at the specified position in this list
	 *  @throws IndexOutOfBoundsException if the index is out of range
	 *                                    (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	public int get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException(String.valueOf(index));
		return list[index];
	}

	/**
	 *  Replaces the element at the specified position in this list with the
	 *  specified element (optional operation).
	 *
	 *  @param index   index of the element to replace
	 *  @param element element to be stored at the specified position
	 *  @return the element previously at the specified position
	 *  @throws UnsupportedOperationException if the <tt>set</tt> operation is not
	 *                                        supported by this list
	 *  @throws ClassCastException            if the class of the specified element
	 *                                        prevents it from being added to this
	 *                                        list
	 *  @throws NullPointerException          if the specified element is null and
	 *                                        this list does not permit null elements
	 *  @throws IllegalArgumentException      if some property of the specified
	 *                                        element prevents it from being added to
	 *                                        this list
	 *  @throws IndexOutOfBoundsException     if the index is out of range
	 *                                        (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	public int set(int index, int element) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int old = list[index];
		list[index] = element;
		return old;
	}

	/**
	 * 查找指定的值并删除
	 *
	 * @param element element
	 * @return true if a element removed
	 */
	public boolean remove(int element) {
		for (int i = 0; i < size; i++) {
			if (list[i] == element) {
				return removeByIndex(i);
			}
		}
		return false;
	}

	/**
	 *  删除位于指定序号的元素
	 *
	 *  @param index index
	 *  @return true if success.
	 */
	public boolean removeByIndex(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int moveLen = size - index;
		System.arraycopy(list, index + 1, list, index, moveLen);
		size--;
		return true;
	}

	/**
	 *  <p>
	 *  This implementation first gets a list iterator (with {@code listIterator()}).
	 *  Then, it iterates over the list until the specified element is found or the
	 *  end of the list is reached.
	 *
	 *  @param o int
	 *  @return index index
	 */
	public int indexOf(int o) {
		for (int i = 0; i < size; i++) {
			if (list[i] == o) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>
	 * This implementation first gets a list iterator that points to the end of the
	 * list (with {@code listIterator(size())}). Then, it iterates backwards over
	 * the list until the specified element is found, or the beginning of the list
	 * is reached.
	 *
	 * @param o o
	 * @return index
	 */
	public int lastIndexOf(int o) {
		for (int i = size - 1; i >= 0; i--) {
			if (list[i] == o) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>
	 * This implementation returns a list that subclasses {@code AbstractList}. The
	 * subclass stores, in private fields, the offset of the subList within the
	 * backing list, the size of the subList (which can change over its lifetime),
	 * and the expected {@code modCount} value of the backing list. There are two
	 * variants of the subclass, one of which implements {@code RandomAccess}. If
	 * this list implements {@code RandomAccess} the returned list will be an
	 * instance of the subclass that implements {@code RandomAccess}.
	 *
	 * <p>
	 * The subclass's {@code set(int, E)}, {@code get(int)}, {@code add(int, E)},
	 * {@code remove(int)}, {@code addAll(int,
	 * Collection)} and {@code removeRange(int, int)} methods all delegate to the
	 * corresponding methods on the backing abstract list, after bounds-checking the
	 * index and adjusting for the offset. The {@code addAll(Collection c)} method
	 * merely returns {@code addAll(size,
	 * c)}.
	 *
	 * <p>
	 * The {@code listIterator(int)} method returns a "wrapper object" over a list
	 * iterator on the backing list, which is created with the corresponding method
	 * on the backing list. The {@code iterator} method merely returns
	 * {@code listIterator()}, and the {@code size} method merely returns the
	 * subclass's {@code size} field.
	 *
	 * <p>
	 * All methods first check to see if the actual {@code modCount} of the backing
	 * list is equal to its expected value, and throw a
	 * {@code ConcurrentModificationException} if it is not.
	 *
	 * @param fromIndex fromIndex
	 * @param toIndex toIndex
	 * @return sublist
	 * @throws IndexOutOfBoundsException endpoint index value out of range
	 *                                   {@code (fromIndex < 0 || toIndex > size)}
	 * @throws IllegalArgumentException  if the endpoint indices are out of order
	 *                                   {@code (fromIndex > toIndex)}
	 */
	public IntList subList(int fromIndex, int toIndex) {
		int len = toIndex - fromIndex;
		if (len < 0 || toIndex > size)
			throw new ArrayIndexOutOfBoundsException(toIndex);
		IntList r = new IntList(len);
		System.arraycopy(list, fromIndex, r.list, 0, len);
		r.size = len;
		return r;
	}

	/**
	 *  Retains only the elements in this list that are contained in the specified
	 *  collection (optional operation). In other words, removes from this list all
	 *  the elements that are not contained in the specified collection.
	 *
	 *  @param c collection containing elements to be retained in this list
	 *  @return <tt>true</tt> if this list changed as a result of the call
	 *  @throws UnsupportedOperationException if the <tt>retainAll</tt> operation is
	 *                                        not supported by this list
	 *  @throws ClassCastException            if the class of an element of this list
	 *                                        is incompatible with the specified
	 *                                        collection (optional)
	 *  @throws NullPointerException          if this list contains a null element
	 *                                        and the specified collection does not
	 *                                        permit null elements (optional), or if
	 *                                        the specified collection is null
	 */
	public boolean retainAll(IntList c) {
		IntList result = new IntList(Math.min(size, c.size));
		for (int i = 0; i < size; i++) {
			int value = list[i];
			if (c.contains(value)) {
				result.add(value);
			}
		}
		if (size == result.size) {
			return false;
		}
		this.size = result.size;
		this.list = result.list;
		return true;
	}

	/*
	 * 数组扩容
	 */
	private void ensureCapacity(int i) {
		if (list.length < i) {
			int newLen = list.length * 2;
			while (newLen < i) {
				newLen *= 2;
			}
			this.list = Arrays.copyOf(list, newLen);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (size > 0) {
			int n = 0;
			sb.append(list[n++]);
			while (n < size) {
				sb.append(", ");
				sb.append(list[n++]);
			}
		}
		sb.append("]size:").append(size);
		return sb.toString();
	}

	/**
	 *  Get the maximum element in list.
	 *
	 *  @return {@link Integer#MIN_VALUE} if there's no element in list.
	 */
	public int getMax() {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < size(); i++) {
			int x = list[i];
			if (x > max) {
				max = x;
			}
		}
		return max;
	}

	/**
	 *  Get the minimum element in list.
	 *
	 *  @return {@link Integer#MAX_VALUE} if there's no element in list.
	 */
	public int getMin() {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < size(); i++) {
			int x = list[i];
			if (x < min) {
				min = x;
			}
		}
		return min;
	}

	/**
	 *  和ArrayList的性能对比测试
	 *  结论是由于节省的装箱拆箱的时间，比ArrayList快=3～4倍。
	 *
	 *  @param args args
	 */
	public static void main(String[] args) {
		int LOOPS = 10000;
		// 准备数据
		int[] data = { 23, 34, 45564, 765, 76, 876, 89, 1, 2, 3, 4, 5 };
		List<Integer> data2 = Arrays.asList(23, 34, 45564, 765, 76, 876, 89, 1, 2, 3, 4, 5);
		// 准备retail的参数
		IntList retail1 = new IntList(new int[] { 5, 7, 2, 23, 76, 98, 2, 1 });
		List<Integer> retail2 = Arrays.asList(5, 7, 2, 23, 76, 98, 2, 1);
		for (int i = 0; i < LOOPS; i++) {
			IntList a1 = new IntList(data);
			a1.indexOf(4);
			a1.lastIndexOf(876);
			a1.subList(2, 8);
			a1.remove(76);
			a1.remove(5);
			a1.retainAll(retail1);
		}
		for (int i = 0; i < LOOPS; i++) {
			List<Integer> a2 = new ArrayList<Integer>(data2);
			a2.indexOf(4);
			a2.lastIndexOf(876);
			a2.subList(2, 8);
			a2.remove((Object) 76);
			a2.remove((Object) 5);
			a2.retainAll(retail2);
		}
	}

	/**
	 * 将int数组转换为标准JavaList
	 * @return list of integer
	 * @param array array
	 */
	public static List<Integer> convert(int[] array) {
		List<Integer> list = new ArrayList<>();
		int len = array.length;
		for (int index = 0; index < len; index++) {
			list.add(array[index]);
		}
		return list;
	}

	/**
	 * 将List&lt;nteger&gt;转换为IntList,并可进一步方便的转换为int[].
	 *
	 * @param array array
	 * @param treatNullAsZero treatNullAsZero
	 * @return array
	 */
	public static IntList convertFrom(Collection<Integer> array, boolean treatNullAsZero) {
		IntList result = new IntList(array.size());
		for (Integer i : array) {
			if (i == null) {
				if (treatNullAsZero) {
					result.add(0);
				}
			} else {
				result.add(i.intValue());
			}
		}
		return result;
	}
}
