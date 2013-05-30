package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class NewProductsTransaction extends Transaction<Void> {
	final Executor e;
	final String field;

	public NewProductsTransaction(final Executor e, final String field) {
		this.e = e;
		this.field = field;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.newProducts(field);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_NEW_PRODUCTS",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		Executor.operations.incrementAndGet();
		return null;
	}

}
