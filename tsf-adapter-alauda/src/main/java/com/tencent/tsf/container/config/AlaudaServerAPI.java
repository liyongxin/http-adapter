/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container.config;

/**
 * @title AlaudaServerAPI
 * @title Ethan
 * @date 2019/3/29 18:23
 * @description TODO
 * @version Version 1.0
 */

public class AlaudaServerAPI {

	/** 创建集群 **/
	public static String ALAUDA_CREATE_CLUSTER         = "/apis/clusterregistry.k8s.io/v1alpha1/namespaces/alauda-system/clusters";
	/** 获取单个集群信息 **/
	public static String ALAUDA_GET_THE_CLUSTER_INFO   = "/apis/clusterregistry.k8s.io/v1alpha1/namespaces/alauda-system/clusters/%s";
	/** 获取集群列表 **/
	public static String ALAUDA_GET_ALL_CLUSTERS       = "/apis/clusterregistry.k8s.io/v1alpha1/namespaces/alauda-system/clusters";
	/** 删除集群 **/
	public static String ALAUDA_DELETE_CLUSTER         = "/clusters/%1$s";
	/** 创建命名空间 **/
	public static String ALAUDA_CREATE_NAMESPACE       = "/kubernetes/%1$s/api/v1/namespaces";
	/** 获取单个命名空间 **/
	public static String ALAUDA_GET_NAMESPACE_INFO     = "/kubernetes/%1$s/api/v1/namespaces/%2$s";
	/** 获取某个集群下的所有命名空间 **/
	public static String ALAUDA_GET_ALL_NAMESPACES     = "/kubernetes/%1$s/api/v1/namespaces";
	/** 获取添加节点的命令 **/
	public static String ALAUDA_CLUSTER_REGISTRATION_TOKENS = "/clusters/%1$s/clusterregistrationtokens";
	/** 获取集群节点信息 **/
	public static String ALAUDA_CLUSTER_NODES          = "/kubernetes/%s/api/v1/nodes";
	/** 获取pod信息 **/
	public static String ALAUDA_CLUSTER_PODS           = "/v1/metrics/%s/prometheus/query?query=";
	/** 删除命名空间 **/
	public static String ALAUDA_DELETE_NAMESPACE       = "/kubernetes/%1$s/api/v1/namespaces/%2$s";
	/** 删除节点 **/
	public static String ALAUDA_DELETE_NODE            = "/nodes/%1$s";
	/** 获取 k8s API Server **/
	public static String ALAUDA_KUBE_CONFIG            = "/clusters/%1$s?action=generateKubeconfig";
	/** 获取集群projects **/
	public static String ALAUDA_CLUSTER_PROJECTS       = "/clusters/%1$s/projects";

	public static String ALAUDA_CLUSTER_NODES_INFO          = "/kubernetes/%s/api/v1/nodes";

	public static String ALAUDA_CLUSTER_PROMETHEUS          = "/v1/metrics/%s/prometheus/query?query=";

}
