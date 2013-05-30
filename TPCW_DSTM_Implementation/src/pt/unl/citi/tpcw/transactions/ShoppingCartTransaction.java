package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class ShoppingCartTransaction extends Transaction<Void> {
	final Executor e;
	final int item_id;
	final boolean create;
	final int id;

	public ShoppingCartTransaction(final Executor e, final int item_id,
			final boolean create, final int id) {
		this.e = e;
		this.item_id = item_id;
		this.create = create;
		this.id = id;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.shoppingCartInteraction(item_id, create, id);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_SHOPPING_CART",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		Executor.operations.incrementAndGet();
		return null;
	}

}
