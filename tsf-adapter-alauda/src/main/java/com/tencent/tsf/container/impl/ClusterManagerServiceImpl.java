/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.tsf.container.config.AlaudaServerPath;
import com.tencent.tsf.container.dto.*;
import com.tencent.tsf.container.models.*;
import com.tencent.tsf.container.service.ClusterManagerService;
import com.tencent.tsf.container.utils.HttpClientUtil;
import com.tencent.tsf.container.utils.ResultHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;



/**
 * @version Version 1.0
 * @title ClusterManagerServiceImpl
 * @title Ethan
 * @date 2019/3/29 13:20
 * @description TODO
 */

@Slf4j
@Service
public class ClusterManagerServiceImpl implements ClusterManagerService {

	@Autowired
	private AlaudaServerPath alaudaServerPath;

	@Override
	public String createCluster(Map<String, String> headers, String name) {
		Assert.hasLength(name, "集群名称不能为空！");

		JSONObject res = new JSONObject();
		res.put("id", name);
		return res.toJSONString();
	}


	@Override
	public String getClusterById(Map<String, String> headers, String clusterId) {
		Assert.hasLength(clusterId, "集群ID不能为空！");

		String url = alaudaServerPath.clusterInfoUrl(clusterId);
		String result = HttpClientUtil.doGet(url, headers);

		BaseResponse data = JSON.parseObject(result, BaseResponse.class);
		if (isSuccess(data)) {
			ClusterInfoDto clusterInfoDto = new ClusterInfoDto();
			JSONObject cluster = JSON.parseObject(result);
			// add base info
			getClusterBaseInfo(clusterInfoDto, cluster);
			//add pod infos
			getNodePodInfo(clusterId, headers, clusterInfoDto);

			return JSON.toJSONString(clusterInfoDto);

		}else {
			data.setError(data.getMessage());
			return JSON.toJSONString(data);
		}

	}

	@Override
	public String getClusters(final Map<String, String> headers, Map<String, Object> params) {
		String url = alaudaServerPath.getAllClustersUrl(params);
		String result = HttpClientUtil.doGet(url, headers);
		BaseResponse data = JSON.parseObject(result, BaseResponse.class);
		if (!isSuccess(data)) {
			log.error("getClusters error, data is {}", data);
			data.setError(data.getMessage());
			return JSON.toJSONString(data);
		}

		JSONObject resultObj = JSON.parseObject(result);
		JSONArray clusterList = resultObj.getJSONArray("items");
		Map<String, Object> res = new HashMap<>();

		if(null == clusterList){
			return ResultHandler.buildErrorReturn("clusters items is null");
		}

		List<String> idsArr = new ArrayList<>();

		if (params.containsKey("ids")){
			idsArr = Arrays.asList(((String) params.get("ids")).split(","));
		}
		if ( idsArr.size() > 0 ){
			JSONArray specClusterList = new JSONArray();
			List<String> finalIdsArr = idsArr;
			clusterList.forEach(it -> {
				String id = getClusterId((JSONObject) it);
				if (finalIdsArr.contains(id)) {
					specClusterList.add(it);
				}
			});
			if (specClusterList.size() > 0 ) {
				clusterList = specClusterList;
			}
		}

		JSONArray actualClusterList = new JSONArray();
		//handle offset and limit
		int count = clusterList.size();
		int offset = 0, limit = 0;

		if (params.containsKey("offset")){
			Object val = params.get("offset");
			if (val instanceof Integer) {
				offset = (Integer) val;
			}else if (val instanceof String) {
				offset = Integer.valueOf( (String) val );
			}
		}
		if (params.containsKey("limit")){
			Object val = params.get("limit");
			if (val instanceof Integer) {
				limit = (Integer) val;
			}else if (val instanceof String) {
				limit = Integer.valueOf( (String) val );
			}
		}
		if (offset >= count) {
			offset = 0;
		}

		if ( limit > 0 ) {
			if ( count > limit ) {
				int top = limit+offset;
				if ( top > count) {
					top = count;
				}
				for (int i=offset; i<top; i++){
					actualClusterList.add(clusterList.get(i));
				}
			}else {
				actualClusterList = clusterList;
			}
		} else {
			if ( offset > 0 ) {
				for (int i=offset; i<count; i++){
					actualClusterList.add(clusterList.get(i));
				}
			}else {
				actualClusterList = clusterList;
			}
		}

		List<ClusterInfoDto> list = new ArrayList<>();
		actualClusterList.forEach(it -> {
			ClusterInfoDto clusterInfoDto = new ClusterInfoDto();
			JSONObject cluster = (JSONObject) it;
			getClusterBaseInfo(clusterInfoDto, cluster);
			String id = clusterInfoDto.getName();
			getNodePodInfo(id, headers, clusterInfoDto);
			list.add(clusterInfoDto);
		});

		res.put("totalCount", count);
		res.put("data", list);
		return JSON.toJSONString(res);
	}

