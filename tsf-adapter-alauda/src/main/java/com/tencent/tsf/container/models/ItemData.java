/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container.models;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @title ClusterInfo
 * @title YongXin Li
 * @date 2019/7/4 20:25
 * @description TODO
 * @version Version 1.0
 */
@Data
public class ItemData {

	private Map<String, Object>     metadata;
	private Map<String, Object>     status;
	private Map<String, Object>     spec;
}
