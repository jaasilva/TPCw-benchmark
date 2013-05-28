package pt.unl.citi.tpcw;

import static pt.unl.citi.tpcw.Operations.OP_POPULATE;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.deuce.Atomic;
import org.deuce.benchmark.stmbench7.core.RuntimeError;
import org.deuce.distribution.replication.full.Bootstrap;
import org.uminho.gsd.benchmarks.benchmark.BenchmarkNodeID;
import org.uminho.gsd.benchmarks.dataStatistics.ResultHandler;
import org.uminho.gsd.benchmarks.generic.Constants;
import org.uminho.gsd.benchmarks.helpers.BenchmarkUtil;
import org.uminho.gsd.benchmarks.helpers.TPM_counter;
import org.uminho.gsd.benchmarks.helpers.ThinkTime;
import org.uminho.gsd.benchmarks.interfaces.Entity;
import org.uminho.gsd.benchmarks.interfaces.Workload.Operation;
import org.uminho.gsd.benchmarks.interfaces.Workload.WorkloadGeneratorInterface;
import org.uminho.gsd.benchmarks.interfaces.executor.DatabaseExecutorInterface;

import pt.unl.citi.tpcw.entities.Address;
import pt.unl.citi.tpcw.entities.Author;
import pt.unl.citi.tpcw.entities.CCXact;
import pt.unl.citi.tpcw.entities.Country;
import pt.unl.citi.tpcw.entities.Customer;
import pt.unl.citi.tpcw.entities.Filter;
import pt.unl.citi.tpcw.entities.Item;
import pt.unl.citi.tpcw.entities.Order;
import pt.unl.citi.tpcw.entities.OrderLine;
import pt.unl.citi.tpcw.entities.ShoppingCart;
import pt.unl.citi.tpcw.entities.ShoppingCartLine;
import pt.unl.citi.tpcw.util.LastOrders;
import pt.unl.citi.tpcw.util.RBTree;
import pt.unl.citi.tpcw.util.trove.THashMap;

public class Executor implements DatabaseExecutorInterface {
	/* Database */
	@Bootstrap(id = 1)
	// static RBTree countries;
	static Country[] countries; // read-only!
	@Bootstrap(id = 2)
	// static RBTree authors;
	static Author[] authors; // read-only!
	@Bootstrap(id = 3)
	static RBTree addresses;
	@Bootstrap(id = 4)
	static RBTree customers;
	@Bootstrap(id = 5)
	static RBTree orders;
	@Bootstrap(id = 6)
	static LastOrders lastOrders;
	@Bootstrap(id = 7)
	static THashMap<Integer, Order> lastCustomerOrder;
	@Bootstrap(id = 8)
	static RBTree orderLines;
	@Bootstrap(id = 9)
	// static RBTree items;
	static Item[] items; // fixed-size!
	@Bootstrap(id = 10)
	static THashMap<String, List<Item>> itemsBySubject;
	@Bootstrap(id = 11)
	static THashMap<String, List<Item>> itemsByAuthorLastName;
	@Bootstrap(id = 12)
	static THashMap<String, List<Item>> itemsByTitle;
	@Bootstrap(id = 13)
	static RBTree ccXacts;
	@Bootstrap(id = 14)
	static RBTree shopCarts;
	@Bootstrap(id = 15)
	static RBTree shopCartLines;

	public Executor(TPM_counter tpm_counter) {
		this.counter = tpm_counter;
	}

	// @Atomic
	private void createTrees(final int num_countries, final int num_authors,
			final int num_customers, final int num_items) {
		// countries = new RBTree();
		createCountries(num_countries);
		System.out.println("COUNTRIES created.");
		// authors = new RBTree();
		createAuthors(num_authors);
		System.out.println("AUTHORS created.");
		createAddresses();
		System.out.println("ADDRESSES created.");
		createCustomers();
		System.out.println("CUSTOMERS created.");
		createOrders();
		System.out.println("ORDERS created.");
		createLastOrders();
		System.out.println("LAST_ORDERS created.");
		createLastCustomerOrders();
		System.out.println("LAST_CUSTOMER_ORDER created.");
		createOrderLines();
		System.out.println("ORDER_LINES created.");
		// items = new RBTree();
		createItems(num_items);
		System.out.println("ITEMS created.");
		createItemsBySubject();
		System.out.println("ITEMS_BY_SUBJECT created.");
		createItemsByAuthorLastName();
		System.out.println("ITEMS_BY_AUTHOR created.");
		createItemsByTitle();
		System.out.println("ITEMS_BY_TITLE created.");
		createCcxact();
		System.out.println("CCXACT created.");
		createShoppingCarts();
		System.out.println("SHOPPING_CART created.");
		createShoppingCartLines();
		System.out.println("SHOPPING_CART_LINES created.");
	}

	@Atomic
	private final void createShoppingCartLines() {
		shopCartLines = new RBTree();
	}

	@Atomic
	private final void createShoppingCarts() {
		shopCarts = new RBTree();
	}

	@Atomic
	private final void createCcxact() {
		ccXacts = new RBTree();
	}

	@Atomic
	private final void createItemsByTitle() {
		itemsByTitle = new THashMap<String, List<Item>>();
	}

	@Atomic
	private final void createItemsByAuthorLastName() {
		itemsByAuthorLastName = new THashMap<String, List<Item>>();
	}

	@Atomic
	private final void createItemsBySubject() {
		itemsBySubject = new THashMap<String, List<Item>>(25); // # dif. subj.
	}

	@Atomic
	private final void createItems(final int num_items) {
		items = new Item[num_items];
	}

	@Atomic
	private final void createOrderLines() {
		orderLines = new RBTree();
	}

	@Atomic
	private final void createLastCustomerOrders() {
		lastCustomerOrder = new THashMap<Integer, Order>(/* num_customers */);
	}

	@Atomic
	private final void createLastOrders() {
		lastOrders = new LastOrders();
	}

	@Atomic
	private final void createOrders() {
		orders = new RBTree();
	}

	@Atomic
	private final void createCustomers() {
		customers = new RBTree();
	}

	@Atomic
	private final void createAddresses() {
		addresses = new RBTree();
	}

	@Atomic
	private final void createAuthors(final int num_authors) {
		authors = new Author[num_authors];
	}

	@Atomic
	private final void createCountries(final int num_countries) {
		countries = new Country[num_countries];
	}

	@Atomic
	public static final void insertCountry(int key, Country val) {
		// countries.insert(key, val);
		countries[key] = val;
	}

	// @Atomic
	public static final Country getCountry(int key) {
		// return (Country) countries.find(key);
		return countries[key];
	}

	// @Atomic
	public static final Author getAuthor(int key) {
		// return (Author) authors.find(key);
		return authors[key];
	}

	@Atomic
	public static final void insertAddress(int key, Address val) {
		final boolean b = addresses.insert(key, val);
		if (!b)
			throw new RuntimeError("Address(" + key + ") already exists.");
	}

	@Atomic
	public static final Address getAddress(int key) {
		return (Address) addresses.find(key);
	}

	@Atomic
	public static final Address getAddress(Filter<Address> f) {
		return addresses.find(f);
	}

	@Atomic
	public static final void insertCustomer(int key, Customer val) {
		final boolean b = customers.insert(key, val);
		if (!b)
			throw new RuntimeError("Customer(" + key + ") already exists.");
	}

	@Atomic
	public static final Customer getCustomer(int key) {
		return (Customer) customers.find(key);
	}

	@Atomic
	public static final void insertAuthor(int key, Author val) {
		// authors.insert(key, val);
		authors[key] = val;
	}

