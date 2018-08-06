package com.idleroom.web.util.cmq;

import java.util.List;
import java.util.TreeMap;

import com.idleroom.web.util.Json.*;

import java.lang.Integer;


public class Account{
	
	protected CMQClient client;
	
	/**
	 * @brief             
	 * @param secretId    从腾讯云官网查看云api的密钥ID
	 * @param secretKey   从腾讯云官网查看云api的密钥
	 * @param endpoint    消息服务api地址，例如：https://cmq-queue-gz.api.qcloud.com
	 * @param method	  请求方法，可使用GET或POST方法
	 */
	public Account(String endpoint, String secretId, String secretKey){
		this.client = new CMQClient(endpoint, "/v2/index.php", secretId, secretKey, "POST");
	}
	
	/**
	 * @brief             
	 * @param secretId    从腾讯云官网查看云api的密钥ID
	 * @param secretKey   从腾讯云官网查看云api的密钥
	 * @param endpoint    消息服务api地址，例如：https://cmq-queue-gz.api.qcloud.com
	 * @param path		  文件路径
	 * @param method	  请求方法，可使用GET或POST方法
	 */
	public Account(String secretId, String secretKey,String endpoint, String path, String method){
		this.client = new CMQClient(endpoint, path, secretId, secretKey, method);
	}
	
	/**
	 * 创建队列
	 *
	 * @param queueName   队列名字
	 * @param meta        队列属性参数
	 * @throws CMQClientException
	 * @throws CMQServerException
	 */
	public void createQueue(String queueName,QueueMeta meta) throws Exception {
		TreeMap<String, String> param = new TreeMap<String, String>();
		if(queueName.equals(""))
			throw new CMQClientException("Invalid parameter:queueName is empty");
		else
			param.put("queueName",queueName);
		
		if(meta.maxMsgHeapNum > 0)
			param.put("maxMsgHeapNum",Integer.toString(meta.maxMsgHeapNum));
		if(meta.pollingWaitSeconds > 0)
			param.put("pollingWaitSeconds",Integer.toString(meta.pollingWaitSeconds));
		if(meta.visibilityTimeout > 0)
			param.put("visibilityTimeout",Integer.toString(meta.visibilityTimeout));
		if(meta.maxMsgSize > 0)
			param.put("maxMsgSize",Integer.toString(meta.maxMsgSize));
		if(meta.msgRetentionSeconds > 0)
			param.put("msgRetentionSeconds",Integer.toString(meta.msgRetentionSeconds));
		
		String result = this.client.call("CreateQueue", param);
		JSONObject jsonObj = new JSONObject(result);
		int code = jsonObj.getInt("code");
		if(code != 0)
			throw new CMQServerException(code,jsonObj.getString("message"),jsonObj.getString("requestId"));
	}
	
	/**
	 * 删除队列
	 *
	 * @param queueName   队列名字
	 * @throws CMQClientException
	 * @throws CMQServerException
	 */
	public void deleteQueue(String queueName) throws Exception {
		TreeMap<String, String> param = new TreeMap<String, String>();
		if(queueName.equals(""))
			throw new CMQClientException("Invalid parameter:queueName is empty");
		else
			param.put("queueName",queueName);
		
		String result = this.client.call("DeleteQueue", param);
		JSONObject jsonObj = new JSONObject(result);
		int code = jsonObj.getInt("code");
		if(code != 0)
			throw new CMQServerException(code,jsonObj.getString("message"),jsonObj.getString("requestId"));
	}
	
	/**
	 * 列出Account的队列列表
	 *
	 * @param totalCount  返回用户帐号下总队列数
	 * @param vtQueue     返回队列列表
	 * @param searchWord  查询关键字
	 * @param offset      
	 * @param limit
	 * @throws CMQClientException
	 * @throws CMQServerException
	 */
	public int listQueue(String searchWord, int offset, int limit, List<String> queueList) throws Exception {
		TreeMap<String, String> param = new TreeMap<String, String>();
		if(!searchWord.equals(""))
			param.put("searchWord",searchWord);
		if(offset >=0 )
			param.put("offset",Integer.toString(offset));
		if(limit > 0 )
			param.put("limit",Integer.toString(limit));
		
		String result = this.client.call("ListQueue", param);
		JSONObject jsonObj = new JSONObject(result);
		int code = jsonObj.getInt("code");
		if(code != 0)
			throw new CMQServerException(code,jsonObj.getString("message"),jsonObj.getString("requestId"));
		
		int totalCount = jsonObj.getInt("totalCount");
		JSONArray jsonArray = jsonObj.getJSONArray("queueList");
		for(int i=0;i<jsonArray.length();i++)
		{	
			JSONObject obj = (JSONObject)jsonArray.get(i);
			queueList.add(obj.getString("queueName"));
		}
		
		return totalCount;
	}
	
	/**
	 * 获取Account的一个Queue对象
	 *
	 */
	public Queue getQueue(String queueName){
	    return new Queue(queueName,this.client);
    }
}
