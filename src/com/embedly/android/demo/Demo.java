package com.embedly.android.demo;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.embedly.api.Api;

public class Demo extends Activity {
	private Api api = new Api("Embedly-Android-Demo");
	private TextView embedResult;
	private Button embedButton;
	private EditText embedUrl;
	private WebView embedWebView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	embedResult = (TextView) findViewById(R.id.embedResult);
    	embedButton = (Button) findViewById(R.id.embedButton);
    	embedUrl = (EditText) findViewById(R.id.queryUrl);
    	embedWebView = (WebView) findViewById(R.id.embedWebView);
    	embedWebView.getSettings().setJavaScriptEnabled(true);
    	embedWebView.getSettings().setPluginsEnabled(true);
    	
        embedButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				fetchEmbed();
			}
		});
    }
    
    public void fetchEmbed() {
    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	mgr.hideSoftInputFromWindow(embedUrl.getWindowToken(), 0);
    	String url = embedUrl.getText().toString();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("url", url);
        params.put("maxwidth", "300");
        JSONArray result = api.oembed(params);

        try {
            JSONObject obj = result.getJSONObject(0);
            String type = obj.getString("type");
    		StringBuffer embedBuf = new StringBuffer();
    		
            if ("photo".equals(type)) {
            	if (!obj.isNull("url")) {
            		embedBuf.append("<div>");
            		embedBuf.append("<center><img width=\"100%25\" src=\"");
            		embedBuf.append(obj.getString("url"));
            		embedBuf.append("\"/></center></div>");
            	}
            } else if ("video".equals(type)) {
	            if (!obj.isNull("html")) {
	            	embedBuf.append(obj.getString("html"));
	            }
            } else if ("rich".equals(type)) {
	            if (!obj.isNull("html")) {
	            	embedBuf.append(obj.getString("html"));
	            }
            } else if ("link".equals(type)) {
            	embedBuf.append("<a href=\"");
            	embedBuf.append(obj.getString("url"));
            	embedBuf.append("\">");
	            if (!obj.isNull("title")) {
	            	embedBuf.append(obj.getString("title"));
	            } else {
	            	embedBuf.append(obj.getString("url"));
	            }
	            embedBuf.append("</a>");
            } else if ("error".equals(type)) {
            	embedBuf.append("<p>");
            	embedBuf.append(obj.getString("error_message"));
            	embedBuf.append("</p>");
            }
    		
            embedWebView.loadData(embedBuf.toString(), "text/html", 
				"utf-8");
            embedResult.setText(embedBuf.toString());
        } catch(JSONException e) {
        	throw new RuntimeException("Couldn't parse response", e);
        }
    }
}