	private Boolean isSuccess(BaseResponse data) {
		if (null == data){
			return Boolean.FALSE;
		}
		if ("Status".equals(data.getKind()) && null != data.getCode() && data.getCode() >= 400) {
			//BaseResponse err = JSON.parseObject(result, BaseResponse.class);
			data.setError(data.getMessage());
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}


	private String getClusterId(JSONObject clusterInfo) {
		JSONObject metadata = clusterInfo.getJSONObject("metadata");
		return metadata.getString("name");
	}

	private void getClusterBaseInfo(ClusterInfoDto clusterInfoDto, JSONObject cluster) {
		JSONObject metadata = cluster.getJSONObject("metadata");
		//log.info("metadata:" + metadata);
		String createdAt = metadata.getString("creationTimestamp");
		//log.info("createAt:" + createdAt);
		String id = metadata.getString("name");
		String name = metadata.getString("name");
		JSONObject requested = cluster.getJSONObject("requested");
		String pods = "0";
		String status;
		if (requested != null) {
			pods = requested.getString("pods");
		}
		Integer runningPodNum = Integer.valueOf(pods);
		JSONObject metadata_status = cluster.getJSONObject("status");
		//log.info("metadata_status:" + metadata_status);
		JSONArray metadata_conditions = metadata_status.getJSONArray("conditions");
		//log.info("metadata_conditions:" + metadata_conditions);
		List status_list = new ArrayList();
		metadata_conditions.stream().forEach(it -> {
			JSONObject cond = (JSONObject) it;
			//log.info( "cond_it:" + cond.getString("status") );
			status_list.add(cond.getString("status"));
		});
		status = "Running";
		for(int i=0;i<status_list.size();i++) {
			//log.info("status_list:" + status_list.get(i));
			if ( ! status_list.get(i).equals("False") ){
				status = "Abnormal";
			}
		}
		JSONObject spec = cluster.getJSONObject("spec");
		JSONObject kubernetesApiEndpoints = spec.getJSONObject("kubernetesApiEndpoints");
		JSONArray serverEndpoints = kubernetesApiEndpoints.getJSONArray("serverEndpoints");
		JSONObject cidr = serverEndpoints.getJSONObject(0);
		String clientCIDR = cidr.getString("clientCIDR");

		clusterInfoDto.setCreatedAt(createdAt);
		clusterInfoDto.setId(id);
		clusterInfoDto.setName(name);
		clusterInfoDto.setRunningPodNum(runningPodNum);
		clusterInfoDto.setStatus(status);
		clusterInfoDto.setUpdatedAt(createdAt);
		clusterInfoDto.setCidr(clientCIDR);
	}

	private String getEncodeUrl(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		}
		return "";
	}