	@Atomic
	public static final List<Author> getAuthors() {
		final List<Author> results = new java.util.LinkedList<Author>();
		for (int i = 0; i < authors.length; i++) {
			results.add(authors[i]);
		}
		return results;
	}

	@Atomic
	public static final void insertItem(int key, Item val) {
		// items.insert(key, val);
		items[key] = val;
		List<Item> list;
		// subject index
		final String subject = val.I_SUBJECT;
		if (!itemsBySubject.containsKey(subject)) {
			list = new java.util.LinkedList<Item>();
			itemsBySubject.put(subject, list);
		} else {
			list = itemsBySubject.get(subject);
		}
		list.add(val);
		// author index
		final String lname = getAuthor(val.I_A_ID).A_LNAME;
		if (!itemsByAuthorLastName.containsKey(lname)) {
			list = new java.util.LinkedList<Item>();
			itemsByAuthorLastName.put(lname, list);
		} else {
			list = itemsByAuthorLastName.get(lname);
		}
		list.add(val);
		// title index
		final String title = val.I_TITLE;
		if (!itemsByTitle.containsKey(title)) {
			list = new java.util.LinkedList<Item>();
			itemsByTitle.put(title, list);
		} else {
			list = itemsByTitle.get(title);
		}
		list.add(val);
	}

	// @Atomic
	public static final Item getItem(int key) {
		// return (Item) items.find(key);
		return items[key];
	}

	@Atomic
	public static final List<Item> getItems() {
		final List<Item> results = new java.util.LinkedList<Item>();
		for (int i = 0; i < items.length; i++) {
			results.add(items[i]);
		}
		return results;
	}

	@Atomic
	public static final void insertOrder(int key, Order val) {
		orders.insert(key, val);
		lastOrders.prepend(val);
		lastCustomerOrder.put(val.O_C_ID, val);
	}

	// @Atomic
	// public static final List<Order> getOrders(Filter<Order> f) {
	// return orders.findAll(f);
	// }

	@Atomic
	public static final void insertOrderLine(int key, OrderLine val) {
		final Order order = (Order) orders.find(val.OL_O_ID);
		// order.orderLines.insert(key, val);
		order.orderLines.add(val);
	}

	// @Atomic
	// public static final List<OrderLine> getOrderLines(Order order,
	// Filter<OrderLine> f) {
	// return order.orderLines.findAll(f);
	// }

	@Atomic
	public static final CCXact getCCXact(int key) {
		return (CCXact) ccXacts.find(key);
	}

	@Atomic
	public static final void insertCcXact(int key, CCXact val) {
		final boolean b = ccXacts.insert(key, val);
		if (!b)
			throw new RuntimeError("CCXact(" + key + ") already exists.");
	}

	@Atomic
	public static final void insertShoppingCart(int key, ShoppingCart val) {
		final boolean b = shopCarts.insert(key, val);
		if (!b)
			throw new RuntimeError("ShoppingCart(" + key + ") already exists.");
	}

	@Atomic
	public static final ShoppingCart getShoppingCart(int key) {
		return (ShoppingCart) shopCarts.find(key);
	}

	@Atomic
	public static final ShoppingCartLine getShoppingCartLine(ShoppingCart cart,
			int key) {
		return (ShoppingCartLine) cart.cartLines.find(key);
	}

	@Atomic
	public static final List<ShoppingCartLine> getShoppingCartLines(
			ShoppingCart cart, Filter<ShoppingCartLine> f) {
		return cart.cartLines.findAll(f);
	}

	/**
	 * TPCW VARIABLES
	 */
	private String[] credit_cards = { "VISA", "MASTERCARD", "DISCOVER", "AMEX",
			"DINERS" };
	private String[] ship_types = { "AIR", "UPS", "FEDEX", "SHIP", "COURIER",
			"MAIL" };
	private String[] status_types = { "PROCESSING", "SHIPPED", "PENDING",
			"DENIED" };

	private Random random = new Random();

	/**
	 * Node id
	 */
	int node_id;
	/**
	 * The number of clients in onde node
	 */
	int one_node_clients;

	int addr_aux_id = 0;
	int num_operations = 0;
	ResultHandler client_result_handler;
	private TPM_counter counter;
	/**
	 * Think time*
	 */
	private long simulatedDelay;

