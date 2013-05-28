package pt.unl.citi.tpcw.util;

import java.util.List;

import org.deuce.Atomic;

import pt.unl.citi.tpcw.entities.Order;

public class LastOrders {
	static class Node {
		final Order order;
		Node prev;
		Node next;

		public Node(final Order o, final Node p, final Node n) {
			order = o;
			prev = p;
			next = n;
		}

		public Node(final Order o) {
			order = o;
		}
	}

	private Node head;
	private Node tail;
	private static final int MAX_ORDERS = 3333;
	private int orders;

	public LastOrders() {
		head = null;
		tail = null;
		orders = 0;
	}

	public final void prepend(final Order o) {
		final Node n = new Node(o);
		n.next = head;
		head = n;
		if (orders == 0) {
			tail = n;
			orders++;
		} else if (orders == MAX_ORDERS) {
			tail = tail.prev;
			tail.next = null;
		} else { // 0 < orders < MAX_ORDERS 
			orders++;
		}
	}
	
	public final List<Order> getList() {
		final List<Order> list = new java.util.LinkedList<Order>();
		for (Node n = head; n != null; n = n.next) {
			((java.util.LinkedList<Order>) list).addFirst(n.order);
		}
		return list;
	}
}
