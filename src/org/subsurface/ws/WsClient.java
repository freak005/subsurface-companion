package org.subsurface.ws;

import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ws.json.DiveParser;
import org.subsurface.ws.json.UserParser;

import android.util.Log;

public class WsClient {

	private static final String TAG = "WsClient";

	protected static final String ACTION_POST_DIVE = "/api/dive/add/";
	protected static final String ACTION_GET_ALL_DIVES = "/api/dive/get/?login=%s";
	protected static final String ACTION_CREATE_USER = "/api/user/new/%s";
	protected static final String ACTION_RESEND_USER = "/api/user/lost/%s";

	private static final String LAST_MODIFIED_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
	
	protected static void prepareRequest(HttpRequestBase request, String url, String action, Date lastModified, Object... parameters) {
		String callUrl = url.endsWith("/") ? url.substring(0, url.length() - 2) : url;
		callUrl += String.format(action, parameters);
		Log.d(TAG, "Calling " + callUrl);
		request.setURI(URI.create(callUrl));
		request.setHeader("Content-type", "application/json");
		request.setHeader("Accept", "application/json");
		if (lastModified != null) {
			request.setHeader("Last-Modified", new SimpleDateFormat(LAST_MODIFIED_DATE_FORMAT, Locale.ENGLISH)
					.format(lastModified));
		}
	}

	public List<DiveLocationLog> getAllDives(String url, String user) throws WsException {
		List<DiveLocationLog> logs;
		InputStream in = null;
		try {
			HttpGet request = new HttpGet();
			prepareRequest(request, url, ACTION_GET_ALL_DIVES, null, user);
			HttpResponse response = new DefaultHttpClient().execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = response.getEntity().getContent();
				logs = DiveParser.parseDives(in);
			} else {
				throw new WsException();
			}
		} catch (Exception e) {
			Log.d(TAG, "getAllDives : error", e);
			throw new WsException();
		} finally { // Close stream
			if (in != null) {
				try {
					in.close();
				} catch (Exception ignored) {}
			}
		}
		return logs;
	}

	public void postDive(DiveLocationLog dive, String url, String user) throws WsException {
		try {
			HttpPost request = new HttpPost();
			Date logDate = new Date(dive.getTimestamp());
			prepareRequest(request, url, ACTION_POST_DIVE, null);
			request.setHeader("Content-type", "application/x-www-form-urlencoded");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("login", user));
			nameValuePairs.add(new BasicNameValuePair("dive_latitude", Double.toString(dive.getLatitude())));
			nameValuePairs.add(new BasicNameValuePair("dive_longitude", Double.toString(dive.getLongitude())));
			nameValuePairs.add(new BasicNameValuePair("dive_date", new SimpleDateFormat("yyyy-MM-dd").format(logDate)));
			nameValuePairs.add(new BasicNameValuePair("dive_time", new SimpleDateFormat("HH:mm").format(logDate)));
			nameValuePairs.add(new BasicNameValuePair("dive_name", dive.getName()));
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			HttpResponse response = new DefaultHttpClient().execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new WsException();
			}
		} catch (Exception e) {
			Log.d(TAG, "postDive : error", e);
			throw new WsException();
		}
	}

	public String createUser(String url, String email) throws WsException {
		String user;
		InputStream in = null;
		try {
			HttpGet request = new HttpGet();
			prepareRequest(request, url, ACTION_CREATE_USER, null, email);
			HttpResponse response = new DefaultHttpClient().execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = response.getEntity().getContent();
				user = UserParser.parseUser(in);
			} else {
				throw new WsException();
			}
		} catch (Exception e) {
			Log.d(TAG, "createUser : error", e);
			throw new WsException();
		} finally { // Close stream
			if (in != null) {
				try {
					in.close();
				} catch (Exception ignored) {}
			}
		}
		return user;
	}

	public void resendUser(String url, String email) throws WsException {
		try {
			HttpGet request = new HttpGet();
			prepareRequest(request, url, ACTION_RESEND_USER, null, email);
			HttpResponse response = new DefaultHttpClient().execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new WsException();
			}
		} catch (Exception e) {
			throw new WsException();
		}
	}
}
