package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class HomeTransaction extends Transaction<Void> {
	final Executor e;
	final int costumer;
	final int item_id;

	public HomeTransaction(final Executor e, final int costumer,
			final int item_id) {
		this.e = e;
		this.costumer = costumer;
		this.item_id = item_id;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.HomeOperation(costumer, item_id);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_HOME",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		Executor.operations.incrementAndGet();
		return null;
	}

}
