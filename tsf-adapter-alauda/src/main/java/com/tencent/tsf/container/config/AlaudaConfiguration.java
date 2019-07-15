/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @title AlaudaConfiguration
 * @title Ethan
 * @date 2019/3/29 11:04
 * @description TODO
 * @version Version 1.0
 */

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "container.platform.alauda")
public class AlaudaConfiguration {

	private String endpoint;
	private String accessKey;
	private String accessToken;

}
