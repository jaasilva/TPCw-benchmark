package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class SearchTransaction extends Transaction<Void> {
	final Executor e;
	final String term;
	final String field;

	public SearchTransaction(final Executor e, final String term,
			final String field) {
		this.e = e;
		this.term = term;
		this.field = field;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.doSearch(term, field);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_SEARCH",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		e.num_operations++;
		return null;
	}

}