	private String getPrometheusValue(String response) {
		JSONObject obj = JSON.parseObject(response);
		JSONObject data_obj = obj.getJSONObject("data");
		JSONArray result_data_obj = data_obj.getJSONArray("result");
		JSONObject value_result_data_obj = result_data_obj.getJSONObject(0);
		JSONArray val_array = (JSONArray) value_result_data_obj.get("value");
		return val_array.getString(1);
	}



	private void getNodePodInfo(String clusterId, Map<String, String> headers, ClusterInfoDto clusterInfoDto) {
		String metricsBaseUrl = alaudaServerPath.clusterPrometheusUrl(clusterId);
		//String clusterPodUrl_base = alaudaServerPath.clusterPodUrl(clusterId);

		String metricNodeUrl = metricsBaseUrl + getEncodeUrl("count(kube_node_info)-count(kube_pod_info{pod=~\"kube-apiserver.*\"})");
		String metricPodUrl = metricsBaseUrl + getEncodeUrl("count(kube_pod_labels{label_platform=\"tsf\"})");
		String metricReadyNodeUrl = metricsBaseUrl + getEncodeUrl("count(kube_node_info)-count(kube_pod_info{pod=~\"kube-apiserver.*\"})-(count(kube_node_status_condition{condition!=\"Ready\", status=\"true\"} ==1) OR ON() vector(0))");

		Map<String, String> metricsReq = new HashMap<>();
		metricsReq.put("totalNodeNum", metricNodeUrl);
		metricsReq.put("runningPodNum", metricPodUrl);
		metricsReq.put("runningNodeNum", metricReadyNodeUrl);
		int totalNodeNum = 0, runningPodNum = 0, runningNodeNum = 0;
		for (String key : metricsReq.keySet()){
			try{
				String url = metricsReq.get(key);
				String metricRes = HttpClientUtil.doGet(url, headers);
				String objNum = getPrometheusValue(metricRes);
				switch (key) {
					case "totalNodeNum":
						totalNodeNum = Integer.valueOf(objNum);
						break;
					case "runningPodNum":
						runningPodNum = Integer.valueOf(objNum);
						break;
					case "runningNodeNum":
						runningNodeNum = Integer.valueOf(objNum);
						break;
				}
			} catch (Exception ex) {
				//todo
				log.error("getNodePodInfo error,", ex.toString());
			}
		}
		clusterInfoDto.setTotalNodeNum(totalNodeNum);
		clusterInfoDto.setRunningPodNum(runningPodNum);
		clusterInfoDto.setRunningNodeNum(runningNodeNum);
	}

	@Override
	public String deleteClusterById(Map<String, String> headers, String clusterId) {
		Assert.hasLength(clusterId, "集群ID不能为空！");

		String url = alaudaServerPath.deleteClusterUrl(clusterId);
		return HttpClientUtil.doDelete(url, headers);
	}

