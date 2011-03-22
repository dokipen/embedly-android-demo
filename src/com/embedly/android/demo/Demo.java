package com.embedly.android.demo;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
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

	private final static String LOADING = "<table width=100%25 height=100%25>"
			+ "<tr><td valign=middle align=center><img "
			+ "src=\"http://static.embed.ly/images/android-loader.gif\"/>"
			+ "</td></tr></table>";

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

		registerForContextMenu(embedResult);
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setText("http://www.flickr.com/photos/silent928/5550274021/");
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			Class.forName("android.webkit.WebView")
					.getMethod("onPause", (Class[]) null)
					.invoke(embedWebView, (Object[]) null);
			embedWebView.resumeTimers();
		} catch (Exception e) {
			throw new RuntimeException("Unknown issue when killing flash", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			Class.forName("android.webkit.WebView")
					.getMethod("onResume", (Class[]) null)
					.invoke(embedWebView, (Object[]) null);
			embedWebView.resumeTimers();
		} catch (Exception e) {
			throw new RuntimeException("Unknown issue when resuming flash", e);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.result, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.copy_result:
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(embedResult.getText());
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void onEmbedResponse(JSONArray response) {
		try {
			JSONObject obj = response.getJSONObject(0);
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

			embedWebView.loadData(embedBuf.toString(), "text/html", "utf-8");
			embedResult.setText(embedBuf.toString());
		} catch (JSONException e) {
			throw new RuntimeException("Couldn't parse response", e);
		}
	}

	public void fetchEmbed() {

		final Handler handler = new Handler();
		final Demo self = this;

		handler.post(new Runnable() {

			public void run() {
				embedWebView.loadData(LOADING, "text/html", "utf-8");
				embedResult.setText("");

				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(embedUrl.getWindowToken(), 0);
			}
		});

		String url = embedUrl.getText().toString();
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("url", url);
		params.put("maxwidth", "300");

		new Thread() {
			@Override
			public void run() {
				final JSONArray response = api.oembed(params);
				handler.post(new Runnable() {
					public void run() {
						self.onEmbedResponse(response);
					}
				});
			}
		}.start();

	}
}