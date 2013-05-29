/*
 * *********************************************************************
 * Copyright (c) 2010 Pedro Gomes and Universidade do Minho.
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ********************************************************************
 */

package org.uminho.gsd.benchmarks.benchmark;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import org.deuce.distribution.TribuDSTM;
import org.deuce.profiling.Profiler;
import org.uminho.gsd.benchmarks.dataStatistics.ResultHandler;
import org.uminho.gsd.benchmarks.interfaces.Workload.AbstractWorkloadGeneratorFactory;
import org.uminho.gsd.benchmarks.interfaces.Workload.WorkloadGeneratorInterface;
import org.uminho.gsd.benchmarks.interfaces.executor.AbstractDatabaseExecutorFactory;
import org.uminho.gsd.benchmarks.interfaces.executor.DatabaseExecutorInterface;
import org.uminho.gsd.benchmarks.mbean.DataStatistics;

public class BenchmarkExecutor {

	// Node id
	private BenchmarkNodeID nodeId;

	// Benchmark interfaces
	private AbstractWorkloadGeneratorFactory workloadInterface;
	private AbstractDatabaseExecutorFactory databaseInterface;

	/**
	 * number of clients on each benchmarking node *
	 */
	public int num_clients;
	/**
	 * number of operations to be executed in each client *
	 */
	public int num_operations;

	private List<ResultHandler> results;

	public BenchmarkExecutor(Class workloadInterface_class,
			String workload_conf, Class databaseInterface_class,
			String database_conf, int num_operations, int num_clients) {

		try {

			databaseInterface = (AbstractDatabaseExecutorFactory) databaseInterface_class
					.getConstructor(BenchmarkExecutor.class, String.class)
					.newInstance(this, database_conf);
			workloadInterface = (AbstractWorkloadGeneratorFactory) workloadInterface_class
					.getConstructor(BenchmarkExecutor.class, String.class)
					.newInstance(this, workload_conf);
			workloadInterface.setDatabaseFactory(databaseInterface);

		} catch (NoSuchMethodException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (InvocationTargetException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (InstantiationException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (IllegalAccessException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}

		this.num_operations = num_operations;
		this.num_clients = num_clients;

		results = new ArrayList<ResultHandler>();

	}

	public void prepare() throws Exception {
		workloadInterface.init();
	}

	public static int masterNServers = 0;
	public static Socket[] masterSockets = null;
	public static ObjectOutputStream[] masterDataStreams = null;
	public static Thread[] masterThreads = null;
	public static WorkloadGeneratorInterface[] masterWorkloads = null;
	public static Lock[] masterLocks = null;
	public static Socket socket = null;
	public static ObjectInputStream stream = null;
	public static volatile boolean stop = false;

	public void run(BenchmarkNodeID id) {

		workloadInterface.setNodeId(id);
		databaseInterface.setNodeId(id);
		workloadInterface.setClientNumber(num_clients);
		databaseInterface.setClientNumber(num_clients);

		nodeId = id;
		// Synchronization Barrier

		final String workloadName = workloadInterface.getName();
		final CountDownLatch synchronizationBarrier = new CountDownLatch(
				num_clients);

		ResultHandler stats_handler = new ResultHandler("workloadName", -1);
		results.add(stats_handler);
		databaseInterface.setStats_handler(stats_handler);
		databaseInterface.startStats();

		if (BenchmarkMain.generator) {
			masterNServers = Integer.getInteger("tribu.replicas");
			masterWorkloads = new WorkloadGeneratorInterface[masterNServers];
			masterLocks = new Lock[masterNServers];
			for (int i = 0; i < masterNServers; i++) {
				workloadInterface.setNodeId(new BenchmarkNodeID(i + 1));
				workloadInterface.setClientNumber(1);
				masterWorkloads[i] = workloadInterface.getClient();
				masterLocks[i] = new java.util.concurrent.locks.ReentrantLock();
			}
			masterSockets = new Socket[masterNServers];
			masterDataStreams = new ObjectOutputStream[masterNServers];
			for (int i = 0; i < masterNServers; i++) {
				System.err.print("Connecting to server " + (i + 1) + " of "
						+ masterNServers + "... ");
				boolean success = false;
				while (!success) {
					try {
						masterSockets[i] = new Socket("node" + (i + 1), 54321);
						masterDataStreams[i] = new ObjectOutputStream(
								masterSockets[i].getOutputStream());
						success = true;
					} catch (UnknownHostException e) {
						//
					} catch (IOException e) {
						//
					}
				}
				System.err.println("done.");
			}
		} else { // master or slave
			try {
				System.err.print("Waiting for generator... ");
				ServerSocket ss = new ServerSocket(54321);
				socket = ss.accept();
				stream = new ObjectInputStream(
						socket.getInputStream());
				System.err.println("done.");
			} catch (IOException e) {
				e.printStackTrace();
			}

			BenchmarkMain.barrierGenerator.join();
			Profiler.enabled = true;
			TribuDSTM.startWorkers();
			TribuDSTM.executeReadOnly = true;
		}

		for (int client_index = 0; client_index < num_clients; client_index++) {

			final DatabaseExecutorInterface executor = databaseInterface
					.getDatabaseClient();
			final WorkloadGeneratorInterface workloadGenerator = workloadInterface
					.getClient();
			final ResultHandler resultHandler = new ResultHandler(workloadName,
					-1);
			results.add(resultHandler);

			final int index = client_index;
			// Create a runnable to run the client. executor.start() must be
			// sequential
			Runnable clientRunnable = new Runnable() {
				public void run() {
					executor.start(workloadGenerator, nodeId, BenchmarkMain.generator ? index % masterNServers : num_operations,
							resultHandler);
					synchronizationBarrier.countDown();
				}
			};
			Thread clientThread = new Thread(clientRunnable, "client:"
					+ client_index);
			clientThread.start();
			System.out.println("Started client " + client_index + ".");
		}
		DataStatistics dataStatistics = new DataStatistics();
		dataStatistics.setTpm_resultHandler(stats_handler);

		try {
			Thread.sleep(BenchmarkMain.duration);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stop = true;

		if (!BenchmarkMain.generator) {
			TribuDSTM.stopWorkers();
			TribuDSTM.executeReadOnly = false;
		}

		try {
			System.out.println("Waiting for " + num_clients + " clients...");
			synchronizationBarrier.await();
		} catch (InterruptedException e) {
			System.out
					.println("[ERROR:] Error in client execution. Interruption on synchronization barrier");
		}

		if (BenchmarkMain.generator) {
			for (Socket socket : masterSockets) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else { // master or slave
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		workloadInterface.finishExecution(results);
	}

	protected AbstractDatabaseExecutorFactory getDatabaseInterface() {
		return databaseInterface;
	}

	public void consolidate() throws Exception {
		BenchmarkSlave.terminated = true;
		workloadInterface.consolidate();
	}
}
