package pt.unl.citi.tpcw.util;

import org.deuce.Atomic;

public class List<T> {
	static class Node<T> {
		final T data;
		Node<T> next;

		public Node(final T d, final Node<T> n) {
			data = d;
			next = n;
		}

		public Node(final T d) {
			data = d;
		}
	}

	private Node<T> head;

	public List() {
		head = null;
	}

	public final void append(final T t) {
		final Node<T> n = new Node(t);
		n.next = head;
		head = n;
	}
	
	public final java.util.List<T> getList() {
		final java.util.List<T> list = new java.util.LinkedList<T>();
		for (Node<T> n = head; n != null; n = n.next) {
			list.add(n.data);
		}
		return list;
	}
}
