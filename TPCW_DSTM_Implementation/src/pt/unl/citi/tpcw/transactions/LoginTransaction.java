package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class LoginTransaction extends Transaction<Void> {
	final Executor e;
	final int id;

	public LoginTransaction(final Executor e, final int id) {
		this.e = e;
		this.id = id;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.refreshSession(id);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_LOGIN",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		Executor.operations.incrementAndGet();
		return null;
	}

}