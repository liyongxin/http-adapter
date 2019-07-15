/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container;

import com.alibaba.fastjson.JSON;
import com.tencent.tsf.container.service.ClusterManagerService;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @version Version 1.0
 * @title ClusterManagerServiceTest
 * @title Yongxin Li
 * @date 2019/7/9 13:24
 * @description TODO
 */

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlaudaApplication.class)
@ContextConfiguration
public class ClusterManagerServiceTest {

	@Autowired
	private ClusterManagerService clusterManagerService;

	//测试
	private String authorization = "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjVlNjc1NDAxM2VkZWUwNmRjY2FlMTQ3Y2RkY2ZlZTU1NjFiOWUwMjAifQ.eyJpc3MiOiJodHRwczovLzEwLjAuMTI4Ljc2L2RleCIsInN1YiI6IkNpUXdPR0U0TmpnMFlpMWtZamc0TFRSaU56TXRPVEJoT1MwelkyUXhOall4WmpVME5qWVNCV3h2WTJGcyIsImF1ZCI6ImFsYXVkYS1hdXRoIiwiZXhwIjoxNTYyOTI5NTU3LCJpYXQiOjE1NjI4NDMxNTcsIm5vbmNlIjoiYWxhdWRhLWNvbnNvbGUiLCJhdF9oYXNoIjoibElvbTJFT0pGQzNCSDhwV2FtQWdUUSIsImVtYWlsIjoiYWRtaW5AYWxhdWRhLmlvIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJhZG1pbiIsImV4dCI6eyJpc19hZG1pbiI6dHJ1ZSwiY29ubl9pZCI6ImxvY2FsIn19.tZ5MclaOlIwNHrQwgzQBBNqPlpGmVxfrHbLVAUpLoB_cNHfWer9W248Gu86Te8m9xTxHoyjZkx2nwveVuqyDTC_gCHh1DFONx-VehKoqZlYGdbtY2PlNc3THVI8VndmNKpwr4dadmjA8I8zPIupU2TrOyMR_4W2qxbF6IG4dueu8PoReLjeZmUxgHmFq57pH8xscgQxAyC6SV2q6ixeGAnwDcTc244UynBPLzZ7bGFJNUhoPL7gQqTl_vcikf72-G9I6ZvHsj8gmyiAKu-TAQw0L86eNLFfTa3hGFue24g6tzclGbVYbPi_2o2YiD0XH0AkepqCIaO6Ot3dZ8GHsUA";
	private String clusterId = "high";

	@Test
	public void createEmptyClusterTest() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		String result = clusterManagerService.createCluster(headers, clusterId);
		System.out.println("------------------------------" + result);
	}

	@Test
	public void getClusterByIdTest() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		String result = clusterManagerService.getClusterById(headers, clusterId);
		System.out.println("------------------------------" + result);
	}

	@Test
	public void clusterUsageTest() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		String result = clusterManagerService.clusterUsage(headers, clusterId);
		System.out.println("------------------------------" + result);
	}

	@Test
	public void getKubernetesAPIServerTest() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		String result = clusterManagerService.getKubernetesAPIServer(headers, clusterId);
		System.out.println("------------------------------" + JSON.toJSONString(result));
	}

	@Test
	public void getClustersTest() {
		Map<String, String> headers = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("limit", 2);
		params.put("offset", 2);
		headers.put("Authorization", authorization);
		String result = clusterManagerService.getClusters(headers, params);
		System.out.println("------------------------------" + result);
	}

	@Test
	public void getClusterNodesTest() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		Map<String, Object> params = new HashMap<>();
		params.put("limit", 5);
		//params.put("offset", 2);
		String result = clusterManagerService.clusterNodes(headers, clusterId, params);
		System.out.println("------------------------------" + result);
	}

}
