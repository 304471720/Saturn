/**
 * Copyright 2016 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * </p>
 */

package com.vip.saturn.job.console.domain;

import com.vip.saturn.job.console.domain.JobBriefInfo.JobType;
import com.vip.saturn.job.console.utils.SaturnConstants;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author chembo.huang
 *
 */
public class JobConfig implements Serializable {

	private static final long serialVersionUID = 7366583369937964951L;

	private Integer rownum;

	private Long id;

	private String jobName;

	private String jobClass;

	private Integer shardingTotalCount;

	private String timeZone;

	private List<String> timeZonesProvided;

	private String cron;

	private String pausePeriodDate;

	private String pausePeriodTime;

	private String shardingItemParameters;

	private String jobParameter;

	private Integer processCountIntervalSeconds;

	private Boolean failover;

	private String description;

	private Integer timeout4AlarmSeconds;

	private Integer timeoutSeconds;

	private Boolean showNormalLog;

	private String channelName;

	private String jobType;

	private String queueName;

	private String createBy;

	private String lastUpdateBy;

	private Date createTime;

	private Date lastUpdateTime;

	private String namespace;

	private String zkList;

	private Integer loadLevel;
	/** 作业重要等级 */
	private Integer jobDegree;
	/** 作业是否上报执行信息：true表示启用，false表示禁用，对于定时作业默认是启用，对于消息作业默认是禁用 */
	private Boolean enabledReport;
	/** 作业的配置状态：true表示启用，false表示禁用，默认是禁用的 */
	private Boolean enabled;
	/** 从zk的config中读取到的已配置的预分配列表 */
	private String preferList;
	/** 从zk的servers节点读取到的预分配候选列表(servers下status节点存在的所有服务ip，即所有正常可运行的服务器) */
	private List<ExecutorProvided> preferListProvided;

	private Boolean useDispreferList;

	private Boolean localMode = false;

	private Boolean useSerial = false;

	private Boolean isCopyJob = false;

	private String originJobName;

	private String jobMode;

	private String customContext;

	private String dependencies;

	private String groups;

	private List<String> dependenciesProvided;

	public void setDefaultValues() {
		timeZone = timeZone == null ? SaturnConstants.TIME_ZONE_ID_DEFAULT : timeZone;
		timeout4AlarmSeconds = timeout4AlarmSeconds == null || timeout4AlarmSeconds < 0 ? 0 : timeout4AlarmSeconds;
		timeoutSeconds = timeoutSeconds == null || timeoutSeconds < 0 ? 0 : timeoutSeconds;
		processCountIntervalSeconds = processCountIntervalSeconds == null ? 300 : processCountIntervalSeconds;
		showNormalLog = showNormalLog == null ? false : showNormalLog;
		loadLevel = loadLevel == null ? 1 : loadLevel;
		useDispreferList = useDispreferList == null ? true : useDispreferList;
		localMode = localMode == null ? false : localMode;
		useSerial = useSerial == null ? false : useSerial;
		jobDegree = jobDegree == null ? 0 : jobDegree;
		if (enabledReport == null) {
			if (JobType.JAVA_JOB.name().equals(jobType) || JobType.SHELL_JOB.name().equals(jobType)) {
				enabledReport = true;
			} else {
				enabledReport = false;
			}
		}
		jobMode = jobMode == null ? "" : jobMode;
		dependencies = dependencies == null ? "" : dependencies;
		groups = groups == null ? "" : groups;
	}

	public Integer getRownum() {
		return rownum;
	}

