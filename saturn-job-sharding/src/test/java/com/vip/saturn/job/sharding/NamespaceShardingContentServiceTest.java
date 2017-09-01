package com.vip.saturn.job.sharding;

import static org.assertj.core.api.Assertions.assertThat;
import com.vip.saturn.job.sharding.entity.Executor;
import com.vip.saturn.job.sharding.entity.Shard;
import com.vip.saturn.job.sharding.service.NamespaceShardingContentService;
import com.vip.saturn.job.utils.NestedZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaopeng.he on 2016/7/12.
 */
public class NamespaceShardingContentServiceTest {

	private NestedZkUtils nestedZkUtils;

	@Before
	public void setUp() throws Exception {
		nestedZkUtils = new NestedZkUtils();
		nestedZkUtils.startServer();

	}

	@After
	public void tearDown() throws IOException {
		if (nestedZkUtils != null) {
			nestedZkUtils.stopServer();
		}
	}

	@Test
	@Ignore
	public void testBigData() throws Exception {
		CuratorFramework framework = nestedZkUtils.createClient("namespace");
		NamespaceShardingContentService namespaceShardingContentService = new NamespaceShardingContentService(
				framework);
		List<Executor> executorList = new ArrayList<>();
		for (int i = 1; i < 250; i++) {
			Executor executor = new Executor();
			executor.setExecutorName("e" + i);
			executor.setIp("ip" + i);
			List<Shard> shardList = new ArrayList<>();
			for (int j = 1; j < 100; j++) {
				Shard shard = new Shard();
				shard.setJobName("job" + i + j);
				shard.setItem(j);
				shard.setLoadLevel(j);
				shardList.add(shard);
				executor.setTotalLoadLevel(executor.getTotalLoadLevel() + shard.getLoadLevel() * shard.getItem());
			}
			executor.setShardList(shardList);
			executorList.add(executor);
		}
		namespaceShardingContentService.persistDirectly(executorList);

		List<Executor> executorList1 = namespaceShardingContentService.getExecutorList();

		assertThat(executorList1.size()).isEqualTo(executorList.size());
	}

}