	@Override
	public String clusterUsage(Map<String, String> headers, String clusterId) {
		Assert.hasLength(clusterId, "集群ID不能为空！");
		String metricsBaseUrl = alaudaServerPath.clusterPrometheusUrl(clusterId);

		String cpuTotalUrl = metricsBaseUrl + getEncodeUrl("sum(machine_cpu_cores)");
		String cpuRequestUrl = metricsBaseUrl + getEncodeUrl("cluster_resource_requests_cpu_cores");
		String cpuLimitUrl = metricsBaseUrl + getEncodeUrl("sum(kube_pod_container_resource_limits_cpu_cores)");
		String memTotalUrl = metricsBaseUrl + getEncodeUrl("sum(node_memory_MemTotal)/1024/1024/1024");
		String memRequestUrl = metricsBaseUrl + getEncodeUrl("sum(kube_pod_container_resource_requests_memory_bytes)/1024/1024/1024");
		String memLimitUrl = metricsBaseUrl + getEncodeUrl("sum(kube_pod_container_resource_limits_memory_bytes)/1024/1024/1024");

		Map<String, String> metricsReq = new HashMap<>();
		metricsReq.put("cpuTotal", cpuTotalUrl);
		metricsReq.put("cpuRequest", cpuRequestUrl);
		metricsReq.put("cpuLimit", cpuLimitUrl);
		metricsReq.put("memTotal", memTotalUrl);
		metricsReq.put("memRequest", memRequestUrl);
		metricsReq.put("memLimit", memLimitUrl);

		String cpuTotal = "", cpuRequest= "", cpuLimit="", memTotal="", memRequest="", memLimit="";
		for (String key : metricsReq.keySet()){
			try{
				String url = metricsReq.get(key);
				String metricRes = HttpClientUtil.doGet(url, headers);
				String objNum = getPrometheusValue(metricRes);
				switch (key) {
					case "cpuTotal":
						cpuTotal = objNum;
						break;
					case "cpuRequest":
						cpuRequest = objNum;
						break;
					case "cpuLimit":
						cpuLimit = objNum;
						break;
					case "memTotal":
						memTotal = objNum;
						break;
					case "memRequest":
						memRequest = objNum;
						break;
					case "memLimit":
						memLimit = objNum;
						break;
				}
			} catch (Exception ex) {
				//todo
				log.error("getNodePodInfo error,", ex.toString());
			}
		}
		metricsReq.put("cpuTotal",decimalFormatMetric(cpuTotal));
		metricsReq.put("cpuRequest", decimalFormatMetric(cpuRequest));
		metricsReq.put("cpuLimit", decimalFormatMetric(cpuLimit));
		metricsReq.put("memTotal", decimalFormatMetric(memTotal));
		metricsReq.put("memRequest", decimalFormatMetric(memRequest));
		metricsReq.put("memLimit", decimalFormatMetric(memLimit));

		return JSON.toJSONString(metricsReq);
	}

	@Override
	public void setMasterNode(Map<String, String> headers, List<ClusterVMDto> masterNodes) {
		//todo
	}

