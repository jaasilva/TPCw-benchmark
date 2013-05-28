package pt.unl.citi.tpcw;

import static pt.unl.citi.tpcw.Operations.OP_POPULATE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uminho.gsd.benchmarks.dataStatistics.ResultHandler;
import org.uminho.gsd.benchmarks.generic.Constants;
import org.uminho.gsd.benchmarks.helpers.BenchmarkUtil;
import org.uminho.gsd.benchmarks.interfaces.Workload.Operation;
import org.uminho.gsd.benchmarks.interfaces.executor.AbstractDatabaseExecutorFactory;
import org.uminho.gsd.benchmarks.interfaces.executor.DatabaseExecutorInterface;
import org.uminho.gsd.benchmarks.interfaces.populator.AbstractBenchmarkPopulator;

import pt.unl.citi.tpcw.entities.Address;
import pt.unl.citi.tpcw.entities.Author;
import pt.unl.citi.tpcw.entities.CCXact;
import pt.unl.citi.tpcw.entities.Country;
import pt.unl.citi.tpcw.entities.Customer;
import pt.unl.citi.tpcw.entities.Item;
import pt.unl.citi.tpcw.entities.Order;
import pt.unl.citi.tpcw.entities.OrderLine;

/* Adapted from MySQL populator */
public class Populator extends AbstractBenchmarkPopulator {

	/**
	 * Time measurements
	 */
	private static boolean delay_inserts = false;
	private static int delay_time = 100;
	private static Random rand = new Random();
	private int rounds = 500;
	private ResultHandler results;
	String result_path;

	// ATTENTION: The NUM_EBS and NUM_ITEMS variables are the only variables
	// that should be modified in order to rescale the DB.
	// scale factors:
	// NUM_EBS: +1
	// NUM_ITEMS: 1 000, 10 000, 100 000, 1 000 000, 10 000 000
	private static/* final */int NUM_EBS = Constants.NUM_EBS;
	private static/* final */int NUM_ITEMS = Constants.NUM_ITEMS;
	private static/* final */int NUM_CUSTOMERS = Constants.NUM_CUSTOMERS;
	private static/* final */int NUM_ADDRESSES = Constants.NUM_ADDRESSES;
	private static/* final */int NUM_AUTHORS = Constants.NUM_AUTHORS;
	private static/* final */int NUM_ORDERS = Constants.NUM_ORDERS;
	// this is constant. Never changes!
	private static final int NUM_COUNTRIES = Constants.NUM_COUNTRIES;

	private static AbstractDatabaseExecutorFactory databaseClientFactory;
	ArrayList<Integer> authors = new ArrayList<Integer>();
	ArrayList<Integer> addresses = new ArrayList<Integer>();
	ArrayList<Integer> countries = new ArrayList<Integer>();
	ArrayList<Integer> customers = new ArrayList<Integer>();
	ArrayList<Integer> items = new ArrayList<Integer>();

	boolean debug = true;
	private static int num_threads = 1;
	boolean error = false;
	private CountDownLatch barrier;

	public Populator(
			AbstractDatabaseExecutorFactory database_interface_factory,
			String conf_filename) {
		super(database_interface_factory, conf_filename);
		databaseClientFactory = database_interface_factory;

		Map<String, String> execution_info = configuration
				.get("BenchmarkPopulator");

		String name = execution_info.get("name");
		if (name == null || name.isEmpty()) {
			name = "TPCW_POPULATOR";
			System.out
					.println("[WARN:] NO DEFINED NAME: DEFAULT -> TPCW_POPULATOR ");
		}

		String num_threads_info = execution_info.get("thread_number");
		if (num_threads_info == null || num_threads_info.isEmpty()) {
			num_threads = 1;
			System.out
					.println("[WARN:] NO THREAD NUMBER DEFINED: DEFAULT -> 1");
		} else {
			num_threads = Integer.parseInt(num_threads_info.trim());
		}

		this.results = new ResultHandler(name, -1);

		result_path = execution_info.get("result_path");
		if (result_path == null || result_path.trim().isEmpty()) {
			result_path = "./results";
		}

		String ebs = execution_info.get("tpcw_numEBS");
		if (ebs != null) {
			Constants.NUM_EBS = Integer.valueOf(ebs.trim());
		} else {
			System.out.println("SCALE FACTOR (EBS) NOT DEFINED. SET TO: "
					+ NUM_EBS);
		}

		String items = execution_info.get("tpcw_numItems");

		if (items != null) {
			Constants.NUM_ITEMS = Integer.valueOf(items.trim());
		} else {
			System.out.println("NUMBER OF ITEMS NOT DEFINED. SET TO: "
					+ NUM_ITEMS);
		}

		Constants.NUM_CUSTOMERS = Constants.NUM_EBS * 2880;
		Constants.NUM_ADDRESSES = 2 * Constants.NUM_CUSTOMERS;
		Constants.NUM_AUTHORS = (int) (.25 * Constants.NUM_ITEMS);
		Constants.NUM_ORDERS = (int) (.9 * Constants.NUM_CUSTOMERS);

		NUM_EBS = Constants.NUM_EBS;
		NUM_ITEMS = Constants.NUM_ITEMS;
		NUM_CUSTOMERS = Constants.NUM_CUSTOMERS;
		NUM_ADDRESSES = Constants.NUM_ADDRESSES;
		NUM_AUTHORS = Constants.NUM_AUTHORS;
		NUM_ORDERS = Constants.NUM_ORDERS;
	}

