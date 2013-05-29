package pt.unl.citi.tpcw.transactions;

import org.deuce.transaction.Transaction;
import org.deuce.transform.ExcludeTM;

import pt.unl.citi.tpcw.Executor;

@ExcludeTM
public class RegisterTransaction extends Transaction<Void> {
	final Executor e;
	final int process_id;
	final int id;

	public RegisterTransaction(final Executor e, final int process_id, final int id) {
		this.e = e;
		this.process_id = process_id;
		this.id = id;
	}

	@Override
	public Void execute() {
		long init_time = System.nanoTime();
		e.CustomerRegistration(process_id, id);
		long end_time = System.nanoTime();
		e.client_result_handler.logResult("OP_REGISTER",
				((end_time / 1000 / 1000) - (init_time / 1000 / 1000)));
		e.counter.increment();
		e.num_operations++;
		return null;
	}

}
