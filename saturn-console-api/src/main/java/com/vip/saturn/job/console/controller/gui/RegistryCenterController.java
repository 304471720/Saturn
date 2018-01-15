package com.vip.saturn.job.console.controller.gui;

import com.google.common.collect.Lists;
import com.vip.saturn.job.console.aop.annotation.Audit;
import com.vip.saturn.job.console.aop.annotation.AuditParam;
import com.vip.saturn.job.console.controller.SuccessResponseEntity;
import com.vip.saturn.job.console.domain.MoveNamespaceBatchStatus;
import com.vip.saturn.job.console.domain.NamespaceDomainInfo;
import com.vip.saturn.job.console.domain.RegistryCenterConfiguration;
import com.vip.saturn.job.console.domain.RequestResult;
import com.vip.saturn.job.console.domain.ZkCluster;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.service.NamespaceZkClusterMappingService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/console")
public class RegistryCenterController extends AbstractGUIController {

	@Resource
	private NamespaceZkClusterMappingService namespaceZkClusterMappingService;

	/**
	 * 创建域
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit(name = "createNamespace")
	@PostMapping(value = "/namespaces")
	public SuccessResponseEntity createNamespace(@AuditParam("namespace") @RequestParam String namespace,
			@AuditParam("zkClusterKey") @RequestParam String zkClusterKey)
			throws SaturnJobConsoleException {
		NamespaceDomainInfo namespaceInfo = constructNamespaceDomainInfo(namespace, zkClusterKey);
		registryCenterService.createNamespace(namespaceInfo);
		return new SuccessResponseEntity();
	}

	private NamespaceDomainInfo constructNamespaceDomainInfo(String namespace, String zkClusterKey) {
		NamespaceDomainInfo namespaceInfo = new NamespaceDomainInfo();
		namespaceInfo.setNamespace(namespace);
		namespaceInfo.setZkCluster(zkClusterKey);
		namespaceInfo.setContent("");
		return namespaceInfo;
	}

	/**
	 * 获取所有域列表
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/namespaces/detail")
	public SuccessResponseEntity queryAllNamespaceInfo() {
		List<RegistryCenterConfiguration> namespaceInfoList = Lists.newLinkedList();
		Collection<ZkCluster> zkClusterList = registryCenterService.getZkClusterList();
		for (ZkCluster zkCluster : zkClusterList) {
			namespaceInfoList.addAll(zkCluster.getRegCenterConfList());
		}
		return new SuccessResponseEntity(namespaceInfoList);
	}

	/**
	 * 刷新注册中心
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit(name = "refreshRegistryCenter")
	@PostMapping(value = "/registryCenter/refresh")
	public SuccessResponseEntity refreshRegistryCenter() {
		registryCenterService.refreshRegCenter();
		return new SuccessResponseEntity();
	}

	/**
	 * 获取所有zk集群信息
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/zkClusters")
	public SuccessResponseEntity getZkClustersInfo(@RequestParam(required = false) String status) {
		Collection<ZkCluster> zkClusters = registryCenterService.getZkClusterList();
		if (StringUtils.isBlank(status) || !"online".equals(status)) {
			return new SuccessResponseEntity(zkClusters);
		}

		List<ZkCluster> onlineZkCluster = filterOnlineZkClusters(zkClusters);
		return new SuccessResponseEntity(onlineZkCluster);
	}

	private List<ZkCluster> filterOnlineZkClusters(Collection<ZkCluster> zkClusters) {
		if (zkClusters == null) {
			return Lists.newLinkedList();
		}

		List<ZkCluster> onlineZkClusters = Lists.newLinkedList();
		for (ZkCluster zkCluster : zkClusters) {
			if (!zkCluster.isOffline()) {
				onlineZkClusters.add(zkCluster);
			}
		}

		return onlineZkClusters;
	}

	// 域迁移
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit(name = "migrateZkCluster")
	@PostMapping(value = "/namespaces/zkCluster/migrate")
	public SuccessResponseEntity migrateZkCluster(@AuditParam("namespaces") @RequestParam String namespaces,
			@AuditParam("zkClusterNew") @RequestParam String zkClusterKeyNew,
			@RequestParam(required = false, defaultValue = "false") boolean updateDBOnly)
			throws SaturnJobConsoleException {
		namespaceZkClusterMappingService.moveNamespaceBatchTo(namespaces, zkClusterKeyNew, getUserNameInSession(),
				updateDBOnly);
		return new SuccessResponseEntity();
	}

	//获取域迁移信息
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/namespaces/zkCluster/migrationStatus")
	public SuccessResponseEntity getZkClusterMigrationStatus() throws SaturnJobConsoleException {
		MoveNamespaceBatchStatus moveNamespaceBatchStatus = namespaceZkClusterMappingService
				.getMoveNamespaceBatchStatus();
		if (moveNamespaceBatchStatus == null) {
			throw new SaturnJobConsoleException("The namespace migration status is not existed in db");
		}
		return new SuccessResponseEntity(moveNamespaceBatchStatus);
	}

}
