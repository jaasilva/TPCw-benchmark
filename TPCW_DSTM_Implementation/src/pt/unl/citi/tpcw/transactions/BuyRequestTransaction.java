package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class BuyRequestTransaction extends Transaction<Void> {
	final Executor e;
	final int cart_id;

	public BuyRequestTransaction(final Executor e, final int cart_id) {
		this.e = e;
		this.cart_id = cart_id;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.BuyRequest(cart_id);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_BUY_REQUEST",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		Executor.operations.incrementAndGet();
		return null;
	}

}