package org.javawing.weixin.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;

public class FormLoginDemo {
	   
	private  CloseableHttpClient client;
	private String cookie;
	private void init(){
		 HttpClientBuilder builder = HttpClientBuilder.create();
		 builder.setRedirectStrategy(new  LaxRedirectStrategy());
		 client =   builder.build();
	}
	
	private String getCookie() throws ClientProtocolException, IOException{
		 HttpGet indexGet = new HttpGet("http://jiaowu.sicau.edu.cn/web/web/web/index.asp");
		 HttpResponse indexRes = client.execute(indexGet);
		 this.cookie = indexRes.getHeaders("Set-Cookie")[0].getValue();
		 indexGet.releaseConnection();
		 return cookie;
	}
	
	private String getbaseDcode() throws IOException{
		  String baseDcode = "";
		  HttpGet jsGet = new HttpGet("http://jiaowu.sicau.edu.cn/jiaoshi/bangong/js/");
		  HttpResponse jsRes = client.execute(jsGet);
		  HttpEntity jsEntity = jsRes.getEntity();
		  
		  InputStream in = jsEntity.getContent();
		  BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		  String temp = reader.readLine();
		  while(temp!=null){
			  if(temp.matches("^dcode2=\\d+$")){
				  baseDcode = temp.replaceAll("dcode2=", "");
				  System.out.println("[Info] basedcode:"+baseDcode);
				  break;
			  }
			  temp = reader.readLine();
		  }
		  jsGet.releaseConnection();
		  return baseDcode;
	}
	
	public void login(String userName,String password) throws IOException{
		  init();
		  getCookie();
		  password = decode(new BigInteger(getbaseDcode()), password);
		  
		  System.out.println("[Info] decode password: "+password);
		  
		  System.out.println("[Info] Login...");
		  List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	      nvps.add(new BasicNameValuePair("user", userName));
	      nvps.add(new BasicNameValuePair("pwd", password));
	      HttpPost httpPost = new HttpPost( "http://jiaowu.sicau.edu.cn/jiaoshi/bangong/check.asp" );
	     
	      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
	      httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.69 Safari/537.36");
	      httpPost.setHeader("Host","jiaowu.sicau.edu.cn");
	      httpPost.setHeader("Cookie", cookie);
	      httpPost.setHeader("Referer","http://jiaowu.sicau.edu.cn/web/web/web/index.asp");
	      HttpResponse response = client.execute(httpPost);
	      if(response.getStatusLine().getStatusCode()==200){
	    	  System.out.println("[Info] Login success.");
	      }
	      HttpEntity entity2 = response.getEntity();
	      
	      InputStream in = entity2.getContent();
	      StringBuilder content = new StringBuilder();
	      int length = -1;
	      byte[] data = new byte[10240];
	      while((length=in.read(data))!=-1){
	    	  content.append(new String(data,0,length));
	      }
	      System.out.println("---------------登录成功后的页面源码-----------------");
	      System.out.println(content);
	      httpPost.releaseConnection();
	      client.close();
	}
	
	private String decode(BigInteger baseDecode,String pwd){
		String dcode = "";
		baseDecode=baseDecode.multiply(new BigInteger("137"));
		String baseDecodeString = baseDecode+"";
		String tmpstr;
		int dcodelen =pwd.length();
		for (int i=1;i<=dcodelen;i++){
			tmpstr = pwd.substring(i-1,i);
			dcode+=(char)((int)tmpstr.charAt(0)-i-Integer.parseInt(baseDecodeString.substring(i-1,i)));
		}
		return dcode;
	}

	public static void main(String[] args) throws Exception{
		new	FormLoginDemo().login("20111921", "8247095");
	}
}


