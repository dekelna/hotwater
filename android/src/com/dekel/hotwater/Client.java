package com.dekel.hotwater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Client {
    HttpClient httpclient = new DefaultHttpClient();
    
    public boolean getCurrentState() throws IOException {
    	HttpGet httpget = new HttpGet(Configuration.SERVER_DOMAIN + "simple");
    	
    	// Execute the request
    	HttpResponse response = httpclient.execute(httpget);    	
    	String state = EntityUtils.toString(response.getEntity());
    	
    	System.out.println("Current state is [" + state + "]");
    	return state.equals("on");
    }
	
    public void postNewState(boolean newStatus) throws IOException {
    	HttpPost httppost = new HttpPost(Configuration.SERVER_DOMAIN);
    	
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("state", newStatus ? "True" : "False"));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        assert(response.getStatusLine().getStatusCode() == 200); // HTTP OK
        
        response.getEntity().consumeContent();
    } 
}
