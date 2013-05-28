package pt.unl.citi.tpcw;

import org.uminho.gsd.benchmarks.benchmark.BenchmarkExecutor;
import org.uminho.gsd.benchmarks.helpers.TPM_counter;
import org.uminho.gsd.benchmarks.interfaces.executor.AbstractDatabaseExecutorFactory;
import org.uminho.gsd.benchmarks.interfaces.executor.DatabaseExecutorInterface;

public class ExecutorFactory extends AbstractDatabaseExecutorFactory {

	public ExecutorFactory(BenchmarkExecutor executor, String conf_file) {
		super(executor, conf_file);
		init();
		// TODO Auto-generated constructor stub
	}

	@Override
	public DatabaseExecutorInterface getDatabaseClient() {
		TPM_counter tpm_counter = new TPM_counter();
		registerCounter(tpm_counter);
		return new Executor(tpm_counter);
	}
	
	private void init() {
		initTPMCounting();
	}

}
