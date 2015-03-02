package com.eyuanku.web.framework.svg.handler;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.eyuanku.web.framework.exce.GlobalException;

/**
 * Http请求图片
 * 
 * @author lvxd
 * 
 */

public class ImageHttpReqHandler {
	
	public static String requestMail(String mailTo,String userAccount,String requestUrl) {
		NameValuePair[] params = new NameValuePair[]{
				new NameValuePair("mailTo", mailTo),
				new NameValuePair("userAccount", userAccount)};
		return httpPostRequest(requestUrl,params);
	}
	
	public static String requestForgetMail(String mailTo,String resetPwdUrl,String requestUrl) {
		NameValuePair[] params = new NameValuePair[]{
				new NameValuePair("mailTo", mailTo),
				new NameValuePair("resetPwdUrl", resetPwdUrl)};
		return httpPostRequest(requestUrl,params);
	}
	
	public static String httpPostRequest(String requestUrl, NameValuePair[] params) 
	{
		HttpClient httpClient = new HttpClient();

		PostMethod post = new PostMethod(requestUrl);
		post.addParameters(params);
		post.addRequestHeader("connection", "keep-alive");

		try
		{
			int statusCode = httpClient.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK)
			{
				throw new GlobalException("request failed:" + post.getStatusLine());
			}
			String result = new String(post.getResponseBody(), "utf-8");
			return result;

		}
		catch (HttpException e)
		{
			throw new GlobalException("Fatal protocol violation: " + e.getMessage(),e);
		}
		catch (IOException e)
		{
			throw new GlobalException("Fatal transport error: " + e.getMessage(),e);
		}
		finally
		{
			post.releaseConnection();
		}

	}

	public static String httpGetRequest(String url)
	{
		HttpClient httpClient = new HttpClient();

		String requestUrl = url;
		GetMethod get = new GetMethod(requestUrl);
		get.addRequestHeader("connection", "keep-alive");

		try
		{
			int statusCode = httpClient.executeMethod(get);

			if (statusCode != HttpStatus.SC_OK)
			{
				throw new GlobalException("request failed:" + get.getStatusLine());
			}
			
			// 过滤特殊字符 \ufeff
			String responseStr = new String(get.getResponseBody(), "utf-8").replaceAll("[\\ufeff]", "");// ^\\x20-\\x7e    \\ufeff
			return responseStr;

		}
		catch (HttpException e)
		{
			throw new GlobalException("Fatal protocol violation: " + e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new GlobalException("Fatal transport violation: " + e.getMessage(), e);
		}
		finally
		{
			get.releaseConnection();
		}

	}
}
