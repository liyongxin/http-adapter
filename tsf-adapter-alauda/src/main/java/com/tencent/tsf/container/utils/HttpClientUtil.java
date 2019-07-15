/*
 * Copyright (c) 2018-2019. All rights reserved.
 * 注意：本内容仅限于项目内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.tencent.tsf.container.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @title HttpClientUtil
 * @title Ethan
 * @date 2019/3/29 11:41
 * @description TODO
 * @version Version 1.0
 */
@Slf4j
public class HttpClientUtil {
	// 使用CA的常量寄存器
	private static SSLConnectionSocketFactory SSLSF_CA = null;
	private static PoolingHttpClientConnectionManager CM_CA = null;
	private static SSLContextBuilder BUILDER_CA = null;
	private static Registry<ConnectionSocketFactory> REGISTRY_CA = null;

	// 不使用CA的常量寄存器
	private static SSLConnectionSocketFactory SSLSF = null;
	private static PoolingHttpClientConnectionManager CM = null;
	private static SSLContextBuilder BUILDER = null;
	private static Registry<ConnectionSocketFactory> REGISTRY = null;

	private static final String HTTP = "http";
	private static final String HTTPS = "https";

	public static String doPost(String url, Map<String, String> headers, Map<String, Object> param) {
		try (CloseableHttpClient httpClient = getHttpsClient(false)) {
			HttpPost httpPost = new HttpPost(url);
			//设置请求参数
			setRequestParams(httpPost, param);
			//设置请求headers
			setRequestHeaders(httpPost, headers);

			HttpResponse response = httpClient.execute(httpPost);
			return responseToStringEntity(response);
		} catch (Exception ex) {
			log.error("", ex);
		}
		return StringUtils.EMPTY;
	}

