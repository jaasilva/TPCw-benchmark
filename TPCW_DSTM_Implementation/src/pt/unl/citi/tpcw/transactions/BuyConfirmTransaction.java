package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class BuyConfirmTransaction extends Transaction<Void> {
	final Executor e;
	final int cust_id;
	final int process_id;
	final int cart_id;

	public BuyConfirmTransaction(final Executor e, final int item_id,
			final int process_id, final int cart_id) {
		this.e = e;
		this.cust_id = item_id;
		this.process_id = process_id;
		this.cart_id = cart_id;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.BuyConfirm(cust_id, process_id, cart_id);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_BUY_CONFIRM",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		e.num_operations++;
		return null;
	}

}