	public void setRownum(Integer rownum) {
		this.rownum = rownum;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public Integer getShardingTotalCount() {
		return shardingTotalCount;
	}

	public void setShardingTotalCount(Integer shardingTotalCount) {
		this.shardingTotalCount = shardingTotalCount;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public List<String> getTimeZonesProvided() {
		return timeZonesProvided;
	}

	public void setTimeZonesProvided(List<String> timeZonesProvided) {
		this.timeZonesProvided = timeZonesProvided;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getPausePeriodDate() {
		return pausePeriodDate;
	}

	public void setPausePeriodDate(String pausePeriodDate) {
		this.pausePeriodDate = pausePeriodDate;
	}

	public String getPausePeriodTime() {
		return pausePeriodTime;
	}

	public void setPausePeriodTime(String pausePeriodTime) {
		this.pausePeriodTime = pausePeriodTime;
	}

	public String getShardingItemParameters() {
		return shardingItemParameters;
	}

	public void setShardingItemParameters(String shardingItemParameters) {
		this.shardingItemParameters = shardingItemParameters;
	}

	public String getJobParameter() {
		return jobParameter;
	}

	public void setJobParameter(String jobParameter) {
		this.jobParameter = jobParameter;
	}

	public Integer getProcessCountIntervalSeconds() {
		return processCountIntervalSeconds;
	}

	public void setProcessCountIntervalSeconds(Integer processCountIntervalSeconds) {
		this.processCountIntervalSeconds = processCountIntervalSeconds;
	}

	public Boolean getFailover() {
		return failover;
	}

	public void setFailover(Boolean failover) {
		this.failover = failover;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getTimeout4AlarmSeconds() {
		return timeout4AlarmSeconds;
	}

	public void setTimeout4AlarmSeconds(Integer timeout4AlarmSeconds) {
		this.timeout4AlarmSeconds = timeout4AlarmSeconds;
	}

	public Integer getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(Integer timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public Boolean getShowNormalLog() {
		return showNormalLog;
	}

	public void setShowNormalLog(Boolean showNormalLog) {
		this.showNormalLog = showNormalLog;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getLastUpdateBy() {
		return lastUpdateBy;
	}

	public void setLastUpdateBy(String lastUpdateBy) {
		this.lastUpdateBy = lastUpdateBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getZkList() {
		return zkList;
	}

	public void setZkList(String zkList) {
		this.zkList = zkList;
	}

	public Integer getLoadLevel() {
		return loadLevel;
	}

	public void setLoadLevel(Integer loadLevel) {
		this.loadLevel = loadLevel;
	}

	public Integer getJobDegree() {
		return jobDegree;
	}

	public void setJobDegree(Integer jobDegree) {
		this.jobDegree = jobDegree;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getPreferList() {
		return preferList;
	}

	public void setPreferList(String preferList) {
		this.preferList = preferList;
	}

	public List<ExecutorProvided> getPreferListProvided() {
		return preferListProvided;
	}

	public void setPreferListProvided(List<ExecutorProvided> preferListProvided) {
		this.preferListProvided = preferListProvided;
	}

	public Boolean getUseDispreferList() {
		return useDispreferList;
	}

	public void setUseDispreferList(Boolean useDispreferList) {
		this.useDispreferList = useDispreferList;
	}

	public Boolean getLocalMode() {
		return localMode;
	}

	public void setLocalMode(Boolean localMode) {
		this.localMode = localMode;
	}

	public Boolean getUseSerial() {
		return useSerial;
	}

	public void setUseSerial(Boolean useSerial) {
		this.useSerial = useSerial;
	}

	public Boolean getIsCopyJob() {
		return isCopyJob;
	}

	public void setIsCopyJob(Boolean isCopyJob) {
		this.isCopyJob = isCopyJob;
	}

	public String getOriginJobName() {
		return originJobName;
	}

	public void setOriginJobName(String originJobName) {
		this.originJobName = originJobName;
	}

	public Boolean getEnabledReport() {
		return enabledReport;
	}

	public void setEnabledReport(Boolean enabledReport) {
		this.enabledReport = enabledReport;
	}

	public String getJobMode() {
		return jobMode;
	}

	public void setJobMode(String jobMode) {
		this.jobMode = jobMode;
	}

	public String getCustomContext() {
		return customContext;
	}

	public void setCustomContext(String customContext) {
		this.customContext = customContext;
	}

	public String getDependencies() {
		return dependencies;
	}

	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public List<String> getDependenciesProvided() {
		return dependenciesProvided;
	}

	public void setDependenciesProvided(List<String> dependenciesProvided) {
		this.dependenciesProvided = dependenciesProvided;
	}

	public String getGroups() {
		return groups;
	}

	public void setGroups(String groups) {
		this.groups = groups;
	}
}
