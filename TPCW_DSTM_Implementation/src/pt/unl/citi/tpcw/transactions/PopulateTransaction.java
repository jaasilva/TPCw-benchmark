package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;
import pt.unl.citi.tpcw.Operations;

@ExcludeTM
public class PopulateTransaction extends Transaction<Void> {
	final Executor e;
	final int num_countries;
	final int num_authors;
	final int num_customers;
	final int num_items;

	public PopulateTransaction(final Executor e, final int num_countries,
			final int num_authors, final int num_customers, final int num_items) {
		this.e = e;
		this.num_countries = num_countries;
		this.num_authors = num_authors;
		this.num_customers = num_customers;
		this.num_items = num_items;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.createTrees(num_countries, num_authors, num_customers, num_items);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult(Operations.OP_POPULATE,
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		e.num_operations++;
		return null;
	}

}