	public boolean populate() {
		DatabaseExecutorInterface client = databaseClientFactory
				.getDatabaseClient();

		Map<String, Object> param = new TreeMap<String, Object>();
		try {
			client.execute(new Operation(OP_POPULATE, param));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (error) {
			return false;
		} else {
			try {
				insertCountries(NUM_COUNTRIES);
				if (delay_inserts) {
					Thread.sleep(delay_time);
				}
				insertAddresses(NUM_ADDRESSES, true);
				if (delay_inserts) {
					Thread.sleep(delay_time);
				}
				insertCustomers(NUM_CUSTOMERS);
				if (delay_inserts) {
					Thread.sleep(delay_time);
				}
				insertAuthors(NUM_AUTHORS, true);
				if (delay_inserts) {
					Thread.sleep(delay_time);
				}
				insertItems(NUM_ITEMS);
				if (delay_inserts) {
					Thread.sleep(delay_time);
				}
				insertOrder_and_CC_XACTS(NUM_ORDERS);

				System.out.println("***Finished***");

			} catch (InterruptedException ex) {
				Logger.getLogger(Populator.class.getName()).log(Level.SEVERE,
						null, ex);
				return false;
			} catch (Exception ex) {
				Logger.getLogger(Populator.class.getName()).log(Level.SEVERE,
						null, ex);
				return false;
			}

			try {
				System.out.println("CREATING INDEXES");
				client.execute(new Operation("CREATE_INDEXES", param));
			} catch (Exception e) {
				return false;
			}

			results.listDataToSOutput();
			results.listDatatoFiles(result_path, "", true);
			results.cleanResults();
			return true;
		}
	}

	public void cleanDB() throws Exception {
	}

	public void BenchmarkClean() throws Exception {
	}

	public void removeALL() throws Exception {
	}

	static void countryInsert(String Operation, int key, Country value,
			ResultHandler results) {

		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertCountry(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
		System.out.print("\r");
		System.out.print("Inserted country "+key);
	}

	static void addressInsert(String Operation, int key, Address value,
			ResultHandler results) {
		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertAddress(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
		System.out.print("\r");
		System.out.print("Inserted address "+key);
	}

	static void customerInsert(String Operation, int key, Customer value,
			ResultHandler results) {
		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertCustomer(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
		System.out.print("\r");
		System.out.print("Inserted customer "+key);
	}

	static void authorInsert(String Operation, int key, Author value,
			ResultHandler results) {
		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertAuthor(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
		System.out.print("\r");
		System.out.print("Inserted author "+key);
	}

	static void itemInsert(String Operation, int key, Item value,
			ResultHandler results) {
		if (value == null)
			throw new Error("Inserted null at items["+key+"]");
		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertItem(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
		System.out.print("\r");
		System.out.print("Inserted item "+key);
	}

	static void orderInsert(String Operation, int key, Order value,
			ResultHandler results) {
		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertOrder(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
		System.out.print("\r");
		System.out.print("Inserted order "+key);
	}

	static void orderLineInsert(String Operation, int key, OrderLine value,
			ResultHandler results) {
		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertOrderLine(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
//		System.out.print("\r");
//		System.out.print("Inserted order line "+key);
	}

	static void ccXactInsert(String Operation, int key, CCXact value,
			ResultHandler results) {
		// long time1 = System.currentTimeMillis();
		long time1 = System.nanoTime();
		Executor.insertCcXact(key, value);
		// long time2 = System.currentTimeMillis();
		long time2 = System.nanoTime();
		results.logResult(Operation, (time2 / 1000 / 1000)
				- (time1 / 1000 / 1000));
//		System.out.print("\r");
//		System.out.print("Inserted ccxact "+key);
	}

	/************************************************************************/
	/************************************************************************/
	/************************************************************************/

	/**
	 * ************ Authors* **************
	 */

	public void insertAuthors(int n, boolean insert)
			throws InterruptedException {
		int threads = num_threads;
		int sections = n;
		int firstSection = 0;

		if (n < num_threads) {
			threads = 1;
			firstSection = n;
		} else {
			sections = (int) Math.floor(n / num_threads);
			int rest = n - (num_threads * sections);
			firstSection = sections + rest;
		}

		System.out.println(">>Inserting " + n + " Authors || populatores "
				+ num_threads);
		barrier = new CountDownLatch(threads);

		AuthorPopulator[] partial_authors = new AuthorPopulator[threads];
		for (int i = threads; i > 0; i--) {

			int base = (threads - i) * sections;

			AuthorPopulator populator = null;
			if (i == 0) {
				populator = new AuthorPopulator(firstSection, base, insert);

			} else {
				populator = new AuthorPopulator(sections, base, insert);
			}
			partial_authors[threads - i] = populator;
			Thread t = new Thread(populator);
			t.start();
		}

		barrier.await();
		for (AuthorPopulator populator : partial_authors) {
			ArrayList<Integer> ids = populator.getData();
			for (int id : ids) {
				authors.add(id);
			}
			if (insert)
				results.addResults(populator.returnResults());
			populator.partial_results.cleanResults();

		}
		partial_authors = null;
		System.gc();

	}

	class AuthorPopulator implements Runnable {
		int num_authors;
		ArrayList<Integer> partial_authors;
		ResultHandler partial_results;
		boolean insertDB;
		int base;

		public AuthorPopulator(int num_authors, int base, boolean insertDB) {
			this.num_authors = num_authors;
			partial_authors = new ArrayList<Integer>();
			partial_results = new ResultHandler("", rounds);
			this.insertDB = insertDB;
			this.base = base;
		}

		public void run() {
			this.insertAuthors(num_authors);
		}

		public void insertAuthors(int n) {

			System.out.println("Inserting Authors: " + n);
			for (int i = 0; i < n; i++) {
				GregorianCalendar cal = BenchmarkUtil.getRandomDate(1800, 1990);

				String[] names = (BenchmarkUtil.getRandomAString(3, 20) + " " + BenchmarkUtil
						.getRandomAString(2, 20)).split(" ");
				String[] Mnames = ("d " + BenchmarkUtil.getRandomAString(2, 20))
						.split(" ");

				String first_name = names[0];
				String last_name = names[1];
				String middle_name = Mnames[1];
				java.sql.Date dob = new java.sql.Date(cal.getTime().getTime());
				String bio = BenchmarkUtil.getRandomAString(125, 500);

				Author a = new Author(base + i, first_name, last_name,
						middle_name, dob, bio);
				if (insertDB)
					authorInsert("INSERT_Authors", (base + i), a,
							partial_results);

				partial_authors.add(i);
			}
			if (debug) {
				System.out.println("Thread finished: " + num_authors
						+ " authors inserted");
			}

			System.out.println();
			barrier.countDown();
		}

		public ArrayList<Integer> getData() {
			return partial_authors;
		}

		public ResultHandler returnResults() {
			return partial_results;
		}
	}

	/**
	 * ************ Customers* **************
	 */
	public void insertCustomers(int n) throws InterruptedException {

		int threads = num_threads;
		int sections = n;
		int firstSection = 0;

		if (n < num_threads) {
			threads = 1;
			firstSection = n;
		} else {
			sections = (int) Math.floor(n / num_threads);
			int rest = n - (num_threads * sections);
			firstSection = sections + rest;
		}
		System.out.println(">>Inserting " + n + " Customers || populatores "
				+ num_threads);
		barrier = new CountDownLatch(threads);

		CustomerPopulator[] partial_Customers = new CustomerPopulator[threads];
		for (int i = threads; i > 0; i--) {

			int base = (threads - i) * sections;
			CustomerPopulator populator = null;
			if (i == 0) {
				populator = new CustomerPopulator(firstSection, base);

			} else {
				populator = new CustomerPopulator(sections, base);
			}
			partial_Customers[threads - i] = populator;
			Thread t = new Thread(populator, "Costumer populator"
					+ (threads - i));
			t.start();
		}
		barrier.await();
		for (CustomerPopulator populator : partial_Customers) {
			ArrayList<Integer> ids = populator.getData();
			for (int id : ids) {
				customers.add(id);
			}
			results.addResults(populator.returnResults());
			populator.partial_results.cleanResults();

		}
		partial_Customers = null;
		System.gc();

	}

	class CustomerPopulator implements Runnable {
		int num_Customers;
		ArrayList<Integer> partial_Customers;
		ResultHandler partial_results;
		int base;

		public CustomerPopulator(int num_Customers, int base) {
			this.num_Customers = num_Customers;
			partial_Customers = new ArrayList<Integer>();
			partial_results = new ResultHandler("", rounds);
			this.base = base;

		}

		public void run() {
			this.insertCustomers(num_Customers);
		}

		public void insertCustomers(int n) {

			System.out.println("Inserting Customers: " + n);
			for (int i = 0; i < n; i++) {
				final int c_ID = base + i;

				String name = (BenchmarkUtil.getRandomAString(8, 13) + " " + BenchmarkUtil
						.getRandomAString(8, 15));
				String[] names = name.split(" ");
				Random r = new Random();
				int random_int = r.nextInt(1000);

				String c_UNAME = names[0] + "_" + (base + i);
				if (base + i >= 1000) {
					c_UNAME = names[0] + "_" + random_int;
				}
				// if(key.length()>=20){
				// System.out.println("TTT");
				// }

				final String c_PASSWD = names[0].charAt(0) + names[1].charAt(0)
						+ "" + random_int;
				// insert(pass, key, "Customer", "C_PASSWD", writeCon);

				final String c_FNAME = names[0];
				// insert(first_name, key, "Customer", "C_FNAME", writeCon);

				final String c_LNAME = names[1];
				// insert(last_name, key, "Customer", "C_LNAME", writeCon);

				int c_PHONE = r.nextInt(999999999 - 100000000) + 100000000;
				// insert(phone, key, "Customer", "C_PHONE", writeCon);

				final String c_EMAIL = c_UNAME + "@"
						+ BenchmarkUtil.getRandomAString(2, 9) + ".com";
				// insert(email, key, "Customer", "C_EMAIL", writeCon);

				double c_DISCOUNT = r.nextDouble();
				// insert(discount, key, "Customer", "C_DISCOUNT", writeCon);

				double c_BALANCE = 0.00;
				// insert(C_BALANCE, key, "Customer", "C_BALANCE", writeCon);

				double c_YTD_PMT = (double) BenchmarkUtil
						.getRandomInt(0, 99999) / 100.0;
				// insert(C_YTD_PMT, key, "Customer", "C_YTD_PMT", writeCon);

				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.DAY_OF_YEAR,
						-1 * BenchmarkUtil.getRandomInt(1, 730));

				Date c_SINCE = cal.getTime();
				// insert(C_SINCE, key, "Customer", "C_SINCE ", writeCon);

				cal.add(Calendar.DAY_OF_YEAR, BenchmarkUtil.getRandomInt(0, 60));
				if (cal.after(new GregorianCalendar())) {
					cal = new GregorianCalendar();
				}

				Date c_LAST_VISIT = cal.getTime();
				// insert(C_LAST_LOGIN, key, "Customer", "C_LAST_LOGIN",
				// writeCon);

				Date c_LOGIN = new Date(System.currentTimeMillis());
				// insert(C_LOGIN, key, "Customer", "C_LOGIN", writeCon);

				cal = new GregorianCalendar();
				cal.add(Calendar.HOUR, 2);

				Date c_EXPIRATION = cal.getTime();
				// insert(C_EXPIRATION, key, "Customer", "C_EXPIRATION",
				// writeCon);

				cal = BenchmarkUtil.getRandomDate(1880, 2000);
				Date c_BIRTHDATE = cal.getTime();
				// insert(C_BIRTHDATE, key, "Customer", "C_BIRTHDATE",
				// writeCon);

				String c_DATA = BenchmarkUtil.getRandomAString(100, 500);
				// insert(C_DATA, key, "Customer", "C_DATA", writeCon);

				int c_ADDR_ID = addresses.get(rand.nextInt(addresses.size()));
				// insert(address.getAddr_id(), key, "Customer", "C_ADDR_ID",
				// writeCon);

				Customer c = new Customer(c_ID, c_UNAME, c_PASSWD, c_FNAME,
						c_LNAME, c_ADDR_ID, c_PHONE + "", c_EMAIL, c_SINCE,
						c_LAST_VISIT, c_LOGIN, c_EXPIRATION, c_DISCOUNT,
						c_BALANCE, c_YTD_PMT, c_BIRTHDATE, c_DATA);

				try {
					customerInsert("INSERT_Customers", (base + i), c,
							partial_results);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

				partial_Customers.add(c.C_ID);

			}
			if (debug) {
				System.out.println("Thread finished: " + num_Customers
						+ " Customers inserted");
			}
			System.out.println();
			barrier.countDown();
		}

		public ArrayList<Integer> getData() {
			return partial_Customers;
		}

		public ResultHandler returnResults() {
			return partial_results;
		}
	}

	/**
	 * ************ Items* **************
	 */
	public void insertItems(int n) throws InterruptedException {
		int threads = num_threads;
		int sections = n;
		int firstSection = 0;

		if (n < num_threads) {
			threads = 1;
			firstSection = n;
		} else {
			sections = (int) Math.floor(n / num_threads);
			int rest = n - (num_threads * sections);
			firstSection = sections + rest;
		}

		System.out.println(">>Inserting " + n + " Items || populatores "
				+ num_threads);
		barrier = new CountDownLatch(threads);

		ItemPopulator[] partial_items = new ItemPopulator[threads];
		for (int i = threads; i > 0; i--) {

			int base = (threads - i) * sections;

			ItemPopulator populator = null;
			if (i == 0) {
				populator = new ItemPopulator(firstSection, base);

			} else {
				populator = new ItemPopulator(sections, base);
			}
			partial_items[threads - i] = populator;
			Thread t = new Thread(populator, "Item populator" + (threads - i));
			t.start();
		}
		barrier.await();

		for (ItemPopulator populator : partial_items) {
			ArrayList<Integer> ids = populator.getData();
			for (int id : ids) {
				items.add(id);
			}
			results.addResults(populator.returnResults());
			populator.partial_results.cleanResults();
		}
		partial_items = null;
		System.gc();

	}

	class ItemPopulator implements Runnable {
		int num_items;
		ArrayList<Integer> partial_items;
		ResultHandler partial_results;
		int base = 0;

		public ItemPopulator(int num_items, int base) {
			this.num_items = num_items;
			partial_items = new ArrayList<Integer>();
			partial_results = new ResultHandler("", rounds);
			this.base = base;
		}

		public void run() {
			this.insertItems(num_items);
		}

		public void insertItems(int n) {

			String[] subjects = { "ARTS", "BIOGRAPHIES", "BUSINESS",
					"CHILDREN", "COMPUTERS", "COOKING", "HEALTH", "HISTORY",
					"HOME", "HUMOR", "LITERATURE", "MYSTERY", "NON-FICTION",
					"PARENTING", "POLITICS", "REFERENCE", "RELIGION",
					"ROMANCE", "SELF-HELP", "SCIENCE-NATURE",
					"SCIENCE-FICTION", "SPORTS", "YOUTH", "TRAVEL" };
			String[] backings = { "HARDBACK", "PAPERBACK", "USED", "AUDIO",
					"LIMITED-EDITION" };

			System.out.println("Inserting Items: " + n);

			ArrayList<String> titles = new ArrayList<String>();
			for (int i = 0; i < n; i++) {

				String title = BenchmarkUtil.getRandomAString(14, 60);
				// int num = rand.nextInt(1000);
				titles.add(title);
			}

			for (int i = 0; i < n; i++) {
				final int i_ID = base + i;
				String i_TITLE;
				String i_PUBLISHER;
				String i_DESC;
				String i_SUBJECT;
				double i_COST;
				int i_STOCK;
				int[] i_RELATED = new int[5];
				int i_PAGE;
				String i_BACKING;
				i_TITLE = titles.get(i);

				int author_pos = rand.nextInt(authors.size());
				int i_A_ID = authors.get(author_pos);

				i_PUBLISHER = BenchmarkUtil.getRandomAString(14, 60);
				// insert(I_PUBLISHER, I_TITLE, column_family, "I_PUBLISHER",
				// writeCon);

				boolean rad1 = rand.nextBoolean();
				i_DESC = null;
				if (rad1) {
					i_DESC = BenchmarkUtil.getRandomAString(100, 500);
					// insert(I_DESC, I_TITLE, column_family, "I_DESC",
					// writeCon);
				}

				i_COST = rand.nextInt(100);
				// insert(I_AUTHOR, I_TITLE, column_family, "I_AUTHOR",
				// writeCon);

				i_STOCK = BenchmarkUtil.getRandomInt(10, 30);
				// insert(I_STOCK, I_TITLE, column_family, "I_STOCK", writeCon);

				for (int z = 0; z < 5; z++) {
					i_RELATED[z] = rand.nextInt(NUM_ITEMS);
				}

				i_PAGE = rand.nextInt(500) + 10;
				// insert(I_PAGE, I_TITLE, column_family, "I_PAGE", writeCon);

				i_SUBJECT = subjects[rand.nextInt(subjects.length - 1)];
				// insert(I_SUBJECT, I_TITLE, column_family, "I_SUBJECT",
				// writeCon);

				i_BACKING = backings[rand.nextInt(backings.length - 1)];
				// insert(I_BACKING, I_TITLE, column_family, "I_BACKING",
				// writeCon);

				GregorianCalendar cal = BenchmarkUtil.getRandomDate(1930, 2000);

				Date i_PUB_DATE = cal.getTime();

				String i_THUMBNAIL = new String("img" + i % 100 + "/thumb_" + i
						+ ".gif");
				String i_IMAGE = new String("img" + i % 100 + "/image_" + i
						+ ".gif");

				double i_SRP = (double) BenchmarkUtil.getRandomInt(100, 99999);
				i_SRP /= 100.0;

				String i_ISBN = BenchmarkUtil.getRandomAString(13);

				Date i_AVAIL = cal.getTime(); // Data
												// when
												// available

				String i_DIMENSION = ((double) BenchmarkUtil.getRandomInt(1,
						9999) / 100.0)
						+ "x"
						+ ((double) BenchmarkUtil.getRandomInt(1, 9999) / 100.0)
						+ "x"
						+ ((double) BenchmarkUtil.getRandomInt(1, 9999) / 100.0);

				Item item = new Item(i_ID, i_TITLE, i_A_ID, i_PUB_DATE,
						i_PUBLISHER, i_SUBJECT, i_DESC, i_RELATED[0],
						i_RELATED[1], i_RELATED[2], i_RELATED[3], i_RELATED[4],
						i_THUMBNAIL, i_IMAGE, i_SRP, i_COST, i_AVAIL, i_STOCK,
						i_ISBN, i_PAGE, i_BACKING, i_DIMENSION);

				itemInsert("INSERT_Items", (base + i), item, partial_results);

				partial_items.add(item.I_ID);

			}
			if (debug) {
				System.out.println("Thread finished: " + num_items
						+ " items inserted");
			}
			System.out.println();
			barrier.countDown();
		}

		public ArrayList<Integer> getData() {
			return partial_items;
		}

		public ResultHandler returnResults() {
			return partial_results;
		}
	}

	/**
	 * *********** Addresses* ***********
	 */
	public void insertAddresses(int n, boolean insert)
			throws InterruptedException {

		int threads = num_threads;
		int sections = n;
		int firstSection = 0;

		if (n < num_threads) {
			threads = 1;
			firstSection = n;
		} else {
			sections = (int) Math.floor(n / num_threads);
			int rest = n - (num_threads * sections);
			firstSection = sections + rest;
		}

		System.out.println(">>Inserting " + n + " Addresses || populatores "
				+ num_threads);

		barrier = new CountDownLatch(threads);
		AddressPopulator[] partial_addresses = new AddressPopulator[threads];
		for (int i = threads; i > 0; i--) {

			int base = (threads - i) * sections;

			AddressPopulator populator = null;
			if (i == 0) {
				populator = new AddressPopulator(firstSection, insert, base);

			} else {
				populator = new AddressPopulator(sections, insert, base);
			}
			Thread t = new Thread(populator, "Address populator"
					+ (threads - i));
			partial_addresses[threads - i] = populator;
			t.start();
		}
		barrier.await();

		for (AddressPopulator populator : partial_addresses) {

			ArrayList<Integer> ids = populator.getData();
			for (int id : ids) {
				addresses.add(id);
			}
			if (insert)
				results.addResults(populator.returnResults());
			populator.partial_results.cleanResults();
			populator = null;
		}
		partial_addresses = null;
		System.gc();

	}

	class AddressPopulator implements Runnable {

		int num_addresses;
		ArrayList<Integer> partial_adresses;
		ResultHandler partial_results;
		boolean insertDB;
		int base = 0;

		public AddressPopulator(int num_addresses, boolean insertDB, int base) {
			this.num_addresses = num_addresses;
			partial_adresses = new ArrayList<Integer>();
			partial_results = new ResultHandler("", rounds);
			this.insertDB = insertDB;
			this.base = base;
		}

		public void run() {
			this.insertAddress(num_addresses);
		}

		private void insertAddress(int n) {

			System.out.println("Inserting Address: " + n);

			String ADDR_STREET1, ADDR_STREET2, ADDR_CITY, ADDR_STATE;
			String ADDR_ZIP;
			int country_id;

			for (int i = 0; i < n; i++) {
				ADDR_STREET1 = "street"
						+ BenchmarkUtil.getRandomAString(10, 30);

				ADDR_STREET2 = "street"
						+ BenchmarkUtil.getRandomAString(10, 30);
				ADDR_CITY = BenchmarkUtil.getRandomAString(4, 30);
				ADDR_STATE = BenchmarkUtil.getRandomAString(2, 20);
				ADDR_ZIP = BenchmarkUtil.getRandomAString(5, 10);
				country_id = countries.get(BenchmarkUtil.getRandomInt(0,
						NUM_COUNTRIES - 1));

				int key = base + i;// country_id + ADDR_STATE + ADDR_CITY +
									// ADDR_ZIP + rand.nextInt(1000);

				Address address = new Address(key, ADDR_STREET1, ADDR_STREET2,
						ADDR_CITY, ADDR_STATE, ADDR_ZIP, country_id);
				// insert(ADDR_STREET1, key, "Addresses", "ADDR_STREET1",
				// writeConsistency);
				// insert(ADDR_STREET2, key, "Addresses", "ADDR_STREET2",
				// writeConsistency);
				// insert(ADDR_STATE, key, "Addresses", "ADDR_STATE",
				// writeConsistency);
				// insert(ADDR_CITY, key, "Addresses", "ADDR_CITY",
				// writeConsistency);
				// insert(ADDR_ZIP, key, "Addresses", "ADDR_ZIP",
				// writeConsistency);
				// insert(country.getCo_id(), key, "Addresses", "ADDR_CO_ID",
				// writeConsistency);

				if (insertDB) {
					addressInsert("INSERT_Addresses", key, address,
							partial_results);
				}
				partial_adresses.add(key);

			}
			if (debug) {
				System.out.println("Thread finished: " + num_addresses
						+ " addresses.");
			}
			
			System.out.println();
			barrier.countDown();
		}

		public ArrayList<Integer> getData() {
			return partial_adresses;
		}

		public ResultHandler returnResults() {
			return partial_results;
		}
	}

	/**
	 * ******** Countries * *********
	 */
	private void insertCountries(int numCountries) {
		String[] countriesNames = { "United States", "United Kingdom",
				"Canada", "Germany", "France", "Japan", "Netherlands", "Italy",
				"Switzerland", "Australia", "Algeria", "Argentina", "Armenia",
				"Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangla Desh",
				"Barbados", "Belarus", "Belgium", "Bermuda", "Bolivia",
				"Botswana", "Brazil", "Bulgaria", "Cayman Islands", "Chad",
				"Chile", "China", "Christmas Island", "Colombia", "Croatia",
				"Cuba", "Cyprus", "Czech Republic", "Denmark",
				"Dominican Republic", "Eastern Caribbean", "Ecuador", "Egypt",
				"El Salvador", "Estonia", "Ethiopia", "Falkland Island",
				"Faroe Island", "Fiji", "Finland", "Gabon", "Gibraltar",
				"Greece", "Guam", "Hong Kong", "Hungary", "Iceland", "India",
				"Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Jamaica",
				"Jordan", "Kazakhstan", "Kuwait", "Lebanon", "Luxembourg",
				"Malaysia", "Mexico", "Mauritius", "New Zealand", "Norway",
				"Pakistan", "Philippines", "Poland", "Portugal", "Romania",
				"Russia", "Saudi Arabia", "Singapore", "Slovakia",
				"South Africa", "South Korea", "Spain", "Sudan", "Sweden",
				"Taiwan", "Thailand", "Trinidad", "Turkey", "Venezuela",
				"Zambia" };

		double[] exchanges = { 1, .625461, 1.46712, 1.86125, 6.24238, 121.907,
				2.09715, 1842.64, 1.51645, 1.54208, 65.3851, 0.998, 540.92,
				13.0949, 3977, 1, .3757, 48.65, 2, 248000, 38.3892, 1, 5.74,
				4.7304, 1.71, 1846, .8282, 627.1999, 494.2, 8.278, 1.5391,
				1677, 7.3044, 23, .543, 36.0127, 7.0707, 15.8, 2.7, 9600,
				3.33771, 8.7, 14.9912, 7.7, .6255, 7.124, 1.9724, 5.65822,
				627.1999, .6255, 309.214, 1, 7.75473, 237.23, 74.147, 42.75,
				8100, 3000, .3083, .749481, 4.12, 37.4, 0.708, 150, .3062,
				1502, 38.3892, 3.8, 9.6287, 25.245, 1.87539, 7.83101, 52,
				37.8501, 3.9525, 190.788, 15180.2, 24.43, 3.7501, 1.72929,
				43.9642, 6.25845, 1190.15, 158.34, 5.282, 8.54477, 32.77,
				37.1414, 6.1764, 401500, 596, 2447.7 };

		String[] currencies = { "Dollars", "Pounds", "Dollars",
				"Deutsche Marks", "Francs", "Yen", "Guilders", "Lira",
				"Francs", "Dollars", "Dinars", "Pesos", "Dram", "Schillings",
				"Manat", "Dollars", "Dinar", "Taka", "Dollars", "Rouble",
				"Francs", "Dollars", "Boliviano", "Pula", "Real", "Lev",
				"Dollars", "Franc", "Pesos", "Yuan Renmimbi", "Dollars",
				"Pesos", "Kuna", "Pesos", "Pounds", "Koruna", "Kroner",
				"Pesos", "Dollars", "Sucre", "Pounds", "Colon", "Kroon",
				"Birr", "Pound", "Krone", "Dollars", "Markka", "Franc",
				"Pound", "Drachmas", "Dollars", "Dollars", "Forint", "Krona",
				"Rupees", "Rupiah", "Rial", "Dinar", "Punt", "Shekels",
				"Dollars", "Dinar", "Tenge", "Dinar", "Pounds", "Francs",
				"Ringgit", "Pesos", "Rupees", "Dollars", "Kroner", "Rupees",
				"Pesos", "Zloty", "Escudo", "Leu", "Rubles", "Riyal",
				"Dollars", "Koruna", "Rand", "Won", "Pesetas", "Dinar",
				"Krona", "Dollars", "Baht", "Dollars", "Lira", "Bolivar",
				"Kwacha" };

		if (numCountries > countriesNames.length) {
			numCountries = countriesNames.length - 1;
		}

		System.out.println(">>Inserting " + numCountries
				+ " countries || populatores " + num_threads);

		for (int i = 0; i < numCountries; i++) {
			Country country = new Country(i, countriesNames[i], currencies[i],
					exchanges[i]);
			countryInsert("INSERT_Countries", i, country, results);
			this.countries.add(i);
		}
		if (debug) {
			System.out.println("Countries:" + countriesNames.length
					+ " inserted");
		}
		System.out.println();
	}

	/**
	 * **************** Order and XACTS * ******************
	 */
	public void insertOrder_and_CC_XACTS(int n) throws InterruptedException {

		int threads = num_threads;
		int sections = n;
		int firstSection = 0;

		if (n < num_threads) {
			threads = 1;
			firstSection = n;
		} else {
			sections = (int) Math.floor(n / num_threads);
			int rest = n - (num_threads * sections);
			firstSection = sections + rest;
		}

		System.out.println(">>Inserting " + n + " Orders || populatores "
				+ num_threads);

		barrier = new CountDownLatch(threads);

		Order_and_XACTSPopulator[] partial_orders = new Order_and_XACTSPopulator[threads];
		for (int i = threads; i > 0; i--) {

			int base = (threads - i) * sections; // /code copy form above
													// constructors, if
													// reactivated please revise

			Order_and_XACTSPopulator populator = null;
			if (i == 0) {
				populator = new Order_and_XACTSPopulator(firstSection, base);

			} else {
				populator = new Order_and_XACTSPopulator(sections, base);
			}
			partial_orders[threads - i] = populator;
			Thread t = new Thread(populator, "Order populator" + (threads - i));
			t.start();
		}
		barrier.await();

		System.out.println("END");

		for (Order_and_XACTSPopulator populator : partial_orders) {
			results.addResults(populator.returnResults());
			populator.partial_results.cleanResults();
			populator = null;
		}
		System.gc();

	}

	class Order_and_XACTSPopulator implements Runnable {
		int num_orders;
		int base = 0;
		ResultHandler partial_results;

		public Order_and_XACTSPopulator(int num_orders, int base) {
			this.num_orders = num_orders;
			partial_results = new ResultHandler("", rounds);
			this.base = base;
		}

		public void run() {
			this.insertOrder_and_CC_XACTS(num_orders);
		}

		public void insertOrder_and_CC_XACTS(int number_keys) {
			//
			//
			System.out.println("Inserting Order: " + number_keys);
			String[] credit_cards = { "VISA", "MASTERCARD", "DISCOVER", "AMEX",
					"DINERS" };
			String[] ship_types = { "AIR", "UPS", "FEDEX", "SHIP", "COURIER",
					"MAIL" };
			String[] status_types = { "PROCESSING", "SHIPPED", "PENDING",
					"DENIED" };
			//
			// long O_ID = begin_key;
			// // ColumnPath path = new ColumnPath(column_family);
			// // path.setSuper_column("ids".getBytes());
			//
			for (int z = 0; z < number_keys; z++) {
				final int o_ID = base + z;
				int o_C_ID;
				Date o_DATE;
				double o_SUB_TOTAL;
				double o_TAX;
				double o_TOTAL;
				Date o_SHIP_DATE;
				String o_SHIP_TYPE;
				int o_SHIP_ADDR_ID;
				String o_STATUS;

				o_C_ID = customers.get(rand.nextInt(customers.size()));

				GregorianCalendar call = new GregorianCalendar();
				o_DATE = call.getTime();
				// insertInSuperColumn(O_DATE, O_C_ID, column_family, O_ID + "",
				// "O_DATE", write_con);

				o_SUB_TOTAL = rand.nextDouble() * 100 * 4;
				// insertInSuperColumn(O_SUB_TOTAL, O_C_ID, column_family, O_ID
				// + "", "O_SUB_TOTAL", write_con);

				o_TAX = o_SUB_TOTAL * 0.21;
				// insertInSuperColumn(O_TAX, O_C_ID, column_family, O_ID + "",
				// "O_TAX", write_con);

				o_TOTAL = o_SUB_TOTAL + o_TAX;
				// insertInSuperColumn(O_TOTAL, O_C_ID, column_family, O_ID +
				// "", "O_TOTAL", write_con);

				call.add(Calendar.DAY_OF_YEAR, -1 * rand.nextInt(60) + 1);
				o_SHIP_DATE = call.getTime();
				// insertInSuperColumn(O_SHIP_DATE, O_C_ID, column_family, O_ID
				// + "", "O_SHIP_DATE", write_con);

				o_SHIP_TYPE = ship_types[rand.nextInt(ship_types.length)];
				// insertInSuperColumn(O_SHIP_TYPE, O_C_ID, column_family, O_ID
				// + "", "O_SHIP_TYPE", write_con);

				o_STATUS = status_types[rand.nextInt(status_types.length)];
				// insertInSuperColumn(O_STATUS, O_C_ID, column_family, O_ID +
				// "", "O_STATUS", write_con);

				int o_BILL_ADDR_ID = addresses.get(BenchmarkUtil.getRandomInt(
						0, NUM_ADDRESSES - 1));
				// insertInSuperColumn(billAddress.getAddr_id(), O_C_ID,
				// column_family, O_ID + "", "O_BILL_ADDR_ID", write_con);

				o_SHIP_ADDR_ID = addresses.get(BenchmarkUtil.getRandomInt(0,
						NUM_ADDRESSES - 1));
				// insertInSuperColumn(O_SHIP_ADDR.getAddr_id(), O_C_ID,
				// column_family, O_ID + "", "O_SHIP_ADDR_ID", write_con);

				Order order = new Order(o_ID, o_C_ID, o_DATE, o_SUB_TOTAL,
						o_TAX, o_TOTAL, o_SHIP_TYPE, o_SHIP_DATE,
						o_BILL_ADDR_ID, o_SHIP_ADDR_ID, o_STATUS);

				orderInsert("INSERT Order", (base + z), order, partial_results);
				// orders.add(order);
				//
				//
				int number_of_items = rand.nextInt(4) + 1;
				//
				for (int i = 0; i < number_of_items; i++) {
					/**
					 * OL_ID OL_O_ID OL_I_ID OL_QTY OL_DISCOUNT OL_COMMENT
					 */
					int oL_ID;
					int oL_O_ID;

					int oL_I_ID;
					int oL_QTY;
					double oL_DISCOUNT;
					String oL_COMMENT;

					oL_ID = i;

					oL_I_ID = items.get(rand.nextInt(items.size()));
					oL_O_ID = order.O_ID;

					oL_QTY = rand.nextInt(4) + 1;

					oL_DISCOUNT = rand.nextInt(30) / 100.0;

					oL_COMMENT = null;

					oL_COMMENT = BenchmarkUtil.getRandomAString(20, 100);

					OrderLine orderline = new OrderLine(oL_ID, oL_O_ID,
							oL_I_ID, oL_QTY, oL_DISCOUNT, oL_COMMENT);
					orderLineInsert("INSERT Order Lines", oL_ID, orderline,
							partial_results);

				}
				//
				//
				int cX_O_ID;
				String cX_TYPE;
				int cX_CC_NUM;
				String cX_CC_NAME;
				Date cX_EXPIRY;

				cX_O_ID = order.O_ID;

				cX_CC_NUM = BenchmarkUtil.getRandomNString(16);
				int key = base + z;

				cX_TYPE = credit_cards[BenchmarkUtil.getRandomInt(0,
						credit_cards.length - 1)];
				// insert(CX_TYPE, key, column_family, "CX_TYPE", write_con);

				// insert(CX_NUM, key, column_family, "CX_NUM", write_con);

				cX_CC_NAME = BenchmarkUtil.getRandomAString(14, 30);
				// insert(CX_NAME, key, column_family, "CX_NAME", write_con);

				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.DAY_OF_YEAR,
						BenchmarkUtil.getRandomInt(10, 730));
				cX_EXPIRY = cal.getTime();
				// insert(CX_EXPIRY, key, column_family, "CX_EXPIRY",
				// write_con);

				// DATE
				// insert(O_SHIP_DATE, key, column_family, "CX_XACT_DATE",
				// write_con);

				// AMOUNT
				// insert(O_TOTAL, key, column_family, "CX_XACT_AMT",
				// write_con);

				int cX_AUTH_ID = 0; // getRandomAString(5,15);// unused
				int cX_CO_ID = countries.get(BenchmarkUtil.getRandomInt(0,
						countries.size() - 1));
				// insert(country.getCo_id(), key, column_family, "CX_CO_ID",
				// write_con);
				double cX_XACT_AMT = order.O_TOTAL;
				Date cX_XACT_DATE = order.O_SHIP_DATE;

				CCXact ccXact = new CCXact(cX_O_ID, cX_TYPE, cX_CC_NUM,
						cX_CC_NAME, cX_EXPIRY, cX_AUTH_ID, cX_XACT_AMT,
						cX_XACT_DATE, cX_CO_ID);

				ccXactInsert("INSERT_CCXact", key, ccXact, partial_results);

				// O_ID++;
			}
			if (debug) {
				System.out.println("Thread finished: " + number_keys
						+ " orders and xact inserted.");
			}
			System.out.println();
			barrier.countDown();

		}

		public ResultHandler returnResults() {
			return partial_results;
		}
	}
}
