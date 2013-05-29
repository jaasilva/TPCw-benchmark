package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class OrderInquiryTransaction extends Transaction<Void> {
	final Executor e;
	final int customer_id;

	public OrderInquiryTransaction(final Executor e, final int customer_id) {
		this.e = e;
		this.customer_id = customer_id;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.OrderInquiry(customer_id);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_ORDER_INQUIRY",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		e.num_operations++;
		return null;
	}

}