	@Override
	public String clusterNodes(Map<String, String> headers, String clusterId, Map<String, Object> params) {
		Assert.hasLength(clusterId, "获取集群节点列表：集群ID不能为空！");
		String clusterNodeUrl = alaudaServerPath.clusterNodeUrl(clusterId);
		String clusterNodesRes = HttpClientUtil.doGet(clusterNodeUrl, headers);

		Map<String, Object> clusterNodesInfo = new HashMap<>();

		BaseResponse clusterNodesResData = JSON.parseObject(clusterNodesRes, BaseResponse.class);
		if ("Status".equals(clusterNodesResData.getKind()) && null != clusterNodesResData.getCode() && clusterNodesResData.getCode() >= 400) {
			//BaseResponse err = JSON.parseObject(result, BaseResponse.class);
			clusterNodesResData.setError(clusterNodesResData.getMessage());
			return JSON.toJSONString(clusterNodesResData);
		}
		List<ItemData> nodesData = clusterNodesResData.getItems();
		if (nodesData == null) {
			String msg = "cluster nodes from items is null";
			log.info("集群节点列表是null，集群ID：{}", clusterId);
			return ResultHandler.buildErrorReturn(msg);
		}
		int totalCount = nodesData.size();
		clusterNodesInfo.put("totalCount", totalCount);
		if (totalCount == 0 ) {
			return JSON.toJSONString(clusterNodesInfo);
		}
		List<ClusterNodeDto> list = new ArrayList<>();
		Map<String,Map<String,String>> nodeMetricDataMap = this.getNodeCpuMemByClusterId(headers, clusterId);

		int offset = 0;
		int limit = 0;
		if (params.containsKey("offset")){
			Object val = params.get("offset");
			if (val instanceof Integer) {
				offset = (Integer) val;
			}else if (val instanceof String) {
				offset = Integer.valueOf( (String) val );
			}
		}
		if (params.containsKey("limit")){
			Object val = params.get("limit");
			if (val instanceof Integer) {
				limit = (Integer) val;
			}else if (val instanceof String) {
				limit = Integer.valueOf( (String) val );
			}
		}
		List<ItemData> specNodesData  = new ArrayList<>();
		if (offset >= totalCount) {
			offset = 0;
		}

		if ( limit > 0 ) {
			if ( totalCount > limit ) {
				int top = limit+offset;
				if ( top > totalCount) {
					top = totalCount;
				}
				for (int i=offset; i<top; i++){
					specNodesData.add(nodesData.get(i));
				}
			}else {
				specNodesData = nodesData;
			}
		} else {
			if ( offset > 0 ) {
				for (int i=offset; i<totalCount; i++){
					specNodesData.add(nodesData.get(i));
				}
			}else {
				specNodesData = nodesData;
			}
		}


		for (ItemData it : specNodesData) {

			ClusterNodeDto info = new ClusterNodeDto();

			Map<String, Object> statusObj = it.getStatus();
			Map<String, Object> metadataObj = it.getMetadata();
			if (null == statusObj || null == metadataObj) {
				continue;
			}
			List<Map<String, Object>> addressList = (List<Map<String, Object>>)statusObj.get("addresses");
			// get lanIp
			String lanIp = getInternalIp(addressList);
			if(null == lanIp || "".equals(lanIp)){
				continue;
			}
			info.setLanIp(lanIp);
			//get uid
			String uid = (String) metadataObj.get("uid");
			if(null == uid || "".equals(uid)){
				continue;
			}
			info.setId(uid);
			info.setCreatedAt(metadataObj.get("creationTimestamp").toString());
			info.setUpdatedAt(metadataObj.get("creationTimestamp").toString());
			//get conditions
			List<Map<String,String>> conditions = (List<Map<String,String>>)statusObj.get("conditions");
			String initStatus = "Abnormal";
			info.setStatus(initStatus);
			if(null == conditions || conditions.size() == 0){
				continue;
			}
			info.setConditions(conditions);
			for (Map<String,String> condition: conditions){
				String type = condition.get("type");
				if ("Ready".equals(type)) {
					if("True".equals(condition.get("status"))){
						info.setStatus("Running");
					}
				}
			}

			Map<String, String> labels = (Map<String, String>)metadataObj.get("labels");
			info.setLabels(labels);

			info.setCpuTotal(getNodeMetricVal(nodeMetricDataMap, "cpuTotal", lanIp));
			info.setCpuRequest(getNodeMetricVal(nodeMetricDataMap, "cpuRequest", lanIp));
			info.setCpuLimit(getNodeMetricVal(nodeMetricDataMap, "cpuLimit", lanIp));
			info.setMemTotal(getNodeMetricVal(nodeMetricDataMap, "memTotal", lanIp));
			info.setMemRequest(getNodeMetricVal(nodeMetricDataMap, "memRequest", lanIp));
			info.setMemLimit(getNodeMetricVal(nodeMetricDataMap, "memLimit", lanIp));
			log.debug("node info", info);
			list.add(info);
		}
		clusterNodesInfo.put("data", list);
		//return list;
		return JSON.toJSONString(clusterNodesInfo);
	}

	private String getInternalIp(List<Map<String,Object>> addressList) {
		for(Map<String,Object> addressMap : addressList){
			if ("InternalIP".equals(addressMap.get("type"))){
				return (String) addressMap.get("address");
			}
		}
		return "";
	}

