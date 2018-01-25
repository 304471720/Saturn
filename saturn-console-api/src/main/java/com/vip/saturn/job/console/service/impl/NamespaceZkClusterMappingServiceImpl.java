package com.vip.saturn.job.console.service.impl;

import com.google.gson.Gson;
import com.vip.saturn.job.console.domain.NamespaceMigrationOverallStatus;
import com.vip.saturn.job.console.domain.NamespaceZkClusterMappingVo;
import com.vip.saturn.job.console.domain.ZkCluster;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.mybatis.entity.JobConfig4DB;
import com.vip.saturn.job.console.mybatis.entity.NamespaceZkClusterMapping;
import com.vip.saturn.job.console.mybatis.entity.TemporarySharedStatus;
import com.vip.saturn.job.console.mybatis.entity.ZkClusterInfo;
import com.vip.saturn.job.console.mybatis.service.CurrentJobConfigService;
import com.vip.saturn.job.console.mybatis.service.NamespaceZkClusterMapping4SqlService;
import com.vip.saturn.job.console.mybatis.service.TemporarySharedStatusService;
import com.vip.saturn.job.console.mybatis.service.ZkClusterInfoService;
import com.vip.saturn.job.console.repository.zookeeper.CuratorRepository;
import com.vip.saturn.job.console.service.JobService;
import com.vip.saturn.job.console.service.NamespaceZkClusterMappingService;
import com.vip.saturn.job.console.service.RegistryCenterService;
import com.vip.saturn.job.console.utils.ConsoleThreadFactory;
import com.vip.saturn.job.console.utils.JobNodePath;
import com.vip.saturn.job.console.utils.ShareStatusModuleNames;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hebelala
 */
@Service
public class NamespaceZkClusterMappingServiceImpl implements NamespaceZkClusterMappingService {

	private static final Logger log = LoggerFactory.getLogger(NamespaceZkClusterMappingServiceImpl.class);

	@Resource
	private ZkClusterInfoService zkClusterInfoService;

	@Resource
	private NamespaceZkClusterMapping4SqlService namespaceZkclusterMapping4SqlService;

	@Resource
	private CuratorRepository curatorRepository;

	@Resource
	private CurrentJobConfigService currentJobConfigService;

	@Resource
	private JobService jobService;

	@Resource
	private RegistryCenterService registryCenterService;

	@Resource
	private TemporarySharedStatusService temporarySharedStatusService;

	private Gson gson = new Gson();

	private ExecutorService moveNamespaceBatchThreadPool;

	@PostConstruct
	public void init() {
		if (moveNamespaceBatchThreadPool != null) {
			moveNamespaceBatchThreadPool.shutdownNow();
		}
		moveNamespaceBatchThreadPool = Executors
				.newSingleThreadExecutor(new ConsoleThreadFactory("moveNamespaceBatchThread", false));
	}

	@PreDestroy
	public void destroy() {
		if (moveNamespaceBatchThreadPool != null) {
			moveNamespaceBatchThreadPool.shutdownNow();
		}
	}

