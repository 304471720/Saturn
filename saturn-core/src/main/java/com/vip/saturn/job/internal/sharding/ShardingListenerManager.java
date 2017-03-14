package com.vip.saturn.job.internal.sharding;

import java.io.UnsupportedEncodingException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.saturn.job.basic.JobScheduler;
import com.vip.saturn.job.internal.config.ConfigurationService;
import com.vip.saturn.job.internal.listener.AbstractJobListener;
import com.vip.saturn.job.internal.listener.AbstractListenerManager;
import com.vip.saturn.job.internal.storage.JobNodePath;

/**
 * 分片监听管理器.
 *
 * @author linzhaoming
 *
 */
public class ShardingListenerManager extends AbstractListenerManager {
	static Logger log = LoggerFactory.getLogger(ShardingListenerManager.class);

	private ConfigurationService confService;

	private final ShardingNode shardingNode;

	private boolean isShutdown;


	public ShardingListenerManager(final JobScheduler jobScheduler) {
        super(jobScheduler);
        confService = jobScheduler.getConfigService();
        shardingNode = new ShardingNode(jobName);
    }
	
	@Override
	public void start() {
		// addDataListener(new ShardingNecessaryJobListener(), jobName);
        zkCacheManager.addTreeCacheListener(new ShardingNecessaryJobListener(), JobNodePath.getNodeFullPath(jobName, ShardingNode.NECESSARY), 0);
	}

	@Override
	public void shutdown() {
		super.shutdown();
		isShutdown = true;
		confService.shutdown();
        zkCacheManager.closeTreeCache(JobNodePath.getNodeFullPath(jobName, ShardingNode.NECESSARY), 0);
	}

	class ShardingNecessaryJobListener extends AbstractJobListener {

		@Override
		protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
			if (isShutdown) {
				return;
			}		
			if (jobScheduler == null || jobScheduler.getJob() == null) {
				return;
			}
			Type type = event.getType();
			if (shardingNode.isShardingNecessaryPath(path)
					&& (type.equals(Type.NODE_ADDED) || type.equals(Type.NODE_UPDATED))) { // 是否有必要resharding，不应该在这里判断，要确保每个executor的resharding事件都执行
				byte[] data = event.getData().getData();
				String dataStr = null;
				if(data != null) {
					try {
						dataStr = new String(data, "UTF-8");
					} catch (UnsupportedEncodingException e) {
					}
				}
				log.info("[{}] msg={} trigger on-resharding event, type:{}, path:{}, data:{}", jobName, jobName,type,path, dataStr);
				jobScheduler.getJob().onResharding();
			}
		}
	}

}
