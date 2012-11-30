/*
 * Copyright 2010-2012 elfCLOUD / elfcloud.fi - SCIS Secure Cloud Infrastructure Services
 *	
 *		Licensed under the Apache License, Version 2.0 (the "License");
 *		you may not use this file except in compliance with the License.
 *		You may obtain a copy of the License at
 *	
 *			http://www.apache.org/licenses/LICENSE-2.0
 *	
 *	   	Unless required by applicable law or agreed to in writing, software
 *	   	distributed under the License is distributed on an "AS IS" BASIS,
 *	   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	   	See the License for the specific language governing permissions and
 *	   	limitations under the License.
 */

package fi.elfcloud.sci;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.elfcloud.sci.exception.ECClientException;
import fi.elfcloud.sci.exception.ECDataItemException;
import fi.elfcloud.sci.exception.ECException;

/**
 * Provides methods for communicating to elfcloud.fi server.
 *
 */
public class Connection {
	private static final String apiVersion = "1.1";
	private static CookieManager manager;
	private static CookieStore cookieJar;
	@SuppressWarnings("unused")
	private static List<HttpCookie> cookies;
	private HttpURLConnection conn;
	private BufferedReader rd;
	private JSONObject json;
	private static Client client;
	private int authTries = 0;
	private static String url = "https://api.elfcloud.fi/";
	
	public Connection(Client client) {
		Connection.client = client;
		Connection.manager = new CookieManager();
		Connection.manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(Connection.manager);
		Connection.cookieJar = manager.getCookieStore();
		Connection.cookies = cookieJar.getCookies();
	}
	
	public Connection () throws ECClientException {
		if (client == null) {
			ECClientException exc = new ECClientException();
			exc.setMessage("Client not initialized");
			throw exc;
		}
	}
	
