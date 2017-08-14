package com.vip.saturn.job.sharding.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.saturn.job.sharding.node.SaturnExecutorsNode;

/**
 * This class should be modified, when the curator bug is fixed. The bug is <a href="https://issues.apache.org/jira/browse/CURATOR-430">CURATOR-430</a>
 *
 * @author hebelala
 */
public class ExecutorCleanService {
	private static Logger LOGGER = LoggerFactory.getLogger(ExecutorCleanService.class);

    private CuratorFramework curatorFramework;

    public ExecutorCleanService(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    /**
     * delete $SaturnExecutors/executors/xxx<br>
     * delete $Jobs/job/servers/xxx<br>
     * delete $Jobs/job/config/preferList content about xxx
     */
    public void clean(String executorName) {
        try {
            String cleanNodePath = SaturnExecutorsNode.getExecutorCleanNodePath(executorName);
            if (curatorFramework.checkExists().forPath(cleanNodePath) != null) {
                byte[] cleanNodeBytes = curatorFramework.getData().forPath(cleanNodePath);
                if (cleanNodeBytes != null) {
                    String cleanNodeData = new String(cleanNodeBytes, "UTF-8");
                    if (Boolean.parseBoolean(cleanNodeData)) {
                        if (curatorFramework.checkExists().forPath(SaturnExecutorsNode.getExecutorIpNodePath(executorName)) == null) {
                            LOGGER.info("Clean the executor {}", executorName);
                            // delete $SaturnExecutors/executors/xxx
                            deleteExecutor(executorName);

                            for(String jobName : getJobList()) {
                                // delete $Jobs/job/servers/xxx
                                deleteJobServerExecutor(jobName, executorName);
                                // delete $Jobs/job/config/preferList content about xxx
                                updateJobConfigPreferListContentToRemoveDeletedExecutor(jobName, executorName);
                            }
                        } else {
                            LOGGER.info("The executor {} is online now, no necessary to clean", executorName);
                        }
                    }
                }
            }
        } catch (NoNodeException e) { // NOSONAR
        	// ignore
        } catch (Exception e) {
            LOGGER.error("Clean the executor " + executorName + " error", e);
        }
    }

    private List<String> getJobList() {
        List<String> jobList = new ArrayList<>();
        try {
            String jobsNodePath = SaturnExecutorsNode.$JOBSNODE_PATH;
            if (curatorFramework.checkExists().forPath(jobsNodePath) != null) {
                List<String> tmp = curatorFramework.getChildren().forPath(jobsNodePath);
                if (tmp != null && !tmp.isEmpty()) {
                    jobList.addAll(tmp);
                }
            }
        } catch (NoNodeException e) { // NOSONAR
            // ignore
        } catch (Exception e) {
            LOGGER.error("Clean the executor, getJobList error", e);
        }
        return jobList;
    }

    /**
     * delete $SaturnExecutors/executors/xxx
     */
    private void deleteExecutor(String executorName) {
        try {
            String executorNodePath = SaturnExecutorsNode.getExecutorNodePath(executorName);
            if(curatorFramework.checkExists().forPath(executorNodePath) != null) {
                List<String> executorChildren = curatorFramework.getChildren().forPath(executorNodePath);
                // 删除executor下子节点，catch异常，打日志，继续删其他节点
                if (executorChildren != null) {
                    for (String tmp : executorChildren) {
                        try {
                            curatorFramework.delete().deletingChildrenIfNeeded().forPath(executorNodePath + "/" + tmp);
                        } catch (NoNodeException e) { // NOSONAR
                            // ignore
                        } catch (Exception e) {
                            LOGGER.error("Clean the executor " + executorName + " error", e);
                        }
                    }
                }
                curatorFramework.delete().deletingChildrenIfNeeded().forPath(executorNodePath);
            }
        } catch (NoNodeException e) { // NOSONAR
            // ignore
        } catch (Exception e) {
            LOGGER.error("Clean the executor, deleteExecutor(" + executorName + ") error", e);
        }
    }

    /**
     * delete $Jobs/job/servers/xxx
     */
    private void deleteJobServerExecutor(String jobName, String executorName) {
        try {
            String jobServersExecutorNodePath = SaturnExecutorsNode.getJobServersExecutorNodePath(jobName, executorName);
            if (curatorFramework.checkExists().forPath(jobServersExecutorNodePath) != null) {
                List<String> jobServersChildren = curatorFramework.getChildren().forPath(jobServersExecutorNodePath);
                // 删除servers下子节点，catch异常，打日志，继续删其他节点
                if (jobServersChildren != null) {
                    for (String tmp : jobServersChildren) {
                        try {
                            curatorFramework.delete().deletingChildrenIfNeeded().forPath(jobServersExecutorNodePath + "/" + tmp);
                        } catch (NoNodeException e) {
                            // ignore
                        } catch (Exception e) {
                            LOGGER.error("Clean the executor " + executorName + " error", e);
                        }
                    }
                }
                curatorFramework.delete().deletingChildrenIfNeeded().forPath(jobServersExecutorNodePath);
            }
        } catch (NoNodeException e) { // NOSONAR
            // ignore
        } catch (Exception e) {
            LOGGER.error("Clean the executor, deleteJobServerExecutor(" + jobName + ", " + executorName + ") error", e);
        }
    }

    /**
     * delete $Jobs/job/config/preferList content about xxx
     */
    private void updateJobConfigPreferListContentToRemoveDeletedExecutor(String jobName, String executorName) {
        try {
            String jobConfigPreferListNodePath = SaturnExecutorsNode.getJobConfigPreferListNodePath(jobName);
            if (curatorFramework.checkExists().forPath(jobConfigPreferListNodePath) != null) {
                Stat stat = new Stat();
                byte[] jobConfigPreferListNodeBytes = curatorFramework.getData().storingStatIn(stat).forPath(jobConfigPreferListNodePath);
                if (jobConfigPreferListNodeBytes != null) {
                    // build the new prefer list string
                    StringBuilder sb = new StringBuilder();
                    String[] split = new String(jobConfigPreferListNodeBytes, "UTF-8").split(",");
                    for (String tmp : split) {
                        String tmpTrim = tmp.trim();
                        if (!tmpTrim.equals(executorName)) {
                            if (sb.length() > 0) {
                                sb.append(',');
                            }
                            sb.append(tmpTrim);
                        }
                    }
                    curatorFramework.setData().withVersion(stat.getVersion()).forPath(jobConfigPreferListNodePath, sb.toString().getBytes("UTF-8"));
                }
            }
        } catch (NoNodeException e) { // NOSONAR
            // ignore
        } catch (KeeperException.BadVersionException e) { // NOSONAR
            // ignore
        } catch (Exception e) {
            LOGGER.error("Clean the executor, updateJobConfigPreferListContentToRemoveDeletedExecutor(" + jobName + ", " + executorName + ") error", e);
        }
    }

}