	private Map<String,Map<String,String>> getNodeCpuMemByClusterId(Map<String, String> headers, String clusterId) {
		String metricsBaseUrl = alaudaServerPath.clusterPrometheusUrl(clusterId);

		String cpuTotalUrl = metricsBaseUrl + getEncodeUrl("label_replace(machine_cpu_cores, \"ip\", \"$1\", \"instance\", \"(.*):.*\")");
		String cpuRequestUrl = metricsBaseUrl + getEncodeUrl("(sum by(node) (kube_pod_container_resource_requests_cpu_cores{node!=\"\"}))  * on (node) group_left(ip) label_replace(label_replace(node_uname_info, \"node\", \"$1\", \"nodename\", \"(.*)\") , \"ip\", \"$1\", \"instance\", \"(.*):.*\")");
		String cpuLimitUrl = metricsBaseUrl + getEncodeUrl("sum by(node) (kube_pod_container_resource_limits_cpu_cores{node!=\"\"}) * on (node) group_left(ip) label_replace(label_replace(node_uname_info, \"node\", \"$1\", \"nodename\", \"(.*)\") , \"ip\", \"$1\", \"instance\", \"(.*):.*\")");

		String memTotalUrl = metricsBaseUrl + getEncodeUrl("label_replace(machine_memory_bytes/1024/1024/1024, \"ip\", \"$1\", \"instance\", \"(.*):.*\")");
		String memRequestUrl = metricsBaseUrl + getEncodeUrl("sum by(node) (kube_pod_container_resource_requests_memory_bytes{node!=\"\"}/1024/1024/1024) * on (node) group_left(ip) label_replace(label_replace(node_uname_info, \"node\", \"$1\", \"nodename\", \"(.*)\") , \"ip\", \"$1\", \"instance\", \"(.*):.*\")");
		String memLimitUrl = metricsBaseUrl + getEncodeUrl("sum by(node) (kube_pod_container_resource_limits_memory_bytes{node!=\"\"}/1024/1024/1024) * on (node) group_left(ip) label_replace(label_replace(node_uname_info, \"node\", \"$1\", \"nodename\", \"(.*)\") , \"ip\", \"$1\", \"instance\", \"(.*):.*\")");


		Map<String, String> metricsReq = new HashMap<>();
		metricsReq.put("cpuTotal", cpuTotalUrl);
		metricsReq.put("cpuRequest", cpuRequestUrl);
		metricsReq.put("cpuLimit", cpuLimitUrl);
		metricsReq.put("memTotal", memTotalUrl);
		metricsReq.put("memRequest", memRequestUrl);
		metricsReq.put("memLimit", memLimitUrl);

		Map<String,Map<String,String>> resultData = new HashMap<>();
		for (String key : metricsReq.keySet()){
			try{
				String url = metricsReq.get(key);
				String metricRes = HttpClientUtil.doGet(url, headers);


				Map<String,String> metricData = getMetricNodeMapData(metricRes);
				resultData.put(key,metricData);

				//getNodeMetricValue(NodeRes, metricRes, key);

			} catch (Exception ex) {
				//todo
				log.error("getNodeCpuMemById error,", ex.toString());
			}
		}
		return resultData;

	}

	private Map<String,String> getMetricNodeMapData(String metricResponse){
		Map<String,String> metricNodeMapData = new HashMap<>();
		JSONObject metricResponseObj = JSON.parseObject(metricResponse);
		if (null == metricResponseObj){
			return metricNodeMapData;
		}
		if (metricResponseObj.containsKey("data") && metricResponseObj.containsKey("status") && "success".equals(metricResponseObj.get("status"))){
			JSONArray result = metricResponseObj.getJSONObject("data").getJSONArray("result");

			result.forEach(it -> {
				JSONObject data = (JSONObject) it;
				metricNodeMapData.put(data.getJSONObject("metric").getString("ip"), decimalFormatMetric(data.getJSONArray("value").get(1).toString()));
			});
		}
		return metricNodeMapData;
	}

	private Float getNodeMetricVal(Map<String,Map<String,String>> nodeMetricDataMap, String metricType, String lanIp) {
		if (!nodeMetricDataMap.containsKey(metricType)){
			return (float) 0;
		}
		Map<String,String> specMetricData = nodeMetricDataMap.get(metricType);
		if (null == specMetricData || !specMetricData.containsKey(lanIp)){
			return (float) 0;
		}
		return Float.parseFloat(specMetricData.get(lanIp));
	}