	@Override
	public List<NamespaceZkClusterMappingVo> getNamespaceZkClusterMappingList() throws SaturnJobConsoleException {
		List<NamespaceZkClusterMappingVo> result = new ArrayList<>();
		List<NamespaceZkClusterMapping> namespaceZkClusterMappingList = namespaceZkclusterMapping4SqlService
				.getAllMappings();
		if (namespaceZkClusterMappingList != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (NamespaceZkClusterMapping tmp : namespaceZkClusterMappingList) {
				try {
					if (tmp.getIsDeleted() == 0) {
						NamespaceZkClusterMappingVo vo = new NamespaceZkClusterMappingVo();
						vo.setNamespace(tmp.getNamespace());
						vo.setZkClusterKey(tmp.getZkClusterKey());
						vo.setCreateTime(sdf.format(tmp.getCreateTime()));
						vo.setCreatedBy(tmp.getCreatedBy());
						vo.setLastUpdateTime(sdf.format(tmp.getLastUpdateTime()));
						vo.setLastUpdatedBy(tmp.getLastUpdatedBy());
						result.add(vo);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return result;
	}

	@Override
	public void initNamespaceZkClusterMapping(String createdBy) throws SaturnJobConsoleException {
		try {
			List<ZkClusterInfo> allZkClusterInfo = zkClusterInfoService.getAllZkClusterInfo();
			if (allZkClusterInfo != null) {
				for (ZkClusterInfo zkClusterInfo : allZkClusterInfo) {
					String zkClusterKey = zkClusterInfo.getZkClusterKey();
					String connectString = zkClusterInfo.getConnectString();
					CuratorFramework curatorFramework = null;
					CuratorRepository.CuratorFrameworkOp curatorFrameworkOp = registryCenterService
							.connectOnly(connectString, null);
					if (curatorFrameworkOp != null) {
						curatorFramework = curatorFrameworkOp.getCuratorFramework();
					}
					if (curatorFramework != null) { // not offline
						try {
							List<String> namespaces = curatorFramework.getChildren().forPath("/");
							if (namespaces != null) {
								for (String namespace : namespaces) {
									if (registryCenterService.namespaceIsCorrect(namespace, curatorFramework)) {
										namespaceZkclusterMapping4SqlService.insert(namespace, "", zkClusterKey,
												createdBy);
									}
								}
							}
						} finally {
							curatorFramework.close();
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SaturnJobConsoleException(e);
		}
	}

	@Override
	public List<String> getZkClusterListWithOnline() throws SaturnJobConsoleException {
		List<String> zkClusterList = new ArrayList<>();
		Collection<ZkCluster> tmp = registryCenterService.getZkClusterList();
		if (tmp != null) {
			Iterator<ZkCluster> iterator = tmp.iterator();
			while (iterator.hasNext()) {
				ZkCluster next = iterator.next();
				if (!next.isOffline()) {
					zkClusterList.add(next.getZkClusterKey());
				}
			}
		}
		return zkClusterList;
	}

	@Transactional(rollbackFor = {SaturnJobConsoleException.class})
	@Override
	public void migrateNamespaceToNewZk(String namespace, String zkClusterKeyNew, String lastUpdatedBy,
			boolean updateDBOnly)
			throws SaturnJobConsoleException {
		try {
			log.info("start move {} to {}", namespace, zkClusterKeyNew);
			if (updateDBOnly) {
				namespaceZkclusterMapping4SqlService.update(namespace, null, zkClusterKeyNew, lastUpdatedBy);
			} else {
				String zkClusterKey = namespaceZkclusterMapping4SqlService.getZkClusterKey(namespace);
				if (zkClusterKey != null && zkClusterKey.equals(zkClusterKeyNew)) {
					// see migrateNamespaceListToNewZk before modify
					throw new SaturnJobConsoleException(
							"The namespace(" + namespace + ") is in " + zkClusterKey);
				}
				ZkCluster zkCluster = registryCenterService.getZkCluster(zkClusterKeyNew);
				if (zkCluster == null) {
					throw new SaturnJobConsoleException("The " + zkClusterKeyNew + " is not exists");
				}
				if (zkCluster.isOffline()) {
					throw new SaturnJobConsoleException("The " + zkClusterKeyNew + " zkCluster is offline");
				}
				String zkAddr = zkCluster.getZkAddr();
				CuratorRepository.CuratorFrameworkOp targetCuratorFrameworkOp = registryCenterService
						.connectOnly(zkAddr,
						null);
				if (targetCuratorFrameworkOp == null) {
					throw new SaturnJobConsoleException("The " + zkClusterKeyNew + " zkCluster is offline");
				}
				CuratorFramework targetCuratorFramework = targetCuratorFrameworkOp.getCuratorFramework();
				try {
					String namespaceNodePath = "/" + namespace;
					if (targetCuratorFramework.checkExists().forPath(namespaceNodePath) != null) {
						targetCuratorFramework.delete().deletingChildrenIfNeeded().forPath(namespaceNodePath);
					}
					String jobsNodePath = namespaceNodePath + JobNodePath.get$JobsNodePath();
					targetCuratorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
							.forPath(jobsNodePath);

					List<JobConfig4DB> configs = currentJobConfigService.findConfigsByNamespace(namespace);
					log.info("get job config list successfully, {}", namespace);
					if (configs != null) {
						for (JobConfig4DB jobConfig : configs) {
							jobService.persistJobFromDB(jobConfig, targetCuratorFrameworkOp);
							log.info("move {}-{} to new zk successfully", namespace, jobConfig.getJobName());
						}
					}
				} finally {
					targetCuratorFramework.close();
				}
				log.info("move {} to zk {} successfully", namespace, zkClusterKeyNew);
				namespaceZkclusterMapping4SqlService.update(namespace, null, zkClusterKeyNew, lastUpdatedBy);
				log.info("update mapping table successfully, {}-{}", namespace, zkClusterKeyNew);
			}
		} catch (SaturnJobConsoleException e) {
			log.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("unexpected exception during move namespace to new zk cluster:" + e.getMessage(), e);
			throw new SaturnJobConsoleException(e.getMessage(), e);
		} finally {
			log.info("end move {} to {}", namespace, zkClusterKeyNew);
		}
	}

	@Override
	public void migrateNamespaceListToNewZk(final String namespaces, final String zkClusterKeyNew,
			final String lastUpdatedBy,
			final boolean updateDBOnly) throws SaturnJobConsoleException {
		final List<String> namespaceList = new ArrayList<>();
		String[] split = namespaces.split(",");
		if (split != null) {
			for (String tmp : split) {
				String namespace = tmp.trim();
				if (!namespace.isEmpty()) {
					namespaceList.add(namespace);
				}
			}
		}
		int size = namespaceList.size();
		final NamespaceMigrationOverallStatus migrationStatus = new NamespaceMigrationOverallStatus(size);
		temporarySharedStatusService.delete(ShareStatusModuleNames.MOVE_NAMESPACE_BATCH_STATUS);
		temporarySharedStatusService.create(ShareStatusModuleNames.MOVE_NAMESPACE_BATCH_STATUS,
				gson.toJson(migrationStatus));
		moveNamespaceBatchThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (String namespace : namespaceList) {
						try {
							migrationStatus.setMoving(namespace);
							temporarySharedStatusService.update(ShareStatusModuleNames.MOVE_NAMESPACE_BATCH_STATUS,
									gson.toJson(migrationStatus));
							migrateNamespaceToNewZk(namespace, zkClusterKeyNew, lastUpdatedBy, updateDBOnly);
							migrationStatus.incrementSuccessCount();
						} catch (SaturnJobConsoleException e) {
							if (("The namespace(" + namespace + ") is in " + zkClusterKeyNew).equals(e.getMessage())) {
								migrationStatus.incrementIgnoreCount();
							} else {
								migrationStatus.incrementFailCount();
							}
						} finally {
							migrationStatus.setMoving("");
							migrationStatus.decrementUnDoCount();
							temporarySharedStatusService.update(ShareStatusModuleNames.MOVE_NAMESPACE_BATCH_STATUS,
									gson.toJson(migrationStatus));
						}
					}
				} finally {
					if (migrationStatus.getSuccessCount() > 0) {
						try {
							registryCenterService.notifyRefreshRegCenter();
						} catch (Exception e) {
							log.error("Fail to refresh registry center.", e);
						}
					}
					migrationStatus.setFinished(true);
					temporarySharedStatusService.update(ShareStatusModuleNames.MOVE_NAMESPACE_BATCH_STATUS,
							gson.toJson(migrationStatus));
				}
			}
		});
	}

	@Override
	public NamespaceMigrationOverallStatus getNamespaceMigrationOverallStatus() {
		TemporarySharedStatus temporarySharedStatus = temporarySharedStatusService
				.get(ShareStatusModuleNames.MOVE_NAMESPACE_BATCH_STATUS);
		if (temporarySharedStatus != null) {
			return gson.fromJson(temporarySharedStatus.getStatusValue(), NamespaceMigrationOverallStatus.class);
		}
		return null;
	}

	@Override
	public void clearNamespaceMigrationOverallStatus() {
		temporarySharedStatusService.delete(ShareStatusModuleNames.MOVE_NAMESPACE_BATCH_STATUS);
	}

}