	private static String responseToStringEntity(HttpResponse response) throws IOException{
		if (response != null) {
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				return EntityUtils.toString(resEntity, Consts.UTF_8.name());
			}
		}
		return StringUtils.EMPTY;
	}

	//请求设置header
	private static void setRequestHeaders(HttpRequestBase httpRequest, Map<String, String> headers) {
		if (!CollectionUtils.isEmpty(headers)) {
			Iterator iteratorSet = headers.entrySet().iterator();
			while (iteratorSet.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) iteratorSet.next();
				httpRequest.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}

	//post请求设置参数
	private static void setRequestParams(HttpPost httpPost, Map<String, Object> param)
			throws UnsupportedEncodingException {
		if(CollectionUtils.isEmpty(param)) return;

		List<NameValuePair> list = new ArrayList<>();
		Iterator iterator = param.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			if(elem.getValue() != null)
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue().toString()));
		}
		if (list.size() > 0) {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, Consts.UTF_8.name());
			//UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list);
			httpPost.setEntity(entity);
		}
	}

	public static String doPost(String url, Map<String, String> headers, String param) {
		try (CloseableHttpClient httpClient = getHttpsClient(false)) {
			HttpPost httpPost = new HttpPost(url);
			//设置请求headers
			setRequestHeaders(httpPost, headers);
			StringEntity entity = new StringEntity(param, Consts.UTF_8.name());
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			return responseToStringEntity(response);
		} catch (Exception ex) {
			log.error("", ex);
		}
		return StringUtils.EMPTY;
	}

	public static String doPost(String url, Map<String, String> headers) {
		return doPost(url, headers, Collections.EMPTY_MAP);
	}


	public static String doGet(String url, Map<String, String> headers) {
		try (CloseableHttpClient httpClient = getHttpsClient(false)) {
			HttpGet httpGet = new HttpGet(url);
			setRequestHeaders(httpGet, headers);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			return responseToStringEntity(response);
		} catch (IOException ioEx) {
			log.error("", ioEx);
		} catch (Exception ex) {
			log.error("", ex);
		}
		return null;
	}

	public static String doDelete(String url, Map<String, String> headers) {
		try (CloseableHttpClient httpClient = getHttpsClient(false)) {
			HttpDelete httpDelete = new HttpDelete(url);
			//设置请求headers
			setRequestHeaders(httpDelete, headers);
			HttpResponse response = httpClient.execute(httpDelete);
			return responseToStringEntity(response);
		} catch (IOException ioEx) {
			log.error("", ioEx);
		} catch (Exception ex) {
			log.error("", ex);
		}
		return StringUtils.EMPTY;
	}


	public static void main (String[] args) {
		String url = "https://43.254.44.13/v3/clusters/%1$s/clusterregistrationtokens";
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Basic dG9rZW4tcGxqd2Y6cms0czRoNG1xMjhkdmNkY2p0NWs3ODh3d2IyNXh3dHZ2endrOHZ3a3R6bDU2N2g5OW41d2Y5");
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		String clusterId = "c-4v4mw";
		Map<String, Object> params = new HashMap<>();
		params.put("clusterId", clusterId);
		url = String.format(url, clusterId);

		try (CloseableHttpClient httpClient = getHttpsClient(false)) {
			HttpPost httpPost = new HttpPost(url);
			//设置请求headers
			setRequestHeaders(httpPost, headers);
			setRequestParams(httpPost, params);

			HttpResponse response = httpClient.execute(httpPost);

			if (response != null) {
				HttpEntity resEntity = response.getEntity();

				Header[] responseHeaders = response.getAllHeaders();
				log.info("----------------------------------------------------\nall headers info: ");
				Arrays.stream(responseHeaders)
						.forEach(it -> log.info("---- {}: {}", it.getName(), it.getValue()));
				log.info("----------------------------------------------------");
				if (resEntity != null) {
					String result = EntityUtils.toString(resEntity, Consts.UTF_8.name());
					log.info("----------------------------------------------------\nresponse: {}", result);
				}
			}
		} catch (Exception ex) {
			log.error("", ex);
		}
		log.info("----------------------------------------------------");
	}

	/**
	 * 获取一个 HttpClient，可以选择是否信任全部 CA
	 *
	 * @param withCA 是否信任全部 CA
	 * @return HttpClient
	 */
	public static CloseableHttpClient getHttpsClient(Boolean withCA) {
		return getHttpsClientBuilder(withCA).build();
	}

	/**
	 * 获取一个 HttpClientBuilder，可以选择是否信任全部 CA
	 *
	 * @param withCA 是否信任全部 CA
	 * @return HttpClientBuilder
	 */
	public static HttpClientBuilder getHttpsClientBuilder(Boolean withCA) {
		try {
			if (withCA) {
				// 使用CA
				if (null == BUILDER_CA) {
					BUILDER_CA = new SSLContextBuilder();
				}
				if (null == SSLSF_CA) {
					SSLSF_CA = new SSLConnectionSocketFactory(BUILDER_CA.build(), new String[] { "TLSv1", "TLSv1.2" }, null, NoopHostnameVerifier.INSTANCE);
				}
				if (null == REGISTRY_CA) {
					REGISTRY_CA = RegistryBuilder.<ConnectionSocketFactory>create().register(HTTP, new PlainConnectionSocketFactory())
							.register(HTTPS, SSLSF_CA).build();
				}
				if (null == CM_CA) {
					CM_CA = new PoolingHttpClientConnectionManager(REGISTRY_CA);
					CM_CA.setMaxTotal(100);
					CM_CA.setDefaultMaxPerRoute(10);
				}
				return HttpClients.custom().setSSLSocketFactory(SSLSF_CA).setConnectionManager(CM_CA).setConnectionManagerShared(true);
			} else {
				// 不使用CA
				if (null == BUILDER) {
					BUILDER = new SSLContextBuilder();
					// 全部信任 不做身份鉴定
					BUILDER.loadTrustMaterial(null, new TrustStrategy() {
						@Override
						public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
							return true;
						}
					});
				}
				if (null == SSLSF) {
					SSLSF = new SSLConnectionSocketFactory(BUILDER.build(), new String[] { "TLSv1", "TLSv1.2" }, null, NoopHostnameVerifier.INSTANCE);
				}
				if (null == REGISTRY) {
					REGISTRY = RegistryBuilder.<ConnectionSocketFactory>create().register(HTTP, new PlainConnectionSocketFactory())
							.register(HTTPS, SSLSF).build();
				}
				if (null == CM) {
					CM = new PoolingHttpClientConnectionManager(REGISTRY);
					CM.setMaxTotal(100);
					CM.setDefaultMaxPerRoute(10);
				}
				return HttpClients.custom().setSSLSocketFactory(SSLSF).setConnectionManager(CM).setConnectionManagerShared(true);
			}
		} catch (Exception e) {
			log.error("[TSF Common] Error on create HttpClient.", e);
		}
		return null;
	}
}
