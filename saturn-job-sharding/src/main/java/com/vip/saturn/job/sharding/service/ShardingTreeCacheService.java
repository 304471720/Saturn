package com.vip.saturn.job.sharding.service;

import com.vip.saturn.job.sharding.TreeCacheThreadFactory;
import com.vip.saturn.job.sharding.entity.ShardingTreeCache;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.utils.CloseableExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hebelala
 */
public class ShardingTreeCacheService {

	private static final Logger logger = LoggerFactory.getLogger(ShardingTreeCacheService.class);

	private String namespace;
	private CuratorFramework curatorFramework;
	private ShardingTreeCache shardingTreeCache;
	private ExecutorService executorService;
	private AtomicBoolean isShutdownFlag = new AtomicBoolean(true);

	public ShardingTreeCacheService(String namespace, CuratorFramework curatorFramework) {
		this.namespace = namespace;
		this.curatorFramework = curatorFramework;
		this.shardingTreeCache = new ShardingTreeCache();
	}

	public void addTreeCacheIfAbsent(String path, int depth) {
		synchronized (isShutdownFlag) {
			if (!isShutdownFlag.get()) {
				try {
					String fullPath = namespace + path;
					if (!shardingTreeCache.containsTreeCache(path, depth)) {
						TreeCache treeCache = TreeCache.newBuilder(curatorFramework, path)
								.setExecutor(new CloseableExecutorService(executorService, false)).setMaxDepth(depth)
								.build();
						treeCache.start();
						TreeCache treeCacheOld = shardingTreeCache.putTreeCacheIfAbsent(path, depth, treeCache);
						if (treeCacheOld != null) {
							treeCache.close();
						} else {
							logger.info("create TreeCache, full path is {}, depth is {}", fullPath, depth);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.warn("{}-ShardingTreeCacheService has been shutdown, cannot do addTreeCacheIfAbsent", namespace);
			}
		}
	}

	public void addTreeCacheListenerIfAbsent(String path, int depth, TreeCacheListener treeCacheListener) {
		synchronized (isShutdownFlag) {
			if (!isShutdownFlag.get()) {
				String fullPath = namespace + path;
				TreeCacheListener treeCacheListenerOld = shardingTreeCache.addTreeCacheListenerIfAbsent(path, depth,
						treeCacheListener);
				if (treeCacheListenerOld == null) {
					logger.info("add {}, full path is {}, depth is {}", treeCacheListener.getClass().getSimpleName(),
							fullPath, depth);
				}
			} else {
				logger.warn("{}-ShardingTreeCacheService has been shutdown, cannot do addTreeCacheListenerIfAbsent",
						namespace);
			}
		}
	}

	public void removeTreeCache(String path, int depth) {
		synchronized (isShutdownFlag) {
			if (!isShutdownFlag.get()) {
				shardingTreeCache.removeTreeCache(path, depth);
			} else {
				logger.warn("{}-ShardingTreeCacheService has been shutdown, cannot do removeTreeCache", namespace);
			}
		}
	}

	public void start() {
		synchronized (isShutdownFlag) {
			if (isShutdownFlag.compareAndSet(true, false)) {
				shutdown0();
				executorService = Executors
						.newSingleThreadExecutor(new TreeCacheThreadFactory("NamespaceSharding-" + namespace));
			} else {
				logger.warn("{}-ShardingTreeCacheService has already started, unnecessary to start", namespace);
			}
		}
	}

	private void shutdown0() {
		shardingTreeCache.shutdown();
		if (executorService != null) {
			executorService.shutdownNow();
		}
	}

	public void shutdown() {
		synchronized (isShutdownFlag) {
			if (isShutdownFlag.compareAndSet(false, true)) {
				shutdown0();
			} else {
				logger.warn("{}-ShardingTreeCacheService has already shutdown, unnecessary to shutdown", namespace);
			}
		}
	}

}
