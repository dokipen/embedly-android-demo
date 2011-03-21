package com.embedly.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

class ApiParameters {
    private Map<String, ArrayList<String>> params;

    public ApiParameters() {
        params = new HashMap<String, ArrayList<String>>();
    }

    public void push(String name, String value) {
        name = filterName(name);
        ArrayList<String> param = getParam(name);
        param.add(value);
    }

    public void push(String name, String[] value) {
        name = filterName(name);
        ArrayList<String> param = getParam(name);
        param.addAll(Arrays.asList(value));
    }

    public ArrayList<String> getParam(String name) {
        name = filterName(name);
        ArrayList<String> param = params.get(name);
        if (param == null) {
            param = new ArrayList<String>();
            params.put(name, param);
        }
        return param;
    }

    private String filterName(String name) {
        if ("url".equals(name)) {
            return "urls";
        }
        return name;
    }

    public String toQuery() throws UnsupportedEncodingException {
        ArrayList<String> query = new ArrayList<String>();
        for (Map.Entry<String, ArrayList<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                query.add(URLEncoder.encode(key, "utf-8") + 
                        "=" + URLEncoder.encode(value, "utf-8"));
            }
        }

        return stringJoin(query, "&");
    }

    public String toString() {
        return "com.embedly.api.ApiParameters["+this.params+"]";
    }

    
    private String stringJoin(ArrayList<String> parts, String seperator) {
    	StringBuffer buffer = new StringBuffer();
    	for (int i = 0; i < parts.size() - 1; ++i) {
    		buffer.append(parts.get(i));
    		buffer.append(seperator);
    	}
    	buffer.append(parts.get(parts.size() - 1));
    	return buffer.toString();
    }
}
