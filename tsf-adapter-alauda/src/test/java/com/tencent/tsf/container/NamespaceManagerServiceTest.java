/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container;

import com.tencent.tsf.container.service.NamespaceManagerService;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @version Version 1.0
 * @title NamespaceManagerServiceTest
 * @title Yongxin Li
 * @date 2019/7/4 19:31
 * @description TODO
 */

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlaudaApplication.class)
@ContextConfiguration
public class NamespaceManagerServiceTest {

	@Autowired
	private NamespaceManagerService namespaceManagerService;

	//测试
	private String authorization = "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6ImVlY2I2ZmM5YjkyOTU1YzQyODEzMmQ2ZWYwNTA3YWJhMDY2NjBkNzIifQ.eyJpc3MiOiJodHRwczovLzEwLjAuMTI4Ljc2L2RleCIsInN1YiI6IkNpUXdPR0U0TmpnMFlpMWtZamc0TFRSaU56TXRPVEJoT1MwelkyUXhOall4WmpVME5qWVNCV3h2WTJGcyIsImF1ZCI6ImFsYXVkYS1hdXRoIiwiZXhwIjoxNTYyNzI1OTM0LCJpYXQiOjE1NjI2Mzk1MzQsIm5vbmNlIjoiYWxhdWRhLWNvbnNvbGUiLCJhdF9oYXNoIjoia3BibkM1Y013MFZoTUYtdXJRU2FrUSIsImVtYWlsIjoiYWRtaW5AYWxhdWRhLmlvIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJhZG1pbiIsImV4dCI6eyJpc19hZG1pbiI6dHJ1ZSwiY29ubl9pZCI6ImxvY2FsIn19.087PFx0mPK-bwQKFPyL7Y2DbKHHotIXfFGCWbAMBrMWP5ocMSOjBd2WOupyqzaUDAyhuXSobdQzF9ldNWyWu2Ewj_XT-nnaWjAI1Wi5DvUF8ZVBPqu-xGQc6BN09m3WOLGDot28QLsZBEizmWcoD2MZjvWq3IIvQ1xbEoLpQnTlmZzUiFNyNzqzDM_Kr26Gqy6VIroVlF_Z6b3zMWm2AEY7PHr5Q70HPlCcsqA04yKOpfGRET1vv09qlcEW66BLefYDV-5JKo_kXkw31lRIrZnb_Xzix_MI97LDhE-6wDjb6qlQlmJ3eVUWv_38MHVPFlTumPaA4n_x0sUDTmJxUmA";

	private String clusterId = "high";

	private String namespaceName = "yxli-tsf-test-namspace";

	private String namespaceId = "high-yxli-tsf-test-namspace";

	@Test
	//@Ignore
	public void createNamespaceTest() {
		Map<String, String> headers = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("name", namespaceName);
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/json");
		String result = namespaceManagerService.createNamespace(headers, clusterId, params);
		//System.out.println("------------------------------" + JSON.toJSONString(result));
		System.out.println("------------------------------" + result);
	}

    @Test
    public void getNamespacesTest() {
        Map<String, String> headers = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("offset", "40");
		params.put("limit", 3);
		headers.put("Authorization", authorization);
		String result = namespaceManagerService.getNamespaces(headers, params, clusterId);
        //System.out.println("------------------------------" + JSON.toJSONString(result));
		System.out.println("------------------------------" + result);
    }

	@Test
	public void getNamespaceInfoByIdTest() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		String result = namespaceManagerService.getNamespaceById(headers, clusterId, namespaceId);
		//System.out.println("------------------------------" + JSON.toJSONString(result));
		System.out.println("------------------------------" + result);
	}

	@Test
	public void deleteNamespaceTest() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		String result = namespaceManagerService.deleteNamespace(headers, clusterId, namespaceId);
		//System.out.println("------------------------------" + JSON.toJSONString(result));
		System.out.println("------------------------------" + result);
	}
}
