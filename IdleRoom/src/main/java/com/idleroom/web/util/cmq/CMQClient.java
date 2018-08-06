package com.idleroom.web.util.cmq;

import java.util.TreeMap;
import java.util.Random;
import java.net.URLEncoder;

public class CMQClient {
	protected String CURRENT_VERSION = "SDK_JAVA_1.1";
	
	protected String endpoint;
	protected String path;
	protected String secretId;
	protected String secretKey;
	protected String method;
	
	
	public CMQClient(String endpoint, String path, String secretId, String secretKey, String method){
		this.endpoint = endpoint;
		this.path = path;
		this.secretId = secretId;
		this.secretKey = secretKey;
		this.method = method;
	}
	
	public String call(String action, TreeMap<String,String> param) throws Exception{
		String rsp = "";
		try{
			param.put("Action", action);
			param.put("Nonce", Integer.toString(new Random().nextInt(java.lang.Integer.MAX_VALUE)));
			param.put("SecretId", this.secretId);
			param.put("Timestamp", Long.toString(System.currentTimeMillis() / 1000));
			param.put("RequestClient", this.CURRENT_VERSION);
	
			String host="";
			if(this.endpoint.startsWith("https"))
				host = this.endpoint.substring(8);
			else
				host = this.endpoint.substring(7);
			String src = "";
			src += this.method + host + this.path + "?";
			
			boolean flag = false;
			for(String key: param.keySet()){
				if(flag)
					src += "&";
				//src += key + "=" + param.get(key);
				src += key.replace("_", ".") + "=" + param.get(key);
				flag = true;
			}
			param.put("Signature",CMQTool.sign(src, this.secretKey));
	
			String url = "";
			String req = "";
			if(this.method.equals("GET")){
				url = this.endpoint + this.path + "?";
				flag = false;
				for(String key: param.keySet()){
					if(flag)
						url += "&";
					url += key + "=" + URLEncoder.encode(param.get(key),"utf-8");
					flag = true;
				}
				if(url.length() > 2048)
					throw new CMQClientException("URL length is larger than 2K when use GET method");
			}
			else{
				url = this.endpoint + this.path;
				flag = false;
				for(String key: param.keySet()){
					if(flag)
						req += "&";
					req += key + "=" + URLEncoder.encode(param.get(key),"utf-8");
					flag = true;
				}
			}
			
			//System.out.println("url:"+url);
			
            int userTimeout=0;
			if(param.containsKey("UserpollingWaitSeconds"))
			{
			  userTimeout=Integer.parseInt(param.get("UserpollingWaitSeconds"));
			}
			rsp = CMQHttp.request(this.method, url, req,userTimeout);
			//System.out.println("rsp:"+rsp);
		
		}catch(Exception e){
			throw e;
		}
		return rsp;
	}
}
