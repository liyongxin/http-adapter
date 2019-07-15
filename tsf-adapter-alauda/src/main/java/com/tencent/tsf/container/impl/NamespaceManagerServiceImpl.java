package com.tencent.tsf.container.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.tsf.container.config.AlaudaServerPath;
import com.tencent.tsf.container.models.BaseResponse;
import com.tencent.tsf.container.models.ItemData;
import com.tencent.tsf.container.service.NamespaceManagerService;
import com.tencent.tsf.container.utils.HttpClientUtil;
import com.tencent.tsf.container.utils.ResultHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NamespaceManagerServiceImpl implements NamespaceManagerService {

    @Autowired
    AlaudaServerPath alaudaServerPath;

    @Override
    public String createNamespace(Map<String, String> headers, String clusterId, Map<String, Object> params) {
        Assert.hasLength(clusterId, "集群ID不能为空！");

        String url = alaudaServerPath.createNamespaceUrl(clusterId);
        String name = (String) params.get("name");
        Assert.hasLength(name, "集群namespace名称不能为空！");
        Map<String, Object> transParams = new HashMap<>();
        transParams.put("metadata", params);
        String postRes = HttpClientUtil.doPost(url, headers, JSON.toJSONString(transParams));
        return resultHandler(postRes, clusterId);
    }

    @Override
    public String getNamespaceById(Map<String, String> headers, String clusterId, String namespaceId) {
        Assert.hasLength(clusterId, "集群ID不能为空！");
        Assert.hasLength(namespaceId, "命名空间ID不能为空！");
        String namespaceName = getNamespaceNameById(namespaceId, clusterId);
        String url = alaudaServerPath.namespaceInfoUrl(clusterId, namespaceName);
        String result = HttpClientUtil.doGet(url, headers);
        return resultHandler(result, clusterId);
    }

    @Override
    public String getNamespaces(Map<String, String> headers, Map<String, Object> params, String clusterId) {
        Assert.hasLength(clusterId, "集群ID不能为空！");

        String url = alaudaServerPath.getAllNamespacesUrl(clusterId, params);
        String result = HttpClientUtil.doGet(url, headers);
        return convertToNameSpaceDtoString(result, clusterId, params);
    }

    @Override
    public String deleteNamespace(Map<String, String> headers, String clusterId, String namespaceId) {
        Assert.hasLength(clusterId, "集群ID不能为空！");
        Assert.hasLength(namespaceId, "命名空间ID不能为空！");
        String nameSpaceName = getNamespaceNameById(namespaceId, clusterId);
        String url = alaudaServerPath.deleteNamespaceUrl(clusterId, nameSpaceName);
        String delRes = HttpClientUtil.doDelete(url, headers);
        return resultHandler(delRes, clusterId);
    }

    private String convertToNameSpaceDtoString(String result, String clusterId, Map<String, Object> params) {

        BaseResponse nsListInfo = JSON.parseObject(result, BaseResponse.class);
        if (nsListInfo == null) {
            return ResultHandler.buildErrorReturn("get namespaces value is null");
        }
        if ("Status".equals(nsListInfo.getKind()) && null != nsListInfo.getCode() && nsListInfo.getCode() >= 400) {
            nsListInfo.setError(nsListInfo.getMessage());
            return JSON.toJSONString(nsListInfo);
        }else {
            List<ItemData> namespaces  = nsListInfo.getItems();
            if (null == namespaces ){
                return ResultHandler.buildErrorReturn("namespaces is null");
            }
            //handle offset and limit
            int count = namespaces.size();
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
            List<ItemData> specNamespaces  = new ArrayList<>();
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
                        specNamespaces.add(namespaces.get(i));
                    }
                }else {
                    specNamespaces = namespaces;
                }
            } else {
                if ( offset > 0 ) {
                    for (int i=offset; i<count; i++){
                        specNamespaces.add(namespaces.get(i));
                    }
                }else {
                    specNamespaces = namespaces;
                }
            }

            JSONObject namespaceData = new JSONObject();
            List<JSONObject> namespaceList = new ArrayList<>();
            specNamespaces.forEach(ns -> {
                JSONObject namespace = itemDataTransferBase(ns, clusterId);
                namespaceList.add(namespace);
            });
            namespaceData.put("data", namespaceList);
            namespaceData.put("totalCount", count);
            return namespaceData.toJSONString();
        }

    }

    private JSONObject itemDataTransferBase(ItemData item, String clusterId){

        JSONObject res = new JSONObject();

        Map<String, Object> metaData = (Map<String, Object>)item.getMetadata();
        Map<String, Object> status = (Map<String, Object>)item.getStatus();

        res.put("id", clusterId + "-" + metaData.get("name"));
        res.put("name", metaData.get("name"));
        res.put("createdAt", metaData.get("creationTimestamp"));
        res.put("updatedAt", metaData.get("creationTimestamp"));
        res.put("status", status.get("phase"));
        return res;
    }

    private String getNamespaceNameById(String namespaceId, String clusterId) {
        Assert.hasLength(clusterId, "集群ID不能为空！");
        Assert.hasLength(namespaceId, "命名空间ID不能为空！");
        return namespaceId.replace(clusterId + "-", "");
    }

    private String getNamespaceIdByName(String namespaceName, String clusterId) {
        Assert.hasLength(clusterId, "集群ID不能为空！");
        Assert.hasLength(namespaceName, "命名空间ID不能为空！");
        return clusterId + "-" + namespaceName;
    }


    private String resultHandler(String httpRes, String clusterId) {
        // check response decide whether req is success
        BaseResponse data = JSON.parseObject(httpRes, BaseResponse.class);
        if ("Status".equals(data.getKind()) && null != data.getCode() && data.getCode() >= 400) {
            data.setError(data.getMessage());
            return JSON.toJSONString(data);
        }
        ItemData namespace = JSON.parseObject(httpRes, ItemData.class);
        JSONObject res = new JSONObject();

        Map<String, Object> metaData = (Map<String, Object>)namespace.getMetadata();
        if (null == metaData ) {
            ResultHandler.buildErrorReturn("Resource metadata is null");
        }
        res.put("id", getNamespaceIdByName((String) metaData.get("name"),clusterId));
        res.put("name", metaData.get("name"));
        if (metaData.containsKey("creationTimestamp")) {
            res.put("createdAt", metaData.get("creationTimestamp"));
            res.put("updatedAt", metaData.get("creationTimestamp"));
        }
        try {
            Map<String, Object> statusObj = (Map<String, Object>)namespace.getStatus();
            res.put("status", (String)statusObj.get("phase"));
        }catch (Exception e) {
            //todo
        }
        return JSON.toJSONString(res);
    }


    @Override
    public String getclusterProject(Map<String, String> headers, String clusterId) {
        Assert.hasLength(clusterId, "集群ID不能为空！");

        JSONObject res = new JSONObject();
        res.put("id", "ignore");

        return JSON.toJSONString(res);
    }

}