	private String decimalFormatMetric(String metricVal){
		if (StringUtils.EMPTY.equals(metricVal)){
			return metricVal;
		}
		Float val = Float.parseFloat(metricVal);
		DecimalFormat decimalFormat=new DecimalFormat(".00"); //构造方法的字符格式这里如果小数不足2位,会以0补足.
		return decimalFormat.format(val);
	}

	@Override
	public void addNodes(Map<String, String> headers, List<ClusterVMDto> nodes) {
		//todo
	}

	@Override
	public void removeNodes(Map<String, String> headers, String clusterId, List<String> ipList) {
		Assert.hasLength(clusterId, "获取集群节点列表：集群ID不能为空！");
	}

	@Override
	public String getKubernetesAPIServer(Map<String, String> headers, String clusterId) {
		Assert.hasLength(clusterId, "kube-apiserver: 集群ID不能为空！");
		String url = alaudaServerPath.clusterInfoUrl(clusterId);
		String result = HttpClientUtil.doGet(url, headers);

		BaseResponse clusterInfo = JSON.parseObject(result, BaseResponse.class);
		if (!isSuccess(clusterInfo)) {
			clusterInfo.setError(clusterInfo.getMessage());
			return JSON.toJSONString(clusterInfo);
		}
		String kubernetesApiEndpointsKey = "kubernetesApiEndpoints";
		String serverEndpointsKey = "serverEndpoints";
		Map<String, Object> spec = (Map<String, Object>)clusterInfo.getSpec();
		if (null == spec || !spec.containsKey(kubernetesApiEndpointsKey)){
			String msg;
			if (null == spec){
				msg = String.format("spec is null, clusterInfo: {}", clusterInfo.toString());
			}else{
				msg = String.format("spec:%s not contains key %s",spec.toString(), kubernetesApiEndpointsKey);
			}
			log.debug(msg);
			return ResultHandler.buildErrorReturn(msg);
		}
		Map<String, Object> kubernetesApiEndpoints = (Map<String, Object>)spec.get(kubernetesApiEndpointsKey);

		if (null == kubernetesApiEndpoints || !kubernetesApiEndpoints.containsKey(serverEndpointsKey)){
			String msg;
			if (null == kubernetesApiEndpoints){
				msg = String.format("kubernetesApiEndpoints is null, spec is %s", spec.toString());
			}else{
				msg = String.format("spec:%s not contains key %s",kubernetesApiEndpoints.toString(), serverEndpointsKey);
			}
			log.debug(msg);
			return ResultHandler.buildErrorReturn(msg);
		}
		List<Map<String, Object>> serverEndpoints = (List<Map<String, Object>>)kubernetesApiEndpoints.get(serverEndpointsKey);
		if (null == serverEndpoints || serverEndpoints.size() == 0){
			String msg;
			if (null == serverEndpoints){
				msg = String.format("serverEndpoints is null, kubernetesApiEndpoints is %s", kubernetesApiEndpoints.toString());
			}else{
				msg = "serverEndpoints size is 0";
			}
			log.debug(msg);
			return ResultHandler.buildErrorReturn(msg);
		}
		String address = (String) serverEndpoints.get(0).get("serverAddress");
		Map<String, Object> resultMap = new HashMap<>();
		if (!address.endsWith("/")){
			address = address + "/";
		}
		resultMap.put("address", address);

		//authorization
		JSONObject authorization = new JSONObject();
		authorization.put("type", "Bearer");
		String token = headers.get("Authorization").replace("Bearer ", "");
		resultMap.put("authorization", authorization);
		authorization.put("credentials", token);
		return JSON.toJSONString(resultMap);
	}


}
