/*
 * *********************************************************************
 * Copyright (c) 2010 Pedro Gomes and Universidade do Minho. All rights
 * reserved. Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ********************************************************************
 */

package org.uminho.gsd.benchmarks.benchmark;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.deuce.Atomic;
import org.deuce.benchmark.Barrier;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.profiling.PRProfiler;
import org.uminho.gsd.benchmarks.helpers.JsonUtil;
import org.uminho.gsd.benchmarks.interfaces.executor.AbstractDatabaseExecutorFactory;
import org.uminho.gsd.benchmarks.interfaces.executor.DatabaseExecutorInterface;
import org.uminho.gsd.benchmarks.interfaces.populator.AbstractBenchmarkPopulator;

public class BenchmarkMain
{
	private static Logger logger = Logger.getLogger(BenchmarkMain.class);

	public static double distribution_factor = -1;
	public static long thinkTime = -1;

	private BenchmarkExecutor executor;

	private String populatorClass;
	private Class worload;
	private Class databaseExecutor;

	private AbstractBenchmarkPopulator populator;

	private static BenchmarkNodeID id;

	// Files
	private String populator_conf;
	private String executor_conf;
	private String workload_conf;

	private static int SlavePort;

	private int number_threads;
	private int operation_number;

	private Map<String, Object> benchmarkExecutorSlaves;

	public static void main(String[] args)
	{
		boolean populate = false;// Populate
		boolean cleanDB = false; // Full clean
		boolean cleanFB = false; // Clean for benchmark execution
		boolean slave = false;
		boolean master = false;
		boolean ocp = false; // only clean and populate

		String workload_alias = "";
		String database_alias = "";

		int num_thread = -1;
		int num_operations = -1;
		double distributionFactor = -1;

		initLogger();

		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];

