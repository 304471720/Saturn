package com.vip.saturn.job.executor;

import com.vip.saturn.job.threads.SaturnThreadFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hebelala
 */
public abstract class FixedConnectionStateListener implements ConnectionStateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FixedConnectionStateListener.class);

	private String executorName;
	private volatile boolean connected = false;
	private volatile boolean closed = false;
	private ExecutorService checkLostThread;

	public FixedConnectionStateListener(String executorName) {
		this.executorName = executorName;
		this.checkLostThread = Executors
				.newSingleThreadExecutor(new SaturnThreadFactory(executorName + "-check-lost-thread", false));
	}

	private long getSessionId(CuratorFramework client) {
		long sessionId;
		try {
			sessionId = client.getZookeeperClient().getZooKeeper().getSessionId();
		} catch (Exception e) {// NOSONAR
			return -1;
		}
		return sessionId;
	}

	@Override
	public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
		if (closed) {
			return;
		}
		if (ConnectionState.SUSPENDED == newState) {
			connected = false;
			String clientStr = client.toString();
			LOGGER.warn("The executor {} found zk is SUSPENDED, client is {}", executorName, clientStr);
			final long sessionId = getSessionId(client);
			if (!closed) {
				checkLostThread.submit(new Runnable() {
					@Override
					public void run() {
						do {
							try {
								Thread.sleep(1000L);
							} catch (InterruptedException e) {
							}
							if (closed) {
								break;
							}
							long newSessionId = getSessionId(client);
							if (sessionId != newSessionId) {
								LOGGER.warn("The executor {} is going to restart for zk lost, client is {}",
										executorName, client);
								whenLost();
								break;
							}
						} while (!closed && !connected);
					}
				});
			}
		} else if (ConnectionState.RECONNECTED == newState) {
			LOGGER.warn("The executor {} found zk is RECONNECTED, client is {}", executorName, client);
			connected = true;
		}
	}

	public abstract void whenLost();

	public void close() {
		this.closed = true;
		this.checkLostThread.shutdownNow();
	}

}