	@Override
	public void start(WorkloadGeneratorInterface workload,
			BenchmarkNodeID nodeId, int operation_number, ResultHandler handler) {
		// TODO Auto-generated method stub
		this.node_id = nodeId.getId();
		client_result_handler = handler;
		this.num_operations = operation_number;
		int r = random.nextInt(100);
		long g_init_time = System.nanoTime();
		for (int operation = 0; operation < operation_number; operation++) {

			// long g_init_time = System.currentTimeMillis();

			try {
				Operation op = workload.getNextOperation();
				// long init_time = System.currentTimeMillis();
				long init_time = System.nanoTime();
				execute(op);
				// long end_time = System.currentTimeMillis();
				long end_time = System.nanoTime();
				client_result_handler.logResult(op.getOperation(),
						((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));

				simulatedDelay = ThinkTime.getThinkTime();

				if (simulatedDelay > 0) {
					Thread.sleep(simulatedDelay);
				}

			} catch (NoSuchFieldException e) {
				System.out.println("[ERROR:] THIS OPERATION DOES NOT EXIST: "
						+ e.getMessage());
			} catch (InterruptedException e) {
				System.out
						.println("[ERROR:] THINK TIME AFTER METHOD EXECUTION INTERRUPTED: "
								+ e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// long end_time = System.currentTimeMillis();
			counter.increment();
			// client_result_handler.logResult("OPERATIONS", (end_time -
			// g_init_time));

		}
		final long g_end_time = System.nanoTime();
		double g_time = g_end_time - g_init_time; // in ns
		g_time = g_time / 1000 / 1000 / 1000; // in s
		final double tps = num_operations / g_time;
		client_result_handler.logResult("TPS", (long) tps);

		// client_result_handler.getResulSet().put("bought", partialBought);
		// client_result_handler.getResulSet().put("total_bought", bought_qty);
		// client_result_handler.getResulSet().put("buying_actions",
		// bought_actions);
		// client_result_handler.getResulSet().put("bought_carts",
		// bought_carts);
		// client_result_handler.getResulSet().put("zeros", zeros);
	}

	@Override
	public void execute(Operation op) throws Exception {
		if (op == null) {
			System.out.println("[ERROR]: NULL OPERATION");
			return;
		}

		String method_name = op.getOperation();
//		System.out.println("Executor will execute " + method_name);

		if (method_name.equalsIgnoreCase(OP_POPULATE)) {
			createTrees(Constants.NUM_COUNTRIES, Constants.NUM_AUTHORS,
					Constants.NUM_CUSTOMERS, Constants.NUM_ITEMS);
		} else if (method_name.equalsIgnoreCase("OP_HOME")) {
			int costumer = (Integer) op.getParameter("COSTUMER");
			int item_id = (Integer) op.getParameter("ITEM");
			HomeOperation(costumer, item_id);
		} else if (method_name.equalsIgnoreCase("OP_NEW_PRODUCTS")) {
			String field = (String) op.getParameter("FIELD");
			newProducts(field);
		} else if (method_name.equalsIgnoreCase("OP_BEST_SELLERS")) {
			String field = (String) op.getParameter("FIELD");
			BestSellers(field);
		} else if (method_name.equalsIgnoreCase("OP_ITEM_INFO")) {
			int id = (Integer) op.getParameter("ITEM");
			productDetail(id);
		} else if (method_name.equalsIgnoreCase("OP_SEARCH")) {
			String term = (String) op.getParameter("TERM");
			String field = (String) op.getParameter("FIELD");
			doSearch(term, field);
		} else if (method_name.equalsIgnoreCase("OP_REGISTER")) {
			String customer = (String) op.getParameter("CUSTOMER");
			int id = getIDfromString(customer, Constants.NUM_CUSTOMERS);
			int process_id = getProcessId(customer);
			CustomerRegistration(process_id, id);
		} else if (method_name.equalsIgnoreCase("OP_LOGIN")) {
			String customer = (String) op.getParameter("CUSTOMER");
			int id = Integer.parseInt(customer);
			refreshSession(id);
		} else if (method_name.equalsIgnoreCase("OP_ORDER_INQUIRY")) {
			String customer = (String) op.getParameter("CUSTOMER");
			int customer_id = Integer.parseInt(customer);
			OrderInquiry(customer_id);
		} else if (method_name.equalsIgnoreCase("OP_ADMIN_CHANGE")) {
			int id = (Integer) op.getParameter("ITEM");
			AdminChange(id);
		} else if (method_name.equalsIgnoreCase("OP_SHOPPING_CART")) {
			boolean create = (Boolean) op.getParameter("CREATE");
			int item_id = (Integer) op.getParameter("ITEM");
			String cart_id = (String) op.getParameter("CART");
			int id = getIDfromString(cart_id);
			shoppingCartInteraction(item_id, create, id);
		} else if (method_name.equalsIgnoreCase("OP_BUY_REQUEST")) {
			String id = (String) op.getParameter("CART");
			int cart_id = getIDfromString(id);
			BuyRequest(cart_id);
		} else if (method_name.equalsIgnoreCase("OP_BUY_CONFIRM")) {
			String cart = (String) op.getParameter("CART");
			String custumer = (String) op.getParameter("CUSTOMER");
			int cust_id = Integer.parseInt(custumer.trim());
			int process_id = getProcessId(cart);
			int cart_id = getIDfromString(cart);
			BuyConfirm(cust_id, process_id, cart_id);
		}

		// TODO Auto-generated method stub

	}

	public final void HomeOperation(final int c_id, final int i_id) {
		// 2.3.3.1 If this User is a Customer and its C_ID is known, then the
		// SUT obtains the following information about the Customer:
		// ? C_FNAME or SC_C_FNAME if the shopping session exists for this
		// customer
		// ? C_LNAME or SC_C_LNAME if the shopping session exists for this
		// customer
		final Customer cust = getCustomer(c_id);
		final String fname = cust.C_FNAME;
		final String lname = cust.C_LNAME;
		// 2.3.3.2 The SUT executes the Promotional Processing, as defined in
		// Clause 2.2.18.
		final Item it = getItem(i_id);
		promotionalProcessing(it);
	}

	@Atomic
	private final void promotionalProcessing(final Item it) {
		final String thumb1 = getItem(it.I_RELATED1).I_THUMBNAIL;
		final String thumb2 = getItem(it.I_RELATED2).I_THUMBNAIL;
		final String thumb3 = getItem(it.I_RELATED3).I_THUMBNAIL;
		final String thumb4 = getItem(it.I_RELATED4).I_THUMBNAIL;
		final String thumb5 = getItem(it.I_RELATED5).I_THUMBNAIL;
	}

	private final void newProducts(final String subject) {
		// 2.12.3.1 Of the entire set of (I_ID, I_TITLE) pairs for items on the
		// selected subject, sorted by descending I_PUB_DATE and ascending
		// I_TITLE, the first 50 pairs (or less if the entire set contains less
		// than 50 pairs) are obtained.
		// The following search predicate is used (where <string> is the content
		// of SUBJECT_STRING):
		// I_SUBJECT = “<string>”
		// The database reads to generate the list of 50 New Products may be
		// performed at an isolation level of 0, (see Clause 3.1.4.1).
		final List<Item> items = itemsBySubject.get(subject);
		if (items == null)
			// no items with such subject
			return;

		/*
		 * FIXME if we sort non-transactionally, which makes sense, then we
		 * should return a list with copies instead of the real objects?
		 */
		Collections.sort(items, new java.util.Comparator<Item>() {
			public int compare(Item o1, Item o2) {
				final Date d1 = getPubDate(o1);
				// final Date d1 = o1.I_PUB_DATE;
				final Date d2 = getPubDate(o2);
				// final Date d2 = o2.I_PUB_DATE;
				if (d1.after(d2)) {
					return -1;
				} else if (d1.equals(d2)) {
					final String t1 = o1.I_TITLE;
					final String t2 = o2.I_TITLE;
					return t1.compareTo(t2);
				} else {
					return 1;
				}
			}
		});
		final List<Item> fiftyItems = new java.util.LinkedList<Item>();
		int n = 0;
		for (Item i : items) {
			fiftyItems.add(i);
			n++;
			if (n == 50)
				break;
		}
		// StringBuilder sb = new StringBuilder();
		// for (Item i : fiftyItems) {
		// sb.append(i.I_PUB_DATE + ", " + i.I_TITLE + ", " + i.I_SUBJECT);
		// sb.append("\n");
		// }
		// System.err.println(sb.toString());
	}

	@Atomic
	private final Date getPubDate(final Item item) {
		return item.I_PUB_DATE;
	}

	public final void BestSellers(final String subject) {
		// 2.13.3.1 Of the entire set of (I_ID, I_TITLE) pairs for items on the
		// selected subject, the first 50 pairs (or less if the entire set
		// contains less than 50 pairs) are obtained. The following search
		// predicate is used (where <string> is the content of SUBJECT_STRING):
		// I_SUBJECT = “<string>”
		// for the order lines of the 3,333 most recent orders
		// (where O_ID = OL_O_ID) based on O_DATE and sorted by descending
		// sum(OL_QTY) grouped on OL_I_ID.
		// The database reads to generate the list of 50 Best Sellers may be
		// performed at an isolation level of 0 (see Clause 3.1.4.1).
		final List<Order> orders = lastOrders.getList();
		/*
		 * FIXME if we sort non-transactionally, which makes sense, then we
		 * should return a list with copies instead of the real objects?
		 */
		// Collections.sort(orders, new java.util.Comparator<Order>() {
		// public int compare(Order o1, Order o2) {
		// final Date d1 = getDate(o1);
		// // final Date d1 = o1.O_DATE;
		// final Date d2 = getDate(o2);
		// // final Date d2 = o2.O_DATE;
		// if (d1.after(d2)) {
		// return -1;
		// } else if (d1.equals(d2)) {
		// return 0;
		// } else {
		// return 1;
		// }
		// }
		// });
		// final List<Order> recentOrders = new java.util.LinkedList<Order>();
		// int n = 0;
		// for (Order o : orders) {
		// recentOrders.add(o);
		// n++;
		// if (n == 3333)
		// break;
		// }
		final Map<Integer, Integer> id2qty = new java.util.TreeMap<Integer, Integer>();
		for (Order o : orders) {
			for (OrderLine ol : o.orderLines) {
				final int id = ol.OL_I_ID;
				final int qty = ol.OL_QTY;
				final Integer v = id2qty.get(id);
				id2qty.put(id, v == null ? qty : v + qty);
			}
		}
		final List<Entry<Integer, Integer>> qties = new java.util.LinkedList<Entry<Integer, Integer>>(
				id2qty.entrySet());
		Collections.sort(qties,
				new java.util.Comparator<Entry<Integer, Integer>>() {
					public int compare(Entry<Integer, Integer> o1,
							Entry<Integer, Integer> o2) {
						if (o1.getValue().intValue() > o2.getValue().intValue())
							return 1;
						else if (o1.getValue().intValue() == o2.getValue()
								.intValue())
							return 0;
						else
							return -1;
					}
				});
		final List<Item> items = new java.util.LinkedList<Item>();
		for (Entry<Integer, Integer> e : qties) {
			final Item i = getItem(e.getKey());
			if (i.I_SUBJECT.equals(subject)) {
				items.add(i);
			}
		}
	}

	public final void productDetail(final int i_id) {
		final Item i = getItem(i_id);
		final String i_TITLE = i.I_TITLE;
		final Author a = getAuthor(i.I_A_ID);
		final String a_FNAME = a.A_FNAME;
		final String a_LNAME = a.A_LNAME;
		// I_PUB_DATE
		final String i_PUBLISHER = i.I_PUBLISHER;
		final String i_SUBJECT = i.I_SUBJECT;
		final String i_DESC = i.I_DESC;
		// I_IMAGE
		// I_COST
		final double i_SRP = i.I_SRP;
		final Date i_AVAIL = i.I_AVAIL;
		final String i_ISBN = i.I_ISBN;
		final int i_PAGE = i.I_PAGE;
		final String i_BACKING = i.I_BACKING;
		final String i_DIMENSION = i.I_DIMENSION;
		productDetailAtomic(i);
	}

	@Atomic
	private final void productDetailAtomic(final Item i) {
		final Date i_PUB_DATE = i.I_PUB_DATE;
		final String i_IMAGE = i.I_IMAGE;
		final double i_COST = getItemCost(i);
	}

	public final void doSearch(final String type, final String search) {
		// 2.11.3.1 Of the entire set of (I_ID, I_TITLE) pairs for items that
		// match the selection criteria, sorted by ascending I_TITLE, the first
		// 50 pairs (or less if the entire set contains less than 50 pairs) are
		// obtained.
		// The SUT is permitted to execute the search with a commercially
		// available text search engine (see Clause 2.2.15 and Clause 6.3.3.1).
		// The following search predicates are used (where <string> is the
		// content of SEARCH_STRING):
		// ? If SEARCH_TYPE = “AUTHOR”, the search predicate is:
		// <string> is found at the beginning of any word in A_LNAME
		// ? If SEARCH_TYPE = “TITLE”, the search predicate is:
		// <string> is found at the beginning of any word in I_TITLE
		// ? If SEARCH_TYPE = “SUBJECT”, the search predicate is:
		// I_SUBJECT = “<string>”
		// Comment: Clause 6.3.3.1 defines the requirements for search results
		// consistency.
		// ? Search Result web interaction. The response page for author or
		// title searches need not reflect any changes committed to the database
		// since the initial population.
		// ? Search Result web interaction. The response page for subject
		// searches must reflect the effects of any changes committed to the
		// database by any web interaction which completed at least 30 seconds
		// before the Search Result web interaction was requested.

		if (type.equalsIgnoreCase("AUTHOR")) {
			doSearchAuthor(search);
		} else if (type.equalsIgnoreCase("TITLE")) {
			doSearchTitle(search);
		} else { // SUBJECT
			doSearchSubject(search);
		}
	}

	private final void doSearchAuthor(final String search) {
		// final List<Item> items = getItems(new Filter<Item>() {
		// public boolean filter(Item obj) {
		// final Author a = getAuthor(obj.I_A_ID);
		// if (a.A_LNAME.equals(search))
		// return true;
		// else
		// return false;
		// }
		// });
		final List<Item> items = itemsByAuthorLastName.get(search);
		if (items == null)
			// no items for such author
			return;
		/*
		 * FIXME if we sort non-transactionally, which makes sense, then we
		 * should return a list with copies instead of the real objects?
		 */
		Collections.sort(items, new java.util.Comparator<Item>() {
			public int compare(final Item o1, final Item o2) {
				final String t1 = o1.I_TITLE;
				final String t2 = o2.I_TITLE;
				return t1.compareTo(t2);
			}
		});
		final List<Item> fiftyItems = new java.util.LinkedList<Item>();
		int n = 0;
		for (final Item i : items) {
			fiftyItems.add(0, i);
			n++;
			if (n == 50)
				break;
		}
	}

	private final void doSearchTitle(final String search) {
		// final List<Item> items = getItems(new Filter<Item>() {
		// public boolean filter(Item obj) {
		// return obj.I_TITLE.equals(search);
		// }
		// });
		final List<Item> items = itemsByTitle.get(search);
		if (items == null)
			// no items for such title
			return;
		/*
		 * FIXME if we sort non-transactionally, which makes sense, then we
		 * should return a list with copies instead of the real objects?
		 */
		Collections.sort(items, new java.util.Comparator<Item>() {
			public int compare(final Item o1, final Item o2) {
				final String t1 = o1.I_TITLE;
				final String t2 = o2.I_TITLE;
				return t1.compareTo(t2);
			}
		});
		final List<Item> fiftyItems = new java.util.LinkedList<Item>();
		int n = 0;
		for (final Item i : items) {
			fiftyItems.add(0, i);
			n++;
			if (n == 50)
				break;
		}
	}

	private final void doSearchSubject(final String search) {
		// final List<Item> items = getItems(new Filter<Item>() {
		// public boolean filter(Item obj) {
		// return obj.I_SUBJECT.equals(search);
		// }
		// });
		final List<Item> items = itemsBySubject.get(search);
		if (items == null)
			// no items for such subject
			return;
		/*
		 * FIXME if we sort non-transactionally, which makes sense, then we
		 * should return a list with copies instead of the real objects?
		 */
		Collections.sort(items, new java.util.Comparator<Item>() {
			public int compare(final Item o1, final Item o2) {
				final String t1 = o1.I_TITLE;
				final String t2 = o2.I_TITLE;
				return t1.compareTo(t2);
			}
		});
		final List<Item> fiftyItems = new java.util.LinkedList<Item>();
		int n = 0;
		for (final Item i : items) {
			fiftyItems.add(0, i);
			n++;
			if (n == 50)
				break;
		}
	}

	public void CustomerRegistration(int process_id, int id) {
		Customer cust = createCostumer(process_id, id);
		insertCustomer(cust.C_ID, cust);
	}

	public void refreshSession(int C_ID) {
		// 2.6.3.1 If RETURNING_FLAG is set to “Y”, then the SUT executes the
		// following steps:
		// ? Obtains the following information about the customer with
		// (C_UNAME = UNAME):
		// ? C_ID or SC_C_ID
		// ? C_PASSWD
		// ? C_FNAME or SC_C_FNAME
		// ? C_LNAME or SC_C_LNAME
		// ? ADDR_STREET1
		// ? ADDR_STREET2
		// ? ADDR_CITY
		// ? ADDR_STATE
		// ? ADDR_ZIP
		// ? CO_NAME
		// ? C_PHONE
		// ? C_EMAIL
		// ? C_BIRTHDATE
		// ? C_DATA
		// ? C_DISCOUNT or SC_C_DISCOUNT
		Customer customer = getCustomer(C_ID);
		refreshSessionReadCustomerInfo(customer);
		// Update the following information within a single database
		// transaction:
		// <start transaction>
		// ? C_LOGIN is set to current date/time
		// ? C_EXPIRATION is set to C_LOGIN + 2 hours
		// <end transaction>
		refreshSessionLoginInfo(customer);
		// ? PASSWD is compared to C_PASSWD.
	}

	private final void refreshSessionReadCustomerInfo(final Customer customer) {
		final int c_id = customer.C_ID;
		final String c_passwd = customer.C_PASSWD;
		final String c_fname = customer.C_FNAME;
		final String c_lname = customer.C_LNAME;
		final Address address = getAddress(customer.C_ADDR_ID);
		final String a_street1 = address.ADDR_STREET1;
		final String a_street2 = address.ADDR_STREET2;
		final String a_city = address.ADDR_CITY;
		final String a_state = address.ADDR_STATE;
		final String a_zip = address.ADDR_ZIP;
		final String co_name = getCountry(address.ADDR_CO_ID).CO_NAME;
		final String c_phone = customer.C_PHONE;
		final String c_email = customer.C_EMAIL;
		final Date c_bd = customer.C_BIRTHDATE;
		final String c_data = customer.C_DATA;
		final double c_dcnt = customer.C_DISCOUNT;
	}

	@Atomic
	private final void refreshSessionLoginInfo(final Customer customer) {
		customer.C_LOGIN = new Date(System.currentTimeMillis());
		// 2 hours in milliseconds
		customer.C_EXPIRATION = new Date(System.currentTimeMillis() + 7200000);
	}

	public void BuyRequest(int shopping_id) {
		ShoppingCart cart = getShoppingCart(shopping_id);
		List<ShoppingCartLine> cartLines = getShoppingCartLines(cart,
				new Filter<ShoppingCartLine>() {
					public boolean filter(ShoppingCartLine obj) {
						return true;
					};
				});
		// 2.6.3.3 The SUT executes the following steps as an atomic set of
		// operations:
		// <start atomic set>
		// ? Updates the following fields of the CART:
		// ? The value of SCL_COST is set to the current value of I_COST
		// from the ITEM table for each item in the CART.
		// ? Update the SC_DATE
		// ? Obtains the following information for each item in the CART:
		// ? SCL_TITLE
		// ? SCL_COST
		// ? SCL_SRP
		// ? SCL_BACKING
		// ? SCL_QTY
		// ? Calculates and updates the following fields of the CART:
		// ? SC_SUB_TOTAL = sum(SCL_COST * SCL_QTY) * (1 – SC_C_DISCOUNT)
		// ? SC_TAX = SC_SUB_TOTAL * 0.0825
		// ? SC_SHIP_COST = 3.00 + (1.00 * sum(SCL_QTY))
		// ? SC_TOTAL = SC_SUB_TOTAL + SC_SHIP_COST + SC_TAX
		// <end atomic set>
		BuyRequestAtomicOp(cart, cartLines);
	}

	@Atomic
	private final void BuyRequestAtomicOp(final ShoppingCart cart,
			final List<ShoppingCartLine> cartLines) {
		int sum_scl_cost_qty = 0;
		int sum_scl_qty = 0;
		for (ShoppingCartLine cartLine : cartLines) {
			cartLine.SCL_COST = getItem(cartLine.SCL_I_ID).I_COST;
			int scl_qty = cartLine.SCL_QTY;
			sum_scl_cost_qty += cartLine.SCL_COST + scl_qty;
			sum_scl_qty += scl_qty;
			final String scl_title = cartLine.SCL_TITLE;
			final double scl_srp = cartLine.SCL_SRP;
			final String scl_back = cartLine.SCL_BACKING;
		}
		cart.SC_DATE = new Date(System.currentTimeMillis());
		final double sc_sub_total = sum_scl_cost_qty * (1 - cart.SC_C_DISCOUNT);
		cart.SC_SUB_TOTAL = sc_sub_total;
		final double sc_tax = sc_sub_total * 0.0825;
		cart.SC_TAX = sc_tax;
		final double sc_ship_cost = 3.00 + (1.00 * sum_scl_qty);
		cart.SC_SHIP_COST = sc_ship_cost;
		cart.SC_TOTAL = sc_sub_total + sc_ship_cost + sc_tax;
	}

	public void OrderInquiry(final int customer) {
		// 2.9.3.2 The SUT obtains the following information about the last
		// order of the customer based on its C_UNAME and within a single
		// database transaction:
		// ? O_ID
		// ? C_FNAME
		// ? C_LNAME
		// ? C_PHONE
		// ? C_EMAIL
		// ? O_DATE
		// ? O_SUB_TOTAL
		// ? O_TAX
		// ? O_TOTAL
		// ? O_SHIP_TYPE
		// ? O_SHIP_DATE
		// ? O_STATUS
		// ? For the billing address:
		// ? ADDR_STREET1
		// ? ADDR_STREET2
		// ? ADDR_CITY
		// ? ADDR_STATE
		// ? ADDR_ZIP
		// ? CO_NAME
		// ? For the shipping address:
		// ? ADDR_STREET1
		// ? ADDR_STREET2
		// ? ADDR_CITY
		// ? ADDR_STATE
		// ? ADDR_ZIP
		// ? CO_NAME
		// ? For each item on the order:
		// ? OL_I_ID
		// ? I_TITLE
		// ? I_PUBLISHER
		// ? I_COST
		// ? OL_QTY
		// ? OL_DISCOUNT
		// ? OL_COMMENTS
		// ? From the credit card transaction of the order:
		// ? CX_TYPE
		// ? CX_AUTH_ID
		// List<Order> orders = getOrders(new Filter<Order>() {
		// public boolean filter(Order obj) {
		// return obj.O_C_ID == customer;
		// }
		// });
		// if (orders.isEmpty()) // client never ordered
		// return;
		/*
		 * FIXME if we sort non-transactionally, which makes sense, then we
		 * should return a list with copies instead of the real objects
		 */
		// Collections.sort(orders, new java.util.Comparator<Order>() {
		// public int compare(Order o1, Order o2) {
		// final Date d1 = getDate(o1);
		// // final Date d1 = o1.O_DATE;
		// final Date d2 = getDate(o2);
		// // final Date d2 = o2.O_DATE;
		// if (d1.after(d2)) {
		// return -1;
		// } else if (d1.equals(d2)) {
		// return 0;
		// } else {
		// return 1;
		// }
		// }
		// });
		// Order last_order = orders.get(0);
		Order last_order = lastCustomerOrder.get(customer);
		if (last_order == null)
			// client never ordered
			return;
		CCXact cc_xact = getCCXact(last_order.O_ID);
		OrderInquiryInfo(customer, last_order, cc_xact);
	}

	@Atomic
	private final Date getDate(Order order) {
		return order.O_DATE;
	}

	// @Atomic
	private final void OrderInquiryInfo(final int c_ID, final Order last_order,
			final CCXact xact) {
		final Customer c = getCustomer(c_ID);
		final int o_id = last_order.O_ID;
		final String c_fname = c.C_FNAME;
		final String c_lname = c.C_LNAME;
		final String c_phone = c.C_PHONE;
		final String c_email = c.C_EMAIL;
		final Date o_date = last_order.O_DATE;
		final double o_sub_total = last_order.O_SUB_TOTAL;
		final double o_tax = last_order.O_TAX;
		final double o_total = last_order.O_TOTAL;
		final String o_ship_type = last_order.O_SHIP_TYPE;
		final Date o_ship_date = last_order.O_SHIP_DATE;
		final String o_status = last_order.O_STATUS;
		final Address bill_a = getAddress(last_order.O_BILL_ADDR_ID);
		final String bill_a_street1 = bill_a.ADDR_STREET1;
		final String bill_a_street2 = bill_a.ADDR_STREET2;
		final String bill_a_city = bill_a.ADDR_CITY;
		final String bill_a_state = bill_a.ADDR_STATE;
		final String bill_a_zip = bill_a.ADDR_ZIP;
		final String bill_co_name = getCountry(bill_a.ADDR_CO_ID).CO_NAME;
		final Address ship_a = getAddress(last_order.O_SHIP_ADDR_ID);
		final String ship_a_street1 = ship_a.ADDR_STREET1;
		final String ship_a_street2 = ship_a.ADDR_STREET2;
		final String ship_a_city = ship_a.ADDR_CITY;
		final String ship_a_state = ship_a.ADDR_STATE;
		final String ship_a_zip = ship_a.ADDR_ZIP;
		final String ship_co_name = getCountry(ship_a.ADDR_CO_ID).CO_NAME;
		// List<OrderLine> orderLines = last_order.orderLines
		// .findAll(new Filter<OrderLine>() {
		// public boolean filter(OrderLine obj) {
		// return true;
		// }
		// });
		for (OrderLine ol : last_order.orderLines) {
			final Item i = getItem(ol.OL_I_ID);
			final int i_id = i.I_ID;
			final String i_title = i.I_TITLE;
			final String i_pub = i.I_PUBLISHER;
			final double i_cost = getItemCost(i);
			final int ol_qty = ol.OL_QTY;
			final double ol_disc = ol.OL_DISCOUNT;
			final String ol_com = ol.OL_COMMENT;
		}
		final String cx_type = xact.CX_TYPE;
		final int cx_auth = xact.CX_AUTH_ID;
	}

	@Atomic
	private double getItemCost(final Item i) {
		return i.I_COST;
	}

	public void AdminChange(int item_id) {
		// 2.15.3.1 The SUT obtains the following data for the targeted item:
		// ? I_SRP
		// ? I_COST
		// ? I_TITLE
		// ? I_IMAGE
		// ? I_THUMBNAIL
		// ? A_FNAME
		// ? A_LNAME
		final Item i = getItem(item_id);
		final double i_srp = i.I_SRP;
		final String i_title = i.I_TITLE;
		final Author a = getAuthor(i.I_A_ID);
		final String a_fname = a.A_FNAME;
		final String a_lname = a.A_LNAME;
		AdminChangeReadItem(i);
		// 2.16.3.2 The SUT updates the targeted item with:
		// (I_COST = I_NEW_COST),
		// (I_IMAGE = I_NEW_IMAGE),
		// (I_THUMBNAIL = I_NEW_ THUMBNAIL) and
		// (I_PUB_DATE = <current-date>)
		// within a single database transaction.
		final double i_cost = random.nextInt(100);
		final String i_image = new String("img" + random.nextInt(1000) % 100
				+ "/image_" + random.nextInt(1000) + ".gif");
		final String i_thumb = i_image.replace("image", "thumb");
		AdminChangeUpdateItem(i, i_cost, i_image, i_thumb);
		// 2.16.3.3 The SUT performs the following processing steps:
		// ? Of all the orders sorted by descending O_DATE, obtain the set of
		// the first 10,000.
		// ? From all orders that include the targeted item in the above set,
		// obtain the set of unique customers that placed these orders.
		// ? From all customers in the above set, obtain the list of unique
		// items ordered by these customers within the above set of 10,000
		// orders, and sort these items by descending aggregated quantity
		// (i.e., sum(OL_QTY) for each unique OL_I_ID).
		// ? If the sorted list above consists of 5 or more items, excluding
		// I_ID:
		// ? Then, obtain the set (I_ID1, I_ID2, I_ID3, I_ID4, I_ID5) of
		// the first five items.
		// ? If the sorted list above contains between 1 and 4 items,
		// excluding I_ID:
		// ? Then, increment I_ID from the last item in the list until 5
		// items are obtained. For example, if the list contains only
		// 3 items, then (I_ID4 = I_ID3 + 1) and (I_ID5 = I_ID3 + 2),
		// wrapping back to the beginning of the I_ID range if the end
		// is reached and skipping duplicate items.
		// ? If the sorted list above contains no item, or only I_ID:
		// ? Then, create a list of 5 items by incrementing the targeted
		// I_ID by steps of 7, such that (I_ID1 = I_ID + 7),
		// (I_ID2 = I_ID + 14) (I_ID3 = I_ID + 21) ((I_ID4 = I_ID + 28)
		// and (I_ID5 = I_ID + 35), wrapping back to the beginning of
		// the I_ID range if the end is reached.
		// ? Update the targeted item with:
		// (I_RELATED1 = I_ID1),
		// (I_RELATED2 = I_ID2),
		// (I_RELATED3 = I_ID3),
		// (I_RELATED4 = I_ID4) and
		// (I_RELATED5 = I_ID5).
		// ? Calculates the discount as I_SRP minus I_COST and displays the
		// discount as the “You Save” amount.
		/* TODO */
	}

	@Atomic
	private final void AdminChangeUpdateItem(final Item i, final double i_cost,
			final String i_image, final String i_thumb) {
		i.I_COST = i_cost;
		i.I_IMAGE = i_image;
		i.I_THUMBNAIL = i_thumb;
		i.I_PUB_DATE = new Date(System.currentTimeMillis());
	}

	@Atomic
	private final void AdminChangeReadItem(final Item i) {
		final double i_cost = i.I_COST;
		final String i_image = i.I_IMAGE;
		final String i_thumb = i.I_THUMBNAIL;

	}

	public int shoppingCartInteraction(int i_id, boolean create, int SHOPPING_ID) {
		ShoppingCart cart = null;
		if (create) {
			// 2.4.3.1 If the SHOPPING_ID is not known, then the SUT creates a
			// new
			// unique SHOPPING_ID. If there is no CART associated with this
			// SHOPPING_ID, then the SUT creates an associated CART initialized
			// as
			// follows:
			// ? SC_SHOPPING_ID = SHOPPING_ID
			// ? SC_DATE = current date and time on the SUT
			final Date sC_DATE = new Date(System.currentTimeMillis());
			final int sC_SHOPPING_ID = SHOPPING_ID;
			cart = new ShoppingCart(sC_SHOPPING_ID, 0, sC_DATE, 0, 0, 0, 0,
					null, null, 0);
			insertShoppingCart(cart.SC_SHOPPING_ID, cart);
		} else {
			cart = getShoppingCart(SHOPPING_ID);
		}
		// 2.4.3.3 If ADD_FLAG = "Y" or the CART is empty or (I_ID, I_QTY) pairs
		// are not empty then do the following updates. (Comment: there are
		// cases
		// where these conditions are not met, processing should skip to clause
		// 2.4.3.4) Based on SHOPPING_ID, the SUT updates the associated CART as
		// an atomic set of operations as follows:
		// <start atomic set>
		// ? If ADD_FLAG = “Y” (and the optional CART limit of 100 items has
		// not been reached):
		// ? If I_ID = SCL_I_ID (i.e., the item already exists in the
		// CART):
		// ? SCL_QTY = SCL_QTY + 1
		// (i.e., increment quantity by 1 for SCL_I_ID)
		// ? Else (i.e., the item does not already exists in CART):
		// ? The SUT obtains the following information about the item
		// I_ID:
		// ? I_COST
		// ? I_SRP
		// ? I_TITLE
		// ? I_BACKING
		// ? The SUT adds the item to the CART with:
		// ? SCL_I_ID = I_ID
		// ? SCL_QTY = 1
		// ? SCL_COST = I_COST
		// ? SCL_SRP = I_SRP
		// ? SCL_TITLE = I_TITLE
		// ? SCL_BACKING = I_BACKING
		shoppingCartAdd(i_id, cart);
		/* TODO stuff missing! */
		return SHOPPING_ID;
	}

	@Atomic
	private final void shoppingCartAdd(final int i_id, final ShoppingCart cart) {
		ShoppingCartLine cartLine = getShoppingCartLine(cart, i_id);
		if (cartLine != null) {
			cartLine.SCL_QTY++;
		} else {
			final Item item = getItem(i_id);
			final double i_cost = item.I_COST;
			final double i_srp = item.I_SRP;
			final String i_title = item.I_TITLE;
			final String i_back = item.I_BACKING;
			cartLine = new ShoppingCartLine(i_id, 1, i_cost, i_srp, i_title,
					i_back);
			cart.cartLines.insert(i_id, cartLine);
		}
	}

	public void BuyConfirm(int customer_id, int process_id, int cart_id) {
		final Customer c = getCustomer(customer_id);
		final ShoppingCart sc = getShoppingCart(cart_id);
		int ship_addr_id = -1;
		final double decision = random.nextDouble();
		// ? On a randomly selected 5% of the time, the shipping address is
		// updated with (STREET_1, STREET_2, CITY, STATE, ZIP, COUNTRY)
		// generated according to Clause 4.7.1
		if (decision < 0.05) {
			final int id = getIDfromValues(node_id, process_id, addr_aux_id,
					Constants.NUM_ADDRESSES);
			addr_aux_id++;
			Address address = generateAddress(id);
			address = enterAddress(address);
			ship_addr_id = address.ADDR_ID;
		} else {
			ship_addr_id = c.C_ADDR_ID;
		}
		final String o_ship_type = ship_types[random.nextInt(ship_types.length)];
		final String cx_type = credit_cards[random.nextInt(credit_cards.length)];
		final int ship_days = random.nextInt(7) + 1;
		final int expiry_days = random.nextInt(730) + 1;
		final String status = status_types[2];

		BuyConfirmAtomicOp(process_id, cart_id, ship_addr_id, c, sc,
				o_ship_type, cx_type, ship_days, expiry_days, status);
	}

	@Atomic
	private final void BuyConfirmAtomicOp(int process_id, int cart_id,
			final int ship_addr_id, final Customer c, final ShoppingCart sc,
			final String o_ship_type, final String cx_type,
			final int ship_days, final int expiry_days, final String status) {
		// 2.7.3.3 The SUT executes the following steps as an atomic set of
		// operations:
		// <start atomic set>
		// ? Creates a new order as follows, within a single database
		// transaction:
		// <start transaction>
		// ? A record is added in the ORDER table with:
		// ? O_ID is unique within the ORDER table (not necessarily
		// sequential or contiguous)
		// ? O_C_ID is set to C_ID or SC_C_ID
		// ? O_DATE is set to the current operating system date and
		// time
		// ? O_SUB_TOTAL is set to SC_SUB_TOTAL
		// ? O_TAX is set to SC_TAX
		// ? O_TOTAL is set to SC_TOTAL
		// ? O_SHIP_TYPE is set to SHIPPING
		// ? O_SHIP_DATE is set to current operating system date +
		// random within [1 .. 7] days
		// ? O_BILL_ADDR_ID is set to C_ADDR_ID
		// ? If the shipping address was passed, O_SHIP_ADDR_ID is set
		// to ADDR_ID else the O_SHIP_ADDR_ID is set to C_ADDR_ID
		// ? O_STATUS is set to “Pending”
		final int o_ID = cart_id;
		final int o_C_ID = c.C_ID;
		final Date o_DATE = new Date();
		final double o_SUB_TOTAL = sc.SC_SUB_TOTAL;
		final double o_TAX = sc.SC_TAX;
		final double o_TOTAL = sc.SC_TOTAL;
		final String o_SHIP_TYPE = o_ship_type;
		final long ship_date = System.currentTimeMillis()
				+ (24 * 3600000 * ship_days);
		final Date o_SHIP_DATE = new Date(ship_date);
		final int o_BILL_ADDR_ID = c.C_ADDR_ID;
		final int o_SHIP_ADDR_ID = ship_addr_id;
		final String o_STATUS = status;
		final Order order = new Order(o_ID, o_C_ID, o_DATE, o_SUB_TOTAL, o_TAX,
				o_TOTAL, o_SHIP_TYPE, o_SHIP_DATE, o_BILL_ADDR_ID,
				o_SHIP_ADDR_ID, o_STATUS);
		orders.insert(cart_id, order);
		// ? For each item in the CART, a record is added in the ORDER_LINE
		// table with:
		// ? OL_ID is unique within the ORDER_LINE record for the order
		// (not necessarily sequential or contiguous)
		// ? OL_O_ID is set to O_ID
		// ? OL_I_ID is set to SCL_I_ID
		// ? OL_QTY is set to SCL_QTY
		// ? OL_DISCOUNT is set to C_DISCOUNT or SC_C_DISCOUNT
		// ? OL_COMMENTS is set to a random a -string [20 ...100]
		// ? For each item in the CART, I_STOCK is retrieved from the item
		// table where I_ID = SCL_I_ID. If I_STOCK exceeds SCL_QTY by 10 or
		// more, then I_STOCK is decreased by SCL_QTY; otherwise I_STOCK is
		// updated to (I_STOCK - SCL_QTY ) + 21.
		final List<ShoppingCartLine> cartLines = getShoppingCartLines(sc,
				new Filter<ShoppingCartLine>() {
					public boolean filter(ShoppingCartLine obj) {
						return true;
					}
				});
		int id = 0;
		final double oL_DISCOUNT = c.C_DISCOUNT;
		for (final ShoppingCartLine scl : cartLines) {
			final int oL_ID = id;
			final int oL_O_ID = o_ID;
			final int oL_I_ID = scl.SCL_I_ID;
			final int oL_QTY = scl.SCL_QTY;
			final String oL_COMMENT = BenchmarkUtil.getRandomAString(20, 100);
			final OrderLine ol = new OrderLine(oL_ID, oL_O_ID, oL_I_ID, oL_QTY,
					oL_DISCOUNT, oL_COMMENT);
			order.orderLines.add(ol);
			final Item i = getItem(oL_I_ID);
			if (i.I_STOCK > oL_QTY + 10) {
				i.I_STOCK -= oL_QTY;
			} else {
				i.I_STOCK = (i.I_STOCK - oL_QTY) + 21;
			}
			id++;
		}
		// <end transaction>
		// ? Creates a new credit card record as follows, within a single
		// database transaction:
		// <start transaction>
		// ? A record is added to the CC_XACTS table with:
		// ? CX_O_ID is set to O_ID
		// ? CX_TYPE is set to CC_TYPE
		// ? CX_NUM is set to CC_NUMBER
		// ? CX_NAME is set to CC_NAME
		// ? CX_EXPIRY is set to CC_EXPIRY
		// ? CX_AUTH_ID is set to AUTH_ID
		// ? CX_XACT_AMT is set to SC_TOTAL
		// ? CX_XACT_DATE is set to the Current date and time on the
		// SUT
		// ? CX_CO_ID is set to COUNTRY from the shipping address
		final int cX_O_ID = o_ID;
		final String cX_TYPE = cx_type;
		final int cX_CC_NUM = BenchmarkUtil.getRandomNString(16);
		final String cX_CC_NAME = c.C_FNAME + " " + c.C_LNAME;
		final long expiry_date = System.currentTimeMillis()
				+ (24 * 3600000 * expiry_days);
		final Date cX_EXPIRY = new Date(expiry_date);
		final int cX_AUTH_ID = 0; // unused
		final double cX_XACT_AMT = o_TOTAL;
		final Date cX_XACT_DATE = new Date();
		final int cX_CO_ID = o_SHIP_ADDR_ID;
		final CCXact xact = new CCXact(cX_O_ID, cX_TYPE, cX_CC_NUM, cX_CC_NAME,
				cX_EXPIRY, cX_AUTH_ID, cX_XACT_AMT, cX_XACT_DATE, cX_CO_ID);
		ccXacts.insert(cX_O_ID, xact);
		// <end transaction>
		// ? Clears all SCL_* items from the CART and updates SC_DATE to current
		// date and time
		// <end atomic set>
		sc.cartLines = new RBTree();
		sc.SC_DATE = new Date();
	}

	private int getProcessId(String id) {
		String[] parts = id.trim().split("\\.");
		int process_id = Integer.parseInt(parts[1]);
		return process_id;
	}

	private int getIDfromString(String id) {
		String[] parts = id.trim().split("\\.");

		int node_id = Integer.parseInt(parts[0]);
		int process_id = Integer.parseInt(parts[1]);

		int length_proc = (this.one_node_clients + "").length();

		int id_id = Integer.parseInt(parts[2]);

		// int length_id = parts[2].length();
		int length_id = (num_operations + "").length();

		int final_id = (int) (node_id * Math.pow(10,
				(length_id + length_proc + 0d)))
				+ (int) (process_id * Math.pow(10, (length_id + 0d))) + id_id;

		return final_id;
	}

	private int getIDfromString(String id, int num) {
		String[] parts = id.trim().split("\\.");

		int node_id = Integer.parseInt(parts[0]);
		int process_id = Integer.parseInt(parts[1]);

		int length_proc = (this.one_node_clients + "").length();

		int id_id = Integer.parseInt(parts[2]);

		// int length_id = parts[2].length();
		int length_id = ((num + num_operations) + "").length();

		int final_id = (int) (node_id * Math.pow(10,
				(length_id + length_proc + 0d)))
				+ (int) (process_id * Math.pow(10, (length_id + 0d)))
				+ (id_id + num + 1);

		return final_id;
	}

	private Customer createCostumer(int process_id, int c_id) {
		final String name = (BenchmarkUtil.getRandomAString(8, 13) + " " + BenchmarkUtil
				.getRandomAString(8, 15));
		final String[] names = name.split(" ");
		final Random r = new Random();
		final int random_int = r.nextInt(1000);

		final String c_UNAME = BenchmarkUtil.DigSyl(c_id, 0);
		final String c_PASSWD = names[0].charAt(0) + names[1].charAt(0) + ""
				+ random_int;
		final String c_FNAME = names[0];
		final String c_LNAME = names[1];
		final int a_id = getIDfromValues(node_id, process_id, addr_aux_id,
				Constants.NUM_ADDRESSES);
		addr_aux_id++;
		Address c_ADDR = generateAddress(a_id);
		c_ADDR = enterAddress(c_ADDR);
		final int c_PHONE = r.nextInt(999999999 - 100000000) + 100000000;
		final String c_EMAIL = c_FNAME + "@"
				+ BenchmarkUtil.getRandomAString(2, 9) + ".com";
		final Date c_SINCE = new Date(System.currentTimeMillis());
		final Date c_LAST_VISIT = new Date(System.currentTimeMillis());
		final Date c_LOGIN = new Date(System.currentTimeMillis());
		// 2 hours in milliseconds
		final Date c_EXPIRATION = new Date(System.currentTimeMillis() + 7200000);
		final double c_DISCOUNT = r.nextDouble();
		final double c_BALANCE = 0.00;
		final double c_YTD_PMT = (double) BenchmarkUtil.getRandomInt(0, 99999) / 100.0;
		final Date c_BIRTHDATE = BenchmarkUtil.getRandomDate(1880, 2000)
				.getTime();
		final String c_DATA = BenchmarkUtil.getRandomAString(100, 500);
		return new Customer(c_id, c_UNAME, c_PASSWD, c_FNAME, c_LNAME,
				c_ADDR.ADDR_ID, Integer.toString(c_PHONE), c_EMAIL, c_SINCE,
				c_LAST_VISIT, c_LOGIN, c_EXPIRATION, c_DISCOUNT, c_BALANCE,
				c_YTD_PMT, c_BIRTHDATE, c_DATA);
	}

	public Address generateAddress(int id) {
		String ADDR_STREET1, ADDR_STREET2, ADDR_CITY, ADDR_STATE;
		String ADDR_ZIP;
		int country_id;
		ADDR_STREET1 = "street" + BenchmarkUtil.getRandomAString(10, 30);
		ADDR_STREET2 = "street" + BenchmarkUtil.getRandomAString(10, 30);
		ADDR_CITY = BenchmarkUtil.getRandomAString(4, 30);
		ADDR_STATE = BenchmarkUtil.getRandomAString(2, 20);
		ADDR_ZIP = BenchmarkUtil.getRandomAString(5, 10);
		country_id = BenchmarkUtil.getRandomInt(0, 91);
		Address address = new Address(id, ADDR_STREET1, ADDR_STREET2,
				ADDR_CITY, ADDR_STATE, ADDR_ZIP, country_id);
		return address;
	}

	private int getIDfromValues(int node_id, int process_id, int id_id, int num) {
		int length_proc = (this.one_node_clients + "").length();
		int length_id = ((num + num_operations) + "").length();
		int final_id = (int) (node_id * Math.pow(10,
				(length_id + length_proc + 0d)))
				+ (int) (process_id * Math.pow(10, (length_id + 0d)))
				+ (id_id + num + 1);
		return final_id;
	}

	@Atomic
	public Address enterAddress(final Address address) {
		// returns the address of the specified address. Adds a new address to
		// the table if needed
		Address existing_addr = getAddress(new Filter<Address>() {
			public boolean filter(Address obj) {
				if (obj == null) {
					System.err.println("obj null");
				} else {
					if (obj.ADDR_CITY == null)
						System.err.println("obj.ADDR_CITY null");
					if (obj.ADDR_STATE == null)
						System.err.println("obj.ADDR_STATE null");
					if (obj.ADDR_STREET1 == null)
						System.err.println("obj.ADDR_STREET1 null");
					if (obj.ADDR_STREET2 == null)
						System.err.println("obj.ADDR_STREET2 null");
					if (obj.ADDR_ZIP == null)
						System.err.println("obj.ADDR_ZIP null");
				}
				return obj.ADDR_CITY.equals(address.ADDR_CITY)
						&& obj.ADDR_CO_ID == address.ADDR_CO_ID
						&& obj.ADDR_STATE.equals(address.ADDR_STATE)
						&& obj.ADDR_STREET1.equals(address.ADDR_STREET1)
						&& obj.ADDR_STREET2.equals(address.ADDR_STREET2)
						&& obj.ADDR_ZIP.equals(address.ADDR_ZIP);
			}
		});
		if (existing_addr == null) {
			insertAddress(address.ADDR_ID, address);
			return address;
		} else {
			return existing_addr;
		}
	}

	@Override
	public Object insert(String key, String path, Entity value)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(String key, String path, String column) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(String key, String path, String column, Object value,
			String superfield) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Object read(String key, String path, String column, String superfield)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, Object>> rangeQuery(String table,
			List<String> fields, int limit) throws Exception {
		Map<String, Map<String, Object>> result = new TreeMap<String, Map<String, Object>>();
		if (table.equalsIgnoreCase("author")) {
			List<Author> all = getAuthors();
			for (Author a : all) {
				TreeMap<String, Object> values = new TreeMap<String, Object>();
				values.put("A_LNAME", a.A_LNAME);
				result.put(Integer.toString(a.A_ID), values);
			}
		} else if (table.equalsIgnoreCase("item")) {
			List<Item> all = getItems();
			for (Item i : all) {
				TreeMap<String, Object> values = new TreeMap<String, Object>();
				values.put("I_TITLE", i.I_TITLE);
				result.put(Integer.toString(i.I_ID), values);
			}
		}
		return result;
	}

	@Override
	public void truncate(String path) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void index(String key, String path, Object value) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void index(String key, String path, String indexed_key,
			Map<String, Object> value) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeClient() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> getInfo() {
		// TODO Auto-generated method stub
		return new TreeMap<String, String>();
	}

}