			if (arg.equalsIgnoreCase("-w"))
			{
				if ((i + 1) != args.length)
				{
					workload_alias = args[i + 1].trim().toLowerCase();
					i++;
				}
				else
				{
					System.out
							.println("[ERROR:] Workload alias option doesn't contain associated parameter");
					return;
				}
			}
			else if (arg.equalsIgnoreCase("-d"))
			{
				if ((i + 1) != args.length)
				{
					database_alias = args[i + 1].trim().toLowerCase();
					i++;
				}
				else
				{
					System.out
							.println("[ERROR:] Data alias option doesn't contain associated parameter");
					return;
				}
			}
			else if (arg.equalsIgnoreCase("-t"))
			{
				if ((i + 1) != args.length)
				{
					try
					{
						num_thread = Integer.parseInt(args[i + 1].trim());
					}
					catch (Exception e)
					{
						System.out
								.println("[ERROR:] An error occurred when parsing the number of threads");
						return;
					}
					i++;
				}
				else
				{
					System.out
							.println("[ERROR:] Thread number option doesn't contain the associated parameter");
					return;
				}
			}
			else if (arg.equalsIgnoreCase("-o"))
			{
				if ((i + 1) != args.length)
				{
					try
					{
						num_operations = Integer.parseInt(args[i + 1].trim());
					}
					catch (Exception e)
					{
						System.out
								.println("[ERROR:] An error occurred when parsing the number of operations");
						return;
					}
					i++;
				}
				else
				{
					System.out
							.println("[ERROR:] Operation number option doesn't contain the associated parameter");
					return;
				}
			}
			else if (arg.equalsIgnoreCase("-df"))
			{
				if ((i + 1) != args.length)
				{
					try
					{
						distributionFactor = Double.parseDouble(args[i + 1]
								.trim());
					}
					catch (Exception e)
					{
						System.out
								.println("[ERROR:] An error occurred when parsing the distribution factor");
						return;
					}
					i++;
				}
				else
				{
					System.out
							.println("[ERROR:] Distribution factor option doesn't contain the associated parameter");
					return;
				}
			}
			else if (arg.equalsIgnoreCase("-tt"))
			{
				if ((i + 1) != args.length)
				{
					try
					{
						thinkTime = Long.parseLong(args[i + 1].trim());
					}
					catch (Exception e)
					{
						System.out
								.println("[ERROR:] An error occurred when parsing the think time");
						return;
					}
					i++;
				}
				else
				{
					System.out
							.println("[ERROR:] The think time option doesn't contain the associated parameter");
					return;
				}
			}
			else if (arg.equalsIgnoreCase("-cb"))
			{
				cleanFB = true;
			}
			else if (arg.equalsIgnoreCase("-p"))
			{
				populate = true;
			}
			else if (arg.equalsIgnoreCase("-pop"))
			{
				ocp = true;
			}
			else if (arg.equalsIgnoreCase("-c"))
			{
				cleanDB = true;
			}
			else if (arg.equalsIgnoreCase("-h"))
			{
				System.out.println(">>Available options:");
				System.out
						.println("------------------------------------------------------");
				System.out
						.println(" -w  <Workload alias>      : a defined workload alias ");
				System.out
						.println(" -d  <Database alias>      : a defined database alias ");
				System.out
						.println(" -t  <Num Threads>         : number of executing threads ");
				System.out
						.println(" -o  <Num Operations>      : number of operations to be executed per thread ");
				System.out
						.println(" -df <distribution factor> : a distribution factor that influences the power law skew on product selection");
				System.out
						.println(" -tt <time milliseconds>   : override the default TPC-W think time to the defined value");
				System.out
						.println("------------------------------------------------------");
				System.out.println(" -c   : clean the database");
				System.out.println(" -cb  : special clean (outdated)");
				System.out.println(" -p   : populate");
				System.out
						.println(" -pop : populate and return (can be used with -c) ");
				System.out
						.println("------------------------------------------------------");
				System.out.println(" -m   : run as master");
				System.out
						.println(" -s <port>   : run as slave in the defined port");
				return;
			}
			else if (arg.equalsIgnoreCase("-s"))
			{
				slave = true;

				if ((i + 1) != args.length)
				{
					try
					{
						SlavePort = Integer.parseInt(args[i + 1]);
					}
					catch (Exception e)
					{
						System.out.println("[ERROR:] ERROR PARSING SLAVE PORT");
						return;
					}
					i++;
				}
				else
				{
					System.out.println("[ERROR:] SLAVE WITH NO AVAILABLE PORT");
					return;
				}

				if (cleanDB /*|| populate*/) // XXX slaves have to populate
				{
					logger.debug("SLAVE DOES NOT ALLOW CLEAN OR POPULATION OPTIONS ");
				}
			}
			else if (arg.equalsIgnoreCase("-m"))
			{
				master = true;
			}
			else
			{
				System.out.println("[WARNING:] OPTION NOT RECOGNIZED: " + arg);
			}
		}

		new BenchmarkMain(master, slave, cleanDB, cleanFB, populate, ocp,
				workload_alias, database_alias, num_thread, num_operations,
				distributionFactor);
		PRProfiler.enabled = false;
		PRProfiler.print();
		barrierEnd.join();
		TribuDSTM.close();
	}

	public BenchmarkMain(boolean master, boolean slave, boolean cleanDB,
			boolean cleanFB, boolean populateDatabase, boolean cap,
			String workload, String database, int thread_number,
			int operation_number, double distribution_fact)
	{
		distribution_factor = distribution_fact;
		boolean success = loadDescriptor(workload, database, thread_number,
				operation_number);
		if (!success)
		{
			logger.fatal("ERROR LOADING FILE");
			return;
		}
		try
		{
			run(master, slave, cleanDB, cleanFB, populateDatabase, cap);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	@Bootstrap(id = 1000)
	static Barrier barrierBegin;
	@Bootstrap(id = 1001)
	static Barrier barrierEnd;
	@Bootstrap(id = 1002)
	static Barrier barrierPop;
	@Bootstrap(id = 1003)
	static Barrier barrierStart;

	@Atomic
	static final void initBarrier()
	{
		if (barrierBegin == null)
		{
			barrierBegin = new Barrier(Integer.getInteger("tribu.replicas"));
		}
		if (barrierEnd == null)
		{
			barrierEnd = new Barrier(Integer.getInteger("tribu.replicas"));
		}
		if (barrierPop == null)
		{
			barrierPop = new Barrier(Integer.getInteger("tribu.replicas"));
		}
		if (barrierStart == null)
		{
			barrierStart = new Barrier(Integer.getInteger("tribu.replicas"));
		}
	}

	public void run(boolean master, boolean slave, boolean cleanDB,
			boolean cleanFB, boolean populate, boolean cap) throws Exception
	{
		// to avoid extra instantiations.
		// if(cap || populate || cleanFB || cleanFB){
		populator = (AbstractBenchmarkPopulator) Class
				.forName(populatorClass)
				.getConstructor(AbstractDatabaseExecutorFactory.class,
						String.class)
				.newInstance(executor.getDatabaseInterface(), populator_conf);
		// }

		initBarrier();
		DatabaseExecutorInterface databaseClient = executor
				.getDatabaseInterface().getDatabaseClient();
		barrierBegin.join();

		if (slave)
		{
			if (populate) { // XXX slaves have to populate
				boolean population_success = populator.populate();
				if (!population_success) {
					return;
				}
			}
			
			barrierPop.join();
			BenchmarkSlave slaveHandler = new BenchmarkSlave(SlavePort,
					executor);
			slaveHandler.run();
		}
		else
		{
			if (cleanDB)
			{
				populator.cleanDB();
			}

			if (cap)
			{
				populator.populate();
				return;
			}

			if (populate)
			{
				boolean population_success = populator.populate();
				if (!population_success)
				{
					return;
				}
			}

			if (cleanFB)
			{
				if (cleanDB && populate)
				{
					logger.info("[INFO:] BENCHMARK CLEANING IS UNNECESSARY, IGNORED");
				}
				else
				{
					populator.BenchmarkClean();
				}
			}

			if (!populate && cleanDB)
			{
				// logger.fatal("THE DATABASE IS PROBABLY EMPTY, ABORTING");
				return;
			}

			if (master)
			{// master, signal slaves
				logger.info("[INFO:] EXECUTING IN MASTER MODE");
				barrierPop.join();
				BenchmarkMaster masterHandler = new BenchmarkMaster(executor,
						benchmarkExecutorSlaves);
				masterHandler.run();

			}
			else
			{ // single node run
				logger.info("[INFO:] EXECUTING IN SINGLE NODE MODE");
				executor.prepare();
				executor.run(new BenchmarkNodeID(1));
				executor.consolidate();
			}
			databaseClient.hashCode();
		}
	}

	public boolean loadDescriptor(String work_alias, String data_alias,
			int num_threads, int num_operations)
	{
		try
		{
			FileInputStream in = null;
			String jsonString_r = "";
			try
			{
				in = new FileInputStream("conf/Benchmark.json");
				BufferedReader bin = new BufferedReader(new InputStreamReader(
						in));
				String s = "";
				StringBuilder sb = new StringBuilder();
				while (s != null)
				{
					sb.append(s);
					s = bin.readLine();
				}
				jsonString_r = sb.toString().replace("\n", "");
				bin.close();
				in.close();
			}
			catch (FileNotFoundException ex)
			{
				logger.error("", ex);
			}
			catch (IOException ex)
			{
				logger.error("", ex);

			}
			finally
			{
				try
				{
					in.close();
				}
				catch (IOException ex)
				{
					logger.error("", ex);
				}
			}

			Map<String, Map<String, Object>> map = JsonUtil
					.getMapMapFromJsonString(jsonString_r);
			Map<String, Object> info = map.get("BenchmarkInfo");

			if (!map.containsKey("BenchmarkInterfaces"))
			{
				logger.fatal("[ERROR:] NO INFORMATION ABOUT THE DATA ENGINE FOUND, ABORTING");
				return false;
			}

			if (!map.containsKey("BenchmarkInfo"))
			{
				logger.fatal("[ERROR] NO CONFIGURATION FILES INFO FOUND");
				return false;
			}

			Map<String, Object> databaseInfo = map.get("BenchmarkInterfaces");

			String databaseClass = "";
			populatorClass = "";

			if (data_alias == null || data_alias.isEmpty())
			{
				databaseClass = (String) databaseInfo
						.get("DataEngineInterface");
				if (databaseClass == null || databaseClass.isEmpty())
				{
					logger.fatal("[ERROR:] NO INFORMATION ABOUT THE DATA ENGINE EXECUTOR");
					return false;
				}
				logger.info("DEFAULT CHOSEN DATABASE ENGINE: " + databaseClass);

				populatorClass = (String) databaseInfo
						.get("BenchmarkPopulator");
				if (populatorClass == null || populatorClass.isEmpty())
				{
					logger.fatal("[ERROR:] NO INFORMATION ABOUT THE POPULATOR");
					return false;
				}
				logger.debug("DEFAULT CHOSEN BENCHMARK POPULATOR: "
						+ populatorClass);

				executor_conf = (String) info
						.get("databaseExecutorConfiguration");
				if (executor_conf == null || executor_conf.isEmpty())
				{
					logger.fatal("[ERROR:] NO DEFAULT CONFIGURATION FILE FOR DATABASE EXECUTOR");
					return false;
				}
			}
			else
			{
				if (!map.containsKey("Database_alias"))
				{
					logger.fatal("No available data alias");
					return false;
				}
				Map<String, Object> database_alias = map.get("Database_alias");
				if (!database_alias.containsKey(data_alias))
				{
					logger.fatal("Data alias " + data_alias
							+ " does not exists");
					return false;
				}

				Map<String, String> alias_info = (Map<String, String>) database_alias
						.get(data_alias);
				// Map<String, String> alias_info =
				// JsonUtil.getMapFromJsonString(alias);

				databaseClass = alias_info.get("DataEngineInterface");
				if (databaseClass == null || databaseClass.isEmpty())
				{
					logger.fatal("[ERROR:] NO INFORMATION ABOUT THE DATA ENGINE EXECUTOR");
					return false;
				}
				logger.info("CHOSEN DATABASE ENGINE FROM ALIAS: "
						+ databaseClass);

				populatorClass = alias_info.get("BenchmarkPopulator");
				if (populatorClass == null || populatorClass.isEmpty())
				{
					logger.debug("[ERROR:] NO INFORMATION ABOUT THE POPULATOR");
					return false;
				}
				logger.debug("CHOSEN BENCHMARK POPULATOR FROM ALIAS: "
						+ populatorClass);

				executor_conf = alias_info.get("databaseExecutorConfiguration");
				if (executor_conf == null || executor_conf.isEmpty())
				{
					logger.fatal("[ERROR:] NO CONFIGURATION FILE FOR DATABASE EXECUTOR ON ALIAS: "
							+ data_alias);
					return false;
				}
			}

			databaseExecutor = Class.forName(databaseClass);

			String benchmarkWorkloadClass = "";

			if (work_alias == null || work_alias.isEmpty())
			{
				benchmarkWorkloadClass = (String) databaseInfo
						.get("BenchmarkWorkload");
				if (benchmarkWorkloadClass == null
						|| benchmarkWorkloadClass.isEmpty())
				{
					logger.debug("[ERROR:] NO INFORMATION ABOUT THE WORKLOAD GENERATOR ON DEFAULT INFO");
					return false;
				}

				workload_conf = (String) info.get("workloadConfiguration");
				if (workload_conf == null || workload_conf.isEmpty())
				{
					logger.fatal("[ERROR:] NO CONFIGURATION FILE FOR WORKLOAD ON DEFAULT INFO");
					return false;
				}
			}
			else
			{
				if (!map.containsKey("Workload_alias"))
				{
					logger.fatal("No available workload alias");
					return false;
				}
				Map<String, Object> workload_alias = map.get("Workload_alias");
				if (!workload_alias.containsKey(work_alias))
				{
					logger.fatal("Workload alias " + work_alias
							+ " does not exists");
					return false;
				}

				Map<String, String> alias_info = (Map<String, String>) workload_alias
						.get(work_alias);
				// Map<String, String> alias_info =
				// JsonUtil.getMapFromJsonString(alias);

				benchmarkWorkloadClass = alias_info.get("BenchmarkWorkload");
				if (benchmarkWorkloadClass == null
						|| benchmarkWorkloadClass.isEmpty())
				{
					logger.fatal("[ERROR:] NO INFORMATION ABOUT THE WORKLOAD GENERATOR ON ALIAS "
							+ work_alias);
					return false;
				}

				workload_conf = alias_info.get("workloadConfiguration");
				if (workload_conf == null || workload_conf.isEmpty())
				{
					logger.fatal("[ERROR:] NO CONFIGURATION FILE FOR WORKLOAD ON ALIAS "
							+ work_alias);
					return false;
				}
			}

			worload = Class.forName(benchmarkWorkloadClass);

			populator_conf = (String) info.get("populatorConfiguration");
			if (populator_conf == null || populator_conf.isEmpty())
			{
				logger.debug("[ERROR:] NO CONFIGURATION FILE FOR POPULATOR");
				return false;
			}

			if (num_threads == -1)
			{
				if (!info.containsKey("thread_number"))
				{
					number_threads = 1;
					logger.warn("[WARNING:] ONE THREAD USED WHEN EXECUTING");
				}
				else
				{
					number_threads = Integer.parseInt((String) info
							.get("thread_number"));
				}
			}
			else
			{
				number_threads = num_threads;
			}

			if (num_operations == -1)
			{
				if (!info.containsKey("operation_number"))
				{
					operation_number = 1000;
					logger.debug("[WARNING:] 1000 OPERATION EXECUTED AS DEFAULT");
				}
				else
				{
					operation_number = Integer.parseInt((String) info
							.get("operation_number"));
					logger.debug("[INFO:] NUMBER OF OPERATIONS -> "
							+ operation_number);
				}

			}
			else
			{
				operation_number = num_operations;
			}

			if (!map.containsKey("BenchmarkSlaves"))
			{
				logger.debug("[WARNING:] NO SLAVES DEFINED");
			}
			else
			{
				Map<String, Object> slave_info = map.get("BenchmarkSlaves");

				for (Entry<String, Object> slave : slave_info.entrySet())
				{
					System.out.println("Running Slave: " + slave.getKey()
							+ " : " + slave.getValue().toString());
				}

				System.out.println();
				benchmarkExecutorSlaves = slave_info;
			}

			System.out.println(">>Selected Database: " + databaseClass);
			System.out.println(">>Selected Workload class: "
					+ worload.getSimpleName());
			System.out.println(">>Selected Workload configuration file: "
					+ workload_conf);
			System.out.println(">>Selected Populator: " + populatorClass);
			System.out.println("-------------------------------------");
			System.out.println(">>Num Threads: " + num_threads);
			System.out.println(">>Num Operations: " + num_operations);
			System.out.println("-------------------------------------");
			System.out.println(">>Think Time: " + thinkTime);
			System.out.println(">>Distribution factor: " + distribution_factor);

			executor = new BenchmarkExecutor(worload, workload_conf,
					databaseExecutor, executor_conf, operation_number,
					number_threads);

			return true;

		}
		catch (ClassNotFoundException ex)
		{
			logger.error("", ex);
		}
		logger.debug("ERROR: THERE IS SOME PROBLEM WITH THE DEFINITIONS FILE OR THE LOADED INTERFACES");
		return false;
	}

	public static void initLogger()
	{
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);// INFO
	}

}
