package com.tencent.tsf.container.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.tsf.container.dto.BaseResponse;
import com.tencent.tsf.container.dto.NamespaceDto;
import com.tencent.tsf.container.service.NamespaceManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/clusters")
@Api(value = "NamespacesController", description = "命名空间管理")
public class NamespacesController extends BaseController {

    @Autowired
    NamespaceManagerService namespaceManagerService;

    @PostMapping("/{clusterId}/namespaces")
    @ApiOperation(value = "创建命名空间", httpMethod = "POST",
            notes = "创建命名空间<br/>" +
                    "请求参数描述：" +
                    "<ul>" +
                    "<li>clusterId：集群ID</li>" +
                    "<li>name：命名空间名称（String），必填</li>" +
                    "</ul>" +
                    "返回参数描述：<br/>", response = BaseResponse.class)
    public BaseResponse createNamespace(@PathVariable("clusterId") String clusterId, @RequestBody Map<String, Object> requestMap, HttpServletRequest request){
        Map<String, String> headers = getCustomHeaders(request);
        log.debug("---- 创建命名空间开始, clusterId: {}", clusterId);
        String project = namespaceManagerService.getclusterProject(headers, clusterId);
        String projectId = JSON.parseObject(project).getString("id");
        requestMap.put("projectId", projectId);

        String data = namespaceManagerService.createNamespace(headers, clusterId, requestMap);
        JSONObject jsonObject = JSON.parseObject(data);
        log.debug("---- 创建命名空间完毕, result: {}", jsonObject);

        if (jsonObject.containsKey("error")){
            return createErrorResult(jsonObject.getInteger("code"), jsonObject.getString("message"));
        }
        String id = jsonObject.get("id").toString();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id",id );

        return createSuccessResult(resultMap);
    }

    @GetMapping("/{clusterId}/namespaces/{namespaceId}")
    @ApiOperation(value = "获取单个命名空间", httpMethod = "GET",
            notes = "获取单个命名空间<br/>" +
                    "请求参数描述：" +
                    "<ul>" +
                    "<li>clusterId：集群ID</li>" +
                    "<li>namespaceId：命名空间ID</li>" +
                    "</ul>" +
                    "返回参数描述：<br/>", response = BaseResponse.class)
    public BaseResponse getNamespace(@PathVariable("clusterId") String clusterId, @PathVariable("namespaceId") String namespaceId, HttpServletRequest request){
        Map<String, String> headers = getCustomHeaders(request);
        log.debug("---- 获取命名空间明细, clusterId: {}, namespaceId: {}", clusterId, namespaceId);
        String data = namespaceManagerService.getNamespaceById(headers, clusterId, namespaceId);
        JSONObject jsonObject = JSON.parseObject(data);
        if (jsonObject.containsKey("error")){
            return createErrorResult(jsonObject.getInteger("code"), jsonObject.getString("message"));
        }
        NamespaceDto namespaceDTO = new NamespaceDto();
        namespaceDTO.setId(jsonObject.get("id").toString());
        namespaceDTO.setName(jsonObject.get("name").toString());
        namespaceDTO.setStatus(jsonObject.get("status").toString());
        namespaceDTO.setCreatedAt(jsonObject.get("createdAt").toString());
        namespaceDTO.setUpdatedAt(jsonObject.get("createdAt").toString());
        log.info("---- 获取命名空间明细完成，result: {}", namespaceDTO);
        return createSuccessResult(namespaceDTO);
    }

    @GetMapping("/{clusterId}/namespaces")
    @ApiOperation(value = "获取命名空间列表", httpMethod = "GET",
            notes = "获取命名空间列表<br/>" +
                    "请求参数描述：" +
                    "<ul>" +
                    "<li>clusterId：集群ID</li>" +
                    "</ul>" +
                    "返回参数描述：<br/>", response = BaseResponse.class)
    public BaseResponse getNamespaces(@PathVariable("clusterId") String clusterId, HttpServletRequest request){
        log.info("-> 获取命名空间列表开始");
        Map<String, String> headers = getCustomHeaders(request);
        Map<String, Object> params = getRequestParams(request);
        String data = namespaceManagerService.getNamespaces(headers, params, clusterId);
        JSONObject jsonObject = JSON.parseObject(data);
        Map<String, Object> resultMap = new HashMap<>();

        if (jsonObject.containsKey("error")) {
            return createErrorResult(jsonObject.getInteger("code"), jsonObject.getString("message"));
        }

        if (null != jsonObject && jsonObject.containsKey("data") && jsonObject.get("data") != null){
            List<Map<String, Object>> dataList = (List) jsonObject.get("data");
            List<NamespaceDto> namespaceList = new ArrayList<>();
            dataList.forEach(map -> {
                NamespaceDto namespaceDTO = new NamespaceDto();
                namespaceDTO.setId(map.get("id").toString());
                namespaceDTO.setName(map.get("name").toString());
                namespaceDTO.setStatus(map.get("status").toString());
                namespaceDTO.setCreatedAt(map.get("createdAt").toString());
                namespaceDTO.setUpdatedAt(map.get("createdAt").toString());
                namespaceList.add(namespaceDTO);
            });

            resultMap.put("totalCount", jsonObject.get("totalCount"));
            resultMap.put("content", namespaceList);
            log.info("-> 获取命名空间列表完毕, resultMap: {}",resultMap);
        }else {
            resultMap.put("totalCount", 0);
            resultMap.put("content", null);
        }

        return createSuccessResult(resultMap);
    }

    @DeleteMapping("/{clusterId}/namespaces/{namespaceId}")
    @ApiOperation(value = "删除命名空间", httpMethod = "DELETE",
            notes = "删除命名空间<br/>" +
                    "请求参数描述：" +
                    "<ul>" +
                    "<li>clusterId：集群ID</li>" +
                    "<li>namespaceId：命名空间ID</li>" +
                    "</ul>" +
                    "返回参数描述：<br/>", response = BaseResponse.class)
    public BaseResponse deleteNamespace(@PathVariable("clusterId") String clusterId, @PathVariable("namespaceId") String namespaceId, HttpServletRequest request){
        Map<String, String> headers = getCustomHeaders(request);
        log.info("-> 删除命名空间列表开始");
        String data = namespaceManagerService.deleteNamespace(headers, clusterId, namespaceId);
        log.info("-> 删除命名空间列表完毕，result: {}", data);
        return handleHttpResult(data);
    }
}