	/**
	 * Authenticates {@link Client}
	 * @return <code>true</code> on successful authentication, else <code>false</code>.
	 * @throws ECException
	 */
	public boolean auth() throws ECException {
		authTries++;
		if (cookieJar.getCookies().size() > 0) {
			terminateSession();
			cookieJar.removeAll();
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("username", client.getUsername());
		map.put("auth_data", client.getPassword());
		map.put("auth_method", client.getAuthMethod());
		map.put("apikey", client.getApikey());
		try {
			JSONObject response = (JSONObject) sendRequest("auth", map);
			JSONObject client = response.getJSONObject("client");
			JSONArray types = client.getJSONArray("types");
			String[] allowedTypes = new String[types.length()];
			for (int i=0; i < types.length(); i++) {
				allowedTypes[i] = types.getString(i);
			}
			Connection.client.setAllowedTypes(allowedTypes);
			Connection.client.setCurrentUser(new User(response.getJSONObject("user")));
			Connection.client.setAccountAdmin(new User(response.getJSONObject("account_admin")));
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sends JSON request to elfcloud.fi server.
	 * @param method request method.
	 * @param params parameters for the method used.
	 * @return result value(s) returned from server.
	 * @throws ECException
	 */
	public Object sendRequest(String method, Map<String, Object> params) 
			throws ECException {
		String line="", result = "";
		JSONObject response = null;
		json = new JSONObject();
		try {
			json.put("method", method);
			json.put("params", new JSONObject(params));
			URL url = new URL(Connection.url + Connection.apiVersion + "/json");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-type", "application/json; charset=utf-8");
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.getOutputStream().write(json.toString().getBytes("UTF8"));
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
			response = new JSONObject(result);
			if (response.has("error")) {
				if (method.equalsIgnoreCase("term")) {
					return "";
				}
				return handleException(response, method, params);
			}
			
			return response.get("result");
			
		} catch (JSONException e) {
			e.printStackTrace();
			ECClientException exception = new ECClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to elfcloud.fi server");
			throw exception;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			ECClientException exception = new ECClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to elfcloud.fi server");
			throw exception;
		}
		return null;
	}

	/**
	 * Does a fetch operation to elfcloud.fi server.
	 * @param headers request headers to be applied
	 * @return connection to elfcloud.fi server
	 * @throws ECException
	 * @see {@link HttpURLConnection}
	 */
	public HttpURLConnection getData(Map<String, String> headers) 
			throws ECException {
		Object[] keys = headers.keySet().toArray();
		URL url;
		try {
			url = new URL(Connection.url + Connection.apiVersion + "/fetch");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-type", "application/octet-stream; charset=utf-8");
			
			for (int i = 0; i < keys.length; i++) {
				conn.setRequestProperty((String) keys[i], headers.get(keys[i]));
			}
			
			String result = conn.getHeaderField("X-ELFCLOUD-RESULT");
			if (!result.equals("OK")) {
				String[] exceptionData = result.split(" ", 4);
				int exceptionID = Integer.parseInt(exceptionData[1]);
				String message = exceptionData[3];
				if (exceptionID == 101) {
					while (authTries < 3) {
						if (auth()) {
							return getData(headers);
						}
					}
					authTries = 0;
				}
				ECDataItemException exception = new ECDataItemException();
				exception.setId(exceptionID);
				exception.setMessage(message);
				throw exception;
			}
			authTries = 0;
			return conn;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			ECClientException exception = new ECClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to elfcloud.fi server");
			throw exception;
		}
		return null;
	}

	/**
	 * Does a store operation to elfcloud.fi server.
	 * @param headers request headers to be applied
	 * @param dataChunk data to be sent
	 * @param len length of the data
	 * @throws ECException
	 */
	public void sendData(Map<String, String> headers, byte[] dataChunk, int len) 
			throws ECException {
		Object[] keys = headers.keySet().toArray();
		URL url;
		
		try {
			url = new URL(Connection.url + Connection.apiVersion + "/store");
			conn = (HttpURLConnection) url.openConnection();
			for (int i = 0; i < keys.length; i++) {
				conn.setRequestProperty((String) keys[i], headers.get(keys[i]));
			}
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream (
					conn.getOutputStream ());
			wr.write(dataChunk, 0, len);
			String result = conn.getHeaderField("X-ELFCLOUD-RESULT");
			wr.close();
			if (conn.getResponseCode() != 200 || !result.equals("OK")) {
				ECDataItemException exception;
				if (conn.getResponseCode() == 200) {
					String[] exceptionData = result.split(" ", 4);
					int exceptionID = Integer.parseInt(exceptionData[1]);
					String message = exceptionData[3];
					if (exceptionID == 101) {
						cookieJar.removeAll();
						while (authTries < 3) {
							if (auth()) {
								sendData(headers, dataChunk, len);
								return;
							}
						}
						authTries = 0;
					}
					exception = new ECDataItemException();
					exception.setId(exceptionID);
					exception.setMessage(message);
				} else {
					exception = new ECDataItemException();
					exception.setId(conn.getResponseCode());
					exception.setMessage(conn.getResponseMessage());
				}
				throw exception;
			} 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			ECClientException exception = new ECClientException();
			exception.setId(408);
			exception.setMessage("Error connecting to elfcloud.fi server");
			authTries = 0;
			throw exception;
		}

	}
	
	/**
	 * Parses the elfcloud.fi server exception response and wraps it in {@link ECException}.<p>
	 * Tries re-authing in case of authentication failure.
	 * @param response server response to be parsed
	 * @param method original request method
	 * @param params original request parameters
	 * @return in case of authentication failure, tries re-authing and returns the response of original call.
	 * @throws ECException
	 * @throws JSONException
	 */
	public Object handleException(JSONObject response, String method, Map<String, Object> params) 
			throws ECException, JSONException {
		JSONObject exception = response.getJSONObject("error");
		String type = exception.getString("data");
		String message = exception.getString("message");
		int id = exception.getInt("code");
		
		if (id == 101) {
			cookieJar.removeAll();
			while (authTries < 3) {
				auth();
				Object excResponse = sendRequest(method, params);
				authTries = 0;
				return excResponse;
			}
		}
		
		ECException exc = new ECException();
		exc.setId(id);
		exc.setMessage(message);
		exc.setType(type);
		authTries = 0;
		throw exc;
	}
	
	/**
	 * Terminates current session.
	 */
	public void terminateSession() {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			sendRequest("term", map);
			cookieJar.removeAll();
		} catch (ECException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates {@link #url} associated with the {@link Connection}
	 * @param serverUrl new server url
	 */
	public void setServerUrl(String serverUrl) {
		Connection.url = serverUrl;
	}
}
