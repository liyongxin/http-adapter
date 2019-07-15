/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container.utils;

import com.alibaba.fastjson.JSON;
import com.tencent.tsf.container.models.BaseResponse;
import lombok.Data;

/**
 * @author Yongxin Li
 * @version Version 1.0
 * @title ResultHandler
 * @date 2019/7/10 16:47
 * @description TODO
 */
@Data
public class ResultHandler {

	public static String buildErrorReturn(String errMsg) {
		BaseResponse res = new BaseResponse();
		res.setError(errMsg);
		res.setCode(500);
		res.setMessage(errMsg);
		return JSON.toJSONString(res);
	}

}
